package com.iracingweekplanner.mobile.presentation.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class IwpAppThemeTest {

    @Test
    fun appThemeColorSchemeUsesRacingPaletteAndNeutralBackground() {
        assertEquals(Color(0xFFB3261E), IwpThemeColors.RacingRed)
        assertEquals(Color(0xFF145DA0), IwpThemeColors.TimingBlue)
        assertEquals(Color(0xFF2E7D32), IwpThemeColors.FlagGreen)
        assertEquals(Color(0xFFF7F8FA), IwpThemeColors.AppBackground)
        assertEquals(Color(0xFFFFFFFF), IwpThemeColors.CardSurface)

        assertEquals(IwpThemeColors.RacingRed, IwpLightColorScheme.primary)
        assertEquals(IwpThemeColors.TimingBlue, IwpLightColorScheme.secondary)
        assertEquals(IwpThemeColors.FlagGreen, IwpLightColorScheme.tertiary)
        assertEquals(IwpThemeColors.AppBackground, IwpLightColorScheme.background)
        assertEquals(IwpThemeColors.CardSurface, IwpLightColorScheme.surface)
    }
}
