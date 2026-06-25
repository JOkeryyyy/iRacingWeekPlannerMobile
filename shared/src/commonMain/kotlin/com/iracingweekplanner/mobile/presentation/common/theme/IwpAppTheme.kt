package com.iracingweekplanner.mobile.presentation.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object IwpThemeColors {
    val RacingRed = Color(0xFFB3261E)
    val TimingBlue = Color(0xFF145DA0)
    val FlagGreen = Color(0xFF2E7D32)
    val AppBackground = Color(0xFFF7F8FA)
    val CardSurface = Color(0xFFFFFFFF)
}

internal val IwpLightColorScheme = lightColorScheme(
    primary = IwpThemeColors.RacingRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = IwpThemeColors.TimingBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3E4FF),
    onSecondaryContainer = Color(0xFF001C38),
    tertiary = IwpThemeColors.FlagGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB7F3B5),
    onTertiaryContainer = Color(0xFF002106),
    background = IwpThemeColors.AppBackground,
    onBackground = Color(0xFF1B1B1F),
    surface = IwpThemeColors.CardSurface,
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

internal val IwpDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFA2C9FF),
    onSecondary = Color(0xFF00315C),
    secondaryContainer = Color(0xFF004883),
    onSecondaryContainer = Color(0xFFD3E4FF),
    tertiary = Color(0xFF9CD69B),
    onTertiary = Color(0xFF00390F),
    tertiaryContainer = Color(0xFF00531A),
    onTertiaryContainer = Color(0xFFB7F3B5),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE3E2E8),
    surface = Color(0xFF1B1B1F),
    onSurface = Color(0xFFE3E2E8),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

internal val IwpTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

@Composable
fun IwpAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) IwpDarkColorScheme else IwpLightColorScheme,
        typography = IwpTypography,
        content = content,
    )
}
