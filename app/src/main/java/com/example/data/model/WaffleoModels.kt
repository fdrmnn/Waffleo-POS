package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import kotlin.math.max
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
    val processingStage: Int = 0, // 0 = BELUM_DIPROSES, 1 = SEDANG_DIPROSES, 2 = SELESAI_DIPROSES
    val historicalProducedItemsPayload: String = "[]",
    val revisionCount: Int = 0,
    val overpaidAmount: Long = 0L,
    val settlementStatus: String = "UNPAID", // UNPAID, PARTIALLY_PAID, PAID_EXACT, OVERPAID_REFUND_REQUIRED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun getPaymentLedger(): List<OrderPaymentLedgerEntry> = OrderPaymentLedgerEntry.deserializeList(paymentLedgerPayload)
    fun getTimelineEvents(): List<OrderTimelineEvent> = OrderTimelineEvent.deserializeList(orderTimelinePayload)

    @get:com.google.firebase.firestore.Exclude
    val parsedLineItems: List<CartLineItem> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        CartLineItem.deserializeList(itemsPayload)
    }

    @get:com.google.firebase.firestore.Exclude
    val parsedHistoricalProducedItems: List<CartLineItem> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        CartLineItem.deserializeList(historicalProducedItemsPayload)
    }

    @get:com.google.firebase.firestore.Exclude
    val effectiveProcessingStage: Int
        get() = when {
            processingStage in 1..2 -> processingStage
            isPrepared -> 2
            else -> 0
        }

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
            title = sanitizeBranchIdsInText(obj.optString("title", "")),
            description = sanitizeBranchIdsInText(obj.optString("description", ""))
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
    val recordType: String = "MENU_ITEM", // MENU_ITEM, MODIFIER, PROMO, BRANCH_CONFIG
    val branchId: String = "Global",
    val name: String = "",
    val category: String = "", // Egg Waffle, Drinks, FLAVOUR, ADD_ON, SWEETNESS, ICE, PROMO, BRANCH
    val priceWalkIn: Long = 0L,
    val priceGrabFood: Long = 0L,
    val priceGoFood: Long = 0L,
    val priceShopeeFood: Long = 0L,
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
    val category: String, // "ORDER", "CATALOG", "STAFF", "SYSTEM"
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
            (log.actionType.contains("ORDER") || log.entityId.startsWith("WFL-")) && log.entityId.isNotBlank() -> {
                val cleanOrderEntity = sanitizeBranchIdsInText(log.entityId)
                "ORDER_${log.branchId}_$cleanOrderEntity"
            }

            // Pengelompokan aktivitas Money Manager
            log.actionType.contains("MONEY") ->
                "MONEY_${log.branchId}_${log.entityId.ifBlank { log.id }}"

            // Pengelompokan perubahan katalog / menu
            (log.actionType.contains("CATALOG") || log.actionType.contains("SEED")) && log.entityId.isNotBlank() ->
                "CATALOG_${log.branchId}_${log.entityId}"

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
            key.startsWith("STORE") -> "STORE_OPERATION"
            key.startsWith("STAFF") -> "STAFF_MANAGEMENT"
            else -> latestLog.actionType
        }

        val cleanBranchLabel = formatCleanBranchName(branchId, branchId)
        val sanitizedEntity = sanitizeBranchIdsInText(entityId)
        val title = when {
            key.startsWith("ORDER") -> "Pesanan $sanitizedEntity"
            key.startsWith("MONEY") -> "Aktivitas Money Manager"
            key.startsWith("CATALOG") -> "Menu / Katalog: $sanitizedEntity"
            key.startsWith("STORE") -> "Operasional Toko Cabang $cleanBranchLabel"
            key.startsWith("STAFF") -> "Akun Staff: $sanitizedEntity"
            else -> sanitizeBranchIdsInText(latestLog.summary)
        }

        val latestSummarySanitized = sanitizeBranchIdsInText(latestLog.summary)
        val highLevelSummary = if (logs.size == 1) {
            latestSummarySanitized
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

                    "Alur: ${stages.joinToString(" ➔ ")} • Terakhir: $latestSummarySanitized"
                }
                key.startsWith("MONEY") -> {
                    "Total ${logs.size} riwayat transaksi Money Manager. Terakhir: $latestSummarySanitized"
                }
                key.startsWith("CATALOG") -> {
                    "Total ${logs.size} pembaruan data pada menu '$entityId'. Terakhir: $latestSummarySanitized"
                }
                key.startsWith("STORE") -> {
                    "Total ${logs.size} aktivitas operasional toko Cabang $cleanBranchLabel. Terakhir: $latestSummarySanitized"
                }
                else -> {
                    "Total ${logs.size} aktivitas pada $entityId. Terakhir: $latestSummarySanitized"
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
    val createdBy: String = "owner",
    val feeAccountChoice: String = "FROM"
) {
    val categoryName: String get() = category
    val subCategoryName: String get() = subCategory
    val timeText: String get() = timeFormatted
    val feeAmount: Long get() = transferFee
    val dateMillis: Long get() = timestampMillis
}

enum class ExpenseNoteMode(val code: String, val label: String) {
    NOTE("NOTE", "Note"),
    MERK("MERK", "Merk");

    companion object {
        fun fromCode(raw: String?): ExpenseNoteMode =
            entries.firstOrNull { it.code.equals(raw?.trim(), ignoreCase = true) } ?: NOTE
    }
}

data class ExpenseUnitGroup(
    val groupTitle: String,
    val units: List<String>
)

val DefaultExpenseUnitGroups: List<ExpenseUnitGroup> = listOf(
    ExpenseUnitGroup("Berat", listOf("gr", "kg", "ons")),
    ExpenseUnitGroup("Volume", listOf("ml", "L", "galon")),
    ExpenseUnitGroup("Kemasan / Unit", listOf("pcs", "pack", "box", "dus", "kaleng", "botol", "pouch", "sak", "ikat", "tabung", "roll"))
)

