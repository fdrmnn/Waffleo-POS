package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.OrderPaymentLedgerEntry
import kotlin.math.roundToInt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.data.model.CartLineItem
import com.example.data.model.CatalogRecord
import com.example.data.model.OrderMedia
import com.example.data.model.OrderRecord
import com.example.data.model.WaffleoSeedData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.AdonanPalette
import com.example.ui.theme.ChampagneGoldAccent
import com.example.ui.theme.DeepCaramelGold
import com.example.ui.theme.ElevatedIvoryCard
import com.example.ui.theme.HoneyAmberWarning
import com.example.ui.theme.HoneyCaramelAccent
import com.example.ui.theme.MatchaSageSuccess
import com.example.ui.theme.MutedCocoaText
import com.example.ui.theme.PastelBerryBg
import com.example.ui.theme.PastelCheeseBg
import com.example.ui.theme.PastelChocoBg
import com.example.ui.theme.PastelCoffeeBg
import com.example.ui.theme.PastelMatchaBg
import com.example.ui.theme.PastelOreoBg
import com.example.ui.theme.RichEspressoPrimary
import com.example.ui.theme.SoftAmberBg
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SoftSageBg
import com.example.ui.theme.SoftTerracottaBg
import com.example.ui.theme.SubtleSandBorder
import com.example.ui.theme.TerracottaDanger
import com.example.ui.viewmodel.WaffleoViewModel

import androidx.compose.foundation.BorderStroke

/**
 * Helper returning soft pastel background and accent colors for each product's flavor profile.
 */
fun resolveProductVisualPalette(itemName: String, isWaffle: Boolean): Triple<Color, Color, Color> {
    val lower = itemName.lowercase()
    return when {
        lower.contains("polos") ->
            Triple(PastelCheeseBg, Color(0xFFE59E46), Color(0xFFFFE4B5))
        lower.contains("matcha") || lower.contains("green") ->
            Triple(PastelMatchaBg, Color(0xFF4F8A5B), Color(0xFF9AD0A2))
        lower.contains("taro") ->
            Triple(Color(0xFFF3EBFA), Color(0xFF7E57C2), Color(0xFFC5A8EB))
        lower.contains("blueberry") ->
            Triple(Color(0xFFEBF0FA), Color(0xFF4353A3), Color(0xFF9FA8DA))
        lower.contains("straw") || lower.contains("berry") || lower.contains("pink") ->
            Triple(PastelBerryBg, Color(0xFFD95B72), Color(0xFFF7A8B8))
        lower.contains("cheese") || lower.contains("cheddar") || lower.contains("butter") || lower.contains("caramel") ->
            Triple(PastelCheeseBg, Color(0xFFD99826), Color(0xFFFAD375))
        lower.contains("oreo") || lower.contains("cookies") || lower.contains("charcoal") ->
            Triple(PastelOreoBg, Color(0xFF332D2B), Color(0xFF8C827D))
        lower.contains("tiramisu") || lower.contains("cappuccino") || lower.contains("aren") || lower.contains("latte") ->
            Triple(PastelCoffeeBg, Color(0xFF7A4928), Color(0xFFC9976B))
        lower.contains("ovomaltine") || lower.contains("hazelnut") ->
            Triple(PastelChocoBg, Color(0xFF6B3418), Color(0xFFC8834B))
        else -> {
            if (isWaffle) {
                Triple(PastelChocoBg, Color(0xFF593423), Color(0xFFB87D59))
            } else {
                Triple(PastelCoffeeBg, Color(0xFF6B4226), Color(0xFFD4A373))
            }
        }
    }
}

/**
 * Rich, multi-layered custom vector Canvas illustration for Egg Bubble Waffles and Café Drinks.
 * Gives every menu card a modern Web/Dribbble POS food illustration look.
 */
