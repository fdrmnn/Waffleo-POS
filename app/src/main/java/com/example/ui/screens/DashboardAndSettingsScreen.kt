package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.BuildConfig
import com.example.data.model.StaffAccount
import com.example.data.model.BankAccountDetail
import com.example.data.model.DigitalWalletDetail
import com.example.data.model.canUserEditStaffAccount
import com.example.data.model.canUserDeleteStaffAccount
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.compose.runtime.CompositionLocalProvider
import com.example.data.model.AuditLogRecord
import com.example.data.model.CartLineItem
import com.example.data.model.CatalogRecord
import com.example.data.model.GroupedAuditLog
import com.example.data.model.OrderMedia
import com.example.data.model.OrderRecord
import com.example.data.model.StaffRole
import com.example.data.model.groupAuditLogRecords
import com.example.data.model.areCategoriesMatching
import com.example.data.model.normalizeCategoryCanonical
import com.example.ui.components.IngredientStockGaugeCard
import com.example.ui.components.WaffleoNominalCalculatorDialog
import com.example.ui.theme.AdonanPalette
import com.example.ui.components.DashboardBankSettlementDialog
import com.example.ui.components.DashboardLabaKotorDialog
import com.example.ui.components.DashboardOrdersListDialog
import com.example.ui.components.MediaDistributionDonutCard
import com.example.ui.components.ModernExecutiveKpiRow
import com.example.ui.components.PeriodSalesPoint
import com.example.ui.components.RechartsHourlyRevenueChartCard
import com.example.ui.components.RechartsPeriodRevenueChartCard
import com.example.ui.components.TopMenuPerformanceBarCard
import com.example.ui.components.PromoFormModalDialog
import com.example.ui.components.PromoManagementSection
import com.example.ui.components.WaffleoProductIllustration
import com.example.ui.components.WaffleoSingleSelectDropdown
import com.example.ui.components.WaffleoSoftTextField
import com.example.ui.components.buildAnnualMonthsSalesSeries
import com.example.ui.components.buildCustomRangeDailySalesSeries
import com.example.ui.components.buildCustomRangeMonthlySalesSeries
import com.example.ui.components.buildHourlySalesSeries
import com.example.ui.components.buildMonthlyDatesSalesSeries
import com.example.ui.components.buildWeeklyDaysSalesSeries
import com.example.ui.theme.ChampagneGoldAccent
import com.example.ui.theme.DeepCaramelGold
import com.example.ui.theme.ElevatedIvoryCard
import com.example.ui.theme.HoneyAmberWarning
import com.example.ui.theme.MatchaSageSuccess
import com.example.ui.theme.MutedCocoaText
import com.example.ui.theme.RichEspressoPrimary
import com.example.ui.theme.SoftAmberBg
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SoftSageBg
import com.example.ui.theme.SoftTerracottaBg
import com.example.ui.theme.SubtleSandBorder
import com.example.ui.theme.TerracottaDanger
import com.example.ui.viewmodel.HistoryFilterPeriod
import com.example.ui.viewmodel.WaffleoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.json.JSONObject