val AllExpenseUnitSymbols: List<String> = DefaultExpenseUnitGroups.flatMap { it.units }

data class ExpenseSubCategoryInputConfig(
    val categoryName: String,
    val subCategoryName: String,
    val noteMode: ExpenseNoteMode = ExpenseNoteMode.NOTE,
    val isUnitDescriptionEnabled: Boolean = false,
    val unitSymbol: String = "kg"
) {
    val configKey: String
        get() = buildKey(categoryName, subCategoryName)

    companion object {
        fun buildKey(categoryName: String, subCategoryName: String): String =
            "${categoryName.trim().lowercase()}::${subCategoryName.trim().lowercase()}"
    }
}

/**
 * Formats the primary headline for an EXPENSE transaction in Money Manager:
 * Moves Sub-Category to the left of Note/Merk separated by " - " (e.g. "Krimer Kental Manis - Omela").
 */
fun formatExpensePrimaryHeadline(
    subCategoryName: String,
    note: String,
    categoryName: String = ""
): String {
    val cleanSub = subCategoryName.trim()
    val cleanNote = note.trim()
    return when {
        cleanSub.isNotEmpty() && cleanNote.isNotEmpty() -> {
            if (cleanSub.equals(cleanNote, ignoreCase = true)) {
                cleanSub
            } else {
                "$cleanSub - $cleanNote"
            }
        }
        cleanSub.isNotEmpty() -> cleanSub
        cleanNote.isNotEmpty() -> cleanNote
        else -> categoryName.trim()
    }
}

/**
 * Parses a quantity + unit description string (e.g. "2.5kg", "3 kg", "500gr", "1.5 L")
 * into Pair(quantityValueText, canonicalUnitSymbol), or null if it does not match a known unit format.
 */
fun parseQuantityAndUnitFromDescription(description: String): Pair<String, String>? {
    val trimmed = description.trim()
    if (trimmed.isEmpty()) return null
    val regex = Regex("""^(\d+(?:[.,]\d+)?)\s*([a-zA-Z]+)$""")
    val match = regex.matchEntire(trimmed) ?: return null
    val qtyPart = match.groupValues[1]
    val rawUnit = match.groupValues[2]
    val canonicalUnit = AllExpenseUnitSymbols.firstOrNull { it.equals(rawUnit, ignoreCase = true) }
        ?: return null
    return qtyPart to canonicalUnit
}

/**
 * Combines a numeric quantity input and a unit symbol into a clean Description string (e.g. "2.5kg").
 */
fun formatQuantityWithUnitDescription(qtyValueText: String, unitSymbol: String): String {
    val cleanQty = qtyValueText.trim().replace(',', '.')
    val cleanUnit = unitSymbol.trim()
    if (cleanQty.isEmpty()) return ""
    if (cleanUnit.isEmpty()) return cleanQty
    // Keep existing unit if user accidentally typed the unit inside the quantity box
    val alreadyHasUnit = parseQuantityAndUnitFromDescription(cleanQty)
    if (alreadyHasUnit != null) {
        return "${alreadyHasUnit.first}${alreadyHasUnit.second}"
    }
    return "$cleanQty$cleanUnit"
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
        get() = "$orderCount Pesanan"
}

fun isUuidOrInternalBranchId(raw: String): Boolean {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return true
    if (trimmed.startsWith("branch", ignoreCase = true)) return true
    val clean = trimmed
        .removePrefix("Cabang ")
        .removePrefix("cabang ")
        .removePrefix("branch_")
        .removePrefix("BRANCH_")
        .removePrefix("branch")
        .removePrefix("BRANCH")
        .trim()
    if (clean.isEmpty()) return true
    if (clean.contains("-") && clean.length >= 20) return true
    if (clean.length >= 6 && clean.any { it.isDigit() } &&
        clean.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == '-' || it == '_' }
    ) {
        return true
    }
    return false
}

object BranchNameRegistry {
    private val idToDisplayName = java.util.concurrent.ConcurrentHashMap<String, String>()

    private fun normalizeBranchToken(raw: String): String {
        return raw.trim()
            .removePrefix("Cabang ")
            .removePrefix("cabang ")
            .trim()
            .lowercase()
            .removePrefix("branch_")
            .removePrefix("branch")
            .replace("-", "")
            .replace("_", "")
            .trim()
    }

    fun registerBranch(branchId: String, displayName: String) {
        val cleanId = branchId.trim().lowercase()
        val cleanName = displayName.trim()
            .removePrefix("Cabang ")
            .removePrefix("cabang ")
            .trim()
        if (cleanId.isNotBlank() && cleanName.isNotBlank() && !isUuidOrInternalBranchId(cleanName)) {
            idToDisplayName[cleanId] = cleanName
            val stripped = cleanId.removePrefix("cabang ").trim()
            if (stripped.isNotBlank()) {
                idToDisplayName[stripped] = cleanName
            }
            val normalizedToken = normalizeBranchToken(cleanId)
            if (normalizedToken.isNotBlank()) {
                idToDisplayName[normalizedToken] = cleanName
            }
        }
    }

    fun registerAll(profiles: List<BranchProfile>) {
        profiles.forEach { profile ->
            val candidateName = profile.name.ifBlank {
                if (!isUuidOrInternalBranchId(profile.branchId)) profile.branchId else ""
            }
            if (candidateName.isNotBlank()) {
                registerBranch(profile.branchId, candidateName)
            }
        }
    }

    fun resolve(rawIdOrName: String): String? {
        val clean = rawIdOrName.trim()
            .removePrefix("Cabang ")
            .removePrefix("cabang ")
            .trim()
            .lowercase()
        if (clean.isBlank()) return null
        idToDisplayName[clean]?.let { return it }
        val normalizedToken = normalizeBranchToken(clean)
        if (normalizedToken.isNotBlank()) {
            idToDisplayName[normalizedToken]?.let { return it }
        }
        return null
    }