@Composable
fun WaffleoProductIllustration(
    itemName: String,
    isWaffle: Boolean,
    modifier: Modifier = Modifier,
    imageUrl: String? = null
) {
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = itemName,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        val (bgPastel, primaryFlavorColor, secondaryFlavorColor) = remember(itemName, isWaffle) {
            resolveProductVisualPalette(itemName, isWaffle)
        }

        Box(
            modifier = modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(bgPastel, bgPastel.copy(alpha = 0.55f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.52f
            val minDim = minOf(w, h)

            // Soft glowing circular halo behind the product
            drawCircle(
                color = Color.White.copy(alpha = 0.72f),
                radius = minDim * 0.42f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = secondaryFlavorColor.copy(alpha = 0.22f),
                radius = minDim * 0.34f,
                center = Offset(cx, cy)
            )

            if (isWaffle) {
                // 1. Egg Bubble Waffle Golden Honeycomb Puff Cluster (Upper Cone)
                val waffleGold = Color(0xFFE59E46)
                val waffleDeep = Color(0xFFC27824)
                val waffleHighlight = Color(0xFFFFE4B5)
                val bubbleRadius = minDim * 0.088f

                // Row of honeycomb bubbles arranged in a fan
                val bubbleOffsets = listOf(
                    Offset(cx - bubbleRadius * 1.6f, cy - minDim * 0.14f),
                    Offset(cx - bubbleRadius * 0.55f, cy - minDim * 0.19f),
                    Offset(cx + bubbleRadius * 0.55f, cy - minDim * 0.19f),
                    Offset(cx + bubbleRadius * 1.6f, cy - minDim * 0.14f),
                    Offset(cx - bubbleRadius * 1.15f, cy - minDim * 0.03f),
                    Offset(cx, cy - minDim * 0.06f),
                    Offset(cx + bubbleRadius * 1.15f, cy - minDim * 0.03f),
                    Offset(cx - bubbleRadius * 0.65f, cy + minDim * 0.07f),
                    Offset(cx + bubbleRadius * 0.65f, cy + minDim * 0.07f)
                )

                bubbleOffsets.forEach { pos ->
                    drawCircle(
                        color = waffleDeep,
                        radius = bubbleRadius,
                        center = Offset(pos.x, pos.y + 2f)
                    )
                    drawCircle(
                        color = waffleGold,
                        radius = bubbleRadius * 0.94f,
                        center = pos
                    )
                    drawCircle(
                        color = waffleHighlight.copy(alpha = 0.75f),
                        radius = bubbleRadius * 0.34f,
                        center = Offset(pos.x - bubbleRadius * 0.25f, pos.y - bubbleRadius * 0.25f)
                    )
                }

                // 2. Whipped Cream & Flavor Topping Crown in the center
                drawCircle(
                    color = Color(0xFFFFFBF5),
                    radius = minDim * 0.14f,
                    center = Offset(cx, cy - minDim * 0.08f)
                )
                drawCircle(
                    color = primaryFlavorColor,
                    radius = minDim * 0.095f,
                    center = Offset(cx, cy - minDim * 0.09f)
                )

                // Topping sprinkles / chunks across the bubbles
                val toppingSpots = listOf(
                    Offset(cx - bubbleRadius * 1.3f, cy - minDim * 0.15f),
                    Offset(cx + bubbleRadius * 1.2f, cy - minDim * 0.14f),
                    Offset(cx - bubbleRadius * 0.4f, cy - minDim * 0.18f),
                    Offset(cx + bubbleRadius * 0.5f, cy - minDim * 0.04f),
                    Offset(cx - bubbleRadius * 0.8f, cy - minDim * 0.02f)
                )
                toppingSpots.forEach { spot ->
                    drawCircle(
                        color = primaryFlavorColor,
                        radius = minDim * 0.035f,
                        center = spot
                    )
                    drawCircle(
                        color = secondaryFlavorColor,
                        radius = minDim * 0.018f,
                        center = Offset(spot.x + 2f, spot.y - 1.5f)
                    )
                }

                // Glossy sauce drizzle wave across the waffle
                val drizzlePath = Path().apply {
                    moveTo(cx - minDim * 0.22f, cy - minDim * 0.09f)
                    quadraticTo(cx - minDim * 0.10f, cy - minDim * 0.18f, cx, cy - minDim * 0.07f)
                    quadraticTo(cx + minDim * 0.10f, cy + minDim * 0.03f, cx + minDim * 0.22f, cy - minDim * 0.10f)
                }
                drawPath(
                    path = drizzlePath,
                    color = primaryFlavorColor,
                    style = Stroke(width = minDim * 0.035f, cap = StrokeCap.Round)
                )

                // 3. Artisanal Paper Cone Holder at the bottom
                val conePath = Path().apply {
                    moveTo(cx - minDim * 0.22f, cy + minDim * 0.02f)
                    lineTo(cx + minDim * 0.22f, cy + minDim * 0.02f)
                    lineTo(cx + minDim * 0.08f, cy + minDim * 0.36f)
                    lineTo(cx - minDim * 0.08f, cy + minDim * 0.36f)
                    close()
                }
                drawPath(path = conePath, color = Color(0xFFFDF8F0))
                drawPath(
                    path = conePath,
                    color = Color(0xFFE3D5C3),
                    style = Stroke(width = 2f)
                )

                // Gold seal emblem on the paper cone
                drawCircle(
                    color = ChampagneGoldAccent,
                    radius = minDim * 0.055f,
                    center = Offset(cx, cy + minDim * 0.18f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = minDim * 0.025f,
                    center = Offset(cx, cy + minDim * 0.18f)
                )
            } else {
                // CAFÉ DRINK ILLUSTRATION (Iced Layered Cup with Dome Lid & Straw)
                val cupWidth = minDim * 0.36f
                val cupHeight = minDim * 0.46f
                val cupLeft = cx - cupWidth / 2f
                val cupTop = cy - cupHeight * 0.35f

                // Diagonal Straw
                drawLine(
                    color = primaryFlavorColor,
                    start = Offset(cx + cupWidth * 0.18f, cupTop - minDim * 0.20f),
                    end = Offset(cx - cupWidth * 0.08f, cupTop + cupHeight * 0.45f),
                    strokeWidth = minDim * 0.042f,
                    cap = StrokeCap.Round
                )

                // Cup Body with Layered Drink Gradient
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFBF7),
                            secondaryFlavorColor,
                            primaryFlavorColor
                        ),
                        startY = cupTop,
                        endY = cupTop + cupHeight
                    ),
                    topLeft = Offset(cupLeft, cupTop),
                    size = Size(cupWidth, cupHeight),
                    cornerRadius = CornerRadius(minDim * 0.07f, minDim * 0.07f)
                )

                // Translucent Ice Cubes inside the cup
                val iceSize = minDim * 0.085f
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.60f),
                    topLeft = Offset(cx - iceSize * 1.1f, cupTop + cupHeight * 0.22f),
                    size = Size(iceSize, iceSize),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.50f),
                    topLeft = Offset(cx + iceSize * 0.15f, cupTop + cupHeight * 0.34f),
                    size = Size(iceSize * 0.92f, iceSize * 0.92f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Whipped Cream / Foam Dome Lid at the top of the cup
                drawArc(
                    color = Color(0xFFFFFDF9),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cupLeft + cupWidth * 0.04f, cupTop - minDim * 0.14f),
                    size = Size(cupWidth * 0.92f, minDim * 0.28f)
                )

                // Cup Flat Rim Pill
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cupLeft - minDim * 0.03f, cupTop - minDim * 0.02f),
                    size = Size(cupWidth + minDim * 0.06f, minDim * 0.048f),
                    cornerRadius = CornerRadius(minDim * 0.025f, minDim * 0.025f)
                )

                // Waffleo Coffee Sleeve in the middle of the cup
                drawRoundRect(
                    color = RichEspressoPrimary.copy(alpha = 0.88f),
                    topLeft = Offset(cupLeft, cupTop + cupHeight * 0.42f),
                    size = Size(cupWidth, cupHeight * 0.24f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawCircle(
                    color = ChampagneGoldAccent,
                    radius = minDim * 0.038f,
                    center = Offset(cx, cupTop + cupHeight * 0.54f)
                )
            }
        }
    }
}
}

/**
 * Reusable Single-Select Option Column adhering strictly to:
 * - Modern borderless Soft Pill trigger
 * - Expands downward when pressed
 * - Displays max 5 options at once (5 * 44.dp = 220.dp), remaining options scroll smoothly
 * - Automatically closes immediately when 1 option is selected
 */
@Composable
fun <T> WaffleoSingleSelectDropdown(
    label: String,
    selectedText: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = SoftOatSurface,
    testTagPrefix: String = "dropdown",
    itemColor: ((T) -> Color)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var triggerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val triggerWidthDp = with(density) { triggerSize.width.toDp() }

    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .onSizeChanged { triggerSize = it }
                    .clip(RoundedCornerShape(50))
                    .clickable(enabled = enabled) { expanded = !expanded }
                    .testTag("${testTagPrefix}_trigger"),
                shape = RoundedCornerShape(50),
                color = if (!enabled) SoftOatSurface.copy(alpha = 0.45f) else if (expanded) SoftGoldHighlight else containerColor,
                tonalElevation = 1.dp,
                shadowElevation = if (enabled) 2.dp else 0.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (expanded) ChampagneGoldAccent else SubtleSandBorder.copy(alpha = 0.75f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val activeOption = options.firstOrNull { optionLabel(it) == selectedText }
                    val activeColor = activeOption?.let { itemColor?.invoke(it) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (activeColor != null) {
                            Surface(
                                shape = CircleShape,
                                color = activeColor,
                                modifier = Modifier.size(9.dp)
                            ) {}
                        }
                        Text(
                            text = selectedText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (activeColor != null) FontWeight.Bold else FontWeight.Medium,
                            color = activeColor ?: if (enabled) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (enabled) ElevatedIvoryCard.copy(alpha = 0.85f) else Color.Transparent,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = label,
                                tint = if (enabled) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (expanded && enabled) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, triggerSize.height + with(density) { 4.dp.roundToPx() }),
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Surface(
                        modifier = Modifier
                            .width(if (triggerWidthDp > 0.dp) triggerWidthDp else 260.dp)
                            .heightIn(max = 220.dp), // Strictly shows 5 options (5 * 44.dp) and scrolls the rest
                        shape = RoundedCornerShape(20.dp),
                        color = ElevatedIvoryCard,
                        tonalElevation = 10.dp,
                        shadowElevation = 14.dp,
                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.55f))
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            items(options) { item ->
                                val itemText = optionLabel(item)
                                val isCurrent = itemText == selectedText
                                val optColor = itemColor?.invoke(item)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isCurrent) SoftGoldHighlight else Color.Transparent)
                                        .clickable {
                                            onOptionSelected(item)
                                            expanded = false // Auto-close on single select
                                        }
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (optColor != null) {
                                            Surface(
                                                shape = CircleShape,
                                                color = optColor,
                                                modifier = Modifier.size(9.dp)
                                            ) {}
                                        }
                                        Text(
                                            text = itemText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = optColor ?: RichEspressoPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Terpilih",
                                            tint = optColor ?: DeepCaramelGold,
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
    }
}

