package com.iracingweekplanner.mobile.presentation.schedule

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iracingweekplanner.mobile.presentation.common.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardUi
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleStatePanelContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleStatePanelVariant
import iracingweekplannermobile.shared.generated.resources.Res
import iracingweekplannermobile.shared.generated.resources.ic_favorites_tab
import iracingweekplannermobile.shared.generated.resources.ic_filters_tab
import iracingweekplannermobile.shared.generated.resources.ic_schedule_tab
import iracingweekplannermobile.shared.generated.resources.ic_settings_tab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleUiFoundationTest {

    @Test
    fun sprint3TokensMatchSharedScheduleDesignConstraints() {
        assertEquals(16.dp, ScheduleUiTokens.ScreenPaddingHorizontal)
        assertEquals(12.dp, ScheduleUiTokens.ScreenPaddingTop)
        assertEquals(16.dp, ScheduleUiTokens.ScreenPaddingBottom)
        assertEquals(8.dp, ScheduleUiTokens.CompactGap)
        assertEquals(12.dp, ScheduleUiTokens.DefaultGap)
        assertEquals(16.dp, ScheduleUiTokens.SectionGap)
        assertEquals(24.dp, ScheduleUiTokens.MajorGap)
        assertEquals(8.dp, ScheduleUiTokens.ControlRadius)
        assertEquals(12.dp, ScheduleUiTokens.CardRadius)
        assertEquals(16.dp, ScheduleUiTokens.BottomNavigationRadius)
        assertEquals(44.dp, ScheduleUiTokens.MinimumIconTouchTarget)
        assertEquals(32.dp, ScheduleUiTokens.MinimumChipHeight)
        assertEquals(64.dp, ScheduleUiTokens.BottomNavigationMinHeight)
        assertEquals(24.sp, ScheduleUiTokens.ScheduleTitleTextSize)
        assertEquals(16.sp, ScheduleUiTokens.SectionTitleTextSize)
        assertEquals(13.sp, ScheduleUiTokens.CaptionTextSize)
    }

    @Test
    fun defaultBottomNavigationKeepsScheduleSelectedAndFutureTabsInactive() {
        val tabs = listOf(
            ScheduleBottomTab(
                label = "Schedule",
                icon = Res.drawable.ic_schedule_tab,
                selected = true,
                enabled = true,
            ),
            ScheduleBottomTab(
                label = "Filters",
                icon = Res.drawable.ic_filters_tab,
                selected = false,
                enabled = false,
            ),
            ScheduleBottomTab(
                label = "Favorites",
                icon = Res.drawable.ic_favorites_tab,
                selected = false,
                enabled = false,
            ),
            ScheduleBottomTab(
                label = "Settings",
                icon = Res.drawable.ic_settings_tab,
                selected = false,
                enabled = false,
            ),
        )

        assertEquals(listOf("Schedule", "Filters", "Favorites", "Settings"), tabs.map { it.label })
        assertTrue(tabs.single { it.label == "Schedule" }.selected)
        assertTrue(tabs.single { it.label == "Schedule" }.enabled)
        assertTrue(tabs.filterNot { it.label == "Schedule" }.all { !it.enabled })
    }

    @Test
    fun statePanelModelsSupportLoadingEmptyAndRetryableErrorContent() {
        val loading = ScheduleStatePanelContent.loading(
            title = "Loading schedule",
            message = "Preparing race week data.",
        )
        val empty = ScheduleStatePanelContent.empty(
            title = "No races this week",
            message = "Try another week.",
        )
        val error = ScheduleStatePanelContent.error(
            title = "Schedule unavailable",
            message = "Retry when the data source is available.",
            retryLabel = "Retry",
        )

        assertEquals(ScheduleStatePanelVariant.Loading, loading.variant)
        assertFalse(loading.canRetry)
        assertEquals(ScheduleStatePanelVariant.Empty, empty.variant)
        assertFalse(empty.canRetry)
        assertEquals(ScheduleStatePanelVariant.Error, error.variant)
        assertTrue(error.canRetry)
    }

    @Test
    fun raceCardModelAcceptsAlreadyPreparedMetadataText() {
        val raceCard = ScheduleRaceCardUi(
            raceId = "race-gt-sprint",
            title = "GT Sprint Series",
            track = "Watkins Glen - Boot",
            carSummary = "GT3 Cars",
            metadataText = "45 min | Rain 35% | Next 8:15 PM",
        )

        assertEquals("race-gt-sprint", raceCard.raceId)
        assertEquals("45 min | Rain 35% | Next 8:15 PM", raceCard.metadataText)
    }

}
