package com.precon.mhsclubs.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The shared visual foundation for the web and Android clients.
 *
 * The palette follows the Schedlify-inspired dark graphite and maroon system so
 * every Compose surface remains visually consistent on either host platform.
 */
private val MhsClubsColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFB94355),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5A1A22),
    onPrimaryContainer = Color(0xFFFFD9DE),
    secondary = Color(0xFFFFA34D),
    onSecondary = Color(0xFF321600),
    secondaryContainer = Color(0xFF673600),
    onSecondaryContainer = Color(0xFFFFDCC1),
    tertiary = Color(0xFF26DE81),
    onTertiary = Color(0xFF00391B),
    tertiaryContainer = Color(0xFF005228),
    onTertiaryContainer = Color(0xFF7BFFAD),
    error = Color(0xFFFF6B78),
    onError = Color(0xFF5C000D),
    errorContainer = Color(0xFF8B1123),
    onErrorContainer = Color(0xFFFFD9DD),
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE6EDF3),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFFB4BDC7),
    outline = Color(0xFF46505A),
    outlineVariant = Color(0xFF30363D),
    surfaceContainerHighest = Color(0xFF21262D)
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
        typography = MaterialTheme.typography,
        shapes = MhsClubsShapes,
        content = content
    )
}
