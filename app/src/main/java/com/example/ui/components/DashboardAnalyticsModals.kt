package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AuditLogRecord
import com.example.data.model.CartLineItem
import com.example.data.model.CatalogRecord
import com.example.data.model.OrderMedia
import com.example.data.model.OrderRecord
import com.example.data.model.OrderTimelineEvent
import com.example.data.model.WaffleoOrderEngine
import com.example.data.model.formatCleanBranchName
import com.example.data.model.sanitizeBranchIdsInText
import com.example.ui.theme.ChampagneGoldAccent
import com.example.ui.theme.DeepCaramelGold
import com.example.ui.theme.ElevatedIvoryCard
import com.example.ui.theme.HoneyAmberWarning
import com.example.ui.theme.MatchaSageSuccess
import com.example.ui.theme.MutedCocoaText
import com.example.ui.theme.RichEspressoPrimary
import com.example.ui.theme.RoastedMochaSecondary
import com.example.ui.theme.SoftAmberBg
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SoftSageBg
import com.example.ui.theme.SoftTerracottaBg
import com.example.ui.theme.SubtleSandBorder
import com.example.ui.theme.TerracottaDanger
import com.example.ui.viewmodel.WaffleoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// =========================================================================================
// BAGIAN A: MODAL DAFTAR TRANSAKSI KANAL & METODE PEMBAYARAN (CLICKABLE CHANNEL POPUP)
// =========================================================================================

/**
 * Interactive Popup Modal displaying the list of actual transactions for a selected payment channel
 * (Cash, QRIS, GrabFood, GoFood, ShopeeFood) filtered by the active Dashboard period and branch.
 */
