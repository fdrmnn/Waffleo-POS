package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CatalogRecord
import com.example.data.model.OrderMedia
import com.example.ui.theme.ChampagneGoldAccent
import com.example.ui.theme.DeepCaramelGold
import com.example.ui.theme.ElevatedIvoryCard
import com.example.ui.theme.MatchaSageSuccess
import com.example.ui.theme.MutedCocoaText
import com.example.ui.theme.RichEspressoPrimary
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SubtleSandBorder
import com.example.ui.viewmodel.WaffleoViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenericItemCustomizationDialog(
    item: CatalogRecord,
    modifierGroups: Map<String, List<CatalogRecord>>,
    currentMedia: OrderMedia,
    onDismiss: () -> Unit,
    onAddToCart: (
        item: CatalogRecord,
        selectedModifiers: List<String>,
        extraWalkIn: Long,
        extraGrab: Long,
        extraGo: Long,
        extraShopee: Long,
        qty: Int
    ) -> Unit
) {
    // Single-choice groups (e.g. Rasa, Flavour, Size, Level)
    val selectedSingleOptions = remember {
        mutableStateMapOf<String, String>().apply {
            modifierGroups.forEach { (groupName, options) ->
                val isMulti = groupName.contains("add", ignoreCase = true) ||
                    groupName.contains("topping", ignoreCase = true) ||
                    groupName.contains("extra", ignoreCase = true)
                if (!isMulti) {
                    options.firstOrNull()?.let { firstOpt ->
                        put(groupName, firstOpt.name)
                    }
                }
            }
        }
    }

    // Multi-choice groups (e.g. Add-on, Topping Extra)
    val selectedMultiOptions = remember {
        mutableStateMapOf<String, Set<String>>().apply {
            modifierGroups.forEach { (groupName, _) ->
                val isMulti = groupName.contains("add", ignoreCase = true) ||
                    groupName.contains("topping", ignoreCase = true) ||
                    groupName.contains("extra", ignoreCase = true)
                if (isMulti) {
                    put(groupName, emptySet())
                }
            }
        }
    }

    var itemQty by remember { mutableIntStateOf(1) }

    // Resolve all selected modifier CatalogRecords
    val selectedRecords = remember(selectedSingleOptions.toMap(), selectedMultiOptions.toMap(), modifierGroups) {
        val list = mutableListOf<CatalogRecord>()
        modifierGroups.forEach { (groupName, options) ->
            val isMulti = groupName.contains("add", ignoreCase = true) ||
                groupName.contains("topping", ignoreCase = true) ||
                groupName.contains("extra", ignoreCase = true)
            if (isMulti) {
                val chosenNames = selectedMultiOptions[groupName] ?: emptySet()
                options.filter { it.name in chosenNames }.forEach { list.add(it) }
            } else {
                val chosenName = selectedSingleOptions[groupName]
                options.firstOrNull { it.name == chosenName }?.let { list.add(it) }
            }
        }
        list
    }

    val extraPriceWalkIn = selectedRecords.sumOf { it.priceWalkIn }
    val extraPriceGrab = selectedRecords.sumOf { it.priceGrabFood }
    val extraPriceGo = selectedRecords.sumOf { it.priceGoFood }
    val extraPriceShopee = selectedRecords.sumOf { it.priceShopeeFood }

    val baseUnitPrice = item.priceForMedia(currentMedia)
    val extraModifierPrice = selectedRecords.sumOf { it.priceForMedia(currentMedia) }
    val unitPrice = baseUnitPrice + extraModifierPrice
    val totalPrice = unitPrice * itemQty

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .widthIn(max = 520.dp)
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftGoldHighlight,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepCaramelGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedCocoaText)
                    }
                }

                // Render each connected modifier category group
                modifierGroups.forEach { (groupName, options) ->
                    val isMulti = groupName.contains("add", ignoreCase = true) ||
                        groupName.contains("topping", ignoreCase = true) ||
                        groupName.contains("extra", ignoreCase = true)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Pilih $groupName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary
                            )
                            if (isMulti) {
                                Text(
                                    text = "(Bisa pilih lebih dari satu)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedCocoaText
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            options.forEach { opt ->
                                val isSelected = if (isMulti) {
                                    selectedMultiOptions[groupName]?.contains(opt.name) == true
                                } else {
                                    selectedSingleOptions[groupName] == opt.name
                                }
                                val addPrice = opt.priceForMedia(currentMedia)
                                val labelText = if (addPrice > 0L) {
                                    "${opt.name} (+${WaffleoViewModel.formatIdr(addPrice)})"
                                } else {
                                    opt.name
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) SoftGoldHighlight else SoftOatSurface,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) ChampagneGoldAccent else SubtleSandBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            if (isMulti) {
                                                val current = selectedMultiOptions[groupName] ?: emptySet()
                                                selectedMultiOptions[groupName] = if (current.contains(opt.name)) {
                                                    current - opt.name
                                                } else {
                                                    current + opt.name
                                                }
                                            } else {
                                                selectedSingleOptions[groupName] = opt.name
                                            }
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MatchaSageSuccess,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = labelText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) RichEspressoPrimary else MutedCocoaText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quantity & Total Price Footer
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftOatSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Harga", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                            Text(
                                text = WaffleoViewModel.formatIdr(totalPrice),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = DeepCaramelGold
                            )
                        }

                        // Stepper [ -  qty  + ]
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ElevatedIvoryCard,
                            border = BorderStroke(1.dp, SubtleSandBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SoftOatSurface,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .clickable { if (itemQty > 1) itemQty-- }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Remove, contentDescription = "Kurangi", tint = RichEspressoPrimary, modifier = Modifier.size(14.dp))
                                    }
                                }

                                Text(
                                    text = itemQty.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = RichEspressoPrimary,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .clickable { itemQty++ }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Add, contentDescription = "Tambah", tint = ElevatedIvoryCard, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Add to Cart Button
                Button(
                    onClick = {
                        val formattedModifiers = mutableListOf<String>()
                        modifierGroups.forEach { (grp, _) ->
                            val isMulti = grp.contains("add", ignoreCase = true) ||
                                grp.contains("topping", ignoreCase = true) ||
                                grp.contains("extra", ignoreCase = true)
                            if (isMulti) {
                                val set = selectedMultiOptions[grp] ?: emptySet()
                                set.forEach { optName ->
                                    formattedModifiers.add("$grp: $optName")
                                }
                            } else {
                                val optName = selectedSingleOptions[grp]
                                if (!optName.isNullOrBlank()) {
                                    formattedModifiers.add("$grp: $optName")
                                }
                            }
                        }

                        onAddToCart(
                            item,
                            formattedModifiers,
                            extraPriceWalkIn,
                            extraPriceGrab,
                            extraPriceGo,
                            extraPriceShopee,
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
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Tambah Ke Keranjang (${WaffleoViewModel.formatIdr(totalPrice)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
