package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MoneyAccountGroupDef
import com.example.data.model.MoneyBookmarkRecord
import com.example.data.model.MoneyCategoryDef
import com.example.data.model.MoneyRepetitionOption
import com.example.data.model.MoneyTransactionRecord
import com.example.data.model.MoneyTransactionType
import com.example.data.model.StaffRole
import com.example.ui.theme.CaramelGold
import com.example.ui.theme.ElevatedIvoryCard
import com.example.ui.theme.HoneyCaramelAccent
import com.example.ui.theme.MatchaSageSuccess
import com.example.ui.theme.MutedCocoaText
import com.example.ui.theme.RichEspressoPrimary
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SoftSageBg
import com.example.ui.theme.SoftTerracottaBg
import com.example.ui.theme.SubtleSandBorder
import com.example.ui.theme.TerracottaDanger
import com.example.ui.theme.WarmAlabasterBg
import com.example.ui.viewmodel.WaffleoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val MoneyIncomeBlue = Color(0xFF2B7CE9)
private val MoneyIncomeSoftBg = Color(0xFFE8F1FD)
private val MoneyExpenseRed = Color(0xFFE5533D)
private val MoneyExpenseSoftBg = Color(0xFFFDECE9)
private val MoneyTransferSlate = Color(0xFF5C6B73)

private val PieChartPalette = listOf(
    Color(0xFFE5533D), // Coral Red
    Color(0xFFF39C12), // Warm Orange
    Color(0xFFF1C40F), // Golden Yellow
    Color(0xFF8BC34A), // Fresh Green
    Color(0xFF26A69A), // Teal
    Color(0xFF2B7CE9), // Ocean Blue
    Color(0xFF5C6BC0), // Indigo
    Color(0xFFAB47BC), // Purple
    Color(0xFF8D6E63)  // Cocoa Brown
)

private enum class MoneyMainTab(val label: String) {
    TRANS("Trans."),
    STATS("Stats"),
    ACCOUNTS("Accounts")
}

private enum class TransSubView(val label: String) {
    DAILY("Daily"),
    MONTHLY("Monthly")
}