@Composable
fun DashboardChannelTransactionsDialog(
    channelName: String,
    periodLabel: String,
    branchName: String,
    orders: List<OrderRecord>,
    catalog: List<CatalogRecord>,
    viewModel: WaffleoViewModel,
    totalDashboardNetRevenue: Long,
    onDismiss: () -> Unit
) {
    var selectedOrderForAudit by remember { mutableStateOf<OrderRecord?>(null) }

    // Sort transactions from newest to oldest
    val sortedOrders = remember(orders) {
        orders.sortedByDescending {
            val dt = (it.updatedAt ?: it.createdAt)?.toDate()
            dt?.time ?: it.id.removePrefix("v1_ord_bd_").removePrefix("v1_ord_").takeWhile { c -> c.isDigit() }.toLongOrNull() ?: 0L
        }
    }

    val totalChannelGross = remember(orders, channelName) {
        orders.sumOf { ord ->
            val isSplit = ord.paymentMethod.contains("Cash + QRIS", ignoreCase = true) ||
                (ord.splitCashAmount > 0L && ord.splitQrisAmount > 0L)
            when {
                isSplit && channelName.equals("Cash", ignoreCase = true) ->
                    if (ord.splitCashAmount > 0L) ord.splitCashAmount else (ord.totalAmount - ord.splitQrisAmount).coerceAtLeast(0L)
                isSplit && channelName.equals("QRIS", ignoreCase = true) ->
                    if (ord.splitQrisAmount > 0L) ord.splitQrisAmount else (ord.totalAmount / 2)
                else -> ord.totalAmount
            }
        }
    }
    val totalChannelNet = remember(orders, catalog, channelName) {
        orders.sumOf { ord ->
            val net = viewModel.calculateOrderNetAmount(ord, catalog)
            val isSplit = ord.paymentMethod.contains("Cash + QRIS", ignoreCase = true) ||
                (ord.splitCashAmount > 0L && ord.splitQrisAmount > 0L)
            when {
                isSplit && channelName.equals("Cash", ignoreCase = true) ->
                    if (ord.splitCashAmount > 0L) ord.splitCashAmount.coerceAtMost(net) else (net - ord.splitQrisAmount).coerceAtLeast(0L)
                isSplit && channelName.equals("QRIS", ignoreCase = true) ->
                    if (ord.splitQrisAmount > 0L) ord.splitQrisAmount.coerceAtMost(net) else (net / 2)
                else -> net
            }
        }
    }
    val totalChannelFee = (totalChannelGross - totalChannelNet).coerceAtLeast(0L)

    val contributionPct = if (totalDashboardNetRevenue > 0L) {
        ((totalChannelNet.toDouble() / totalDashboardNetRevenue.toDouble()) * 100.0).roundToInt()
    } else {
        0
    }

    val isPlatform = channelName in listOf("GrabFood", "GoFood", "ShopeeFood")
    val channelColor = when (channelName) {
        "Cash" -> Color(0xFF15803D)
        "QRIS" -> Color(0xFF1D4ED8)
        "GrabFood" -> Color(0xFF00B14F)
        "GoFood" -> Color(0xFFEE2737)
        "ShopeeFood" -> Color(0xFFEE4D2D)
        else -> DeepCaramelGold
    }
    val channelIcon = when (channelName) {
        "Cash" -> Icons.Default.Payments
        "QRIS" -> Icons.Default.QrCode2
        "GrabFood", "GoFood" -> Icons.Default.DeliveryDining
        "ShopeeFood" -> Icons.Default.LocalMall
        else -> Icons.Default.ReceiptLong
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .widthIn(max = 660.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            border = BorderStroke(1.dp, SubtleSandBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = channelColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, channelColor.copy(alpha = 0.3f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = channelIcon,
                                    contentDescription = channelName,
                                    tint = channelColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = channelName.uppercase(Locale.ROOT),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RichEspressoPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftOatSurface,
                                    border = BorderStroke(0.8.dp, SubtleSandBorder)
                                ) {
                                    Text(
                                        text = "Daftar Transaksi",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MutedCocoaText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Periode: $periodLabel • Cabang ${com.example.data.model.formatCleanBranchName(branchName, branchName)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_channel_dialog_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SoftOatSurface,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = RichEspressoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // KPI Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Orders
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftOatSurface.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, SubtleSandBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Total Transaksi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCocoaText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${sortedOrders.size} Pesanan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = RichEspressoPrimary
                            )
                        }
                    }

                    // Total Net Revenue
                    Surface(
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(16.dp),
                        color = if (sortedOrders.isNotEmpty()) channelColor.copy(alpha = 0.08f) else SoftOatSurface.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, if (sortedOrders.isNotEmpty()) channelColor.copy(alpha = 0.3f) else SubtleSandBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isPlatform) "Net Masuk BCA" else "Pendapatan Bersih",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (sortedOrders.isNotEmpty()) channelColor else MutedCocoaText,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = WaffleoViewModel.formatIdr(totalChannelNet),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (sortedOrders.isNotEmpty()) channelColor else RichEspressoPrimary
                            )
                        }
                    }

                    // Contribution
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftGoldHighlight.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Kontribusi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCocoaText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$contributionPct% Porsi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepCaramelGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable List of Actual Transactions
                if (sortedOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SoftOatSurface,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = channelIcon,
                                        contentDescription = null,
                                        tint = MutedCocoaText.copy(alpha = 0.6f),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Belum ada transaksi selesai menggunakan $channelName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MutedCocoaText
                            )
                            Text(
                                text = "Transaksi selesai pada periode $periodLabel akan muncul di sini.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCocoaText.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(sortedOrders, key = { it.id }) { order ->
                            CompactChannelTransactionRow(
                                order = order,
                                channelName = channelName,
                                catalog = catalog,
                                viewModel = viewModel,
                                isPlatform = isPlatform,
                                channelColor = channelColor,
                                onClick = { selectedOrderForAudit = order }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedOrderForAudit?.let { targetOrder ->
        ChannelOrderAuditTrailDialog(
            order = targetOrder,
            catalog = catalog,
            viewModel = viewModel,
            onDismiss = { selectedOrderForAudit = null }
        )
    }
}

/**
 * Compact clickable transaction row inside Distribusi Kanal & Metode Pembayaran dialog.
 * Clicking any row opens the full Audit Trail popup for that order.
 */
@Composable
private fun CompactChannelTransactionRow(
    order: OrderRecord,
    channelName: String,
    catalog: List<CatalogRecord>,
    viewModel: WaffleoViewModel,
    isPlatform: Boolean,
    channelColor: Color,
    onClick: () -> Unit
) {
    val timeDateLabel = remember(order) {
        val dt = (order.updatedAt ?: order.createdAt)?.toDate()
        val resolvedDate = if (dt != null) {
            dt
        } else {
            val epochMs = order.id
                .removePrefix("v1_ord_bd_")
                .removePrefix("v1_ord_")
                .takeWhile { it.isDigit() }
                .toLongOrNull()
            if (epochMs != null && epochMs > 1_000_000_000L) Date(epochMs) else Date()
        }
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(resolvedDate)
    }

    val fullOrderNet = remember(order, catalog) { viewModel.calculateOrderNetAmount(order, catalog) }
    val platformFee = (order.totalAmount - fullOrderNet).coerceAtLeast(0L)
    val isSplit = order.paymentMethod.contains("Cash + QRIS", ignoreCase = true) ||
        (order.splitCashAmount > 0L && order.splitQrisAmount > 0L)

    val displayedChannelNet = remember(order, channelName, fullOrderNet, isSplit) {
        when {
            isSplit && channelName.equals("Cash", ignoreCase = true) ->
                if (order.splitCashAmount > 0L) order.splitCashAmount.coerceAtMost(fullOrderNet)
                else (fullOrderNet - order.splitQrisAmount).coerceAtLeast(0L)
            isSplit && channelName.equals("QRIS", ignoreCase = true) ->
                if (order.splitQrisAmount > 0L) order.splitQrisAmount.coerceAtMost(fullOrderNet)
                else (fullOrderNet / 2)
            else -> fullOrderNet
        }
    }

    val customerLabel = remember(order) {
        val cust = order.customerData.trim()
        when {
            cust.isNotBlank() && !cust.equals("Walk In Guest", ignoreCase = true) -> cust
            order.orderMedia.equals("Di tempat", ignoreCase = true) -> "Pelanggan Di tempat"
            else -> ""
        }
    }

    val primaryHeaderLabel = remember(order.orderMedia, customerLabel) {
        if (customerLabel.isNotBlank() && !customerLabel.equals(order.orderMedia, ignoreCase = true)) {
            "${order.orderMedia}  •  $customerLabel"
        } else {
            order.orderMedia
        }
    }

    val subtitleLabel = remember(timeDateLabel, order, isPlatform, platformFee, isSplit) {
        buildString {
            append(timeDateLabel)
            append(" • Metode: ")
            append(order.paymentMethod.ifBlank { "Cash" })
            if (isSplit) {
                append(" (Tunai ")
                append(WaffleoViewModel.formatIdr(order.splitCashAmount))
                append(" + QRIS ")
                append(WaffleoViewModel.formatIdr(order.splitQrisAmount))
                append(")")
            }
            if (isPlatform && platformFee > 0L) {
                append(" • Potongan: -")
                append(WaffleoViewModel.formatIdr(platformFee))
            }
            if (order.discountAmount > 0L) {
                append(" • Promo: -")
                append(WaffleoViewModel.formatIdr(order.discountAmount))
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SoftOatSurface.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.75f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("channel_tx_row_${order.displayOrderNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                            text = order.displayOrderNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftGoldHighlight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = primaryHeaderLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = subtitleLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedCocoaText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = WaffleoViewModel.formatIdr(displayedChannelNet),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = channelColor
                    )
                    Text(
                        text = if (isPlatform) "Net Masuk BCA" else "Dana Masuk",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MatchaSageSuccess
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Lihat Audit Trail",
                    tint = MutedCocoaText.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Audit Trail Modal opened when clicking any transaction row inside Distribusi Kanal & Metode Pembayaran.
 * Displays complete order summary, sorted item breakdown, financial settlement, and chronological audit timeline.
 */
@Composable
fun ChannelOrderAuditTrailDialog(
    order: OrderRecord,
    catalog: List<CatalogRecord>,
    viewModel: WaffleoViewModel,
    onDismiss: () -> Unit
) {
    val allAuditLogs by viewModel.auditLogs.collectAsState()
    val menuCategories by viewModel.customMenuCategories.collectAsState()
    val modifierCategories by viewModel.customModifierCategories.collectAsState()

    val sortedItems = remember(order.itemsPayload, catalog, menuCategories, modifierCategories) {
        WaffleoOrderEngine.sortCartLineItems(
            items = CartLineItem.deserializeList(order.itemsPayload),
            catalog = catalog,
            menuCategories = menuCategories,
            modifierCategories = modifierCategories
        )
    }

    val media = remember(order.orderMedia) { OrderMedia.fromLabel(order.orderMedia) }
    val isPlatform = media.isPlatform
    val netAmount = remember(order, catalog) { viewModel.calculateOrderNetAmount(order, catalog) }
    val platformFee = (order.totalAmount - netAmount).coerceAtLeast(0L)

    val fallbackDateText = remember(order) {
        val dt = (order.createdAt ?: order.updatedAt)?.toDate()
            ?: Date(
                order.id.removePrefix("v1_ord_bd_").removePrefix("v1_ord_")
                    .takeWhile { it.isDigit() }.toLongOrNull() ?: System.currentTimeMillis()
            )
        SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt)
    }

    val effectiveTimelineEvents = remember(order, fallbackDateText) {
        val rawEvents = order.getTimelineEvents()
        if (rawEvents.isNotEmpty()) {
            rawEvents
        } else {
            listOfNotNull(
                OrderTimelineEvent(
                    eventId = "evt_seed_1",
                    timeText = fallbackDateText,
                    eventType = "ORDER_CREATED",
                    actorUsername = order.createdBy,
                    title = "Pesanan Dibuat",
                    description = "Pesanan ${order.displayOrderNumber} masuk via ${order.orderMedia} (${order.customerData}). Total: ${WaffleoViewModel.formatIdr(order.totalAmount)}"
                ),
                if (order.paidAmount > 0L) {
                    OrderTimelineEvent(
                        eventId = "evt_seed_2",
                        timeText = fallbackDateText,
                        eventType = "PAYMENT_EVENT",
                        actorUsername = order.createdBy,
                        title = "Pembayaran Diterima",
                        description = "Diterima ${WaffleoViewModel.formatIdr(order.paidAmount)} via ${order.paymentMethod}."
                    )
                } else null,
                if (order.status == "DONE") {
                    OrderTimelineEvent(
                        eventId = "evt_seed_3",
                        timeText = fallbackDateText,
                        eventType = "ORDER_COMPLETED",
                        actorUsername = order.createdBy,
                        title = "Pesanan Selesai (Done)",
                        description = "Pesanan diselesaikan dan tercatat pada laporan."
                    )
                } else if (order.status == "CANCELLED") {
                    OrderTimelineEvent(
                        eventId = "evt_seed_3",
                        timeText = order.cancelledAtText.ifBlank { fallbackDateText },
                        eventType = "ORDER_CANCELLED",
                        actorUsername = order.cancelledBy,
                        title = "Pesanan Dibatalkan",
                        description = "Alasan: ${order.cancelReason.ifBlank { "-" }}"
                    )
                } else null
            )
        }
    }

    val relatedAuditLogs = remember(allAuditLogs, order) {
        allAuditLogs
            .filter { log ->
                log.entityId.isNotBlank() && (
                    log.entityId == order.id ||
                        log.entityId == order.orderNumber ||
                        log.entityId == order.displayOrderNumber
                    )
            }
            .sortedBy { it.createdAt?.toDate()?.time ?: 0L }
    }

    val (statusBg, statusFg, statusText) = when (order.status) {
        "DONE" -> Triple(SoftSageBg, MatchaSageSuccess, "Selesai (Done)")
        "CANCELLED" -> Triple(SoftTerracottaBg, TerracottaDanger, "Dibatalkan")
        "ERASED" -> Triple(SoftTerracottaBg, TerracottaDanger, "Dihapus")
        else -> Triple(SoftGoldHighlight, DeepCaramelGold, "Aktif")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.86f)
                .widthIn(max = 700.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = BorderStroke(1.dp, SubtleSandBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SoftGoldHighlight,
                            border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Audit Trail",
                                    tint = DeepCaramelGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Audit Trail Pesanan",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RichEspressoPrimary
                                )
                                Surface(shape = RoundedCornerShape(6.dp), color = RichEspressoPrimary) {
                                    Text(
                                        text = order.displayOrderNumber,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftGoldHighlight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(shape = RoundedCornerShape(50), color = statusBg) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusFg,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${order.orderMedia} • Kasir: @${order.createdBy} • Cabang ${formatCleanBranchName(order.branchId, order.branchId)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_audit_trail_dialog_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SoftOatSurface,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = RichEspressoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = SubtleSandBorder.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Ringkasan Pesanan & Finansial
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SoftOatSurface.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.8f)),
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
                                Text(
                                    text = "Ringkasan Item & Finansial",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                                if (order.customerData.isNotBlank()) {
                                    Text(
                                        text = "Pelanggan: ${order.customerData}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MutedCocoaText
                                    )
                                }
                            }

                            if (sortedItems.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    sortedItems.forEach { line ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${line.qty}x ${line.formattedTitle()}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = RichEspressoPrimary
                                                )
                                                val customText = line.formattedCustomization()
                                                if (customText.isNotBlank() && customText != "Tanpa Add-on" && customText != "Standar") {
                                                    Text(
                                                        text = "+ $customText",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MutedCocoaText
                                                    )
                                                }
                                            }
                                            Text(
                                                text = WaffleoViewModel.formatIdr(line.lineTotalFor(media)),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary
                                            )
                                        }
                                    }
                                }
                            } else if (order.itemsSummary.isNotBlank()) {
                                Text(
                                    text = order.itemsSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RichEspressoPrimary
                                )
                            }

                            if (order.notes.isNotBlank() && order.notes != "-") {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoftAmberBg.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "Catatan: ${order.notes}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HoneyAmberWarning,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = SubtleSandBorder.copy(alpha = 0.6f))

                            if (order.discountAmount > 0L) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Diskon Promo ${if (order.promoCode.isNotBlank()) "(${order.promoCode})" else ""}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TerracottaDanger
                                    )
                                    Text(
                                        text = "-${WaffleoViewModel.formatIdr(order.discountAmount)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaDanger
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Pesanan (${order.paymentMethod.ifBlank { "Cash" }})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RichEspressoPrimary
                                )
                                Text(
                                    text = WaffleoViewModel.formatIdr(order.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                            }

                            if (order.paymentMethod.contains("Cash + QRIS", ignoreCase = true) ||
                                (order.splitCashAmount > 0L && order.splitQrisAmount > 0L)
                            ) {
                                Text(
                                    text = "Rincian Split: Tunai ${WaffleoViewModel.formatIdr(order.splitCashAmount)} • QRIS ${WaffleoViewModel.formatIdr(order.splitQrisAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedCocoaText
                                )
                            }

                            if (isPlatform && platformFee > 0L) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Potongan Komisi Platform (${order.orderMedia})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TerracottaDanger
                                    )
                                    Text(
                                        text = "-${WaffleoViewModel.formatIdr(platformFee)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaDanger
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dana Masuk Bersih",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MatchaSageSuccess
                                )
                                Text(
                                    text = WaffleoViewModel.formatIdr(netAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MatchaSageSuccess
                                )
                            }
                        }
                    }

                    // 2. Jejak Kronologi Pesanan (Order Timeline Events)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SoftOatSurface.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
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
                                Text(
                                    text = "Kronologi Aktivitas Pesanan",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                                Surface(shape = RoundedCornerShape(50), color = SoftGoldHighlight) {
                                    Text(
                                        text = "${effectiveTimelineEvents.size} Langkah",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCaramelGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            effectiveTimelineEvents.forEachIndexed { idx, ev ->
                                ChannelAuditTimelineStepCard(
                                    event = ev,
                                    isLast = idx == effectiveTimelineEvents.lastIndex
                                )
                            }
                        }
                    }

                    // 3. Log Audit Sistem Tambahan (jika ada record di koleksi AuditLog)
                    if (relatedAuditLogs.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SoftOatSurface.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Rekam Log Audit Sistem (${relatedAuditLogs.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )

                                relatedAuditLogs.forEach { log ->
                                    val logTime = remember(log.createdAt) {
                                        val dt = log.createdAt?.toDate()
                                        if (dt != null) SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt)
                                        else fallbackDateText
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ElevatedIvoryCard,
                                        border = BorderStroke(0.8.dp, SubtleSandBorder.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(shape = RoundedCornerShape(50), color = SoftOatSurface) {
                                                    Text(
                                                        text = log.actionType.ifBlank { "AUDIT" },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = RichEspressoPrimary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = logTime,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MutedCocoaText
                                                )
                                            }
                                            Text(
                                                text = "Oleh: @${log.actorUsername} (${log.actorRole})",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MutedCocoaText
                                            )
                                            if (log.summary.isNotBlank()) {
                                                Text(
                                                    text = sanitizeBranchIdsInText(log.summary),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = RichEspressoPrimary
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

@Composable
private fun ChannelAuditTimelineStepCard(
    event: OrderTimelineEvent,
    isLast: Boolean
) {
    val (icon, color) = when (event.eventType) {
        "ORDER_CREATED" -> Icons.Default.Receipt to DeepCaramelGold
        "PAYMENT_EVENT" -> Icons.Default.Payment to MatchaSageSuccess
        "ORDER_EDITED" -> Icons.Default.Edit to DeepCaramelGold
        "ORDER_PREPARED" -> Icons.Default.Restaurant to HoneyAmberWarning
        "ORDER_COMPLETED", "ORDER_DONE" -> Icons.Default.CheckCircle to MatchaSageSuccess
        "ORDER_CANCELLED" -> Icons.Default.Cancel to TerracottaDanger
        else -> Icons.Default.Info to MutedCocoaText
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, color),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(MutedCocoaText.copy(alpha = 0.25f))
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = ElevatedIvoryCard,
            border = BorderStroke(0.8.dp, SubtleSandBorder.copy(alpha = 0.6f)),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 4.dp)
        ) {
            Column(modifier = Modifier.padding(11.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sanitizeBranchIdsInText(event.title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedCocoaText
                    )
                }
                if (event.actorUsername.isNotBlank()) {
                    Text(
                        text = "Pelaksana: @${event.actorUsername}${if (event.actorRole.isNotBlank()) " (${event.actorRole})" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedCocoaText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (event.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sanitizeBranchIdsInText(event.description),
                        style = MaterialTheme.typography.bodySmall,
                        color = RichEspressoPrimary
                    )
                }
            }
        }
    }
}

// =========================================================================================
// BAGIAN B: MODAL ANALISIS VARIASI PESANAN PRODUK UTAMA (PRODUCT VARIATION ANALYSIS POPUP)
// =========================================================================================

/**
 * Data representation of an aggregated order variation configuration.
 */
data class ProductVariationConfig(
    val title: String,
    val customization: String,
    val unitsSold: Int,
    val grossRevenue: Long,
    val orderCount: Int
)

/**
 * Analyzes actual done order items and groups them into unique order configurations
 * (e.g., Original Blueberry, Pandan Blueberry + Chocochip, etc.)
 */
fun analyzeProductOrderConfigurations(
    productName: String,
    categoryName: String,
    doneOrders: List<OrderRecord>,
    catalog: List<CatalogRecord>
): Pair<Int, List<ProductVariationConfig>> {
    val cleanParentName = productName.trim()
    val configMap = LinkedHashMap<String, MutableVariationTracker>()
    var totalProductUnits = 0

    doneOrders.forEach { order ->
        val media = OrderMedia.fromLabel(order.orderMedia)
        val lines = CartLineItem.deserializeList(order.itemsPayload)

        if (lines.isNotEmpty()) {
            lines.forEach { line ->
                val isPrimary = line.primaryItemName.equals(cleanParentName, ignoreCase = true) ||
                    (line.primaryItemId.isNotBlank() && catalog.any { it.id == line.primaryItemId && it.name.equals(cleanParentName, ignoreCase = true) })
                val isSecondary = (line.isHalfHalf || line.isDoubleTopping) && (
                    line.secondaryItemName.equals(cleanParentName, ignoreCase = true) ||
                    (line.secondaryItemId.isNotBlank() && catalog.any { it.id == line.secondaryItemId && it.name.equals(cleanParentName, ignoreCase = true) })
                )

                if (isPrimary || isSecondary) {
                    totalProductUnits += line.qty

                    // Format title according to category rules
                    val title = when {
                        line.category.contains("Egg Waffle", ignoreCase = true) || categoryName.contains("Egg Waffle", ignoreCase = true) -> {
                            val flavour = line.flavourName.ifBlank { "Original" }.trim()
                            when {
                                line.isHalfHalf && line.secondaryItemName.isNotBlank() ->
                                    "$flavour 1/2 ${line.primaryItemName} + 1/2 ${line.secondaryItemName}".trim()
                                line.isDoubleTopping && line.secondaryItemName.isNotBlank() ->
                                    "$flavour ${line.primaryItemName} + ${line.secondaryItemName}".trim()
                                else ->
                                    "$flavour $cleanParentName".trim()
                            }
                        }
                        line.category.contains("Drinks", ignoreCase = true) || line.category.contains("Minum", ignoreCase = true) ||
                            categoryName.contains("Drinks", ignoreCase = true) || categoryName.contains("Minum", ignoreCase = true) -> {
                            cleanParentName
                        }
                        else -> {
                            // Snacks or custom categories
                            if (line.flavourName.isNotBlank() && !line.flavourName.equals("Original", ignoreCase = true)) {
                                "$cleanParentName (${line.flavourName})"
                            } else {
                                cleanParentName
                            }
                        }
                    }

                    // Format modifiers / add-ons
                    val customParts = mutableListOf<String>()
                    if (line.category.contains("Drinks", ignoreCase = true) || line.category.contains("Minum", ignoreCase = true) ||
                        categoryName.contains("Drinks", ignoreCase = true) || categoryName.contains("Minum", ignoreCase = true)) {
                        if (line.sweetnessLevel.isNotBlank()) customParts.add("Sweetness: ${line.sweetnessLevel}")
                        if (line.icedLevel.isNotBlank()) customParts.add("Ice: ${line.icedLevel}")
                    }
                    if (line.addOnNames.isNotEmpty()) {
                        customParts.add("Add-on: " + line.addOnNames.joinToString(", "))
                    }
                    val customization = customParts.joinToString(" • ")

                    val key = "$title|||$customization"
                    val tracker = configMap.getOrPut(key) {
                        MutableVariationTracker(title = title, customization = customization)
                    }
                    tracker.units += line.qty
                    tracker.revenue += line.lineTotalFor(media)
                    tracker.orderIds.add(order.id)
                }
            }
        } else if (order.itemsSummary.contains(cleanParentName, ignoreCase = true)) {
            // Fallback for legacy records without itemsPayload
            val key = "$cleanParentName|||"
            val tracker = configMap.getOrPut(key) {
                MutableVariationTracker(title = cleanParentName, customization = "")
            }
            tracker.units += 1
            tracker.revenue += order.totalAmount
            tracker.orderIds.add(order.id)
            totalProductUnits += 1
        }
    }

    val configs = configMap.values.map { tracker ->
        ProductVariationConfig(
            title = tracker.title,
            customization = tracker.customization,
            unitsSold = tracker.units,
            grossRevenue = tracker.revenue,
            orderCount = tracker.orderIds.size
        )
    }.sortedByDescending { it.unitsSold }

    return totalProductUnits to configs
}

private class MutableVariationTracker(
    val title: String,
    val customization: String,
    var units: Int = 0,
    var revenue: Long = 0L,
    val orderIds: MutableSet<String> = mutableSetOf()
)

/**
 * Interactive Popup Modal displaying the Order Variation Analysis with stylish Horizontal Bar Charts.
 */
@Composable
fun DashboardProductVariationAnalysisDialog(
    productName: String,
    categoryName: String = "",
    subCategoryName: String = "",
    periodLabel: String = "",
    branchName: String = "",
    doneOrders: List<OrderRecord>,
    catalog: List<CatalogRecord>,
    onDismiss: () -> Unit
) {
    val (totalUnitsSold, variations) = remember(productName, categoryName, doneOrders, catalog) {
        analyzeProductOrderConfigurations(
            productName = productName,
            categoryName = categoryName,
            doneOrders = doneOrders,
            catalog = catalog
        )
    }

    val distinctOrdersCount = remember(productName, categoryName, doneOrders, catalog) {
        val cleanParentName = productName.trim()
        doneOrders.count { order ->
            val lines = order.parsedLineItems
            if (lines.isNotEmpty()) {
                lines.any { line ->
                    val isPrimary = line.primaryItemName.equals(cleanParentName, ignoreCase = true) ||
                        (line.primaryItemId.isNotBlank() && catalog.any { it.id == line.primaryItemId && it.name.equals(cleanParentName, ignoreCase = true) })
                    val isSecondary = (line.isHalfHalf || line.isDoubleTopping) && (
                        line.secondaryItemName.equals(cleanParentName, ignoreCase = true) ||
                            (line.secondaryItemId.isNotBlank() && catalog.any { it.id == line.secondaryItemId && it.name.equals(cleanParentName, ignoreCase = true) })
                        )
                    isPrimary || isSecondary
                }
            } else {
                order.itemsSummary.contains(cleanParentName, ignoreCase = true)
            }
        }
    }

    var showAllVariations by remember { mutableStateOf(false) }
    val displayedVariations = if (showAllVariations || variations.size <= 5) {
        variations
    } else {
        variations.take(5)
    }

    val maxVariationUnits = (variations.maxOfOrNull { it.unitsSold } ?: 1).coerceAtLeast(1)

    val isDrink = categoryName.contains("Drinks", ignoreCase = true) || categoryName.contains("Minum", ignoreCase = true)
    val unitLabel = if (isDrink) "cup" else "pcs"

    val accentColor = when {
        categoryName.contains("Waffle", ignoreCase = true) -> DeepCaramelGold
        isDrink -> MatchaSageSuccess
        else -> Color(0xFFB45309) // Warm Amber for Snacks
    }
    val barGradient = when {
        categoryName.contains("Waffle", ignoreCase = true) -> Brush.horizontalGradient(listOf(DeepCaramelGold, ChampagneGoldAccent))
        isDrink -> Brush.horizontalGradient(listOf(MatchaSageSuccess, Color(0xFF34D399)))
        else -> Brush.horizontalGradient(listOf(Color(0xFFB45309), Color(0xFFF59E0B)))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 660.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            border = BorderStroke(1.dp, SubtleSandBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = accentColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = productName,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = productName.uppercase(Locale.ROOT),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RichEspressoPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftOatSurface,
                                    border = BorderStroke(0.8.dp, SubtleSandBorder)
                                ) {
                                    val catDisplay = if (subCategoryName.isNotBlank()) "$categoryName • $subCategoryName" else categoryName
                                    Text(
                                        text = catDisplay,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MutedCocoaText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Analisis Variasi Pesanan • $periodLabel • Cabang ${com.example.data.model.formatCleanBranchName(branchName, branchName)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_variation_analysis_dialog")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SoftOatSurface,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = RichEspressoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // KPI Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Terjual
                    Surface(
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(16.dp),
                        color = accentColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Total Terjual",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$totalUnitsSold $unitLabel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor
                            )
                        }
                    }

                    // Transaksi
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftOatSurface.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, SubtleSandBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Di Pesanan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCocoaText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$distinctOrdersCount Transaksi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = RichEspressoPrimary
                            )
                        }
                    }

                    // Format Variasi Terdeteksi
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftGoldHighlight.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Format Varian",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCocoaText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${variations.size} Format",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepCaramelGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Chart Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Variasi Pesanan Terpopuler",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = RichEspressoPrimary
                    )

                    if (variations.size > 5) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftOatSurface,
                            border = BorderStroke(0.8.dp, SubtleSandBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { showAllVariations = !showAllVariations }
                                .testTag("toggle_all_variations_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (showAllVariations) "Tampilkan 5 Teratas" else "Lihat Semua (${variations.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                                Icon(
                                    imageVector = if (showAllVariations) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = RichEspressoPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Bar Chart Container with internal scroll
                if (variations.isEmpty() || totalUnitsSold == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SoftOatSurface,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = MutedCocoaText.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Belum ada pesanan selesai untuk $productName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MutedCocoaText
                            )
                            Text(
                                text = "Pilihan varian pelanggan pada periode $periodLabel akan muncul di sini.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCocoaText.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        displayedVariations.forEachIndexed { index, config ->
                            val rank = index + 1
                            val rankStr = String.format(Locale.US, "%02d", rank)
                            val sharePct = if (totalUnitsSold > 0) {
                                (config.unitsSold.toDouble() / totalUnitsSold.toDouble()) * 100.0
                            } else 0.0
                            val sharePctText = String.format(Locale.forLanguageTag("id-ID"), "%.1f%%", sharePct)
                            val barFraction = if (maxVariationUnits > 0) {
                                (config.unitsSold.toFloat() / maxVariationUnits.toFloat()).coerceIn(0.06f, 1f)
                            } else 0.06f

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SoftOatSurface.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    // Row 1: Rank Badge + Configuration Title + Units Sold (e.g. 99x)
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
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (rank <= 3) accentColor.copy(alpha = 0.15f) else ElevatedIvoryCard,
                                                border = BorderStroke(0.8.dp, if (rank <= 3) accentColor.copy(alpha = 0.4f) else SubtleSandBorder)
                                            ) {
                                                Text(
                                                    text = rankStr,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (rank <= 3) accentColor else RichEspressoPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = config.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = accentColor.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "${config.unitsSold}x",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = accentColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // Secondary Line: Modifiers / Add-ons / Sweetness
                                    if (config.customization.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = config.customization,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedCocoaText,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = 28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(7.dp))

                                    // Horizontal Progress Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(ElevatedIvoryCard)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(barFraction)
                                                .clip(RoundedCornerShape(50))
                                                .background(barGradient)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Percentage Share & Gross Revenue
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$sharePctText dari total ${productName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DeepCaramelGold
                                        )

                                        if (config.grossRevenue > 0L) {
                                            Text(
                                                text = WaffleoViewModel.formatIdr(config.grossRevenue),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary
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
