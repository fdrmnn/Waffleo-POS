package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONArray
import org.json.JSONObject

@IgnoreExtraProperties
data class OrderRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val orderNumber: String = "",
    val sequenceIndex: Long = 0L,
    val branchId: String = "Global",
    val orderMedia: String = "Di tempat",
    val customerData: String = "",
    val itemsSummary: String = "",
    val itemsPayload: String = "[]",
    val notes: String = "",
    val promoCode: String = "",
    val subtotalAmount: Long = 0L,
    val discountAmount: Long = 0L,
    val totalAmount: Long = 0L,
    val paidAmount: Long = 0L,
    val cashReceivedAmount: Long = 0L,
    val paymentMethod: String = "Belum Bayar",
    @get:PropertyName("isPaid") @field:PropertyName("isPaid") val isPaid: Boolean = false,
    @get:PropertyName("isPrepared") @field:PropertyName("isPrepared") val isPrepared: Boolean = false,
    val status: String = "ACTIVE", // ACTIVE, DONE, CANCELLED
    val cancelReason: String = "",
    val cancelledBy: String = "",
    val cancelledAtText: String = "",
    val shortfallAmount: Long = 0L,
    val changeDueAmount: Long = 0L,
    val splitCashAmount: Long = 0L,
    val splitQrisAmount: Long = 0L,
    val createdBy: String = "owner",
    val paymentLedgerPayload: String = "[]",
    val orderTimelinePayload: String = "[]",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun getPaymentLedger(): List<OrderPaymentLedgerEntry> = OrderPaymentLedgerEntry.deserializeList(paymentLedgerPayload)
    fun getTimelineEvents(): List<OrderTimelineEvent> = OrderTimelineEvent.deserializeList(orderTimelinePayload)

    @get:com.google.firebase.firestore.Exclude
    val displayOrderNumber: String
        get() {
            if (orderNumber.isBlank()) return "WFL-000"
            val parts = orderNumber.split("-")
            return if (parts.size >= 4) {
                val lastPart = parts.lastOrNull() ?: String.format(java.util.Locale.US, "%03d", sequenceIndex)
                "WFL-$lastPart"
            } else if (parts.size == 2 && parts[0] == "WFL") {
                orderNumber
            } else {
                "WFL-${String.format(java.util.Locale.US, "%03d", sequenceIndex.coerceAtLeast(1L))}"
            }
        }
}