/**
 * Reusable Multi-Select Option Column adhering strictly to:
 * - Modern borderless Soft Pill trigger
 * - Opens in a floating overlay layer (Popup) above the content without shifting layout below
 * - Displays max 5 options at once (scrollable LazyColumn for remaining options)
 * - Provides [ Add ] pill button at the bottom of the expanded option list to confirm selection & close
 */
@Composable
fun WaffleoMultiSelectDropdown(
    label: String,
    selectedItems: List<CatalogRecord>,
    options: List<CatalogRecord>,
    media: OrderMedia,
    onConfirmSelection: (List<CatalogRecord>) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "multiselect"
) {
    var expanded by remember { mutableStateOf(false) }
    var triggerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val triggerWidthDp = with(density) { triggerSize.width.toDp() }

    val summaryLabel = if (selectedItems.isEmpty()) {
        "Kosong (Pilih Add-on)"
    } else {
        selectedItems.joinToString(", ") { it.name }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = RichEspressoPrimary,
            modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .onSizeChanged { triggerSize = it }
                    .clip(RoundedCornerShape(50))
                    .clickable { expanded = !expanded }
                    .testTag("${testTagPrefix}_trigger"),
                shape = RoundedCornerShape(50),
                color = if (expanded) SoftGoldHighlight else SoftOatSurface,
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (expanded) ChampagneGoldAccent else SubtleSandBorder.copy(alpha = 0.75f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = summaryLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedItems.isEmpty()) MutedCocoaText else RichEspressoPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = ElevatedIvoryCard.copy(alpha = 0.85f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = label,
                                tint = RichEspressoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (expanded) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, triggerSize.height + with(density) { 4.dp.roundToPx() }),
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Surface(
                        modifier = Modifier
                            .width(if (triggerWidthDp > 0.dp) triggerWidthDp else 260.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = ElevatedIvoryCard,
                        tonalElevation = 10.dp,
                        shadowElevation = 14.dp,
                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            // Scrollable list of options with immediate selection toggling on checkbox or row click
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .padding(vertical = 2.dp)
                            ) {
                                items(options) { item ->
                                    val isChecked = selectedItems.any { it.id == item.id }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isChecked) SoftGoldHighlight.copy(alpha = 0.65f) else Color.Transparent)
                                            .clickable {
                                                val newItems = if (isChecked) {
                                                    selectedItems.filter { it.id != item.id }
                                                } else {
                                                    selectedItems + item
                                                }
                                                onConfirmSelection(newItems)
                                            }
                                            .padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val newItems = if (checked) {
                                                    if (selectedItems.none { it.id == item.id }) selectedItems + item else selectedItems
                                                } else {
                                                    selectedItems.filter { it.id != item.id }
                                                }
                                                onConfirmSelection(newItems)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = RichEspressoPrimary,
                                                checkmarkColor = ElevatedIvoryCard
                                            )
                                        )
                                        Text(
                                            text = "${item.name} (+${WaffleoViewModel.formatIdr(item.priceForMedia(media))})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                            color = RichEspressoPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
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

/**
 * Egg Waffle Customization Modal implementing the exact structural matrix with Modern Web POS aesthetics:
 * Row 1: [ Topping Utama Pilihan (Left) ]            [ Topping Utama Kedua 1/2-1/2 (Right) ]
 * Row 2:                                             [ Checkbox: Aktifkan Porsi 1/2 - 1/2 ]
 * Row 3: [ Flavour (Default: Original) ]  [ Add on (Default: Kosong, Multi-select + Add) ]
 * Row 4:                                             [ Total Harga Item ]
 * Row 5:                                             [ Add to Cart ]
 */
@Composable
fun EggWaffleCustomizationDialog(
    initialPrimaryTopping: CatalogRecord,
    allPrimaryToppings: List<CatalogRecord>,
    flavours: List<CatalogRecord>,
    addOns: List<CatalogRecord>,
    currentMedia: OrderMedia,
    calculatePrice: (CatalogRecord, Boolean, Boolean, CatalogRecord?, CatalogRecord?, List<CatalogRecord>) -> Long,
    onDismiss: () -> Unit,
    onAddToCart: (CatalogRecord, Boolean, Boolean, CatalogRecord?, CatalogRecord?, List<CatalogRecord>, Int) -> Unit
) {
    val isPlatformMedia = currentMedia.isPlatform
    val canUseMultiTopping = !isPlatformMedia

    var primaryTopping1 by remember { mutableStateOf(initialPrimaryTopping) }
    var isHalfHalfChecked by remember { mutableStateOf(false) }
    var isDoubleToppingChecked by remember { mutableStateOf(false) }
    var itemQty by remember { mutableIntStateOf(1) }

    LaunchedEffect(currentMedia) {
        if (currentMedia.isPlatform) {
            isHalfHalfChecked = false
            isDoubleToppingChecked = false
        }
    }

    var primaryTopping2 by remember {
        mutableStateOf(allPrimaryToppings.firstOrNull { it.id != initialPrimaryTopping.id } ?: initialPrimaryTopping)
    }
    var selectedFlavour by remember {
        mutableStateOf(flavours.firstOrNull { it.name.equals("Original", ignoreCase = true) } ?: flavours.firstOrNull())
    }
    var selectedAddOns by remember { mutableStateOf<List<CatalogRecord>>(emptyList()) }

    val hasSecondary = canUseMultiTopping && (isHalfHalfChecked || isDoubleToppingChecked)

    val unitItemPrice = calculatePrice(
        primaryTopping1,
        if (canUseMultiTopping) isHalfHalfChecked else false,
        if (canUseMultiTopping) isDoubleToppingChecked else false,
        if (hasSecondary) primaryTopping2 else null,
        selectedFlavour,
        selectedAddOns
    )
    val totalItemPrice = unitItemPrice * itemQty

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .padding(10.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header: Detail Item with Live Waffle Illustration Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        WaffleoProductIllustration(
                            itemName = primaryTopping1.name,
                            isWaffle = true,
                            imageUrl = primaryTopping1.imageUrl,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Column {
                            Text(
                                text = primaryTopping1.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = RichEspressoPrimary
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = SoftOatSurface,
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_waffle_modal")) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ROW 1: [ Topping Utama 1 ] (Left)   &   [ Topping Utama 2 (1/2 - 1/2 / Double) ] (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    WaffleoSingleSelectDropdown(
                        label = "Topping Utama",
                        selectedText = "${primaryTopping1.name} (${WaffleoViewModel.formatIdr(primaryTopping1.priceForMedia(currentMedia))})",
                        options = allPrimaryToppings,
                        optionLabel = { "${it.name} (${WaffleoViewModel.formatIdr(it.priceForMedia(currentMedia))})" },
                        onOptionSelected = { primaryTopping1 = it },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "waffle_primary1"
                    )

                    WaffleoSingleSelectDropdown(
                        label = when {
                            isPlatformMedia -> "Topping Utama 2 (Tidak Tersedia di ${currentMedia.label})"
                            isDoubleToppingChecked -> "Topping Utama 2 (Double Topping)"
                            isHalfHalfChecked -> "Topping Utama (Porsi 1/2 - 1/2)"
                            else -> "Topping Utama 2 (Porsi 1/2 / Double)"
                        },
                        selectedText = if (hasSecondary) {
                            "${primaryTopping2.name} (${WaffleoViewModel.formatIdr(primaryTopping2.priceForMedia(currentMedia))})"
                        } else if (isPlatformMedia) {
                            "Nonaktif di ${currentMedia.label}"
                        } else {
                            "Aktifkan ceklis 1/2 atau Double Topping di bawah"
                        },
                        options = allPrimaryToppings,
                        optionLabel = { "${it.name} (${WaffleoViewModel.formatIdr(it.priceForMedia(currentMedia))})" },
                        onOptionSelected = { primaryTopping2 = it },
                        enabled = hasSecondary,
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "waffle_primary2"
                    )
                }

                // ROW 2: Micro-checkboxes "Kombinasi Porsi 1/2" and "Double Topping" aligned under Right Topping Utama
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox 1: Kombinasi Porsi 1/2
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (!canUseMultiTopping) SoftOatSurface.copy(alpha = 0.4f) else if (isHalfHalfChecked) SoftGoldHighlight else SoftOatSurface,
                        border = if (!canUseMultiTopping) BorderStroke(1.dp, MutedCocoaText.copy(alpha = 0.15f)) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .then(
                                if (canUseMultiTopping) {
                                    Modifier.clickable {
                                        isHalfHalfChecked = !isHalfHalfChecked
                                        if (isHalfHalfChecked) isDoubleToppingChecked = false
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .testTag("waffle_half_half_checkbox")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (!canUseMultiTopping) ElevatedIvoryCard.copy(alpha = 0.5f) else if (isHalfHalfChecked) RichEspressoPrimary else ElevatedIvoryCard,
                                border = BorderStroke(
                                    1.dp,
                                    if (!canUseMultiTopping) MutedCocoaText.copy(alpha = 0.2f) else if (isHalfHalfChecked) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.size(14.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isHalfHalfChecked && canUseMultiTopping) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ElevatedIvoryCard,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Kombinasi Porsi 1/2",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!canUseMultiTopping) MutedCocoaText.copy(alpha = 0.45f) else RichEspressoPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Checkbox 2: Double Topping
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (!canUseMultiTopping) SoftOatSurface.copy(alpha = 0.4f) else if (isDoubleToppingChecked) SoftGoldHighlight else SoftOatSurface,
                        border = if (!canUseMultiTopping) BorderStroke(1.dp, MutedCocoaText.copy(alpha = 0.15f)) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .then(
                                if (canUseMultiTopping) {
                                    Modifier.clickable {
                                        isDoubleToppingChecked = !isDoubleToppingChecked
                                        if (isDoubleToppingChecked) isHalfHalfChecked = false
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .testTag("waffle_double_topping_checkbox")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (!canUseMultiTopping) ElevatedIvoryCard.copy(alpha = 0.5f) else if (isDoubleToppingChecked) RichEspressoPrimary else ElevatedIvoryCard,
                                border = BorderStroke(
                                    1.dp,
                                    if (!canUseMultiTopping) MutedCocoaText.copy(alpha = 0.2f) else if (isDoubleToppingChecked) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.size(14.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isDoubleToppingChecked && canUseMultiTopping) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ElevatedIvoryCard,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Double Topping",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!canUseMultiTopping) MutedCocoaText.copy(alpha = 0.45f) else RichEspressoPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ROW 3: [ Flavour ] & [ Add on ] side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    WaffleoSingleSelectDropdown(
                        label = "Flavour Adonan",
                        selectedText = selectedFlavour?.let {
                            val extra = it.priceForMedia(currentMedia)
                            if (extra > 0L) "${it.name} (+${WaffleoViewModel.formatIdr(extra)})" else it.name
                        } ?: "Original",
                        options = flavours,
                        optionLabel = {
                            val extra = it.priceForMedia(currentMedia)
                            if (extra > 0L) "${it.name} (+${WaffleoViewModel.formatIdr(extra)})" else it.name
                        },
                        onOptionSelected = { selectedFlavour = it },
                        itemColor = { AdonanPalette.getColorForFlavour(it.name) },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "waffle_flavour"
                    )

                    WaffleoMultiSelectDropdown(
                        label = "Add on (Topping Tambahan)",
                        selectedItems = selectedAddOns,
                        options = addOns,
                        media = currentMedia,
                        onConfirmSelection = { selectedAddOns = it },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "waffle_addon"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ROW 4 & ROW 5: [ Total ] & Stepper + [ Add to Cart ] aligned on the bottom-right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SoftOatSurface,
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Total Harga Item:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MutedCocoaText
                                )
                                Text(
                                    text = WaffleoViewModel.formatIdr(totalItemPrice),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = RichEspressoPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Quantity Stepper Capsule [ -  qty  + ]
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ElevatedIvoryCard,
                                border = BorderStroke(1.dp, MutedCocoaText.copy(alpha = 0.2f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.height(46.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (itemQty > 1) SoftOatSurface else SoftOatSurface.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .clickable(enabled = itemQty > 1) { itemQty-- }
                                            .testTag("waffle_dialog_minus_btn")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Kurangi Qty",
                                                tint = if (itemQty > 1) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.35f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$itemQty",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = RichEspressoPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Surface(
                                        shape = CircleShape,
                                        color = RichEspressoPrimary,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .clickable { itemQty++ }
                                            .testTag("waffle_dialog_plus_btn")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Tambah Qty",
                                                tint = ElevatedIvoryCard,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    onAddToCart(
                                        primaryTopping1,
                                        if (canUseMultiTopping) isHalfHalfChecked else false,
                                        if (canUseMultiTopping) isDoubleToppingChecked else false,
                                        if (hasSecondary) primaryTopping2 else null,
                                        selectedFlavour,
                                        selectedAddOns,
                                        itemQty
                                    )
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RichEspressoPrimary,
                                    contentColor = ElevatedIvoryCard
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .height(46.dp)
                                    .width(200.dp)
                                    .testTag("waffle_add_to_cart_btn")
                            ) {
                                Text(
                                    text = if (itemQty > 1) "Add to Cart ($itemQty)" else "Add to Cart",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Drinks Customization Modal with 2 Option Columns:
 * [ Sweetness Level ] (Default: Normal) & [ Iced Level ] (Default: Normal)
 */
@Composable
fun DrinkCustomizationDialog(
    drink: CatalogRecord,
    sweetnessOptions: List<CatalogRecord>,
    iceOptions: List<CatalogRecord>,
    currentMedia: OrderMedia,
    onDismiss: () -> Unit,
    onAddToCart: (CatalogRecord, String, String, Int) -> Unit
) {
    var selectedSweetness by remember {
        mutableStateOf(
            sweetnessOptions.firstOrNull { it.name.equals("Normal", ignoreCase = true) }?.name ?: "Normal"
        )
    }
    var selectedIce by remember {
        mutableStateOf(
            iceOptions.firstOrNull { it.name.equals("Normal", ignoreCase = true) }?.name ?: "Normal"
        )
    }
    var itemQty by remember { mutableIntStateOf(1) }

    val unitPrice = drink.priceForMedia(currentMedia)
    val totalPrice = unitPrice * itemQty

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.70f)
                .padding(10.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        WaffleoProductIllustration(
                            itemName = drink.name,
                            isWaffle = false,
                            imageUrl = drink.imageUrl,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Column {
                            Text(
                                text = drink.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = RichEspressoPrimary
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = SoftOatSurface,
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_drink_modal")) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    WaffleoSingleSelectDropdown(
                        label = "Sweetness Level (Default: Normal)",
                        selectedText = selectedSweetness,
                        options = sweetnessOptions.map { it.name }.ifEmpty { listOf("Normal") },
                        optionLabel = { it },
                        onOptionSelected = { selectedSweetness = it },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "drink_sweetness"
                    )

                    WaffleoSingleSelectDropdown(
                        label = "Iced Level (Default: Normal)",
                        selectedText = selectedIce,
                        options = iceOptions.map { it.name }.ifEmpty { listOf("Normal") },
                        optionLabel = { it },
                        onOptionSelected = { selectedIce = it },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "drink_ice"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SoftOatSurface
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("Total Harga Minuman", style = MaterialTheme.typography.bodyMedium, color = MutedCocoaText)
                            Text(
                                text = WaffleoViewModel.formatIdr(totalPrice),
                                style = MaterialTheme.typography.headlineMedium,
                                color = RichEspressoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stepper Capsule [ -  qty  + ]
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ElevatedIvoryCard,
                            border = BorderStroke(1.dp, MutedCocoaText.copy(alpha = 0.2f)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.height(46.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (itemQty > 1) SoftOatSurface else SoftOatSurface.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable(enabled = itemQty > 1) { itemQty-- }
                                        .testTag("drink_dialog_minus_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Kurangi Qty",
                                            tint = if (itemQty > 1) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.35f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "$itemQty",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = RichEspressoPrimary,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable { itemQty++ }
                                        .testTag("drink_dialog_plus_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Tambah Qty",
                                            tint = ElevatedIvoryCard,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onAddToCart(drink, selectedSweetness, selectedIce, itemQty)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RichEspressoPrimary,
                                contentColor = ElevatedIvoryCard
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(46.dp)
                                .width(190.dp)
                                .testTag("drink_add_to_cart_btn")
                        ) {
                            Text(
                                text = if (itemQty > 1) "Add to Cart ($itemQty)" else "Add to Cart",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class PaymentCalcTarget {
    NONE,
    CASH_RECEIVED,
    SPLIT_CASH,
    SPLIT_QRIS
}

/**
 * Mathematical evaluation helper for expression string into Long amount (Rupiah).
 */
fun evaluateSimpleMathExpression(expr: String): Long {
    val clean = expr.replace(" ", "").replace("×", "*").replace("÷", "/")
    if (clean.isEmpty()) return 0L
    val direct = clean.toLongOrNull()
    if (direct != null) return direct.coerceAtLeast(0L)

    return try {
        var total = 0.0
        var currentNumber = ""
        var lastOp = '+'

        fun applyOp(numStr: String, op: Char) {
            val v = numStr.toDoubleOrNull() ?: 0.0
            when (op) {
                '+' -> total += v
                '-' -> total -= v
                '*' -> total *= v
                '/' -> if (v != 0.0) total /= v
            }
        }

        for (ch in clean) {
            if (ch.isDigit() || ch == '.') {
                currentNumber += ch
            } else if (ch in listOf('+', '-', '*', '/')) {
                if (currentNumber.isNotEmpty()) {
                    applyOp(currentNumber, lastOp)
                    currentNumber = ""
                }
                lastOp = ch
            }
        }
        if (currentNumber.isNotEmpty()) {
            applyOp(currentNumber, lastOp)
        }
        total.toLong().coerceAtLeast(0L)
    } catch (e: Exception) {
        clean.filter { it.isDigit() }.toLongOrNull() ?: 0L
    }
}

/**
 * Touchable Pill field representing nominal amount that triggers the Nominal Calculator dialog.
 */
@Composable
fun WaffleoNominalInputField(
    label: String,
    value: Long,
    placeholder: String = "Sentuh untuk menghitung nominal...",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = RichEspressoPrimary,
            modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = SoftOatSurface,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.85f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (value > 0L) WaffleoViewModel.formatIdr(value) else placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (value > 0L) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (value > 0L) RichEspressoPrimary else MutedCocoaText
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SoftGoldHighlight,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Buka Kalkulator",
                            tint = DeepCaramelGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Popup Modal Kalkulator Nominal matching the attached screenshot 100%.
 */
@Composable
fun WaffleoNominalCalculatorDialog(
    title: String = "Kalkulator Nominal",
    initialValue: Long = 0L,
    accentColor: Color = TerracottaDanger,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var expression by remember { mutableStateOf(if (initialValue > 0) initialValue.toString() else "") }
    val evaluated = remember(expression) { evaluateSimpleMathExpression(expression) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 380.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Bar matching user's image
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
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RichEspressoPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = RichEspressoPrimary
                        )
                    }
                }

                // Calculation Display Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftOatSurface,
                    border = BorderStroke(1.dp, SubtleSandBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (expression.isNotBlank()) expression else "0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedCocoaText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(evaluated),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Keypad matching user specification
                val rows = listOf(
                    listOf("C", "+", "-", "×"),
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "="),
                    listOf("1", "2", "3", "⌫"),
                    listOf("0", "00", "000", "Done")
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftOatSurface,
                    border = BorderStroke(1.dp, SubtleSandBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rows.forEach { rowKeys ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowKeys.forEach { key ->
                                    val isDone = key == "Done"
                                    val isOp = key in listOf("+", "-", "×", "÷", "=", "⌫", "C")
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = when {
                                            isDone -> accentColor
                                            isOp -> SoftGoldHighlight
                                            else -> ElevatedIvoryCard
                                        },
                                        border = BorderStroke(1.dp, SubtleSandBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                when (key) {
                                                    "Done" -> {
                                                        onConfirm(evaluated)
                                                        onDismiss()
                                                    }
                                                    "=" -> {
                                                        expression = evaluated.toString()
                                                    }
                                                    "C" -> {
                                                        expression = ""
                                                    }
                                                    "⌫" -> {
                                                        if (expression.isNotEmpty()) {
                                                            expression = expression.dropLast(1)
                                                        }
                                                    }
                                                    else -> {
                                                        expression += key
                                                    }
                                                }
                                            }
                                            .testTag("calc_key_$key")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isDone) Color.White else RichEspressoPrimary
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

/**
 * Payment Modal for Cart or Orders screen (supports Tunai with change calculator, QRIS, Platform,
 * and handles shortfall payment after order edit).
 */
@Composable
fun PaymentModalDialog(
    title: String,
    totalBillAmount: Long,
    alreadyPaidAmount: Long = 0L,
    defaultMedia: String = "Di tempat",
    originalSubtotalAmount: Long = 0L,
    promoDiscountAmount: Long = 0L,
    promoCode: String = "",
    onDismiss: () -> Unit,
    onConfirmPayment: (
        method: String,
        cashReceived: Long,
        splitCash: Long,
        splitQris: Long,
        qrisBank: String,
        changeAmount: Long
    ) -> Unit
) {
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

    val amountDue = (totalBillAmount - alreadyPaidAmount).coerceAtLeast(0L)
    val isPlatformMedia = defaultMedia.equals("GrabFood", ignoreCase = true) ||
        defaultMedia.equals("GoFood", ignoreCase = true) ||
        defaultMedia.equals("ShopeeFood", ignoreCase = true)

    val methods = if (isPlatformMedia) {
        listOf("Platform ($defaultMedia)")
    } else {
        listOf("Tunai (Cash)", "QRIS", "Cash + QRIS", "Transfer Tunai")
    }

    var selectedMethod by remember {
        mutableStateOf(
            if (isPlatformMedia) "Platform ($defaultMedia)" else "Tunai (Cash)"
        )
    }
    var selectedQrisBank by remember { mutableStateOf("Bank Mandiri") }
    var selectedTransferBank by remember { mutableStateOf("Bank Mandiri") }

    // Tunai single payment state
    var cashInput by remember(amountDue) { mutableStateOf(amountDue.toString()) }
    val cashNumeric = cashInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val changeAmount = (cashNumeric - amountDue).coerceAtLeast(0L)
    val isCashSufficient = selectedMethod != "Tunai (Cash)" || cashNumeric >= amountDue

    // Split "Cash + QRIS" states
    var splitCashInput by remember(amountDue) { mutableStateOf("") }
    var splitQrisInput by remember(amountDue) { mutableStateOf("") }
    val splitCashNumeric = splitCashInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val splitQrisNumeric = splitQrisInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val totalSplitReceived = splitCashNumeric + splitQrisNumeric
    val isSplitSufficient = totalSplitReceived >= amountDue
    val splitChangeAmount = (totalSplitReceived - amountDue).coerceAtLeast(0L)

    var activeCalculatorTarget by remember { mutableStateOf(PaymentCalcTarget.NONE) }

    val canConfirm = when (selectedMethod) {
        "Tunai (Cash)" -> isCashSufficient
        "Cash + QRIS" -> isSplitSufficient
        else -> true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = RichEspressoPrimary)
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftOatSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (effDiscount > 0L) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Harga Pesanan:", style = MaterialTheme.typography.bodyMedium, color = MutedCocoaText)
                                Text(WaffleoViewModel.formatIdr(effSubtotal), style = MaterialTheme.typography.titleMedium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val promoTag = if (promoCode.isNotBlank()) " ($promoCode)" else ""
                                Text("Potongan Promo$promoTag:", style = MaterialTheme.typography.bodyMedium, color = TerracottaDanger)
                                Text("-${WaffleoViewModel.formatIdr(effDiscount)}", style = MaterialTheme.typography.titleMedium, color = TerracottaDanger, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Harga Pesanan:", style = MaterialTheme.typography.bodyMedium, color = MutedCocoaText)
                                Text(WaffleoViewModel.formatIdr(totalBillAmount), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        if (alreadyPaidAmount > 0L) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sudah Dibayar Sebelumnya:", style = MaterialTheme.typography.bodyMedium, color = MatchaSageSuccess)
                                Text(WaffleoViewModel.formatIdr(alreadyPaidAmount), style = MaterialTheme.typography.titleMedium, color = MatchaSageSuccess)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = SubtleSandBorder.copy(alpha = 0.6f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (alreadyPaidAmount > 0L) "Sisa Kurang Bayar:" else "Tagihan yang Harus Dibayar:",
                                style = MaterialTheme.typography.titleMedium,
                                color = RichEspressoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = WaffleoViewModel.formatIdr(amountDue),
                                style = MaterialTheme.typography.titleLarge,
                                color = RichEspressoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isPlatformMedia) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftOatSurface,
                        border = BorderStroke(1.dp, SubtleSandBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Metode Pembayaran: Platform ($defaultMedia)",
                                style = MaterialTheme.typography.titleSmall,
                                color = RichEspressoPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pesanan $defaultMedia otomatis diselesaikan via settlement akun mitra ke Rekening BCA.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText
                            )
                        }
                    }
                } else {
                    WaffleoSingleSelectDropdown(
                        label = "Metode Pembayaran",
                        selectedText = selectedMethod,
                        options = methods,
                        optionLabel = { it },
                        onOptionSelected = { selectedMethod = it },
                        testTagPrefix = "payment_method"
                    )
                }

                if (selectedMethod == "QRIS") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Bank Mandiri", "BCA").forEach { bankName ->
                            val isSelected = selectedQrisBank == bankName
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) RichEspressoPrimary else SoftOatSurface,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) RichEspressoPrimary else SubtleSandBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedQrisBank = bankName }
                                    .testTag("qris_bank_${bankName.lowercase().replace(" ", "_")}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bankName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) ElevatedIvoryCard else RichEspressoPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // SPLIT PAYMENT: Cash + QRIS
                if (selectedMethod == "Cash + QRIS") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Kolom Uang Cash
                        Column(modifier = Modifier.weight(1f)) {
                            WaffleoNominalInputField(
                                label = "Uang Cash (Rp)",
                                value = splitCashNumeric,
                                placeholder = "Nominal Cash...",
                                onClick = { activeCalculatorTarget = PaymentCalcTarget.SPLIT_CASH },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "pay_split_cash_input"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val neededCash = (amountDue - splitQrisNumeric).coerceAtLeast(0L)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftGoldHighlight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { splitCashInput = neededCash.toString() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Pas Tunai: ${WaffleoViewModel.formatIdr(neededCash)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RichEspressoPrimary
                                    )
                                }
                            }
                        }

                        // Kolom QRIS
                        Column(modifier = Modifier.weight(1f)) {
                            WaffleoNominalInputField(
                                label = "QRIS (Rp)",
                                value = splitQrisNumeric,
                                placeholder = "Nominal QRIS...",
                                onClick = { activeCalculatorTarget = PaymentCalcTarget.SPLIT_QRIS },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "pay_split_qris_input"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val neededQris = (amountDue - splitCashNumeric).coerceAtLeast(0L)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftGoldHighlight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { splitQrisInput = neededQris.toString() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Pas QRIS: ${WaffleoViewModel.formatIdr(neededQris)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RichEspressoPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Bank Mandiri", "BCA").forEach { bankName ->
                            val isSelected = selectedQrisBank == bankName
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) RichEspressoPrimary else SoftOatSurface,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) RichEspressoPrimary else SubtleSandBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedQrisBank = bankName }
                                    .testTag("split_qris_bank_${bankName.lowercase().replace(" ", "_")}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bankName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) ElevatedIvoryCard else RichEspressoPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSplitSufficient) SoftSageBg else SoftTerracottaBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Diterima (Cash + QRIS):",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RichEspressoPrimary
                                )
                                Text(
                                    text = WaffleoViewModel.formatIdr(totalSplitReceived),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RichEspressoPrimary
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = SubtleSandBorder.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isSplitSufficient) "Kembalian Uang Tunai (Cash):" else "Total Pembayaran Masih Kurang:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSplitSufficient) MatchaSageSuccess else TerracottaDanger
                                )
                                Text(
                                    text = if (isSplitSufficient) {
                                        WaffleoViewModel.formatIdr(splitChangeAmount)
                                    } else {
                                        WaffleoViewModel.formatIdr(amountDue - totalSplitReceived)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSplitSufficient) MatchaSageSuccess else TerracottaDanger
                                )
                            }
                        }
                    }
                }

                if (selectedMethod == "Transfer Tunai") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pilih Akun Bank Tujuan Transfer",
                        style = MaterialTheme.typography.labelLarge,
                        color = MutedCocoaText,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SoftOatSurface,
                        border = BorderStroke(1.dp, SubtleSandBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Kolom Kiri: Grup Akun (Bank)
                            Column(
                                modifier = Modifier
                                    .weight(0.42f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RichEspressoPrimary.copy(alpha = 0.08f))
                                    .padding(10.dp)
                            ) {
                                Text("Grup Akun", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = RichEspressoPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🏦 Bank",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ElevatedIvoryCard,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            // Kolom Kanan: Nama Akun Spesifik (Bank Mandiri / BCA)
                            Column(
                                modifier = Modifier.weight(0.58f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Akun Rekening", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                                listOf("Bank Mandiri", "BCA").forEach { bankAcc ->
                                    val isSelected = selectedTransferBank == bankAcc
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) HoneyCaramelAccent else ElevatedIvoryCard,
                                        border = BorderStroke(1.dp, if (isSelected) RichEspressoPrimary else SubtleSandBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { selectedTransferBank = bankAcc }
                                            .testTag("transfer_bank_${bankAcc.lowercase().replace(" ", "_")}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = bankAcc,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = RichEspressoPrimary
                                            )
                                            if (isSelected) {
                                                Text("✓", style = MaterialTheme.typography.titleMedium, color = RichEspressoPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedMethod == "Tunai (Cash)") {
                    Spacer(modifier = Modifier.height(10.dp))
                    WaffleoNominalInputField(
                        label = "Uang Tunai Diterima (Rp)",
                        value = cashNumeric,
                        placeholder = "Masukkan nominal tunai...",
                        onClick = { activeCalculatorTarget = PaymentCalcTarget.CASH_RECEIVED },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "pay_cash_input"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(amountDue, 50000L, 100000L).distinct().forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftGoldHighlight,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { cashInput = preset.toString() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (preset == amountDue) "Uang Pas" else WaffleoViewModel.formatIdr(preset),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = RichEspressoPrimary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCashSufficient) SoftSageBg else SoftTerracottaBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isCashSufficient) "Kembalian Tunai:" else "Uang Tunai Kurang:",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isCashSufficient) MatchaSageSuccess else TerracottaDanger
                            )
                            Text(
                                text = if (isCashSufficient) {
                                    WaffleoViewModel.formatIdr(changeAmount)
                                } else {
                                    WaffleoViewModel.formatIdr(amountDue - cashNumeric)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isCashSufficient) MatchaSageSuccess else TerracottaDanger
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftOatSurface,
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                            Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val resolvedMethod = when (selectedMethod) {
                                "QRIS", "QRIS Waffleo" -> "QRIS ($selectedQrisBank)"
                                "Transfer Tunai" -> "Transfer Tunai ($selectedTransferBank)"
                                "Cash + QRIS" -> "Cash + QRIS ($selectedQrisBank)"
                                else -> selectedMethod
                            }
                            val effSplitCash = if (selectedMethod == "Cash + QRIS") splitCashNumeric else 0L
                            val effSplitQris = if (selectedMethod == "Cash + QRIS") splitQrisNumeric else 0L
                            val effChange = when (selectedMethod) {
                                "Cash + QRIS" -> splitChangeAmount
                                "Tunai (Cash)" -> changeAmount
                                else -> 0L
                            }
                            val effCashReceived = when (selectedMethod) {
                                "Tunai (Cash)" -> cashNumeric
                                "Cash + QRIS" -> totalSplitReceived
                                else -> amountDue
                            }
                            onConfirmPayment(
                                resolvedMethod,
                                effCashReceived,
                                effSplitCash,
                                effSplitQris,
                                selectedQrisBank,
                                effChange
                            )
                            onDismiss()
                        },
                        enabled = canConfirm,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatchaSageSuccess,
                            contentColor = ElevatedIvoryCard
                        ),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("confirm_pay_button")
                    ) {
                        Text(
                            text = if (selectedMethod == "Cash + QRIS" && !isSplitSufficient) {
                                "Nominal Belum Cukup"
                            } else if (selectedMethod == "Tunai (Cash)" && !isCashSufficient) {
                                "Tunai Belum Cukup"
                            } else {
                                "Konfirmasi Lunas (Paid)"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    when (activeCalculatorTarget) {
        PaymentCalcTarget.CASH_RECEIVED -> {
            WaffleoNominalCalculatorDialog(
                title = "Kalkulator: Uang Tunai Diterima",
                initialValue = cashNumeric,
                accentColor = TerracottaDanger,
                onDismiss = { activeCalculatorTarget = PaymentCalcTarget.NONE },
                onConfirm = { calculatedAmount ->
                    cashInput = calculatedAmount.toString()
                }
            )
        }
        PaymentCalcTarget.SPLIT_CASH -> {
            WaffleoNominalCalculatorDialog(
                title = "Kalkulator: Uang Cash (Split)",
                initialValue = splitCashNumeric,
                accentColor = DeepCaramelGold,
                onDismiss = { activeCalculatorTarget = PaymentCalcTarget.NONE },
                onConfirm = { calculatedAmount ->
                    splitCashInput = calculatedAmount.toString()
                }
            )
        }
        PaymentCalcTarget.SPLIT_QRIS -> {
            WaffleoNominalCalculatorDialog(
                title = "Kalkulator: QRIS (Split)",
                initialValue = splitQrisNumeric,
                accentColor = MatchaSageSuccess,
                onDismiss = { activeCalculatorTarget = PaymentCalcTarget.NONE },
                onConfirm = { calculatedAmount ->
                    splitQrisInput = calculatedAmount.toString()
                }
            )
        }
        PaymentCalcTarget.NONE -> {}
    }
}

/**
 * Cancel Order Confirmation Dialog with Reason Dropdown
 */
@Composable
fun CancelOrderDialog(
    order: OrderRecord,
    onDismiss: () -> Unit,
    onConfirmCancel: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(WaffleoSeedData.defaultCancelReasons.first()) }
    var customNote by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Batalkan Pesanan ${order.displayOrderNumber}?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TerracottaDanger
                )

                Spacer(modifier = Modifier.height(14.dp))

                WaffleoSingleSelectDropdown(
                    label = "Pilih Alasan Pembatalan",
                    selectedText = selectedReason,
                    options = WaffleoSeedData.defaultCancelReasons,
                    optionLabel = { it },
                    onOptionSelected = { selectedReason = it },
                    testTagPrefix = "cancel_reason"
                )

                Spacer(modifier = Modifier.height(10.dp))

                WaffleoSoftTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    label = "Keterangan Tambahan (Opsional)",
                    placeholder = "Tulis catatan tambahan...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                var sliderOffsetX by remember { mutableFloatStateOf(0f) }
                var sliderTrackWidth by remember { mutableFloatStateOf(1f) }
                val density = LocalDensity.current
                val thumbSizeDp = 44.dp
                val thumbSizePx = with(density) { thumbSizeDp.toPx() }
                val maxDrag = (sliderTrackWidth - thumbSizePx).coerceAtLeast(1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftOatSurface,
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                            Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                        }
                    }

                    // Slide to Confirm Slider track
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SoftTerracottaBg)
                            .border(1.dp, TerracottaDanger.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .onSizeChanged { sliderTrackWidth = it.width.toFloat() }
                            .testTag("confirm_cancel_order_slider"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Centered track hint label
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Geser untuk Membatalkan ➔",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TerracottaDanger.copy(alpha = 0.85f)
                            )
                        }

                        // Draggable thumb
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(sliderOffsetX.roundToInt(), 0) }
                                .size(thumbSizeDp)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(TerracottaDanger)
                                .pointerInput(sliderTrackWidth) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (sliderOffsetX >= maxDrag * 0.75f) {
                                                // Trigger cancellation
                                                val finalReason = if (customNote.isBlank()) {
                                                    selectedReason
                                                } else {
                                                    "$selectedReason (${customNote.trim()})"
                                                }
                                                onConfirmCancel(finalReason)
                                                onDismiss()
                                            } else {
                                                // Snap back
                                                sliderOffsetX = 0f
                                            }
                                        },
                                        onHorizontalDrag = { _, dragAmount ->
                                            sliderOffsetX = (sliderOffsetX + dragAmount).coerceIn(0f, maxDrag)
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Geser",
                                tint = ElevatedIvoryCard,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Thermal Receipt Preview & Bluetooth Print Dialog formatted as an authentic Cafe Receipt
 */
@Composable
fun ThermalReceiptDialog(
    order: OrderRecord,
    onDismiss: () -> Unit,
    onPrinted: () -> Unit
) {
    val waktuPesanan = remember(order.createdAt, order.updatedAt) {
        val dt = (order.createdAt ?: order.updatedAt)?.toDate()
        if (dt != null) {
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("id-ID")).format(dt)
        } else {
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date())
        }
    }
    val lineItems = remember(order.itemsPayload) { CartLineItem.deserializeList(order.itemsPayload) }
    val orderMediaEnum = remember(order.orderMedia) { OrderMedia.fromLabel(order.orderMedia) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier.widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Action Bar
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
                                    imageVector = Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = DeepCaramelGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Pratinjau Struk Kasir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = RichEspressoPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Realistic Thermal Paper Container
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, Color(0xFFE5E5E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Store Identity Header
                        Text(
                            text = "WAFFLEO",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF1E1E1E)
                        )
                        Text(
                            text = "Cabang ${com.example.data.model.formatCleanBranchName(order.branchId, order.branchId)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF757575)
                        )

                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - - - - - - - -",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFBDBDBD),
                            modifier = Modifier.padding(vertical = 4.dp),
                            maxLines = 1
                        )

                        // Order Metadata Table
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            ReceiptInfoRow("No. Order", order.displayOrderNumber)
                            ReceiptInfoRow("Waktu", waktuPesanan)
                            ReceiptInfoRow("Kasir", order.createdBy)
                            ReceiptInfoRow("Kanal", order.orderMedia)
                            if (order.customerData.isNotBlank() && !order.customerData.equals("Walk In Guest", ignoreCase = true) && !order.customerData.equals("Pelanggan Di tempat", ignoreCase = true)) {
                                ReceiptInfoRow("Pelanggan", order.customerData)
                            }
                        }

                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - - - - - - - -",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFBDBDBD),
                            modifier = Modifier.padding(vertical = 4.dp),
                            maxLines = 1
                        )

                        // Itemized Product List
                        if (lineItems.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                lineItems.forEach { item ->
                                    val itemTotal = item.lineTotalFor(orderMediaEnum)
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "${item.qty}x ${item.formattedTitle()}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = Color(0xFF1E1E1E),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = WaffleoViewModel.formatIdr(itemTotal),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = Color(0xFF1E1E1E)
                                            )
                                        }
                                        if (item.addOnNames.isNotEmpty()) {
                                            val prefix = if (item.category.equals("Egg Waffle", ignoreCase = true)) "   + " else "   • "
                                            Text(
                                                text = "$prefix${item.addOnNames.joinToString(", ")}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                color = Color(0xFF616161)
                                            )
                                        }
                                        if (item.category.equals("Drinks", ignoreCase = true) && (item.sweetnessLevel != "Normal" || item.icedLevel != "Normal")) {
                                            Text(
                                                text = "   (${item.sweetnessLevel}, ${item.icedLevel})",
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                color = Color(0xFF616161)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = order.itemsSummary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF1E1E1E),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (order.notes.isNotBlank()) {
                            Text(
                                text = "Catatan Pesanan: ${order.notes}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF616161),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }

                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - - - - - - - -",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFBDBDBD),
                            modifier = Modifier.padding(vertical = 4.dp),
                            maxLines = 1
                        )

                        // Subtotal & Discounts
                        val effectiveSubtotal = if (order.subtotalAmount > 0L) {
                            order.subtotalAmount
                        } else if (order.discountAmount > 0L) {
                            order.totalAmount + order.discountAmount
                        } else {
                            order.totalAmount
                        }
                        ReceiptInfoRow("Subtotal", WaffleoViewModel.formatIdr(effectiveSubtotal))
                        val effDiscount = if (order.discountAmount > 0L) {
                            order.discountAmount
                        } else if (effectiveSubtotal > order.totalAmount) {
                            effectiveSubtotal - order.totalAmount
                        } else {
                            0L
                        }
                        if (effDiscount > 0L) {
                            val promoTag = if (order.promoCode.isNotBlank()) " (${order.promoCode})" else ""
                            ReceiptInfoRow("Diskon Promo$promoTag", "-${WaffleoViewModel.formatIdr(effDiscount)}")
                        }

                        Text(
                            text = "= = = = = = = = = = = = = = = = = = = = = = = =",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(vertical = 3.dp),
                            maxLines = 1
                        )

                        // Grand Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF1E1E1E)
                            )
                            Text(
                                text = WaffleoViewModel.formatIdr(order.totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF1E1E1E)
                            )
                        }

                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - - - - - - - -",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFBDBDBD),
                            modifier = Modifier.padding(vertical = 4.dp),
                            maxLines = 1
                        )

                        // Payment & Settlement Info
                        if (order.isPaid) {
                            val ledgerEntries = remember(order.paymentLedgerPayload) {
                                OrderPaymentLedgerEntry.deserializeList(order.paymentLedgerPayload)
                                    .filter { it.paymentType != "CHANGE_REFUND_RETURN" }
                            }

                            if (ledgerEntries.isNotEmpty()) {
                                ReceiptInfoRow("Metode Bayar", "")
                                ledgerEntries.forEach { entry ->
                                    val cleanMethodName = entry.paymentMethod
                                        .replace("QRIS Waffleo", "QRIS")
                                        .replace("Tunai (Cash)", "Tunai (Cash)")
                                    ReceiptInfoRow("  • $cleanMethodName", WaffleoViewModel.formatIdr(entry.amount))
                                }
                            } else {
                                val cleanMethod = order.paymentMethod.replace("QRIS Waffleo", "QRIS")
                                ReceiptInfoRow("Metode Bayar", cleanMethod)
                                if (order.splitCashAmount > 0L && order.splitQrisAmount > 0L) {
                                    ReceiptInfoRow("  • Tunai (Cash)", WaffleoViewModel.formatIdr(order.splitCashAmount))
                                    ReceiptInfoRow("  • QRIS", WaffleoViewModel.formatIdr(order.splitQrisAmount))
                                }
                            }

                            val nominalBayar = when {
                                order.cashReceivedAmount > 0L -> order.cashReceivedAmount
                                order.changeDueAmount > 0L -> order.totalAmount + order.changeDueAmount
                                order.paidAmount > 0L -> order.paidAmount
                                else -> order.totalAmount
                            }
                            ReceiptInfoRow("Total Diterima", WaffleoViewModel.formatIdr(nominalBayar))

                            val change = when {
                                order.changeDueAmount > 0L -> order.changeDueAmount
                                order.cashReceivedAmount > order.totalAmount -> order.cashReceivedAmount - order.totalAmount
                                order.paidAmount > order.totalAmount -> order.paidAmount - order.totalAmount
                                else -> 0L
                            }
                            ReceiptInfoRow("Kembalian", WaffleoViewModel.formatIdr(change))
                        } else {
                            ReceiptInfoRow("Status Bayar", "BELUM LUNAS")
                            val sisa = (order.totalAmount - order.paidAmount).coerceAtLeast(0L)
                            ReceiptInfoRow("Sisa Tagihan", WaffleoViewModel.formatIdr(sisa))
                        }

                        if (order.shortfallAmount > 0L) {
                            ReceiptInfoRow("Kurang Bayar (Edit)", WaffleoViewModel.formatIdr(order.shortfallAmount))
                        }

                        Text(
                            text = "= = = = = = = = = = = = = = = = = = = = = = = =",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(vertical = 4.dp),
                            maxLines = 1
                        )

                        // Cafe Thank You & Social Footer
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "*** TERIMA KASIH ***",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF1E1E1E)
                        )
                        Text(
                            text = "Selamat Menikmati :) • Instagram: waffleo.pgk",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF616161)
                        )
                        Text(
                            text = "Simpan struk ini sebagai bukti pembayaran",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftOatSurface,
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                            Text("Tutup", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onPrinted()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RichEspressoPrimary,
                            contentColor = ElevatedIvoryCard
                        ),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Struk Thermal", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = Color(0xFF424242)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF1E1E1E)
        )
    }
}