@Composable
fun DashboardScreen(
    viewModel: WaffleoViewModel
) {
    val allOrders by viewModel.orders.collectAsState()
    val catalog by viewModel.catalogRecords.collectAsState()
    val customMenuCategories by viewModel.customMenuCategories.collectAsState()
    val customModifierCategories by viewModel.customModifierCategories.collectAsState()
    val modifierToMenuCategoryMap by viewModel.modifierToMenuCategoryMap.collectAsState()
    val sessionState by viewModel.session.collectAsState()

    var selectedPeriod by remember { mutableStateOf(HistoryFilterPeriod.TODAY) }
    var customDays by remember { mutableIntStateOf(7) }

    var isCustomDateRangeActive by remember { mutableStateOf(false) }
    var customStartDateMillis by remember { mutableStateOf<Long?>(null) }
    var customEndDateMillis by remember { mutableStateOf<Long?>(null) }
    var showCustomDatePickerDialog by remember { mutableStateOf(false) }
    var showOmzetDialog by remember { mutableStateOf(false) }
    var showLabaKotorDialog by remember { mutableStateOf(false) }
    var showBcaSettlementDialog by remember { mutableStateOf(false) }
    var showMandiriSettlementDialog by remember { mutableStateOf(false) }

    val periodFilteredOrders = remember(
        allOrders, selectedPeriod, customDays, isCustomDateRangeActive, customStartDateMillis, customEndDateMillis
    ) {
        allOrders.filter { ord ->
            val resolvedDate = WaffleoViewModel.resolveOrderTimestampDate(ord)
            WaffleoViewModel.matchesPeriodFilterRange(
                timestamp = com.google.firebase.Timestamp(resolvedDate),
                period = selectedPeriod,
                customDaysBack = customDays,
                isCustomRangeActive = isCustomDateRangeActive,
                startDateMillis = customStartDateMillis,
                endDateMillis = customEndDateMillis
            )
        }
    }

    val doneOrders = remember(periodFilteredOrders) { periodFilteredOrders.filter { it.status == "DONE" } }
    val activeOrders = remember(periodFilteredOrders) { periodFilteredOrders.filter { it.status == "ACTIVE" } }
    val cancelledOrders = remember(periodFilteredOrders) { periodFilteredOrders.filter { it.status == "CANCELLED" } }

    val omzetPlatformGross = remember(doneOrders) { doneOrders.sumOf { it.totalAmount } }
    val totalDiskonPromo = remember(doneOrders) { doneOrders.sumOf { it.discountAmount } }
    val totalPotonganPlatform = remember(doneOrders, catalog) {
        doneOrders.sumOf { viewModel.calculateOrderPlatformFee(it, catalog) }
    }
    val uangMasukBersihSistem = remember(doneOrders, catalog) {
        doneOrders.sumOf { viewModel.calculateOrderNetAmount(it, catalog) }
    }
    val saldoMasukBcaPlatform = remember(doneOrders, catalog) {
        val platformNet = doneOrders
            .filter { it.orderMedia in listOf("GrabFood", "GoFood", "ShopeeFood") }
            .sumOf { viewModel.calculateOrderNetAmount(it, catalog) }
        val bcaDirectNet = doneOrders
            .filter { it.orderMedia !in listOf("GrabFood", "GoFood", "ShopeeFood") }
            .sumOf { ord ->
                val net = viewModel.calculateOrderNetAmount(ord, catalog)
                val method = ord.paymentMethod
                if (method.contains("BCA", ignoreCase = true)) {
                    val isSplit = method.contains("Cash + QRIS", ignoreCase = true) ||
                        (ord.splitQrisAmount > 0L && ord.splitCashAmount > 0L)
                    if (isSplit) {
                        if (ord.splitQrisAmount > 0L) ord.splitQrisAmount.coerceAtMost(net) else (net / 2)
                    } else net
                } else 0L
            }
        platformNet + bcaDirectNet
    }
    val saldoMasukMandiri = remember(doneOrders, catalog) {
        doneOrders.sumOf { ord ->
            if (ord.orderMedia in listOf("GrabFood", "GoFood", "ShopeeFood")) {
                0L
            } else {
                val net = viewModel.calculateOrderNetAmount(ord, catalog)
                val method = ord.paymentMethod
                val isSplit = method.contains("Cash + QRIS", ignoreCase = true) ||
                    (ord.splitQrisAmount > 0L && ord.splitCashAmount > 0L)
                val isBca = method.contains("BCA", ignoreCase = true)
                val isMandiri = !isBca // Default QRIS and Transfer in Waffleo is Bank Mandiri

                if (isSplit) {
                    val qrisPart = if (ord.splitQrisAmount > 0L) ord.splitQrisAmount.coerceAtMost(net) else (net / 2)
                    if (isMandiri) qrisPart else 0L
                } else if (method.contains("QRIS", ignoreCase = true) || method.contains("Transfer", ignoreCase = true) || method.contains("Mandiri", ignoreCase = true)) {
                    if (isMandiri) net else 0L
                } else {
                    0L
                }
            }
        }
    }
    // Estimasi Laba Kotor = Total Penjualan setelah Diskon dikurangi Biaya Platform / Fee Order (tanpa HPP Resep)
    val labaKotor = uangMasukBersihSistem.coerceAtLeast(0L)

    val periodBounds = remember(
        selectedPeriod, customDays, isCustomDateRangeActive, customStartDateMillis, customEndDateMillis
    ) {
        WaffleoViewModel.resolvePeriodBoundsMillis(
            period = selectedPeriod,
            customDaysBack = customDays,
            isCustomRangeActive = isCustomDateRangeActive,
            startDateMillis = customStartDateMillis,
            endDateMillis = customEndDateMillis
        )
    }

    val customRangeLabel = remember(selectedPeriod, customDays, isCustomDateRangeActive, periodBounds) {
        val (startMs, endMs) = periodBounds
        val cStart = Calendar.getInstance().apply { timeInMillis = startMs }
        val cEnd = Calendar.getInstance().apply { timeInMillis = endMs }
        val sameYear = cStart.get(Calendar.YEAR) == cEnd.get(Calendar.YEAR)
        val fmtStart = SimpleDateFormat(if (sameYear) "dd MMM" else "dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        val fmtEnd = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        val rangeDates = "${fmtStart.format(Date(startMs))} - ${fmtEnd.format(Date(endMs))}"
        if (selectedPeriod == HistoryFilterPeriod.CUSTOM && !isCustomDateRangeActive) {
            "$customDays Hari Terakhir • $rangeDates"
        } else {
            rangeDates
        }
    }

    val hourlySeries = remember(doneOrders, catalog, selectedPeriod) {
        val isMultiDay = selectedPeriod != HistoryFilterPeriod.TODAY &&
            selectedPeriod != HistoryFilterPeriod.YESTERDAY
        buildHourlySalesSeries(
            doneOrders = doneOrders,
            isMultiDayPeriod = isMultiDay
        ) { ord ->
            viewModel.calculateOrderNetAmount(ord, catalog)
        }
    }

    val customMonthlySeries = remember(doneOrders, catalog, selectedPeriod, periodBounds) {
        if (selectedPeriod == HistoryFilterPeriod.CUSTOM) {
            buildCustomRangeMonthlySalesSeries(
                doneOrders = doneOrders,
                startMillis = periodBounds.first,
                endMillis = periodBounds.second
            ) { ord ->
                viewModel.calculateOrderNetAmount(ord, catalog)
            }
        } else null
    }

    val secondaryChartInfo = remember(doneOrders, catalog, selectedPeriod, periodBounds, customRangeLabel) {
        val calculateNet: (OrderRecord) -> Long = { viewModel.calculateOrderNetAmount(it, catalog) }
        when (selectedPeriod) {
            HistoryFilterPeriod.TODAY, HistoryFilterPeriod.YESTERDAY -> null
            HistoryFilterPeriod.THIS_WEEK -> Triple(
                "Tren Pendapatan Mingguan per Hari",
                "Omzet & uang masuk bersih per hari (Senin - Minggu)",
                buildWeeklyDaysSalesSeries(doneOrders, calculateNet)
            )
            HistoryFilterPeriod.THIS_MONTH -> Triple(
                "Tren Pendapatan Bulanan per Tanggal",
                "Omzet & uang masuk bersih per tanggal (Tgl 01 - 31)",
                buildMonthlyDatesSalesSeries(doneOrders, calculateNet)
            )
            HistoryFilterPeriod.THIS_YEAR -> Triple(
                "Tren Pendapatan Tahunan per Bulan",
                "Omzet & uang masuk bersih per bulan (Jan - Des)",
                buildAnnualMonthsSalesSeries(doneOrders, calculateNet)
            )
            HistoryFilterPeriod.CUSTOM -> Triple(
                "Tren Pendapatan Custom per Tanggal",
                "Omzet & uang masuk bersih per tanggal sesuai rentang waktu ($customRangeLabel)",
                buildCustomRangeDailySalesSeries(
                    doneOrders = doneOrders,
                    startMillis = periodBounds.first,
                    endMillis = periodBounds.second,
                    calculateNet = calculateNet
                )
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 0. Top Header: Hanya tombol pemilihan periode di kanan atas (seperti Order History)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WaffleoSingleSelectDropdown(
                        label = "",
                        selectedText = selectedPeriod.label,
                        options = HistoryFilterPeriod.entries,
                        optionLabel = { it.label },
                        onOptionSelected = { selectedPeriod = it },
                        containerColor = ElevatedIvoryCard,
                        modifier = Modifier.width(160.dp),
                        testTagPrefix = "dashboard_period"
                    )

                    if (selectedPeriod == HistoryFilterPeriod.CUSTOM) {
                        val customOptions = listOf(
                            "3 Hari Terakhir",
                            "7 Hari Terakhir",
                            "14 Hari Terakhir",
                            "30 Hari Terakhir",
                            "60 Hari Terakhir",
                            "90 Hari Terakhir",
                            "Custom (Pilih Tanggal)"
                        )
                        val sdf = remember { SimpleDateFormat("dd MMM", Locale.forLanguageTag("id-ID")) }
                        val customDisplayText = if (isCustomDateRangeActive && customStartDateMillis != null && customEndDateMillis != null) {
                            "${sdf.format(Date(customStartDateMillis!!))} - ${sdf.format(Date(customEndDateMillis!!))}"
                        } else {
                            "$customDays Hari Terakhir"
                        }

                        WaffleoSingleSelectDropdown(
                            label = "",
                            selectedText = customDisplayText,
                            options = customOptions,
                            optionLabel = { it },
                            onOptionSelected = { choice ->
                                if (choice == "Custom (Pilih Tanggal)") {
                                    showCustomDatePickerDialog = true
                                } else {
                                    val days = choice.split(" ")[0].toIntOrNull() ?: 7
                                    customDays = days
                                    isCustomDateRangeActive = false
                                }
                            },
                            containerColor = ElevatedIvoryCard,
                            modifier = Modifier.width(180.dp),
                            testTagPrefix = "dashboard_custom_days"
                        )
                    }
                }
            }
        }

        // 1. Modern Executive KPI Row with Icon Badges & Margin %
        item {
            ModernExecutiveKpiRow(
                omzetGross = omzetPlatformGross,
                potonganPlatform = totalPotonganPlatform,
                totalDiskonPromo = totalDiskonPromo,
                labaKotor = labaKotor,
                saldoBcaPlatform = saldoMasukBcaPlatform,
                saldoMandiriPlatform = saldoMasukMandiri,
                doneCount = doneOrders.size,
                onOmzetClick = { showOmzetDialog = true },
                onLabaKotorClick = { showLabaKotorDialog = true },
                onBcaClick = { showBcaSettlementDialog = true },
                onMandiriClick = { showMandiriSettlementDialog = true }
            )
        }

        // 2. Side-by-Side Cards: Distribusi Kanal & Metode Pembayaran + Menu Terlaris
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                MediaDistributionDonutCard(
                    doneOrders = doneOrders,
                    catalog = catalog,
                    viewModel = viewModel,
                    periodLabel = selectedPeriod.label,
                    branchName = viewModel.getBranchDisplayName(sessionState.branchId),
                    modifier = Modifier.weight(1f)
                )

                TopMenuPerformanceBarCard(
                    doneOrders = doneOrders,
                    catalog = catalog,
                    customMenuCategories = customMenuCategories,
                    customModifierCategories = customModifierCategories,
                    modifierToMenuCategoryMap = modifierToMenuCategoryMap,
                    periodLabel = selectedPeriod.label,
                    branchName = viewModel.getBranchDisplayName(sessionState.branchId),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Primary Period Revenue Chart (Per Hari / Tanggal / Bulan) displayed if active
        if (secondaryChartInfo != null) {
            item {
                RechartsPeriodRevenueChartCard(
                    title = secondaryChartInfo.first,
                    subtitle = secondaryChartInfo.second,
                    periodSeries = secondaryChartInfo.third,
                    customMonthlySeries = customMonthlySeries,
                    customRangeLabel = customRangeLabel
                )
            }
        }

        // 4. Recharts-Style Interactive Flexible Hourly Revenue Spline & Area Chart
        item {
            RechartsHourlyRevenueChartCard(
                hourlySeries = hourlySeries,
                doneOrders = doneOrders,
                calculateNet = { ord -> viewModel.calculateOrderNetAmount(ord, catalog) },
                selectedPeriod = selectedPeriod,
                customRangeLabel = customRangeLabel
            )
        }

        // Diagram Kapasitas Stok Bahan Baku Resep disembunyikan sementara sesuai instruksi pemilik proyek
    }

    if (showCustomDatePickerDialog) {
        CustomDateRangePickerDialog(
            initialStartMillis = customStartDateMillis,
            initialEndMillis = customEndDateMillis,
            onDismiss = { showCustomDatePickerDialog = false },
            onConfirm = { startMs, endMs ->
                customStartDateMillis = startMs
                customEndDateMillis = endMs
                isCustomDateRangeActive = true
                showCustomDatePickerDialog = false
            }
        )
    }

    if (showOmzetDialog) {
        DashboardOrdersListDialog(
            periodLabel = selectedPeriod.label,
            orders = doneOrders,
            totalOmzet = omzetPlatformGross,
            onDismiss = { showOmzetDialog = false }
        )
    }

    if (showLabaKotorDialog) {
        DashboardLabaKotorDialog(
            periodLabel = selectedPeriod.label,
            omzetGross = omzetPlatformGross,
            potonganPlatform = totalPotonganPlatform,
            totalDiskonPromo = totalDiskonPromo,
            labaKotor = labaKotor,
            onDismiss = { showLabaKotorDialog = false }
        )
    }

    if (showBcaSettlementDialog) {
        DashboardBankSettlementDialog(
            bankName = "BCA",
            periodLabel = selectedPeriod.label,
            totalSettlement = saldoMasukBcaPlatform,
            orders = doneOrders,
            catalog = catalog,
            viewModel = viewModel,
            onDismiss = { showBcaSettlementDialog = false }
        )
    }

    if (showMandiriSettlementDialog) {
        DashboardBankSettlementDialog(
            bankName = "Mandiri",
            periodLabel = selectedPeriod.label,
            totalSettlement = saldoMasukMandiri,
            orders = doneOrders,
            catalog = catalog,
            viewModel = viewModel,
            onDismiss = { showMandiriSettlementDialog = false }
        )
    }
}

@Composable
internal fun CustomDateRangePickerDialog(
    initialStartMillis: Long?,
    initialEndMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit
) {
    val calNow = Calendar.getInstance()
    var startDay by remember {
        mutableIntStateOf(
            if (initialStartMillis != null) Calendar.getInstance().apply { timeInMillis = initialStartMillis }.get(Calendar.DAY_OF_MONTH)
            else calNow.get(Calendar.DAY_OF_MONTH)
        )
    }
    var startMonth by remember {
        mutableIntStateOf(
            if (initialStartMillis != null) Calendar.getInstance().apply { timeInMillis = initialStartMillis }.get(Calendar.MONTH)
            else calNow.get(Calendar.MONTH)
        )
    }
    var startYear by remember {
        mutableIntStateOf(
            if (initialStartMillis != null) Calendar.getInstance().apply { timeInMillis = initialStartMillis }.get(Calendar.YEAR)
            else calNow.get(Calendar.YEAR)
        )
    }

    var endDay by remember {
        mutableIntStateOf(
            if (initialEndMillis != null) Calendar.getInstance().apply { timeInMillis = initialEndMillis }.get(Calendar.DAY_OF_MONTH)
            else calNow.get(Calendar.DAY_OF_MONTH)
        )
    }
    var endMonth by remember {
        mutableIntStateOf(
            if (initialEndMillis != null) Calendar.getInstance().apply { timeInMillis = initialEndMillis }.get(Calendar.MONTH)
            else calNow.get(Calendar.MONTH)
        )
    }
    var endYear by remember {
        mutableIntStateOf(
            if (initialEndMillis != null) Calendar.getInstance().apply { timeInMillis = initialEndMillis }.get(Calendar.YEAR)
            else calNow.get(Calendar.YEAR)
        )
    }

    val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val years = (2020..2030).toList()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ElevatedIvoryCard,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pilih Rentang Tanggal Custom",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "Tentukan tanggal mulai dan tanggal selesai filter laporan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                    }
                }

                // Tanggal Mulai (Start Date)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftOatSurface, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tanggal Mulai (Start Date)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WaffleoSingleSelectDropdown(
                            label = "Tgl",
                            selectedText = String.format("%02d", startDay),
                            options = (1..31).toList(),
                            optionLabel = { String.format("%02d", it) },
                            onOptionSelected = { startDay = it },
                            modifier = Modifier.weight(0.8f)
                        )
                        WaffleoSingleSelectDropdown(
                            label = "Bulan",
                            selectedText = monthNames.getOrElse(startMonth) { "Januari" },
                            options = monthNames.indices.toList(),
                            optionLabel = { monthNames[it] },
                            onOptionSelected = { startMonth = it },
                            modifier = Modifier.weight(1.4f)
                        )
                        WaffleoSingleSelectDropdown(
                            label = "Tahun",
                            selectedText = startYear.toString(),
                            options = years,
                            optionLabel = { it.toString() },
                            onOptionSelected = { startYear = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Tanggal Selesai (End Date)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftOatSurface, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tanggal Selesai (End Date)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WaffleoSingleSelectDropdown(
                            label = "Tgl",
                            selectedText = String.format("%02d", endDay),
                            options = (1..31).toList(),
                            optionLabel = { String.format("%02d", it) },
                            onOptionSelected = { endDay = it },
                            modifier = Modifier.weight(0.8f)
                        )
                        WaffleoSingleSelectDropdown(
                            label = "Bulan",
                            selectedText = monthNames.getOrElse(endMonth) { "Januari" },
                            options = monthNames.indices.toList(),
                            optionLabel = { monthNames[it] },
                            onOptionSelected = { endMonth = it },
                            modifier = Modifier.weight(1.4f)
                        )
                        WaffleoSingleSelectDropdown(
                            label = "Tahun",
                            selectedText = endYear.toString(),
                            options = years,
                            optionLabel = { it.toString() },
                            onOptionSelected = { endYear = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftOatSurface, contentColor = RichEspressoPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val cStart = Calendar.getInstance().apply {
                                set(Calendar.YEAR, startYear)
                                set(Calendar.MONTH, startMonth)
                                set(Calendar.DAY_OF_MONTH, 1)
                                val maxStartDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                                set(Calendar.DAY_OF_MONTH, startDay.coerceIn(1, maxStartDay))
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val cEnd = Calendar.getInstance().apply {
                                set(Calendar.YEAR, endYear)
                                set(Calendar.MONTH, endMonth)
                                set(Calendar.DAY_OF_MONTH, 1)
                                val maxEndDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                                set(Calendar.DAY_OF_MONTH, endDay.coerceIn(1, maxEndDay))
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }
                            val finalStart = minOf(cStart.timeInMillis, cEnd.timeInMillis)
                            val finalEnd = maxOf(cStart.timeInMillis, cEnd.timeInMillis)
                            val normalizedStart = Calendar.getInstance().apply {
                                timeInMillis = finalStart
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            val normalizedEnd = Calendar.getInstance().apply {
                                timeInMillis = finalEnd
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }.timeInMillis
                            onConfirm(normalizedStart, normalizedEnd)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepCaramelGold, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Terapkan Filter", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class SettingsTabMeta(
    val sectionIndex: Int,
    val title: String,
    val icon: ImageVector
)

/**
 * Modern, clutter-free Settings Control Center:
 * - Single sleek top command bar with Icon+Label tabs, inline Search pill, and [+ Baru] action button
 * - Zero redundant explanatory paragraphs
 * - Multi-column visual card grids with instant toggle switches, quick restock actions, and clean price pills
 */
@Composable
fun SettingsScreen(
    viewModel: WaffleoViewModel
) {
    val session by viewModel.session.collectAsState()
    val catalog by viewModel.catalogRecords.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val staffAccounts by viewModel.staffAccounts.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val customMenuCategories by viewModel.customMenuCategories.collectAsState()
    val customModifierCategories by viewModel.customModifierCategories.collectAsState()
    val allOrdersList by viewModel.allOrders.collectAsState()

    val isAllowedToEdit = session.role.canManageCatalogAndDelete
    var selectedSection by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var subFilter by remember { mutableStateOf("ALL") }
    var menuSubFilter by remember { mutableStateOf("ALL") }
    var modifierSubFilter by remember { mutableStateOf("ALL") }

    val customSubCategoriesMap by viewModel.customSubCategories.collectAsState()

    val allMenuCategories = remember(catalog, customMenuCategories) {
        (customMenuCategories + catalog.filter { it.recordType == "MENU_ITEM" }.map { it.category })
            .filter { it.isNotBlank() }
            .distinctBy { normalizeCategoryCanonical(it) }
    }
    val allModifierCategories = remember(catalog, customModifierCategories) {
        (customModifierCategories + catalog.filter { it.recordType == "MODIFIER" }.map { it.category })
            .filter { it.isNotBlank() }
            .distinctBy { normalizeCategoryCanonical(it) }
    }

    val activePlatforms = remember(catalog) {
        catalog.filter { it.recordType == "BRANCH_CONFIG" && it.isActive }
            .map { it.category.ifBlank { it.name }.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    var editingCatalogItem by remember { mutableStateOf<CatalogRecord?>(null) }
    var showCreateModalForType by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showPromoFormModal by remember { mutableStateOf(false) }
    var editingPromoItem by remember { mutableStateOf<CatalogRecord?>(null) }
    var showAddCategoryForType by remember { mutableStateOf<String?>(null) }
    var editingCategoryForType by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAddSubCategoryForCategory by remember { mutableStateOf<String?>(null) }
    var editingSubCategoryForCategory by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAddStaffModal by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<StaffAccount?>(null) }
    var selectedGroupedLog by remember { mutableStateOf<GroupedAuditLog?>(null) }

    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val digitalWallets by viewModel.digitalWallets.collectAsState()

    var editingBankAccount by remember { mutableStateOf<BankAccountDetail?>(null) }
    var showAddBankModal by remember { mutableStateOf(false) }
    var editingDigitalWallet by remember { mutableStateOf<DigitalWalletDetail?>(null) }
    var showAddWalletModal by remember { mutableStateOf(false) }
    var generalDeleteConfirmTarget by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    var isReorderModeActive by remember { mutableStateOf(false) }

    // Note: Tab "Stok" (sectionIndex = 2) disimpan dulu (shelved) sesuai instruksi pemilik proyek
    val tabs = remember {
        listOf(
            SettingsTabMeta(0, "Menu", Icons.Default.RestaurantMenu),
            SettingsTabMeta(1, "Modifier", Icons.Default.Tune),
            // SettingsTabMeta(2, "Stok", Icons.Default.Inventory2), // Disimpan dulu (shelved)
            SettingsTabMeta(3, "Promo", Icons.Default.ConfirmationNumber),
            SettingsTabMeta(4, "Pengaturan Lainnya", Icons.Default.Storefront),
            SettingsTabMeta(5, "Audit Log", Icons.Default.History)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Unified Top Command Bar: [ Segmented Icon Tabs ]  +  [ Search Pill ]  +  [ Urutkan Button ]  +  [ + Baru Button ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Segmented Pill Navigation
            Surface(
                shape = RoundedCornerShape(50),
                color = ElevatedIvoryCard,
                shadowElevation = 3.dp,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabs.forEach { tab ->
                        val active = selectedSection == tab.sectionIndex
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    selectedSection = tab.sectionIndex
                                    subFilter = "ALL"
                                    menuSubFilter = "ALL"
                                    modifierSubFilter = "ALL"
                                    if (tab.sectionIndex !in 0..1) {
                                        isReorderModeActive = false
                                    }
                                }
                                .testTag("settings_tab_${tab.sectionIndex}"),
                            color = if (active) RichEspressoPrimary else Color.Transparent,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (active) ChampagneGoldAccent else MutedCocoaText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) ElevatedIvoryCard else RichEspressoPrimary,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Compact Search Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = ElevatedIvoryCard,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .width(180.dp)
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = MutedCocoaText,
                        modifier = Modifier.size(16.dp)
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Cari data...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedCocoaText.copy(alpha = 0.65f)
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = RichEspressoPrimary,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(RichEspressoPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reset",
                            tint = MutedCocoaText,
                            modifier = Modifier
                                .size(15.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            // Toggle Reorder Mode Button (for Menu & Modifier tabs)
            if (isAllowedToEdit && selectedSection in 0..1) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isReorderModeActive) DeepCaramelGold else ElevatedIvoryCard,
                    border = BorderStroke(1.dp, if (isReorderModeActive) DeepCaramelGold else SubtleSandBorder),
                    shadowElevation = if (isReorderModeActive) 4.dp else 2.dp,
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { isReorderModeActive = !isReorderModeActive }
                        .testTag("settings_toggle_reorder_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (isReorderModeActive) Icons.Default.Check else Icons.Default.SwapVert,
                            contentDescription = "Atur Urutan",
                            tint = if (isReorderModeActive) ElevatedIvoryCard else RichEspressoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isReorderModeActive) "Selesai Urut" else "Urutkan",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isReorderModeActive) ElevatedIvoryCard else RichEspressoPrimary
                        )
                    }
                }
            }

            // Primary Context Action Button (+ Menu / + Modifier / + Bahan / + Promo / + Akun)
            if (isAllowedToEdit && selectedSection in 0..4) {
                val actionLabel = when (selectedSection) {
                    0 -> "Menu"
                    1 -> "Modifier"
                    2 -> "Bahan"
                    3 -> "Promo"
                    else -> "Akun"
                }
                Button(
                    onClick = {
                        when (selectedSection) {
                            0 -> showCreateModalForType = "MENU_ITEM" to (if (subFilter != "ALL") subFilter else "Egg Waffle")
                            1 -> showCreateModalForType = "MODIFIER" to (if (subFilter != "ALL") subFilter else (customModifierCategories.firstOrNull() ?: "Add-on"))
                            2 -> showCreateModalForType = "INGREDIENT" to "RAW_MATERIAL"
                            3 -> {
                                editingPromoItem = null
                                showPromoFormModal = true
                            }
                            4 -> showAddStaffModal = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RichEspressoPrimary,
                        contentColor = SoftGoldHighlight
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("settings_add_item_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(actionLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Tab Content Area
        when (selectedSection) {
            0 -> {
                val menuCategories = remember(catalog, customMenuCategories) {
                    (customMenuCategories + catalog.filter { it.recordType == "MENU_ITEM" }.map { it.category })
                        .filter { it.isNotBlank() }
                        .distinctBy { normalizeCategoryCanonical(it) }
                }
                val menuItems = remember(catalog, searchQuery, subFilter, menuSubFilter, menuCategories) {
                    catalog.filter {
                        it.recordType == "MENU_ITEM" &&
                            (subFilter == "ALL" || areCategoriesMatching(it.category, subFilter)) &&
                            (subFilter == "ALL" || menuSubFilter == "ALL" || areCategoriesMatching(it.subCategory, menuSubFilter)) &&
                            (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
                    }.sortedWith(
                        compareBy<CatalogRecord> { rec ->
                            val cIdx = menuCategories.indexOfFirst { areCategoriesMatching(it, rec.category) }
                            if (cIdx >= 0) cIdx else 999
                        }
                            .thenBy { if (it.sortOrder < 0) 999999 else it.sortOrder }
                            .thenBy { it.name.lowercase() }
                    )
                }
                val menuFilterOptions = listOf("ALL" to "Semua (${catalog.count { it.recordType == "MENU_ITEM" }})") +
                    menuCategories.map { it to it }

                FilterChipRow(
                    options = menuFilterOptions,
                    selected = subFilter,
                    onSelect = {
                        subFilter = it
                        menuSubFilter = "ALL"
                    },
                    onEditCategoryClick = if (isAllowedToEdit) { { cat -> editingCategoryForType = "MENU" to cat } } else null,
                    onReorderCategoryClick = if (isAllowedToEdit) { { catKey, dir -> viewModel.reorderMenuCategory(catKey, dir) } } else null,
                    onAddCategoryClick = if (isAllowedToEdit) { { showAddCategoryForType = "MENU" } } else null
                )

                // Sub-Kategori Row: Muncul tepat di bawah list kategori hanya jika kategori utama di klik (bukan ALL)
                if (subFilter != "ALL") {
                    val activeMenuSubs = remember(subFilter, customSubCategoriesMap, catalog) {
                        viewModel.getSubCategoriesForCategory(subFilter)
                    }
                    val subCatOptions = listOf("ALL" to "Semua") + activeMenuSubs.map { it to it }
                    Spacer(modifier = Modifier.height(6.dp))
                    FilterChipRow(
                        options = subCatOptions,
                        selected = menuSubFilter,
                        onSelect = { menuSubFilter = it },
                        onEditCategoryClick = if (isAllowedToEdit) { { sub -> editingSubCategoryForCategory = subFilter to sub } } else null,
                        onAddCategoryClick = if (isAllowedToEdit) { { showAddSubCategoryForCategory = subFilter } } else null,
                        addLabel = "Sub Kategori"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                ReorderableCatalogGrid(
                    items = menuItems,
                    canEdit = isAllowedToEdit && searchQuery.isBlank(),
                    isReorderModeActive = isReorderModeActive,
                    onCommitReorder = { orderedIds ->
                        viewModel.reorderCatalogItems(orderedIds, "MENU_ITEM")
                    },
                    cardContent = { item, index, totalCount, isDragging, onStepMove ->
                        MenuSettingsGridCard(
                            item = item,
                            canEdit = isAllowedToEdit,
                            activePlatforms = activePlatforms,
                            isDragging = isDragging,
                            isReorderModeActive = isReorderModeActive,
                            orderPosition = index + 1,
                            canMovePrev = index > 0,
                            canMoveNext = index < totalCount - 1,
                            onMovePrev = { onStepMove(-1) },
                            onMoveNext = { onStepMove(1) },
                            onEdit = { editingCatalogItem = item },
                            onToggleActive = { viewModel.toggleCatalogRecordActive(item) }
                        )
                    }
                )
            }

            1 -> {
                val modifierCategories = remember(catalog, customModifierCategories) {
                    (customModifierCategories + catalog.filter { it.recordType == "MODIFIER" }.map { it.category })
                        .filter { it.isNotBlank() }
                        .distinctBy { normalizeCategoryCanonical(it) }
                }
                val modifiers = remember(catalog, searchQuery, subFilter, modifierSubFilter, modifierCategories) {
                    catalog.filter {
                        it.recordType == "MODIFIER" &&
                            (subFilter == "ALL" || areCategoriesMatching(it.category, subFilter)) &&
                            (subFilter == "ALL" || modifierSubFilter == "ALL" || areCategoriesMatching(it.subCategory, modifierSubFilter)) &&
                            (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
                    }.sortedWith(
                        compareBy<CatalogRecord> { rec ->
                            val cIdx = modifierCategories.indexOfFirst { areCategoriesMatching(it, rec.category) }
                            if (cIdx >= 0) cIdx else 999
                        }
                            .thenBy { if (it.sortOrder < 0) 999999 else it.sortOrder }
                            .thenBy { it.name.lowercase() }
                    )
                }
                val modifierFilterOptions = listOf("ALL" to "Semua (${catalog.count { it.recordType == "MODIFIER" }})") +
                    modifierCategories.map { cat ->
                        cat to cat
                    }

                FilterChipRow(
                    options = modifierFilterOptions,
                    selected = subFilter,
                    onSelect = {
                        subFilter = it
                        modifierSubFilter = "ALL"
                    },
                    onEditCategoryClick = if (isAllowedToEdit) { { cat -> editingCategoryForType = "MODIFIER" to cat } } else null,
                    onReorderCategoryClick = if (isAllowedToEdit) { { catKey, dir -> viewModel.reorderModifierCategory(catKey, dir) } } else null,
                    onAddCategoryClick = if (isAllowedToEdit) { { showAddCategoryForType = "MODIFIER" } } else null
                )

                // Sub-Kategori Row: Muncul tepat di bawah list kategori hanya jika kategori utama di klik (bukan ALL)
                if (subFilter != "ALL") {
                    val activeModSubs = remember(subFilter, customSubCategoriesMap, catalog) {
                        viewModel.getSubCategoriesForCategory(subFilter)
                    }
                    val modSubCatOptions = listOf("ALL" to "Semua") + activeModSubs.map { it to it }
                    Spacer(modifier = Modifier.height(6.dp))
                    FilterChipRow(
                        options = modSubCatOptions,
                        selected = modifierSubFilter,
                        onSelect = { modifierSubFilter = it },
                        onEditCategoryClick = if (isAllowedToEdit) { { sub -> editingSubCategoryForCategory = subFilter to sub } } else null,
                        onAddCategoryClick = if (isAllowedToEdit) { { showAddSubCategoryForCategory = subFilter } } else null,
                        addLabel = "Sub Kategori"
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ReorderableCatalogGrid(
                    items = modifiers,
                    canEdit = isAllowedToEdit && searchQuery.isBlank(),
                    isReorderModeActive = isReorderModeActive,
                    onCommitReorder = { orderedIds ->
                        viewModel.reorderCatalogItems(orderedIds, "MODIFIER")
                    },
                    cardContent = { item, index, totalCount, isDragging, onStepMove ->
                        ModifierSettingsGridCard(
                            item = item,
                            canEdit = isAllowedToEdit,
                            activePlatforms = activePlatforms,
                            isDragging = isDragging,
                            isReorderModeActive = isReorderModeActive,
                            orderPosition = index + 1,
                            canMovePrev = index > 0,
                            canMoveNext = index < totalCount - 1,
                            onMovePrev = { onStepMove(-1) },
                            onMoveNext = { onStepMove(1) },
                            onEdit = { editingCatalogItem = item },
                            onToggleActive = { viewModel.toggleCatalogRecordActive(item) }
                        )
                    }
                )
            }

            2 -> {
                val ingredients = remember(catalog, searchQuery, subFilter) {
                    catalog.filter {
                        it.recordType == "INGREDIENT" &&
                            (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
                    }.filter { ing ->
                        if (subFilter == "LOW") {
                            val minS = try { JSONObject(ing.metadataJson).optDouble("minStock", 300.0) } catch (e: Exception) { 300.0 }
                            ing.stockQty <= minS
                        } else true
                    }
                }
                FilterChipRow(
                    options = listOf(
                        "ALL" to "Semua Bahan (${catalog.count { it.recordType == "INGREDIENT" }})",
                        "LOW" to "Perlu Restock"
                    ),
                    selected = subFilter,
                    onSelect = { subFilter = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(ingredients, key = { it.id }) { item ->
                        IngredientSettingsGridCard(
                            item = item,
                            canEdit = isAllowedToEdit,
                            onQuickRestock = {
                                val nextQty = item.stockQty + 1000.0
                                viewModel.saveOrUpdateCatalogRecord(
                                    item.copy(stockQty = nextQty),
                                    "Quick Restock +1.000 ${item.stockUnit} untuk '${item.name}'"
                                )
                            },
                            onEdit = { editingCatalogItem = item }
                        )
                    }
                }
            }

            3 -> {
                val promos = remember(catalog) {
                    catalog.filter { it.recordType == "PROMO" }
                }
                val branchOrders = remember(allOrdersList, session.branchId) {
                    allOrdersList.filter { it.branchId.equals(session.branchId, ignoreCase = true) }
                }
                val perfStats = remember(branchOrders, promos) {
                    viewModel.computePromoPerformanceStats(branchOrders, promos)
                }
                PromoManagementSection(
                    promos = promos,
                    allCatalog = catalog,
                    branches = branches.ifEmpty { listOf("Global") },
                    activeBranchId = session.branchId,
                    searchQuery = searchQuery,
                    canEdit = isAllowedToEdit,
                    getUsageCount = { rec -> viewModel.getPromoUsageCount(rec, allOrdersList) },
                    isPromoUsedInTx = { rec -> viewModel.isPromoUsedInTransactions(rec, allOrdersList) },
                    performanceStats = perfStats,
                    onCreateNewPromo = {
                        editingPromoItem = null
                        showPromoFormModal = true
                    },
                    onEditPromo = { rec ->
                        editingPromoItem = rec
                        showPromoFormModal = true
                    },
                    onDuplicatePromo = { rec -> viewModel.duplicatePromoRecord(rec) },
                    onTogglePromoActive = { rec -> viewModel.togglePromoRecordActive(rec) },
                    onDeleteOrArchivePromo = { rec -> viewModel.deleteOrArchivePromoRecord(rec) }
                )
            }

            4 -> {
                val platformConfigs = remember(catalog) {
                    catalog.filter { it.recordType == "BRANCH_CONFIG" }
                }
                // Strictly isolated to the active branch (and hides Developer account)
                val filteredAccounts = remember(staffAccounts, session.branchId, searchQuery) {
                    staffAccounts.filter { acc ->
                        acc.role != StaffRole.DEVELOPER &&
                            acc.branchId.equals(session.branchId, ignoreCase = true) &&
                            (searchQuery.isBlank() ||
                                acc.displayName.contains(searchQuery, ignoreCase = true) ||
                                acc.username.contains(searchQuery, ignoreCase = true))
                    }
                }

                var selectedOtherSettingTopic by remember { mutableStateOf("PLATFORM") }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left Column (Width 210.dp): Topic Selector
                    Card(
                        modifier = Modifier
                            .width(210.dp)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Pengaturan Lainnya",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            // Topic Item 1: Platform Online
                            val isPlatformSelected = selectedOtherSettingTopic == "PLATFORM"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isPlatformSelected) SoftGoldHighlight else SoftOatSurface,
                                border = BorderStroke(1.dp, if (isPlatformSelected) DeepCaramelGold else SubtleSandBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedOtherSettingTopic = "PLATFORM" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = if (isPlatformSelected) DeepCaramelGold else MutedCocoaText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Platform Online",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isPlatformSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPlatformSelected) RichEspressoPrimary else MutedCocoaText
                                    )
                                }
                            }

                            // Topic Item 2: Akun Cabang
                            val isStaffSelected = selectedOtherSettingTopic == "STAFF"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isStaffSelected) SoftGoldHighlight else SoftOatSurface,
                                border = BorderStroke(1.dp, if (isStaffSelected) DeepCaramelGold else SubtleSandBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedOtherSettingTopic = "STAFF" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isStaffSelected) DeepCaramelGold else MutedCocoaText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Akun Cabang",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isStaffSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isStaffSelected) RichEspressoPrimary else MutedCocoaText
                                    )
                                }
                            }

                            // Topic Item 3: Bank
                            val isBankSelected = selectedOtherSettingTopic == "BANK"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isBankSelected) SoftGoldHighlight else SoftOatSurface,
                                border = BorderStroke(1.dp, if (isBankSelected) DeepCaramelGold else SubtleSandBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedOtherSettingTopic = "BANK" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = if (isBankSelected) DeepCaramelGold else MutedCocoaText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Bank",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isBankSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isBankSelected) RichEspressoPrimary else MutedCocoaText
                                    )
                                }
                            }

                            // Topic Item 4: Dompet Digital
                            val isWalletSelected = selectedOtherSettingTopic == "WALLET"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isWalletSelected) SoftGoldHighlight else SoftOatSurface,
                                border = BorderStroke(1.dp, if (isWalletSelected) DeepCaramelGold else SubtleSandBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedOtherSettingTopic = "WALLET" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = if (isWalletSelected) DeepCaramelGold else MutedCocoaText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Dompet Digital",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isWalletSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isWalletSelected) RichEspressoPrimary else MutedCocoaText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftOatSurface.copy(alpha = 0.75f),
                                border = BorderStroke(1.dp, SubtleSandBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_app_version_card")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "WAFFLEO POS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RichEspressoPrimary
                                        )
                                        Text(
                                            text = "Build ${BuildConfig.VERSION_CODE}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedCocoaText
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = SoftGoldHighlight,
                                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.45f))
                                    ) {
                                        Text(
                                            text = "v${BuildConfig.VERSION_NAME}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepCaramelGold,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right Column (weight 1f): Content of Selected Topic
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        when (selectedOtherSettingTopic) {
                            "PLATFORM" -> {
                                // Topic: Platform Online Content
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = SoftSageBg,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountBalance,
                                                        contentDescription = null,
                                                        tint = MatchaSageSuccess,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Platform Online (${platformConfigs.size})",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = SoftGoldHighlight
                                        ) {
                                            Text(
                                                text = "Cabang ${viewModel.getBranchDisplayName(session.branchId)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepCaramelGold,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(platformConfigs, key = { it.id }) { cfg ->
                                            val feeByOrder = maxOf(cfg.priceGrabFood, cfg.priceGoFood, cfg.priceShopeeFood)
                                            val settlementBank = cfg.subCategory.ifBlank { "BCA" }
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = SoftOatSurface.copy(alpha = 0.7f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = cfg.category.ifBlank { cfg.name },
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = RichEspressoPrimary
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            SmallTagPill(
                                                                text = "Komisi ${cfg.priceWalkIn}%",
                                                                bg = SoftGoldHighlight,
                                                                fg = RichEspressoPrimary
                                                            )
                                                            SmallTagPill(
                                                                text = "Order Fee ${WaffleoViewModel.formatIdr(feeByOrder)}",
                                                                bg = ElevatedIvoryCard,
                                                                fg = MutedCocoaText
                                                            )
                                                            SmallTagPill(
                                                                text = "Pencairan: $settlementBank",
                                                                bg = SoftSageBg,
                                                                fg = MatchaSageSuccess
                                                            )
                                                        }
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        // Toggle Pill: Aktif / Nonaktif
                                                        Surface(
                                                            shape = RoundedCornerShape(50),
                                                            color = if (cfg.isActive) SoftSageBg else SoftTerracottaBg,
                                                            border = BorderStroke(1.dp, if (cfg.isActive) MatchaSageSuccess else TerracottaDanger),
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(50))
                                                                .clickable { viewModel.toggleCatalogRecordActive(cfg) }
                                                        ) {
                                                            Text(
                                                                text = if (cfg.isActive) "Aktif" else "Nonaktif",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (cfg.isActive) MatchaSageSuccess else TerracottaDanger,
                                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                            )
                                                        }

                                                        if (isAllowedToEdit) {
                                                            // Edit Button
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = ElevatedIvoryCard,
                                                                shadowElevation = 1.dp,
                                                                modifier = Modifier
                                                                    .size(34.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable { editingCatalogItem = cfg }
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Edit,
                                                                        contentDescription = "Edit",
                                                                        tint = RichEspressoPrimary,
                                                                        modifier = Modifier.size(15.dp)
                                                                    )
                                                                }
                                                            }

                                                            // Hapus Button (dengan konfirmasi pop up)
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = SoftTerracottaBg,
                                                                shadowElevation = 1.dp,
                                                                modifier = Modifier
                                                                    .size(34.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        generalDeleteConfirmTarget = "Apakah Anda yakin ingin menghapus Platform Online '${cfg.category.ifBlank { cfg.name }}'?" to { viewModel.deleteCatalogRecord(cfg) }
                                                                    }
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.DeleteOutline,
                                                                        contentDescription = "Hapus",
                                                                        tint = TerracottaDanger,
                                                                        modifier = Modifier.size(15.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Button "+ Platform Online"
                                    Button(
                                        onClick = {
                                            showCreateModalForType = "BRANCH_CONFIG" to "Platform Online"
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = RichEspressoPrimary,
                                            contentColor = SoftGoldHighlight
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Platform Online", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            "STAFF" -> {
                                // Topic: Akun Cabang Content
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = SoftGoldHighlight,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = RichEspressoPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Akun Cabang (${filteredAccounts.size})",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary
                                            )
                                        }
                                        if (session.role.canManageStaffAccounts) {
                                            Button(
                                                onClick = { showAddStaffModal = true },
                                                shape = RoundedCornerShape(50),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = RichEspressoPrimary,
                                                    contentColor = SoftGoldHighlight
                                                ),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Akun", style = MaterialTheme.typography.labelLarge)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(filteredAccounts) { acc ->
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = SoftOatSurface.copy(alpha = 0.7f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = RichEspressoPrimary,
                                                            modifier = Modifier.size(34.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text(
                                                                    text = acc.displayName.take(1).uppercase(),
                                                                    style = MaterialTheme.typography.labelLarge,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = SoftGoldHighlight
                                                                )
                                                            }
                                                        }
                                                        Column {
                                                            Text(
                                                                text = acc.displayName,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = RichEspressoPrimary
                                                            )
                                                            Text(
                                                                text = "Username : ${acc.username}  •  Cabang ${viewModel.getBranchDisplayName(acc.branchId)}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MutedCocoaText
                                                            )
                                                        }
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        SmallTagPill(
                                                            text = acc.role.label,
                                                            bg = when (acc.role) {
                                                                StaffRole.DEVELOPER -> RichEspressoPrimary
                                                                StaffRole.OWNER -> SoftGoldHighlight
                                                                StaffRole.SUPERVISOR -> SoftSageBg
                                                                StaffRole.KASIR -> ElevatedIvoryCard
                                                            },
                                                            fg = when (acc.role) {
                                                                StaffRole.DEVELOPER -> SoftGoldHighlight
                                                                StaffRole.OWNER -> DeepCaramelGold
                                                                StaffRole.SUPERVISOR -> MatchaSageSuccess
                                                                StaffRole.KASIR -> RichEspressoPrimary
                                                            }
                                                        )

                                                        if (canUserEditStaffAccount(session.role, session.username, acc)) {
                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = ElevatedIvoryCard,
                                                                border = BorderStroke(1.dp, SubtleSandBorder),
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .clickable { accountToEdit = acc }
                                                                    .testTag("edit_account_${acc.username}")
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Edit,
                                                                        contentDescription = "Edit Akun",
                                                                        tint = DeepCaramelGold,
                                                                        modifier = Modifier.size(13.dp)
                                                                    )
                                                                    Text(
                                                                        text = "Edit",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = DeepCaramelGold
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "BANK" -> {
                                // Topic: Bank Settings Content
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = SoftAmberBg,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountBalance,
                                                        contentDescription = null,
                                                        tint = DeepCaramelGold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Pengaturan Bank (${bankAccounts.size})",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = SoftGoldHighlight
                                        ) {
                                            Text(
                                                text = "Terkoneksi Money Manager",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepCaramelGold,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(bankAccounts, key = { it.id }) { b ->
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = SoftOatSurface.copy(alpha = 0.7f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = b.name,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = RichEspressoPrimary
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            if (b.ownerName.isNotBlank()) {
                                                                SmallTagPill(
                                                                    text = "Pemilik: ${b.ownerName}",
                                                                    bg = SoftGoldHighlight,
                                                                    fg = RichEspressoPrimary
                                                                )
                                                            }
                                                            if (b.accountNumber.isNotBlank()) {
                                                                SmallTagPill(
                                                                    text = "No. Rek: ${b.accountNumber}",
                                                                    bg = ElevatedIvoryCard,
                                                                    fg = MutedCocoaText
                                                                )
                                                            }
                                                        }
                                                    }

                                                    if (isAllowedToEdit) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = ElevatedIvoryCard,
                                                                shadowElevation = 1.dp,
                                                                modifier = Modifier
                                                                    .size(34.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable { editingBankAccount = b }
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RichEspressoPrimary, modifier = Modifier.size(15.dp))
                                                                }
                                                            }
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = SoftTerracottaBg,
                                                                shadowElevation = 1.dp,
                                                                modifier = Modifier
                                                                    .size(34.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        generalDeleteConfirmTarget = "Apakah Anda yakin ingin menghapus Bank '${b.name}'?" to { viewModel.deleteBankAccount(b.id) }
                                                                    }
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = TerracottaDanger, modifier = Modifier.size(15.dp))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { showAddBankModal = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = RichEspressoPrimary,
                                            contentColor = SoftGoldHighlight
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Bank Baru", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            else -> {
                                // Topic: Digital Wallet Settings Content
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = SoftSageBg,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Payments,
                                                        contentDescription = null,
                                                        tint = MatchaSageSuccess,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Dompet Digital (${digitalWallets.size})",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = SoftGoldHighlight
                                        ) {
                                            Text(
                                                text = "Terkoneksi Money Manager",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepCaramelGold,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(digitalWallets, key = { it.id }) { w ->
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = SoftOatSurface.copy(alpha = 0.7f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = w.name,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = RichEspressoPrimary
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            if (w.ownerName.isNotBlank()) {
                                                                SmallTagPill(
                                                                    text = "Pemilik: ${w.ownerName}",
                                                                    bg = SoftGoldHighlight,
                                                                    fg = RichEspressoPrimary
                                                                )
                                                            }
                                                            if (w.phoneNumber.isNotBlank()) {
                                                                SmallTagPill(
                                                                    text = "No. HP: ${w.phoneNumber}",
                                                                    bg = ElevatedIvoryCard,
                                                                    fg = MutedCocoaText
                                                                )
                                                            }
                                                            if (w.email.isNotBlank()) {
                                                                SmallTagPill(
                                                                    text = w.email,
                                                                    bg = SoftSageBg,
                                                                    fg = MatchaSageSuccess
                                                                )
                                                            }
                                                        }
                                                    }

                                                    if (isAllowedToEdit) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = ElevatedIvoryCard,
                                                                shadowElevation = 1.dp,
                                                                modifier = Modifier
                                                                    .size(34.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable { editingDigitalWallet = w }
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RichEspressoPrimary, modifier = Modifier.size(15.dp))
                                                                }
                                                            }
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = SoftTerracottaBg,
                                                                shadowElevation = 1.dp,
                                                                modifier = Modifier
                                                                    .size(34.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        generalDeleteConfirmTarget = "Apakah Anda yakin ingin menghapus Dompet Digital '${w.name}'?" to { viewModel.deleteDigitalWallet(w.id) }
                                                                    }
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = TerracottaDanger, modifier = Modifier.size(15.dp))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { showAddWalletModal = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = RichEspressoPrimary,
                                            contentColor = SoftGoldHighlight
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Dompet Digital Baru", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                var auditPeriodMode by remember { mutableStateOf("ALL_TIME") }
                var auditSelectedPeriod by remember { mutableStateOf(HistoryFilterPeriod.TODAY) }
                var showAuditCustomDateDialog by remember { mutableStateOf(false) }
                var auditCustomStartMillis by remember { mutableStateOf<Long?>(null) }
                var auditCustomEndMillis by remember { mutableStateOf<Long?>(null) }
                var isAuditCustomRangeActive by remember { mutableStateOf(false) }

                val allGroupedLogs = remember(auditLogs) {
                    groupAuditLogRecords(auditLogs)
                }

                val filteredGroupedLogs = remember(
                    allGroupedLogs,
                    searchQuery,
                    subFilter,
                    auditPeriodMode,
                    auditSelectedPeriod,
                    isAuditCustomRangeActive,
                    auditCustomStartMillis,
                    auditCustomEndMillis
                ) {
                    allGroupedLogs.filter { group ->
                        val matchesFilter = when (subFilter) {
                            "ORDER" -> group.category == "ORDER"
                            "CATALOG" -> group.category == "CATALOG"
                            "STOCK" -> group.category == "STOCK"
                            "STAFF" -> group.category == "STAFF"
                            else -> true
                        }
                        val matchesPeriod = if (auditPeriodMode == "ALL_TIME") {
                            true
                        } else {
                            group.subLogsChronological.any { sub ->
                                WaffleoViewModel.matchesPeriodFilterRange(
                                    timestamp = sub.createdAt ?: group.latestTimestamp,
                                    period = auditSelectedPeriod,
                                    customDaysBack = 7,
                                    isCustomRangeActive = isAuditCustomRangeActive,
                                    startDateMillis = auditCustomStartMillis,
                                    endDateMillis = auditCustomEndMillis
                                )
                            }
                        }
                        val matchesQuery = searchQuery.isBlank() ||
                            group.title.contains(searchQuery, ignoreCase = true) ||
                            group.highLevelSummary.contains(searchQuery, ignoreCase = true) ||
                            group.entityId.contains(searchQuery, ignoreCase = true) ||
                            group.latestActor.contains(searchQuery, ignoreCase = true) ||
                            group.subLogsChronological.any { sub ->
                                sub.summary.contains(searchQuery, ignoreCase = true) ||
                                    sub.actionType.contains(searchQuery, ignoreCase = true) ||
                                    sub.actorUsername.contains(searchQuery, ignoreCase = true)
                            }
                        matchesFilter && matchesPeriod && matchesQuery
                    }
                }

                val auditPeriodOptions = remember {
                    listOf("ALL_TIME") + HistoryFilterPeriod.entries.map { it.name }
                }
                val auditPeriodLabel: (String) -> String = remember(isAuditCustomRangeActive, auditCustomStartMillis, auditCustomEndMillis) {
                    { key ->
                        if (key == "ALL_TIME") {
                            "Semua Waktu"
                        } else {
                            val p = HistoryFilterPeriod.valueOf(key)
                            if (p == HistoryFilterPeriod.CUSTOM && isAuditCustomRangeActive && auditCustomStartMillis != null && auditCustomEndMillis != null) {
                                val fmt = SimpleDateFormat("dd MMM yy", Locale.forLanguageTag("id-ID"))
                                "${fmt.format(Date(auditCustomStartMillis!!))} - ${fmt.format(Date(auditCustomEndMillis!!))}"
                            } else {
                                p.label
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FilterChipRow(
                            options = listOf(
                                "ALL" to "Semua Aktivitas (${allGroupedLogs.size})",
                                "ORDER" to "Transaksi & Order (${allGroupedLogs.count { it.category == "ORDER" }})",
                                "CATALOG" to "Menu & Harga",
                                "STOCK" to "Stok Bahan",
                                "STAFF" to "Akses & Toko"
                            ),
                            selected = subFilter,
                            onSelect = { subFilter = it }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    WaffleoSingleSelectDropdown(
                        label = "",
                        selectedText = auditPeriodLabel(auditPeriodMode),
                        options = auditPeriodOptions,
                        optionLabel = auditPeriodLabel,
                        onOptionSelected = { chosenKey ->
                            auditPeriodMode = chosenKey
                            if (chosenKey != "ALL_TIME") {
                                val period = HistoryFilterPeriod.valueOf(chosenKey)
                                auditSelectedPeriod = period
                                if (period == HistoryFilterPeriod.CUSTOM) {
                                    showAuditCustomDateDialog = true
                                } else {
                                    isAuditCustomRangeActive = false
                                }
                            } else {
                                isAuditCustomRangeActive = false
                            }
                        },
                        containerColor = ElevatedIvoryCard,
                        modifier = Modifier.width(175.dp),
                        testTagPrefix = "audit_period_filter"
                    )
                }

                if (showAuditCustomDateDialog) {
                    CustomDateRangePickerDialog(
                        initialStartMillis = auditCustomStartMillis,
                        initialEndMillis = auditCustomEndMillis,
                        onDismiss = { showAuditCustomDateDialog = false },
                        onConfirm = { startMs, endMs ->
                            auditCustomStartMillis = startMs
                            auditCustomEndMillis = endMs
                            isAuditCustomRangeActive = true
                            showAuditCustomDateDialog = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredGroupedLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MutedCocoaText.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Tidak ada aktivitas audit pada filter ini",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedCocoaText
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredGroupedLogs, key = { it.groupKey }) { group ->
                            val timeStr = remember(group.latestTimestamp) {
                                val dt = group.latestTimestamp?.toDate()
                                if (dt != null) SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt)
                                else "Baru saja"
                            }
                            val badge = getAuditBadgeInfo(group.mainActionType, group.category)

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedGroupedLog = group }
                                    .testTag("audit_group_${group.groupKey}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = badge.bg,
                                                border = BorderStroke(1.dp, badge.fg.copy(alpha = 0.25f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = badge.icon,
                                                        contentDescription = null,
                                                        tint = badge.fg,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = badge.label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = badge.fg
                                                    )
                                                }
                                            }

                                            Text(
                                                text = com.example.data.model.sanitizeBranchIdsInText(group.title),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (group.totalSubActions > 1) {
                                                SmallTagPill(
                                                    text = "${group.totalSubActions} Catatan",
                                                    bg = SoftGoldHighlight,
                                                    fg = DeepCaramelGold
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = SoftOatSurface
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = MutedCocoaText,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = "${group.latestActor} (${group.latestActorRole})",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = RichEspressoPrimary
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = SoftOatSurface
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Schedule,
                                                        contentDescription = null,
                                                        tint = MutedCocoaText,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = timeStr,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MutedCocoaText
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Breadcrumb alur tahapan jika pesanan / multi aksi
                                    if (group.totalSubActions > 1) {
                                        val stages = remember(group.subLogsChronological) {
                                            val list = mutableListOf<String>()
                                            list.add("Dibuat")
                                            val editCount = group.subLogsChronological.count { it.actionType.contains("EDIT") }
                                            val payCount = group.subLogsChronological.count { it.actionType.contains("PAID") }
                                            if (editCount > 0) list.add("${editCount}x Diedit")
                                            if (payCount > 0) list.add("${payCount}x Dibayar")
                                            val isDone = group.subLogsChronological.any { it.actionType.contains("DONE") }
                                            val isCancel = group.subLogsChronological.any { it.actionType.contains("CANCEL") }
                                            if (isDone) list.add("Selesai")
                                            else if (isCancel) list.add("Pesanan Dihapus")
                                            list
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Kronologi:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MutedCocoaText
                                            )
                                            stages.forEachIndexed { idx, stage ->
                                                val stColor = when {
                                                    stage == "Selesai" -> SoftSageBg to MatchaSageSuccess
                                                    stage == "Pesanan Dihapus" -> SoftTerracottaBg to TerracottaDanger
                                                    stage.contains("Dibayar") -> SoftSageBg to MatchaSageSuccess
                                                    stage.contains("Diedit") -> SoftAmberBg to HoneyAmberWarning
                                                    else -> SoftGoldHighlight to DeepCaramelGold
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = stColor.first
                                                ) {
                                                    Text(
                                                        text = stage,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = stColor.second,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                if (idx < stages.size - 1) {
                                                    Text(
                                                        text = "➔",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MutedCocoaText
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Baris Ringkasan Narasi & Ajakan Buka Kronologi
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = group.highLevelSummary.substringAfter("• Terakhir: ").ifBlank { group.highLevelSummary },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = RichEspressoPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = SoftOatSurface,
                                            border = BorderStroke(1.dp, SubtleSandBorder),
                                            modifier = Modifier.padding(start = 12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "Buka Audit Trail",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepCaramelGold
                                                )
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = "Detail",
                                                    tint = DeepCaramelGold,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPromoFormModal) {
        PromoFormModalDialog(
            existingPromo = editingPromoItem,
            allCatalog = catalog,
            availableMenuCategories = allMenuCategories,
            availableBranches = branches.ifEmpty { listOf("Global") },
            activeBranchId = session.branchId,
            onDismiss = {
                showPromoFormModal = false
                editingPromoItem = null
            },
            onSavePromo = { name, cfg ->
                viewModel.savePromoWithConfig(editingPromoItem, name, cfg)
                showPromoFormModal = false
                editingPromoItem = null
            }
        )
    }

    editingCatalogItem?.let { record ->
        CatalogEditorDialog(
            initialRecord = record,
            isNew = false,
            canDelete = isAllowedToEdit && record.recordType in listOf("MENU_ITEM", "MODIFIER", "INGREDIENT", "PROMO"),
            customCategories = if (record.recordType == "MENU_ITEM") allMenuCategories else allModifierCategories,
            availableModifiers = catalog.filter { it.recordType == "MODIFIER" && it.isActive },
            activePlatforms = activePlatforms,
            onGetSubCategories = { cat -> viewModel.getSubCategoriesForCategory(cat) },
            onDismiss = { editingCatalogItem = null },
            onDelete = {
                viewModel.deleteCatalogRecord(record)
                editingCatalogItem = null
            },
            onSave = { updated ->
                viewModel.saveOrUpdateCatalogRecord(
                    updated,
                    "Memperbarui '${updated.name}' (${updated.category})"
                )
            }
        )
    }

    showCreateModalForType?.let { (recType, defCat) ->
        CatalogEditorDialog(
            initialRecord = CatalogRecord(
                recordType = recType,
                category = defCat,
                branchId = session.branchId,
                name = "",
                priceWalkIn = 0L,
                priceGrabFood = 0L,
                priceGoFood = 0L,
                priceShopeeFood = 0L,
                stockQty = 0.0,
                stockUnit = "gram"
            ),
            isNew = true,
            canDelete = false,
            customCategories = if (recType == "MENU_ITEM") allMenuCategories else allModifierCategories,
            availableModifiers = catalog.filter { it.recordType == "MODIFIER" && it.isActive },
            activePlatforms = activePlatforms,
            onGetSubCategories = { cat -> viewModel.getSubCategoriesForCategory(cat) },
            onDismiss = { showCreateModalForType = null },
            onSave = { created ->
                viewModel.saveOrUpdateCatalogRecord(
                    created,
                    "Menambahkan '${created.name}' (${created.category})"
                )
            }
        )
    }

    if (showAddStaffModal) {
        val selectableBranches = if (session.role == StaffRole.DEVELOPER) {
            branches.ifEmpty { listOf("Global") }
        } else {
            listOf(session.branchId)
        }
        AddStaffAccountDialog(
            existingBranches = selectableBranches,
            defaultBranch = session.branchId,
            onDismiss = { showAddStaffModal = false },
            onConfirm = { br, u, p, r, dn ->
                viewModel.addBranchAndAccount(br, u, p, r, dn)
            }
        )
    }

    accountToEdit?.let { targetAcc ->
        EditStaffAccountDialog(
            account = targetAcc,
            actorRole = session.role,
            onDismiss = { accountToEdit = null },
            onDeleteAccount = if (canUserDeleteStaffAccount(session.role, session.username, targetAcc)) {
                {
                    generalDeleteConfirmTarget = "Apakah Anda yakin ingin menghapus Akun Staff '${targetAcc.displayName}' (@${targetAcc.username})?" to {
                        viewModel.deleteStaffAccount(targetAcc.username, targetAcc.branchId) {
                            accountToEdit = null
                        }
                    }
                }
            } else null,
            onConfirm = { updatedDisplayName, newPassword, updatedRole, email, phone ->
                viewModel.updateStaffAccount(
                    targetUsername = targetAcc.username,
                    targetBranchId = targetAcc.branchId,
                    newDisplayName = updatedDisplayName,
                    newPassword = newPassword,
                    newRole = updatedRole,
                    newEmail = email,
                    newPhone = phone,
                    onSuccess = { accountToEdit = null }
                )
            }
        )
    }

    showAddCategoryForType?.let { targetType ->
        AddCategoryDialog(
            targetType = targetType,
            onDismiss = { showAddCategoryForType = null },
            onSave = { newCat ->
                if (targetType == "MENU") {
                    viewModel.addMenuCategory(newCat)
                } else {
                    viewModel.addModifierCategory(newCat)
                }
            }
        )
    }

    editingCategoryForType?.let { (targetType, catName) ->
        EditCategoryModalDialog(
            targetType = targetType,
            categoryName = catName,
            onDismiss = { editingCategoryForType = null },
            onSave = { newName ->
                viewModel.renameCategory(targetType, catName, newName)
                if (subFilter.equals(catName, ignoreCase = true)) {
                    subFilter = newName
                }
            },
            onDelete = {
                viewModel.deleteCategory(targetType, catName)
                if (subFilter.equals(catName, ignoreCase = true)) {
                    subFilter = "ALL"
                    menuSubFilter = "ALL"
                    modifierSubFilter = "ALL"
                }
            }
        )
    }

    showAddSubCategoryForCategory?.let { targetCat ->
        AddSubCategoryDialog(
            categoryName = targetCat,
            onDismiss = { showAddSubCategoryForCategory = null },
            onSave = { newSubName ->
                viewModel.addSubCategory(targetCat, newSubName)
            }
        )
    }

    editingSubCategoryForCategory?.let { (catName, subName) ->
        EditSubCategoryDialog(
            categoryName = catName,
            subCategoryName = subName,
            onDismiss = { editingSubCategoryForCategory = null },
            onSave = { updatedSubName ->
                viewModel.editSubCategory(catName, subName, updatedSubName)
                if (menuSubFilter.equals(subName, ignoreCase = true)) {
                    menuSubFilter = updatedSubName
                }
                if (modifierSubFilter.equals(subName, ignoreCase = true)) {
                    modifierSubFilter = updatedSubName
                }
            },
            onDelete = {
                viewModel.deleteSubCategory(catName, subName)
                if (menuSubFilter.equals(subName, ignoreCase = true)) {
                    menuSubFilter = "ALL"
                }
                if (modifierSubFilter.equals(subName, ignoreCase = true)) {
                    modifierSubFilter = "ALL"
                }
            }
        )
    }

    selectedGroupedLog?.let { group ->
        val relatedOrder = allOrdersList.firstOrNull {
            it.orderNumber.equals(group.entityId, ignoreCase = true) ||
            it.displayOrderNumber.equals(group.entityId, ignoreCase = true) ||
            it.id.equals(group.entityId, ignoreCase = true) ||
            (group.entityId.isNotBlank() && it.orderNumber.contains(group.entityId, ignoreCase = true))
        }
        AuditLogDetailTimelineDialog(
            group = group,
            relatedOrder = relatedOrder,
            onDismiss = { selectedGroupedLog = null }
        )
    }

    if (showAddBankModal) {
        EditBankAccountDialog(
            bank = null,
            onDismiss = { showAddBankModal = false },
            onSave = { newBank ->
                viewModel.saveBankAccount(newBank)
                showAddBankModal = false
            }
        )
    }

    editingBankAccount?.let { bank ->
        EditBankAccountDialog(
            bank = bank,
            onDismiss = { editingBankAccount = null },
            onSave = { updatedBank ->
                viewModel.saveBankAccount(updatedBank)
                editingBankAccount = null
            }
        )
    }

    if (showAddWalletModal) {
        EditDigitalWalletDialog(
            wallet = null,
            onDismiss = { showAddWalletModal = false },
            onSave = { newWallet ->
                viewModel.saveDigitalWallet(newWallet)
                showAddWalletModal = false
            }
        )
    }

    editingDigitalWallet?.let { wallet ->
        EditDigitalWalletDialog(
            wallet = wallet,
            onDismiss = { editingDigitalWallet = null },
            onSave = { updatedWallet ->
                viewModel.saveDigitalWallet(updatedWallet)
                editingDigitalWallet = null
            }
        )
    }

    generalDeleteConfirmTarget?.let { (msg, onConfirmAction) ->
        GeneralDeleteConfirmDialog(
            message = msg,
            onDismiss = { generalDeleteConfirmTarget = null },
            onConfirm = {
                onConfirmAction()
                generalDeleteConfirmTarget = null
            }
        )
    }
}

@Composable
private fun EditBankAccountDialog(
    bank: BankAccountDetail?,
    onDismiss: () -> Unit,
    onSave: (BankAccountDetail) -> Unit
) {
    var name by remember { mutableStateOf(bank?.name ?: "") }
    var ownerName by remember { mutableStateOf(bank?.ownerName ?: "") }
    var accountNumber by remember { mutableStateOf(bank?.accountNumber ?: "") }

    val isNew = bank == null
    val title = if (isNew) "Tambah Rekening Bank Baru" else "Edit Rekening Bank"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 460.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "Terkoneksi langsung dengan Money Manager & Metode Pembayaran",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedCocoaText)
                    }
                }

                WaffleoSoftTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nama Bank",
                    placeholder = "Misal: Bank Mandiri, BCA, BRI",
                    modifier = Modifier.fillMaxWidth()
                )

                WaffleoSoftTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = "Pemilik Rekening",
                    placeholder = "Nama pemilik rekening",
                    modifier = Modifier.fillMaxWidth()
                )

                WaffleoSoftTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = "Nomor Rekening",
                    placeholder = "Nomor rekening bank",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftOatSurface, contentColor = RichEspressoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val id = bank?.id?.ifBlank { null } ?: "bank_${System.currentTimeMillis()}"
                            onSave(
                                BankAccountDetail(
                                    id = id,
                                    name = name.trim(),
                                    ownerName = ownerName.trim(),
                                    accountNumber = accountNumber.trim()
                                )
                            )
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = RichEspressoPrimary, contentColor = SoftGoldHighlight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditDigitalWalletDialog(
    wallet: DigitalWalletDetail?,
    onDismiss: () -> Unit,
    onSave: (DigitalWalletDetail) -> Unit
) {
    var name by remember { mutableStateOf(wallet?.name ?: "") }
    var ownerName by remember { mutableStateOf(wallet?.ownerName ?: "") }
    var phoneNumber by remember { mutableStateOf(wallet?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(wallet?.email ?: "") }

    val isNew = wallet == null
    val title = if (isNew) "Tambah Dompet Digital Baru" else "Edit Dompet Digital"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 460.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "Terkoneksi langsung dengan Money Manager",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedCocoaText)
                    }
                }

                WaffleoSoftTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nama Dompet Digital",
                    placeholder = "Misal: ShopeePay, GoPay, OVO, Dana",
                    modifier = Modifier.fillMaxWidth()
                )

                WaffleoSoftTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = "Pemilik",
                    placeholder = "Nama pemilik akun",
                    modifier = Modifier.fillMaxWidth()
                )

                WaffleoSoftTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Nomor Handphone",
                    placeholder = "08xxxxxxxxxx",
                    modifier = Modifier.fillMaxWidth()
                )

                WaffleoSoftTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "email@domain.com",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftOatSurface, contentColor = RichEspressoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val id = wallet?.id?.ifBlank { null } ?: "wallet_${System.currentTimeMillis()}"
                            onSave(
                                DigitalWalletDetail(
                                    id = id,
                                    name = name.trim(),
                                    ownerName = ownerName.trim(),
                                    phoneNumber = phoneNumber.trim(),
                                    email = email.trim()
                                )
                            )
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = RichEspressoPrimary, contentColor = SoftGoldHighlight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneralDeleteConfirmDialog(
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = SoftTerracottaBg,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = TerracottaDanger,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = "Konfirmasi Hapus Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichEspressoPrimary
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedCocoaText,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftOatSurface, contentColor = RichEspressoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaDanger, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hapus", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    onEditCategoryClick: ((String) -> Unit)? = null,
    onReorderCategoryClick: ((key: String, direction: Int) -> Unit)? = null,
    onAddCategoryClick: (() -> Unit)? = null,
    addLabel: String = "Kategori"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (key, label) ->
            val isSelected = selected.equals(key, ignoreCase = true)
            val canEditThis = isSelected && !key.equals("ALL", ignoreCase = true) && onEditCategoryClick != null
            val canReorderThis = isSelected && !key.equals("ALL", ignoreCase = true) && onReorderCategoryClick != null
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) SoftGoldHighlight else ElevatedIvoryCard,
                shadowElevation = if (isSelected) 2.dp else 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        if (canEditThis) {
                            onEditCategoryClick!!(key)
                        } else {
                            onSelect(key)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (canReorderThis) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Geser Kiri",
                            tint = DeepCaramelGold,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onReorderCategoryClick!!(key, -1) }
                        )
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) RichEspressoPrimary else MutedCocoaText
                    )
                    if (canEditThis) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Kategori $label",
                            tint = DeepCaramelGold,
                            modifier = Modifier
                                .size(13.dp)
                                .clickable { onEditCategoryClick!!(key) }
                        )
                    }
                    if (canReorderThis) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Geser Kanan",
                            tint = DeepCaramelGold,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onReorderCategoryClick!!(key, 1) }
                        )
                    }
                }
            }
        }

        if (onAddCategoryClick != null) {
            Surface(
                shape = RoundedCornerShape(50),
                color = ElevatedIvoryCard,
                border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.85f)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onAddCategoryClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = addLabel,
                        tint = DeepCaramelGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = addLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepCaramelGold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderableCatalogGrid(
    items: List<CatalogRecord>,
    canEdit: Boolean,
    isReorderModeActive: Boolean,
    onCommitReorder: (List<String>) -> Unit,
    cardContent: @Composable (
        item: CatalogRecord,
        index: Int,
        totalCount: Int,
        isDragging: Boolean,
        onStepMove: (Int) -> Unit
    ) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val gridState = rememberLazyGridState()

    var localOrderedItems by remember { mutableStateOf(items) }
    var draggedItemId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Synchronize localOrderedItems when external items change (unless currently dragging)
    LaunchedEffect(items) {
        if (draggedItemId == null) {
            localOrderedItems = items
        }
    }

    val currentOnCommit by rememberUpdatedState(onCommitReorder)
    val currentLocalItems by rememberUpdatedState(localOrderedItems)

    // Custom ViewConfiguration with 600ms (0.6s) long-press timeout as approved by user
    val baseViewConfig = LocalViewConfiguration.current
    val customViewConfig = remember(baseViewConfig) {
        object : ViewConfiguration by baseViewConfig {
            override val longPressTimeoutMillis: Long
                get() = 600L
        }
    }

    fun handleDragStart(itemId: String) {
        if (!canEdit) return
        draggedItemId = itemId
        dragOffset = Offset.Zero
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun handleDragDelta(delta: Offset) {
        val activeId = draggedItemId ?: return
        dragOffset += delta

        val currentIndex = currentLocalItems.indexOfFirst { it.id == activeId }
        if (currentIndex == -1) return

        val layoutInfo = gridState.layoutInfo
        val draggedInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.key == activeId } ?: return

        val centerX = draggedInfo.offset.x + dragOffset.x + draggedInfo.size.width / 2f
        val centerY = draggedInfo.offset.y + dragOffset.y + draggedInfo.size.height / 2f

        val targetInfo = layoutInfo.visibleItemsInfo.firstOrNull { info ->
            info.key != activeId &&
                centerX >= info.offset.x && centerX <= (info.offset.x + info.size.width) &&
                centerY >= info.offset.y && centerY <= (info.offset.y + info.size.height)
        }

        if (targetInfo != null) {
            val targetId = targetInfo.key as? String ?: return
            val targetIndex = currentLocalItems.indexOfFirst { it.id == targetId }
            if (targetIndex != -1 && targetIndex != currentIndex) {
                val oldOffset = draggedInfo.offset
                val newOffset = targetInfo.offset
                val mutable = currentLocalItems.toMutableList()
                val moved = mutable.removeAt(currentIndex)
                mutable.add(targetIndex, moved)
                localOrderedItems = mutable
                // Compensate dragOffset so the floating card stays anchored under the user's finger
                dragOffset += Offset(
                    (oldOffset.x - newOffset.x).toFloat(),
                    (oldOffset.y - newOffset.y).toFloat()
                )
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    fun handleDragEnd() {
        if (draggedItemId != null) {
            val finalIds = currentLocalItems.map { it.id }
            draggedItemId = null
            dragOffset = Offset.Zero
            currentOnCommit(finalIds)
        }
    }

    fun handleDragCancel() {
        draggedItemId = null
        dragOffset = Offset.Zero
    }

    CompositionLocalProvider(LocalViewConfiguration provides customViewConfig) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            itemsIndexed(localOrderedItems, key = { _, it -> it.id }) { index, item ->
                val isDragging = draggedItemId == item.id
                val itemModifier = Modifier
                    .zIndex(if (isDragging) 20f else 0f)
                    .graphicsLayer {
                        if (isDragging) {
                            translationX = dragOffset.x
                            translationY = dragOffset.y
                            scaleX = 1.045f
                            scaleY = 1.045f
                            shadowElevation = 28f
                            alpha = 0.96f
                        }
                    }
                    .then(
                        if (canEdit) {
                            Modifier.pointerInput(item.id, isReorderModeActive, canEdit) {
                                if (isReorderModeActive) {
                                    detectDragGestures(
                                        onDragStart = { handleDragStart(item.id) },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            handleDragDelta(dragAmount)
                                        },
                                        onDragEnd = { handleDragEnd() },
                                        onDragCancel = { handleDragCancel() }
                                    )
                                } else {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { handleDragStart(item.id) },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            handleDragDelta(dragAmount)
                                        },
                                        onDragEnd = { handleDragEnd() },
                                        onDragCancel = { handleDragCancel() }
                                    )
                                }
                            }
                        } else {
                            Modifier
                        }
                    )

                Box(modifier = itemModifier) {
                    cardContent(
                        item,
                        index,
                        localOrderedItems.size,
                        isDragging
                    ) { dir ->
                        val targetIdx = (index + dir).coerceIn(0, localOrderedItems.lastIndex)
                        if (targetIdx != index) {
                            val mutable = localOrderedItems.toMutableList()
                            val moved = mutable.removeAt(index)
                            mutable.add(targetIdx, moved)
                            localOrderedItems = mutable
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentOnCommit(mutable.map { it.id })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuSettingsGridCard(
    item: CatalogRecord,
    canEdit: Boolean,
    activePlatforms: List<String> = emptyList(),
    isDragging: Boolean = false,
    isReorderModeActive: Boolean = false,
    orderPosition: Int = 0,
    canMovePrev: Boolean = false,
    canMoveNext: Boolean = false,
    onMovePrev: () -> Unit = {},
    onMoveNext: () -> Unit = {},
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDragging -> SoftGoldHighlight
                item.isActive -> ElevatedIvoryCard
                else -> ElevatedIvoryCard.copy(alpha = 0.55f)
            }
        ),
        border = if (isDragging) {
            BorderStroke(2.dp, RichEspressoPrimary)
        } else if (isReorderModeActive) {
            BorderStroke(1.2.dp, DeepCaramelGold.copy(alpha = 0.65f))
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 14.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isReorderModeActive || isDragging) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RichEspressoPrimary,
                            modifier = Modifier.height(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragIndicator,
                                    contentDescription = "Geser",
                                    tint = SoftGoldHighlight,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "#$orderPosition",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElevatedIvoryCard
                                )
                            }
                        }
                    }
                    WaffleoProductIllustration(
                        itemName = item.name,
                        isWaffle = item.category == "Egg Waffle",
                        imageUrl = item.imageUrl,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        SmallTagPill(
                            text = if (item.subCategory.isNotBlank()) "${item.category} • ${item.subCategory}" else item.category,
                            bg = SoftOatSurface,
                            fg = DeepCaramelGold
                        )
                    }
                }

                if (canEdit) {
                    if (isReorderModeActive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (canMovePrev) SoftOatSurface else SoftOatSurface.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = canMovePrev, onClick = onMovePrev)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Geser Kiri",
                                        tint = if (canMovePrev) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (canMoveNext) SoftOatSurface else SoftOatSurface.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = canMoveNext, onClick = onMoveNext)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Geser Kanan",
                                        tint = if (canMoveNext) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = item.isActive,
                                onCheckedChange = { onToggleActive() },
                                colors = SwitchDefaults.colors(checkedThumbColor = MatchaSageSuccess),
                                modifier = Modifier.scale(0.75f)
                            )
                            Surface(
                                shape = CircleShape,
                                color = SoftOatSurface,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onEdit)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = RichEspressoPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val displayChips = remember(item, activePlatforms) {
                val list = mutableListOf("Di tempat" to item.priceWalkIn)
                activePlatforms.forEach { pName ->
                    list.add(pName to item.priceForPlatformName(pName))
                }
                list
            }

            displayChips.chunked(2).forEachIndexed { idx, pair ->
                if (idx > 0) Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pair.forEach { (channelLabel, priceVal) ->
                        val isWalkIn = channelLabel == "Di tempat"
                        PriceChannelChip(
                            channel = channelLabel,
                            price = priceVal,
                            bg = if (isWalkIn) SoftGoldHighlight else SoftOatSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceChannelChip(
    channel: String,
    price: Long,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = channel,
                style = MaterialTheme.typography.labelSmall,
                color = MutedCocoaText
            )
            Text(
                text = WaffleoViewModel.formatIdr(price),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = RichEspressoPrimary
            )
        }
    }
}

@Composable
private fun ModifierSettingsGridCard(
    item: CatalogRecord,
    canEdit: Boolean,
    activePlatforms: List<String> = emptyList(),
    isDragging: Boolean = false,
    isReorderModeActive: Boolean = false,
    orderPosition: Int = 0,
    canMovePrev: Boolean = false,
    canMoveNext: Boolean = false,
    onMovePrev: () -> Unit = {},
    onMoveNext: () -> Unit = {},
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDragging -> SoftGoldHighlight
                item.isActive -> ElevatedIvoryCard
                else -> ElevatedIvoryCard.copy(alpha = 0.55f)
            }
        ),
        border = if (isDragging) {
            BorderStroke(2.dp, RichEspressoPrimary)
        } else if (isReorderModeActive) {
            BorderStroke(1.2.dp, DeepCaramelGold.copy(alpha = 0.65f))
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 14.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isReorderModeActive || isDragging) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RichEspressoPrimary,
                            modifier = Modifier.height(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragIndicator,
                                    contentDescription = "Geser",
                                    tint = SoftGoldHighlight,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "#$orderPosition",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElevatedIvoryCard
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        val isFlavour = item.category.equals("FLAVOUR", ignoreCase = true)
                        val adonanColor = if (isFlavour) AdonanPalette.getColorForFlavour(item.name) else RichEspressoPrimary
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isFlavour) {
                                Surface(
                                    shape = CircleShape,
                                    color = adonanColor,
                                    modifier = Modifier.size(9.dp)
                                ) {}
                            }
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = adonanColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        SmallTagPill(
                            text = if (item.subCategory.isNotBlank()) "${item.category} • ${item.subCategory}" else item.category,
                            bg = if (isFlavour) AdonanPalette.getSoftBgForFlavour(item.name) else SoftGoldHighlight,
                            fg = if (isFlavour) adonanColor else DeepCaramelGold
                        )
                    }
                }

                if (canEdit) {
                    if (isReorderModeActive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (canMovePrev) SoftOatSurface else SoftOatSurface.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = canMovePrev, onClick = onMovePrev)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Geser Kiri",
                                        tint = if (canMovePrev) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (canMoveNext) SoftOatSurface else SoftOatSurface.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = canMoveNext, onClick = onMoveNext)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Geser Kanan",
                                        tint = if (canMoveNext) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = item.isActive,
                                onCheckedChange = { onToggleActive() },
                                colors = SwitchDefaults.colors(checkedThumbColor = MatchaSageSuccess),
                                modifier = Modifier.scale(0.75f)
                            )
                            Surface(
                                shape = CircleShape,
                                color = SoftOatSurface,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onEdit)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = RichEspressoPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val displayChips = remember(item, activePlatforms) {
                val list = mutableListOf("Di tempat" to item.priceWalkIn)
                activePlatforms.forEach { pName ->
                    list.add(pName to item.priceForPlatformName(pName))
                }
                list
            }

            displayChips.chunked(2).forEachIndexed { idx, pair ->
                if (idx > 0) Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pair.forEach { (channelLabel, priceVal) ->
                        val isWalkIn = channelLabel == "Di tempat"
                        PriceChannelChip(
                            channel = channelLabel,
                            price = priceVal,
                            bg = if (isWalkIn) SoftGoldHighlight else SoftOatSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientSettingsGridCard(
    item: CatalogRecord,
    canEdit: Boolean,
    onQuickRestock: () -> Unit,
    onEdit: () -> Unit
) {
    val minStock = remember(item.metadataJson) {
        try { JSONObject(item.metadataJson).optDouble("minStock", 300.0) } catch (e: Exception) { 300.0 }
    }
    val isLow = item.stockQty <= minStock
    val fillRatio = (item.stockQty / (minStock * 5.0).coerceAtLeast(1000.0)).toFloat().coerceIn(0.05f, 1f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLow) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = TerracottaDanger,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                SmallTagPill(
                    text = "${item.stockQty.toInt()} ${item.stockUnit}",
                    bg = if (isLow) SoftAmberBg else SoftSageBg,
                    fg = if (isLow) TerracottaDanger else MatchaSageSuccess
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SoftOatSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillRatio)
                        .clip(RoundedCornerShape(50))
                        .background(if (isLow) TerracottaDanger else MatchaSageSuccess)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HPP: ${WaffleoViewModel.formatIdr(item.costPerUnit)}/${item.stockUnit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedCocoaText
                )

                if (canEdit) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftSageBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onQuickRestock)
                        ) {
                            Text(
                                text = "+1.000",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MatchaSageSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = SoftOatSurface,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onEdit)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = RichEspressoPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromoSettingsGridCard(
    promo: CatalogRecord,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    val benefitBadge = remember(promo) {
        when (promo.category) {
            "BUNDLING" -> "Hemat ${WaffleoViewModel.formatIdr(promo.priceWalkIn)} / Pasang"
            "PERCENT" -> "Diskon ${promo.priceWalkIn}% (Min. 2 Waffle)"
            "NOMINAL_MIN" -> {
                val minOrd = try { JSONObject(promo.metadataJson).optLong("minOrder", 80000L) } catch (e: Exception) { 80000L }
                "Potongan ${WaffleoViewModel.formatIdr(promo.priceWalkIn)} (Min. ${WaffleoViewModel.formatIdr(minOrd)})"
            }
            "PERCENT_CAP" -> {
                val meta = try { JSONObject(promo.metadataJson) } catch (e: Exception) { JSONObject() }
                val cap = meta.optLong("maxCap", 15000L)
                "Diskon ${promo.priceWalkIn}% (Maks. ${WaffleoViewModel.formatIdr(cap)})"
            }
            else -> "Promo ${WaffleoViewModel.formatIdr(promo.priceWalkIn)}"
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (promo.isActive) ElevatedIvoryCard else ElevatedIvoryCard.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftGoldHighlight,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = DeepCaramelGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = promo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SmallTagPill(
                            text = benefitBadge,
                            bg = SoftSageBg,
                            fg = MatchaSageSuccess
                        )
                        SmallTagPill(
                            text = promo.category,
                            bg = SoftOatSurface,
                            fg = MutedCocoaText
                        )
                    }
                }
            }

            if (canEdit) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = promo.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(checkedThumbColor = MatchaSageSuccess),
                        modifier = Modifier.scale(0.8f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = SoftOatSurface,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onEdit)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = RichEspressoPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    targetType: String, // "MENU" or "MODIFIER"
    onDismiss: () -> Unit,
    onSave: (categoryName: String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val isMenu = targetType == "MENU"
    val title = if (isMenu) "Tambah Kategori Menu Baru" else "Tambah Kategori Modifier Baru"
    val hint = if (isMenu) "Misal: Toast, Pastry, Croissant, Snacks" else "Misal: Rasa, Level Pedas, Topping Extra"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 440.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = if (isMenu) "Kategori menu akan langsung muncul di filter & POS." else "Kategori modifier dapat dihubungkan ke item menu mana saja.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedCocoaText)
                    }
                }

                WaffleoSoftTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = "Nama Kategori",
                    placeholder = hint,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftOatSurface,
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                            Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val trimmed = categoryName.trim()
                            if (trimmed.isNotBlank()) {
                                onSave(trimmed)
                                onDismiss()
                            }
                        },
                        enabled = categoryName.isNotBlank(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCaramelGold,
                            contentColor = Color.White,
                            disabledContainerColor = SubtleSandBorder,
                            disabledContentColor = MutedCocoaText
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Kategori", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditCategoryModalDialog(
    targetType: String, // "MENU" or "MODIFIER"
    categoryName: String,
    onDismiss: () -> Unit,
    onSave: (newName: String) -> Unit,
    onDelete: () -> Unit
) {
    var updatedName by remember { mutableStateOf(categoryName) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    val isMenu = targetType == "MENU"
    val title = if (isMenu) "Edit Kategori Menu" else "Edit Kategori Modifier"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 460.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = if (isMenu) "Atur nama kategori menu" else "Atur nama kategori modifier",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedCocoaText)
                    }
                }

                WaffleoSoftTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it },
                    label = "Nama Kategori",
                    placeholder = "Nama Kategori...",
                    modifier = Modifier.fillMaxWidth()
                )

                if (showConfirmDelete) {
                    Surface(
                        color = SoftTerracottaBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Yakin ingin menghapus kategori '$categoryName'?",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaDanger
                            )
                            Text(
                                text = "Item/modifier di kategori ini akan dipindahkan ke kategori standar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = RichEspressoPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { showConfirmDelete = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftOatSurface, contentColor = RichEspressoPrimary)
                                ) {
                                    Text("Batal")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        onDelete()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaDanger, contentColor = Color.White)
                                ) {
                                    Text("Ya, Hapus", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showConfirmDelete = true },
                        modifier = Modifier
                            .background(SoftTerracottaBg, CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Kategori", tint = TerracottaDanger)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftOatSurface,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onDismiss)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                                Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                            }
                        }

                        Button(
                            onClick = {
                                val trimmed = updatedName.trim()
                                if (trimmed.isNotBlank()) {
                                    onSave(trimmed)
                                    onDismiss()
                                }
                            },
                            enabled = updatedName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepCaramelGold,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Simpan Perubahan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSubCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onSave: (subCategoryName: String) -> Unit
) {
    var subCategoryName by remember { mutableStateOf("") }
    val title = "Tambah Sub Kategori"
    val hint = "Misal: Spread, Crunchy, Premium, Coffee..."

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 440.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "Kategori Utama: $categoryName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedCocoaText)
                    }
                }

                WaffleoSoftTextField(
                    value = subCategoryName,
                    onValueChange = { subCategoryName = it },
                    label = "Nama Sub Kategori",
                    placeholder = hint,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftOatSurface,
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                            Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val trimmed = subCategoryName.trim()
                            if (trimmed.isNotBlank()) {
                                onSave(trimmed)
                                onDismiss()
                            }
                        },
                        enabled = subCategoryName.isNotBlank(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCaramelGold,
                            contentColor = Color.White,
                            disabledContainerColor = SubtleSandBorder,
                            disabledContentColor = MutedCocoaText
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Sub Kategori", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditSubCategoryDialog(
    categoryName: String,
    subCategoryName: String,
    onDismiss: () -> Unit,
    onSave: (newName: String) -> Unit,
    onDelete: () -> Unit
) {
    var updatedName by remember { mutableStateOf(subCategoryName) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 460.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Sub Kategori",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "Kategori Utama: $categoryName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedCocoaText)
                    }
                }

                WaffleoSoftTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it },
                    label = "Nama Sub Kategori",
                    placeholder = "Nama Sub Kategori...",
                    modifier = Modifier.fillMaxWidth()
                )

                if (showConfirmDelete) {
                    Surface(
                        color = SoftTerracottaBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Hapus Sub Kategori '$subCategoryName'?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaDanger
                            )
                            Text(
                                text = "Item di dalam sub-kategori ini akan tetap ada dengan sub-kategori dikosongkan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = RichEspressoPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftOatSurface,
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable { showConfirmDelete = false }
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                        Text("Batal", style = MaterialTheme.typography.labelMedium, color = RichEspressoPrimary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        onDelete()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaDanger),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Ya, Hapus", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showConfirmDelete = true },
                        modifier = Modifier
                            .background(SoftTerracottaBg, CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Sub Kategori", tint = TerracottaDanger)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftOatSurface,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onDismiss)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                                Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                            }
                        }
                        Button(
                            onClick = {
                                val trimmed = updatedName.trim()
                                if (trimmed.isNotBlank()) {
                                    onSave(trimmed)
                                    onDismiss()
                                }
                            },
                            enabled = updatedName.isNotBlank() && !showConfirmDelete,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepCaramelGold,
                                contentColor = Color.White,
                                disabledContainerColor = SubtleSandBorder,
                                disabledContentColor = MutedCocoaText
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private data class AuditBadgeInfo(
    val label: String,
    val icon: ImageVector,
    val bg: Color,
    val fg: Color
)

private fun getAuditBadgeInfo(actionType: String, category: String): AuditBadgeInfo {
    return when {
        actionType.contains("ERASE") -> AuditBadgeInfo("Pesanan Dihapus", Icons.Default.DeleteOutline, SoftTerracottaBg, TerracottaDanger)
        actionType.contains("CANCEL") -> AuditBadgeInfo("Pesanan Dibatalkan", Icons.Default.Cancel, SoftTerracottaBg, TerracottaDanger)
        actionType.contains("DONE") -> AuditBadgeInfo("Pesanan Selesai", Icons.Default.CheckCircle, SoftSageBg, MatchaSageSuccess)
        actionType.contains("PAID") -> AuditBadgeInfo("Pembayaran Lunas", Icons.Default.Payments, SoftSageBg, MatchaSageSuccess)
        actionType.contains("MONEY_TX_DELETED") -> AuditBadgeInfo("Hapus Money Manager", Icons.Default.DeleteOutline, SoftTerracottaBg, TerracottaDanger)
        actionType.contains("MONEY_TX_UPDATED") -> AuditBadgeInfo("Edit Money Manager", Icons.Default.Edit, SoftAmberBg, HoneyAmberWarning)
        actionType.contains("MONEY_TX_ADDED") || actionType.contains("MONEY") -> AuditBadgeInfo("Input Money Manager", Icons.Default.Payments, SoftSageBg, MatchaSageSuccess)
        actionType.contains("EDIT") -> AuditBadgeInfo("Perubahan Pesanan", Icons.Default.Edit, SoftAmberBg, HoneyAmberWarning)
        actionType.contains("CREATED") -> AuditBadgeInfo("Pesanan Baru", Icons.Default.RestaurantMenu, SoftGoldHighlight, DeepCaramelGold)
        actionType.contains("CATALOG_DELETED") -> AuditBadgeInfo("Menu Dihapus", Icons.Default.DeleteOutline, SoftTerracottaBg, TerracottaDanger)
        actionType.contains("CATALOG_CATEGORY") -> AuditBadgeInfo("Kategori Baru", Icons.Default.Add, SoftGoldHighlight, DeepCaramelGold)
        actionType.contains("CATALOG") -> AuditBadgeInfo("Katalog & Menu", Icons.Default.RestaurantMenu, SoftGoldHighlight, DeepCaramelGold)
        actionType.contains("STOCK") -> AuditBadgeInfo("Stok Bahan Baku", Icons.Default.Inventory2, SoftOatSurface, RichEspressoPrimary)
        actionType.contains("STORE") -> AuditBadgeInfo("Operasional Toko", Icons.Default.Storefront, SoftOatSurface, RichEspressoPrimary)
        actionType.contains("STAFF") || actionType.contains("ACCOUNT") || actionType.contains("LOGIN") -> AuditBadgeInfo("Akses & Akun", Icons.Default.Person, SoftOatSurface, RichEspressoPrimary)
        actionType.contains("SECURITY") -> AuditBadgeInfo("Peringatan Sistem", Icons.Default.Warning, SoftTerracottaBg, TerracottaDanger)
        else -> AuditBadgeInfo(actionType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, Icons.Default.History, SoftOatSurface, RichEspressoPrimary)
    }
}

@Composable
private fun SmallTagPill(
    text: String,
    bg: Color,
    fg: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatalogEditorDialog(
    initialRecord: CatalogRecord,
    isNew: Boolean,
    canDelete: Boolean = false,
    customCategories: List<String> = emptyList(),
    availableModifiers: List<CatalogRecord> = emptyList(),
    activePlatforms: List<String> = emptyList(),
    onGetSubCategories: (String) -> List<String> = { emptyList() },
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (CatalogRecord) -> Unit
) {
    var name by remember { mutableStateOf(if (isNew) "" else initialRecord.name) }
    var category by remember { mutableStateOf(initialRecord.category) }
    var subCategory by remember { mutableStateOf(if (isNew) "" else initialRecord.subCategory) }
    var isTypingCustomSubCategory by remember { mutableStateOf(false) }
    var customSubCategoryInput by remember { mutableStateOf("") }
    var walkIn by remember { mutableStateOf(if (isNew || initialRecord.priceWalkIn == 0L) "" else initialRecord.priceWalkIn.toString()) }
    var grab by remember { mutableStateOf(if (isNew || initialRecord.priceGrabFood == 0L) "" else initialRecord.priceGrabFood.toString()) }
    var gofood by remember { mutableStateOf(if (isNew || initialRecord.priceGoFood == 0L) "" else initialRecord.priceGoFood.toString()) }
    var shopee by remember { mutableStateOf(if (isNew || initialRecord.priceShopeeFood == 0L) "" else initialRecord.priceShopeeFood.toString()) }

    var platformPricesMap by remember(initialRecord) {
        mutableStateOf(if (isNew) emptyMap() else initialRecord.getPlatformPricesMap())
    }
    var stockQty by remember { mutableStateOf(if (isNew || initialRecord.stockQty == 0.0) "" else initialRecord.stockQty.toInt().toString()) }
    var stockUnit by remember { mutableStateOf(if (isNew) "gram" else initialRecord.stockUnit) }
    var imageUrl by remember { mutableStateOf(if (isNew) "" else initialRecord.imageUrl) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val initialAllowedModifiers = remember { initialRecord.getAllowedModifiers().toMutableMap() }
    var selectedModifiersMap by remember { mutableStateOf(initialAllowedModifiers.toMap()) }
    val modifierCategoriesGrouped = remember(availableModifiers) { availableModifiers.groupBy { it.category } }

    var activeCalcPriceTarget by remember { mutableStateOf<String?>(null) }

    val currentSubCatOptions = remember(category) {
        onGetSubCategories(category).distinct().filter { it.isNotBlank() }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { imageUrl = it.toString() }
    }

    val categoryOptions = when (initialRecord.recordType) {
        "MENU_ITEM" -> (customCategories + listOf(initialRecord.category)).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
        "MODIFIER" -> (customCategories + availableModifiers.map { it.category } + listOf(initialRecord.category)).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
        "INGREDIENT" -> listOf("RAW_MATERIAL")
        "PROMO" -> listOf("BUNDLING", "PERCENT", "NOMINAL_MIN", "PERCENT_CAP")
        else -> listOf(initialRecord.category)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isNew) "Tambah Data" else "Edit ${initialRecord.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = RichEspressoPrimary
                )

                // PHOTO SELECTION & PREVIEW CARD
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftOatSurface,
                    border = BorderStroke(1.dp, SubtleSandBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        WaffleoProductIllustration(
                            itemName = name.ifBlank { "Menu" },
                            isWaffle = category == "Egg Waffle",
                            imageUrl = imageUrl,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Foto / Gambar Menu",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary
                            )
                            Text(
                                text = if (imageUrl.isNotBlank()) "Foto kustom tersimpan" else "Gunakan foto kustom atau ilustrasi standar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = RichEspressoPrimary,
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (imageUrl.isNotBlank()) "Ubah Foto" else "Pilih Foto",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (imageUrl.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = SoftTerracottaBg,
                                        modifier = Modifier
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(50))
                                            .clickable { imageUrl = "" }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Hapus",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TerracottaDanger
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (initialRecord.recordType != "BRANCH_CONFIG") {
                    WaffleoSingleSelectDropdown(
                        label = "Kategori",
                        selectedText = category,
                        options = categoryOptions,
                        optionLabel = { it },
                        onOptionSelected = {
                            category = it
                            val nextSubs = onGetSubCategories(it)
                            if (subCategory.isNotBlank() && subCategory !in nextSubs) {
                                subCategory = ""
                            }
                        }
                    )
                }

                // Kolom Sub Kategori tepat di bawah Kategori untuk Menu & Modifier
                if (initialRecord.recordType in listOf("MENU_ITEM", "MODIFIER")) {
                    val subDropdownOptions = remember(currentSubCatOptions, subCategory) {
                        val list = mutableListOf("Tanpa Sub Kategori")
                        currentSubCatOptions.forEach { if (it !in list) list.add(it) }
                        if (subCategory.isNotBlank() && subCategory !in list) {
                            list.add(subCategory)
                        }
                        list.add("+ Ketik Sub Kategori Baru...")
                        list
                    }

                    if (isTypingCustomSubCategory) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WaffleoSoftTextField(
                                value = customSubCategoryInput,
                                onValueChange = {
                                    customSubCategoryInput = it
                                    subCategory = it.trim()
                                },
                                label = "Sub Kategori (Kustom)",
                                placeholder = "Ketik nama sub-kategori...",
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftOatSurface,
                                border = BorderStroke(1.dp, SubtleSandBorder),
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        isTypingCustomSubCategory = false
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
                                    Text("Pilih List ▾", style = MaterialTheme.typography.labelMedium, color = RichEspressoPrimary)
                                }
                            }
                        }
                    } else {
                        WaffleoSingleSelectDropdown(
                            label = "Sub Kategori",
                            selectedText = if (subCategory.isNotBlank()) subCategory else "Tanpa Sub Kategori",
                            options = subDropdownOptions,
                            optionLabel = { it },
                            onOptionSelected = { chosen ->
                                if (chosen == "+ Ketik Sub Kategori Baru...") {
                                    isTypingCustomSubCategory = true
                                    customSubCategoryInput = ""
                                } else if (chosen == "Tanpa Sub Kategori") {
                                    subCategory = ""
                                } else {
                                    subCategory = chosen
                                }
                            }
                        )
                    }
                }

                WaffleoSoftTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (initialRecord.recordType == "BRANCH_CONFIG") {
                            category = it
                        }
                    },
                    label = if (initialRecord.recordType == "BRANCH_CONFIG") "Nama Platform" else "Nama",
                    placeholder = if (initialRecord.recordType == "BRANCH_CONFIG") "e.g. GrabFood, ShopeeFood..." else "Masukkan nama...",
                    modifier = Modifier.fillMaxWidth()
                )

                if (initialRecord.recordType == "INGREDIENT") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WaffleoSoftTextField(
                            value = stockQty,
                            onValueChange = { stockQty = it.filter { ch -> ch.isDigit() } },
                            label = "Jumlah Stok",
                            placeholder = "1000",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        WaffleoSoftTextField(
                            value = stockUnit,
                            onValueChange = { stockUnit = it },
                            label = "Satuan (gram/ml/pcs)",
                            placeholder = "gram",
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else if (initialRecord.recordType == "BRANCH_CONFIG") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CatalogPriceWithCalcField(
                            value = walkIn,
                            onValueChange = { walkIn = it },
                            label = "Komisi Platform (%)",
                            placeholder = "0",
                            prefix = "",
                            suffix = "%",
                            onOpenCalculator = { activeCalcPriceTarget = "WALK_IN" },
                            modifier = Modifier.weight(1f)
                        )
                        CatalogPriceWithCalcField(
                            value = grab,
                            onValueChange = {
                                val digits = it.filter { ch -> ch.isDigit() }
                                grab = digits
                                gofood = digits
                                shopee = digits
                            },
                            label = "Potongan By Order (Rp)",
                            placeholder = "0",
                            prefix = "Rp",
                            suffix = "",
                            onOpenCalculator = { activeCalcPriceTarget = "GRABFOOD" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Pencairan Dana Bank Selection Section
                    var selectedBank by remember { mutableStateOf(subCategory.ifBlank { "BCA" }) }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SoftOatSurface,
                        border = BorderStroke(1.dp, SubtleSandBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Pencairan Dana :",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Group Pill: Bank
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = RichEspressoPrimary,
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = SoftGoldHighlight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Grup Akun: Bank",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftGoldHighlight
                                        )
                                    }
                                }

                                // Bank account choice pills: BCA, Bank Mandiri
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("BCA", "Bank Mandiri").forEach { bankName ->
                                        val isSelectedBank = selectedBank.equals(bankName, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelectedBank) SoftGoldHighlight else ElevatedIvoryCard,
                                            border = BorderStroke(1.5.dp, if (isSelectedBank) DeepCaramelGold else SubtleSandBorder),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    selectedBank = bankName
                                                    subCategory = bankName
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = bankName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelectedBank) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelectedBank) RichEspressoPrimary else MutedCocoaText
                                                )
                                                if (isSelectedBank) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = DeepCaramelGold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    data class PriceInputSpec(
                        val key: String,
                        val label: String,
                        val value: String,
                        val onValueChange: (String) -> Unit
                    )

                    val allPriceSpecs = remember(walkIn, platformPricesMap, activePlatforms, initialRecord, isNew) {
                        val list = mutableListOf(
                            PriceInputSpec("WALK_IN", "Harga Di tempat / Nilai", walkIn) { walkIn = it }
                        )
                        activePlatforms.forEach { pName ->
                            val existingVal = platformPricesMap[pName] ?: initialRecord.priceForPlatformName(pName)
                            val valStr = if (existingVal == 0L && !platformPricesMap.containsKey(pName) && isNew) "" else if (existingVal == 0L) "" else existingVal.toString()
                            val currentVal = platformPricesMap[pName]?.let { if (it == 0L) "" else it.toString() } ?: valStr
                            list.add(
                                PriceInputSpec(pName, "Harga $pName", currentVal) { newVal ->
                                    val digits = newVal.filter { ch -> ch.isDigit() }
                                    val priceVal = digits.toLongOrNull() ?: 0L
                                    platformPricesMap = platformPricesMap + (pName to priceVal)
                                }
                            )
                        }
                        list
                    }

                    allPriceSpecs.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            pair.forEach { spec ->
                                CatalogPriceWithCalcField(
                                    value = spec.value,
                                    onValueChange = spec.onValueChange,
                                    label = spec.label,
                                    placeholder = "0",
                                    onOpenCalculator = { activeCalcPriceTarget = spec.key },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Sub-item / Modifier connection selector for Menu Items
                if (initialRecord.recordType == "MENU_ITEM" && modifierCategoriesGrouped.isNotEmpty()) {
                    val totalActiveCategories = selectedModifiersMap.size
                    val totalSelectedOptions = selectedModifiersMap.values.sumOf { it.size }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SoftOatSurface,
                        border = BorderStroke(1.dp, SubtleSandBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Section Header with Icon & Active Summary Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SoftGoldHighlight,
                                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.6f)),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Layers,
                                                contentDescription = null,
                                                tint = DeepCaramelGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Koneksi Modifier (Sub-Item)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RichEspressoPrimary
                                        )
                                        Text(
                                            text = "Pilih kategori & opsi modifier untuk menu ini di POS",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedCocoaText
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = ElevatedIvoryCard,
                                    border = BorderStroke(1.dp, if (totalActiveCategories > 0) DeepCaramelGold else SubtleSandBorder),
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (totalActiveCategories > 0) MatchaSageSuccess else MutedCocoaText.copy(alpha = 0.4f))
                                        )
                                        Text(
                                            text = if (totalActiveCategories > 0) "$totalActiveCategories Kategori ($totalSelectedOptions Opsi)" else "Belum Ada",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (totalActiveCategories > 0) RichEspressoPrimary else MutedCocoaText
                                        )
                                    }
                                }
                            }

                            // Category Cards List
                            modifierCategoriesGrouped.forEach { (modCat, modOptions) ->
                                val isCatActive = selectedModifiersMap.containsKey(modCat)
                                val selectedItemsInCat = selectedModifiersMap[modCat] ?: emptyList()

                                val catIcon = when {
                                    modCat.contains("FLAVOUR", ignoreCase = true) || modCat.contains("RASA", ignoreCase = true) -> Icons.Default.Cake
                                    modCat.contains("SWEET", ignoreCase = true) || modCat.contains("GULA", ignoreCase = true) || modCat.contains("MANIS", ignoreCase = true) -> Icons.Default.LocalCafe
                                    modCat.contains("ICE", ignoreCase = true) || modCat.contains("ES", ignoreCase = true) || modCat.contains("DINGIN", ignoreCase = true) -> Icons.Default.AcUnit
                                    modCat.contains("ADD", ignoreCase = true) || modCat.contains("TOPPING", ignoreCase = true) || modCat.contains("EXTRA", ignoreCase = true) -> Icons.Default.Stars
                                    else -> Icons.Default.AutoAwesome
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isCatActive) ElevatedIvoryCard else ElevatedIvoryCard.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        width = if (isCatActive) 1.5.dp else 1.dp,
                                        color = if (isCatActive) DeepCaramelGold else SubtleSandBorder.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Header Row: Category Icon + Title + Status + Switch Toggle
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    val newMap = selectedModifiersMap.toMutableMap()
                                                    if (isCatActive) {
                                                        newMap.remove(modCat)
                                                    } else {
                                                        newMap[modCat] = modOptions.map { it.name }
                                                    }
                                                    selectedModifiersMap = newMap
                                                }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (isCatActive) SoftGoldHighlight else SoftOatSurface,
                                                    border = BorderStroke(1.dp, if (isCatActive) ChampagneGoldAccent else SubtleSandBorder),
                                                    modifier = Modifier.size(38.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = catIcon,
                                                            contentDescription = null,
                                                            tint = if (isCatActive) DeepCaramelGold else MutedCocoaText.copy(alpha = 0.5f),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = modCat,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isCatActive) RichEspressoPrimary else MutedCocoaText
                                                    )
                                                    if (isCatActive) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(6.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (selectedItemsInCat.isNotEmpty()) MatchaSageSuccess else HoneyAmberWarning)
                                                            )
                                                            Text(
                                                                text = "${selectedItemsInCat.size} dari ${modOptions.size} opsi aktif",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = if (selectedItemsInCat.isNotEmpty()) MatchaSageSuccess else MutedCocoaText
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = "${modOptions.size} opsi tersedia • Nonaktif",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MutedCocoaText.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                }
                                            }

                                            Switch(
                                                checked = isCatActive,
                                                onCheckedChange = { checked ->
                                                    val newMap = selectedModifiersMap.toMutableMap()
                                                    if (checked) {
                                                        newMap[modCat] = modOptions.map { it.name }
                                                    } else {
                                                        newMap.remove(modCat)
                                                    }
                                                    selectedModifiersMap = newMap
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = ElevatedIvoryCard,
                                                    checkedTrackColor = DeepCaramelGold,
                                                    uncheckedThumbColor = SubtleSandBorder,
                                                    uncheckedTrackColor = SoftOatSurface
                                                ),
                                                modifier = Modifier.scale(0.85f)
                                            )
                                        }

                                        // Expanded Options Panel when Category is Active
                                        if (isCatActive) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                thickness = 0.8.dp,
                                                color = SubtleSandBorder.copy(alpha = 0.5f)
                                            )

                                            // Quick Actions Bar
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Pilih Opsi yang Diizinkan:",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MutedCocoaText
                                                )

                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = SoftGoldHighlight.copy(alpha = 0.6f),
                                                        border = BorderStroke(0.8.dp, ChampagneGoldAccent),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(50))
                                                            .clickable {
                                                                val newMap = selectedModifiersMap.toMutableMap()
                                                                newMap[modCat] = modOptions.map { it.name }
                                                                selectedModifiersMap = newMap
                                                            }
                                                    ) {
                                                        Text(
                                                            text = "Pilih Semua",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = DeepCaramelGold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = SoftOatSurface,
                                                        border = BorderStroke(0.8.dp, SubtleSandBorder),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(50))
                                                            .clickable {
                                                                val newMap = selectedModifiersMap.toMutableMap()
                                                                newMap[modCat] = emptyList()
                                                                selectedModifiersMap = newMap
                                                            }
                                                    ) {
                                                        Text(
                                                            text = "Kosongkan",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MutedCocoaText,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Options Chips
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                modOptions.forEach { opt ->
                                                    val isOptionChecked = selectedItemsInCat.contains(opt.name)
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = if (isOptionChecked) SoftGoldHighlight else ElevatedIvoryCard,
                                                        border = BorderStroke(
                                                            width = if (isOptionChecked) 1.5.dp else 1.dp,
                                                            color = if (isOptionChecked) DeepCaramelGold else SubtleSandBorder
                                                        ),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(50))
                                                            .clickable {
                                                                val newMap = selectedModifiersMap.toMutableMap()
                                                                val currentList = (newMap[modCat] ?: emptyList()).toMutableList()
                                                                if (isOptionChecked) {
                                                                    currentList.remove(opt.name)
                                                                } else {
                                                                    currentList.add(opt.name)
                                                                }
                                                                newMap[modCat] = currentList
                                                                selectedModifiersMap = newMap
                                                            }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            if (isOptionChecked) {
                                                                Icon(
                                                                    imageVector = Icons.Default.CheckCircle,
                                                                    contentDescription = null,
                                                                    tint = MatchaSageSuccess,
                                                                    modifier = Modifier.size(15.dp)
                                                                )
                                                            } else {
                                                                Icon(
                                                                    imageVector = Icons.Default.Add,
                                                                    contentDescription = null,
                                                                    tint = MutedCocoaText.copy(alpha = 0.4f),
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                            }
                                                            Text(
                                                                text = opt.name,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = if (isOptionChecked) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isOptionChecked) RichEspressoPrimary else MutedCocoaText
                                                            )
                                                            if (opt.priceWalkIn > 0L) {
                                                                Text(
                                                                    text = "+${java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(opt.priceWalkIn)}",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isOptionChecked) DeepCaramelGold else MutedCocoaText.copy(alpha = 0.6f)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Row: [ Hapus ] on Bottom-Left (for existing Menu/Modifier/Stok/Promo) + [ Batal | Simpan ] on Bottom-Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isNew && canDelete && onDelete != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftTerracottaBg,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { showDeleteConfirmDialog = true }
                                .testTag("catalog_delete_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus",
                                    tint = TerracottaDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Hapus",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaDanger
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftOatSurface,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onDismiss)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                                Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                            }
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val baseRecord = initialRecord.withSubCategory(subCategory)
                                    val grabPrice = platformPricesMap["GrabFood"] ?: platformPricesMap["Grab"] ?: initialRecord.priceGrabFood
                                    val goPrice = platformPricesMap["GoFood"] ?: platformPricesMap["Go"] ?: initialRecord.priceGoFood
                                    val shopeePrice = platformPricesMap["ShopeeFood"] ?: platformPricesMap["Shopee"] ?: initialRecord.priceShopeeFood

                                    val updatedMetadata = if (initialRecord.recordType == "BRANCH_CONFIG") {
                                        val pct = walkIn.toLongOrNull() ?: 0L
                                        val fee = grab.toLongOrNull() ?: 0L
                                        val cleanBrName = com.example.data.model.formatCleanBranchName(initialRecord.branchId, initialRecord.branchId)
                                        """{"commissionPercent":$pct,"perOrderFee":$fee,"bankAccount":"Rekening Bank BCA (Cabang $cleanBrName)"}"""
                                    } else if (initialRecord.recordType in listOf("MENU_ITEM", "MODIFIER")) {
                                        baseRecord.withPlatformPrices(platformPricesMap, selectedModifiersMap)
                                    } else {
                                        baseRecord.metadataJson
                                    }
                                    onSave(
                                        baseRecord.copy(
                                            name = name.trim(),
                                            category = category,
                                            priceWalkIn = walkIn.toLongOrNull() ?: 0L,
                                            priceGrabFood = grabPrice,
                                            priceGoFood = goPrice,
                                            priceShopeeFood = shopeePrice,
                                            stockQty = stockQty.toDoubleOrNull() ?: 0.0,
                                            stockUnit = stockUnit.ifBlank { "gram" },
                                            imageUrl = imageUrl.trim(),
                                            metadataJson = updatedMetadata
                                        )
                                    )
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = RichEspressoPrimary)
                        ) {
                            Text("Simpan", modifier = Modifier.padding(horizontal = 6.dp))
                        }
                    }
                }
            }
        }
    }

    // Crucial Confirmation Pop-up: "Hapus item? Ya / Tidak"
    if (showDeleteConfirmDialog && onDelete != null) {
        Dialog(onDismissRequest = { showDeleteConfirmDialog = false }) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SoftTerracottaBg,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = TerracottaDanger,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hapus item?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hapus '${initialRecord.name}' (${initialRecord.category}) dari daftar Cabang ${com.example.data.model.formatCleanBranchName(initialRecord.branchId, initialRecord.branchId)}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedCocoaText
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftOatSurface,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { showDeleteConfirmDialog = false }
                                .testTag("delete_item_confirm_no_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Tidak",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                            }
                        }
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                onDelete()
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TerracottaDanger,
                                contentColor = ElevatedIvoryCard
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("delete_item_confirm_yes_btn")
                        ) {
                            Text(
                                 text = "Ya",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    activeCalcPriceTarget?.let { target ->
        val calcTitle = if (target == "WALK_IN") "Hitung Harga Di tempat" else "Hitung Harga $target"
        val initialVal = if (target == "WALK_IN") {
            walkIn.toLongOrNull() ?: 0L
        } else {
            platformPricesMap[target] ?: initialRecord.priceForPlatformName(target)
        }
        WaffleoNominalCalculatorDialog(
            title = calcTitle,
            initialValue = initialVal,
            accentColor = RichEspressoPrimary,
            onDismiss = { activeCalcPriceTarget = null },
            onConfirm = { calculatedAmount ->
                if (target == "WALK_IN") {
                    walkIn = calculatedAmount.toString()
                } else {
                    platformPricesMap = platformPricesMap + (target to calculatedAmount)
                }
                activeCalcPriceTarget = null
            }
        )
    }
}

@Composable
private fun CatalogPriceWithCalcField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    onOpenCalculator: () -> Unit,
    modifier: Modifier = Modifier,
    prefix: String = "Rp",
    suffix: String = ""
) {
    val formattedDisplay = remember(value) {
        val num = value.filter { it.isDigit() }.toLongOrNull()
        if (num != null) {
            java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(num)
        } else {
            ""
        }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = RichEspressoPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SoftOatSurface,
            border = BorderStroke(1.dp, SubtleSandBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenCalculator)
                .testTag("catalog_price_field_${label.lowercase().replace(" ", "_").replace("/", "_")}")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (prefix.isNotBlank()) {
                        Text(
                            text = prefix,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepCaramelGold
                        )
                    }
                    if (formattedDisplay.isNotEmpty()) {
                        Text(
                            text = formattedDisplay,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedCocoaText.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (suffix.isNotBlank()) {
                        Text(
                            text = suffix,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepCaramelGold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = ElevatedIvoryCard,
                    border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.6f)),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Buka Kalkulator $label",
                            tint = DeepCaramelGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddStaffAccountDialog(
    existingBranches: List<String>,
    defaultBranch: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, StaffRole, String) -> Unit
) {
    val branchOptions = remember(existingBranches, defaultBranch) {
        if (existingBranches.isNotEmpty()) existingBranches else listOf(defaultBranch.ifBlank { "Global" })
    }
    var branch by remember(branchOptions, defaultBranch) {
        mutableStateOf(
            if (branchOptions.any { it.equals(defaultBranch, ignoreCase = true) }) defaultBranch
            else branchOptions.first()
        )
    }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    val selectableRoles = remember { listOf(StaffRole.OWNER, StaffRole.SUPERVISOR, StaffRole.KASIR) }
    var role by remember { mutableStateOf(StaffRole.KASIR) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Tambah Akun Cabang", style = MaterialTheme.typography.headlineMedium, color = RichEspressoPrimary)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WaffleoSingleSelectDropdown(
                        label = "Cabang",
                        selectedText = "Cabang ${com.example.data.model.formatCleanBranchName(branch, branch)}",
                        options = branchOptions,
                        optionLabel = { "Cabang ${com.example.data.model.formatCleanBranchName(it, it)}" },
                        onOptionSelected = { branch = it },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "add_staff_branch"
                    )
                    WaffleoSingleSelectDropdown(
                        label = "Role",
                        selectedText = role.label,
                        options = selectableRoles,
                        optionLabel = { it.label },
                        onOptionSelected = { role = it },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "add_staff_role"
                    )
                }

                WaffleoSoftTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = "Nama Staff",
                    placeholder = "Nama staff...",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WaffleoSoftTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username",
                        placeholder = "Username...",
                        modifier = Modifier.weight(1f)
                    )
                    WaffleoSoftTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Password...",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftOatSurface,
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                            Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (branch.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                                onConfirm(branch, username, password, role, displayName)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = RichEspressoPrimary)
                    ) {
                        Text("Simpan", modifier = Modifier.padding(horizontal = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EditStaffAccountDialog(
    account: StaffAccount,
    actorRole: StaffRole,
    onDismiss: () -> Unit,
    onDeleteAccount: (() -> Unit)? = null,
    onConfirm: (displayName: String, newPassword: String, role: StaffRole, email: String, phone: String) -> Unit
) {
    var displayName by remember(account) { mutableStateOf(account.displayName) }
    var newPassword by remember { mutableStateOf("") }
    var email by remember(account) { mutableStateOf(account.email) }
    var phone by remember(account) { mutableStateOf(account.phone) }

    // Daftar role yang diizinkan untuk dipilih berdasarkan hak akses actor
    val assignableRoles = remember(actorRole, account) {
        val allowed = actorRole.assignableRoles()
        if (allowed.contains(account.role)) allowed else allowed + account.role
    }
    var selectedRole by remember(account) { mutableStateOf(account.role) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Edit Akun Cabang",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        val displayBranchName = com.example.data.model.formatCleanBranchName(account.branchId, account.branchId)
                        Text(
                            text = "Username : ${account.username} • Cabang $displayBranchName",
                            style = MaterialTheme.typography.labelMedium,
                            color = MutedCocoaText
                        )
                    }
                    SmallTagPill(
                        text = account.role.label,
                        bg = SoftGoldHighlight,
                        fg = DeepCaramelGold
                    )
                }

                HorizontalDivider(color = SubtleSandBorder.copy(alpha = 0.5f))

                // Informasi Identifier Terkunci (Username & Cabang tidak bisa diganti di sini demi integritas data)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftOatSurface,
                        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("Username (Terkunci)", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                            Text(account.username, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftOatSurface,
                        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("Cabang (Terkunci)", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                            Text("Cabang ${com.example.data.model.formatCleanBranchName(account.branchId, account.branchId)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                        }
                    }
                }

                // Nama Staff
                WaffleoSoftTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = "Nama Lengkap / Panggilan",
                    placeholder = "Nama staff...",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "edit_staff_name"
                )

                // Role Dropdown (Hanya menampilkan role yang boleh diatur oleh level actor)
                if (assignableRoles.isNotEmpty()) {
                    WaffleoSingleSelectDropdown(
                        label = "Hak Akses (Role)",
                        selectedText = selectedRole.label,
                        options = assignableRoles,
                        optionLabel = { it.label },
                        onOptionSelected = { selectedRole = it },
                        modifier = Modifier.fillMaxWidth(),
                        testTagPrefix = "edit_staff_role"
                    )
                }

                // Password Baru
                WaffleoSoftTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Password Baru (Opsional)",
                    placeholder = "Kosongkan jika tidak ingin mengubah password",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "edit_staff_password"
                )

                // Email & Telepon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WaffleoSoftTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        placeholder = "nama@email.com",
                        modifier = Modifier.weight(1f),
                        testTag = "edit_staff_email"
                    )
                    WaffleoSoftTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "No. Telepon / WA",
                        placeholder = "08xxxxxxxxxx",
                        modifier = Modifier.weight(1f),
                        testTag = "edit_staff_phone"
                    )
                }

                // Tombol Aksi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDeleteAccount != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftTerracottaBg,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { onDeleteAccount() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus",
                                    tint = TerracottaDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Hapus Akun Staff",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaDanger
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftOatSurface,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onDismiss)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                                Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                            }
                        }
                        Button(
                            onClick = {
                                if (displayName.isNotBlank()) {
                                    onConfirm(displayName, newPassword, selectedRole, email, phone)
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = RichEspressoPrimary),
                            modifier = Modifier.testTag("save_edit_account_button")
                        ) {
                            Text("Simpan Perubahan", modifier = Modifier.padding(horizontal = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuditLogNarrativeDetailView(
    subLog: AuditLogRecord,
    group: GroupedAuditLog,
    relatedOrder: OrderRecord?
) {
    val beforeObj = remember(subLog.beforeSnapshot) {
        try {
            if (subLog.beforeSnapshot.isNotBlank() && subLog.beforeSnapshot != "{}") JSONObject(subLog.beforeSnapshot) else null
        } catch (e: Exception) {
            null
        }
    }
    val afterObj = remember(subLog.afterSnapshot) {
        try {
            if (subLog.afterSnapshot.isNotBlank() && subLog.afterSnapshot != "{}") JSONObject(subLog.afterSnapshot) else null
        } catch (e: Exception) {
            null
        }
    }

    when (subLog.actionType) {
        "ORDER_CREATED" -> {
            val mediaLabel = afterObj?.optString("media")?.ifBlank { null } ?: relatedOrder?.orderMedia ?: "Di tempat"
            val customerName = afterObj?.optString("customer")?.takeIf { it.isNotBlank() && it != "null" && !it.equals("Walk In Guest", ignoreCase = true) && !it.equals("Pelanggan Di tempat", ignoreCase = true) }
                ?: relatedOrder?.customerData?.takeIf { it.isNotBlank() && !it.equals("Walk In Guest", ignoreCase = true) && !it.equals("Pelanggan Di tempat", ignoreCase = true) }
            val customerNotes = afterObj?.optString("notes")?.takeIf { it.isNotBlank() && it != "null" && it != "-" }
                ?: relatedOrder?.notes?.takeIf { it.isNotBlank() && it != "-" }

            val totalAmt = afterObj?.optLong("totalAmount", 0L)?.takeIf { it > 0L }
                ?: afterObj?.optLong("platformTotal", 0L)?.takeIf { it > 0L }
                ?: relatedOrder?.totalAmount ?: 0L

            val isPaid = afterObj?.optBoolean("isPaid", false) ?: (relatedOrder?.isPaid ?: false)
            val payMethod = afterObj?.optString("paymentMethod")?.takeIf { it.isNotBlank() && it != "null" && it != "Belum Bayar" }
                ?: relatedOrder?.paymentMethod?.takeIf { it.isNotBlank() && it != "Belum Bayar" }

            val platformFee = afterObj?.optLong("platformFee", 0L) ?: 0L
            val netBca = afterObj?.optLong("netEnteredBca", 0L) ?: 0L

            val itemsPayloadStr = afterObj?.optString("itemsPayload")?.ifBlank { null } ?: relatedOrder?.itemsPayload ?: ""
            val parsedItems = remember(itemsPayloadStr) {
                if (itemsPayloadStr.isNotBlank() && itemsPayloadStr != "[]") {
                    try { CartLineItem.deserializeList(itemsPayloadStr) } catch (e: Exception) { emptyList() }
                } else emptyList()
            }
            val itemsSummaryText = afterObj?.optString("itemsSummary")?.ifBlank { null } ?: relatedOrder?.itemsSummary ?: ""

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ElevatedIvoryCard,
                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Baris Info Order
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftGoldHighlight
                            ) {
                                Text(
                                    text = "Media: $mediaLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCaramelGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (customerName != null) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftOatSurface
                                ) {
                                    Text(
                                        text = "Pelanggan: $customerName",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RichEspressoPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Status bayar saat diinput
                        if (isPaid) {
                            Surface(shape = RoundedCornerShape(50), color = SoftSageBg) {
                                Text(
                                    text = "Lunas Saat Input",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MatchaSageSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(shape = RoundedCornerShape(50), color = SoftAmberBg) {
                                Text(
                                    text = "Belum Bayar (Bayar Nanti)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HoneyAmberWarning,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Catatan Khusus Customer (HANYA tampil jika ada)
                    if (customerNotes != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftAmberBg.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = HoneyAmberWarning,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Catatan: $customerNotes",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HoneyAmberWarning
                                )
                            }
                        }
                    }

                    // Rincian Menu yang Dipesan Awal
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftOatSurface, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Rincian Item Dipesan:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MutedCocoaText
                        )

                        if (parsedItems.isNotEmpty()) {
                            parsedItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = RichEspressoPrimary
                                        ) {
                                            Text(
                                                text = "${item.qty}x",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = item.singleLineTitle(),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.category == "Egg Waffle") {
                                                    AdonanPalette.getColorForFlavour(item.flavourName)
                                                } else {
                                                    RichEspressoPrimary
                                                }
                                            )
                                            if (item.addOnNames.isNotEmpty()) {
                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    item.addOnNames.forEach { addon ->
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = ElevatedIvoryCard
                                                        ) {
                                                            Text(
                                                                text = "+$addon",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MutedCocoaText,
                                                                maxLines = 1,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    val linePrice = item.unitPriceWalkIn * item.qty
                                    if (linePrice > 0L) {
                                        Text(
                                            text = WaffleoViewModel.formatIdr(linePrice),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RichEspressoPrimary
                                        )
                                    }
                                }
                            }
                        } else if (itemsSummaryText.isNotBlank()) {
                            Text(
                                text = itemsSummaryText,
                                style = MaterialTheme.typography.bodySmall,
                                color = RichEspressoPrimary
                            )
                        }
                    }

                    // Ringkasan Finansial Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPaid && payMethod != null) "Lunas via $payMethod" else "Total Tagihan:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = WaffleoViewModel.formatIdr(totalAmt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                    }

                    // Khusus Pesanan Platform Online (GrabFood/GoFood/ShopeeFood) jika ada potongan komisi
                    if (platformFee > 0L) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftOatSurface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Komisi Platform Online: -${WaffleoViewModel.formatIdr(platformFee)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TerracottaDanger
                                )
                                Text(
                                    text = "Pencairan Net BCA: ${WaffleoViewModel.formatIdr(netBca)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MatchaSageSuccess
                                )
                            }
                        }
                    }
                }
            }
        }

        "ORDER_PAID" -> {
            val paidAmt = afterObj?.optLong("paidAmount", 0L)?.takeIf { it > 0L }
                ?: relatedOrder?.totalAmount ?: 0L
            val payMethod = afterObj?.optString("paymentMethod")?.ifBlank { afterObj.optString("method") }?.ifBlank { null }
                ?: relatedOrder?.paymentMethod ?: "Tunai"
            val changeDue = afterObj?.optLong("changeDue", 0L)?.takeIf { it > 0L }
                ?: relatedOrder?.changeDueAmount ?: 0L
            val splitCash = afterObj?.optLong("splitCash", 0L) ?: (relatedOrder?.splitCashAmount ?: 0L)
            val splitQris = afterObj?.optLong("splitQris", 0L) ?: (relatedOrder?.splitQrisAmount ?: 0L)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftSageBg.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MatchaSageSuccess.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MatchaSageSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Pembayaran Diterima & Lunas",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MatchaSageSuccess
                            )
                        }

                        Text(
                            text = WaffleoViewModel.formatIdr(paidAmt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MatchaSageSuccess
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Metode Pembayaran:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                        Surface(shape = RoundedCornerShape(50), color = ElevatedIvoryCard) {
                            Text(
                                text = payMethod,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Jika Split Payment
                    if (payMethod.contains("Cash + QRIS", ignoreCase = true) && splitCash > 0L) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElevatedIvoryCard
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tunai: ${WaffleoViewModel.formatIdr(splitCash)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedCocoaText
                                )
                                Text(
                                    text = "QRIS: ${WaffleoViewModel.formatIdr(splitQris)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedCocoaText
                                )
                            }
                        }
                    }

                    // Kembalian Tunai (HANYA tampil jika > 0)
                    if (changeDue > 0L) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftSageBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Uang Kembalian Diserahkan:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MatchaSageSuccess
                                )
                                Text(
                                    text = WaffleoViewModel.formatIdr(changeDue),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MatchaSageSuccess
                                )
                            }
                        }
                    }
                }
            }
        }

        "ORDER_EDITED" -> {
            val beforeTotal = beforeObj?.optLong("total", 0L) ?: 0L
            val afterTotal = afterObj?.optLong("total", 0L) ?: (relatedOrder?.totalAmount ?: 0L)
            val beforeItems = beforeObj?.optString("items")?.ifBlank { null }
            val afterItems = afterObj?.optString("items")?.ifBlank { null } ?: relatedOrder?.itemsSummary
            val shortfall = afterObj?.optLong("shortfall", 0L) ?: (relatedOrder?.shortfallAmount ?: 0L)
            val changeDue = afterObj?.optLong("changeDue", 0L) ?: (relatedOrder?.changeDueAmount ?: 0L)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftAmberBg.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, HoneyAmberWarning.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = HoneyAmberWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Perubahan Pesanan (Revisi Nota)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = HoneyAmberWarning
                            )
                        }

                        val diff = afterTotal - beforeTotal
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (diff > 0) SoftTerracottaBg else if (diff < 0) SoftSageBg else SoftOatSurface
                        ) {
                            Text(
                                text = if (diff > 0) "+${WaffleoViewModel.formatIdr(diff)}" else if (diff < 0) "-${WaffleoViewModel.formatIdr(-diff)}" else "Nilai Sama",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (diff > 0) TerracottaDanger else if (diff < 0) MatchaSageSuccess else MutedCocoaText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Perbandingan Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Semula: ${WaffleoViewModel.formatIdr(beforeTotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                        Text("➔", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                        Text(
                            text = "Total Baru: ${WaffleoViewModel.formatIdr(afterTotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                    }

                    // Perbandingan Daftar Item jika ada
                    if (beforeItems != null && afterItems != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ElevatedIvoryCard, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Sebelum Revisi: $beforeItems",
                                style = MaterialTheme.typography.bodySmall,
                                color = TerracottaDanger
                            )
                            Text(
                                text = "Sesudah Revisi: $afterItems",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MatchaSageSuccess
                            )
                        }
                    }

                    // Rekonsiliasi Kasir (HANYA tampil jika > 0)
                    if (shortfall > 0L) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftTerracottaBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = TerracottaDanger,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Kurang Bayar: ${WaffleoViewModel.formatIdr(shortfall)} (Customer wajib melunasi kekurangan)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaDanger
                                )
                            }
                        }
                    } else if (changeDue > 0L) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftSageBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MatchaSageSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Kembalian Tunai: ${WaffleoViewModel.formatIdr(changeDue)} (Wajib diserahkan ke customer)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MatchaSageSuccess
                                )
                            }
                        }
                    }
                }
            }
        }

        "ORDER_PREPARED" -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftAmberBg.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, HoneyAmberWarning.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = DeepCaramelGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Pesanan Selesai Dimasak (Prepared)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepCaramelGold
                        )
                        Text(
                            text = "Seluruh item pesanan telah selesai dibuat di dapur dan siap disajikan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                }
            }
        }

        "ORDER_DONE", "ORDER_DONE_STOCK_DEDUCTED" -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftSageBg.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MatchaSageSuccess.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MatchaSageSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Pesanan Selesai & Diserahkan (Done!)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MatchaSageSuccess
                        )
                        Text(
                            text = "Pesanan telah diserahkan kepada pelanggan dan transaksi ditutup. Stok bahan baku otomatis dipotong dari inventory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                }
            }
        }

        "ORDER_ERASED" -> {
            val reason = afterObj?.optString("reason")?.ifBlank { null }
                ?: beforeObj?.optString("reason")?.ifBlank { null }
                ?: relatedOrder?.cancelReason?.ifBlank { null }
                ?: "Pesanan dihapus permanen dari sistem"
            val erasedBy = afterObj?.optString("erasedBy")?.ifBlank { null }
                ?: subLog.actorUsername

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftTerracottaBg.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, TerracottaDanger.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = TerracottaDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Pesanan Dihapus (Permanen)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaDanger
                        )
                    }

                    Text(
                        text = "Alasan: $reason",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaDanger
                    )

                    Text(
                        text = "Dihapus oleh: @$erasedBy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedCocoaText
                    )
                }
            }
        }

        "ORDER_CANCELLED" -> {
            val reason = afterObj?.optString("reason")?.ifBlank { null }
                ?: relatedOrder?.cancelReason?.ifBlank { null }
                ?: "Pesanan dibatalkan oleh pengguna"
            val cancelledBy = afterObj?.optString("cancelledBy")?.ifBlank { null }
                ?: relatedOrder?.cancelledBy
                ?: subLog.actorUsername

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftTerracottaBg.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, TerracottaDanger.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = TerracottaDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Pesanan Dibatalkan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaDanger
                        )
                    }

                    Text(
                        text = "Alasan: $reason",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaDanger
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dibatalkan oleh: @$cancelledBy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = "Stok bahan aman (tidak dikurangi)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MatchaSageSuccess
                        )
                    }
                }
            }
        }

        "MONEY_TX_ADDED", "MONEY_TX_UPDATED", "MONEY_TX_DELETED" -> {
            val isDelete = subLog.actionType.contains("DELETED")
            val isUpdate = subLog.actionType.contains("UPDATED")
            val targetObj = if (isDelete) beforeObj ?: afterObj else afterObj ?: beforeObj

            val amountVal = targetObj?.optLong("amount", 0L) ?: 0L
            val catName = targetObj?.optString("cat")?.ifBlank { null }
                ?: targetObj?.optString("categoryName")?.ifBlank { null }
                ?: "Money Manager"
            val subCatName = targetObj?.optString("sub")?.ifBlank { null }
                ?: targetObj?.optString("subCategoryName")?.ifBlank { null }
                ?: ""
            val noteVal = targetObj?.optString("note")?.ifBlank { null }
                ?: targetObj?.optString("noteVal")?.ifBlank { null }
                ?: ""

            val isIncome = subLog.summary.contains("Income", ignoreCase = true) || subLog.summary.contains("Pemasukan", ignoreCase = true)
            val isExpense = subLog.summary.contains("Expense", ignoreCase = true) || subLog.summary.contains("Pengeluaran", ignoreCase = true) || subLog.summary.contains("Biaya", ignoreCase = true)
            val isTransfer = subLog.summary.contains("Transfer", ignoreCase = true)

            val badgeTypeLabel = when {
                isIncome -> "PEMASUKAN"
                isExpense -> "PENGELUARAN"
                isTransfer -> "TRANSFER"
                else -> "MONEY MANAGER"
            }
            val badgeBg = when {
                isDelete -> SoftTerracottaBg
                isExpense -> SoftTerracottaBg
                isIncome -> SoftSageBg
                else -> SoftGoldHighlight
            }
            val badgeFg = when {
                isDelete -> TerracottaDanger
                isExpense -> TerracottaDanger
                isIncome -> MatchaSageSuccess
                else -> DeepCaramelGold
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ElevatedIvoryCard,
                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Baris: Type Badge + Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = badgeBg,
                            border = BorderStroke(1.dp, badgeFg.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDelete) Icons.Default.DeleteOutline else if (isExpense) Icons.Default.Payments else Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = badgeFg,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isDelete) "DIHAPUS • $badgeTypeLabel" else badgeTypeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeFg
                                )
                            }
                        }

                        if (amountVal > 0L) {
                            Text(
                                text = WaffleoViewModel.formatIdr(amountVal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDelete) TerracottaDanger else RichEspressoPrimary
                            )
                        }
                    }

                    // Kategori & Sub-Kategori Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftOatSurface,
                            border = BorderStroke(1.dp, SubtleSandBorder)
                        ) {
                            Text(
                                text = "Kategori: $catName",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (subCatName.isNotBlank() && subCatName != "null") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SoftOatSurface,
                                border = BorderStroke(1.dp, SubtleSandBorder)
                            ) {
                                Text(
                                    text = "Sub: $subCatName",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MutedCocoaText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Catatan / Note jika ada
                    if (noteVal.isNotBlank() && noteVal != "null") {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftAmberBg.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = DeepCaramelGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Catatan: $noteVal",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = RichEspressoPrimary
                                )
                            }
                        }
                    }

                    // Perbandingan Sebelum vs Sesudah jika Edit (Update)
                    if (isUpdate && beforeObj != null && afterObj != null) {
                        val prevAmt = beforeObj.optLong("amount", 0L)
                        val currAmt = afterObj.optLong("amount", 0L)
                        if (prevAmt != currAmt && prevAmt > 0L) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Perubahan Nominal:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedCocoaText
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = SoftTerracottaBg) {
                                        Text(
                                            text = WaffleoViewModel.formatIdr(prevAmt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TerracottaDanger,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text("➔", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                                    Surface(shape = RoundedCornerShape(4.dp), color = SoftSageBg) {
                                        Text(
                                            text = WaffleoViewModel.formatIdr(currAmt),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MatchaSageSuccess,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        else -> {
            // Non-Order Category (Catalog, Stock, Staff, Store, etc.)
            // Tampilan narasi eksekutif bersih tanpa raw JSON table
            if (beforeObj != null && afterObj != null) {
                val allKeys = (beforeObj.keys().asSequence().toList() + afterObj.keys().asSequence().toList()).distinct()
                val changedEntries = allKeys.mapNotNull { k ->
                    val bVal = beforeObj.opt(k)?.toString() ?: ""
                    val aVal = afterObj.opt(k)?.toString() ?: ""
                    if (bVal != aVal && bVal.isNotBlank() && aVal.isNotBlank()) {
                        val friendlyKey = when (k) {
                            "walkIn", "priceWalkIn" -> "Harga Walk-In"
                            "grab", "priceGrabFood" -> "Harga GrabFood"
                            "gofood", "priceGoFood" -> "Harga GoFood"
                            "shopee", "priceShopeeFood" -> "Harga ShopeeFood"
                            "stock", "stockQty" -> "Kuantitas Stok"
                            "active", "isActive" -> "Status Aktif"
                            "role" -> "Hak Akses"
                            else -> k.replaceFirstChar { it.uppercase() }
                        }
                        Triple(friendlyKey, bVal, aVal)
                    } else null
                }

                if (changedEntries.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftOatSurface,
                        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            changedEntries.forEach { (label, bVal, aVal) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RichEspressoPrimary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = SoftTerracottaBg) {
                                            Text(
                                                text = bVal,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TerracottaDanger,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text("➔", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                                        Surface(shape = RoundedCornerShape(4.dp), color = SoftSageBg) {
                                            Text(
                                                text = aVal,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MatchaSageSuccess,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogDetailTimelineDialog(
    group: GroupedAuditLog,
    relatedOrder: OrderRecord?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                // Header Dialog Executive
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when {
                                    group.category == "ORDER" || group.entityId.startsWith("WFL-") -> "Kronologi & Rekam Jejak Pesanan"
                                    group.category == "MONEY" -> "Kronologi & Rekam Jejak Money Manager"
                                    else -> "Kronologi & Rekam Jejak Audit"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary
                            )
                            SmallTagPill(
                                text = "${group.totalSubActions} Langkah",
                                bg = SoftGoldHighlight,
                                fg = DeepCaramelGold
                            )
                        }

                        Text(
                            text = com.example.data.model.sanitizeBranchIdsInText(group.title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = RichEspressoPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Cabang ${com.example.data.model.formatCleanBranchName(group.branchId, group.branchId)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MutedCocoaText
                            )
                            SmallTagPill(
                                text = "Status: Terverifikasi (Immutable)",
                                bg = SoftSageBg,
                                fg = MatchaSageSuccess
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = RichEspressoPrimary
                        )
                    }
                }

                HorizontalDivider(
                    color = SubtleSandBorder.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Ringkasan Cepat Pesanan (Executive Overview) jika ada Order terkait
                if (relatedOrder != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftOatSurface),
                        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RichEspressoPrimary
                                    ) {
                                        Text(
                                            text = "#${relatedOrder.sequenceIndex} • ${relatedOrder.displayOrderNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    val isGrab = relatedOrder.orderMedia.contains("Grab", ignoreCase = true)
                                    val isGo = relatedOrder.orderMedia.contains("Go", ignoreCase = true)
                                    val isShopee = relatedOrder.orderMedia.contains("Shopee", ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = when {
                                            isGrab -> Color(0xFF00B14F).copy(alpha = 0.12f)
                                            isGo -> Color(0xFFE2211C).copy(alpha = 0.12f)
                                            isShopee -> Color(0xFFEE4D2D).copy(alpha = 0.12f)
                                            else -> SoftGoldHighlight
                                        }
                                    ) {
                                        Text(
                                            text = relatedOrder.orderMedia,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isGrab -> Color(0xFF007A37)
                                                isGo -> Color(0xFFC01511)
                                                isShopee -> Color(0xFFB72E15)
                                                else -> DeepCaramelGold
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (relatedOrder.customerData.isNotBlank() && !relatedOrder.customerData.equals("Walk In Guest", ignoreCase = true) && !relatedOrder.customerData.equals("Pelanggan Di tempat", ignoreCase = true)) {
                                        Text(
                                            text = "• Pelanggan: ${relatedOrder.customerData}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = RichEspressoPrimary
                                        )
                                    }
                                }

                                // Status Transaksi
                                val (stBg, stFg, stLabel) = when {
                                    relatedOrder.status == "CANCELLED" -> Triple(SoftTerracottaBg, TerracottaDanger, "Pesanan Dihapus")
                                    relatedOrder.status == "DONE" -> Triple(SoftSageBg, MatchaSageSuccess, "Selesai (Done!)")
                                    relatedOrder.isPrepared -> Triple(SoftAmberBg, HoneyAmberWarning, "Dimasak (Prepared)")
                                    else -> Triple(SoftGoldHighlight, DeepCaramelGold, "Antrean Aktif")
                                }
                                Surface(shape = RoundedCornerShape(50), color = stBg) {
                                    Text(
                                        text = stLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = stFg,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Total & Catatan jika ada
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (relatedOrder.notes.isNotBlank() && relatedOrder.notes != "-") {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SoftAmberBg.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "Catatan: ${relatedOrder.notes}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = HoneyAmberWarning,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Total Transaksi:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedCocoaText
                                    )
                                    Text(
                                        text = WaffleoViewModel.formatIdr(relatedOrder.totalAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = RichEspressoPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // List Timeline Stepper Vertikal
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(group.subLogsChronological) { index, subLog ->
                        val isLast = index == group.subLogsChronological.size - 1
                        val subTimeStr = remember(subLog.createdAt) {
                            val dt = subLog.createdAt?.toDate()
                            if (dt != null) SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt)
                            else "Waktu tidak tercatat"
                        }

                        val subBadge = getAuditBadgeInfo(subLog.actionType, group.category)

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Kolom Indikator Stepper Vertikal
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(36.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = subBadge.fg,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (!isLast) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(84.dp)
                                            .background(SubtleSandBorder)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Kartu Rincian Sub-Aksi
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftOatSurface),
                                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = if (isLast) 0.dp else 14.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = subBadge.bg,
                                            border = BorderStroke(1.dp, subBadge.fg.copy(alpha = 0.25f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = subBadge.icon,
                                                    contentDescription = null,
                                                    tint = subBadge.fg,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = subBadge.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = subBadge.fg
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = ElevatedIvoryCard
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = MutedCocoaText,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Text(
                                                        text = "@${subLog.actorUsername} (${subLog.actorRole})",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = RichEspressoPrimary
                                                    )
                                                }
                                            }
                                            Text(
                                                text = subTimeStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MutedCocoaText
                                            )
                                        }
                                    }

                                    Text(
                                        text = com.example.data.model.sanitizeBranchIdsInText(subLog.summary),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = RichEspressoPrimary
                                    )

                                    // Tampilan Narasi Human-Readable Eksklusif
                                    AuditLogNarrativeDetailView(
                                        subLog = subLog,
                                        group = group,
                                        relatedOrder = relatedOrder
                                    )
                                }
                            }
                        }
                    }
                }

                // Tombol Tutup
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = RichEspressoPrimary)
                    ) {
                        Text("Tutup Kronologi", modifier = Modifier.padding(horizontal = 14.dp))
                    }
                }
            }
        }
    }
}