data class OrderPaymentLedgerEntry(
    val paymentId: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val timeText: String = "",
    val paymentType: String = "INITIAL_PAYMENT", // INITIAL_PAYMENT, SHORTFALL_SETTLEMENT, CHANGE_REFUND_RETURN
    val paymentMethod: String = "",
    val amount: Long = 0L,
    val cashierUsername: String = "owner",
    val note: String = ""
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("paymentId", paymentId)
        put("timestampMillis", timestampMillis)
        put("timeText", timeText)
        put("paymentType", paymentType)
        put("paymentMethod", paymentMethod)
        put("amount", amount)
        put("cashierUsername", cashierUsername)
        put("note", note)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): OrderPaymentLedgerEntry = OrderPaymentLedgerEntry(
            paymentId = obj.optString("paymentId", ""),
            timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
            timeText = obj.optString("timeText", ""),
            paymentType = obj.optString("paymentType", "INITIAL_PAYMENT"),
            paymentMethod = obj.optString("paymentMethod", ""),
            amount = obj.optLong("amount", 0L),
            cashierUsername = obj.optString("cashierUsername", "owner"),
            note = obj.optString("note", "")
        )

        fun serializeList(list: List<OrderPaymentLedgerEntry>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJsonObject()) }
            return arr.toString()
        }

        fun deserializeList(json: String): List<OrderPaymentLedgerEntry> {
            if (json.isBlank()) return emptyList()
            return try {
                val arr = JSONArray(json)
                val res = mutableListOf<OrderPaymentLedgerEntry>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    res.add(fromJsonObject(obj))
                }
                res
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

data class OrderTimelineEvent(
    val eventId: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val timeText: String = "",
    val eventType: String = "ORDER_CREATED", // ORDER_CREATED, PAYMENT_EVENT, ORDER_EDITED, ORDER_PREPARED, ORDER_COMPLETED, ORDER_CANCELLED
    val actorUsername: String = "owner",
    val actorRole: String = "Owner",
    val title: String = "",
    val description: String = ""
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("eventId", eventId)
        put("timestampMillis", timestampMillis)
        put("timeText", timeText)
        put("eventType", eventType)
        put("actorUsername", actorUsername)
        put("actorRole", actorRole)
        put("title", title)
        put("description", description)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): OrderTimelineEvent = OrderTimelineEvent(
            eventId = obj.optString("eventId", ""),
            timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
            timeText = obj.optString("timeText", ""),
            eventType = obj.optString("eventType", "ORDER_CREATED"),
            actorUsername = obj.optString("actorUsername", "owner"),
            actorRole = obj.optString("actorRole", "Owner"),
            title = obj.optString("title", ""),
            description = obj.optString("description", "")
        )

        fun serializeList(list: List<OrderTimelineEvent>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJsonObject()) }
            return arr.toString()
        }

        fun deserializeList(json: String): List<OrderTimelineEvent> {
            if (json.isBlank()) return emptyList()
            return try {
                val arr = JSONArray(json)
                val res = mutableListOf<OrderTimelineEvent>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    res.add(fromJsonObject(obj))
                }
                res
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

@IgnoreExtraProperties
data class CatalogRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val recordType: String = "MENU_ITEM", // MENU_ITEM, MODIFIER, INGREDIENT, PROMO, BRANCH_CONFIG
    val branchId: String = "Global",
    val name: String = "",
    val category: String = "", // Egg Waffle, Drinks, FLAVOUR, ADD_ON, SWEETNESS, ICE, RAW_MATERIAL, PROMO, BRANCH
    val priceWalkIn: Long = 0L,
    val priceGrabFood: Long = 0L,
    val priceGoFood: Long = 0L,
    val priceShopeeFood: Long = 0L,
    val stockQty: Double = 0.0,
    val stockUnit: String = "gram",
    val costPerUnit: Long = 0L,
    val metadataJson: String = "{}",
    val imageUrl: String = "",
    @get:PropertyName("isActive") @PropertyName("isActive") val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun priceForMedia(media: OrderMedia): Long = when (media) {
        OrderMedia.WALK_IN, OrderMedia.WHATSAPP, OrderMedia.INSTAGRAM -> priceWalkIn
        OrderMedia.GRABFOOD -> priceGrabFood
        OrderMedia.GOFOOD -> priceGoFood
        OrderMedia.SHOPEEFOOD -> priceShopeeFood
    }

    fun getPlatformPricesMap(): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        if (priceGrabFood > 0L) map["GrabFood"] = priceGrabFood
        if (priceGoFood > 0L) map["GoFood"] = priceGoFood
        if (priceShopeeFood > 0L) map["ShopeeFood"] = priceShopeeFood

        if (metadataJson.isNotBlank()) {
            try {
                val json = JSONObject(metadataJson)
                val pricesObj = json.optJSONObject("customPlatformPrices") ?: json.optJSONObject("platformPrices")
                if (pricesObj != null) {
                    val keys = pricesObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = pricesObj.optLong(k, -1L)
                        if (v >= 0L) {
                            map[k] = v
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
        return map
    }

    fun priceForPlatformName(platformName: String): Long {
        val cleanName = platformName.trim()
        if (cleanName.isBlank()) return priceWalkIn
        val map = getPlatformPricesMap()
        if (map.containsKey(cleanName)) return map[cleanName] ?: 0L
        val entry = map.entries.firstOrNull { it.key.equals(cleanName, ignoreCase = true) }
        if (entry != null) return entry.value

        if (cleanName.equals("GrabFood", ignoreCase = true) || cleanName.equals("Grab", ignoreCase = true)) return priceGrabFood
        if (cleanName.equals("GoFood", ignoreCase = true) || cleanName.equals("Go", ignoreCase = true)) return priceGoFood
        if (cleanName.equals("ShopeeFood", ignoreCase = true) || cleanName.equals("Shopee", ignoreCase = true)) return priceShopeeFood

        return 0L
    }

    fun withPlatformPrices(
        platformPrices: Map<String, Long>,
        allowedModifiersMap: Map<String, List<String>> = emptyMap()
    ): String {
        return try {
            val json = if (metadataJson.isNotBlank()) JSONObject(metadataJson) else JSONObject()
            val pricesObj = JSONObject()
            platformPrices.forEach { (name, price) ->
                pricesObj.put(name, price)
            }
            json.put("customPlatformPrices", pricesObj)
            if (allowedModifiersMap.isNotEmpty()) {
                val modObj = JSONObject()
                allowedModifiersMap.forEach { (cat, opts) ->
                    val arr = JSONArray()
                    opts.forEach { arr.put(it) }
                    modObj.put(cat, arr)
                }
                json.put("allowedModifiers", modObj)
            }
            json.toString()
        } catch (e: Throwable) {
            metadataJson
        }
    }

    fun getAllowedModifiers(): Map<String, List<String>> {
        if (metadataJson.isBlank() || !metadataJson.contains("allowedModifiers")) return emptyMap()
        return try {
            val json = JSONObject(metadataJson)
            val modObj = json.optJSONObject("allowedModifiers")
            if (modObj != null) {
                val map = mutableMapOf<String, List<String>>()
                val keys = modObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val arr = modObj.optJSONArray(key) ?: JSONArray()
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.optString(i))
                    }
                    map[key] = list
                }
                if (map.isNotEmpty()) return map
            }
            fallbackParseAllowedModifiers(metadataJson)
        } catch (e: Throwable) {
            fallbackParseAllowedModifiers(metadataJson)
        }
    }

    private fun fallbackParseAllowedModifiers(raw: String): Map<String, List<String>> {
        val map = mutableMapOf<String, List<String>>()
        val modIdx = raw.indexOf("\"allowedModifiers\"")
        if (modIdx == -1) return emptyMap()
        val startBrace = raw.indexOf('{', modIdx)
        if (startBrace == -1) return emptyMap()

        val categoryRegex = Regex("\"([^\"]+)\"\\s*:\\s*\\[([^\\]]*)\\]")
        for (match in categoryRegex.findAll(raw.substring(startBrace))) {
            val catName = match.groupValues[1]
            if (catName == "allowedModifiers") continue
            val itemsRaw = match.groupValues[2]
            val items = Regex("\"([^\"]+)\"").findAll(itemsRaw).map { it.groupValues[1] }.toList()
            map[catName] = items
        }
        return map
    }

    fun withAllowedModifiers(modifiers: Map<String, List<String>>): String {
        return try {
            val json = if (metadataJson.isNotBlank()) JSONObject(metadataJson) else JSONObject()
            val modObj = JSONObject()
            modifiers.forEach { (cat, items) ->
                modObj.put(cat, JSONArray(items))
            }
            json.put("allowedModifiers", modObj)
            val res = json.toString()
            if (res.isNotBlank() && res != "{}" && res.contains("allowedModifiers")) res else fallbackBuildAllowedModifiers(modifiers)
        } catch (e: Throwable) {
            fallbackBuildAllowedModifiers(modifiers)
        }
    }

    private fun fallbackBuildAllowedModifiers(modifiers: Map<String, List<String>>): String {
        val pairs = modifiers.entries.joinToString(",") { (cat, items) ->
            val itemsStr = items.joinToString(",") { "\"$it\"" }
            "\"$cat\":[$itemsStr]"
        }
        return """{"allowedModifiers":{$pairs}}"""
    }

    val subCategory: String
        get() {
            if (metadataJson.isBlank()) return ""
            val fromJson = try {
                JSONObject(metadataJson).optString("subCategory", "")
            } catch (_: Throwable) {
                ""
            }
            if (fromJson.isNotBlank()) return fromJson
            val regex = Regex("\"subCategory\"\\s*:\\s*\"([^\"]*)\"")
            return regex.find(metadataJson)?.groupValues?.getOrNull(1).orEmpty()
        }

    fun withSubCategory(subCat: String): CatalogRecord {
        val clean = subCat.trim()
        val updatedMeta = try {
            val json = if (metadataJson.isNotBlank()) JSONObject(metadataJson) else JSONObject()
            if (clean.isNotBlank()) {
                json.put("subCategory", clean)
            } else {
                json.remove("subCategory")
            }
            val res = json.toString()
            if (res.isNotBlank() && (clean.isBlank() || res.contains("\"subCategory\""))) res else fallbackWithSubCategory(metadataJson, clean)
        } catch (_: Throwable) {
            fallbackWithSubCategory(metadataJson, clean)
        }
        return copy(metadataJson = updatedMeta)
    }

    private fun fallbackWithSubCategory(raw: String, clean: String): String {
        val regex = Regex("\"subCategory\"\\s*:\\s*\"[^\"]*\"")
        val without = if (raw.contains(regex)) {
            raw.replace(regex, "").replace(",,", ",").replace("{,", "{").replace(",}", "}").trim()
        } else raw.trim()

        if (clean.isBlank()) return if (without.isBlank() || without == "{}") "{}" else without

        return if (without.isBlank() || without == "{}") {
            """{"subCategory":"$clean"}"""
        } else if (without.endsWith("}")) {
            val inner = without.substring(1, without.length - 1).trim()
            if (inner.isBlank()) """{"subCategory":"$clean"}""" else """{"subCategory":"$clean",$inner}"""
        } else {
            """{"subCategory":"$clean"}"""
        }
    }

    val sortOrder: Int
        get() {
            if (metadataJson.isBlank()) return 999999
            val fromJson = try {
                val obj = JSONObject(metadataJson)
                if (obj.has("sortOrder")) obj.optInt("sortOrder", 999999) else -1
            } catch (_: Throwable) {
                -1
            }
            if (fromJson != -1) return fromJson
            val regex = Regex("\"sortOrder\"\\s*:\\s*(-?\\d+)")
            return regex.find(metadataJson)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 999999
        }

    fun withSortOrder(order: Int): CatalogRecord {
        val updatedMeta = try {
            val json = if (metadataJson.isNotBlank()) JSONObject(metadataJson) else JSONObject()
            json.put("sortOrder", order)
            val res = json.toString()
            if (res.isNotBlank() && res.contains("\"sortOrder\"")) res else fallbackWithSortOrder(metadataJson, order)
        } catch (_: Throwable) {
            fallbackWithSortOrder(metadataJson, order)
        }
        return copy(metadataJson = updatedMeta)
    }

    private fun fallbackWithSortOrder(raw: String, order: Int): String {
        val regex = Regex("\"sortOrder\"\\s*:\\s*-?\\d+")
        val without = if (raw.contains(regex)) {
            raw.replace(regex, "").replace(",,", ",").replace("{,", "{").replace(",}", "}").trim()
        } else raw.trim()

        return if (without.isBlank() || without == "{}") {
            """{"sortOrder":$order}"""
        } else if (without.endsWith("}")) {
            val inner = without.substring(1, without.length - 1).trim()
            if (inner.isBlank()) """{"sortOrder":$order}""" else """{"sortOrder":$order,$inner}"""
        } else {
            """{"sortOrder":$order}"""
        }
    }
}

