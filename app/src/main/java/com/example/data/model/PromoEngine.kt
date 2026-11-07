package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong
import org.json.JSONArray
import org.json.JSONObject

enum class PromoType(val code: String, val label: String, val shortDesc: String) {
    BUNDLE("BUNDLE", "Bundle / Paket Kombo", "Harga paket atau potongan kombo beberapa produk (misal: 1 Waffle + 1 Minuman)"),
    QUANTITY_DISCOUNT("QUANTITY_DISCOUNT", "Diskon Jumlah Pembelian", "Potongan bertingkat berdasarkan jumlah unit produk yang dibeli"),
    PRODUCT_DISCOUNT("PRODUCT_DISCOUNT", "Diskon Produk / Kategori", "Diskon persentase atau nominal pada produk atau kategori tertentu"),
    MINIMUM_PURCHASE("MINIMUM_PURCHASE", "Minimum Pembelian", "Diskon otomatis saat subtotal belanja mencapai nominal minimum");

    companion object {
        fun fromCode(raw: String): PromoType = when (raw.trim().uppercase()) {
            "BUNDLE", "BUNDLING" -> BUNDLE
            "QUANTITY_DISCOUNT", "PERCENT" -> QUANTITY_DISCOUNT
            "PRODUCT_DISCOUNT" -> PRODUCT_DISCOUNT
            "MINIMUM_PURCHASE", "NOMINAL_MIN", "PERCENT_CAP" -> MINIMUM_PURCHASE
            else -> BUNDLE
        }
    }
}

enum class PromoBenefitType(val code: String, val label: String) {
    FIXED_PACKAGE_PRICE("FIXED_PACKAGE_PRICE", "Harga Tetap Paket (Rp)"),
    DISCOUNT_NOMINAL("DISCOUNT_NOMINAL", "Diskon Nominal (Rp)"),
    DISCOUNT_PERCENT("DISCOUNT_PERCENT", "Diskon Persentase (%)");

    companion object {
        fun fromCode(raw: String): PromoBenefitType =
            entries.firstOrNull { it.code.equals(raw.trim(), ignoreCase = true) } ?: DISCOUNT_NOMINAL
    }
}

enum class PromoStatus(val code: String, val label: String) {
    DRAFT("DRAFT", "Draft"),
    SCHEDULED("SCHEDULED", "Terjadwal"),
    ACTIVE("ACTIVE", "Aktif"),
    PAUSED("PAUSED", "Nonaktif"),
    EXPIRED("EXPIRED", "Kedaluwarsa"),
    EXHAUSTED("EXHAUSTED", "Kuota Habis")
}

data class PromoBundleGroupRule(
    val groupName: String = "Kelompok Produk",
    val requiredQty: Int = 1,
    val targetCategories: List<String> = emptyList(),
    val targetProductNames: List<String> = emptyList() // empty = all products in targetCategories
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("groupName", groupName)
        put("requiredQty", requiredQty.coerceAtLeast(1))
        put("targetCategories", JSONArray(targetCategories))
        put("targetProductNames", JSONArray(targetProductNames))
    }

    companion object {
        fun fromJson(obj: JSONObject): PromoBundleGroupRule {
            val catsArr = obj.optJSONArray("targetCategories") ?: JSONArray()
            val cats = (0 until catsArr.length()).mapNotNull { catsArr.optString(it).takeIf { s -> s.isNotBlank() } }
            val prodsArr = obj.optJSONArray("targetProductNames") ?: JSONArray()
            val prods = (0 until prodsArr.length()).mapNotNull { prodsArr.optString(it).takeIf { s -> s.isNotBlank() } }
            return PromoBundleGroupRule(
                groupName = obj.optString("groupName", "Kelompok Produk"),
                requiredQty = obj.optInt("requiredQty", 1).coerceAtLeast(1),
                targetCategories = cats,
                targetProductNames = prods
            )
        }
    }
}

data class PromoQuantityTier(
    val minQty: Int = 2,
    val benefitType: PromoBenefitType = PromoBenefitType.DISCOUNT_NOMINAL,
    val discountValue: Long = 2000L
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("minQty", minQty.coerceAtLeast(1))
        put("benefitType", benefitType.code)
        put("discountValue", discountValue.coerceAtLeast(0L))
    }

    companion object {
        fun fromJson(obj: JSONObject): PromoQuantityTier = PromoQuantityTier(
            minQty = obj.optInt("minQty", 2).coerceAtLeast(1),
            benefitType = PromoBenefitType.fromCode(obj.optString("benefitType", "DISCOUNT_NOMINAL")),
            discountValue = obj.optLong("discountValue", 2000L).coerceAtLeast(0L)
        )
    }
}

