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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.remember
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
import com.example.ui.theme.TerracottaDanger
import com.example.ui.theme.buildAnnotatedCartTitle
import com.example.ui.theme.buildAnnotatedFlavourLine
import com.example.ui.viewmodel.WaffleoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
 * Modal Pop-up Estimasi Laba Kotor
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
                            Text("Estimasi Laba Kotor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
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
                            Text("Estimasi Laba Kotor:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCaramelGold)
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
                        text = "• Estimasi Laba Kotor dihitung dari total omzet penjualan dikurangi biaya komisi platform online (GrabFood, GoFood, ShopeeFood) dan diskon promo.",
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
 * Modal Pop-up Settlement Rekening Bank (BCA / Mandiri)
 */
@Composable
fun DashboardBankSettlementDialog(
    bankName: String, // "BCA" or "Mandiri"
    periodLabel: String,
    totalSettlement: Long,
    orders: List<OrderRecord>,
    catalog: List<CatalogRecord>,
    viewModel: WaffleoViewModel,
    onDismiss: () -> Unit
) {
    val isBca = bankName.contains("BCA", ignoreCase = true)
    val accentColor = if (isBca) DeepCaramelGold else Color(0xFF003882)
    val badgeBg = if (isBca) SoftGoldHighlight else Color(0xFFE8F0FE)

    val relevantOrders = remember(orders, bankName) {
        if (isBca) {
            orders.filter { ord ->
                ord.orderMedia in listOf("GrabFood", "GoFood", "ShopeeFood") ||
                    ord.paymentMethod.contains("BCA", ignoreCase = true)
            }
        } else {
            orders.filter { ord ->
                ord.orderMedia !in listOf("GrabFood", "GoFood", "ShopeeFood") &&
                    (ord.paymentMethod.contains("Mandiri", ignoreCase = true) ||
                        ord.paymentMethod.contains("QRIS", ignoreCase = true) ||
                        ord.paymentMethod.contains("Transfer", ignoreCase = true)) &&
                    !ord.paymentMethod.contains("BCA", ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .widthIn(max = 800.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = CircleShape, color = badgeBg, modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("Settlement Rekening Bank $bankName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            Text("Periode: $periodLabel • Total Masuk: ${WaffleoViewModel.formatIdr(totalSettlement)}", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (relevantOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada transaksi settlement $bankName pada periode ini", style = MaterialTheme.typography.bodyLarge, color = MutedCocoaText)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(relevantOrders, key = { it.id }) { ord ->
                            val isPlatform = ord.orderMedia in listOf("GrabFood", "GoFood", "ShopeeFood")
                            val netAmount = if (isPlatform) {
                                viewModel.calculateOrderNetAmount(ord, catalog)
                            } else {
                                if (ord.splitQrisAmount > 0L) ord.splitQrisAmount else ord.totalAmount
                            }
                            val deduction = if (isPlatform) viewModel.calculateOrderPlatformFee(ord, catalog) else 0L

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftOatSurface.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, SoftOatSurface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = RichEspressoPrimary) {
                                                Text(
                                                    text = ord.displayOrderNumber,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SoftGoldHighlight,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "${ord.orderMedia}  •  ${ord.customerData.ifBlank { "Guest" }}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RichEspressoPrimary
                                            )
                                        }
                                        Text(
                                            text = "Metode: ${ord.paymentMethod}${if (deduction > 0L) " • Potongan: -${WaffleoViewModel.formatIdr(deduction)}" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedCocoaText
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = WaffleoViewModel.formatIdr(netAmount),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                        Text("Dana Masuk", style = MaterialTheme.typography.labelSmall, color = MatchaSageSuccess)
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