@IgnoreExtraProperties
data class AuditLogRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val branchId: String = "Global",
    val actorUsername: String = "owner",
    val actorRole: String = "Owner",
    val actionType: String = "",
    val entityId: String = "",
    val summary: String = "",
    val beforeSnapshot: String = "{}",
    val afterSnapshot: String = "{}",
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * Representasi agregasi audit log (Main Action) yang merangkum rangkaian sub-aksi
 * pada suatu entitas (misal siklus pesanan, edit berulang menu, perubahan akun).
 */
data class GroupedAuditLog(
    val groupKey: String,
    val entityId: String,
    val category: String, // "ORDER", "CATALOG", "STOCK", "STAFF", "SYSTEM"
    val mainActionType: String,
    val title: String,
    val highLevelSummary: String,
    val latestActor: String,
    val latestActorRole: String,
    val branchId: String,
    val latestTimestamp: Timestamp?,
    val subLogsChronological: List<AuditLogRecord>
) {
    val totalSubActions: Int get() = subLogsChronological.size
}

fun groupAuditLogRecords(rawLogs: List<AuditLogRecord>): List<GroupedAuditLog> {
    if (rawLogs.isEmpty()) return emptyList()

    // Urutkan secara kronologis ascending (awal ke akhir) untuk membentuk riwayat timeline
    val sortedAsc = rawLogs.sortedBy { it.createdAt?.seconds ?: 0L }

    val groupsMap = LinkedHashMap<String, MutableList<AuditLogRecord>>()

    for (log in sortedAsc) {
        val groupKey = when {
            // Pengelompokan seluruh siklus pada satu Pesanan
            (log.actionType.contains("ORDER") || log.entityId.startsWith("WFL-")) && log.entityId.isNotBlank() ->
                "ORDER_${log.branchId}_${log.entityId}"

            // Pengelompokan aktivitas Money Manager
            log.actionType.contains("MONEY") ->
                "MONEY_${log.branchId}_${log.entityId.ifBlank { log.id }}"

            // Pengelompokan perubahan katalog / menu
            (log.actionType.contains("CATALOG") || log.actionType.contains("SEED")) && log.entityId.isNotBlank() ->
                "CATALOG_${log.branchId}_${log.entityId}"

            // Pengelompokan perubahan stok bahan baku
            log.actionType.contains("STOCK") && log.entityId.isNotBlank() ->
                "STOCK_${log.branchId}_${log.entityId}"

            // Pengelompokan operasional status toko (Dipisah per event/sesi aktivitas agar tertrack berurutan dengan jelas)
            log.actionType.contains("STORE") ->
                "STORE_${log.branchId}_${log.id.ifBlank { log.createdAt?.seconds?.toString() ?: log.hashCode().toString() }}"

            // Pengelompokan manajemen akun & profil staff
            log.actionType.contains("ACCOUNT") || log.actionType.contains("STAFF") ->
                "STAFF_${log.branchId}_${log.entityId.ifBlank { log.actorUsername }}"

            // Aksi mandiri lainnya
            else ->
                "SINGLE_${log.id.ifBlank { log.hashCode().toString() }}"
        }
        groupsMap.getOrPut(groupKey) { mutableListOf() }.add(log)
    }

    return groupsMap.map { (key, logs) ->
        val latestLog = logs.last()
        val firstLog = logs.first()
        val entityId = latestLog.entityId.ifBlank { firstLog.entityId }
        val branchId = latestLog.branchId

        val category = when {
            key.startsWith("ORDER") -> "ORDER"
            key.startsWith("MONEY") -> "MONEY"
            key.startsWith("CATALOG") -> "CATALOG"
            key.startsWith("STOCK") -> "STOCK"
            key.startsWith("STORE") || key.startsWith("STAFF") -> "STAFF"
            else -> "SYSTEM"
        }

        val mainActionType = when {
            key.startsWith("ORDER") -> {
                when {
                    logs.any { it.actionType.contains("ERASE") } -> "ORDER_ERASED"
                    logs.any { it.actionType.contains("CANCEL") } -> "ORDER_CANCELLED"
                    logs.any { it.actionType.contains("DONE") } -> "ORDER_DONE"
                    logs.any { it.actionType.contains("PAID") } -> "ORDER_PAID"
                    logs.any { it.actionType.contains("PREPARED") } -> "ORDER_PREPARED"
                    logs.any { it.actionType.contains("EDITED") } -> "ORDER_EDITED"
                    else -> "ORDER_CREATED"
                }
            }
            key.startsWith("MONEY") -> {
                when {
                    logs.any { it.actionType.contains("DELETED") } -> "MONEY_TX_DELETED"
                    logs.any { it.actionType.contains("UPDATED") } -> "MONEY_TX_UPDATED"
                    else -> "MONEY_TX_ADDED"
                }
            }
            key.startsWith("CATALOG") -> "CATALOG_UPDATE"
            key.startsWith("STOCK") -> "STOCK_UPDATE"
            key.startsWith("STORE") -> "STORE_OPERATION"
            key.startsWith("STAFF") -> "STAFF_MANAGEMENT"
            else -> latestLog.actionType
        }

        val title = when {
            key.startsWith("ORDER") -> "Pesanan $entityId"
            key.startsWith("MONEY") -> "Aktivitas Money Manager"
            key.startsWith("CATALOG") -> "Menu / Katalog: $entityId"
            key.startsWith("STOCK") -> "Stok Bahan: $entityId"
            key.startsWith("STORE") -> "Operasional Toko Cabang $branchId"
            key.startsWith("STAFF") -> "Akun Staff: $entityId"
            else -> latestLog.summary
        }

        val highLevelSummary = if (logs.size == 1) {
            latestLog.summary
        } else {
            when {
                key.startsWith("ORDER") -> {
                    val hasErased = logs.any { it.actionType.contains("ERASE") }
                    val hasDone = logs.any { it.actionType.contains("DONE") }
                    val hasCancel = logs.any { it.actionType.contains("CANCEL") }
                    val editCount = logs.count { it.actionType.contains("EDIT") }
                    val payCount = logs.count { it.actionType.contains("PAID") }

                    val stages = mutableListOf<String>()
                    stages.add("Dibuat")
                    if (editCount > 0) stages.add("${editCount}x Diedit")
                    if (payCount > 0) stages.add("${payCount}x Dibayar")
                    stages.add(if (hasErased) "Dihapus (Erased)" else if (hasDone) "Selesai (Done)" else if (hasCancel) "Dibatalkan" else "Diproses")

                    "Alur: ${stages.joinToString(" ➔ ")} • Terakhir: ${latestLog.summary}"
                }
                key.startsWith("MONEY") -> {
                    "Total ${logs.size} riwayat transaksi Money Manager. Terakhir: ${latestLog.summary}"
                }
                key.startsWith("CATALOG") -> {
                    "Total ${logs.size} pembaruan data pada menu '$entityId'. Terakhir: ${latestLog.summary}"
                }
                key.startsWith("STOCK") -> {
                    "Total ${logs.size} pergerakan/penyesuaian stok '$entityId'. Terakhir: ${latestLog.summary}"
                }
                key.startsWith("STORE") -> {
                    "Total ${logs.size} aktivitas operasional toko cabang $branchId. Terakhir: ${latestLog.summary}"
                }
                else -> {
                    "Total ${logs.size} aktivitas pada $entityId. Terakhir: ${latestLog.summary}"
                }
            }
        }

        GroupedAuditLog(
            groupKey = key,
            entityId = entityId,
            category = category,
            mainActionType = mainActionType,
            title = title,
            highLevelSummary = highLevelSummary,
            latestActor = latestLog.actorUsername,
            latestActorRole = latestLog.actorRole,
            branchId = branchId,
            latestTimestamp = latestLog.createdAt ?: firstLog.createdAt,
            subLogsChronological = logs
        )
    }.sortedByDescending { it.latestTimestamp?.seconds ?: 0L }
}

