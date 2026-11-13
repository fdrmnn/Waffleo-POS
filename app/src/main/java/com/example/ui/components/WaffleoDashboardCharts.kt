package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.example.data.model.CartLineItem
import com.example.data.model.CatalogRecord
import com.example.data.model.OrderRecord
import com.example.data.model.areCategoriesMatching
import com.example.data.model.normalizeCategoryCanonical
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
import com.example.ui.viewmodel.HistoryFilterPeriod
import com.example.ui.viewmodel.WaffleoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

enum class HourlyStatMode(val label: String) {
    MEAN("Mean"),
    MEDIAN("Median"),
    MODUS("Modus")
}

enum class CustomChartGranularity(val label: String) {
    PER_DATE("Per Tanggal"),
    PER_MONTH("Per Bulan")
}

data class HourlySalesPoint(
    val hour: Int,
    val label: String,
    val grossRevenue: Long,
    val netRevenue: Long,
    val orderCount: Int
)

data class PeriodSalesPoint(
    val index: Int,
    val label: String,
    val fullLabel: String,
    val grossRevenue: Long,
    val netRevenue: Long,
    val orderCount: Int
)

data class MediaSliceStat(
    val mediaName: String,
    val orderCount: Int,
    val grossRevenue: Long,
    val netRevenue: Long,
    val feeCut: Long,
    val color: Color,
    val isPlatform: Boolean,
    val subtitle: String = "",
    val pillBg: Color = SoftSageBg,
    val pillText: Color = MatchaSageSuccess,
    val icon: ImageVector = Icons.Default.Payments
)

data class TopProductStat(
    val id: String = "",
    val name: String,
    val category: String,
    val subCategory: String = "",
    val modifierBadgeText: String = "",
    val qtySold: Int,
    val grossRevenue: Long
)

private fun computeMedianLong(values: List<Long>): Long {
    if (values.isEmpty()) return 0L
    val sorted = values.sorted()
    val mid = sorted.size / 2
    return if (sorted.size % 2 == 1) {
        sorted[mid]
    } else {
        ((sorted[mid - 1] + sorted[mid]) / 2.0).roundToLong()
    }
}

private fun computeMedianInt(values: List<Int>): Int {
    if (values.isEmpty()) return 0
    val sorted = values.sorted()
    val mid = sorted.size / 2
    return if (sorted.size % 2 == 1) {
        sorted[mid]
    } else {
        ((sorted[mid - 1] + sorted[mid]) / 2.0).roundToInt()
    }
}

private fun computeModusLong(values: List<Long>, fallbackPool: List<Long> = emptyList()): Long {
    val nonZero = values.filter { it > 0L }
    if (nonZero.isEmpty()) return 0L
    val freq = nonZero.groupingBy { it }.eachCount()
    val maxFreq = freq.values.maxOrNull() ?: 1
    if (maxFreq > 1) {
        return freq.entries
            .filter { it.value == maxFreq }
            .maxByOrNull { it.key }?.key ?: nonZero.first()
    }
    if (fallbackPool.isNotEmpty()) {
        val poolNonZero = fallbackPool.filter { it > 0L }
        val poolFreq = poolNonZero.groupingBy { it }.eachCount()
        val poolMaxFreq = poolFreq.values.maxOrNull() ?: 1
        if (poolMaxFreq > 1) {
            return poolFreq.entries
                .filter { it.value == poolMaxFreq }
                .maxByOrNull { it.key }?.key ?: poolNonZero.first()
        }
        return poolNonZero.minOrNull() ?: nonZero.first()
    }
    return nonZero.minOrNull() ?: nonZero.first()
}

private fun computeModusInt(values: List<Int>, fallbackPool: List<Int> = emptyList()): Int {
    val nonZero = values.filter { it > 0 }
    if (nonZero.isEmpty()) return 0
    val freq = nonZero.groupingBy { it }.eachCount()
    val maxFreq = freq.values.maxOrNull() ?: 1
    if (maxFreq > 1) {
        return freq.entries
            .filter { it.value == maxFreq }
            .maxByOrNull { it.key }?.key ?: nonZero.first()
    }
    if (fallbackPool.isNotEmpty()) {
        val poolNonZero = fallbackPool.filter { it > 0 }
        val poolFreq = poolNonZero.groupingBy { it }.eachCount()
        val poolMaxFreq = poolFreq.values.maxOrNull() ?: 1
        if (poolMaxFreq > 1) {
            return poolFreq.entries
                .filter { it.value == poolMaxFreq }
                .maxByOrNull { it.key }?.key ?: poolNonZero.first()
        }
        return poolNonZero.minOrNull() ?: nonZero.first()
    }
    return nonZero.minOrNull() ?: nonZero.first()
}

/**
 * Computes flexible hourly sales points dynamically from store order timestamps.
 * Supports single-day totals as well as multi-day Mean, Median, and Modus aggregation per hour.
 */
fun buildHourlySalesSeries(
    doneOrders: List<OrderRecord>,
    statMode: HourlyStatMode = HourlyStatMode.MEAN,
    isMultiDayPeriod: Boolean = false,
    calculateNet: (OrderRecord) -> Long
): List<HourlySalesPoint> {
    val dateKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val resolvedOrders = doneOrders.map { order ->
        val date = WaffleoViewModel.resolveOrderTimestampDate(order)
        val cal = Calendar.getInstance().apply { time = date }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val dateKey = dateKeyFormatter.format(date)
        Triple(order, hour, dateKey)
    }

    val orderHours = resolvedOrders.map { it.second }
    val minObservedHour = orderHours.minOrNull() ?: 9
    val maxObservedHour = orderHours.maxOrNull() ?: 21

    val startHour = minOf(minObservedHour, 8)
    val endHour = maxOf(maxObservedHour, 21)
    val hours = (startHour..endHour).toList()

    if (!isMultiDayPeriod) {
        val grossByHour = mutableMapOf<Int, Long>()
        val netByHour = mutableMapOf<Int, Long>()
        val countByHour = mutableMapOf<Int, Int>()

        hours.forEach { h ->
            grossByHour[h] = 0L
            netByHour[h] = 0L
            countByHour[h] = 0
        }

        resolvedOrders.forEach { (order, rawHour, _) ->
            val bucketHour = rawHour.coerceIn(startHour, endHour)
            val net = calculateNet(order)
            grossByHour[bucketHour] = (grossByHour[bucketHour] ?: 0L) + order.totalAmount
            netByHour[bucketHour] = (netByHour[bucketHour] ?: 0L) + net
            countByHour[bucketHour] = (countByHour[bucketHour] ?: 0) + 1
        }

        return hours.map { h ->
            HourlySalesPoint(
                hour = h,
                label = String.format("%02d:00", h),
                grossRevenue = grossByHour[h] ?: 0L,
                netRevenue = netByHour[h] ?: 0L,
                orderCount = countByHour[h] ?: 0
            )
        }
    }

    // Multi-day aggregation (This Week, This Month, This Year, Custom) with Mean / Median / Modus
    val activeDatesInPeriod = resolvedOrders.map { it.third }.distinct()
    val activeDaysCount = activeDatesInPeriod.size.coerceAtLeast(1)

    val ordersByHour = resolvedOrders.groupBy { it.second.coerceIn(startHour, endHour) }

    return hours.map { h ->
        val entriesAtHour = ordersByHour[h].orEmpty()
        if (entriesAtHour.isEmpty()) {
            HourlySalesPoint(
                hour = h,
                label = String.format("%02d:00", h),
                grossRevenue = 0L,
                netRevenue = 0L,
                orderCount = 0
            )
        } else {
            val byDate = entriesAtHour.groupBy { it.third }
            val dailyGrossTotals = byDate.values.map { list -> list.sumOf { it.first.totalAmount } }
            val dailyNetTotals = byDate.values.map { list -> list.sumOf { calculateNet(it.first) } }
            val dailyOrderCounts = byDate.values.map { list -> list.size }

            val individualGross = entriesAtHour.map { it.first.totalAmount }
            val individualNet = entriesAtHour.map { calculateNet(it.first) }
            val individualItemCounts = entriesAtHour.map {
                CartLineItem.deserializeList(it.first.itemsPayload).sumOf { item -> item.qty }.coerceAtLeast(1)
            }

            when (statMode) {
                HourlyStatMode.MEAN -> {
                    val totalGross = entriesAtHour.sumOf { it.first.totalAmount }
                    val totalNet = entriesAtHour.sumOf { calculateNet(it.first) }
                    val totalOrders = entriesAtHour.size
                    val meanGross = (totalGross.toDouble() / activeDaysCount.toDouble()).roundToLong()
                    val meanNet = (totalNet.toDouble() / activeDaysCount.toDouble()).roundToLong()
                    val meanCount = (totalOrders.toDouble() / activeDaysCount.toDouble()).roundToInt().coerceAtLeast(1)
                    HourlySalesPoint(
                        hour = h,
                        label = String.format("%02d:00", h),
                        grossRevenue = meanGross,
                        netRevenue = meanNet,
                        orderCount = meanCount
                    )
                }
                HourlyStatMode.MEDIAN -> {
                    val medGross = if (byDate.size >= 2) {
                        computeMedianLong(dailyGrossTotals)
                    } else {
                        computeMedianLong(individualGross)
                    }
                    val medNet = if (byDate.size >= 2) {
                        computeMedianLong(dailyNetTotals)
                    } else {
                        computeMedianLong(individualNet)
                    }
                    val medCount = if (byDate.size >= 2) {
                        computeMedianInt(dailyOrderCounts).coerceAtLeast(1)
                    } else {
                        ((entriesAtHour.size + 1) / 2).coerceAtLeast(1)
                    }
                    HourlySalesPoint(
                        hour = h,
                        label = String.format("%02d:00", h),
                        grossRevenue = medGross,
                        netRevenue = medNet,
                        orderCount = medCount
                    )
                }
                HourlyStatMode.MODUS -> {
                    val modGross = if (byDate.size >= 2) {
                        computeModusLong(dailyGrossTotals, fallbackPool = individualGross)
                    } else {
                        computeModusLong(individualGross)
                    }
                    val modNet = if (byDate.size >= 2) {
                        computeModusLong(dailyNetTotals, fallbackPool = individualNet)
                    } else {
                        computeModusLong(individualNet)
                    }
                    val modCount = if (byDate.size >= 2) {
                        computeModusInt(dailyOrderCounts, fallbackPool = individualItemCounts).coerceAtLeast(1)
                    } else {
                        computeModusInt(individualItemCounts).coerceAtLeast(1)
                    }
                    HourlySalesPoint(
                        hour = h,
                        label = String.format("%02d:00", h),
                        grossRevenue = modGross,
                        netRevenue = modNet,
                        orderCount = modCount
                    )
                }
            }
        }
    }
}

/**
 * Computes daily sales points for the days of the week (Senin to Minggu).
 */
fun buildWeeklyDaysSalesSeries(
    doneOrders: List<OrderRecord>,
    calculateNet: (OrderRecord) -> Long
): List<PeriodSalesPoint> {
    val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    val grossByDay = LongArray(7)
    val netByDay = LongArray(7)
    val countByDay = IntArray(7)

    doneOrders.forEach { order ->
        val ts = WaffleoViewModel.resolveOrderTimestampDate(order)
        val cal = Calendar.getInstance().apply { time = ts }
        val dayIndex = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
        val net = calculateNet(order)
        grossByDay[dayIndex] += order.totalAmount
        netByDay[dayIndex] += net
        countByDay[dayIndex] += 1
    }

    return dayNames.mapIndexed { i, name ->
        PeriodSalesPoint(
            index = i,
            label = name,
            fullLabel = "Hari $name",
            grossRevenue = grossByDay[i],
            netRevenue = netByDay[i],
            orderCount = countByDay[i]
        )
    }
}

