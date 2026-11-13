package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CartLineItem
import com.example.data.model.CatalogRecord
import com.example.data.model.OrderMedia
import com.example.data.model.OrderRecord
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
import com.example.ui.theme.buildAnnotatedCartTitle
import com.example.ui.theme.buildAnnotatedFlavourLine
import com.example.ui.viewmodel.WaffleoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Modal dialog menampilkan masing-masing pesanan pada saat periode Dashboard terpilih
 * dengan tampilan persis kartu di halaman Orders (namun tanpa tombol aksi).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardOrdersListDialog(
    periodLabel: String,
    orders: List<OrderRecord>,
    totalOmzet: Long,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 840.dp)
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = CircleShape, color = SoftSageBg, modifier = Modifier.size(34.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Payments, contentDescription = null, tint = MatchaSageSuccess, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(
                                text = "Daftar Pesanan Omzet Penjualan",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary
                            )
                        }
                        Text(
                            text = "Periode: $periodLabel • ${orders.size} Pesanan Selesai • Total: ${WaffleoViewModel.formatIdr(totalOmzet)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada pesanan selesai pada periode ini", style = MaterialTheme.typography.bodyLarge, color = MutedCocoaText)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 14.dp)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            DashboardOrderPreviewCard(order = order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardOrderPreviewCard(order: OrderRecord) {
    val waktuMasuk = remember(order) {
        val timelineEvents = order.getTimelineEvents()
        val createEvt = timelineEvents.firstOrNull { it.eventType == "ORDER_CREATED" } ?: timelineEvents.firstOrNull()
        if (createEvt != null && createEvt.timeText.isNotBlank()) {
            createEvt.timeText
        } else {
            val dt = (order.createdAt ?: order.updatedAt)?.toDate()
            if (dt != null) {
                SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt)
            } else {
                val epochMs = order.id.removePrefix("ord_").takeWhile { it.isDigit() }.toLongOrNull()
                if (epochMs != null && epochMs > 1000000000L) {
                    SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(epochMs))
                } else {
                    SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date())
                }
            }
        }
    }
    val waktuSelesai = remember(order) {
        val timelineEvents = order.getTimelineEvents()
        val doneEvt = timelineEvents.lastOrNull { it.eventType == "ORDER_DONE" || it.eventType == "ORDER_STATUS_CHANGED" }
            ?: timelineEvents.lastOrNull()
        if (doneEvt != null && doneEvt.timeText.isNotBlank()) {
            doneEvt.timeText
        } else {
            val dt = (order.updatedAt ?: order.createdAt)?.toDate()
            if (dt != null) {
                SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt)
            } else {
                waktuMasuk
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left badge: WFL number + status pill
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftSageBg
                ) {
                    Text(
                        text = order.displayOrderNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MatchaSageSuccess,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Selesai",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MatchaSageSuccess,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Middle: Channel, Cashier & Payment, Timestamps
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                val customerSuffix = if (order.customerData.isNotBlank() && order.customerData != "Walk In Guest" && order.customerData != "Pelanggan Di tempat") " • ${order.customerData}" else ""
                Text(
                    text = "${order.orderMedia}$customerSuffix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichEspressoPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val payDesc = if (order.isPaid) order.paymentMethod else "Belum Lunas"
                Text(
                    text = "Kasir: ${order.createdBy} • $payDesc",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedCocoaText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Masuk: $waktuMasuk • Selesai: $waktuSelesai",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedCocoaText.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right: Nominal Price
            Text(
                text = WaffleoViewModel.formatIdr(order.totalAmount),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = RichEspressoPrimary
            )
        }
    }
}

/**
 * Modal Pop-up Pendapatan Bersih Penjualan (Net Sales)
 */
@Composable
fun DashboardLabaKotorDialog(
    periodLabel: String,
    omzetGross: Long,
    potonganPlatform: Long,
    totalDiskonPromo: Long,
    labaKotor: Long,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = CircleShape, color = SoftOatSurface, modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = RichEspressoPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("Pendapatan Bersih Penjualan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            Text("Periode: $periodLabel", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                    }
                }

                // Breakdown Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftOatSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (totalDiskonPromo > 0L) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal Penjualan (Sebelum Promo):", style = MaterialTheme.typography.bodyMedium, color = MutedCocoaText)
                                Text(WaffleoViewModel.formatIdr(omzetGross + totalDiskonPromo), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MutedCocoaText)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Diskon Promo Toko:", style = MaterialTheme.typography.bodyMedium, color = DeepCaramelGold)
                                Text("-${WaffleoViewModel.formatIdr(totalDiskonPromo)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = DeepCaramelGold)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Omzet Penjualan (Gross):", style = MaterialTheme.typography.bodyMedium, color = RichEspressoPrimary)
                            Text(WaffleoViewModel.formatIdr(omzetGross), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Potongan Komisi Platform Online:", style = MaterialTheme.typography.bodyMedium, color = TerracottaDanger)
                            Text("-${WaffleoViewModel.formatIdr(potonganPlatform)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TerracottaDanger)
                        }

                        HorizontalDivider(color = SoftOatSurface, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Pendapatan Bersih:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCaramelGold)
                            Text(
                                text = WaffleoViewModel.formatIdr(labaKotor),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepCaramelGold
                            )
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(12.dp), color = SoftSageBg, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "• Pendapatan Bersih Penjualan (Net Sales) adalah dana penjualan bersih setelah potongan promo & komisi platform online (GrabFood, GoFood, ShopeeFood), belum dikurangi HPP bahan baku.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchaSageSuccess,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modal Pop-up Rincian Potongan Platform & Promo (Card 3 - Opsi A)
 */
@Composable
fun DashboardDeductionsDialog(
    periodLabel: String,
    omzetGross: Long,
    potonganPlatform: Long,
    totalDiskonPromo: Long,
    orders: List<OrderRecord>,
    catalog: List<CatalogRecord>,
    viewModel: WaffleoViewModel,
    onDismiss: () -> Unit
) {
    var selectedOrderForAudit by remember { mutableStateOf<OrderRecord?>(null) }
    val totalDeductions = potonganPlatform + totalDiskonPromo
    val grossBeforePromo = (omzetGross + totalDiskonPromo).coerceAtLeast(1L)
    val deductionPct = if (omzetGross > 0L || totalDiskonPromo > 0L) {
        ((totalDeductions.toDouble() / grossBeforePromo.toDouble()) * 100.0).roundToInt()
    } else 0

    val platformBreakdown = remember(orders, catalog) {
        listOf("GrabFood", "GoFood", "ShopeeFood").map { platformName ->
            val pOrders = orders.filter { it.orderMedia.equals(platformName, ignoreCase = true) }
            val pGross = pOrders.sumOf { it.totalAmount }
            val pFee = pOrders.sumOf { viewModel.calculateOrderPlatformFee(it, catalog) }
            Triple(platformName, pOrders.size, pFee to pGross)
        }.filter { it.second > 0 || it.third.first > 0L }
    }

    val affectedOrders = remember(orders, catalog) {
        orders.filter { ord ->
            ord.discountAmount > 0L || viewModel.calculateOrderPlatformFee(ord, catalog) > 0L
        }.sortedByDescending {
            val dt = (it.updatedAt ?: it.createdAt)?.toDate()
            dt?.time ?: it.id.removePrefix("v1_ord_bd_").removePrefix("v1_ord_").takeWhile { c -> c.isDigit() }.toLongOrNull() ?: 0L
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.86f)
                .widthIn(max = 780.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(shape = CircleShape, color = SoftAmberBg, modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = HoneyAmberWarning, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("Potongan Platform & Promo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            Text("Periode: $periodLabel • Total Rasio Potongan: $deductionPct%", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Top KPI Summary Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftAmberBg.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, HoneyAmberWarning.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Potongan", style = MaterialTheme.typography.labelSmall, color = HoneyAmberWarning, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(WaffleoViewModel.formatIdr(totalDeductions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = HoneyAmberWarning)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftTerracottaBg.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, TerracottaDanger.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Komisi Platform", style = MaterialTheme.typography.labelSmall, color = TerracottaDanger, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(WaffleoViewModel.formatIdr(potonganPlatform), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TerracottaDanger)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftGoldHighlight.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Diskon Promo Toko", style = MaterialTheme.typography.labelSmall, color = DeepCaramelGold, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(WaffleoViewModel.formatIdr(totalDiskonPromo), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = DeepCaramelGold)
                        }
                    }
                }

                if (platformBreakdown.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftOatSurface.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Rincian Komisi per Platform Online", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            platformBreakdown.forEach { (platformName, count, feePair) ->
                                val (fee, pGross) = feePair
                                val effPct = if (pGross > 0L) ((fee.toDouble() / pGross.toDouble()) * 100.0).roundToInt() else 0
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$platformName ($count order • ~$effPct%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedCocoaText,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "-${WaffleoViewModel.formatIdr(fee)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaDanger
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Transaksi Terdampak Potongan (${affectedOrders.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RichEspressoPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (affectedOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada potongan komisi platform atau diskon promo pada periode ini", style = MaterialTheme.typography.bodyLarge, color = MutedCocoaText)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(affectedOrders, key = { it.id }) { ord ->
                            val fee = viewModel.calculateOrderPlatformFee(ord, catalog)
                            val disc = ord.discountAmount
                            val net = viewModel.calculateOrderNetAmount(ord, catalog)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftOatSurface.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedOrderForAudit = ord }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = RichEspressoPrimary) {
                                                Text(
                                                    text = ord.displayOrderNumber,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SoftGoldHighlight,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = ord.orderMedia,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary
                                            )
                                        }
                                        val detailParts = buildList {
                                            if (fee > 0L) add("Komisi Platform: -${WaffleoViewModel.formatIdr(fee)}")
                                            if (disc > 0L) add("Promo ${ord.promoCode.ifBlank { "" }}: -${WaffleoViewModel.formatIdr(disc)}".trim())
                                        }.joinToString(" • ")
                                        Text(
                                            text = "$detailParts • Net: ${WaffleoViewModel.formatIdr(net)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedCocoaText
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "-${WaffleoViewModel.formatIdr(fee + disc)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = HoneyAmberWarning
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Audit Trail",
                                            tint = MutedCocoaText.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
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

    selectedOrderForAudit?.let { ord ->
        ChannelOrderAuditTrailDialog(
            order = ord,
            catalog = catalog,
            viewModel = viewModel,
            onDismiss = { selectedOrderForAudit = null }
        )
    }
}

/**
 * Modal Pop-up Rata-rata per Pesanan / Average Order Value (Card 4 - Opsi A)
 */
@Composable
fun DashboardAovDetailDialog(
    periodLabel: String,
    omzetGross: Long,
    orders: List<OrderRecord>,
    catalog: List<CatalogRecord>,
    viewModel: WaffleoViewModel,
    onDismiss: () -> Unit
) {
    var selectedOrderForAudit by remember { mutableStateOf<OrderRecord?>(null) }
    val stats = remember(orders) { computeExecutiveAovStats(orders) }
    val avgNetValue = remember(orders, catalog) {
        if (orders.isEmpty()) 0L else orders.sumOf { viewModel.calculateOrderNetAmount(it, catalog) } / orders.size
    }

    val channelAovRows = remember(orders) {
        orders.groupBy { it.orderMedia.ifBlank { "Di tempat" } }
            .map { (mediaName, list) ->
                val total = list.sumOf { it.totalAmount }
                val avg = total / list.size
                val itemsCount = list.sumOf { ord ->
                    val parsed = ord.parsedLineItems
                    if (parsed.isNotEmpty()) parsed.sumOf { it.qty } else 1
                }
                val avgItems = itemsCount.toDouble() / list.size.toDouble()
                Triple(mediaName, list.size, Triple(total, avg, avgItems))
            }
            .sortedByDescending { it.third.second }
    }

    val topOrders = remember(orders) {
        orders.sortedByDescending { it.totalAmount }.take(10)
    }

    val blueAccent = Color(0xFF1D4ED8)
    val blueBg = Color(0xFFDBEAFE)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.86f)
                .widthIn(max = 780.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(shape = CircleShape, color = blueBg, modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = blueAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("Rata-rata per Pesanan (AOV)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            Text("Periode: $periodLabel • ${orders.size} Pesanan Selesai • Total Omzet: ${WaffleoViewModel.formatIdr(omzetGross)}", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Top 3 Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(16.dp),
                        color = blueBg.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, blueAccent.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Rata-rata Gross (AOV)", style = MaterialTheme.typography.labelSmall, color = blueAccent, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(WaffleoViewModel.formatIdr(stats.averageOrderValue), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = blueAccent)
                            Text("Net: ${WaffleoViewModel.formatIdr(avgNetValue)}/ord", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftGoldHighlight.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Rata-rata Item", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                            Spacer(modifier = Modifier.height(2.dp))
                            val avgItemStr = String.format(Locale.forLanguageTag("id-ID"), "%.1f item", stats.averageItemsPerOrder)
                            Text(avgItemStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = DeepCaramelGold)
                            Text("${stats.totalItems} total item", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftOatSurface.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, SubtleSandBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Rentang Pesanan", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Max: ${WaffleoViewModel.formatIdr(stats.maxOrderValue)}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MatchaSageSuccess)
                            Text("Min: ${WaffleoViewModel.formatIdr(stats.minOrderValue)}", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada pesanan selesai pada periode ini", style = MaterialTheme.typography.bodyLarge, color = MutedCocoaText)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        if (channelAovRows.isNotEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SoftOatSurface.copy(alpha = 0.55f),
                                    border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("Perbandingan AOV per Kanal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                                        channelAovRows.forEach { (mediaName, count, tripleVal) ->
                                            val (_, avgVal, avgItems) = tripleVal
                                            val avgItemsFmt = String.format(Locale.forLanguageTag("id-ID"), "%.1f item", avgItems)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "$mediaName ($count order • $avgItemsFmt)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MutedCocoaText,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${WaffleoViewModel.formatIdr(avgVal)}/order",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = blueAccent
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pesanan dengan Nilai Tertinggi (Top ${topOrders.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary
                            )
                        }

                        items(topOrders, key = { it.id }) { ord ->
                            val itemCount = ord.parsedLineItems.sumOf { it.qty }.coerceAtLeast(1)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftOatSurface.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedOrderForAudit = ord }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = RichEspressoPrimary) {
                                                Text(
                                                    text = ord.displayOrderNumber,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SoftGoldHighlight,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "${ord.orderMedia} • ${ord.customerData.ifBlank { "Pelanggan" }}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "$itemCount item • Pembayaran: ${ord.paymentMethod.ifBlank { "Cash" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedCocoaText
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = WaffleoViewModel.formatIdr(ord.totalAmount),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = blueAccent
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Audit Trail",
                                            tint = MutedCocoaText.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
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

    selectedOrderForAudit?.let { ord ->
        ChannelOrderAuditTrailDialog(
            order = ord,
            catalog = catalog,
            viewModel = viewModel,
            onDismiss = { selectedOrderForAudit = null }
        )
    }
}
