package com.precon.mhsclubs.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The shared visual foundation for the web and Android clients.
 *
 * The palette follows the MHS Clubs dark graphite and maroon system so
 * every Compose surface remains visually consistent on either host platform.
 */
/** Colours sampled from the approved MHS Clubs mobile Figma frames. */
private val MhsClubsColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF937962),
    onPrimary = Color(0xFF191919),
    primaryContainer = Color(0xFF937962),
    onPrimaryContainer = Color(0xFF191919),
    secondary = Color(0xFF937962),
    onSecondary = Color(0xFF191919),
    secondaryContainer = Color(0xFF937962),
    onSecondaryContainer = Color(0xFF191919),
    tertiary = Color(0xFF7A0001),
    onTertiary = Color.White,
    error = Color(0xFFFF424C),
    onError = Color(0xFF191919),
    errorContainer = Color(0xFF441D1E),
    onErrorContainer = Color(0xFFFF424C),
    background = Color(0xFF121212),
    onBackground = Color(0xFFF2F4F3),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFF2F4F3),
    surfaceVariant = Color(0xFF252525),
    onSurfaceVariant = Color(0xFFF2F4F3),
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2A2A2A),
    surfaceContainerHighest = Color(0xFF252525)
)

private val MhsClubsTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 40.sp,
        color = FigmaText
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        color = FigmaText
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = FigmaText
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = FigmaText
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = FigmaText
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = FigmaText
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = FigmaText
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = FigmaText
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = FigmaText
    )
)

private val MhsClubsShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MhsClubsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MhsClubsColorScheme,
        typography = MhsClubsTypography,
        shapes = MhsClubsShapes,
        content = content
    )
}
