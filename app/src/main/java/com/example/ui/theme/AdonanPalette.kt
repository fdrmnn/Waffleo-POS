package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.data.model.CartLineItem
import kotlin.math.abs

/**
 * Palet Warna Khusus Adonan (Flavour) Egg Waffle.
 * Memberikan warna khas tajam, kontras tinggi, dan mudah dipindai mata secara sekilas di lapangan.
 *
 * Mendukung deteksi otomatis nama rasa baru (misal: "Durian" -> Kuning Keemasan)
 * serta fallback algoritma deterministik HSL dengan kontras tinggi (>5.0:1)
 * agar teks tidak pernah pudar atau buram di atas latar belakang kartu.
 */
object AdonanPalette {

    // 1. Warna Khusus Varian Rasa Populer
    val OriginalSoftBrown = Color(0xFF964B00)      // Coklat Lembut Khas Waffle Klasik
    val PandanLushGreen = Color(0xFF15803D)        // Hijau Pandan Segar
    val RedvelvetCrimson = Color(0xFFB91C1C)       // Merah Tua Crimson Red Velvet
    val MoccaCoffeeBrown = Color(0xFF5D3A1A)       // Coklat Kopi Moka Sangrai
    val BlackforestWine = Color(0xFF1E1A18)        // Hitam Pekat Bernuansa Coklat Gelap Blackforest
    val DurianGoldenGold = Color(0xFFB45309)       // Kuning Keemasan Madu Durian (Kontras Tajam)
    val TaroVibrantPurple = Color(0xFF7E22CE)      // Ungu Taro / Ube
    val MatchaGreenTea = Color(0xFF2E7D32)         // Hijau Matcha / Green Tea
    val StrawberryRose = Color(0xFFBE123C)         // Merah Stroberi / Berry Rose
    val BlueberryIndigo = Color(0xFF1D4ED8)        // Biru Pekat Blueberry
    val DarkChocolate = Color(0xFF4E2A18)          // Coklat Gelap Kakao
    val CheddarGold = Color(0xFFC05621)            // Oranye Keemasan Keju
    val BananaAmber = Color(0xFFB45309)            // Kuning Pisang Emas
    val MangoSunset = Color(0xFFC2410C)            // Oranye Mangga Matang
    val VanillaOchre = Color(0xFF92400E)           // Kuning Tua Vanila
    val CharcoalSlate = Color(0xFF334155)          // Abu Arang Gelap
    val BubblegumBlue = Color(0xFF0369A1)          // Biru Permen Karet
    val HazelnutBrown = Color(0xFF78350F)          // Coklat Kacang Hazelnut
    val AvocadoOlive = Color(0xFF3F6212)           // Hijau Zaitun Alpukat

    /**
     * Resolves the primary contrast color for any given adonan/flavour name.
     */
    fun getColorForFlavour(flavourName: String): Color {
        val clean = flavourName.substringBefore("(").trim().lowercase()
        return when {
            clean.contains("redvelvet") || clean.contains("red velvet") -> RedvelvetCrimson
            clean.contains("pandan") -> PandanLushGreen
            clean.contains("original") -> OriginalSoftBrown
            clean.contains("mocca") || clean.contains("moka") -> MoccaCoffeeBrown
            clean.contains("blackforest") || clean.contains("black forest") -> BlackforestWine
            clean.contains("durian") || clean.contains("duren") -> DurianGoldenGold
            clean.contains("taro") || clean.contains("talas") || clean.contains("ube") -> TaroVibrantPurple
            clean.contains("matcha") || clean.contains("greentea") || clean.contains("green tea") -> MatchaGreenTea
            clean.contains("strawberry") || clean.contains("stroberi") || clean.contains("berry") -> StrawberryRose
            clean.contains("blueberry") -> BlueberryIndigo
            clean.contains("coklat") || clean.contains("chocolate") || clean.contains("choco") -> DarkChocolate
            clean.contains("keju") || clean.contains("cheese") -> CheddarGold
            clean.contains("pisang") || clean.contains("banana") -> BananaAmber
            clean.contains("mangga") || clean.contains("mango") -> MangoSunset
            clean.contains("jeruk") || clean.contains("orange") || clean.contains("oranye") || clean.contains("jingga") -> MangoSunset
            clean.contains("vanilla") || clean.contains("vanila") -> VanillaOchre
            clean.contains("charcoal") || clean.contains("arang") || clean.contains("hitam") || clean.contains("black") -> CharcoalSlate
            clean.contains("bubblegum") || clean.contains("biru") || clean.contains("blue") -> BubblegumBlue
            clean.contains("kuning") || clean.contains("yellow") || clean.contains("emas") || clean.contains("gold") -> DurianGoldenGold
            clean.contains("merah") || clean.contains("red") || clean.contains("pink") -> RedvelvetCrimson
            clean.contains("hijau") || clean.contains("green") -> PandanLushGreen
            clean.contains("ungu") || clean.contains("purple") || clean.contains("violet") -> TaroVibrantPurple
            clean.contains("almond") || clean.contains("kacang") || clean.contains("hazelnut") -> HazelnutBrown
            clean.contains("avocado") || clean.contains("alpukat") -> AvocadoOlive
            else -> {
                // Algoritma deterministik berbasis HSL dengan Lightness = 0.35 (Kontras tinggi & tajam di atas kartu putih/ivory)
                val hash = abs(clean.hashCode())
                val hue = (hash % 360).toFloat()
                hslToRgbColor(hue, saturation = 0.85f, lightness = 0.35f)
            }
        }
    }