data class PromoConfig(
    val description: String = "",
    val promoType: PromoType = PromoType.BUNDLE,
    val isDraft: Boolean = false,
    val isArchived: Boolean = false,
    // Target Produk & Kategori
    val applyToAllProducts: Boolean = false,
    val targetCategories: List<String> = listOf("Egg Waffle"),
    val targetProductNames: List<String> = emptyList(),
    val excludedProductNames: List<String> = emptyList(),
    val includeModifiersInDiscount: Boolean = false,
    // Aturan Kombo / Bundle
    val bundleGroups: List<PromoBundleGroupRule> = listOf(
        PromoBundleGroupRule("Waffle Pilihan", 1, listOf("Egg Waffle")),
        PromoBundleGroupRule("Minuman Pilihan", 1, listOf("Drinks"))
    ),
    val allowShareItemsWithOtherPromos: Boolean = false,
    // Keuntungan & Tingkatan
    val benefitType: PromoBenefitType = PromoBenefitType.DISCOUNT_NOMINAL,
    val discountValue: Long = 4000L,
    val fixedPackagePrice: Long = 29000L,
    val maxDiscountCap: Long = 0L, // 0 = tanpa batas maksimum
    val quantityTiers: List<PromoQuantityTier> = listOf(
        PromoQuantityTier(2, PromoBenefitType.DISCOUNT_NOMINAL, 2000L),
        PromoQuantityTier(3, PromoBenefitType.DISCOUNT_NOMINAL, 4000L)
    ),
    // Syarat Minimum
    val minItemQty: Int = 1,
    val minPurchaseAmount: Long = 0L,
    // Jadwal & Happy Hour
    val startDateKey: String = "", // yyyy-MM-dd (kosong = langsung berlaku)
    val endDateKey: String = "",   // yyyy-MM-dd (kosong = tanpa batas akhir)
    val activeDaysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7), // 1=Senin .. 7=Minggu
    val isHappyHourEnabled: Boolean = false,
    val startTimeText: String = "00:00", // HH:mm
    val endTimeText: String = "23:59",   // HH:mm
    // Cabang Target
    val applyToAllBranches: Boolean = true,
    val targetBranches: List<String> = listOf("Global"),
    // Kuota & Batas Penggunaan
    val hasQuotaLimit: Boolean = false,
    val maxTotalQuota: Int = 100,
    val usedCount: Int = 0,
    val maxUsagePerTransaction: Int = 0, // 0 = otomatis kelipatan tak terbatas dalam 1 transaksi
    // Pengaturan Tambahan
    val isAutoApply: Boolean = true,
    val voucherCode: String = "",
    val priority: Int = 10,
    val isStackable: Boolean = false
) {
    val benefitValue: Long
        get() = if (promoType == PromoType.BUNDLE && benefitType == PromoBenefitType.FIXED_PACKAGE_PRICE) {
            fixedPackagePrice
        } else {
            discountValue
        }

    val minProductQty: Int
        get() = minItemQty

    val happyHourEnabled: Boolean
        get() = isHappyHourEnabled

    val maxApplyPerTransaction: Int
        get() = maxUsagePerTransaction

    fun toJsonString(sortOrder: Int = 999999): String = toMetadataJson(
        existingMetaJson = JSONObject().put("sortOrder", sortOrder).toString()
    )

    fun toMetadataJson(existingMetaJson: String = "{}"): String {
        val obj = try {
            if (existingMetaJson.isNotBlank()) JSONObject(existingMetaJson) else JSONObject()
        } catch (_: Throwable) {
            JSONObject()
        }
        obj.put("promoSchemaV2", true)
        obj.put("description", description)
        obj.put("promoType", promoType.code)
        obj.put("isDraft", isDraft)
        obj.put("isArchived", isArchived)
        obj.put("applyToAllProducts", applyToAllProducts)
        obj.put("targetCategories", JSONArray(targetCategories))
        obj.put("targetProductNames", JSONArray(targetProductNames))
        obj.put("excludedProductNames", JSONArray(excludedProductNames))
        obj.put("includeModifiersInDiscount", includeModifiersInDiscount)

        val bundleArr = JSONArray()
        bundleGroups.forEach { bundleArr.put(it.toJson()) }
        obj.put("bundleGroups", bundleArr)
        obj.put("allowShareItemsWithOtherPromos", allowShareItemsWithOtherPromos)

        obj.put("benefitType", benefitType.code)
        obj.put("benefitValue", benefitValue)
        obj.put("discountValue", discountValue)
        obj.put("fixedPackagePrice", fixedPackagePrice)
        obj.put("maxDiscountCap", maxDiscountCap)

        val tiersArr = JSONArray()
        quantityTiers.sortedBy { it.minQty }.forEach { tiersArr.put(it.toJson()) }
        obj.put("quantityTiers", tiersArr)

        obj.put("minItemQty", minItemQty)
        obj.put("minProductQty", minItemQty)
        obj.put("minPurchaseAmount", minPurchaseAmount)
        obj.put("minOrder", minPurchaseAmount) // backward compatibility
        obj.put("maxCap", maxDiscountCap)      // backward compatibility

        obj.put("startDateKey", startDateKey)
        obj.put("endDateKey", endDateKey)
        obj.put("activeDaysOfWeek", JSONArray(activeDaysOfWeek.toList()))
        obj.put("isHappyHourEnabled", isHappyHourEnabled)
        obj.put("happyHourEnabled", isHappyHourEnabled)
        obj.put("startTimeText", startTimeText)
        obj.put("endTimeText", endTimeText)

        obj.put("applyToAllBranches", applyToAllBranches)
        obj.put("targetBranches", JSONArray(targetBranches))

        obj.put("hasQuotaLimit", hasQuotaLimit)
        obj.put("maxTotalQuota", maxTotalQuota)
        obj.put("usedCount", usedCount)
        obj.put("maxUsagePerTransaction", maxUsagePerTransaction)
        obj.put("maxApplyPerTransaction", maxUsagePerTransaction)

        obj.put("isAutoApply", isAutoApply)
        obj.put("voucherCode", voucherCode.trim().uppercase())
        obj.put("priority", priority)
        obj.put("isStackable", isStackable)
        return obj.toString()
    }

    companion object {
        fun fromCatalogRecord(record: CatalogRecord): PromoConfig {
            val raw = record.metadataJson
            val obj = try {
                if (raw.isNotBlank()) JSONObject(raw) else JSONObject()
            } catch (_: Throwable) {
                JSONObject()
            }

            if (!obj.optBoolean("promoSchemaV2", false)) {
                val legacyType = PromoType.fromCode(record.category)
                val minOrd = obj.optLong("minOrder", if (record.category == "NOMINAL_MIN") 80000L else 40000L)
                val maxCap = obj.optLong("maxCap", 15000L)
                val bType = when (record.category) {
                    "PERCENT", "PERCENT_CAP" -> PromoBenefitType.DISCOUNT_PERCENT
                    else -> PromoBenefitType.DISCOUNT_NOMINAL
                }
                return PromoConfig(
                    description = obj.optString("description", ""),
                    promoType = legacyType,
                    applyToAllProducts = record.category in listOf("NOMINAL_MIN", "PERCENT_CAP"),
                    targetCategories = if (record.category == "PERCENT") listOf("Egg Waffle") else listOf("Egg Waffle", "Drinks"),
                    benefitType = bType,
                    discountValue = record.priceWalkIn,
                    fixedPackagePrice = record.priceWalkIn,
                    maxDiscountCap = if (record.category == "PERCENT_CAP") maxCap else 0L,
                    quantityTiers = if (record.category == "PERCENT") {
                        listOf(PromoQuantityTier(2, PromoBenefitType.DISCOUNT_PERCENT, record.priceWalkIn))
                    } else emptyList(),
                    minItemQty = if (record.category == "PERCENT") 2 else 1,
                    minPurchaseAmount = if (record.category in listOf("NOMINAL_MIN", "PERCENT_CAP")) minOrd else 0L,
                    applyToAllBranches = true,
                    targetBranches = listOf(record.branchId)
                )
            }

            fun JSONArray?.toStringList(): List<String> {
                if (this == null) return emptyList()
                return (0 until length()).mapNotNull { optString(it).takeIf { s -> s.isNotBlank() } }
            }

            fun JSONArray?.toIntSet(): Set<Int> {
                if (this == null) return setOf(1, 2, 3, 4, 5, 6, 7)
                val set = (0 until length()).mapNotNull {
                    val v = optInt(it, -1)
                    if (v in 1..7) v else null
                }.toSet()
                return set.ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) }
            }

            val bundleArr = obj.optJSONArray("bundleGroups")
            val bundles = if (bundleArr != null && bundleArr.length() > 0) {
                (0 until bundleArr.length()).mapNotNull { idx ->
                    bundleArr.optJSONObject(idx)?.let { PromoBundleGroupRule.fromJson(it) }
                }
            } else {
                listOf(
                    PromoBundleGroupRule("Waffle Pilihan", 1, listOf("Egg Waffle")),
                    PromoBundleGroupRule("Minuman Pilihan", 1, listOf("Drinks"))
                )
            }

            val tiersArr = obj.optJSONArray("quantityTiers")
            val tiers = if (tiersArr != null && tiersArr.length() > 0) {
                (0 until tiersArr.length()).mapNotNull { idx ->
                    tiersArr.optJSONObject(idx)?.let { PromoQuantityTier.fromJson(it) }
                }
            } else {
                emptyList()
            }

            val bVal = obj.optLong("benefitValue", record.priceWalkIn)
            return PromoConfig(
                description = obj.optString("description", ""),
                promoType = PromoType.fromCode(obj.optString("promoType", record.category)),
                isDraft = obj.optBoolean("isDraft", false),
                isArchived = obj.optBoolean("isArchived", false),
                applyToAllProducts = obj.optBoolean("applyToAllProducts", false),
                targetCategories = obj.optJSONArray("targetCategories").toStringList(),
                targetProductNames = obj.optJSONArray("targetProductNames").toStringList(),
                excludedProductNames = obj.optJSONArray("excludedProductNames").toStringList(),
                includeModifiersInDiscount = obj.optBoolean("includeModifiersInDiscount", false),
                bundleGroups = bundles,
                allowShareItemsWithOtherPromos = obj.optBoolean("allowShareItemsWithOtherPromos", false),
                benefitType = PromoBenefitType.fromCode(obj.optString("benefitType", "DISCOUNT_NOMINAL")),
                discountValue = obj.optLong("discountValue", bVal),
                fixedPackagePrice = obj.optLong("fixedPackagePrice", bVal),
                maxDiscountCap = obj.optLong("maxDiscountCap", 0L),
                quantityTiers = tiers,
                minItemQty = obj.optInt("minItemQty", obj.optInt("minProductQty", 1)).coerceAtLeast(1),
                minPurchaseAmount = obj.optLong("minPurchaseAmount", 0L).coerceAtLeast(0L),
                startDateKey = obj.optString("startDateKey", ""),
                endDateKey = obj.optString("endDateKey", ""),
                activeDaysOfWeek = obj.optJSONArray("activeDaysOfWeek").toIntSet(),
                isHappyHourEnabled = obj.optBoolean("isHappyHourEnabled", obj.optBoolean("happyHourEnabled", false)),
                startTimeText = obj.optString("startTimeText", "00:00").ifBlank { "00:00" },
                endTimeText = obj.optString("endTimeText", "23:59").ifBlank { "23:59" },
                applyToAllBranches = obj.optBoolean("applyToAllBranches", true),
                targetBranches = obj.optJSONArray("targetBranches").toStringList().ifEmpty { listOf(record.branchId) },
                hasQuotaLimit = obj.optBoolean("hasQuotaLimit", false),
                maxTotalQuota = obj.optInt("maxTotalQuota", 100).coerceAtLeast(1),
                usedCount = obj.optInt("usedCount", 0).coerceAtLeast(0),
                maxUsagePerTransaction = obj.optInt("maxUsagePerTransaction", obj.optInt("maxApplyPerTransaction", 0)).coerceAtLeast(0),
                isAutoApply = obj.optBoolean("isAutoApply", true),
                voucherCode = obj.optString("voucherCode", ""),
                priority = obj.optInt("priority", 10),
                isStackable = obj.optBoolean("isStackable", false)
            )
        }
    }
}

