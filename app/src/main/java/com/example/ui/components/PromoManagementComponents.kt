package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CatalogRecord
import com.example.data.model.PromoBenefitType
import com.example.data.model.PromoBundleGroupRule
import com.example.data.model.PromoConfig
import com.example.data.model.PromoPerformanceStat
import com.example.data.model.PromoQuantityTier
import com.example.data.model.PromoStatus
import com.example.data.model.PromoType
import com.example.data.model.WaffleoPromoEngine
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
import com.example.ui.viewmodel.WaffleoViewModel

@Composable
fun PromoStatusBadge(status: PromoStatus) {
    val (bg, fg) = when (status) {
        PromoStatus.ACTIVE -> SoftSageBg to MatchaSageSuccess
        PromoStatus.SCHEDULED -> SoftGoldHighlight to DeepCaramelGold
        PromoStatus.DRAFT -> SoftOatSurface to MutedCocoaText
        PromoStatus.PAUSED -> SoftAmberBg to HoneyAmberWarning
        PromoStatus.EXPIRED -> SoftTerracottaBg to TerracottaDanger
        PromoStatus.EXHAUSTED -> SoftTerracottaBg to TerracottaDanger
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.28f))
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromoManagementSection(
    promos: List<CatalogRecord>,
    allCatalog: List<CatalogRecord>,
    branches: List<String>,
    activeBranchId: String,
    searchQuery: String,
    canEdit: Boolean,
    getUsageCount: (CatalogRecord) -> Int,
    isPromoUsedInTx: (CatalogRecord) -> Boolean,
    performanceStats: List<PromoPerformanceStat>,
    onCreateNewPromo: () -> Unit,
    onEditPromo: (CatalogRecord) -> Unit,
    onDuplicatePromo: (CatalogRecord) -> Unit,
    onTogglePromoActive: (CatalogRecord) -> Unit,
    onDeleteOrArchivePromo: (CatalogRecord) -> Unit
) {
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var detailPromoTarget by remember { mutableStateOf<CatalogRecord?>(null) }
    var deleteConfirmTarget by remember { mutableStateOf<CatalogRecord?>(null) }

    val enrichedPromos = remember(promos, searchQuery) {
        promos.map { rec ->
            val cfg = PromoConfig.fromCatalogRecord(rec)
            val used = getUsageCount(rec)
            val status = WaffleoPromoEngine.resolvePromoStatus(rec, cfg, used)
            Triple(rec, cfg, status)
        }.filter { (rec, cfg, _) ->
            searchQuery.isBlank() ||
                rec.name.contains(searchQuery, ignoreCase = true) ||
                cfg.description.contains(searchQuery, ignoreCase = true) ||
                cfg.promoType.label.contains(searchQuery, ignoreCase = true) ||
                cfg.voucherCode.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredPromos = remember(enrichedPromos, selectedStatusFilter) {
        if (selectedStatusFilter == "ALL" || selectedStatusFilter == "PERFORMANCE") {
            enrichedPromos
        } else {
            enrichedPromos.filter { it.third.code == selectedStatusFilter }
        }
    }

    val filterTabs = remember(enrichedPromos) {
        val countAll = enrichedPromos.size
        val countActive = enrichedPromos.count { it.third == PromoStatus.ACTIVE }
        val countScheduled = enrichedPromos.count { it.third == PromoStatus.SCHEDULED }
        val countPaused = enrichedPromos.count { it.third == PromoStatus.PAUSED }
        val countExpired = enrichedPromos.count { it.third == PromoStatus.EXPIRED }
        val countExhausted = enrichedPromos.count { it.third == PromoStatus.EXHAUSTED }
        val countDraft = enrichedPromos.count { it.third == PromoStatus.DRAFT }
        listOf(
            "ALL" to "Semua ($countAll)",
            "ACTIVE" to "Aktif ($countActive)",
            "SCHEDULED" to "Terjadwal ($countScheduled)",
            "PAUSED" to "Nonaktif ($countPaused)",
            "EXPIRED" to "Kedaluwarsa ($countExpired)",
            "EXHAUSTED" to "Kuota Habis ($countExhausted)",
            "DRAFT" to "Draft ($countDraft)",
            "PERFORMANCE" to "📊 Laporan Performa"
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            filterTabs.forEach { (key, label) ->
                val isSelected = selectedStatusFilter == key
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) RichEspressoPrimary else ElevatedIvoryCard,
                    border = BorderStroke(1.dp, if (isSelected) RichEspressoPrimary else SubtleSandBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { selectedStatusFilter = key }
                        .testTag("promo_filter_$key")
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SoftGoldHighlight else RichEspressoPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedStatusFilter == "PERFORMANCE") {
            PromoPerformanceReportSection(
                stats = performanceStats,
                onBackToList = { selectedStatusFilter = "ALL" }
            )
        } else if (filteredPromos.isEmpty()) {
            // Informative Empty State
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                border = BorderStroke(1.dp, SubtleSandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SoftGoldHighlight,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = DeepCaramelGold,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedStatusFilter == "ALL" && searchQuery.isBlank()) {
                            "Belum Ada Promo Terdaftar"
                        } else {
                            "Tidak Ada Promo pada Filter Ini"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Buat program Paket Kombo (Waffle + Minuman), Diskon Jumlah Pembelian, Diskon Produk, Happy Hour, atau Minimum Pembelian yang otomatis aktif di layar Kasir/POS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedCocoaText
                    )
                    if (canEdit && selectedStatusFilter == "ALL") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCreateNewPromo,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RichEspressoPrimary,
                                contentColor = SoftGoldHighlight
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("promo_empty_create_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buat Promo Pertama", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredPromos, key = { it.first.id }) { (promo, cfg, status) ->
                    val usedCount = getUsageCount(promo)
                    PromoManagementCard(
                        promo = promo,
                        config = cfg,
                        status = status,
                        usedCount = usedCount,
                        canEdit = canEdit,
                        onViewDetail = { detailPromoTarget = promo },
                        onEdit = { onEditPromo(promo) },
                        onDuplicate = { onDuplicatePromo(promo) },
                        onToggleActive = { onTogglePromoActive(promo) },
                        onDelete = { deleteConfirmTarget = promo }
                    )
                }
            }
        }
    }

    detailPromoTarget?.let { target ->
        val cfg = PromoConfig.fromCatalogRecord(target)
        val used = getUsageCount(target)
        val status = WaffleoPromoEngine.resolvePromoStatus(target, cfg, used)
        val stat = performanceStats.firstOrNull { it.promoId == target.id }
        PromoDetailModalDialog(
            promo = target,
            config = cfg,
            status = status,
            usedCount = used,
            stat = stat,
            canEdit = canEdit,
            onDismiss = { detailPromoTarget = null },
            onEdit = {
                val p = target
                detailPromoTarget = null
                onEditPromo(p)
            }
        )
    }

    deleteConfirmTarget?.let { target ->
        val alreadyUsed = isPromoUsedInTx(target)
        Dialog(onDismissRequest = { deleteConfirmTarget = null }) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                border = BorderStroke(1.dp, SubtleSandBorder),
                modifier = Modifier.width(420.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (alreadyUsed) "Arsipkan Promo '${target.name}'?" else "Hapus Promo '${target.name}'?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichEspressoPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (alreadyUsed) {
                            "Promo ini sudah pernah digunakan dalam transaksi kasir. Untuk menjaga keutuhan histori transaksi dan laporan keuangan, promo akan dinonaktifkan & diarsipkan secara aman."
                        } else {
                            "Promo ini belum pernah digunakan dalam transaksi dan akan dihapus secara permanen dari daftar promo."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedCocoaText
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { deleteConfirmTarget = null },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Batal", color = RichEspressoPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val p = target
                                deleteConfirmTarget = null
                                onDeleteOrArchivePromo(p)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TerracottaDanger,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("confirm_delete_promo_btn")
                        ) {
                            Text(if (alreadyUsed) "Nonaktifkan & Arsipkan" else "Hapus Promo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromoManagementCard(
    promo: CatalogRecord,
    config: PromoConfig,
    status: PromoStatus,
    usedCount: Int,
    canEdit: Boolean,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val benefitSummary = remember(config) { WaffleoPromoEngine.formatBenefitSummary(config) }
    val scheduleText = remember(config) {
        val dateRange = when {
            config.startDateKey.isNotBlank() && config.endDateKey.isNotBlank() -> "${config.startDateKey} s/d ${config.endDateKey}"
            config.startDateKey.isNotBlank() -> "Mulai ${config.startDateKey}"
            config.endDateKey.isNotBlank() -> "S/d ${config.endDateKey}"
            else -> "Setiap Hari (Tanpa Batas Tanggal)"
        }
        val happyHour = if (config.isHappyHourEnabled) " • Jam ${config.startTimeText}-${config.endTimeText}" else ""
        "$dateRange$happyHour"
    }
    val branchLabel = remember(config, promo.branchId) {
        if (config.applyToAllBranches) "Semua Cabang"
        else if (config.targetBranches.isNotEmpty()) "Cabang: ${config.targetBranches.joinToString(", ")}"
        else "Cabang: ${promo.branchId}"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
        border = BorderStroke(1.dp, SubtleSandBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("promo_card_${promo.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Type Badge + Status Badge + Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftGoldHighlight
                    ) {
                        Text(
                            text = config.promoType.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepCaramelGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    PromoStatusBadge(status = status)
                }

                if (canEdit) {
                    Switch(
                        checked = promo.isActive && !config.isDraft,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElevatedIvoryCard,
                            checkedTrackColor = MatchaSageSuccess,
                            uncheckedThumbColor = MutedCocoaText,
                            uncheckedTrackColor = SoftOatSurface
                        ),
                        modifier = Modifier.testTag("promo_switch_${promo.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Description
            Text(
                text = promo.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RichEspressoPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (config.description.isNotBlank()) {
                Text(
                    text = config.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedCocoaText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlight Box: Benefit / Harga Paket
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftOatSurface,
                border = BorderStroke(1.dp, SubtleSandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Benefit / Nilai Promo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = benefitSummary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MatchaSageSuccess
                        )
                    }
                    if (!config.isAutoApply && config.voucherCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RichEspressoPrimary
                        ) {
                            Text(
                                text = "Kode: ${config.voucherCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftGoldHighlight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftSageBg
                        ) {
                            Text(
                                text = "Otomatis POS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MatchaSageSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata Rows: Schedule, Branch, Quota
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MutedCocoaText, modifier = Modifier.size(13.dp))
                Text(text = scheduleText, style = MaterialTheme.typography.labelSmall, color = MutedCocoaText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = MutedCocoaText, modifier = Modifier.size(13.dp))
                Text(text = branchLabel, style = MaterialTheme.typography.labelSmall, color = MutedCocoaText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quota Row
            if (config.hasQuotaLimit && config.maxTotalQuota > 0) {
                val progress = (usedCount.toFloat() / config.maxTotalQuota.toFloat()).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kuota Terpakai: $usedCount / ${config.maxTotalQuota}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (usedCount >= config.maxTotalQuota) TerracottaDanger else RichEspressoPrimary
                    )
                    Text(
                        text = "Sisa ${(config.maxTotalQuota - usedCount).coerceAtLeast(0)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedCocoaText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (usedCount >= config.maxTotalQuota) TerracottaDanger else MatchaSageSuccess,
                    trackColor = SoftOatSurface
                )
            } else {
                Text(
                    text = "Digunakan: ${usedCount}x transaksi • Kuota Tanpa Batas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedCocoaText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SubtleSandBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row: Detail, Edit, Duplikasi, Hapus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SoftOatSurface,
                    border = BorderStroke(1.dp, SubtleSandBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onViewDetail() }
                        .testTag("promo_detail_btn_${promo.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Detail", tint = RichEspressoPrimary, modifier = Modifier.size(14.dp))
                        Text("Detail", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                    }
                }

                if (canEdit) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftOatSurface,
                            border = BorderStroke(1.dp, SubtleSandBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onDuplicate() }
                                .testTag("promo_duplicate_btn_${promo.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Duplikasi", tint = DeepCaramelGold, modifier = Modifier.size(13.dp))
                                Text("Duplikat", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = DeepCaramelGold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftGoldHighlight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onEdit() }
                                .testTag("promo_edit_btn_${promo.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RichEspressoPrimary, modifier = Modifier.size(13.dp))
                                Text("Edit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = SoftTerracottaBg,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable { onDelete() }
                                .testTag("promo_delete_btn_${promo.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = TerracottaDanger, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromoPerformanceReportSection(
    stats: List<PromoPerformanceStat>,
    onBackToList: () -> Unit
) {
    val totalTxWithPromo = stats.sumOf { it.transactionCount }
    val totalDiscountGiven = stats.sumOf { it.totalDiscountGiven }
    val totalNetRevenue = stats.sumOf { it.totalNetRevenue }

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary KPI Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                border = BorderStroke(1.dp, SubtleSandBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Transaksi Promo", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                    Text("$totalTxWithPromo Transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                border = BorderStroke(1.dp, SubtleSandBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Diskon Diberikan", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                    Text(WaffleoViewModel.formatIdr(totalDiscountGiven), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TerracottaDanger)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                border = BorderStroke(1.dp, SubtleSandBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Penjualan Bersih Promo", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                    Text(WaffleoViewModel.formatIdr(totalNetRevenue), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MatchaSageSuccess)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(stats, key = { it.promoId }) { stat ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                    border = BorderStroke(1.dp, SubtleSandBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.4f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(stat.promoName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                                PromoStatusBadge(stat.status)
                            }
                            Text(
                                text = "${stat.promoTypeLabel} • Produk Terlaris: ${stat.topProductsSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1.6f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Digunakan", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                                Text("${stat.usageCount}x (${stat.transactionCount} trx)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Diskon", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                                Text("-${WaffleoViewModel.formatIdr(stat.totalDiscountGiven)}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TerracottaDanger)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Penjualan Bersih", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                                Text(WaffleoViewModel.formatIdr(stat.totalNetRevenue), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MatchaSageSuccess)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromoDetailModalDialog(
    promo: CatalogRecord,
    config: PromoConfig,
    status: PromoStatus,
    usedCount: Int,
    stat: PromoPerformanceStat?,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val dayNames = mapOf(
        1 to "Senin", 2 to "Selasa", 3 to "Rabu", 4 to "Kamis",
        5 to "Jumat", 6 to "Sabtu", 7 to "Minggu"
    )
    val daysSummary = if (config.activeDaysOfWeek.size >= 7) {
        "Setiap Hari (Senin - Minggu)"
    } else {
        config.activeDaysOfWeek.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            border = BorderStroke(1.dp, SubtleSandBorder),
            modifier = Modifier
                .width(560.dp)
                .heightIn(max = 640.dp)
                .padding(16.dp)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PromoStatusBadge(status)
                            Text(
                                text = config.promoType.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = DeepCaramelGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = promo.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedCocoaText)
                    }
                }

                if (config.description.isNotBlank()) {
                    Text(
                        text = config.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedCocoaText,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SubtleSandBorder)
                Spacer(modifier = Modifier.height(12.dp))

                PromoDetailRow("Benefit Utama", WaffleoPromoEngine.formatBenefitSummary(config))
                if (config.promoType == PromoType.BUNDLE) {
                    val groupsDesc = config.bundleGroups.joinToString(" + ") { grp ->
                        val target = if (grp.targetProductNames.isNotEmpty()) grp.targetProductNames.joinToString("/")
                        else if (grp.targetCategories.isNotEmpty()) grp.targetCategories.joinToString("/")
                        else "Semua Produk"
                        "${grp.requiredQty}x ${grp.groupName} ($target)"
                    }
                    PromoDetailRow("Komposisi Paket", groupsDesc)
                } else if (config.promoType == PromoType.QUANTITY_DISCOUNT) {
                    val tiersDesc = config.quantityTiers.sortedBy { it.minQty }.joinToString(" • ") { t ->
                        val valStr = if (t.benefitType == PromoBenefitType.DISCOUNT_PERCENT) "${t.discountValue}%" else WaffleoViewModel.formatIdr(t.discountValue)
                        "Beli ≥${t.minQty} hemat $valStr"
                    }
                    PromoDetailRow("Tier Kuantitas", tiersDesc)
                }
                PromoDetailRow(
                    "Target Produk",
                    when {
                        config.applyToAllProducts -> "Semua Produk Menu"
                        config.targetProductNames.isNotEmpty() -> "Produk: ${config.targetProductNames.joinToString(", ")}"
                        config.targetCategories.isNotEmpty() -> "Kategori: ${config.targetCategories.joinToString(", ")}"
                        else -> "Sesuai Aturan Paket"
                    }
                )
                if (config.excludedProductNames.isNotEmpty()) {
                    PromoDetailRow("Produk Dikecualikan", config.excludedProductNames.joinToString(", "))
                }
                PromoDetailRow(
                    "Perlakuan Modifier",
                    if (config.includeModifiersInDiscount) "Ikut Didiskon" else "Harga Normal (Tidak Ikut Didiskon)"
                )
                PromoDetailRow(
                    "Periode Tanggal",
                    if (config.startDateKey.isBlank() && config.endDateKey.isBlank()) "Tanpa Batas Tanggal"
                    else "${config.startDateKey.ifBlank { "Awal" }} s/d ${config.endDateKey.ifBlank { "Seterusnya" }}"
                )
                PromoDetailRow("Hari Berlaku", daysSummary)
                PromoDetailRow(
                    "Jam Happy Hour",
                    if (config.isHappyHourEnabled) "${config.startTimeText} - ${config.endTimeText}" else "Seharian Penuh (00:00 - 23:59)"
                )
                PromoDetailRow(
                    "Cabang Berlaku",
                    if (config.applyToAllBranches) "Semua Cabang" else config.targetBranches.joinToString(", ").ifBlank { promo.branchId }
                )
                PromoDetailRow(
                    "Kuota & Pemakaian",
                    if (config.hasQuotaLimit) "$usedCount / ${config.maxTotalQuota} (Maks ${config.maxUsagePerTransaction}x per transaksi)"
                    else "$usedCount kali digunakan (Kuota Tanpa Batas)"
                )
                PromoDetailRow(
                    "Pemicu & Kombinasi",
                    buildString {
                        append(if (config.isAutoApply) "Otomatis di Kasir" else "Kode Voucher: ${config.voucherCode}")
                        append(" • Prioritas ${config.priority}")
                        append(if (config.isStackable) " • Dapat Digabung" else " • Tidak Dapat Digabung")
                    }
                )

                if (stat != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftOatSurface,
                        border = BorderStroke(1.dp, SubtleSandBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Ringkasan Performa Transaksi Aktual", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total Transaksi: ${stat.transactionCount} trx • Total Diskon: ${WaffleoViewModel.formatIdr(stat.totalDiscountGiven)} • Penjualan Bersih: ${WaffleoViewModel.formatIdr(stat.totalNetRevenue)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText
                            )
                            Text(
                                text = "Produk Terlaris di Promo Ini: ${stat.topProductsSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MatchaSageSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(50)) {
                        Text("Tutup", color = RichEspressoPrimary)
                    }
                    if (canEdit) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onEdit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RichEspressoPrimary,
                                contentColor = SoftGoldHighlight
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Promo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromoDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MutedCocoaText,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = RichEspressoPrimary,
            modifier = Modifier.weight(0.62f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromoFormModalDialog(
    existingPromo: CatalogRecord?,
    allCatalog: List<CatalogRecord>,
    availableMenuCategories: List<String>,
    availableBranches: List<String>,
    activeBranchId: String,
    onDismiss: () -> Unit,
    onSavePromo: (name: String, config: PromoConfig) -> Unit
) {
    val initialCfg = remember(existingPromo) {
        if (existingPromo != null) {
            PromoConfig.fromCatalogRecord(existingPromo)
        } else {
            PromoConfig(
                promoType = PromoType.BUNDLE,
                benefitType = PromoBenefitType.FIXED_PACKAGE_PRICE,
                fixedPackagePrice = 29000L,
                discountValue = 4000L,
                bundleGroups = listOf(
                    PromoBundleGroupRule("Waffle Pilihan", 1, listOf("Egg Waffle"), emptyList()),
                    PromoBundleGroupRule("Minuman Pilihan", 1, listOf("Drinks"), emptyList())
                ),
                quantityTiers = listOf(
                    PromoQuantityTier(2, PromoBenefitType.DISCOUNT_NOMINAL, 2000L),
                    PromoQuantityTier(3, PromoBenefitType.DISCOUNT_NOMINAL, 4000L)
                ),
                applyToAllBranches = true,
                targetBranches = listOf(activeBranchId)
            )
        }
    }

    var promoName by remember { mutableStateOf(existingPromo?.name ?: "") }
    var description by remember { mutableStateOf(initialCfg.description) }
    var selectedType by remember { mutableStateOf(initialCfg.promoType) }
    var isDraft by remember { mutableStateOf(initialCfg.isDraft) }

    // Target Promo State
    var applyToAllProducts by remember { mutableStateOf(initialCfg.applyToAllProducts) }
    val selectedCategories = remember {
        mutableStateListOf<String>().apply { addAll(initialCfg.targetCategories) }
    }
    val selectedProducts = remember {
        mutableStateListOf<String>().apply { addAll(initialCfg.targetProductNames) }
    }
    val excludedProducts = remember {
        mutableStateListOf<String>().apply { addAll(initialCfg.excludedProductNames) }
    }
    val bundleGroups = remember {
        mutableStateListOf<PromoBundleGroupRule>().apply {
            if (initialCfg.bundleGroups.isNotEmpty()) {
                addAll(initialCfg.bundleGroups)
            } else {
                add(PromoBundleGroupRule("Waffle Pilihan", 1, listOf("Egg Waffle"), emptyList()))
                add(PromoBundleGroupRule("Minuman Pilihan", 1, listOf("Drinks"), emptyList()))
            }
        }
    }

    // Benefit State
    var benefitType by remember { mutableStateOf(initialCfg.benefitType) }
    var discountValueText by remember { mutableStateOf(initialCfg.discountValue.toString()) }
    var fixedPackagePriceText by remember { mutableStateOf(initialCfg.fixedPackagePrice.toString()) }
    var maxDiscountCapText by remember { mutableStateOf(if (initialCfg.maxDiscountCap > 0L) initialCfg.maxDiscountCap.toString() else "0") }

    val quantityTiers = remember {
        mutableStateListOf<PromoQuantityTier>().apply {
            if (initialCfg.quantityTiers.isNotEmpty()) {
                addAll(initialCfg.quantityTiers)
            } else {
                add(PromoQuantityTier(2, PromoBenefitType.DISCOUNT_NOMINAL, 2000L))
                add(PromoQuantityTier(3, PromoBenefitType.DISCOUNT_NOMINAL, 4000L))
            }
        }
    }

    // Conditions State
    var minItemQtyText by remember { mutableStateOf(initialCfg.minItemQty.toString()) }
    var minPurchaseAmountText by remember { mutableStateOf(initialCfg.minPurchaseAmount.toString()) }
    var includeModifiersInDiscount by remember { mutableStateOf(initialCfg.includeModifiersInDiscount) }

    // Schedule & Happy Hour State
    var startDateKey by remember { mutableStateOf(initialCfg.startDateKey) }
    var endDateKey by remember { mutableStateOf(initialCfg.endDateKey) }
    val activeDays = remember {
        mutableStateListOf<Int>().apply { addAll(initialCfg.activeDaysOfWeek.ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) }) }
    }
    var isHappyHourEnabled by remember { mutableStateOf(initialCfg.isHappyHourEnabled) }
    var startTimeText by remember { mutableStateOf(initialCfg.startTimeText.ifBlank { "13:00" }) }
    var endTimeText by remember { mutableStateOf(initialCfg.endTimeText.ifBlank { "16:00" }) }

    // Branch State
    var applyToAllBranches by remember { mutableStateOf(initialCfg.applyToAllBranches) }
    val selectedBranches = remember {
        mutableStateListOf<String>().apply {
            if (initialCfg.targetBranches.isNotEmpty()) addAll(initialCfg.targetBranches)
            else add(activeBranchId)
        }
    }

    // Quota State
    var hasQuotaLimit by remember { mutableStateOf(initialCfg.hasQuotaLimit) }
    var maxTotalQuotaText by remember { mutableStateOf(if (initialCfg.maxTotalQuota > 0) initialCfg.maxTotalQuota.toString() else "100") }
    var maxUsagePerTxText by remember { mutableStateOf(initialCfg.maxUsagePerTransaction.toString()) }

    // Additional Settings State
    var isAutoApply by remember { mutableStateOf(initialCfg.isAutoApply) }
    var voucherCode by remember { mutableStateOf(initialCfg.voucherCode) }
    var priorityText by remember { mutableStateOf(initialCfg.priority.toString()) }
    var isStackable by remember { mutableStateOf(initialCfg.isStackable) }
    var allowShareItems by remember { mutableStateOf(initialCfg.allowShareItemsWithOtherPromos) }

    var validationError by remember { mutableStateOf<String?>(null) }

    val menuProducts = remember(allCatalog) {
        allCatalog.filter { it.recordType == "MENU_ITEM" }.distinctBy { it.name.lowercase() }
    }
    val allCategories = remember(availableMenuCategories, menuProducts) {
        (availableMenuCategories + menuProducts.map { it.category })
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            border = BorderStroke(1.dp, SubtleSandBorder),
            modifier = Modifier
                .width(740.dp)
                .heightIn(max = 680.dp)
                .padding(12.dp)
                .testTag("promo_form_modal")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftOatSurface)
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (existingPromo == null) "Buat Promo Baru Waffleo" else "Edit Promo: ${existingPromo.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "Atur aturan paket kombo, diskon bertingkat, Happy Hour, cabang, dan kuota secara fleksibel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = RichEspressoPrimary)
                    }
                }

                // Validation Error Banner
                validationError?.let { err ->
                    Surface(
                        color = SoftTerracottaBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ $err",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaDanger,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
                        )
                    }
                }

                // Scrollable Form Body (Sections A - H)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- SECTION A: INFORMASI UMUM ---
                    PromoSectionCard(title = "A. Informasi Umum Promo") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WaffleoSoftTextField(
                                value = promoName,
                                onValueChange = { promoName = it; validationError = null },
                                label = "Nama Promo *",
                                placeholder = "Contoh: Paket Hemat Waffle + Drink",
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                modifier = Modifier.weight(0.55f),
                                testTag = "promo_name_input"
                            )
                            WaffleoSoftTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = "Deskripsi Singkat",
                                placeholder = "Contoh: 1 Waffle + 1 Minuman hanya Rp29.000",
                                modifier = Modifier.weight(0.45f),
                                testTag = "promo_desc_input"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Jenis Promo *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PromoType.entries.forEach { pType ->
                                val isSel = selectedType == pType
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSel) RichEspressoPrimary else SoftOatSurface,
                                    border = BorderStroke(1.dp, if (isSel) RichEspressoPrimary else SubtleSandBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedType = pType
                                            if (pType == PromoType.BUNDLE && benefitType != PromoBenefitType.FIXED_PACKAGE_PRICE) {
                                                benefitType = PromoBenefitType.FIXED_PACKAGE_PRICE
                                            } else if (pType != PromoType.BUNDLE && benefitType == PromoBenefitType.FIXED_PACKAGE_PRICE) {
                                                benefitType = PromoBenefitType.DISCOUNT_NOMINAL
                                            }
                                        }
                                        .testTag("promo_type_${pType.code}")
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = pType.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) SoftGoldHighlight else RichEspressoPrimary
                                        )
                                        Text(
                                            text = pType.shortDesc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSel) ElevatedIvoryCard.copy(alpha = 0.85f) else MutedCocoaText
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Status Penyimpanan:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            PromoOptionChip(
                                label = "Aktif / Siap Jalan",
                                selected = !isDraft,
                                onClick = { isDraft = false }
                            )
                            PromoOptionChip(
                                label = "Simpan sebagai Draft",
                                selected = isDraft,
                                onClick = { isDraft = true }
                            )
                        }
                    }

                    // --- SECTION B: TARGET PROMO ---
                    PromoSectionCard(title = "B. Target Produk / Komposisi Paket") {
                        if (selectedType == PromoType.BUNDLE) {
                            Text(
                                text = "Tentukan kelompok produk yang harus ada di keranjang untuk membentuk 1 Paket Kombo:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedCocoaText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            bundleGroups.forEachIndexed { idx, grp ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SoftOatSurface,
                                    border = BorderStroke(1.dp, SubtleSandBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            WaffleoSoftTextField(
                                                value = grp.groupName,
                                                onValueChange = { newName ->
                                                    bundleGroups[idx] = grp.copy(groupName = newName)
                                                },
                                                label = "Nama Kelompok #${idx + 1}",
                                                placeholder = "Waffle / Minuman",
                                                modifier = Modifier.weight(0.55f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            WaffleoSoftTextField(
                                                value = grp.requiredQty.toString(),
                                                onValueChange = { qStr ->
                                                    val q = qStr.filter { it.isDigit() }.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                                    bundleGroups[idx] = grp.copy(requiredQty = q)
                                                },
                                                label = "Jumlah Wajib",
                                                placeholder = "1",
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(0.28f)
                                            )
                                            if (bundleGroups.size > 1) {
                                                IconButton(onClick = { bundleGroups.removeAt(idx) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Hapus Kelompok", tint = TerracottaDanger)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Kategori yang Diizinkan di Kelompok #${idx + 1}:", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            allCategories.forEach { cat ->
                                                val isCatSel = grp.targetCategories.any { it.equals(cat, ignoreCase = true) }
                                                PromoOptionChip(
                                                    label = cat,
                                                    selected = isCatSel,
                                                    onClick = {
                                                        val nextCats = if (isCatSel) {
                                                            grp.targetCategories.filterNot { it.equals(cat, ignoreCase = true) }
                                                        } else {
                                                            grp.targetCategories + cat
                                                        }
                                                        bundleGroups[idx] = grp.copy(targetCategories = nextCats)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    bundleGroups.add(
                                        PromoBundleGroupRule(
                                            groupName = "Kelompok #${bundleGroups.size + 1}",
                                            requiredQty = 1,
                                            targetCategories = listOf(allCategories.firstOrNull() ?: "Egg Waffle")
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Kelompok Produk Paket", color = RichEspressoPrimary)
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PromoOptionChip(
                                    label = "Semua Produk Menu",
                                    selected = applyToAllProducts,
                                    onClick = { applyToAllProducts = true }
                                )
                                PromoOptionChip(
                                    label = "Kategori / Produk Tertentu",
                                    selected = !applyToAllProducts,
                                    onClick = { applyToAllProducts = false }
                                )
                            }

                            if (!applyToAllProducts) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pilih Kategori Target:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    allCategories.forEach { cat ->
                                        val isSel = selectedCategories.any { it.equals(cat, ignoreCase = true) }
                                        PromoOptionChip(
                                            label = cat,
                                            selected = isSel,
                                            onClick = {
                                                if (isSel) selectedCategories.removeAll { it.equals(cat, ignoreCase = true) }
                                                else selectedCategories.add(cat)
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Atau Pilih Produk Spesifik (Kosongkan jika berlaku untuk seluruh kategori di atas):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    menuProducts.forEach { prod ->
                                        val isSel = selectedProducts.any { it.equals(prod.name, ignoreCase = true) }
                                        PromoOptionChip(
                                            label = prod.name,
                                            selected = isSel,
                                            onClick = {
                                                if (isSel) selectedProducts.removeAll { it.equals(prod.name, ignoreCase = true) }
                                                else selectedProducts.add(prod.name)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- SECTION C: BENEFIT PROMO ---
                    PromoSectionCard(title = "C. Benefit & Perhitungan Diskon") {
                        when (selectedType) {
                            PromoType.BUNDLE -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PromoBenefitType.entries.forEach { bType ->
                                        PromoOptionChip(
                                            label = bType.label,
                                            selected = benefitType == bType,
                                            onClick = { benefitType = bType }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                if (benefitType == PromoBenefitType.FIXED_PACKAGE_PRICE) {
                                    WaffleoSoftTextField(
                                        value = fixedPackagePriceText,
                                        onValueChange = { fixedPackagePriceText = it.filter { c -> c.isDigit() } },
                                        label = "Harga Tetap Paket Promo (Rp) *",
                                        placeholder = "Contoh: 29000",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(0.5f),
                                        testTag = "promo_fixed_price_input"
                                    )
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        WaffleoSoftTextField(
                                            value = discountValueText,
                                            onValueChange = { discountValueText = it.filter { c -> c.isDigit() } },
                                            label = if (benefitType == PromoBenefitType.DISCOUNT_PERCENT) "Persentase Diskon (%) *" else "Nominal Potongan Paket (Rp) *",
                                            placeholder = if (benefitType == PromoBenefitType.DISCOUNT_PERCENT) "15" else "4000",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            testTag = "promo_discount_val_input"
                                        )
                                        WaffleoSoftTextField(
                                            value = maxDiscountCapText,
                                            onValueChange = { maxDiscountCapText = it.filter { c -> c.isDigit() } },
                                            label = "Maksimal Potongan Rp (0 = Tanpa Batas)",
                                            placeholder = "0",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            PromoType.QUANTITY_DISCOUNT -> {
                                Text(
                                    text = "Atur tingkatan diskon berdasarkan jumlah produk yang dibeli (Contoh: Beli 2 diskon Rp2.000, Beli 3 diskon Rp4.000):",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedCocoaText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                quantityTiers.forEachIndexed { idx, tier ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        WaffleoSoftTextField(
                                            value = tier.minQty.toString(),
                                            onValueChange = { qStr ->
                                                val q = qStr.filter { it.isDigit() }.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                                quantityTiers[idx] = tier.copy(minQty = q)
                                            },
                                            label = "Min. Jumlah Beli",
                                            placeholder = "2",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(0.28f)
                                        )
                                        PromoOptionChip(
                                            label = "Rp",
                                            selected = tier.benefitType == PromoBenefitType.DISCOUNT_NOMINAL,
                                            onClick = { quantityTiers[idx] = tier.copy(benefitType = PromoBenefitType.DISCOUNT_NOMINAL) }
                                        )
                                        PromoOptionChip(
                                            label = "%",
                                            selected = tier.benefitType == PromoBenefitType.DISCOUNT_PERCENT,
                                            onClick = { quantityTiers[idx] = tier.copy(benefitType = PromoBenefitType.DISCOUNT_PERCENT) }
                                        )
                                        WaffleoSoftTextField(
                                            value = tier.discountValue.toString(),
                                            onValueChange = { vStr ->
                                                val v = vStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                                quantityTiers[idx] = tier.copy(discountValue = v)
                                            },
                                            label = if (tier.benefitType == PromoBenefitType.DISCOUNT_PERCENT) "Diskon (%)" else "Potongan (Rp)",
                                            placeholder = "2000",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(0.42f)
                                        )
                                        if (quantityTiers.size > 1) {
                                            IconButton(onClick = { quantityTiers.removeAt(idx) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus Tier", tint = TerracottaDanger)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = {
                                        val nextMin = (quantityTiers.maxOfOrNull { it.minQty } ?: 1) + 1
                                        quantityTiers.add(PromoQuantityTier(nextMin, PromoBenefitType.DISCOUNT_NOMINAL, 4000L))
                                    },
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah Tingkatan Jumlah (Tier)", color = RichEspressoPrimary)
                                }
                            }

                            PromoType.PRODUCT_DISCOUNT, PromoType.MINIMUM_PURCHASE -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PromoOptionChip(
                                        label = "Diskon Nominal (Rp)",
                                        selected = benefitType == PromoBenefitType.DISCOUNT_NOMINAL,
                                        onClick = { benefitType = PromoBenefitType.DISCOUNT_NOMINAL }
                                    )
                                    PromoOptionChip(
                                        label = "Diskon Persentase (%)",
                                        selected = benefitType == PromoBenefitType.DISCOUNT_PERCENT,
                                        onClick = { benefitType = PromoBenefitType.DISCOUNT_PERCENT }
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    WaffleoSoftTextField(
                                        value = discountValueText,
                                        onValueChange = { discountValueText = it.filter { c -> c.isDigit() } },
                                        label = if (benefitType == PromoBenefitType.DISCOUNT_PERCENT) "Persentase Diskon (%) *" else "Nominal Diskon (Rp) *",
                                        placeholder = if (benefitType == PromoBenefitType.DISCOUNT_PERCENT) "10" else "5000",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        testTag = "promo_discount_val_input"
                                    )
                                    WaffleoSoftTextField(
                                        value = maxDiscountCapText,
                                        onValueChange = { maxDiscountCapText = it.filter { c -> c.isDigit() } },
                                        label = "Maksimal Potongan Rp (0 = Tanpa Batas)",
                                        placeholder = "0",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // --- SECTION D: SYARAT & KETENTUAN PROMO ---
                    PromoSectionCard(title = "D. Syarat Minimum & Perlakuan Modifier") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            WaffleoSoftTextField(
                                value = minPurchaseAmountText,
                                onValueChange = { minPurchaseAmountText = it.filter { c -> c.isDigit() } },
                                label = "Minimum Total Belanja (Rp)",
                                placeholder = "0",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                testTag = "promo_min_purchase_input"
                            )
                            WaffleoSoftTextField(
                                value = minItemQtyText,
                                onValueChange = { minItemQtyText = it.filter { c -> c.isDigit() } },
                                label = "Minimum Jumlah Item",
                                placeholder = "1",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sertakan Harga Modifier / Topping Tambahan dalam Perhitungan Diskon",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                                Text(
                                    text = "Jika nonaktif (rekomendasi), harga topping/add-on tambahan tetap dihitung normal.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedCocoaText
                                )
                            }
                            Switch(
                                checked = includeModifiersInDiscount,
                                onCheckedChange = { includeModifiersInDiscount = it }
                            )
                        }

                        if (menuProducts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Produk yang Dikecualikan dari Promo (Opsional):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                menuProducts.forEach { prod ->
                                    val isEx = excludedProducts.any { it.equals(prod.name, ignoreCase = true) }
                                    PromoOptionChip(
                                        label = prod.name,
                                        selected = isEx,
                                        onClick = {
                                            if (isEx) excludedProducts.removeAll { it.equals(prod.name, ignoreCase = true) }
                                            else excludedProducts.add(prod.name)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // --- SECTION E: JADWAL BERLAKU & HAPPY HOUR ---
                    PromoSectionCard(title = "E. Jadwal Berlaku & Happy Hour") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            WaffleoSoftTextField(
                                value = startDateKey,
                                onValueChange = { startDateKey = it.trim() },
                                label = "Tanggal Mulai (YYYY-MM-DD, kosong = langsung)",
                                placeholder = "2026-11-01",
                                modifier = Modifier.weight(1f),
                                testTag = "promo_start_date_input"
                            )
                            WaffleoSoftTextField(
                                value = endDateKey,
                                onValueChange = { endDateKey = it.trim() },
                                label = "Tanggal Berakhir (YYYY-MM-DD, kosong = selamanya)",
                                placeholder = "2026-12-31",
                                modifier = Modifier.weight(1f),
                                testTag = "promo_end_date_input"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Hari Berlaku:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        val dayPairs = listOf(
                            1 to "Senin", 2 to "Selasa", 3 to "Rabu", 4 to "Kamis",
                            5 to "Jumat", 6 to "Sabtu", 7 to "Minggu"
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            dayPairs.forEach { (dNum, dLabel) ->
                                val isDayActive = dNum in activeDays
                                PromoOptionChip(
                                    label = dLabel,
                                    selected = isDayActive,
                                    onClick = {
                                        if (isDayActive && activeDays.size > 1) activeDays.remove(dNum)
                                        else if (!isDayActive) activeDays.add(dNum)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Aktifkan Jam Happy Hour", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                                Text("Batasi promo hanya pada rentang jam operasional tertentu (misal 13:00 - 16:00)", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                            }
                            Switch(
                                checked = isHappyHourEnabled,
                                onCheckedChange = { isHappyHourEnabled = it },
                                modifier = Modifier.testTag("promo_happy_hour_switch")
                            )
                        }
                        if (isHappyHourEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                WaffleoSoftTextField(
                                    value = startTimeText,
                                    onValueChange = { startTimeText = it },
                                    label = "Jam Mulai (HH:mm)",
                                    placeholder = "13:00",
                                    modifier = Modifier.weight(1f),
                                    testTag = "promo_start_time_input"
                                )
                                WaffleoSoftTextField(
                                    value = endTimeText,
                                    onValueChange = { endTimeText = it },
                                    label = "Jam Selesai (HH:mm)",
                                    placeholder = "16:00",
                                    modifier = Modifier.weight(1f),
                                    testTag = "promo_end_time_input"
                                )
                            }
                        }
                    }

                    // --- SECTION F: CABANG BERLAKU ---
                    PromoSectionCard(title = "F. Cabang Berlaku") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PromoOptionChip(
                                label = "Semua Cabang",
                                selected = applyToAllBranches,
                                onClick = { applyToAllBranches = true }
                            )
                            PromoOptionChip(
                                label = "Cabang Tertentu",
                                selected = !applyToAllBranches,
                                onClick = { applyToAllBranches = false }
                            )
                        }
                        if (!applyToAllBranches) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                availableBranches.forEach { br ->
                                    val isBrSel = selectedBranches.any { it.equals(br, ignoreCase = true) }
                                    PromoOptionChip(
                                        label = br,
                                        selected = isBrSel,
                                        onClick = {
                                            if (isBrSel && selectedBranches.size > 1) selectedBranches.removeAll { it.equals(br, ignoreCase = true) }
                                            else if (!isBrSel) selectedBranches.add(br)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // --- SECTION G: KUOTA PENGGUNAAN ---
                    PromoSectionCard(title = "G. Kuota Penggunaan") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PromoOptionChip(
                                label = "Tanpa Batas Kuota",
                                selected = !hasQuotaLimit,
                                onClick = { hasQuotaLimit = false }
                            )
                            PromoOptionChip(
                                label = "Batasi Total Kuota Promo",
                                selected = hasQuotaLimit,
                                onClick = { hasQuotaLimit = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (hasQuotaLimit) {
                                WaffleoSoftTextField(
                                    value = maxTotalQuotaText,
                                    onValueChange = { maxTotalQuotaText = it.filter { c -> c.isDigit() } },
                                    label = "Maksimal Total Kuota Promo *",
                                    placeholder = "100",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    testTag = "promo_max_quota_input"
                                )
                            }
                            WaffleoSoftTextField(
                                value = maxUsagePerTxText,
                                onValueChange = { maxUsagePerTxText = it.filter { c -> c.isDigit() } },
                                label = "Maksimal Paket per 1 Transaksi (0 = Tanpa Batas)",
                                placeholder = "0",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // --- SECTION H: PENGATURAN TAMBAHAN ---
                    PromoSectionCard(title = "H. Pengaturan Kasir, Prioritas & Kombinasi Promo") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PromoOptionChip(
                                label = "Otomatis Aktif di Kasir",
                                selected = isAutoApply,
                                onClick = { isAutoApply = true }
                            )
                            PromoOptionChip(
                                label = "Wajib Input Kode Voucher",
                                selected = !isAutoApply,
                                onClick = { isAutoApply = false }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!isAutoApply) {
                                WaffleoSoftTextField(
                                    value = voucherCode,
                                    onValueChange = { voucherCode = it.trim().uppercase() },
                                    label = "Kode Voucher Promo *",
                                    placeholder = "HEMAT29",
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                    modifier = Modifier.weight(1f),
                                    testTag = "promo_voucher_code_input"
                                )
                            }
                            WaffleoSoftTextField(
                                value = priorityText,
                                onValueChange = { priorityText = it.filter { c -> c.isDigit() } },
                                label = "Skor Prioritas (Semakin tinggi semakin diutamakan)",
                                placeholder = "10",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dapat Digabung dengan Promo Lain (Stackable)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = RichEspressoPrimary)
                                Text("Jika nonaktif, sistem otomatis memilih 1 promo terbaik yang paling menguntungkan pelanggan.", style = MaterialTheme.typography.labelSmall, color = MutedCocoaText)
                            }
                            Switch(checked = isStackable, onCheckedChange = { isStackable = it })
                        }
                    }
                }

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftOatSurface)
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(50)) {
                        Text("Batal", color = RichEspressoPrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val cleanName = promoName.trim()
                            if (cleanName.isBlank()) {
                                validationError = "Nama promo wajib diisi."
                                return@Button
                            }
                            val discVal = discountValueText.toLongOrNull() ?: 0L
                            val fixedPkg = fixedPackagePriceText.toLongOrNull() ?: 0L
                            val maxCap = maxDiscountCapText.toLongOrNull() ?: 0L
                            val minQty = (minItemQtyText.toIntOrNull() ?: 1).coerceAtLeast(1)
                            val minPurchase = (minPurchaseAmountText.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                            val maxQuota = (maxTotalQuotaText.toIntOrNull() ?: 0).coerceAtLeast(0)
                            val maxPerTx = (maxUsagePerTxText.toIntOrNull() ?: 0).coerceAtLeast(0)
                            val prio = priorityText.toIntOrNull() ?: 10

                            if (selectedType == PromoType.BUNDLE) {
                                if (bundleGroups.isEmpty()) {
                                    validationError = "Paket kombo harus memiliki minimal 1 kelompok produk."
                                    return@Button
                                }
                                if (benefitType == PromoBenefitType.FIXED_PACKAGE_PRICE && fixedPkg <= 0L) {
                                    validationError = "Harga tetap paket harus lebih dari Rp 0."
                                    return@Button
                                }
                                if (benefitType == PromoBenefitType.DISCOUNT_PERCENT && (discVal <= 0L || discVal > 100L)) {
                                    validationError = "Persentase diskon paket harus di antara 1% - 100%."
                                    return@Button
                                }
                                if (benefitType == PromoBenefitType.DISCOUNT_NOMINAL && discVal <= 0L) {
                                    validationError = "Nominal potongan paket harus lebih dari Rp 0."
                                    return@Button
                                }
                            } else if (selectedType == PromoType.QUANTITY_DISCOUNT) {
                                if (quantityTiers.isEmpty() || quantityTiers.any { it.discountValue <= 0L }) {
                                    validationError = "Setiap tingkatan (tier) diskon jumlah harus memiliki nilai diskon > 0."
                                    return@Button
                                }
                                if (quantityTiers.any { it.benefitType == PromoBenefitType.DISCOUNT_PERCENT && it.discountValue > 100L }) {
                                    validationError = "Persentase diskon pada tier tidak boleh melebihi 100%."
                                    return@Button
                                }
                            } else {
                                if (discVal <= 0L) {
                                    validationError = "Nilai diskon harus lebih dari 0."
                                    return@Button
                                }
                                if (benefitType == PromoBenefitType.DISCOUNT_PERCENT && discVal > 100L) {
                                    validationError = "Persentase diskon tidak boleh melebihi 100%."
                                    return@Button
                                }
                            }

                            if (startDateKey.isNotBlank() && endDateKey.isNotBlank() && endDateKey < startDateKey) {
                                validationError = "Tanggal berakhir tidak boleh lebih awal dari tanggal mulai."
                                return@Button
                            }
                            if (hasQuotaLimit && maxQuota <= 0) {
                                validationError = "Kuota maksimal harus lebih dari 0 jika pembatasan kuota diaktifkan."
                                return@Button
                            }
                            if (!isAutoApply && voucherCode.isBlank()) {
                                validationError = "Kode voucher wajib diisi jika promo tidak otomatis aktif."
                                return@Button
                            }

                            val finalCfg = PromoConfig(
                                description = description.trim(),
                                promoType = selectedType,
                                isDraft = isDraft,
                                isArchived = false,
                                benefitType = benefitType,
                                discountValue = discVal,
                                fixedPackagePrice = fixedPkg,
                                maxDiscountCap = maxCap,
                                applyToAllProducts = applyToAllProducts,
                                targetCategories = selectedCategories.toList(),
                                targetProductNames = selectedProducts.toList(),
                                excludedProductNames = excludedProducts.toList(),
                                includeModifiersInDiscount = includeModifiersInDiscount,
                                bundleGroups = bundleGroups.toList(),
                                quantityTiers = quantityTiers.sortedBy { it.minQty },
                                minItemQty = minQty,
                                minPurchaseAmount = minPurchase,
                                startDateKey = startDateKey,
                                endDateKey = endDateKey,
                                activeDaysOfWeek = activeDays.toSet(),
                                isHappyHourEnabled = isHappyHourEnabled,
                                startTimeText = startTimeText,
                                endTimeText = endTimeText,
                                applyToAllBranches = applyToAllBranches,
                                targetBranches = selectedBranches.toList(),
                                hasQuotaLimit = hasQuotaLimit,
                                maxTotalQuota = maxQuota,
                                maxUsagePerTransaction = maxPerTx,
                                usedCount = initialCfg.usedCount,
                                isAutoApply = isAutoApply,
                                voucherCode = voucherCode,
                                priority = prio,
                                isStackable = isStackable,
                                allowShareItemsWithOtherPromos = allowShareItems
                            )
                            onSavePromo(cleanName, finalCfg)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RichEspressoPrimary,
                            contentColor = SoftGoldHighlight
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("promo_save_submit_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (existingPromo == null) "Simpan Promo Baru" else "Simpan Perubahan Promo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PromoSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SoftOatSurface.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, SubtleSandBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = RichEspressoPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun PromoOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) RichEspressoPrimary else ElevatedIvoryCard,
        border = BorderStroke(1.dp, if (selected) RichEspressoPrimary else SubtleSandBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) SoftGoldHighlight else RichEspressoPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
