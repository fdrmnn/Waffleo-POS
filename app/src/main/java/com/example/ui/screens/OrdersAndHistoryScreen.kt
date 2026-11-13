package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.RoomService
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewStream
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.data.model.CartLineItem
import com.example.data.model.CatalogRecord
import com.example.data.model.OrderPaymentLedgerEntry
import com.example.data.model.OrderRecord
import com.example.data.model.OrderTimelineEvent
import com.example.data.model.StaffRole
import com.example.data.model.WaffleoOrderEngine
import com.example.data.model.capitalizeSentenceStart
import com.example.ui.components.CancelOrderDialog
import com.example.ui.components.PaymentModalDialog
import com.example.ui.components.ThermalReceiptDialog
import com.example.ui.components.WaffleoSingleSelectDropdown
import com.example.ui.theme.ChampagneGoldAccent
import com.example.ui.theme.DeepCaramelGold
import com.example.ui.theme.ElevatedIvoryCard
import com.example.ui.theme.HoneyAmberWarning
import com.example.ui.theme.MatchaSageSuccess
import com.example.ui.theme.MutedCocoaText
import com.example.ui.theme.RichEspressoPrimary
import com.example.ui.theme.buildAnnotatedCartTitle
import com.example.ui.theme.buildAnnotatedCartTitleSingleLine
import com.example.ui.theme.buildAnnotatedFlavourLine
import com.example.ui.theme.RoastedMochaSecondary
import com.example.ui.theme.SoftAmberBg
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SoftSageBg
import com.example.ui.theme.SoftTerracottaBg
import com.example.ui.theme.SubtleSandBorder
import com.example.ui.theme.TerracottaDanger
import com.example.ui.viewmodel.HistoryFilterPeriod
import com.example.ui.viewmodel.WaffleoViewModel
import com.example.data.model.OrderMedia
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersQueueScreen(
    viewModel: WaffleoViewModel
) {
    val allOrders by viewModel.orders.collectAsState()
    val catalog by viewModel.catalogRecords.collectAsState()
    val customMenuCategories by viewModel.customMenuCategories.collectAsState()
    val customModifierCategories by viewModel.customModifierCategories.collectAsState()
    val uiPrefs by viewModel.sessionUiPreferences.collectAsState()
    val activeOrders = remember(allOrders) {
        allOrders.filter { it.status == "ACTIVE" }.sortedBy { it.sequenceIndex }
    }

    var orderToCancel by remember { mutableStateOf<OrderRecord?>(null) }
    var orderToPay by remember { mutableStateOf<OrderRecord?>(null) }
    var orderToPrint by remember { mutableStateOf<OrderRecord?>(null) }

    val gridColumns = uiPrefs.ordersGridColumns
    val itemDisplayFormat = if (uiPrefs.ordersItemDisplayFormat == 2) 2 else 1 // 1 = Checklist per unit (default); 2 = Grouped Qty
    var showFormatDropdown by remember { mutableStateOf(false) }

    val dropdownArrowRotation by animateFloatAsState(
        targetValue = if (showFormatDropdown) 180f else 0f,
        label = "dropdownArrow"
    )
    val formatIcon = when (itemDisplayFormat) {
        2 -> Icons.Rounded.ViewAgenda
        else -> Icons.Rounded.Checklist
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Modern Floating Control Cluster: Display Mode Switcher & Column Toggle (Zero layout shift)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Pemilih Format Tampilan (Stylish & Eye-Catching)
            Box {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ElevatedIvoryCard,
                    shadowElevation = 2.dp,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (showFormatDropdown) DeepCaramelGold else ChampagneGoldAccent.copy(alpha = 0.75f)
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { showFormatDropdown = true }
                        .testTag("display_format_dropdown_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 5.dp, end = 10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RichEspressoPrimary,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = formatIcon,
                                    contentDescription = null,
                                    tint = SoftGoldHighlight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Tampilan $itemDisplayFormat",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Pilih Tampilan",
                            tint = DeepCaramelGold,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(dropdownArrowRotation)
                        )
                    }
                }

                // Dropdown Menu (Overlay popup: tidak mendorong layout di bawahnya)
                DropdownMenu(
                    expanded = showFormatDropdown,
                    onDismissRequest = { showFormatDropdown = false },
                    modifier = Modifier.background(ElevatedIvoryCard)
                ) {
                    val formatOptions = listOf(
                        Triple(1, "Tampilan 1", "Checklist per unit item" to Icons.Rounded.Checklist),
                        Triple(2, "Tampilan 2", "Ringkas & dikelompokkan" to Icons.Rounded.ViewAgenda)
                    )
                    formatOptions.forEach { (fmtNum, fmtTitle, meta) ->
                        val (fmtSub, fmtIc) = meta
                        val isSelected = itemDisplayFormat == fmtNum
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) RichEspressoPrimary else SoftGoldHighlight,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = fmtIc,
                                                contentDescription = null,
                                                tint = if (isSelected) SoftGoldHighlight else DeepCaramelGold,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fmtTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isSelected) DeepCaramelGold else RichEspressoPrimary
                                        )
                                        Text(
                                            text = fmtSub,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MutedCocoaText
                                        )
                                    }
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SoftSageBg,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = "Aktif",
                                                    tint = MatchaSageSuccess,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            onClick = {
                                viewModel.setOrdersItemDisplayFormat(fmtNum)
                                showFormatDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tombol Kolom Tunggal (Toggle 1 Kolom / 2 Kolom dalam 1 tombol stylish)
            Surface(
                shape = RoundedCornerShape(50),
                color = ElevatedIvoryCard,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, SubtleSandBorder),
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable { viewModel.setOrdersGridColumns(if (gridColumns == 1) 2 else 1) }
                    .testTag("column_toggle_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 5.dp, end = 11.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SoftGoldHighlight,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (gridColumns == 1) Icons.Rounded.ViewStream else Icons.Rounded.GridView,
                                contentDescription = null,
                                tint = DeepCaramelGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = if (gridColumns == 1) "1 Kolom" else "2 Kolom",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (activeOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = ElevatedIvoryCard,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = ChampagneGoldAccent,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Belum ada antrean pesanan aktif",
                        style = MaterialTheme.typography.titleLarge,
                        color = RichEspressoPrimary
                    )
                    Text(
                        text = "Kirim pesanan dari keranjang pada halaman Menus",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedCocoaText
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 2.dp)
            ) {
                items(activeOrders, key = { it.id }) { order ->
                    val isPlat = OrderMedia.fromLabel(order.orderMedia).isPlatform
                    val netBca = if (isPlat) viewModel.calculateOrderNetAmount(order) else order.totalAmount
                    val platformFee = if (isPlat) viewModel.calculateOrderPlatformFee(order) else 0L
                    ActiveOrderCard(
                        order = order,
                        netBcaAmount = netBca,
                        platformFeeAmount = platformFee,
                        isCompactColumns = gridColumns > 1,
                        displayFormat = itemDisplayFormat,
                        catalog = catalog,
                        menuCategories = customMenuCategories,
                        modifierCategories = customModifierCategories,
                        onDeleteClick = { orderToCancel = order },
                        onEditClick = { viewModel.startEditingOrder(order) },
                        onPrintClick = { orderToPrint = order },
                        onPayClick = {
                            if (!order.isPaid) orderToPay = order
                        },
                        onPrepareClick = {
                            viewModel.cycleOrderProcessingStage(order)
                        },
                        onDoneClick = {
                            viewModel.completeOrder(order)
                        },
                        onRefundOverpaidClick = {
                            viewModel.confirmOrderOverpaymentRefund(order, "Cash")
                        },
                        onToggleUnitCheck = { item, unitIndex ->
                            viewModel.toggleOrderItemUnitChecked(order, item.lineId, unitIndex)
                        }
                    )
                }
            }
        }
    }

    orderToCancel?.let { ord ->
        com.example.ui.components.CancelOrderDialog(
            order = ord,
            onDismiss = { orderToCancel = null },
            onConfirmCancel = { reason ->
                viewModel.cancelOrderWithReason(ord, reason)
                orderToCancel = null
            }
        )
    }

    orderToPay?.let { ord ->
        PaymentModalDialog(
            title = "Pelunasan Pesanan ${ord.displayOrderNumber}",
            totalBillAmount = ord.totalAmount,
            alreadyPaidAmount = ord.paidAmount,
            defaultMedia = ord.orderMedia,
            originalSubtotalAmount = ord.subtotalAmount,
            promoDiscountAmount = ord.discountAmount,
            promoCode = ord.promoCode,
            onDismiss = { orderToPay = null },
            onConfirmPayment = { method, cashReceived, splitCash, splitQris, qrisBank, changeDue ->
                viewModel.markOrderPaidInQueue(ord, method, splitCash, splitQris, qrisBank, changeDue, cashReceived)
            }
        )
    }

    orderToPrint?.let { ord ->
        ThermalReceiptDialog(
            order = ord,
            onDismiss = { orderToPrint = null },
            onPrinted = {
                viewModel.showBanner("Struk ${ord.displayOrderNumber} berhasil dikirim ke printer thermal.")
            },
            catalog = catalog,
            menuCategories = customMenuCategories,
            modifierCategories = customModifierCategories
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveOrderCard(
    order: OrderRecord,
    netBcaAmount: Long,
    platformFeeAmount: Long,
    isCompactColumns: Boolean = false,
    displayFormat: Int = 1,
    catalog: List<CatalogRecord> = emptyList(),
    menuCategories: List<String> = listOf("Egg Waffle", "Drinks"),
    modifierCategories: List<String> = listOf("Flavour", "Add-on", "Sweetness", "Ice Level"),
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onPrintClick: () -> Unit,
    onPayClick: () -> Unit,
    onPrepareClick: () -> Unit,
    onDoneClick: () -> Unit,
    onRefundOverpaidClick: () -> Unit = {},
    onToggleUnitCheck: (CartLineItem, Int) -> Unit = { _, _ -> }
) {
    val isDoneEnabled = order.isPaid && order.isPrepared && order.overpaidAmount <= 0L
    val isPlatformOrder = order.orderMedia in listOf("GrabFood", "GoFood", "ShopeeFood")
    // Universal Settings-driven sort order across Tampilan 1, 2, 3, 1 Kolom, and 2 Kolom
    val lineItems = remember(order.itemsPayload, order.revisionCount, catalog, menuCategories, modifierCategories) {
        WaffleoOrderEngine.sortCartLineItems(
            items = CartLineItem.deserializeList(order.itemsPayload),
            catalog = catalog,
            menuCategories = menuCategories,
            modifierCategories = modifierCategories,
            separateNewRevisionAdditions = order.revisionCount > 0
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Header: Order ID Pill, Media Badge Pill, Customer Data, Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = RichEspressoPrimary
                    ) {
                        Text(
                            text = "#${order.sequenceIndex} • ${order.displayOrderNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SoftGoldHighlight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftGoldHighlight
                    ) {
                        Text(
                            text = "${order.orderMedia}  |  ${order.customerData}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RichEspressoPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Stylish Modern Price Typography with Promo Strikethrough
                val hasPromoDiscount = order.discountAmount > 0L || (order.subtotalAmount > order.totalAmount)
                if (hasPromoDiscount) {
                    val originalSubtotal = if (order.subtotalAmount > 0L) order.subtotalAmount else (order.totalAmount + order.discountAmount)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = WaffleoViewModel.formatIdr(originalSubtotal),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                textDecoration = TextDecoration.LineThrough,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MutedCocoaText
                        )
                        Text(
                            text = ">>",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TerracottaDanger
                        )
                        Text(
                            text = WaffleoViewModel.formatIdr(order.totalAmount),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = RichEspressoPrimary
                        )
                    }
                } else {
                    Text(
                        text = WaffleoViewModel.formatIdr(order.totalAmount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = RichEspressoPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Structured Line Items with High-Contrast Quantity Badge & Modifier Chips
            // Inline with Right-Aligned Action Buttons (Saves ~40-50dp vertical height per card)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftOatSurface.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column: Items and Notes (weight allows items to flow downwards naturally if multiple items)
                    Column(
                        modifier = Modifier.weight(if (isCompactColumns) 1.25f else 1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (lineItems.isNotEmpty()) {
                            if (displayFormat == 1) {
                                // TAMPILAN 1 (Default): List flat per unit dengan modifier di kanan (otomatis turun ke bawah semua jika kepanjangan) + ceklis dinamis
                                lineItems.forEach { item ->
                                    val singleLineTitle = remember(item) { item.buildAnnotatedCartTitleSingleLine() }

                                    for (uIdx in 0 until item.qty) {
                                        val isUnitChecked = uIdx < item.completedUnitCount || item.isCompletedLockedBadge || order.processingStage >= 2
                                        ChecklistUnitItemRow(
                                            singleLineTitle = singleLineTitle,
                                            item = item,
                                            isUnitChecked = isUnitChecked,
                                            onToggle = { onToggleUnitCheck(item, uIdx) }
                                        )
                                    }
                                }
                            } else {
                                // TAMPILAN 2: Grouped Qty badge + chips
                                lineItems.forEach { item ->
                                    val isItemCompleted = item.processingStage >= 2 || (order.isPrepared && !item.isNewRevisionAddition)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // High-contrast quantity pill + ✅ indicator for completed items (NEVER ❌)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isItemCompleted) SoftSageBg else SoftGoldHighlight,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isItemCompleted) MatchaSageSuccess.copy(alpha = 0.7f) else ChampagneGoldAccent.copy(alpha = 0.6f)
                                            )
                                        ) {
                                            Text(
                                                text = if (isItemCompleted) "✅ ${item.qty}x" else "${item.qty}x",
                                                color = if (isItemCompleted) MatchaSageSuccess else RichEspressoPrimary,
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            val annotatedTitle = remember(item) { item.buildAnnotatedCartTitle() }
                                            Text(
                                                text = annotatedTitle,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 12.5.sp,
                                                    lineHeight = 16.5.sp,
                                                    letterSpacing = (-0.1).sp
                                                ),
                                                color = RichEspressoPrimary
                                            )

                                            // Variant & Add-on chips
                                            FlowRow(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (item.addOnNames.isNotEmpty()) {
                                                    item.addOnNames.forEach { addon ->
                                                        val isCheeseInWaffle = addon.contains("keju dalam waffle", ignoreCase = true) ||
                                                            (addon.contains("keju", ignoreCase = true) && addon.contains("waffle", ignoreCase = true))
                                                        val isCustomMod = addon.contains(":")
                                                        val addOnLabel = if (isCustomMod || addon.startsWith("+")) addon else "+$addon"

                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = when {
                                                                isCheeseInWaffle -> SoftAmberBg
                                                                isCustomMod -> SoftGoldHighlight.copy(alpha = 0.85f)
                                                                else -> SoftSageBg
                                                            },
                                                            border = when {
                                                                isCheeseInWaffle -> BorderStroke(0.5.dp, HoneyAmberWarning)
                                                                isCustomMod -> BorderStroke(0.5.dp, ChampagneGoldAccent)
                                                                else -> null
                                                            }
                                                        ) {
                                                            Text(
                                                                text = addOnLabel,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = if (isCheeseInWaffle) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                                color = when {
                                                                    isCheeseInWaffle -> DeepCaramelGold
                                                                    isCustomMod -> RichEspressoPrimary
                                                                    else -> MatchaSageSuccess
                                                                },
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (item.category == "Drinks") {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = SoftGoldHighlight.copy(alpha = 0.7f)
                                                    ) {
                                                        Text(
                                                            text = "Ice: ${item.icedLevel} • Sugar: ${item.sweetnessLevel}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = RichEspressoPrimary,
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Fallback parser for orders without JSON itemsPayload
                            order.itemsSummary.lines().filter { it.isNotBlank() }.forEach { line ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val parts = line.trim().split(" ", limit = 2)
                                    if (parts.size >= 2 && parts[0].endsWith("x", ignoreCase = true)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SoftGoldHighlight,
                                            border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.6f))
                                        ) {
                                            Text(
                                                text = parts[0],
                                                color = RichEspressoPrimary,
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        val annotatedLine = remember(parts[1]) { buildAnnotatedFlavourLine(parts[1]) }
                                        Text(
                                            text = annotatedLine,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 12.5.sp,
                                                lineHeight = 16.5.sp,
                                                letterSpacing = (-0.1).sp
                                            ),
                                            color = RichEspressoPrimary
                                        )
                                    } else {
                                        val annotatedLine = remember(line) { buildAnnotatedFlavourLine(line) }
                                        Text(
                                            text = annotatedLine,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 12.5.sp,
                                                lineHeight = 16.5.sp,
                                                letterSpacing = (-0.1).sp
                                            ),
                                            color = RichEspressoPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Catatan Customer if any (Fit-to-content width, NOT full-width)
                        if (order.notes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SoftAmberBg,
                                border = BorderStroke(0.5.dp, HoneyAmberWarning.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(top = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = DeepCaramelGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Catatan: ${order.notes}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCaramelGold
                                    )
                                }
                            }
                        }
                    }

                    // Right: Action Buttons (Inline right cluster spanning symmetrically up to middle line)
                    if (!isCompactColumns) {
                        // 1-Kolom View: 6 buttons in 1 row spanning symmetrically up to the center line (~50% width)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OrderActionButtons(
                                order = order,
                                isDoneEnabled = isDoneEnabled,
                                onDeleteClick = onDeleteClick,
                                onEditClick = onEditClick,
                                onPrintClick = onPrintClick,
                                onPayClick = onPayClick,
                                onPrepareClick = onPrepareClick,
                                onDoneClick = onDoneClick
                            )
                        }
                    } else {
                        // 2-Kolom View: 2 baris x 3 tombol khusus icon/logo tanpa teks agar rapi & tidak terpotong
                        Column(
                            modifier = Modifier
                                .weight(0.75f)
                                .padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OrderDeleteButton(order = order, iconOnly = true, modifier = Modifier.weight(1f), onClick = onDeleteClick)
                                OrderEditButton(order = order, iconOnly = true, modifier = Modifier.weight(1f), onClick = onEditClick)
                                OrderPrintButton(order = order, iconOnly = true, modifier = Modifier.weight(1f), onClick = onPrintClick)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OrderPayButton(order = order, iconOnly = true, modifier = Modifier.weight(1f), onClick = onPayClick)
                                OrderPrepareButton(order = order, modifier = Modifier.weight(1f), onClick = onPrepareClick)
                                OrderDoneButton(order = order, isDoneEnabled = isDoneEnabled, iconOnly = true, modifier = Modifier.weight(1f), onClick = onDoneClick)
                            }
                        }
                    }
                }
            }

            // Phase 9: Payment Reconciliation Banner after Order Edit (Shortfall vs Overpaid / Refund Required)
            if (order.shortfallAmount > 0L) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftTerracottaBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Text(
                        text = "⚠️ KURANG BAYAR: ${WaffleoViewModel.formatIdr(order.shortfallAmount)} (Klik Bayar untuk pelunasan selisih)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaDanger,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            } else if (order.overpaidAmount > 0L) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftAmberBg,
                    border = BorderStroke(1.dp, HoneyAmberWarning),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💸 LEBIH BAYAR / WAJIB REFUND: ${WaffleoViewModel.formatIdr(order.overpaidAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepCaramelGold,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onRefundOverpaidClick,
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RichEspressoPrimary,
                                contentColor = SoftGoldHighlight
                            ),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("confirm_refund_overpaid_btn_${order.orderNumber}")
                        ) {
                            Text(
                                text = "Konfirmasi Refund",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChecklistUnitItemRow(
    singleLineTitle: AnnotatedString,
    item: CartLineItem,
    isUnitChecked: Boolean,
    onToggle: () -> Unit
) {
    val hasModifiers = item.addOnNames.isNotEmpty() || item.category == "Drinks"

    @Composable
    fun ModifierChipsList() {
        if (item.addOnNames.isNotEmpty()) {
            item.addOnNames.forEach { addon ->
                val isCheeseInWaffle = addon.contains("keju dalam waffle", ignoreCase = true) ||
                    (addon.contains("keju", ignoreCase = true) && addon.contains("waffle", ignoreCase = true))
                val isCustomMod = addon.contains(":")
                val addOnLabel = if (isCustomMod || addon.startsWith("+")) addon else "+$addon"

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when {
                        isCheeseInWaffle -> SoftAmberBg
                        isCustomMod -> SoftGoldHighlight.copy(alpha = 0.85f)
                        else -> SoftSageBg
                    },
                    border = when {
                        isCheeseInWaffle -> BorderStroke(0.5.dp, HoneyAmberWarning)
                        isCustomMod -> BorderStroke(0.5.dp, ChampagneGoldAccent)
                        else -> null
                    }
                ) {
                    Text(
                        text = addOnLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isCheeseInWaffle) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = when {
                            isCheeseInWaffle -> DeepCaramelGold
                            isCustomMod -> RichEspressoPrimary
                            else -> MatchaSageSuccess
                        },
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
        if (item.category == "Drinks") {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SoftGoldHighlight.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "Ice: ${item.icedLevel} • Sugar: ${item.sweetnessLevel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = RichEspressoPrimary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }

    @Composable
    fun UnitCheckBoxBadge(modifier: Modifier = Modifier) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (isUnitChecked) MatchaSageSuccess else Color.Transparent,
            border = BorderStroke(
                width = 1.dp,
                color = if (isUnitChecked) MatchaSageSuccess else SubtleSandBorder
            ),
            modifier = modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isUnitChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Diceklis",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 1.5.dp, horizontal = 2.dp)
    ) {
        SubcomposeLayout { constraints ->
            val maxW = constraints.maxWidth
            val unbounded = Constraints(maxWidth = Constraints.Infinity, maxHeight = Constraints.Infinity)

            val titleSingleWidth = subcompose("measure_title") {
                Text(
                    text = singleLineTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 16.5.sp,
                        letterSpacing = (-0.1).sp
                    ),
                    color = RichEspressoPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }.first().measure(unbounded).width

            val tailSingleWidth = subcompose("measure_tail") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ModifierChipsList()
                    UnitCheckBoxBadge()
                }
            }.first().measure(unbounded).width

            val gapPx = 6.dp.roundToPx()
            val fitsOneLine = (titleSingleWidth + gapPx + tailSingleWidth) <= maxW

            val finalPlaceable = if (fitsOneLine) {
                subcompose("render_inline") {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = singleLineTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.5.sp,
                                letterSpacing = (-0.1).sp
                            ),
                            color = RichEspressoPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                        if (hasModifiers) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                ModifierChipsList()
                                UnitCheckBoxBadge()
                            }
                        } else {
                            UnitCheckBoxBadge()
                        }
                    }
                }.first().measure(constraints.copy(minWidth = 0))
            } else if (hasModifiers) {
                // Jika kepanjangan & memiliki modifier: pindahkan SEMUA modifier (+ tombol ceklis) ke bawah judul item
                subcompose("render_stacked") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = singleLineTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.5.sp,
                                letterSpacing = (-0.1).sp
                            ),
                            color = RichEspressoPrimary
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ModifierChipsList()
                            UnitCheckBoxBadge(modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                }.first().measure(constraints.copy(minWidth = 0))
            } else {
                // Jika kepanjangan tanpa modifier: judul wrap natural tanpa terpotong dengan ceklis di kanan
                subcompose("render_wrapped_plain") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = singleLineTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.5.sp,
                                letterSpacing = (-0.1).sp
                            ),
                            color = RichEspressoPrimary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        UnitCheckBoxBadge()
                    }
                }.first().measure(constraints.copy(minWidth = 0))
            }

            layout(finalPlaceable.width, finalPlaceable.height) {
                finalPlaceable.placeRelative(0, 0)
            }
        }
    }
}

@Composable
private fun OrderDeleteButton(order: OrderRecord, iconOnly: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = SoftTerracottaBg,
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("order_delete_btn_${order.orderNumber}")
    ) {
        if (iconOnly) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Batal", tint = TerracottaDanger, modifier = Modifier.size(18.dp))
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Batal", tint = TerracottaDanger, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Batal", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp), fontWeight = FontWeight.Bold, color = TerracottaDanger, maxLines = 1)
            }
        }
    }
}

@Composable
private fun OrderEditButton(order: OrderRecord, iconOnly: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = SoftOatSurface,
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("order_edit_btn_${order.orderNumber}")
    ) {
        if (iconOnly) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RichEspressoPrimary, modifier = Modifier.size(17.dp))
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RichEspressoPrimary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Edit", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp), fontWeight = FontWeight.Bold, color = RichEspressoPrimary, maxLines = 1)
            }
        }
    }
}

