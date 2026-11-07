package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WaffleoLocalStore
import com.example.data.model.AuditLogRecord
import com.example.data.model.BranchProfile
import com.example.data.model.CartLineItem
import com.example.data.model.CatalogRecord
import com.example.data.model.DailyChannelRecapItem
import com.example.data.model.MoneyAccountGroupDef
import com.example.data.model.BankAccountDetail
import com.example.data.model.DigitalWalletDetail
import com.example.data.model.MoneyBookmarkRecord
import com.example.data.model.MoneyCategoryDef
import com.example.data.model.MoneyRepetitionOption
import com.example.data.model.MoneyTransactionRecord
import com.example.data.model.MoneyTransactionType
import com.example.data.model.OrderMedia
import com.example.data.model.OrderPaymentLedgerEntry
import com.example.data.model.OrderRecord
import com.example.data.model.OrderTimelineEvent
import com.example.data.model.AppliedPromoDetail
import com.example.data.model.CartPromoEvaluationResult
import com.example.data.model.PromoBenefitType
import com.example.data.model.PromoConfig
import com.example.data.model.PromoPerformanceStat
import com.example.data.model.PromoStatus
import com.example.data.model.PromoType
import com.example.data.model.StaffAccount
import com.example.data.model.StaffRole
import com.example.data.model.WaffleoPromoEngine
import com.example.data.model.WaffleoSeedData
import com.example.data.model.areCategoriesMatching
import com.example.data.model.normalizeCategoryCanonical
import com.example.data.repository.WaffleoRepository
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToLong
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class MainNavDestination(val title: String) {
    MENUS("Menus"),
    ORDERS("Orders"),
    ORDER_HISTORY("Order History"),
    DASHBOARD("Dashboard"),
    SETTINGS("Settings"),
    MONEY_MANAGER("Money Manager")
}

enum class MenuCategoryTab(val label: String) {
    ALL("All"),
    EGG_WAFFLE("Egg Waffle"),
    DRINKS("Drinks")
}

enum class HistoryFilterPeriod(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom")
}

