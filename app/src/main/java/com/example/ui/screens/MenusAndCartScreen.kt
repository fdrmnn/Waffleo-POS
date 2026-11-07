package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.zIndex
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.data.model.CartLineItem
import com.example.data.model.CatalogRecord
import com.example.data.model.OrderMedia
import com.example.data.model.PromoConfig
import com.example.data.model.PromoStatus
import com.example.data.model.WaffleoPromoEngine
import com.example.data.model.areCategoriesMatching
import com.example.data.model.normalizeCategoryCanonical
import com.example.ui.components.DrinkCustomizationDialog
import com.example.ui.components.EggWaffleCustomizationDialog
import com.example.ui.components.GenericItemCustomizationDialog
import com.example.ui.components.PaymentModalDialog
import com.example.ui.components.WaffleoFloatingCard
import com.example.ui.components.WaffleoPillBadge
import com.example.ui.components.WaffleoPillButton
import com.example.ui.components.WaffleoPillButtonVariant
import com.example.ui.components.WaffleoProductIllustration
import com.example.ui.components.WaffleoSingleSelectDropdown
import com.example.ui.components.WaffleoSoftTextField
import com.example.ui.theme.ChampagneGoldAccent
import com.example.ui.theme.DeepCaramelGold
import com.example.ui.theme.buildAnnotatedCartTitle
import com.example.ui.theme.ElevatedIvoryCard
import com.example.ui.theme.HoneyAmberWarning
import com.example.ui.theme.HoneyCaramelAccent
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
import com.example.ui.viewmodel.MenuCategoryTab
import com.example.ui.viewmodel.WaffleoViewModel
import org.json.JSONObject

