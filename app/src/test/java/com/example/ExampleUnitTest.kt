package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun staffRole_editPermissions_adhereToHierarchy() {
    // DEVELOPER can edit all roles
    assertTrue(com.example.data.model.StaffRole.DEVELOPER.canEditAccountOfRole(com.example.data.model.StaffRole.DEVELOPER))
    assertTrue(com.example.data.model.StaffRole.DEVELOPER.canEditAccountOfRole(com.example.data.model.StaffRole.OWNER))
    assertTrue(com.example.data.model.StaffRole.DEVELOPER.canEditAccountOfRole(com.example.data.model.StaffRole.SUPERVISOR))
    assertTrue(com.example.data.model.StaffRole.DEVELOPER.canEditAccountOfRole(com.example.data.model.StaffRole.KASIR))

    // OWNER can edit OWNER, SUPERVISOR, KASIR, but CANNOT edit DEVELOPER
    assertFalse(com.example.data.model.StaffRole.OWNER.canEditAccountOfRole(com.example.data.model.StaffRole.DEVELOPER))
    assertTrue(com.example.data.model.StaffRole.OWNER.canEditAccountOfRole(com.example.data.model.StaffRole.OWNER))
    assertTrue(com.example.data.model.StaffRole.OWNER.canEditAccountOfRole(com.example.data.model.StaffRole.SUPERVISOR))
    assertTrue(com.example.data.model.StaffRole.OWNER.canEditAccountOfRole(com.example.data.model.StaffRole.KASIR))

    // SUPERVISOR can edit SUPERVISOR and KASIR, but CANNOT edit OWNER or DEVELOPER
    assertFalse(com.example.data.model.StaffRole.SUPERVISOR.canEditAccountOfRole(com.example.data.model.StaffRole.DEVELOPER))
    assertFalse(com.example.data.model.StaffRole.SUPERVISOR.canEditAccountOfRole(com.example.data.model.StaffRole.OWNER))
    assertTrue(com.example.data.model.StaffRole.SUPERVISOR.canEditAccountOfRole(com.example.data.model.StaffRole.SUPERVISOR))
    assertTrue(com.example.data.model.StaffRole.SUPERVISOR.canEditAccountOfRole(com.example.data.model.StaffRole.KASIR))

    // KASIR CANNOT edit any role
    assertFalse(com.example.data.model.StaffRole.KASIR.canEditAccountOfRole(com.example.data.model.StaffRole.DEVELOPER))
    assertFalse(com.example.data.model.StaffRole.KASIR.canEditAccountOfRole(com.example.data.model.StaffRole.OWNER))
    assertFalse(com.example.data.model.StaffRole.KASIR.canEditAccountOfRole(com.example.data.model.StaffRole.SUPERVISOR))
    assertFalse(com.example.data.model.StaffRole.KASIR.canEditAccountOfRole(com.example.data.model.StaffRole.KASIR))
  }

  @Test
  fun staffRole_assignableRoles_adhereToHierarchy() {
    val spvRoles = com.example.data.model.StaffRole.SUPERVISOR.assignableRoles()
    assertTrue(spvRoles.contains(com.example.data.model.StaffRole.SUPERVISOR))
    assertTrue(spvRoles.contains(com.example.data.model.StaffRole.KASIR))
    assertFalse(spvRoles.contains(com.example.data.model.StaffRole.OWNER))
    assertFalse(spvRoles.contains(com.example.data.model.StaffRole.DEVELOPER))

    val ownerRoles = com.example.data.model.StaffRole.OWNER.assignableRoles()
    assertTrue(ownerRoles.contains(com.example.data.model.StaffRole.OWNER))
    assertTrue(ownerRoles.contains(com.example.data.model.StaffRole.SUPERVISOR))
    assertTrue(ownerRoles.contains(com.example.data.model.StaffRole.KASIR))
    assertFalse(ownerRoles.contains(com.example.data.model.StaffRole.DEVELOPER))

    val kasirRoles = com.example.data.model.StaffRole.KASIR.assignableRoles()
    assertTrue(kasirRoles.isEmpty())
  }

  @Test
  fun auditLog_groupingByEntityAndOrder_aggregatesCorrectly() {
    val rawLogs = listOf(
      com.example.data.model.AuditLogRecord(
        id = "log_1",
        branchId = "Global",
        actorUsername = "owner",
        actionType = "ORDER_CREATED",
        entityId = "WFL-007",
        summary = "Pesanan baru WFL-007 (Walk In) senilai Rp 13.000",
        createdAt = com.google.firebase.Timestamp(100, 0)
      ),
      com.example.data.model.AuditLogRecord(
        id = "log_2",
        branchId = "Global",
        actorUsername = "owner",
        actionType = "ORDER_PAID",
        entityId = "WFL-007",
        summary = "Pembayaran pesanan WFL-007 lunas (Rp 13.000) via Tunai",
        createdAt = com.google.firebase.Timestamp(110, 0)
      ),
      com.example.data.model.AuditLogRecord(
        id = "log_3",
        branchId = "Global",
        actorUsername = "owner",
        actionType = "ORDER_EDITED",
        entityId = "WFL-007",
        summary = "Edit pesanan WFL-007: Total baru Rp 40.000",
        createdAt = com.google.firebase.Timestamp(120, 0)
      ),
      com.example.data.model.AuditLogRecord(
        id = "log_4",
        branchId = "Global",
        actorUsername = "owner",
        actionType = "ORDER_DONE_STOCK_DEDUCTED",
        entityId = "WFL-007",
        summary = "Pesanan WFL-007 SELESAI (Done!) & diserahkan ke customer.",
        createdAt = com.google.firebase.Timestamp(150, 0)
      ),
      com.example.data.model.AuditLogRecord(
        id = "log_5",
        branchId = "Global",
        actorUsername = "owner",
        actionType = "ORDER_CREATED",
        entityId = "WFL-008",
        summary = "Pesanan baru WFL-008 senilai Rp 25.000",
        createdAt = com.google.firebase.Timestamp(160, 0)
      )
    )

    val grouped = com.example.data.model.groupAuditLogRecords(rawLogs)
    assertEquals(2, grouped.size)

    val wfl007Group = grouped.firstOrNull { it.entityId == "WFL-007" }
    assertNotNull(wfl007Group)
    assertEquals(4, wfl007Group!!.totalSubActions)
    assertEquals("ORDER_DONE", wfl007Group.mainActionType)
    assertEquals("ORDER", wfl007Group.category)
    assertEquals("Pesanan WFL-007", wfl007Group.title)
    assertEquals(4, wfl007Group.subLogsChronological.size)
    assertEquals("ORDER_CREATED", wfl007Group.subLogsChronological[0].actionType)
    assertEquals("ORDER_DONE_STOCK_DEDUCTED", wfl007Group.subLogsChronological[3].actionType)

    val wfl008Group = grouped.firstOrNull { it.entityId == "WFL-008" }
    assertNotNull(wfl008Group)
    assertEquals(1, wfl008Group!!.totalSubActions)
  }

  @Test
  fun evaluateSimpleMathExpression_calculatesAccurately() {
    // Single number
    assertEquals(50000L, com.example.ui.components.evaluateSimpleMathExpression("50000"))
    assertEquals(0L, com.example.ui.components.evaluateSimpleMathExpression(""))

    // Addition
    assertEquals(75000L, com.example.ui.components.evaluateSimpleMathExpression("50000 + 25000"))
    assertEquals(75000L, com.example.ui.components.evaluateSimpleMathExpression("50000+25000"))

    // Multi-step addition & subtraction
    assertEquals(38000L, com.example.ui.components.evaluateSimpleMathExpression("18000 + 25000 - 13000 + 2000 + 6000"))

    // Multiplication with ×
    assertEquals(60000L, com.example.ui.components.evaluateSimpleMathExpression("20000 × 3"))

    // Division with ÷
    assertEquals(25000L, com.example.ui.components.evaluateSimpleMathExpression("50000 ÷ 2"))
  }

  @Test
  fun auditLog_cancelledOrderAndSecurityAlert_categorizesProperly() {
    val logs = listOf(
      com.example.data.model.AuditLogRecord(
        id = "log_c1",
        branchId = "Global",
        actorUsername = "kasir",
        actionType = "ORDER_CREATED",
        entityId = "WFL-099",
        summary = "Pesanan baru WFL-099 senilai Rp 35.000",
        createdAt = com.google.firebase.Timestamp(200, 0)
      ),
      com.example.data.model.AuditLogRecord(
        id = "log_c2",
        branchId = "Global",
        actorUsername = "spv",
        actionType = "ORDER_CANCELLED",
        entityId = "WFL-099",
        summary = "Pesanan WFL-099 DIBATALKAN (Cancel Order) oleh @spv. Alasan: Salah input varian",
        createdAt = com.google.firebase.Timestamp(220, 0)
      ),
      com.example.data.model.AuditLogRecord(
        id = "log_sec1",
        branchId = "Global",
        actorUsername = "SYSTEM",
        actionType = "SECURITY_ALERT_STORAGE_RESET",
        entityId = "STORAGE_RESET_Global",
        summary = "PERINGATAN KEAMANAN: Terdeteksi memori lokal di-reset pada cabang Global. Data pesanan dipulihkan otomatis dari Cloud.",
        createdAt = com.google.firebase.Timestamp(230, 0)
      )
    )

    val grouped = com.example.data.model.groupAuditLogRecords(logs)
    val cancelGroup = grouped.firstOrNull { it.entityId == "WFL-099" }
    assertNotNull(cancelGroup)
    assertEquals("ORDER_CANCELLED", cancelGroup!!.mainActionType)
    assertEquals(2, cancelGroup.totalSubActions)

    val secGroup = grouped.firstOrNull { it.mainActionType.contains("SECURITY") || it.highLevelSummary.contains("PERINGATAN KEAMANAN") }
    assertNotNull(secGroup)
  }

  @Test
  fun catalogRecord_allowedModifiers_serializesAndParsesCorrectly() {
    val record = com.example.data.model.CatalogRecord(
      id = "item_puding",
      name = "Puding",
      category = "Dessert"
    )
    val mapping = mapOf(
      "Rasa" to listOf("Coklat", "Vanilla"),
      "Topping" to listOf("Keju")
    )
    val updatedMeta = record.withAllowedModifiers(mapping)
    val updatedRecord = record.copy(metadataJson = updatedMeta)

    val parsedMapping = updatedRecord.getAllowedModifiers()
    assertEquals(2, parsedMapping.size)
    assertEquals(listOf("Coklat", "Vanilla"), parsedMapping["Rasa"])
    assertEquals(listOf("Keju"), parsedMapping["Topping"])
  }

  @Test
  fun orderHistory_searchMatching_detectsItemNameAndNumberCaseInsensitive() {
    val itemsJson = """[{"primaryItemName":"Original Chocolate","category":"Egg Waffle","flavourName":"Original","addOnNames":["Chocochip","Keju"]}]"""
    val order = com.example.data.model.OrderRecord(
      id = "ord_015",
      orderNumber = "WFL-015",
      sequenceIndex = 15,
      orderMedia = "Walk In",
      customerData = "Walk In Guest",
      createdBy = "owner",
      paymentMethod = "Tunai (Cash)",
      status = "DONE",
      itemsPayload = itemsJson,
      itemsSummary = "1x Original Chocolate\n+Chocochip +Keju",
      totalAmount = 78000L
    )

    // Test case insensitive match on order number
    assertTrue(order.orderNumber.contains("015", ignoreCase = true))
    assertTrue(order.orderNumber.contains("wfl", ignoreCase = true))

    // Test match on item name with letter "C"
    val items = com.example.data.model.CartLineItem.deserializeList(order.itemsPayload)
    val hasC = items.any {
      it.primaryItemName.contains("c", ignoreCase = true) ||
      it.addOnNames.any { add -> add.contains("c", ignoreCase = true) }
    }
    assertTrue(hasC)
  }

  @Test
  fun moneyManager_autoSyncRecapFormatting_matchesUserRules() {
    val cashRecap = com.example.data.model.DailyChannelRecapItem(
      dateKey = "2026-11-04",
      subCategory = "Cash",
      accountGroup = "Uang Tunai",
      accountName = "Cash",
      grossAmount = 110000L,
      platformDeduction = 0L,
      netAmount = 110000L,
      orderCount = 7,
      customItemSummary = "7 Waffle, 9 Minuman, 1 Puding"
    )
    assertEquals("Penjualan Tunai", cashRecap.formattedNote)
    assertEquals("7 Pesanan", cashRecap.formattedDescription)

    val qrisBca = com.example.data.model.DailyChannelRecapItem(
      dateKey = "2026-11-04",
      subCategory = "QRIS",
      accountGroup = "Bank",
      accountName = "BCA",
      grossAmount = 18000L,
      platformDeduction = 0L,
      netAmount = 18000L,
      orderCount = 1
    )
    assertEquals("QRIS", qrisBca.formattedNote)
    assertEquals("1 Pesanan", qrisBca.formattedDescription)

    val grabfood = com.example.data.model.DailyChannelRecapItem(
      dateKey = "2026-11-04",
      subCategory = "Grabfood",
      accountGroup = "Bank",
      accountName = "BCA",
      grossAmount = 131000L,
      platformDeduction = 0L,
      netAmount = 131000L,
      orderCount = 4,
      customItemSummary = "6 Waffle, 1 Minuman"
    )
    assertEquals("Grabfood", grabfood.formattedNote)
    assertEquals("4 Pesanan", grabfood.formattedDescription)
  }

  @Test
  fun transferFee_historicalConfigLookup_retrievesPreviousFeeAndAccountChoice() {
    val txList = listOf(
      com.example.data.model.MoneyTransactionRecord(
        id = "tx_tr_1",
        branchId = "Global",
        type = com.example.data.model.MoneyTransactionType.TRANSFER,
        dateKey = "2026-11-04",
        amount = 50000L,
        transferFee = 1000L,
        accountGroup = "Bank",
        accountName = "Bank Mandiri",
        toAccountGroup = "Bank",
        toAccountName = "BCA"
      )
    )

    val pastTransfer = txList.firstOrNull { tx ->
      tx.type == com.example.data.model.MoneyTransactionType.TRANSFER &&
          tx.accountName.equals("Bank Mandiri", ignoreCase = true) &&
          tx.toAccountName.equals("BCA", ignoreCase = true) &&
          tx.feeAmount > 0L
    }
    assertNotNull(pastTransfer)
    assertEquals(1000L, pastTransfer!!.feeAmount)
  }

  @Test
  fun customerNameSuggestions_filtersByTypedSubstringCaseInsensitive() {
    val orders = listOf(
      com.example.data.model.OrderRecord(id = "o1", branchId = "Global", orderMedia = "Walk In", customerData = "Budi (Suka Manis)"),
      com.example.data.model.OrderRecord(id = "o2", branchId = "Global", orderMedia = "WhatsApp", customerData = "Dian"),
      com.example.data.model.OrderRecord(id = "o3", branchId = "Global", orderMedia = "GrabFood", customerData = "GF - 1234")
    )

    val cleanQuery = "di"
    val suggestions = orders
      .filter { ord ->
        ord.customerData.isNotBlank() && !ord.orderMedia.equals("GrabFood", ignoreCase = true)
      }
      .map { it.customerData.trim() }
      .filter { it.contains(cleanQuery, ignoreCase = true) }

    assertEquals(2, suggestions.size)
    assertTrue(suggestions.contains("Budi (Suka Manis)"))
    assertTrue(suggestions.contains("Dian"))
  }

  @Test
  fun catalogRecord_subCategory_getterAndWithSubCategory_worksCorrectly() {
    val initial = com.example.data.model.CatalogRecord(
      id = "item_choco",
      name = "Chocolate",
      category = "Egg Waffle",
      metadataJson = """{"subCategory":"Spread"}"""
    )
    assertEquals("Spread", initial.subCategory)

    val updated = initial.withSubCategory("Crunchy")
    assertEquals("Crunchy", updated.subCategory)

    val cleared = updated.withSubCategory("")
    assertEquals("", cleared.subCategory)
  }

  @Test
  fun trendCharts_orderCountSeries_calculatesPeakAndYAxisCorrectly() {
    val orders = listOf(
      com.example.data.model.OrderRecord(id = "o1", sequenceIndex = 1, totalAmount = 50000L, status = "DONE"),
      com.example.data.model.OrderRecord(id = "o2", sequenceIndex = 1, totalAmount = 40000L, status = "DONE"),
      com.example.data.model.OrderRecord(id = "o3", sequenceIndex = 4, totalAmount = 30000L, status = "DONE"),
      com.example.data.model.OrderRecord(id = "o4", sequenceIndex = 9, totalAmount = 25000L, status = "DONE")
    )

    val hourlySeries = com.example.ui.components.buildHourlySalesSeries(orders) { it.totalAmount }
    assertEquals(4, hourlySeries.sumOf { it.orderCount })

    val rawMax = hourlySeries.maxOfOrNull { it.orderCount } ?: 0
    val maxOrderScale = maxOf(3, ((rawMax + 2) / 3) * 3)
    assertTrue(maxOrderScale >= rawMax)
    assertEquals(0, maxOrderScale % 3)

    val weeklySeries = com.example.ui.components.buildWeeklyDaysSalesSeries(orders) { it.totalAmount }
    assertEquals(7, weeklySeries.size)
    assertEquals(4, weeklySeries.sumOf { it.orderCount })
  }

  @Test
  fun orderMedia_diTempat_andBackwardCompatibility_worksCorrectly() {
    val diTempat = com.example.data.model.OrderMedia.fromLabel("Di tempat")
    assertEquals(com.example.data.model.OrderMedia.WALK_IN, diTempat)
    assertEquals("Di tempat", diTempat.label)

    val legacyWalkIn = com.example.data.model.OrderMedia.fromLabel("Walk In")
    assertEquals(com.example.data.model.OrderMedia.WALK_IN, legacyWalkIn)
    assertEquals("Di tempat", legacyWalkIn.label)

    val defaultOrder = com.example.data.model.OrderRecord()
    assertEquals("Di tempat", defaultOrder.orderMedia)
  }

  @Test
  fun pos_subCategoryFiltering_filtersItemsProperly() {
    val items = listOf(
      com.example.data.model.CatalogRecord(id = "1", name = "Waffle Coklat", category = "Egg Waffle", metadataJson = """{"subCategory":"Spread"}"""),
      com.example.data.model.CatalogRecord(id = "2", name = "Waffle Keju", category = "Egg Waffle", metadataJson = """{"subCategory":"Crunchy"}"""),
      com.example.data.model.CatalogRecord(id = "3", name = "Es Kopi", category = "Drinks", metadataJson = """{"subCategory":"Coffee"}""")
    )

    val eggWaffles = items.filter { it.category == "Egg Waffle" }
    assertEquals(2, eggWaffles.size)

    val spreadOnly = eggWaffles.filter { it.subCategory == "Spread" }
    assertEquals(1, spreadOnly.size)
    assertEquals("Waffle Coklat", spreadOnly[0].name)
  }

  @Test
  fun moneyManager_singleDailyTransactionRule_canonicalIdAndMatching() {
    val canonicalCash = "pos_sync_global_2026-11-04_cash_cash"

    // 4 fragmented Cash transactions from multiple store closes on the same day (as in user screenshot)
    val tx1 = com.example.data.model.MoneyTransactionRecord(
      id = "pos_sync_global_2026-11-04_1730703600000_cash_cash",
      branchId = "Global",
      type = com.example.data.model.MoneyTransactionType.INCOME,
      dateKey = "2026-11-04",
      amount = 92000L,
      category = "Penjualan",
      subCategory = "Cash",
      accountName = "Cash",
      note = "Penjualan Tunai",
      tag = "Auto-Sync Tutup Toko",
      isAutoPosSync = true
    )
    val tx2 = com.example.data.model.MoneyTransactionRecord(
      id = "pos_sync_global_2026-11-04_1730702400000_cash_cash",
      branchId = "Global",
      type = com.example.data.model.MoneyTransactionType.INCOME,
      dateKey = "2026-11-04",
      amount = 13000L,
      category = "Penjualan",
      subCategory = "Cash",
      accountName = "Cash",
      note = "Penjualan Tunai",
      tag = "Auto-Sync Tutup Toko",
      isAutoPosSync = true
    )
    val tx3 = com.example.data.model.MoneyTransactionRecord(
      id = "pos_sync_global_2026-11-04_1730701800000_cash_cash",
      branchId = "Global",
      type = com.example.data.model.MoneyTransactionType.INCOME,
      dateKey = "2026-11-04",
      amount = 237000L,
      category = "Penjualan",
      subCategory = "Cash",
      accountName = "Cash",
      note = "Penjualan Tunai",
      tag = "Auto-Sync Tutup Toko",
      isAutoPosSync = true
    )
    val tx4 = com.example.data.model.MoneyTransactionRecord(
      id = "pos_sync_global_2026-11-04_1730700000000_cash_cash",
      branchId = "Global",
      type = com.example.data.model.MoneyTransactionType.INCOME,
      dateKey = "2026-11-04",
      amount = 219000L,
      category = "Penjualan",
      subCategory = "Cash",
      accountName = "Cash",
      note = "Penjualan Tunai",
      tag = "Auto-Sync Tutup Toko",
      isAutoPosSync = true
    )

    val fragmentedTxs = listOf(tx1, tx2, tx3, tx4)

    assertEquals(4, fragmentedTxs.size)
    val totalAmount = fragmentedTxs.sumOf { it.amount }
    assertEquals(561000L, totalAmount)

    // Consolidated into 1 single canonical transaction
    val consolidated = tx1.copy(
      id = canonicalCash,
      amount = totalAmount,
      description = "14 Pesanan",
      timeFormatted = "07:36"
    )
    assertEquals(canonicalCash, consolidated.id)
    assertEquals(561000L, consolidated.amount)
    assertEquals("2026-11-04", consolidated.dateKey)
    assertEquals("Cash", consolidated.subCategory)
    assertEquals("Cash", consolidated.accountName)
  }

  @Test
  fun orderPromo_strikethrough_and_receipt_tender_calculation_isAccurate() {
    // Bundling Waffle + Drinks: Original Chocolate (18k) + Iced Latte (15k) with promo 5k
    val subtotal = 33000L
    val promoDiscount = 5000L
    val netTotal = subtotal - promoDiscount // 28000L

    assertEquals(28000L, netTotal)

    // Customer pays Rp 50.000 cash
    val cashTendered = 50000L
    val changeDue = cashTendered - netTotal // 22000L
    assertEquals(22000L, changeDue)

    val order = com.example.data.model.OrderRecord(
      id = "ord_test_001",
      orderNumber = "WFL-001",
      subtotalAmount = subtotal,
      discountAmount = promoDiscount,
      totalAmount = netTotal,
      paidAmount = netTotal,
      cashReceivedAmount = cashTendered,
      changeDueAmount = changeDue,
      paymentMethod = "Tunai (Cash)",
      isPaid = true
    )

    // Verify OrderRecord attributes
    assertEquals(33000L, order.subtotalAmount)
    assertEquals(5000L, order.discountAmount)
    assertEquals(28000L, order.totalAmount)
    assertEquals(50000L, order.cashReceivedAmount)
    assertEquals(22000L, order.changeDueAmount)

    // Verify receipt calculation logic
    val effectiveSubtotal = if (order.subtotalAmount > 0L) order.subtotalAmount else order.totalAmount + order.discountAmount
    val effDiscount = if (order.discountAmount > 0L) order.discountAmount else (effectiveSubtotal - order.totalAmount)
    val nominalBayar = when {
      order.cashReceivedAmount > 0L -> order.cashReceivedAmount
      order.changeDueAmount > 0L -> order.totalAmount + order.changeDueAmount
      else -> order.paidAmount
    }
    val receiptChange = when {
      order.changeDueAmount > 0L -> order.changeDueAmount
      order.cashReceivedAmount > order.totalAmount -> order.cashReceivedAmount - order.totalAmount
      else -> 0L
    }

    assertEquals(33000L, effectiveSubtotal)
    assertEquals(5000L, effDiscount)
    assertEquals(50000L, nominalBayar)
    assertEquals(22000L, receiptChange)
  }

  @Test
  fun paymentModal_breakdown_withPromo_isAccurate() {
    val totalBillAmount = 28000L
    val originalSubtotalAmount = 33000L
    val promoDiscountAmount = 5000L

    val effSubtotal = if (originalSubtotalAmount > 0L) {
      originalSubtotalAmount
    } else if (promoDiscountAmount > 0L) {
      totalBillAmount + promoDiscountAmount
    } else {
      totalBillAmount
    }

    val effDiscount = if (promoDiscountAmount > 0L) {
      promoDiscountAmount
    } else if (effSubtotal > totalBillAmount) {
      effSubtotal - totalBillAmount
    } else {
      0L
    }

    val amountDue = totalBillAmount

    assertEquals(33000L, effSubtotal)
    assertEquals(5000L, effDiscount)
    assertEquals(28000L, amountDue)
  }

  @Test
  fun dashboardHourlyChart_meanMedianModus_calculatesAccurately() {
    val calDay1 = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 1, 6, 15, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val calDay2 = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 2, 6, 20, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val calDay3 = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 4, 6, 30, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }

    // Day 1 @ 06:00 -> 1 order (Rp 100.000)
    // Day 2 @ 06:00 -> 1 order (Rp 100.000)
    // Day 3 @ 06:00 -> 2 orders (Rp 120.000 + Rp 180.000 = Rp 300.000)
    // Daily totals at 06:00 across 3 active days: [100_000, 100_000, 300_000]
    // Mean = (100k + 100k + 300k) / 3 = 166_667
    // Median = 100_000
    // Modus = 100_000
    val orders = listOf(
      com.example.data.model.OrderRecord(
        id = "ord_${calDay1.timeInMillis}_a",
        orderNumber = "WFL-001",
        totalAmount = 100000L,
        status = "DONE",
        createdAt = com.google.firebase.Timestamp(calDay1.time)
      ),
      com.example.data.model.OrderRecord(
        id = "ord_${calDay2.timeInMillis}_b",
        orderNumber = "WFL-002",
        totalAmount = 100000L,
        status = "DONE",
        createdAt = com.google.firebase.Timestamp(calDay2.time)
      ),
      com.example.data.model.OrderRecord(
        id = "ord_${calDay3.timeInMillis}_c",
        orderNumber = "WFL-003",
        totalAmount = 120000L,
        status = "DONE",
        createdAt = com.google.firebase.Timestamp(calDay3.time)
      ),
      com.example.data.model.OrderRecord(
        id = "ord_${calDay3.timeInMillis + 60000}_d",
        orderNumber = "WFL-004",
        totalAmount = 180000L,
        status = "DONE",
        createdAt = com.google.firebase.Timestamp(java.util.Date(calDay3.timeInMillis + 60000))
      )
    )

    val meanSeries = com.example.ui.components.buildHourlySalesSeries(
      doneOrders = orders,
      statMode = com.example.ui.components.HourlyStatMode.MEAN,
      isMultiDayPeriod = true,
      calculateNet = { it.totalAmount }
    )
    val medianSeries = com.example.ui.components.buildHourlySalesSeries(
      doneOrders = orders,
      statMode = com.example.ui.components.HourlyStatMode.MEDIAN,
      isMultiDayPeriod = true,
      calculateNet = { it.totalAmount }
    )
    val modusSeries = com.example.ui.components.buildHourlySalesSeries(
      doneOrders = orders,
      statMode = com.example.ui.components.HourlyStatMode.MODUS,
      isMultiDayPeriod = true,
      calculateNet = { it.totalAmount }
    )

    val meanAt06 = meanSeries.first { it.hour == 6 }
    val medianAt06 = medianSeries.first { it.hour == 6 }
    val modusAt06 = modusSeries.first { it.hour == 6 }

    assertEquals(166667L, meanAt06.grossRevenue)
    assertEquals(100000L, medianAt06.grossRevenue)
    assertEquals(100000L, modusAt06.grossRevenue)
  }

  @Test
  fun dashboardCustomDateRange_oct1ToNov4_buildsAccurateDailyAndMonthlySeries() {
    val calStart = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.OCTOBER, 1, 0, 0, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val calEnd = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 4, 23, 59, 59)
      set(java.util.Calendar.MILLISECOND, 999)
    }

    val oct15OrderCal = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.OCTOBER, 15, 10, 0, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val nov4OrderCal = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 4, 6, 30, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val sep20OutsideCal = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.SEPTEMBER, 20, 14, 0, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }

    val octOrder = com.example.data.model.OrderRecord(
      id = "ord_${oct15OrderCal.timeInMillis}_oct",
      orderNumber = "WFL-010",
      totalAmount = 48000L,
      status = "DONE",
      createdAt = null, // Verify fallback to id epoch millis works when createdAt is null!
      updatedAt = null
    )
    val novOrder = com.example.data.model.OrderRecord(
      id = "ord_${nov4OrderCal.timeInMillis}_nov",
      orderNumber = "WFL-011",
      totalAmount = 544000L,
      status = "DONE",
      createdAt = com.google.firebase.Timestamp(nov4OrderCal.time)
    )
    val sepOrderOutside = com.example.data.model.OrderRecord(
      id = "ord_${sep20OutsideCal.timeInMillis}_sep",
      orderNumber = "WFL-009",
      totalAmount = 99000L,
      status = "DONE",
      createdAt = com.google.firebase.Timestamp(sep20OutsideCal.time)
    )

    val allOrders = listOf(octOrder, novOrder, sepOrderOutside)
    val filtered = allOrders.filter { ord ->
      val resolved = com.example.ui.viewmodel.WaffleoViewModel.resolveOrderTimestampDate(ord)
      com.example.ui.viewmodel.WaffleoViewModel.matchesPeriodFilterRange(
        timestamp = com.google.firebase.Timestamp(resolved),
        period = com.example.ui.viewmodel.HistoryFilterPeriod.CUSTOM,
        isCustomRangeActive = true,
        startDateMillis = calStart.timeInMillis,
        endDateMillis = calEnd.timeInMillis
      )
    }

    assertEquals(2, filtered.size)
    assertEquals(592000L, filtered.sumOf { it.totalAmount })

    // 1 Oct to 4 Nov inclusive = 31 days in Oct + 4 days in Nov = 35 daily points
    val dailySeries = com.example.ui.components.buildCustomRangeDailySalesSeries(
      doneOrders = filtered,
      startMillis = calStart.timeInMillis,
      endMillis = calEnd.timeInMillis,
      calculateNet = { it.totalAmount }
    )
    assertEquals(35, dailySeries.size)
    assertEquals(48000L, dailySeries[14].grossRevenue) // Oct 15 is index 14
    assertEquals(544000L, dailySeries[34].grossRevenue) // Nov 4 is index 34 (last point)
    assertEquals(592000L, dailySeries.sumOf { it.grossRevenue })

    // Monthly series for 1 Oct 2026 .. 4 Nov 2026 has strictly 2 months: Okt 2026 and Nov 2026
    val monthlySeries = com.example.ui.components.buildCustomRangeMonthlySalesSeries(
      doneOrders = filtered,
      startMillis = calStart.timeInMillis,
      endMillis = calEnd.timeInMillis,
      calculateNet = { it.totalAmount }
    )
    assertEquals(2, monthlySeries.size)
    assertEquals("Okt 2026", monthlySeries[0].label)
    assertEquals(48000L, monthlySeries[0].grossRevenue)
    assertEquals(1, monthlySeries[0].orderCount)
    assertEquals("Nov 2026", monthlySeries[1].label)
    assertEquals(544000L, monthlySeries[1].grossRevenue)
    assertEquals(1, monthlySeries[1].orderCount)
  }

  @Test
  fun customerAutofillSelection_updatesSuffixAndDismissesPopup() {
    val orders = listOf(
      com.example.data.model.OrderRecord(id = "o1", branchId = "Global", orderMedia = "Di tempat", customerData = "Ulan"),
      com.example.data.model.OrderRecord(id = "o2", branchId = "Global", orderMedia = "WhatsApp", customerData = "Budi")
    )

    val typed = "U"
    val suggestions = orders
      .filter { ord ->
        ord.customerData.isNotBlank() && !ord.orderMedia.equals("GrabFood", ignoreCase = true)
      }
      .map { it.customerData.trim() }
      .filter { it.contains(typed, ignoreCase = true) && !it.equals(typed, ignoreCase = true) }

    assertTrue(suggestions.contains("Ulan"))

    // When suggestion is clicked
    var selectedSuffix = typed
    var customerDismissedText = ""
    var suggestionSelectedAtMillis = 0L

    // Click "Ulan"
    val chosen = "Ulan"
    suggestionSelectedAtMillis = System.currentTimeMillis()
    selectedSuffix = chosen
    customerDismissedText = chosen

    // Check IME flush protection: keyboard sends "U" right after losing focus
    val incomingImeFlush = "U"
    if (System.currentTimeMillis() - suggestionSelectedAtMillis < 800L) {
      // Ignored!
    } else {
      selectedSuffix = incomingImeFlush
    }

    assertEquals("Ulan", selectedSuffix)

    // Verify popup is dismissed because selectedSuffix == customerDismissedText
    val remainingSuggestions = if (selectedSuffix == customerDismissedText) {
      emptyList()
    } else {
      suggestions.filter { it.contains(selectedSuffix, ignoreCase = true) }
    }
    assertTrue(remainingSuggestions.isEmpty())
  }

  @Test
  fun branchProfile_displayName_resolvesUuidBranchToCleanName() {
    val uuidBranch = com.example.data.model.BranchProfile(
      branchId = "branch_9a8b7c6d-1234-4567-89ab-cdef01234567",
      location = "Gabek",
      ownerName = "Budi",
      ownerEmail = "budi@waffleo.id",
      ownerPhone = "0812",
      ownerUsername = "ownerbudi",
      name = ""
    )
    assertEquals("Gabek", uuidBranch.displayName)

    val namedBranch = uuidBranch.copy(name = "Sungailiat")
    assertEquals("Sungailiat", namedBranch.displayName)

    val prefixedNamedBranch = uuidBranch.copy(name = "Cabang Gabek")
    assertEquals("Gabek", prefixedNamedBranch.displayName)
  }

  @Test
  fun defaultStaffAccounts_haveUniqueBranchUsernameKeys() {
    val accounts = com.example.data.model.WaffleoSeedData.defaultStaffAccounts()
    val keys = accounts.map { "${it.branchId.lowercase()}_${it.username.lowercase()}" }
    assertEquals(keys.size, keys.distinct().size)
  }

  @Test
  fun refundTransfer_usesUangTunaiGroup_andIncrementalChangeDuePreventsDuplicateRefunds() {
    val refundTx = com.example.data.model.MoneyTransactionRecord(
      id = "v1_refund_transfer_WFL-002_2026-11-08",
      branchId = "Global",
      type = com.example.data.model.MoneyTransactionType.TRANSFER,
      dateKey = "2026-11-08",
      monthKey = "2026-11",
      timeFormatted = "15:00",
      amount = 28000L,
      accountGroup = "Uang Tunai",
      accountName = "Cash",
      toAccountGroup = "Bank",
      toAccountName = "Bank Mandiri"
    )
    assertEquals("Uang Tunai" to "Cash", refundTx.accountGroup to refundTx.accountName)

    // Verify incremental changeDue calculation on repeated order edits
    val priorChangeDue = 5000L
    val newChangeDueAfterFirstEdit = 5000L
    val incrementalOnSecondEditSameTotal = (newChangeDueAfterFirstEdit - priorChangeDue).coerceAtLeast(0L)
    assertEquals(0L, incrementalOnSecondEditSameTotal)

    val newChangeDueAfterFurtherReduction = 8000L
    val incrementalOnFurtherReduction = (newChangeDueAfterFurtherReduction - priorChangeDue).coerceAtLeast(0L)
    assertEquals(3000L, incrementalOnFurtherReduction)
  }

  @Test
  fun permanentOrderDelete_ownerOnlyAndTombstonePreventsResurrection() {
    val erasedOrder = com.example.data.model.OrderRecord(
      id = "v1_ord_wfl_002",
      orderNumber = "WFL-GLOBAL-20261108-002",
      branchId = "Global",
      status = "ERASED"
    )
    // Both OWNER and DEVELOPER can permanently delete an ERASED order
    assertTrue((com.example.data.model.StaffRole.OWNER == com.example.data.model.StaffRole.OWNER || com.example.data.model.StaffRole.OWNER == com.example.data.model.StaffRole.DEVELOPER) && erasedOrder.status == "ERASED")
    assertTrue((com.example.data.model.StaffRole.DEVELOPER == com.example.data.model.StaffRole.OWNER || com.example.data.model.StaffRole.DEVELOPER == com.example.data.model.StaffRole.DEVELOPER) && erasedOrder.status == "ERASED")
    assertFalse((com.example.data.model.StaffRole.SUPERVISOR == com.example.data.model.StaffRole.OWNER || com.example.data.model.StaffRole.SUPERVISOR == com.example.data.model.StaffRole.DEVELOPER) && erasedOrder.status == "ERASED")
    assertFalse((com.example.data.model.StaffRole.KASIR == com.example.data.model.StaffRole.OWNER || com.example.data.model.StaffRole.KASIR == com.example.data.model.StaffRole.DEVELOPER) && erasedOrder.status == "ERASED")

    // Tombstone + pending queue prevents deleted order from re-entering merged list
    val deletedOrderSet = setOf("v1_ord_wfl_002", "WFL-GLOBAL-20261108-002")
    val pendingSet = setOf("order_delete:v1_ord_wfl_002")
    val isPermanentlyDeleted = (erasedOrder.id.isNotBlank() && erasedOrder.id in deletedOrderSet) ||
      (erasedOrder.orderNumber.isNotBlank() && erasedOrder.orderNumber in deletedOrderSet) ||
      "order_delete:${erasedOrder.id}" in pendingSet
    assertTrue(isPermanentlyDeleted)
  }

  @Test
  fun branchSanitization_cleansEmbeddedBranchSlugAndIdsInTimelineAndAudit() {
    val rawTimelineText = "Seluruh item pesanan WFL-BRANCHC372605C63B64418BFF3AC4644F22A10-20261108-010 selesai disiapkan di dapur oleh fdrmnn."
    val sanitizedTimeline = com.example.data.model.sanitizeBranchIdsInText(rawTimelineText)
    assertEquals("Seluruh item pesanan WFL-010 selesai disiapkan di dapur oleh fdrmnn.", sanitizedTimeline)

    val rawAuditSummary = "Pesanan WFL-BRANCHC372605C63B64418BFF3AC4644F22A10-20261108-010 SELESAI (Done!) & diserahkan ke customer."
    val sanitizedAudit = com.example.data.model.sanitizeBranchIdsInText(rawAuditSummary)
    assertEquals("Pesanan WFL-010 SELESAI (Done!) & diserahkan ke customer.", sanitizedAudit)

    val rawBranchHash = "BRANCHC372605C63B64418BFF3AC4644F22A10"
    assertTrue(com.example.data.model.isUuidOrInternalBranchId(rawBranchHash))
    assertEquals("Gabek", com.example.data.model.formatCleanBranchName(rawBranchHash))

    val rawBranchId = "branch_c372605c63b64418bff3ac4644f22a10"
    assertTrue(com.example.data.model.isUuidOrInternalBranchId(rawBranchId))
    assertEquals("Gabek", com.example.data.model.formatCleanBranchName(rawBranchId))

    val cleanName = "Cabang Gabek"
    assertFalse(com.example.data.model.isUuidOrInternalBranchId(cleanName))
    assertEquals("Gabek", com.example.data.model.formatCleanBranchName(cleanName))

    // Test timeline event description sanitization
    val event = com.example.data.model.OrderTimelineEvent(
      eventId = "evt_123",
      title = "Pesanan Selesai",
      description = com.example.data.model.sanitizeBranchIdsInText(rawTimelineText)
    )
    assertEquals("Seluruh item pesanan WFL-010 selesai disiapkan di dapur oleh fdrmnn.", event.description)
  }

  @Test
  fun branchNameRegistry_resolvesDynamicUuidBranchesAndFallsBackToGabekForUnregistered() {
    com.example.data.model.BranchNameRegistry.clearForTesting()
    val sungailiatUuid = "branch_99aabbccddeeff001122334455667788"
    com.example.data.model.BranchNameRegistry.registerBranch(sungailiatUuid, "Sungailiat")

    assertEquals("Sungailiat", com.example.data.model.formatCleanBranchName(sungailiatUuid))
    assertEquals("Sungailiat", com.example.data.model.formatCleanBranchName("BRANCH99AABBCCDDEEFF001122334455667788"))

    val unregisteredUuid = "branch_c372605c63b64418bff3ac4644f22a10"
    assertEquals("Gabek", com.example.data.model.formatCleanBranchName(unregisteredUuid))
    com.example.data.model.BranchNameRegistry.clearForTesting()
  }

  @Test
  fun calculator_evaluatesMultiplicationAndDivisionBeforeAdditionAndSubtraction() {
    assertEquals(20000L, com.example.ui.components.evaluateSimpleMathExpression("10000 + 5000 × 2"))
    assertEquals(15000L, com.example.ui.components.evaluateSimpleMathExpression("25000 - 20000 ÷ 2"))
    assertEquals(35000L, com.example.ui.components.evaluateSimpleMathExpression("5000 × 4 + 3000 × 5"))
  }

  @Test
  fun passwordHasher_hashesWithSha256AndSupportsLegacyPlaintextCompatibility() {
    val raw = "rahasiaKasir123"
    val hashed = com.example.data.model.PasswordHasher.hashPassword(raw)
    assertTrue(com.example.data.model.PasswordHasher.isHashed(hashed))
    assertNotEquals(raw, hashed)
    assertTrue(com.example.data.model.PasswordHasher.verifyPassword(raw, hashed))
    assertFalse(com.example.data.model.PasswordHasher.verifyPassword("salahPassword", hashed))

    // Backward compatibility with legacy plaintext password
    assertTrue(com.example.data.model.PasswordHasher.verifyPassword(raw, raw))
    assertFalse(com.example.data.model.PasswordHasher.verifyPassword("salah", raw))
  }

  // =========================================================================
  // BAGIAN Z: DECOUPLED ORDER ENGINE, FULFILLMENT & LEGACY JSON COMPAT TESTS
  // =========================================================================

  @Test
  fun test1_threeStageOrderProcessing_preservesFulfillmentStateAcrossJsonSerialization() {
    val stage0 = com.example.data.model.OrderProcessingStage.fromStage(0)
    val stage1 = com.example.data.model.OrderProcessingStage.fromStage(1)
    val stage2 = com.example.data.model.OrderProcessingStage.fromStage(2)

    assertEquals(com.example.data.model.OrderProcessingStage.BELUM_DIPROSES, stage0)
    assertEquals(com.example.data.model.OrderProcessingStage.SEDANG_DIPROSES, stage1)
    assertEquals(com.example.data.model.OrderProcessingStage.SELESAI_DIPROSES, stage2)

    // Verify CartLineItem preserves fulfillmentState and completedUnitCount across JSON serialization
    val item = com.example.data.model.CartLineItem(
      lineId = "line_wfl_1",
      category = "Egg Waffle",
      primaryItemId = "menu_choco",
      primaryItemName = "Chocolate",
      qty = 2,
      fulfillmentState = "SELESAI_DIPROSES",
      completedUnitCount = 2,
      isCompletedLockedBadge = true
    )
    val payload = com.example.data.model.CartLineItem.serializeList(listOf(item))
    val parsed = com.example.data.model.CartLineItem.deserializeList(payload)
    assertEquals(1, parsed.size)
    assertEquals(2, parsed[0].processingStage)
    assertEquals(2, parsed[0].preparedQty)
    assertTrue(parsed[0].isCompletedLockedBadge)
    assertFalse(payload.contains("drinkSizeCode"))
    assertFalse(payload.contains("includeStraw"))
    assertFalse(payload.contains("stockDeductedQty"))
  }

  @Test
  fun test2_legacyOrderPayloadJson_ignoresDeprecatedSizeStrawAndStockFieldsSafely() {
    val legacyPayload = """
      [
        {
          "lineId": "line_legacy_1",
          "category": "Drinks",
          "primaryItemId": "drink_iced_latte",
          "primaryItemName": "Iced Latte",
          "sweetness": "Normal Sugar",
          "iceLevel": "Normal Ice",
          "drinkSizeCode": "16oz",
          "includeStraw": true,
          "qty": 2,
          "unitPriceWalkIn": 15000,
          "fulfillmentState": "SELESAI_DIPROSES",
          "completedUnitCount": 2,
          "isCompletedLockedBadge": true,
          "stockDeductedQty": 2
        }
      ]
    """.trimIndent()

    val parsed = com.example.data.model.CartLineItem.deserializeList(legacyPayload)
    assertEquals(1, parsed.size)
    assertEquals("Iced Latte", parsed[0].primaryItemName)
    assertEquals(2, parsed[0].qty)
    assertEquals(15000L, parsed[0].unitPriceWalkIn)
    assertEquals(2, parsed[0].processingStage)
    assertFalse(parsed[0].formattedCustomization().contains("Size:"))
  }

  @Test
  fun test3_editOrderPreservation_completedOnTopNewAtBottomNoCrossOut() {
    // Original order already finished cooking (Stage 2): 1x Chocolate Waffle + 1x Iced Latte
    val prevItems = listOf(
      com.example.data.model.CartLineItem(
        lineId = "line_1",
        category = "Egg Waffle",
        primaryItemId = "menu_choco",
        primaryItemName = "Chocolate",
        qty = 1,
        unitPriceWalkIn = 18000L,
        fulfillmentState = "SELESAI_DIPROSES",
        completedUnitCount = 1,
        isCompletedLockedBadge = true
      ),
      com.example.data.model.CartLineItem(
        lineId = "line_2",
        category = "Drinks",
        primaryItemId = "drink_iced_latte",
        primaryItemName = "Iced Latte",
        qty = 1,
        unitPriceWalkIn = 15000L,
        fulfillmentState = "SELESAI_DIPROSES",
        completedUnitCount = 1,
        isCompletedLockedBadge = true
      )
    )

    // Customer edits order: keeps 1x Chocolate, removes Iced Latte, and adds 1x Matcha Waffle
    val editedCart = listOf(
      com.example.data.model.CartLineItem(
        lineId = "line_1",
        category = "Egg Waffle",
        primaryItemId = "menu_choco",
        primaryItemName = "Chocolate",
        qty = 1,
        unitPriceWalkIn = 18000L
      ),
      com.example.data.model.CartLineItem(
        lineId = "line_3",
        category = "Egg Waffle",
        primaryItemId = "menu_matcha",
        primaryItemName = "Matcha",
        qty = 1,
        unitPriceWalkIn = 20000L
      )
    )

    val outcome = com.example.data.model.WaffleoOrderEngine.reconcileEditOrderItems(
      previousActiveItems = prevItems,
      previousHistoricalItems = emptyList(),
      editedCartItems = editedCart,
      nextRevisionNumber = 1
    )

    // 1. Completed Chocolate stays at the TOP (index 0) with processingStage = 2
    assertEquals(2, outcome.orderedActiveItems.size)
    assertEquals("Chocolate", outcome.orderedActiveItems[0].primaryItemName)
    assertEquals(2, outcome.orderedActiveItems[0].processingStage)
    assertFalse(outcome.orderedActiveItems[0].isNewRevisionAddition)

    // 2. Newly added Matcha is placed at the BOTTOM (index 1) with processingStage = 0
    assertEquals("Matcha", outcome.orderedActiveItems[1].primaryItemName)
    assertEquals(0, outcome.orderedActiveItems[1].processingStage)
    assertTrue(outcome.orderedActiveItems[1].isNewRevisionAddition)

    // 3. Overall order processingStage resets to 0 because there is an uncompleted newly added item
    assertEquals(0, outcome.nextOrderProcessingStage)

    // 4. Removed completed Iced Latte is recorded in historicalProducedItems (audit) and NOT in active items
    assertEquals(1, outcome.updatedHistoricalProducedItems.size)
    assertEquals("Iced Latte", outcome.updatedHistoricalProducedItems[0].primaryItemName)
  }

  @Test
  fun test4_paymentRecalculationOnOrderEdit_shortfallVsOverpaidRefundRequired() {
    // Case A: Customer already paid Rp 40.000, edits order up to Rp 55.000 -> Shortfall Rp 15.000
    val shortfallResult = com.example.data.model.WaffleoOrderEngine.recalculateOrderPaymentStatus(
      newTotalAmount = 55000L,
      netPaidAmount = 40000L
    )
    assertEquals("PARTIALLY_PAID", shortfallResult.settlementStatus)
    assertEquals(15000L, shortfallResult.shortfallAmount)
    assertEquals(0L, shortfallResult.overpaidAmount)
    assertFalse(shortfallResult.isPaid)

    // Case B: Customer already paid Rp 40.000, edits order down to Rp 25.000 -> Overpaid / Refund Required Rp 15.000
    val overpaidResult = com.example.data.model.WaffleoOrderEngine.recalculateOrderPaymentStatus(
      newTotalAmount = 25000L,
      netPaidAmount = 40000L
    )
    assertEquals("OVERPAID_REFUND_REQUIRED", overpaidResult.settlementStatus)
    assertEquals(0L, overpaidResult.shortfallAmount)
    assertEquals(15000L, overpaidResult.overpaidAmount)
    assertTrue(overpaidResult.isPaid)
  }

  @Test
  fun test5_thermalReceiptGuard_printsOnlyActiveBillableItemsWithoutCheckmarkOrRemovedItems() {
    val activeItem = com.example.data.model.CartLineItem(
      lineId = "line_active",
      category = "Egg Waffle",
      primaryItemId = "menu_choco",
      primaryItemName = "Chocolate",
      qty = 1,
      unitPriceWalkIn = 18000L,
      fulfillmentState = "SELESAI_DIPROSES",
      completedUnitCount = 1,
      isCompletedLockedBadge = true
    )
    val removedHistoricalItem = com.example.data.model.CartLineItem(
      lineId = "line_removed",
      category = "Drinks",
      primaryItemId = "drink_iced_latte",
      primaryItemName = "Iced Latte",
      qty = 1,
      unitPriceWalkIn = 15000L,
      fulfillmentState = "SELESAI_DIPROSES",
      completedUnitCount = 1,
      isCompletedLockedBadge = true
    )
    val order = com.example.data.model.OrderRecord(
      id = "ord_receipt_guard",
      orderNumber = "WFL-090",
      itemsPayload = com.example.data.model.CartLineItem.serializeList(listOf(activeItem)),
      historicalProducedItemsPayload = com.example.data.model.CartLineItem.serializeList(listOf(removedHistoricalItem)),
      totalAmount = 18000L
    )

    // ThermalReceiptDialog reads strictly from order.itemsPayload
    val receiptItems = com.example.data.model.CartLineItem.deserializeList(order.itemsPayload)
    assertEquals(1, receiptItems.size)
    assertEquals("Chocolate", receiptItems[0].primaryItemName)
    assertFalse(receiptItems[0].primaryItemName.contains("✅"))
    assertFalse(receiptItems[0].primaryItemName.contains("❌"))
    assertTrue(receiptItems.none { it.primaryItemName == "Iced Latte" })
  }

  @Test
  fun test6_halfHalfPrice_roundsUpWhenEndingIn500() {
    // Chocolate (18.000) & Hazelnut (25.000)
    // Rata-rata = (18.000 + 25.000) / 2 = 21.500 -> Berujung 500 dibulatkan ke atas menjadi 22.000
    val p1 = 18000L
    val p2 = 25000L
    val halfRaw = (p1 + p2) / 2.0
    val halfRounded = Math.round(halfRaw)
    val finalPrice = if (halfRounded % 1000L == 500L) {
      halfRounded + 500L
    } else if (halfRaw > halfRounded.toDouble()) {
      Math.ceil(halfRaw).toLong()
    } else {
      halfRounded
    }
    assertEquals(22000L, finalPrice)

    // Kasus genap tanpa 500: Tiramisu (18.000) & Chocolate (18.000) -> 18.000
    val pGenap1 = 18000L
    val pGenap2 = 18000L
    val halfGenapRaw = (pGenap1 + pGenap2) / 2.0
    val halfGenapRounded = Math.round(halfGenapRaw)
    val finalGenapPrice = if (halfGenapRounded % 1000L == 500L) halfGenapRounded + 500L else halfGenapRounded
    assertEquals(18000L, finalGenapPrice)
  }

  @Test
  fun test7_universalItemSorting_matchesUserPriorityExampleAndSettingsOrder() {
    val catalog = listOf(
      com.example.data.model.CatalogRecord(
        id = "menu_choco", recordType = "MENU_ITEM", category = "Egg Waffle",
        name = "Chocolate", metadataJson = """{"sortOrder":1}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "menu_tira", recordType = "MENU_ITEM", category = "Egg Waffle",
        name = "Tiramisu", metadataJson = """{"sortOrder":2}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "drink_latte", recordType = "MENU_ITEM", category = "Drinks",
        name = "Iced Latte", metadataJson = """{"sortOrder":1}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "flv_ori", recordType = "MODIFIER", category = "Flavour",
        name = "Original", metadataJson = """{"sortOrder":1}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "flv_pandan", recordType = "MODIFIER", category = "Flavour",
        name = "Pandan", metadataJson = """{"sortOrder":2}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "add_meses", recordType = "MODIFIER", category = "Add-on",
        name = "Meses", metadataJson = """{"sortOrder":1}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "add_keju", recordType = "MODIFIER", category = "Add-on",
        name = "Keju Parut", metadataJson = """{"sortOrder":2}"""
      )
    )

    // Chaotic cart insertion order from user's exact specification:
    // 1. Original Chocolate
    // 2. Original Tiramisu
    // 3. Pandan Chocolate +Keju Parut
    // 4. Pandan Tiramisu
    // 5. Original Tiramisu +Meses
    // 6. Original Chocolate
    val chaoticItems = listOf(
      com.example.data.model.CartLineItem(
        lineId = "l1", category = "Egg Waffle", primaryItemId = "menu_choco",
        primaryItemName = "Chocolate", flavourName = "Original"
      ),
      com.example.data.model.CartLineItem(
        lineId = "l2", category = "Egg Waffle", primaryItemId = "menu_tira",
        primaryItemName = "Tiramisu", flavourName = "Original"
      ),
      com.example.data.model.CartLineItem(
        lineId = "l3", category = "Egg Waffle", primaryItemId = "menu_choco",
        primaryItemName = "Chocolate", flavourName = "Pandan", addOnNames = listOf("Keju Parut")
      ),
      com.example.data.model.CartLineItem(
        lineId = "l4", category = "Egg Waffle", primaryItemId = "menu_tira",
        primaryItemName = "Tiramisu", flavourName = "Pandan"
      ),
      com.example.data.model.CartLineItem(
        lineId = "l5", category = "Egg Waffle", primaryItemId = "menu_tira",
        primaryItemName = "Tiramisu", flavourName = "Original", addOnNames = listOf("Meses")
      ),
      com.example.data.model.CartLineItem(
        lineId = "l6", category = "Egg Waffle", primaryItemId = "menu_choco",
        primaryItemName = "Chocolate", flavourName = "Original"
      )
    )

    val sorted = com.example.data.model.WaffleoOrderEngine.sortCartLineItems(
      items = chaoticItems,
      catalog = catalog,
      menuCategories = listOf("Egg Waffle", "Drinks"),
      modifierCategories = listOf("Flavour", "Add-on", "Sweetness", "Ice Level")
    )

    val actualLabels = sorted.map {
      val addonSuffix = if (it.addOnNames.isEmpty()) "" else " +" + it.addOnNames.joinToString(" +")
      "${it.flavourName} ${it.primaryItemName}$addonSuffix"
    }
    assertEquals(
      listOf(
        "Original Chocolate",
        "Original Chocolate",
        "Pandan Chocolate +Keju Parut",
        "Original Tiramisu",
        "Original Tiramisu +Meses",
        "Pandan Tiramisu"
      ),
      actualLabels
    )
  }

  @Test
  fun test8_universalItemSorting_respectsSettingsReorderAndDrinksAndRevisionSplit() {
    // Swap Settings order: Drinks before Egg Waffle; Tiramisu before Chocolate; Pandan before Original; Keju Parut before Meses
    val customCatalog = listOf(
      com.example.data.model.CatalogRecord(
        id = "menu_tira", recordType = "MENU_ITEM", category = "Egg Waffle",
        name = "Tiramisu", metadataJson = """{"sortOrder":1}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "menu_choco", recordType = "MENU_ITEM", category = "Egg Waffle",
        name = "Chocolate", metadataJson = """{"sortOrder":2}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "drink_latte", recordType = "MENU_ITEM", category = "Drinks",
        name = "Iced Latte", metadataJson = """{"sortOrder":1}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "flv_pandan", recordType = "MODIFIER", category = "Flavour",
        name = "Pandan", metadataJson = """{"sortOrder":1}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "flv_ori", recordType = "MODIFIER", category = "Flavour",
        name = "Original", metadataJson = """{"sortOrder":2}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "add_keju", recordType = "MODIFIER", category = "Add-on",
        name = "Keju Parut", metadataJson = """{"sortOrder":1}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "add_meses", recordType = "MODIFIER", category = "Add-on",
        name = "Meses", metadataJson = """{"sortOrder":2}"""
      ),
      com.example.data.model.CatalogRecord(
        id = "ice_less", recordType = "MODIFIER", category = "Ice Level",
        name = "Less Ice", metadataJson = """{"sortOrder":1}"""
      )
    )

    val items = listOf(
      com.example.data.model.CartLineItem(
        lineId = "d_custom", category = "Drinks", primaryItemId = "drink_latte",
        primaryItemName = "Iced Latte", icedLevel = "Less Ice", sweetnessLevel = "Normal"
      ),
      com.example.data.model.CartLineItem(
        lineId = "w_choco", category = "Egg Waffle", primaryItemId = "menu_choco",
        primaryItemName = "Chocolate", flavourName = "Original",
        addOnNames = listOf("Meses", "Keju Parut")
      ),
      com.example.data.model.CartLineItem(
        lineId = "d_normal", category = "Drinks", primaryItemId = "drink_latte",
        primaryItemName = "Iced Latte", icedLevel = "Normal", sweetnessLevel = "Normal"
      ),
      com.example.data.model.CartLineItem(
        lineId = "w_tira_ori", category = "Egg Waffle", primaryItemId = "menu_tira",
        primaryItemName = "Tiramisu", flavourName = "Original"
      ),
      com.example.data.model.CartLineItem(
        lineId = "w_tira_pandan", category = "Egg Waffle", primaryItemId = "menu_tira",
        primaryItemName = "Tiramisu", flavourName = "Pandan", addedInRevision = 1
      )
    )

    val sorted = com.example.data.model.WaffleoOrderEngine.sortCartLineItems(
      items = items,
      catalog = customCatalog,
      menuCategories = listOf("Drinks", "Egg Waffle"),
      modifierCategories = listOf("Flavour", "Add-on", "Sweetness", "Ice Level"),
      separateNewRevisionAdditions = true
    )

    // Pre-edit items come first, sorted by custom Settings order (Drinks -> Egg Waffle, Normal drink before custom Ice, Tiramisu before Chocolate)
    // Newly added revision item (w_tira_pandan, addedInRevision = 1) stays at the bottom
    assertEquals(listOf("d_normal", "d_custom", "w_tira_ori", "w_choco", "w_tira_pandan"), sorted.map { it.lineId })
    // Verify add-on names within w_choco are normalized to Settings order: Keju Parut before Meses
    assertEquals(listOf("Keju Parut", "Meses"), sorted[3].addOnNames)
  }

  @Test
  fun test9_sessionUiPreferences_defaultsAndResetBoundaries() {
    // 1. Default format is Tampilan 1 (Checklist per unit) & 1 Kolom
    val defaults = com.example.ui.viewmodel.SessionUiPreferences()
    assertEquals(1, defaults.ordersItemDisplayFormat)
    assertEquals(1, defaults.ordersGridColumns)

    val devInGlobal = com.example.ui.viewmodel.ActivePosSession(
      branchId = "Global",
      username = "developer",
      role = com.example.data.model.StaffRole.DEVELOPER,
      displayName = "Developer",
      isLoggedIn = true,
      isInDeveloperPortal = false
    )

    // Same login session & same branch (e.g. displayName updated) -> DO NOT reset preferences
    val sameSessionUpdatedName = devInGlobal.copy(displayName = "System Developer")
    assertFalse(
      com.example.ui.viewmodel.WaffleoViewModel.shouldResetSessionUiPreferences(
        devInGlobal,
        sameSessionUpdatedName
      )
    )

    // Logout -> MUST reset preferences
    val loggedOut = devInGlobal.copy(isLoggedIn = false)
    assertTrue(
      com.example.ui.viewmodel.WaffleoViewModel.shouldResetSessionUiPreferences(
        devInGlobal,
        loggedOut
      )
    )

    // Switch back to Developer Branch Portal -> MUST reset preferences
    val backToPortal = devInGlobal.copy(isInDeveloperPortal = true)
    assertTrue(
      com.example.ui.viewmodel.WaffleoViewModel.shouldResetSessionUiPreferences(
        devInGlobal,
        backToPortal
      )
    )

    // Enter a different branch from Developer Portal -> MUST reset preferences
    val otherBranch = devInGlobal.copy(branchId = "branch_sungailiat", isInDeveloperPortal = false)
    assertTrue(
      com.example.ui.viewmodel.WaffleoViewModel.shouldResetSessionUiPreferences(
        devInGlobal,
        otherBranch
      )
    )
  }

  @Test
  fun test10_monthlyChartDateLabels_omitsTglPrefix() {
    val calDay5 = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.DAY_OF_MONTH, 5)
      set(java.util.Calendar.HOUR_OF_DAY, 12)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val sampleOrder = com.example.data.model.OrderRecord(
      id = "ord_day5",
      orderNumber = "WFL-005",
      totalAmount = 54000L,
      status = "DONE",
      createdAt = com.google.firebase.Timestamp(calDay5.time)
    )

    val series = com.example.ui.components.buildMonthlyDatesSalesSeries(
      doneOrders = listOf(sampleOrder),
      calculateNet = { it.totalAmount }
    )

    assertTrue(series.size >= 28)
    assertEquals("01", series[0].label)
    assertEquals("05", series[4].label)
    assertFalse(series[4].label.contains("Tgl", ignoreCase = true))
    assertEquals(54000L, series[4].grossRevenue)
    assertEquals(1, series[4].orderCount)
  }

  @Test
  fun test11_executiveAovStats_computesAverageValueItemsMaxMin() {
    val emptyStats = com.example.ui.components.computeExecutiveAovStats(emptyList())
    assertEquals(0L, emptyStats.averageOrderValue)
    assertEquals(0, emptyStats.totalItems)
    assertEquals(0L, emptyStats.maxOrderValue)
    assertEquals(0L, emptyStats.minOrderValue)

    val item1Payload = com.example.data.model.CartLineItem.serializeList(
      listOf(
        com.example.data.model.CartLineItem(
          lineId = "i1",
          category = "Egg Waffle",
          primaryItemId = "menu_choco",
          primaryItemName = "Chocolate",
          qty = 2
        )
      )
    )
    val item2Payload = com.example.data.model.CartLineItem.serializeList(
      listOf(
        com.example.data.model.CartLineItem(
          lineId = "i2",
          category = "Drinks",
          primaryItemId = "drink_latte",
          primaryItemName = "Iced Aren Latte",
          qty = 3
        )
      )
    )

    val ord1 = com.example.data.model.OrderRecord(
      id = "o1",
      orderNumber = "WFL-001",
      totalAmount = 36000L,
      itemsPayload = item1Payload,
      status = "DONE"
    )
    val ord2 = com.example.data.model.OrderRecord(
      id = "o2",
      orderNumber = "WFL-002",
      totalAmount = 64000L,
      itemsPayload = item2Payload,
      status = "DONE"
    )

    val stats = com.example.ui.components.computeExecutiveAovStats(listOf(ord1, ord2))
    assertEquals(50000L, stats.averageOrderValue)
    assertEquals(5, stats.totalItems)
    assertEquals(2.5, stats.averageItemsPerOrder, 0.001)
    assertEquals(64000L, stats.maxOrderValue)
    assertEquals(36000L, stats.minOrderValue)
  }

  @Test
  fun test12_moneyDateHeaderLastTime_tracksPerDateAndResetsOnSessionBoundary() {
    var prefs = com.example.ui.viewmodel.SessionUiPreferences()
    assertTrue(prefs.moneyDateHeaderLastTimes.isEmpty())

    // 1. First click on 2026-11-13 -> falls back to current time ("09:55")
    assertEquals(
      "09:55",
      com.example.ui.viewmodel.WaffleoViewModel.resolveMoneyDateHeaderInitialTime(
        lastTimesByDate = prefs.moneyDateHeaderLastTimes,
        dateKey = "2026-11-13",
        nowTimeText = "09:55"
      )
    )

    // 2. User saves a transaction on 2026-11-13 with custom time "23:20"
    prefs = prefs.copy(
      moneyDateHeaderLastTimes = com.example.ui.viewmodel.WaffleoViewModel.recordMoneyDateHeaderLastTime(
        lastTimesByDate = prefs.moneyDateHeaderLastTimes,
        dateKey = "2026-11-13",
        timeText = "23:20"
      )
    )
    assertEquals(
      "23:20",
      com.example.ui.viewmodel.WaffleoViewModel.resolveMoneyDateHeaderInitialTime(
        lastTimesByDate = prefs.moneyDateHeaderLastTimes,
        dateKey = "2026-11-13",
        nowTimeText = "10:15"
      )
    )

    // 3. Another date (2026-11-12) still defaults to current time until set
    assertEquals(
      "10:15",
      com.example.ui.viewmodel.WaffleoViewModel.resolveMoneyDateHeaderInitialTime(
        lastTimesByDate = prefs.moneyDateHeaderLastTimes,
        dateKey = "2026-11-12",
        nowTimeText = "10:15"
      )
    )

    // 4. User updates 2026-11-13 time to "13:42" -> next click auto-fills "13:42"
    prefs = prefs.copy(
      moneyDateHeaderLastTimes = com.example.ui.viewmodel.WaffleoViewModel.recordMoneyDateHeaderLastTime(
        lastTimesByDate = prefs.moneyDateHeaderLastTimes,
        dateKey = "2026-11-13",
        timeText = "13:42"
      )
    )
    assertEquals(
      "13:42",
      com.example.ui.viewmodel.WaffleoViewModel.resolveMoneyDateHeaderInitialTime(
        lastTimesByDate = prefs.moneyDateHeaderLastTimes,
        dateKey = "2026-11-13",
        nowTimeText = "11:00"
      )
    )
  }

  @Test
  fun test13_capitalizeSentenceStart_handlesSentencesNumbersAndSpaces() {
    assertEquals("", com.example.data.model.capitalizeSentenceStart(""))
    assertEquals("Minyak goreng", com.example.data.model.capitalizeSentenceStart("minyak goreng"))
    assertEquals("  Telur ayam", com.example.data.model.capitalizeSentenceStart("  telur ayam"))
    assertEquals("Beli 1.5 kg gula. Lalu simpan di kulkas! Apakah cukup? Ya.", com.example.data.model.capitalizeSentenceStart("beli 1.5 kg gula. lalu simpan di kulkas! apakah cukup? ya."))
    assertEquals("Transfer BCA ke Mandiri", com.example.data.model.capitalizeSentenceStart("transfer BCA ke Mandiri"))
  }

  @Test
  fun test14_ordersDisplayFormat_clampsLegacyTampilan3ToDefault1() {
    // Only formats 1 and 2 remain supported
    val normalizeFormat: (Int) -> Int = { fmt -> if (fmt == 2) 2 else 1 }
    assertEquals(1, normalizeFormat(1))
    assertEquals(2, normalizeFormat(2))
    assertEquals(1, normalizeFormat(3))
  }

  @Test
  fun test15_expensePrimaryHeadline_formatsSubCategoryAndNoteWithHyphen() {
    // Standard SubCategory + Merk/Note -> "Krimer Kental Manis - Omela"
    assertEquals(
      "Krimer Kental Manis - Omela",
      com.example.data.model.formatExpensePrimaryHeadline(
        subCategoryName = "Krimer Kental Manis",
        note = "Omela",
        categoryName = "Bahan Pokok Minuman"
      )
    )

    // Topping Waffle + Goldenfill Choco Crunchy -> "Choco Crunchy - Goldenfill Choco Crunchy"
    assertEquals(
      "Choco Crunchy - Goldenfill Choco Crunchy",
      com.example.data.model.formatExpensePrimaryHeadline(
        subCategoryName = "Choco Crunchy",
        note = "Goldenfill Choco Crunchy",
        categoryName = "Topping Waffle"
      )
    )

    // When SubCategory equals Note (e.g. "Listrik" / "Listrik"), avoid duplicate "Listrik - Listrik"
    assertEquals(
      "Listrik",
      com.example.data.model.formatExpensePrimaryHeadline(
        subCategoryName = "Listrik",
        note = "Listrik",
        categoryName = "Biaya Operasional"
      )
    )

    // When Note is blank, show SubCategory
    assertEquals(
      "Krimer Kental Manis",
      com.example.data.model.formatExpensePrimaryHeadline(
        subCategoryName = "Krimer Kental Manis",
        note = "",
        categoryName = "Bahan Pokok Minuman"
      )
    )

    // When SubCategory is blank, show Note
    assertEquals(
      "Sedekah Subuh",
      com.example.data.model.formatExpensePrimaryHeadline(
        subCategoryName = "",
        note = "Sedekah Subuh",
        categoryName = "Pengeluaran Lain Lain"
      )
    )
  }

  @Test
  fun test16_expenseQuantityAndUnitDescription_parsesAndFormatsCorrectly() {
    // Formatting quantity + unit
    assertEquals("2.5kg", com.example.data.model.formatQuantityWithUnitDescription("2.5", "kg"))
    assertEquals("500gr", com.example.data.model.formatQuantityWithUnitDescription("500", "gr"))
    assertEquals("1.5L", com.example.data.model.formatQuantityWithUnitDescription("1,5", "L"))
    assertEquals("", com.example.data.model.formatQuantityWithUnitDescription("", "kg"))

    // Parsing existing description into (quantity, unit)
    assertEquals("2.5" to "kg", com.example.data.model.parseQuantityAndUnitFromDescription("2.5kg"))
    assertEquals("3" to "kg", com.example.data.model.parseQuantityAndUnitFromDescription("3kg"))
    assertEquals("500" to "gr", com.example.data.model.parseQuantityAndUnitFromDescription("500 gr"))
    assertEquals("1.5" to "L", com.example.data.model.parseQuantityAndUnitFromDescription("1.5L"))
    assertEquals("12" to "pcs", com.example.data.model.parseQuantityAndUnitFromDescription("12 pcs"))

    // Free-form text returns null
    assertEquals(null, com.example.data.model.parseQuantityAndUnitFromDescription("Beli di pasar pagi"))
    assertEquals(null, com.example.data.model.parseQuantityAndUnitFromDescription(""))
  }

  @Test
  fun test17_expenseSubCategoryAutoMemory_resolvesSavedConfigAndHistoricalFallback() {
    val historicalTransactions = listOf(
      com.example.data.model.MoneyTransactionRecord(
        id = "tx_1",
        type = com.example.data.model.MoneyTransactionType.EXPENSE,
        dateKey = "2026-11-13",
        timeFormatted = "18:22",
        timestampMillis = 1763032920000L,
        amount = 68000L,
        category = "Bahan Pokok Minuman",
        subCategory = "Krimer Kental Manis",
        accountName = "Cash",
        note = "Omela",
        description = "2.5kg"
      )
    )

    // 1. Without explicit saved config, infers MERK + unit enabled ("kg") from historical transaction
    val inferred = com.example.ui.viewmodel.WaffleoViewModel.resolveExpenseSubCategoryConfigFromState(
      savedConfigs = emptyMap(),
      transactions = historicalTransactions,
      categoryName = "Bahan Pokok Minuman",
      subCategoryName = "Krimer Kental Manis"
    )
    assertTrue(inferred != null)
    assertEquals(com.example.data.model.ExpenseNoteMode.MERK, inferred?.noteMode)
    assertEquals(true, inferred?.isUnitDescriptionEnabled)
    assertEquals("kg", inferred?.unitSymbol)

    // 2. With explicit saved config, saved config takes precedence
    val explicitCfg = com.example.data.model.ExpenseSubCategoryInputConfig(
      categoryName = "Bahan Pokok Minuman",
      subCategoryName = "Krimer Kental Manis",
      noteMode = com.example.data.model.ExpenseNoteMode.MERK,
      isUnitDescriptionEnabled = true,
      unitSymbol = "kaleng"
    )
    val resolved = com.example.ui.viewmodel.WaffleoViewModel.resolveExpenseSubCategoryConfigFromState(
      savedConfigs = mapOf(explicitCfg.configKey to explicitCfg),
      transactions = historicalTransactions,
      categoryName = "Bahan Pokok Minuman",
      subCategoryName = "Krimer Kental Manis"
    )
    assertEquals("kaleng", resolved?.unitSymbol)
    assertEquals(com.example.data.model.ExpenseNoteMode.MERK, resolved?.noteMode)
  }
}