enum class OrderMedia(val label: String, val prefix: String, val requiresCustomerCode: Boolean) {
    WALK_IN("Di tempat", "", false),
    WHATSAPP("WhatsApp", "", false),
    INSTAGRAM("Instagram", "", false),
    GRABFOOD("GrabFood", "GF - ", true),
    GOFOOD("GoFood", "F - ", true),
    SHOPEEFOOD("ShopeeFood", "# ", true);

    val isPlatform: Boolean
        get() = this == GRABFOOD || this == GOFOOD || this == SHOPEEFOOD

    val defaultSettlementMethod: String
        get() = when (this) {
            WALK_IN -> "Cash"
            WHATSAPP -> "Cash / Transfer"
            INSTAGRAM -> "Cash / Transfer"
            GRABFOOD -> "GrabFood -> Rekening BCA"
            GOFOOD -> "GoFood -> Rekening BCA"
            SHOPEEFOOD -> "ShopeeFood -> Rekening BCA"
        }

    companion object {
        fun fromLabel(label: String): OrderMedia =
            entries.firstOrNull {
                it.label.equals(label, ignoreCase = true) ||
                (it == WALK_IN && (label.equals("Walk In", ignoreCase = true) || label.equals("Di tempat", ignoreCase = true)))
            } ?: WALK_IN
    }
}

