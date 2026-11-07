package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

/**
 * Shared Button Variants for the Waffleo 'Floating Pill' Design System.
 */
enum class WaffleoPillButtonVariant {
    PrimaryEspresso,
    CreamGold,
    SoftSecondary,
    SuccessSage,
    WarningAmber,
    DangerTerracotta
}

/**
 * Shared 'Floating Pill' Button with capsule geometry, elevated surface, and clean subtle border.
 */
@Composable
fun WaffleoPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: WaffleoPillButtonVariant = WaffleoPillButtonVariant.PrimaryEspresso,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    height: Dp = 44.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
    testTag: String = ""
) {
    val (containerColor, contentColor, borderColor) = when (variant) {
        WaffleoPillButtonVariant.PrimaryEspresso -> Triple(
            RichEspressoPrimary,
            ElevatedIvoryCard,
            ChampagneGoldAccent.copy(alpha = 0.35f)
        )
        WaffleoPillButtonVariant.CreamGold -> Triple(
            SoftGoldHighlight,
            RichEspressoPrimary,
            ChampagneGoldAccent.copy(alpha = 0.55f)
        )
        WaffleoPillButtonVariant.SoftSecondary -> Triple(
            SoftOatSurface,
            RichEspressoPrimary,
            SubtleSandBorder.copy(alpha = 0.85f)
        )
        WaffleoPillButtonVariant.SuccessSage -> Triple(
            MatchaSageSuccess,
            ElevatedIvoryCard,
            MatchaSageSuccess.copy(alpha = 0.4f)
        )
        WaffleoPillButtonVariant.WarningAmber -> Triple(
            HoneyAmberWarning,
            ElevatedIvoryCard,
            HoneyAmberWarning.copy(alpha = 0.4f)
        )
        WaffleoPillButtonVariant.DangerTerracotta -> Triple(
            SoftTerracottaBg,
            TerracottaDanger,
            TerracottaDanger.copy(alpha = 0.30f)
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = SoftOatSurface.copy(alpha = 0.7f),
            disabledContentColor = MutedCocoaText.copy(alpha = 0.45f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) borderColor else SubtleSandBorder.copy(alpha = 0.4f)
        ),
        contentPadding = contentPadding,
        modifier = modifier
            .height(height)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Shared 'Floating Pill' Elevated Card with organic rounded corners, elevated surface, and clean subtle border.
 */
@Composable
fun WaffleoFloatingCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    containerColor: Color = ElevatedIvoryCard,
    borderColor: Color = SubtleSandBorder.copy(alpha = 0.65f),
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = modifier
        .then(
            if (onClick != null) {
                Modifier
                    .clip(shape)
                    .clickable(onClick = onClick)
            } else {
                Modifier
            }
        )
        .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(width = 1.dp, color = borderColor),
        content = content
    )
}

/**
 * Shared 'Floating Pill' Surface Container for status badges, price pills, and capsule rows.
 */
@Composable
fun WaffleoPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SoftGoldHighlight,
    contentColor: Color = DeepCaramelGold,
    borderColor: Color = ChampagneGoldAccent.copy(alpha = 0.35f),
    elevation: Dp = 1.dp,
    horizontalPadding: Dp = 11.dp,
    verticalPadding: Dp = 4.dp
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor,
        shadowElevation = elevation,
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
        )
    }
}

/**
 * Shared 'Floating Pill' Row Container with rounded corners, elevated surface, and clean border.
 */
@Composable
fun WaffleoPillSurface(
    modifier: Modifier = Modifier,
    containerColor: Color = SoftOatSurface,
    borderColor: Color = SubtleSandBorder.copy(alpha = 0.70f),
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(50)
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(shape)
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        shape = shape,
        color = containerColor,
        tonalElevation = 1.dp,
        shadowElevation = elevation,
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Shared 'Floating Pill' Input Field with capsule corners, elevated surface, and clean focus-reactive border.
 */
@Composable
fun WaffleoSoftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String = "",
    prefixText: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    containerColor: Color = SoftOatSurface,
    onFocusChanged: ((androidx.compose.ui.focus.FocusState) -> Unit)? = null,
    testTag: String = ""
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = RichEspressoPrimary,
                modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = if (isFocused) ElevatedIvoryCard else containerColor,
            tonalElevation = if (isFocused) 2.dp else 1.dp,
            shadowElevation = if (isFocused) 4.dp else 2.dp,
            border = BorderStroke(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) ChampagneGoldAccent else SubtleSandBorder.copy(alpha = 0.75f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (prefixText.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftGoldHighlight,
                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = prefixText.trim(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = DeepCaramelGold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedCocoaText.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = RichEspressoPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = keyboardOptions,
                        visualTransformation = visualTransformation,
                        cursorBrush = SolidColor(ChampagneGoldAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                isFocused = it.isFocused
                                onFocusChanged?.invoke(it)
                            }
                            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
                    )
                }
            }
        }
    }
}