@Composable
private fun OrderPrintButton(order: OrderRecord, iconOnly: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = SoftOatSurface,
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("order_print_btn_${order.orderNumber}")
    ) {
        if (iconOnly) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.Print, contentDescription = "Print", tint = RichEspressoPrimary, modifier = Modifier.size(17.dp))
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = "Print", tint = RichEspressoPrimary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Print", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp), fontWeight = FontWeight.Bold, color = RichEspressoPrimary, maxLines = 1)
            }
        }
    }
}

@Composable
private fun OrderPayButton(order: OrderRecord, iconOnly: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isPlatform = order.orderMedia in listOf("GrabFood", "GoFood", "ShopeeFood")
    if (isPlatform) {
        // Platform orders (GrabFood/GoFood/ShopeeFood): Uang baru masuk saat pesanan selesai, tombol dikosongkan tanda " - "
        Surface(
            shape = RoundedCornerShape(50),
            color = SoftOatSurface.copy(alpha = 0.5f),
            modifier = modifier.height(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = " - ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Bold,
                    color = MutedCocoaText.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        Button(
            onClick = onClick,
            enabled = !order.isPaid,
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = if (iconOnly) 0.dp else 4.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (order.isPaid) MatchaSageSuccess else ChampagneGoldAccent,
                contentColor = if (order.isPaid) ElevatedIvoryCard else RichEspressoPrimary,
                disabledContainerColor = MatchaSageSuccess,
                disabledContentColor = ElevatedIvoryCard
            ),
            modifier = modifier
                .height(36.dp)
                .testTag("order_pay_btn_${order.orderNumber}")
        ) {
            if (iconOnly) {
                Icon(
                    imageVector = if (order.isPaid) Icons.Default.CheckCircle else Icons.Default.Payment,
                    contentDescription = if (order.isPaid) "Lunas" else "Bayar",
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Icon(
                    imageVector = if (order.isPaid) Icons.Default.CheckCircle else Icons.Default.Payment,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (order.isPaid) "Lunas" else "Bayar",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 3-Stage Order Processing Button:
 * - Stage 0 (BELUM_DIPROSES): Neutral surface with stylish Waffle & Cooking Pot icon.
 * - Stage 1 (SEDANG_DIPROSES): Yellow (HoneyAmberWarning) with Pot on cooking stove & steam rising effect.
 * - Stage 2 (SELESAI_DIPROSES): Green (MatchaSageSuccess) with Cooking Pot and lid open 45°.
 */
@Composable
private fun OrderPrepareButton(order: OrderRecord, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val stage = when {
        order.processingStage >= 2 || order.isPrepared -> 2
        order.processingStage == 1 -> 1
        else -> 0
    }
    val bgColor = when (stage) {
        2 -> MatchaSageSuccess
        1 -> HoneyAmberWarning
        else -> SoftOatSurface
    }
    val iconColor = when (stage) {
        2 -> ElevatedIvoryCard
        1 -> RichEspressoPrimary
        else -> RichEspressoPrimary
    }
    val borderStroke = when (stage) {
        2 -> BorderStroke(1.dp, MatchaSageSuccess)
        1 -> BorderStroke(1.dp, DeepCaramelGold)
        else -> BorderStroke(1.dp, SubtleSandBorder)
    }
    val desc = when (stage) {
        2 -> "Selesai Diproses (Hijau)"
        1 -> "Sedang Diproses (Kuning)"
        else -> "Belum Diproses"
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = borderStroke,
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("order_prepare_btn_${order.orderNumber}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState = stage,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.85f)) togetherWith fadeOut()
                },
                label = "orderPrepareIcon"
            ) { targetStage ->
                val stageIcon = when (targetStage) {
                    1 -> Icons.Rounded.LocalFireDepartment
                    2 -> Icons.Rounded.RoomService
                    else -> Icons.Rounded.HourglassTop
                }
                Icon(
                    imageVector = stageIcon,
                    contentDescription = desc,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun OrderDoneButton(order: OrderRecord, isDoneEnabled: Boolean, iconOnly: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (!isDoneEnabled) {
        Surface(
            shape = RoundedCornerShape(50),
            color = SoftOatSurface.copy(alpha = 0.5f),
            modifier = modifier
                .height(36.dp)
                .testTag("order_done_btn_${order.orderNumber}")
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = " - ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Bold,
                    color = MutedCocoaText.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        Button(
            onClick = onClick,
            enabled = true,
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = if (iconOnly) 0.dp else 6.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RichEspressoPrimary,
                contentColor = SoftGoldHighlight
            ),
            modifier = modifier
                .height(36.dp)
                .testTag("order_done_btn_${order.orderNumber}")
        ) {
            if (iconOnly) {
                Icon(
                    imageVector = Icons.Rounded.DoneAll,
                    contentDescription = "Selesai",
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "Selesai",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RowScope.OrderActionButtons(
    order: OrderRecord,
    isDoneEnabled: Boolean,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onPrintClick: () -> Unit,
    onPayClick: () -> Unit,
    onPrepareClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    OrderDeleteButton(order = order, modifier = Modifier.weight(1f), onClick = onDeleteClick)
    OrderEditButton(order = order, modifier = Modifier.weight(1f), onClick = onEditClick)
    OrderPrintButton(order = order, modifier = Modifier.weight(1f), onClick = onPrintClick)
    OrderPayButton(order = order, modifier = Modifier.weight(1f), onClick = onPayClick)
    OrderPrepareButton(order = order, modifier = Modifier.weight(1f), onClick = onPrepareClick)
    OrderDoneButton(order = order, isDoneEnabled = isDoneEnabled, modifier = Modifier.weight(1f), onClick = onDoneClick)
}

/**
 * Halaman Order History dengan Segmented Pill Bar & Floating Cards
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderHistoryScreen(
    viewModel: WaffleoViewModel
) {
    val allOrders by viewModel.orders.collectAsState()
    val catalog by viewModel.catalogRecords.collectAsState()
    val customMenuCategories by viewModel.customMenuCategories.collectAsState()
    val customModifierCategories by viewModel.customModifierCategories.collectAsState()
    val session by viewModel.session.collectAsState()
    val uiPrefs by viewModel.sessionUiPreferences.collectAsState()
    val selectedTabIndex = uiPrefs.historySelectedTabIndex
    val selectedPeriod = uiPrefs.historySelectedPeriod
    val customDays = uiPrefs.historyCustomDays
    var showCustomDateDialog by remember { mutableStateOf(false) }
    val customStartMillis = uiPrefs.historyCustomStartMillis
    val customEndMillis = uiPrefs.historyCustomEndMillis
    val isCustomRangeActive = uiPrefs.historyIsCustomRangeActive
    val searchQuery = uiPrefs.historySearchQuery
    var clickedOrderForDetail by remember { mutableStateOf<OrderRecord?>(null) }

    val periodOrders = remember(allOrders, selectedPeriod, customDays, isCustomRangeActive, customStartMillis, customEndMillis) {
        allOrders.filter { ord ->
            (ord.status == "DONE" || ord.status == "CANCELLED" || ord.status == "ERASED") &&
                WaffleoViewModel.matchesPeriodFilterRange(
                    timestamp = com.google.firebase.Timestamp(WaffleoViewModel.resolveOrderTimestampDate(ord)),
                    period = selectedPeriod,
                    customDaysBack = customDays,
                    isCustomRangeActive = isCustomRangeActive,
                    startDateMillis = customStartMillis,
                    endDateMillis = customEndMillis
                )
        }
    }

    val searchMatchedOrders = remember(periodOrders, searchQuery) {
        if (searchQuery.isBlank()) {
            periodOrders
        } else {
            periodOrders.filter { it.matchesSearchQuery(searchQuery) }
        }
    }

    val filteredOrders = remember(searchMatchedOrders, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> searchMatchedOrders.filter { it.status != "ERASED" }
            1 -> searchMatchedOrders.filter { it.status == "DONE" }
            2 -> searchMatchedOrders.filter { it.status == "CANCELLED" }
            3 -> searchMatchedOrders.filter { it.status == "ERASED" }
            else -> searchMatchedOrders
        }.sortedByDescending { WaffleoViewModel.resolveOrderTimestampDate(it).time }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Top Bar: Kolom Pencarian di kiri + Date Period Filter di kanan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kolom Pencarian tepat di sebelah kiri tombol opsi hari
            Surface(
                shape = RoundedCornerShape(50),
                color = ElevatedIvoryCard,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
                    .height(42.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari Pesanan",
                        tint = DeepCaramelGold,
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { q -> viewModel.updateSessionUiPreferences { it.copy(historySearchQuery = q) } },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_search_input"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = RichEspressoPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari pesanan (menu, rasa, #no, kasir, platform)...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedCocoaText.copy(alpha = 0.7f)
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateSessionUiPreferences { it.copy(historySearchQuery = "") } },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus Pencarian",
                                tint = MutedCocoaText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Tombol opsi hari
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WaffleoSingleSelectDropdown(
                    label = "",
                    selectedText = selectedPeriod.label,
                    options = HistoryFilterPeriod.entries,
                    optionLabel = { it.label },
                    onOptionSelected = { nextPeriod ->
                        viewModel.updateSessionUiPreferences {
                            it.copy(
                                historySelectedPeriod = nextPeriod,
                                historyIsCustomRangeActive = if (nextPeriod == HistoryFilterPeriod.CUSTOM) it.historyIsCustomRangeActive else false
                            )
                        }
                        if (nextPeriod == HistoryFilterPeriod.CUSTOM) {
                            showCustomDateDialog = true
                        }
                    },
                    containerColor = ElevatedIvoryCard,
                    modifier = Modifier.width(160.dp),
                    testTagPrefix = "history_period"
                )

                if (selectedPeriod == HistoryFilterPeriod.CUSTOM) {
                    if (isCustomRangeActive && customStartMillis != null && customEndMillis != null) {
                        val fmt = SimpleDateFormat("dd MMM yy", Locale.forLanguageTag("id-ID"))
                        val rangeText = "${fmt.format(Date(customStartMillis!!))} - ${fmt.format(Date(customEndMillis!!))}"
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftGoldHighlight,
                            border = BorderStroke(1.dp, ChampagneGoldAccent),
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { showCustomDateDialog = true }
                                .testTag("history_custom_range_pill")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = rangeText,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                            }
                        }
                    } else {
                        WaffleoSingleSelectDropdown(
                            label = "",
                            selectedText = "$customDays Hari Terakhir",
                            options = listOf(3, 7, 14, 30, 60, 90),
                            optionLabel = { "$it Hari Terakhir" },
                            onOptionSelected = { nextDays ->
                                viewModel.updateSessionUiPreferences {
                                    it.copy(
                                        historyCustomDays = nextDays,
                                        historyIsCustomRangeActive = false
                                    )
                                }
                            },
                            containerColor = ElevatedIvoryCard,
                            modifier = Modifier.width(160.dp),
                            testTagPrefix = "history_custom_days"
                        )
                    }
                }
            }
        }

        if (showCustomDateDialog) {
            CustomDateRangePickerDialog(
                initialStartMillis = customStartMillis,
                initialEndMillis = customEndMillis,
                onDismiss = { showCustomDateDialog = false },
                onConfirm = { startMs, endMs ->
                    viewModel.updateSessionUiPreferences {
                        it.copy(
                            historyCustomStartMillis = startMs,
                            historyCustomEndMillis = endMs,
                            historyIsCustomRangeActive = true
                        )
                    }
                    showCustomDateDialog = false
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Floating Segmented Pill Bar: | All | Done | Cancelled |
        Surface(
            shape = RoundedCornerShape(50),
            color = ElevatedIvoryCard,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val doneCount = searchMatchedOrders.count { it.status == "DONE" }
                val cancelledCount = searchMatchedOrders.count { it.status == "CANCELLED" }
                val erasedCount = searchMatchedOrders.count { it.status == "ERASED" }
                val allCount = doneCount + cancelledCount

                val tabs = listOf(
                    Triple(0, "All — Semua ($allCount)", RichEspressoPrimary),
                    Triple(1, "Done — Selesai ($doneCount)", MatchaSageSuccess),
                    Triple(2, "Cancelled — Batal ($cancelledCount)", TerracottaDanger),
                    Triple(3, "Erased — Dihapus ($erasedCount)", MutedCocoaText)
                )

                tabs.forEach { (index, label, activeColor) ->
                    val isSelected = selectedTabIndex == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { viewModel.updateSessionUiPreferences { it.copy(historySelectedTabIndex = index) } }
                            .testTag(
                                when (index) {
                                    0 -> "history_tab_all"
                                    1 -> "history_tab_done"
                                    2 -> "history_tab_cancelled"
                                    else -> "history_tab_erased"
                                }
                            ),
                        color = if (isSelected) activeColor else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) ElevatedIvoryCard else RichEspressoPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = ElevatedIvoryCard,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = ChampagneGoldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "Tidak ada pesanan cocok dengan '$searchQuery'" else "Tidak ada data pada tab dan periode ini",
                        style = MaterialTheme.typography.titleMedium,
                        color = MutedCocoaText
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp, top = 2.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    val waktuMasuk = remember(order) {
                        val timelineEvents = order.getTimelineEvents()
                        val createEvt = timelineEvents.firstOrNull { it.eventType == "ORDER_CREATED" } ?: timelineEvents.firstOrNull()
                        if (createEvt != null && createEvt.timeText.isNotBlank()) {
                            createEvt.timeText
                        } else {
                            val dt = (order.createdAt ?: order.updatedAt)?.toDate()
                            if (dt != null) {
                                SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt)
                            } else {
                                val epochMs = order.id.removePrefix("ord_").takeWhile { it.isDigit() }.toLongOrNull()
                                if (epochMs != null && epochMs > 1000000000L) {
                                    SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date(epochMs))
                                } else {
                                    SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date())
                                }
                            }
                        }
                    }
                    val waktuSelesai = remember(order) {
                        val timelineEvents = order.getTimelineEvents()
                        val doneEvt = timelineEvents.lastOrNull {
                            it.eventType == "ORDER_COMPLETED" ||
                                it.eventType == "ORDER_DONE" ||
                                it.eventType == "ORDER_STATUS_CHANGED"
                        } ?: timelineEvents.lastOrNull()
                        if (doneEvt != null && doneEvt.timeText.isNotBlank()) {
                            doneEvt.timeText
                        } else {
                            val dt = (order.updatedAt ?: order.createdAt)?.toDate()
                            if (dt != null) {
                                SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt)
                            } else {
                                waktuMasuk
                            }
                        }
                    }
                    val waktuDibatalkan = remember(order) {
                        if (order.cancelledAtText.isNotBlank()) {
                            order.cancelledAtText
                        } else {
                            val cancelEvt = order.getTimelineEvents().lastOrNull { it.eventType == "ORDER_CANCELLED" }
                            cancelEvt?.timeText ?: waktuSelesai
                        }
                    }

                    val lineItems = remember(order.itemsPayload, catalog, customMenuCategories, customModifierCategories) {
                        WaffleoOrderEngine.sortCartLineItems(
                            items = CartLineItem.deserializeList(order.itemsPayload),
                            catalog = catalog,
                            menuCategories = customMenuCategories,
                            modifierCategories = customModifierCategories
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { clickedOrderForDetail = order }
                            .testTag("history_row_${order.orderNumber}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                        border = BorderStroke(1.dp, SoftOatSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Header: [#15 • WFL-015]  [Walk In | Walk In Guest]  [Dibatalkan]  ----  [Rp 78.000]  [(i)]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Surface(shape = RoundedCornerShape(50), color = RichEspressoPrimary) {
                                        val orderNumAnnotated = remember(order, searchQuery) {
                                            highlightTextMatches(
                                                text = "#${order.sequenceIndex} • ${order.displayOrderNumber}",
                                                query = searchQuery,
                                                defaultStyle = SpanStyle(color = SoftGoldHighlight, fontWeight = FontWeight.Bold),
                                                matchStyle = SpanStyle(color = RichEspressoPrimary, background = SoftGoldHighlight, fontWeight = FontWeight.Black)
                                            )
                                        }
                                        Text(
                                            text = orderNumAnnotated,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(shape = RoundedCornerShape(50), color = SoftGoldHighlight) {
                                        val mediaCustAnnotated = remember(order, searchQuery) {
                                            highlightTextMatches(
                                                text = "${order.orderMedia}  |  ${order.customerData.ifBlank { "Guest" }}",
                                                query = searchQuery,
                                                defaultStyle = SpanStyle(color = RichEspressoPrimary, fontWeight = FontWeight.SemiBold),
                                                matchStyle = SpanStyle(color = RichEspressoPrimary, background = ChampagneGoldAccent, fontWeight = FontWeight.Black)
                                            )
                                        }
                                        Text(
                                            text = mediaCustAnnotated,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    if (order.status == "CANCELLED") {
                                        Surface(shape = RoundedCornerShape(50), color = SoftTerracottaBg) {
                                            Text(
                                                text = "Dibatalkan",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TerracottaDanger,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    } else if (order.status == "ERASED") {
                                        Surface(shape = RoundedCornerShape(50), color = SoftTerracottaBg.copy(alpha = 0.7f)) {
                                            Text(
                                                text = "Dihapus (Erased)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TerracottaDanger,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                // Right side: Nominal Price + (i) Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val hasHistoryPromo = order.discountAmount > 0L || (order.subtotalAmount > order.totalAmount)
                                    if (hasHistoryPromo) {
                                        val originalSubtotal = if (order.subtotalAmount > 0L) order.subtotalAmount else (order.totalAmount + order.discountAmount)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = WaffleoViewModel.formatIdr(originalSubtotal),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 13.sp,
                                                    textDecoration = TextDecoration.LineThrough,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MutedCocoaText
                                            )
                                            Text(
                                                text = ">>",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = TerracottaDanger
                                            )
                                            Text(
                                                text = WaffleoViewModel.formatIdr(order.totalAmount),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontSize = 17.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = (-0.3).sp
                                                ),
                                                color = if (order.status == "CANCELLED") TerracottaDanger else RichEspressoPrimary
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = WaffleoViewModel.formatIdr(order.totalAmount),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontSize = 17.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = (-0.3).sp
                                            ),
                                            color = if (order.status == "CANCELLED") TerracottaDanger else RichEspressoPrimary
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = SoftGoldHighlight,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .clickable { clickedOrderForDetail = order }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Rincian Transaksi",
                                                tint = DeepCaramelGold,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Body: Items preview
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftOatSurface.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (lineItems.isNotEmpty()) {
                                        lineItems.forEach { item ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(shape = RoundedCornerShape(6.dp), color = RichEspressoPrimary) {
                                                    Text(
                                                        text = "${item.qty}x",
                                                        color = SoftGoldHighlight,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    val annotatedTitle = remember(item, searchQuery) {
                                                        item.buildAnnotatedCartTitleWithHighlight(query = searchQuery)
                                                    }
                                                    Text(
                                                        text = annotatedTitle,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 12.5.sp,
                                                            lineHeight = 16.5.sp
                                                        ),
                                                        color = RichEspressoPrimary
                                                    )
                                                    FlowRow(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 2.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        if (item.addOnNames.isNotEmpty()) {
                                                            item.addOnNames.forEach { addon ->
                                                                val isCheese = addon.contains("keju dalam waffle", ignoreCase = true)
                                                                val addOnLabel = if (addon.startsWith("+")) addon else "+$addon"
                                                                val highlightedAddon = remember(addOnLabel, searchQuery) {
                                                                    highlightTextMatches(
                                                                        text = addOnLabel,
                                                                        query = searchQuery,
                                                                        defaultStyle = SpanStyle(
                                                                            color = if (isCheese) DeepCaramelGold else MatchaSageSuccess,
                                                                            fontWeight = if (isCheese) FontWeight.ExtraBold else FontWeight.SemiBold
                                                                        ),
                                                                        matchStyle = SpanStyle(
                                                                            color = RichEspressoPrimary,
                                                                            background = SoftGoldHighlight,
                                                                            fontWeight = FontWeight.Black
                                                                        )
                                                                    )
                                                                }
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = if (isCheese) SoftAmberBg else SoftSageBg,
                                                                    border = if (isCheese) BorderStroke(0.5.dp, HoneyAmberWarning) else null
                                                                ) {
                                                                    Text(
                                                                        text = highlightedAddon,
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        if (item.category == "Drinks") {
                                                            val drinksInfo = "Ice: ${item.icedLevel} • Sugar: ${item.sweetnessLevel}"
                                                            val highlightedDrinks = remember(drinksInfo, searchQuery) {
                                                                highlightTextMatches(
                                                                    text = drinksInfo,
                                                                    query = searchQuery,
                                                                    defaultStyle = SpanStyle(color = RichEspressoPrimary, fontWeight = FontWeight.Normal),
                                                                    matchStyle = SpanStyle(color = RichEspressoPrimary, background = SoftGoldHighlight, fontWeight = FontWeight.Black)
                                                                )
                                                            }
                                                            Surface(shape = RoundedCornerShape(4.dp), color = SoftGoldHighlight.copy(alpha = 0.7f)) {
                                                                Text(
                                                                    text = highlightedDrinks,
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        order.itemsSummary.lines().filter { it.isNotBlank() }.forEach { line ->
                                            val annotatedLine = remember(line, searchQuery) {
                                                highlightTextMatches(
                                                    text = line,
                                                    query = searchQuery,
                                                    defaultStyle = SpanStyle(color = RichEspressoPrimary, fontWeight = FontWeight.Medium),
                                                    matchStyle = SpanStyle(color = RichEspressoPrimary, background = SoftGoldHighlight, fontWeight = FontWeight.Black)
                                                )
                                            }
                                            Text(text = annotatedLine, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }

                                    if (order.notes.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SoftAmberBg,
                                            border = BorderStroke(0.5.dp, HoneyAmberWarning.copy(alpha = 0.6f)),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                Icon(Icons.Default.Info, contentDescription = null, tint = DeepCaramelGold, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Catatan: ${order.notes}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DeepCaramelGold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Footer: Kasir, status pembayaran, jam masuk, jam selesai
                            val timeLabel = if (order.status == "CANCELLED") {
                                "Masuk: $waktuMasuk • Batal: $waktuDibatalkan"
                            } else {
                                "Masuk: $waktuMasuk • Selesai: $waktuSelesai"
                            }
                            val payDesc = if (order.isPaid) order.paymentMethod else "Belum Lunas"
                            val footerText = "Kasir: ${order.createdBy} • $payDesc • $timeLabel"
                            val highlightedFooter = remember(footerText, searchQuery) {
                                highlightTextMatches(
                                    text = footerText,
                                    query = searchQuery,
                                    defaultStyle = SpanStyle(color = MutedCocoaText.copy(alpha = 0.85f)),
                                    matchStyle = SpanStyle(color = RichEspressoPrimary, background = SoftGoldHighlight, fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = highlightedFooter,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Complete Order Detail Modal when clicked
    clickedOrderForDetail?.let { ord ->
        var showEraseConfirmDialog by remember { mutableStateOf(false) }
        var showPurgeConfirmDialog by remember { mutableStateOf(false) }
        var showThermalReceiptReprint by remember { mutableStateOf(false) }
        var eraseReasonInput by remember { mutableStateOf("Dihapus oleh Owner") }
        val canEraseOrder = (session.role == StaffRole.OWNER || session.role == StaffRole.DEVELOPER) && ord.status != "ERASED"
        val canPurgeOrderPermanently = (session.role == StaffRole.OWNER || session.role == StaffRole.DEVELOPER) && ord.status == "ERASED"

        val dtText = remember(ord.updatedAt, ord.createdAt) {
            val dt = (ord.updatedAt ?: ord.createdAt)?.toDate()
            if (dt != null) SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt) else "-"
        }
        Dialog(
            onDismissRequest = { clickedOrderForDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.92f)
                    .padding(8.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                ) {
                    // Header Bar with Title and Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Detail Lengkap Pesanan ${ord.displayOrderNumber}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary
                            )
                            Text(
                                text = "Audit trail riwayat pembuatan, perubahan menu, pembayaran bertahap, dan status pesanan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = SoftOatSurface,
                            modifier = Modifier.size(38.dp)
                        ) {
                            IconButton(onClick = { clickedOrderForDetail = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable Audit Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // SECTION 1: PROFIL & IDENTITAS PESANAN
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SoftOatSurface.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, SoftOatSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Profil & Identitas Pesanan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OrderDetailMetaItem(label = "No. Pesanan", value = ord.displayOrderNumber)
                                        OrderDetailMetaItem(label = "Media Pesanan", value = ord.orderMedia)
                                        OrderDetailMetaItem(label = "Customer Data", value = ord.customerData.ifBlank { "Pelanggan Di tempat" })
                                        OrderDetailMetaItem(label = "Cabang", value = "Cabang ${viewModel.getBranchDisplayName(ord.branchId)}")
                                    }
                                    Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OrderDetailMetaItem(label = "Diinput Oleh", value = ord.createdBy)
                                        OrderDetailMetaItem(label = "Waktu Masuk", value = dtText)
                                        if (ord.status == "CANCELLED") {
                                            OrderDetailMetaItem(label = "Status Akhir", value = "Dibatalkan (${ord.cancelledAtText})", isDanger = true)
                                        } else if (ord.status == "ERASED") {
                                            OrderDetailMetaItem(label = "Status Akhir", value = "Dihapus dari Penjualan (${ord.cancelledAtText})", isDanger = true)
                                        } else {
                                            OrderDetailMetaItem(label = "Status Akhir", value = "Selesai (Done)", isSuccess = true)
                                        }
                                    }
                                }
                            }
                        }

                        // SECTION 2: RINCIAN ITEM PESANAN TERKINI
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SoftOatSurface.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, SoftOatSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Rincian Item Pesanan Terkini",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ElevatedIvoryCard,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val lineItems = remember(ord.itemsPayload, catalog, customMenuCategories, customModifierCategories) {
                                            WaffleoOrderEngine.sortCartLineItems(
                                                items = CartLineItem.deserializeList(ord.itemsPayload),
                                                catalog = catalog,
                                                menuCategories = customMenuCategories,
                                                modifierCategories = customModifierCategories
                                            )
                                        }
                                        if (lineItems.isNotEmpty()) {
                                            lineItems.forEachIndexed { idx, item ->
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.Top
                                                    ) {
                                                        Text(
                                                            text = "${item.qty}x ${item.formattedTitle()}",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = RichEspressoPrimary,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Text(
                                                            text = WaffleoViewModel.formatIdr(item.lineTotalFor(OrderMedia.fromLabel(ord.orderMedia))),
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = DeepCaramelGold
                                                        )
                                                    }
                                                    if (item.addOnNames.isNotEmpty()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        val isEggWaffle = item.category.equals("Egg Waffle", ignoreCase = true)
                                                        val label = if (isEggWaffle) "Add on : " else "Pilihan / Varian : "
                                                        Text(
                                                            text = "$label${item.addOnNames.joinToString(", ")}",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MatchaSageSuccess
                                                        )
                                                    }
                                                    if (item.category.equals("Drinks", ignoreCase = true) && (item.sweetnessLevel != "Normal" || item.icedLevel != "Normal")) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "Varian : Ice ${item.icedLevel} • Sugar ${item.sweetnessLevel}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MutedCocoaText
                                                        )
                                                    }
                                                }
                                                if (idx < lineItems.lastIndex) {
                                                    HorizontalDivider(color = SoftOatSurface, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 4.dp))
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = ord.itemsSummary.ifBlank { "Tidak ada rincian item" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = RichEspressoPrimary
                                            )
                                        }

                                        if (ord.notes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Notes: ${ord.notes}", style = MaterialTheme.typography.bodySmall, color = DeepCaramelGold, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val isOrdCancelled = ord.status == "CANCELLED"
                                val isOrdPlatform = ord.orderMedia in listOf("GrabFood", "GoFood", "ShopeeFood")
                                val ordPlatformFee = if (isOrdCancelled) 0L else viewModel.calculateOrderPlatformFee(ord)
                                val ordNetBca = if (isOrdCancelled) 0L else viewModel.calculateOrderNetAmount(ord)

                                val hasOrdPromo = ord.discountAmount > 0L || (ord.subtotalAmount > ord.totalAmount)
                                if (hasOrdPromo) {
                                    val originalSubtotal = if (ord.subtotalAmount > 0L) ord.subtotalAmount else (ord.totalAmount + ord.discountAmount)
                                    val promoDiscount = if (ord.discountAmount > 0L) ord.discountAmount else (originalSubtotal - ord.totalAmount)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Total Harga Pesanan (Subtotal):", style = MaterialTheme.typography.bodyMedium, color = MutedCocoaText)
                                        Text(WaffleoViewModel.formatIdr(originalSubtotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val promoTag = if (ord.promoCode.isNotBlank()) " (${ord.promoCode})" else ""
                                        Text("Potongan Promo$promoTag:", style = MaterialTheme.typography.bodyMedium, color = TerracottaDanger)
                                        Text("-${WaffleoViewModel.formatIdr(promoDiscount)}", style = MaterialTheme.typography.bodyMedium, color = TerracottaDanger, fontWeight = FontWeight.Bold)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = SoftOatSurface)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isOrdPlatform) "Total Harga ${ord.orderMedia}:" else "Total Akhir Tagihan:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = WaffleoViewModel.formatIdr(ord.totalAmount),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOrdCancelled) TerracottaDanger else RichEspressoPrimary
                                    )
                                }

                                if (isOrdPlatform) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (isOrdCancelled) {
                                        Text(
                                            text = "Status: Pesanan Dibatalkan • Uang Masuk Rekening BCA: Rp 0",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaDanger
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Potongan Komisi & Order ${ord.orderMedia}:", style = MaterialTheme.typography.bodySmall, color = TerracottaDanger)
                                            Text("-${WaffleoViewModel.formatIdr(ordPlatformFee)}", style = MaterialTheme.typography.bodySmall, color = TerracottaDanger, fontWeight = FontWeight.SemiBold)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Uang Masuk Bersih Rekening BCA:", style = MaterialTheme.typography.titleSmall, color = MatchaSageSuccess, fontWeight = FontWeight.Bold)
                                            Text(WaffleoViewModel.formatIdr(ordNetBca), style = MaterialTheme.typography.titleSmall, color = MatchaSageSuccess, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // SECTION 3: BUKU KAS & RIWAYAT PEMBAYARAN (MULTI-PAYMENT LEDGER)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SoftOatSurface.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, SoftOatSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val isOrdCancelled = ord.status == "CANCELLED"
                                val paymentBadgeText = when {
                                    isOrdCancelled -> "DIBATALKAN"
                                    ord.isPaid -> "LUNAS (${WaffleoViewModel.formatIdr(ord.paidAmount)})"
                                    ord.shortfallAmount > 0L -> "BELUM LUNAS (Kurang: ${WaffleoViewModel.formatIdr(ord.shortfallAmount)})"
                                    else -> "BELUM LUNAS (${WaffleoViewModel.formatIdr(ord.totalAmount)})"
                                }
                                val paymentBadgeColor = if (isOrdCancelled) TerracottaDanger else (if (ord.isPaid) MatchaSageSuccess else TerracottaDanger)
                                val paymentBadgeBg = if (isOrdCancelled) SoftTerracottaBg else (if (ord.isPaid) SoftSageBg else SoftTerracottaBg)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Buku Kas & Riwayat Pembayaran (Payment Ledger)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = RichEspressoPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = paymentBadgeBg
                                    ) {
                                        Text(
                                            text = paymentBadgeText,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = paymentBadgeColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val ledgerEntries = ord.getPaymentLedger()
                                if (ledgerEntries.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ledgerEntries.forEachIndexed { index, entry ->
                                            PaymentLedgerRowCard(index = index + 1, entry = entry)
                                        }
                                    }
                                } else {
                                    // Fallback for older orders without structured ledger
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ElevatedIvoryCard,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Pembayaran Tercatat", style = MaterialTheme.typography.labelMedium, color = MutedCocoaText)
                                                Text(ord.paymentMethod, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                Text("Waktu: $dtText • Kasir: ${ord.createdBy}", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                                            }
                                            Text(
                                                text = WaffleoViewModel.formatIdr(ord.paidAmount),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MatchaSageSuccess
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // SECTION 4: KRONOLOGI LENGKAP PERJALANAN PESANAN (AUDIT TRAIL TIMELINE)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SoftOatSurface.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, SoftOatSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Jejak Kronologi Lengkap Pesanan (Audit Timeline)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                                Text(
                                    text = "Rekaman kejadian dari pesanan masuk, perubahan item, konfirmasi kasir, hingga selesai",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedCocoaText
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val timelineEvents = ord.getTimelineEvents()
                                val effectiveEvents = if (timelineEvents.isNotEmpty()) {
                                    timelineEvents
                                } else {
                                    // Reconstruct synthetic events for older orders
                                    listOfNotNull(
                                        OrderTimelineEvent(
                                            eventId = "evt_seed_1",
                                            timeText = dtText,
                                            eventType = "ORDER_CREATED",
                                            actorUsername = ord.createdBy,
                                            title = "Pesanan Dibuat",
                                            description = "Pesanan ${ord.displayOrderNumber} masuk via ${ord.orderMedia} (${ord.customerData}). Total: ${WaffleoViewModel.formatIdr(ord.totalAmount)}"
                                        ),
                                        if (ord.paidAmount > 0L) {
                                            OrderTimelineEvent(
                                                eventId = "evt_seed_2",
                                                timeText = dtText,
                                                eventType = "PAYMENT_EVENT",
                                                actorUsername = ord.createdBy,
                                                title = "Pembayaran Diterima",
                                                description = "Diterima ${WaffleoViewModel.formatIdr(ord.paidAmount)} via ${ord.paymentMethod}."
                                            )
                                        } else null,
                                        if (ord.status == "DONE") {
                                            OrderTimelineEvent(
                                                eventId = "evt_seed_3",
                                                timeText = dtText,
                                                eventType = "ORDER_COMPLETED",
                                                actorUsername = ord.createdBy,
                                                title = "Pesanan Selesai (Done)",
                                                description = "Pesanan diselesaikan dan masuk ke Order History."
                                            )
                                        } else if (ord.status == "CANCELLED") {
                                            OrderTimelineEvent(
                                                eventId = "evt_seed_3",
                                                timeText = ord.cancelledAtText.ifBlank { dtText },
                                                eventType = "ORDER_CANCELLED",
                                                actorUsername = ord.cancelledBy,
                                                title = "Pesanan Dibatalkan",
                                                description = "Alasan: ${ord.cancelReason}"
                                            )
                                        } else null
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    effectiveEvents.forEachIndexed { idx, ev ->
                                        OrderTimelineEventCard(index = idx + 1, event = ev, isLast = idx == effectiveEvents.size - 1)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Footer Action Bar: "Hapus Pesanan" on the far left (for Owner/Developer), "Tutup Detail" on the right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canEraseOrder || canPurgeOrderPermanently) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (canEraseOrder) {
                                    OutlinedButton(
                                        onClick = { showEraseConfirmDialog = true },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = TerracottaDanger
                                        ),
                                        border = BorderStroke(1.5.dp, TerracottaDanger.copy(alpha = 0.85f)),
                                        modifier = Modifier
                                            .height(46.dp)
                                            .testTag("order_detail_erase_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Hapus Pesanan",
                                            modifier = Modifier.size(18.dp),
                                            tint = TerracottaDanger
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Hapus Pesanan",
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaDanger
                                        )
                                    }
                                }

                                if (canPurgeOrderPermanently) {
                                    OutlinedButton(
                                        onClick = { showPurgeConfirmDialog = true },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = TerracottaDanger
                                        ),
                                        border = BorderStroke(1.5.dp, TerracottaDanger.copy(alpha = 0.85f)),
                                        modifier = Modifier
                                            .height(46.dp)
                                            .testTag("order_detail_purge_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Hapus Permanen",
                                            modifier = Modifier.size(18.dp),
                                            tint = TerracottaDanger
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Hapus Permanen",
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaDanger
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { showThermalReceiptReprint = true },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.2.dp, RichEspressoPrimary.copy(alpha = 0.7f)),
                                modifier = Modifier
                                    .height(46.dp)
                                    .testTag("order_detail_reprint_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = "Cetak Struk",
                                    modifier = Modifier.size(18.dp),
                                    tint = RichEspressoPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cetak Struk",
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                            }

                            Button(
                                onClick = { clickedOrderForDetail = null },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = RichEspressoPrimary),
                                modifier = Modifier
                                    .height(46.dp)
                                    .width(160.dp)
                                    .testTag("order_detail_close_button")
                            ) {
                                Text("Tutup Detail")
                            }
                        }
                    }
                }
            }

            if (showThermalReceiptReprint) {
                ThermalReceiptDialog(
                    order = ord,
                    onDismiss = { showThermalReceiptReprint = false },
                    onPrinted = {
                        viewModel.showBanner("Struk pesanan ${ord.displayOrderNumber} berhasil dicetak ulang.")
                    },
                    catalog = catalog,
                    menuCategories = customMenuCategories,
                    modifierCategories = customModifierCategories
                )
            }

            if (showEraseConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showEraseConfirmDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = TerracottaDanger,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Hapus Pesanan ${ord.displayOrderNumber}?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Apakah Anda yakin ingin menghapus pesanan ${ord.displayOrderNumber} senilai ${WaffleoViewModel.formatIdr(ord.totalAmount)}?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RichEspressoPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftTerracottaBg,
                                border = BorderStroke(1.dp, TerracottaDanger.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Dampak Sinkronisasi Otomatis:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaDanger
                                    )
                                    Text(
                                        text = "• Pesanan dipindahkan ke tab 'Erased — Dihapus' untuk audit trail.\n• Rekapan uang masuk di Money Manager & Dashboard otomatis dikurangi.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RichEspressoPrimary
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = eraseReasonInput,
                                onValueChange = { eraseReasonInput = capitalizeSentenceStart(it) },
                                label = { Text("Alasan Penghapusan (Catatan Audit)") },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("erase_order_reason_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.eraseOrderAndRevertMetrics(
                                    order = ord,
                                    reason = eraseReasonInput.ifBlank { "Dihapus oleh Owner" }
                                )
                                showEraseConfirmDialog = false
                                clickedOrderForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaDanger),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("confirm_erase_order_action_button")
                        ) {
                            Text("Ya, Hapus Pesanan", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showEraseConfirmDialog = false },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Batal")
                        }
                    },
                    containerColor = ElevatedIvoryCard,
                    shape = RoundedCornerShape(22.dp)
                )
            }

            if (showPurgeConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showPurgeConfirmDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = TerracottaDanger,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Hapus Permanen Pesanan ${ord.displayOrderNumber}?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "Tindakan ini akan menghapus dokumen pesanan ${ord.displayOrderNumber} secara permanen dari Cloud Firestore dan penyimpanan lokal. Pesanan ghaib/sampah ini tidak akan pernah muncul kembali.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RichEspressoPrimary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteOrderRecord(ord)
                                showPurgeConfirmDialog = false
                                clickedOrderForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaDanger),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("confirm_purge_order_action_button")
                        ) {
                            Text("Ya, Hapus Permanen", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showPurgeConfirmDialog = false },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Batal")
                        }
                    },
                    containerColor = ElevatedIvoryCard,
                    shape = RoundedCornerShape(22.dp)
                )
            }
        }
    }
}

private fun OrderRecord.matchesSearchQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim().lowercase(Locale.ROOT)

    if (orderNumber.lowercase(Locale.ROOT).contains(q)) return true
    if ("#$sequenceIndex".contains(q) || sequenceIndex.toString() == q) return true
    if (orderMedia.lowercase(Locale.ROOT).contains(q)) return true
    if (customerData.lowercase(Locale.ROOT).contains(q)) return true
    if (createdBy.lowercase(Locale.ROOT).contains(q)) return true
    if (paymentMethod.lowercase(Locale.ROOT).contains(q)) return true
    if (status.lowercase(Locale.ROOT).contains(q)) return true
    if (notes.lowercase(Locale.ROOT).contains(q)) return true
    if (itemsSummary.lowercase(Locale.ROOT).contains(q)) return true

    val lineItems = parsedLineItems
    for (item in lineItems) {
        if (item.primaryItemName.lowercase(Locale.ROOT).contains(q)) return true
        if (item.secondaryItemName.lowercase(Locale.ROOT).contains(q)) return true
        if (item.flavourName.lowercase(Locale.ROOT).contains(q)) return true
        if (item.addOnNames.any { it.lowercase(Locale.ROOT).contains(q) }) return true
        if (item.sweetnessLevel.lowercase(Locale.ROOT).contains(q)) return true
        if (item.icedLevel.lowercase(Locale.ROOT).contains(q)) return true
    }
    return false
}

private fun highlightTextMatches(
    text: String,
    query: String,
    defaultStyle: SpanStyle = SpanStyle(color = RichEspressoPrimary, fontWeight = FontWeight.Normal),
    matchStyle: SpanStyle = SpanStyle(
        fontWeight = FontWeight.Black,
        color = RichEspressoPrimary,
        background = SoftGoldHighlight
    )
): AnnotatedString {
    val q = query.trim().lowercase(Locale.ROOT)
    if (q.isBlank() || !text.lowercase(Locale.ROOT).contains(q)) {
        return buildAnnotatedString {
            withStyle(defaultStyle) { append(text) }
        }
    }
    return buildAnnotatedString {
        var start = 0
        val lowerText = text.lowercase(Locale.ROOT)
        while (start < text.length) {
            val idx = lowerText.indexOf(q, start)
            if (idx == -1) {
                withStyle(defaultStyle) {
                    append(text.substring(start))
                }
                break
            }
            if (idx > start) {
                withStyle(defaultStyle) {
                    append(text.substring(start, idx))
                }
            }
            val matchEnd = idx + q.length
            withStyle(matchStyle) {
                append(text.substring(idx, matchEnd))
            }
            start = matchEnd
        }
    }
}

private fun CartLineItem.buildAnnotatedCartTitleWithHighlight(
    query: String = "",
    baseColor: Color = RichEspressoPrimary,
    baseWeight: FontWeight = FontWeight.SemiBold
): AnnotatedString {
    val original = buildAnnotatedCartTitle(baseColor, baseWeight)
    val q = query.trim().lowercase(Locale.ROOT)
    if (q.isBlank() || !original.text.lowercase(Locale.ROOT).contains(q)) {
        return original
    }
    val lowerText = original.text.lowercase(Locale.ROOT)
    return buildAnnotatedString {
        append(original)
        var start = 0
        while (start < original.length) {
            val idx = lowerText.indexOf(q, start)
            if (idx == -1) break
            val end = idx + q.length
            addStyle(
                SpanStyle(
                    fontWeight = FontWeight.Black,
                    background = SoftGoldHighlight
                ),
                idx,
                end
            )
            start = end
        }
    }
}

@Composable
private fun OrderDetailMetaItem(
    label: String,
    value: String,
    isDanger: Boolean = false,
    isSuccess: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MutedCocoaText,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isDanger || isSuccess) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isDanger -> TerracottaDanger
                isSuccess -> MatchaSageSuccess
                else -> RichEspressoPrimary
            },
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun PaymentLedgerRowCard(
    index: Int,
    entry: OrderPaymentLedgerEntry
) {
    val isRefund = entry.paymentType == "CHANGE_REFUND_RETURN"
    val isShortfall = entry.paymentType == "SHORTFALL_SETTLEMENT"

    val badgeBg = when {
        isRefund -> SoftTerracottaBg
        isShortfall -> SoftGoldHighlight
        else -> SoftSageBg
    }
    val badgeColor = when {
        isRefund -> TerracottaDanger
        isShortfall -> DeepCaramelGold
        else -> MatchaSageSuccess
    }
    val badgeLabel = when {
        isRefund -> "Kembalian Tunai Customer"
        isShortfall -> "Pelunasan Kekurangan (#$index)"
        else -> "Pembayaran (#$index)"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ElevatedIvoryCard,
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        color = badgeBg
                    ) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = entry.timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedCocoaText
                    )
                }

                Text(
                    text = if (isRefund) "-${WaffleoViewModel.formatIdr(entry.amount)}" else "+${WaffleoViewModel.formatIdr(entry.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRefund) TerracottaDanger else MatchaSageSuccess
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Metode: ${entry.paymentMethod}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = RichEspressoPrimary
                )
                Text(
                    text = "Kasir: ${entry.cashierUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedCocoaText
                )
            }

            if (entry.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catatan: ${entry.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedCocoaText
                )
            }
        }
    }
}

@Composable
private fun OrderTimelineEventCard(
    index: Int,
    event: OrderTimelineEvent,
    isLast: Boolean
) {
    val (icon, color) = when (event.eventType) {
        "ORDER_CREATED" -> Icons.Default.Receipt to DeepCaramelGold
        "PAYMENT_EVENT" -> Icons.Default.Payment to MatchaSageSuccess
        "ORDER_EDITED" -> Icons.Default.Edit to DeepCaramelGold
        "ORDER_PREPARED" -> Icons.Default.Restaurant to HoneyAmberWarning
        "ORDER_COMPLETED" -> Icons.Default.CheckCircle to MatchaSageSuccess
        "ORDER_CANCELLED" -> Icons.Default.Cancel to TerracottaDanger
        else -> Icons.Default.Info to MutedCocoaText
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Bullet Icon & vertical connector line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, color),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(1.dp))
                ) {
                    Surface(color = MutedCocoaText.copy(alpha = 0.25f), modifier = Modifier.fillMaxSize()) {}
                }
            }
        }

        // Event Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = ElevatedIvoryCard,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary
                    )
                    Text(
                        text = event.timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedCocoaText
                    )
                }

                if (event.actorUsername.isNotBlank()) {
                    Text(
                        text = "Pelaksana: ${event.actorUsername}${if (event.actorRole.isNotBlank()) " (${event.actorRole})" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedCocoaText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (event.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = com.example.data.model.sanitizeBranchIdsInText(event.description),
                        style = MaterialTheme.typography.bodySmall,
                        color = RichEspressoPrimary
                    )
                }
            }
        }
    }
}
