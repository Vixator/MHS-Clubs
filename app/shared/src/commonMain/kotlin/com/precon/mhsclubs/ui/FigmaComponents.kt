package com.precon.mhsclubs.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val Stadium = RoundedCornerShape(100.dp)

/** Shrinks a surface slightly while pressed so taps feel physical. */
@Composable
private fun Modifier.pressable(
    interactionSource: MutableInteractionSource,
    onClick: (() -> Unit)?,
    enabled: Boolean = true,
    pressedScale: Float = 0.97f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val currentScale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 900f)
    )
    return scale(currentScale).then(
        if (onClick != null) {
            Modifier.clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick
            )
        } else Modifier
    )
}

@Composable
fun FigmaScreen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 402.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            content = content
        )
    }
}

@Composable
fun FigmaTitle(text: String, modifier: Modifier = Modifier, compact: Boolean = false) {
    Text(
        text = text,
        style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
fun FigmaBackLabel(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(Stadium)
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** A compact metadata chip. Filled reads as accent; outlined reads as neutral detail. */
@Composable
fun FigmaPill(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    borderColor: Color? = MaterialTheme.colorScheme.outlineVariant,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .clip(Stadium)
            .background(background)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, Stadium) else Modifier)
            .pressable(interactionSource, onClick)
            .heightIn(min = if (onClick == null) 28.dp else 44.dp)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) Icon(icon, null, tint = contentColor, modifier = Modifier.size(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FigmaActionButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    background: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = 52.dp)
            .animateContentSize(),
        shape = Stadium,
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = contentColor,
            disabledContainerColor = background.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.38f)
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

/**
 * The standard content surface: a dark raised panel with a hairline edge.
 *
 * Height is driven by content; callers should pass a minimum rather than a fixed height.
 */
@Composable
fun FigmaCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, shape)
            .pressable(interactionSource, onClick)
            .animateContentSize()
            .padding(16.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            content()
        }
    }
}

@Composable
fun FigmaSegmentedControl(
    first: String,
    second: String,
    firstSelected: Boolean,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier
            .clip(Stadium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, Stadium)
            .heightIn(min = 48.dp)
            .padding(3.dp)
    ) {
        Segment(first, firstSelected, modifier = Modifier.weight(1f), onClick = onFirst, enabled = enabled)
        Segment(second, !firstSelected, modifier = Modifier.weight(1f), onClick = onSecond, enabled = enabled)
    }
}

/** An outlined split control for filtering a list down to one club. */
@Composable
fun FigmaClubFilter(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(Stadium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, Stadium)
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Group,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            Icons.Default.KeyboardArrowDown,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}

private val navigationIcons = mapOf(
    "Clubs" to Icons.Default.Group,
    "Calendar" to Icons.Default.CalendarToday,
    "Updates" to Icons.Default.Campaign,
    "Account" to Icons.Default.Person
)

@Composable
fun FigmaBottomNavigation(
    selected: String,
    onClubs: () -> Unit,
    onCalendar: () -> Unit,
    onUpdates: () -> Unit,
    onAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf("Clubs" to onClubs, "Calendar" to onCalendar, "Updates" to onUpdates, "Account" to onAccount)
    Row(
        modifier = modifier
            .clip(Stadium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, Stadium)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, action) ->
            NavigationTab(label, label == selected, Modifier.weight(1f), action)
        }
    }
}

@Composable
private fun NavigationTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val indicator by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
    )
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(Stadium)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = indicator))
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        navigationIcons[label]?.let {
            Icon(it, null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(2.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun FigmaOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
            placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodyLarge) } },
            singleLine = singleLine,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = scheme.onSurface,
                unfocusedTextColor = scheme.onSurface,
                cursorColor = scheme.primary,
                errorCursorColor = scheme.error,
                focusedBorderColor = scheme.primary,
                unfocusedBorderColor = scheme.outlineVariant,
                errorBorderColor = scheme.error,
                focusedLabelColor = scheme.primary,
                unfocusedLabelColor = scheme.onSurfaceVariant,
                focusedPlaceholderColor = scheme.onSurfaceVariant,
                unfocusedPlaceholderColor = scheme.onSurfaceVariant,
                errorLabelColor = scheme.error,
                errorPlaceholderColor = scheme.error,
                focusedContainerColor = scheme.surfaceVariant,
                unfocusedContainerColor = scheme.surfaceVariant,
                errorContainerColor = scheme.surfaceVariant
            ),
            shape = MaterialTheme.shapes.small
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp)
            )
        }
    }
}

@Composable
fun FigmaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

/**
 * Consistent, considerate presentation for loading, empty, and recoverable-error states.
 * The caller owns all data and retry behavior; this component only standardizes the visual shell.
 */
@Composable
fun FigmaStatusPanel(
    title: String,
    message: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    FigmaCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = iconTint,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            FigmaActionButton(
                text = actionLabel,
                modifier = Modifier.fillMaxWidth(),
                onClick = onAction
            )
        }
    }
}

@Composable
fun FigmaDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

/** A circular monogram used wherever a member has no avatar image. */
@Composable
fun FigmaMonogram(name: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .sizeIn(minWidth = 44.dp, minHeight = 44.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name?.initials() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

fun String.initials(): String =
    split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")

@Composable
private fun Segment(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit, enabled: Boolean) {
    val fill by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
    )
    Box(
        modifier = modifier
            .clip(Stadium)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = fill))
            .clickable(enabled = enabled, onClick = onClick)
            .heightIn(min = 40.dp)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = (if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                .copy(alpha = if (enabled) 1f else 0.38f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
