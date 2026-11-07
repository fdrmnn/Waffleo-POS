package com.example.data.model

object WaffleoSeedData {

    fun defaultStaffAccounts(): List<StaffAccount> = listOf(
        StaffAccount(
            branchId = "Global",
            username = "developer",
            password = "developer",
            role = StaffRole.DEVELOPER,
            displayName = "System Developer (Master)",
            email = "developer@waffleo.id",
            phone = "0811-0000-0000"
        ),
        StaffAccount(
            branchId = "Global",
            username = "owner",
            password = "owner",
            role = StaffRole.OWNER,
            displayName = "Owner Global",
            email = "owner.global@waffleo.id",
            phone = "0812-1000-2000"
        ),
        StaffAccount(
            branchId = "Global",
            username = "supervisor",
            password = "supervisor",
            role = StaffRole.SUPERVISOR,
            displayName = "Supervisor Global",
            email = "spv.global@waffleo.id",
            phone = "0812-1000-2001"
        ),
        StaffAccount(
            branchId = "Global",
            username = "spv",
            password = "spv123",
            role = StaffRole.SUPERVISOR,
            displayName = "Supervisor Global (Alias)",
            email = "spv.global@waffleo.id",
            phone = "0812-1000-2001"
        ),
        StaffAccount(
            branchId = "Global",
            username = "kasir",
            password = "kasir",
            role = StaffRole.KASIR,
            displayName = "Kasir Utama Global",
            email = "kasir.global@waffleo.id",
            phone = "0812-1000-2002"
        ),
        StaffAccount(
            branchId = "Global",
            username = "kasir",
            password = "kasir123",
            role = StaffRole.KASIR,
            displayName = "Kasir Utama Global (Alias)",
            email = "kasir.global@waffleo.id",
            phone = "0812-1000-2002"
        )
    )

    val defaultBranches = listOf("Global")

    val defaultBranchProfiles = listOf(
        BranchProfile(
            branchId = "Global",
            location = "Pusat Operasional & Flagship Store",
            ownerName = "Fadhil (Owner Global)",
            ownerEmail = "owner.global@waffleo.id",
            ownerPhone = "0812-1000-2000",
            ownerUsername = "owner",
            initializedFrom = "Master System",
            createdAtText = "Cabang Utama"
        )
    )

    val defaultCancelReasons = listOf(
        "Customer berubah pikiran / batal beli",
        "Salah input menu oleh kasir",
        "Stok topping / bahan mendadak habis",
        "Pesanan Platform Online (GrabFood/GoFood/ShopeeFood) dibatalkan driver",
        "Waktu tunggu terlalu lama",
        "Kendala pembayaran non-tunai / QRIS"
    )

    val legacyDummyCatalogIds: Set<String> = setOf(
        "waffle_choco_crunch",
        "waffle_matcha_azuki",
        "waffle_lotus_biscoff",
        "waffle_oreo_cheese",
        "waffle_strawberry_velvet",
        "waffle_cheddar_melt",
        "waffle_tiramisu_almond",
        "drink_aren_latte",
        "drink_iced_americano",
        "drink_salted_caramel",
        "drink_yuzu_espresso",
        "drink_belgian_choco",
        "drink_strawberry_cloud",
        "flv_choco",
        "flv_matcha",
        "flv_charcoal",
        "flv_taro",
        "flv_coffee",
        "add_icecream",
        "add_chocochips",
        "add_almond",
        "add_cheese",
        "add_marshmallow",
        "add_biscoff",
        "add_oreo",
        "add_maple",
        "ing_premix_waffle",
        "ing_telur_ayam",
        "ing_fresh_milk",
        "ing_kopi_espresso",
        "ing_topping_saus",
        "ing_packaging",
        "ing_batter",
        "ing_egg",
        "ing_box",
        "ing_choco",
        "ing_matcha",
        "ing_cheese",
        "ing_cup",
        "prm_bundling_5k",
        "prm_duo_waffle_10",
        "prm_min_80k",
        "prm_happyhour_15"
    )

