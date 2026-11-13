package com.example

import com.example.data.model.CartLineItem
import com.example.data.model.CatalogRecord
import com.example.data.model.OrderMedia
import com.example.data.model.PromoBenefitType
import com.example.data.model.PromoBundleGroupRule
import com.example.data.model.PromoConfig
import com.example.data.model.PromoQuantityTier
import com.example.data.model.PromoStatus
import com.example.data.model.PromoType
import com.example.data.model.WaffleoPromoEngine
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PromoEngineTest {

    private val waffleTiramisu = CatalogRecord(
        id = "waffle_tiramisu",
        branchId = "Global",
        recordType = "MENU_ITEM",
        category = "Egg Waffle",
        name = "Tiramisu",
        priceWalkIn = 18000L,
        isActive = true
    )

    private val waffleChoco = CatalogRecord(
        id = "waffle_choco",
        branchId = "Global",
        recordType = "MENU_ITEM",
        category = "Egg Waffle",
        name = "Chocolate",
        priceWalkIn = 18000L,
        isActive = true
    )

    private val drinkMatcha = CatalogRecord(
        id = "drink_matcha",
        branchId = "Global",
        recordType = "MENU_ITEM",
        category = "Drinks",
        name = "Matcha Latte",
        priceWalkIn = 15000L,
        isActive = true
    )

    private val baseCatalog = listOf(waffleTiramisu, waffleChoco, drinkMatcha)

    private fun buildPromoRecord(
        id: String,
        name: String,
        config: PromoConfig,
        branchId: String = "Global",
        isActive: Boolean = true
    ): CatalogRecord {
        return CatalogRecord(
            id = id,
            branchId = branchId,
            recordType = "PROMO",
            category = config.promoType.code,
            name = name,
            priceWalkIn = if (config.benefitType == PromoBenefitType.FIXED_PACKAGE_PRICE) config.fixedPackagePrice else config.discountValue,
            isActive = isActive,
            metadataJson = config.toJsonString(1)
        )
    }

    private fun fixedTimeMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun scenario01_validBundle_appliesFixedPackagePriceCorrectly() {
        // 1 Waffle (Rp18.000) + 1 Minuman (Rp15.000) = Normal Rp33.000 -> Harga Paket Rp29.000 -> Diskon Rp4.000
        val bundleCfg = PromoConfig(
            promoType = PromoType.BUNDLE,
            benefitType = PromoBenefitType.FIXED_PACKAGE_PRICE,
            fixedPackagePrice = 29000L,
            bundleGroups = listOf(
                PromoBundleGroupRule("Waffle", 1, listOf("Egg Waffle")),
                PromoBundleGroupRule("Minuman", 1, listOf("Drinks"))
            )
        )
        val promoRec = buildPromoRecord("promo_bundle_29k", "Paket Kombo 29rb", bundleCfg)
        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L),
            CartLineItem(lineId = "2", category = "Drinks", primaryItemId = drinkMatcha.id, primaryItemName = "Matcha Latte", qty = 1, unitPriceWalkIn = 15000L)
        )

        val result = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec
        )

        assertEquals(33000L, result.subtotalBeforeDiscount)
        assertEquals(4000L, result.totalDiscountAmount)
        assertEquals(29000L, result.finalTotalAmount)
        assertEquals("Paket Kombo 29rb", result.primaryPromoName)
    }

    @Test
    fun scenario02_incompleteBundle_doesNotApplyDiscount() {
        val bundleCfg = PromoConfig(
            promoType = PromoType.BUNDLE,
            benefitType = PromoBenefitType.FIXED_PACKAGE_PRICE,
            fixedPackagePrice = 29000L,
            bundleGroups = listOf(
                PromoBundleGroupRule("Waffle", 1, listOf("Egg Waffle")),
                PromoBundleGroupRule("Minuman", 1, listOf("Drinks"))
            )
        )
        val promoRec = buildPromoRecord("promo_bundle_29k", "Paket Kombo 29rb", bundleCfg)
        val cartOnlyWaffle = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L)
        )

        val result = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cartOnlyWaffle,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec
        )

        assertEquals(0L, result.totalDiscountAmount)
        assertEquals(18000L, result.finalTotalAmount)
    }

    @Test
    fun scenario03_quantityDiscount_belowMinimum_returnsZeroDiscount() {
        val qtyCfg = PromoConfig(
            promoType = PromoType.QUANTITY_DISCOUNT,
            applyToAllProducts = false,
            targetCategories = listOf("Egg Waffle"),
            quantityTiers = listOf(
                PromoQuantityTier(minQty = 2, benefitType = PromoBenefitType.DISCOUNT_NOMINAL, discountValue = 2000L),
                PromoQuantityTier(minQty = 3, benefitType = PromoBenefitType.DISCOUNT_NOMINAL, discountValue = 4000L)
            )
        )
        val promoRec = buildPromoRecord("promo_qty_waffle", "Beli Banyak Waffle Hemat", qtyCfg)
        val cart1Waffle = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L)
        )

        val result = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart1Waffle,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec
        )

        assertEquals(0L, result.totalDiscountAmount)
        assertEquals(18000L, result.finalTotalAmount)
    }

    @Test
    fun scenario04_quantityDiscount_meetsTiers_appliesCorrectTierDiscount() {
        val qtyCfg = PromoConfig(
            promoType = PromoType.QUANTITY_DISCOUNT,
            applyToAllProducts = false,
            targetCategories = listOf("Egg Waffle"),
            quantityTiers = listOf(
                PromoQuantityTier(minQty = 2, benefitType = PromoBenefitType.DISCOUNT_NOMINAL, discountValue = 2000L),
                PromoQuantityTier(minQty = 3, benefitType = PromoBenefitType.DISCOUNT_NOMINAL, discountValue = 4000L)
            )
        )
        val promoRec = buildPromoRecord("promo_qty_waffle", "Beli Banyak Waffle Hemat", qtyCfg)

        // Beli 2 Waffle -> Diskon Rp 2.000
        val cart2Waffles = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 2, unitPriceWalkIn = 18000L)
        )
        val res2 = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart2Waffles,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec
        )
        assertEquals(2000L, res2.totalDiscountAmount)
        assertEquals(34000L, res2.finalTotalAmount)

        // Beli 3 Waffle -> Diskon Rp 4.000
        val cart3Waffles = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 2, unitPriceWalkIn = 18000L),
            CartLineItem(lineId = "2", category = "Egg Waffle", primaryItemId = waffleChoco.id, primaryItemName = "Chocolate", qty = 1, unitPriceWalkIn = 18000L)
        )
        val res3 = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart3Waffles,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec
        )
        assertEquals(4000L, res3.totalDiscountAmount)
        assertEquals(50000L, res3.finalTotalAmount)
    }

    @Test
    fun scenario05_specificProductDiscount_onlyDiscountsTargetProductAndLeavesOthersNormal() {
        val prodCfg = PromoConfig(
            promoType = PromoType.PRODUCT_DISCOUNT,
            benefitType = PromoBenefitType.DISCOUNT_PERCENT,
            discountValue = 20L, // 20% off Tiramisu (18.000 * 20% = 3.600)
            applyToAllProducts = false,
            targetProductNames = listOf("Tiramisu"),
            includeModifiersInDiscount = false
        )
        val promoRec = buildPromoRecord("promo_tiramisu_20", "Diskon Tiramisu 20%", prodCfg)
        // Tiramisu with Rp3.000 add-on (unitPriceWalkIn = 21.000, base = 18.000) + Matcha Latte (15.000)
        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", addOnNames = listOf("Keju"), qty = 1, unitPriceWalkIn = 21000L),
            CartLineItem(lineId = "2", category = "Drinks", primaryItemId = drinkMatcha.id, primaryItemName = "Matcha Latte", qty = 1, unitPriceWalkIn = 15000L)
        )

        val result = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec
        )

        // 20% of base Rp18.000 = Rp3.600 (modifier Rp3.000 & Matcha Rp15.000 are not discounted)
        assertEquals(36000L, result.subtotalBeforeDiscount)
        assertEquals(3600L, result.totalDiscountAmount)
        assertEquals(32400L, result.finalTotalAmount)
    }

    @Test
    fun scenario06_promoOutsideActiveDateRange_isNotApplied() {
        val cfg = PromoConfig(
            promoType = PromoType.MINIMUM_PURCHASE,
            benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
            discountValue = 5000L,
            minPurchaseAmount = 10000L,
            startDateKey = "2026-12-01",
            endDateKey = "2026-12-31"
        )
        val promoRec = buildPromoRecord("promo_dec", "Promo Desember", cfg)
        val novTime = fixedTimeMillis(2026, 11, 15, 14, 0)
        val janTime = fixedTimeMillis(2027, 1, 5, 14, 0)

        assertEquals(PromoStatus.SCHEDULED, WaffleoPromoEngine.resolvePromoStatus(promoRec, cfg, 0, novTime))
        assertEquals(PromoStatus.EXPIRED, WaffleoPromoEngine.resolvePromoStatus(promoRec, cfg, 0, janTime))

        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L)
        )
        val evalNov = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec,
            evaluationTimeMillis = novTime
        )
        assertEquals(0L, evalNov.totalDiscountAmount)
    }

    @Test
    fun scenario07_promoOutsideHappyHourTimeWindow_isNotApplied() {
        val cfg = PromoConfig(
            promoType = PromoType.PRODUCT_DISCOUNT,
            benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
            discountValue = 3000L,
            applyToAllProducts = true,
            isHappyHourEnabled = true,
            startTimeText = "13:00",
            endTimeText = "16:00"
        )
        val promoRec = buildPromoRecord("promo_hh", "Happy Hour Siang", cfg)
        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L)
        )

        // At 11:00 (Outside Happy Hour) -> 0 discount
        val time11am = fixedTimeMillis(2026, 11, 6, 11, 0)
        val eval11am = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec,
            evaluationTimeMillis = time11am
        )
        assertEquals(0L, eval11am.totalDiscountAmount)

        // At 14:30 (Inside Happy Hour) -> Rp 3.000 discount
        val time230pm = fixedTimeMillis(2026, 11, 6, 14, 30)
        val eval230pm = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec,
            evaluationTimeMillis = time230pm
        )
        assertEquals(3000L, eval230pm.totalDiscountAmount)
    }

    @Test
    fun scenario08_promoForDifferentBranch_isNotApplied() {
        val cfg = PromoConfig(
            promoType = PromoType.MINIMUM_PURCHASE,
            benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
            discountValue = 5000L,
            minPurchaseAmount = 10000L,
            applyToAllBranches = false,
            targetBranches = listOf("Gabek")
        )
        val promoRec = buildPromoRecord("promo_gabek_only", "Spesial Cabang Gabek", cfg, branchId = "Gabek")
        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L)
        )

        val evalGlobal = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec
        )
        assertEquals(0L, evalGlobal.totalDiscountAmount)

        val evalGabek = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Gabek",
            catalog = baseCatalog + promoRec
        )
        assertEquals(5000L, evalGabek.totalDiscountAmount)
    }

    @Test
    fun scenario09_promoQuotaExhausted_statusIsExhaustedAndDiscountIsZero() {
        val cfg = PromoConfig(
            promoType = PromoType.MINIMUM_PURCHASE,
            benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
            discountValue = 5000L,
            minPurchaseAmount = 10000L,
            hasQuotaLimit = true,
            maxTotalQuota = 10,
            usedCount = 10
        )
        val promoRec = buildPromoRecord("promo_quota_10", "Promo Kuota 10", cfg)
        assertEquals(PromoStatus.EXHAUSTED, WaffleoPromoEngine.resolvePromoStatus(promoRec, cfg, 10))

        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L)
        )
        val eval = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec
        )
        assertEquals(0L, eval.totalDiscountAmount)
    }

    @Test
    fun scenario10_twoCompetingNonStackablePromos_selectsHighestDiscountOrHighestPriority() {
        val promoSmall = buildPromoRecord(
            "promo_small",
            "Diskon 2rb",
            PromoConfig(
                promoType = PromoType.MINIMUM_PURCHASE,
                benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
                discountValue = 2000L,
                minPurchaseAmount = 10000L,
                priority = 50,
                isStackable = false
            )
        )
        val promoBig = buildPromoRecord(
            "promo_big",
            "Diskon 5rb",
            PromoConfig(
                promoType = PromoType.MINIMUM_PURCHASE,
                benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
                discountValue = 5000L,
                minPurchaseAmount = 10000L,
                priority = 10,
                isStackable = false
            )
        )
        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L)
        )

        val result = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoSmall + promoBig
        )
        assertEquals(5000L, result.totalDiscountAmount)
        assertEquals("Diskon 5rb", result.primaryPromoName)
    }

    @Test
    fun scenario11_stackablePromos_combinesDiscountsWithoutReusingConsumedItems() {
        val bundleCfg = PromoConfig(
            promoType = PromoType.BUNDLE,
            benefitType = PromoBenefitType.FIXED_PACKAGE_PRICE,
            fixedPackagePrice = 29000L, // saves 4.000 on 1 Waffle + 1 Drink
            bundleGroups = listOf(
                PromoBundleGroupRule("Waffle", 1, listOf("Egg Waffle")),
                PromoBundleGroupRule("Minuman", 1, listOf("Drinks"))
            ),
            isStackable = true,
            allowShareItemsWithOtherPromos = false
        )
        val extraWafflePromoCfg = PromoConfig(
            promoType = PromoType.PRODUCT_DISCOUNT,
            benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
            discountValue = 2000L,
            applyToAllProducts = false,
            targetCategories = listOf("Egg Waffle"),
            isStackable = true,
            allowShareItemsWithOtherPromos = false
        )
        val p1 = buildPromoRecord("p_bundle", "Paket Kombo", bundleCfg)
        val p2 = buildPromoRecord("p_waffle", "Diskon Waffle 2rb", extraWafflePromoCfg)

        // Cart has 2 Waffles + 1 Drink:
        // 1 Waffle + 1 Drink consumed by Paket Kombo (saves 4.000)
        // Remaining 1 Waffle gets Diskon Waffle 2rb (saves 2.000) -> Total discount 6.000!
        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 2, unitPriceWalkIn = 18000L),
            CartLineItem(lineId = "2", category = "Drinks", primaryItemId = drinkMatcha.id, primaryItemName = "Matcha Latte", qty = 1, unitPriceWalkIn = 15000L)
        )
        val result = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + p1 + p2
        )
        assertEquals(6000L, result.totalDiscountAmount)
        assertEquals(2, result.appliedPromos.size)
    }

    @Test
    fun scenario12_itemRemovedFromCartAfterPromoActive_automaticallyCancelsBundlePromo() {
        val bundleCfg = PromoConfig(
            promoType = PromoType.BUNDLE,
            benefitType = PromoBenefitType.FIXED_PACKAGE_PRICE,
            fixedPackagePrice = 29000L,
            bundleGroups = listOf(
                PromoBundleGroupRule("Waffle", 1, listOf("Egg Waffle")),
                PromoBundleGroupRule("Minuman", 1, listOf("Drinks"))
            )
        )
        val promoRec = buildPromoRecord("promo_bundle", "Paket Kombo", bundleCfg)
        val fullCart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L),
            CartLineItem(lineId = "2", category = "Drinks", primaryItemId = drinkMatcha.id, primaryItemName = "Matcha Latte", qty = 1, unitPriceWalkIn = 15000L)
        )
        val beforeRemove = WaffleoPromoEngine.evaluateCartPromos(fullCart, OrderMedia.WALK_IN, "Global", baseCatalog + promoRec)
        assertEquals(4000L, beforeRemove.totalDiscountAmount)

        // Drink removed from cart -> promo immediately cancelled
        val afterRemove = WaffleoPromoEngine.evaluateCartPromos(fullCart.take(1), OrderMedia.WALK_IN, "Global", baseCatalog + promoRec)
        assertEquals(0L, afterRemove.totalDiscountAmount)
        assertEquals(18000L, afterRemove.finalTotalAmount)
    }

    @Test
    fun scenario13_itemQuantityChangedInCart_recalculatesMultipleBundlePacketsCorrectly() {
        val bundleCfg = PromoConfig(
            promoType = PromoType.BUNDLE,
            benefitType = PromoBenefitType.FIXED_PACKAGE_PRICE,
            fixedPackagePrice = 29000L, // saves 4.000 per pair
            bundleGroups = listOf(
                PromoBundleGroupRule("Waffle", 1, listOf("Egg Waffle")),
                PromoBundleGroupRule("Minuman", 1, listOf("Drinks"))
            )
        )
        val promoRec = buildPromoRecord("promo_bundle", "Paket Kombo", bundleCfg)
        // 2 Waffles + 2 Drinks = 2 complete packets -> saves 8.000
        val cart2Pairs = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 2, unitPriceWalkIn = 18000L),
            CartLineItem(lineId = "2", category = "Drinks", primaryItemId = drinkMatcha.id, primaryItemName = "Matcha Latte", qty = 2, unitPriceWalkIn = 15000L)
        )
        val eval2Pairs = WaffleoPromoEngine.evaluateCartPromos(cart2Pairs, OrderMedia.WALK_IN, "Global", baseCatalog + promoRec)
        assertEquals(8000L, eval2Pairs.totalDiscountAmount)
        assertEquals(58000L, eval2Pairs.finalTotalAmount)
        assertEquals(2, eval2Pairs.appliedPromos.first().appliedPacketCount)
    }

    @Test
    fun scenario14_cancelledOrVoidTransaction_restoresPromoQuotaConsistently() {
        val cfg = PromoConfig(
            promoType = PromoType.MINIMUM_PURCHASE,
            benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
            discountValue = 5000L,
            minPurchaseAmount = 10000L,
            hasQuotaLimit = true,
            maxTotalQuota = 1,
            usedCount = 0
        )
        val promoRec = buildPromoRecord("promo_q1", "Promo Kuota 1", cfg)
        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L)
        )

        // When active order uses the 1 quota (dynamicUsageMap = 1), promo is EXHAUSTED
        val evalWhenUsed = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec,
            dynamicUsageMap = mapOf("promo_q1" to 1)
        )
        assertEquals(0L, evalWhenUsed.totalDiscountAmount)

        // When order is cancelled/voided, dynamicUsageMap drops back to 0 -> promo is ACTIVE again!
        val evalAfterCancel = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec,
            dynamicUsageMap = mapOf("promo_q1" to 0)
        )
        assertEquals(5000L, evalAfterCancel.totalDiscountAmount)
    }

    @Test
    fun scenario15_discountRoundingAndNegativePriceProtection_neverProducesNegativeTotal() {
        val hugeDiscountCfg = PromoConfig(
            promoType = PromoType.MINIMUM_PURCHASE,
            benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
            discountValue = 50000L, // larger than cart subtotal 18.000
            minPurchaseAmount = 10000L
        )
        val promoRec = buildPromoRecord("promo_huge", "Diskon Besar", hugeDiscountCfg)
        val cart = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 1, unitPriceWalkIn = 18000L)
        )
        val eval = WaffleoPromoEngine.evaluateCartPromos(cart, OrderMedia.WALK_IN, "Global", baseCatalog + promoRec)
        assertEquals(18000L, eval.totalDiscountAmount)
        assertEquals(0L, eval.finalTotalAmount)
        assertTrue(eval.finalTotalAmount >= 0L)
    }

    @Test
    fun scenario16_concurrentLastQuotaBoundary_capsUsageToRemainingQuotaAndBlocksNextCheckout() {
        val cfg = PromoConfig(
            promoType = PromoType.BUNDLE,
            benefitType = PromoBenefitType.FIXED_PACKAGE_PRICE,
            fixedPackagePrice = 29000L,
            bundleGroups = listOf(
                PromoBundleGroupRule("Waffle", 1, listOf("Egg Waffle")),
                PromoBundleGroupRule("Minuman", 1, listOf("Drinks"))
            ),
            hasQuotaLimit = true,
            maxTotalQuota = 10,
            usedCount = 9 // Only 1 quota remaining!
        )
        val promoRec = buildPromoRecord("promo_last_quota", "Paket Terbatas", cfg)
        // Cashier A has 2 pairs in cart, but only 1 quota remains -> only 1 packet gets discounted!
        val cart2Pairs = listOf(
            CartLineItem(lineId = "1", category = "Egg Waffle", primaryItemId = waffleTiramisu.id, primaryItemName = "Tiramisu", qty = 2, unitPriceWalkIn = 18000L),
            CartLineItem(lineId = "2", category = "Drinks", primaryItemId = drinkMatcha.id, primaryItemName = "Matcha Latte", qty = 2, unitPriceWalkIn = 15000L)
        )
        val cashierAEval = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart2Pairs,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec,
            dynamicUsageMap = mapOf("promo_last_quota" to 9)
        )
        assertEquals(1, cashierAEval.appliedPromos.first().appliedPacketCount)
        assertEquals(4000L, cashierAEval.totalDiscountAmount)

        // Cashier B checks out right after Cashier A took the 10th quota -> receives 0 discount
        val cashierBEval = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = cart2Pairs,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + promoRec,
            dynamicUsageMap = mapOf("promo_last_quota" to 10)
        )
        assertEquals(0L, cashierBEval.totalDiscountAmount)
        assertFalse(cashierBEval.hasActivePromo)
    }

    @Test
    fun scenario17_doubleToppingWaffleWithAddOns_correctlySeparatesPolosBasePriceFromAddOnPrice() {
        val wafflePolos = CatalogRecord(
            id = "waffle_polos",
            branchId = "Global",
            recordType = "MENU_ITEM",
            category = "Egg Waffle",
            name = "Polos",
            priceWalkIn = 13000L,
            isActive = true
        )
        val addonChocochip = CatalogRecord(
            id = "addon_chocochip",
            branchId = "Global",
            recordType = "MODIFIER",
            category = "Add-on",
            name = "Chocochip",
            priceWalkIn = 3000L,
            isActive = true
        )
        // Double Topping Tiramisu (18k) + Chocolate (18k) - Polos (13k) = 23k base + 3k Chocochip = 26k total
        val doubleToppingCart = listOf(
            CartLineItem(
                lineId = "dt_1",
                category = "Egg Waffle",
                primaryItemId = waffleTiramisu.id,
                primaryItemName = "Tiramisu",
                secondaryItemId = waffleChoco.id,
                secondaryItemName = "Chocolate",
                isDoubleTopping = true,
                addOnNames = listOf("Chocochip"),
                qty = 1,
                unitPriceWalkIn = 26000L
            )
        )
        val cfg = PromoConfig(
            promoType = PromoType.PRODUCT_DISCOUNT,
            benefitType = PromoBenefitType.DISCOUNT_PERCENT,
            discountValue = 20L, // 20% off base price (23.000 * 20% = 4.600), excluding 3.000 Chocochip add-on
            applyToAllProducts = true
        )
        val promoRec = buildPromoRecord("promo_dt", "Diskon 20% Base", cfg)
        val eval = WaffleoPromoEngine.evaluateCartPromos(
            cartItems = doubleToppingCart,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + wafflePolos + addonChocochip + promoRec
        )
        assertEquals(26000L, eval.subtotalBeforeDiscount)
        assertEquals(4600L, eval.totalDiscountAmount)
        assertEquals(21400L, eval.finalTotalAmount)
    }

    @Test
    fun scenario18_filterEligiblePromosForCart_includesEligibleAutoAndVoucherAndExcludesIneligible() {
        val autoPromoEligible = buildPromoRecord(
            id = "promo_auto_waffle",
            name = "Diskon Waffle 10%",
            config = PromoConfig(
                promoType = PromoType.PRODUCT_DISCOUNT,
                applyToAllProducts = true,
                benefitType = PromoBenefitType.DISCOUNT_PERCENT,
                discountValue = 10L,
                minItemQty = 1,
                applyToAllBranches = true,
                isAutoApply = true
            )
        )
        val voucherPromoEligible = buildPromoRecord(
            id = "promo_voucher_hemat",
            name = "Voucher Hemat 5rb",
            config = PromoConfig(
                promoType = PromoType.MINIMUM_PURCHASE,
                applyToAllProducts = true,
                benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
                discountValue = 5000L,
                minPurchaseAmount = 15000L,
                applyToAllBranches = true,
                isAutoApply = false,
                voucherCode = "HEMAT5K"
            )
        )
        val minOrderIneligiblePromo = buildPromoRecord(
            id = "promo_min_100k",
            name = "Diskon 20rb Min 100rb",
            config = PromoConfig(
                promoType = PromoType.MINIMUM_PURCHASE,
                applyToAllProducts = true,
                benefitType = PromoBenefitType.DISCOUNT_NOMINAL,
                discountValue = 20000L,
                minPurchaseAmount = 100000L,
                applyToAllBranches = true,
                isAutoApply = true
            )
        )

        val cartItems = listOf(
            CartLineItem(
                lineId = "line_1",
                category = "Egg Waffle",
                primaryItemId = waffleChoco.id,
                primaryItemName = waffleChoco.name,
                qty = 1,
                unitPriceWalkIn = 18000L
            )
        )

        val eligible = WaffleoPromoEngine.filterEligiblePromosForCart(
            cartItems = cartItems,
            media = OrderMedia.WALK_IN,
            activeBranchId = "Global",
            catalog = baseCatalog + autoPromoEligible + voucherPromoEligible + minOrderIneligiblePromo
        )

        assertEquals(2, eligible.size)
        assertTrue(eligible.any { it.id == "promo_auto_waffle" })
        assertTrue(eligible.any { it.id == "promo_voucher_hemat" })
        assertFalse(eligible.any { it.id == "promo_min_100k" })
        assertEquals("promo_voucher_hemat", eligible[0].id)
        assertEquals("HEMAT5K", PromoConfig.fromCatalogRecord(eligible[0]).voucherCode)
    }
}