/**
 * Computes daily sales points for dates of the month (Tgl 01 to 30/31).
 */
fun buildMonthlyDatesSalesSeries(
    doneOrders: List<OrderRecord>,
    calculateNet: (OrderRecord) -> Long
): List<PeriodSalesPoint> {
    val calNow = Calendar.getInstance()
    val maxDays = calNow.getActualMaximum(Calendar.DAY_OF_MONTH).coerceIn(28, 31)

    val grossByDate = LongArray(maxDays)
    val netByDate = LongArray(maxDays)
    val countByDate = IntArray(maxDays)

    doneOrders.forEach { order ->
        val ts = WaffleoViewModel.resolveOrderTimestampDate(order)
        val cal = Calendar.getInstance().apply { time = ts }
        val dateIndex = (cal.get(Calendar.DAY_OF_MONTH) - 1).coerceIn(0, maxDays - 1)
        val net = calculateNet(order)
        grossByDate[dateIndex] += order.totalAmount
        netByDate[dateIndex] += net
        countByDate[dateIndex] += 1
    }

    return (1..maxDays).map { d ->
        val idx = d - 1
        PeriodSalesPoint(
            index = idx,
            label = String.format(Locale.US, "%02d", d),
            fullLabel = String.format(Locale.US, "Tanggal %02d", d),
            grossRevenue = grossByDate[idx],
            netRevenue = netByDate[idx],
            orderCount = countByDate[idx]
        )
    }
}

/**
 * Computes monthly sales points for months of the year (Jan to Des).
 */
fun buildAnnualMonthsSalesSeries(
    doneOrders: List<OrderRecord>,
    calculateNet: (OrderRecord) -> Long
): List<PeriodSalesPoint> {
    val monthShortNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
    val monthFullNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

    val grossByMonth = LongArray(12)
    val netByMonth = LongArray(12)
    val countByMonth = IntArray(12)

    doneOrders.forEach { order ->
        val ts = WaffleoViewModel.resolveOrderTimestampDate(order)
        val cal = Calendar.getInstance().apply { time = ts }
        val monthIndex = cal.get(Calendar.MONTH).coerceIn(0, 11)
        val net = calculateNet(order)
        grossByMonth[monthIndex] += order.totalAmount
        netByMonth[monthIndex] += net
        countByMonth[monthIndex] += 1
    }

    return monthShortNames.mapIndexed { i, shortName ->
        PeriodSalesPoint(
            index = i,
            label = shortName,
            fullLabel = "Bulan ${monthFullNames[i]}",
            grossRevenue = grossByMonth[i],
            netRevenue = netByMonth[i],
            orderCount = countByMonth[i]
        )
    }
}

/**
 * Computes chronological daily sales points strictly covering `[startMillis .. endMillis]`
 * (e.g. 01 Okt 2026 .. 04 Nov 2026) for Custom date ranges.
 */
fun buildCustomRangeDailySalesSeries(
    doneOrders: List<OrderRecord>,
    startMillis: Long,
    endMillis: Long,
    calculateNet: (OrderRecord) -> Long
): List<PeriodSalesPoint> {
    val minMs = minOf(startMillis, endMillis)
    val maxMs = maxOf(startMillis, endMillis)

    val calCursor = Calendar.getInstance().apply {
        timeInMillis = minMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val calEnd = Calendar.getInstance().apply {
        timeInMillis = maxMs
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    val keyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val shortFmt = SimpleDateFormat("dd MMM", Locale.forLanguageTag("id-ID"))
    val fullFmt = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))

    data class DaySlot(val key: String, val shortLabel: String, val fullLabel: String)
    val slots = mutableListOf<DaySlot>()
    var safety = 0
    while (calCursor.timeInMillis <= calEnd.timeInMillis && safety < 730) {
        val d = calCursor.time
        slots.add(
            DaySlot(
                key = keyFmt.format(d),
                shortLabel = shortFmt.format(d),
                fullLabel = fullFmt.format(d)
            )
        )
        calCursor.add(Calendar.DAY_OF_YEAR, 1)
        safety++
    }

    if (slots.isEmpty()) {
        val d = Date(minMs)
        slots.add(DaySlot(keyFmt.format(d), shortFmt.format(d), fullFmt.format(d)))
    }

    val grossMap = mutableMapOf<String, Long>()
    val netMap = mutableMapOf<String, Long>()
    val countMap = mutableMapOf<String, Int>()

    doneOrders.forEach { order ->
        val ts = WaffleoViewModel.resolveOrderTimestampDate(order)
        if (ts.time in minMs..calEnd.timeInMillis) {
            val k = keyFmt.format(ts)
            val net = calculateNet(order)
            grossMap[k] = (grossMap[k] ?: 0L) + order.totalAmount
            netMap[k] = (netMap[k] ?: 0L) + net
            countMap[k] = (countMap[k] ?: 0) + 1
        }
    }

    return slots.mapIndexed { idx, slot ->
        PeriodSalesPoint(
            index = idx,
            label = slot.shortLabel,
            fullLabel = slot.fullLabel,
            grossRevenue = grossMap[slot.key] ?: 0L,
            netRevenue = netMap[slot.key] ?: 0L,
            orderCount = countMap[slot.key] ?: 0
        )
    }
}

/**
 * Computes chronological monthly sales points strictly covering the months within `[startMillis .. endMillis]`
 * (e.g. Okt 2026, Nov 2026) for multi-month Custom date ranges.
 */
fun buildCustomRangeMonthlySalesSeries(
    doneOrders: List<OrderRecord>,
    startMillis: Long,
    endMillis: Long,
    calculateNet: (OrderRecord) -> Long
): List<PeriodSalesPoint> {
    val monthShortNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
    val monthFullNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

    val minMs = minOf(startMillis, endMillis)
    val maxMs = maxOf(startMillis, endMillis)

    val calStart = Calendar.getInstance().apply {
        timeInMillis = minMs
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val calEnd = Calendar.getInstance().apply {
        timeInMillis = maxMs
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val spanMultiYear = calStart.get(Calendar.YEAR) != calEnd.get(Calendar.YEAR)

    data class MonthSlot(val key: String, val shortLabel: String, val fullLabel: String)
    val slots = mutableListOf<MonthSlot>()
    val cursor = calStart.clone() as Calendar
    var safety = 0
    while (
        (cursor.get(Calendar.YEAR) < calEnd.get(Calendar.YEAR) ||
            (cursor.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) && cursor.get(Calendar.MONTH) <= calEnd.get(Calendar.MONTH))) &&
        safety < 60
    ) {
        val y = cursor.get(Calendar.YEAR)
        val m = cursor.get(Calendar.MONTH).coerceIn(0, 11)
        val key = String.format(Locale.US, "%04d-%02d", y, m + 1)
        val shortLabel = if (spanMultiYear) "${monthShortNames[m]} '${y % 100}" else "${monthShortNames[m]} $y"
        val fullLabel = "Bulan ${monthFullNames[m]} $y"
        slots.add(MonthSlot(key, shortLabel, fullLabel))
        cursor.add(Calendar.MONTH, 1)
        safety++
    }

    val grossMap = mutableMapOf<String, Long>()
    val netMap = mutableMapOf<String, Long>()
    val countMap = mutableMapOf<String, Int>()

    doneOrders.forEach { order ->
        val ts = WaffleoViewModel.resolveOrderTimestampDate(order)
        if (ts.time in minMs..calEnd.timeInMillis) {
            val c = Calendar.getInstance().apply { time = ts }
            val key = String.format(Locale.US, "%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
            val net = calculateNet(order)
            grossMap[key] = (grossMap[key] ?: 0L) + order.totalAmount
            netMap[key] = (netMap[key] ?: 0L) + net
            countMap[key] = (countMap[key] ?: 0) + 1
        }
    }

    return slots.mapIndexed { idx, slot ->
        PeriodSalesPoint(
            index = idx,
            label = slot.shortLabel,
            fullLabel = slot.fullLabel,
            grossRevenue = grossMap[slot.key] ?: 0L,
            netRevenue = netMap[slot.key] ?: 0L,
            orderCount = countMap[slot.key] ?: 0
        )
    }
}

data class ExecutiveAovStats(
    val averageOrderValue: Long,
    val totalItems: Int,
    val averageItemsPerOrder: Double,
    val maxOrderValue: Long,
    val minOrderValue: Long
)

fun computeExecutiveAovStats(doneOrders: List<OrderRecord>): ExecutiveAovStats {
    if (doneOrders.isEmpty()) {
        return ExecutiveAovStats(0L, 0, 0.0, 0L, 0L)
    }
    val totalOmzet = doneOrders.sumOf { it.totalAmount }
    val totalItems = doneOrders.sumOf { ord ->
        val parsed = ord.parsedLineItems
        if (parsed.isNotEmpty()) parsed.sumOf { it.qty } else 1
    }
    val maxVal = doneOrders.maxOf { it.totalAmount }
    val minVal = doneOrders.minOf { it.totalAmount }
    return ExecutiveAovStats(
        averageOrderValue = totalOmzet / doneOrders.size,
        totalItems = totalItems,
        averageItemsPerOrder = totalItems.toDouble() / doneOrders.size.toDouble(),
        maxOrderValue = maxVal,
        minOrderValue = minVal
    )
}

/**
 * Modern Executive KPI Row (Opsi A):
 * 1. Total Omzet Penjualan
 * 2. Pendapatan Bersih
 * 3. Potongan Platform & Promo
 * 4. Rata-rata per Pesanan (AOV)
 */
@Composable
fun ModernExecutiveKpiRow(
    omzetGross: Long,
    potonganPlatform: Long,
    totalDiskonPromo: Long = 0L,
    labaKotor: Long,
    doneCount: Int,
    totalItemsCount: Int = doneCount,
    onOmzetClick: (() -> Unit)? = null,
    onLabaKotorClick: (() -> Unit)? = null,
    onDeductionsClick: (() -> Unit)? = null,
    onAovClick: (() -> Unit)? = null
) {
    val grossBeforeDeductions = (omzetGross + totalDiskonPromo).coerceAtLeast(1L)
    val marginPct = if (omzetGross > 0L || totalDiskonPromo > 0L) {
        ((labaKotor.toDouble() / grossBeforeDeductions.toDouble()) * 100.0).roundToInt()
    } else 0
    val totalDeductions = potonganPlatform + totalDiskonPromo
    val deductionPct = if (omzetGross > 0L || totalDiskonPromo > 0L) {
        ((totalDeductions.toDouble() / grossBeforeDeductions.toDouble()) * 100.0).roundToInt()
    } else 0
    val avgOrderValue = if (doneCount > 0) omzetGross / doneCount else 0L
    val avgItemsPerOrder = if (doneCount > 0) totalItemsCount.toDouble() / doneCount.toDouble() else 0.0
    val aovBadgeText = if (doneCount > 0) {
        String.format(Locale.forLanguageTag("id-ID"), "%.1f item ↗", avgItemsPerOrder)
    } else {
        "0 item ↗"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExecutiveKpiCard(
            title = "Total Omzet Penjualan",
            value = WaffleoViewModel.formatIdr(omzetGross),
            badgeText = "$doneCount Done ↗",
            subtitle = "",
            icon = Icons.Default.Payments,
            accentColor = MatchaSageSuccess,
            badgeBg = SoftSageBg,
            onClick = onOmzetClick,
            modifier = Modifier.weight(1f)
        )
        ExecutiveKpiCard(
            title = "Pendapatan Bersih",
            value = WaffleoViewModel.formatIdr(labaKotor),
            badgeText = "Net $marginPct% ↗",
            subtitle = "",
            icon = Icons.Default.TrendingUp,
            accentColor = RichEspressoPrimary,
            badgeBg = SoftOatSurface,
            onClick = onLabaKotorClick,
            modifier = Modifier.weight(1f)
        )
        ExecutiveKpiCard(
            title = "Potongan Platform & Promo",
            value = WaffleoViewModel.formatIdr(totalDeductions),
            badgeText = "Rasio $deductionPct% ↗",
            subtitle = "",
            icon = Icons.Default.LocalOffer,
            accentColor = HoneyAmberWarning,
            badgeBg = SoftAmberBg,
            onClick = onDeductionsClick,
            modifier = Modifier.weight(1f)
        )
        ExecutiveKpiCard(
            title = "Rata-rata per Pesanan",
            value = WaffleoViewModel.formatIdr(avgOrderValue),
            badgeText = aovBadgeText,
            subtitle = "",
            icon = Icons.Default.Analytics,
            accentColor = Color(0xFF1D4ED8),
            badgeBg = Color(0xFFDBEAFE),
            onClick = onAovClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExecutiveKpiCard(
    title: String,
    value: String,
    badgeText: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badgeBg: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MutedCocoaText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedCocoaText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Recharts-inspired Interactive Hourly Sales Spline Area Chart:
 * - Smooth cubic Bezier curve + vertical area gradient
 * - Compares Gross Platform/Walk-In Revenue (dashed Champagne Gold) vs Net System Inflow (solid Matcha Sage)
 * - Interactive tap/click on any hour column to inspect exact hourly metrics in a floating Recharts tooltip card
 */
enum class TrendChartMetricMode {
    REVENUE,
    ORDERS
}

@Composable
private fun TrendMetricModeSwitcher(
    selectedMode: TrendChartMetricMode,
    onModeSelected: (TrendChartMetricMode) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = SoftOatSurface,
        border = BorderStroke(1.dp, SubtleSandBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isRevenue = selectedMode == TrendChartMetricMode.REVENUE
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isRevenue) RichEspressoPrimary else Color.Transparent,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onModeSelected(TrendChartMetricMode.REVENUE) }
                    .testTag("${testTagPrefix}_switch_revenue")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = if (isRevenue) MatchaSageSuccess else MutedCocoaText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Pendapatan",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isRevenue) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isRevenue) SoftGoldHighlight else MutedCocoaText
                    )
                }
            }

            val isOrders = selectedMode == TrendChartMetricMode.ORDERS
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isOrders) RichEspressoPrimary else Color.Transparent,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onModeSelected(TrendChartMetricMode.ORDERS) }
                    .testTag("${testTagPrefix}_switch_orders")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = if (isOrders) ChampagneGoldAccent else MutedCocoaText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Pesanan Masuk",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isOrders) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isOrders) SoftGoldHighlight else MutedCocoaText
                    )
                }
            }
        }
    }
}

