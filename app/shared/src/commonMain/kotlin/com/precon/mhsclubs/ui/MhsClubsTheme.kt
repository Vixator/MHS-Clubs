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
 * The single source of truth for MHS Clubs colour, type, and shape.
 *
 * Screens must read every visual value from [MaterialTheme] so the whole app can be
 * restyled from this file alone.
 */

// Layered near-black surfaces carry the UI; tan is reserved for accents.
private val Ink = Color(0xFF0F0F0F)
private val SurfaceCard = Color(0xFF171717)
private val SurfaceRaised = Color(0xFF1F1F1F)
private val SurfaceInput = Color(0xFF242424)
private val Hairline = Color(0xFF2C2C2C)
private val HairlineStrong = Color(0xFF3D3A36)

/** Lightened from the brand tan so accent text clears WCAG AA on dark surfaces. */
private val Tan = Color(0xFFC9A77F)
private val TanPressed = Color(0xFFD8BC9A)
private val Maroon = Color(0xFF991B1E)
private val MaroonSurface = Color(0xFF2A1315)

private val TextPrimary = Color(0xFFF4F3F1)
private val TextSecondary = Color(0xFFA8A29B)
private val Danger = Color(0xFFFF5A5F)
private val DangerSurface = Color(0xFF3A1B1D)

private val MhsClubsColorScheme: ColorScheme = darkColorScheme(
    primary = Tan,
    onPrimary = Ink,
    primaryContainer = TanPressed,
    onPrimaryContainer = Ink,
    secondary = TextSecondary,
    onSecondary = Ink,
    secondaryContainer = SurfaceRaised,
    onSecondaryContainer = TextPrimary,
    tertiary = Maroon,
    onTertiary = TextPrimary,
    tertiaryContainer = MaroonSurface,
    onTertiaryContainer = Tan,
    error = Danger,
    onError = Ink,
    errorContainer = DangerSurface,
    onErrorContainer = Danger,
    background = Ink,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceInput,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceCard,
    surfaceContainerHigh = SurfaceRaised,
    surfaceContainerHighest = SurfaceInput,
    outline = HairlineStrong,
    outlineVariant = Hairline
)

/** Low-emphasis text for inactive dates and disabled metadata. */
val ColorScheme.textTertiary: Color get() = Color(0xFF6F6A64)

/**
 * A seven-step scale. Sizes are 40/28/20/17/15/13/11 with negative tracking on the
 * display sizes; colour is deliberately absent so styles work on any surface.
 */
private val MhsClubsTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.2).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.6).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.4.sp
    )
)

private val MhsClubsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