data class AppliedPromoDetail(
    val promoId: String,
    val promoName: String,
    val promoType: PromoType,
    val discountAmount: Long,
    val packetsOrUnitsApplied: Int,
    val matchedItemNames: List<String>,
    val badgeSummary: String,
    val isAutoApplied: Boolean
) {
    val appliedPacketCount: Int
        get() = packetsOrUnitsApplied

    val matchedItemSummary: String
        get() = matchedItemNames.joinToString(", ")

    fun toJson(): JSONObject = JSONObject().apply {
        put("promoId", promoId)
        put("promoName", promoName)
        put("promoType", promoType.code)
        put("discountAmount", discountAmount)
        put("packetsOrUnitsApplied", packetsOrUnitsApplied)
        put("matchedItemNames", JSONArray(matchedItemNames))
        put("badgeSummary", badgeSummary)
        put("isAutoApplied", isAutoApplied)
    }

    companion object {
        fun fromJson(obj: JSONObject): AppliedPromoDetail {
            val namesArr = obj.optJSONArray("matchedItemNames") ?: JSONArray()
            val names = (0 until namesArr.length()).mapNotNull { namesArr.optString(it).takeIf { s -> s.isNotBlank() } }
            return AppliedPromoDetail(
                promoId = obj.optString("promoId", ""),
                promoName = obj.optString("promoName", ""),
                promoType = PromoType.fromCode(obj.optString("promoType", "BUNDLE")),
                discountAmount = obj.optLong("discountAmount", 0L),
                packetsOrUnitsApplied = obj.optInt("packetsOrUnitsApplied", 1),
                matchedItemNames = names,
                badgeSummary = obj.optString("badgeSummary", ""),
                isAutoApplied = obj.optBoolean("isAutoApplied", true)
            )
        }
    }
}