@Composable
fun RechartsHourlyRevenueChartCard(
    hourlySeries: List<HourlySalesPoint>,
    doneOrders: List<OrderRecord> = emptyList(),
    calculateNet: ((OrderRecord) -> Long)? = null,
    selectedPeriod: HistoryFilterPeriod = HistoryFilterPeriod.TODAY,
    customRangeLabel: String = "",
    modifier: Modifier = Modifier
) {
    var metricMode by remember { mutableStateOf(TrendChartMetricMode.REVENUE) }
    val isOrderMode = metricMode == TrendChartMetricMode.ORDERS

    val isMultiDayPeriod = selectedPeriod != HistoryFilterPeriod.TODAY &&
        selectedPeriod != HistoryFilterPeriod.YESTERDAY

    var statMode by remember(selectedPeriod) { mutableStateOf(HourlyStatMode.MEAN) }

    val activeHourlySeries = remember(hourlySeries, doneOrders, statMode, isMultiDayPeriod, calculateNet) {
        if (calculateNet != null && statMode != HourlyStatMode.MEAN) {
            buildHourlySalesSeries(
                doneOrders = doneOrders,
                statMode = statMode,
                isMultiDayPeriod = isMultiDayPeriod,
                calculateNet = calculateNet
            )
        } else {
            hourlySeries
        }
    }

    val peakRevenueIdx = remember(activeHourlySeries) {
        val maxIdx = activeHourlySeries.indices.maxByOrNull { activeHourlySeries[it].netRevenue } ?: 0
        if (activeHourlySeries.getOrNull(maxIdx)?.netRevenue == 0L) 4 else maxIdx
    }
    val peakOrderIdx = remember(activeHourlySeries) {
        val maxIdx = activeHourlySeries.indices.maxByOrNull { activeHourlySeries[it].orderCount } ?: 0
        if (activeHourlySeries.getOrNull(maxIdx)?.orderCount == 0) 4 else maxIdx
    }
    val activePeakIdx = if (isOrderMode) peakOrderIdx else peakRevenueIdx

    var selectedHourIndex by remember(activeHourlySeries) { mutableIntStateOf(activePeakIdx) }
    var showGrossOverlay by remember { mutableStateOf(true) }

    val selectedPoint = activeHourlySeries.getOrElse(selectedHourIndex) {
        HourlySalesPoint(12, "12:00", 0L, 0L, 0)
    }
    val maxChartValue = remember(activeHourlySeries) {
        (activeHourlySeries.maxOfOrNull { maxOf(it.grossRevenue, it.netRevenue) } ?: 100_000L)
            .coerceAtLeast(50_000L)
    }
    val maxOrderChartValue = remember(activeHourlySeries) {
        val rawMax = activeHourlySeries.maxOfOrNull { it.orderCount } ?: 0
        maxOf(3, ((rawMax + 2) / 3) * 3)
    }
    val totalHourlyOrders = remember(activeHourlySeries) {
        activeHourlySeries.sumOf { it.orderCount }
    }

    val mainChartTitle = remember(selectedPeriod, isOrderMode) {
        when (selectedPeriod) {
            HistoryFilterPeriod.TODAY -> if (isOrderMode) {
                "Tren Pesanan Masuk per Jam"
            } else {
                "Tren Pendapatan Harian per Jam (Live Recharts Area)"
            }
            HistoryFilterPeriod.YESTERDAY -> if (isOrderMode) {
                "Tren Pesanan Masuk Harian per Jam (Kemarin)"
            } else {
                "Tren Pendapatan Harian per Jam (Kemarin)"
            }
            HistoryFilterPeriod.THIS_WEEK -> if (isOrderMode) {
                "Tren Pesanan Masuk Mingguan per Jam"
            } else {
                "Tren Pendapatan Mingguan per Jam"
            }
            HistoryFilterPeriod.THIS_MONTH -> if (isOrderMode) {
                "Tren Pesanan Masuk Bulanan per Jam"
            } else {
                "Tren Pendapatan Bulanan per Jam"
            }
            HistoryFilterPeriod.THIS_YEAR -> if (isOrderMode) {
                "Tren Pesanan Masuk Tahunan per Jam"
            } else {
                "Tren Pendapatan Tahunan per Jam"
            }
            HistoryFilterPeriod.CUSTOM -> {
                if (isOrderMode) {
                    "Tren Pesanan Masuk Custom per Jam"
                } else {
                    "Tren Pendapatan Custom per Jam"
                }
            }
        }
    }

    val customRangeSubLabel = remember(selectedPeriod, customRangeLabel) {
        if (selectedPeriod == HistoryFilterPeriod.CUSTOM && customRangeLabel.isNotBlank()) {
            "($customRangeLabel)"
        } else {
            ""
        }
    }

    val chartSubtitle = remember(isOrderMode, isMultiDayPeriod, statMode) {
        if (isMultiDayPeriod) {
            val modeDesc = when (statMode) {
                HourlyStatMode.MEAN -> "Rata-rata (Mean)"
                HourlyStatMode.MEDIAN -> "Nilai Tengah (Median)"
                HourlyStatMode.MODUS -> "Nilai Terbanyak (Modus)"
            }
            if (isOrderMode) {
                "$modeDesc jumlah pesanan masuk per jam pada rentang periode yang dipilih"
            } else {
                "$modeDesc pendapatan Gross & Uang Masuk Bersih per jam pada rentang periode yang dipilih"
            }
        } else {
            if (isOrderMode) {
                "Pantau jam ramai (rush hour) pesanan masuk untuk mengetahui trafik transaksi di setiap jam"
            } else {
                "Sentuh titik jam pada grafik untuk melihat detail transaksi, Gross Platform, dan Uang Masuk Bersih"
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_hourly_recharts_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Header + 2-Button Switcher & Legend Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = mainChartTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = RichEspressoPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (customRangeSubLabel.isNotBlank()) {
                            Text(
                                text = customRangeSubLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedCocoaText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        val peakPt = activeHourlySeries.getOrNull(activePeakIdx)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isOrderMode) SoftGoldHighlight else SoftSageBg,
                            border = if (isOrderMode) BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.6f)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (isOrderMode) DeepCaramelGold else MatchaSageSuccess,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isOrderMode) {
                                        "Peak: ${peakPt?.label ?: "13:00"} (${peakPt?.orderCount ?: 0} Pesanan)"
                                    } else {
                                        "Peak: ${peakPt?.label ?: "13:00"}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOrderMode) RichEspressoPrimary else MatchaSageSuccess,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = chartSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedCocoaText
                    )

                    // Separate Mean, Median, Modus Pill Buttons (for This Week / Month / Year / Custom)
                    if (isMultiDayPeriod) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HourlyStatMode.entries.forEach { mode ->
                                val isSelectedMode = statMode == mode
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelectedMode) RichEspressoPrimary else SoftOatSurface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelectedMode) ChampagneGoldAccent else SubtleSandBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { statMode = mode }
                                        .testTag("hourly_stat_mode_${mode.name.lowercase()}")
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelectedMode) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelectedMode) SoftGoldHighlight else RichEspressoPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Controls: Mode Switcher on top, Legend pills neatly placed beneath it
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TrendMetricModeSwitcher(
                        selectedMode = metricMode,
                        onModeSelected = { newMode ->
                            metricMode = newMode
                            selectedHourIndex = if (newMode == TrendChartMetricMode.ORDERS) peakOrderIdx else peakRevenueIdx
                        },
                        testTagPrefix = "hourly_chart"
                    )

                    if (!isOrderMode) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendDotPill(
                                color = MatchaSageSuccess,
                                label = "Uang Masuk Bersih",
                                active = true,
                                onClick = {}
                            )
                            LegendDotPill(
                                color = ChampagneGoldAccent,
                                label = "Gross Sebelum Potongan",
                                active = showGrossOverlay,
                                onClick = { showGrossOverlay = !showGrossOverlay }
                            )
                        }
                    } else {
                        LegendDotPill(
                            color = DeepCaramelGold,
                            label = if (isMultiDayPeriod) {
                                "${statMode.label} Akumulasi: $totalHourlyOrders Pesanan"
                            } else {
                                "Total: $totalHourlyOrders Pesanan"
                            },
                            active = true,
                            onClick = {}
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Recharts Tooltip Banner for the Selected Hour
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SoftOatSurface.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = RichEspressoPrimary
                        ) {
                            Text(
                                text = "Jam ${selectedPoint.label}",
                                style = MaterialTheme.typography.labelLarge,
                                color = SoftGoldHighlight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        if (isOrderMode) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftGoldHighlight,
                                border = BorderStroke(1.dp, ChampagneGoldAccent)
                            ) {
                                Text(
                                    text = "${selectedPoint.orderCount} Pesanan Masuk",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            if (totalHourlyOrders > 0) {
                                val pct = ((selectedPoint.orderCount * 100f) / totalHourlyOrders.toFloat()).roundToInt()
                                Text(
                                    text = "($pct% dari total pesanan)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MutedCocoaText
                                )
                            }
                        } else {
                            Text(
                                text = "${selectedPoint.orderCount} Pesanan Selesai",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = RichEspressoPrimary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Gross: ${WaffleoViewModel.formatIdr(selectedPoint.grossRevenue)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepCaramelGold
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftSageBg
                        ) {
                            Text(
                                text = "Net Masuk Sistem: ${WaffleoViewModel.formatIdr(selectedPoint.netRevenue)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MatchaSageSuccess,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Y-Axis Labels + Smooth Bezier Area Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(195.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Y-Axis Scale Column (Left)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 22.dp, end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    if (isOrderMode) {
                        Text(
                            text = "$maxOrderChartValue Pesanan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "${(maxOrderChartValue * 2) / 3} Pesanan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = "${maxOrderChartValue / 3} Pesanan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = "0 Pesanan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                    } else {
                        Text(
                            text = formatShortIdr(maxChartValue),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = formatShortIdr((maxChartValue * 0.66).toLong()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = formatShortIdr((maxChartValue * 0.33).toLong()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = "Rp 0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                    }
                }

                // Main Recharts Area + X-Axis Labels
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .pointerInput(activeHourlySeries) {
                                detectTapGestures { tapOffset ->
                                    if (activeHourlySeries.isNotEmpty() && size.width > 0) {
                                        val stepX = size.width.toFloat() / (activeHourlySeries.size - 1).coerceAtLeast(1)
                                        val tappedIdx = ((tapOffset.x + stepX / 2f) / stepX)
                                            .toInt()
                                            .coerceIn(0, activeHourlySeries.lastIndex)
                                        selectedHourIndex = tappedIdx
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val topPad = 12.dp.toPx()
                            val bottomPad = 8.dp.toPx()
                            val usableH = (h - topPad - bottomPad).coerceAtLeast(1f)
                            val count = activeHourlySeries.size
                            val stepX = if (count > 1) w / (count - 1).toFloat() else w

                            // 1. Horizontal Recharts dashed grid lines
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            for (i in 0..3) {
                                val y = topPad + usableH * (i / 3f)
                                drawLine(
                                    color = SubtleSandBorder.copy(alpha = 0.7f),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = if (i < 3) dashEffect else null
                                )
                            }

                            if (count > 1) {
                                if (isOrderMode) {
                                    val orderPoints = activeHourlySeries.mapIndexed { idx, pt ->
                                        val ratio = (pt.orderCount.toFloat() / maxOrderChartValue.toFloat()).coerceIn(0f, 1f)
                                        Offset(
                                            x = idx * stepX,
                                            y = topPad + usableH * (1f - ratio)
                                        )
                                    }

                                    val orderSpline = buildSmoothBezierPath(orderPoints)
                                    val areaPath = Path().apply {
                                        addPath(orderSpline)
                                        lineTo(orderPoints.last().x, topPad + usableH)
                                        lineTo(orderPoints.first().x, topPad + usableH)
                                        close()
                                    }
                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                DeepCaramelGold.copy(alpha = 0.38f),
                                                ChampagneGoldAccent.copy(alpha = 0.14f),
                                                Color.Transparent
                                            ),
                                            startY = topPad,
                                            endY = topPad + usableH
                                        )
                                    )
                                    drawPath(
                                        path = orderSpline,
                                        color = DeepCaramelGold,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    val selOffset = orderPoints.getOrNull(selectedHourIndex)
                                    if (selOffset != null) {
                                        drawLine(
                                            color = RichEspressoPrimary.copy(alpha = 0.35f),
                                            start = Offset(selOffset.x, topPad),
                                            end = Offset(selOffset.x, topPad + usableH),
                                            strokeWidth = 1.5.dp.toPx(),
                                            pathEffect = dashEffect
                                        )
                                    }

                                    orderPoints.forEachIndexed { idx, pt ->
                                        val isSelected = idx == selectedHourIndex
                                        if (isSelected) {
                                            drawCircle(
                                                color = DeepCaramelGold.copy(alpha = 0.24f),
                                                radius = 10.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = ElevatedIvoryCard,
                                                radius = 5.5.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = DeepCaramelGold,
                                                radius = 4.dp.toPx(),
                                                center = pt
                                            )
                                        } else {
                                            drawCircle(
                                                color = ElevatedIvoryCard,
                                                radius = 3.5.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = DeepCaramelGold,
                                                radius = 2.3.dp.toPx(),
                                                center = pt
                                            )
                                        }
                                    }
                                } else {
                                    val netPoints = activeHourlySeries.mapIndexed { idx, pt ->
                                        val ratio = (pt.netRevenue.toFloat() / maxChartValue.toFloat()).coerceIn(0f, 1f)
                                        Offset(
                                            x = idx * stepX,
                                            y = topPad + usableH * (1f - ratio)
                                        )
                                    }
                                    val grossPoints = activeHourlySeries.mapIndexed { idx, pt ->
                                        val ratio = (pt.grossRevenue.toFloat() / maxChartValue.toFloat()).coerceIn(0f, 1f)
                                        Offset(
                                            x = idx * stepX,
                                            y = topPad + usableH * (1f - ratio)
                                        )
                                    }

                                    // 2. Optional Gross Revenue comparison spline (Champagne Gold dashed curve)
                                    if (showGrossOverlay) {
                                        val grossSpline = buildSmoothBezierPath(grossPoints)
                                        drawPath(
                                            path = grossSpline,
                                            color = ChampagneGoldAccent.copy(alpha = 0.85f),
                                            style = Stroke(
                                                width = 2.2.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                                            )
                                        )
                                    }

                                    // 3. Net Revenue smooth cubic Bezier area fill + solid stroke
                                    val netSpline = buildSmoothBezierPath(netPoints)
                                    val areaPath = Path().apply {
                                        addPath(netSpline)
                                        lineTo(netPoints.last().x, topPad + usableH)
                                        lineTo(netPoints.first().x, topPad + usableH)
                                        close()
                                    }
                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MatchaSageSuccess.copy(alpha = 0.34f),
                                                ChampagneGoldAccent.copy(alpha = 0.10f),
                                                Color.Transparent
                                            ),
                                            startY = topPad,
                                            endY = topPad + usableH
                                        )
                                    )
                                    drawPath(
                                        path = netSpline,
                                        color = MatchaSageSuccess,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // 4. Selected Hour Vertical Crosshair Line
                                    val selOffset = netPoints.getOrNull(selectedHourIndex)
                                    if (selOffset != null) {
                                        drawLine(
                                            color = RichEspressoPrimary.copy(alpha = 0.35f),
                                            start = Offset(selOffset.x, topPad),
                                            end = Offset(selOffset.x, topPad + usableH),
                                            strokeWidth = 1.5.dp.toPx(),
                                            pathEffect = dashEffect
                                        )
                                    }

                                    // 5. Data Nodes along the spline
                                    netPoints.forEachIndexed { idx, pt ->
                                        val isSelected = idx == selectedHourIndex
                                        if (isSelected) {
                                            drawCircle(
                                                color = MatchaSageSuccess.copy(alpha = 0.22f),
                                                radius = 10.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = ElevatedIvoryCard,
                                                radius = 5.5.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = MatchaSageSuccess,
                                                radius = 4.dp.toPx(),
                                                center = pt
                                            )
                                        } else {
                                            drawCircle(
                                                color = ElevatedIvoryCard,
                                                radius = 3.5.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = MatchaSageSuccess,
                                                radius = 2.3.dp.toPx(),
                                                center = pt
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // X-Axis Hour Labels (clickable as well)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activeHourlySeries.forEachIndexed { idx, pt ->
                            val isSelected = idx == selectedHourIndex
                            Text(
                                text = pt.label.substringBefore(":"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RichEspressoPrimary else MutedCocoaText,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedHourIndex = idx }
                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Secondary Recharts-Style Interactive Trend Chart for Multi-day / Monthly / Annual / Custom Periods
 * Supports switching between "Pendapatan" and "Pesanan Masuk", plus Per Tanggal / Per Bulan for multi-month Custom ranges.
 */
@Composable
fun RechartsPeriodRevenueChartCard(
    title: String,
    subtitle: String,
    periodSeries: List<PeriodSalesPoint>,
    customMonthlySeries: List<PeriodSalesPoint>? = null,
    customRangeLabel: String = "",
    modifier: Modifier = Modifier
) {
    var metricMode by remember { mutableStateOf(TrendChartMetricMode.REVENUE) }
    val isOrderMode = metricMode == TrendChartMetricMode.ORDERS

    val hasMultiMonthCustomToggle = customMonthlySeries != null && customMonthlySeries.size >= 2
    var customGranularity by remember(periodSeries, customMonthlySeries) {
        mutableStateOf(
            if (hasMultiMonthCustomToggle && periodSeries.size > 45) {
                CustomChartGranularity.PER_MONTH
            } else {
                CustomChartGranularity.PER_DATE
            }
        )
    }

    val activePeriodSeries = remember(periodSeries, customMonthlySeries, hasMultiMonthCustomToggle, customGranularity) {
        if (hasMultiMonthCustomToggle && customGranularity == CustomChartGranularity.PER_MONTH) {
            customMonthlySeries ?: periodSeries
        } else {
            periodSeries
        }
    }

    var showGrossOverlay by remember { mutableStateOf(true) }

    val peakRevenueIdx = remember(activePeriodSeries) {
        if (activePeriodSeries.isEmpty()) 0
        else activePeriodSeries.indices.maxByOrNull { activePeriodSeries[it].netRevenue } ?: 0
    }
    val peakOrderIdx = remember(activePeriodSeries) {
        if (activePeriodSeries.isEmpty()) 0
        else activePeriodSeries.indices.maxByOrNull { activePeriodSeries[it].orderCount } ?: 0
    }
    val activePeakIdx = if (isOrderMode) peakOrderIdx else peakRevenueIdx

    var selectedIndex by remember(activePeriodSeries) { mutableIntStateOf(activePeakIdx) }

    val selectedPoint = activePeriodSeries.getOrElse(selectedIndex) {
        activePeriodSeries.firstOrNull() ?: PeriodSalesPoint(0, "-", "-", 0L, 0L, 0)
    }

    val maxChartValue = remember(activePeriodSeries) {
        (activePeriodSeries.maxOfOrNull { maxOf(it.grossRevenue, it.netRevenue) } ?: 100_000L)
            .coerceAtLeast(50_000L)
    }
    val maxOrderChartValue = remember(activePeriodSeries) {
        val rawMax = activePeriodSeries.maxOfOrNull { it.orderCount } ?: 0
        maxOf(3, ((rawMax + 2) / 3) * 3)
    }
    val totalPeriodOrders = remember(activePeriodSeries) {
        activePeriodSeries.sumOf { it.orderCount }
    }

    val cleanMainTitle = remember(title, hasMultiMonthCustomToggle, customGranularity) {
        if (hasMultiMonthCustomToggle) {
            if (customGranularity == CustomChartGranularity.PER_MONTH) {
                "Tren Pendapatan Custom per Bulan"
            } else {
                "Tren Pendapatan Custom per Tanggal"
            }
        } else {
            if (title.contains("(")) {
                title.substringBefore("(").trim()
            } else {
                title
            }
        }
    }

    val rangeSubLabel = remember(customRangeLabel, hasMultiMonthCustomToggle, title) {
        if (hasMultiMonthCustomToggle && customRangeLabel.isNotBlank()) {
            "($customRangeLabel)"
        } else if (title.contains("(") && title.contains(")")) {
            "(${title.substringAfter("(").substringBefore(")")})"
        } else {
            ""
        }
    }

    val displayedTitle = remember(cleanMainTitle, isOrderMode) {
        if (isOrderMode) {
            cleanMainTitle.replace("Pendapatan", "Pesanan Masuk")
        } else {
            cleanMainTitle
        }
    }
    val displayedSubtitle = remember(subtitle, isOrderMode, hasMultiMonthCustomToggle, customGranularity, customRangeLabel) {
        if (hasMultiMonthCustomToggle) {
            val unitLabel = if (customGranularity == CustomChartGranularity.PER_MONTH) "per bulan" else "per tanggal"
            if (isOrderMode) {
                "Jumlah pesanan masuk selesai $unitLabel sesuai rentang waktu ($customRangeLabel)"
            } else {
                "Omzet & uang masuk bersih $unitLabel sesuai rentang waktu ($customRangeLabel)"
            }
        } else if (isOrderMode) {
            "Jumlah pesanan masuk selesai per periode untuk memetakan hari/tanggal/bulan tersibuk"
        } else {
            subtitle
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_period_recharts_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Header + Peak Badge + 2-Button Mode Switcher & Legend Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = displayedTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = RichEspressoPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (rangeSubLabel.isNotBlank() || (activePeriodSeries.isNotEmpty() && activePeriodSeries.getOrNull(activePeakIdx) != null)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (rangeSubLabel.isNotBlank()) {
                                Text(
                                    text = rangeSubLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedCocoaText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (activePeriodSeries.isNotEmpty()) {
                                val peakPt = activePeriodSeries.getOrNull(activePeakIdx)
                                if (peakPt != null) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isOrderMode) SoftGoldHighlight else SoftSageBg,
                                        border = if (isOrderMode) BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.6f)) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = if (isOrderMode) DeepCaramelGold else MatchaSageSuccess,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = if (isOrderMode) {
                                                    "Peak: ${peakPt.label} (${peakPt.orderCount} Pesanan)"
                                                } else {
                                                    "Peak: ${peakPt.label}"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isOrderMode) RichEspressoPrimary else MatchaSageSuccess,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayedSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedCocoaText
                    )

                    if (hasMultiMonthCustomToggle) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomChartGranularity.entries.forEach { gran ->
                                val isSelectedGran = customGranularity == gran
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelectedGran) RichEspressoPrimary else SoftOatSurface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelectedGran) ChampagneGoldAccent else SubtleSandBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { customGranularity = gran }
                                        .testTag("custom_granularity_${gran.name.lowercase()}")
                                ) {
                                    Text(
                                        text = gran.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelectedGran) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelectedGran) SoftGoldHighlight else RichEspressoPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Controls: Mode Switcher on top, Legend pills neatly placed beneath it
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TrendMetricModeSwitcher(
                        selectedMode = metricMode,
                        onModeSelected = { newMode ->
                            metricMode = newMode
                            selectedIndex = if (newMode == TrendChartMetricMode.ORDERS) peakOrderIdx else peakRevenueIdx
                        },
                        testTagPrefix = "period_chart"
                    )

                    if (!isOrderMode) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendDotPill(
                                color = MatchaSageSuccess,
                                label = "Uang Masuk Bersih",
                                active = true,
                                onClick = {}
                            )
                            LegendDotPill(
                                color = ChampagneGoldAccent,
                                label = "Gross Sebelum Potongan",
                                active = showGrossOverlay,
                                onClick = { showGrossOverlay = !showGrossOverlay }
                            )
                        }
                    } else {
                        LegendDotPill(
                            color = DeepCaramelGold,
                            label = "Total: $totalPeriodOrders Pesanan",
                            active = true,
                            onClick = {}
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Tooltip Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SoftOatSurface.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = RichEspressoPrimary
                        ) {
                            Text(
                                text = selectedPoint.fullLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = SoftGoldHighlight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        if (isOrderMode) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftGoldHighlight,
                                border = BorderStroke(1.dp, ChampagneGoldAccent)
                            ) {
                                Text(
                                    text = "${selectedPoint.orderCount} Pesanan Masuk",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            if (totalPeriodOrders > 0) {
                                val pct = ((selectedPoint.orderCount * 100f) / totalPeriodOrders.toFloat()).roundToInt()
                                Text(
                                    text = "($pct% dari total periode)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MutedCocoaText
                                )
                            }
                        } else {
                            Text(
                                text = "${selectedPoint.orderCount} Pesanan Selesai",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = RichEspressoPrimary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Gross: ${WaffleoViewModel.formatIdr(selectedPoint.grossRevenue)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepCaramelGold
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftSageBg
                        ) {
                            Text(
                                text = "Net Masuk Sistem: ${WaffleoViewModel.formatIdr(selectedPoint.netRevenue)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MatchaSageSuccess,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Y-Axis Scale + Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(195.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 22.dp, end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    if (isOrderMode) {
                        Text(
                            text = "$maxOrderChartValue Pesanan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "${(maxOrderChartValue * 2) / 3} Pesanan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = "${maxOrderChartValue / 3} Pesanan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = "0 Pesanan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                    } else {
                        Text(
                            text = formatShortIdr(maxChartValue),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = formatShortIdr((maxChartValue * 0.66).toLong()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = formatShortIdr((maxChartValue * 0.33).toLong()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = "Rp 0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .pointerInput(activePeriodSeries) {
                                detectTapGestures { tapOffset ->
                                    if (activePeriodSeries.isNotEmpty() && size.width > 0) {
                                        val stepX = size.width.toFloat() / (activePeriodSeries.size - 1).coerceAtLeast(1)
                                        val tappedIdx = ((tapOffset.x + stepX / 2f) / stepX)
                                            .toInt()
                                            .coerceIn(0, activePeriodSeries.lastIndex)
                                        selectedIndex = tappedIdx
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val topPad = 12.dp.toPx()
                            val bottomPad = 8.dp.toPx()
                            val usableH = (h - topPad - bottomPad).coerceAtLeast(1f)
                            val count = activePeriodSeries.size
                            val stepX = if (count > 1) w / (count - 1).toFloat() else w

                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            for (i in 0..3) {
                                val y = topPad + usableH * (i / 3f)
                                drawLine(
                                    color = SubtleSandBorder.copy(alpha = 0.7f),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = if (i < 3) dashEffect else null
                                )
                            }

                            if (count > 1) {
                                if (isOrderMode) {
                                    val orderPoints = activePeriodSeries.mapIndexed { idx, pt ->
                                        val ratio = (pt.orderCount.toFloat() / maxOrderChartValue.toFloat()).coerceIn(0f, 1f)
                                        Offset(x = idx * stepX, y = topPad + usableH * (1f - ratio))
                                    }

                                    val orderSpline = buildSmoothBezierPath(orderPoints)
                                    val areaPath = Path().apply {
                                        addPath(orderSpline)
                                        lineTo(orderPoints.last().x, topPad + usableH)
                                        lineTo(orderPoints.first().x, topPad + usableH)
                                        close()
                                    }
                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                DeepCaramelGold.copy(alpha = 0.38f),
                                                ChampagneGoldAccent.copy(alpha = 0.14f),
                                                Color.Transparent
                                            ),
                                            startY = topPad,
                                            endY = topPad + usableH
                                        )
                                    )
                                    drawPath(
                                        path = orderSpline,
                                        color = DeepCaramelGold,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    val selOffset = orderPoints.getOrNull(selectedIndex)
                                    if (selOffset != null) {
                                        drawLine(
                                            color = RichEspressoPrimary.copy(alpha = 0.35f),
                                            start = Offset(selOffset.x, topPad),
                                            end = Offset(selOffset.x, topPad + usableH),
                                            strokeWidth = 1.5.dp.toPx(),
                                            pathEffect = dashEffect
                                        )
                                    }

                                    orderPoints.forEachIndexed { idx, pt ->
                                        val isSelected = idx == selectedIndex
                                        if (isSelected) {
                                            drawCircle(
                                                color = DeepCaramelGold.copy(alpha = 0.24f),
                                                radius = 10.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = ElevatedIvoryCard,
                                                radius = 5.5.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = DeepCaramelGold,
                                                radius = 4.dp.toPx(),
                                                center = pt
                                            )
                                        } else {
                                            drawCircle(
                                                color = ElevatedIvoryCard,
                                                radius = 3.5.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = DeepCaramelGold,
                                                radius = 2.3.dp.toPx(),
                                                center = pt
                                            )
                                        }
                                    }
                                } else {
                                    val netPoints = activePeriodSeries.mapIndexed { idx, pt ->
                                        val ratio = (pt.netRevenue.toFloat() / maxChartValue.toFloat()).coerceIn(0f, 1f)
                                        Offset(x = idx * stepX, y = topPad + usableH * (1f - ratio))
                                    }
                                    val grossPoints = activePeriodSeries.mapIndexed { idx, pt ->
                                        val ratio = (pt.grossRevenue.toFloat() / maxChartValue.toFloat()).coerceIn(0f, 1f)
                                        Offset(x = idx * stepX, y = topPad + usableH * (1f - ratio))
                                    }

                                    if (showGrossOverlay) {
                                        val grossSpline = buildSmoothBezierPath(grossPoints)
                                        drawPath(
                                            path = grossSpline,
                                            color = ChampagneGoldAccent.copy(alpha = 0.85f),
                                            style = Stroke(
                                                width = 2.2.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                                            )
                                        )
                                    }

                                    val netSpline = buildSmoothBezierPath(netPoints)
                                    val areaPath = Path().apply {
                                        addPath(netSpline)
                                        lineTo(netPoints.last().x, topPad + usableH)
                                        lineTo(netPoints.first().x, topPad + usableH)
                                        close()
                                    }
                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MatchaSageSuccess.copy(alpha = 0.34f),
                                                ChampagneGoldAccent.copy(alpha = 0.10f),
                                                Color.Transparent
                                            ),
                                            startY = topPad,
                                            endY = topPad + usableH
                                        )
                                    )
                                    drawPath(
                                        path = netSpline,
                                        color = MatchaSageSuccess,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    val selOffset = netPoints.getOrNull(selectedIndex)
                                    if (selOffset != null) {
                                        drawLine(
                                            color = RichEspressoPrimary.copy(alpha = 0.35f),
                                            start = Offset(selOffset.x, topPad),
                                            end = Offset(selOffset.x, topPad + usableH),
                                            strokeWidth = 1.5.dp.toPx(),
                                            pathEffect = dashEffect
                                        )
                                    }

                                    netPoints.forEachIndexed { idx, pt ->
                                        val isSelected = idx == selectedIndex
                                        if (isSelected) {
                                            drawCircle(
                                                color = MatchaSageSuccess.copy(alpha = 0.22f),
                                                radius = 10.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = ElevatedIvoryCard,
                                                radius = 5.5.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = MatchaSageSuccess,
                                                radius = 4.dp.toPx(),
                                                center = pt
                                            )
                                        } else {
                                            drawCircle(
                                                color = ElevatedIvoryCard,
                                                radius = 3.5.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = MatchaSageSuccess,
                                                radius = 2.3.dp.toPx(),
                                                center = pt
                                            )
                                        }
                                    }
                                }
                            } else if (count == 1) {
                                // Single point rendering when range has 1 bucket
                                val pt = activePeriodSeries.first()
                                val ratio = if (isOrderMode) {
                                    (pt.orderCount.toFloat() / maxOrderChartValue.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    (pt.netRevenue.toFloat() / maxChartValue.toFloat()).coerceIn(0f, 1f)
                                }
                                val centerOffset = Offset(w / 2f, topPad + usableH * (1f - ratio))
                                val nodeColor = if (isOrderMode) DeepCaramelGold else MatchaSageSuccess
                                drawLine(
                                    color = RichEspressoPrimary.copy(alpha = 0.35f),
                                    start = Offset(centerOffset.x, topPad),
                                    end = Offset(centerOffset.x, topPad + usableH),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = dashEffect
                                )
                                drawCircle(color = nodeColor.copy(alpha = 0.24f), radius = 10.dp.toPx(), center = centerOffset)
                                drawCircle(color = ElevatedIvoryCard, radius = 5.5.dp.toPx(), center = centerOffset)
                                drawCircle(color = nodeColor, radius = 4.dp.toPx(), center = centerOffset)
                            }
                        }
                    }

                    // X-Axis Period Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp),
                        horizontalArrangement = if (activePeriodSeries.size == 1) Arrangement.Center else Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val count = activePeriodSeries.size
                        val stepLabel = when {
                            count > 25 -> 5
                            count > 16 -> 3
                            count > 12 -> 2
                            else -> 1
                        }
                        activePeriodSeries.forEachIndexed { idx, pt ->
                            val isSelected = idx == selectedIndex
                            val showText = idx % stepLabel == 0 || idx == activePeriodSeries.lastIndex || isSelected
                            val labelText = if (showText) pt.label else "•"
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RichEspressoPrimary else if (showText) MutedCocoaText else MutedCocoaText.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedIndex = idx }
                                    .padding(horizontal = 2.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildSmoothBezierPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    if (points.size == 1) {
        path.moveTo(points[0].x, points[0].y)
        return path
    }
    if (points.size == 2) {
        path.moveTo(points[0].x, points[0].y)
        path.lineTo(points[1].x, points[1].y)
        return path
    }

    path.moveTo(points.first().x, points.first().y)
    for (i in 0 until points.size - 1) {
        val p0 = if (i > 0) points[i - 1] else points[i]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = if (i + 2 < points.size) points[i + 2] else p2

        val dx = (p2.x - p1.x)
        val controlX1 = p1.x + dx * 0.36f
        val controlY1 = p1.y + (p2.y - p0.y) * 0.18f
        val controlX2 = p2.x - dx * 0.36f
        val controlY2 = p2.y - (p3.y - p1.y) * 0.18f

        path.cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
    }
    return path
}

@Composable
private fun LegendDotPill(
    color: Color,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) SoftOatSurface else SoftOatSurface.copy(alpha = 0.4f),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
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
                    .background(if (active) color else MutedCocoaText.copy(alpha = 0.4f))
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) RichEspressoPrimary else MutedCocoaText,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

/**
 * Interactive Multi-Segment Donut Chart for Sales Channel & Payment Distribution
 * (Cash, QRIS, GrabFood, GoFood, ShopeeFood)
 */
@Composable
fun MediaDistributionDonutCard(
    doneOrders: List<OrderRecord>,
    catalog: List<CatalogRecord>,
    viewModel: WaffleoViewModel,
    periodLabel: String = "Hari Ini",
    branchName: String = "Global",
    modifier: Modifier = Modifier
) {
    var selectedSliceForDialog by remember { mutableStateOf<MediaSliceStat?>(null) }
    val slices = remember(doneOrders, catalog) {
        // 1. GrabFood
        val grabOrders = doneOrders.filter { it.orderMedia.equals("GrabFood", ignoreCase = true) }
        val grabGross = grabOrders.sumOf { it.totalAmount }
        val grabNet = grabOrders.sumOf { viewModel.calculateOrderNetAmount(it, catalog) }
        val grabFee = (grabGross - grabNet).coerceAtLeast(0L)

        // 2. GoFood
        val gofoodOrders = doneOrders.filter { it.orderMedia.equals("GoFood", ignoreCase = true) }
        val gofoodGross = gofoodOrders.sumOf { it.totalAmount }
        val gofoodNet = gofoodOrders.sumOf { viewModel.calculateOrderNetAmount(it, catalog) }
        val gofoodFee = (gofoodGross - gofoodNet).coerceAtLeast(0L)

        // 3. ShopeeFood
        val shopeeOrders = doneOrders.filter { it.orderMedia.equals("ShopeeFood", ignoreCase = true) }
        val shopeeGross = shopeeOrders.sumOf { it.totalAmount }
        val shopeeNet = shopeeOrders.sumOf { viewModel.calculateOrderNetAmount(it, catalog) }
        val shopeeFee = (shopeeGross - shopeeNet).coerceAtLeast(0L)

        // Direct orders (Walk In, WhatsApp, Instagram, etc.)
        val directOrders = doneOrders.filter { it.orderMedia !in listOf("GrabFood", "GoFood", "ShopeeFood") }

        var cashGross = 0L
        var cashNet = 0L
        var cashCount = 0

        var qrisGross = 0L
        var qrisNet = 0L
        var qrisCount = 0

        for (ord in directOrders) {
            val net = viewModel.calculateOrderNetAmount(ord, catalog)
            val gross = ord.totalAmount
            val method = ord.paymentMethod
            val isSplit = method.contains("Cash + QRIS", ignoreCase = true) ||
                (ord.splitQrisAmount > 0L && ord.splitCashAmount > 0L)

            if (isSplit) {
                val cashPart = if (ord.splitCashAmount > 0L) ord.splitCashAmount.coerceAtMost(net) else (net - (ord.splitQrisAmount.coerceAtMost(net)))
                val qrisPart = (net - cashPart).coerceAtLeast(0L)
                if (cashPart > 0L) {
                    cashGross += ord.splitCashAmount.takeIf { it > 0L } ?: cashPart
                    cashNet += cashPart
                    cashCount++
                }
                if (qrisPart > 0L) {
                    qrisGross += ord.splitQrisAmount.takeIf { it > 0L } ?: qrisPart
                    qrisNet += qrisPart
                    qrisCount++
                }
            } else if (method.contains("QRIS", ignoreCase = true) || method.contains("Transfer", ignoreCase = true) || method.contains("Mandiri", ignoreCase = true) || method.contains("BCA", ignoreCase = true)) {
                qrisGross += gross
                qrisNet += net
                qrisCount++
            } else {
                // Default is Cash
                cashGross += gross
                cashNet += net
                cashCount++
            }
        }

        listOf(
            MediaSliceStat(
                mediaName = "Cash",
                orderCount = cashCount,
                grossRevenue = cashGross,
                netRevenue = cashNet,
                feeCut = 0L,
                color = Color(0xFF15803D), // Emerald Green
                isPlatform = false,
                subtitle = "Penerimaan Kasir Tunai",
                pillBg = Color(0xFFDCFCE7),
                pillText = Color(0xFF166534),
                icon = Icons.Default.Payments
            ),
            MediaSliceStat(
                mediaName = "QRIS",
                orderCount = qrisCount,
                grossRevenue = qrisGross,
                netRevenue = qrisNet,
                feeCut = 0L,
                color = Color(0xFF1D4ED8), // Royal Blue
                isPlatform = false,
                subtitle = "Pencairan Bank Mandiri & BCA",
                pillBg = Color(0xFFDBEAFE),
                pillText = Color(0xFF1E40AF),
                icon = Icons.Default.QrCode2
            ),
            MediaSliceStat(
                mediaName = "GrabFood",
                orderCount = grabOrders.size,
                grossRevenue = grabGross,
                netRevenue = grabNet,
                feeCut = grabFee,
                color = Color(0xFF00B14F), // Grab Green
                isPlatform = true,
                subtitle = if (grabOrders.isNotEmpty()) "Gross ${formatShortIdr(grabGross)} • Komisi -${formatShortIdr(grabFee)}" else "Platform Online • BCA",
                pillBg = Color(0xFFD1FAE5),
                pillText = Color(0xFF065F46),
                icon = Icons.Default.DeliveryDining
            ),
            MediaSliceStat(
                mediaName = "GoFood",
                orderCount = gofoodOrders.size,
                grossRevenue = gofoodGross,
                netRevenue = gofoodNet,
                feeCut = gofoodFee,
                color = Color(0xFFEE2737), // GoFood Red
                isPlatform = true,
                subtitle = if (gofoodOrders.isNotEmpty()) "Gross ${formatShortIdr(gofoodGross)} • Komisi -${formatShortIdr(gofoodFee)}" else "Platform Online • BCA",
                pillBg = Color(0xFFFFE4E6),
                pillText = Color(0xFF9F1239),
                icon = Icons.Default.DeliveryDining
            ),
            MediaSliceStat(
                mediaName = "ShopeeFood",
                orderCount = shopeeOrders.size,
                grossRevenue = shopeeGross,
                netRevenue = shopeeNet,
                feeCut = shopeeFee,
                color = Color(0xFFEE4D2D), // Shopee Orange
                isPlatform = true,
                subtitle = if (shopeeOrders.isNotEmpty()) "Gross ${formatShortIdr(shopeeGross)} • Komisi -${formatShortIdr(shopeeFee)}" else "Platform Online • BCA",
                pillBg = Color(0xFFFFEDD5),
                pillText = Color(0xFF9A3412),
                icon = Icons.Default.LocalMall
            )
        )
    }

    val totalNetAll = remember(slices) { slices.sumOf { it.netRevenue } }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.75f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Cinematic Header Row with Gradient Badge + Net Summary Pill
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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(RichEspressoPrimary, RoastedMochaSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = ChampagneGoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Distribusi Kanal & Metode Pembayaran",
                            style = MaterialTheme.typography.titleLarge,
                            color = RichEspressoPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Proporsi pendapatan bersih per kanal (Cash, QRIS & Platform Online)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SoftGoldHighlight,
                    border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Total Net:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                        Text(
                            text = WaffleoViewModel.formatIdr(totalNetAll),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepCaramelGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cinematic Multi-Ring Donut Canvas with Soft Ambient Halo
                Box(
                    modifier = Modifier.size(148.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 20.dp.toPx()
                        val outerDecoStroke = 2.dp.toPx()
                        val diameter = size.minDimension - strokeWidth - 8.dp.toPx()
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        val arcSize = Size(diameter, diameter)

                        // Soft radial ambient center glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    SoftGoldHighlight.copy(alpha = 0.55f),
                                    SoftOatSurface.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            radius = diameter * 0.44f,
                            center = center
                        )

                        // Subtle outer decorative ring
                        drawCircle(
                            color = SubtleSandBorder.copy(alpha = 0.55f),
                            radius = (size.minDimension - outerDecoStroke) / 2f,
                            center = center,
                            style = Stroke(width = outerDecoStroke)
                        )

                        // Base track ring
                        drawArc(
                            color = SoftOatSurface,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        if (totalNetAll > 0L) {
                            var startAngle = -90f
                            val nonZeroSlices = slices.filter { it.netRevenue > 0L }
                            val gapDegrees = if (nonZeroSlices.size > 1) 4f else 0f
                            nonZeroSlices.forEach { slice ->
                                val sweep = (slice.netRevenue.toFloat() / totalNetAll.toFloat()) * 360f
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle + (gapDegrees / 2f),
                                    sweepAngle = (sweep - gapDegrees).coerceAtLeast(5f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                startAngle += sweep
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${doneOrders.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "Order Selesai",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedCocoaText
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (doneOrders.isNotEmpty()) SoftSageBg else SoftOatSurface
                        ) {
                            Text(
                                text = if (doneOrders.isNotEmpty()) "100% Lunas" else "0 Transaksi",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (doneOrders.isNotEmpty()) MatchaSageSuccess else MutedCocoaText,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Detailed Breakdown Cards next to Donut (Cash, QRIS, GrabFood, GoFood, ShopeeFood)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slices.forEach { slice ->
                        val sharePct = if (totalNetAll > 0L) {
                            ((slice.netRevenue.toDouble() / totalNetAll.toDouble()) * 100.0).roundToInt()
                        } else 0
                        val shareFraction = if (totalNetAll > 0L) {
                            (slice.netRevenue.toFloat() / totalNetAll.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SoftOatSurface.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (slice.orderCount > 0) slice.color.copy(alpha = 0.28f) else SubtleSandBorder.copy(alpha = 0.65f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedSliceForDialog = slice }
                                .testTag("dashboard_channel_card_${slice.mediaName.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 11.dp, vertical = 8.dp)
                            ) {
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
                                            shape = RoundedCornerShape(10.dp),
                                            color = slice.pillBg,
                                            border = BorderStroke(1.dp, slice.color.copy(alpha = 0.25f)),
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = slice.icon,
                                                    contentDescription = slice.mediaName,
                                                    tint = slice.color,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            // Line 1: Channel Name in bold
                                            Text(
                                                text = slice.mediaName,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = RichEspressoPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            // Line 2: ( 0x • 0% ) badge + Subtitle underneath, spacious and never vertically wrapped!
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = ElevatedIvoryCard,
                                                    border = BorderStroke(0.8.dp, if (slice.orderCount > 0) slice.color.copy(alpha = 0.4f) else SubtleSandBorder)
                                                ) {
                                                    Text(
                                                        text = "${slice.orderCount}x • $sharePct%",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (slice.orderCount > 0) slice.color else MutedCocoaText,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                        maxLines = 1
                                                    )
                                                }
                                                Text(
                                                    text = slice.subtitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MutedCocoaText,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = slice.pillBg,
                                        border = BorderStroke(1.dp, slice.color.copy(alpha = 0.22f))
                                    ) {
                                        Text(
                                            text = if (slice.isPlatform) {
                                                "BCA: ${WaffleoViewModel.formatIdr(slice.netRevenue)}"
                                            } else {
                                                WaffleoViewModel.formatIdr(slice.netRevenue)
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = slice.pillText,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (totalNetAll > 0L) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(ElevatedIvoryCard)
                                    ) {
                                        if (shareFraction > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(shareFraction.coerceAtLeast(0.03f))
                                                    .clip(RoundedCornerShape(50))
                                                    .background(slice.color)
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

    // Modal Popup: Daftar Transaksi Kanal Pembayaran (Interactive Clickable Channel Popup)
    selectedSliceForDialog?.let { slice ->
        val filteredOrdersForChannel = remember(slice, doneOrders) {
            when (slice.mediaName) {
                "GrabFood" -> doneOrders.filter { it.orderMedia.equals("GrabFood", ignoreCase = true) }
                "GoFood" -> doneOrders.filter { it.orderMedia.equals("GoFood", ignoreCase = true) }
                "ShopeeFood" -> doneOrders.filter { it.orderMedia.equals("ShopeeFood", ignoreCase = true) }
                "QRIS" -> doneOrders.filter { ord ->
                    if (ord.orderMedia in listOf("GrabFood", "GoFood", "ShopeeFood")) {
                        false
                    } else {
                        val isSplit = ord.paymentMethod.contains("Cash + QRIS", ignoreCase = true) ||
                            (ord.splitCashAmount > 0L && ord.splitQrisAmount > 0L)
                        (isSplit && ord.splitQrisAmount > 0L) ||
                            (!isSplit && (ord.paymentMethod.contains("QRIS", ignoreCase = true) || ord.paymentMethod.contains("Transfer", ignoreCase = true) || ord.paymentMethod.contains("BCA", ignoreCase = true) || ord.paymentMethod.contains("Mandiri", ignoreCase = true)))
                    }
                }
                else -> doneOrders.filter { ord -> // Cash
                    if (ord.orderMedia in listOf("GrabFood", "GoFood", "ShopeeFood")) {
                        false
                    } else {
                        val isSplit = ord.paymentMethod.contains("Cash + QRIS", ignoreCase = true) ||
                            (ord.splitCashAmount > 0L && ord.splitQrisAmount > 0L)
                        (isSplit && ord.splitCashAmount > 0L) ||
                            (!isSplit && !ord.paymentMethod.contains("QRIS", ignoreCase = true) && !ord.paymentMethod.contains("Transfer", ignoreCase = true) && !ord.paymentMethod.contains("BCA", ignoreCase = true) && !ord.paymentMethod.contains("Mandiri", ignoreCase = true))
                    }
                }
            }
        }

        DashboardChannelTransactionsDialog(
            channelName = slice.mediaName,
            periodLabel = periodLabel,
            branchName = branchName,
            orders = filteredOrdersForChannel,
            catalog = catalog,
            viewModel = viewModel,
            totalDashboardNetRevenue = totalNetAll,
            onDismiss = { selectedSliceForDialog = null }
        )
    }
}

/**
 * Menu Terlaris Card with Dynamic Category Tabs (adapting live to Menu & Modifier categories/items:
 * "Semua | Egg Waffle (..) | Minuman (..) | Snacks (..) | dst") and a cinematic leaderboard.
 */
@Composable
fun TopMenuPerformanceBarCard(
    doneOrders: List<OrderRecord>,
    catalog: List<CatalogRecord>,
    customMenuCategories: List<String> = emptyList(),
    customModifierCategories: List<String> = emptyList(),
    modifierToMenuCategoryMap: Map<String, String> = emptyMap(),
    periodLabel: String = "",
    branchName: String = "",
    modifier: Modifier = Modifier
) {
    var selectedProductForAnalysis by remember { mutableStateOf<TopProductStat?>(null) }

    // 1. Dynamically resolve active Menu Categories in their configured order
    val activeMenuCategories = remember(catalog, customMenuCategories) {
        val existingMenuRecords = catalog.filter { it.recordType == "MENU_ITEM" }
        val fromCatalog = existingMenuRecords.map { it.category }.filter { it.isNotBlank() }
        // Only keep categories that exist in customMenuCategories or have items in catalog
        val merged = if (customMenuCategories.isNotEmpty()) {
            customMenuCategories + fromCatalog
        } else {
            listOf("Egg Waffle", "Drinks") + fromCatalog
        }
        merged
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { normalizeCategoryCanonical(it) }
    }

    // 2. Dynamically resolve existing Modifier Categories & Modifier items in catalog
    val activeModifierRecords = remember(catalog) {
        catalog.filter { it.recordType == "MODIFIER" }
    }
    val activeModifierCategoryNames = remember(activeModifierRecords, customModifierCategories) {
        val fromMods = activeModifierRecords.map { it.category }.filter { it.isNotBlank() }
        (customModifierCategories + fromMods)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { normalizeCategoryCanonical(it) }
    }

    var selectedSubTab by remember { mutableStateOf("ALL") }

    // Ensure selectedSubTab resets to "ALL" if its category was deleted/renamed
    val effectiveSelectedTab = remember(selectedSubTab, activeMenuCategories) {
        if (selectedSubTab == "ALL" || activeMenuCategories.any { areCategoriesMatching(it, selectedSubTab) }) {
            selectedSubTab
        } else {
            "ALL"
        }
    }

    // 3. Synchronize Menu Items, Modifier Bindings, and Sold Quantities
    val (allMenuStats, categorySoldQtyMap) = remember(
        doneOrders,
        catalog,
        activeMenuCategories,
        activeModifierRecords,
        activeModifierCategoryNames,
        modifierToMenuCategoryMap
    ) {
        val menuRecords = catalog
            .filter { it.recordType == "MENU_ITEM" }
            .sortedWith(
                compareBy<CatalogRecord> { rec ->
                    val cIdx = activeMenuCategories.indexOfFirst { areCategoriesMatching(it, rec.category) }
                    if (cIdx >= 0) cIdx else 999
                }
                    .thenBy { if (it.sortOrder < 0) 999999 else it.sortOrder }
                    .thenBy { it.name.lowercase() }
            )

        val validModifierNames = activeModifierRecords.map { it.name.lowercase() }.toSet()

        // Helper to resolve linked modifier summary for a catalog menu item
        fun resolveModifierBadgeForMenu(menuRec: CatalogRecord, soldModifierCounts: Map<String, Int>): String {
            // Check if item has explicit allowedModifiers or category-level modifierToMenuCategoryMap
            val explicitAllowed = menuRec.getAllowedModifiers()
            val linkedModCategories = mutableListOf<Pair<String, List<String>>>()

            if (explicitAllowed.isNotEmpty()) {
                explicitAllowed.forEach { (modCat, allowedItemNames) ->
                    val catStillExists = activeModifierCategoryNames.any { areCategoriesMatching(it, modCat) }
                    if (catStillExists) {
                        val existingModItems = activeModifierRecords.filter { modRec ->
                            areCategoriesMatching(modRec.category, modCat) &&
                                (allowedItemNames.isEmpty() || allowedItemNames.any { it.equals(modRec.name, ignoreCase = true) })
                        }.map { it.name }
                        if (existingModItems.isNotEmpty()) {
                            linkedModCategories.add(modCat to existingModItems)
                        }
                    }
                }
            }

            // Also check modifierToMenuCategoryMap (e.g. "Rasa" -> "Snacks")
            modifierToMenuCategoryMap.forEach { (modCat, targetMenuCat) ->
                if (areCategoriesMatching(targetMenuCat, menuRec.category) &&
                    activeModifierCategoryNames.any { areCategoriesMatching(it, modCat) } &&
                    linkedModCategories.none { areCategoriesMatching(it.first, modCat) }
                ) {
                    val existingModItems = activeModifierRecords
                        .filter { areCategoriesMatching(it.category, modCat) }
                        .map { it.name }
                    linkedModCategories.add(modCat to existingModItems)
                }
            }

            if (soldModifierCounts.isNotEmpty()) {
                val topSoldMods = soldModifierCounts.entries
                    .sortedByDescending { it.value }
                    .take(2)
                    .joinToString(", ") { "${it.key} (${it.value}x)" }
                if (topSoldMods.isNotBlank()) return "Modifier: $topSoldMods"
            }

            if (linkedModCategories.isEmpty()) return ""
            return linkedModCategories.joinToString(" • ") { (modCat, items) ->
                if (items.isEmpty()) {
                    "Modifier: $modCat"
                } else {
                    val preview = items.take(3).joinToString(", ") + if (items.size > 3) " +${items.size - 3}" else ""
                    "$modCat: $preview"
                }
            }
        }

        // Track sales per menu item ID and per category
        val qtyByMenuId = mutableMapOf<String, Int>()
        val revByMenuId = mutableMapOf<String, Long>()
        val modsByMenuId = mutableMapOf<String, MutableMap<String, Int>>()
        val catSoldTotals = LinkedHashMap<String, Int>()
        activeMenuCategories.forEach { catSoldTotals[it] = 0 }

        doneOrders.forEach { order ->
            val media = com.example.data.model.OrderMedia.fromLabel(order.orderMedia)
            val parsed = order.parsedLineItems
            parsed.forEach { line ->
                // Match against currently existing MENU_ITEM in catalog (by ID first, then by name)
                val matchedMenu = menuRecords.firstOrNull { it.id == line.primaryItemId }
                    ?: menuRecords.firstOrNull { it.name.equals(line.primaryItemName, ignoreCase = true) }
                val matchedSecondaryMenu = if ((line.isHalfHalf || line.isDoubleTopping) &&
                    (line.secondaryItemId.isNotBlank() || line.secondaryItemName.isNotBlank())
                ) {
                    menuRecords.firstOrNull { line.secondaryItemId.isNotBlank() && it.id == line.secondaryItemId }
                        ?: menuRecords.firstOrNull { it.name.equals(line.secondaryItemName, ignoreCase = true) }
                } else null

                // If the menu item or its category was deleted from Menu settings, skip it
                if (matchedMenu != null && activeMenuCategories.any { areCategoriesMatching(it, matchedMenu.category) }) {
                    val menuId = matchedMenu.id
                    val lineRev = line.lineTotalFor(media)
                    val primaryRev = if (matchedSecondaryMenu != null && matchedSecondaryMenu.id != menuId) lineRev / 2 else lineRev
                    qtyByMenuId[menuId] = (qtyByMenuId[menuId] ?: 0) + line.qty
                    revByMenuId[menuId] = (revByMenuId[menuId] ?: 0L) + primaryRev

                    val canonicalCat = activeMenuCategories.firstOrNull { areCategoriesMatching(it, matchedMenu.category) }
                        ?: matchedMenu.category
                    catSoldTotals[canonicalCat] = (catSoldTotals[canonicalCat] ?: 0) + line.qty

                    // Record active modifiers used on this sold line (filtering out deleted modifiers)
                    val modCountMap = modsByMenuId.getOrPut(menuId) { mutableMapOf() }
                    if (areCategoriesMatching(matchedMenu.category, "Egg Waffle") && line.flavourName.isNotBlank()) {
                        if (line.flavourName.equals("Original", ignoreCase = true) || line.flavourName.lowercase() in validModifierNames) {
                            modCountMap[line.flavourName] = (modCountMap[line.flavourName] ?: 0) + line.qty
                        }
                    }
                    line.addOnNames.forEach { addOn ->
                        if (addOn.lowercase() in validModifierNames) {
                            modCountMap[addOn] = (modCountMap[addOn] ?: 0) + line.qty
                        }
                    }
                }

                if (matchedSecondaryMenu != null &&
                    matchedSecondaryMenu.id != matchedMenu?.id &&
                    activeMenuCategories.any { areCategoriesMatching(it, matchedSecondaryMenu.category) }
                ) {
                    val secId = matchedSecondaryMenu.id
                    val lineRev = line.lineTotalFor(media)
                    val secRev = lineRev - (lineRev / 2)
                    qtyByMenuId[secId] = (qtyByMenuId[secId] ?: 0) + line.qty
                    revByMenuId[secId] = (revByMenuId[secId] ?: 0L) + secRev
                }
            }
        }

        // Build TopProductStat list for all existing menu items in activeMenuCategories:
        // Sold items ranked highest first, followed by unsold items in their natural category & sortOrder sequence.
        val stats = menuRecords
            .filter { rec -> activeMenuCategories.any { areCategoriesMatching(it, rec.category) } }
            .map { rec ->
                val canonicalCat = activeMenuCategories.firstOrNull { areCategoriesMatching(it, rec.category) } ?: rec.category
                val soldQty = qtyByMenuId[rec.id] ?: 0
                val soldRev = revByMenuId[rec.id] ?: 0L
                val soldMods = modsByMenuId[rec.id] ?: emptyMap()
                TopProductStat(
                    id = rec.id,
                    name = rec.name,
                    category = canonicalCat,
                    subCategory = rec.subCategory,
                    modifierBadgeText = resolveModifierBadgeForMenu(rec, soldMods),
                    qtySold = soldQty,
                    grossRevenue = soldRev
                )
            }
            .sortedWith(
                compareByDescending<TopProductStat> { it.qtySold > 0 }
                    .thenByDescending { it.qtySold }
                    .thenByDescending { it.grossRevenue }
            )

        stats to catSoldTotals
    }

    val filteredItems = remember(allMenuStats, effectiveSelectedTab) {
        if (effectiveSelectedTab == "ALL") {
            allMenuStats
        } else {
            allMenuStats.filter { areCategoriesMatching(it.category, effectiveSelectedTab) }
        }
    }

    val maxQty = (filteredItems.maxOfOrNull { it.qtySold } ?: 1).coerceAtLeast(1)
    val totalCategoryRevenue = remember(filteredItems) { filteredItems.sumOf { it.grossRevenue } }

    // Dynamic Tabs: Semua (total pcs sold) | Egg Waffle (pcs sold) | Minuman (pcs sold) | Snacks (pcs sold) | dst
    val subTabs = remember(allMenuStats, activeMenuCategories, categorySoldQtyMap) {
        val totalPcsSold = categorySoldQtyMap.values.sum()
        val tabs = mutableListOf<Pair<String, String>>()
        tabs.add("ALL" to "Semua ($totalPcsSold)")
        activeMenuCategories.forEach { cat ->
            val pcsSoldInCat = categorySoldQtyMap[cat] ?: 0
            val displayLabel = if (cat.equals("Drinks", ignoreCase = true)) "Minuman" else cat
            tabs.add(cat to "$displayLabel ($pcsSoldInCat)")
        }
        tabs
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
        border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.75f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Cinematic Header Row with Trophy Badge
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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(DeepCaramelGold, ChampagneGoldAccent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = ElevatedIvoryCard,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Menu Terlaris",
                            style = MaterialTheme.typography.titleLarge,
                            color = RichEspressoPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        val summaryLine = remember(categorySoldQtyMap) {
                            categorySoldQtyMap.entries.joinToString(" • ") { (cat, qty) ->
                                val unit = if (cat.equals("Drinks", ignoreCase = true) || cat.equals("Minuman", ignoreCase = true)) "cup" else "pcs"
                                val label = if (cat.equals("Drinks", ignoreCase = true)) "Minuman" else cat
                                "$label: $qty $unit"
                            }
                        }
                        Text(
                            text = summaryLine.ifBlank { "Peringkat menu & keterikatan modifier aktif" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedCocoaText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (totalCategoryRevenue > 0L) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftSageBg,
                        border = BorderStroke(1.dp, MatchaSageSuccess.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = WaffleoViewModel.formatIdr(totalCategoryRevenue),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MatchaSageSuccess,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Scrollable Sub-Category Filter Tabs: Semua | Egg Waffle (..) | Minuman (..) | Snacks (..) | dst
            Surface(
                shape = RoundedCornerShape(50),
                color = SoftOatSurface,
                border = BorderStroke(1.dp, SubtleSandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subTabs.forEach { (key, label) ->
                        val isSelected = if (key == "ALL") {
                            effectiveSelectedTab == "ALL"
                        } else {
                            areCategoriesMatching(effectiveSelectedTab, key)
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) RichEspressoPrimary else Color.Transparent,
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedSubTab = key }
                                .testTag("dashboard_top_menu_tab_${key.lowercase().replace(" ", "_")}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 14.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isSelected) ChampagneGoldAccent else RichEspressoPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val isolateScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset = available

                    override suspend fun onPostFling(
                        consumed: Velocity,
                        available: Velocity
                    ): Velocity = available
                }
            }

            // Scrollable Leaderboard Container
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SoftOatSurface.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.75f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(268.dp)
                    .nestedScroll(isolateScrollConnection)
            ) {
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = MutedCocoaText.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Belum ada item menu aktif pada kategori ini.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MutedCocoaText
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(isolateScrollConnection)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredItems.forEachIndexed { idx, prod ->
                            val rank = idx + 1
                            val hasSales = prod.qtySold > 0
                            val fillFraction = if (hasSales) {
                                (prod.qtySold.toFloat() / maxQty.toFloat()).coerceIn(0.08f, 1f)
                            } else {
                                0f
                            }
                            val catCanonical = normalizeCategoryCanonical(prod.category)
                            val accentColor = when {
                                catCanonical.contains("waffle") -> DeepCaramelGold
                                catCanonical.contains("drink") || catCanonical.contains("minum") -> MatchaSageSuccess
                                else -> Color(0xFFB45309) // Warm Cinnamon Amber for Snacks & custom categories
                            }
                            val barGradient = when {
                                catCanonical.contains("waffle") -> Brush.horizontalGradient(listOf(DeepCaramelGold, ChampagneGoldAccent))
                                catCanonical.contains("drink") || catCanonical.contains("minum") -> Brush.horizontalGradient(listOf(MatchaSageSuccess, Color(0xFF34D399)))
                                else -> Brush.horizontalGradient(listOf(Color(0xFFB45309), Color(0xFFF59E0B)))
                            }

                            val rankBadgeBg = when {
                                rank == 1 && hasSales -> Color(0xFFFEF3C7)
                                rank == 2 && hasSales -> Color(0xFFF1F5F9)
                                rank == 3 && hasSales -> Color(0xFFFFEDD5)
                                else -> ElevatedIvoryCard
                            }
                            val rankBadgeFg = when {
                                rank == 1 && hasSales -> Color(0xFFB45309)
                                rank == 2 && hasSales -> Color(0xFF475569)
                                rank == 3 && hasSales -> Color(0xFF9A3412)
                                else -> RichEspressoPrimary
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ElevatedIvoryCard,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (rank == 1 && hasSales) ChampagneGoldAccent.copy(alpha = 0.65f) else SubtleSandBorder.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        selectedProductForAnalysis = prod
                                    }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp)
                                ) {
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
                                            // Rank Badge (#1, #2, #3...)
                                            Surface(
                                                shape = RoundedCornerShape(9.dp),
                                                color = rankBadgeBg,
                                                border = BorderStroke(1.dp, rankBadgeFg.copy(alpha = 0.22f)),
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "#$rank",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = rankBadgeFg
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = prod.name,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = RichEspressoPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    val displayCatTag = if (prod.category.equals("Drinks", ignoreCase = true)) "Minuman" else prod.category
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = SoftOatSurface
                                                    ) {
                                                        Text(
                                                            text = if (prod.subCategory.isNotBlank()) "$displayCatTag • ${prod.subCategory}" else displayCatTag,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MutedCocoaText,
                                                            maxLines = 1,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }

                                                if (prod.modifierBadgeText.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Tune,
                                                            contentDescription = null,
                                                            tint = accentColor,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                        Text(
                                                            text = prod.modifierBadgeText,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Medium,
                                                            color = accentColor,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Right-side Sales & Revenue Pill + Analysis Icon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = if (hasSales) accentColor.copy(alpha = 0.12f) else SoftOatSurface
                                                ) {
                                                    Text(
                                                        text = "${prod.qtySold} terjual",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (hasSales) accentColor else MutedCocoaText,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                if (hasSales) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = WaffleoViewModel.formatIdr(prod.grossRevenue),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = RichEspressoPrimary
                                                    )
                                                }
                                            }

                                            Icon(
                                                imageVector = Icons.Default.Analytics,
                                                contentDescription = "Analisis Variasi",
                                                tint = accentColor.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Progress Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(SoftOatSurface)
                                    ) {
                                        if (fillFraction > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(fillFraction)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(barGradient)
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

    // Modal Popup: Analisis Variasi Pesanan Produk Induk
    selectedProductForAnalysis?.let { prod ->
        DashboardProductVariationAnalysisDialog(
            productName = prod.name,
            categoryName = prod.category,
            subCategoryName = prod.subCategory,
            periodLabel = periodLabel,
            branchName = branchName,
            doneOrders = doneOrders,
            catalog = catalog,
            onDismiss = { selectedProductForAnalysis = null }
        )
    }
}

private fun formatShortIdr(amount: Long): String {
    return when {
        amount >= 1_000_000L -> String.format("Rp %.1fJt", amount / 1_000_000.0)
        amount >= 1_000L -> "Rp ${amount / 1_000L}rb"
        else -> "Rp $amount"
    }
}