    fun clearForTesting() {
        idToDisplayName.clear()
    }
}

object PasswordHasher {
    private const val PREFIX = "sha256:"

    fun isHashed(value: String): Boolean = value.startsWith(PREFIX) && value.length == PREFIX.length + 64

    fun hashPassword(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        if (isHashed(trimmed)) return trimmed
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(trimmed.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }
        return "$PREFIX$hex"
    }

    fun verifyPassword(input: String, stored: String): Boolean {
        val cleanInput = input.trim()
        val cleanStored = stored.trim()
        if (cleanInput.isEmpty() || cleanStored.isEmpty()) return false
        return if (isHashed(cleanStored)) {
            hashPassword(cleanInput).equals(cleanStored, ignoreCase = true)
        } else {
            cleanInput == cleanStored
        }
    }
}

fun formatCleanBranchName(rawName: String, fallbackBranchId: String = ""): String {
    val trimmed = rawName.trim()
        .removePrefix("Cabang ")
        .removePrefix("cabang ")
        .trim()
    if (trimmed.isNotBlank() && !isUuidOrInternalBranchId(trimmed)) {
        return trimmed
    }
    BranchNameRegistry.resolve(trimmed)?.let { return it }
    val fb = fallbackBranchId.trim()
        .removePrefix("Cabang ")
        .removePrefix("cabang ")
        .trim()
    if (fb.equals("Global", ignoreCase = true) || trimmed.equals("Global", ignoreCase = true)) {
        return "Global"
    }
    if (fb.isNotBlank() && !isUuidOrInternalBranchId(fb)) {
        return fb
    }
    BranchNameRegistry.resolve(fb)?.let { return it }
    return "Gabek"
}

