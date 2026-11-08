package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.model.AuditLogRecord
import com.example.data.model.BranchProfile
import com.example.data.model.CatalogRecord
import com.example.data.model.MoneyBookmarkRecord
import com.example.data.model.MoneyTransactionRecord
import com.example.data.model.MoneyTransactionType
import com.example.data.model.OrderRecord
import com.example.data.model.StaffAccount
import com.example.data.model.StaffRole
import com.example.data.model.BankAccountDetail
import com.example.data.model.DigitalWalletDetail
import com.example.data.model.WaffleoSeedData
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Penyimpanan Persisten Lokal (Offline-First Engine) & Pemantau Jaringan Otomatis.
 *
 * Menjamin:
 * 1. Aplikasi berjalan 100% secara Offline saat tidak ada internet (data tersimpan di memori internal perangkat).
 * 2. Antrean Sinkronisasi Idempoten (`pendingSyncKeys`) mencegah double-input saat koneksi kembali menyala.
 * 3. Otorisasi 1x Akun Google per Cabang (`authorizedBranches`) agar staff cukup login 1x per cabang di perangkat.
 * 4. Deteksi Anti-Manipulasi / Reset Storage (`highestSequencePerBranch`) melalui pelacakan nomor urut monotonik.
 */
class WaffleoLocalStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isNetworkOnline = MutableStateFlow(checkInitialOnlineState())
    val isNetworkOnline: StateFlow<Boolean> = _isNetworkOnline.asStateFlow()

    private var onNetworkRestoredCallback: (() -> Unit)? = null

    init {
        performOneTimeProductionCleanIfNeeded()
        registerNetworkCallback()
    }

    /**
     * Membersihkan seluruh data dummy lokal (Pesanan, Transaksi Keuangan, Bookmark, Promo, Audit Log)
     * satu kali saat persiapan rilis v1.0, sambil mempertahankan seluruh Kategori, Sub-Kategori,
     * dan Item pada Menu & Modifier (beserta konfigurasi cabang/staff/bank/wallet).
     */
    private fun performOneTimeProductionCleanIfNeeded() {
        if (prefs.getBoolean(KEY_PRODUCTION_CLEAN_V1_DONE, false)) return
        val existingCatalog = loadCatalog()
        if (existingCatalog != null) {
            val cleanedCatalog = existingCatalog.filter { rec ->
                rec.recordType in listOf("MENU_ITEM", "MODIFIER", "BRANCH_CONFIG") &&
                    rec.id !in WaffleoSeedData.legacyDummyCatalogIds
            }
            saveCatalog(cleanedCatalog)
        }
        saveOrders(emptyList())
        saveMoneyTransactions(emptyList())
        saveMoneyBookmarks(emptyList())
        saveAuditLogs(emptyList())
        saveRecappedOrderIds(emptySet())
        savePendingSyncKeys(emptySet())
        prefs.edit()
            .remove(KEY_ACTIVE_SESSION)
            .remove(KEY_HIGHEST_ORDER_SEQ)
            .putBoolean(KEY_PRODUCTION_CLEAN_V1_DONE, true)
            .apply()
    }

    fun setOnNetworkRestoredListener(listener: () -> Unit) {
        onNetworkRestoredCallback = listener
    }

    private fun checkInitialOnlineState(): Boolean {
        return try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val activeNet = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNet) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true
        }
    }

    private fun registerNetworkCallback() {
        try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(
                request,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        val wasOffline = !_isNetworkOnline.value
                        _isNetworkOnline.value = true
                        if (wasOffline) {
                            onNetworkRestoredCallback?.invoke()
                        }
                    }

                    override fun onLost(network: Network) {
                        _isNetworkOnline.value = checkInitialOnlineState()
                    }
                }
            )
        } catch (e: Exception) {
            // Fallback for unit test / Robolectric environments
            _isNetworkOnline.value = true
        }
    }

    // --- 1-TIME GOOGLE AUTH PER BRANCH ---

    fun isBranchGoogleAuthorized(branchId: String): Boolean {
        val clean = branchId.trim().lowercase()
        val map = loadAuthorizedBranchesMap()
        return map.containsKey(clean)
    }

    fun getBranchGoogleAccountLabel(branchId: String): String? {
        val clean = branchId.trim().lowercase()
        return loadAuthorizedBranchesMap()[clean]
    }

    fun markBranchGoogleAuthorized(branchId: String, googleAccountLabel: String) {
        val clean = branchId.trim().lowercase()
        val map = loadAuthorizedBranchesMap().toMutableMap()
        map[clean] = googleAccountLabel.ifBlank { "Akun Google Cabang ($branchId)" }
        val json = JSONObject()
        map.forEach { (k, v) -> json.put(k, v) }
        prefs.edit().putString(KEY_AUTHORIZED_BRANCHES, json.toString()).apply()
    }

    private fun loadAuthorizedBranchesMap(): Map<String, String> {
        val raw = prefs.getString(KEY_AUTHORIZED_BRANCHES, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val res = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                res[k] = obj.optString(k, "")
            }
            res
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // --- PENDING OFFLINE SYNC QUEUE (IDEMPOTENT KEYS) ---

    fun loadPendingSyncKeys(): Set<String> {
        val raw = prefs.getString(KEY_PENDING_SYNC_KEYS, null) ?: return emptySet()
        return try {
            val arr = JSONArray(raw)
            val set = LinkedHashSet<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optString(i, "")
                if (item.isNotBlank()) set.add(item)
            }
            set
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun savePendingSyncKeys(keys: Set<String>) {
        val arr = JSONArray()
        keys.forEach { arr.put(it) }
        prefs.edit().putString(KEY_PENDING_SYNC_KEYS, arr.toString()).apply()
    }

    // --- MONOTONIC SEQUENCE TRACKER (ANTI-MANIPULATION / STORAGE RESET DETECTION) ---

    private fun normalizeBranchKey(branchId: String): String {
        return branchId.trim().lowercase().removePrefix("cabang ").replace("_", " ").trim()
    }

    fun getHighestRecordedOrderSeq(branchId: String): Long {
        val clean = normalizeBranchKey(branchId)
        val raw = prefs.getString(KEY_HIGHEST_ORDER_SEQ, null) ?: return 0L
        return try {
            JSONObject(raw).optLong(clean, 0L)
        } catch (e: Exception) {
            0L
        }
    }

    fun updateHighestRecordedOrderSeq(branchId: String, sequenceIndex: Long) {
        val clean = normalizeBranchKey(branchId)
        val obj = try {
            val raw = prefs.getString(KEY_HIGHEST_ORDER_SEQ, null)
            if (raw != null) JSONObject(raw) else JSONObject()
        } catch (e: Exception) {
            JSONObject()
        }
        val current = obj.optLong(clean, 0L)
        if (sequenceIndex > current) {
            obj.put(clean, sequenceIndex)
            prefs.edit().putString(KEY_HIGHEST_ORDER_SEQ, obj.toString()).apply()
        }
    }

    fun getHighestRecordedDailyOrderSeq(branchId: String, dateStr: String): Long {
        val clean = normalizeBranchKey(branchId)
        val key = "${clean}_$dateStr"
        val raw = prefs.getString("KEY_HIGHEST_DAILY_ORDER_SEQ", null) ?: return 0L
        return try {
            JSONObject(raw).optLong(key, 0L)
        } catch (e: Exception) {
            0L
        }
    }

    fun updateHighestRecordedDailyOrderSeq(branchId: String, dateStr: String, sequenceIndex: Long) {
        val clean = normalizeBranchKey(branchId)
        val key = "${clean}_$dateStr"
        val obj = try {
            val raw = prefs.getString("KEY_HIGHEST_DAILY_ORDER_SEQ", null)
            if (raw != null) JSONObject(raw) else JSONObject()
        } catch (e: Exception) {
            JSONObject()
        }
        val current = obj.optLong(key, 0L)
        if (sequenceIndex > current) {
            obj.put(key, sequenceIndex)
            prefs.edit().putString("KEY_HIGHEST_DAILY_ORDER_SEQ", obj.toString()).apply()
        }
    }

    // --- ORDERS PERSISTENCE ---

    fun saveOrders(orders: List<OrderRecord>) {
        val arr = JSONArray()
        orders.forEach { ord ->
            arr.put(
                JSONObject().apply {
                    put("id", ord.id)
                    put("userId", ord.userId)
                    put("orderNumber", ord.orderNumber)
                    put("sequenceIndex", ord.sequenceIndex)
                    put("branchId", ord.branchId)
                    put("orderMedia", ord.orderMedia)
                    put("customerData", ord.customerData)
                    put("itemsSummary", ord.itemsSummary)
                    put("itemsPayload", ord.itemsPayload)
                    put("notes", ord.notes)
                    put("promoCode", ord.promoCode)
                    put("subtotalAmount", ord.subtotalAmount)
                    put("discountAmount", ord.discountAmount)
                    put("totalAmount", ord.totalAmount)
                    put("paidAmount", ord.paidAmount)
                    put("cashReceivedAmount", ord.cashReceivedAmount)
                    put("paymentMethod", ord.paymentMethod)
                    put("isPaid", ord.isPaid)
                    put("isPrepared", ord.isPrepared)
                    put("status", ord.status)
                    put("cancelReason", ord.cancelReason)
                    put("cancelledBy", ord.cancelledBy)
                    put("cancelledAtText", ord.cancelledAtText)
                    put("shortfallAmount", ord.shortfallAmount)
                    put("changeDueAmount", ord.changeDueAmount)
                    put("splitCashAmount", ord.splitCashAmount)
                    put("splitQrisAmount", ord.splitQrisAmount)
                    put("createdBy", ord.createdBy)
                    put("paymentLedgerPayload", ord.paymentLedgerPayload)
                    put("orderTimelinePayload", ord.orderTimelinePayload)
                    put("createdAtMs", ord.createdAt?.toDate()?.time ?: System.currentTimeMillis())
                    put("updatedAtMs", ord.updatedAt?.toDate()?.time ?: System.currentTimeMillis())
                }
            )
        }
        prefs.edit().putString(KEY_ORDERS, arr.toString()).apply()
    }

    fun loadOrders(): List<OrderRecord>? {
        val raw = prefs.getString(KEY_ORDERS, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<OrderRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val createdMs = obj.optLong("createdAtMs", System.currentTimeMillis())
                val updatedMs = obj.optLong("updatedAtMs", createdMs)
                list.add(
                    OrderRecord(
                        id = obj.optString("id", ""),
                        userId = obj.optString("userId", ""),
                        orderNumber = obj.optString("orderNumber", ""),
                        sequenceIndex = obj.optLong("sequenceIndex", 0L),
                        branchId = obj.optString("branchId", "Global"),
                        orderMedia = obj.optString("orderMedia", "Walk In"),
                        customerData = obj.optString("customerData", ""),
                        itemsSummary = obj.optString("itemsSummary", ""),
                        itemsPayload = obj.optString("itemsPayload", "[]"),
                        notes = obj.optString("notes", ""),
                        promoCode = obj.optString("promoCode", ""),
                        subtotalAmount = obj.optLong("subtotalAmount", 0L),
                        discountAmount = obj.optLong("discountAmount", 0L),
                        totalAmount = obj.optLong("totalAmount", 0L),
                        paidAmount = obj.optLong("paidAmount", 0L),
                        cashReceivedAmount = obj.optLong("cashReceivedAmount", 0L),
                        paymentMethod = obj.optString("paymentMethod", "Belum Bayar"),
                        isPaid = obj.optBoolean("isPaid", false),
                        isPrepared = obj.optBoolean("isPrepared", false),
                        status = obj.optString("status", "ACTIVE"),
                        cancelReason = obj.optString("cancelReason", ""),
                        cancelledBy = obj.optString("cancelledBy", ""),
                        cancelledAtText = obj.optString("cancelledAtText", ""),
                        shortfallAmount = obj.optLong("shortfallAmount", 0L),
                        changeDueAmount = obj.optLong("changeDueAmount", 0L),
                        splitCashAmount = obj.optLong("splitCashAmount", 0L),
                        splitQrisAmount = obj.optLong("splitQrisAmount", 0L),
                        createdBy = obj.optString("createdBy", "owner"),
                        paymentLedgerPayload = obj.optString("paymentLedgerPayload", "[]"),
                        orderTimelinePayload = obj.optString("orderTimelinePayload", "[]"),
                        createdAt = Timestamp(Date(createdMs)),
                        updatedAt = Timestamp(Date(updatedMs))
                    )
                )
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    // --- CATALOG PERSISTENCE ---

    fun saveCatalog(records: List<CatalogRecord>) {
        val arr = JSONArray()
        records.forEach { rec ->
            arr.put(
                JSONObject().apply {
                    put("id", rec.id)
                    put("userId", rec.userId)
                    put("recordType", rec.recordType)
                    put("branchId", rec.branchId)
                    put("name", rec.name)
                    put("category", rec.category)
                    put("priceWalkIn", rec.priceWalkIn)
                    put("priceGrabFood", rec.priceGrabFood)
                    put("priceGoFood", rec.priceGoFood)
                    put("priceShopeeFood", rec.priceShopeeFood)
                    put("stockQty", rec.stockQty)
                    put("stockUnit", rec.stockUnit)
                    put("costPerUnit", rec.costPerUnit)
                    put("metadataJson", rec.metadataJson)
                    put("imageUrl", rec.imageUrl)
                    put("isActive", rec.isActive)
                }
            )
        }
        prefs.edit().putString(KEY_CATALOG, arr.toString()).apply()
    }

    fun loadCatalog(): List<CatalogRecord>? {
        val raw = prefs.getString(KEY_CATALOG, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<CatalogRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    CatalogRecord(
                        id = obj.optString("id", ""),
                        userId = obj.optString("userId", ""),
                        recordType = obj.optString("recordType", "MENU_ITEM"),
                        branchId = obj.optString("branchId", "Global"),
                        name = obj.optString("name", ""),
                        category = obj.optString("category", ""),
                        priceWalkIn = obj.optLong("priceWalkIn", 0L),
                        priceGrabFood = obj.optLong("priceGrabFood", 0L),
                        priceGoFood = obj.optLong("priceGoFood", 0L),
                        priceShopeeFood = obj.optLong("priceShopeeFood", 0L),
                        stockQty = obj.optDouble("stockQty", 0.0),
                        stockUnit = obj.optString("stockUnit", "gram"),
                        costPerUnit = obj.optLong("costPerUnit", 0L),
                        metadataJson = obj.optString("metadataJson", "{}"),
                        imageUrl = obj.optString("imageUrl", ""),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    // --- MONEY TRANSACTIONS & BOOKMARKS PERSISTENCE ---

    fun saveMoneyTransactions(list: List<MoneyTransactionRecord>) {
        val arr = JSONArray()
        list.forEach { tx ->
            arr.put(
                JSONObject().apply {
                    put("id", tx.id)
                    put("branchId", tx.branchId)
                    put("type", tx.type.name)
                    put("timestampMillis", tx.timestampMillis)
                    put("dateKey", tx.dateKey)
                    put("monthKey", tx.monthKey)
                    put("timeFormatted", tx.timeFormatted)
                    put("amount", tx.amount)
                    put("transferFee", tx.transferFee)
                    put("category", tx.category)
                    put("subCategory", tx.subCategory)
                    put("accountGroup", tx.accountGroup)
                    put("accountName", tx.accountName)
                    put("toAccountGroup", tx.toAccountGroup)
                    put("toAccountName", tx.toAccountName)
                    put("note", tx.note)
                    put("description", tx.description)
                    put("tag", tx.tag)
                    put("repetition", tx.repetition)
                    put("isAutoPosSync", tx.isAutoPosSync)
                    put("posSyncKey", tx.posSyncKey)
                    put("createdBy", tx.createdBy)
                }
            )
        }
        prefs.edit().putString(KEY_MONEY_TX, arr.toString()).apply()
    }

    fun loadMoneyTransactions(): List<MoneyTransactionRecord>? {
        val raw = prefs.getString(KEY_MONEY_TX, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<MoneyTransactionRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val typeName = obj.optString("type", MoneyTransactionType.EXPENSE.name)
                val txType = try {
                    MoneyTransactionType.valueOf(typeName)
                } catch (e: Exception) {
                    MoneyTransactionType.EXPENSE
                }
                list.add(
                    MoneyTransactionRecord(
                        id = obj.optString("id", ""),
                        branchId = obj.optString("branchId", "Global"),
                        type = txType,
                        timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                        dateKey = obj.optString("dateKey", ""),
                        monthKey = obj.optString("monthKey", ""),
                        timeFormatted = obj.optString("timeFormatted", "08:00"),
                        amount = obj.optLong("amount", 0L),
                        transferFee = obj.optLong("transferFee", 0L),
                        category = obj.optString("category", ""),
                        subCategory = obj.optString("subCategory", ""),
                        accountGroup = obj.optString("accountGroup", "Uang Tunai"),
                        accountName = obj.optString("accountName", "Cash"),
                        toAccountGroup = obj.optString("toAccountGroup", ""),
                        toAccountName = obj.optString("toAccountName", ""),
                        note = obj.optString("note", ""),
                        description = obj.optString("description", ""),
                        tag = obj.optString("tag", ""),
                        repetition = obj.optString("repetition", "None"),
                        isAutoPosSync = obj.optBoolean("isAutoPosSync", false),
                        posSyncKey = obj.optString("posSyncKey", ""),
                        createdBy = obj.optString("createdBy", "owner")
                    )
                )
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    fun saveMoneyBookmarks(list: List<MoneyBookmarkRecord>) {
        val arr = JSONArray()
        list.forEach { bm ->
            arr.put(
                JSONObject().apply {
                    put("id", bm.id)
                    put("branchId", bm.branchId)
                    put("title", bm.title)
                    put("type", bm.type.name)
                    put("amount", bm.amount)
                    put("category", bm.category)
                    put("subCategory", bm.subCategory)
                    put("accountGroup", bm.accountGroup)
                    put("accountName", bm.accountName)
                    put("toAccountGroup", bm.toAccountGroup)
                    put("toAccountName", bm.toAccountName)
                    put("note", bm.note)
                    put("description", bm.description)
                    put("tag", bm.tag)
                }
            )
        }
        prefs.edit().putString(KEY_MONEY_BOOKMARKS, arr.toString()).apply()
    }

    fun loadMoneyBookmarks(): List<MoneyBookmarkRecord>? {
        val raw = prefs.getString(KEY_MONEY_BOOKMARKS, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<MoneyBookmarkRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val typeName = obj.optString("type", MoneyTransactionType.EXPENSE.name)
                val txType = try {
                    MoneyTransactionType.valueOf(typeName)
                } catch (e: Exception) {
                    MoneyTransactionType.EXPENSE
                }
                list.add(
                    MoneyBookmarkRecord(
                        id = obj.optString("id", ""),
                        branchId = obj.optString("branchId", "Global"),
                        title = obj.optString("title", ""),
                        type = txType,
                        amount = obj.optLong("amount", 0L),
                        category = obj.optString("category", ""),
                        subCategory = obj.optString("subCategory", ""),
                        accountGroup = obj.optString("accountGroup", "Uang Tunai"),
                        accountName = obj.optString("accountName", "Cash"),
                        toAccountGroup = obj.optString("toAccountGroup", ""),
                        toAccountName = obj.optString("toAccountName", ""),
                        note = obj.optString("note", ""),
                        description = obj.optString("description", ""),
                        tag = obj.optString("tag", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    // --- STAFF ACCOUNTS & BRANCHES PERSISTENCE ---

    fun saveStaffAccounts(accounts: List<StaffAccount>) {
        val arr = JSONArray()
        accounts.forEach { acc ->
            arr.put(
                JSONObject().apply {
                    put("branchId", acc.branchId)
                    put("username", acc.username)
                    put("password", acc.password)
                    put("role", acc.role.name)
                    put("displayName", acc.displayName)
                    put("email", acc.email)
                    put("phone", acc.phone)
                }
            )
        }
        prefs.edit().putString(KEY_STAFF_ACCOUNTS, arr.toString()).apply()
    }

    fun loadStaffAccounts(): List<StaffAccount>? {
        val raw = prefs.getString(KEY_STAFF_ACCOUNTS, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<StaffAccount>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val roleName = obj.optString("role", StaffRole.KASIR.name)
                val role = try {
                    StaffRole.valueOf(roleName)
                } catch (e: Exception) {
                    StaffRole.KASIR
                }
                list.add(
                    StaffAccount(
                        branchId = obj.optString("branchId", "Global"),
                        username = obj.optString("username", ""),
                        password = obj.optString("password", ""),
                        role = role,
                        displayName = obj.optString("displayName", ""),
                        email = obj.optString("email", ""),
                        phone = obj.optString("phone", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    fun saveBranchesAndProfiles(branches: List<String>, profiles: List<BranchProfile>) {
        val branchArr = JSONArray()
        branches.forEach { branchArr.put(it) }
        val profArr = JSONArray()
        profiles.forEach { p ->
            profArr.put(
                JSONObject().apply {
                    put("branchId", p.branchId)
                    put("name", p.name)
                    put("location", p.location)
                    put("ownerName", p.ownerName)
                    put("ownerEmail", p.ownerEmail)
                    put("ownerPhone", p.ownerPhone)
                    put("ownerUsername", p.ownerUsername)
                    put("initializedFrom", p.initializedFrom)
                    put("createdAtText", p.createdAtText)
                }
            )
        }
        prefs.edit()
            .putString(KEY_BRANCHES, branchArr.toString())
            .putString(KEY_BRANCH_PROFILES, profArr.toString())
            .apply()
    }

    fun loadBranches(): List<String>? {
        val raw = prefs.getString(KEY_BRANCHES, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val b = arr.optString(i, "")
                if (b.isNotBlank()) list.add(b)
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    fun loadBranchProfiles(): List<BranchProfile>? {
        val raw = prefs.getString(KEY_BRANCH_PROFILES, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<BranchProfile>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val bId = obj.optString("branchId", "Global")
                val rawName = obj.optString("name", "")
                list.add(
                    BranchProfile(
                        branchId = bId,
                        name = com.example.data.model.formatCleanBranchName(rawName, bId),
                        location = obj.optString("location", ""),
                        ownerName = obj.optString("ownerName", ""),
                        ownerEmail = obj.optString("ownerEmail", ""),
                        ownerPhone = obj.optString("ownerPhone", ""),
                        ownerUsername = obj.optString("ownerUsername", "owner"),
                        initializedFrom = com.example.data.model.sanitizeBranchIdsInText(obj.optString("initializedFrom", "Master Seed")),
                        createdAtText = obj.optString("createdAtText", "Default System")
                    )
                )
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    // --- AUDIT LOGS PERSISTENCE ---

    fun saveAuditLogs(logs: List<AuditLogRecord>) {
        val arr = JSONArray()
        logs.take(500).forEach { log ->
            arr.put(
                JSONObject().apply {
                    put("id", log.id)
                    put("userId", log.userId)
                    put("branchId", log.branchId)
                    put("actorUsername", log.actorUsername)
                    put("actorRole", log.actorRole)
                    put("actionType", log.actionType)
                    put("entityId", log.entityId)
                    put("summary", log.summary)
                    put("beforeSnapshot", log.beforeSnapshot)
                    put("afterSnapshot", log.afterSnapshot)
                    put("createdAtMs", log.createdAt?.toDate()?.time ?: System.currentTimeMillis())
                }
            )
        }
        prefs.edit().putString(KEY_AUDIT_LOGS, arr.toString()).apply()
    }

    fun loadAuditLogs(): List<AuditLogRecord>? {
        val raw = prefs.getString(KEY_AUDIT_LOGS, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<AuditLogRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val ms = obj.optLong("createdAtMs", System.currentTimeMillis())
                list.add(
                    AuditLogRecord(
                        id = obj.optString("id", ""),
                        userId = obj.optString("userId", ""),
                        branchId = obj.optString("branchId", "Global"),
                        actorUsername = obj.optString("actorUsername", "owner"),
                        actorRole = obj.optString("actorRole", "Owner"),
                        actionType = obj.optString("actionType", ""),
                        entityId = obj.optString("entityId", ""),
                        summary = obj.optString("summary", ""),
                        beforeSnapshot = obj.optString("beforeSnapshot", "{}"),
                        afterSnapshot = obj.optString("afterSnapshot", "{}"),
                        createdAt = Timestamp(Date(ms))
                    )
                )
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    // --- CUSTOM CATEGORIES PERSISTENCE ---

    fun saveCustomCategories(menuCategories: List<String>, modifierCategories: List<String>) {
        val menuArr = JSONArray()
        menuCategories.filter { it.isNotBlank() }.distinctBy { it.lowercase() }.forEach { menuArr.put(it) }
        val modArr = JSONArray()
        modifierCategories.filter { it.isNotBlank() }.distinctBy { it.lowercase() }.forEach { modArr.put(it) }
        prefs.edit()
            .putString(KEY_CUSTOM_MENU_CATEGORIES, menuArr.toString())
            .putString(KEY_CUSTOM_MODIFIER_CATEGORIES, modArr.toString())
            .apply()
    }

    fun loadCustomMenuCategories(): List<String>? {
        val raw = prefs.getString(KEY_CUSTOM_MENU_CATEGORIES, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val str = arr.optString(i)
                if (str.isNotBlank()) list.add(str)
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    fun loadCustomModifierCategories(): List<String>? {
        val raw = prefs.getString(KEY_CUSTOM_MODIFIER_CATEGORIES, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val str = arr.optString(i)
                if (str.isNotBlank()) list.add(str)
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    // --- TRANSFER FEE ACCOUNT CHOICE PERSISTENCE ---

    fun saveTransferFeeAccountChoice(choice: String) {
        prefs.edit().putString(KEY_TRANSFER_FEE_ACCOUNT_CHOICE, choice).apply()
    }

    fun loadTransferFeeAccountChoice(): String {
        return prefs.getString(KEY_TRANSFER_FEE_ACCOUNT_CHOICE, "FROM") ?: "FROM"
    }

    // --- RECAPPED ORDER IDS PERSISTENCE ---

    fun saveRecappedOrderIds(ids: Set<String>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        prefs.edit().putString(KEY_RECAPPED_ORDER_IDS, arr.toString()).apply()
    }

    fun loadRecappedOrderIds(): Set<String> {
        val raw = prefs.getString(KEY_RECAPPED_ORDER_IDS, null) ?: return emptySet()
        return try {
            val arr = JSONArray(raw)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                val str = arr.optString(i)
                if (str.isNotBlank()) set.add(str)
            }
            set
        } catch (_: Exception) {
            emptySet()
        }
    }

    // --- CUSTOM SUB-CATEGORIES PERSISTENCE ---

    fun saveCustomSubCategories(map: Map<String, List<String>>) {
        val obj = JSONObject()
        map.forEach { (cat, subs) ->
            val arr = JSONArray()
            subs.distinct().filter { it.isNotBlank() }.forEach { arr.put(it) }
            obj.put(cat, arr)
        }
        prefs.edit().putString(KEY_CUSTOM_SUB_CATEGORIES, obj.toString()).apply()
    }

    fun loadCustomSubCategories(): Map<String, List<String>>? {
        val raw = prefs.getString(KEY_CUSTOM_SUB_CATEGORIES, null) ?: return null
        return try {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, List<String>>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val arr = obj.optJSONArray(key) ?: JSONArray()
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i)
                    if (s.isNotBlank()) list.add(s)
                }
                map[key] = list
            }
            map
        } catch (_: Exception) {
            null
        }
    }

    fun saveStoreOpenStatus(branchId: String, isOpen: Boolean) {
        prefs.edit().putBoolean("${KEY_STORE_OPEN_PREFIX}_$branchId", isOpen).apply()
    }

    fun loadStoreOpenStatus(branchId: String): Boolean? {
        val key = "${KEY_STORE_OPEN_PREFIX}_$branchId"
        return if (prefs.contains(key)) prefs.getBoolean(key, true) else null
    }

    fun purgeBranchDataLocally(targetBranchId: String) {
        if (targetBranchId.isBlank() || targetBranchId.equals("Global", ignoreCase = true)) return
        val lowTarget = targetBranchId.trim().lowercase()
        val baseSlug = lowTarget.removePrefix("cabang ").trim()

        fun matchesTarget(branch: String): Boolean {
            val low = branch.trim().lowercase()
            return low == lowTarget || low == baseSlug || low.removePrefix("cabang ").trim() == baseSlug
        }

        // 1. Purge orders
        val orders = loadOrders()?.filterNot { matchesTarget(it.branchId) }
        if (orders != null) saveOrders(orders)

        // 2. Purge catalog
        val catalog = loadCatalog()?.filterNot { matchesTarget(it.branchId) }
        if (catalog != null) saveCatalog(catalog)

        // 3. Purge staff accounts
        val staff = loadStaffAccounts()?.filterNot { matchesTarget(it.branchId) }
        if (staff != null) saveStaffAccounts(staff)

        // 4. Purge money transactions
        val txs = loadMoneyTransactions()?.filterNot { matchesTarget(it.branchId) }
        if (txs != null) saveMoneyTransactions(txs)

        // 5. Purge profiles and branch list
        val profiles = loadBranchProfiles()?.filterNot { matchesTarget(it.branchId) }
        val branches = loadBranches()?.filterNot { matchesTarget(it) }
        if (profiles != null && branches != null) saveBranchesAndProfiles(branches, profiles)

        // 6. Clear store open status
        prefs.edit()
            .remove("${KEY_STORE_OPEN_PREFIX}_$targetBranchId")
            .remove("${KEY_STORE_OPEN_PREFIX}_$lowTarget")
            .remove("${KEY_STORE_OPEN_PREFIX}_$baseSlug")
            .apply()
    }

    fun saveBankAccounts(list: List<BankAccountDetail>) {
        val arr = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("ownerName", item.ownerName)
            obj.put("accountNumber", item.accountNumber)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_BANK_ACCOUNTS, arr.toString()).apply()
    }

    fun loadBankAccounts(): List<BankAccountDetail>? {
        val raw = prefs.getString(KEY_BANK_ACCOUNTS, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<BankAccountDetail>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    BankAccountDetail(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        ownerName = obj.optString("ownerName"),
                        accountNumber = obj.optString("accountNumber")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveDigitalWallets(list: List<DigitalWalletDetail>) {
        val arr = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("ownerName", item.ownerName)
            obj.put("phoneNumber", item.phoneNumber)
            obj.put("email", item.email)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_DIGITAL_WALLETS, arr.toString()).apply()
    }

    fun loadDigitalWallets(): List<DigitalWalletDetail>? {
        val raw = prefs.getString(KEY_DIGITAL_WALLETS, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<DigitalWalletDetail>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    DigitalWalletDetail(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        ownerName = obj.optString("ownerName"),
                        phoneNumber = obj.optString("phoneNumber"),
                        email = obj.optString("email")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveDeletedBranchIds(ids: Set<String>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        prefs.edit().putString(KEY_DELETED_BRANCHES, arr.toString()).apply()
    }

    fun loadDeletedBranchIds(): Set<String> {
        val raw = prefs.getString(KEY_DELETED_BRANCHES, null) ?: return emptySet()
        return try {
            val arr = JSONArray(raw)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                val s = arr.optString(i)
                if (s.isNotBlank()) set.add(s.trim().lowercase())
            }
            set
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun saveDeletedOrderIds(ids: Set<String>) {
        val arr = JSONArray()
        ids.take(1500).forEach { if (it.isNotBlank()) arr.put(it.trim()) }
        prefs.edit().putString(KEY_DELETED_ORDERS, arr.toString()).apply()
    }

    fun loadDeletedOrderIds(): Set<String> {
        val raw = prefs.getString(KEY_DELETED_ORDERS, null) ?: return emptySet()
        return try {
            val arr = JSONArray(raw)
            val set = LinkedHashSet<String>()
            for (i in 0 until arr.length()) {
                val s = arr.optString(i).trim()
                if (s.isNotBlank()) set.add(s)
            }
            set
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun saveActiveSessionJson(sessionJson: String) {
        prefs.edit().putString(KEY_ACTIVE_SESSION, sessionJson).apply()
    }

    fun loadActiveSessionJson(): String? {
        return prefs.getString(KEY_ACTIVE_SESSION, null)
    }

    fun saveLastLoginBranch(branchId: String) {
        if (branchId.isNotBlank()) {
            prefs.edit().putString(KEY_LAST_LOGIN_BRANCH, branchId).apply()
        }
    }

    fun getLastLoginBranch(): String? {
        return prefs.getString(KEY_LAST_LOGIN_BRANCH, null)?.takeIf { it.isNotBlank() }
    }

    companion object {
        private const val PREFS_NAME = "waffleo_offline_store_v1"
        private const val KEY_STORE_OPEN_PREFIX = "store_open_status"
        private const val KEY_AUTHORIZED_BRANCHES = "authorized_google_branches_json"
        private const val KEY_PENDING_SYNC_KEYS = "pending_sync_keys_json"
        private const val KEY_HIGHEST_ORDER_SEQ = "highest_order_seq_json"
        private const val KEY_ORDERS = "orders_json"
        private const val KEY_CATALOG = "catalog_json"
        private const val KEY_MONEY_TX = "money_tx_json"
        private const val KEY_MONEY_BOOKMARKS = "money_bookmarks_json"
        private const val KEY_STAFF_ACCOUNTS = "staff_accounts_json"
        private const val KEY_BRANCHES = "branches_json"
        private const val KEY_BRANCH_PROFILES = "branch_profiles_json"
        private const val KEY_AUDIT_LOGS = "audit_logs_json"
        private const val KEY_CUSTOM_MENU_CATEGORIES = "custom_menu_categories_json"
        private const val KEY_CUSTOM_MODIFIER_CATEGORIES = "custom_modifier_categories_json"
        private const val KEY_TRANSFER_FEE_ACCOUNT_CHOICE = "transfer_fee_account_choice"
        private const val KEY_RECAPPED_ORDER_IDS = "recapped_order_ids_json"
        private const val KEY_CUSTOM_SUB_CATEGORIES = "custom_sub_categories_json"
        private const val KEY_BANK_ACCOUNTS = "bank_accounts_json"
        private const val KEY_DIGITAL_WALLETS = "digital_wallets_json"
        private const val KEY_DELETED_BRANCHES = "deleted_branches_json"
        private const val KEY_DELETED_ORDERS = "deleted_orders_json"
        private const val KEY_PRODUCTION_CLEAN_V1_DONE = "production_clean_v1_done"
        private const val KEY_ACTIVE_SESSION = "active_pos_session_json"
        private const val KEY_LAST_LOGIN_BRANCH = "last_login_branch_id"
    }
}