data class CartPromoEvaluationResult(
    val appliedPromos: List<AppliedPromoDetail> = emptyList(),
    val eligibleAlternativePromos: List<AppliedPromoDetail> = emptyList(),
    val subtotalBeforeDiscount: Long = 0L,
    val totalDiscountAmount: Long = 0L,
    val finalTotalAmount: Long = 0L,
    val statusNote: String = ""
) {
    val hasActivePromo: Boolean
        get() = totalDiscountAmount > 0L && appliedPromos.isNotEmpty()

    val primaryPromoName: String
        get() = appliedPromos.joinToString(" + ") { it.promoName }

    fun toSnapshotJson(): String {
        val obj = JSONObject()
        obj.put("subtotalBeforeDiscount", subtotalBeforeDiscount)
        obj.put("totalDiscountAmount", totalDiscountAmount)
        obj.put("finalTotalAmount", finalTotalAmount)
        val arr = JSONArray()
        appliedPromos.forEach { arr.put(it.toJson()) }
        obj.put("appliedPromos", arr)
        return obj.toString()
    }
}

data class PromoPerformanceStat(
    val promoId: String,
    val promoName: String,
    val promoTypeLabel: String,
    val status: PromoStatus,
    val usageCount: Int,
    val transactionCount: Int,
    val totalDiscountGiven: Long,
    val totalNetRevenue: Long,
    val topProductsSummary: String
)

object WaffleoPromoEngine {

    /**
     * Evaluates the real-time dynamic status of a Promo based on its active flag, draft flag,
     * quota usage, start/end dates, day of week, and Happy Hour time window.
     */
    fun resolvePromoStatus(
        record: CatalogRecord,
        config: PromoConfig = PromoConfig.fromCatalogRecord(record),
        dynamicUsedCount: Int? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): PromoStatus {
        if (config.isDraft) return PromoStatus.DRAFT
        if (!record.isActive || config.isArchived) return PromoStatus.PAUSED

        val effectiveUsed = dynamicUsedCount ?: config.usedCount
        if (config.hasQuotaLimit && effectiveUsed >= config.maxTotalQuota) {
            return PromoStatus.EXHAUSTED
        }

        val nowDate = Date(nowMillis)
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayKey = dateFmt.format(nowDate)

        if (config.startDateKey.isNotBlank() && todayKey < config.startDateKey) {
            return PromoStatus.SCHEDULED
        }
        if (config.endDateKey.isNotBlank() && todayKey > config.endDateKey) {
            return PromoStatus.EXPIRED
        }

        val cal = Calendar.getInstance().apply { time = nowDate }
        // Convert Calendar.SUNDAY(1)..SATURDAY(7) to ISO 1(Mon)..7(Sun)
        val rawDow = cal.get(Calendar.DAY_OF_WEEK)
        val isoDow = if (rawDow == Calendar.SUNDAY) 7 else rawDow - 1
        if (config.activeDaysOfWeek.isNotEmpty() && isoDow !in config.activeDaysOfWeek) {
            return PromoStatus.SCHEDULED
        }

        if (config.happyHourEnabled) {
            val timeFmt = SimpleDateFormat("HH:mm", Locale.US)
            val nowTime = timeFmt.format(nowDate).replace('.', ':')
            val startT = config.startTimeText.replace('.', ':')
            val endT = config.endTimeText.replace('.', ':')
            val inTimeWindow = if (startT <= endT) {
                nowTime in startT..endT
            } else {
                // Overnight window (e.g. 21:00 - 02:00)
                nowTime >= startT || nowTime <= endT
            }
            if (!inTimeWindow) {
                return PromoStatus.SCHEDULED
            }
        }

        return PromoStatus.ACTIVE
    }

    fun isPromoApplicableToBranch(record: CatalogRecord, config: PromoConfig, activeBranchId: String): Boolean {
        if (config.applyToAllBranches) return true
        if (record.branchId.equals(activeBranchId, ignoreCase = true)) return true
        return config.targetBranches.any { it.equals(activeBranchId, ignoreCase = true) }
    }