fun sanitizeBranchIdsInText(text: String): String {
    if (text.isBlank()) return text
    val replaceUuidOrBranchToken: (MatchResult) -> CharSequence = { match ->
        BranchNameRegistry.resolve(match.value) ?: "Gabek"
    }
    return text
        // Format order numbers that contain raw branch slugs or long hashes into clean WFL-010 format
        .replace(Regex("(?i)WFL-[A-Za-z0-9_-]{10,}-\\d{8}-(\\d+)"), "WFL-$1")
        .replace(Regex("(?i)WFL-BRANCH[0-9A-Z_-]{6,}-(\\d{8})-(\\d+)"), "WFL-$2")
        .replace(Regex("(?i)BRANCH[0-9A-F]{8,}"), replaceUuidOrBranchToken)
        .replace(Regex("(?i)BRANCH[0-9A-Za-z_]{8,}"), replaceUuidOrBranchToken)
        .replace(Regex("(?i)branch_[0-9a-f-]{6,}"), replaceUuidOrBranchToken)
        .replace(Regex("(?i)\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b"), replaceUuidOrBranchToken)
        .replace(Regex("(?i)\\b[0-9a-f]{20,}\\b"), replaceUuidOrBranchToken)
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
        get() = formatCleanBranchName(name, branchId)
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
    val unitPriceShopeeFood: Long = 0L,
    val fulfillmentState: String = "BELUM_DIPROSES", // BELUM_DIPROSES, SEDANG_DIPROSES, SELESAI_DIPROSES
    val completedUnitCount: Int = 0,
    val isCompletedLockedBadge: Boolean = false,
    val addedInRevision: Int = 0
) {
    val processingStage: Int
        get() = when {
            isCompletedLockedBadge || fulfillmentState == "SELESAI_DIPROSES" -> 2
            fulfillmentState == "SEDANG_DIPROSES" -> 1
            else -> 0
        }

    val preparedQty: Int
        get() = completedUnitCount

    val isNewRevisionAddition: Boolean
        get() = addedInRevision > 0 && !isCompletedLockedBadge && fulfillmentState != "SELESAI_DIPROSES"

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

    fun toJsonString(): String {
        val addOnsJson = "[${addOnNames.joinToString(",") { "\"${escapeJson(it)}\"" }}]"
        return """{"lineId":"${escapeJson(lineId)}","category":"${escapeJson(category)}","primaryItemId":"${escapeJson(primaryItemId)}","primaryItemName":"${escapeJson(primaryItemName)}","isHalfHalf":$isHalfHalf,"isDoubleTopping":$isDoubleTopping,"secondaryItemId":"${escapeJson(secondaryItemId)}","secondaryItemName":"${escapeJson(secondaryItemName)}","flavourName":"${escapeJson(flavourName)}","addOnNames":$addOnsJson,"sweetnessLevel":"${escapeJson(sweetnessLevel)}","icedLevel":"${escapeJson(icedLevel)}","qty":$qty,"unitPriceWalkIn":$unitPriceWalkIn,"unitPriceGrabFood":$unitPriceGrabFood,"unitPriceGoFood":$unitPriceGoFood,"unitPriceShopeeFood":$unitPriceShopeeFood,"fulfillmentState":"${escapeJson(fulfillmentState)}","completedUnitCount":$completedUnitCount,"isCompletedLockedBadge":$isCompletedLockedBadge,"addedInRevision":$addedInRevision}"""
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
        put("fulfillmentState", fulfillmentState)
        put("completedUnitCount", completedUnitCount)
        put("isCompletedLockedBadge", isCompletedLockedBadge)
        put("addedInRevision", addedInRevision)
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
                unitPriceShopeeFood = obj.optLong("unitPriceShopeeFood", 0L),
                fulfillmentState = obj.optString("fulfillmentState", "BELUM_DIPROSES"),
                completedUnitCount = obj.optInt("completedUnitCount", 0),
                isCompletedLockedBadge = obj.optBoolean("isCompletedLockedBadge", false),
                addedInRevision = obj.optInt("addedInRevision", 0)
            )
        }

        fun fromRawJsonObjectString(raw: String): CartLineItem {
            val addOnsRaw = extractJsonArrayRaw(raw, "addOnNames")
            val addOns = Regex(""""((?:\\.|[^"\\])*)"""").findAll(addOnsRaw)
                .map { it.groupValues[1] }
                .filter { it.isNotBlank() }
                .toList()
            return CartLineItem(
                lineId = extractJsonString(raw, "lineId").ifBlank { "line_${System.currentTimeMillis()}" },
                category = extractJsonString(raw, "category").ifBlank { "Egg Waffle" },
                primaryItemId = extractJsonString(raw, "primaryItemId"),
                primaryItemName = extractJsonString(raw, "primaryItemName"),
                isHalfHalf = extractJsonBoolean(raw, "isHalfHalf", false),
                isDoubleTopping = extractJsonBoolean(raw, "isDoubleTopping", false),
                secondaryItemId = extractJsonString(raw, "secondaryItemId"),
                secondaryItemName = extractJsonString(raw, "secondaryItemName"),
                flavourName = extractJsonString(raw, "flavourName").ifBlank { "Original" },
                addOnNames = addOns,
                sweetnessLevel = extractJsonString(raw, "sweetnessLevel").ifBlank { "Normal" },
                icedLevel = extractJsonString(raw, "icedLevel").ifBlank { "Normal" },
                qty = extractJsonInt(raw, "qty", 1),
                unitPriceWalkIn = extractJsonLong(raw, "unitPriceWalkIn", 0L),
                unitPriceGrabFood = extractJsonLong(raw, "unitPriceGrabFood", 0L),
                unitPriceGoFood = extractJsonLong(raw, "unitPriceGoFood", 0L),
                unitPriceShopeeFood = extractJsonLong(raw, "unitPriceShopeeFood", 0L),
                fulfillmentState = extractJsonString(raw, "fulfillmentState").ifBlank { "BELUM_DIPROSES" },
                completedUnitCount = extractJsonInt(raw, "completedUnitCount", 0),
                isCompletedLockedBadge = extractJsonBoolean(raw, "isCompletedLockedBadge", false),
                addedInRevision = extractJsonInt(raw, "addedInRevision", 0)
            )
        }

        fun serializeList(items: List<CartLineItem>): String {
            return "[${items.joinToString(",") { it.toJsonString() }}]"
        }

        fun deserializeList(payload: String): List<CartLineItem> {
            if (payload.isBlank() || payload == "[]") return emptyList()
            val objects = splitJsonObjectsArray(payload)
            if (objects.isNotEmpty()) {
                val parsed = objects.map { fromRawJsonObjectString(it) }.filter { it.primaryItemName.isNotBlank() }
                if (parsed.isNotEmpty()) return parsed
            }
            return try {
                val arr = JSONArray(payload)
                val list = mutableListOf<CartLineItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    list.add(fromJsonObject(obj))
                }
                list
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

enum class OrderProcessingStage(val stage: Int, val code: String, val label: String) {
    BELUM_DIPROSES(0, "BELUM_DIPROSES", "Belum Diproses"),
    SEDANG_DIPROSES(1, "SEDANG_DIPROSES", "Sedang Diproses"),
    SELESAI_DIPROSES(2, "SELESAI_DIPROSES", "Selesai Diproses");

    companion object {
        fun fromStage(stage: Int): OrderProcessingStage = when (stage) {
            1 -> SEDANG_DIPROSES
            2 -> SELESAI_DIPROSES
            else -> BELUM_DIPROSES
        }
    }
}

data class EditOrderReconciliationResult(
    val orderedActiveItems: List<CartLineItem>,
    val updatedHistoricalProducedItems: List<CartLineItem>,
    val newlyAddedUnprocessedUnitsCount: Int,
    val nextOrderProcessingStage: Int,
    val nextIsPreparedFlag: Boolean
)

data class PaymentRecalculationResult(
    val newTotalAmount: Long,
    val netPaidAmount: Long,
    val shortfallAmount: Long,
    val overpaidAmount: Long,
    val isPaid: Boolean,
    val settlementStatus: String // UNPAID, PARTIALLY_PAID, PAID_EXACT, OVERPAID_REFUND_REQUIRED
)

object WaffleoOrderEngine {

    fun reconcileEditOrderItems(
        previousActiveItems: List<CartLineItem>,
        previousHistoricalItems: List<CartLineItem>,
        editedCartItems: List<CartLineItem>,
        nextRevisionNumber: Int
    ): EditOrderReconciliationResult {
        val completedTopList = mutableListOf<CartLineItem>()
        val uncompletedBottomList = mutableListOf<CartLineItem>()
        val historicalPool = previousHistoricalItems.toMutableList()

        data class CompletedBucket(
            val item: CartLineItem,
            var remainingCompletedUnits: Int
        )

        val buckets = previousActiveItems.mapNotNull { prev ->
            val compUnits = if (prev.isCompletedLockedBadge || prev.fulfillmentState == "SELESAI_DIPROSES") {
                if (prev.completedUnitCount > 0) prev.completedUnitCount else prev.qty
            } else {
                prev.completedUnitCount.coerceAtLeast(0)
            }
            if (compUnits > 0) CompletedBucket(prev, compUnits) else null
        }.toMutableList()

        for (edited in editedCartItems) {
            val matchingBucket = buckets.firstOrNull {
                it.remainingCompletedUnits > 0 && isSameMenuCustomization(it.item, edited)
            }

            if (matchingBucket != null) {
                val preservedUnits = minOf(edited.qty, matchingBucket.remainingCompletedUnits)
                val newlyAddedUnits = (edited.qty - preservedUnits).coerceAtLeast(0)
                matchingBucket.remainingCompletedUnits -= preservedUnits

                if (preservedUnits > 0) {
                    completedTopList.add(
                        edited.copy(
                            qty = preservedUnits,
                            fulfillmentState = "SELESAI_DIPROSES",
                            completedUnitCount = preservedUnits,
                            isCompletedLockedBadge = true,
                            addedInRevision = matchingBucket.item.addedInRevision
                        )
                    )
                }
                if (newlyAddedUnits > 0) {
                    uncompletedBottomList.add(
                        edited.copy(
                            lineId = "${edited.lineId}_rev$nextRevisionNumber",
                            qty = newlyAddedUnits,
                            fulfillmentState = "BELUM_DIPROSES",
                            completedUnitCount = 0,
                            isCompletedLockedBadge = false,
                            addedInRevision = nextRevisionNumber
                        )
                    )
                }
            } else {
                uncompletedBottomList.add(
                    edited.copy(
                        fulfillmentState = "BELUM_DIPROSES",
                        completedUnitCount = 0,
                        isCompletedLockedBadge = false,
                        addedInRevision = if (edited.addedInRevision > 0) edited.addedInRevision else nextRevisionNumber
                    )
                )
            }
        }

        for (bucket in buckets) {
            if (bucket.remainingCompletedUnits > 0) {
                historicalPool.add(
                    bucket.item.copy(
                        lineId = "${bucket.item.lineId}_hist_rev$nextRevisionNumber",
                        qty = bucket.remainingCompletedUnits,
                        fulfillmentState = "SELESAI_DIPROSES",
                        completedUnitCount = bucket.remainingCompletedUnits,
                        isCompletedLockedBadge = true
                    )
                )
            }
        }

        val finalActive = completedTopList + uncompletedBottomList
        val newlyAddedCount = uncompletedBottomList.sumOf { it.qty }
        val nextStage = if (newlyAddedCount > 0) {
            OrderProcessingStage.BELUM_DIPROSES.stage
        } else if (completedTopList.isNotEmpty()) {
            OrderProcessingStage.SELESAI_DIPROSES.stage
        } else {
            OrderProcessingStage.BELUM_DIPROSES.stage
        }

        return EditOrderReconciliationResult(
            orderedActiveItems = finalActive,
            updatedHistoricalProducedItems = historicalPool,
            newlyAddedUnprocessedUnitsCount = newlyAddedCount,
            nextOrderProcessingStage = nextStage,
            nextIsPreparedFlag = (nextStage == OrderProcessingStage.SELESAI_DIPROSES.stage)
        )
    }

    fun isSameMenuCustomization(a: CartLineItem, b: CartLineItem): Boolean {
        return a.category.equals(b.category, ignoreCase = true) &&
            a.primaryItemName.equals(b.primaryItemName, ignoreCase = true) &&
            a.isHalfHalf == b.isHalfHalf &&
            a.isDoubleTopping == b.isDoubleTopping &&
            a.secondaryItemName.equals(b.secondaryItemName, ignoreCase = true) &&
            a.flavourName.equals(b.flavourName, ignoreCase = true) &&
            a.addOnNames.map { it.lowercase().trim() }.sorted() == b.addOnNames.map { it.lowercase().trim() }.sorted() &&
            a.sweetnessLevel.equals(b.sweetnessLevel, ignoreCase = true) &&
            a.icedLevel.equals(b.icedLevel, ignoreCase = true)
    }

    private fun resolveEffectiveMenuCategoryRank(
        category: String,
        menuCategories: List<String>
    ): Int {
        val idx = menuCategories.indexOfFirst { areCategoriesMatching(it, category) }
        if (idx >= 0) return idx
        return when {
            areCategoriesMatching(category, "Egg Waffle") -> 0
            areCategoriesMatching(category, "Drinks") -> 1
            else -> 500
        }
    }

    private fun resolveEffectiveModifierCategoryRank(
        category: String,
        modifierCategories: List<String>
    ): Int {
        val idx = modifierCategories.indexOfFirst { areCategoriesMatching(it, category) }
        if (idx >= 0) return idx
        return when {
            areCategoriesMatching(category, "Flavour") -> 0
            areCategoriesMatching(category, "Add-on") -> 1
            areCategoriesMatching(category, "Sweetness") -> 2
            areCategoriesMatching(category, "Ice Level") -> 3
            else -> 500
        }
    }

    private data class ResolvedModifierSortKey(
        val settingsIndex: Int,
        val catRank: Int,
        val sortOrder: Int,
        val normalizedName: String
    ) : Comparable<ResolvedModifierSortKey> {
        override fun compareTo(other: ResolvedModifierSortKey): Int {
            if (settingsIndex != other.settingsIndex) return settingsIndex.compareTo(other.settingsIndex)
            if (catRank != other.catRank) return catRank.compareTo(other.catRank)
            if (sortOrder != other.sortOrder) return sortOrder.compareTo(other.sortOrder)
            return normalizedName.compareTo(other.normalizedName)
        }
    }

    private fun buildSortedCatalogForLookup(
        catalog: List<CatalogRecord>,
        menuCategories: List<String>,
        modifierCategories: List<String>
    ): Pair<List<CatalogRecord>, List<CatalogRecord>> {
        val menuItems = catalog
            .filter { it.recordType == "MENU_ITEM" }
            .sortedWith(
                compareBy<CatalogRecord> { resolveEffectiveMenuCategoryRank(it.category, menuCategories) }
                    .thenBy { if (it.sortOrder < 0) 999999 else it.sortOrder }
                    .thenBy { it.name.lowercase() }
            )
        val modifierItems = catalog
            .filter { it.recordType == "MODIFIER" }
            .sortedWith(
                compareBy<CatalogRecord> { resolveEffectiveModifierCategoryRank(it.category, modifierCategories) }
                    .thenBy { if (it.sortOrder < 0) 999999 else it.sortOrder }
                    .thenBy { it.name.lowercase() }
            )
        return menuItems to modifierItems
    }

    private fun resolveModifierKey(
        rawModifier: String,
        preferredCategory: String?,
        sortedModifiers: List<CatalogRecord>,
        modifierCategories: List<String>
    ): ResolvedModifierSortKey {
        val trimmed = rawModifier.removePrefix("+").trim()
        val hasExplicitCat = trimmed.contains(":")
        val targetCat = if (hasExplicitCat) trimmed.substringBefore(":").trim() else preferredCategory
        val targetName = if (hasExplicitCat) trimmed.substringAfter(":").trim() else trimmed
        val cleanBaseName = targetName.substringBefore("(").trim()

        val matchedIdx = if (targetCat != null) {
            sortedModifiers.indexOfFirst { rec ->
                areCategoriesMatching(rec.category, targetCat) && (
                    rec.name.equals(targetName, ignoreCase = true) ||
                        rec.name.substringBefore("(").trim().equals(cleanBaseName, ignoreCase = true)
                    )
            }.takeIf { it >= 0 } ?: sortedModifiers.indexOfFirst { rec ->
                rec.name.equals(targetName, ignoreCase = true) ||
                    rec.name.substringBefore("(").trim().equals(cleanBaseName, ignoreCase = true)
            }
        } else {
            sortedModifiers.indexOfFirst { rec ->
                rec.name.equals(targetName, ignoreCase = true) ||
                    rec.name.substringBefore("(").trim().equals(cleanBaseName, ignoreCase = true)
            }
        }

        return if (matchedIdx >= 0) {
            val rec = sortedModifiers[matchedIdx]
            ResolvedModifierSortKey(
                settingsIndex = matchedIdx,
                catRank = resolveEffectiveModifierCategoryRank(rec.category, modifierCategories),
                sortOrder = if (rec.sortOrder < 0) 999999 else rec.sortOrder,
                normalizedName = targetName.lowercase()
            )
        } else {
            val fallbackCatRank = if (targetCat != null) {
                resolveEffectiveModifierCategoryRank(targetCat, modifierCategories)
            } else {
                500
            }
            val fallbackOrder = if (preferredCategory.equals("Flavour", ignoreCase = true) &&
                cleanBaseName.equals("Original", ignoreCase = true)
            ) {
                -1
            } else {
                999999
            }
            ResolvedModifierSortKey(
                settingsIndex = Int.MAX_VALUE,
                catRank = fallbackCatRank,
                sortOrder = fallbackOrder,
                normalizedName = targetName.lowercase()
            )
        }
    }

    /**
     * Sorts raw modifier / add-on display strings according to Settings modifier category & sortOrder.
     */
    fun sortModifierNamesBySettings(
        modifierNames: List<String>,
        catalog: List<CatalogRecord> = emptyList(),
        modifierCategories: List<String> = listOf("Flavour", "Add-on", "Sweetness", "Ice Level")
    ): List<String> {
        if (modifierNames.size <= 1) return modifierNames
        val (_, sortedModifiers) = buildSortedCatalogForLookup(catalog, emptyList(), modifierCategories)
        return modifierNames.sortedWith { a, b ->
            val keyA = resolveModifierKey(a, null, sortedModifiers, modifierCategories)
            val keyB = resolveModifierKey(b, null, sortedModifiers, modifierCategories)
            keyA.compareTo(keyB)
        }
    }

    /**
     * Universal Cart & Order Line-Item Sorting:
     * 1. (Optional) Revision group boundary: pre-edit items before newly added revision additions
     * 2. Menu Category order (Settings order)
     * 3. Menu Item order (Settings MENU_ITEM sortOrder + name; single < double topping < half-half + secondary item order)
     * 4. Flavour order (Settings MODIFIER "Flavour" sortOrder + name; keeps same-menu items grouped by flavour)
     * 5. No-modifier priority (items without modifiers always precede items with modifiers within the same menu & flavour)
     * 6. Modifiers sorted lexicographically according to Settings MODIFIER order
     * 7. Deterministic tie-breakers
     */
    fun sortCartLineItems(
        items: List<CartLineItem>,
        catalog: List<CatalogRecord> = emptyList(),
        menuCategories: List<String> = listOf("Egg Waffle", "Drinks"),
        modifierCategories: List<String> = listOf("Flavour", "Add-on", "Sweetness", "Ice Level"),
        separateNewRevisionAdditions: Boolean = false
    ): List<CartLineItem> {
        if (items.isEmpty()) return emptyList()
        val (sortedMenuItems, sortedModifiers) = buildSortedCatalogForLookup(
            catalog = catalog,
            menuCategories = menuCategories,
            modifierCategories = modifierCategories
        )
        val hasRevisionSplit = separateNewRevisionAdditions && items.any { it.isNewRevisionAddition }

        fun resolveMenuIndex(itemId: String, itemName: String, itemCategory: String): Int {
            if (itemId.isNotBlank()) {
                val byId = sortedMenuItems.indexOfFirst { it.id == itemId }
                if (byId >= 0) return byId
            }
            val byCatAndName = sortedMenuItems.indexOfFirst {
                areCategoriesMatching(it.category, itemCategory) && it.name.equals(itemName, ignoreCase = true)
            }
            if (byCatAndName >= 0) return byCatAndName
            val byName = sortedMenuItems.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
            return if (byName >= 0) byName else Int.MAX_VALUE
        }

        fun hasEffectiveModifiers(item: CartLineItem): Boolean {
            return if (areCategoriesMatching(item.category, "Drinks")) {
                item.addOnNames.isNotEmpty() ||
                    (item.icedLevel.isNotBlank() && !item.icedLevel.equals("Normal", ignoreCase = true)) ||
                    (item.sweetnessLevel.isNotBlank() && !item.sweetnessLevel.equals("Normal", ignoreCase = true))
            } else {
                item.addOnNames.isNotEmpty()
            }
        }

        fun buildEffectiveModifierKeys(item: CartLineItem): List<ResolvedModifierSortKey> {
            val preferredCat = if (areCategoriesMatching(item.category, "Egg Waffle")) "Add-on" else null
            val keys = item.addOnNames.map { modName ->
                resolveModifierKey(modName, preferredCat, sortedModifiers, modifierCategories)
            }.toMutableList()
            if (areCategoriesMatching(item.category, "Drinks")) {
                if (item.icedLevel.isNotBlank() && !item.icedLevel.equals("Normal", ignoreCase = true)) {
                    keys.add(resolveModifierKey(item.icedLevel, "Ice Level", sortedModifiers, modifierCategories))
                }
                if (item.sweetnessLevel.isNotBlank() && !item.sweetnessLevel.equals("Normal", ignoreCase = true)) {
                    keys.add(resolveModifierKey(item.sweetnessLevel, "Sweetness", sortedModifiers, modifierCategories))
                }
            }
            return keys.sorted()
        }

        val normalizedItems = items.map { item ->
            if (item.addOnNames.size > 1) {
                item.copy(
                    addOnNames = sortModifierNamesBySettings(
                        modifierNames = item.addOnNames,
                        catalog = sortedModifiers,
                        modifierCategories = modifierCategories
                    )
                )
            } else {
                item
            }
        }

        return normalizedItems.sortedWith(Comparator { a, b ->
            if (hasRevisionSplit) {
                val revA = if (a.isNewRevisionAddition) 1 else 0
                val revB = if (b.isNewRevisionAddition) 1 else 0
                if (revA != revB) return@Comparator revA.compareTo(revB)
            }

            // 1. Menu Category Order
            val catRankA = resolveEffectiveMenuCategoryRank(a.category, menuCategories)
            val catRankB = resolveEffectiveMenuCategoryRank(b.category, menuCategories)
            if (catRankA != catRankB) return@Comparator catRankA.compareTo(catRankB)
            val catNameCmp = a.category.lowercase().compareTo(b.category.lowercase())
            if (catNameCmp != 0) return@Comparator catNameCmp

            // 2. Menu Item Order (Settings MENU_ITEM order)
            val menuIdxA = resolveMenuIndex(a.primaryItemId, a.primaryItemName, a.category)
            val menuIdxB = resolveMenuIndex(b.primaryItemId, b.primaryItemName, b.category)
            if (menuIdxA != menuIdxB) return@Comparator menuIdxA.compareTo(menuIdxB)
            val primaryNameCmp = a.primaryItemName.lowercase().compareTo(b.primaryItemName.lowercase())
            if (primaryNameCmp != 0) return@Comparator primaryNameCmp

            // Single < Double Topping < Half-Half + Secondary Item Order
            val comboA = when {
                a.isDoubleTopping -> 1
                a.isHalfHalf -> 2
                else -> 0
            }
            val comboB = when {
                b.isDoubleTopping -> 1
                b.isHalfHalf -> 2
                else -> 0
            }
            if (comboA != comboB) return@Comparator comboA.compareTo(comboB)
            if (comboA > 0) {
                val secIdxA = resolveMenuIndex(a.secondaryItemId, a.secondaryItemName, a.category)
                val secIdxB = resolveMenuIndex(b.secondaryItemId, b.secondaryItemName, b.category)
                if (secIdxA != secIdxB) return@Comparator secIdxA.compareTo(secIdxB)
                val secNameCmp = a.secondaryItemName.lowercase().compareTo(b.secondaryItemName.lowercase())
                if (secNameCmp != 0) return@Comparator secNameCmp
            }

            // 3. Flavour Order (Settings MODIFIER "Flavour" order)
            val hasFlavourA = !areCategoriesMatching(a.category, "Drinks") && a.flavourName.isNotBlank()
            val hasFlavourB = !areCategoriesMatching(b.category, "Drinks") && b.flavourName.isNotBlank()
            if (hasFlavourA || hasFlavourB) {
                val flvKeyA = if (hasFlavourA) {
                    resolveModifierKey(a.flavourName, "Flavour", sortedModifiers, modifierCategories)
                } else {
                    ResolvedModifierSortKey(Int.MAX_VALUE, 500, 999999, "")
                }
                val flvKeyB = if (hasFlavourB) {
                    resolveModifierKey(b.flavourName, "Flavour", sortedModifiers, modifierCategories)
                } else {
                    ResolvedModifierSortKey(Int.MAX_VALUE, 500, 999999, "")
                }
                val flvCmp = flvKeyA.compareTo(flvKeyB)
                if (flvCmp != 0) return@Comparator flvCmp
            }

            // 4. Prioritize items WITHOUT modifiers before items WITH modifiers
            val modFlagA = if (hasEffectiveModifiers(a)) 1 else 0
            val modFlagB = if (hasEffectiveModifiers(b)) 1 else 0
            if (modFlagA != modFlagB) return@Comparator modFlagA.compareTo(modFlagB)

            // 5. Modifier comparison according to Settings modifier order
            val modsA = buildEffectiveModifierKeys(a)
            val modsB = buildEffectiveModifierKeys(b)
            val minSize = minOf(modsA.size, modsB.size)
            for (i in 0 until minSize) {
                val mCmp = modsA[i].compareTo(modsB[i])
                if (mCmp != 0) return@Comparator mCmp
            }
            if (modsA.size != modsB.size) return@Comparator modsA.size.compareTo(modsB.size)

            // 6. Deterministic fallback
            val iceCmp = a.icedLevel.lowercase().compareTo(b.icedLevel.lowercase())
            if (iceCmp != 0) return@Comparator iceCmp
            val sweetCmp = a.sweetnessLevel.lowercase().compareTo(b.sweetnessLevel.lowercase())
            if (sweetCmp != 0) return@Comparator sweetCmp
            a.lineId.compareTo(b.lineId)
        })
    }

    fun recalculateOrderPaymentStatus(
        newTotalAmount: Long,
        netPaidAmount: Long
    ): PaymentRecalculationResult {
        val cleanTotal = max(0L, newTotalAmount)
        val cleanPaid = max(0L, netPaidAmount)
        return when {
            cleanPaid <= 0L -> PaymentRecalculationResult(
                newTotalAmount = cleanTotal,
                netPaidAmount = 0L,
                shortfallAmount = cleanTotal,
                overpaidAmount = 0L,
                isPaid = false,
                settlementStatus = "UNPAID"
            )
            cleanPaid < cleanTotal -> PaymentRecalculationResult(
                newTotalAmount = cleanTotal,
                netPaidAmount = cleanPaid,
                shortfallAmount = cleanTotal - cleanPaid,
                overpaidAmount = 0L,
                isPaid = false,
                settlementStatus = "PARTIALLY_PAID"
            )
            cleanPaid == cleanTotal -> PaymentRecalculationResult(
                newTotalAmount = cleanTotal,
                netPaidAmount = cleanPaid,
                shortfallAmount = 0L,
                overpaidAmount = 0L,
                isPaid = true,
                settlementStatus = "PAID_EXACT"
            )
            else -> PaymentRecalculationResult(
                newTotalAmount = cleanTotal,
                netPaidAmount = cleanPaid,
                shortfallAmount = 0L,
                overpaidAmount = cleanPaid - cleanTotal,
                isPaid = true,
                settlementStatus = "OVERPAID_REFUND_REQUIRED"
            )
        }
    }
}

internal fun escapeJson(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "")
}

internal fun extractJsonString(raw: String, key: String): String {
    if (raw.isBlank()) return ""
    val pattern = Regex(""""$key"\s*:\s*"((?:\\.|[^"\\])*)"""")
    val match = pattern.find(raw) ?: return ""
    return match.groupValues[1]
        .replace("\\\"", "\"")
        .replace("\\n", "\n")
        .replace("\\\\", "\\")
}

internal fun extractJsonDouble(raw: String, key: String, default: Double = 0.0): Double {
    if (raw.isBlank()) return default
    val pattern = Regex(""""$key"\s*:\s*(-?\d+(?:\.\d+)?)""")
    return pattern.find(raw)?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: default
}

internal fun extractJsonLong(raw: String, key: String, default: Long = 0L): Long {
    if (raw.isBlank()) return default
    val pattern = Regex(""""$key"\s*:\s*(-?\d+)""")
    return pattern.find(raw)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: default
}

internal fun extractJsonInt(raw: String, key: String, default: Int = 0): Int {
    if (raw.isBlank()) return default
    val pattern = Regex(""""$key"\s*:\s*(-?\d+)""")
    return pattern.find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: default
}

internal fun extractJsonBoolean(raw: String, key: String, default: Boolean = false): Boolean {
    if (raw.isBlank()) return default
    val pattern = Regex(""""$key"\s*:\s*(true|false)""", RegexOption.IGNORE_CASE)
    val token = pattern.find(raw)?.groupValues?.getOrNull(1)?.lowercase() ?: return default
    return token == "true"
}

internal fun extractJsonArrayRaw(raw: String, key: String): String {
    val keyIdx = raw.indexOf("\"$key\"")
    if (keyIdx == -1) return "[]"
    val openBracket = raw.indexOf('[', keyIdx)
    if (openBracket == -1) return "[]"
    var depth = 0
    var inQuotes = false
    var escaped = false
    for (i in openBracket until raw.length) {
        val ch = raw[i]
        if (escaped) {
            escaped = false
            continue
        }
        if (ch == '\\') {
            escaped = true
            continue
        }
        if (ch == '"') {
            inQuotes = !inQuotes
            continue
        }
        if (!inQuotes) {
            if (ch == '[') depth++
            else if (ch == ']') {
                depth--
                if (depth == 0) {
                    return raw.substring(openBracket, i + 1)
                }
            }
        }
    }
    return "[]"
}

internal fun splitJsonObjectsArray(arrayRaw: String): List<String> {
    val trimmed = arrayRaw.trim()
    if (trimmed.isBlank() || trimmed == "[]") return emptyList()
    val objects = mutableListOf<String>()
    var depth = 0
    var objStart = -1
    var inQuotes = false
    var escaped = false

    for (i in trimmed.indices) {
        val ch = trimmed[i]
        if (escaped) {
            escaped = false
            continue
        }
        if (ch == '\\') {
            escaped = true
            continue
        }
        if (ch == '"') {
            inQuotes = !inQuotes
            continue
        }
        if (!inQuotes) {
            if (ch == '{') {
                if (depth == 0) objStart = i
                depth++
            } else if (ch == '}') {
                depth--
                if (depth == 0 && objStart != -1) {
                    objects.add(trimmed.substring(objStart, i + 1))
                    objStart = -1
                }
            }
        }
    }
    return objects
}

/**
 * Automatically capitalizes the first alphabetical character of a string and
 * any alphabetical character starting a new sentence (after `.`, `!`, `?` + whitespace),
 * while preserving user-typed mid-sentence uppercase letters and numbers/decimals (e.g. `1.5 kg`).
 */
fun capitalizeSentenceStart(input: String): String {
    if (input.isEmpty()) return input
    val chars = input.toCharArray()
    var shouldCapitalizeNext = true
    var sawTerminator = false
    for (i in chars.indices) {
        val ch = chars[i]
        if (shouldCapitalizeNext) {
            if (ch.isLetter()) {
                if (ch.isLowerCase()) {
                    chars[i] = ch.titlecaseChar()
                }
                shouldCapitalizeNext = false
                sawTerminator = false
            } else if (!ch.isWhitespace()) {
                shouldCapitalizeNext = false
                sawTerminator = false
            }
        } else {
            if (ch == '.' || ch == '!' || ch == '?') {
                sawTerminator = true
            } else if (sawTerminator && ch.isWhitespace()) {
                shouldCapitalizeNext = true
            } else {
                sawTerminator = false
            }
        }
    }
    return String(chars)
}


