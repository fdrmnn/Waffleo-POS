package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WaffleoColorScheme = lightColorScheme(
    primary = RichEspressoPrimary,
    onPrimary = ElevatedIvoryCard,
    primaryContainer = SoftGoldHighlight,
    onPrimaryContainer = RichEspressoPrimary,
    secondary = ChampagneGoldAccent,
    onSecondary = RichEspressoPrimary,
    secondaryContainer = SoftOatSurface,
    onSecondaryContainer = RichEspressoPrimary,
    tertiary = MatchaSageSuccess,
    onTertiary = ElevatedIvoryCard,
    background = WarmAlabasterBg,
    onBackground = RichEspressoPrimary,
    surface = ElevatedIvoryCard,
    onSurface = RichEspressoPrimary,
    surfaceVariant = SoftOatSurface,
    onSurfaceVariant = MutedCocoaText,
    outline = SoftOatSurface,
    outlineVariant = SoftGoldHighlight,
    error = TerracottaDanger,
    onError = ElevatedIvoryCard
)

val WaffleoShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WaffleoColorScheme,
        typography = Typography,
        shapes = WaffleoShapes,
        content = content
    )
}