    fun formatBenefitSummary(config: PromoConfig): String {
        val valIdr = "Rp %,d".format(Locale.forLanguageTag("id-ID"), config.benefitValue)
        return when (config.promoType) {
            PromoType.BUNDLE -> when (config.benefitType) {
                PromoBenefitType.FIXED_PACKAGE_PRICE -> "Harga Paket $valIdr"
                PromoBenefitType.DISCOUNT_NOMINAL -> "Hemat $valIdr / Paket"
                PromoBenefitType.DISCOUNT_PERCENT -> "Diskon Paket ${config.benefitValue}%"
            }
            PromoType.QUANTITY_DISCOUNT -> {
                val topTier = config.quantityTiers.minByOrNull { it.minQty }
                if (topTier != null) {
                    val tVal = if (topTier.benefitType == PromoBenefitType.DISCOUNT_PERCENT) {
                        "${topTier.discountValue}%"
                    } else {
                        "Rp %,d".format(Locale.forLanguageTag("id-ID"), topTier.discountValue)
                    }
                    "Beli ${topTier.minQty}+ Diskon $tVal"
                } else {
                    "Diskon Kuantitas"
                }
            }
            PromoType.PRODUCT_DISCOUNT -> when (config.benefitType) {
                PromoBenefitType.DISCOUNT_PERCENT -> {
                    val capTxt = if (config.maxDiscountCap > 0) " (Maks Rp %,d)".format(Locale.forLanguageTag("id-ID"), config.maxDiscountCap) else ""
                    "Diskon ${config.benefitValue}%$capTxt"
                }
                else -> "Potongan $valIdr / Item"
            }
            PromoType.MINIMUM_PURCHASE -> {
                val minTxt = "Rp %,d".format(Locale.forLanguageTag("id-ID"), config.minPurchaseAmount)
                when (config.benefitType) {
                    PromoBenefitType.DISCOUNT_PERCENT -> "Min. $minTxt Diskon ${config.benefitValue}%"
                    else -> "Min. $minTxt Potongan $valIdr"
                }
            }
        }
    }

    fun formatBenefitSummary(record: CatalogRecord, config: PromoConfig = PromoConfig.fromCatalogRecord(record)): String =
        formatBenefitSummary(config)

    /**
     * Flatten cart lines into individual purchasable units so that bundle matching and quantity
     * calculation work strictly at the unit level and prevent double-counting items across non-stackable promos.
     */
    private data class ExpandableCartUnit(
        val unitIndex: Int,
        val lineId: String,
        val category: String,
        val primaryItemId: String,
        val primaryItemName: String,
        val baseMenuUnitPrice: Long,
        val fullUnitPriceWithModifiers: Long
    ) {
        fun effectivePrice(includeModifiers: Boolean): Long =
            if (includeModifiers) fullUnitPriceWithModifiers else baseMenuUnitPrice
    }

    private fun expandCartToUnits(
        cartItems: List<CartLineItem>,
        media: OrderMedia,
        catalog: List<CatalogRecord>
    ): List<ExpandableCartUnit> {
        val units = mutableListOf<ExpandableCartUnit>()
        var idx = 0
        for (line in cartItems) {
            val fullUnit = line.unitPriceFor(media)
            val catalogMenu = catalog.firstOrNull {
                it.recordType == "MENU_ITEM" &&
                    (it.id == line.primaryItemId || it.name.equals(line.primaryItemName, ignoreCase = true))
            }
            val basePrice = if (catalogMenu != null) {
                val p1 = catalogMenu.priceForMedia(media)
                if (line.isHalfHalf && line.secondaryItemName.isNotBlank()) {
                    val cat2 = catalog.firstOrNull {
                        it.recordType == "MENU_ITEM" &&
                            (it.id == line.secondaryItemId || it.name.equals(line.secondaryItemName, ignoreCase = true))
                    }
                    val p2 = cat2?.priceForMedia(media) ?: p1
                    ((p1 + p2) / 2.0).roundToLong()
                } else if (line.isDoubleTopping && line.secondaryItemName.isNotBlank()) {
                    val cat2 = catalog.firstOrNull {
                        it.recordType == "MENU_ITEM" &&
                            (it.id == line.secondaryItemId || it.name.equals(line.secondaryItemName, ignoreCase = true))
                    }
                    p1 + (cat2?.priceForMedia(media) ?: 0L)
                } else {
                    p1
                }
            } else {
                fullUnit
            }.coerceAtMost(fullUnit).coerceAtLeast(0L)

            repeat(line.qty.coerceAtLeast(0)) {
                units.add(
                    ExpandableCartUnit(
                        unitIndex = idx++,
                        lineId = line.lineId,
                        category = line.category,
                        primaryItemId = line.primaryItemId,
                        primaryItemName = line.primaryItemName,
                        baseMenuUnitPrice = if (basePrice > 0L) basePrice else fullUnit,
                        fullUnitPriceWithModifiers = fullUnit
                    )
                )
            }
        }
        return units
    }

    private fun unitMatchesTarget(
        unit: ExpandableCartUnit,
        applyToAll: Boolean,
        targetCategories: List<String>,
        targetProductNames: List<String>,
        excludedProductNames: List<String>
    ): Boolean {
        if (excludedProductNames.any { it.equals(unit.primaryItemName, ignoreCase = true) }) {
            return false
        }
        if (applyToAll) return true
        val nameMatches = targetProductNames.any { it.equals(unit.primaryItemName, ignoreCase = true) }
        if (targetProductNames.isNotEmpty() && nameMatches) return true
        if (targetProductNames.isNotEmpty() && targetCategories.isEmpty()) return false
        val catMatches = targetCategories.any { areCategoriesMatching(it, unit.category) }
        return if (targetProductNames.isEmpty()) {
            catMatches
        } else {
            nameMatches || catMatches
        }
    }