@Composable
fun MoneyManagerScreen(
    viewModel: WaffleoViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val allTransactions by viewModel.moneyTransactions.collectAsStateWithLifecycle()
    val bookmarks by viewModel.moneyBookmarks.collectAsStateWithLifecycle()
    val accountGroups by viewModel.moneyAccountGroups.collectAsStateWithLifecycle()
    val categories by viewModel.moneyCategories.collectAsStateWithLifecycle()
    val selectedMonthKey by viewModel.moneySelectedMonthKey.collectAsStateWithLifecycle()
    val consolidateAll by viewModel.moneyConsolidateAllBranches.collectAsStateWithLifecycle()
    val dailyPosItemSummaryMap by viewModel.dailyPosItemSummaryMap.collectAsStateWithLifecycle()

    var activeMainTab by remember { mutableStateOf(MoneyMainTab.TRANS) }
    var activeTransSubView by remember { mutableStateOf(TransSubView.DAILY) }
    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAccountFilter by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Modals state
    var showBookmarksDialog by remember { mutableStateOf(false) }
    var showTransactionModal by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<MoneyTransactionRecord?>(null) }
    var prefillFromBookmark by remember { mutableStateOf<MoneyBookmarkRecord?>(null) }

    // Filter transactions for the selected month and optional search/account filter
    val monthTransactions = remember(allTransactions, selectedMonthKey, searchQuery, selectedAccountFilter) {
        allTransactions.filter { tx ->
            val matchesMonth = tx.dateKey.startsWith(selectedMonthKey)
            val matchesAccount = selectedAccountFilter?.let { (grp, acc) ->
                (tx.accountGroup == grp && tx.accountName == acc) ||
                    (tx.type == MoneyTransactionType.TRANSFER && tx.toAccountGroup == grp && tx.toAccountName == acc)
            } ?: true
            val q = searchQuery.trim()
            val matchesSearch = q.isEmpty() ||
                tx.note.contains(q, ignoreCase = true) ||
                tx.categoryName.contains(q, ignoreCase = true) ||
                tx.subCategoryName.contains(q, ignoreCase = true) ||
                tx.accountName.contains(q, ignoreCase = true) ||
                tx.toAccountName.contains(q, ignoreCase = true) ||
                tx.tag.contains(q, ignoreCase = true) ||
                tx.description.contains(q, ignoreCase = true)
            matchesMonth && matchesAccount && matchesSearch
        }
    }

    val monthIncome = remember(monthTransactions) {
        monthTransactions.filter { it.type == MoneyTransactionType.INCOME }.sumOf { it.amount }
    }
    val monthExpense = remember(monthTransactions) {
        monthTransactions.filter { it.type == MoneyTransactionType.EXPENSE }.sumOf { it.amount + it.feeAmount } +
            monthTransactions.filter { it.type == MoneyTransactionType.TRANSFER }.sumOf { it.feeAmount }
    }
    val monthTotalNet = monthIncome - monthExpense

    val accountBalances = remember(allTransactions, consolidateAll, session) {
        viewModel.calculateMoneyAccountBalances(allTransactions)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmAlabasterBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. TOP CONTROL HEADER: Left (< Oct 2026 >) | Center (Trans. | Stats | Accounts) | Right (Sync POS, Bookmarks ⭐≡, Search 🔍, + Input)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ElevatedIvoryCard,
                border = BorderStroke(1.dp, SubtleSandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Month Navigator (< Oct 2026 >) + Role/Consolidation Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftOatSurface,
                                border = BorderStroke(1.dp, SubtleSandBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.navigateMoneyMonth(-1) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("mm_prev_month_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            contentDescription = "Bulan Sebelumnya",
                                            tint = RichEspressoPrimary
                                        )
                                    }
                                    Text(
                                        text = formatMonthKeyLabel(selectedMonthKey),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = RichEspressoPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.navigateMoneyMonth(1) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("mm_next_month_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Bulan Berikutnya",
                                            tint = RichEspressoPrimary
                                        )
                                    }
                                }
                            }

                            if (session.role == StaffRole.DEVELOPER) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (consolidateAll) RichEspressoPrimary else SoftGoldHighlight,
                                    border = BorderStroke(1.dp, RichEspressoPrimary.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { viewModel.toggleMoneyConsolidateAllBranches() }
                                        .testTag("mm_consolidate_toggle")
                                ) {
                                    Text(
                                        text = if (consolidateAll) "🌐 Konsolidasi Semua Cabang" else "🏪 Cabang ${viewModel.getBranchDisplayName(session.branchId)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (consolidateAll) ElevatedIvoryCard else RichEspressoPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            } else if (session.role == StaffRole.SUPERVISOR) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftGoldHighlight
                                ) {
                                    Text(
                                        text = "Mode SPV (Lihat & Input)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RichEspressoPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Center: Main Sub-Tabs (Trans. | Stats | Accounts)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftOatSurface,
                            border = BorderStroke(1.dp, SubtleSandBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                MoneyMainTab.entries.forEach { tab ->
                                    val selected = activeMainTab == tab
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (selected) RichEspressoPrimary else Color.Transparent,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable { activeMainTab = tab }
                                            .testTag("mm_main_tab_${tab.name.lowercase()}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = when (tab) {
                                                    MoneyMainTab.TRANS -> Icons.Default.ReceiptLong
                                                    MoneyMainTab.STATS -> Icons.Default.PieChart
                                                    MoneyMainTab.ACCOUNTS -> Icons.Default.AccountBalanceWallet
                                                },
                                                contentDescription = tab.label,
                                                tint = if (selected) HoneyCaramelAccent else MutedCocoaText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = tab.label,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (selected) ElevatedIvoryCard else RichEspressoPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Right: Bookmarks Icon (⭐≡) + Search Icon (🔍) + (+ Input)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            // Bookmarks Button (⭐≡ Star + 3 lines as in reference screenshot)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftGoldHighlight,
                                border = BorderStroke(1.dp, HoneyCaramelAccent),
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showBookmarksDialog = true }
                                    .testTag("mm_bookmarks_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Bookmarks",
                                        tint = RichEspressoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                        contentDescription = "Daftar Bookmark",
                                        tint = RichEspressoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Search Icon Button (🔍)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (searchVisible || searchQuery.isNotBlank()) RichEspressoPrimary else SoftOatSurface,
                                border = BorderStroke(1.dp, SubtleSandBorder),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        searchVisible = !searchVisible
                                        if (!searchVisible) searchQuery = ""
                                    }
                                    .testTag("mm_search_toggle_btn")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Cari Transaksi",
                                        tint = if (searchVisible || searchQuery.isNotBlank()) ElevatedIvoryCard else RichEspressoPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Primary + Input Button
                            Button(
                                onClick = {
                                    editingTransaction = null
                                    prefillFromBookmark = null
                                    showTransactionModal = true
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MoneyExpenseRed,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("mm_add_tx_top_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Input", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    // Expandable Search Bar & Active Account Filter Chip
                    if (searchVisible || selectedAccountFilter != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (searchVisible) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "Cari Note (mis. Fortune, Telur), Kategori, Sub-kategori, Akun, atau Tag...",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = MutedCocoaText, modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Bersihkan", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SoftOatSurface,
                                        unfocusedContainerColor = SoftOatSurface,
                                        focusedBorderColor = RichEspressoPrimary,
                                        unfocusedBorderColor = SubtleSandBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("mm_search_input")
                                )
                            }
                            selectedAccountFilter?.let { (grp, acc) ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MoneyIncomeSoftBg,
                                    border = BorderStroke(1.dp, MoneyIncomeBlue.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { selectedAccountFilter = null }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Filter Akun: $grp • $acc",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MoneyIncomeBlue
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus Filter Akun",
                                            tint = MoneyIncomeBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. MAIN CONTENT AREA BASED ON TAB
            when (activeMainTab) {
                MoneyMainTab.TRANS -> {
                    MoneyTransactionsTabContent(
                        monthIncome = monthIncome,
                        monthExpense = monthExpense,
                        monthTotalNet = monthTotalNet,
                        activeSubView = activeTransSubView,
                        onSubViewChange = { activeTransSubView = it },
                        monthTransactions = monthTransactions,
                        allTransactions = allTransactions,
                        selectedMonthKey = selectedMonthKey,
                        canEditOrDelete = session.role.canEditOrDeleteMoneyManager,
                        dailyPosItemSummaryMap = dailyPosItemSummaryMap,
                        onSelectMonthFromMonthlyView = { mKey ->
                            viewModel.setMoneySelectedMonthKey(mKey)
                            activeTransSubView = TransSubView.DAILY
                        },
                        onClickTransaction = { tx ->
                            if (session.role.canEditOrDeleteMoneyManager) {
                                editingTransaction = tx
                                prefillFromBookmark = null
                                showTransactionModal = true
                            } else {
                                viewModel.showBanner("Akses Supervisor: Hanya dapat melihat & menambah transaksi baru.")
                            }
                        }
                    )
                }

                MoneyMainTab.STATS -> {
                    MoneyStatsTabContent(
                        monthTransactions = monthTransactions,
                        allTransactions = allTransactions,
                        selectedMonthKey = selectedMonthKey,
                        monthIncome = monthIncome,
                        monthExpense = monthExpense,
                        onClickTransaction = { tx ->
                            if (session.role.canEditOrDeleteMoneyManager) {
                                editingTransaction = tx
                                prefillFromBookmark = null
                                showTransactionModal = true
                            } else {
                                viewModel.showBanner("Akses Supervisor: Hanya dapat melihat & menambah transaksi baru.")
                            }
                        }
                    )
                }

                MoneyMainTab.ACCOUNTS -> {
                    MoneyAccountsTabContent(
                        accountGroups = accountGroups,
                        accountBalances = accountBalances,
                        onSelectAccountToFilter = { grp, acc ->
                            selectedAccountFilter = grp to acc
                            activeMainTab = MoneyMainTab.TRANS
                            activeTransSubView = TransSubView.DAILY
                        }
                    )
                }
            }
        }

        // Floating Action Button (+) on bottom right (matching Money Manager red circular '+' button)
        FloatingActionButton(
            onClick = {
                editingTransaction = null
                prefillFromBookmark = null
                showTransactionModal = true
            },
            shape = CircleShape,
            containerColor = MoneyExpenseRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
                .testTag("mm_fab_add_tx")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Transaksi Money Manager",
                modifier = Modifier.size(28.dp)
            )
        }

        // Bookmarks Modal (⭐≡)
        if (showBookmarksDialog) {
            MoneyBookmarksDialog(
                bookmarks = bookmarks,
                canDelete = session.role.canEditOrDeleteMoneyManager,
                onDismiss = { showBookmarksDialog = false },
                onSelectBookmark = { bm ->
                    showBookmarksDialog = false
                    editingTransaction = null
                    prefillFromBookmark = bm
                    showTransactionModal = true
                },
                onDeleteBookmark = { bmId ->
                    viewModel.deleteMoneyBookmark(bmId)
                }
            )
        }

        // Transaction Input / Edit Modal
        if (showTransactionModal) {
            val defaultFeeChoice by viewModel.transferFeeAccountChoice.collectAsState()
            MoneyTransactionFormDialog(
                existingTransaction = editingTransaction,
                prefillBookmark = prefillFromBookmark,
                categories = categories,
                accountGroups = accountGroups,
                canDelete = session.role.canEditOrDeleteMoneyManager && editingTransaction != null,
                defaultFeeAccountChoice = defaultFeeChoice,
                onFeeAccountChoiceChange = { choice -> viewModel.saveLastTransferFeeAccountChoice(choice) },
                getNoteSuggestions = { prefix -> viewModel.getMoneyNoteSuggestions(prefix) },
                getHistoricalTransferFeeConfig = { fromAcc, toAcc -> viewModel.getHistoricalTransferFeeConfig(fromAcc, toAcc) },
                onDismiss = {
                    showTransactionModal = false
                    editingTransaction = null
                    prefillFromBookmark = null
                },
                onSaveTransaction = { txRecord, keepOpenForNext ->
                    viewModel.saveOrUpdateMoneyTransaction(txRecord)
                    if (!keepOpenForNext) {
                        showTransactionModal = false
                        editingTransaction = null
                        prefillFromBookmark = null
                    } else {
                        editingTransaction = null
                        prefillFromBookmark = null
                    }
                },
                onDeleteTransaction = { txId ->
                    viewModel.deleteMoneyTransaction(txId)
                    showTransactionModal = false
                    editingTransaction = null
                },
                onSaveAsBookmark = { bmRecord ->
                    viewModel.saveMoneyBookmark(bmRecord)
                }
            )
        }
    }
}

// =========================================================================================
// TAB 1: TRANS. (DAILY & MONTHLY LEDGER)
// =========================================================================================

@Composable
private fun MoneyTransactionsTabContent(
    monthIncome: Long,
    monthExpense: Long,
    monthTotalNet: Long,
    activeSubView: TransSubView,
    onSubViewChange: (TransSubView) -> Unit,
    monthTransactions: List<MoneyTransactionRecord>,
    allTransactions: List<MoneyTransactionRecord>,
    selectedMonthKey: String,
    canEditOrDelete: Boolean,
    dailyPosItemSummaryMap: Map<String, String> = emptyMap(),
    onSelectMonthFromMonthlyView: (String) -> Unit,
    onClickTransaction: (MoneyTransactionRecord) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary Strip (Income | Expenses | Total) + Daily/Monthly Switcher
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ElevatedIvoryCard,
            border = BorderStroke(1.dp, SubtleSandBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3 KPI Columns matching Money Manager screenshot 1
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Income", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                        Text(
                            text = WaffleoViewModel.formatIdr(monthIncome),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MoneyIncomeBlue
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(SubtleSandBorder)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Expenses", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                        Text(
                            text = WaffleoViewModel.formatIdr(monthExpense),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MoneyExpenseRed
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(SubtleSandBorder)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                        Text(
                            text = WaffleoViewModel.formatIdr(monthTotalNet),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (monthTotalNet >= 0L) RichEspressoPrimary else MoneyExpenseRed
                        )
                    }
                }

                // Daily / Monthly sub-view pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SoftOatSurface,
                    border = BorderStroke(1.dp, SubtleSandBorder)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        TransSubView.entries.forEach { sub ->
                            val isSel = activeSubView == sub
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSel) RichEspressoPrimary else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onSubViewChange(sub) }
                            ) {
                                Text(
                                    text = sub.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSel) ElevatedIvoryCard else MutedCocoaText,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeSubView == TransSubView.DAILY) {
            val groupedByDate = remember(monthTransactions) {
                monthTransactions.groupBy { it.dateKey }.toSortedMap(compareByDescending { it })
            }

            if (groupedByDate.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ElevatedIvoryCard,
                    border = BorderStroke(1.dp, SubtleSandBorder),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MutedCocoaText.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada catatan transaksi pada bulan ${formatMonthKeyLabel(selectedMonthKey)}.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MutedCocoaText
                            )
                            Text(
                                text = "Klik tombol '+ Input', pilih dari Bookmarks (⭐≡), atau tutup toko untuk auto-sync penjualan POS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    groupedByDate.forEach { (dateKey, dayTxList) ->
                        val dayIncome = dayTxList.filter { it.type == MoneyTransactionType.INCOME }.sumOf { it.amount }
                        val dayExpense = dayTxList.filter { it.type == MoneyTransactionType.EXPENSE }.sumOf { it.amount + it.feeAmount } +
                            dayTxList.filter { it.type == MoneyTransactionType.TRANSFER }.sumOf { it.feeAmount }

                        item(key = "day_group_$dateKey") {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = ElevatedIvoryCard,
                                border = BorderStroke(1.dp, SubtleSandBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Date Header Bar (e.g. "31  Sat  10.2026" | Blue Day Income | Red Day Expense)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SoftOatSurface.copy(alpha = 0.7f))
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val (dayNum, dayOfWeekText, monthYearDot) = parseDateHeaderParts(dateKey)
                                            Text(
                                                text = dayNum,
                                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = RichEspressoPrimary
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (dayOfWeekText) {
                                                    "Sun" -> MoneyExpenseRed
                                                    "Sat" -> MoneyIncomeBlue
                                                    else -> MutedCocoaText.copy(alpha = 0.25f)
                                                }
                                            ) {
                                                Text(
                                                    text = dayOfWeekText,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (dayOfWeekText == "Sun" || dayOfWeekText == "Sat") Color.White else RichEspressoPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = monthYearDot,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedCocoaText
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = WaffleoViewModel.formatIdr(dayIncome),
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MoneyIncomeBlue
                                            )
                                            Text(
                                                text = WaffleoViewModel.formatIdr(dayExpense),
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MoneyExpenseRed
                                            )
                                        }
                                    }

                                    // Daily POS Items & Orders Summary Sub-bar (e.g. "• 8 Pesanan : 24 Waffle, 1 Puding")
                                    val dailyPosSummary = dailyPosItemSummaryMap[dateKey]
                                    if (!dailyPosSummary.isNullOrBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SoftOatSurface.copy(alpha = 0.45f))
                                                .padding(horizontal = 16.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = SoftGoldHighlight,
                                                border = BorderStroke(0.5.dp, HoneyCaramelAccent.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = dailyPosSummary,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    ),
                                                    color = RichEspressoPrimary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = SubtleSandBorder.copy(alpha = 0.6f))

                                    // Individual Transaction Rows inside this date
                                    dayTxList.forEachIndexed { idx, tx ->
                                        MoneyTransactionRowItem(
                                            tx = tx,
                                            canEdit = canEditOrDelete,
                                            onClick = { onClickTransaction(tx) }
                                        )
                                        if (idx < dayTxList.lastIndex) {
                                            HorizontalDivider(
                                                color = SubtleSandBorder.copy(alpha = 0.35f),
                                                modifier = Modifier.padding(horizontal = 16.dp)
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
            // MONTHLY SUB-VIEW
            val yearPrefix = selectedMonthKey.take(4)
            val monthsOfYear = remember(allTransactions, yearPrefix) {
                (12 downTo 1).map { m ->
                    val mKey = String.format(Locale.US, "%s-%02d", yearPrefix, m)
                    val list = allTransactions.filter { it.dateKey.startsWith(mKey) }
                    val inc = list.filter { it.type == MoneyTransactionType.INCOME }.sumOf { it.amount }
                    val exp = list.filter { it.type == MoneyTransactionType.EXPENSE }.sumOf { it.amount + it.feeAmount } +
                        list.filter { it.type == MoneyTransactionType.TRANSFER }.sumOf { it.feeAmount }
                    Triple(mKey, inc, exp)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(monthsOfYear, key = { it.first }) { (mKey, inc, exp) ->
                    val isCurrentMonth = mKey == selectedMonthKey
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCurrentMonth) SoftGoldHighlight.copy(alpha = 0.45f) else ElevatedIvoryCard,
                        border = BorderStroke(1.dp, if (isCurrentMonth) HoneyCaramelAccent else SubtleSandBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectMonthFromMonthlyView(mKey) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatMonthKeyLabel(mKey),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = RichEspressoPrimary,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(
                                text = WaffleoViewModel.formatIdr(inc),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MoneyIncomeBlue
                            )
                            Text(
                                text = WaffleoViewModel.formatIdr(exp),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MoneyExpenseRed
                            )
                            Text(
                                text = WaffleoViewModel.formatIdr(inc - exp),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (inc - exp >= 0L) RichEspressoPrimary else MoneyExpenseRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoneyTransactionRowItem(
    tx: MoneyTransactionRecord,
    canEdit: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Category + Sub-category
        Column(modifier = Modifier.width(185.dp)) {
            Text(
                text = if (tx.type == MoneyTransactionType.TRANSFER) "⇄ Transfer" else tx.categoryName,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = RichEspressoPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (tx.type != MoneyTransactionType.TRANSFER && tx.subCategoryName.isNotBlank()) {
                Text(
                    text = tx.subCategoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedCocoaText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (tx.type == MoneyTransactionType.TRANSFER && tx.feeAmount > 0L) {
                Text(
                    text = "Biaya Admin: ${WaffleoViewModel.formatIdr(tx.feeAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MoneyExpenseRed
                )
            }
        }

        // Center: Bold Note + Description + Account Route + Tag
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = tx.note.ifBlank {
                        if (tx.type == MoneyTransactionType.TRANSFER) "${tx.accountName} → ${tx.toAccountName}"
                        else tx.subCategoryName.ifBlank { tx.categoryName }
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = RichEspressoPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (tx.tag.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftGoldHighlight
                    ) {
                        Text(
                            text = "#${tx.tag}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RichEspressoPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                if (tx.repetition.isNotBlank() && tx.repetition != MoneyRepetitionOption.NONE.label) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftOatSurface
                    ) {
                        Text(
                            text = "↻ ${tx.repetition}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (tx.type == MoneyTransactionType.TRANSFER) {
                        "${tx.accountName} → ${tx.toAccountName}"
                    } else {
                        tx.accountName
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedCocoaText
                )
                if (tx.description.isNotBlank()) {
                    Text(
                        text = "• ${tx.description}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedCocoaText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Right: Nominal Amount (Blue for Income, Red for Expense, Slate for Transfer) + Time
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = WaffleoViewModel.formatIdr(tx.amount),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = when (tx.type) {
                    MoneyTransactionType.INCOME -> MoneyIncomeBlue
                    MoneyTransactionType.EXPENSE -> MoneyExpenseRed
                    MoneyTransactionType.TRANSFER -> MoneyTransferSlate
                }
            )
            Text(
                text = if (canEdit) "${tx.timeText} • Edit" else tx.timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MutedCocoaText
            )
        }
    }
}

// =========================================================================================
// TAB 2: STATS (PIE CHART + CATEGORY DRILL-DOWN + 6-MONTH TREND LINE + SUB-CATEGORY BREAKDOWN)
// =========================================================================================

@Composable
private fun MoneyStatsTabContent(
    monthTransactions: List<MoneyTransactionRecord>,
    allTransactions: List<MoneyTransactionRecord>,
    selectedMonthKey: String,
    monthIncome: Long,
    monthExpense: Long,
    onClickTransaction: (MoneyTransactionRecord) -> Unit
) {
    var selectedStatsType by remember { mutableStateOf(MoneyTransactionType.EXPENSE) }

    val filteredByType = remember(monthTransactions, selectedStatsType) {
        monthTransactions.filter { it.type == selectedStatsType }
    }
    val totalForType = remember(filteredByType) {
        filteredByType.sumOf { it.amount }.coerceAtLeast(1L)
    }

    data class CategoryStatSlice(
        val categoryName: String,
        val totalAmount: Long,
        val percentage: Float,
        val color: Color,
        val transactions: List<MoneyTransactionRecord>
    )

    val categorySlices = remember(filteredByType, totalForType) {
        val grouped = filteredByType.groupBy { it.categoryName }
        grouped.entries
            .sortedByDescending { entry -> entry.value.sumOf { it.amount } }
            .mapIndexed { idx, entry ->
                val sum = entry.value.sumOf { it.amount }
                val pct = (sum.toFloat() / totalForType.toFloat()) * 100f
                CategoryStatSlice(
                    categoryName = entry.key,
                    totalAmount = sum,
                    percentage = pct,
                    color = PieChartPalette[idx % PieChartPalette.size],
                    transactions = entry.value
                )
            }
    }

    var selectedCategoryName by remember(categorySlices, selectedStatsType) {
        mutableStateOf(categorySlices.firstOrNull()?.categoryName.orEmpty())
    }
    LaunchedEffect(categorySlices) {
        if (selectedCategoryName.isBlank() || categorySlices.none { it.categoryName == selectedCategoryName }) {
            selectedCategoryName = categorySlices.firstOrNull()?.categoryName.orEmpty()
        }
    }

    val activeSlice = categorySlices.firstOrNull { it.categoryName == selectedCategoryName } ?: categorySlices.firstOrNull()

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // LEFT PANE (46%): Income/Expense Switcher + Donut Pie Chart + Category Percentage Ranking
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ElevatedIvoryCard,
            border = BorderStroke(1.dp, SubtleSandBorder),
            modifier = Modifier
                .weight(0.46f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income vs Expense Toggle Header (matching Screenshot 8)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isInc = selectedStatsType == MoneyTransactionType.INCOME
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isInc) MoneyIncomeSoftBg else SoftOatSurface,
                        border = BorderStroke(1.5.dp, if (isInc) MoneyIncomeBlue else SubtleSandBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedStatsType = MoneyTransactionType.INCOME }
                            .testTag("mm_stats_income_tab")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Income", style = MaterialTheme.typography.labelMedium, color = MutedCocoaText)
                            Text(
                                text = WaffleoViewModel.formatIdr(monthIncome),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isInc) MoneyIncomeBlue else RichEspressoPrimary
                            )
                        }
                    }

                    val isExp = selectedStatsType == MoneyTransactionType.EXPENSE
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isExp) MoneyExpenseSoftBg else SoftOatSurface,
                        border = BorderStroke(1.5.dp, if (isExp) MoneyExpenseRed else SubtleSandBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedStatsType = MoneyTransactionType.EXPENSE }
                            .testTag("mm_stats_expense_tab")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Expenses", style = MaterialTheme.typography.labelMedium, color = MutedCocoaText)
                            Text(
                                text = WaffleoViewModel.formatIdr(monthExpense),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isExp) MoneyExpenseRed else RichEspressoPrimary
                            )
                        }
                    }
                }

                if (categorySlices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Belum ada data ${selectedStatsType.label} pada bulan ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedCocoaText
                        )
                    }
                } else {
                    // Donut Chart Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(185.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(165.dp)) {
                            var startAngle = -90f
                            val strokeWidth = 36.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

                            categorySlices.forEach { slice ->
                                val sweep = (slice.percentage / 100f) * 360f
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep.coerceAtLeast(1.5f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(diameter, diameter),
                                    style = Stroke(width = strokeWidth)
                                )
                                startAngle += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = selectedStatsType.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCocoaText
                            )
                            Text(
                                text = WaffleoViewModel.formatIdr(filteredByType.sumOf { it.amount }),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = RichEspressoPrimary
                            )
                        }
                    }

                    HorizontalDivider(color = SubtleSandBorder.copy(alpha = 0.6f))

                    // Category Percentage Ranked List (Click to drill down on right pane!)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categorySlices, key = { it.categoryName }) { slice ->
                            val isSelected = activeSlice?.categoryName == slice.categoryName
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SoftGoldHighlight.copy(alpha = 0.55f) else SoftOatSurface.copy(alpha = 0.6f),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) RichEspressoPrimary else Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedCategoryName = slice.categoryName }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
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
                                            color = slice.color
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "%.1f%%", slice.percentage),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                        Text(
                                            text = slice.categoryName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = RichEspressoPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = WaffleoViewModel.formatIdr(slice.totalAmount),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = RichEspressoPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // RIGHT PANE (54%): DRILL-DOWN FOR SELECTED CATEGORY (6-Month Trend Line Chart + Sub-Category Breakdown + Transactions)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ElevatedIvoryCard,
            border = BorderStroke(1.dp, SubtleSandBorder),
            modifier = Modifier
                .weight(0.54f)
                .fillMaxHeight()
        ) {
            if (activeSlice == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Pilih kategori di sebelah kiri untuk melihat rincian sub-kategori.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedCocoaText
                    )
                }
            } else {
                // Compute 6-month trend for activeSlice.categoryName
                val sixMonthsTrend = remember(allTransactions, selectedMonthKey, activeSlice.categoryName, selectedStatsType) {
                    buildSixMonthTrendForCategory(
                        allTransactions = allTransactions,
                        endMonthKey = selectedMonthKey,
                        categoryName = activeSlice.categoryName,
                        type = selectedStatsType
                    )
                }

                var selectedSubCategoryName by remember(activeSlice.categoryName, selectedStatsType, selectedMonthKey) {
                    mutableStateOf<String?>(null)
                }

                // Compute Sub-Category breakdown inside activeSlice
                val subCategoryBreakdown = remember(activeSlice) {
                    val catTotal = activeSlice.totalAmount.coerceAtLeast(1L)
                    activeSlice.transactions
                        .groupBy { it.subCategoryName.ifBlank { activeSlice.categoryName } }
                        .entries
                        .sortedByDescending { e -> e.value.sumOf { it.amount } }
                        .mapIndexed { idx, entry ->
                            val subSum = entry.value.sumOf { it.amount }
                            val subPct = (subSum.toFloat() / catTotal.toFloat()) * 100f
                            Triple(entry.key, subSum, subPct) to PieChartPalette[idx % PieChartPalette.size]
                        }
                }

                val filteredSubCategoryTransactions = remember(activeSlice, selectedSubCategoryName) {
                    val selSub = selectedSubCategoryName
                    if (selSub == null) {
                        emptyList()
                    } else {
                        activeSlice.transactions.filter { tx ->
                            val txSub = tx.subCategoryName.ifBlank { activeSlice.categoryName }
                            txSub.equals(selSub, ignoreCase = true)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    // 1. Category Drill-Down Header (matching Screenshots 12-16)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeSlice.categoryName,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RichEspressoPrimary
                                )
                                Text(
                                    text = "Rincian Sub-Kategori & Tren 6 Bulan (${formatMonthKeyLabel(selectedMonthKey)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedCocoaText
                                )
                            }
                            Text(
                                text = WaffleoViewModel.formatIdr(activeSlice.totalAmount),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (selectedStatsType == MoneyTransactionType.INCOME) MoneyIncomeBlue else MoneyExpenseRed
                            )
                        }
                    }

                    // 2. 6-Month Trend Line Chart Card (May .. Oct as in Screenshots 12-16)
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SoftOatSurface,
                            border = BorderStroke(1.dp, SubtleSandBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                val maxVal = (sixMonthsTrend.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(1L)
                                val lineColor = if (selectedStatsType == MoneyTransactionType.INCOME) MoneyIncomeBlue else MoneyExpenseRed

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .padding(top = 12.dp, bottom = 4.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val count = sixMonthsTrend.size
                                        if (count > 1) {
                                            val stepX = size.width / (count - 1).toFloat()
                                            val usableH = size.height - 24.dp.toPx()
                                            val points = sixMonthsTrend.mapIndexed { i, pair ->
                                                val ratio = pair.second.toFloat() / maxVal.toFloat()
                                                Offset(
                                                    x = i * stepX,
                                                    y = usableH - (ratio * (usableH - 12.dp.toPx()))
                                                )
                                            }
                                            val path = Path().apply {
                                                points.forEachIndexed { idx, pt ->
                                                    if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                                                }
                                            }
                                            drawPath(
                                                path = path,
                                                color = lineColor,
                                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                            points.forEachIndexed { idx, pt ->
                                                drawCircle(
                                                    color = if (idx == points.lastIndex) lineColor else ElevatedIvoryCard,
                                                    radius = 5.dp.toPx(),
                                                    center = pt
                                                )
                                                drawCircle(
                                                    color = lineColor,
                                                    radius = 5.dp.toPx(),
                                                    center = pt,
                                                    style = Stroke(width = 2.dp.toPx())
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    sixMonthsTrend.forEachIndexed { idx, (mLabel, amt) ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = formatShortIdr(amt),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (idx == sixMonthsTrend.lastIndex) lineColor else MutedCocoaText
                                            )
                                            Text(
                                                text = mLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (idx == sixMonthsTrend.lastIndex) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (idx == sixMonthsTrend.lastIndex) RichEspressoPrimary else MutedCocoaText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Sub-Category Percentage Breakdown List (Clickable to filter & display Riwayat Transaksi Kategori Ini!)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Komposisi Sub-Kategori (${subCategoryBreakdown.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RichEspressoPrimary
                                )
                                Text(
                                    text = "Sentuh salah satu sub-kategori untuk menampilkan riwayat transaksinya",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedCocoaText
                                )
                            }
                            if (selectedSubCategoryName != null) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftGoldHighlight,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { selectedSubCategoryName = null }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Tutup Riwayat",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = RichEspressoPrimary
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Tutup Riwayat",
                                            tint = RichEspressoPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(subCategoryBreakdown, key = { it.first.first }) { (triple, badgeColor) ->
                        val (subName, subAmount, subPct) = triple
                        val isSubSelected = selectedSubCategoryName.equals(subName, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSubSelected) SoftGoldHighlight.copy(alpha = 0.55f) else SoftOatSurface.copy(alpha = 0.65f),
                            border = BorderStroke(
                                width = if (isSubSelected) 1.5.dp else 1.dp,
                                color = if (isSubSelected) RichEspressoPrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedSubCategoryName = if (isSubSelected) null else subName
                                }
                                .testTag("mm_subcat_${subName.lowercase().replace(" ", "_")}")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
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
                                            color = badgeColor
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "%.1f%%", subPct),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = subName,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.SemiBold
                                            ),
                                            color = RichEspressoPrimary
                                        )
                                    }
                                    Text(
                                        text = WaffleoViewModel.formatIdr(subAmount),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = RichEspressoPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(SubtleSandBorder.copy(alpha = 0.5f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((subPct / 100f).coerceIn(0.02f, 1f))
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(50))
                                            .background(badgeColor)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Individual Transactions in this Category — ONLY displayed when a Sub-Category is selected!
                    if (selectedSubCategoryName != null) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Riwayat Transaksi Kategori Ini — $selectedSubCategoryName (${filteredSubCategoryTransactions.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = RichEspressoPrimary
                            )
                        }

                        items(filteredSubCategoryTransactions, key = { it.id }) { tx ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftOatSurface.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onClickTransaction(tx) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${tx.note.ifBlank { tx.subCategoryName }} (${tx.subCategoryName.ifBlank { tx.categoryName }})",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = RichEspressoPrimary
                                        )
                                        Text(
                                            text = "${tx.dateKey} • ${tx.accountName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedCocoaText
                                        )
                                    }
                                    Text(
                                        text = WaffleoViewModel.formatIdr(tx.amount),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (tx.type == MoneyTransactionType.INCOME) MoneyIncomeBlue else MoneyExpenseRed
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

// =========================================================================================
// TAB 3: ACCOUNTS (4 ACCOUNT GROUPS & REAL-TIME BALANCES)
// =========================================================================================

@Composable
private fun MoneyAccountsTabContent(
    accountGroups: List<MoneyAccountGroupDef>,
    accountBalances: Map<Pair<String, String>, Long>,
    onSelectAccountToFilter: (String, String) -> Unit
) {
    val totalAssets = remember(accountBalances) {
        accountBalances.values.filter { it > 0L }.sum()
    }
    val totalLiabilities = remember(accountBalances) {
        accountBalances.values.filter { it < 0L }.sum()
    }
    val netWorth = totalAssets + totalLiabilities

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Summary Strip: Assets | Liabilities | Total (matching Screenshot 17)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ElevatedIvoryCard,
            border = BorderStroke(1.dp, SubtleSandBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Assets", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                    Text(
                        text = WaffleoViewModel.formatIdr(totalAssets),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MoneyIncomeBlue
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(SubtleSandBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Liabilities", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                    Text(
                        text = WaffleoViewModel.formatIdr(totalLiabilities),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MoneyExpenseRed
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(SubtleSandBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.bodySmall, color = MutedCocoaText)
                    Text(
                        text = WaffleoViewModel.formatIdr(netWorth),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RichEspressoPrimary
                    )
                }
            }
        }

        // 4 Account Group Cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(accountGroups, key = { it.groupName }) { group ->
                val groupTotal = group.accounts.sumOf { acc -> accountBalances[group.groupName to acc] ?: 0L }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ElevatedIvoryCard,
                    border = BorderStroke(1.dp, SubtleSandBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Group Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftOatSurface.copy(alpha = 0.7f))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MutedCocoaText
                            )
                            Text(
                                text = WaffleoViewModel.formatIdr(groupTotal),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (groupTotal >= 0L) RichEspressoPrimary else MoneyExpenseRed
                            )
                        }
                        HorizontalDivider(color = SubtleSandBorder.copy(alpha = 0.6f))
                        group.accounts.forEachIndexed { idx, accName ->
                            val bal = accountBalances[group.groupName to accName] ?: 0L
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectAccountToFilter(group.groupName, accName) }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SoftGoldHighlight,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalance,
                                                contentDescription = null,
                                                tint = RichEspressoPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = accName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = RichEspressoPrimary
                                        )
                                        Text(
                                            text = "Klik untuk lihat riwayat mutasi",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedCocoaText
                                        )
                                    }
                                }
                                Text(
                                    text = WaffleoViewModel.formatIdr(bal),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (bal >= 0L) MoneyIncomeBlue else MoneyExpenseRed
                                )
                            }
                            if (idx < group.accounts.lastIndex) {
                                HorizontalDivider(
                                    color = SubtleSandBorder.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================================
// BOOKMARKS MODAL (⭐≡ Star + 3 lines at top right)
// =========================================================================================

@Composable
private fun MoneyBookmarksDialog(
    bookmarks: List<MoneyBookmarkRecord>,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onSelectBookmark: (MoneyBookmarkRecord) -> Unit,
    onDeleteBookmark: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .widthIn(min = 460.dp, max = 540.dp)
                .fillMaxWidth(0.60f)
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = CaramelGold)
                        Column {
                            Text(
                                text = "Bookmarks Transaksi Cepat",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = RichEspressoPrimary
                            )
                            Text(
                                text = "Klik item bookmark untuk langsung mengisi form dengan waktu saat ini",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SubtleSandBorder)
                Spacer(modifier = Modifier.height(10.dp))

                if (bookmarks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada Bookmark tersimpan. Anda dapat menyimpan Bookmark dari form Input Transaksi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedCocoaText,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(bookmarks, key = { it.id }) { bm ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftOatSurface,
                                border = BorderStroke(1.dp, SubtleSandBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectBookmark(bm) }
                                    .testTag("mm_bookmark_item_${bm.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (bm.type) {
                                                    MoneyTransactionType.INCOME -> MoneyIncomeSoftBg
                                                    MoneyTransactionType.EXPENSE -> MoneyExpenseSoftBg
                                                    MoneyTransactionType.TRANSFER -> SoftGoldHighlight
                                                }
                                            ) {
                                                Text(
                                                    text = bm.type.label,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = when (bm.type) {
                                                        MoneyTransactionType.INCOME -> MoneyIncomeBlue
                                                        MoneyTransactionType.EXPENSE -> MoneyExpenseRed
                                                        MoneyTransactionType.TRANSFER -> RichEspressoPrimary
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = bm.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = RichEspressoPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${bm.categoryName}${if (bm.subCategoryName.isNotBlank()) " / ${bm.subCategoryName}" else ""} • ${bm.accountGroup} (${bm.accountName})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedCocoaText
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = WaffleoViewModel.formatIdr(bm.amount),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = when (bm.type) {
                                                MoneyTransactionType.INCOME -> MoneyIncomeBlue
                                                MoneyTransactionType.EXPENSE -> MoneyExpenseRed
                                                MoneyTransactionType.TRANSFER -> RichEspressoPrimary
                                            }
                                        )
                                        if (canDelete) {
                                            IconButton(
                                                onClick = { onDeleteBookmark(bm.id) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Hapus Bookmark",
                                                    tint = TerracottaDanger,
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
        }
    }
}

// =========================================================================================
// TRANSACTION INPUT & EDIT MODAL (Income | Expense | Transfer + 2-Column Pickers + Note Auto-Complete + Rep/Inst. + Calculator)
// =========================================================================================

private enum class ActiveOverlayPicker {
    NONE,
    CATEGORY,
    ACCOUNT_PRIMARY,
    ACCOUNT_TRANSFER_TO,
    REPETITION,
    AMOUNT_CALCULATOR,
    FEE_CALCULATOR
}

@Composable
private fun MoneyTransactionFormDialog(
    existingTransaction: MoneyTransactionRecord?,
    prefillBookmark: MoneyBookmarkRecord?,
    categories: List<MoneyCategoryDef>,
    accountGroups: List<MoneyAccountGroupDef>,
    canDelete: Boolean,
    defaultFeeAccountChoice: String = "FROM",
    onFeeAccountChoiceChange: (String) -> Unit = {},
    getNoteSuggestions: (String) -> List<String>,
    getHistoricalTransferFeeConfig: (String, String) -> WaffleoViewModel.TransferFeeHistoryConfig? = { _, _ -> null },
    onDismiss: () -> Unit,
    onSaveTransaction: (MoneyTransactionRecord, Boolean) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onSaveAsBookmark: (MoneyBookmarkRecord) -> Unit
) {
    val nowDate = remember { Date() }
    val defaultDateKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(nowDate) }
    val defaultTimeText = remember { SimpleDateFormat("HH:mm", Locale.US).format(nowDate) }

    var txType by remember {
        mutableStateOf(
            existingTransaction?.type
                ?: prefillBookmark?.type
                ?: MoneyTransactionType.EXPENSE
        )
    }

    val context = LocalContext.current

    // Notice: When opened from a Bookmark, Date & Time are set to the current moment when clicked!
    var dateKey by remember {
        mutableStateOf(existingTransaction?.dateKey ?: defaultDateKey)
    }
    var timeText by remember {
        mutableStateOf(existingTransaction?.timeText ?: defaultTimeText)
    }

    val formattedDateDisplay = remember(dateKey) {
        try {
            val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey)
            if (d != null) SimpleDateFormat("dd/MM/yyyy (E)", Locale.US).format(d) else dateKey
        } catch (_: Exception) {
            dateKey
        }
    }

    val showNativeDatePicker = {
        val cal = Calendar.getInstance()
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}

        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val showNativeTimePicker = {
        val parts = timeText.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        android.app.TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                timeText = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)
            },
            hour,
            minute,
            true
        ).show()
    }

    var repetition by remember {
        mutableStateOf(
            MoneyRepetitionOption.entries.firstOrNull {
                it.label.equals(existingTransaction?.repetition, ignoreCase = true)
            } ?: MoneyRepetitionOption.NONE
        )
    }

    var amountExpression by remember {
        mutableStateOf(
            when {
                existingTransaction != null -> existingTransaction.amount.toString()
                prefillBookmark != null && prefillBookmark.amount > 0L -> prefillBookmark.amount.toString()
                else -> ""
            }
        )
    }
    var showFeeField by remember {
        mutableStateOf((existingTransaction?.feeAmount ?: 0L) > 0L)
    }
    var feeExpression by remember {
        mutableStateOf(
            if ((existingTransaction?.feeAmount ?: 0L) > 0L) existingTransaction?.feeAmount.toString() else ""
        )
    }
    var feeAccountChoice by remember {
        mutableStateOf(defaultFeeAccountChoice)
    }

    val evaluatedFeeAmount = remember(feeExpression) {
        evaluateSimpleMathExpression(feeExpression)
    }

    val categoriesForType = remember(categories, txType) {
        val targetType = if (txType == MoneyTransactionType.INCOME) MoneyTransactionType.INCOME else MoneyTransactionType.EXPENSE
        categories.filter { it.type == targetType }
    }

    var selectedCategoryName by remember {
        mutableStateOf(
            existingTransaction?.categoryName
                ?: prefillBookmark?.categoryName
                ?: ""
        )
    }
    var selectedSubCategoryName by remember {
        mutableStateOf(
            existingTransaction?.subCategoryName
                ?: prefillBookmark?.subCategoryName
                ?: ""
        )
    }

    // Keep category in sync when switching between Income and Expense
    LaunchedEffect(txType) {
        if (txType != MoneyTransactionType.TRANSFER && selectedCategoryName.isNotBlank()) {
            val validCats = categories.filter { it.type == txType }
            if (validCats.none { it.categoryName == selectedCategoryName }) {
                val firstCat = validCats.firstOrNull()
                selectedCategoryName = firstCat?.categoryName.orEmpty()
                selectedSubCategoryName = firstCat?.subCategories?.firstOrNull().orEmpty()
            }
        }
    }

    var selectedAccountGroup by remember {
        mutableStateOf(
            existingTransaction?.accountGroup
                ?: prefillBookmark?.accountGroup
                ?: ""
        )
    }
    var selectedAccountName by remember {
        mutableStateOf(
            existingTransaction?.accountName
                ?: prefillBookmark?.accountName
                ?: ""
        )
    }

    var toAccountGroup by remember {
        mutableStateOf(
            existingTransaction?.toAccountGroup?.ifBlank { "" }
                ?: prefillBookmark?.toAccountGroup?.ifBlank { "" }
                ?: ""
        )
    }
    var toAccountName by remember {
        mutableStateOf(
            existingTransaction?.toAccountName?.ifBlank { "" }
                ?: prefillBookmark?.toAccountName?.ifBlank { "" }
                ?: ""
        )
    }

    // Transfer Pair Fee Auto-Fill when user selects From & To accounts
    LaunchedEffect(selectedAccountName, toAccountName, txType) {
        if (txType == MoneyTransactionType.TRANSFER &&
            selectedAccountName.isNotBlank() &&
            toAccountName.isNotBlank() &&
            existingTransaction == null &&
            prefillBookmark == null
        ) {
            val histConfig = getHistoricalTransferFeeConfig(selectedAccountName, toAccountName)
            if (histConfig != null && histConfig.feeAmount > 0L) {
                showFeeField = true
                feeExpression = histConfig.feeAmount.toString()
                feeAccountChoice = histConfig.feeAccountChoice
            }
        }
    }

    var noteText by remember {
        mutableStateOf(
            existingTransaction?.note
                ?: prefillBookmark?.note
                ?: ""
        )
    }
    var noteSuggestionsDismissedForText by remember { mutableStateOf("") }

    var tagText by remember {
        mutableStateOf(
            existingTransaction?.tag
                ?: prefillBookmark?.tag
                ?: ""
        )
    }
    var showTagInput by remember {
        mutableStateOf((existingTransaction?.tag ?: prefillBookmark?.tag).orEmpty().isNotBlank())
    }

    var descriptionText by remember {
        mutableStateOf(
            existingTransaction?.description
                ?: prefillBookmark?.description
                ?: ""
        )
    }

    var activePicker by remember { mutableStateOf(ActiveOverlayPicker.NONE) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val noteSuggestions = remember(noteText, noteSuggestionsDismissedForText) {
        if (noteText.isBlank() || noteText == noteSuggestionsDismissedForText) {
            emptyList()
        } else {
            getNoteSuggestions(noteText)
        }
    }

    val evaluatedAmount = remember(amountExpression) {
        evaluateSimpleMathExpression(amountExpression)
    }

    val activeAccentColor = when (txType) {
        MoneyTransactionType.INCOME -> MoneyIncomeBlue
        MoneyTransactionType.EXPENSE -> MoneyExpenseRed
        MoneyTransactionType.TRANSFER -> RichEspressoPrimary
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 460.dp, max = 540.dp)
                .fillMaxWidth(0.60f)
                .heightIn(max = 660.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // 1. TOP BAR: Title + Income | Expense | Transfer Tabs + Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            MoneyTransactionType.entries.forEach { tabType ->
                                val isSel = txType == tabType
                                val tabColor = when (tabType) {
                                    MoneyTransactionType.INCOME -> MoneyIncomeBlue
                                    MoneyTransactionType.EXPENSE -> MoneyExpenseRed
                                    MoneyTransactionType.TRANSFER -> RichEspressoPrimary
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) tabColor.copy(alpha = 0.12f) else SoftOatSurface,
                                    border = BorderStroke(
                                        width = if (isSel) 1.5.dp else 1.dp,
                                        color = if (isSel) tabColor else SubtleSandBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            txType = tabType
                                            activePicker = ActiveOverlayPicker.NONE
                                        }
                                        .testTag("mm_form_tab_${tabType.name.lowercase()}")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tabType.label,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSel) tabColor else MutedCocoaText
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. MAIN FORM SCROLL AREA
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ROW A: DATE & TIME + REP/INST. BUTTON AT FAR RIGHT (matching WhatsApp Image 2026-11-02 at 00.21.31.jpeg)
                        FormRowContainer(label = "Date") {
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
                                    // 1. DATE SELECTOR (Click opens Calendar DatePicker matching screenshot)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SoftOatSurface,
                                        border = BorderStroke(1.dp, SubtleSandBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { showNativeDatePicker() }
                                            .testTag("mm_date_picker_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Pilih Tanggal",
                                                tint = activeAccentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = formattedDateDisplay,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = RichEspressoPrimary
                                            )
                                        }
                                    }

                                    // 2. TIME SELECTOR (Click opens Clock TimePicker matching screenshot)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SoftOatSurface,
                                        border = BorderStroke(1.dp, SubtleSandBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { showNativeTimePicker() }
                                            .testTag("mm_time_picker_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = "Pilih Jam",
                                                tint = activeAccentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = timeText,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = RichEspressoPrimary
                                            )
                                        }
                                    }
                                }

                                // Far-right Rep/Inst. Button (matching Screenshot 5 & 6)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (repetition != MoneyRepetitionOption.NONE) activeAccentColor else SoftOatSurface,
                                    border = BorderStroke(1.dp, if (repetition != MoneyRepetitionOption.NONE) activeAccentColor else SubtleSandBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            activePicker = if (activePicker == ActiveOverlayPicker.REPETITION) {
                                                ActiveOverlayPicker.NONE
                                            } else {
                                                ActiveOverlayPicker.REPETITION
                                            }
                                        }
                                        .testTag("mm_rep_inst_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Repeat,
                                            contentDescription = "Rep/Inst.",
                                            tint = if (repetition != MoneyRepetitionOption.NONE) Color.White else RichEspressoPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (repetition == MoneyRepetitionOption.NONE) "Rep/Inst." else repetition.label,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (repetition != MoneyRepetitionOption.NONE) Color.White else RichEspressoPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // ROW B: AMOUNT (Clicking opens Calculator overlay like Category and Account)
                        FormRowContainer(label = "Amount") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SoftOatSurface,
                                    border = BorderStroke(
                                        width = if (activePicker == ActiveOverlayPicker.AMOUNT_CALCULATOR) 1.5.dp else 1.dp,
                                        color = if (activePicker == ActiveOverlayPicker.AMOUNT_CALCULATOR) activeAccentColor else SubtleSandBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            activePicker = if (activePicker == ActiveOverlayPicker.AMOUNT_CALCULATOR) {
                                                ActiveOverlayPicker.NONE
                                            } else {
                                                ActiveOverlayPicker.AMOUNT_CALCULATOR
                                            }
                                        }
                                        .testTag("mm_amount_selector_btn")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (evaluatedAmount > 0L) {
                                                WaffleoViewModel.formatIdr(evaluatedAmount)
                                            } else if (amountExpression.isNotBlank()) {
                                                amountExpression
                                            } else {
                                                "0"
                                            },
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = activeAccentColor
                                            )
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Calculate,
                                                contentDescription = "Kalkulator",
                                                tint = activeAccentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Hitung ▾",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = activeAccentColor
                                            )
                                        }
                                    }
                                }

                                if (txType == MoneyTransactionType.TRANSFER) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (showFeeField) MoneyExpenseSoftBg else SoftOatSurface,
                                        border = BorderStroke(1.dp, if (showFeeField) MoneyExpenseRed else SubtleSandBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable {
                                                showFeeField = !showFeeField
                                                if (!showFeeField) {
                                                    feeExpression = ""
                                                    if (activePicker == ActiveOverlayPicker.FEE_CALCULATOR) {
                                                        activePicker = ActiveOverlayPicker.NONE
                                                    }
                                                }
                                            }
                                    ) {
                                        Text(
                                            text = "Fee",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (showFeeField) MoneyExpenseRed else RichEspressoPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // ROW B2: FEE (Biaya Admin Transfer - Dedicated row with Nominal Calculator)
                        if (txType == MoneyTransactionType.TRANSFER && showFeeField) {
                            FormRowContainer(label = "Fee (Admin)") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MoneyExpenseSoftBg,
                                        border = BorderStroke(
                                            width = if (activePicker == ActiveOverlayPicker.FEE_CALCULATOR) 1.5.dp else 1.dp,
                                            color = if (activePicker == ActiveOverlayPicker.FEE_CALCULATOR) MoneyExpenseRed else SubtleSandBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activePicker = if (activePicker == ActiveOverlayPicker.FEE_CALCULATOR) {
                                                    ActiveOverlayPicker.NONE
                                                } else {
                                                    ActiveOverlayPicker.FEE_CALCULATOR
                                                }
                                            }
                                            .testTag("mm_fee_selector_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (evaluatedFeeAmount > 0L) {
                                                    WaffleoViewModel.formatIdr(evaluatedFeeAmount)
                                                } else if (feeExpression.isNotBlank()) {
                                                    feeExpression
                                                } else {
                                                    "0"
                                                },
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MoneyExpenseRed
                                                )
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Calculate,
                                                    contentDescription = "Kalkulator Fee",
                                                    tint = MoneyExpenseRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Hitung ▾",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MoneyExpenseRed
                                                )
                                            }
                                        }
                                    }

                                    // Fee Account Choice (Default FROM vs TO)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Account Fee:",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MutedCocoaText
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (feeAccountChoice == "FROM") RichEspressoPrimary else SoftOatSurface,
                                                border = BorderStroke(1.dp, if (feeAccountChoice == "FROM") RichEspressoPrimary else SubtleSandBorder),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable {
                                                        feeAccountChoice = "FROM"
                                                        onFeeAccountChoiceChange("FROM")
                                                    }
                                            ) {
                                                Text(
                                                    text = "From ($selectedAccountName)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (feeAccountChoice == "FROM") SoftGoldHighlight else RichEspressoPrimary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (feeAccountChoice == "TO") RichEspressoPrimary else SoftOatSurface,
                                                border = BorderStroke(1.dp, if (feeAccountChoice == "TO") RichEspressoPrimary else SubtleSandBorder),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable {
                                                        feeAccountChoice = "TO"
                                                        onFeeAccountChoiceChange("TO")
                                                    }
                                            ) {
                                                Text(
                                                    text = "To ($toAccountName)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (feeAccountChoice == "TO") SoftGoldHighlight else RichEspressoPrimary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ROW C & D: CATEGORY & ACCOUNT (or FROM & TO for Transfer)
                        if (txType != MoneyTransactionType.TRANSFER) {
                            FormRowContainer(label = "Category") {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SoftOatSurface,
                                    border = BorderStroke(
                                        width = if (activePicker == ActiveOverlayPicker.CATEGORY) 1.5.dp else 1.dp,
                                        color = if (activePicker == ActiveOverlayPicker.CATEGORY) activeAccentColor else SubtleSandBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            activePicker = if (activePicker == ActiveOverlayPicker.CATEGORY) {
                                                ActiveOverlayPicker.NONE
                                            } else {
                                                ActiveOverlayPicker.CATEGORY
                                            }
                                        }
                                        .testTag("mm_category_selector_btn")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (selectedSubCategoryName.isNotBlank()) {
                                                "$selectedCategoryName  /  $selectedSubCategoryName"
                                            } else {
                                                selectedCategoryName.ifBlank { "Pilih Kategori..." }
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = RichEspressoPrimary
                                        )
                                        Text(
                                            text = "Pilih Kategori ▾",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = activeAccentColor
                                        )
                                    }
                                }
                            }

                            FormRowContainer(label = "Account") {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SoftOatSurface,
                                    border = BorderStroke(
                                        width = if (activePicker == ActiveOverlayPicker.ACCOUNT_PRIMARY) 1.5.dp else 1.dp,
                                        color = if (activePicker == ActiveOverlayPicker.ACCOUNT_PRIMARY) activeAccentColor else SubtleSandBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            activePicker = if (activePicker == ActiveOverlayPicker.ACCOUNT_PRIMARY) {
                                                ActiveOverlayPicker.NONE
                                            } else {
                                                ActiveOverlayPicker.ACCOUNT_PRIMARY
                                            }
                                        }
                                        .testTag("mm_account_selector_btn")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (selectedAccountName.isNotBlank()) {
                                                if (selectedAccountGroup.isNotBlank()) "$selectedAccountGroup  •  $selectedAccountName" else selectedAccountName
                                            } else {
                                                "Pilih Akun..."
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (selectedAccountName.isNotBlank()) RichEspressoPrimary else MutedCocoaText
                                        )
                                        Text(
                                            text = "Pilih Akun ▾",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = activeAccentColor
                                        )
                                    }
                                }
                            }
                        } else {
                            // TRANSFER MODE: From & To
                            FormRowContainer(label = "From") {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SoftOatSurface,
                                    border = BorderStroke(1.dp, SubtleSandBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            activePicker = if (activePicker == ActiveOverlayPicker.ACCOUNT_PRIMARY) {
                                                ActiveOverlayPicker.NONE
                                            } else {
                                                ActiveOverlayPicker.ACCOUNT_PRIMARY
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (selectedAccountName.isNotBlank()) {
                                                if (selectedAccountGroup.isNotBlank()) "$selectedAccountGroup  •  $selectedAccountName" else selectedAccountName
                                            } else {
                                                "Pilih Akun Asal..."
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (selectedAccountName.isNotBlank()) RichEspressoPrimary else MutedCocoaText
                                        )
                                        Text("Pilih Akun Asal ▾", style = MaterialTheme.typography.labelMedium, color = activeAccentColor)
                                    }
                                }
                            }

                            FormRowContainer(label = "To") {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SoftOatSurface,
                                    border = BorderStroke(1.dp, SubtleSandBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            activePicker = if (activePicker == ActiveOverlayPicker.ACCOUNT_TRANSFER_TO) {
                                                ActiveOverlayPicker.NONE
                                            } else {
                                                ActiveOverlayPicker.ACCOUNT_TRANSFER_TO
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (toAccountName.isNotBlank()) {
                                                if (toAccountGroup.isNotBlank()) "$toAccountGroup  •  $toAccountName" else toAccountName
                                            } else {
                                                "Pilih Akun Tujuan..."
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (toAccountName.isNotBlank()) RichEspressoPrimary else MutedCocoaText
                                        )
                                        Text("Pilih Akun Tujuan ▾", style = MaterialTheme.typography.labelMedium, color = activeAccentColor)
                                    }
                                }
                            }
                        }

                        // ROW E: NOTE WITH PREFIX AUTO-COMPLETE OVERLAY (Typing "F" or "Fort" suggests "Fortune", etc.)
                        Box(modifier = Modifier.fillMaxWidth().zIndex(10f)) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                FormRowContainer(label = "Note") {
                                    OutlinedTextField(
                                        value = noteText,
                                        onValueChange = {
                                            noteText = it
                                            noteSuggestionsDismissedForText = ""
                                        },
                                        placeholder = { Text("Ketik nama item/catatan (mis. Fortune, Sunco, 2 Kg)...") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = SoftOatSurface,
                                            unfocusedContainerColor = SoftOatSurface,
                                            focusedBorderColor = activeAccentColor,
                                            unfocusedBorderColor = SubtleSandBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("mm_note_input")
                                    )
                                }

                                // Floating Auto-Complete Suggestion List when typing prefix in Note
                                if (noteSuggestions.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = RichEspressoPrimary,
                                        border = BorderStroke(1.dp, HoneyCaramelAccent),
                                        shadowElevation = 10.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 95.dp, top = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Riwayat Note berawalan \"${noteText.trim()}\" (Klik untuk pilih cepat):",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = HoneyCaramelAccent
                                                )
                                                Text(
                                                    text = "Tutup ✕",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ElevatedIvoryCard,
                                                    modifier = Modifier.clickable {
                                                        noteSuggestionsDismissedForText = noteText
                                                    }
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                noteSuggestions.forEach { suggestion ->
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = ElevatedIvoryCard,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(50))
                                                            .clickable {
                                                                noteText = suggestion
                                                                noteSuggestionsDismissedForText = suggestion
                                                            }
                                                            .testTag("mm_note_suggestion_$suggestion")
                                                    ) {
                                                        Text(
                                                            text = suggestion,
                                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                            color = RichEspressoPrimary,
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ROW F: SEPARATE "+ Tag" PILL ROW (Kept distinct above Description per User Comment)
                        FormRowContainer(label = "") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (showTagInput || tagText.isNotBlank()) SoftGoldHighlight else SoftOatSurface,
                                    border = BorderStroke(1.dp, SubtleSandBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { showTagInput = !showTagInput }
                                        .testTag("mm_tag_pill_btn")
                                ) {
                                    Text(
                                        text = if (tagText.isNotBlank()) "# $tagText" else "+ Tag",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = RichEspressoPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }

                                if (showTagInput) {
                                    OutlinedTextField(
                                        value = tagText,
                                        onValueChange = { tagText = it },
                                        placeholder = { Text("Nama Tag...") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                    )
                                }
                            }
                        }

                        // ROW G: DESCRIPTION ROW (Distinct from Tag)
                        FormRowContainer(label = "Description") {
                            OutlinedTextField(
                                value = descriptionText,
                                onValueChange = { descriptionText = it },
                                placeholder = { Text("Keterangan tambahan...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SoftOatSurface,
                                    unfocusedContainerColor = SoftOatSurface,
                                    focusedBorderColor = activeAccentColor,
                                    unfocusedBorderColor = SubtleSandBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mm_description_input")
                            )
                        }


                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = SubtleSandBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. BOTTOM ACTION ROW: Left (Hapus + Simpan Bookmark) | Right (Batal, Continue, Save)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (canDelete && existingTransaction != null) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftTerracottaBg,
                                    border = BorderStroke(1.dp, TerracottaDanger.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable { showDeleteConfirm = true }
                                        .testTag("mm_delete_tx_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                                            color = TerracottaDanger
                                        )
                                    }
                                }
                            }

                            // Save as Bookmark button (⭐)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftGoldHighlight,
                                border = BorderStroke(1.dp, HoneyCaramelAccent),
                                modifier = Modifier
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        val finalAmt = evaluateSimpleMathExpression(amountExpression)
                                        val title = noteText.trim().ifEmpty {
                                            selectedSubCategoryName.ifEmpty { selectedCategoryName.ifEmpty { "Bookmark Transaksi" } }
                                        }
                                        onSaveAsBookmark(
                                            MoneyBookmarkRecord(
                                                title = title,
                                                type = txType,
                                                amount = finalAmt,
                                                category = selectedCategoryName,
                                                subCategory = selectedSubCategoryName,
                                                accountGroup = selectedAccountGroup,
                                                accountName = selectedAccountName,
                                                toAccountGroup = if (txType == MoneyTransactionType.TRANSFER) toAccountGroup else "",
                                                toAccountName = if (txType == MoneyTransactionType.TRANSFER) toAccountName else "",
                                                note = noteText.trim(),
                                                description = descriptionText.trim(),
                                                tag = tagText.trim()
                                            )
                                        )
                                    }
                                    .testTag("mm_save_bookmark_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = "Simpan ke Bookmarks",
                                        tint = RichEspressoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "+ Bookmark",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = RichEspressoPrimary
                                    )
                                }
                            }
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
                                Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                                    Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                                }
                            }

                            if (existingTransaction == null) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftOatSurface,
                                    border = BorderStroke(1.dp, activeAccentColor),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            val finalAmt = evaluateSimpleMathExpression(amountExpression)
                                            val finalFeeAmt = evaluateSimpleMathExpression(feeExpression)
                                            if (finalAmt > 0L) {
                                                val rec = buildTransactionRecord(
                                                    existing = null,
                                                    txType = txType,
                                                    dateKey = dateKey,
                                                    timeText = timeText,
                                                    amount = finalAmt,
                                                    feeAmount = if (txType == MoneyTransactionType.TRANSFER && showFeeField) finalFeeAmt else 0L,
                                                    categoryName = selectedCategoryName,
                                                    subCategoryName = selectedSubCategoryName,
                                                    accountGroup = selectedAccountGroup,
                                                    accountName = selectedAccountName,
                                                    toAccountGroup = toAccountGroup,
                                                    toAccountName = toAccountName,
                                                    note = noteText,
                                                    description = descriptionText,
                                                    tag = tagText,
                                                    repetition = repetition
                                                )
                                                onSaveTransaction(rec, true)
                                                amountExpression = ""
                                                feeExpression = ""
                                                noteText = ""
                                                descriptionText = ""
                                            }
                                        }
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                        Text("Continue", style = MaterialTheme.typography.labelLarge, color = activeAccentColor)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val finalAmt = evaluateSimpleMathExpression(amountExpression)
                                    val finalFeeAmt = evaluateSimpleMathExpression(feeExpression)
                                    if (finalAmt > 0L) {
                                        val rec = buildTransactionRecord(
                                            existing = existingTransaction,
                                            txType = txType,
                                            dateKey = dateKey,
                                            timeText = timeText,
                                            amount = finalAmt,
                                            feeAmount = if (txType == MoneyTransactionType.TRANSFER && showFeeField) finalFeeAmt else 0L,
                                            categoryName = selectedCategoryName,
                                            subCategoryName = selectedSubCategoryName,
                                            accountGroup = selectedAccountGroup,
                                            accountName = selectedAccountName,
                                            toAccountGroup = toAccountGroup,
                                            toAccountName = toAccountName,
                                            note = noteText,
                                            description = descriptionText,
                                            tag = tagText,
                                            repetition = repetition
                                        )
                                        onSaveTransaction(rec, false)
                                    }
                                },
                                enabled = evaluatedAmount > 0L,
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = activeAccentColor,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("mm_save_tx_btn")
                            ) {
                                Text("Save", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                // =========================================================================
                // FLOATING 2-COLUMN OVERLAY PICKERS (Category, Account, Repetition)
                // =========================================================================

                // Background Scrim for floating overlays (tap outside to close)
                if (activePicker != ActiveOverlayPicker.NONE) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable { activePicker = ActiveOverlayPicker.NONE }
                    )
                }

                if (activePicker == ActiveOverlayPicker.CATEGORY) {
                    TwoColumnCategoryOverlayPicker(
                        categories = categoriesForType,
                        initialCategoryName = selectedCategoryName,
                        selectedSubCategoryName = selectedSubCategoryName,
                        accentColor = activeAccentColor,
                        onSelectCategoryAndSub = { catName, subName ->
                            selectedCategoryName = catName
                            selectedSubCategoryName = subName
                            activePicker = ActiveOverlayPicker.NONE
                        },
                        onClose = { activePicker = ActiveOverlayPicker.NONE },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }

                if (activePicker == ActiveOverlayPicker.ACCOUNT_PRIMARY) {
                    TwoColumnAccountOverlayPicker(
                        title = "Pilih Akun Rekening / Dompet",
                        accountGroups = accountGroups,
                        initialGroup = selectedAccountGroup,
                        selectedAccount = selectedAccountName,
                        accentColor = activeAccentColor,
                        onSelectGroupAndAccount = { grp, acc ->
                            selectedAccountGroup = grp
                            selectedAccountName = acc
                            activePicker = ActiveOverlayPicker.NONE
                        },
                        onClose = { activePicker = ActiveOverlayPicker.NONE },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }

                if (activePicker == ActiveOverlayPicker.ACCOUNT_TRANSFER_TO) {
                    TwoColumnAccountOverlayPicker(
                        title = "Pilih Akun Tujuan Transfer (To)",
                        accountGroups = accountGroups,
                        initialGroup = toAccountGroup,
                        selectedAccount = toAccountName,
                        accentColor = activeAccentColor,
                        onSelectGroupAndAccount = { grp, acc ->
                            toAccountGroup = grp
                            toAccountName = acc
                            activePicker = ActiveOverlayPicker.NONE
                        },
                        onClose = { activePicker = ActiveOverlayPicker.NONE },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }

                if (activePicker == ActiveOverlayPicker.REPETITION) {
                    RepetitionOverlayPicker(
                        selectedOption = repetition,
                        accentColor = activeAccentColor,
                        onSelectOption = { opt ->
                            repetition = opt
                            activePicker = ActiveOverlayPicker.NONE
                        },
                        onClose = { activePicker = ActiveOverlayPicker.NONE },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(20.dp)
                    )
                }

                if (activePicker == ActiveOverlayPicker.AMOUNT_CALCULATOR) {
                    AmountCalculatorOverlayPicker(
                        expression = amountExpression,
                        accentColor = activeAccentColor,
                        onExpressionChange = { amountExpression = it },
                        onEvaluateNow = {
                            val res = evaluateSimpleMathExpression(amountExpression)
                            if (res > 0L) amountExpression = res.toString()
                        },
                        onDoneClick = {
                            val res = evaluateSimpleMathExpression(amountExpression)
                            if (res > 0L) amountExpression = res.toString()
                            activePicker = ActiveOverlayPicker.NONE
                        },
                        onClose = { activePicker = ActiveOverlayPicker.NONE },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }

                if (activePicker == ActiveOverlayPicker.FEE_CALCULATOR) {
                    AmountCalculatorOverlayPicker(
                        expression = feeExpression,
                        accentColor = MoneyExpenseRed,
                        onExpressionChange = { feeExpression = it },
                        onEvaluateNow = {
                            val res = evaluateSimpleMathExpression(feeExpression)
                            if (res > 0L) feeExpression = res.toString()
                        },
                        onDoneClick = {
                            val res = evaluateSimpleMathExpression(feeExpression)
                            if (res > 0L) feeExpression = res.toString()
                            activePicker = ActiveOverlayPicker.NONE
                        },
                        onClose = { activePicker = ActiveOverlayPicker.NONE },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }

                // Destructive Delete Confirmation Dialog ("Hapus item? Ya / Tidak")
                if (showDeleteConfirm && existingTransaction != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .clickable { showDeleteConfirm = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ElevatedIvoryCard,
                            border = BorderStroke(1.dp, SubtleSandBorder),
                            modifier = Modifier
                                .width(340.dp)
                                .clickable(enabled = false) {}
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Hapus item?",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RichEspressoPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Transaksi '${existingTransaction.note.ifBlank { existingTransaction.categoryName }}' (${WaffleoViewModel.formatIdr(existingTransaction.amount)}) akan dihapus permanen.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedCocoaText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = SoftOatSurface,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(50))
                                            .clickable { showDeleteConfirm = false }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("Tidak", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            showDeleteConfirm = false
                                            onDeleteTransaction(existingTransaction.id)
                                        },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = TerracottaDanger,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Text("Ya, Hapus", style = MaterialTheme.typography.labelLarge)
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
private fun FormRowContainer(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MutedCocoaText,
            modifier = Modifier.width(82.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

/**
 * 2-Column Floating Overlay Picker for Category (Left = Main Category, Right = Sub-Category)
 */
@Composable
private fun TwoColumnCategoryOverlayPicker(
    categories: List<MoneyCategoryDef>,
    initialCategoryName: String,
    selectedSubCategoryName: String,
    accentColor: Color,
    onSelectCategoryAndSub: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeMainCat by remember(initialCategoryName, categories) {
        mutableStateOf(
            categories.firstOrNull { it.categoryName == initialCategoryName }
                ?: categories.firstOrNull()
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = RichEspressoPrimary,
        border = BorderStroke(1.5.dp, HoneyCaramelAccent),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(295.dp)
            .zIndex(30f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category (Kolom Kiri: Kategori Utama • Kolom Kanan: Sub-Kategori)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ElevatedIvoryCard
                )
                IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = ElevatedIvoryCard)
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                // Left Column: Main Categories
                LazyColumn(
                    modifier = Modifier
                        .weight(0.46f)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = 0.12f))
                ) {
                    items(categories, key = { it.categoryName }) { cat ->
                        val isSel = activeMainCat?.categoryName == cat.categoryName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSel) accentColor.copy(alpha = 0.35f) else Color.Transparent)
                                .clickable {
                                    activeMainCat = cat
                                    if (cat.subCategories.isEmpty()) {
                                        onSelectCategoryAndSub(cat.categoryName, "")
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.categoryName,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = ElevatedIvoryCard
                            )
                            if (cat.subCategories.isNotEmpty()) {
                                Text("›", style = MaterialTheme.typography.titleMedium, color = HoneyCaramelAccent)
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    }
                }

                // Right Column: Sub-Categories for activeMainCat
                val subs = activeMainCat?.subCategories.orEmpty()
                LazyColumn(
                    modifier = Modifier
                        .weight(0.54f)
                        .fillMaxHeight()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (subs.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = HoneyCaramelAccent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        activeMainCat?.let { onSelectCategoryAndSub(it.categoryName, "") }
                                    }
                            ) {
                                Text(
                                    text = "Pilih '${activeMainCat?.categoryName.orEmpty()}' (Tanpa Sub-Kategori)",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = RichEspressoPrimary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    } else {
                        items(subs, key = { it }) { sub ->
                            val isSubSelected = selectedSubCategoryName == sub
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSubSelected) HoneyCaramelAccent else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        activeMainCat?.let { onSelectCategoryAndSub(it.categoryName, sub) }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sub,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isSubSelected) RichEspressoPrimary else ElevatedIvoryCard
                                    )
                                    if (isSubSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = RichEspressoPrimary,
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
 * 2-Column Floating Overlay Picker for Accounts (Left = Account Group, Right = Specific Account)
 */
@Composable
private fun TwoColumnAccountOverlayPicker(
    title: String,
    accountGroups: List<MoneyAccountGroupDef>,
    initialGroup: String,
    selectedAccount: String,
    accentColor: Color,
    onSelectGroupAndAccount: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeGroup by remember(initialGroup, accountGroups) {
        mutableStateOf(
            accountGroups.firstOrNull { it.groupName == initialGroup }
                ?: accountGroups.firstOrNull()
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = RichEspressoPrimary,
        border = BorderStroke(1.5.dp, HoneyCaramelAccent),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .zIndex(30f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ElevatedIvoryCard
                )
                IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = ElevatedIvoryCard)
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                // Left Column: 4 Account Groups (Uang Tunai, Bank, Dompet Digital, Operasional)
                LazyColumn(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = 0.12f))
                ) {
                    items(accountGroups, key = { it.groupName }) { grp ->
                        val isSel = activeGroup?.groupName == grp.groupName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSel) accentColor.copy(alpha = 0.35f) else Color.Transparent)
                                .clickable { activeGroup = grp }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = grp.groupName,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = ElevatedIvoryCard
                            )
                            Text("›", style = MaterialTheme.typography.titleMedium, color = HoneyCaramelAccent)
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    }
                }

                // Right Column: Accounts in activeGroup
                val accounts = activeGroup?.accounts.orEmpty()
                LazyColumn(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(accounts, key = { it }) { acc ->
                        val isAccSelected = selectedAccount == acc
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAccSelected) HoneyCaramelAccent else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    activeGroup?.let { onSelectGroupAndAccount(it.groupName, acc) }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = acc,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isAccSelected) RichEspressoPrimary else ElevatedIvoryCard
                                )
                                if (isAccSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = RichEspressoPrimary,
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

/**
 * Repetition Picker Overlay (matching Screenshot 6: None, Every Day, Weekdays, Weekend, Every Week, etc.)
 */
@Composable
private fun RepetitionOverlayPicker(
    selectedOption: MoneyRepetitionOption,
    accentColor: Color,
    onSelectOption: (MoneyRepetitionOption) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = ElevatedIvoryCard,
        border = BorderStroke(1.5.dp, RichEspressoPrimary),
        shadowElevation = 20.dp,
        modifier = modifier
            .width(340.dp)
            .heightIn(max = 460.dp)
            .zIndex(40f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Repetition (Otomatis Berulang)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RichEspressoPrimary
                )
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = SubtleSandBorder)
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(MoneyRepetitionOption.entries, key = { it.name }) { opt ->
                    val isSel = opt == selectedOption
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) accentColor.copy(alpha = 0.14f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectOption(opt) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = opt.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSel) accentColor else RichEspressoPrimary
                            )
                            if (isSel) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = accentColor,
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

/**
 * Overlay Calculator Picker triggered by clicking Amount (matching user request)
 */
@Composable
private fun AmountCalculatorOverlayPicker(
    expression: String,
    accentColor: Color,
    onExpressionChange: (String) -> Unit,
    onEvaluateNow: () -> Unit,
    onDoneClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val evaluated = remember(expression) { evaluateSimpleMathExpression(expression) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ElevatedIvoryCard,
        border = BorderStroke(1.dp, SubtleSandBorder),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {}
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Bar
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
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Kalkulator Nominal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RichEspressoPrimary
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                }
            }

            // Calculation Display Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftOatSurface,
                border = BorderStroke(1.dp, SubtleSandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Keypad
            MoneyCalculatorKeypad(
                expression = expression,
                accentColor = accentColor,
                onExpressionChange = onExpressionChange,
                onEvaluateNow = onEvaluateNow,
                onDoneClick = onDoneClick
            )
        }
    }
}

/**
 * Interactive Calculator Keypad matching Screenshot 6
 */
@Composable
private fun MoneyCalculatorKeypad(
    expression: String,
    accentColor: Color,
    onExpressionChange: (String) -> Unit,
    onEvaluateNow: () -> Unit,
    onDoneClick: () -> Unit
) {
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
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    when (key) {
                                        "Done" -> onDoneClick()
                                        "=" -> onEvaluateNow()
                                        "C" -> onExpressionChange("")
                                        "⌫" -> if (expression.isNotEmpty()) {
                                            onExpressionChange(expression.dropLast(1))
                                        }
                                        else -> onExpressionChange(expression + key)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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

// =========================================================================================
// HELPER FUNCTIONS
// =========================================================================================

private fun buildTransactionRecord(
    existing: MoneyTransactionRecord?,
    txType: MoneyTransactionType,
    dateKey: String,
    timeText: String,
    amount: Long,
    feeAmount: Long,
    categoryName: String,
    subCategoryName: String,
    accountGroup: String,
    accountName: String,
    toAccountGroup: String,
    toAccountName: String,
    note: String,
    description: String,
    tag: String,
    repetition: MoneyRepetitionOption
): MoneyTransactionRecord {
    val resolvedDate = dateKey.trim().ifEmpty { "2026-10-31" }
    return MoneyTransactionRecord(
        id = existing?.id.orEmpty(),
        branchId = existing?.branchId.orEmpty(),
        type = txType,
        dateKey = resolvedDate,
        monthKey = resolvedDate.take(7),
        timeFormatted = timeText.trim().ifEmpty { "12:00" },
        amount = amount,
        transferFee = if (txType == MoneyTransactionType.TRANSFER) feeAmount else 0L,
        category = if (txType == MoneyTransactionType.TRANSFER) "Transfer" else categoryName,
        subCategory = if (txType == MoneyTransactionType.TRANSFER) "" else subCategoryName,
        accountGroup = accountGroup,
        accountName = accountName,
        toAccountGroup = if (txType == MoneyTransactionType.TRANSFER) toAccountGroup else "",
        toAccountName = if (txType == MoneyTransactionType.TRANSFER) toAccountName else "",
        note = note.trim(),
        description = description.trim(),
        tag = tag.trim(),
        repetition = repetition.label
    )
}

private fun evaluateSimpleMathExpression(expr: String): Long {
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

private fun formatMonthKeyLabel(monthKey: String): String {
    val parts = monthKey.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = parts.getOrNull(1)?.toIntOrNull() ?: 10
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val mLabel = monthNames.getOrElse((month - 1).coerceIn(0, 11)) { "Oct" }
    return "$mLabel $year"
}

private fun parseDateHeaderParts(dateKey: String): Triple<String, String, String> {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dt = sdf.parse(dateKey) ?: Date()
        val cal = Calendar.getInstance().apply { time = dt }
        val dayNum = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
        val dayOfWeek = SimpleDateFormat("EEE", Locale.US).format(dt)
        val monthYear = SimpleDateFormat("MM.yyyy", Locale.US).format(dt)
        Triple(dayNum, dayOfWeek, monthYear)
    } catch (e: Exception) {
        Triple(dateKey.takeLast(2), "Day", dateKey.take(7).replace("-", "."))
    }
}

private fun buildSixMonthTrendForCategory(
    allTransactions: List<MoneyTransactionRecord>,
    endMonthKey: String,
    categoryName: String,
    type: MoneyTransactionType
): List<Pair<String, Long>> {
    val parts = endMonthKey.split("-")
    val endYear = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val endMonth = parts.getOrNull(1)?.toIntOrNull() ?: 10
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, endYear)
        set(Calendar.MONTH, endMonth - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, -5)
    }

    val result = mutableListOf<Pair<String, Long>>()
    repeat(6) {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val mKey = String.format(Locale.US, "%04d-%02d", y, m)
        val label = monthNames.getOrElse((m - 1).coerceIn(0, 11)) { "" }
        val sum = allTransactions
            .filter { it.dateKey.startsWith(mKey) && it.type == type && it.categoryName == categoryName }
            .sumOf { it.amount }
        result.add(label to sum)
        cal.add(Calendar.MONTH, 1)
    }
    return result
}

private fun formatShortIdr(amount: Long): String {
    return when {
        amount >= 1_000_000L -> String.format(Locale.US, "%.1fjt", amount / 1_000_000.0)
        amount >= 1_000L -> "${(amount / 1_000.0).roundToInt()}rb"
        else -> amount.toString()
    }
}