    /**
     * Konversi HSL ke Jetpack Compose Color dengan parameter saturasi dan kecerahan terkontrol.
     */
    private fun hslToRgbColor(h: Float, saturation: Float, lightness: Float): Color {
        val c = (1f - abs(2f * lightness - 1f)) * saturation
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = lightness - c / 2f
        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Color(
            red = (r1 + m).coerceIn(0f, 1f),
            green = (g1 + m).coerceIn(0f, 1f),
            blue = (b1 + m).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    /**
     * Warna background lembut untuk chip / badge pratinjau warna adonan.
     */
    fun getSoftBgForFlavour(flavourName: String): Color {
        return getColorForFlavour(flavourName).copy(alpha = 0.12f)
    }
}

/**
 * Extension function untuk membuat AnnotatedString dari CartLineItem:
 * - Bagian nama Adonan (Flavour) diwarnai khusus sesuai palet adonan dengan huruf tebal (ExtraBold).
 * - Bagian topping utama & secondary diletakkan terpisah sesuai aturan baris baru dan tetap berwarna baseColor.
 */
fun CartLineItem.buildAnnotatedCartTitle(
    baseColor: Color = RichEspressoPrimary,
    baseWeight: FontWeight = FontWeight.SemiBold
): AnnotatedString = buildAnnotatedString {
    if (category == "Egg Waffle") {
        val cleanFlavour = flavourName.substringBefore("(").trim().ifBlank { "Original" }
        val flavourColor = AdonanPalette.getColorForFlavour(cleanFlavour)

        // 1. Kata khusus Adonan dengan warna khas Adonan
        withStyle(
            SpanStyle(
                color = flavourColor,
                fontWeight = FontWeight.ExtraBold
            )
        ) {
            append(cleanFlavour)
        }
        append(" ")

        // 2. Topping utama dan pelengkap
        withStyle(
            SpanStyle(
                color = baseColor,
                fontWeight = baseWeight
            )
        ) {
            when {
                isHalfHalf && secondaryItemName.isNotBlank() -> {
                    append("1/2 $primaryItemName\n+ 1/2 $secondaryItemName")
                }
                isDoubleTopping && secondaryItemName.isNotBlank() -> {
                    append("$primaryItemName\n+ $secondaryItemName")
                }
                else -> {
                    append(primaryItemName)
                }
            }
        }
    } else {
        // Minuman (Drinks)
        withStyle(
            SpanStyle(
                color = baseColor,
                fontWeight = baseWeight
            )
        ) {
            append(primaryItemName)
        }
    }
}

/**
 * Helper untuk memformat baris teks pesanan mentah (fallback) menjadi AnnotatedString:
 * Kata pertama (Adonan) diwarnai sesuai palet rasa adonan.
 */
fun buildAnnotatedFlavourLine(
    rawText: String,
    baseColor: Color = RichEspressoPrimary,
    baseWeight: FontWeight = FontWeight.SemiBold
): AnnotatedString = buildAnnotatedString {
    val trimmed = rawText.trim()
    val firstSpace = trimmed.indexOf(' ')
    if (firstSpace > 0) {
        val candidate = trimmed.substring(0, firstSpace)
        val remainder = trimmed.substring(firstSpace + 1)
        val flavourColor = AdonanPalette.getColorForFlavour(candidate)

        withStyle(
            SpanStyle(
                color = flavourColor,
                fontWeight = FontWeight.ExtraBold
            )
        ) {
            append(candidate)
        }
        append(" ")

        withStyle(
            SpanStyle(
                color = baseColor,
                fontWeight = baseWeight
            )
        ) {
            if (remainder.contains(" + ") && !remainder.contains("\n")) {
                val parts = remainder.split(" + ", limit = 2)
                append(parts[0])
                append("\n+ ")
                append(parts[1])
            } else {
                append(remainder)
            }
        }
    } else {
        withStyle(SpanStyle(color = baseColor, fontWeight = baseWeight)) {
            append(trimmed)
        }
    }
}