    /**
     * Evaluates a single promo against available (unconsumed) cart units.
     * Returns Pair(AppliedPromoDetail, Set<Int> of consumed unitIndices) if eligible and discount > 0.
     */
    private fun evaluateSinglePromoOnUnits(
        record: CatalogRecord,
        config: PromoConfig,
        availableUnits: List<ExpandableCartUnit>,
        allCartUnits: List<ExpandableCartUnit>,
        remainingQuota: Int?
    ): Pair<AppliedPromoDetail, Set<Int>>? {
        if (availableUnits.isEmpty()) return null

        val maxLimitFromTx = if (config.maxApplyPerTransaction > 0) config.maxApplyPerTransaction else Int.MAX_VALUE
        val maxLimitFromQuota = remainingQuota ?: Int.MAX_VALUE
        val effectiveMaxApplications = minOf(maxLimitFromTx, maxLimitFromQuota)
        if (effectiveMaxApplications <= 0) return null

        val eligibleUnits = availableUnits.filter {
            unitMatchesTarget(
                unit = it,
                applyToAll = config.applyToAllProducts,
                targetCategories = config.targetCategories,
                targetProductNames = config.targetProductNames,
                excludedProductNames = config.excludedProductNames
            )
        }

        val eligibleSubtotal = eligibleUnits.sumOf { it.effectivePrice(config.includeModifiersInDiscount) }
        if (config.minPurchaseAmount > 0L && eligibleSubtotal < config.minPurchaseAmount) {
            return null
        }
        if (config.minProductQty > 1 && eligibleUnits.size < config.minProductQty) {
            return null
        }

        return when (config.promoType) {
            PromoType.BUNDLE -> {
                val groups = config.bundleGroups.ifEmpty { return null }
                val usedUnitIds = mutableSetOf<Int>()
                val matchedNames = mutableListOf<String>()
                var packetsCount = 0
                var totalBundleDiscount = 0L

                while (packetsCount < effectiveMaxApplications) {
                    val packetUnits = mutableListOf<ExpandableCartUnit>()
                    var canFormPacket = true

                    for (group in groups) {
                        val candidates = availableUnits.filter { u ->
                            u.unitIndex !in usedUnitIds &&
                                packetUnits.none { it.unitIndex == u.unitIndex } &&
                                u.primaryItemName !in config.excludedProductNames &&
                                (group.targetCategories.isEmpty() || group.targetCategories.any { c -> areCategoriesMatching(c, u.category) }) &&
                                (group.targetProductNames.isEmpty() || group.targetProductNames.any { p -> p.equals(u.primaryItemName, ignoreCase = true) })
                        }.sortedByDescending { it.effectivePrice(config.includeModifiersInDiscount) }

                        if (candidates.size < group.requiredQty) {
                            canFormPacket = false
                            break
                        }
                        packetUnits.addAll(candidates.take(group.requiredQty))
                    }

                    if (!canFormPacket || packetUnits.isEmpty()) break

                    val packetNormalPrice = packetUnits.sumOf { it.effectivePrice(config.includeModifiersInDiscount) }
                    val packetDiscount: Long = when (config.benefitType) {
                        PromoBenefitType.FIXED_PACKAGE_PRICE -> (packetNormalPrice - config.benefitValue).coerceAtLeast(0L)
                        PromoBenefitType.DISCOUNT_NOMINAL -> config.benefitValue.coerceAtMost(packetNormalPrice)
                        PromoBenefitType.DISCOUNT_PERCENT -> {
                            val raw = ((packetNormalPrice * config.benefitValue) / 100.0).roundToLong()
                            val capped = if (config.maxDiscountCap > 0L) minOf(raw, config.maxDiscountCap) else raw
                            capped.coerceAtMost(packetNormalPrice)
                        }
                    }

                    if (packetDiscount <= 0L) break

                    packetUnits.forEach { u ->
                        usedUnitIds.add(u.unitIndex)
                        matchedNames.add(u.primaryItemName)
                    }
                    totalBundleDiscount += packetDiscount
                    packetsCount++
                }

                if (packetsCount > 0 && totalBundleDiscount > 0L) {
                    val capDiscount = if (config.maxDiscountCap > 0L) minOf(totalBundleDiscount, config.maxDiscountCap) else totalBundleDiscount
                    AppliedPromoDetail(
                        promoId = record.id,
                        promoName = record.name,
                        promoType = PromoType.BUNDLE,
                        discountAmount = capDiscount,
                        packetsOrUnitsApplied = packetsCount,
                        matchedItemNames = matchedNames.distinct(),
                        badgeSummary = "${packetsCount}x Paket Kombo",
                        isAutoApplied = config.isAutoApply
                    ) to usedUnitIds
                } else null
            }

            PromoType.QUANTITY_DISCOUNT -> {
                if (eligibleUnits.isEmpty()) return null
                val totalQty = eligibleUnits.size
                val sortedTiers = config.quantityTiers.sortedByDescending { it.minQty }
                val matchedTier = sortedTiers.firstOrNull { totalQty >= it.minQty }
                val discountAmount: Long = if (matchedTier != null) {
                    when (matchedTier.benefitType) {
                        PromoBenefitType.DISCOUNT_PERCENT -> ((eligibleSubtotal * matchedTier.discountValue) / 100.0).roundToLong()
                        else -> matchedTier.discountValue
                    }
                } else if (config.quantityTiers.isEmpty() && totalQty >= config.minProductQty) {
                    when (config.benefitType) {
                        PromoBenefitType.DISCOUNT_PERCENT -> ((eligibleSubtotal * config.benefitValue) / 100.0).roundToLong()
                        else -> config.benefitValue
                    }
                } else 0L

                val capped = (if (config.maxDiscountCap > 0L) minOf(discountAmount, config.maxDiscountCap) else discountAmount)
                    .coerceAtMost(eligibleSubtotal)
                    .coerceAtLeast(0L)

                if (capped > 0L) {
                    AppliedPromoDetail(
                        promoId = record.id,
                        promoName = record.name,
                        promoType = PromoType.QUANTITY_DISCOUNT,
                        discountAmount = capped,
                        packetsOrUnitsApplied = totalQty,
                        matchedItemNames = eligibleUnits.map { it.primaryItemName }.distinct(),
                        badgeSummary = "Beli $totalQty unit",
                        isAutoApplied = config.isAutoApply
                    ) to eligibleUnits.map { it.unitIndex }.toSet()
                } else null
            }

            PromoType.PRODUCT_DISCOUNT -> {
                if (eligibleUnits.isEmpty()) return null
                val limitedUnits = eligibleUnits
                    .sortedByDescending { it.effectivePrice(config.includeModifiersInDiscount) }
                    .take(effectiveMaxApplications)
                val targetSub = limitedUnits.sumOf { it.effectivePrice(config.includeModifiersInDiscount) }
                val rawDisc = when (config.benefitType) {
                    PromoBenefitType.DISCOUNT_PERCENT -> ((targetSub * config.benefitValue) / 100.0).roundToLong()
                    else -> limitedUnits.sumOf { u ->
                        config.benefitValue.coerceAtMost(u.effectivePrice(config.includeModifiersInDiscount))
                    }
                }
                val capped = (if (config.maxDiscountCap > 0L) minOf(rawDisc, config.maxDiscountCap) else rawDisc)
                    .coerceAtMost(targetSub)
                    .coerceAtLeast(0L)

                if (capped > 0L) {
                    AppliedPromoDetail(
                        promoId = record.id,
                        promoName = record.name,
                        promoType = PromoType.PRODUCT_DISCOUNT,
                        discountAmount = capped,
                        packetsOrUnitsApplied = limitedUnits.size,
                        matchedItemNames = limitedUnits.map { it.primaryItemName }.distinct(),
                        badgeSummary = "${limitedUnits.size} produk diskon",
                        isAutoApplied = config.isAutoApply
                    ) to limitedUnits.map { it.unitIndex }.toSet()
                } else null
            }

            PromoType.MINIMUM_PURCHASE -> {
                val basePool = if (config.applyToAllProducts) availableUnits else eligibleUnits
                val poolSubtotal = basePool.sumOf { it.effectivePrice(config.includeModifiersInDiscount) }
                val minReq = config.minPurchaseAmount.coerceAtLeast(1L)
                if (poolSubtotal < minReq) return null

                val rawDisc = when (config.benefitType) {
                    PromoBenefitType.DISCOUNT_PERCENT -> ((poolSubtotal * config.benefitValue) / 100.0).roundToLong()
                    else -> config.benefitValue
                }
                val capped = (if (config.maxDiscountCap > 0L) minOf(rawDisc, config.maxDiscountCap) else rawDisc)
                    .coerceAtMost(poolSubtotal)
                    .coerceAtLeast(0L)

                if (capped > 0L) {
                    AppliedPromoDetail(
                        promoId = record.id,
                        promoName = record.name,
                        promoType = PromoType.MINIMUM_PURCHASE,
                        discountAmount = capped,
                        packetsOrUnitsApplied = 1,
                        matchedItemNames = basePool.map { it.primaryItemName }.distinct(),
                        badgeSummary = "Min. Belanja Rp %,d".format(Locale.forLanguageTag("id-ID"), minReq),
                        isAutoApplied = config.isAutoApply
                    ) to basePool.map { it.unitIndex }.toSet()
                } else null
            }
        }
    }