data class ActivePosSession(
    val branchId: String = "Global",
    val username: String = "",
    val role: StaffRole = StaffRole.KASIR,
    val displayName: String = "",
    val rememberMe: Boolean = false,
    val loginTimeFormatted: String = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(Date()),
    val isLoggedIn: Boolean = false,
    val isInDeveloperPortal: Boolean = false
) {
    fun toJsonString(): String = JSONObject().apply {
        put("branchId", branchId)
        put("username", username)
        put("role", role.name)
        put("displayName", displayName)
        put("rememberMe", rememberMe)
        put("loginTimeFormatted", loginTimeFormatted)
        put("isLoggedIn", isLoggedIn)
        put("isInDeveloperPortal", isInDeveloperPortal)
    }.toString()

    companion object {
        fun fromJsonString(raw: String?): ActivePosSession? {
            if (raw.isNullOrBlank()) return null
            return try {
                val obj = JSONObject(raw)
                val roleStr = obj.optString("role", StaffRole.KASIR.name)
                val role = try {
                    StaffRole.valueOf(roleStr)
                } catch (_: Exception) {
                    StaffRole.KASIR
                }
                ActivePosSession(
                    branchId = obj.optString("branchId", "Global").ifBlank { "Global" },
                    username = obj.optString("username", ""),
                    role = role,
                    displayName = obj.optString("displayName", ""),
                    rememberMe = obj.optBoolean("rememberMe", false),
                    loginTimeFormatted = obj.optString(
                        "loginTimeFormatted",
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(Date())
                    ),
                    isLoggedIn = obj.optBoolean("isLoggedIn", false),
                    isInDeveloperPortal = obj.optBoolean("isInDeveloperPortal", false)
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

class WaffleoViewModel(
    private val repository: WaffleoRepository,
    private val currentUserId: String,
    private val localStore: WaffleoLocalStore? = null
) : ViewModel() {

    // Canonical Shared Cloud Workspace ID so all authenticated devices share one unified source of truth
    private val workspaceId: String = WaffleoRepository.SHARED_WORKSPACE_ID

    // Staff & Branch Login State (Restored from LocalStore if present; defaults to Global / owner)
    private val _session = MutableStateFlow(
        localStore?.loadActiveSessionJson()?.let { ActivePosSession.fromJsonString(it) }
            ?: ActivePosSession()
    )
    val session: StateFlow<ActivePosSession> = _session.asStateFlow()

    private fun updateAndPersistSession(newSession: ActivePosSession) {
        _session.value = newSession
        localStore?.saveActiveSessionJson(newSession.toJsonString())
        val branchOpen = localStore?.loadStoreOpenStatus(newSession.branchId)
        if (branchOpen != null) {
            _isStoreOpen.value = branchOpen
        }
    }

    private val _staffAccounts = MutableStateFlow(
        localStore?.loadStaffAccounts()?.takeIf { it.isNotEmpty() }
            ?: WaffleoSeedData.defaultStaffAccounts()
    )
    val staffAccounts: StateFlow<List<StaffAccount>> = _staffAccounts.asStateFlow()

    private val _branches = MutableStateFlow(
        run {
            val deleted = localStore?.loadDeletedBranchIds() ?: emptySet()
            val raw = localStore?.loadBranches()?.takeIf { it.isNotEmpty() }
                ?: WaffleoSeedData.defaultBranches
            raw.filterNot { b ->
                val low = b.trim().lowercase()
                val base = low.removePrefix("cabang ").trim()
                low in deleted || base in deleted || "cabang $base" in deleted
            }
        }
    )
    val branches: StateFlow<List<String>> = _branches.asStateFlow()

    private val _branchProfiles = MutableStateFlow(
        run {
            val deleted = localStore?.loadDeletedBranchIds() ?: emptySet()
            val raw = localStore?.loadBranchProfiles()?.takeIf { it.isNotEmpty() }
                ?: WaffleoSeedData.defaultBranchProfiles
            raw.filterNot { p ->
                val low = p.branchId.trim().lowercase()
                val base = low.removePrefix("cabang ").trim()
                low in deleted || base in deleted || "cabang $base" in deleted
            }
        }
    )
    val branchProfiles: StateFlow<List<BranchProfile>> = _branchProfiles.asStateFlow()

    // Network Connectivity & Offline Pending Sync State
    val isNetworkOnline: StateFlow<Boolean> =
        localStore?.isNetworkOnline ?: MutableStateFlow(true).asStateFlow()

    private val _pendingSyncKeys = MutableStateFlow<Set<String>>(
        localStore?.loadPendingSyncKeys() ?: emptySet()
    )
    val pendingSyncKeys: StateFlow<Set<String>> = _pendingSyncKeys.asStateFlow()

    private val _isSyncingOfflineQueue = MutableStateFlow(false)
    val isSyncingOfflineQueue: StateFlow<Boolean> = _isSyncingOfflineQueue.asStateFlow()

    private val _authorizedBranchesVersion = MutableStateFlow(0)

    // Store Open/Close Status per Top Bar requirement (Persisted in LocalStore so cold restart preserves it)
    private val _isStoreOpen = MutableStateFlow(
        localStore?.loadStoreOpenStatus(_session.value.branchId) ?: true
    )
    val isStoreOpen: StateFlow<Boolean> = _isStoreOpen.asStateFlow()

    // Current Navigation Screen
    private val _currentDestination = MutableStateFlow(MainNavDestination.MENUS)
    val currentDestination: StateFlow<MainNavDestination> = _currentDestination.asStateFlow()

    // Selected Menu Tab (All | Egg Waffle | Drinks)
    private val _selectedMenuTab = MutableStateFlow(MenuCategoryTab.ALL)
    val selectedMenuTab: StateFlow<MenuCategoryTab> = _selectedMenuTab.asStateFlow()

    // Cart State
    private val _cartMedia = MutableStateFlow(OrderMedia.WALK_IN)
    val cartMedia: StateFlow<OrderMedia> = _cartMedia.asStateFlow()

    // Customer Data raw suffix (without prefix for platform, or full name for Walk In)
    private val _customerInputSuffix = MutableStateFlow("")
    val customerInputSuffix: StateFlow<String> = _customerInputSuffix.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartLineItem>>(emptyList())
    val cartItems: StateFlow<List<CartLineItem>> = _cartItems.asStateFlow()

    private val _cartNotes = MutableStateFlow("")
    val cartNotes: StateFlow<String> = _cartNotes.asStateFlow()

    // Defaults to "AUTO" so qualifying promos are automatically detected in POS cart
    private val _selectedPromoId = MutableStateFlow("AUTO")
    val selectedPromoId: StateFlow<String> = _selectedPromoId.asStateFlow()

    private val _cartVoucherCode = MutableStateFlow("")
    val cartVoucherCode: StateFlow<String> = _cartVoucherCode.asStateFlow()

    // Cart Pre-Payment State (since customer can pay while still in Cart or later in Orders)
    private val _cartIsPaid = MutableStateFlow(false)
    val cartIsPaid: StateFlow<Boolean> = _cartIsPaid.asStateFlow()

    private val _cartPaidAmount = MutableStateFlow(0L)
    val cartPaidAmount: StateFlow<Long> = _cartPaidAmount.asStateFlow()

    private val _cartPaymentMethod = MutableStateFlow("Belum Bayar")
    val cartPaymentMethod: StateFlow<String> = _cartPaymentMethod.asStateFlow()

    private val _cartSplitCash = MutableStateFlow(0L)
    val cartSplitCash: StateFlow<Long> = _cartSplitCash.asStateFlow()

    private val _cartSplitQris = MutableStateFlow(0L)
    val cartSplitQris: StateFlow<Long> = _cartSplitQris.asStateFlow()

    private val _cartQrisBank = MutableStateFlow("Bank Mandiri")
    val cartQrisBank: StateFlow<String> = _cartQrisBank.asStateFlow()

    private val _cartChangeAmount = MutableStateFlow(0L)
    val cartChangeAmount: StateFlow<Long> = _cartChangeAmount.asStateFlow()

    private val _cartCashReceived = MutableStateFlow(0L)
    val cartCashReceived: StateFlow<Long> = _cartCashReceived.asStateFlow()

    private val _cartPaymentLedger = MutableStateFlow<List<OrderPaymentLedgerEntry>>(emptyList())
    val cartPaymentLedger: StateFlow<List<OrderPaymentLedgerEntry>> = _cartPaymentLedger.asStateFlow()

    // Editing Order State (When an order's [Edit] button is clicked in Orders screen)
    private val _editingOrder = MutableStateFlow<OrderRecord?>(null)
    val editingOrder: StateFlow<OrderRecord?> = _editingOrder.asStateFlow()

    // Backdated Order ("Data lampau") State for Supervisor / Owner / Developer in Cart
    private val _cartIsBackdated = MutableStateFlow(false)
    val cartIsBackdated: StateFlow<Boolean> = _cartIsBackdated.asStateFlow()

    private val _cartBackdatedDateKey = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    )
    val cartBackdatedDateKey: StateFlow<String> = _cartBackdatedDateKey.asStateFlow()

    private val _cartBackdatedTimeText = MutableStateFlow("00:00")
    val cartBackdatedTimeText: StateFlow<String> = _cartBackdatedTimeText.asStateFlow()

    // Status Banner / Toast Feedback
    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // Maintenance Mode (Temporary / Local Only Changes for Developer)
    private val _isMaintenanceModeOn = MutableStateFlow(false)
    val isMaintenanceModeOn: StateFlow<Boolean> = _isMaintenanceModeOn.asStateFlow()

    fun toggleMaintenanceMode() {
        if (_session.value.role != StaffRole.DEVELOPER) return
        val next = !_isMaintenanceModeOn.value
        _isMaintenanceModeOn.value = next
        showBanner(if (next) "Maintenance Mode: ON (Perubahan hanya bersifat lokal & sementara)" else "Maintenance Mode: OFF (Perubahan tersinkron ke Cloud)")
    }

    fun isDeveloperSession(): Boolean {
        return _session.value.role == StaffRole.DEVELOPER
    }

    fun getLastLoginBranch(): String? {
        return localStore?.getLastLoginBranch()
    }

    // 1-Server Architecture: All devices directly connect to central cloud without Google Login
    val isCloudAuthenticated: Boolean
        get() = true

    // Deleted Branch Tombstones (persisted locally & synced via Cloud workspace_settings)
    private val _deletedBranchIds = MutableStateFlow<Set<String>>(
        localStore?.loadDeletedBranchIds() ?: emptySet()
    )

    private fun isBranchDeleted(raw: String, deletedSet: Set<String> = _deletedBranchIds.value): Boolean {
        if (raw.isBlank() || raw.equals("Global", ignoreCase = true)) return false
        val low = raw.trim().lowercase()
        val base = low.removePrefix("cabang ").trim()
        return low in deletedSet || base in deletedSet || "cabang $base" in deletedSet
    }

    // Cloud Data Streams with Local Persistent Storage & Seed Fallback
    private val _localSeededCatalog = MutableStateFlow(
        (localStore?.loadCatalog()?.takeIf { it.isNotEmpty() }
            ?: WaffleoSeedData.defaultCatalogRecords(workspaceId))
            .filterNot { isBranchDeleted(it.branchId) }
    )
    private val _localOrders = MutableStateFlow<List<OrderRecord>>(
        (localStore?.loadOrders()?.takeIf { it.isNotEmpty() }
            ?: WaffleoSeedData.defaultOrderRecords(workspaceId, "Global"))
            .filterNot { isBranchDeleted(it.branchId) }
    )
    private val _localAuditLogs = MutableStateFlow<List<AuditLogRecord>>(
        localStore?.loadAuditLogs() ?: emptyList()
    )

    val allCatalogRecords: StateFlow<List<CatalogRecord>> = (
        if (isCloudAuthenticated) {
            combine(
                repository.observeCatalog(workspaceId).catch { e ->
                    Log.w("WaffleoVM", "Catalog stream fallback to local", e)
                    emit(emptyList())
                },
                _localSeededCatalog,
                _deletedBranchIds
            ) { cloudList, localList, deletedSet ->
                val pending = _pendingSyncKeys.value
                val cleanedCloud = cloudList.filter { rec ->
                    rec.id !in WaffleoSeedData.legacyDummyCatalogIds &&
                        rec.recordType != "INGREDIENT" &&
                        !isBranchDeleted(rec.branchId, deletedSet) &&
                        "catalog_delete:${rec.id}" !in pending
                }
                if (cleanedCloud.isEmpty() || cleanedCloud.none { it.id == "waffle_polos" }) {
                    localList.filterNot { isBranchDeleted(it.branchId, deletedSet) || "catalog_delete:${it.id}" in pending }
                } else {
                    val mergedMap = LinkedHashMap<String, CatalogRecord>()
                    cleanedCloud.forEach { cloudRec ->
                        if ("catalog:${cloudRec.id}" !in pending) {
                            mergedMap[cloudRec.id] = cloudRec
                        }
                    }
                    localList.forEach { localRec ->
                        if (!isBranchDeleted(localRec.branchId, deletedSet) &&
                            "catalog_delete:${localRec.id}" !in pending &&
                            ("catalog:${localRec.id}" in pending || !mergedMap.containsKey(localRec.id))
                        ) {
                            mergedMap[localRec.id] = localRec
                        }
                    }
                    mergedMap.values.toList()
                }
            }
        } else {
            _localSeededCatalog
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), _localSeededCatalog.value)

    private fun resolveCategorySortRank(rec: CatalogRecord): Int {
        val menuCats = try { _customMenuCategories.value } catch (_: Throwable) { listOf("Egg Waffle", "Drinks") }
        val modCats = try { _customModifierCategories.value } catch (_: Throwable) { listOf("Flavour", "Add-on", "Sweetness", "Ice Level") }
        return when (rec.recordType) {
            "MENU_ITEM" -> {
                val idx = menuCats.indexOfFirst { areCategoriesMatching(it, rec.category) }
                if (idx >= 0) idx else 500
            }
            "MODIFIER" -> {
                val idx = modCats.indexOfFirst { areCategoriesMatching(it, rec.category) }
                if (idx >= 0) idx else 500
            }
            else -> 900
        }
    }

    private fun catalogItemComparator(): Comparator<CatalogRecord> =
        compareBy<CatalogRecord> { resolveCategorySortRank(it) }
            .thenBy { if (it.sortOrder < 0) 999999 else it.sortOrder }
            .thenBy { it.name.lowercase() }

    // Strictly isolated per active branch session (while including multi-branch Promos targeting active branch), sorted by category order, custom sortOrder, then name
    val catalogRecords: StateFlow<List<CatalogRecord>> = combine(allCatalogRecords, _session) { list, sess ->
        list.filter { rec ->
            if (rec.recordType == "PROMO") {
                val cfg = PromoConfig.fromCatalogRecord(rec)
                !cfg.isArchived && WaffleoPromoEngine.isPromoApplicableToBranch(rec, cfg, sess.branchId)
            } else {
                rec.branchId.equals(sess.branchId, ignoreCase = true)
            }
        }.sortedWith(catalogItemComparator())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        _localSeededCatalog.value
            .filter { rec ->
                if (rec.recordType == "PROMO") {
                    val cfg = PromoConfig.fromCatalogRecord(rec)
                    !cfg.isArchived && WaffleoPromoEngine.isPromoApplicableToBranch(rec, cfg, _session.value.branchId)
                } else {
                    rec.branchId.equals(_session.value.branchId, ignoreCase = true)
                }
            }
            .sortedWith(catalogItemComparator())
    )

    val allOrders: StateFlow<List<OrderRecord>> = (
        if (isCloudAuthenticated) {
            combine(
                repository.observeOrders(workspaceId).catch { e ->
                    Log.w("WaffleoVM", "Orders stream error", e)
                    emit(emptyList())
                },
                _localOrders,
                _deletedBranchIds
            ) { cloudList, localList, deletedSet ->
                val pending = _pendingSyncKeys.value
                val validCloud = cloudList.filter {
                    it.id.startsWith("v1_") &&
                        it.orderNumber != "WFL-001" &&
                        it.id != "WFL-001" &&
                        !isBranchDeleted(it.branchId, deletedSet) &&
                        "order_delete:${it.id}" !in pending &&
                        (!it.createdBy.contains("developer", ignoreCase = true) || isDeveloperSession())
                }
                val validLocal = localList.filter {
                    it.orderNumber != "WFL-001" &&
                        it.id != "WFL-001" &&
                        !isBranchDeleted(it.branchId, deletedSet) &&
                        "order_delete:${it.id}" !in pending &&
                        (!it.createdBy.contains("developer", ignoreCase = true) || isDeveloperSession())
                }
                val mergedMap = LinkedHashMap<String, OrderRecord>()
                validCloud.forEach { cloudOrd ->
                    mergedMap[cloudOrd.id] = cloudOrd
                }
                validLocal.forEach { localOrd ->
                    if ("order:${localOrd.id}" in pending ||
                        localOrd.createdBy.contains("developer", ignoreCase = true) ||
                        !mergedMap.containsKey(localOrd.id)
                    ) {
                        mergedMap[localOrd.id] = localOrd
                    }
                }
                mergedMap.values
                    .filterNot { "order_delete:${it.id}" in pending }
                    .sortedBy { it.sequenceIndex }
            }
        } else {
            _localOrders
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), _localOrders.value.filter { it.orderNumber != "WFL-001" && it.id != "WFL-001" })

    val orders: StateFlow<List<OrderRecord>> = combine(allOrders, _session) { list, sess ->
        list.filter { isSameBranch(it.branchId, sess.branchId) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val auditLogs: StateFlow<List<AuditLogRecord>> = (
        if (isCloudAuthenticated) {
            repository.observeAuditLogs(workspaceId)
                .catch { e ->
                    Log.w("WaffleoVM", "AuditLogs stream error", e)
                    emit(emptyList())
                }
                .combine(_localAuditLogs) { cloudList, localList ->
                    val validCloud = cloudList.filter { it.id.startsWith("v1_") }
                    if (validCloud.isEmpty() && localList.isNotEmpty()) {
                        localList
                    } else {
                        val mergedMap = LinkedHashMap<String, AuditLogRecord>()
                        (validCloud + localList).forEach { log ->
                            if (log.id.isNotBlank() && !mergedMap.containsKey(log.id)) {
                                mergedMap[log.id] = log
                            }
                        }
                        mergedMap.values.sortedByDescending { it.createdAt?.seconds ?: 0L }
                    }
                }
        } else {
            _localAuditLogs
        }
    ).combine(_session) { list, sess ->
        list.filter { isSameBranch(it.branchId, sess.branchId) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun getBranchDisplayName(branchId: String): String {
        if (branchId.isBlank() || branchId.equals("Global", ignoreCase = true)) return "Global"
        val profile = _branchProfiles.value.firstOrNull {
            it.branchId.equals(branchId, ignoreCase = true) || it.name.equals(branchId, ignoreCase = true)
        }
        if (profile != null) {
            val dName = profile.displayName
            if (dName.isNotBlank() && !dName.equals(branchId, ignoreCase = true)) return dName
        }
        val staffAcc = _staffAccounts.value.firstOrNull { it.branchId.equals(branchId, ignoreCase = true) && !it.username.equals("developer", ignoreCase = true) }
        if (staffAcc != null) {
            val u = staffAcc.username
            if (u.isNotBlank() && !u.equals("owner", ignoreCase = true)) {
                return u.replaceFirstChar { it.uppercaseChar() }
            }
        }
        if (branchId.contains("-") && branchId.length > 20) {
            return "Gabek"
        }
        val clean = branchId.removePrefix("branch_").replace("_", " ").trim()
        return if (clean.isNotBlank()) clean.replaceFirstChar { it.uppercaseChar() } else branchId
    }

    // --- MONEY MANAGER & SETTINGS: BANK & DIGITAL WALLET STATES ---
    private val defaultBankAccounts = listOf(
        BankAccountDetail(id = "bank_mandiri", name = "Bank Mandiri", ownerName = "Waffleo", accountNumber = "123-00-9876543-2"),
        BankAccountDetail(id = "bank_bca", name = "BCA", ownerName = "Waffleo", accountNumber = "825-001-2345")
    )
    private val defaultDigitalWallets = listOf(
        DigitalWalletDetail(id = "wallet_shopeepay", name = "ShopeePay", ownerName = "Fadhil", phoneNumber = "081234567890", email = "waffleo.gabek@gmail.com")
    )

    private val _bankAccounts = MutableStateFlow<List<BankAccountDetail>>(
        localStore?.loadBankAccounts()?.takeIf { it.isNotEmpty() } ?: defaultBankAccounts
    )
    val bankAccounts: StateFlow<List<BankAccountDetail>> = _bankAccounts.asStateFlow()

    private val _digitalWallets = MutableStateFlow<List<DigitalWalletDetail>>(
        localStore?.loadDigitalWallets()?.takeIf { it.isNotEmpty() } ?: defaultDigitalWallets
    )
    val digitalWallets: StateFlow<List<DigitalWalletDetail>> = _digitalWallets.asStateFlow()

    // --- MONEY MANAGER STATE FLOWS ---
    private val _moneyAccountGroups = MutableStateFlow(
        WaffleoSeedData.defaultMoneyAccountGroups.map { grp ->
            when {
                grp.groupName.equals("Bank", ignoreCase = true) -> {
                    val loaded = localStore?.loadBankAccounts()?.takeIf { it.isNotEmpty() } ?: defaultBankAccounts
                    grp.copy(accounts = loaded.map { it.name })
                }
                grp.groupName.equals("Dompet Digital", ignoreCase = true) -> {
                    val loaded = localStore?.loadDigitalWallets()?.takeIf { it.isNotEmpty() } ?: defaultDigitalWallets
                    grp.copy(accounts = loaded.map { it.name })
                }
                else -> grp
            }
        }
    )
    val moneyAccountGroups: StateFlow<List<MoneyAccountGroupDef>> = _moneyAccountGroups.asStateFlow()

    private fun syncMoneyAccountGroupsWithBanksAndWallets() {
        val currentGroups = _moneyAccountGroups.value.map { grp ->
            when {
                grp.groupName.equals("Bank", ignoreCase = true) -> grp.copy(accounts = _bankAccounts.value.map { it.name }.ifEmpty { listOf("Bank Mandiri", "BCA") })
                grp.groupName.equals("Dompet Digital", ignoreCase = true) -> grp.copy(accounts = _digitalWallets.value.map { it.name }.ifEmpty { listOf("ShopeePay") })
                else -> grp
            }
        }
        _moneyAccountGroups.value = currentGroups
    }

    fun saveBankAccount(detail: BankAccountDetail) {
        val current = _bankAccounts.value.toMutableList()
        val idx = current.indexOfFirst { it.id == detail.id || (detail.id.isBlank() && it.name.equals(detail.name, ignoreCase = true)) }
        val finalItem = if (detail.id.isBlank()) detail.copy(id = "bank_${System.currentTimeMillis()}") else detail
        if (idx >= 0) {
            current[idx] = finalItem
        } else {
            current.add(finalItem)
        }
        _bankAccounts.value = current
        localStore?.saveBankAccounts(current)
        syncMoneyAccountGroupsWithBanksAndWallets()
        syncWorkspaceSettingsToCloud()
        logAuditEvent(
            actionType = "BANK_ACCOUNT_SAVED",
            entityId = finalItem.name,
            summary = "Menyimpan pengaturan Bank '${finalItem.name}' (No. Rek: ${finalItem.accountNumber})",
            beforeSnapshot = "{}",
            afterSnapshot = """{"name":"${finalItem.name}","owner":"${finalItem.ownerName}","number":"${finalItem.accountNumber}"}"""
        )
        showBanner("Pengaturan Bank '${finalItem.name}' berhasil disimpan.")
    }

    fun deleteBankAccount(id: String) {
        val target = _bankAccounts.value.firstOrNull { it.id == id }
        val current = _bankAccounts.value.filterNot { it.id == id }
        _bankAccounts.value = current
        localStore?.saveBankAccounts(current)
        syncMoneyAccountGroupsWithBanksAndWallets()
        syncWorkspaceSettingsToCloud()
        if (target != null) {
            logAuditEvent(
                actionType = "BANK_ACCOUNT_DELETED",
                entityId = target.name,
                summary = "Menghapus Bank '${target.name}'",
                beforeSnapshot = """{"name":"${target.name}"}""",
                afterSnapshot = "{}"
            )
            showBanner("Bank '${target.name}' berhasil dihapus.")
        }
    }

    fun saveDigitalWallet(detail: DigitalWalletDetail) {
        val current = _digitalWallets.value.toMutableList()
        val idx = current.indexOfFirst { it.id == detail.id || (detail.id.isBlank() && it.name.equals(detail.name, ignoreCase = true)) }
        val finalItem = if (detail.id.isBlank()) detail.copy(id = "wallet_${System.currentTimeMillis()}") else detail
        if (idx >= 0) {
            current[idx] = finalItem
        } else {
            current.add(finalItem)
        }
        _digitalWallets.value = current
        localStore?.saveDigitalWallets(current)
        syncMoneyAccountGroupsWithBanksAndWallets()
        syncWorkspaceSettingsToCloud()
        logAuditEvent(
            actionType = "WALLET_SAVED",
            entityId = finalItem.name,
            summary = "Menyimpan Dompet Digital '${finalItem.name}' (No. HP: ${finalItem.phoneNumber})",
            beforeSnapshot = "{}",
            afterSnapshot = """{"name":"${finalItem.name}","owner":"${finalItem.ownerName}","phone":"${finalItem.phoneNumber}"}"""
        )
        showBanner("Dompet Digital '${finalItem.name}' berhasil disimpan.")
    }

    fun deleteDigitalWallet(id: String) {
        val target = _digitalWallets.value.firstOrNull { it.id == id }
        val current = _digitalWallets.value.filterNot { it.id == id }
        _digitalWallets.value = current
        localStore?.saveDigitalWallets(current)
        syncMoneyAccountGroupsWithBanksAndWallets()
        syncWorkspaceSettingsToCloud()
        if (target != null) {
            logAuditEvent(
                actionType = "WALLET_DELETED",
                entityId = target.name,
                summary = "Menghapus Dompet Digital '${target.name}'",
                beforeSnapshot = """{"name":"${target.name}"}""",
                afterSnapshot = "{}"
            )
            showBanner("Dompet Digital '${target.name}' berhasil dihapus.")
        }
    }

    private val _moneyCategories = MutableStateFlow(WaffleoSeedData.defaultMoneyCategories)
    val moneyCategories: StateFlow<List<MoneyCategoryDef>> = _moneyCategories.asStateFlow()

    private val _allMoneyTransactions = MutableStateFlow<List<MoneyTransactionRecord>>(
        run {
            val rawList = (localStore?.loadMoneyTransactions()?.takeIf { it.isNotEmpty() }
                ?: WaffleoSeedData.defaultMoneyTransactions("Global")).toMutableList()
            val transfersWithFee = rawList.filter { it.type == MoneyTransactionType.TRANSFER && it.transferFee > 0L }
            if (transfersWithFee.isNotEmpty()) {
                val toRemove = mutableSetOf<String>()
                transfersWithFee.forEach { tr ->
                    val dupExpense = rawList.firstOrNull { exp ->
                        exp.type == MoneyTransactionType.EXPENSE &&
                            exp.dateKey == tr.dateKey &&
                            exp.amount == tr.transferFee &&
                            (exp.note.contains("Biaya Administrasi", ignoreCase = true) || exp.subCategory.contains("Biaya Administrasi", ignoreCase = true)) &&
                            exp.id !in toRemove
                    }
                    if (dupExpense != null) {
                        toRemove.add(dupExpense.id)
                    }
                }
                if (toRemove.isNotEmpty()) {
                    rawList.removeAll { it.id in toRemove }
                    localStore?.saveMoneyTransactions(rawList)
                }
            }
            rawList
        }
    )
    private val _allMoneyBookmarks = MutableStateFlow(
        localStore?.loadMoneyBookmarks()?.takeIf { it.isNotEmpty() }
            ?: WaffleoSeedData.defaultMoneyBookmarks("Global")
    )

    // Selected month in Money Manager (YYYY-MM), defaults to current active month
    private val _moneySelectedMonthKey = MutableStateFlow(
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    )
    val moneySelectedMonthKey: StateFlow<String> = _moneySelectedMonthKey.asStateFlow()

    // Developer cross-branch consolidation toggle in Money Manager
    private val _moneyConsolidateAllBranches = MutableStateFlow(false)
    val moneyConsolidateAllBranches: StateFlow<Boolean> = _moneyConsolidateAllBranches.asStateFlow()

    private val _transferFeeAccountChoice = MutableStateFlow(
        localStore?.loadTransferFeeAccountChoice() ?: "FROM"
    )
    val transferFeeAccountChoice: StateFlow<String> = _transferFeeAccountChoice.asStateFlow()

    private val _recappedOrderIds = MutableStateFlow<Set<String>>(
        localStore?.loadRecappedOrderIds() ?: emptySet()
    )
    val recappedOrderIds: StateFlow<Set<String>> = _recappedOrderIds.asStateFlow()

    fun markOrdersAsRecapped(orderIds: Collection<String>) {
        if (orderIds.isEmpty()) return
        val next = _recappedOrderIds.value + orderIds
        _recappedOrderIds.value = next
        localStore?.saveRecappedOrderIds(next)
        syncWorkspaceSettingsToCloud()
    }

    fun saveLastTransferFeeAccountChoice(choice: String) {
        val clean = if (choice.equals("TO", ignoreCase = true)) "TO" else "FROM"
        _transferFeeAccountChoice.value = clean
        localStore?.saveTransferFeeAccountChoice(clean)
        syncWorkspaceSettingsToCloud()
    }

    val moneyTransactions: StateFlow<List<MoneyTransactionRecord>> = combine(
        _allMoneyTransactions,
        _session,
        _moneyConsolidateAllBranches
    ) { list, sess, consolidate ->
        val filtered = if (sess.role == StaffRole.DEVELOPER && consolidate) {
            list
        } else {
            list.filter { it.branchId.equals(sess.branchId, ignoreCase = true) }
        }
        filtered.sortedWith(
            compareByDescending<MoneyTransactionRecord> { it.dateKey }
                .thenBy { if (it.subCategoryName.equals("Cash", ignoreCase = true) || it.note.equals("Penjualan Tunai", ignoreCase = true)) 0 else 1 }
                .thenByDescending { it.timeText }
                .thenByDescending { it.id }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), _allMoneyTransactions.value)

    val dailyPosItemSummaryMap: StateFlow<Map<String, String>> = combine(
        orders,
        _session
    ) { orderList, sess ->
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val map = mutableMapOf<String, String>()

        val eligibleByDate = orderList
            .filter { it.branchId.equals(sess.branchId, ignoreCase = true) && it.isPaid && it.status != "CANCELLED" && it.status != "ERASED" }
            .groupBy { dateFmt.format(resolveOrderTimestampDate(it)) }

        eligibleByDate.forEach { (dKey, ordsOnDate) ->
            val totalOrders = ordsOnDate.size
            val itemCounts = mutableMapOf<String, Int>()

            ordsOnDate.forEach { ord ->
                val parsedItems = CartLineItem.deserializeList(ord.itemsPayload)
                if (parsedItems.isNotEmpty()) {
                    parsedItems.forEach { item ->
                        val label = when {
                            item.category.equals("Egg Waffle", ignoreCase = true) -> "Waffle"
                            item.category.equals("Drinks", ignoreCase = true) -> "Minuman"
                            item.category.equals("Dessert", ignoreCase = true) && item.primaryItemName.isNotBlank() -> item.primaryItemName
                            item.category.isNotBlank() && !item.category.equals("General", ignoreCase = true) -> item.category
                            item.primaryItemName.isNotBlank() -> item.primaryItemName
                            else -> "Item"
                        }
                        itemCounts[label] = (itemCounts[label] ?: 0) + item.qty
                    }
                } else {
                    itemCounts["Waffle"] = (itemCounts["Waffle"] ?: 0) + 1
                }
            }

            val itemParts = itemCounts.entries.map { (label, count) -> "$count $label" }
            val itemSummary = if (itemParts.isNotEmpty()) itemParts.joinToString(", ") else "0 Item"
            map[dKey] = "• $totalOrders Pesanan : $itemSummary"
        }

        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val moneyBookmarks: StateFlow<List<MoneyBookmarkRecord>> = combine(
        _allMoneyBookmarks,
        _session
    ) { list, sess ->
        list.filter { it.branchId.equals(sess.branchId, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), _allMoneyBookmarks.value)

    // Dynamic Custom Categories & Modifier-to-Menu Mapping (Declared BEFORE init to prevent NPE)
    private fun cleanCategoryTitle(raw: String): String {
        val trimmed = raw.trim()
        return when {
            areCategoriesMatching(trimmed, "ADD_ON") -> "Add-on"
            areCategoriesMatching(trimmed, "FLAVOUR") -> "Flavour"
            areCategoriesMatching(trimmed, "SWEETNESS") -> "Sweetness"
            areCategoriesMatching(trimmed, "ICE") -> "Ice Level"
            else -> trimmed
        }
    }

    private val _customMenuCategories = MutableStateFlow<List<String>>(
        run {
            val saved = localStore?.loadCustomMenuCategories() ?: emptyList()
            val defaultList = listOf("Egg Waffle", "Drinks")
            val fromCatalog = _localSeededCatalog.value
                .filter { it.recordType == "MENU_ITEM" }
                .map { it.category }
            if (saved.isNotEmpty()) {
                (saved + fromCatalog).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
            } else {
                (defaultList + fromCatalog).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
            }
        }
    )
    val customMenuCategories: StateFlow<List<String>> = _customMenuCategories.asStateFlow()

    private val _customModifierCategories = MutableStateFlow<List<String>>(
        run {
            val saved = localStore?.loadCustomModifierCategories() ?: emptyList()
            val defaultList = listOf("Flavour", "Add-on", "Sweetness", "Ice Level")
            val fromCatalog = _localSeededCatalog.value
                .filter { it.recordType == "MODIFIER" }
                .map { cleanCategoryTitle(it.category) }
            val rawMerged = if (saved.isNotEmpty()) (saved.map { cleanCategoryTitle(it) } + fromCatalog) else (defaultList + fromCatalog)
            rawMerged.filter { it.isNotBlank() }.distinctBy { normalizeCategoryCanonical(it) }
        }
    )
    val customModifierCategories: StateFlow<List<String>> = _customModifierCategories.asStateFlow()

    private val _modifierToMenuCategoryMap = MutableStateFlow<Map<String, String>>(
        mapOf(
            "Flavour" to "Egg Waffle",
            "Add-on" to "Egg Waffle",
            "Sweetness" to "Drinks",
            "Ice Level" to "Drinks",
            "FLAVOUR" to "Egg Waffle",
            "ADD_ON" to "Egg Waffle",
            "SWEETNESS" to "Drinks",
            "ICE" to "Drinks"
        )
    )
    val modifierToMenuCategoryMap: StateFlow<Map<String, String>> = _modifierToMenuCategoryMap.asStateFlow()

    private val _customSubCategories = MutableStateFlow<Map<String, List<String>>>(
        run {
            val saved = localStore?.loadCustomSubCategories() ?: emptyMap()
            val initial = mutableMapOf<String, MutableList<String>>()
            initial.getOrPut("Egg Waffle") { mutableListOf() }.addAll(listOf("Spread", "Crunchy", "Premium"))
            initial.getOrPut("Drinks") { mutableListOf() }.addAll(listOf("Coffee", "Non-Coffee"))
            saved.forEach { (cat, subs) ->
                val list = initial.getOrPut(cat) { mutableListOf() }
                subs.forEach { if (it !in list) list.add(it) }
            }
            _localSeededCatalog.value.forEach { rec ->
                if (rec.category.isNotBlank() && rec.subCategory.isNotBlank()) {
                    val list = initial.getOrPut(rec.category) { mutableListOf() }
                    if (rec.subCategory !in list) list.add(rec.subCategory)
                }
            }
            initial.mapValues { it.value.distinct().filter { s -> s.isNotBlank() } }
        }
    )
    val customSubCategories: StateFlow<Map<String, List<String>>> = _customSubCategories.asStateFlow()

    private var hasSeededCloud = false
    private var hasSeededInitialMoneyTxs = false
    private var hasReceivedCloudBranches = false
    private var hasReportedStorageResetAlert = false
    private val cleanedOrphanBranches = mutableSetOf<String>()

    init {
        // Purge any locally cached records belonging to explicitly deleted branches and perform cache sanitization on startup
        performLocalCacheSanitization()

        // Initialize monotonic sequence high-water mark from existing orders
        _localOrders.value.groupBy { it.branchId }.forEach { (bId, ordList) ->
            val maxSeq = ordList.maxOfOrNull { it.sequenceIndex } ?: 0L
            localStore?.updateHighestRecordedOrderSeq(bId, maxSeq)
        }

        // Listen for network restoration to automatically flush pending offline queue
        localStore?.setOnNetworkRestoredListener {
            flushPendingSyncToCloud()
        }

        // Automatically consolidate any past fragmented daily POS sync transactions
        viewModelScope.launch {
            consolidateAllDailyPosTransactions(silent = true)
        }

        if (isCloudAuthenticated) {
            viewModelScope.launch {
                // Migrate any legacy per-user documents into the unified Shared Cloud Workspace
                repository.migrateLegacyUserToSharedWorkspace(currentUserId, workspaceId)
            }
            observeMultiDeviceMoneyAndStaffStreams()
            seedInitialCatalogToCloudIfNeeded()
            if (isNetworkOnline.value && _pendingSyncKeys.value.isNotEmpty()) {
                flushPendingSyncToCloud()
            }
        }
    }

    private fun purgeDeletedBranchesFromLocalState(deletedSet: Set<String>) {
        if (deletedSet.isEmpty()) return
        val filteredBranches = _branches.value.filterNot { isBranchDeleted(it, deletedSet) }.ifEmpty { listOf("Global") }
        val filteredProfiles = _branchProfiles.value.filterNot { isBranchDeleted(it.branchId, deletedSet) }.ifEmpty { WaffleoSeedData.defaultBranchProfiles }
        if (filteredBranches != _branches.value || filteredProfiles != _branchProfiles.value) {
            _branches.value = filteredBranches
            _branchProfiles.value = filteredProfiles
            localStore?.saveBranchesAndProfiles(filteredBranches, filteredProfiles)
        }
        val filteredStaff = _staffAccounts.value.filterNot { isBranchDeleted(it.branchId, deletedSet) }
        if (filteredStaff.size != _staffAccounts.value.size) {
            _staffAccounts.value = filteredStaff
            localStore?.saveStaffAccounts(filteredStaff)
        }
        val filteredCatalog = _localSeededCatalog.value.filterNot { isBranchDeleted(it.branchId, deletedSet) }
        if (filteredCatalog.size != _localSeededCatalog.value.size) {
            _localSeededCatalog.value = filteredCatalog
            localStore?.saveCatalog(filteredCatalog)
        }
        val filteredOrders = _localOrders.value.filterNot { isBranchDeleted(it.branchId, deletedSet) }
        if (filteredOrders.size != _localOrders.value.size) {
            _localOrders.value = filteredOrders
            localStore?.saveOrders(filteredOrders)
        }
        val filteredTxs = _allMoneyTransactions.value.filterNot { isBranchDeleted(it.branchId, deletedSet) }
        if (filteredTxs.size != _allMoneyTransactions.value.size) {
            _allMoneyTransactions.value = filteredTxs
            localStore?.saveMoneyTransactions(filteredTxs)
        }
    }

    /**
     * Pushes only active, non-deleted local data to the Shared Cloud Workspace when explicitly requested.
     */
    private suspend fun pushAllLocalDataToSharedCloudWorkspace() {
        if (!isCloudAuthenticated || !isNetworkOnline.value) return
        try {
            val deletedSet = _deletedBranchIds.value
            _branchProfiles.value.filterNot { isBranchDeleted(it.branchId, deletedSet) }.forEach { prof ->
                repository.saveBranchProfile(workspaceId, prof)
            }
            _staffAccounts.value.filterNot { isBranchDeleted(it.branchId, deletedSet) }.forEach { acc ->
                repository.saveStaffAccount(workspaceId, acc)
            }
            _localOrders.value.filter {
                it.id.startsWith("v1_") &&
                    !isBranchDeleted(it.branchId, deletedSet) &&
                    !it.createdBy.contains("developer", ignoreCase = true)
            }.forEach { ord ->
                repository.updateOrder(workspaceId, ord.copy(userId = workspaceId))
            }
            _allMoneyTransactions.value.filter {
                it.id.startsWith("v1_") &&
                    !isBranchDeleted(it.branchId, deletedSet) &&
                    !it.createdBy.contains("developer", ignoreCase = true)
            }.forEach { tx ->
                repository.saveMoneyTransaction(workspaceId, tx)
            }
            pushWorkspaceSettingsNow()
        } catch (e: Exception) {
            Log.w("WaffleoVM", "Background local-to-cloud push skipped: ${e.message}")
        }
    }

    /**
     * Reconciles active branches strictly from `branch_profiles` (Cloud-authoritative + pending local creations),
     * NEVER auto-synthesizing deleted/ghost branches from orphaned staff or catalog records.
     */
    private fun reconcileDiscoveredBranches(
        cloudProfiles: List<BranchProfile> = emptyList(),
        isAuthoritativeCloudSnapshot: Boolean = false
    ) {
        val deletedSet = _deletedBranchIds.value
        val pending = _pendingSyncKeys.value

        val profileMap = LinkedHashMap<String, BranchProfile>()
        val defaultGlobal = WaffleoSeedData.defaultBranchProfiles.first()
        profileMap["global"] = _branchProfiles.value.firstOrNull { it.branchId.equals("Global", ignoreCase = true) } ?: defaultGlobal

        if (isAuthoritativeCloudSnapshot) {
            hasReceivedCloudBranches = true
            // Cloud is authoritative: include non-deleted Cloud profiles
            cloudProfiles.forEach { cp ->
                val key = cp.branchId.trim().lowercase()
                val base = key.removePrefix("cabang ").trim()
                if (!isBranchDeleted(cp.branchId, deletedSet) &&
                    "branch_delete:$key" !in pending &&
                    "branch_delete:$base" !in pending &&
                    cp.initializedFrom != "Tersinkronisasi dari Cloud"
                ) {
                    profileMap[key] = cp
                } else if (isBranchDeleted(cp.branchId, deletedSet) || cp.initializedFrom == "Tersinkronisasi dari Cloud") {
                    // Clean up ghost or deleted branch profile in Cloud
                    val cleanKey = cp.branchId.trim()
                    if (cleanedOrphanBranches.add(cleanKey.lowercase())) {
                        viewModelScope.launch {
                            repository.deleteBranchCascadeInCloud(workspaceId, cleanKey)
                        }
                    }
                }
            }
            // Keep any local branch profiles that have a pending creation in flight
            _branchProfiles.value.forEach { localProf ->
                val key = localProf.branchId.trim().lowercase()
                if (!isBranchDeleted(localProf.branchId, deletedSet) &&
                    "branch_profile:$key" in pending
                ) {
                    profileMap[key] = localProf
                }
            }
        } else {
            _branchProfiles.value.forEach { p ->
                if (!isBranchDeleted(p.branchId, deletedSet) && p.initializedFrom != "Tersinkronisasi dari Cloud") {
                    profileMap[p.branchId.trim().lowercase()] = p
                }
            }
        }

        val updatedProfiles = profileMap.values.filterNot { isBranchDeleted(it.branchId, deletedSet) }
        val updatedBranches = updatedProfiles.map { it.branchId.trim() }.distinctBy { it.lowercase() }.ifEmpty { listOf("Global") }

        if (updatedBranches != _branches.value || updatedProfiles != _branchProfiles.value) {
            _branches.value = updatedBranches
            _branchProfiles.value = updatedProfiles
            localStore?.saveBranchesAndProfiles(updatedBranches, updatedProfiles)
        }
    }

    private fun observeMultiDeviceMoneyAndStaffStreams() {
        // 1. Live Cloud Branch Profiles Mirror (Authoritative across all devices)
        viewModelScope.launch {
            repository.observeBranchProfiles(workspaceId).catch { emit(emptyList()) }.collect { cloudProfiles ->
                if (cloudProfiles.none { it.branchId.equals("Global", ignoreCase = true) } && isNetworkOnline.value) {
                    val globalProf = _branchProfiles.value.firstOrNull { it.branchId.equals("Global", ignoreCase = true) }
                        ?: WaffleoSeedData.defaultBranchProfiles.first()
                    repository.saveBranchProfile(workspaceId, globalProf)
                }
                reconcileDiscoveredBranches(
                    cloudProfiles = cloudProfiles,
                    isAuthoritativeCloudSnapshot = true
                )
            }
        }
        // 2. Live Cloud Money Transactions Mirror (Syncs additions, edits, and deletions in real time)
        viewModelScope.launch {
            repository.observeMoneyTransactions(workspaceId).catch { emit(emptyList()) }.collect { cloudTxs ->
                val legacyTxs = cloudTxs.filterNot { it.id.startsWith("v1_") }
                if (legacyTxs.isNotEmpty()) {
                    legacyTxs.forEach { oldTx ->
                        repository.deleteMoneyTransaction(workspaceId, oldTx.id)
                    }
                }
                val deletedSet = _deletedBranchIds.value
                val pending = _pendingSyncKeys.value

                // Purge any cloud transactions belonging to deleted branches
                cloudTxs.filter { isBranchDeleted(it.branchId, deletedSet) }.forEach { orphanTx ->
                    repository.deleteMoneyTransaction(workspaceId, orphanTx.id)
                }

                val validCloudTxs = cloudTxs.filter {
                    it.id.startsWith("v1_") &&
                        !isBranchDeleted(it.branchId, deletedSet) &&
                        "money_tx_delete:${it.id}" !in pending
                }

                if (validCloudTxs.isEmpty() && !hasSeededInitialMoneyTxs && _allMoneyTransactions.value.isNotEmpty()) {
                    hasSeededInitialMoneyTxs = true
                    _allMoneyTransactions.value
                        .filter { it.id.startsWith("v1_") && !isBranchDeleted(it.branchId, deletedSet) && !it.createdBy.contains("developer", ignoreCase = true) }
                        .forEach { tx ->
                            repository.saveMoneyTransaction(workspaceId, tx)
                        }
                } else {
                    hasSeededInitialMoneyTxs = true
                    val map = LinkedHashMap<String, MoneyTransactionRecord>()
                    // Start from authoritative Cloud transactions
                    validCloudTxs.forEach { cTx ->
                        if ("money_tx:${cTx.id}" !in pending) {
                            map[cTx.id] = cTx
                        }
                    }
                    // Overlay only pending local writes or developer shadow transactions during active dev session
                    _allMoneyTransactions.value.forEach { localTx ->
                        if (!isBranchDeleted(localTx.branchId, deletedSet) &&
                            "money_tx_delete:${localTx.id}" !in pending &&
                            ("money_tx:${localTx.id}" in pending || (isDeveloperSession() && localTx.createdBy.contains("developer", ignoreCase = true)))
                        ) {
                            map[localTx.id] = localTx
                        }
                    }
                    val merged = map.values.toList()
                    if (merged != _allMoneyTransactions.value) {
                        _allMoneyTransactions.value = merged
                        localStore?.saveMoneyTransactions(merged)
                        consolidateAllDailyPosTransactions(silent = true)
                    }
                }
            }
        }
        // 3. Live Cloud Staff Accounts Mirror (Syncs creations, edits, and deletions across devices)
        viewModelScope.launch {
            repository.observeStaffAccounts(workspaceId).catch { emit(emptyList()) }.collect { cloudStaff ->
                val deletedSet = _deletedBranchIds.value
                val pending = _pendingSyncKeys.value

                // Automatically purge orphaned staff accounts in Cloud for deleted branches
                cloudStaff.filter { isBranchDeleted(it.branchId, deletedSet) }.forEach { orphanAcc ->
                    repository.deleteStaffAccount(workspaceId, orphanAcc.branchId, orphanAcc.username)
                }

                val map = LinkedHashMap<String, StaffAccount>()
                WaffleoSeedData.defaultStaffAccounts().forEach {
                    map["${it.branchId.lowercase()}_${it.username.lowercase()}"] = it
                }
                cloudStaff.filterNot { isBranchDeleted(it.branchId, deletedSet) }.forEach { cAcc ->
                    val k = "${cAcc.branchId.lowercase()}_${cAcc.username.lowercase()}"
                    val delKey = "staff_delete:${cAcc.branchId}|${cAcc.username}"
                    if ("staff:$k" !in pending && delKey !in pending) {
                        map[k] = cAcc
                    }
                }
                _staffAccounts.value.filterNot { isBranchDeleted(it.branchId, deletedSet) }.forEach { localAcc ->
                    val k = "${localAcc.branchId.lowercase()}_${localAcc.username.lowercase()}"
                    if ("staff:$k" in pending) {
                        map[k] = localAcc
                    }
                }
                val merged = map.values.toList()
                if (merged != _staffAccounts.value) {
                    _staffAccounts.value = merged
                    localStore?.saveStaffAccounts(merged)
                }
            }
        }
        // 4. Live Cloud Catalog Mirror (Syncs menu, modifier, promo items & purges deleted branch items)
        viewModelScope.launch {
            repository.observeCatalog(workspaceId).catch { emit(emptyList()) }.collect { cloudCatalog ->
                val deletedSet = _deletedBranchIds.value
                val pending = _pendingSyncKeys.value

                // Purge orphaned catalog items for deleted branches in Cloud
                cloudCatalog.filter { isBranchDeleted(it.branchId, deletedSet) }.forEach { orphanItem ->
                    repository.deleteCatalogItem(workspaceId, orphanItem.id)
                }

                val cleanedCloud = cloudCatalog.filter { rec ->
                    rec.id !in WaffleoSeedData.legacyDummyCatalogIds &&
                        rec.recordType != "INGREDIENT" &&
                        !isBranchDeleted(rec.branchId, deletedSet) &&
                        "catalog_delete:${rec.id}" !in pending
                }
                if (cleanedCloud.isNotEmpty()) {
                    val mergedMap = LinkedHashMap<String, CatalogRecord>()
                    cleanedCloud.forEach { cloudRec ->
                        if ("catalog:${cloudRec.id}" !in pending) {
                            mergedMap[cloudRec.id] = cloudRec
                        }
                    }
                    _localSeededCatalog.value.forEach { localRec ->
                        if (!isBranchDeleted(localRec.branchId, deletedSet) &&
                            "catalog_delete:${localRec.id}" !in pending &&
                            ("catalog:${localRec.id}" in pending || !mergedMap.containsKey(localRec.id))
                        ) {
                            mergedMap[localRec.id] = localRec
                        }
                    }
                    val mergedCatalog = mergedMap.values.toList()
                    if (mergedCatalog != _localSeededCatalog.value) {
                        _localSeededCatalog.value = mergedCatalog
                        localStore?.saveCatalog(mergedCatalog)
                    }
                }
            }
        }
        // 5. Live Cloud Orders Mirror & anti-manipulation sequence high-water mark
        viewModelScope.launch {
            repository.observeOrders(workspaceId).catch { emit(emptyList()) }.collect { cloudOrders ->
                val deletedSet = _deletedBranchIds.value
                val pending = _pendingSyncKeys.value
                val validCloudOrders = cloudOrders.filter {
                    it.id.startsWith("v1_") &&
                        !isBranchDeleted(it.branchId, deletedSet) &&
                        "order_delete:${it.id}" !in pending
                }
                if (validCloudOrders.isNotEmpty()) {
                    validCloudOrders.groupBy { it.branchId }.forEach { (bId, branchCloudOrders) ->
                        val cloudMaxSeq = branchCloudOrders.maxOfOrNull { it.sequenceIndex } ?: 0L
                        val localMaxSeq = localStore?.getHighestRecordedOrderSeq(bId) ?: 0L
                        val localBranchOrders = _localOrders.value.filter { it.branchId.equals(bId, ignoreCase = true) }
                        val currentLocalListMax = localBranchOrders.maxOfOrNull { it.sequenceIndex } ?: 0L

                        if (cloudMaxSeq > currentLocalListMax && currentLocalListMax <= 3L && cloudMaxSeq > 3L && !hasReportedStorageResetAlert) {
                            hasReportedStorageResetAlert = true
                            localStore?.updateHighestRecordedOrderSeq(bId, cloudMaxSeq)
                            logAuditEvent(
                                actionType = "SECURITY_ALERT_STORAGE_RESET",
                                entityId = bId,
                                summary = "Deteksi Pemulihan / Reset Penyimpanan Lokal pada Cabang $bId: Urutan pesanan diselaraskan otomatis ke Cloud (#$cloudMaxSeq) untuk mencegah bentrok nomor pesanan.",
                                beforeSnapshot = """{"localMaxSeq":$currentLocalListMax,"recordedHighSeq":$localMaxSeq}""",
                                afterSnapshot = """{"cloudMaxSeq":$cloudMaxSeq,"status":"RECOVERED"}"""
                            )
                        } else {
                            localStore?.updateHighestRecordedOrderSeq(bId, cloudMaxSeq)
                        }
                    }
                }
                val mergedMap = LinkedHashMap<String, OrderRecord>()
                validCloudOrders.forEach { cOrd ->
                    if ("order:${cOrd.id}" !in pending) {
                        mergedMap[cOrd.id] = cOrd
                    }
                }
                _localOrders.value.forEach { localOrd ->
                    if (!isBranchDeleted(localOrd.branchId, deletedSet) &&
                        "order_delete:${localOrd.id}" !in pending &&
                        ("order:${localOrd.id}" in pending || (isDeveloperSession() && localOrd.createdBy.contains("developer", ignoreCase = true)) || validCloudOrders.isEmpty())
                    ) {
                        mergedMap[localOrd.id] = localOrd
                    }
                }
                val mergedList = mergedMap.values.sortedBy { it.sequenceIndex }
                if (mergedList != _localOrders.value) {
                    _localOrders.value = mergedList
                    localStore?.saveOrders(mergedList)
                }
            }
        }
        // 6. Observe Cloud Audit Logs and persist locally
        viewModelScope.launch {
            repository.observeAuditLogs(workspaceId).catch { emit(emptyList()) }.collect { cloudLogs ->
                val validCloudLogs = cloudLogs.filter { it.id.startsWith("v1_") && !isBranchDeleted(it.branchId) }
                if (validCloudLogs.isNotEmpty()) {
                    val mergedMap = LinkedHashMap<String, AuditLogRecord>()
                    (validCloudLogs + _localAuditLogs.value.filterNot { isBranchDeleted(it.branchId) }).forEach { log ->
                        if (log.id.isNotBlank() && !mergedMap.containsKey(log.id)) {
                            mergedMap[log.id] = log
                        }
                    }
                    val mergedLogs = mergedMap.values.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                    _localAuditLogs.value = mergedLogs
                    localStore?.saveAuditLogs(mergedLogs)
                }
            }
        }
        // 7. Observe Cloud Workspace Settings (Deleted Branch Tombstones, Banks, Wallets, Categories, Subcategories, Bookmarks, Recapped IDs)
        viewModelScope.launch {
            repository.observeWorkspaceSettings(workspaceId).catch { emit(emptyMap()) }.collect { settingsMap ->
                if (settingsMap.isNotEmpty()) {
                    applyCloudWorkspaceSettings(settingsMap)
                }
            }
        }
    }

    private fun markPendingSyncKey(key: String) {
        val next = _pendingSyncKeys.value + key
        _pendingSyncKeys.value = next
        localStore?.savePendingSyncKeys(next)
    }

    private fun clearPendingSyncKey(key: String) {
        if (key !in _pendingSyncKeys.value) return
        val next = _pendingSyncKeys.value - key
        _pendingSyncKeys.value = next
        localStore?.savePendingSyncKeys(next)
    }

    /**
     * Triggered when user taps the Cloud Status Pill to force an immediate bidirectional sync
     * without ever prompting Google Sign-In.
     */
    fun forceFullCloudRefreshAndSync() {
        if (!isNetworkOnline.value) {
            showBanner("Perangkat sedang Offline. Data akan otomatis tersinkron ke Cloud saat internet aktif.")
            return
        }
        flushPendingSyncToCloud()
        showBanner("Cloud Firestore Terhubung Live • Seluruh data cabang & Money Manager sinkron real-time.")
    }

    /**
     * Automatically flushes all offline-queued items (`_pendingSyncKeys`) to Cloud Firestore
     * using deterministic IDs (`docRef.set`) so repeated retries never cause double-input.
     */
    fun flushPendingSyncToCloud() {
        if (!isCloudAuthenticated || !isNetworkOnline.value) return
        val keysToSync = _pendingSyncKeys.value.toList()
        if (keysToSync.isEmpty()) {
            viewModelScope.launch { pushWorkspaceSettingsNow() }
            return
        }

        viewModelScope.launch {
            _isSyncingOfflineQueue.value = true
            var syncedCount = 0
            for (syncKey in keysToSync) {
                val parts = syncKey.split(":", limit = 2)
                if (parts.size < 2) {
                    clearPendingSyncKey(syncKey)
                    continue
                }
                val type = parts[0]
                val entityId = parts[1]
                val ok = when (type) {
                    "order" -> {
                        val ord = _localOrders.value.firstOrNull { it.id == entityId }
                        if (ord != null && !isBranchDeleted(ord.branchId)) repository.updateOrder(workspaceId, ord.copy(userId = workspaceId)) else true
                    }
                    "catalog" -> {
                        val cat = _localSeededCatalog.value.firstOrNull { it.id == entityId }
                        if (cat != null && !isBranchDeleted(cat.branchId)) repository.saveCatalogItem(workspaceId, cat.copy(userId = workspaceId)) else true
                    }
                    "catalog_delete" -> {
                        repository.deleteCatalogItem(workspaceId, entityId)
                        true
                    }
                    "money_tx" -> {
                        val tx = _allMoneyTransactions.value.firstOrNull { it.id == entityId }
                        if (tx != null && !isBranchDeleted(tx.branchId)) repository.saveMoneyTransaction(workspaceId, tx) else true
                    }
                    "money_tx_delete" -> {
                        repository.deleteMoneyTransaction(workspaceId, entityId)
                        true
                    }
                    "staff" -> {
                        val acc = _staffAccounts.value.firstOrNull {
                            "${it.branchId.lowercase()}_${it.username.lowercase()}" == entityId
                        }
                        if (acc != null && !isBranchDeleted(acc.branchId)) repository.saveStaffAccount(workspaceId, acc) else true
                    }
                    "staff_delete" -> {
                        val subParts = entityId.split("|", limit = 2)
                        if (subParts.size == 2) {
                            repository.deleteStaffAccount(workspaceId, subParts[0], subParts[1])
                        } else true
                    }
                    "branch_profile" -> {
                        val prof = _branchProfiles.value.firstOrNull {
                            it.branchId.trim().lowercase() == entityId
                        }
                        if (prof != null && !isBranchDeleted(prof.branchId)) repository.saveBranchProfile(workspaceId, prof) else true
                    }
                    "branch_delete" -> {
                        repository.deleteBranchCascadeInCloud(workspaceId, entityId)
                    }
                    "workspace_settings" -> {
                        pushWorkspaceSettingsNow()
                    }
                    "audit" -> {
                        val log = _localAuditLogs.value.firstOrNull { it.id == entityId }
                        if (log != null) repository.appendAuditLog(workspaceId, log.copy(userId = workspaceId)) else true
                    }
                    else -> true
                }
                if (ok) {
                    clearPendingSyncKey(syncKey)
                    syncedCount++
                }
            }
            _isSyncingOfflineQueue.value = false
            if (syncedCount > 0) {
                showBanner("Auto-Sync Online Berhasil • $syncedCount data tersinkronisasi ke Cloud.")
            }
        }
    }

    private fun seedInitialCatalogToCloudIfNeeded() {
        if (hasSeededCloud) return
        hasSeededCloud = true
        viewModelScope.launch {
            try {
                repository.observeCatalog(workspaceId).catch { emit(emptyList()) }.collect { existing ->
                    val legacyItems = existing.filter {
                        it.id in WaffleoSeedData.legacyDummyCatalogIds || it.recordType == "INGREDIENT"
                    }
                    if (legacyItems.isNotEmpty()) {
                        legacyItems.forEach { oldItem ->
                            repository.deleteCatalogItem(workspaceId, oldItem.id)
                        }
                    }
                    val needsRealMasterData = existing.isEmpty() || legacyItems.isNotEmpty() || existing.none { it.id == "waffle_polos" }
                    if (needsRealMasterData) {
                        val masterRecords = _localSeededCatalog.value.ifEmpty {
                            WaffleoSeedData.defaultCatalogRecords(workspaceId)
                        }
                        masterRecords.forEach { item ->
                            repository.saveCatalogItem(workspaceId, item.copy(userId = workspaceId))
                        }
                    }
                    return@collect
                }
            } catch (e: Exception) {
                Log.w("WaffleoVM", "Initial seed check skipped: ${e.message}")
            }
        }
    }

    private var bannerDismissJob: Job? = null

    fun showBanner(message: String) {
        bannerDismissJob?.cancel()
        _statusBannerMessage.value = message
        bannerDismissJob = viewModelScope.launch {
            delay(2000L)
            _statusBannerMessage.value = null
        }
    }

    fun dismissBanner() {
        bannerDismissJob?.cancel()
        _statusBannerMessage.value = null
    }

    // --- 1-TIME GOOGLE AUTH PER BRANCH & LOGIN HELPERS ---

    fun isBranchGoogleAuthorized(branchId: String): Boolean {
        _authorizedBranchesVersion.value // read state to trigger recomposition if needed
        return localStore?.isBranchGoogleAuthorized(branchId) ?: true
    }

    fun getBranchGoogleAccountLabel(branchId: String): String {
        _authorizedBranchesVersion.value
        return localStore?.getBranchGoogleAccountLabel(branchId)
            ?: "Akun Google Cabang $branchId"
    }

    fun markBranchGoogleAuthorized(branchId: String, googleAccountLabel: String) {
        localStore?.markBranchGoogleAuthorized(branchId, googleAccountLabel)
        _authorizedBranchesVersion.value += 1
        logAuditEvent(
            actionType = "BRANCH_GOOGLE_AUTHORIZED",
            entityId = branchId,
            summary = "Verifikasi 1x Akun Google Cabang $branchId berhasil diotorisasi pada perangkat ini ($googleAccountLabel)",
            beforeSnapshot = """{"authorized":false}""",
            afterSnapshot = """{"authorized":true,"branch":"$branchId","googleAccount":"$googleAccountLabel"}"""
        )
    }

    fun isDeveloperCredentials(usernameInput: String, passwordInput: String): Boolean {
        val cleanUser = usernameInput.trim()
        val devAccount = _staffAccounts.value.firstOrNull {
            it.role == StaffRole.DEVELOPER &&
                it.username.equals(cleanUser, ignoreCase = true) &&
                it.password == passwordInput
        }
        return devAccount != null || (cleanUser.equals("developer", ignoreCase = true) && passwordInput == "developer")
    }

    fun validateStaffCredentials(branch: String, usernameInput: String, passwordInput: String): StaffAccount? {
        val cleanUser = usernameInput.trim()
        return _staffAccounts.value.firstOrNull {
            it.branchId.equals(branch, ignoreCase = true) &&
                it.username.equals(cleanUser, ignoreCase = true) &&
                it.password == passwordInput
        }
    }

    // --- LOGIN & TOP BAR ACTIONS ---

    fun loginStaff(branch: String, usernameInput: String, passwordInput: String, rememberMe: Boolean): Boolean {
        val cleanUser = usernameInput.trim()
        val nowText = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(Date())

        // Master Developer check (Mutlak & Kasta Tertinggi across all branches)
        val devAccount = _staffAccounts.value.firstOrNull {
            it.role == StaffRole.DEVELOPER &&
                it.username.equals(cleanUser, ignoreCase = true) &&
                it.password == passwordInput
        }
        if (devAccount != null || (cleanUser.equals("developer", ignoreCase = true) && passwordInput == "developer")) {
            updateAndPersistSession(
                ActivePosSession(
                    branchId = "Global",
                    username = devAccount?.username ?: "developer",
                    role = StaffRole.DEVELOPER,
                    displayName = devAccount?.displayName ?: "System Developer (Master)",
                    rememberMe = rememberMe,
                    loginTimeFormatted = nowText,
                    isLoggedIn = true,
                    isInDeveloperPortal = true
                )
            )
            logAuditEvent(
                actionType = "DEVELOPER_LOGIN",
                entityId = "developer",
                summary = "Master Developer login ke Portal Manajemen Multi-Cabang",
                beforeSnapshot = "{}",
                afterSnapshot = """{"user":"developer","role":"Developer","portal":true}"""
            )
            showBanner("Login Developer Berhasil • Pilih Cabang atau Tambah Cabang Baru")
            return true
        }

        val account = validateStaffCredentials(branch, cleanUser, passwordInput)
        return if (account != null) {
            updateAndPersistSession(
                ActivePosSession(
                    branchId = account.branchId,
                    username = account.username,
                    role = account.role,
                    displayName = account.displayName,
                    rememberMe = rememberMe,
                    loginTimeFormatted = nowText,
                    isLoggedIn = true,
                    isInDeveloperPortal = false
                )
            )
            if (!account.role.canAccessMoneyManager && _currentDestination.value == MainNavDestination.MONEY_MANAGER) {
                _currentDestination.value = MainNavDestination.MENUS
            }
            localStore?.saveLastLoginBranch(account.branchId)
            logAuditEvent(
                actionType = "STAFF_LOGIN",
                entityId = account.username,
                summary = "Login berhasil di cabang ${account.branchId} sebagai ${account.role.label}",
                beforeSnapshot = "{}",
                afterSnapshot = """{"branch":"${account.branchId}","user":"${account.username}","role":"${account.role.label}"}"""
            )
            showBanner("Selamat datang, ${account.displayName} (${getBranchDisplayName(account.branchId)})")
            true
        } else {
            false
        }
    }

    fun enterBranchAsDeveloper(targetBranch: String) {
        val current = _session.value
        if (current.role != StaffRole.DEVELOPER) return
        purgeDeveloperTestOrdersAndData()
        localStore?.saveLastLoginBranch(targetBranch)
        clearCart()
        autoRepairEmptyBranchCatalogIfNeeded(targetBranch)
        updateAndPersistSession(
            current.copy(
                branchId = targetBranch,
                isInDeveloperPortal = false
            )
        )
        _currentDestination.value = MainNavDestination.MENUS
        val displayTarget = getBranchDisplayName(targetBranch)
        logAuditEvent(
            actionType = "DEV_ENTER_BRANCH",
            entityId = targetBranch,
            summary = "Developer masuk ke ruang operasional Cabang $displayTarget (Full Access)",
            beforeSnapshot = """{"branch":"${current.branchId}","portal":true}""",
            afterSnapshot = """{"branch":"$targetBranch","portal":false}"""
        )
        showBanner("Developer aktif di Cabang $displayTarget (Full Access)")
    }

    fun deleteBranchPermanently(branchId: String, devUsername: String, devPassword: String): Result<Unit> {
        val cleanBranch = branchId.trim()
        if (cleanBranch.equals("Global", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Cabang 'Global' adalah Master System dan tidak dapat dihapus."))
        }
        val isDevValid = (devUsername.trim().equals("developer", ignoreCase = true) && devPassword.trim() == "developer") ||
            _staffAccounts.value.any { it.role == StaffRole.DEVELOPER && it.username.equals(devUsername.trim(), ignoreCase = true) && it.password == devPassword.trim() }

        if (!isDevValid) {
            showBanner("Username / Password salah")
            return Result.failure(IllegalArgumentException("Username / Password salah"))
        }

        val profile = _branchProfiles.value.firstOrNull { it.branchId.equals(cleanBranch, ignoreCase = true) || it.name.equals(cleanBranch, ignoreCase = true) }
        val resolvedBranchId = profile?.branchId ?: cleanBranch
        val resolvedName = profile?.name?.ifBlank { null } ?: profile?.displayName ?: cleanBranch

        val baseSlug = resolvedName.lowercase().removePrefix("cabang ").trim()
        val branchVariants = setOf(
            resolvedBranchId.lowercase(),
            resolvedName.lowercase(),
            baseSlug,
            "cabang $baseSlug"
        )
        val deletedSet = _deletedBranchIds.value + branchVariants
        _deletedBranchIds.value = deletedSet
        localStore?.saveDeletedBranchIds(deletedSet)

        fun isBranchMatch(candidate: String): Boolean {
            val c = candidate.trim().lowercase()
            return c in branchVariants || c == resolvedBranchId.lowercase() || c.removePrefix("cabang ").trim() == baseSlug
        }

        // Capture items to delete BEFORE updating StateFlows
        val staffToDelete = _staffAccounts.value.filter { isBranchMatch(it.branchId) }
        val catalogToDelete = _localSeededCatalog.value.filter { isBranchMatch(it.branchId) }
        val txsToDelete = _allMoneyTransactions.value.filter { isBranchMatch(it.branchId) }

        val updatedProfiles = _branchProfiles.value.filterNot { isBranchMatch(it.branchId) }
        val updatedBranches = _branches.value.filterNot { isBranchMatch(it) || it.equals(resolvedBranchId, ignoreCase = true) }
        _branchProfiles.value = updatedProfiles
        _branches.value = updatedBranches
        localStore?.saveBranchesAndProfiles(updatedBranches, updatedProfiles)

        val updatedStaff = _staffAccounts.value.filterNot { isBranchMatch(it.branchId) }
        _staffAccounts.value = updatedStaff
        localStore?.saveStaffAccounts(updatedStaff)

        val updatedCatalog = _localSeededCatalog.value.filterNot { isBranchMatch(it.branchId) }
        _localSeededCatalog.value = updatedCatalog
        localStore?.saveCatalog(updatedCatalog)

        val updatedOrders = _localOrders.value.filterNot { isBranchMatch(it.branchId) }
        _localOrders.value = updatedOrders
        localStore?.saveOrders(updatedOrders)

        val updatedTxs = _allMoneyTransactions.value.filterNot { isBranchMatch(it.branchId) }
        _allMoneyTransactions.value = updatedTxs
        localStore?.saveMoneyTransactions(updatedTxs)

        // Purge local store caches for this branch
        localStore?.purgeBranchDataLocally(resolvedBranchId)
        localStore?.purgeBranchDataLocally(resolvedName)

        // Return session to Global if current session was on deleted branch
        if (_session.value.branchId.equals(resolvedBranchId, ignoreCase = true) || _session.value.branchId.equals(resolvedName, ignoreCase = true)) {
            updateAndPersistSession(
                _session.value.copy(
                    branchId = "Global",
                    isInDeveloperPortal = true
                )
            )
        }

        // Clear any pending creation keys for this branch and mark branch_delete key
        branchVariants.forEach { clearPendingSyncKey("branch_profile:$it") }
        staffToDelete.forEach { clearPendingSyncKey("staff:${it.branchId.lowercase()}_${it.username.lowercase()}") }
        catalogToDelete.forEach { clearPendingSyncKey("catalog:${it.id}") }
        txsToDelete.forEach { clearPendingSyncKey("money_tx:${it.id}") }

        val delKey = "branch_delete:${resolvedBranchId.lowercase()}"
        markPendingSyncKey(delKey)
        syncWorkspaceSettingsToCloud()

        if (isCloudAuthenticated && isNetworkOnline.value) {
            viewModelScope.launch {
                staffToDelete.forEach {
                    repository.deleteStaffAccount(workspaceId, it.branchId, it.username)
                }
                catalogToDelete.forEach {
                    repository.deleteCatalogItem(workspaceId, it.id)
                }
                txsToDelete.forEach {
                    repository.deleteMoneyTransaction(workspaceId, it.id)
                }
                val ok = repository.deleteBranchCascadeInCloud(workspaceId, resolvedBranchId)
                if (ok) clearPendingSyncKey(delKey)
                pushWorkspaceSettingsNow()
            }
        }

        logAuditEvent(
            actionType = "BRANCH_DELETED",
            entityId = resolvedName,
            summary = "Cabang '$resolvedName' ($resolvedBranchId) dihapus secara permanen dari sistem & Cloud oleh Developer ($devUsername)",
            beforeSnapshot = """{"branch":"$resolvedName","id":"$resolvedBranchId"}""",
            afterSnapshot = "{}"
        )

        showBanner("Cabang '$resolvedName' berhasil dihapus secara permanen dari seluruh perangkat.")
        return Result.success(Unit)
    }

    fun autoRepairEmptyBranchCatalogIfNeeded(branchId: String) {
        if (branchId.isBlank()) return
        // STRICT SAFETY: Never auto-repair non-Global branches.
        // Branches created as 'Empty' must stay empty.
        // Branches created with 'Salin Data' are populated explicitly by createNewBranchByDeveloper.
        if (!branchId.equals("Global", ignoreCase = true)) return

        val existingItems = _localSeededCatalog.value.filter {
            it.branchId.equals(branchId, ignoreCase = true) && it.recordType != "BRANCH_CONFIG"
        }
        if (existingItems.isEmpty()) {
            val defaultSeed = WaffleoSeedData.defaultCatalogRecords(workspaceId).filter {
                it.recordType in listOf("MENU_ITEM", "MODIFIER", "PROMO")
            }
            val repairedRecords = defaultSeed.map { rec ->
                val cleanCat = rec.category.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
                val cleanName = rec.name.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
                rec.copy(
                    id = "cat_global_${rec.recordType.lowercase()}_${cleanCat}_${cleanName}".take(90),
                    userId = workspaceId,
                    branchId = "Global",
                    createdAt = null,
                    updatedAt = null
                )
            }.distinctBy { it.id }
            val merged = (_localSeededCatalog.value.filterNot { it.branchId.equals("Global", ignoreCase = true) } + repairedRecords).distinctBy { it.id }
            _localSeededCatalog.value = merged
            localStore?.saveCatalog(merged)
            repairedRecords.forEach { syncCatalogItemToCloud(it) }
        }
    }

    fun openDeveloperBranchPortal() {
        val current = _session.value
        if (current.role != StaffRole.DEVELOPER) return
        purgeDeveloperTestOrdersAndData()
        updateAndPersistSession(current.copy(isInDeveloperPortal = true))
    }

    fun createNewBranchByDeveloper(
        branchName: String,
        location: String,
        ownerName: String,
        ownerEmail: String,
        ownerPhone: String,
        ownerUsername: String,
        ownerPassword: String,
        copyFromBranch: String? // null or blank = Empty (manual input by owner), else clone from source branch
    ): Result<String> {
        val cleanBranch = branchName.trim()
        val cleanOwnerUser = ownerUsername.trim()
        if (cleanBranch.isEmpty()) {
            return Result.failure(IllegalArgumentException("Nama cabang wajib diisi."))
        }
        if (_branches.value.any { b ->
                val p = _branchProfiles.value.firstOrNull { it.branchId == b }
                (p?.name?.equals(cleanBranch, ignoreCase = true) == true) || b.equals(cleanBranch, ignoreCase = true)
            }) {
            return Result.failure(IllegalArgumentException("Cabang '$cleanBranch' sedang aktif terdaftar."))
        }
        if (cleanOwnerUser.isEmpty() || ownerPassword.isEmpty()) {
            return Result.failure(IllegalArgumentException("Username dan Password Owner cabang wajib diisi."))
        }
        if (cleanOwnerUser.equals("developer", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Username 'developer' bersifat mutlak dan tidak dapat digunakan untuk akun cabang."))
        }

        val nowText = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(Date())
        val initModeLabel = if (!copyFromBranch.isNullOrBlank()) "Salin Data dari $copyFromBranch" else "Kosong (Input Manual Owner)"

        // Generate a 100% unique UUID Branch ID for this new branch entity
        val newBranchId = "branch_" + java.util.UUID.randomUUID().toString()

        // 1. Register branch & branch profile locally
        _branches.value = (_branches.value + newBranchId).distinctBy { it.lowercase() }
        val profile = BranchProfile(
            branchId = newBranchId,
            name = cleanBranch,
            location = location.trim().ifEmpty { "Lokasi belum diatur" },
            ownerName = ownerName.trim().ifEmpty { "Owner $cleanBranch" },
            ownerEmail = ownerEmail.trim(),
            ownerPhone = ownerPhone.trim(),
            ownerUsername = cleanOwnerUser,
            initializedFrom = initModeLabel,
            createdAtText = nowText
        )
        _branchProfiles.value = (_branchProfiles.value.filterNot { it.branchId.equals(newBranchId, ignoreCase = true) } + profile)
        localStore?.saveBranchesAndProfiles(_branches.value, _branchProfiles.value)

        // 2. Register Owner StaffAccount locally
        val ownerAcc = StaffAccount(
            branchId = newBranchId,
            username = cleanOwnerUser,
            password = ownerPassword,
            role = StaffRole.OWNER,
            displayName = ownerName.trim().ifEmpty { "Owner $cleanBranch" },
            email = ownerEmail.trim(),
            phone = ownerPhone.trim()
        )
        _staffAccounts.value = (_staffAccounts.value.filterNot {
            it.branchId.equals(newBranchId, ignoreCase = true) && it.username.equals(cleanOwnerUser, ignoreCase = true)
        } + ownerAcc)
        localStore?.saveStaffAccounts(_staffAccounts.value)

        // 3. Prepare catalog data for the new branch (Copy vs Empty)
        val newRecords: List<CatalogRecord> = if (!copyFromBranch.isNullOrBlank()) {
            val pool = (allCatalogRecords.value + _localSeededCatalog.value).distinctBy { it.id }
            var sourceRecords = pool.filter {
                (it.branchId.equals(copyFromBranch, ignoreCase = true) ||
                 (_branchProfiles.value.firstOrNull { p -> p.branchId == it.branchId }?.name?.equals(copyFromBranch, ignoreCase = true) == true)) &&
                it.recordType in listOf("MENU_ITEM", "MODIFIER", "PROMO")
            }
            if (sourceRecords.isEmpty() && copyFromBranch.equals("Global", ignoreCase = true)) {
                sourceRecords = WaffleoSeedData.defaultCatalogRecords(workspaceId).filter {
                    it.recordType in listOf("MENU_ITEM", "MODIFIER", "PROMO")
                }
            }
            val deduplicatedSource = sourceRecords.distinctBy {
                "${it.recordType}|${it.category.trim().lowercase()}|${it.name.trim().lowercase()}|${it.subCategory.trim().lowercase()}"
            }
            deduplicatedSource.map { rec ->
                val cleanCat = rec.category.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
                val cleanName = rec.name.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
                val itemSlug = "cat_${newBranchId.take(16)}_${rec.recordType.lowercase()}_${cleanCat}_${cleanName}_${java.util.UUID.randomUUID().toString().take(6)}"
                rec.copy(
                    id = itemSlug,
                    userId = workspaceId,
                    branchId = newBranchId,
                    createdAt = null,
                    updatedAt = null
                )
            }.distinctBy { it.id }
        } else {
            emptyList()
        }

        // Wipe any residual local records for newBranchId before saving newRecords
        val stripped = _localSeededCatalog.value.filterNot { it.branchId.equals(newBranchId, ignoreCase = true) }
        val merged = (stripped + newRecords).distinctBy { it.id }
        _localSeededCatalog.value = merged
        localStore?.saveCatalog(merged)

        // Mark pending sync keys immediately so reconcileDiscoveredBranches protects the new branch
        val profKey = "branch_profile:${newBranchId.lowercase()}"
        val staffKey = "staff:${newBranchId.lowercase()}_${cleanOwnerUser.lowercase()}"
        markPendingSyncKey(profKey)
        markPendingSyncKey(staffKey)
        markPendingSyncKey("workspace_settings:all")
        newRecords.forEach { markPendingSyncKey("catalog:${it.id}") }

        // 4. Sequentially execute Cloud operations: Write new branch profile, staff, settings, and catalog
        viewModelScope.launch {
            if (isCloudAuthenticated && isNetworkOnline.value) {
                // Step A: Save new BranchProfile & StaffAccount to Cloud
                if (repository.saveBranchProfile(workspaceId, profile)) {
                    clearPendingSyncKey(profKey)
                }
                if (repository.saveStaffAccount(workspaceId, ownerAcc)) {
                    clearPendingSyncKey(staffKey)
                }
                if (pushWorkspaceSettingsNow()) {
                    clearPendingSyncKey("workspace_settings:all")
                }

                // Step B: Save all copied catalog items to Cloud
                newRecords.forEach { item ->
                    if (repository.saveCatalogItem(workspaceId, item.copy(userId = workspaceId))) {
                        clearPendingSyncKey("catalog:${item.id}")
                    }
                }
            }

            logAuditEvent(
                actionType = "BRANCH_CREATED",
                entityId = newBranchId,
                summary = "Cabang baru '$cleanBranch' (ID: $newBranchId) dibuat oleh Developer (Owner: $cleanOwnerUser • Mode Data: $initModeLabel)",
                beforeSnapshot = "{}",
                afterSnapshot = """{"branch":"$cleanBranch","branchId":"$newBranchId","location":"${profile.location}","owner":"$cleanOwnerUser","initMode":"$initModeLabel","itemsCopied":${newRecords.size}}"""
            )
            showBanner("Cabang '$cleanBranch' berhasil dibuat & disinkronkan ke Cloud ($initModeLabel).")
        }

        return Result.success(newBranchId)
    }

    fun logoutStaff() {
        val prev = _session.value
        val pendingCount = _pendingSyncKeys.value.size
        if (localStore != null && !isNetworkOnline.value && pendingCount > 0 && prev.role != StaffRole.DEVELOPER) {
            showBanner("Peringatan Keamanan: Terdapat $pendingCount data transaksi offline yang belum tersinkronisasi. Nyalakan internet terlebih dahulu sebelum Log Out.")
            return
        }

        if (prev.role == StaffRole.DEVELOPER || prev.branchId.equals("Global", ignoreCase = true)) {
            purgeDeveloperTestOrdersAndData()
        }

        performLocalCacheSanitization()
        logAuditEvent(
            actionType = "STAFF_LOGOUT",
            entityId = prev.username,
            summary = "Logout dari cabang ${prev.branchId}",
            beforeSnapshot = """{"user":"${prev.username}","loginTime":"${prev.loginTimeFormatted}"}""",
            afterSnapshot = """{"isLoggedIn":false}"""
        )
        updateAndPersistSession(prev.copy(isLoggedIn = false))
    }

    private fun purgeDeveloperTestOrdersAndData() {
        val currentOrders = _localOrders.value
        val cleanedOrders = currentOrders.filter { ord ->
            !ord.createdBy.contains("developer", ignoreCase = true) &&
                ord.orderNumber != "WFL-001" &&
                ord.id != "WFL-001"
        }
        _localOrders.value = cleanedOrders
        localStore?.saveOrders(cleanedOrders)

        val currentTxs = _allMoneyTransactions.value
        val cleanedTxs = currentTxs.filter { tx ->
            !tx.createdBy.contains("developer", ignoreCase = true)
        }
        _allMoneyTransactions.value = cleanedTxs
        localStore?.saveMoneyTransactions(cleanedTxs)

        val currentLogs = _localAuditLogs.value
        val cleanedLogs = currentLogs.filter { log ->
            !log.actorUsername.contains("developer", ignoreCase = true) &&
            !log.actorRole.contains("developer", ignoreCase = true)
        }
        _localAuditLogs.value = cleanedLogs
        localStore?.saveAuditLogs(cleanedLogs)

        val purgedOrderIds = currentOrders.filterNot { it in cleanedOrders.toSet() }.map { it.id }.toSet()
        if (purgedOrderIds.isNotEmpty()) {
            val nextRecapped = _recappedOrderIds.value - purgedOrderIds
            _recappedOrderIds.value = nextRecapped
            localStore?.saveRecappedOrderIds(nextRecapped)
        }

        clearCart()
        _isMaintenanceModeOn.value = false
    }

    /**
     * Complete local cache sanitization mechanism that ensures leftover ghost orders, erased records,
     * orphan developer test data, or deleted branch entries are thoroughly purged on app start,
     * logout, or session/branch switching.
     */
    fun performLocalCacheSanitization() {
        val deletedSet = _deletedBranchIds.value
        val pendingKeys = _pendingSyncKeys.value

        // 1. Purge records belonging to explicitly deleted branches
        purgeDeletedBranchesFromLocalState(deletedSet)

        // 2. Clean stale or ghost orders from local state
        val isDev = isDeveloperSession()
        val currentOrders = _localOrders.value
        val sanitizedOrders = currentOrders.filter { ord ->
            val isBranchValid = !isBranchDeleted(ord.branchId, deletedSet)
            val isNotErased = ord.status != "ERASED"
            val isNotGhostDelete = "order_delete:${ord.id}" !in pendingKeys
            val isNotOrphanDevTest = isDev || (!ord.createdBy.contains("developer", ignoreCase = true) && ord.orderNumber != "WFL-001" && ord.id != "WFL-001")
            isBranchValid && isNotErased && isNotGhostDelete && isNotOrphanDevTest
        }

        if (sanitizedOrders.size != currentOrders.size) {
            _localOrders.value = sanitizedOrders
            localStore?.saveOrders(sanitizedOrders)
        }

        // 3. Clean money transactions from deleted branches or orphan dev entries
        val currentTxs = _allMoneyTransactions.value
        val sanitizedTxs = currentTxs.filter { tx ->
            !isBranchDeleted(tx.branchId, deletedSet) &&
            (isDev || !tx.createdBy.contains("developer", ignoreCase = true))
        }
        if (sanitizedTxs.size != currentTxs.size) {
            _allMoneyTransactions.value = sanitizedTxs
            localStore?.saveMoneyTransactions(sanitizedTxs)
        }

        // 4. Reset cart and temporary editing state
        clearCart()
        _editingOrder.value = null
    }

    fun toggleStoreOpenStatus() {
        val before = _isStoreOpen.value
        val after = !before
        _isStoreOpen.value = after
        localStore?.saveStoreOpenStatus(_session.value.branchId, after)
        syncWorkspaceSettingsToCloud()
        val statusLabel = if (after) "BUKA (OPEN)" else "TUTUP (CLOSED)"
        if (before && !after) {
            // Setiap kali Toko dinyatakan TUTUP, lakukan rekapitulasi shift terhadap income dan input otomatis ke Money Manager
            val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayKey = dateFmt.format(Date())
            val todayOrders = orders.value.filter { ord ->
                ord.isPaid && ord.status != "CANCELLED" && ord.status != "ERASED" &&
                    !isBackdatedOrder(ord) &&
                    dateFmt.format(resolveOrderTimestampDate(ord)) == todayKey
            }
            val shiftUnrecappedOrders = todayOrders.filter { it.id !in _recappedOrderIds.value }
            val recapItems = computeStoreCloseDailyRecap(shiftUnrecappedOrders, catalogRecords.value, onlyToday = true, ignoreAlreadyRecapped = false)
            val totalNetIncome = recapItems.sumOf { it.netAmount }
            val channelCount = recapItems.size
            syncPosSalesToMoneyManager(silent = true, triggeredByStoreClose = true)
            val displayBranch = getBranchDisplayName(_session.value.branchId)
            logAuditEvent(
                actionType = "STORE_CLOSED_AUTO_SYNC",
                entityId = _session.value.branchId,
                summary = "Toko cabang $displayBranch DITUTUP • Rekapitulasi Shift (${shiftUnrecappedOrders.size} pesanan, ${formatIdr(totalNetIncome)}) otomatis disinkronkan ke Money Manager",
                beforeSnapshot = """{"isStoreOpen":$before}""",
                afterSnapshot = """{"isStoreOpen":$after,"shiftOrders":${shiftUnrecappedOrders.size},"autoSyncedChannels":$channelCount,"totalNetIncome":$totalNetIncome}"""
            )
            if (channelCount > 0) {
                showBanner("Toko $displayBranch Ditutup • Rekap Shift ${formatIdr(totalNetIncome)} (${shiftUnrecappedOrders.size} pesanan) tersinkronisasi ke Money Manager")
            } else {
                showBanner("Status Toko $displayBranch: $statusLabel")
            }
        } else {
            val displayBranch = getBranchDisplayName(_session.value.branchId)
            logAuditEvent(
                actionType = "STORE_STATUS",
                entityId = _session.value.branchId,
                summary = "Status toko cabang $displayBranch diubah menjadi $statusLabel",
                beforeSnapshot = """{"isStoreOpen":$before}""",
                afterSnapshot = """{"isStoreOpen":$after}"""
            )
            showBanner("Status Toko $displayBranch: $statusLabel")
        }
    }

    fun navigateTo(destination: MainNavDestination) {
        if (destination == MainNavDestination.MONEY_MANAGER && !_session.value.role.canAccessMoneyManager) {
            showBanner("Akses Money Manager hanya untuk Supervisor, Owner, dan Developer.")
            return
        }
        _currentDestination.value = destination
    }

    fun selectMenuTab(tab: MenuCategoryTab) {
        _selectedMenuTab.value = tab
    }

    // --- EGG WAFFLE & DRINKS PRICE CALCULATION ---

    /**
     * Calculates Egg Waffle price for a specific OrderMedia.
     * When 1/2 + 1/2 is checked with 2 primary toppings:
     * Average of Primary Topping A and Primary Topping B + Flavour + Sum(Add-ons).
     * When Double Topping is checked with 2 primary toppings:
     * Topping Utama 1 + Topping Utama 2 - Harga Waffle Polos + Flavour + Sum(Add-ons).
     */
    fun calculateEggWafflePriceForMedia(
        media: OrderMedia,
        primary1: CatalogRecord,
        isHalfHalf: Boolean,
        isDoubleTopping: Boolean = false,
        primary2: CatalogRecord?,
        flavour: CatalogRecord?,
        addOns: List<CatalogRecord>
    ): Long {
        val safeDouble = if (media.isPlatform) false else isDoubleTopping
        val safeHalf = if (media.isPlatform) false else isHalfHalf
        val p1 = primary1.priceForMedia(media)
        val p2 = primary2?.priceForMedia(media) ?: 0L
        val basePrice = when {
            safeDouble && primary2 != null -> {
                val polosRecord = catalogRecords.value.firstOrNull {
                    it.recordType == "MENU_ITEM" && (it.id == "waffle_polos" || it.name.equals("Polos", ignoreCase = true))
                }
                val polosPrice = polosRecord?.priceForMedia(media) ?: when (media) {
                    OrderMedia.WALK_IN, OrderMedia.WHATSAPP, OrderMedia.INSTAGRAM -> 13000L
                    OrderMedia.GRABFOOD, OrderMedia.GOFOOD -> 17000L
                    OrderMedia.SHOPEEFOOD -> 17500L
                }
                (p1 + p2 - polosPrice).coerceAtLeast(p1)
            }
            safeHalf && primary2 != null -> {
                ((p1 + p2) / 2.0).roundToLong()
            }
            else -> p1
        }
        val flavourPrice = flavour?.priceForMedia(media) ?: 0L
        val addOnTotal = addOns.sumOf { it.priceForMedia(media) }
        return basePrice + flavourPrice + addOnTotal
    }

    fun addEggWaffleToCart(
        primary1: CatalogRecord,
        isHalfHalf: Boolean,
        isDoubleTopping: Boolean = false,
        primary2: CatalogRecord?,
        flavour: CatalogRecord?,
        addOns: List<CatalogRecord>,
        qty: Int = 1
    ) {
        if (!_isStoreOpen.value) {
            showBanner("Toko sedang tutup. Buka toko terlebih dahulu untuk melakukan transaksi.")
            return
        }
        val canMultiTopping = !_cartMedia.value.isPlatform
        val effHalf = canMultiTopping && isHalfHalf && primary2 != null
        val effDouble = canMultiTopping && isDoubleTopping && primary2 != null
        val flvName = flavour?.name ?: "Original"
        val addOnNames = addOns.map { it.name }.sorted()

        val walkInPrice = calculateEggWafflePriceForMedia(OrderMedia.WALK_IN, primary1, effHalf, effDouble, primary2, flavour, addOns)
        val grabPrice = calculateEggWafflePriceForMedia(OrderMedia.GRABFOOD, primary1, effHalf, effDouble, primary2, flavour, addOns)
        val goPrice = calculateEggWafflePriceForMedia(OrderMedia.GOFOOD, primary1, effHalf, effDouble, primary2, flavour, addOns)
        val shopeePrice = calculateEggWafflePriceForMedia(OrderMedia.SHOPEEFOOD, primary1, effHalf, effDouble, primary2, flavour, addOns)

        val addQty = qty.coerceAtLeast(1)
        val currentList = _cartItems.value.toMutableList()
        val existingIdx = currentList.indexOfFirst {
            it.category == "Egg Waffle" &&
                it.primaryItemId == primary1.id &&
                it.isHalfHalf == effHalf &&
                it.isDoubleTopping == effDouble &&
                it.secondaryItemId == (if (effHalf || effDouble) primary2?.id.orEmpty() else "") &&
                it.flavourName == flvName &&
                it.addOnNames == addOnNames
        }

        if (existingIdx >= 0) {
            val item = currentList[existingIdx]
            currentList[existingIdx] = item.copy(qty = item.qty + addQty)
        } else {
            currentList.add(
                CartLineItem(
                    lineId = "line_${UUID.randomUUID().toString().take(8)}",
                    category = "Egg Waffle",
                    primaryItemId = primary1.id,
                    primaryItemName = primary1.name,
                    isHalfHalf = effHalf,
                    isDoubleTopping = effDouble,
                    secondaryItemId = if (effHalf || effDouble) primary2?.id.orEmpty() else "",
                    secondaryItemName = if (effHalf || effDouble) primary2?.name.orEmpty() else "",
                    flavourName = flvName,
                    addOnNames = addOnNames,
                    qty = addQty,
                    unitPriceWalkIn = walkInPrice,
                    unitPriceGrabFood = grabPrice,
                    unitPriceGoFood = goPrice,
                    unitPriceShopeeFood = shopeePrice
                )
            )
        }
        _cartItems.value = currentList
        reconcileCartPrepaymentAfterChange()
    }

    fun addDrinkToCart(
        drink: CatalogRecord,
        sweetnessLevel: String,
        icedLevel: String,
        qty: Int = 1
    ) {
        if (!_isStoreOpen.value) {
            showBanner("Toko sedang tutup. Buka toko terlebih dahulu untuk melakukan transaksi.")
            return
        }
        val addQty = qty.coerceAtLeast(1)
        val currentList = _cartItems.value.toMutableList()
        val existingIdx = currentList.indexOfFirst {
            it.category == "Drinks" &&
                it.primaryItemId == drink.id &&
                it.sweetnessLevel == sweetnessLevel &&
                it.icedLevel == icedLevel
        }

        if (existingIdx >= 0) {
            val item = currentList[existingIdx]
            currentList[existingIdx] = item.copy(qty = item.qty + addQty)
        } else {
            currentList.add(
                CartLineItem(
                    lineId = "line_${UUID.randomUUID().toString().take(8)}",
                    category = "Drinks",
                    primaryItemId = drink.id,
                    primaryItemName = drink.name,
                    sweetnessLevel = sweetnessLevel,
                    icedLevel = icedLevel,
                    qty = addQty,
                    unitPriceWalkIn = drink.priceWalkIn,
                    unitPriceGrabFood = drink.priceGrabFood,
                    unitPriceGoFood = drink.priceGoFood,
                    unitPriceShopeeFood = drink.priceShopeeFood
                )
            )
        }
        _cartItems.value = currentList
        reconcileCartPrepaymentAfterChange()
    }

    fun updateCartItemQuantity(lineId: String, delta: Int) {
        if (!_isStoreOpen.value && delta > 0) {
            showBanner("Toko sedang tutup. Buka toko terlebih dahulu untuk melakukan transaksi.")
            return
        }
        val updated = _cartItems.value.mapNotNull { item ->
            if (item.lineId == lineId) {
                val newQty = item.qty + delta
                if (newQty <= 0) null else item.copy(qty = newQty)
            } else {
                item
            }
        }
        _cartItems.value = updated
        reconcileCartPrepaymentAfterChange()
    }

    fun setCartOrderMedia(media: OrderMedia) {
        val prevMedia = _cartMedia.value
        _cartMedia.value = media
        // Strip old prefix if any so customerInputSuffix stays clean
        if (prevMedia != media) {
            _customerInputSuffix.value = _customerInputSuffix.value
                .removePrefix("GF - ")
                .removePrefix("F - ")
                .removePrefix("# ")
                .trim()
            if (prevMedia.isPlatform && !media.isPlatform && _editingOrder.value == null) {
                _cartIsPaid.value = false
                _cartPaidAmount.value = 0L
                _cartPaymentMethod.value = "Belum Bayar"
            }
            if (media.isPlatform) {
                // Grabfood, Gofood, dan Shopeefood tidak bisa Kombinasi Porsi 1/2 dan Double Topping
                val hasMultiTopping = _cartItems.value.any { it.isHalfHalf || it.isDoubleTopping }
                if (hasMultiTopping) {
                    _cartItems.value = _cartItems.value.map { item ->
                        if (item.category == "Egg Waffle" && (item.isHalfHalf || item.isDoubleTopping)) {
                            val p1 = catalogRecords.value.firstOrNull { it.id == item.primaryItemId }
                            val flv = catalogRecords.value.firstOrNull { it.name.equals(item.flavourName, ignoreCase = true) }
                            val adds = catalogRecords.value.filter { it.name in item.addOnNames }
                            if (p1 != null) {
                                val walkIn = calculateEggWafflePriceForMedia(OrderMedia.WALK_IN, p1, false, false, null, flv, adds)
                                val grab = calculateEggWafflePriceForMedia(OrderMedia.GRABFOOD, p1, false, false, null, flv, adds)
                                val go = calculateEggWafflePriceForMedia(OrderMedia.GOFOOD, p1, false, false, null, flv, adds)
                                val shopee = calculateEggWafflePriceForMedia(OrderMedia.SHOPEEFOOD, p1, false, false, null, flv, adds)
                                item.copy(
                                    isHalfHalf = false,
                                    isDoubleTopping = false,
                                    secondaryItemId = "",
                                    secondaryItemName = "",
                                    unitPriceWalkIn = walkIn,
                                    unitPriceGrabFood = grab,
                                    unitPriceGoFood = go,
                                    unitPriceShopeeFood = shopee
                                )
                            } else {
                                item.copy(
                                    isHalfHalf = false,
                                    isDoubleTopping = false,
                                    secondaryItemId = "",
                                    secondaryItemName = ""
                                )
                            }
                        } else {
                            item
                        }
                    }
                }
            }
        }
        reconcileCartPrepaymentAfterChange()
    }

    private fun capitalizeWords(input: String): String {
        if (input.isEmpty()) return ""
        return input.split(" ").joinToString(" ") { word ->
            if (word.isEmpty()) ""
            else word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    fun setCustomerInputSuffix(value: String) {
        // Keep only the suffix without repeating the locked prefix
        val cleaned = value
            .removePrefix("GF - ")
            .removePrefix("F - ")
            .removePrefix("# ")
        _customerInputSuffix.value = capitalizeWords(cleaned)
    }

    fun formattedCustomerData(): String {
        val media = _cartMedia.value
        val suffix = _customerInputSuffix.value.trim()
        return if (media.prefix.isNotEmpty()) {
            if (suffix.isEmpty()) "" else "${media.prefix}$suffix"
        } else {
            suffix
        }
    }

    fun setCartNotes(notes: String) {
        _cartNotes.value = capitalizeWords(notes)
    }

    fun setSelectedPromo(promoId: String) {
        _selectedPromoId.value = promoId
        reconcileCartPrepaymentAfterChange()
    }

    fun setCartVoucherCode(code: String) {
        _cartVoucherCode.value = code.trim().uppercase()
        reconcileCartPrepaymentAfterChange()
    }

    fun setCartBackdatedEnabled(enabled: Boolean) {
        if (!_session.value.role.canInputBackdatedOrder) {
            _cartIsBackdated.value = false
            return
        }
        _cartIsBackdated.value = enabled
        if (enabled) {
            if (_cartBackdatedDateKey.value.isBlank()) {
                _cartBackdatedDateKey.value = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            }
            if (_cartBackdatedTimeText.value.isBlank()) {
                _cartBackdatedTimeText.value = "00:00"
            }
        }
    }

    fun setCartBackdatedDateKey(dateKey: String) {
        if (dateKey.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            _cartBackdatedDateKey.value = dateKey
        }
    }

    fun setCartBackdatedTimeText(timeText: String) {
        val normalized = timeText.trim().replace('.', ':')
        val parts = normalized.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        _cartBackdatedTimeText.value = String.format(Locale.US, "%02d:%02d", h, m)
    }

    fun resolveCartEffectiveTimestampMillis(): Long {
        if (!_cartIsBackdated.value || !_session.value.role.canInputBackdatedOrder) {
            return System.currentTimeMillis()
        }
        return try {
            val dParts = _cartBackdatedDateKey.value.split("-")
            val tParts = _cartBackdatedTimeText.value.replace('.', ':').split(":")
            val year = dParts.getOrNull(0)?.toIntOrNull() ?: return System.currentTimeMillis()
            val month = (dParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
            val day = dParts.getOrNull(2)?.toIntOrNull() ?: 1
            val hour = (tParts.getOrNull(0)?.toIntOrNull() ?: 0).coerceIn(0, 23)
            val minute = (tParts.getOrNull(1)?.toIntOrNull() ?: 0).coerceIn(0, 59)
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun isBackdatedOrder(order: OrderRecord): Boolean {
        if (order.id.startsWith("v1_ord_bd_")) return true
        val createdMs = resolveOrderTimestampDate(order).time
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFmt.format(Date(createdMs)) != dateFmt.format(Date())
    }

    private fun resolveOrderActionTimestampMillis(order: OrderRecord): Long {
        return if (isBackdatedOrder(order)) {
            val baseMs = resolveOrderTimestampDate(order).time
            val maxTimelineMs = order.getTimelineEvents().maxOfOrNull { it.timestampMillis } ?: baseMs
            maxOf(baseMs, maxTimelineMs) + 1000L
        } else {
            System.currentTimeMillis()
        }
    }

    fun calculateCartSubtotal(
        items: List<CartLineItem> = _cartItems.value,
        media: OrderMedia = _cartMedia.value
    ): Long {
        return items.sumOf { it.lineTotalFor(media) }
    }

    fun computeDynamicPromoUsageMap(ordersList: List<OrderRecord> = allOrders.value): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        val validOrders = ordersList.filter { it.status != "CANCELLED" && it.status != "ERASED" && it.discountAmount > 0L && it.promoCode.isNotBlank() }
        val allPromos = (allCatalogRecords.value + _localSeededCatalog.value)
            .filter { it.recordType == "PROMO" }
            .distinctBy { it.id }

        validOrders.forEach { ord ->
            val rawCode = ord.promoCode.trim()
            val tokens = rawCode.split("+", ",", ";").map { it.trim() }.filter { it.isNotBlank() }
            tokens.forEach { token ->
                val matched = allPromos.firstOrNull {
                    it.id.equals(token, ignoreCase = true) ||
                        it.name.equals(token, ignoreCase = true) ||
                        token.startsWith(it.name, ignoreCase = true)
                }
                if (matched != null) {
                    map[matched.id] = (map[matched.id] ?: 0) + 1
                }
            }
        }
        return map
    }

    fun evaluateCartPromoDetails(
        items: List<CartLineItem> = _cartItems.value,
        media: OrderMedia = _cartMedia.value,
        promoIdOrMode: String = _selectedPromoId.value,
        voucherCode: String = _cartVoucherCode.value,
        allCatalog: List<CatalogRecord> = catalogRecords.value,
        evaluationTimeMillis: Long = resolveCartEffectiveTimestampMillis()
    ): CartPromoEvaluationResult {
        val mode = promoIdOrMode.ifBlank { "AUTO" }
        return WaffleoPromoEngine.evaluateCartPromos(
            cartItems = items,
            media = media,
            activeBranchId = _session.value.branchId,
            catalog = allCatalog,
            selectedPromoModeOrId = mode,
            voucherCodeInput = voucherCode,
            dynamicUsageMap = computeDynamicPromoUsageMap(),
            evaluationTimeMillis = evaluationTimeMillis
        )
    }

    fun calculatePromoDiscount(
        items: List<CartLineItem> = _cartItems.value,
        media: OrderMedia = _cartMedia.value,
        promoId: String = _selectedPromoId.value,
        allCatalog: List<CatalogRecord> = catalogRecords.value
    ): Long {
        if (items.isEmpty()) return 0L
        return evaluateCartPromoDetails(
            items = items,
            media = media,
            promoIdOrMode = promoId.ifBlank { "AUTO" },
            voucherCode = _cartVoucherCode.value,
            allCatalog = allCatalog
        ).totalDiscountAmount
    }

    fun calculateCartTotal(): Long {
        val sub = calculateCartSubtotal()
        val disc = calculatePromoDiscount()
        return (sub - disc).coerceAtLeast(0L)
    }

    /**
     * Returns (commissionPercent, perOrderFeeIdr, bankAccountLabel) for a platform media
     * configured in BRANCH_CONFIG records (defaulting to 20% commission + Rp 0 / configurable per-order fee -> Rekening Bank BCA).
     */
    fun getPlatformFeeConfig(
        media: OrderMedia,
        allCatalog: List<CatalogRecord> = catalogRecords.value
    ): Triple<Double, Long, String> {
        if (!media.isPlatform) return Triple(0.0, 0L, "Kas / Non-Platform")
        val cfgId = when (media) {
            OrderMedia.GRABFOOD -> "cfg_platform_grabfood"
            OrderMedia.GOFOOD -> "cfg_platform_gofood"
            OrderMedia.SHOPEEFOOD -> "cfg_platform_shopeefood"
            OrderMedia.WALK_IN, OrderMedia.WHATSAPP, OrderMedia.INSTAGRAM -> ""
        }
        val rec = allCatalog.firstOrNull {
            it.recordType == "BRANCH_CONFIG" &&
                (it.id == cfgId ||
                    it.id.endsWith(cfgId) ||
                    it.name.equals(media.label, ignoreCase = true) ||
                    it.category.equals(media.label, ignoreCase = true))
        }
        if (rec == null) {
            val defaultComm = if (media == OrderMedia.SHOPEEFOOD) 25.0 else 20.0
            val defaultFee = if (media == OrderMedia.GOFOOD) 1000L else 0L
            return Triple(defaultComm, defaultFee, "Rekening Bank BCA (Cabang ${_session.value.branchId})")
        }
        val meta = try { JSONObject(rec.metadataJson) } catch (e: Exception) { JSONObject() }
        val commPct = meta.optDouble("commissionPercent", rec.priceWalkIn.toDouble())
        val fallbackMetaFee = meta.optLong("orderFee", meta.optLong("perOrderFee", 0L))
        val orderFee = when (media) {
            OrderMedia.GRABFOOD -> if (rec.priceGrabFood > 0L) rec.priceGrabFood else fallbackMetaFee
            OrderMedia.GOFOOD -> if (rec.priceGoFood > 0L) rec.priceGoFood else fallbackMetaFee
            OrderMedia.SHOPEEFOOD -> if (rec.priceShopeeFood > 0L) rec.priceShopeeFood else fallbackMetaFee
            OrderMedia.WALK_IN, OrderMedia.WHATSAPP, OrderMedia.INSTAGRAM -> 0L
        }
        val bankLabel = meta.optString("bankAccount", "Rekening Bank BCA (Cabang ${_session.value.branchId})")
        return Triple(commPct, orderFee, bankLabel)
    }

    /**
     * Calculates total platform deduction = (grossAmount * commissionPercent%) + perOrderFeeIdr.
     */
    fun calculatePlatformDeduction(
        grossAmount: Long,
        media: OrderMedia,
        allCatalog: List<CatalogRecord> = catalogRecords.value
    ): Long {
        if (!media.isPlatform || grossAmount <= 0L) return 0L
        val (commPct, perOrderFee, _) = getPlatformFeeConfig(media, allCatalog)
        val commissionNominal = ((grossAmount * commPct) / 100.0).roundToLong()
        return (commissionNominal + perOrderFee).coerceAtMost(grossAmount)
    }

    /**
     * Calculates the actual net money entering the system / Rekening Bank BCA (Cabang Global)
     * after deducting platform commission and per-order fee.
     */
    fun calculateNetEnteredSystemAmount(
        grossAmount: Long,
        media: OrderMedia,
        allCatalog: List<CatalogRecord> = catalogRecords.value
    ): Long {
        if (!media.isPlatform) return grossAmount
        val deduction = calculatePlatformDeduction(grossAmount, media, allCatalog)
        return (grossAmount - deduction).coerceAtLeast(0L)
    }

    fun calculateOrderPlatformFee(
        order: OrderRecord,
        allCatalog: List<CatalogRecord> = catalogRecords.value
    ): Long {
        val media = OrderMedia.fromLabel(order.orderMedia)
        return calculatePlatformDeduction(order.totalAmount, media, allCatalog)
    }

    fun calculateOrderNetAmount(
        order: OrderRecord,
        allCatalog: List<CatalogRecord> = catalogRecords.value
    ): Long {
        val media = OrderMedia.fromLabel(order.orderMedia)
        return calculateNetEnteredSystemAmount(order.totalAmount, media, allCatalog)
    }

    private fun reconcileCartPrepaymentAfterChange() {
        val total = calculateCartTotal()
        val media = _cartMedia.value
        if (total <= 0L && _editingOrder.value == null) {
            _cartIsPaid.value = false
            _cartPaidAmount.value = 0L
            _cartPaymentMethod.value = "Belum Bayar"
            _cartPaymentLedger.value = emptyList()
            _cartSplitCash.value = 0L
            _cartSplitQris.value = 0L
            _cartChangeAmount.value = 0L
            return
        }
        if (media.isPlatform) {
            // Platform orders (GrabFood, GoFood, ShopeeFood) are ALWAYS paid via platform -> Rekening BCA
            _cartIsPaid.value = total > 0L
            _cartPaidAmount.value = total
            _cartPaymentMethod.value = media.defaultSettlementMethod
            return
        }
        val paid = _cartPaidAmount.value
        if (paid > 0L) {
            _cartIsPaid.value = paid >= total && total > 0L
        } else {
            _cartIsPaid.value = false
        }
    }

    fun markCartPaid(
        method: String,
        amountPaid: Long,
        splitCash: Long = 0L,
        splitQris: Long = 0L,
        qrisBank: String = "Bank Mandiri",
        changeAmount: Long = 0L
    ) {
        if (!_isStoreOpen.value) {
            showBanner("Toko sedang tutup. Buka toko terlebih dahulu untuk melakukan transaksi.")
            return
        }
        val total = calculateCartTotal()
        if (total <= 0L) return

        val prevPaid = _cartPaidAmount.value
        val remainingDue = (total - prevPaid).coerceAtLeast(0L)
        val settledAmount = if (amountPaid >= remainingDue && remainingDue > 0L) {
            remainingDue
        } else if (amountPaid > 0L) {
            minOf(amountPaid, remainingDue)
        } else {
            remainingDue
        }

        val nowMs = resolveCartEffectiveTimestampMillis()
        val nowText = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(nowMs))
        val currentLedger = _cartPaymentLedger.value.toMutableList()
        val isFirstPayment = prevPaid == 0L && currentLedger.isEmpty()
        val pType = if (isFirstPayment) "INITIAL_PAYMENT" else "ADDITIONAL_PAYMENT"

        if (method.contains("Cash + QRIS", ignoreCase = true)) {
            val netQris = splitQris.coerceAtMost(settledAmount)
            val netCash = (settledAmount - netQris).coerceAtLeast(0L)

            if (netQris > 0L) {
                currentLedger.add(
                    OrderPaymentLedgerEntry(
                        paymentId = "pay_${nowMs}_qris_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs,
                        timeText = nowText,
                        paymentType = pType,
                        paymentMethod = "QRIS Waffleo ($qrisBank)",
                        amount = netQris,
                        cashierUsername = _session.value.username,
                        note = if (isFirstPayment) "Porsi Pembayaran QRIS ($qrisBank)" else "Porsi QRIS ($qrisBank) Pelunasan Sisa"
                    )
                )
            }
            if (netCash > 0L) {
                currentLedger.add(
                    OrderPaymentLedgerEntry(
                        paymentId = "pay_${nowMs}_cash_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs + 1,
                        timeText = nowText,
                        paymentType = pType,
                        paymentMethod = "Tunai (Cash)",
                        amount = netCash,
                        cashierUsername = _session.value.username,
                        note = if (changeAmount > 0L) "Porsi Tunai (Diterima ${formatIdr(splitCash)}, Kembalian: ${formatIdr(changeAmount)})" else "Porsi Tunai"
                    )
                )
            }
            if (changeAmount > 0L) {
                currentLedger.add(
                    OrderPaymentLedgerEntry(
                        paymentId = "pay_${nowMs}_change_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs + 2,
                        timeText = nowText,
                        paymentType = "CHANGE_REFUND_RETURN",
                        paymentMethod = "Tunai (Kembalian)",
                        amount = changeAmount,
                        cashierUsername = _session.value.username,
                        note = "Kembalian tunai diserahkan ke pelanggan dari pembayaran Cash + QRIS"
                    )
                )
            }
            _cartSplitCash.value = _cartSplitCash.value + splitCash
            _cartSplitQris.value = _cartSplitQris.value + splitQris
            _cartQrisBank.value = qrisBank
        } else {
            currentLedger.add(
                OrderPaymentLedgerEntry(
                    paymentId = "pay_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                    timestampMillis = nowMs,
                    timeText = nowText,
                    paymentType = pType,
                    paymentMethod = method,
                    amount = settledAmount,
                    cashierUsername = _session.value.username,
                    note = if (changeAmount > 0L) "Pembayaran $method (Kembalian: ${formatIdr(changeAmount)})" else if (isFirstPayment) "Pembayaran di keranjang" else "Pelunasan sisa tagihan di keranjang"
                )
            )
            if (changeAmount > 0L) {
                currentLedger.add(
                    OrderPaymentLedgerEntry(
                        paymentId = "pay_${nowMs}_change_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs + 1,
                        timeText = nowText,
                        paymentType = "CHANGE_REFUND_RETURN",
                        paymentMethod = "Tunai (Kembalian)",
                        amount = changeAmount,
                        cashierUsername = _session.value.username,
                        note = "Kembalian tunai diserahkan ke pelanggan"
                    )
                )
            }
            if (method.contains("Tunai", ignoreCase = true)) {
                _cartSplitCash.value = _cartSplitCash.value + settledAmount
            } else if (method.contains("QRIS", ignoreCase = true)) {
                _cartSplitQris.value = _cartSplitQris.value + settledAmount
                _cartQrisBank.value = qrisBank
            }
        }

        _cartPaymentLedger.value = currentLedger
        val newPaid = prevPaid + settledAmount
        _cartPaidAmount.value = newPaid
        _cartIsPaid.value = newPaid >= total && total > 0L
        _cartChangeAmount.value = changeAmount
        _cartCashReceived.value = amountPaid

        // Determine effective overall payment method
        val hasCash = currentLedger.any { it.paymentMethod.contains("Tunai", ignoreCase = true) && it.paymentType != "CHANGE_REFUND_RETURN" }
        val hasQris = currentLedger.any { it.paymentMethod.contains("QRIS", ignoreCase = true) }
        val hasTransfer = currentLedger.any { it.paymentMethod.contains("Transfer", ignoreCase = true) }
        _cartPaymentMethod.value = when {
            hasCash && hasQris -> "Cash + QRIS ($qrisBank)"
            hasCash && hasTransfer -> "Tunai + Transfer"
            hasQris -> "QRIS Waffleo ($qrisBank)"
            hasTransfer -> "Transfer Tunai ($qrisBank)"
            else -> method
        }

        if (_cartIsPaid.value) {
            showBanner("Pembayaran Keranjang LUNAS (${_cartPaymentMethod.value}) - Total: ${formatIdr(newPaid)}")
        } else {
            showBanner("Pembayaran Sebagian: ${formatIdr(settledAmount)} via $method")
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartMedia.value = OrderMedia.WALK_IN
        _customerInputSuffix.value = ""
        _cartNotes.value = ""
        _selectedPromoId.value = "AUTO"
        _cartVoucherCode.value = ""
        _cartIsPaid.value = false
        _cartPaidAmount.value = 0L
        _cartPaymentMethod.value = "Belum Bayar"
        _cartSplitCash.value = 0L
        _cartSplitQris.value = 0L
        _cartQrisBank.value = "Bank Mandiri"
        _cartChangeAmount.value = 0L
        _cartCashReceived.value = 0L
        _cartPaymentLedger.value = emptyList()
        _editingOrder.value = null
        _cartIsBackdated.value = false
        _cartBackdatedDateKey.value = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        _cartBackdatedTimeText.value = "00:00"
    }

    // --- SUBMIT CART TO ORDERS OR APPLY ORDER EDIT ---

    fun submitCartToOrders(onValidationError: (String) -> Unit, onSuccess: () -> Unit) {
        if (!_isStoreOpen.value) {
            onValidationError("Toko sedang tutup. Buka toko terlebih dahulu untuk melakukan transaksi.")
            showBanner("Toko sedang tutup. Buka toko terlebih dahulu untuk melakukan transaksi.")
            return
        }
        val items = _cartItems.value
        if (items.isEmpty()) {
            onValidationError("Keranjang masih kosong. Pilih menu terlebih dahulu.")
            return
        }
        val media = _cartMedia.value
        val suffix = _customerInputSuffix.value.trim()
        if (media.requiresCustomerCode && suffix.isEmpty()) {
            onValidationError("Nomor kode pesanan ${media.label} (${media.prefix}...) wajib diisi!")
            return
        }

        val fullCustomerData = if (media.prefix.isNotEmpty()) {
            "${media.prefix}$suffix"
        } else {
            suffix.ifEmpty { "Pelanggan Di tempat" }
        }

        val effectiveEvalMs = resolveCartEffectiveTimestampMillis()
        val promoEvaluation = evaluateCartPromoDetails(
            items = items,
            media = media,
            promoIdOrMode = _selectedPromoId.value,
            voucherCode = _cartVoucherCode.value,
            allCatalog = catalogRecords.value,
            evaluationTimeMillis = effectiveEvalMs
        )
        val subtotal = calculateCartSubtotal(items, media)
        val discount = promoEvaluation.totalDiscountAmount.coerceAtMost(subtotal)
        val newTotal = (subtotal - discount).coerceAtLeast(0L)
        val resolvedPromoCodeForOrder = if (discount > 0L && promoEvaluation.appliedPromos.isNotEmpty()) {
            promoEvaluation.primaryPromoName
        } else {
            ""
        }
        val platformFee = calculatePlatformDeduction(newTotal, media)
        val netEnteredSystem = calculateNetEnteredSystemAmount(newTotal, media)
        val summaryText = items.joinToString("\n") { item ->
            "${item.qty}x ${item.formattedTitle()} [${item.formattedCustomization()}]"
        }
        val payloadJson = CartLineItem.serializeList(items)
        val currentEditing = _editingOrder.value

        viewModelScope.launch {
            if (currentEditing == null) {
                // CREATE NEW ORDER (Daily counter per branch: Cloud format WFL-BRANCH-YYYYMMDD-001, App display WFL-001)
                val isBackdatedCart = _cartIsBackdated.value && _session.value.role.canInputBackdatedOrder
                val nowMs = resolveCartEffectiveTimestampMillis()
                val nowText = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(nowMs))

                val activeBranch = _session.value.branchId
                val normActiveBranch = normalizeBranchKey(activeBranch)
                val branchSlug = normActiveBranch.uppercase().replace(Regex("[^A-Z0-9]"), "").ifEmpty { "GLOBAL" }
                val dateFmt = SimpleDateFormat("yyyyMMdd", Locale.US)
                val dateKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val dateStr = dateFmt.format(Date(nowMs))
                val todayStr = dateKeyFmt.format(Date(nowMs))

                val todayBranchOrders = allOrders.value.filter { ord ->
                    isSameBranch(ord.branchId, activeBranch) &&
                    ord.orderNumber != "WFL-001" &&
                    ord.createdAt?.toDate()?.let { dateKeyFmt.format(it) } == todayStr
                }
                val maxInOrdersToday = todayBranchOrders.maxOfOrNull { it.sequenceIndex } ?: 0L
                val maxRecordedDaily = localStore?.getHighestRecordedDailyOrderSeq(normActiveBranch, dateStr) ?: 0L
                val nextSeq = maxOf(maxInOrdersToday, maxRecordedDaily) + 1L

                localStore?.updateHighestRecordedDailyOrderSeq(normActiveBranch, dateStr, nextSeq)
                localStore?.updateHighestRecordedOrderSeq(normActiveBranch, nextSeq)

                val cloudOrderNum = "WFL-$branchSlug-$dateStr-${String.format(Locale.US, "%03d", nextSeq)}"
                val isOrderPaid = media.isPlatform || _cartIsPaid.value || (_cartPaidAmount.value >= newTotal && newTotal > 0L)
                val paidNow = when {
                    media.isPlatform -> newTotal
                    isOrderPaid -> newTotal
                    _cartPaidAmount.value > 0L -> minOf(_cartPaidAmount.value, newTotal)
                    else -> 0L
                }
                val resolvedMethod = when {
                    media.isPlatform -> media.defaultSettlementMethod
                    _cartPaymentMethod.value != "Belum Bayar" -> _cartPaymentMethod.value
                    _cartPaidAmount.value > 0L -> _cartPaymentMethod.value
                    else -> "Belum Bayar"
                }
                val shortfall = if (!isOrderPaid && !media.isPlatform && paidNow > 0L) (newTotal - paidNow).coerceAtLeast(0L) else 0L
                val changeDue = if (!media.isPlatform) {
                    maxOf(_cartChangeAmount.value, if (_cartPaidAmount.value > newTotal) _cartPaidAmount.value - newTotal else 0L)
                } else 0L
                val effCashReceived = when {
                    media.isPlatform -> newTotal
                    _cartCashReceived.value > 0L -> _cartCashReceived.value
                    changeDue > 0L -> newTotal + changeDue
                    isOrderPaid -> paidNow
                    else -> 0L
                }

                val ledger = mutableListOf<OrderPaymentLedgerEntry>()
                val timeline = mutableListOf<OrderTimelineEvent>()

                timeline.add(
                    OrderTimelineEvent(
                        eventId = "evt_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs,
                        timeText = nowText,
                        eventType = "ORDER_CREATED",
                        actorUsername = _session.value.username,
                        actorRole = _session.value.role.label,
                        title = "Pesanan Dibuat",
                        description = "Pesanan masuk via ${media.label} ($fullCustomerData) senilai ${formatIdr(newTotal)} oleh ${_session.value.username}."
                    )
                )

                if (_cartPaymentLedger.value.isNotEmpty()) {
                    val alignedCartLedger = if (isBackdatedCart) {
                        _cartPaymentLedger.value.mapIndexed { idx, entry ->
                            entry.copy(
                                timestampMillis = nowMs + idx,
                                timeText = nowText
                            )
                        }
                    } else {
                        _cartPaymentLedger.value
                    }
                    ledger.addAll(alignedCartLedger)
                    if (changeDue > 0L) {
                        ledger.add(
                            OrderPaymentLedgerEntry(
                                paymentId = "pay_${nowMs}_change_${UUID.randomUUID().toString().take(4)}",
                                timestampMillis = nowMs + 10,
                                timeText = nowText,
                                paymentType = "CHANGE_REFUND_RETURN",
                                paymentMethod = "Tunai (Kembalian)",
                                amount = changeDue,
                                cashierUsername = _session.value.username,
                                note = "Kembalian tunai diserahkan ke pelanggan akibat penyesuaian nominal pesanan"
                            )
                        )
                    }
                    timeline.add(
                        OrderTimelineEvent(
                            eventId = "evt_${nowMs + 1}_${UUID.randomUUID().toString().take(4)}",
                            timestampMillis = nowMs + 1,
                            timeText = nowText,
                            eventType = "PAYMENT_EVENT",
                            actorUsername = _session.value.username,
                            actorRole = _session.value.role.label,
                            title = if (isOrderPaid) "Pembayaran Dikonfirmasi (Lunas)" else "Pembayaran Sebagian (Kurang Bayar)",
                            description = "Tercatat pembayaran ${formatIdr(paidNow)} via $resolvedMethod oleh ${_session.value.username}."
                        )
                    )
                } else if (isOrderPaid && paidNow > 0L) {
                    if (media.isPlatform) {
                        ledger.add(
                            OrderPaymentLedgerEntry(
                                paymentId = "pay_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                                timestampMillis = nowMs,
                                timeText = nowText,
                                paymentType = "INITIAL_PAYMENT",
                                paymentMethod = resolvedMethod,
                                amount = paidNow,
                                cashierUsername = _session.value.username,
                                note = "Otomatis diselesaikan melalui ${media.label} -> Rekening BCA"
                            )
                        )
                        timeline.add(
                            OrderTimelineEvent(
                                eventId = "evt_${nowMs + 1}_${UUID.randomUUID().toString().take(4)}",
                                timestampMillis = nowMs + 1,
                                timeText = nowText,
                                eventType = "PAYMENT_EVENT",
                                actorUsername = _session.value.username,
                                actorRole = _session.value.role.label,
                                title = "Pembayaran Platform (${media.label})",
                                description = "Pesanan ${media.label} otomatis diselesaikan ke Rekening BCA senilai ${formatIdr(paidNow)}."
                            )
                        )
                    } else if (resolvedMethod.contains("Cash + QRIS", ignoreCase = true)) {
                        val qBank = _cartQrisBank.value
                        val splitCash = _cartSplitCash.value
                        val splitQris = _cartSplitQris.value
                        val splitChange = _cartChangeAmount.value
                        val netQris = splitQris.coerceAtMost(newTotal)
                        val netCash = (newTotal - netQris).coerceAtLeast(0L)

                        ledger.add(
                            OrderPaymentLedgerEntry(
                                paymentId = "pay_${nowMs}_qris_${UUID.randomUUID().toString().take(4)}",
                                timestampMillis = nowMs,
                                timeText = nowText,
                                paymentType = "INITIAL_PAYMENT",
                                paymentMethod = "QRIS Waffleo ($qBank)",
                                amount = netQris,
                                cashierUsername = _session.value.username,
                                note = "Porsi Pembayaran QRIS ($qBank)"
                            )
                        )
                        ledger.add(
                            OrderPaymentLedgerEntry(
                                paymentId = "pay_${nowMs}_cash_${UUID.randomUUID().toString().take(4)}",
                                timestampMillis = nowMs + 1,
                                timeText = nowText,
                                paymentType = "INITIAL_PAYMENT",
                                paymentMethod = "Tunai (Cash)",
                                amount = netCash,
                                cashierUsername = _session.value.username,
                                note = if (splitChange > 0L) "Porsi Tunai (Diterima ${formatIdr(splitCash)}, Kembalian Tunai: ${formatIdr(splitChange)})" else "Porsi Tunai"
                            )
                        )
                        if (splitChange > 0L) {
                            ledger.add(
                                OrderPaymentLedgerEntry(
                                    paymentId = "pay_${nowMs}_change_${UUID.randomUUID().toString().take(4)}",
                                    timestampMillis = nowMs + 2,
                                    timeText = nowText,
                                    paymentType = "CHANGE_REFUND_RETURN",
                                    paymentMethod = "Tunai (Kembalian)",
                                    amount = splitChange,
                                    cashierUsername = _session.value.username,
                                    note = "Kembalian tunai diserahkan ke pelanggan dari pembayaran Cash + QRIS"
                                )
                            )
                        }
                    } else {
                        ledger.add(
                            OrderPaymentLedgerEntry(
                                paymentId = "pay_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                                timestampMillis = nowMs,
                                timeText = nowText,
                                paymentType = "INITIAL_PAYMENT",
                                paymentMethod = resolvedMethod,
                                amount = paidNow,
                                cashierUsername = _session.value.username,
                                note = "Pembayaran lunas di awal (Kasir)"
                            )
                        )
                    }
                }

                val orderIdPrefix = if (isBackdatedCart) "v1_ord_bd_" else "v1_ord_"
                val newOrder = OrderRecord(
                    id = "${orderIdPrefix}${nowMs}_${UUID.randomUUID().toString().take(4)}",
                    userId = workspaceId,
                    orderNumber = cloudOrderNum,
                    sequenceIndex = nextSeq,
                    branchId = _session.value.branchId,
                    orderMedia = media.label,
                    customerData = fullCustomerData,
                    itemsSummary = summaryText,
                    itemsPayload = payloadJson,
                    notes = _cartNotes.value.trim(),
                    promoCode = resolvedPromoCodeForOrder,
                    subtotalAmount = subtotal,
                    discountAmount = discount,
                    totalAmount = newTotal,
                    paidAmount = paidNow,
                    cashReceivedAmount = effCashReceived,
                    paymentMethod = resolvedMethod,
                    isPaid = isOrderPaid,
                    isPrepared = false,
                    status = "ACTIVE",
                    shortfallAmount = shortfall,
                    changeDueAmount = changeDue,
                    splitCashAmount = if (resolvedMethod.contains("Cash + QRIS", ignoreCase = true)) _cartSplitCash.value else 0L,
                    splitQrisAmount = if (resolvedMethod.contains("Cash + QRIS", ignoreCase = true)) _cartSplitQris.value else 0L,
                    createdBy = _session.value.username,
                    paymentLedgerPayload = OrderPaymentLedgerEntry.serializeList(ledger),
                    orderTimelinePayload = OrderTimelineEvent.serializeList(timeline),
                    createdAt = com.google.firebase.Timestamp(Date(nowMs)),
                    updatedAt = com.google.firebase.Timestamp(Date(nowMs))
                )
                updateLocalOrderItem(newOrder)
                syncOrderToCloud(newOrder, isNew = true)

                // Increment usedCount on applied promos so quota is updated immediately
                if (discount > 0L && promoEvaluation.appliedPromos.isNotEmpty()) {
                    promoEvaluation.appliedPromos.forEach { applied ->
                        incrementPromoUsageCount(applied.promoId, delta = 1)
                    }
                }
                if (isBackdatedCart) {
                    // Backdated orders are synced on their own target date when completed and must never be recapped during Tutup Toko
                    markOrdersAsRecapped(listOf(newOrder.id))
                }
                val auditSummary = if (media.isPlatform) {
                    "Pesanan baru ${newOrder.displayOrderNumber} (${media.label} - $fullCustomerData) Harga Platform ${formatIdr(newTotal)} • Uang Masuk Rekening BCA: ${formatIdr(netEnteredSystem)} (Potongan: -${formatIdr(platformFee)})"
                } else {
                    "Pesanan baru ${newOrder.displayOrderNumber} (${media.label} - $fullCustomerData) senilai ${formatIdr(newTotal)}"
                }
                val createdSnapshot = JSONObject().apply {
                    put("orderNumber", newOrder.orderNumber)
                    put("displayOrderNumber", newOrder.displayOrderNumber)
                    put("media", media.label)
                    put("customer", newOrder.customerData)
                    put("notes", newOrder.notes)
                    put("itemsSummary", newOrder.itemsSummary)
                    put("itemsPayload", newOrder.itemsPayload)
                    put("totalAmount", newOrder.totalAmount)
                    put("platformTotal", newTotal)
                    put("platformFee", platformFee)
                    put("netEnteredBca", netEnteredSystem)
                    put("isPaid", newOrder.isPaid)
                    put("paymentMethod", newOrder.paymentMethod)
                    put("paidAmount", newOrder.paidAmount)
                    put("changeDueAmount", newOrder.changeDueAmount)
                }.toString()
                logAuditEvent(
                    actionType = "ORDER_CREATED",
                    entityId = newOrder.orderNumber,
                    summary = auditSummary,
                    beforeSnapshot = "{}",
                    afterSnapshot = createdSnapshot,
                    customTimestampMillis = nowMs
                )
                clearCart()
                showBanner("Pesanan ${newOrder.displayOrderNumber} berhasil dikirim ke halaman Orders.")
                onSuccess()
            } else {
                // APPLY CHANGES TO EXISTING EDITED ORDER (Preserving sequenceIndex!)
                val previousPaid = maxOf(currentEditing.paidAmount, if (_cartIsPaid.value) _cartPaidAmount.value else 0L)
                val wasPaidBefore = currentEditing.isPaid || _cartIsPaid.value

                val isStillPaid: Boolean
                val shortfall: Long
                val changeDue: Long
                val effectivePaidAmount: Long

                if (media.isPlatform) {
                    // Platform orders are always settled automatically by the platform -> Rekening Bank BCA
                    isStillPaid = true
                    shortfall = 0L
                    changeDue = 0L
                    effectivePaidAmount = newTotal
                } else if (wasPaidBefore && previousPaid > 0L) {
                    when {
                        newTotal > previousPaid -> {
                            // Total baru > uang yang sudah dibayar -> belum lunas, wajib bayar kekurangan!
                            isStillPaid = false
                            shortfall = newTotal - previousPaid
                            changeDue = 0L
                            effectivePaidAmount = previousPaid
                        }
                        newTotal < previousPaid -> {
                            // Total baru < uang yang sudah dibayar -> lunas, kasir beri kembalian Cash!
                            isStillPaid = true
                            shortfall = 0L
                            changeDue = previousPaid - newTotal
                            effectivePaidAmount = newTotal
                        }
                        else -> {
                            isStillPaid = true
                            shortfall = 0L
                            changeDue = 0L
                            effectivePaidAmount = newTotal
                        }
                    }
                } else {
                    isStillPaid = false
                    shortfall = 0L
                    changeDue = 0L
                    effectivePaidAmount = 0L
                }

                val isEditedBackdated = (_cartIsBackdated.value && _session.value.role.canInputBackdatedOrder) || isBackdatedOrder(currentEditing)
                val effectiveBaseMs = if (_cartIsBackdated.value && _session.value.role.canInputBackdatedOrder) {
                    resolveCartEffectiveTimestampMillis()
                } else {
                    resolveOrderTimestampDate(currentEditing).time
                }
                val nowMs = if (isEditedBackdated) {
                    val maxTl = currentEditing.getTimelineEvents().maxOfOrNull { it.timestampMillis } ?: effectiveBaseMs
                    maxOf(effectiveBaseMs, maxTl) + 1000L
                } else {
                    System.currentTimeMillis()
                }
                val nowText = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(nowMs))
                val ledger = currentEditing.getPaymentLedger().toMutableList()
                val timeline = currentEditing.getTimelineEvents().toMutableList()

                // Bootstrap initial timeline & payment if empty
                if (timeline.isEmpty()) {
                    timeline.add(
                        OrderTimelineEvent(
                            eventId = "evt_init_${currentEditing.id}",
                            timestampMillis = nowMs - 600000,
                            timeText = "Waktu Pembuatan Awal",
                            eventType = "ORDER_CREATED",
                            actorUsername = currentEditing.createdBy,
                            actorRole = "Staff",
                            title = "Pesanan Dibuat",
                            description = "Pesanan ${currentEditing.orderNumber} masuk via ${currentEditing.orderMedia} (${currentEditing.customerData})."
                        )
                    )
                    if (currentEditing.paidAmount > 0L) {
                        ledger.add(
                            OrderPaymentLedgerEntry(
                                paymentId = "pay_init_${currentEditing.id}",
                                timestampMillis = nowMs - 500000,
                                timeText = "Pembayaran Awal",
                                paymentType = "INITIAL_PAYMENT",
                                paymentMethod = currentEditing.paymentMethod,
                                amount = currentEditing.paidAmount,
                                cashierUsername = currentEditing.createdBy,
                                note = "Pembayaran awal yang telah tercatat"
                            )
                        )
                    }
                }

                val oldItems = CartLineItem.deserializeList(currentEditing.itemsPayload)
                val deltaItems = mutableListOf<String>()
                items.forEach { newItem ->
                    val matchedOld = oldItems.firstOrNull { it.lineId == newItem.lineId || it.formattedTitle() == newItem.formattedTitle() }
                    if (matchedOld == null) {
                        deltaItems.add("+ ${newItem.qty}x ${newItem.formattedTitle()}")
                    } else if (newItem.qty > matchedOld.qty) {
                        val addedQty = newItem.qty - matchedOld.qty
                        deltaItems.add("+ ${addedQty}x ${newItem.formattedTitle()}")
                    } else if (newItem.formattedCustomization() != matchedOld.formattedCustomization()) {
                        deltaItems.add("• ${newItem.qty}x ${newItem.formattedTitle()} [${newItem.formattedCustomization()}]")
                    }
                }
                val deltaSummary = if (deltaItems.isNotEmpty()) {
                    deltaItems.joinToString("\n")
                } else {
                    summaryText
                }

                val editDesc = buildString {
                    append("Perubahan pesanan dikonfirmasi oleh ${_session.value.username} (${_session.value.role.label}).\n")
                    append("• Total Semula: ${formatIdr(currentEditing.totalAmount)} -> Total Baru: ${formatIdr(newTotal)}\n")
                    if (shortfall > 0L) {
                        append("• Kurang Bayar: ${formatIdr(shortfall)} (Customer wajib melunasi kekurangan)\n")
                    }
                    if (changeDue > 0L) {
                        append("• Kembalian Tunai: ${formatIdr(changeDue)} (Wajib diserahkan ke customer)\n")
                    }
                    append("• Rincian Item Baru:\n$deltaSummary")
                }

                timeline.add(
                    OrderTimelineEvent(
                        eventId = "evt_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs,
                        timeText = nowText,
                        eventType = "ORDER_EDITED",
                        actorUsername = _session.value.username,
                        actorRole = _session.value.role.label,
                        title = "Perubahan Pesanan Dikonfirmasi",
                        description = editDesc
                    )
                )

                if (changeDue > 0L) {
                    ledger.add(
                        OrderPaymentLedgerEntry(
                            paymentId = "pay_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                            timestampMillis = nowMs,
                            timeText = nowText,
                            paymentType = "CHANGE_REFUND_RETURN",
                            paymentMethod = "Tunai (Kembalian)",
                            amount = changeDue,
                            cashierUsername = _session.value.username,
                            note = "Pengembalian kembalian uang tunai ke customer akibat perubahan pesanan"
                        )
                    )
                    timeline.add(
                        OrderTimelineEvent(
                            eventId = "evt_${nowMs + 1}_${UUID.randomUUID().toString().take(4)}",
                            timestampMillis = nowMs + 1,
                            timeText = nowText,
                            eventType = "PAYMENT_EVENT",
                            actorUsername = _session.value.username,
                            actorRole = _session.value.role.label,
                            title = "Kembalian Tunai Diserahkan",
                            description = "Diserahkan kembalian uang tunai ${formatIdr(changeDue)} kepada customer oleh ${_session.value.username}."
                        )
                    )
                }

                val updatedOrder = currentEditing.copy(
                    orderMedia = media.label,
                    customerData = fullCustomerData,
                    itemsSummary = summaryText,
                    itemsPayload = payloadJson,
                    notes = _cartNotes.value.trim(),
                    promoCode = resolvedPromoCodeForOrder,
                    subtotalAmount = subtotal,
                    discountAmount = discount,
                    totalAmount = newTotal,
                    paidAmount = effectivePaidAmount,
                    paymentMethod = when {
                        media.isPlatform -> media.defaultSettlementMethod
                        effectivePaidAmount > 0L -> if (_cartPaymentMethod.value != "Belum Bayar") _cartPaymentMethod.value else currentEditing.paymentMethod
                        else -> "Belum Bayar"
                    },
                    isPaid = isStillPaid,
                    shortfallAmount = shortfall,
                    changeDueAmount = changeDue,
                    createdBy = _session.value.username,
                    paymentLedgerPayload = OrderPaymentLedgerEntry.serializeList(ledger),
                    orderTimelinePayload = OrderTimelineEvent.serializeList(timeline),
                    createdAt = if (_cartIsBackdated.value && _session.value.role.canInputBackdatedOrder) {
                        com.google.firebase.Timestamp(Date(effectiveBaseMs))
                    } else {
                        currentEditing.createdAt ?: com.google.firebase.Timestamp(resolveOrderTimestampDate(currentEditing))
                    },
                    updatedAt = com.google.firebase.Timestamp(Date(nowMs))
                )

                updateLocalOrderItem(updatedOrder)
                syncOrderToCloud(updatedOrder, isNew = false)
                logAuditEvent(
                    actionType = "ORDER_EDITED",
                    entityId = updatedOrder.orderNumber,
                    summary = "Edit pesanan ${updatedOrder.orderNumber}: Total lama ${formatIdr(currentEditing.totalAmount)} -> Total baru ${formatIdr(newTotal)} (Kurang Bayar: ${formatIdr(shortfall)}, Kembalian Tunai: ${formatIdr(changeDue)})",
                    beforeSnapshot = """{"total":${currentEditing.totalAmount},"paid":${currentEditing.paidAmount},"items":${JSONObject.quote(currentEditing.itemsSummary)}}""",
                    afterSnapshot = """{"total":$newTotal,"paid":$effectivePaidAmount,"shortfall":$shortfall,"changeDue":$changeDue,"items":${JSONObject.quote(summaryText)}}""",
                    customTimestampMillis = nowMs
                )
                val ordDate = resolveOrderTimestampDate(updatedOrder)
                val ordDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(ordDate)
                val ordTimeText = SimpleDateFormat("HH:mm", Locale.US).format(ordDate)
                val hasExistingDateSync = _allMoneyTransactions.value.any {
                    it.dateKey == ordDateKey && (it.isAutoPosSync || it.id.startsWith("v1_pos_sync_"))
                }
                if ((isBackdatedOrder(updatedOrder) && updatedOrder.status == "DONE") || hasExistingDateSync) {
                    syncPosSalesForDateKey(targetDateKey = ordDateKey, preferredTimeText = ordTimeText, silent = true)
                }

                val feedbackMsg = when {
                    shortfall > 0L -> "Perubahan ${updatedOrder.orderNumber} disimpan. Status BELUM LUNAS: Customer wajib melunasi kekurangan ${formatIdr(shortfall)}."
                    changeDue > 0L -> "Perubahan ${updatedOrder.orderNumber} disimpan. Berikan KEMBALIAN TUNAI ke Customer senilai ${formatIdr(changeDue)}."
                    else -> "Perubahan pesanan ${updatedOrder.orderNumber} berhasil disimpan pada urutan semula."
                }
                clearCart()
                _currentDestination.value = MainNavDestination.ORDERS
                showBanner(feedbackMsg)
                onSuccess()
            }
        }
    }

    // --- ORDERS SCREEN ACTIONS ---

    /**
     * Loads the selected order back into the Cart for editing and navigates to Menus screen.
     */
    fun startEditingOrder(order: OrderRecord) {
        val parsedItems = CartLineItem.deserializeList(order.itemsPayload)
        val media = OrderMedia.fromLabel(order.orderMedia)
        val cleanSuffix = order.customerData
            .removePrefix("GF - ")
            .removePrefix("F - ")
            .removePrefix("# ")
            .let { if (it == "Walk In Guest" || it == "Pelanggan Di tempat") "" else it }

        _editingOrder.value = order
        _cartItems.value = parsedItems
        _cartMedia.value = media
        _customerInputSuffix.value = cleanSuffix
        _cartNotes.value = order.notes
        val matchedPromoId = catalogRecords.value.firstOrNull {
            it.recordType == "PROMO" &&
                (it.id.equals(order.promoCode, ignoreCase = true) || it.name.equals(order.promoCode, ignoreCase = true))
        }?.id
        _selectedPromoId.value = matchedPromoId ?: if (order.promoCode.isBlank()) "NONE" else "AUTO"
        _cartIsPaid.value = order.isPaid
        _cartPaidAmount.value = order.paidAmount
        _cartPaymentMethod.value = order.paymentMethod
        _cartPaymentLedger.value = order.getPaymentLedger()
        _cartSplitCash.value = order.splitCashAmount
        _cartSplitQris.value = order.splitQrisAmount
        val isBd = isBackdatedOrder(order)
        _cartIsBackdated.value = isBd && _session.value.role.canInputBackdatedOrder
        if (isBd) {
            val ordDate = resolveOrderTimestampDate(order)
            _cartBackdatedDateKey.value = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(ordDate)
            _cartBackdatedTimeText.value = SimpleDateFormat("HH:mm", Locale.US).format(ordDate)
        } else {
            _cartBackdatedDateKey.value = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            _cartBackdatedTimeText.value = "00:00"
        }
        _currentDestination.value = MainNavDestination.MENUS
        showBanner("Mode Edit Aktif: Memanggil pesanan ${order.orderNumber} ke Cart")
    }

    fun cancelEditingMode() {
        clearCart()
        showBanner("Mode edit pesanan dibatalkan.")
    }

    fun markOrderPaidInQueue(
        order: OrderRecord,
        paymentMethod: String,
        splitCash: Long = 0L,
        splitQris: Long = 0L,
        qrisBank: String = "Bank Mandiri",
        changeAmount: Long = 0L,
        cashTendered: Long = 0L
    ) {
        viewModelScope.launch {
            val nowMs = resolveOrderActionTimestampMillis(order)
            val nowText = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(nowMs))
            val ledger = order.getPaymentLedger().toMutableList()
            val timeline = order.getTimelineEvents().toMutableList()

            // Bootstrap initial creation event if empty
            if (timeline.isEmpty()) {
                timeline.add(
                    OrderTimelineEvent(
                        eventId = "evt_init_${order.id}",
                        timestampMillis = nowMs - 600000,
                        timeText = "Waktu Pembuatan Awal",
                        eventType = "ORDER_CREATED",
                        actorUsername = order.createdBy,
                        actorRole = "Staff",
                        title = "Pesanan Dibuat",
                        description = "Pesanan ${order.orderNumber} masuk via ${order.orderMedia} (${order.customerData})."
                    )
                )
            }

            val isSettlingShortfall = order.shortfallAmount > 0L
            val paymentAmount = if (isSettlingShortfall) {
                order.shortfallAmount
            } else {
                val rem = order.totalAmount - order.paidAmount
                if (rem > 0L) rem else order.totalAmount
            }

            val pType = if (isSettlingShortfall) "SHORTFALL_SETTLEMENT" else "INITIAL_PAYMENT"
            val pNote = if (isSettlingShortfall) {
                "Pelunasan sisa kekurangan bayar akibat edit pesanan"
            } else {
                "Pembayaran lunas di antrean pesanan"
            }

            if (paymentMethod.contains("Cash + QRIS", ignoreCase = true)) {
                val netQris = splitQris.coerceAtMost(paymentAmount)
                val netCash = (paymentAmount - netQris).coerceAtLeast(0L)

                ledger.add(
                    OrderPaymentLedgerEntry(
                        paymentId = "pay_${nowMs}_qris_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs,
                        timeText = nowText,
                        paymentType = pType,
                        paymentMethod = "QRIS Waffleo ($qrisBank)",
                        amount = netQris,
                        cashierUsername = _session.value.username,
                        note = "$pNote (Porsi QRIS $qrisBank)"
                    )
                )
                ledger.add(
                    OrderPaymentLedgerEntry(
                        paymentId = "pay_${nowMs}_cash_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs + 1,
                        timeText = nowText,
                        paymentType = pType,
                        paymentMethod = "Tunai (Cash)",
                        amount = netCash,
                        cashierUsername = _session.value.username,
                        note = if (changeAmount > 0L) "$pNote (Porsi Tunai Diterima: ${formatIdr(splitCash)}, Kembalian Tunai: ${formatIdr(changeAmount)})" else "$pNote (Porsi Tunai)"
                    )
                )
                if (changeAmount > 0L) {
                    ledger.add(
                        OrderPaymentLedgerEntry(
                            paymentId = "pay_${nowMs}_change_${UUID.randomUUID().toString().take(4)}",
                            timestampMillis = nowMs + 2,
                            timeText = nowText,
                            paymentType = "CHANGE_REFUND_RETURN",
                            paymentMethod = "Tunai (Kembalian)",
                            amount = changeAmount,
                            cashierUsername = _session.value.username,
                            note = "Kembalian tunai diserahkan ke pelanggan dari pembayaran Cash + QRIS"
                        )
                    )
                }
            } else {
                ledger.add(
                    OrderPaymentLedgerEntry(
                        paymentId = "pay_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs,
                        timeText = nowText,
                        paymentType = pType,
                        paymentMethod = paymentMethod,
                        amount = paymentAmount,
                        cashierUsername = _session.value.username,
                        note = pNote
                    )
                )
            }

            val resolvedPaymentMethod = if (isSettlingShortfall && order.paymentMethod != "Belum Bayar" && order.paymentMethod.isNotBlank()) {
                "${order.paymentMethod} + $paymentMethod"
            } else {
                paymentMethod
            }

            val timelineDesc = if (paymentMethod.contains("Cash + QRIS", ignoreCase = true)) {
                val netQris = splitQris.coerceAtMost(paymentAmount)
                val netCash = (paymentAmount - netQris).coerceAtLeast(0L)
                "Diterima QRIS ${formatIdr(netQris)} ($qrisBank) + Tunai ${formatIdr(netCash)} (Diterima Tunai: ${formatIdr(splitCash)}, Kembalian: ${formatIdr(changeAmount)}) oleh ${_session.value.username}. Status: LUNAS (${formatIdr(order.totalAmount)})."
            } else {
                "Diterima pembayaran ${formatIdr(paymentAmount)} via $paymentMethod oleh ${_session.value.username}. Status: LUNAS (${formatIdr(order.totalAmount)})."
            }

            timeline.add(
                OrderTimelineEvent(
                    eventId = "evt_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                    timestampMillis = nowMs,
                    timeText = nowText,
                    eventType = "PAYMENT_EVENT",
                    actorUsername = _session.value.username,
                    actorRole = _session.value.role.label,
                    title = if (isSettlingShortfall) "Pelunasan Kekurangan Dikonfirmasi" else "Pembayaran Dikonfirmasi",
                    description = timelineDesc
                )
            )

            val actualTendered = when {
                cashTendered > 0L -> cashTendered
                changeAmount > 0L -> order.totalAmount + changeAmount
                paymentMethod.contains("Cash + QRIS", ignoreCase = true) && (splitCash > 0L || splitQris > 0L) -> splitCash + splitQris
                paymentMethod.contains("Tunai", ignoreCase = true) -> order.totalAmount + changeAmount
                else -> order.totalAmount
            }

            val updated = order.copy(
                isPaid = true,
                paidAmount = order.totalAmount,
                cashReceivedAmount = actualTendered,
                paymentMethod = resolvedPaymentMethod,
                shortfallAmount = 0L,
                changeDueAmount = changeAmount,
                splitCashAmount = if (paymentMethod.contains("Cash + QRIS", ignoreCase = true)) splitCash else order.splitCashAmount,
                splitQrisAmount = if (paymentMethod.contains("Cash + QRIS", ignoreCase = true)) splitQris else order.splitQrisAmount,
                paymentLedgerPayload = OrderPaymentLedgerEntry.serializeList(ledger),
                orderTimelinePayload = OrderTimelineEvent.serializeList(timeline),
                createdAt = order.createdAt ?: com.google.firebase.Timestamp(resolveOrderTimestampDate(order)),
                updatedAt = com.google.firebase.Timestamp(Date(nowMs))
            )
            updateLocalOrderItem(updated)
            syncOrderToCloud(updated, isNew = false)
            val paidSnapshot = JSONObject().apply {
                put("isPaid", true)
                put("paidAmount", order.totalAmount)
                put("paymentMethod", resolvedPaymentMethod)
                put("changeDue", order.changeDueAmount)
                put("splitCash", if (paymentMethod.contains("Cash + QRIS", ignoreCase = true)) splitCash else order.splitCashAmount)
                put("splitQris", if (paymentMethod.contains("Cash + QRIS", ignoreCase = true)) splitQris else order.splitQrisAmount)
            }.toString()
            logAuditEvent(
                actionType = "ORDER_PAID",
                entityId = order.orderNumber,
                summary = "Pembayaran pesanan ${order.orderNumber} lunas (${formatIdr(order.totalAmount)}) via $resolvedPaymentMethod",
                beforeSnapshot = """{"isPaid":${order.isPaid},"paidAmount":${order.paidAmount},"shortfall":${order.shortfallAmount}}""",
                afterSnapshot = paidSnapshot,
                customTimestampMillis = nowMs
            )
            showBanner("Pesanan ${order.orderNumber} dinyatakan LUNAS (Paid).")
        }
    }

    /**
     * One-way click: Prepare -> Prepared. Cannot be toggled back once Prepared!
     */
    fun markOrderPreparedOneWay(order: OrderRecord) {
        if (order.isPrepared) return
        viewModelScope.launch {
            val nowMs = resolveOrderActionTimestampMillis(order)
            val nowText = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(nowMs))
            val timeline = order.getTimelineEvents().toMutableList()

            // Bootstrap initial creation event if empty
            if (timeline.isEmpty()) {
                timeline.add(
                    OrderTimelineEvent(
                        eventId = "evt_init_${order.id}",
                        timestampMillis = nowMs - 600000,
                        timeText = "Waktu Pembuatan Awal",
                        eventType = "ORDER_CREATED",
                        actorUsername = order.createdBy,
                        actorRole = "Staff",
                        title = "Pesanan Dibuat",
                        description = "Pesanan ${order.orderNumber} masuk via ${order.orderMedia} (${order.customerData})."
                    )
                )
            }

            timeline.add(
                OrderTimelineEvent(
                    eventId = "evt_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                    timestampMillis = nowMs,
                    timeText = nowText,
                    eventType = "ORDER_PREPARED",
                    actorUsername = _session.value.username,
                    actorRole = _session.value.role.label,
                    title = "Pesanan Selesai Dimasak (Prepared)",
                    description = "Seluruh item pesanan ${order.orderNumber} selesai disiapkan di dapur oleh ${_session.value.username}."
                )
            )
            val updated = order.copy(
                isPrepared = true,
                orderTimelinePayload = OrderTimelineEvent.serializeList(timeline),
                createdAt = order.createdAt ?: com.google.firebase.Timestamp(resolveOrderTimestampDate(order)),
                updatedAt = com.google.firebase.Timestamp(Date(nowMs))
            )
            updateLocalOrderItem(updated)
            syncOrderToCloud(updated, isNew = false)
            logAuditEvent(
                actionType = "ORDER_PREPARED",
                entityId = order.orderNumber,
                summary = "Seluruh item pada pesanan ${order.orderNumber} selesai dibuat (Prepared)",
                beforeSnapshot = """{"isPrepared":false}""",
                afterSnapshot = """{"isPrepared":true}""",
                customTimestampMillis = nowMs
            )
            showBanner("Pesanan ${order.orderNumber} telah selesai dibuat (Prepared).")
        }
    }

    /**
     * Done! button:
     * 1. Strictly requires order.isPaid && order.isPrepared.
     * 2. Moves order to Order History (status = "DONE").
     * 3. Deducts raw material stocks ONLY at this exact moment.
     */
    fun completeOrderAndDeductStock(order: OrderRecord) {
        if (!order.isPaid || !order.isPrepared) {
            showBanner("Pesanan harus berstatus Paid dan Prepared sebelum tombol Done! dapat ditekan.")
            return
        }
        viewModelScope.launch {
            val nowMs = resolveOrderActionTimestampMillis(order)
            val nowText = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(nowMs))
            val timeline = order.getTimelineEvents().toMutableList()

            // Bootstrap initial creation event if empty
            if (timeline.isEmpty()) {
                timeline.add(
                    OrderTimelineEvent(
                        eventId = "evt_init_${order.id}",
                        timestampMillis = nowMs - 600000,
                        timeText = "Waktu Pembuatan Awal",
                        eventType = "ORDER_CREATED",
                        actorUsername = order.createdBy,
                        actorRole = "Staff",
                        title = "Pesanan Dibuat",
                        description = "Pesanan ${order.orderNumber} masuk via ${order.orderMedia} (${order.customerData})."
                    )
                )
            }

            timeline.add(
                OrderTimelineEvent(
                    eventId = "evt_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                    timestampMillis = nowMs,
                    timeText = nowText,
                    eventType = "ORDER_COMPLETED",
                    actorUsername = _session.value.username,
                    actorRole = _session.value.role.label,
                    title = "Pesanan Selesai",
                    description = "Pesanan ${order.orderNumber} diserahkan ke customer oleh ${_session.value.username} dan dipindahkan ke Order History."
                )
            )
            val updatedOrder = order.copy(
                status = "DONE",
                shortfallAmount = 0L,
                changeDueAmount = 0L,
                orderTimelinePayload = OrderTimelineEvent.serializeList(timeline),
                createdAt = order.createdAt ?: com.google.firebase.Timestamp(resolveOrderTimestampDate(order)),
                updatedAt = com.google.firebase.Timestamp(Date(nowMs))
            )
            updateLocalOrderItem(updatedOrder)
            syncOrderToCloud(updatedOrder, isNew = false)

            // Calculate ingredient usage from order line items
            val lineItems = CartLineItem.deserializeList(order.itemsPayload)
            val totalWaffles = lineItems.filter { it.category == "Egg Waffle" }.sumOf { it.qty }
            val totalDrinks = lineItems.filter { it.category == "Drinks" }.sumOf { it.qty }

            val currentIngredients = catalogRecords.value.filter { it.recordType == "INGREDIENT" }
            val deductedDetails = mutableListOf<String>()

            currentIngredients.forEach { ing ->
                val meta = try { JSONObject(ing.metadataJson) } catch (e: Exception) { JSONObject() }
                val perWaffle = meta.optDouble("usagePerWaffle", 0.0)
                val perDrink = meta.optDouble("usagePerDrink", 0.0)
                val usedQty = (totalWaffles * perWaffle) + (totalDrinks * perDrink)
                if (usedQty > 0.0) {
                    val newStock = (ing.stockQty - usedQty).coerceAtLeast(0.0)
                    val updatedIng = ing.copy(stockQty = newStock)
                    updateLocalSeedCatalogItem(updatedIng)
                    syncCatalogItemToCloud(updatedIng)
                    deductedDetails.add("${ing.name}: -${usedQty.toInt()} ${ing.stockUnit} (Sisa: ${newStock.toInt()} ${ing.stockUnit})")
                }
            }

            logAuditEvent(
                actionType = "ORDER_DONE_STOCK_DEDUCTED",
                entityId = order.orderNumber,
                summary = "Pesanan ${order.orderNumber} SELESAI (Done!) & diserahkan ke customer.",
                beforeSnapshot = """{"status":"ACTIVE"}""",
                afterSnapshot = """{"status":"DONE"}""",
                customTimestampMillis = nowMs
            )

            // Immediately sync backdated orders (or any date that already has POS sync in Money Manager) to Money Manager under the order's date & time
            val ordDate = resolveOrderTimestampDate(updatedOrder)
            val ordDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(ordDate)
            val ordTimeText = SimpleDateFormat("HH:mm", Locale.US).format(ordDate)
            val hasExistingDateSync = _allMoneyTransactions.value.any {
                it.dateKey == ordDateKey && (it.isAutoPosSync || it.id.startsWith("v1_pos_sync_"))
            }
            if (isBackdatedOrder(updatedOrder) || hasExistingDateSync) {
                syncPosSalesForDateKey(
                    targetDateKey = ordDateKey,
                    preferredTimeText = ordTimeText,
                    silent = true
                )
            }

            showBanner("Pesanan ${order.orderNumber} Selesai (Done!) & dipindahkan ke Order History.")
        }
    }

    fun cancelOrderWithReason(order: OrderRecord, reason: String) {
        viewModelScope.launch {
            val nowMs = resolveOrderActionTimestampMillis(order)
            val nowText = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(nowMs))
            val actorInfo = "${_session.value.username} (${_session.value.role.label})"
            val timeline = order.getTimelineEvents().toMutableList()

            // Bootstrap initial creation event if empty
            if (timeline.isEmpty()) {
                timeline.add(
                    OrderTimelineEvent(
                        eventId = "evt_init_${order.id}",
                        timestampMillis = nowMs - 600000,
                        timeText = "Waktu Pembuatan Awal",
                        eventType = "ORDER_CREATED",
                        actorUsername = order.createdBy,
                        actorRole = "Staff",
                        title = "Pesanan Dibuat",
                        description = "Pesanan ${order.orderNumber} masuk via ${order.orderMedia} (${order.customerData})."
                    )
                )
            }

            timeline.add(
                OrderTimelineEvent(
                    eventId = "evt_${nowMs}_${UUID.randomUUID().toString().take(4)}",
                    timestampMillis = nowMs,
                    timeText = nowText,
                    eventType = "ORDER_CANCELLED",
                    actorUsername = _session.value.username,
                    actorRole = _session.value.role.label,
                    title = "Pesanan Dibatalkan",
                    description = "Pesanan dibatalkan oleh $actorInfo. Alasan: $reason."
                )
            )

            if (order.isPaid && order.totalAmount > 0L) {
                timeline.add(
                    OrderTimelineEvent(
                        eventId = "evt_${nowMs + 1}_refund_${UUID.randomUUID().toString().take(4)}",
                        timestampMillis = nowMs + 1,
                        timeText = nowText,
                        eventType = "ORDER_REFUND",
                        actorUsername = _session.value.username,
                        actorRole = _session.value.role.label,
                        title = "Pengembalian Dana (Refund)",
                        description = "Pengembalian dana senilai ${formatIdr(order.totalAmount)} dalam bentuk Tunai (Cash)."
                    )
                )
            }
            val updated = order.copy(
                status = "CANCELLED",
                cancelReason = reason,
                cancelledBy = actorInfo,
                cancelledAtText = nowText,
                orderTimelinePayload = OrderTimelineEvent.serializeList(timeline),
                createdAt = order.createdAt ?: com.google.firebase.Timestamp(resolveOrderTimestampDate(order)),
                updatedAt = com.google.firebase.Timestamp(Date(nowMs))
            )
            updateLocalOrderItem(updated)
            syncOrderToCloud(updated, isNew = false)
            // Restore promo quota if order had a promo applied
            if (order.status != "CANCELLED" && order.status != "ERASED" && order.discountAmount > 0L && order.promoCode.isNotBlank()) {
                restorePromoQuotaForOrder(order)
            }
            logAuditEvent(
                actionType = "ORDER_CANCELLED",
                entityId = order.orderNumber,
                summary = "Pesanan ${order.orderNumber} DIBATALKAN oleh $actorInfo pada $nowText. Alasan: $reason (Stok bahan tidak dikurangi)",
                beforeSnapshot = """{"status":"ACTIVE","total":${order.totalAmount}}""",
                afterSnapshot = """{"status":"CANCELLED","reason":${JSONObject.quote(reason)},"cancelledBy":${JSONObject.quote(actorInfo)},"cancelledAt":"$nowText"}""",
                customTimestampMillis = nowMs
            )
            val ordDate = resolveOrderTimestampDate(updated)
            val ordDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(ordDate)
            val hasExistingDateSync = _allMoneyTransactions.value.any {
                it.dateKey == ordDateKey && (it.isAutoPosSync || it.id.startsWith("v1_pos_sync_"))
            }
            if (isBackdatedOrder(updated) || hasExistingDateSync) {
                syncPosSalesForDateKey(targetDateKey = ordDateKey, silent = true)
            }
            showBanner("Pesanan ${order.orderNumber} dibatalkan dan dipindahkan ke Riwayat Dibatalkan.")
        }
    }

    /**
     * OWNER-ONLY ORDER ERASURE (ERASED STATUS):
     * Completely removes the order from active sales/recap metrics as if it never existed:
     * 1. Transitions status to 'ERASED' and preserves full audit timeline in 'Erased - Dihapus' tab.
     * 2. Restores raw material stock if the order was previously completed ('DONE').
     * 3. Re-synchronizes Money Manager daily POS transactions for the order's date.
     * 4. Appends a rich, detailed AUDIT LOG entry capturing pre/post snapshots and restored amounts.
     */
    fun eraseOrderAndRevertMetrics(order: OrderRecord, reason: String = "Dihapus oleh Owner") {
        if (_session.value.role != StaffRole.OWNER && _session.value.role != StaffRole.DEVELOPER) {
            showBanner("Hanya akun Owner yang memiliki wewenang untuk menghapus pesanan.")
            return
        }

        viewModelScope.launch {
            val nowMs = System.currentTimeMillis()
            val nowText = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(nowMs))
            val actorInfo = "${_session.value.username} (${_session.value.role.label})"
            val timeline = order.getTimelineEvents().toMutableList()

            // 1. Record ERASED timeline event
            timeline.add(
                OrderTimelineEvent(
                    eventId = "evt_${nowMs}_erased_${UUID.randomUUID().toString().take(4)}",
                    timestampMillis = nowMs,
                    timeText = nowText,
                    eventType = "ORDER_ERASED",
                    actorUsername = _session.value.username,
                    actorRole = _session.value.role.label,
                    title = "Pesanan Dihapus (Erased)",
                    description = "Pesanan ${order.orderNumber} dihapus oleh $actorInfo dari daftar penjualan aktif dan dipindahkan ke arsip 'Erased - Dihapus'. Catatan: $reason."
                )
            )

            // 2. Restore ingredient stocks IF order was previously completed (DONE)
            val restoredIngredients = mutableListOf<String>()
            if (order.status == "DONE") {
                val lineItems = CartLineItem.deserializeList(order.itemsPayload)
                val totalWaffles = lineItems.filter { it.category == "Egg Waffle" }.sumOf { it.qty }
                val totalDrinks = lineItems.filter { it.category == "Drinks" }.sumOf { it.qty }

                val currentIngredients = catalogRecords.value.filter { it.recordType == "INGREDIENT" }
                currentIngredients.forEach { ing ->
                    val meta = try { JSONObject(ing.metadataJson) } catch (e: Exception) { JSONObject() }
                    val perWaffle = meta.optDouble("usagePerWaffle", 0.0)
                    val perDrink = meta.optDouble("usagePerDrink", 0.0)
                    val usedQty = (totalWaffles * perWaffle) + (totalDrinks * perDrink)
                    if (usedQty > 0.0) {
                        val restoredStock = ing.stockQty + usedQty
                        val updatedIng = ing.copy(stockQty = restoredStock)
                        updateLocalSeedCatalogItem(updatedIng)
                        syncCatalogItemToCloud(updatedIng)
                        restoredIngredients.add("${ing.name}: +${usedQty.toInt()} ${ing.stockUnit} (Total: ${restoredStock.toInt()} ${ing.stockUnit})")
                    }
                }
            }

            // 3. Mark Order as ERASED with preserved details
            val erasedOrder = order.copy(
                status = "ERASED",
                cancelReason = reason,
                cancelledBy = actorInfo,
                cancelledAtText = nowText,
                orderTimelinePayload = OrderTimelineEvent.serializeList(timeline),
                createdAt = order.createdAt ?: com.google.firebase.Timestamp(resolveOrderTimestampDate(order)),
                updatedAt = com.google.firebase.Timestamp(Date(nowMs))
            )
            updateLocalOrderItem(erasedOrder)
            syncOrderToCloud(erasedOrder, isNew = false)
            // Restore promo quota if order had a promo applied and wasn't already cancelled/erased
            if (order.status != "CANCELLED" && order.status != "ERASED" && order.discountAmount > 0L && order.promoCode.isNotBlank()) {
                restorePromoQuotaForOrder(order)
            }

            // 4. Log comprehensive Audit Trail
            val restoredStockText = if (restoredIngredients.isNotEmpty()) " • Stok dipulihkan: ${restoredIngredients.joinToString(", ")}" else ""
            logAuditEvent(
                actionType = "ORDER_ERASED",
                entityId = order.orderNumber,
                summary = "Pesanan ${order.orderNumber} senilai ${formatIdr(order.totalAmount)} DIHAPUS oleh $actorInfo. Data keuangan & penjualan disinkronkan kembali$restoredStockText.",
                beforeSnapshot = """{"status":"${order.status}","totalAmount":${order.totalAmount},"paidAmount":${order.paidAmount},"isPaid":${order.isPaid},"itemsSummary":${JSONObject.quote(order.itemsSummary)}}""",
                afterSnapshot = """{"status":"ERASED","erasedBy":${JSONObject.quote(actorInfo)},"erasedAt":"$nowText","reason":${JSONObject.quote(reason)},"restoredIngredients":${JSONObject.quote(restoredIngredients.joinToString("; "))}}""",
                customTimestampMillis = nowMs
            )

            // 5. Re-synchronize Money Manager for this order's dateKey so daily sales & cash flow reflect the deduction
            val ordDate = resolveOrderTimestampDate(order)
            val ordDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(ordDate)
            syncPosSalesForDateKey(targetDateKey = ordDateKey, silent = true)

            showBanner("Pesanan ${order.orderNumber} berhasil dihapus dari daftar penjualan aktif dan dipindahkan ke 'Erased - Dihapus'.")
        }
    }

    fun deleteOrderRecord(order: OrderRecord) {
        if (_session.value.role != StaffRole.OWNER && _session.value.role != StaffRole.DEVELOPER) {
            showBanner("Hanya Owner atau Developer yang dapat menghapus pesanan secara permanen.")
            return
        }
        viewModelScope.launch {
            val updatedLocal = _localOrders.value.filterNot { it.id == order.id }
            _localOrders.value = updatedLocal
            localStore?.saveOrders(updatedLocal)

            clearPendingSyncKey("order:${order.id}")
            val delKey = "order_delete:${order.id}"
            markPendingSyncKey(delKey)

            if (isCloudAuthenticated && isNetworkOnline.value) {
                repository.deleteOrder(workspaceId, order.id)
                clearPendingSyncKey(delKey)
            }
            logAuditEvent(
                actionType = "ORDER_DELETED_PERMANENT",
                entityId = order.id,
                summary = "Menghapus permanen pesanan ${order.orderNumber} dari database",
                beforeSnapshot = """{"id":"${order.id}","orderNumber":"${order.orderNumber}","total":${order.totalAmount}}""",
                afterSnapshot = "{}"
            )
            showBanner("Pesanan ${order.orderNumber} berhasil dihapus permanen.")
        }
    }

    // --- DYNAMIC CATALOG, MODIFIER, INGREDIENT, PROMO & STAFF MANAGEMENT ---

    fun saveOrUpdateCatalogRecord(record: CatalogRecord, actionDescription: String) {
        viewModelScope.launch {
            val existing = allCatalogRecords.value.firstOrNull { it.id == record.id }
            val resolvedBranch = record.branchId.ifBlank { _session.value.branchId }
            val finalRecord = if (record.id.isBlank()) {
                val branchSiblings = allCatalogRecords.value.filter {
                    it.branchId.equals(resolvedBranch, ignoreCase = true) && it.recordType == record.recordType
                }
                val maxOrder = branchSiblings.mapNotNull { it.sortOrder.takeIf { o -> o < 900000 } }.maxOrNull() ?: branchSiblings.size
                record.copy(
                    id = "cat_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                    userId = workspaceId,
                    branchId = resolvedBranch
                ).withSortOrder(maxOrder + 1)
            } else {
                val baseRec = record.copy(
                    userId = workspaceId,
                    branchId = resolvedBranch,
                    createdAt = existing?.createdAt
                )
                if (existing != null && existing.sortOrder < 900000 && baseRec.sortOrder >= 900000) {
                    baseRec.withSortOrder(existing.sortOrder)
                } else {
                    baseRec
                }
            }
            if (finalRecord.recordType == "MODIFIER" && finalRecord.category.isNotBlank()) {
                if (_customModifierCategories.value.none { it.equals(finalRecord.category, ignoreCase = true) }) {
                    val updatedMods = (_customModifierCategories.value + finalRecord.category).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
                    _customModifierCategories.value = updatedMods
                    localStore?.saveCustomCategories(_customMenuCategories.value, updatedMods)
                    syncWorkspaceSettingsToCloud()
                }
            } else if (finalRecord.recordType == "MENU_ITEM" && finalRecord.category.isNotBlank()) {
                if (_customMenuCategories.value.none { it.equals(finalRecord.category, ignoreCase = true) }) {
                    val updatedMenus = (_customMenuCategories.value + finalRecord.category).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
                    _customMenuCategories.value = updatedMenus
                    localStore?.saveCustomCategories(updatedMenus, _customModifierCategories.value)
                    syncWorkspaceSettingsToCloud()
                }
            }
            updateLocalSeedCatalogItem(finalRecord)
            syncCatalogItemToCloud(finalRecord)
            logAuditEvent(
                actionType = "CATALOG_UPDATED",
                entityId = finalRecord.id,
                summary = actionDescription,
                beforeSnapshot = existing?.let { """{"name":"${it.name}","walkIn":${it.priceWalkIn},"stock":${it.stockQty},"active":${it.isActive}}""" } ?: "{}",
                afterSnapshot = """{"name":"${finalRecord.name}","walkIn":${finalRecord.priceWalkIn},"stock":${finalRecord.stockQty},"active":${finalRecord.isActive}}"""
            )
            showBanner(actionDescription)
        }
    }

    /**
     * Saves the new visual ordering of Menu or Modifier items after a drag-and-drop or step-move gesture,
     * persisting immediately to local storage and synchronizing live to Cloud Firestore.
     */
    fun reorderCatalogItems(orderedSubsetIds: List<String>, recordType: String) {
        if (orderedSubsetIds.isEmpty()) return
        val activeBranch = _session.value.branchId
        // Merge allCatalogRecords and _localSeededCatalog to ensure every active branch item is present
        val baseMap = LinkedHashMap<String, CatalogRecord>()
        _localSeededCatalog.value.forEach { baseMap[it.id] = it }
        allCatalogRecords.value.forEach { if (!baseMap.containsKey(it.id)) baseMap[it.id] = it }

        val branchItems = baseMap.values
            .filter { it.branchId.equals(activeBranch, ignoreCase = true) && it.recordType == recordType }
            .sortedWith(catalogItemComparator())

        if (branchItems.isEmpty()) return

        val subsetSet = orderedSubsetIds.toSet()
        val itemById = branchItems.associateBy { it.id }
        val orderedSubset = orderedSubsetIds.mapNotNull { itemById[it] }

        // Replace the subset slots in branchItems in their new order
        var subsetCursor = 0
        val newFullBranchOrder = branchItems.map { item ->
            if (item.id in subsetSet && subsetCursor < orderedSubset.size) {
                orderedSubset[subsetCursor++]
            } else {
                item
            }
        }

        val updatedRecordsToSync = mutableListOf<CatalogRecord>()
        newFullBranchOrder.forEachIndexed { index, rec ->
            val targetOrder = index + 1
            if (rec.sortOrder != targetOrder) {
                val updated = rec.withSortOrder(targetOrder)
                baseMap[updated.id] = updated
                updatedRecordsToSync.add(updated)
            }
        }

        if (updatedRecordsToSync.isNotEmpty()) {
            val updatedList = baseMap.values.toList()
            _localSeededCatalog.value = updatedList
            localStore?.saveCatalog(updatedList)
            updatedRecordsToSync.forEach { rec ->
                syncCatalogItemToCloud(rec)
            }
            val label = if (recordType == "MODIFIER") "Modifier" else "Menu"
            showBanner("Urutan posisi item $label berhasil disimpan & disinkronkan.")
        }
    }

    fun deleteCatalogRecord(record: CatalogRecord) {
        if (!_session.value.role.canManageCatalogAndDelete) {
            showBanner("Akses ditolak: Hanya Owner, Supervisor, atau Developer yang dapat menghapus item.")
            return
        }
        viewModelScope.launch {
            _localSeededCatalog.value = _localSeededCatalog.value.filterNot { it.id == record.id }
            localStore?.saveCatalog(_localSeededCatalog.value)
            clearPendingSyncKey("catalog:${record.id}")
            if (record.id.isNotBlank()) {
                val delKey = "catalog_delete:${record.id}"
                markPendingSyncKey(delKey)
                if (isCloudAuthenticated && isNetworkOnline.value) {
                    repository.deleteCatalogItem(workspaceId, record.id)
                    clearPendingSyncKey(delKey)
                }
            }
            logAuditEvent(
                actionType = "CATALOG_DELETED",
                entityId = record.id,
                summary = "Menghapus item '${record.name}' (${record.category}) dari Cabang ${record.branchId}",
                beforeSnapshot = """{"id":"${record.id}","name":"${record.name}","category":"${record.category}","walkIn":${record.priceWalkIn}}""",
                afterSnapshot = "{}"
            )
            showBanner("Item '${record.name}' berhasil dihapus.")
        }
    }

    fun toggleCatalogRecordActive(record: CatalogRecord) {
        val nextActive = !record.isActive
        saveOrUpdateCatalogRecord(
            record = record.copy(isActive = nextActive),
            actionDescription = "Status '${record.name}' diubah menjadi ${if (nextActive) "AKTIF" else "NON-AKTIF"}"
        )
    }

    // --- PROMO MANAGEMENT & ANALYTICS ENGINE ---

    fun incrementPromoUsageCount(promoId: String, delta: Int) {
        if (promoId.isBlank() || delta == 0) return
        val pool = (allCatalogRecords.value + _localSeededCatalog.value).distinctBy { it.id }
        val promo = pool.firstOrNull { it.id == promoId && it.recordType == "PROMO" } ?: return
        val cfg = PromoConfig.fromCatalogRecord(promo)
        val newUsed = (cfg.usedCount + delta).coerceAtLeast(0)
        val updatedCfg = cfg.copy(usedCount = newUsed)
        val updatedRec = promo.copy(
            metadataJson = updatedCfg.toJsonString(promo.sortOrder),
            updatedAt = com.google.firebase.Timestamp(Date())
        )
        updateLocalSeedCatalogItem(updatedRec)
        syncCatalogItemToCloud(updatedRec)
    }

    fun restorePromoQuotaForOrder(order: OrderRecord) {
        val rawCode = order.promoCode.trim()
        if (rawCode.isBlank()) return
        val allPromos = (allCatalogRecords.value + _localSeededCatalog.value)
            .filter { it.recordType == "PROMO" }
            .distinctBy { it.id }
        val tokens = rawCode.split("+", ",", ";").map { it.trim() }.filter { it.isNotBlank() }
        tokens.forEach { token ->
            val matched = allPromos.firstOrNull {
                it.id.equals(token, ignoreCase = true) ||
                    it.name.equals(token, ignoreCase = true) ||
                    token.startsWith(it.name, ignoreCase = true)
            }
            if (matched != null) {
                incrementPromoUsageCount(matched.id, delta = -1)
            }
        }
    }

    fun savePromoWithConfig(
        existingRecord: CatalogRecord?,
        name: String,
        config: PromoConfig
    ) {
        if (!_session.value.role.canManageCatalogAndDelete) {
            showBanner("Akses ditolak: Hanya Owner, Supervisor, atau Developer yang dapat mengelola Promo.")
            return
        }
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            showBanner("Nama promo wajib diisi.")
            return
        }
        val activeBranch = _session.value.branchId
        val resolvedBranch = existingRecord?.branchId?.ifBlank { activeBranch } ?: activeBranch
        val sortOrd = existingRecord?.sortOrder?.takeIf { it < 900000 } ?: run {
            val siblings = allCatalogRecords.value.filter { it.recordType == "PROMO" }
            (siblings.mapNotNull { it.sortOrder.takeIf { o -> o < 900000 } }.maxOrNull() ?: siblings.size) + 1
        }
        val primaryPriceField = when (config.promoType) {
            PromoType.BUNDLE -> if (config.benefitType == PromoBenefitType.FIXED_PACKAGE_PRICE) config.fixedPackagePrice else config.discountValue
            PromoType.QUANTITY_DISCOUNT -> config.quantityTiers.firstOrNull()?.discountValue ?: config.discountValue
            PromoType.PRODUCT_DISCOUNT, PromoType.MINIMUM_PURCHASE -> config.discountValue
        }
        val finalId = existingRecord?.id?.takeIf { it.isNotBlank() }
            ?: "promo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
        val nowTs = com.google.firebase.Timestamp(Date())

        val finalRecord = CatalogRecord(
            id = finalId,
            userId = workspaceId,
            branchId = resolvedBranch,
            recordType = "PROMO",
            category = config.promoType.code,
            name = cleanName,
            priceWalkIn = primaryPriceField,
            priceGrabFood = primaryPriceField,
            priceGoFood = primaryPriceField,
            priceShopeeFood = primaryPriceField,
            isActive = !config.isDraft && (existingRecord?.isActive ?: true),
            metadataJson = config.toJsonString(sortOrd),
            createdAt = existingRecord?.createdAt ?: nowTs,
            updatedAt = nowTs
        )

        updateLocalSeedCatalogItem(finalRecord)
        syncCatalogItemToCloud(finalRecord)

        val benefitText = WaffleoPromoEngine.formatBenefitSummary(config)
        val isNew = existingRecord == null || existingRecord.id.isBlank()
        logAuditEvent(
            actionType = if (isNew) "PROMO_CREATED" else "PROMO_UPDATED",
            entityId = finalRecord.id,
            summary = if (isNew) {
                "Membuat Promo Baru '$cleanName' (${config.promoType.label} • $benefitText)"
            } else {
                "Memperbarui Promo '$cleanName' (${config.promoType.label} • $benefitText)"
            },
            beforeSnapshot = existingRecord?.let {
                """{"name":${JSONObject.quote(it.name)},"category":"${it.category}","active":${it.isActive}}"""
            } ?: "{}",
            afterSnapshot = """{"id":"${finalRecord.id}","name":${JSONObject.quote(cleanName)},"type":"${config.promoType.code}","benefit":${JSONObject.quote(benefitText)},"active":${finalRecord.isActive},"draft":${config.isDraft}}"""
        )
        showBanner(if (isNew) "Promo '$cleanName' berhasil dibuat & disimpan." else "Perubahan Promo '$cleanName' berhasil disimpan.")
    }

    fun duplicatePromoRecord(promo: CatalogRecord) {
        if (!_session.value.role.canManageCatalogAndDelete) {
            showBanner("Akses ditolak: Hanya Owner, Supervisor, atau Developer yang dapat menduplikasi Promo.")
            return
        }
        val origCfg = PromoConfig.fromCatalogRecord(promo)
        val copyName = "${promo.name} (Salinan)"
        val copyCfg = origCfg.copy(
            isDraft = true,
            usedCount = 0,
            voucherCode = if (origCfg.voucherCode.isNotBlank()) "${origCfg.voucherCode}2" else ""
        )
        val siblings = allCatalogRecords.value.filter { it.recordType == "PROMO" }
        val nextSort = (siblings.mapNotNull { it.sortOrder.takeIf { o -> o < 900000 } }.maxOrNull() ?: siblings.size) + 1
        val nowTs = com.google.firebase.Timestamp(Date())
        val newRec = promo.copy(
            id = "promo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            userId = workspaceId,
            branchId = _session.value.branchId,
            name = copyName,
            isActive = false,
            metadataJson = copyCfg.toJsonString(nextSort),
            createdAt = nowTs,
            updatedAt = nowTs
        )
        updateLocalSeedCatalogItem(newRec)
        syncCatalogItemToCloud(newRec)
        logAuditEvent(
            actionType = "PROMO_DUPLICATED",
            entityId = newRec.id,
            summary = "Menduplikasi Promo '${promo.name}' menjadi '$copyName' (Status: Draft)",
            beforeSnapshot = """{"sourcePromoId":"${promo.id}","sourceName":${JSONObject.quote(promo.name)}}""",
            afterSnapshot = """{"newPromoId":"${newRec.id}","newName":${JSONObject.quote(copyName)}}"""
        )
        showBanner("Promo '${promo.name}' berhasil diduplikasi sebagai Draft.")
    }

    fun togglePromoRecordActive(promo: CatalogRecord) {
        if (!_session.value.role.canManageCatalogAndDelete) {
            showBanner("Akses ditolak: Hanya Owner, Supervisor, atau Developer yang dapat mengubah status Promo.")
            return
        }
        val cfg = PromoConfig.fromCatalogRecord(promo)
        val nextActive = !promo.isActive
        val updatedCfg = if (nextActive && cfg.isDraft) cfg.copy(isDraft = false) else cfg
        val updatedRec = promo.copy(
            isActive = nextActive,
            metadataJson = updatedCfg.toJsonString(promo.sortOrder),
            updatedAt = com.google.firebase.Timestamp(Date())
        )
        updateLocalSeedCatalogItem(updatedRec)
        syncCatalogItemToCloud(updatedRec)
        val statusLabel = if (nextActive) "AKTIF" else "NONAKTIF"
        logAuditEvent(
            actionType = "PROMO_STATUS_CHANGED",
            entityId = promo.id,
            summary = "Mengubah status Promo '${promo.name}' menjadi $statusLabel",
            beforeSnapshot = """{"active":${promo.isActive},"draft":${cfg.isDraft}}""",
            afterSnapshot = """{"active":$nextActive,"draft":${updatedCfg.isDraft}}"""
        )
        showBanner("Status Promo '${promo.name}' diubah menjadi $statusLabel.")
    }

    fun isPromoUsedInTransactions(promo: CatalogRecord, ordersList: List<OrderRecord> = allOrders.value): Boolean {
        return ordersList.any { ord ->
            ord.discountAmount > 0L &&
                ord.promoCode.isNotBlank() &&
                ord.promoCode.split("+", ",", ";").any { token ->
                    val t = token.trim()
                    t.equals(promo.id, ignoreCase = true) ||
                        t.equals(promo.name, ignoreCase = true) ||
                        t.startsWith(promo.name, ignoreCase = true)
                }
        }
    }

    fun getPromoUsageCount(promo: CatalogRecord, ordersList: List<OrderRecord> = allOrders.value): Int {
        val cfg = PromoConfig.fromCatalogRecord(promo)
        val dynamicMap = computeDynamicPromoUsageMap(ordersList)
        return maxOf(cfg.usedCount, dynamicMap[promo.id] ?: 0)
    }

    fun deleteOrArchivePromoRecord(promo: CatalogRecord) {
        if (!_session.value.role.canManageCatalogAndDelete) {
            showBanner("Akses ditolak: Hanya Owner, Supervisor, atau Developer yang dapat menghapus Promo.")
            return
        }
        val wasUsed = isPromoUsedInTransactions(promo)
        if (wasUsed) {
            // Promo sudah pernah digunakan dalam transaksi: Jangan hapus paksa agar histori transaksi tetap utuh. Nonaktifkan/arsipkan.
            val cfg = PromoConfig.fromCatalogRecord(promo)
            val archivedCfg = cfg.copy(isArchived = true, isDraft = false)
            val updatedRec = promo.copy(
                isActive = false,
                metadataJson = archivedCfg.toJsonString(promo.sortOrder),
                updatedAt = com.google.firebase.Timestamp(Date())
            )
            updateLocalSeedCatalogItem(updatedRec)
            syncCatalogItemToCloud(updatedRec)
            logAuditEvent(
                actionType = "PROMO_ARCHIVED",
                entityId = promo.id,
                summary = "Mengarsipkan & menonaktifkan Promo '${promo.name}' (Promo sudah pernah digunakan dalam transaksi sehingga histori tetap terjaga)",
                beforeSnapshot = """{"id":"${promo.id}","name":${JSONObject.quote(promo.name)},"active":${promo.isActive},"archived":false}""",
                afterSnapshot = """{"id":"${promo.id}","name":${JSONObject.quote(promo.name)},"active":false,"archived":true}"""
            )
            showBanner("Promo '${promo.name}' telah digunakan di transaksi historis -> Dinonaktifkan & Diarsipkan agar histori aman.")
        } else {
            deleteCatalogRecord(promo)
        }
    }

    fun computePromoPerformanceStats(
        ordersList: List<OrderRecord> = orders.value,
        promosList: List<CatalogRecord> = catalogRecords.value.filter { it.recordType == "PROMO" }
    ): List<PromoPerformanceStat> {
        val validOrders = ordersList.filter {
            it.status != "CANCELLED" && it.status != "ERASED" && it.discountAmount > 0L && it.promoCode.isNotBlank()
        }
        val dynamicUsage = computeDynamicPromoUsageMap(ordersList)

        return promosList.map { promo ->
            val cfg = PromoConfig.fromCatalogRecord(promo)
            val matchingOrders = validOrders.filter { ord ->
                ord.promoCode.split("+", ",", ";").any { token ->
                    val t = token.trim()
                    t.equals(promo.id, ignoreCase = true) ||
                        t.equals(promo.name, ignoreCase = true) ||
                        t.startsWith(promo.name, ignoreCase = true)
                }
            }
            val productCounts = mutableMapOf<String, Int>()
            matchingOrders.forEach { ord ->
                val items = CartLineItem.deserializeList(ord.itemsPayload)
                items.forEach { line ->
                    val key = line.primaryItemName.ifBlank { line.category }
                    productCounts[key] = (productCounts[key] ?: 0) + line.qty
                }
            }
            val topProds = productCounts.entries
                .sortedByDescending { it.value }
                .take(3)
                .joinToString(", ") { "${it.key} (${it.value}x)" }
                .ifBlank { "Belum ada transaksi" }

            val txCount = matchingOrders.size
            val used = maxOf(cfg.usedCount, dynamicUsage[promo.id] ?: txCount)
            val status = WaffleoPromoEngine.resolvePromoStatus(promo, cfg, used)

            PromoPerformanceStat(
                promoId = promo.id,
                promoName = promo.name,
                promoTypeLabel = cfg.promoType.label,
                status = status,
                usageCount = used,
                transactionCount = txCount,
                totalDiscountGiven = matchingOrders.sumOf { it.discountAmount },
                totalNetRevenue = matchingOrders.sumOf { it.totalAmount },
                topProductsSummary = topProds
            )
        }.sortedWith(
            compareByDescending<PromoPerformanceStat> { it.totalNetRevenue }
                .thenByDescending { it.usageCount }
        )
    }

    fun addMenuCategory(categoryName: String) {
        val trimmed = categoryName.trim()
        if (trimmed.isBlank()) return
        if (_customMenuCategories.value.any { it.equals(trimmed, ignoreCase = true) }) {
            showBanner("Kategori Menu '$trimmed' sudah ada.")
            return
        }
        val updated = (_customMenuCategories.value + trimmed).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
        _customMenuCategories.value = updated
        localStore?.saveCustomCategories(updated, _customModifierCategories.value)
        syncWorkspaceSettingsToCloud()
        logAuditEvent(
            actionType = "CATALOG_CATEGORY_ADDED",
            entityId = trimmed,
            summary = "Menambahkan kategori menu baru: '$trimmed'",
            beforeSnapshot = "{}",
            afterSnapshot = """{"categoryName":"$trimmed","targetType":"MENU_ITEM"}"""
        )
        showBanner("Kategori Menu '$trimmed' berhasil ditambahkan.")
    }

    fun addModifierCategory(categoryName: String, connectedMenuCategory: String = "") {
        val trimmed = categoryName.trim()
        if (trimmed.isBlank()) return
        if (_customModifierCategories.value.any { it.equals(trimmed, ignoreCase = true) }) {
            showBanner("Kategori Modifier '$trimmed' sudah ada.")
            return
        }
        val updated = (_customModifierCategories.value + trimmed).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
        _customModifierCategories.value = updated
        localStore?.saveCustomCategories(_customMenuCategories.value, updated)
        if (connectedMenuCategory.isNotBlank()) {
            _modifierToMenuCategoryMap.value = _modifierToMenuCategoryMap.value + (trimmed to connectedMenuCategory.trim())
        }
        syncWorkspaceSettingsToCloud()
        logAuditEvent(
            actionType = "CATALOG_CATEGORY_ADDED",
            entityId = trimmed,
            summary = "Menambahkan kategori modifier baru: '$trimmed'",
            beforeSnapshot = "{}",
            afterSnapshot = """{"categoryName":"$trimmed","targetType":"MODIFIER"}"""
        )
        showBanner("Kategori Modifier '$trimmed' berhasil ditambahkan.")
    }

    fun reorderMenuCategory(categoryKey: String, direction: Int) {
        val current = _customMenuCategories.value.toMutableList()
        val idx = current.indexOfFirst { it.equals(categoryKey, ignoreCase = true) }
        if (idx >= 0) {
            val targetIdx = idx + direction
            if (targetIdx in current.indices) {
                val item = current.removeAt(idx)
                current.add(targetIdx, item)
                _customMenuCategories.value = current
                localStore?.saveCustomCategories(current, _customModifierCategories.value)
                syncWorkspaceSettingsToCloud()
                showBanner("Urutan Kategori Menu '$categoryKey' diperbarui.")
            }
        }
    }

    fun reorderModifierCategory(categoryKey: String, direction: Int) {
        val current = _customModifierCategories.value.toMutableList()
        val idx = current.indexOfFirst { it.equals(categoryKey, ignoreCase = true) }
        if (idx >= 0) {
            val targetIdx = idx + direction
            if (targetIdx in current.indices) {
                val item = current.removeAt(idx)
                current.add(targetIdx, item)
                _customModifierCategories.value = current
                localStore?.saveCustomCategories(_customMenuCategories.value, current)
                syncWorkspaceSettingsToCloud()
                showBanner("Urutan Kategori Modifier '$categoryKey' diperbarui.")
            }
        }
    }

    fun renameCategory(
        targetType: String,
        oldName: String,
        newName: String,
        connectedMenuCategory: String? = null
    ) {
        val trimmedOld = oldName.trim()
        val trimmedNew = newName.trim()
        if (trimmedOld.isBlank() || trimmedNew.isBlank()) return

        if (targetType == "MENU") {
            val currentList = _customMenuCategories.value.toMutableList()
            val idx = currentList.indexOfFirst { areCategoriesMatching(it, trimmedOld) }
            if (idx >= 0) {
                currentList[idx] = trimmedNew
            } else {
                currentList.add(trimmedNew)
            }
            val deduplicated = currentList.filter { it.isNotBlank() }.distinctBy { normalizeCategoryCanonical(it) }
            _customMenuCategories.value = deduplicated
            localStore?.saveCustomCategories(deduplicated, _customModifierCategories.value)

            val updatedCatalog = _localSeededCatalog.value.map { rec ->
                if (rec.recordType == "MENU_ITEM" && areCategoriesMatching(rec.category, trimmedOld)) {
                    val updatedRec = rec.copy(category = trimmedNew)
                    syncCatalogItemToCloud(updatedRec)
                    updatedRec
                } else {
                    rec
                }
            }
            _localSeededCatalog.value = updatedCatalog
            localStore?.saveCatalog(updatedCatalog)

            // Migrate subcategories map key
            val currentSubMap = _customSubCategories.value.toMutableMap()
            val oldSubKey = currentSubMap.keys.firstOrNull { areCategoriesMatching(it, trimmedOld) }
            if (oldSubKey != null) {
                val subs = currentSubMap.remove(oldSubKey) ?: emptyList()
                currentSubMap[trimmedNew] = subs
                _customSubCategories.value = currentSubMap
                localStore?.saveCustomSubCategories(currentSubMap)
            }

            val currentMap = _modifierToMenuCategoryMap.value.toMutableMap()
            currentMap.forEach { (modCat, menuCat) ->
                if (areCategoriesMatching(menuCat, trimmedOld)) {
                    currentMap[modCat] = trimmedNew
                }
            }
            _modifierToMenuCategoryMap.value = currentMap
            syncWorkspaceSettingsToCloud()

            logAuditEvent(
                actionType = "CATALOG_CATEGORY_RENAMED",
                entityId = trimmedNew,
                summary = "Mengubah nama Kategori Menu '$trimmedOld' menjadi '$trimmedNew'",
                beforeSnapshot = """{"oldCategory":"$trimmedOld"}""",
                afterSnapshot = """{"newCategory":"$trimmedNew"}"""
            )
            showBanner("Kategori Menu '$trimmedOld' berhasil diubah menjadi '$trimmedNew'.")
        } else {
            val currentList = _customModifierCategories.value.toMutableList()
            val idx = currentList.indexOfFirst { areCategoriesMatching(it, trimmedOld) }
            if (idx >= 0) {
                currentList[idx] = trimmedNew
            } else {
                currentList.add(trimmedNew)
            }
            val deduplicated = currentList.filter { it.isNotBlank() }.distinctBy { normalizeCategoryCanonical(it) }
            _customModifierCategories.value = deduplicated
            localStore?.saveCustomCategories(_customMenuCategories.value, deduplicated)

            val updatedCatalog = _localSeededCatalog.value.map { rec ->
                if (rec.recordType == "MODIFIER" && areCategoriesMatching(rec.category, trimmedOld)) {
                    val updatedRec = rec.copy(category = trimmedNew)
                    syncCatalogItemToCloud(updatedRec)
                    updatedRec
                } else {
                    rec
                }
            }
            _localSeededCatalog.value = updatedCatalog
            localStore?.saveCatalog(updatedCatalog)

            // Migrate subcategories map key
            val currentSubMap = _customSubCategories.value.toMutableMap()
            val oldSubKey = currentSubMap.keys.firstOrNull { areCategoriesMatching(it, trimmedOld) }
            if (oldSubKey != null) {
                val subs = currentSubMap.remove(oldSubKey) ?: emptyList()
                currentSubMap[trimmedNew] = subs
                _customSubCategories.value = currentSubMap
                localStore?.saveCustomSubCategories(currentSubMap)
            }

            val currentMap = _modifierToMenuCategoryMap.value.toMutableMap()
            val existingConnected = currentMap.entries.firstOrNull { areCategoriesMatching(it.key, trimmedOld) }?.value
            currentMap.entries.removeIf { areCategoriesMatching(it.key, trimmedOld) }
            if (!connectedMenuCategory.isNullOrBlank()) {
                currentMap[trimmedNew] = connectedMenuCategory.trim()
            } else if (existingConnected != null) {
                currentMap[trimmedNew] = existingConnected
            }
            _modifierToMenuCategoryMap.value = currentMap
            syncWorkspaceSettingsToCloud()

            logAuditEvent(
                actionType = "CATALOG_CATEGORY_RENAMED",
                entityId = trimmedNew,
                summary = "Mengubah Kategori Modifier '$trimmedOld' menjadi '$trimmedNew'",
                beforeSnapshot = """{"oldCategory":"$trimmedOld"}""",
                afterSnapshot = """{"newCategory":"$trimmedNew"}"""
            )
            showBanner("Kategori Modifier '$trimmedOld' berhasil diubah menjadi '$trimmedNew'.")
        }
    }

    fun deleteCategory(targetType: String, categoryName: String) {
        val trimmed = categoryName.trim()
        if (trimmed.isBlank()) return
        val activeBranch = _session.value.branchId

        if (targetType == "MENU") {
            val updatedMenus = _customMenuCategories.value.filterNot { areCategoriesMatching(it, trimmed) }
            _customMenuCategories.value = updatedMenus
            localStore?.saveCustomCategories(updatedMenus, _customModifierCategories.value)
            val itemsToRemove = _localSeededCatalog.value.filter { rec ->
                rec.recordType == "MENU_ITEM" &&
                    rec.branchId.equals(activeBranch, ignoreCase = true) &&
                    areCategoriesMatching(rec.category, trimmed)
            }
            val removeIds = itemsToRemove.map { it.id }.toSet()
            val updatedCatalog = _localSeededCatalog.value.filterNot { it.id in removeIds }
            _localSeededCatalog.value = updatedCatalog
            localStore?.saveCatalog(updatedCatalog)
            viewModelScope.launch {
                itemsToRemove.forEach { rec ->
                    clearPendingSyncKey("catalog:${rec.id}")
                    if (rec.id.isNotBlank()) {
                        val delKey = "catalog_delete:${rec.id}"
                        markPendingSyncKey(delKey)
                        if (isCloudAuthenticated && isNetworkOnline.value) {
                            repository.deleteCatalogItem(workspaceId, rec.id)
                            clearPendingSyncKey(delKey)
                        }
                    }
                }
            }

            val currentSubMap = _customSubCategories.value.toMutableMap()
            currentSubMap.entries.removeIf { areCategoriesMatching(it.key, trimmed) }
            _customSubCategories.value = currentSubMap
            localStore?.saveCustomSubCategories(currentSubMap)

            val currentMap = _modifierToMenuCategoryMap.value.toMutableMap()
            currentMap.entries.removeIf { areCategoriesMatching(it.value, trimmed) }
            _modifierToMenuCategoryMap.value = currentMap
            syncWorkspaceSettingsToCloud()

            logAuditEvent(
                actionType = "CATALOG_CATEGORY_DELETED",
                entityId = trimmed,
                summary = "Menghapus Kategori Menu '$trimmed'",
                beforeSnapshot = """{"category":"$trimmed"}""",
                afterSnapshot = "{}"
            )
            showBanner("Kategori Menu '$trimmed' berhasil dihapus.")
        } else {
            val updatedMods = _customModifierCategories.value.filterNot { areCategoriesMatching(it, trimmed) }
            _customModifierCategories.value = updatedMods
            localStore?.saveCustomCategories(_customMenuCategories.value, updatedMods)
            val itemsToRemove = _localSeededCatalog.value.filter { rec ->
                rec.recordType == "MODIFIER" &&
                    rec.branchId.equals(activeBranch, ignoreCase = true) &&
                    areCategoriesMatching(rec.category, trimmed)
            }
            val removeIds = itemsToRemove.map { it.id }.toSet()
            val updatedCatalog = _localSeededCatalog.value.filterNot { it.id in removeIds }
            _localSeededCatalog.value = updatedCatalog
            localStore?.saveCatalog(updatedCatalog)
            viewModelScope.launch {
                itemsToRemove.forEach { rec ->
                    clearPendingSyncKey("catalog:${rec.id}")
                    if (rec.id.isNotBlank()) {
                        val delKey = "catalog_delete:${rec.id}"
                        markPendingSyncKey(delKey)
                        if (isCloudAuthenticated && isNetworkOnline.value) {
                            repository.deleteCatalogItem(workspaceId, rec.id)
                            clearPendingSyncKey(delKey)
                        }
                    }
                }
            }

            val currentSubMap = _customSubCategories.value.toMutableMap()
            currentSubMap.entries.removeIf { areCategoriesMatching(it.key, trimmed) }
            _customSubCategories.value = currentSubMap
            localStore?.saveCustomSubCategories(currentSubMap)

            val currentMap = _modifierToMenuCategoryMap.value.toMutableMap()
            currentMap.entries.removeIf { areCategoriesMatching(it.key, trimmed) }
            _modifierToMenuCategoryMap.value = currentMap
            syncWorkspaceSettingsToCloud()

            logAuditEvent(
                actionType = "CATALOG_CATEGORY_DELETED",
                entityId = trimmed,
                summary = "Menghapus Kategori Modifier '$trimmed'",
                beforeSnapshot = """{"category":"$trimmed"}""",
                afterSnapshot = "{}"
            )
            showBanner("Kategori Modifier '$trimmed' berhasil dihapus.")
        }
    }

    fun getSubCategoriesForCategory(category: String): List<String> {
        val trimmedCat = category.trim()
        val fromState = _customSubCategories.value.entries
            .firstOrNull { areCategoriesMatching(it.key, trimmedCat) }?.value ?: emptyList()
        val fromCatalog = _localSeededCatalog.value
            .filter { areCategoriesMatching(it.category, trimmedCat) && it.subCategory.isNotBlank() }
            .map { it.subCategory }
        return (fromState + fromCatalog).filter { it.isNotBlank() }.distinctBy { normalizeCategoryCanonical(it) }
    }

    fun addSubCategory(category: String, subCategoryName: String) {
        val trimmedCat = category.trim()
        val trimmedSub = subCategoryName.trim()
        if (trimmedCat.isBlank() || trimmedSub.isBlank()) return

        val currentMap = _customSubCategories.value.toMutableMap()
        val existingKey = currentMap.keys.firstOrNull { areCategoriesMatching(it, trimmedCat) } ?: trimmedCat
        val existingList = (currentMap[existingKey] ?: emptyList()).toMutableList()
        if (existingList.any { areCategoriesMatching(it, trimmedSub) }) {
            showBanner("Sub-kategori '$trimmedSub' sudah ada pada kategori '$trimmedCat'.")
            return
        }
        existingList.add(trimmedSub)
        currentMap[existingKey] = existingList.distinctBy { normalizeCategoryCanonical(it) }
        _customSubCategories.value = currentMap
        localStore?.saveCustomSubCategories(currentMap)
        syncWorkspaceSettingsToCloud()

        logAuditEvent(
            actionType = "CATALOG_SUBCATEGORY_ADDED",
            entityId = "$trimmedCat/$trimmedSub",
            summary = "Menambahkan sub-kategori baru: '$trimmedSub' pada kategori '$trimmedCat'",
            beforeSnapshot = "{}",
            afterSnapshot = """{"category":"$trimmedCat","subCategory":"$trimmedSub"}"""
        )
        showBanner("Sub-kategori '$trimmedSub' berhasil ditambahkan ke '$trimmedCat'.")
    }

    fun editSubCategory(category: String, oldSubName: String, newSubName: String) {
        val trimmedCat = category.trim()
        val trimmedOld = oldSubName.trim()
        val trimmedNew = newSubName.trim()
        if (trimmedCat.isBlank() || trimmedOld.isBlank() || trimmedNew.isBlank()) return

        val currentMap = _customSubCategories.value.toMutableMap()
        val existingKey = currentMap.keys.firstOrNull { areCategoriesMatching(it, trimmedCat) } ?: trimmedCat
        val existingList = (currentMap[existingKey] ?: emptyList()).toMutableList()
        val idx = existingList.indexOfFirst { areCategoriesMatching(it, trimmedOld) }
        if (idx >= 0) {
            existingList[idx] = trimmedNew
        } else {
            existingList.add(trimmedNew)
        }
        currentMap[existingKey] = existingList.distinctBy { normalizeCategoryCanonical(it) }
        _customSubCategories.value = currentMap
        localStore?.saveCustomSubCategories(currentMap)
        syncWorkspaceSettingsToCloud()

        val updatedCatalog = _localSeededCatalog.value.map { rec ->
            if (areCategoriesMatching(rec.category, trimmedCat) && areCategoriesMatching(rec.subCategory, trimmedOld)) {
                val updatedRec = rec.withSubCategory(trimmedNew)
                syncCatalogItemToCloud(updatedRec)
                updatedRec
            } else {
                rec
            }
        }
        _localSeededCatalog.value = updatedCatalog
        localStore?.saveCatalog(updatedCatalog)

        logAuditEvent(
            actionType = "CATALOG_SUBCATEGORY_UPDATED",
            entityId = "$trimmedCat/$trimmedNew",
            summary = "Mengubah sub-kategori '$trimmedOld' menjadi '$trimmedNew' pada kategori '$trimmedCat'",
            beforeSnapshot = """{"category":"$trimmedCat","oldSubCategory":"$trimmedOld"}""",
            afterSnapshot = """{"category":"$trimmedCat","newSubCategory":"$trimmedNew"}"""
        )
        showBanner("Sub-kategori '$trimmedOld' diubah menjadi '$trimmedNew'.")
    }

    fun deleteSubCategory(category: String, subCategoryName: String) {
        val trimmedCat = category.trim()
        val trimmedSub = subCategoryName.trim()
        if (trimmedCat.isBlank() || trimmedSub.isBlank()) return

        val currentMap = _customSubCategories.value.toMutableMap()
        val existingKey = currentMap.keys.firstOrNull { areCategoriesMatching(it, trimmedCat) } ?: trimmedCat
        val existingList = (currentMap[existingKey] ?: emptyList()).toMutableList()
        val filtered = existingList.filterNot { areCategoriesMatching(it, trimmedSub) }
        currentMap[existingKey] = filtered
        _customSubCategories.value = currentMap
        localStore?.saveCustomSubCategories(currentMap)
        syncWorkspaceSettingsToCloud()

        val updatedCatalog = _localSeededCatalog.value.map { rec ->
            if (areCategoriesMatching(rec.category, trimmedCat) && areCategoriesMatching(rec.subCategory, trimmedSub)) {
                val updatedRec = rec.withSubCategory("")
                syncCatalogItemToCloud(updatedRec)
                updatedRec
            } else {
                rec
            }
        }
        _localSeededCatalog.value = updatedCatalog
        localStore?.saveCatalog(updatedCatalog)

        logAuditEvent(
            actionType = "CATALOG_SUBCATEGORY_DELETED",
            entityId = "$trimmedCat/$trimmedSub",
            summary = "Menghapus sub-kategori '$trimmedSub' dari kategori '$trimmedCat'",
            beforeSnapshot = """{"category":"$trimmedCat","subCategory":"$trimmedSub"}""",
            afterSnapshot = "{}"
        )
        showBanner("Sub-kategori '$trimmedSub' berhasil dihapus dari '$trimmedCat'.")
    }

    fun addCustomMenuItemToCart(
        menuItem: CatalogRecord,
        selectedModifiers: List<String>,
        extraPriceWalkIn: Long = 0L,
        extraPriceGrab: Long = 0L,
        extraPriceGo: Long = 0L,
        extraPriceShopee: Long = 0L,
        qty: Int = 1
    ) {
        if (!_isStoreOpen.value) {
            showBanner("Toko sedang tutup. Buka toko terlebih dahulu untuk melakukan transaksi.")
            return
        }
        val addQty = qty.coerceAtLeast(1)
        val currentList = _cartItems.value.toMutableList()
        val sortedMods = selectedModifiers.sorted()
        val existingIdx = currentList.indexOfFirst {
            it.category == menuItem.category &&
                it.primaryItemId == menuItem.id &&
                it.addOnNames == sortedMods
        }

        if (existingIdx >= 0) {
            val item = currentList[existingIdx]
            currentList[existingIdx] = item.copy(qty = item.qty + addQty)
        } else {
            currentList.add(
                CartLineItem(
                    lineId = "line_${UUID.randomUUID().toString().take(8)}",
                    category = menuItem.category,
                    primaryItemId = menuItem.id,
                    primaryItemName = menuItem.name,
                    addOnNames = sortedMods,
                    qty = addQty,
                    unitPriceWalkIn = menuItem.priceWalkIn + extraPriceWalkIn,
                    unitPriceGrabFood = menuItem.priceGrabFood + extraPriceGrab,
                    unitPriceGoFood = menuItem.priceGoFood + extraPriceGo,
                    unitPriceShopeeFood = menuItem.priceShopeeFood + extraPriceShopee
                )
            )
        }
        _cartItems.value = currentList
        reconcileCartPrepaymentAfterChange()
    }


    fun addBranchAndAccount(branchName: String, username: String, password: String, role: StaffRole, displayName: String) {
        val cleanBranch = branchName.trim().ifEmpty { _session.value.branchId }
        val cleanUser = username.trim()
        if (cleanUser.equals("developer", ignoreCase = true)) {
            showBanner("Username 'developer' bersifat mutlak dan tidak dapat ditambahkan sebagai akun cabang.")
            return
        }
        val newAcc = StaffAccount(cleanBranch, cleanUser, password, role, displayName.trim().ifEmpty { cleanUser })
        _staffAccounts.value = _staffAccounts.value + newAcc
        localStore?.saveStaffAccounts(_staffAccounts.value)
        syncStaffAccountToCloud(newAcc)
        val displayBranch = getBranchDisplayName(cleanBranch)
        logAuditEvent(
            actionType = "ACCOUNT_CREATED",
            entityId = "${cleanBranch}_$cleanUser",
            summary = "Menambahkan akun ${role.label} '$cleanUser' khusus untuk cabang $displayBranch",
            beforeSnapshot = "{}",
            afterSnapshot = """{"branch":"$cleanBranch","username":"$cleanUser","role":"${role.label}"}"""
        )
        showBanner("Akun '$cleanUser' (${role.label}) untuk cabang $displayBranch berhasil ditambahkan.")
    }

    fun updateStaffAccount(
        targetUsername: String,
        targetBranchId: String,
        newDisplayName: String,
        newPassword: String,
        newRole: StaffRole,
        newEmail: String = "",
        newPhone: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val actor = _session.value
        val currentList = _staffAccounts.value.toMutableList()
        val idx = currentList.indexOfFirst {
            it.username.equals(targetUsername, ignoreCase = true) &&
                it.branchId.equals(targetBranchId, ignoreCase = true)
        }

        if (idx < 0) {
            val err = "Akun '$targetUsername' tidak ditemukan di cabang ${getBranchDisplayName(targetBranchId)}."
            onError(err)
            showBanner(err)
            return
        }

        val oldAcc = currentList[idx]
        if (!actor.role.canEditAccountOfRole(oldAcc.role)) {
            val err = "Role ${actor.role.label} tidak diizinkan mengedit akun ${oldAcc.role.label}."
            onError(err)
            showBanner(err)
            return
        }

        if (!actor.role.canEditAccountOfRole(newRole)) {
            val err = "Role ${actor.role.label} tidak memiliki wewenang memberikan hak akses ${newRole.label}."
            onError(err)
            showBanner(err)
            return
        }

        val updatedAcc = oldAcc.copy(
            displayName = newDisplayName.trim().ifEmpty { oldAcc.displayName },
            password = if (newPassword.isNotBlank()) newPassword.trim() else oldAcc.password,
            role = newRole,
            email = newEmail.trim(),
            phone = newPhone.trim()
        )

        currentList[idx] = updatedAcc
        _staffAccounts.value = currentList
        localStore?.saveStaffAccounts(currentList)
        syncStaffAccountToCloud(updatedAcc)

        // If the actor edited their own account, update session name and role
        if (actor.username.equals(targetUsername, ignoreCase = true)) {
            updateAndPersistSession(
                actor.copy(
                    displayName = updatedAcc.displayName,
                    role = updatedAcc.role
                )
            )
        }

        logAuditEvent(
            actionType = "ACCOUNT_EDITED",
            entityId = "${targetBranchId}_$targetUsername",
            summary = "Memperbarui akun '$targetUsername' (${updatedAcc.displayName}, role ${updatedAcc.role.label}) oleh ${actor.username}",
            beforeSnapshot = """{"displayName":"${oldAcc.displayName}","role":"${oldAcc.role.label}","email":"${oldAcc.email}","phone":"${oldAcc.phone}"}""",
            afterSnapshot = """{"displayName":"${updatedAcc.displayName}","role":"${updatedAcc.role.label}","email":"${updatedAcc.email}","phone":"${updatedAcc.phone}"}"""
        )
        showBanner("Data akun '$targetUsername' berhasil diperbarui.")
        onSuccess()
    }

    fun deleteStaffAccount(
        targetUsername: String,
        targetBranchId: String,
        onSuccess: () -> Unit = {}
    ) {
        val actor = _session.value
        val currentList = _staffAccounts.value.toMutableList()
        val idx = currentList.indexOfFirst {
            it.username.equals(targetUsername, ignoreCase = true) &&
                it.branchId.equals(targetBranchId, ignoreCase = true)
        }
        if (idx >= 0) {
            val removed = currentList.removeAt(idx)
            _staffAccounts.value = currentList
            localStore?.saveStaffAccounts(currentList)
            syncStaffAccountDeletionToCloud(targetBranchId, targetUsername)
            logAuditEvent(
                actionType = "ACCOUNT_DELETED",
                entityId = "${targetBranchId}_$targetUsername",
                summary = "Akun staff '$targetUsername' (${removed.displayName}) cabang ${getBranchDisplayName(targetBranchId)} dihapus oleh ${actor.username}",
                beforeSnapshot = """{"username":"$targetUsername","role":"${removed.role.label}"}""",
                afterSnapshot = "{}"
            )
            showBanner("Akun staff '$targetUsername' berhasil dihapus.")
            onSuccess()
        }
    }

    // --- MONEY MANAGER ACTIONS & CALCULATIONS ---

    fun navigateMoneyMonth(deltaMonths: Int) {
        val currentKey = _moneySelectedMonthKey.value
        val parts = currentKey.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 10
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, deltaMonths)
        }
        val nextYear = cal.get(Calendar.YEAR)
        val nextMonth = cal.get(Calendar.MONTH) + 1
        _moneySelectedMonthKey.value = String.format(Locale.US, "%04d-%02d", nextYear, nextMonth)
    }

    fun setMoneySelectedMonthKey(monthKey: String) {
        _moneySelectedMonthKey.value = monthKey
    }

    fun toggleMoneyConsolidateAllBranches() {
        if (_session.value.role != StaffRole.DEVELOPER) return
        val next = !_moneyConsolidateAllBranches.value
        _moneyConsolidateAllBranches.value = next
        showBanner(
            if (next) "Mode Konsolidasi Seluruh Cabang Aktif" else "Mode Isolasi Cabang ${getBranchDisplayName(_session.value.branchId)} Aktif"
        )
    }

    fun getMoneyNoteSuggestions(prefix: String): List<String> {
        val cleanPrefix = prefix.trim()
        if (cleanPrefix.isEmpty()) return emptyList()
        val branch = _session.value.branchId
        val fromTx = _allMoneyTransactions.value
            .filter { it.branchId.equals(branch, ignoreCase = true) && it.note.isNotBlank() }
            .map { it.note.trim() }
        val fromBookmarks = _allMoneyBookmarks.value
            .filter { it.branchId.equals(branch, ignoreCase = true) && it.note.isNotBlank() }
            .map { it.note.trim() }
        return (fromTx + fromBookmarks)
            .distinct()
            .filter { it.startsWith(cleanPrefix, ignoreCase = true) && !it.equals(cleanPrefix, ignoreCase = true) }
            .take(8)
    }

    fun getCustomerNameSuggestions(query: String): List<String> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return emptyList()
        val branch = _session.value.branchId
        val namesFromOrders = _localOrders.value
            .filter { ord ->
                ord.branchId.equals(branch, ignoreCase = true) &&
                    ord.customerData.isNotBlank() &&
                    !ord.orderMedia.equals("Grabfood", ignoreCase = true) &&
                    !ord.orderMedia.equals("Gofood", ignoreCase = true) &&
                    !ord.orderMedia.equals("Shopeefood", ignoreCase = true)
            }
            .map { ord ->
                ord.customerData.replace(Regex("^(GF\\s*-\\s*|F\\s*-\\s*|#\\s*)"), "").trim()
            }
            .filter { it.isNotBlank() && !it.equals("Walk In Guest", ignoreCase = true) && !it.equals("Pelanggan Di tempat", ignoreCase = true) }

        return namesFromOrders
            .distinct()
            .filter { it.contains(cleanQuery, ignoreCase = true) && !it.equals(cleanQuery, ignoreCase = true) }
            .sortedBy { if (it.startsWith(cleanQuery, ignoreCase = true)) 0 else 1 }
            .take(12)
    }

    data class TransferFeeHistoryConfig(
        val feeAmount: Long,
        val feeAccountChoice: String
    )

    fun getHistoricalTransferFeeConfig(fromAccount: String, toAccount: String): TransferFeeHistoryConfig? {
        if (fromAccount.isBlank() || toAccount.isBlank()) return null
        val pastTransfer = _allMoneyTransactions.value.firstOrNull { tx ->
            tx.type == MoneyTransactionType.TRANSFER &&
                tx.accountName.equals(fromAccount, ignoreCase = true) &&
                tx.toAccountName.equals(toAccount, ignoreCase = true) &&
                tx.feeAmount > 0L
        }
        if (pastTransfer != null) {
            val feeExpense = _allMoneyTransactions.value.firstOrNull { tx ->
                tx.type == MoneyTransactionType.EXPENSE &&
                    tx.dateKey == pastTransfer.dateKey &&
                    tx.amount == pastTransfer.feeAmount &&
                    (tx.subCategory.equals("Biaya Administrasi", ignoreCase = true) || tx.note.contains("Biaya Administrasi", ignoreCase = true))
            }
            val choice = if (feeExpense != null && feeExpense.accountName.equals(toAccount, ignoreCase = true)) "TO" else "FROM"
            return TransferFeeHistoryConfig(pastTransfer.feeAmount, choice)
        }

        return null
    }

    fun saveOrUpdateMoneyTransaction(record: MoneyTransactionRecord) {
        val sess = _session.value
        if (!sess.role.canAccessMoneyManager) {
            showBanner("Akses ditolak: Anda tidak memiliki izin akses Money Manager.")
            return
        }
        val existing = _allMoneyTransactions.value.firstOrNull { it.id == record.id }
        if (existing != null && !sess.role.canEditOrDeleteMoneyManager) {
            showBanner("Supervisor hanya diizinkan menginput transaksi baru, tidak dapat mengedit transaksi yang sudah ada.")
            return
        }

        val resolvedBranch = record.branchId.ifBlank { sess.branchId }
        val finalRecord = if (record.id.isBlank()) {
            record.copy(
                id = "v1_mm_tx_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                branchId = resolvedBranch,
                createdBy = sess.username
            )
        } else {
            record.copy(
                branchId = resolvedBranch
            )
        }

        val currentList = _allMoneyTransactions.value.toMutableList()
        val idx = currentList.indexOfFirst { it.id == finalRecord.id }
        if (idx >= 0) {
            currentList[idx] = finalRecord
        } else {
            currentList.add(0, finalRecord)
        }
        _allMoneyTransactions.value = currentList
        localStore?.saveMoneyTransactions(currentList)
        syncMoneyTxToCloud(finalRecord)

        // Ensure view jumps to the month of the saved transaction if different
        val txMonth = finalRecord.dateKey.take(7)
        if (txMonth.length == 7) {
            _moneySelectedMonthKey.value = txMonth
        }

        val actionTitle = if (existing == null) "MONEY_TX_ADDED" else "MONEY_TX_UPDATED"
        logAuditEvent(
            actionType = actionTitle,
            entityId = finalRecord.id,
            summary = "${if (existing == null) "Input" else "Edit"} ${finalRecord.type.label} Money Manager: ${finalRecord.categoryName}/${finalRecord.subCategoryName} (${formatIdr(finalRecord.amount)}) • Note: ${finalRecord.note}",
            beforeSnapshot = existing?.let { """{"amount":${it.amount},"cat":"${it.categoryName}","sub":"${it.subCategoryName}","note":"${it.note}"}""" } ?: "{}",
            afterSnapshot = """{"amount":${finalRecord.amount},"cat":"${finalRecord.categoryName}","sub":"${finalRecord.subCategoryName}","note":"${finalRecord.note}"}"""
        )
        showBanner(
            if (existing == null) "Transaksi ${finalRecord.type.label} (${formatIdr(finalRecord.amount)}) berhasil disimpan."
            else "Perubahan transaksi berhasil disimpan."
        )
    }

    fun deleteMoneyTransaction(transactionId: String) {
        val sess = _session.value
        if (!sess.role.canEditOrDeleteMoneyManager) {
            showBanner("Akses ditolak: Hanya Owner atau Developer yang dapat menghapus transaksi Money Manager.")
            return
        }
        val target = _allMoneyTransactions.value.firstOrNull { it.id == transactionId } ?: return
        _allMoneyTransactions.value = _allMoneyTransactions.value.filterNot { it.id == transactionId }
        localStore?.saveMoneyTransactions(_allMoneyTransactions.value)
        clearPendingSyncKey("money_tx:$transactionId")
        val delKey = "money_tx_delete:$transactionId"
        markPendingSyncKey(delKey)
        if (isCloudAuthenticated && isNetworkOnline.value) {
            viewModelScope.launch {
                repository.deleteMoneyTransaction(workspaceId, transactionId)
                clearPendingSyncKey(delKey)
            }
        }
        logAuditEvent(
            actionType = "MONEY_TX_DELETED",
            entityId = target.id,
            summary = "Menghapus transaksi Money Manager ${target.type.label}: ${target.categoryName}/${target.subCategoryName} (${formatIdr(target.amount)})",
            beforeSnapshot = """{"id":"${target.id}","amount":${target.amount},"note":"${target.note}"}""",
            afterSnapshot = "{}"
        )
        showBanner("Transaksi '${target.note.ifBlank { target.categoryName }}' berhasil dihapus.")
    }

    fun saveMoneyBookmark(bookmark: MoneyBookmarkRecord) {
        val sess = _session.value
        val finalBookmark = if (bookmark.id.isBlank()) {
            bookmark.copy(
                id = "bm_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                branchId = sess.branchId
            )
        } else {
            bookmark.copy(branchId = sess.branchId)
        }
        val list = _allMoneyBookmarks.value.toMutableList()
        val idx = list.indexOfFirst { it.id == finalBookmark.id }
        if (idx >= 0) {
            list[idx] = finalBookmark
        } else {
            list.add(0, finalBookmark)
        }
        _allMoneyBookmarks.value = list
        localStore?.saveMoneyBookmarks(list)
        syncWorkspaceSettingsToCloud()
        showBanner("Bookmark '${finalBookmark.title}' disimpan ke daftar Bookmarks.")
    }

    fun deleteMoneyBookmark(bookmarkId: String) {
        val target = _allMoneyBookmarks.value.firstOrNull { it.id == bookmarkId } ?: return
        _allMoneyBookmarks.value = _allMoneyBookmarks.value.filterNot { it.id == bookmarkId }
        localStore?.saveMoneyBookmarks(_allMoneyBookmarks.value)
        syncWorkspaceSettingsToCloud()
        showBanner("Bookmark '${target.title}' dihapus.")
    }

    /**
     * Computes real-time balance per (accountGroup, accountName) based on initial balances + all transactions.
     */
    fun calculateMoneyAccountBalances(
        transactions: List<MoneyTransactionRecord> = moneyTransactions.value
    ): Map<Pair<String, String>, Long> {
        val map = mutableMapOf<Pair<String, String>, Long>()
        val sess = _session.value
        val consolidate = sess.role == StaffRole.DEVELOPER && _moneyConsolidateAllBranches.value

        WaffleoSeedData.defaultMoneyAccountGroups.forEach { grp ->
            grp.accounts.forEach { acc ->
                map[grp.groupName to acc] = 0L
            }
        }

        WaffleoSeedData.defaultMoneyInitialBalances
            .filter { consolidate || it.branchId.equals(sess.branchId, ignoreCase = true) }
            .forEach { initBal ->
                val key = initBal.accountGroup to initBal.accountName
                map[key] = (map[key] ?: 0L) + initBal.initialBalance
            }

        transactions.forEach { tx ->
            when (tx.type) {
                MoneyTransactionType.INCOME -> {
                    val key = tx.accountGroup to tx.accountName
                    map[key] = (map[key] ?: 0L) + tx.amount
                }
                MoneyTransactionType.EXPENSE -> {
                    val key = tx.accountGroup to tx.accountName
                    map[key] = (map[key] ?: 0L) - tx.amount - tx.feeAmount
                }
                MoneyTransactionType.TRANSFER -> {
                    val fromKey = tx.accountGroup to tx.accountName
                    val toKey = tx.toAccountGroup to tx.toAccountName
                    map[fromKey] = (map[fromKey] ?: 0L) - tx.amount - tx.feeAmount
                    if (tx.toAccountName.isNotBlank()) {
                        map[toKey] = (map[toKey] ?: 0L) + tx.amount
                    }
                }
            }
        }
        return map
    }

    /**
     * Computes the daily sales recapitulation grouped by date and Money Manager Income channel
     * (Cash -> Cash, QRIS -> Bank Mandiri / BCA, Transfer Tunai -> Bank Mandiri / BCA,
     * GrabFood / GoFood / ShopeeFood -> BCA Net).
     */
    fun computeStoreCloseDailyRecap(
        orderList: List<OrderRecord> = orders.value,
        allCatalog: List<CatalogRecord> = catalogRecords.value,
        onlyToday: Boolean = true,
        ignoreAlreadyRecapped: Boolean = false
    ): List<DailyChannelRecapItem> {
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayKey = dateFmt.format(Date())
        val alreadyRecapped = if (ignoreAlreadyRecapped) emptySet() else _recappedOrderIds.value

        val eligibleOrders = orderList.filter { ord ->
            ord.isPaid && ord.status != "CANCELLED" && ord.status != "ERASED" &&
                (ignoreAlreadyRecapped || ord.id !in alreadyRecapped) &&
                (!onlyToday || (!isBackdatedOrder(ord) && dateFmt.format(resolveOrderTimestampDate(ord)) == todayKey))
        }
        if (eligibleOrders.isEmpty()) return emptyList()

        data class PosChannelGroupKey(
            val dateKey: String,
            val subCategoryName: String,
            val accountGroup: String,
            val accountName: String
        )

        class PosChannelAccumulator {
            var grossTotal: Long = 0L
            var feeTotal: Long = 0L
            var netTotal: Long = 0L
            var orderCount: Int = 0
            val itemCounts = mutableMapOf<String, Int>()

            fun addItemCounts(ord: OrderRecord) {
                val parsedItems = CartLineItem.deserializeList(ord.itemsPayload)
                if (parsedItems.isNotEmpty()) {
                    parsedItems.forEach { item ->
                        val label = when {
                            item.category.equals("Egg Waffle", ignoreCase = true) -> "Waffle"
                            item.category.equals("Drinks", ignoreCase = true) -> "Minuman"
                            item.category.equals("Dessert", ignoreCase = true) && item.primaryItemName.isNotBlank() -> item.primaryItemName
                            item.category.isNotBlank() && !item.category.equals("General", ignoreCase = true) -> item.category
                            item.primaryItemName.isNotBlank() -> item.primaryItemName
                            else -> "Item"
                        }
                        itemCounts[label] = (itemCounts[label] ?: 0) + item.qty
                    }
                } else {
                    itemCounts["Waffle"] = (itemCounts["Waffle"] ?: 0) + 1
                }
            }

            fun buildSummaryString(): String {
                val parts = itemCounts.entries.map { (label, count) -> "$count $label" }
                return parts.joinToString(", ")
            }
        }

        val grouped = linkedMapOf<PosChannelGroupKey, PosChannelAccumulator>()

        eligibleOrders.forEach { ord ->
            val ordDate = resolveOrderTimestampDate(ord)
            val dKey = dateFmt.format(ordDate)
            val media = OrderMedia.fromLabel(ord.orderMedia)
            val grossAmt = ord.totalAmount
            val feeAmt = calculateOrderPlatformFee(ord, allCatalog)
            val netAmt = calculateOrderNetAmount(ord, allCatalog)

            // Penanganan Khusus Split Payment: Cash + QRIS
            val isSplitCashQris = ord.paymentMethod.contains("Cash + QRIS", ignoreCase = true) ||
                (ord.splitQrisAmount > 0L && ord.splitCashAmount > 0L)

            if (isSplitCashQris) {
                val isBca = ord.paymentMethod.contains("BCA", ignoreCase = true)
                val targetBank = if (isBca) "BCA" else "Bank Mandiri"
                val qrisPart = if (ord.splitQrisAmount > 0L) ord.splitQrisAmount.coerceAtMost(netAmt) else (netAmt / 2)
                val cashPart = (netAmt - qrisPart).coerceAtLeast(0L)

                if (cashPart > 0L) {
                    val cashKey = PosChannelGroupKey(
                        dateKey = dKey,
                        subCategoryName = "Cash",
                        accountGroup = "Uang Tunai",
                        accountName = "Cash"
                    )
                    val accCash = grouped.getOrPut(cashKey) { PosChannelAccumulator() }
                    accCash.grossTotal += cashPart
                    accCash.netTotal += cashPart
                    accCash.orderCount += 1
                    accCash.addItemCounts(ord)
                }
                if (qrisPart > 0L) {
                    val qrisKey = PosChannelGroupKey(
                        dateKey = dKey,
                        subCategoryName = "QRIS",
                        accountGroup = "Bank",
                        accountName = targetBank
                    )
                    val accQris = grouped.getOrPut(qrisKey) { PosChannelAccumulator() }
                    accQris.grossTotal += qrisPart
                    accQris.netTotal += qrisPart
                    accQris.orderCount += 1
                    accQris.addItemCounts(ord)
                }
                return@forEach
            }

            val channelKey = when {
                media == OrderMedia.GRABFOOD -> PosChannelGroupKey(
                    dateKey = dKey,
                    subCategoryName = "Grabfood",
                    accountGroup = "Bank",
                    accountName = "BCA"
                )
                media == OrderMedia.GOFOOD -> PosChannelGroupKey(
                    dateKey = dKey,
                    subCategoryName = "Gofood",
                    accountGroup = "Bank",
                    accountName = "BCA"
                )
                media == OrderMedia.SHOPEEFOOD -> PosChannelGroupKey(
                    dateKey = dKey,
                    subCategoryName = "Shopeefood",
                    accountGroup = "Bank",
                    accountName = "BCA"
                )
                ord.paymentMethod.startsWith("QRIS Waffleo", ignoreCase = true) ||
                    ord.paymentMethod.contains("QRIS", ignoreCase = true) -> {
                    val isBca = ord.paymentMethod.contains("BCA", ignoreCase = true)
                    val targetBank = if (isBca) "BCA" else "Bank Mandiri"
                    PosChannelGroupKey(
                        dateKey = dKey,
                        subCategoryName = "QRIS",
                        accountGroup = "Bank",
                        accountName = targetBank
                    )
                }
                ord.paymentMethod.startsWith("Transfer Tunai", ignoreCase = true) ||
                    ord.paymentMethod.contains("Transfer", ignoreCase = true) -> {
                    val isBca = ord.paymentMethod.contains("BCA", ignoreCase = true)
                    val targetBank = if (isBca) "BCA" else "Bank Mandiri"
                    PosChannelGroupKey(
                        dateKey = dKey,
                        subCategoryName = "Transfer Tunai",
                        accountGroup = "Bank",
                        accountName = targetBank
                    )
                }
                else -> PosChannelGroupKey(
                    dateKey = dKey,
                    subCategoryName = "Cash",
                    accountGroup = "Uang Tunai",
                    accountName = "Cash"
                )
            }

            val acc = grouped.getOrPut(channelKey) { PosChannelAccumulator() }
            acc.grossTotal += grossAmt
            acc.feeTotal += feeAmt
            acc.netTotal += netAmt
            acc.orderCount += 1
            acc.addItemCounts(ord)
        }

        // Post-processing for Cash (Penjualan Tunai):
        // Combine items & order count from Cash AND QRIS orders for the same dateKey
        val dateKeys = grouped.keys.map { it.dateKey }.distinct()
        val cashSummaryByDate = mutableMapOf<String, Pair<String, Int>>()

        dateKeys.forEach { dKey ->
            val cashAndQrisOrders = eligibleOrders.filter { ord ->
                val oDate = dateFmt.format(resolveOrderTimestampDate(ord))
                if (oDate != dKey) return@filter false
                val isQris = ord.paymentMethod.contains("QRIS", ignoreCase = true)
                val isCash = ord.paymentMethod.contains("Cash", ignoreCase = true) ||
                    ord.paymentMethod.contains("Tunai", ignoreCase = true)
                val isOnline = ord.orderMedia in listOf("Grabfood", "Gofood", "Shopeefood")
                (isQris || isCash) && !isOnline
            }
            if (cashAndQrisOrders.isNotEmpty()) {
                val combinedAcc = PosChannelAccumulator()
                cashAndQrisOrders.forEach {
                    combinedAcc.addItemCounts(it)
                }
                cashSummaryByDate[dKey] = Pair(combinedAcc.buildSummaryString(), cashAndQrisOrders.size)
            }
        }

        return grouped.map { (key, acc) ->
            val isCashChannel = key.subCategoryName.equals("Cash", ignoreCase = true)
            val combinedInfo = if (isCashChannel) cashSummaryByDate[key.dateKey] else null
            val finalSummary = combinedInfo?.first ?: acc.buildSummaryString()
            val finalOrderCount = combinedInfo?.second ?: acc.orderCount

            DailyChannelRecapItem(
                dateKey = key.dateKey,
                subCategory = key.subCategoryName,
                accountGroup = key.accountGroup,
                accountName = key.accountName,
                grossAmount = acc.grossTotal,
                platformDeduction = acc.feeTotal,
                netAmount = acc.netTotal,
                orderCount = finalOrderCount,
                customItemSummary = finalSummary
            )
        }
    }

    fun computeOrdersTotalItemSummary(ordersList: List<OrderRecord>): String {
        val itemCounts = mutableMapOf<String, Int>()
        ordersList.forEach { ord ->
            val parsedItems = CartLineItem.deserializeList(ord.itemsPayload)
            if (parsedItems.isNotEmpty()) {
                parsedItems.forEach { item ->
                    val label = when {
                        item.category.equals("Egg Waffle", ignoreCase = true) -> "Waffle"
                        item.category.equals("Drinks", ignoreCase = true) -> "Minuman"
                        item.category.equals("Dessert", ignoreCase = true) && item.primaryItemName.isNotBlank() -> item.primaryItemName
                        item.category.isNotBlank() && !item.category.equals("General", ignoreCase = true) -> item.category
                        item.primaryItemName.isNotBlank() -> item.primaryItemName
                        else -> "Item"
                    }
                    itemCounts[label] = (itemCounts[label] ?: 0) + item.qty
                }
            } else {
                itemCounts["Waffle"] = (itemCounts["Waffle"] ?: 0) + 1
            }
        }
        val parts = itemCounts.entries.map { (label, count) -> "$count $label" }
        return if (parts.isNotEmpty()) parts.joinToString(", ") else "0 Item"
    }

    fun buildCanonicalPosSyncId(
        branchId: String,
        dateKey: String,
        subCategory: String,
        accountName: String
    ): String {
        val cleanBranch = branchId.lowercase().trim().replace(" ", "_")
        val cleanSub = subCategory.lowercase().trim().replace(" ", "_")
        val cleanAcc = accountName.lowercase().trim().replace(" ", "_")
        return "v1_pos_sync_${cleanBranch}_${dateKey}_${cleanSub}_${cleanAcc}"
    }

    fun isMatchingPosSyncTx(
        tx: MoneyTransactionRecord,
        branchId: String,
        dateKey: String,
        subCategory: String,
        accountName: String
    ): Boolean {
        if (!tx.branchId.equals(branchId, ignoreCase = true)) {
            return false
        }
        if (tx.dateKey != dateKey) return false

        val canonicalId = buildCanonicalPosSyncId(branchId, dateKey, subCategory, accountName)
        if (tx.id == canonicalId) return true

        val isPosSync = tx.isAutoPosSync ||
            tx.id.startsWith("pos_sync_") ||
            tx.id.startsWith("v1_pos_sync_") ||
            tx.tag.contains("Auto-Sync", ignoreCase = true) ||
            tx.tag.contains("POS Sync", ignoreCase = true) ||
            tx.category.equals("Penjualan", ignoreCase = true)

        if (!isPosSync) return false

        val targetSub = subCategory.lowercase().trim()
        val txSub = tx.subCategory.lowercase().trim()
        val txNote = tx.note.lowercase().trim()
        val txAcc = tx.accountName.lowercase().trim()
        val targetAcc = accountName.lowercase().trim()

        val subMatch = when (targetSub) {
            "cash" -> txSub == "cash" || txNote.contains("penjualan tunai") || txNote.contains("cash") || txAcc == "cash"
            "qris" -> txSub == "qris" || txNote.contains("qris")
            "transfer tunai" -> txSub.contains("transfer") || txNote.contains("transfer")
            "grabfood" -> txSub == "grabfood" || txNote.contains("grabfood")
            "gofood" -> txSub == "gofood" || txNote.contains("gofood")
            "shopeefood" -> txSub == "shopeefood" || txNote.contains("shopeefood")
            else -> txSub == targetSub || txNote.contains(targetSub)
        }

        val accMatch = when {
            targetAcc == "cash" -> txAcc == "cash"
            targetAcc.contains("bca") -> txAcc.contains("bca")
            targetAcc.contains("mandiri") -> txAcc.contains("mandiri")
            else -> txAcc == targetAcc
        }

        return subMatch && accMatch
    }

    /**
     * Consolidates duplicate / fragmented daily POS sync transactions across all dates.
     * Enforces the business rule that each channel (Cash, QRIS BCA, Grabfood, etc.)
     * must have EXACTLY ONE transaction per day in Money Manager.
     * When multiple fragmented transactions exist for the same date & channel (e.g. from multiple past shifts),
     * they are consolidated into one canonical transaction reflecting the accurate daily total,
     * and the redundant duplicate entries are permanently cleaned up.
     */
    fun consolidateAllDailyPosTransactions(silent: Boolean = true) {
        val currentTxs = _allMoneyTransactions.value.toMutableList()
        if (currentTxs.isEmpty()) return

        val activeBranch = _session.value.branchId
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val allValidOrders = orders.value.filter { it.isPaid && it.status != "CANCELLED" && it.status != "ERASED" }
        val ordersByDate = allValidOrders.groupBy { dateFmt.format(resolveOrderTimestampDate(it)) }

        val deletedIds = mutableListOf<String>()
        var changesMade = false

        val posSyncDates = currentTxs
            .filter { it.isAutoPosSync || it.id.startsWith("pos_sync_") || it.tag.contains("Auto-Sync") || it.category.equals("Penjualan", ignoreCase = true) }
            .map { it.dateKey }
            .distinct()

        posSyncDates.forEach { dKey ->
            val ordersOnDate = ordersByDate[dKey] ?: emptyList()
            val recapsOnDate = if (ordersOnDate.isNotEmpty()) {
                computeStoreCloseDailyRecap(ordersOnDate, catalogRecords.value, onlyToday = false, ignoreAlreadyRecapped = true)
            } else {
                emptyList()
            }

            val existingPosOnDate = currentTxs.filter { it.dateKey == dKey && (it.isAutoPosSync || it.id.startsWith("pos_sync_") || it.tag.contains("Auto-Sync") || it.category.equals("Penjualan", ignoreCase = true)) }
            val channelsOnDate = mutableSetOf<Pair<String, String>>()
            recapsOnDate.forEach { channelsOnDate.add(it.subCategory to it.accountName) }
            existingPosOnDate.forEach {
                val sub = when {
                    it.subCategory.isNotBlank() -> it.subCategory
                    it.note.contains("Penjualan Tunai", ignoreCase = true) -> "Cash"
                    it.note.contains("QRIS", ignoreCase = true) -> "QRIS"
                    it.note.contains("Transfer", ignoreCase = true) -> "Transfer Tunai"
                    else -> "Cash"
                }
                channelsOnDate.add(sub to it.accountName)
            }

            channelsOnDate.forEach { (subCat, accName) ->
                val matchingIndexes = currentTxs.indices.filter { idx ->
                    val tx = currentTxs[idx]
                    isMatchingPosSyncTx(tx, activeBranch, dKey, subCat, accName)
                }

                if (matchingIndexes.size > 1 || (matchingIndexes.isNotEmpty() && recapsOnDate.isNotEmpty())) {
                    val canonicalId = buildCanonicalPosSyncId(activeBranch, dKey, subCat, accName)
                    val recapItem = recapsOnDate.firstOrNull { it.subCategory.equals(subCat, ignoreCase = true) && it.accountName.equals(accName, ignoreCase = true) }

                    val primaryIdx = matchingIndexes.first()
                    val primaryTx = currentTxs[primaryIdx]

                    val finalAmount = recapItem?.netAmount ?: matchingIndexes.sumOf { currentTxs[it].amount }
                    val finalNote = recapItem?.formattedNote ?: primaryTx.note.ifBlank {
                        if (subCat.equals("Cash", ignoreCase = true)) "Penjualan Tunai" else subCat
                    }
                    val finalDesc = recapItem?.formattedDescription ?: "${matchingIndexes.size} Transaksi Terkonsolidasi"

                    val latestTime = matchingIndexes.map { currentTxs[it].timeFormatted }.maxOrNull() ?: primaryTx.timeFormatted

                    val consolidatedTx = primaryTx.copy(
                        id = canonicalId,
                        branchId = activeBranch,
                        type = MoneyTransactionType.INCOME,
                        dateKey = dKey,
                        monthKey = dKey.take(7),
                        timeFormatted = latestTime,
                        amount = finalAmount,
                        category = "Penjualan",
                        subCategory = subCat,
                        accountGroup = recapItem?.accountGroup ?: primaryTx.accountGroup,
                        accountName = accName,
                        note = finalNote,
                        description = finalDesc,
                        tag = "Auto-Sync Tutup Toko",
                        isAutoPosSync = true,
                        posSyncKey = "$dKey|$subCat|$accName"
                    )

                    currentTxs[primaryIdx] = consolidatedTx
                    syncMoneyTxToCloud(consolidatedTx)
                    changesMade = true

                    for (i in matchingIndexes.size - 1 downTo 1) {
                        val dupIdx = matchingIndexes[i]
                        val dupTx = currentTxs.removeAt(dupIdx)
                        if (dupTx.id != canonicalId) {
                            deletedIds.add(dupTx.id)
                        }
                        changesMade = true
                    }
                }
            }
        }

        if (changesMade) {
            _allMoneyTransactions.value = currentTxs
            localStore?.saveMoneyTransactions(currentTxs)

            if (deletedIds.isNotEmpty()) {
                deletedIds.forEach { delId ->
                    clearPendingSyncKey("money_tx:$delId")
                    if (isCloudAuthenticated) {
                        viewModelScope.launch {
                            repository.deleteMoneyTransaction(workspaceId, delId)
                        }
                    }
                }
            }
            if (!silent) {
                showBanner("Konsolidasi Transaksi Sukses: Transaksi harian dirapikan menjadi 1 per kanal.")
            }
        }
    }

    /**
     * Synchronizes paid POS orders into Money Manager Income entries grouped by date and settlement channel.
     * Enforces the Single Transaction per Channel per Day rule:
     * Exactly 1 transaction per channel (Cash, QRIS BCA, Grabfood, etc.) for each dateKey.
     * When new orders arrive, are cancelled, or when Store Closes / Opens,
     * the existing transaction for that date & channel is updated in-place (bertambah/berkurang).
     * Any old/split duplicate transactions for that date & channel are automatically consolidated.
     */
    fun syncPosSalesToMoneyManager(silent: Boolean = false, triggeredByStoreClose: Boolean = false) {
        val activeBranch = _session.value.branchId

        // When closing the store, finalize any paid ACTIVE non-backdated orders into DONE so the daily shift queue is cleanly settled
        if (triggeredByStoreClose) {
            val activePaidOrders = orders.value.filter {
                it.status == "ACTIVE" && it.isPaid && !isBackdatedOrder(it)
            }
            if (activePaidOrders.isNotEmpty()) {
                viewModelScope.launch {
                    activePaidOrders.forEach { ord ->
                        val finalized = ord.copy(
                            isPrepared = true,
                            status = "DONE",
                            shortfallAmount = 0L,
                            changeDueAmount = 0L
                        )
                        updateLocalOrderItem(finalized)
                        syncOrderToCloud(finalized, isNew = false)
                    }
                }
            }
        }

        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayKey = dateFmt.format(Date())
        syncPosSalesForDateKey(
            targetDateKey = todayKey,
            preferredTimeText = null,
            silent = silent,
            triggeredByStoreClose = triggeredByStoreClose
        )
    }

    /**
     * Synchronizes paid POS orders for a specific `targetDateKey` (either today on Store Close,
     * or a past `dateKey` when a backdated order is completed/updated) into Money Manager.
     */
    fun syncPosSalesForDateKey(
        targetDateKey: String,
        preferredTimeText: String? = null,
        silent: Boolean = true,
        triggeredByStoreClose: Boolean = false
    ) {
        val activeBranch = _session.value.branchId
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeFmt = SimpleDateFormat("HH:mm", Locale.US)

        val dateOrders = orders.value.filter { ord ->
            ord.isPaid && ord.status != "CANCELLED" && ord.status != "ERASED" &&
                (triggeredByStoreClose || ord.status == "DONE") &&
                dateFmt.format(resolveOrderTimestampDate(ord)) == targetDateKey
        }

        val recapItems = computeStoreCloseDailyRecap(
            orderList = dateOrders,
            allCatalog = catalogRecords.value,
            onlyToday = false,
            ignoreAlreadyRecapped = true
        )

        val latestOrderTimeText = dateOrders
            .maxByOrNull { resolveOrderTimestampDate(it).time }
            ?.let { timeFmt.format(resolveOrderTimestampDate(it)) }
        val nowTimeText = preferredTimeText?.takeIf { it.isNotBlank() }
            ?: latestOrderTimeText
            ?: timeFmt.format(Date())

        val currentMoneyList = _allMoneyTransactions.value.toMutableList()
        val deletedIds = mutableListOf<String>()
        var affectedCount = 0

        recapItems.forEach { item ->
            val canonicalId = buildCanonicalPosSyncId(activeBranch, item.dateKey, item.subCategory, item.accountName)

            val matchingIndexes = currentMoneyList.indices.filter { idx ->
                val tx = currentMoneyList[idx]
                isMatchingPosSyncTx(tx, activeBranch, item.dateKey, item.subCategory, item.accountName)
            }

            if (item.netAmount > 0L) {
                if (matchingIndexes.isNotEmpty()) {
                    // In-place update: adjusts existing transaction (bertambah/berkurang)
                    val primaryIdx = matchingIndexes.first()
                    val existingTx = currentMoneyList[primaryIdx]
                    val updatedTx = existingTx.copy(
                        id = canonicalId,
                        branchId = activeBranch,
                        type = MoneyTransactionType.INCOME,
                        dateKey = item.dateKey,
                        monthKey = item.dateKey.take(7),
                        timeFormatted = preferredTimeText?.takeIf { it.isNotBlank() }
                            ?: existingTx.timeFormatted.ifBlank { nowTimeText },
                        amount = item.netAmount,
                        category = "Penjualan",
                        subCategory = item.subCategory,
                        accountGroup = item.accountGroup,
                        accountName = item.accountName,
                        note = item.formattedNote,
                        description = item.formattedDescription,
                        tag = "Auto-Sync Tutup Toko",
                        isAutoPosSync = true,
                        posSyncKey = "${item.dateKey}|${item.subCategory}|${item.accountName}",
                        createdBy = existingTx.createdBy.ifBlank { _session.value.username }
                    )
                    currentMoneyList[primaryIdx] = updatedTx
                    syncMoneyTxToCloud(updatedTx)
                    affectedCount++

                    // Remove any redundant duplicate records for this channel on this date
                    for (i in matchingIndexes.size - 1 downTo 1) {
                        val dupIdx = matchingIndexes[i]
                        val dupTx = currentMoneyList.removeAt(dupIdx)
                        if (dupTx.id != canonicalId) {
                            deletedIds.add(dupTx.id)
                        }
                    }
                } else {
                    // Create single new record
                    val newTx = MoneyTransactionRecord(
                        id = canonicalId,
                        branchId = activeBranch,
                        type = MoneyTransactionType.INCOME,
                        dateKey = item.dateKey,
                        monthKey = item.dateKey.take(7),
                        timeFormatted = nowTimeText,
                        amount = item.netAmount,
                        category = "Penjualan",
                        subCategory = item.subCategory,
                        accountGroup = item.accountGroup,
                        accountName = item.accountName,
                        note = item.formattedNote,
                        description = item.formattedDescription,
                        tag = "Auto-Sync Tutup Toko",
                        isAutoPosSync = true,
                        posSyncKey = "${item.dateKey}|${item.subCategory}|${item.accountName}",
                        createdBy = _session.value.username
                    )
                    currentMoneyList.add(0, newTx)
                    syncMoneyTxToCloud(newTx)
                    affectedCount++
                }
            } else {
                for (i in matchingIndexes.indices.reversed()) {
                    val idx = matchingIndexes[i]
                    val removed = currentMoneyList.removeAt(idx)
                    deletedIds.add(removed.id)
                }
            }
        }

        if (deletedIds.isNotEmpty()) {
            deletedIds.forEach { delId ->
                clearPendingSyncKey("money_tx:$delId")
                if (isCloudAuthenticated) {
                    viewModelScope.launch {
                        repository.deleteMoneyTransaction(workspaceId, delId)
                    }
                }
            }
        }

        // Handle non-cash cancelled orders that were paid: cash was refunded to buyer, so record Transfer From Cash To Bank Account
        if (triggeredByStoreClose) {
            val cancelledPaidOnDate = orders.value.filter { ord ->
                ord.status == "CANCELLED" && ord.isPaid && ord.totalAmount > 0L &&
                    dateFmt.format(resolveOrderTimestampDate(ord)) == targetDateKey
            }
            cancelledPaidOnDate.forEach { cOrd ->
                val targetBank = when {
                    cOrd.paymentMethod.contains("Mandiri", ignoreCase = true) -> "Bank Mandiri"
                    cOrd.paymentMethod.contains("BCA", ignoreCase = true) -> "BCA"
                    else -> "Bank Mandiri"
                }
                val refundTransferId = "v1_refund_transfer_${cOrd.orderNumber}_${targetDateKey}"
                val exists = currentMoneyList.any { it.id == refundTransferId }
                if (!exists) {
                    val refundTx = MoneyTransactionRecord(
                        id = refundTransferId,
                        branchId = activeBranch,
                        type = MoneyTransactionType.TRANSFER,
                        dateKey = targetDateKey,
                        monthKey = targetDateKey.take(7),
                        timeFormatted = nowTimeText,
                        amount = cOrd.totalAmount,
                        transferFee = 0L,
                        category = "Transfer",
                        subCategory = "Transfer Dana",
                        accountGroup = "Kas",
                        accountName = "Cash",
                        toAccountGroup = "Bank",
                        toAccountName = targetBank,
                        note = "Refund",
                        description = cOrd.orderNumber,
                        tag = "Auto-Refund Kasir",
                        isAutoPosSync = true,
                        createdBy = _session.value.username
                    )
                    currentMoneyList.add(0, refundTx)
                    syncMoneyTxToCloud(refundTx)
                }
            }
        }

        // If all orders for a channel on targetDateKey were cancelled/removed, clean up any orphaned POS sync records on targetDateKey
        val activeRecapPairs = recapItems.filter { it.netAmount > 0L }.map { it.subCategory.lowercase() to it.accountName.lowercase() }.toSet()
        for (i in currentMoneyList.indices.reversed()) {
            val tx = currentMoneyList[i]
            if (tx.dateKey == targetDateKey &&
                tx.branchId.equals(activeBranch, ignoreCase = true) &&
                (tx.isAutoPosSync || tx.id.startsWith("v1_pos_sync_")) &&
                tx.type == MoneyTransactionType.INCOME
            ) {
                val pair = tx.subCategory.lowercase() to tx.accountName.lowercase()
                if (pair !in activeRecapPairs) {
                    val removed = currentMoneyList.removeAt(i)
                    deletedIds.add(removed.id)
                }
            }
        }

        if (deletedIds.isNotEmpty()) {
            deletedIds.distinct().forEach { delId ->
                clearPendingSyncKey("money_tx:$delId")
                if (isCloudAuthenticated) {
                    viewModelScope.launch {
                        repository.deleteMoneyTransaction(workspaceId, delId)
                    }
                }
            }
        }

        markOrdersAsRecapped(dateOrders.map { it.id })

        _allMoneyTransactions.value = currentMoneyList
        localStore?.saveMoneyTransactions(currentMoneyList)

        // Consolidate any historical dates
        consolidateAllDailyPosTransactions(silent = true)

        if (triggeredByStoreClose) {
            val latestDate = recapItems.maxOfOrNull { it.dateKey } ?: targetDateKey
            if (latestDate.length >= 7) {
                _moneySelectedMonthKey.value = latestDate.take(7)
            }
        }
        if (!silent) {
            val totalRecapped = recapItems.sumOf { it.netAmount }
            showBanner("Sync Keuangan Berhasil: $affectedCount kanal penjualan (${formatIdr(totalRecapped)}) tersinkronisasi (1 transaksi/kanal).")
        }
    }

    private fun updateLocalOrderItem(updated: OrderRecord) {
        val current = _localOrders.value.toMutableList()
        val idx = current.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            current[idx] = updated
        } else {
            current.add(updated)
        }
        val sorted = current.sortedBy { it.sequenceIndex }
        _localOrders.value = sorted
        localStore?.saveOrders(sorted)
        localStore?.updateHighestRecordedOrderSeq(updated.branchId, updated.sequenceIndex)
    }

    private fun updateLocalSeedCatalogItem(updated: CatalogRecord) {
        val current = _localSeededCatalog.value.toMutableList()
        val idx = current.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            current[idx] = updated
        } else {
            current.add(updated)
        }
        _localSeededCatalog.value = current
        localStore?.saveCatalog(current)
    }

    private fun syncOrderToCloud(order: OrderRecord, isNew: Boolean) {
        if (isBranchDeleted(order.branchId)) return
        // Do not upload brand-new test shadow orders created by developer to Cloud Firestore
        if (isNew && (isDeveloperSession() || order.createdBy.contains("developer", ignoreCase = true))) {
            return
        }
        val key = "order:${order.id}"
        clearPendingSyncKey("order_delete:${order.id}")
        markPendingSyncKey(key)
        if (!isCloudAuthenticated || !isNetworkOnline.value) return
        viewModelScope.launch {
            val normalizedOrder = order.copy(userId = workspaceId)
            val ok = if (isNew) {
                repository.createOrder(workspaceId, normalizedOrder)
            } else {
                repository.updateOrder(workspaceId, normalizedOrder)
            }
            if (ok) clearPendingSyncKey(key)
        }
    }

    private fun syncCatalogItemToCloud(item: CatalogRecord) {
        if ((_isMaintenanceModeOn.value && isDeveloperSession()) || isBranchDeleted(item.branchId)) return
        val key = "catalog:${item.id}"
        clearPendingSyncKey("catalog_delete:${item.id}")
        markPendingSyncKey(key)
        if (!isCloudAuthenticated || !isNetworkOnline.value) return
        viewModelScope.launch {
            val ok = repository.saveCatalogItem(workspaceId, item.copy(userId = workspaceId))
            if (ok) clearPendingSyncKey(key)
        }
    }

    private fun syncMoneyTxToCloud(tx: MoneyTransactionRecord) {
        if ((_isMaintenanceModeOn.value && isDeveloperSession()) ||
            tx.createdBy.contains("developer", ignoreCase = true) ||
            isBranchDeleted(tx.branchId)
        ) {
            return
        }
        val key = "money_tx:${tx.id}"
        clearPendingSyncKey("money_tx_delete:${tx.id}")
        markPendingSyncKey(key)
        if (!isCloudAuthenticated || !isNetworkOnline.value) return
        viewModelScope.launch {
            val ok = repository.saveMoneyTransaction(workspaceId, tx)
            if (ok) clearPendingSyncKey(key)
        }
    }

    private fun syncStaffAccountToCloud(account: StaffAccount) {
        if ((_isMaintenanceModeOn.value && isDeveloperSession()) || isBranchDeleted(account.branchId)) return
        val k = "${account.branchId.lowercase()}_${account.username.lowercase()}"
        val key = "staff:$k"
        clearPendingSyncKey("staff_delete:${account.branchId}|${account.username}")
        markPendingSyncKey(key)
        if (!isCloudAuthenticated || !isNetworkOnline.value) return
        viewModelScope.launch {
            val ok = repository.saveStaffAccount(workspaceId, account)
            if (ok) clearPendingSyncKey(key)
        }
    }

    private fun syncStaffAccountDeletionToCloud(branchId: String, username: String) {
        if (_isMaintenanceModeOn.value && isDeveloperSession()) return
        val k = "${branchId.lowercase()}_${username.lowercase()}"
        clearPendingSyncKey("staff:$k")
        val delKey = "staff_delete:$branchId|$username"
        markPendingSyncKey(delKey)
        if (!isCloudAuthenticated || !isNetworkOnline.value) return
        viewModelScope.launch {
            val ok = repository.deleteStaffAccount(workspaceId, branchId, username)
            if (ok) clearPendingSyncKey(delKey)
        }
    }

    private fun syncBranchProfileToCloud(profile: BranchProfile) {
        if ((_isMaintenanceModeOn.value && isDeveloperSession()) || isBranchDeleted(profile.branchId)) return
        val k = profile.branchId.trim().lowercase()
        val key = "branch_profile:$k"
        markPendingSyncKey(key)
        if (!isCloudAuthenticated || !isNetworkOnline.value) return
        viewModelScope.launch {
            val ok = repository.saveBranchProfile(workspaceId, profile)
            if (ok) clearPendingSyncKey(key)
        }
    }

    private fun syncWorkspaceSettingsToCloud() {
        if (_isMaintenanceModeOn.value && isDeveloperSession()) return
        val key = "workspace_settings:all"
        markPendingSyncKey(key)
        if (!isCloudAuthenticated || !isNetworkOnline.value) return
        viewModelScope.launch {
            val ok = pushWorkspaceSettingsNow()
            if (ok) clearPendingSyncKey(key)
        }
    }

    private suspend fun pushWorkspaceSettingsNow(): Boolean {
        if (!isCloudAuthenticated || !isNetworkOnline.value) return false
        return try {
            val payload = JSONObject().apply {
                val deletedArr = JSONArray()
                _deletedBranchIds.value.forEach { deletedArr.put(it) }
                put("deletedBranchIds", deletedArr)

                val banksArr = JSONArray()
                _bankAccounts.value.forEach { item ->
                    banksArr.put(
                        JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("ownerName", item.ownerName)
                            put("accountNumber", item.accountNumber)
                        }
                    )
                }
                put("bankAccounts", banksArr)

                val walletsArr = JSONArray()
                _digitalWallets.value.forEach { item ->
                    walletsArr.put(
                        JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("ownerName", item.ownerName)
                            put("phoneNumber", item.phoneNumber)
                            put("email", item.email)
                        }
                    )
                }
                put("digitalWallets", walletsArr)

                val bookmarksArr = JSONArray()
                _allMoneyBookmarks.value.filterNot { isBranchDeleted(it.branchId) }.forEach { bm ->
                    bookmarksArr.put(
                        JSONObject().apply {
                            put("id", bm.id)
                            put("branchId", bm.branchId)
                            put("type", bm.type.name)
                            put("title", bm.title)
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
                put("moneyBookmarks", bookmarksArr)

                val menuCatArr = JSONArray()
                _customMenuCategories.value.forEach { menuCatArr.put(it) }
                put("customMenuCategories", menuCatArr)

                val modCatArr = JSONArray()
                _customModifierCategories.value.forEach { modCatArr.put(it) }
                put("customModifierCategories", modCatArr)

                val subCatObj = JSONObject()
                _customSubCategories.value.forEach { (cat, subs) ->
                    val arr = JSONArray()
                    subs.forEach { arr.put(it) }
                    subCatObj.put(cat, arr)
                }
                put("customSubCategories", subCatObj)

                put("transferFeeAccountChoice", _transferFeeAccountChoice.value)

                val recappedArr = JSONArray()
                _recappedOrderIds.value.take(500).forEach { recappedArr.put(it) }
                put("recappedOrderIds", recappedArr)
            }.toString()

            repository.saveWorkspaceSetting(workspaceId, "global_config", payload)
        } catch (e: Exception) {
            Log.w("WaffleoVM", "Failed to push workspace settings: ${e.message}")
            false
        }
    }

    private fun applyCloudWorkspaceSettings(settingsMap: Map<String, String>) {
        if ("workspace_settings:all" in _pendingSyncKeys.value) return
        val raw = settingsMap["global_config"] ?: return
        try {
            val obj = JSONObject(raw)

            obj.optJSONArray("deletedBranchIds")?.let { arr ->
                val cloudDeleted = mutableSetOf<String>()
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i).trim().lowercase()
                    if (s.isNotBlank() && s != "global") cloudDeleted.add(s)
                }
                // Preserve any locally pending branch deletions not yet synced to Cloud
                val pendingLocalDeletes = _pendingSyncKeys.value
                    .filter { it.startsWith("branch_delete:") }
                    .flatMap {
                        val slug = it.removePrefix("branch_delete:").trim().lowercase()
                        listOf(slug, "cabang $slug")
                    }
                    .toSet()
                // Remove any branch that currently has a pending creation in flight
                val pendingCreations = _pendingSyncKeys.value
                    .filter { it.startsWith("branch_profile:") }
                    .flatMap {
                        val b = it.removePrefix("branch_profile:").trim().lowercase()
                        val base = b.removePrefix("cabang ").trim()
                        listOf(b, base, "cabang $base")
                    }
                    .toSet()
                val authoritativeDeleted = (cloudDeleted + pendingLocalDeletes) - pendingCreations
                if (authoritativeDeleted != _deletedBranchIds.value) {
                    _deletedBranchIds.value = authoritativeDeleted
                    localStore?.saveDeletedBranchIds(authoritativeDeleted)
                    purgeDeletedBranchesFromLocalState(authoritativeDeleted)
                }
            }

            obj.optJSONArray("bankAccounts")?.let { arr ->
                val list = mutableListOf<BankAccountDetail>()
                for (i in 0 until arr.length()) {
                    val b = arr.optJSONObject(i) ?: continue
                    list.add(
                        BankAccountDetail(
                            id = b.optString("id"),
                            name = b.optString("name"),
                            ownerName = b.optString("ownerName"),
                            accountNumber = b.optString("accountNumber")
                        )
                    )
                }
                if (list.isNotEmpty() && list != _bankAccounts.value) {
                    _bankAccounts.value = list
                    localStore?.saveBankAccounts(list)
                    syncMoneyAccountGroupsWithBanksAndWallets()
                }
            }

            obj.optJSONArray("digitalWallets")?.let { arr ->
                val list = mutableListOf<DigitalWalletDetail>()
                for (i in 0 until arr.length()) {
                    val w = arr.optJSONObject(i) ?: continue
                    list.add(
                        DigitalWalletDetail(
                            id = w.optString("id"),
                            name = w.optString("name"),
                            ownerName = w.optString("ownerName"),
                            phoneNumber = w.optString("phoneNumber"),
                            email = w.optString("email")
                        )
                    )
                }
                if (list.isNotEmpty() && list != _digitalWallets.value) {
                    _digitalWallets.value = list
                    localStore?.saveDigitalWallets(list)
                    syncMoneyAccountGroupsWithBanksAndWallets()
                }
            }

            obj.optJSONArray("moneyBookmarks")?.let { arr ->
                val list = mutableListOf<MoneyBookmarkRecord>()
                for (i in 0 until arr.length()) {
                    val bm = arr.optJSONObject(i) ?: continue
                    val branchId = bm.optString("branchId", "Global")
                    if (isBranchDeleted(branchId)) continue
                    val type = try {
                        MoneyTransactionType.valueOf(bm.optString("type", MoneyTransactionType.EXPENSE.name))
                    } catch (_: Exception) {
                        MoneyTransactionType.EXPENSE
                    }
                    list.add(
                        MoneyBookmarkRecord(
                            id = bm.optString("id"),
                            branchId = branchId,
                            type = type,
                            title = bm.optString("title"),
                            amount = bm.optLong("amount", 0L),
                            category = bm.optString("category"),
                            subCategory = bm.optString("subCategory"),
                            accountGroup = bm.optString("accountGroup", "Uang Tunai"),
                            accountName = bm.optString("accountName", "Cash"),
                            toAccountGroup = bm.optString("toAccountGroup"),
                            toAccountName = bm.optString("toAccountName"),
                            note = bm.optString("note"),
                            description = bm.optString("description"),
                            tag = bm.optString("tag")
                        )
                    )
                }
                if (list.isNotEmpty() && list != _allMoneyBookmarks.value) {
                    _allMoneyBookmarks.value = list
                    localStore?.saveMoneyBookmarks(list)
                }
            }

            val menuCatArr = obj.optJSONArray("customMenuCategories")
            val modCatArr = obj.optJSONArray("customModifierCategories")
            if (menuCatArr != null || modCatArr != null) {
                val menus = mutableListOf<String>()
                if (menuCatArr != null) {
                    for (i in 0 until menuCatArr.length()) {
                        val s = menuCatArr.optString(i)
                        if (s.isNotBlank()) menus.add(s)
                    }
                }
                val mods = mutableListOf<String>()
                if (modCatArr != null) {
                    for (i in 0 until modCatArr.length()) {
                        val s = modCatArr.optString(i)
                        if (s.isNotBlank()) mods.add(s)
                    }
                }
                if (menus.isNotEmpty()) _customMenuCategories.value = menus.distinctBy { it.lowercase() }
                if (mods.isNotEmpty()) _customModifierCategories.value = mods.map { cleanCategoryTitle(it) }.distinctBy { normalizeCategoryCanonical(it) }
                localStore?.saveCustomCategories(_customMenuCategories.value, _customModifierCategories.value)
            }

            obj.optJSONObject("customSubCategories")?.let { subObj ->
                val map = _customSubCategories.value.toMutableMap()
                val keys = subObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val arr = subObj.optJSONArray(k) ?: continue
                    val list = (map[k] ?: emptyList()).toMutableList()
                    for (i in 0 until arr.length()) {
                        val s = arr.optString(i)
                        if (s.isNotBlank() && list.none { areCategoriesMatching(it, s) }) {
                            list.add(s)
                        }
                    }
                    map[k] = list
                }
                _customSubCategories.value = map
                localStore?.saveCustomSubCategories(map)
            }

            val feeChoice = obj.optString("transferFeeAccountChoice", "")
            if (feeChoice == "TO" || feeChoice == "FROM") {
                _transferFeeAccountChoice.value = feeChoice
                localStore?.saveTransferFeeAccountChoice(feeChoice)
            }

            obj.optJSONArray("recappedOrderIds")?.let { arr ->
                val set = _recappedOrderIds.value.toMutableSet()
                for (i in 0 until arr.length()) {
                    val id = arr.optString(i)
                    if (id.isNotBlank()) set.add(id)
                }
                if (set != _recappedOrderIds.value) {
                    _recappedOrderIds.value = set
                    localStore?.saveRecappedOrderIds(set)
                }
            }
        } catch (e: Exception) {
            Log.w("WaffleoVM", "Failed to parse cloud workspace settings: ${e.message}")
        }
    }

    private fun logAuditEvent(
        actionType: String,
        entityId: String,
        summary: String,
        beforeSnapshot: String,
        afterSnapshot: String,
        customTimestampMillis: Long? = null
    ) {
        viewModelScope.launch {
            val active = _session.value
            val tsMs = customTimestampMillis ?: System.currentTimeMillis()
            val cleanBranch = active.branchId.trim().ifBlank { "Global" }.take(80)
            val cleanUser = active.username.trim().ifBlank { "system" }.take(80)
            val cleanRole = active.role.label.trim().ifBlank { "Kasir" }.take(60)
            val cleanAction = actionType.trim().ifBlank { "SYSTEM_EVENT" }.take(80)
            val cleanEntity = entityId.trim().ifBlank { "system" }.take(128)
            val cleanSummary = summary.trim().ifBlank { "Aktivitas sistem tercatat." }.take(2000)
            val cleanBefore = beforeSnapshot.ifBlank { "{}" }.take(10000)
            val cleanAfter = afterSnapshot.ifBlank { "{}" }.take(10000)

            val entry = AuditLogRecord(
                id = "v1_log_${tsMs}_${UUID.randomUUID().toString().take(4)}",
                userId = workspaceId,
                branchId = cleanBranch,
                actorUsername = cleanUser,
                actorRole = cleanRole,
                actionType = cleanAction,
                entityId = cleanEntity,
                summary = cleanSummary,
                beforeSnapshot = cleanBefore,
                afterSnapshot = cleanAfter,
                createdAt = com.google.firebase.Timestamp(Date(tsMs))
            )
            val updatedLogs = (listOf(entry) + _localAuditLogs.value)
                .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            _localAuditLogs.value = updatedLogs
            localStore?.saveAuditLogs(updatedLogs)
            val key = "audit:${entry.id}"
            markPendingSyncKey(key)
            if (isCloudAuthenticated && isNetworkOnline.value) {
                val ok = repository.appendAuditLog(workspaceId, entry)
                if (ok) clearPendingSyncKey(key)
            }
        }
    }

    companion object {
        const val LOCAL_PREVIEW_USER_ID = WaffleoRepository.SHARED_WORKSPACE_ID
        fun formatIdr(amount: Long): String {
            val format = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            return "Rp ${format.format(amount)}"
        }

        /**
         * Resolves the true order timestamp Date reliably:
         * 1. Prefers `order.createdAt` so later cloud syncs do not shift an order's historical date/hour.
         * 2. If `createdAt` is null, extracts epoch millis from `order.id` if formatted as `"v1_ord_bd_<millis>_..."`, `"v1_ord_<millis>_..."`, or `"ord_<millis>_..."`.
         * 3. Otherwise checks the earliest `ORDER_CREATED` timeline event.
         * 4. Falls back to `order.updatedAt`, or `Date()` if all are absent.
         */
        fun resolveOrderTimestampDate(order: OrderRecord): Date {
            order.createdAt?.toDate()?.let { return it }
            val strippedId = order.id
                .removePrefix("v1_ord_bd_")
                .removePrefix("v1_ord_")
                .removePrefix("ord_")
            val firstSegment = strippedId.substringBefore("_")
            val ms = firstSegment.toLongOrNull()
            if (ms != null && ms > 1_500_000_000_000L) {
                return Date(ms)
            }
            val timelineCreated = order.getTimelineEvents()
                .firstOrNull { it.eventType == "ORDER_CREATED" && it.timestampMillis > 1_500_000_000_000L }
            if (timelineCreated != null) {
                return Date(timelineCreated.timestampMillis)
            }
            order.updatedAt?.toDate()?.let { return it }
            return Date()
        }

        /**
         * Computes the inclusive `[startMillis, endMillis]` calendar bounds (`00:00:00.000` to `23:59:59.999`)
         * for any `HistoryFilterPeriod`, including Custom Date Range and Custom N Days Back.
         */
        fun resolvePeriodBoundsMillis(
            period: HistoryFilterPeriod,
            customDaysBack: Int = 7,
            isCustomRangeActive: Boolean = false,
            startDateMillis: Long? = null,
            endDateMillis: Long? = null,
            nowMillis: Long = System.currentTimeMillis()
        ): Pair<Long, Long> {
            if (period == HistoryFilterPeriod.CUSTOM && isCustomRangeActive && startDateMillis != null && endDateMillis != null) {
                val rawMin = minOf(startDateMillis, endDateMillis)
                val rawMax = maxOf(startDateMillis, endDateMillis)
                val calStart = Calendar.getInstance().apply {
                    timeInMillis = rawMin
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calEnd = Calendar.getInstance().apply {
                    timeInMillis = rawMax
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                return calStart.timeInMillis to calEnd.timeInMillis
            }

            val calEnd = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val calStart = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            when (period) {
                HistoryFilterPeriod.TODAY -> {
                    // Today 00:00:00.000 .. 23:59:59.999
                }
                HistoryFilterPeriod.YESTERDAY -> {
                    calStart.add(Calendar.DAY_OF_YEAR, -1)
                    calEnd.add(Calendar.DAY_OF_YEAR, -1)
                }
                HistoryFilterPeriod.THIS_WEEK -> {
                    calStart.add(Calendar.DAY_OF_YEAR, -6)
                }
                HistoryFilterPeriod.THIS_MONTH -> {
                    calStart.set(Calendar.DAY_OF_MONTH, 1)
                    calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
                }
                HistoryFilterPeriod.THIS_YEAR -> {
                    calStart.set(Calendar.MONTH, Calendar.JANUARY)
                    calStart.set(Calendar.DAY_OF_MONTH, 1)
                    calEnd.set(Calendar.MONTH, Calendar.DECEMBER)
                    calEnd.set(Calendar.DAY_OF_MONTH, 31)
                }
                HistoryFilterPeriod.CUSTOM -> {
                    val daysOffset = (customDaysBack.coerceAtLeast(1) - 1)
                    calStart.add(Calendar.DAY_OF_YEAR, -daysOffset)
                }
            }
            return calStart.timeInMillis to calEnd.timeInMillis
        }

        fun matchesPeriodFilterRange(
            timestamp: com.google.firebase.Timestamp?,
            period: HistoryFilterPeriod,
            customDaysBack: Int = 7,
            isCustomRangeActive: Boolean = false,
            startDateMillis: Long? = null,
            endDateMillis: Long? = null
        ): Boolean {
            val date = timestamp?.toDate() ?: Date()
            val (startMs, endMs) = resolvePeriodBoundsMillis(
                period = period,
                customDaysBack = customDaysBack,
                isCustomRangeActive = isCustomRangeActive,
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis
            )
            return date.time in startMs..endMs
        }

        fun matchesPeriodFilter(
            timestamp: com.google.firebase.Timestamp?,
            period: HistoryFilterPeriod,
            customDaysBack: Int = 7
        ): Boolean {
            val date = timestamp?.toDate() ?: Date()
            val (startMs, endMs) = resolvePeriodBoundsMillis(
                period = period,
                customDaysBack = customDaysBack,
                isCustomRangeActive = false
            )
            return date.time in startMs..endMs
        }

        fun normalizeBranchKey(branch: String?): String {
            if (branch.isNullOrBlank()) return "global"
            return branch.trim().lowercase().removePrefix("cabang ").replace("_", " ").trim()
        }

        fun isSameBranch(b1: String?, b2: String?): Boolean {
            return normalizeBranchKey(b1) == normalizeBranchKey(b2)
        }
    }
}