@Composable
fun MenusAndCartScreen(
    viewModel: WaffleoViewModel
) {
    val selectedTab by viewModel.selectedMenuTab.collectAsState()
    val catalog by viewModel.catalogRecords.collectAsState()
    val cartMedia by viewModel.cartMedia.collectAsState()
    val customerSuffix by viewModel.customerInputSuffix.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartNotes by viewModel.cartNotes.collectAsState()
    val selectedPromoId by viewModel.selectedPromoId.collectAsState()
    val cartVoucherCode by viewModel.cartVoucherCode.collectAsState()
    val session by viewModel.session.collectAsState()
    val cartIsPaid by viewModel.cartIsPaid.collectAsState()
    val cartPaidAmount by viewModel.cartPaidAmount.collectAsState()
    val editingOrder by viewModel.editingOrder.collectAsState()
    val cartIsBackdated by viewModel.cartIsBackdated.collectAsState()
    val cartBackdatedDateKey by viewModel.cartBackdatedDateKey.collectAsState()
    val cartBackdatedTimeText by viewModel.cartBackdatedTimeText.collectAsState()
    val isStoreOpen by viewModel.isStoreOpen.collectAsState()

    val customMenuCategories by viewModel.customMenuCategories.collectAsState()
    val modifierToMenuCategoryMap by viewModel.modifierToMenuCategoryMap.collectAsState()

    val activeWaffles = remember(catalog) {
        catalog.filter { it.recordType == "MENU_ITEM" && it.category == "Egg Waffle" && it.isActive }
    }
    val activeDrinks = remember(catalog) {
        catalog.filter { it.recordType == "MENU_ITEM" && it.category == "Drinks" && it.isActive }
    }
    val activeFlavours = remember(catalog) {
        catalog.filter { it.recordType == "MODIFIER" && areCategoriesMatching(it.category, "Flavour") && it.isActive }
    }
    val activeAddOns = remember(catalog) {
        catalog.filter { it.recordType == "MODIFIER" && areCategoriesMatching(it.category, "Add-on") && it.isActive }
    }
    val activeSweetness = remember(catalog) {
        catalog.filter { it.recordType == "MODIFIER" && areCategoriesMatching(it.category, "Sweetness") && it.isActive }
    }
    val activeIce = remember(catalog) {
        catalog.filter { it.recordType == "MODIFIER" && areCategoriesMatching(it.category, "Ice Level") && it.isActive }
    }
    val activePromos = remember(catalog, session.branchId) {
        catalog.filter { rec ->
            if (rec.recordType != "PROMO") return@filter false
            val cfg = PromoConfig.fromCatalogRecord(rec)
            val used = viewModel.getPromoUsageCount(rec)
            WaffleoPromoEngine.resolvePromoStatus(rec, cfg, used) == PromoStatus.ACTIVE &&
                WaffleoPromoEngine.isPromoApplicableToBranch(rec, cfg, session.branchId)
        }
    }
    val hasAnyVoucherPromo = remember(activePromos) {
        activePromos.any { PromoConfig.fromCatalogRecord(it).voucherCode.isNotBlank() }
    }

    val posMenuCategories = remember(customMenuCategories, catalog) {
        val fromCatalog = catalog.filter { it.recordType == "MENU_ITEM" }.map { it.category }
        (listOf("Semua") + customMenuCategories + fromCatalog)
            .filter { it.isNotBlank() }
            .distinctBy { normalizeCategoryCanonical(it) }
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var activeCategoryTab by remember { mutableStateOf("Semua") }
    var activeSubCategoryTab by remember { mutableStateOf("Semua") }

    val activeCategorySubCategories = remember(activeCategoryTab, catalog) {
        if (activeCategoryTab.equals("Semua", ignoreCase = true)) {
            emptyList()
        } else {
            viewModel.getSubCategoriesForCategory(activeCategoryTab)
        }
    }

    var clickedWaffleForModal by remember { mutableStateOf<CatalogRecord?>(null) }
    var clickedDrinkForModal by remember { mutableStateOf<CatalogRecord?>(null) }
    var clickedGenericItemForModal by remember { mutableStateOf<Pair<CatalogRecord, Map<String, List<CatalogRecord>>>?>(null) }
    var showCartPayModal by remember { mutableStateOf(false) }
    var validationErrorText by remember { mutableStateOf<String?>(null) }
    var customerDismissedText by remember { mutableStateOf("") }
    var suggestionSelectedAtMillis by remember { mutableLongStateOf(0L) }

    val customerSuggestions = remember(customerSuffix, customerDismissedText, cartMedia) {
        if (cartMedia.requiresCustomerCode || customerSuffix.isBlank() || customerSuffix == customerDismissedText) {
            emptyList()
        } else {
            viewModel.getCustomerNameSuggestions(customerSuffix)
        }
    }

    val displayedMenuItems = remember(catalog, activeCategoryTab, activeSubCategoryTab, posMenuCategories) {
        val orderedCats = posMenuCategories.filterNot { it.equals("Semua", ignoreCase = true) }
        val activeMenuItems = catalog
            .filter { it.recordType == "MENU_ITEM" && it.isActive }
            .sortedWith(
                compareBy<CatalogRecord> { rec ->
                    val catIdx = orderedCats.indexOfFirst { areCategoriesMatching(it, rec.category) }
                    if (catIdx >= 0) catIdx else 999
                }
                    .thenBy { if (it.sortOrder < 0) 999999 else it.sortOrder }
                    .thenBy { it.name.lowercase() }
            )
        if (activeCategoryTab.equals("Semua", ignoreCase = true)) {
            activeMenuItems
        } else {
            val inCat = activeMenuItems.filter { areCategoriesMatching(it.category, activeCategoryTab) }
            if (activeSubCategoryTab.equals("Semua", ignoreCase = true)) {
                inCat
            } else {
                inCat.filter { areCategoriesMatching(it.subCategory, activeSubCategoryTab) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // LEFT PANE: MODERN WEB-STYLE VISUAL MENU CATALOG (60% width)
        Column(
            modifier = Modifier
                .weight(0.60f)
                .fillMaxHeight()
        ) {
            // Floating Pill Category Segmented Bar with Dynamic Categories
            Surface(
                shape = RoundedCornerShape(50),
                color = ElevatedIvoryCard,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    posMenuCategories.forEach { catLabel ->
                        val isSelected = activeCategoryTab.equals(catLabel, ignoreCase = true)
                        val tabIcon = when (catLabel.uppercase()) {
                            "SEMUA" -> Icons.Default.GridView
                            "EGG WAFFLE" -> Icons.Default.BakeryDining
                            "DRINKS" -> Icons.Default.LocalCafe
                            else -> Icons.Default.RestaurantMenu
                        }
                        Surface(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    activeCategoryTab = catLabel
                                    activeSubCategoryTab = "Semua"
                                }
                                .testTag("menu_tab_${catLabel.lowercase()}"),
                            color = if (isSelected) RichEspressoPrimary else Color.Transparent,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) ChampagneGoldAccent.copy(alpha = 0.25f) else SoftOatSurface,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = tabIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) SoftGoldHighlight else MutedCocoaText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = catLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) ElevatedIvoryCard else RichEspressoPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Sub-Category Small Pills (Only shown when a specific category is active and has sub-categories)
            if (!activeCategoryTab.equals("Semua", ignoreCase = true) && activeCategorySubCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val subOptions = listOf("Semua") + activeCategorySubCategories
                    subOptions.forEach { subCat ->
                        val isSelected = activeSubCategoryTab.equals(subCat, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) DeepCaramelGold else ElevatedIvoryCard,
                            border = BorderStroke(1.dp, if (isSelected) DeepCaramelGold else SubtleSandBorder),
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { activeSubCategoryTab = subCat }
                                .testTag("pos_subcat_${subCat.lowercase().replace(" ", "_")}")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = subCat,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ElevatedIvoryCard else RichEspressoPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Menu Grid with 3 Columns per Row & Colorful Product Illustrations
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp, top = 2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedMenuItems, key = { it.id }) { item ->
                    MenuCatalogCard(
                        item = item,
                        media = cartMedia,
                        onClick = {
                            if (!isStoreOpen) {
                                viewModel.showBanner("Toko sedang tutup. Buka toko terlebih dahulu untuk melakukan transaksi.")
                                return@MenuCatalogCard
                            }
                            if (item.category.equals("Egg Waffle", ignoreCase = true)) {
                                clickedWaffleForModal = item
                            } else if (item.category.equals("Drinks", ignoreCase = true)) {
                                clickedDrinkForModal = item
                            } else {
                                // Check if item explicitly defines allowed modifiers
                                val itemAllowedMods = item.getAllowedModifiers()
                                val modifierGroups: Map<String, List<CatalogRecord>> = if (itemAllowedMods.isNotEmpty()) {
                                    val map = mutableMapOf<String, List<CatalogRecord>>()
                                    itemAllowedMods.forEach { (catName, itemNames) ->
                                        val records = catalog.filter {
                                            it.recordType == "MODIFIER" && it.isActive &&
                                                it.category.equals(catName, ignoreCase = true) &&
                                                (itemNames.isEmpty() || it.name in itemNames)
                                        }
                                        if (records.isNotEmpty()) {
                                            map[catName] = records
                                        }
                                    }
                                    map
                                } else {
                                    // Fallback: check connected modifier categories
                                    val connectedModCats = modifierToMenuCategoryMap.filter { (_, targetMenuCat) ->
                                        targetMenuCat.equals(item.category, ignoreCase = true)
                                    }.keys

                                    catalog.filter {
                                        it.recordType == "MODIFIER" && it.isActive &&
                                            (it.category in connectedModCats || it.category.equals(item.category, ignoreCase = true))
                                    }.groupBy { it.category }
                                }

                                if (modifierGroups.isNotEmpty()) {
                                    clickedGenericItemForModal = item to modifierGroups
                                } else {
                                    viewModel.addCustomMenuItemToCart(item, emptyList(), 0L, 0L, 0L, 0L, 1)
                                }
                            }
                        }
                    )
                }
            }
        }

        // RIGHT PANE: FLOATING SOFT CART PANEL (40% width)
        WaffleoFloatingCard(
            modifier = Modifier
                .weight(0.40f)
                .fillMaxHeight(),
            cornerRadius = 24.dp,
            elevation = 5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Banner when Editing an Order recalled from Orders Screen
                editingOrder?.let { editing ->
                    Surface(
                        color = SoftAmberBg,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Edit Mode",
                                    tint = HoneyAmberWarning
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Edit Pesanan ${editing.orderNumber} (Urutan #${editing.sequenceIndex})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = RichEspressoPrimary
                                    )
                                    if (editing.paidAmount > 0L) {
                                        Text(
                                            text = "Sudah dibayar: ${WaffleoViewModel.formatIdr(editing.paidAmount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MatchaSageSuccess
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = { viewModel.cancelEditingMode() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Batal Edit", tint = RichEspressoPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                val activePlatforms = remember(catalog) {
                    catalog.filter { it.recordType == "BRANCH_CONFIG" && it.isActive }
                        .map { it.category.ifBlank { it.name }.trim() }
                        .filter { it.isNotBlank() }
                        .distinct()
                }

                val availableMediaOptions = remember(activePlatforms) {
                    val base = listOf(OrderMedia.WALK_IN, OrderMedia.WHATSAPP, OrderMedia.INSTAGRAM)
                    val platformMedias = activePlatforms.mapNotNull { platName ->
                        OrderMedia.entries.firstOrNull { it.label.equals(platName, ignoreCase = true) }
                            ?: OrderMedia.entries.firstOrNull { it.name.equals(platName, ignoreCase = true) }
                    }
                    base + platformMedias
                }

                androidx.compose.runtime.LaunchedEffect(availableMediaOptions) {
                    if (cartMedia !in availableMediaOptions) {
                        viewModel.setCartOrderMedia(OrderMedia.WALK_IN)
                    }
                }

                // TOP SECTION OF CART: [ Media Pesanan ] & [ Customer Data ] (Soft Pill Inputs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    WaffleoSingleSelectDropdown(
                        label = "Media Pesanan",
                        selectedText = cartMedia.label,
                        options = availableMediaOptions,
                        optionLabel = { it.label },
                        onOptionSelected = {
                            viewModel.setCartOrderMedia(it)
                            validationErrorText = null
                        },
                        modifier = Modifier.weight(0.46f),
                        testTagPrefix = "cart_media"
                    )

                    Box(modifier = Modifier.weight(0.54f)) {
                        var fieldSize by remember { mutableStateOf(IntSize.Zero) }
                        val density = LocalDensity.current
                        val fieldWidthDp = with(density) { fieldSize.width.toDp() }

                        WaffleoSoftTextField(
                            value = customerSuffix,
                            onValueChange = { newValue ->
                                viewModel.setCustomerInputSuffix(newValue)
                                customerDismissedText = ""
                                validationErrorText = null
                            },
                            label = if (cartMedia.requiresCustomerCode) "Customer Data *" else "Customer Data",
                            prefixText = cartMedia.prefix,
                            placeholder = if (cartMedia.requiresCustomerCode) "Nomor ID..." else "Nama / Meja",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged { fieldSize = it }
                                .testTag("cart_customer_data_input")
                        )

                        // Floating Customer Data Autofill Overlay on top of cart layer (does not push items below)
                        if (!cartMedia.requiresCustomerCode && customerSuggestions.isNotEmpty()) {
                            Popup(
                                alignment = Alignment.TopStart,
                                offset = IntOffset(0, fieldSize.height + with(density) { 4.dp.roundToPx() }),
                                onDismissRequest = {
                                    customerDismissedText = customerSuffix
                                },
                                properties = PopupProperties(
                                    focusable = false,
                                    dismissOnBackPress = false,
                                    dismissOnClickOutside = false
                                )
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = RichEspressoPrimary,
                                    border = BorderStroke(1.dp, HoneyCaramelAccent),
                                    shadowElevation = 14.dp,
                                    modifier = Modifier
                                        .width(if (fieldWidthDp > 0.dp) fieldWidthDp else 240.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Customer : ",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = HoneyCaramelAccent
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        customerDismissedText = customerSuffix
                                                        keyboardController?.hide()
                                                        focusManager.clearFocus()
                                                    }
                                                    .testTag("cart_customer_close_btn"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Tutup",
                                                    tint = ElevatedIvoryCard,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 160.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            customerSuggestions.forEach { suggestion ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = ElevatedIvoryCard,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .clickable {
                                                            val chosen = suggestion
                                                            suggestionSelectedAtMillis = System.currentTimeMillis()
                                                            viewModel.setCustomerInputSuffix(chosen)
                                                            customerDismissedText = chosen
                                                            keyboardController?.hide()
                                                            focusManager.clearFocus()
                                                        }
                                                        .testTag("cart_customer_suggestion_$suggestion")
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = suggestion,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = RichEspressoPrimary
                                                        )
                                                        Text(
                                                            text = "Pilih ↵",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = HoneyCaramelAccent
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

                // DATA LAMPAU ROW: Only for Supervisor / Owner / Developer
                // Left (under Media Pesanan): "[ ] Data lampau"
                // Right (under Customer Data): Compact "Tanggal" & "Waktu" buttons with top-layer Popup overlays
                if (session.role.canInputBackdatedOrder) {
                    var showDatePopup by remember { mutableStateOf(false) }
                    var showTimePopup by remember { mutableStateOf(false) }
                    var backdateRowSize by remember { mutableStateOf(IntSize.Zero) }
                    val density = LocalDensity.current

                    val formattedShortDate = remember(cartBackdatedDateKey) {
                        try {
                            val inFmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            val outFmt = java.text.SimpleDateFormat("dd MMM yy", java.util.Locale.forLanguageTag("id-ID"))
                            val parsed = inFmt.parse(cartBackdatedDateKey)
                            if (parsed != null) outFmt.format(parsed) else cartBackdatedDateKey
                        } catch (e: Exception) {
                            cartBackdatedDateKey
                        }
                    }
                    val formatted24hTime = remember(cartBackdatedTimeText) {
                        cartBackdatedTimeText.replace(':', '.')
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .onSizeChanged { backdateRowSize = it },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left column directly below Media Pesanan: "[ ] Data lampau"
                        Row(
                            modifier = Modifier
                                .weight(0.46f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val next = !cartIsBackdated
                                    viewModel.setCartBackdatedEnabled(next)
                                    if (!next) {
                                        showDatePopup = false
                                        showTimePopup = false
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .testTag("cart_backdate_checkbox"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (cartIsBackdated) RichEspressoPrimary else ElevatedIvoryCard,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (cartIsBackdated) RichEspressoPrimary else MutedCocoaText.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.size(16.dp)
                            ) {
                                if (cartIsBackdated) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Tercentang",
                                            tint = HoneyCaramelAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Data lampau",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (cartIsBackdated) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = if (cartIsBackdated) RichEspressoPrimary else MutedCocoaText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Right column directly below Customer Data: Compact "Tanggal" & "Waktu" buttons
                        Box(modifier = Modifier.weight(0.54f)) {
                            if (cartIsBackdated) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Compact Tanggal Button
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (showDatePopup) SoftGoldHighlight else SoftOatSurface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (showDatePopup) HoneyCaramelAccent else SubtleSandBorder
                                        ),
                                        modifier = Modifier
                                            .weight(1.12f)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                showTimePopup = false
                                                showDatePopup = !showDatePopup
                                            }
                                            .testTag("cart_backdate_date_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f, fill = false)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = "Tanggal",
                                                    tint = DeepCaramelGold,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Tanggal",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = MutedCocoaText,
                                                    maxLines = 1
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = formattedShortDate,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp
                                                ),
                                                color = RichEspressoPrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Compact Waktu Button (24h: 00.00 - 23.59)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (showTimePopup) SoftGoldHighlight else SoftOatSurface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (showTimePopup) HoneyCaramelAccent else SubtleSandBorder
                                        ),
                                        modifier = Modifier
                                            .weight(0.88f)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                showDatePopup = false
                                                showTimePopup = !showTimePopup
                                            }
                                            .testTag("cart_backdate_time_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f, fill = false)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccessTime,
                                                    contentDescription = "Waktu",
                                                    tint = DeepCaramelGold,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Waktu",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = MutedCocoaText,
                                                    maxLines = 1
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = formatted24hTime,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp
                                                ),
                                                color = RichEspressoPrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                // Top-layer Floating Popup for Tanggal (Does NOT push content below)
                                if (showDatePopup) {
                                    Popup(
                                        alignment = Alignment.TopEnd,
                                        offset = IntOffset(
                                            0,
                                            backdateRowSize.height + with(density) { 4.dp.roundToPx() }
                                        ),
                                        onDismissRequest = { showDatePopup = false },
                                        properties = PopupProperties(
                                            focusable = true,
                                            dismissOnBackPress = true,
                                            dismissOnClickOutside = true
                                        )
                                    ) {
                                        BackdateCalendarTopPopup(
                                            selectedDateKey = cartBackdatedDateKey,
                                            onSelectDateKey = { chosenDateKey ->
                                                viewModel.setCartBackdatedDateKey(chosenDateKey)
                                                showDatePopup = false
                                            },
                                            onDismiss = { showDatePopup = false }
                                        )
                                    }
                                }

                                // Top-layer Floating Popup for Waktu 24 Jam (00.00 - 23.59, Does NOT push content below)
                                if (showTimePopup) {
                                    Popup(
                                        alignment = Alignment.TopEnd,
                                        offset = IntOffset(
                                            0,
                                            backdateRowSize.height + with(density) { 4.dp.roundToPx() }
                                        ),
                                        onDismissRequest = { showTimePopup = false },
                                        properties = PopupProperties(
                                            focusable = true,
                                            dismissOnBackPress = true,
                                            dismissOnClickOutside = true
                                        )
                                    ) {
                                        Backdate24HourTimeTopPopup(
                                            selectedTimeText = cartBackdatedTimeText,
                                            onSelectTimeText = { chosenTime ->
                                                viewModel.setCartBackdatedTimeText(chosenTime)
                                                showTimePopup = false
                                            },
                                            onDismiss = { showTimePopup = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (validationErrorText != null) {
                    Surface(
                        color = SoftTerracottaBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Text(
                            text = validationErrorText.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            color = TerracottaDanger,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // MIDDLE SCROLLABLE SECTION: CART ITEMS + PROMO + NOTES
                val promoOptions = remember(activePromos) {
                    listOf(
                        CatalogRecord(id = "AUTO", name = "✨ Otomatis (Promo Terbaik)"),
                        CatalogRecord(id = "NONE", name = "Tanpa Promo")
                    ) + activePromos
                }
                val effectiveSelectedMode = selectedPromoId.ifBlank { "AUTO" }
                val currentPromoObj = promoOptions.firstOrNull { it.id == effectiveSelectedMode } ?: promoOptions.first()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (cartItems.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = SoftOatSurface.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 22.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ElevatedIvoryCard,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingBag,
                                                contentDescription = null,
                                                tint = ChampagneGoldAccent,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Keranjang Pesanan Kosong",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = RichEspressoPrimary
                                    )
                                    Text(
                                        text = "Sentuh kartu Egg Waffle atau Drinks di kiri",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedCocoaText
                                    )
                                }
                            }
                        }
                    } else {
                        items(cartItems, key = { it.lineId }) { lineItem ->
                            CartItemRow(
                                item = lineItem,
                                media = cartMedia,
                                onDecrement = { viewModel.updateCartItemQuantity(lineItem.lineId, -1) },
                                onIncrement = { viewModel.updateCartItemQuantity(lineItem.lineId, +1) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            WaffleoSingleSelectDropdown(
                                label = "Promo Waffleo",
                                selectedText = currentPromoObj.name,
                                options = promoOptions,
                                optionLabel = { it.name },
                                onOptionSelected = { viewModel.setSelectedPromo(it.id) },
                                modifier = Modifier.weight(0.48f),
                                testTagPrefix = "cart_promo"
                            )

                            WaffleoSoftTextField(
                                value = cartNotes,
                                onValueChange = { viewModel.setCartNotes(it) },
                                label = "Notes Customer",
                                placeholder = "Catatan khusus...",
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                modifier = Modifier.weight(0.52f),
                                testTag = "cart_notes_input"
                            )
                        }
                        if (hasAnyVoucherPromo || cartVoucherCode.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            WaffleoSoftTextField(
                                value = cartVoucherCode,
                                onValueChange = { viewModel.setCartVoucherCode(it) },
                                label = "Kode Voucher Promo (Opsional)",
                                placeholder = "Masukkan kode promo...",
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "cart_voucher_input"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // PINNED BOTTOM FOOTER: Total Harga Pesanan + Floating Pill Buttons [ Pay / Paid ] & [ Add to Orders ]
                val promoEvaluation = viewModel.evaluateCartPromoDetails(
                    items = cartItems,
                    media = cartMedia,
                    promoIdOrMode = effectiveSelectedMode,
                    voucherCode = cartVoucherCode,
                    allCatalog = catalog
                )
                val subtotal = promoEvaluation.subtotalBeforeDiscount
                val discount = promoEvaluation.totalDiscountAmount
                val totalAmount = promoEvaluation.finalTotalAmount
                val platformDeduction = viewModel.calculatePlatformDeduction(totalAmount, cartMedia, catalog)
                val netBcaAmount = viewModel.calculateNetEnteredSystemAmount(totalAmount, cartMedia, catalog)

                val paidSoFar = if (cartMedia.isPlatform) 0L else maxOf(editingOrder?.paidAmount ?: 0L, cartPaidAmount)
                val cartShortfall = if (!cartMedia.isPlatform && paidSoFar > 0L && totalAmount > paidSoFar) {
                    totalAmount - paidSoFar
                } else 0L
                val cartChangeDue = if (!cartMedia.isPlatform && paidSoFar > 0L && totalAmount < paidSoFar) {
                    paidSoFar - totalAmount
                } else 0L

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftOatSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        if (discount > 0L && promoEvaluation.appliedPromos.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal (${cartItems.sumOf { it.qty }} item)", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                                Text(WaffleoViewModel.formatIdr(subtotal), style = MaterialTheme.typography.labelMedium)
                            }
                            promoEvaluation.appliedPromos.forEach { applied ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Promo ${applied.promoName} (${applied.badgeSummary})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MatchaSageSuccess,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "-${WaffleoViewModel.formatIdr(applied.discountAmount)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MatchaSageSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (applied.matchedItemSummary.isNotBlank()) {
                                    Text(
                                        text = "↳ ${applied.matchedItemSummary}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedCocoaText
                                    )
                                }
                            }
                        } else if (promoEvaluation.statusNote.isNotBlank() && effectiveSelectedMode != "AUTO" && effectiveSelectedMode != "NONE" && cartItems.isNotEmpty()) {
                            Text(
                                text = "Info Promo: ${promoEvaluation.statusNote}",
                                style = MaterialTheme.typography.labelSmall,
                                color = HoneyAmberWarning,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (cartMedia.isPlatform) "Total (${cartMedia.label})" else "Total Pesanan",
                                style = MaterialTheme.typography.titleMedium,
                                color = MutedCocoaText
                            )
                            Text(
                                text = WaffleoViewModel.formatIdr(totalAmount),
                                style = MaterialTheme.typography.headlineMedium,
                                color = RichEspressoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (cartShortfall > 0L) {
                            Text(
                                text = "Sudah Dibayar: ${WaffleoViewModel.formatIdr(paidSoFar)} • Kurang Bayar: ${WaffleoViewModel.formatIdr(cartShortfall)} (Belum Lunas)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TerracottaDanger,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        } else if (cartChangeDue > 0L) {
                            Text(
                                text = "Sudah Dibayar: ${WaffleoViewModel.formatIdr(paidSoFar)} • Kembalian Tunai: ${WaffleoViewModel.formatIdr(cartChangeDue)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MatchaSageSuccess,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Pill Buttons: [ Pay / Paid ] & [ Add to Orders / Apply Changes ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isCurrentlyPaid = (cartMedia.isPlatform || (cartIsPaid && cartShortfall == 0L) || (paidSoFar >= totalAmount && totalAmount > 0L)) && totalAmount > 0L
                    WaffleoPillButton(
                        text = when {
                            cartMedia.isPlatform -> "Paid"
                            isCurrentlyPaid -> "Paid"
                            cartShortfall > 0L -> "Bayar Sisa (${WaffleoViewModel.formatIdr(cartShortfall)})"
                            else -> "Pay"
                        },
                        onClick = {
                            if (!isStoreOpen) {
                                viewModel.showBanner("Toko sedang tutup. Buka toko terlebih dahulu untuk melakukan transaksi.")
                                return@WaffleoPillButton
                            }
                            if (cartItems.isNotEmpty()) {
                                if (cartMedia.isPlatform) {
                                    viewModel.showBanner("Pembayaran ${cartMedia.label} otomatis masuk ke Rekening Bank BCA.")
                                } else if (!isCurrentlyPaid) {
                                    showCartPayModal = true
                                }
                            }
                        },
                        enabled = cartItems.isNotEmpty(),
                        variant = if (isCurrentlyPaid || cartMedia.isPlatform) {
                            WaffleoPillButtonVariant.SuccessSage
                        } else {
                            WaffleoPillButtonVariant.CreamGold
                        },
                        icon = if (isCurrentlyPaid || cartMedia.isPlatform) Icons.Default.CheckCircle else Icons.Default.Payment,
                        modifier = Modifier.weight(0.38f),
                        testTag = "cart_pay_button"
                    )

                    WaffleoPillButton(
                        text = if (editingOrder != null) "Apply Perubahan (#${editingOrder?.orderNumber})" else "Add to Orders",
                        onClick = {
                            viewModel.submitCartToOrders(
                                onValidationError = { msg -> validationErrorText = msg },
                                onSuccess = { validationErrorText = null }
                            )
                        },
                        variant = if (editingOrder != null) {
                            WaffleoPillButtonVariant.WarningAmber
                        } else {
                            WaffleoPillButtonVariant.PrimaryEspresso
                        },
                        modifier = Modifier.weight(0.62f),
                        testTag = "cart_submit_order_button"
                    )
                }
            }
        }
    }
}

    // Modal for Egg Waffle Customization
    clickedWaffleForModal?.let { primaryWaffle ->
        val allowedMap = primaryWaffle.getAllowedModifiers()
        val effectiveFlavours = if (allowedMap.isNotEmpty()) {
            val allowedFlv = allowedMap.entries
                .firstOrNull { it.key.equals("Flavour", ignoreCase = true) || it.key.equals("FLAVOUR", ignoreCase = true) }
                ?.value
            if (allowedFlv != null) activeFlavours.filter { it.name in allowedFlv } else activeFlavours
        } else {
            activeFlavours
        }
        val effectiveAddOns = if (allowedMap.isNotEmpty()) {
            val allowedAdds = allowedMap.entries
                .firstOrNull { it.key.contains("Add", ignoreCase = true) }
                ?.value
            if (allowedAdds != null) activeAddOns.filter { it.name in allowedAdds } else activeAddOns
        } else {
            activeAddOns
        }

        EggWaffleCustomizationDialog(
            initialPrimaryTopping = primaryWaffle,
            allPrimaryToppings = activeWaffles,
            flavours = effectiveFlavours,
            addOns = effectiveAddOns,
            currentMedia = cartMedia,
            calculatePrice = { p1, isHalf, isDouble, p2, flv, adds ->
                viewModel.calculateEggWafflePriceForMedia(cartMedia, p1, isHalf, isDouble, p2, flv, adds)
            },
            onDismiss = { clickedWaffleForModal = null },
            onAddToCart = { p1, isHalf, isDouble, p2, flv, adds, qty ->
                viewModel.addEggWaffleToCart(p1, isHalf, isDouble, p2, flv, adds, qty)
            }
        )
    }

    // Modal for Drinks Customization
    clickedDrinkForModal?.let { drinkItem ->
        val allowedMap = drinkItem.getAllowedModifiers()
        val effectiveSweetness = if (allowedMap.isNotEmpty()) {
            val allowedSweet = allowedMap.entries
                .firstOrNull { it.key.contains("Sweet", ignoreCase = true) }
                ?.value
            if (allowedSweet != null) activeSweetness.filter { it.name in allowedSweet } else activeSweetness
        } else {
            activeSweetness
        }
        val effectiveIce = if (allowedMap.isNotEmpty()) {
            val allowedIceList = allowedMap.entries
                .firstOrNull { it.key.contains("Ice", ignoreCase = true) }
                ?.value
            if (allowedIceList != null) activeIce.filter { it.name in allowedIceList } else activeIce
        } else {
            activeIce
        }

        DrinkCustomizationDialog(
            drink = drinkItem,
            sweetnessOptions = effectiveSweetness,
            iceOptions = effectiveIce,
            currentMedia = cartMedia,
            onDismiss = { clickedDrinkForModal = null },
            onAddToCart = { drink, sweet, ice, qty ->
                viewModel.addDrinkToCart(drink, sweet, ice, qty)
            }
        )
    }

    // Modal for Custom Menu Category Customization
    clickedGenericItemForModal?.let { (customItem, modGroups) ->
        GenericItemCustomizationDialog(
            item = customItem,
            modifierGroups = modGroups,
            currentMedia = cartMedia,
            onDismiss = { clickedGenericItemForModal = null },
            onAddToCart = { item, selectedMods, extraWalkIn, extraGrab, extraGo, extraShopee, qty ->
                viewModel.addCustomMenuItemToCart(
                    item,
                    selectedMods,
                    extraWalkIn,
                    extraGrab,
                    extraGo,
                    extraShopee,
                    qty
                )
            }
        )
    }

    // Modal for Paying inside Cart
    if (showCartPayModal) {
        val payEval = viewModel.evaluateCartPromoDetails(
            items = cartItems,
            media = cartMedia,
            promoIdOrMode = selectedPromoId.ifBlank { "AUTO" },
            voucherCode = cartVoucherCode,
            allCatalog = catalog
        )
        val totalBill = payEval.finalTotalAmount
        val originalSubtotal = payEval.subtotalBeforeDiscount
        val promoDiscount = payEval.totalDiscountAmount
        val alreadyPaid = if (cartMedia.isPlatform) 0L else maxOf(editingOrder?.paidAmount ?: 0L, cartPaidAmount)
        PaymentModalDialog(
            title = "Pembayaran Keranjang (${cartMedia.label})",
            totalBillAmount = totalBill,
            alreadyPaidAmount = if (totalBill > alreadyPaid) alreadyPaid else 0L,
            defaultMedia = cartMedia.label,
            originalSubtotalAmount = originalSubtotal,
            promoDiscountAmount = promoDiscount,
            promoCode = payEval.primaryPromoName,
            onDismiss = { showCartPayModal = false },
            onConfirmPayment = { method, amountPaid, splitCash, splitQris, qrisBank, changeDue ->
                viewModel.markCartPaid(method, amountPaid, splitCash, splitQris, qrisBank, changeDue)
            }
        )
    }
}

/**
 * Modern Web/HTML POS Visual Product Card featuring custom food/drink Canvas illustration,
 * soft floating category pill, clean Outfit typography, and circular '+' action button.
 */
@Composable
private fun MenuCatalogCard(
    item: CatalogRecord,
    media: OrderMedia,
    onClick: () -> Unit
) {
    val isWaffle = item.category == "Egg Waffle"
    val subCat = remember(item.subCategory, item.metadataJson) {
        val direct = item.subCategory.trim()
        if (direct.isNotBlank()) {
            direct
        } else {
            try {
                JSONObject(item.metadataJson).optString("subCategory", "").trim()
            } catch (e: Exception) {
                ""
            }
        }
    }

    WaffleoFloatingCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        elevation = 3.dp,
        onClick = onClick,
        testTag = "menu_card_${item.id}"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Colorful Product Illustration Header with Floating Pill Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .height(82.dp)
                    .clip(RoundedCornerShape(15.dp))
            ) {
                WaffleoProductIllustration(
                    itemName = item.name,
                    isWaffle = isWaffle,
                    imageUrl = item.imageUrl,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Translucent Category Pill (only if item has a subCategory)
                if (subCat.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = subCat,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = RichEspressoPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichEspressoPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WaffleoPillBadge(
                        text = WaffleoViewModel.formatIdr(item.priceForMedia(media)),
                        containerColor = SoftGoldHighlight,
                        contentColor = DeepCaramelGold,
                        horizontalPadding = 9.dp,
                        verticalPadding = 3.dp
                    )
                    Surface(
                        shape = CircleShape,
                        color = RichEspressoPrimary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah",
                                tint = ElevatedIvoryCard,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CartItemRow(
    item: CartLineItem,
    media: OrderMedia,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ElevatedIvoryCard,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top Row: Thumbnail + Item Title & Price + Stepper [ -  qty  + ]
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
                    WaffleoProductIllustration(
                        itemName = item.primaryItemName,
                        isWaffle = item.category == "Egg Waffle",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        val annotatedTitle = remember(item) { item.buildAnnotatedCartTitle() }
                        Text(
                            text = annotatedTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.5.sp,
                                letterSpacing = (-0.1).sp
                            ),
                            color = RichEspressoPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = WaffleoViewModel.formatIdr(item.lineTotalFor(media)),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = DeepCaramelGold
                            )
                        )
                    }
                }

                // Stepper [ -  qty  + ]
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SoftOatSurface,
                    border = BorderStroke(0.5.dp, SubtleSandBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElevatedIvoryCard,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onDecrement)
                                .testTag("cart_qty_minus_${item.lineId}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Kurangi Qty",
                                    tint = RichEspressoPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Text(
                            text = item.qty.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Surface(
                            shape = CircleShape,
                            color = RichEspressoPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onIncrement)
                                .testTag("cart_qty_plus_${item.lineId}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah Qty",
                                    tint = SoftGoldHighlight,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Flow: Badges & Chips (Adonan, Toppings 1/2 or Double, dan SEMUA Add-on)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (item.category == "Egg Waffle") {
                    if (item.addOnNames.isNotEmpty()) {
                        item.addOnNames.forEach { addon ->
                            val isCheeseInWaffle = addon.contains("keju dalam waffle", ignoreCase = true) ||
                                (addon.contains("keju", ignoreCase = true) && addon.contains("waffle", ignoreCase = true))
                            val addOnText = if (addon.startsWith("+")) addon else "+$addon"

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCheeseInWaffle) SoftAmberBg else SoftSageBg,
                                border = if (isCheeseInWaffle) BorderStroke(0.5.dp, HoneyAmberWarning) else null
                            ) {
                                Text(
                                    text = addOnText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isCheeseInWaffle) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isCheeseInWaffle) DeepCaramelGold else MatchaSageSuccess,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SoftOatSurface
                        ) {
                            Text(
                                text = "Tanpa Add-on",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCocoaText,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else if (item.category == "Drinks") {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftGoldHighlight.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "Ice: ${item.icedLevel} • Sugar: ${item.sweetnessLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = RichEspressoPrimary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (item.addOnNames.isNotEmpty()) {
                    item.addOnNames.forEach { mod ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SoftGoldHighlight.copy(alpha = 0.85f),
                            border = BorderStroke(0.5.dp, ChampagneGoldAccent)
                        ) {
                            Text(
                                text = mod,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = RichEspressoPrimary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackdateCalendarTopPopup(
    selectedDateKey: String,
    onSelectDateKey: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialCal = remember(selectedDateKey) {
        val cal = java.util.Calendar.getInstance()
        try {
            val parts = selectedDateKey.split("-")
            if (parts.size == 3) {
                cal.set(java.util.Calendar.YEAR, parts[0].toInt())
                cal.set(java.util.Calendar.MONTH, parts[1].toInt() - 1)
                cal.set(java.util.Calendar.DAY_OF_MONTH, parts[2].toInt())
            }
        } catch (_: Exception) {
        }
        cal
    }

    var displayedYear by remember { mutableIntStateOf(initialCal.get(java.util.Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(initialCal.get(java.util.Calendar.MONTH)) }

    val monthNames = remember {
        listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
    }
    val dayHeaders = remember { listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab") }

    val daysInMonthInfo = remember(displayedYear, displayedMonth) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, displayedYear)
            set(java.util.Calendar.MONTH, displayedMonth)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
        val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        firstDayOfWeek to maxDays
    }

    val todayKey = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ElevatedIvoryCard,
        border = BorderStroke(1.5.dp, HoneyCaramelAccent),
        shadowElevation = 18.dp,
        modifier = Modifier
            .width(268.dp)
            .testTag("cart_backdate_calendar_popup")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header: Title + Today quick button + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pilih Tanggal Pesanan",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichEspressoPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftGoldHighlight,
                        border = BorderStroke(0.8.dp, ChampagneGoldAccent),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelectDateKey(todayKey) }
                    ) {
                        Text(
                            text = "Hari Ini",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = DeepCaramelGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MutedCocoaText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Month & Year switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftOatSurface, RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (displayedMonth == 0) {
                                displayedMonth = 11
                                displayedYear -= 1
                            } else {
                                displayedMonth -= 1
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Bulan Sebelumnya",
                        tint = RichEspressoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${monthNames.getOrElse(displayedMonth) { "" }} $displayedYear",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichEspressoPrimary
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (displayedMonth == 11) {
                                displayedMonth = 0
                                displayedYear += 1
                            } else {
                                displayedMonth += 1
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Bulan Berikutnya",
                        tint = RichEspressoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Day of Week Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { dName ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MutedCocoaText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar Grid
            val (firstOffset, maxDays) = daysInMonthInfo
            val totalCells = ((firstOffset + maxDays + 6) / 7) * 7
            val rows = totalCells / 7

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNum = cellIndex - firstOffset + 1
                            if (dayNum in 1..maxDays) {
                                val cellDateKey = String.format(
                                    java.util.Locale.US,
                                    "%04d-%02d-%02d",
                                    displayedYear,
                                    displayedMonth + 1,
                                    dayNum
                                )
                                val isSelected = cellDateKey == selectedDateKey
                                val isToday = cellDateKey == todayKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                isSelected -> RichEspressoPrimary
                                                isToday -> SoftGoldHighlight
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { onSelectDateKey(cellDateKey) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = when {
                                            isSelected -> ElevatedIvoryCard
                                            isToday -> DeepCaramelGold
                                            else -> RichEspressoPrimary
                                        }
                                    )
                                }
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
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
private fun Backdate24HourTimeTopPopup(
    selectedTimeText: String,
    onSelectTimeText: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialParts = remember(selectedTimeText) {
        val clean = selectedTimeText.replace('.', ':').split(":")
        val h = clean.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
        val m = clean.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        h to m
    }

    var selectedHour by remember(selectedTimeText) { mutableIntStateOf(initialParts.first) }
    var selectedMinute by remember(selectedTimeText) { mutableIntStateOf(initialParts.second) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ElevatedIvoryCard,
        border = BorderStroke(1.5.dp, HoneyCaramelAccent),
        shadowElevation = 18.dp,
        modifier = Modifier
            .width(236.dp)
            .testTag("cart_backdate_time_popup")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Waktu 24 Jam (00.00 - 23.59)",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = RichEspressoPrimary
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = MutedCocoaText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Current 24h Preview Banner + Reset 00.00
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SoftOatSurface,
                border = BorderStroke(1.dp, SubtleSandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%02d.%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        ),
                        color = RichEspressoPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElevatedIvoryCard,
                            border = BorderStroke(0.8.dp, SubtleSandBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedHour = 0
                                    selectedMinute = 0
                                }
                        ) {
                            Text(
                                text = "00.00",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = MutedCocoaText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SoftGoldHighlight,
                            border = BorderStroke(0.8.dp, ChampagneGoldAccent),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val cal = java.util.Calendar.getInstance()
                                    selectedHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                                    selectedMinute = cal.get(java.util.Calendar.MINUTE)
                                }
                        ) {
                            Text(
                                text = "Sekarang",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = DeepCaramelGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Two Scrollable Columns: Jam (00..23) & Menit (00..59)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Jam Column (00 - 23)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(SoftOatSurface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Jam (00-23)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = MutedCocoaText
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items((0..23).toList()) { h ->
                            val isSel = h == selectedHour
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) RichEspressoPrimary else Color.Transparent)
                                    .clickable { selectedHour = h }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%02d", h),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSel) ElevatedIvoryCard else RichEspressoPrimary
                                )
                            }
                        }
                    }
                }

                // Menit Column (00 - 59)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(SoftOatSurface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Menit (00-59)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = MutedCocoaText
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items((0..59).toList()) { m ->
                            val isSel = m == selectedMinute
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) RichEspressoPrimary else Color.Transparent)
                                    .clickable { selectedMinute = m }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%02d", m),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSel) ElevatedIvoryCard else RichEspressoPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onSelectTimeText(
                        String.format(java.util.Locale.US, "%02d:%02d", selectedHour, selectedMinute)
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RichEspressoPrimary,
                    contentColor = ElevatedIvoryCard
                ),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .testTag("cart_backdate_time_confirm_btn")
            ) {
                Text(
                    text = "Pilih Waktu (${String.format(java.util.Locale.US, "%02d.%02d", selectedHour, selectedMinute)})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

