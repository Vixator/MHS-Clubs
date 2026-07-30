package com.precon.mhsclubs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val FigmaPage = Color(0xFF191919)
val FigmaPageAlt = Color(0xFF121212)
val FigmaText = Color(0xFFF2F4F3)
val FigmaTan = Color(0xFF937962)
val FigmaDarkText = Color(0xFF191919)
val FigmaRed = Color(0xFF7A0001)
val FigmaBrightRed = Color(0xFFFF424C)
val FigmaSurface = Color(0xFF1E1E1E)
val FigmaSurfaceAlt = Color(0xFF252525)

@Composable
fun FigmaScreen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = modifier.background(FigmaPage), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.widthIn(max = 402.dp).fillMaxWidth().padding(horizontal = 21.dp),
            content = content
        )
    }
}

@Composable
fun FigmaTitle(text: String, modifier: Modifier = Modifier, compact: Boolean = false) {
    Text(
        text = text,
        color = FigmaText,
        fontSize = if (compact) 28.sp else 40.sp,
        lineHeight = if (compact) 35.sp else 40.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier
    )
}

@Composable
fun FigmaBackLabel(label: String, onClick: () -> Unit) {
    Text(
        text = "‹  $label",
        color = FigmaTan,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp)
    )
}

@Composable
fun FigmaPill(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = FigmaTan,
    contentColor: Color = FigmaDarkText,
    borderColor: Color? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(100.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(background)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) Icon(icon, null, tint = contentColor)
        Text(text, color = contentColor, fontSize = 17.sp, lineHeight = 22.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun FigmaActionButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    background: Color = FigmaTan,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val effectiveAlpha = if (enabled) 1f else 0.4f
    Row(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .clickable(onClick = onClick, enabled = enabled)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) Icon(icon, null, tint = contentColor, modifier = Modifier.alpha(effectiveAlpha))
        if (icon != null) Box(Modifier.widthIn(min = 8.dp))
        Text(text, color = contentColor, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
    }
}

@Composable
fun FigmaCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(FigmaTan)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(13.dp),
        content = content
    )
}

@Composable
fun FigmaSegmentedControl(
    first: String,
    second: String,
    firstSelected: Boolean,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(100.dp)
    Row(modifier.clip(shape).border(1.dp, FigmaText.copy(alpha = 0.3f), shape).height(36.dp)) {
        Segment(first, firstSelected, modifier = Modifier.weight(1f), onClick = onFirst)
        Segment(second, !firstSelected, modifier = Modifier.weight(1f), onClick = onSecond)
    }
}

/** The XSmall outlined split button used for club filtering in the Figma screens. */
@Composable
fun FigmaClubFilter(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leadingShape = RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp)
    val trailingShape = RoundedCornerShape(topEnd = 100.dp, bottomEnd = 100.dp)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(leadingShape)
                .background(FigmaPageAlt)
                .border(1.dp, FigmaText.copy(alpha = 0.3f), leadingShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.Group, null, tint = FigmaTan)
            Text(label, color = FigmaTan, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(
            modifier = Modifier
                .height(32.dp)
                .widthIn(min = 48.dp)
                .clip(trailingShape)
                .background(FigmaPageAlt)
                .border(1.dp, FigmaText.copy(alpha = 0.3f), trailingShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.KeyboardArrowDown, null, tint = FigmaTan)
        }
    }
}

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
            .clip(RoundedCornerShape(100.dp))
            .background(FigmaSurfaceAlt)
            .border(1.dp, FigmaText.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, action) ->
            val isSelected = label == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSelected) FigmaTan else Color.Transparent)
                    .clickable(onClick = action),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) FigmaDarkText else FigmaText.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
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
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label?.let { { Text(it, color = if (isError) FigmaBrightRed else FigmaText.copy(alpha = 0.5f)) } },
            placeholder = placeholder?.let { { Text(it, color = FigmaText.copy(alpha = 0.4f)) } },
            singleLine = singleLine,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(color = FigmaText, fontSize = 17.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = FigmaText,
                unfocusedTextColor = FigmaText,
                cursorColor = FigmaTan,
                errorCursorColor = FigmaBrightRed,
                focusedBorderColor = FigmaTan,
                unfocusedBorderColor = FigmaText.copy(alpha = 0.3f),
                errorBorderColor = FigmaBrightRed,
                focusedLabelColor = FigmaTan,
                unfocusedLabelColor = FigmaText.copy(alpha = 0.5f),
                focusedPlaceholderColor = FigmaText.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = FigmaText.copy(alpha = 0.5f),
                errorLabelColor = FigmaBrightRed,
                errorPlaceholderColor = FigmaBrightRed,
                focusedContainerColor = FigmaPageAlt,
                unfocusedContainerColor = FigmaPageAlt
            ),
            shape = RoundedCornerShape(12.dp)
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = FigmaBrightRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun FigmaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = FigmaTan,
    fontSize: Int = 14
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FigmaDivider(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(1.dp).background(FigmaText.copy(alpha = 0.15f)))
}

@Composable
private fun Segment(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(if (selected) FigmaTan else FigmaPageAlt)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(label, color = if (selected) Color.White else FigmaText, fontSize = 12.sp) }
}