enum class StaffRole(val label: String) {
    DEVELOPER("Developer"),
    OWNER("Owner"),
    SUPERVISOR("Supervisor"),
    KASIR("Kasir");

    val canManageCatalogAndDelete: Boolean
        get() = this == DEVELOPER || this == OWNER || this == SUPERVISOR

    val canInputBackdatedOrder: Boolean
        get() = this == DEVELOPER || this == OWNER || this == SUPERVISOR

    val canManageStaffAccounts: Boolean
        get() = this == DEVELOPER || this == OWNER || this == SUPERVISOR

    val canAccessMoneyManager: Boolean
        get() = this == DEVELOPER || this == OWNER || this == SUPERVISOR

    val canEditOrDeleteMoneyManager: Boolean
        get() = this == DEVELOPER || this == OWNER

    val level: Int
        get() = when (this) {
            DEVELOPER -> 4
            OWNER -> 3
            SUPERVISOR -> 2
            KASIR -> 1
        }

    fun canEditAccountOfRole(targetRole: StaffRole): Boolean {
        return when (this) {
            DEVELOPER -> true
            OWNER -> targetRole != DEVELOPER
            SUPERVISOR -> targetRole == SUPERVISOR || targetRole == KASIR
            KASIR -> false
        }
    }

    fun assignableRoles(): List<StaffRole> {
        return when (this) {
            DEVELOPER -> listOf(DEVELOPER, OWNER, SUPERVISOR, KASIR)
            OWNER -> listOf(OWNER, SUPERVISOR, KASIR)
            SUPERVISOR -> listOf(SUPERVISOR, KASIR)
            KASIR -> emptyList()
        }
    }