    fun defaultCatalogRecords(userId: String): List<CatalogRecord> = listOf(
        // --- EGG WAFFLE TOPPING UTAMA (F.2 Master Data: Spread, Crunchy, Premium) ---
        CatalogRecord(
            id = "waffle_polos",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Polos",
            category = "Egg Waffle",
            priceWalkIn = 13000L,
            priceGrabFood = 17000L,
            priceGoFood = 17000L,
            priceShopeeFood = 17500L,
            costPerUnit = 6000L,
            metadataJson = """{"subCategory":"Spread","series":"Spread","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_chocolate",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Chocolate",
            category = "Egg Waffle",
            priceWalkIn = 18000L,
            priceGrabFood = 22500L,
            priceGoFood = 22500L,
            priceShopeeFood = 24000L,
            costPerUnit = 8000L,
            metadataJson = """{"subCategory":"Spread","series":"Spread","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":35},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_tiramisu",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Tiramisu",
            category = "Egg Waffle",
            priceWalkIn = 18000L,
            priceGrabFood = 22500L,
            priceGoFood = 22500L,
            priceShopeeFood = 24000L,
            costPerUnit = 8000L,
            metadataJson = """{"subCategory":"Spread","series":"Spread","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":35},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_greentea",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Greentea",
            category = "Egg Waffle",
            priceWalkIn = 18000L,
            priceGrabFood = 22500L,
            priceGoFood = 22500L,
            priceShopeeFood = 24000L,
            costPerUnit = 8000L,
            metadataJson = """{"subCategory":"Spread","series":"Spread","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":35},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_taro",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Taro",
            category = "Egg Waffle",
            priceWalkIn = 18000L,
            priceGrabFood = 22500L,
            priceGoFood = 22500L,
            priceShopeeFood = 24000L,
            costPerUnit = 8000L,
            metadataJson = """{"subCategory":"Spread","series":"Spread","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":35},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_cappuccino",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Cappuccino",
            category = "Egg Waffle",
            priceWalkIn = 19000L,
            priceGrabFood = 24000L,
            priceGoFood = 24000L,
            priceShopeeFood = 26000L,
            costPerUnit = 8500L,
            metadataJson = """{"subCategory":"Spread","series":"Spread","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":35},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_strawberry",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Strawberry",
            category = "Egg Waffle",
            priceWalkIn = 18000L,
            priceGrabFood = 22500L,
            priceGoFood = 22500L,
            priceShopeeFood = 24000L,
            costPerUnit = 8000L,
            metadataJson = """{"subCategory":"Spread","series":"Spread","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":35},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_blueberry",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Blueberry",
            category = "Egg Waffle",
            priceWalkIn = 18000L,
            priceGrabFood = 22500L,
            priceGoFood = 22500L,
            priceShopeeFood = 24000L,
            costPerUnit = 8000L,
            metadataJson = """{"subCategory":"Spread","series":"Spread","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":35},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_choco_crunchy",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Choco Crunchy",
            category = "Egg Waffle",
            priceWalkIn = 20000L,
            priceGrabFood = 25000L,
            priceGoFood = 25000L,
            priceShopeeFood = 27000L,
            costPerUnit = 9000L,
            metadataJson = """{"subCategory":"Crunchy","series":"Crunchy","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":40},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_tiramisu_crunchy",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Tiramisu Crunchy",
            category = "Egg Waffle",
            priceWalkIn = 20000L,
            priceGrabFood = 25000L,
            priceGoFood = 25000L,
            priceShopeeFood = 27000L,
            costPerUnit = 9000L,
            metadataJson = """{"subCategory":"Crunchy","series":"Crunchy","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":40},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_hazelnut",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Hazelnut",
            category = "Egg Waffle",
            priceWalkIn = 25000L,
            priceGrabFood = 31500L,
            priceGoFood = 31500L,
            priceShopeeFood = 34000L,
            costPerUnit = 11000L,
            metadataJson = """{"subCategory":"Premium","series":"Premium","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":45},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),
        CatalogRecord(
            id = "waffle_ovomaltine",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Ovomaltine",
            category = "Egg Waffle",
            priceWalkIn = 36000L,
            priceGrabFood = 45000L,
            priceGoFood = 45000L,
            priceShopeeFood = 48000L,
            costPerUnit = 15500L,
            metadataJson = """{"subCategory":"Premium","series":"Premium","recipe":[{"ingredient":"Tepung Premix Waffleo","qty":120},{"ingredient":"Telur Ayam Segar","qty":1},{"ingredient":"Susu Fresh Milk","qty":80},{"ingredient":"Topping & Saus Premium","qty":50},{"ingredient":"Paper Tray & Cup Waffleo","qty":1}]}""",
            isActive = true
        ),

        // --- DRINKS (Coffee / Non-Coffee) ---
        CatalogRecord(
            id = "drink_iced_latte",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Iced Latte",
            category = "Drinks",
            priceWalkIn = 15000L,
            priceGrabFood = 18750L,
            priceGoFood = 18750L,
            priceShopeeFood = 20000L,
            costPerUnit = 6000L,
            metadataJson = """{"subCategory":"Coffee","type":"Coffee"}""",
            isActive = true
        ),
        CatalogRecord(
            id = "drink_iced_aren_latte",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Iced Aren Latte",
            category = "Drinks",
            priceWalkIn = 16000L,
            priceGrabFood = 20000L,
            priceGoFood = 20000L,
            priceShopeeFood = 22000L,
            costPerUnit = 6500L,
            metadataJson = """{"subCategory":"Coffee","type":"Coffee"}""",
            isActive = true
        ),
        CatalogRecord(
            id = "drink_iced_caramel_latte",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Iced Caramel Latte",
            category = "Drinks",
            priceWalkIn = 20000L,
            priceGrabFood = 25000L,
            priceGoFood = 25000L,
            priceShopeeFood = 27000L,
            costPerUnit = 7800L,
            metadataJson = """{"subCategory":"Coffee","type":"Coffee"}""",
            isActive = true
        ),
        CatalogRecord(
            id = "drink_iced_hazelnut_latte",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Iced Hazelnut Latte",
            category = "Drinks",
            priceWalkIn = 20000L,
            priceGrabFood = 25000L,
            priceGoFood = 25000L,
            priceShopeeFood = 27000L,
            costPerUnit = 7800L,
            metadataJson = """{"subCategory":"Coffee","type":"Coffee"}""",
            isActive = true
        ),
        CatalogRecord(
            id = "drink_iced_chocolate",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Iced Chocolate",
            category = "Drinks",
            priceWalkIn = 15000L,
            priceGrabFood = 18750L,
            priceGoFood = 18750L,
            priceShopeeFood = 20000L,
            costPerUnit = 6000L,
            metadataJson = """{"subCategory":"Non-Coffee","type":"Non-Coffee"}""",
            isActive = true
        ),
        CatalogRecord(
            id = "drink_cookies_cream",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Cookies & Cream",
            category = "Drinks",
            priceWalkIn = 15000L,
            priceGrabFood = 18750L,
            priceGoFood = 18750L,
            priceShopeeFood = 20000L,
            costPerUnit = 6200L,
            metadataJson = """{"subCategory":"Non-Coffee","type":"Non-Coffee"}""",
            isActive = true
        ),
        CatalogRecord(
            id = "drink_matcha_latte",
            userId = userId,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Matcha Latte",
            category = "Drinks",
            priceWalkIn = 20000L,
            priceGrabFood = 25000L,
            priceGoFood = 25000L,
            priceShopeeFood = 27000L,
            costPerUnit = 8000L,
            metadataJson = """{"subCategory":"Non-Coffee","type":"Non-Coffee"}""",
            isActive = true
        ),

        // --- MODIFIERS: FLAVOUR (Egg Waffle Batter - required, pick 1, default Original) ---
        CatalogRecord(id = "flv_original", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Original", category = "Flavour", priceWalkIn = 0L, priceGrabFood = 0L, priceGoFood = 0L, priceShopeeFood = 0L, isActive = true),
        CatalogRecord(id = "flv_pandan", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Pandan", category = "Flavour", priceWalkIn = 2000L, priceGrabFood = 3000L, priceGoFood = 3000L, priceShopeeFood = 3000L, isActive = true),
        CatalogRecord(id = "flv_redvelvet", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Redvelvet", category = "Flavour", priceWalkIn = 2000L, priceGrabFood = 3000L, priceGoFood = 3000L, priceShopeeFood = 3000L, isActive = true),
        CatalogRecord(id = "flv_blackforest", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Blackforest", category = "Flavour", priceWalkIn = 2000L, priceGrabFood = 3000L, priceGoFood = 3000L, priceShopeeFood = 3000L, isActive = true),
        CatalogRecord(id = "flv_mocca", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Mocca", category = "Flavour", priceWalkIn = 2000L, priceGrabFood = 3000L, priceGoFood = 3000L, priceShopeeFood = 3000L, isActive = true),

        // --- MODIFIERS: ADD ON (Egg Waffle Extra Toppings - optional, multi-select) ---
        CatalogRecord(id = "add_meses", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Meses", category = "Add-on", priceWalkIn = 2000L, priceGrabFood = 3000L, priceGoFood = 3000L, priceShopeeFood = 3000L, isActive = true),
        CatalogRecord(id = "add_chocochip", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Chocochip", category = "Add-on", priceWalkIn = 4000L, priceGrabFood = 5000L, priceGoFood = 5000L, priceShopeeFood = 5000L, isActive = true),
        CatalogRecord(id = "add_keju_parut", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Keju Parut", category = "Add-on", priceWalkIn = 6000L, priceGrabFood = 7000L, priceGoFood = 7000L, priceShopeeFood = 7000L, isActive = true),
        CatalogRecord(id = "add_keju_dalam_waffle", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Keju dalam Waffle", category = "Add-on", priceWalkIn = 6000L, priceGrabFood = 7000L, priceGoFood = 7000L, priceShopeeFood = 7000L, isActive = true),

        // --- MODIFIERS: SWEETNESS LEVEL ---
        CatalogRecord(id = "swt_normal", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Normal", category = "Sweetness", isActive = true),
        CatalogRecord(id = "swt_less75", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Less Sweet (75%)", category = "Sweetness", isActive = true),
        CatalogRecord(id = "swt_half50", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Half Sweet (50%)", category = "Sweetness", isActive = true),
        CatalogRecord(id = "swt_slight25", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Slight Sweet (25%)", category = "Sweetness", isActive = true),
        CatalogRecord(id = "swt_nosugar", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "No Sugar (0%)", category = "Sweetness", isActive = true),
        CatalogRecord(id = "swt_extra125", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Extra Sweet (125%)", category = "Sweetness", isActive = true),

        // --- MODIFIERS: ICED LEVEL ---
        CatalogRecord(id = "ice_normal", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Normal", category = "Ice Level", isActive = true),
        CatalogRecord(id = "ice_less", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Less Ice", category = "Ice Level", isActive = true),
        CatalogRecord(id = "ice_more", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "More Ice", category = "Ice Level", isActive = true),
        CatalogRecord(id = "ice_separate", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Es Pisah (Separate Ice)", category = "Ice Level", isActive = true),
        CatalogRecord(id = "ice_noice", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "No Ice (Tetap Dingin)", category = "Ice Level", isActive = true),
        CatalogRecord(id = "ice_warm", userId = userId, recordType = "MODIFIER", branchId = "Global", name = "Warm / Hangat", category = "Ice Level", isActive = true),

        // --- INGREDIENTS & PROMOS (DIKOSONGKAN SESUAI INSTRUKSI USER) ---


        // --- PLATFORM FEE & BCA SETTLEMENT CONFIGS (CABANG GLOBAL) ---
        CatalogRecord(
            id = "cfg_platform_grabfood",
            userId = userId,
            recordType = "BRANCH_CONFIG",
            branchId = "Global",
            name = "GrabFood -> Rekening Bank BCA",
            category = "GrabFood",
            priceWalkIn = 20L, // Komisi Platform (20%)
            priceGrabFood = 0L, // Flat Fee (Rp 0)
            metadataJson = """{"bankAccount":"Rekening Bank BCA (Cabang Global)","commissionPercent":20.0,"orderFee":0}""",
            isActive = true
        ),
        CatalogRecord(
            id = "cfg_platform_gofood",
            userId = userId,
            recordType = "BRANCH_CONFIG",
            branchId = "Global",
            name = "GoFood -> Rekening Bank BCA",
            category = "GoFood",
            priceWalkIn = 20L, // Komisi Platform (20%)
            priceGoFood = 1000L, // Flat Fee (Rp 1.000/order)
            metadataJson = """{"bankAccount":"Rekening Bank BCA (Cabang Global)","commissionPercent":20.0,"orderFee":1000}""",
            isActive = true
        ),
        CatalogRecord(
            id = "cfg_platform_shopeefood",
            userId = userId,
            recordType = "BRANCH_CONFIG",
            branchId = "Global",
            name = "ShopeeFood -> Rekening Bank BCA",
            category = "ShopeeFood",
            priceWalkIn = 25L, // Komisi Platform (25%)
            priceShopeeFood = 0L, // Flat Fee (Rp 0)
            metadataJson = """{"bankAccount":"Rekening Bank BCA (Cabang Global)","commissionPercent":25.0,"orderFee":0}""",
            isActive = true
        )
    )

    // --- MONEY MANAGER: MASTER ACCOUNT GROUPS & ACCOUNTS ---
    val defaultMoneyAccountGroups: List<MoneyAccountGroupDef> = listOf(
        MoneyAccountGroupDef(
            groupName = "Uang Tunai",
            accounts = listOf("Cash", "Dana di Fadhil")
        ),
        MoneyAccountGroupDef(
            groupName = "Bank",
            accounts = listOf("Bank Mandiri", "BCA")
        ),
        MoneyAccountGroupDef(
            groupName = "Dompet Digital",
            accounts = listOf("ShopeePay")
        ),
        MoneyAccountGroupDef(
            groupName = "Operasional",
            accounts = listOf("Sewa & Tabungan")
        )
    )

    val defaultMoneyInitialBalances: List<MoneyAccountInitialBalance> = listOf(
        MoneyAccountInitialBalance(branchId = "Global", accountGroup = "Uang Tunai", accountName = "Cash", initialBalance = 0L),
        MoneyAccountInitialBalance(branchId = "Global", accountGroup = "Uang Tunai", accountName = "Dana di Fadhil", initialBalance = 0L),
        MoneyAccountInitialBalance(branchId = "Global", accountGroup = "Bank", accountName = "Bank Mandiri", initialBalance = 0L),
        MoneyAccountInitialBalance(branchId = "Global", accountGroup = "Bank", accountName = "BCA", initialBalance = 0L),
        MoneyAccountInitialBalance(branchId = "Global", accountGroup = "Dompet Digital", accountName = "ShopeePay", initialBalance = 0L),
        MoneyAccountInitialBalance(branchId = "Global", accountGroup = "Operasional", accountName = "Sewa & Tabungan", initialBalance = 0L)
    )

    // --- MONEY MANAGER: MASTER CATEGORIES & SUB-CATEGORIES (SESUAI REVISI USER) ---
    val defaultMoneyCategories: List<MoneyCategoryDef> = listOf(
        // INCOME
        MoneyCategoryDef(
            type = MoneyTransactionType.INCOME,
            emoji = "💰",
            categoryName = "Penjualan",
            subCategories = listOf("Cash", "QRIS", "Grabfood", "Gofood", "Shopeefood", "Transfer Tunai")
        ),
        MoneyCategoryDef(
            type = MoneyTransactionType.INCOME,
            emoji = "📝",
            categoryName = "Lain lain",
            subCategories = emptyList()
        ),

        // EXPENSE
        MoneyCategoryDef(
            type = MoneyTransactionType.EXPENSE,
            emoji = "🍱",
            categoryName = "Bahan Pokok Waffle",
            subCategories = listOf(
                "Tepung Terigu",
                "Maizena",
                "Gula",
                "Baking Powder",
                "Perisa",
                "Telur",
                "Evaporasi",
                "Minyak",
                "Air"
            )
        ),
        MoneyCategoryDef(
            type = MoneyTransactionType.EXPENSE,
            emoji = "🍼",
            categoryName = "Topping Waffle",
            subCategories = listOf(
                "Choco Spread",
                "Tiramisu",
                "Greentea",
                "Taro",
                "Cappuccino",
                "Strawberry",
                "Choco Crunchy",
                "Tiramisu Crunchy",
                "Blueberry",
                "Hazelnut",
                "Ovomaltine",
                "Meses",
                "Chocochip",
                "Keju"
            )
        ),
        MoneyCategoryDef(
            type = MoneyTransactionType.EXPENSE,
            emoji = "🍻",
            categoryName = "Bahan Pokok Minuman",
            subCategories = listOf(
                "Biji Kopi",
                "Freshmilk",
                "Krimer",
                "Evaporasi",
                "Gula Aren",
                "Cocoa Powder",
                "Cookies",
                "Syrup",
                "Topping",
                "Krimer Kental Manis",
                "Cup",
                "Sedotan",
                "Batu Es"
            )
        ),
        MoneyCategoryDef(
            type = MoneyTransactionType.EXPENSE,
            emoji = "📦",
            categoryName = "Packing",
            subCategories = listOf(
                "Plastik",
                "Stiker Logo",
                "Paperbag"
            )
        ),
        MoneyCategoryDef(
            type = MoneyTransactionType.EXPENSE,
            emoji = "🔄",
            categoryName = "Biaya Operasional",
            subCategories = listOf(
                "Listrik",
                "Kebersihan",
                "Sewa",
                "Lain lain"
            )
        ),
        MoneyCategoryDef(
            type = MoneyTransactionType.EXPENSE,
            emoji = "🔨",
            categoryName = "Item / Tools",
            subCategories = emptyList()
        ),
        MoneyCategoryDef(
            type = MoneyTransactionType.EXPENSE,
            emoji = "🛠️",
            categoryName = "Maintenance",
            subCategories = emptyList()
        ),
        MoneyCategoryDef(
            type = MoneyTransactionType.EXPENSE,
            emoji = "📫",
            categoryName = "Gaji Pegawai",
            subCategories = emptyList()
        ),
        MoneyCategoryDef(
            type = MoneyTransactionType.EXPENSE,
            emoji = "♾️",
            categoryName = "Pengeluaran Lain Lain",
            subCategories = listOf(
                "Sedekah Subuh",
                "Biaya Administrasi",
                "Lain-lain"
            )
        )
    )

    fun defaultMoneyBookmarks(branchId: String = "Global"): List<MoneyBookmarkRecord> = emptyList()

    fun defaultMoneyTransactions(branchId: String = "Global"): List<MoneyTransactionRecord> = emptyList()

    fun defaultOrderRecords(userId: String, branchId: String = "Global"): List<OrderRecord> = emptyList()
}