    /**
     * Master POS Promo Evaluation Engine:
     * - Checks branch, date, day of week, Happy Hour window, quota, and product/subtotal rules.
     * - Automatically applies the best promo (or stackable combination) when `selectedPromoId == "AUTO"`.
     * - Allows cashier to explicitly select a specific promo or `"NONE"` (Tanpa Promo), or enter a voucher code.
     */
    fun evaluateCartPromos(
        cartItems: List<CartLineItem>,
        media: OrderMedia,
        activeBranchId: String,
        catalog: List<CatalogRecord>,
        selectedPromoModeOrId: String = "AUTO", // "AUTO", "NONE", or specific promoId
        voucherCodeInput: String = "",
        dynamicUsageMap: Map<String, Int> = emptyMap(),
        evaluationTimeMillis: Long = System.currentTimeMillis()
    ): CartPromoEvaluationResult {
        val subtotal = cartItems.sumOf { it.lineTotalFor(media) }
        if (cartItems.isEmpty() || subtotal <= 0L) {
            return CartPromoEvaluationResult(subtotalBeforeDiscount = 0L, finalTotalAmount = 0L)
        }
        if (selectedPromoModeOrId.equals("NONE", ignoreCase = true)) {
            return CartPromoEvaluationResult(
                subtotalBeforeDiscount = subtotal,
                totalDiscountAmount = 0L,
                finalTotalAmount = subtotal,
                statusNote = "Promo dinonaktifkan manual oleh Kasir"
            )
        }

        val allUnits = expandCartToUnits(cartItems, media, catalog)
        val cleanVoucher = voucherCodeInput.trim().uppercase()

        // Filter promos that are ACTIVE right now and valid for activeBranchId
        val activePromoCandidates = catalog
            .filter { it.recordType == "PROMO" }
            .map { rec -> rec to PromoConfig.fromCatalogRecord(rec) }
            .filter { (rec, cfg) ->
                val used = maxOf(cfg.usedCount, dynamicUsageMap[rec.id] ?: 0)
                val status = resolvePromoStatus(rec, cfg, used, evaluationTimeMillis)
                status == PromoStatus.ACTIVE && isPromoApplicableToBranch(rec, cfg, activeBranchId)
            }

        if (activePromoCandidates.isEmpty()) {
            return CartPromoEvaluationResult(
                subtotalBeforeDiscount = subtotal,
                totalDiscountAmount = 0L,
                finalTotalAmount = subtotal
            )
        }

        // Evaluate all candidates individually to know all eligible alternatives
        val individualEvaluations = activePromoCandidates.mapNotNull { (rec, cfg) ->
            val matchesVoucher = cleanVoucher.isNotBlank() && cfg.voucherCode.equals(cleanVoucher, ignoreCase = true)
            val explicitlySelected = selectedPromoModeOrId == rec.id
            if (!cfg.isAutoApply && !matchesVoucher && !explicitlySelected) {
                return@mapNotNull null
            }
            val used = maxOf(cfg.usedCount, dynamicUsageMap[rec.id] ?: 0)
            val remQuota = if (cfg.hasQuotaLimit) (cfg.maxTotalQuota - used).coerceAtLeast(0) else null
            val res = evaluateSinglePromoOnUnits(rec, cfg, allUnits, allUnits, remQuota) ?: return@mapNotNull null
            Triple(rec, cfg, res)
        }.sortedWith(
            compareByDescending<Triple<CatalogRecord, PromoConfig, Pair<AppliedPromoDetail, Set<Int>>>> { it.third.first.discountAmount }
                .thenByDescending { it.second.priority }
        )

        if (individualEvaluations.isEmpty()) {
            return CartPromoEvaluationResult(
                subtotalBeforeDiscount = subtotal,
                totalDiscountAmount = 0L,
                finalTotalAmount = subtotal
            )
        }

        val allEligibleDetails = individualEvaluations.map { it.third.first }

        // If cashier explicitly chose a specific promo ID, apply that promo (or return 0 if not met)
        if (selectedPromoModeOrId.isNotBlank() && !selectedPromoModeOrId.equals("AUTO", ignoreCase = true)) {
            val chosen = individualEvaluations.firstOrNull {
                it.first.id == selectedPromoModeOrId || it.first.name.equals(selectedPromoModeOrId, ignoreCase = true)
            }
            return if (chosen != null) {
                val discount = chosen.third.first.discountAmount.coerceAtMost(subtotal)
                CartPromoEvaluationResult(
                    appliedPromos = listOf(chosen.third.first.copy(discountAmount = discount)),
                    eligibleAlternativePromos = allEligibleDetails,
                    subtotalBeforeDiscount = subtotal,
                    totalDiscountAmount = discount,
                    finalTotalAmount = (subtotal - discount).coerceAtLeast(0L)
                )
            } else {
                CartPromoEvaluationResult(
                    appliedPromos = emptyList(),
                    eligibleAlternativePromos = allEligibleDetails,
                    subtotalBeforeDiscount = subtotal,
                    totalDiscountAmount = 0L,
                    finalTotalAmount = subtotal,
                    statusNote = "Syarat promo yang dipilih belum terpenuhi"
                )
            }
        }

        // AUTO MODE: Compare best non-stackable promo vs combined stackable promos
        val bestSingle = individualEvaluations.first()

        val stackableCandidates = individualEvaluations.filter { it.second.isStackable }
        val stackedApplied = mutableListOf<AppliedPromoDetail>()
        val consumedUnitIndices = mutableSetOf<Int>()

        for ((rec, cfg, _) in stackableCandidates) {
            val remainingPool = if (cfg.allowShareItemsWithOtherPromos) {
                allUnits
            } else {
                allUnits.filter { it.unitIndex !in consumedUnitIndices }
            }
            val used = maxOf(cfg.usedCount, dynamicUsageMap[rec.id] ?: 0)
            val remQuota = if (cfg.hasQuotaLimit) (cfg.maxTotalQuota - used).coerceAtLeast(0) else null
            val eval = evaluateSinglePromoOnUnits(rec, cfg, remainingPool, allUnits, remQuota)
            if (eval != null && eval.first.discountAmount > 0L) {
                stackedApplied.add(eval.first)
                if (!cfg.allowShareItemsWithOtherPromos) {
                    consumedUnitIndices.addAll(eval.second)
                }
            }
        }

        val stackedTotalDiscount = stackedApplied.sumOf { it.discountAmount }.coerceAtMost(subtotal)
        val singleTotalDiscount = bestSingle.third.first.discountAmount.coerceAtMost(subtotal)

        return if (stackedApplied.size > 1 && stackedTotalDiscount > singleTotalDiscount) {
            CartPromoEvaluationResult(
                appliedPromos = stackedApplied,
                eligibleAlternativePromos = allEligibleDetails,
                subtotalBeforeDiscount = subtotal,
                totalDiscountAmount = stackedTotalDiscount,
                finalTotalAmount = (subtotal - stackedTotalDiscount).coerceAtLeast(0L)
            )
        } else {
            CartPromoEvaluationResult(
                appliedPromos = listOf(bestSingle.third.first.copy(discountAmount = singleTotalDiscount)),
                eligibleAlternativePromos = allEligibleDetails,
                subtotalBeforeDiscount = subtotal,
                totalDiscountAmount = singleTotalDiscount,
                finalTotalAmount = (subtotal - singleTotalDiscount).coerceAtLeast(0L)
            )
        }
    }
}