    companion object {
        fun fromLabel(label: String): StaffRole =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: KASIR
    }
}

enum class MoneyTransactionType(val label: String) {
    INCOME("Income"),
    EXPENSE("Expense"),
    TRANSFER("Transfer")
}

enum class MoneyRepetitionOption(val label: String) {
    NONE("None"),
    EVERY_DAY("Every Day"),
    WEEKDAYS("Weekdays"),
    WEEKEND("Weekend"),
    EVERY_WEEK("Every Week"),
    EVERY_2_WEEKS("Every 2 Weeks"),
    EVERY_4_WEEKS("Every 4 Weeks"),
    EVERY_MONTH("Every Month"),
    END_OF_MONTH("The End of the Month"),
    EVERY_2_MONTHS("Every 2 Months"),
    EVERY_3_MONTHS("Every 3 Months"),
    EVERY_4_MONTHS("Every 4 Months"),
    EVERY_6_MONTHS("Every 6 Months"),
    ANNUALLY("Annually")
}

data class MoneyAccountGroupDef(
    val groupName: String,
    val accounts: List<String>
)

data class BankAccountDetail(
    val id: String = "",
    val name: String = "",
    val ownerName: String = "",
    val accountNumber: String = ""
)

data class DigitalWalletDetail(
    val id: String = "",
    val name: String = "",
    val ownerName: String = "",
    val phoneNumber: String = "",
    val email: String = ""
)

fun canUserEditStaffAccount(sessionRole: StaffRole, sessionUsername: String, targetAccount: StaffAccount): Boolean {
    return when (sessionRole) {
        StaffRole.DEVELOPER -> true
        StaffRole.OWNER -> targetAccount.role != StaffRole.DEVELOPER
        StaffRole.SUPERVISOR -> {
            if (targetAccount.role == StaffRole.OWNER || targetAccount.role == StaffRole.DEVELOPER) {
                false
            } else {
                targetAccount.username.equals(sessionUsername, ignoreCase = true)
            }
        }
        StaffRole.KASIR -> false
    }
}

fun canUserEditStaffAccount(sessionAccount: StaffAccount, targetAccount: StaffAccount): Boolean =
    canUserEditStaffAccount(sessionAccount.role, sessionAccount.username, targetAccount)

fun canUserDeleteStaffAccount(sessionRole: StaffRole, sessionUsername: String, targetAccount: StaffAccount): Boolean {
    return when (sessionRole) {
        StaffRole.DEVELOPER -> true
        StaffRole.OWNER -> {
            targetAccount.role != StaffRole.DEVELOPER && !targetAccount.username.equals(sessionUsername, ignoreCase = true)
        }
        StaffRole.SUPERVISOR -> false
        StaffRole.KASIR -> false
    }
}

fun canUserDeleteStaffAccount(sessionAccount: StaffAccount, targetAccount: StaffAccount): Boolean =
    canUserDeleteStaffAccount(sessionAccount.role, sessionAccount.username, targetAccount)

data class MoneyAccountInitialBalance(
    val branchId: String = "Global",
    val accountGroup: String,
    val accountName: String,
    val initialBalance: Long = 0L
)

data class MoneyCategoryDef(
    val type: MoneyTransactionType,
    val emoji: String,
    val categoryName: String,
    val subCategories: List<String> = emptyList()
) {
    val displayTitle: String
        get() = if (emoji.isNotBlank()) "$emoji $categoryName" else categoryName
}

data class MoneyBookmarkRecord(
    val id: String = "",
    val branchId: String = "Global",
    val title: String,
    val type: MoneyTransactionType = MoneyTransactionType.EXPENSE,
    val amount: Long = 0L,
    val category: String = "",
    val subCategory: String = "",
    val accountGroup: String = "Uang Tunai",
    val accountName: String = "Cash",
    val toAccountGroup: String = "",
    val toAccountName: String = "",
    val note: String = "",
    val description: String = "",
    val tag: String = ""
) {
    val categoryName: String get() = category
    val subCategoryName: String get() = subCategory
}

