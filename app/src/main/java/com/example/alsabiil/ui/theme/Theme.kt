package com.example.alsabiil.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppPalette {
    EMERALD, BLACK, TEAL, INDIGO, GOLD, DARK_EMERALD;

    companion object {
        fun fromId(id: String): AppPalette = when (id) {
            "black" -> BLACK
            "teal" -> TEAL
            "indigo" -> INDIGO
            "gold" -> GOLD
            "dark_emerald" -> DARK_EMERALD
            else -> EMERALD
        }
    }
}

private val EmeraldColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimaryDark,
    secondary = EmeraldSecondary,
    onSecondary = Color.White,
    background = EmeraldBackground,
    onBackground = Color(0xFF2D3748),
    surface = Color.White,
    onSurface = Color(0xFF2D3748)
)

private val RichBlackColorScheme = darkColorScheme(
    primary = RichBlackPrimary,
    onPrimary = Color.White,
    primaryContainer = RichBlackPrimaryDark,
    secondary = RichBlackSecondary,
    onSecondary = Color.White,
    background = RichBlackBackground,
    surface = RichBlackAccent.copy(alpha = 0.08f)
)

private val DeepTealColorScheme = darkColorScheme(
    primary = DeepTealPrimary,
    onPrimary = Color.White,
    primaryContainer = DeepTealPrimaryDark,
    secondary = DeepTealSecondary,
    onSecondary = Color.White,
    background = DeepTealBackground,
    surface = DeepTealAccent.copy(alpha = 0.1f)
)

private val ForestGreenColorScheme = darkColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestGreenPrimaryDark,
    secondary = ForestGreenSecondary,
    onSecondary = Color.White,
    background = ForestGreenBackground,
    surface = ForestGreenAccent.copy(alpha = 0.1f)
)

private val DarkEmeraldColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkEmeraldPrimaryDark,
    secondary = DarkEmeraldSecondary,
    onSecondary = Color.White,
    background = DarkEmeraldBackground,
    surface = DarkEmeraldAccent.copy(alpha = 0.08f)
)

@Composable
fun AlSabiilTheme(
    palette: AppPalette = AppPalette.EMERALD,
    content: @Composable () -> Unit
) {
    val colorScheme = when (palette) {
        AppPalette.EMERALD -> EmeraldColorScheme
        AppPalette.BLACK -> RichBlackColorScheme
        AppPalette.TEAL -> DeepTealColorScheme
        AppPalette.INDIGO -> DeepTealColorScheme 
        AppPalette.GOLD -> ForestGreenColorScheme 
        AppPalette.DARK_EMERALD -> DarkEmeraldColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