data class MoneyTransactionRecord(
    val id: String = "",
    val branchId: String = "Global",
    val type: MoneyTransactionType = MoneyTransactionType.EXPENSE,
    val timestampMillis: Long = System.currentTimeMillis(),
    val dateKey: String = "", // yyyy-MM-dd
    val monthKey: String = "", // yyyy-MM
    val timeFormatted: String = "08:00",
    val amount: Long = 0L,
    val transferFee: Long = 0L,
    val category: String = "",
    val subCategory: String = "",
    val accountGroup: String = "Uang Tunai",
    val accountName: String = "Cash",
    val toAccountGroup: String = "",
    val toAccountName: String = "",
    val note: String = "",
    val description: String = "",
    val tag: String = "",
    val repetition: String = "None",
    val isAutoPosSync: Boolean = false,
    val posSyncKey: String = "",
    val createdBy: String = "owner"
) {
    val categoryName: String get() = category
    val subCategoryName: String get() = subCategory
    val timeText: String get() = timeFormatted
    val feeAmount: Long get() = transferFee
}

data class DailyChannelRecapItem(
    val dateKey: String,
    val subCategory: String,
    val accountGroup: String,
    val accountName: String,
    val grossAmount: Long,
    val platformDeduction: Long,
    val netAmount: Long,
    val orderCount: Int,
    val waffleCount: Int = 0,
    val drinkCount: Int = 0,
    val customItemSummary: String = ""
) {
    val formattedItemSummary: String
        get() {
            if (customItemSummary.isNotBlank()) return customItemSummary
            val parts = mutableListOf<String>()
            if (waffleCount > 0) parts.add("$waffleCount Waffle")
            if (drinkCount > 0) parts.add("$drinkCount Minuman")
            return if (parts.isNotEmpty()) parts.joinToString(", ") else ""
        }

    val formattedNote: String
        get() = when {
            subCategory.equals("Cash", ignoreCase = true) -> "Penjualan Tunai"
            subCategory.equals("QRIS", ignoreCase = true) -> "QRIS"
            subCategory.equals("Transfer Tunai", ignoreCase = true) -> "Transfer Tunai"
            else -> subCategory
        }

    val formattedDescription: String
        get() = when {
            subCategory.equals("QRIS", ignoreCase = true) -> ""
            else -> "$orderCount Pesanan"
        }
}

data class BranchProfile(
    val branchId: String,
    val location: String,
    val ownerName: String,
    val ownerEmail: String,
    val ownerPhone: String,
    val ownerUsername: String,
    val initializedFrom: String = "Master Seed",
    val createdAtText: String = "Default System",
    val name: String = ""
) {
    val displayName: String
        get() = when {
            name.isNotBlank() -> name
            branchId.equals("Global", ignoreCase = true) -> "Global"
            else -> "Gabek"
        }
}

data class StaffAccount(
    val branchId: String,
    val username: String,
    val password: String,
    val role: StaffRole,
    val displayName: String,
    val email: String = "",
    val phone: String = ""
)

data class RecipeUsage(
    val ingredientName: String,
    val amountPerPortion: Double,
    val unit: String
)

data class CartLineItem(
    val lineId: String,
    val category: String, // "Egg Waffle" or "Drinks"
    val primaryItemId: String,
    val primaryItemName: String,
    val isHalfHalf: Boolean = false,
    val isDoubleTopping: Boolean = false,
    val secondaryItemId: String = "",
    val secondaryItemName: String = "",
    val flavourName: String = "Original",
    val addOnNames: List<String> = emptyList(),
    val sweetnessLevel: String = "Normal",
    val icedLevel: String = "Normal",
    val qty: Int = 1,
    val unitPriceWalkIn: Long = 0L,
    val unitPriceGrabFood: Long = 0L,
    val unitPriceGoFood: Long = 0L,
    val unitPriceShopeeFood: Long = 0L
) {
    fun unitPriceFor(media: OrderMedia): Long = when (media) {
        OrderMedia.WALK_IN, OrderMedia.WHATSAPP, OrderMedia.INSTAGRAM -> unitPriceWalkIn
        OrderMedia.GRABFOOD -> unitPriceGrabFood
        OrderMedia.GOFOOD -> unitPriceGoFood
        OrderMedia.SHOPEEFOOD -> unitPriceShopeeFood
    }

    fun lineTotalFor(media: OrderMedia): Long = unitPriceFor(media) * qty

    fun singleLineTitle(): String = when {
        category == "Egg Waffle" && isHalfHalf && secondaryItemName.isNotBlank() ->
            "$flavourName 1/2 $primaryItemName + 1/2 $secondaryItemName".trim()
        category == "Egg Waffle" && isDoubleTopping && secondaryItemName.isNotBlank() ->
            "$flavourName $primaryItemName + $secondaryItemName".trim()
        category == "Egg Waffle" ->
            "$flavourName $primaryItemName".trim()
        else -> primaryItemName
    }

    fun displayCartTitle(): String = when {
        category == "Egg Waffle" && isHalfHalf && secondaryItemName.isNotBlank() ->
            "$flavourName 1/2 $primaryItemName\n+ 1/2 $secondaryItemName".trim()
        category == "Egg Waffle" && isDoubleTopping && secondaryItemName.isNotBlank() ->
            "$flavourName $primaryItemName\n+ $secondaryItemName".trim()
        category == "Egg Waffle" ->
            "$flavourName $primaryItemName".trim()
        else -> primaryItemName
    }

    fun formattedTitle(): String = singleLineTitle()

    fun formattedCustomization(): String = when {
        category == "Egg Waffle" -> if (addOnNames.isEmpty()) "Tanpa Add-on" else addOnNames.joinToString(", ")
        category == "Drinks" -> "Sweetness: $sweetnessLevel • Ice: $icedLevel"
        else -> if (addOnNames.isNotEmpty()) addOnNames.joinToString(", ") else "Standar"
    }

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("lineId", lineId)
        put("category", category)
        put("primaryItemId", primaryItemId)
        put("primaryItemName", primaryItemName)
        put("isHalfHalf", isHalfHalf)
        put("isDoubleTopping", isDoubleTopping)
        put("secondaryItemId", secondaryItemId)
        put("secondaryItemName", secondaryItemName)
        put("flavourName", flavourName)
        put("addOnNames", JSONArray(addOnNames))
        put("sweetnessLevel", sweetnessLevel)
        put("icedLevel", icedLevel)
        put("qty", qty)
        put("unitPriceWalkIn", unitPriceWalkIn)
        put("unitPriceGrabFood", unitPriceGrabFood)
        put("unitPriceGoFood", unitPriceGoFood)
        put("unitPriceShopeeFood", unitPriceShopeeFood)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): CartLineItem {
            val addOnsArr = obj.optJSONArray("addOnNames") ?: JSONArray()
            val addOns = mutableListOf<String>()
            for (i in 0 until addOnsArr.length()) {
                addOns.add(addOnsArr.optString(i))
            }
            return CartLineItem(
                lineId = obj.optString("lineId", "line_${System.currentTimeMillis()}"),
                category = obj.optString("category", "Egg Waffle"),
                primaryItemId = obj.optString("primaryItemId", ""),
                primaryItemName = obj.optString("primaryItemName", ""),
                isHalfHalf = obj.optBoolean("isHalfHalf", false),
                isDoubleTopping = obj.optBoolean("isDoubleTopping", false),
                secondaryItemId = obj.optString("secondaryItemId", ""),
                secondaryItemName = obj.optString("secondaryItemName", ""),
                flavourName = obj.optString("flavourName", "Original"),
                addOnNames = addOns,
                sweetnessLevel = obj.optString("sweetnessLevel", "Normal"),
                icedLevel = obj.optString("icedLevel", "Normal"),
                qty = obj.optInt("qty", 1),
                unitPriceWalkIn = obj.optLong("unitPriceWalkIn", 0L),
                unitPriceGrabFood = obj.optLong("unitPriceGrabFood", 0L),
                unitPriceGoFood = obj.optLong("unitPriceGoFood", 0L),
                unitPriceShopeeFood = obj.optLong("unitPriceShopeeFood", 0L)
            )
        }

        fun serializeList(items: List<CartLineItem>): String {
            val arr = JSONArray()
            items.forEach { arr.put(it.toJsonObject()) }
            return arr.toString()
        }

        fun deserializeList(payload: String): List<CartLineItem> {
            return try {
                val arr = JSONArray(payload)
                val list = mutableListOf<CartLineItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    list.add(fromJsonObject(obj))
                }
                if (list.isNotEmpty()) list else fallbackDeserializeList(payload)
            } catch (e: Exception) {
                fallbackDeserializeList(payload)
            }
        }

        private fun fallbackDeserializeList(payload: String): List<CartLineItem> {
            if (payload.isBlank() || payload == "[]") return emptyList()
            return try {
                val items = mutableListOf<CartLineItem>()
                val nameRegex = Regex(""""primaryItemName"\s*:\s*"([^"]*)"""")
                val categoryRegex = Regex(""""category"\s*:\s*"([^"]*)"""")
                val flavourRegex = Regex(""""flavourName"\s*:\s*"([^"]*)"""")
                val qtyRegex = Regex(""""qty"\s*:\s*(\d+)""")

                val names = nameRegex.findAll(payload).map { it.groupValues[1] }.toList()
                val categories = categoryRegex.findAll(payload).map { it.groupValues[1] }.toList()
                val flavours = flavourRegex.findAll(payload).map { it.groupValues[1] }.toList()
                val quantities = qtyRegex.findAll(payload).map { it.groupValues[1].toIntOrNull() ?: 1 }.toList()

                for (i in names.indices) {
                    items.add(
                        CartLineItem(
                            lineId = "line_$i",
                            category = categories.getOrElse(i) { "Egg Waffle" },
                            primaryItemId = "item_$i",
                            primaryItemName = names[i],
                            flavourName = flavours.getOrElse(i) { "Original" },
                            qty = quantities.getOrElse(i) { 1 }
                        )
                    )
                }
                items
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

fun normalizeCategoryCanonical(name: String): String {
    return name.trim()
        .replace("_", " ")
        .replace("-", " ")
        .replace(Regex("\\s+"), " ")
        .lowercase()
}

fun areCategoriesMatching(a: String, b: String): Boolean {
    if (a.equals(b, ignoreCase = true)) return true
    return normalizeCategoryCanonical(a) == normalizeCategoryCanonical(b)
}

