package com.iracingweekplanner.mobile.presentation.schedule

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleRaceCardContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelVariant
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
            ScheduleBottomTab(label = "Schedule", selected = true, enabled = true),
            ScheduleBottomTab(label = "Filters", selected = false, enabled = false),
            ScheduleBottomTab(label = "Favorites", selected = false, enabled = false),
            ScheduleBottomTab(label = "Settings", selected = false, enabled = false),
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
        val raceCard = ScheduleRaceCardContent(
            title = "GT Sprint Series",
            track = "Watkins Glen - Boot",
            carSummary = "GT3 Cars",
            metadataText = "45 min | Rain 35% | Next 8:15 PM",
        )

        assertEquals("45 min | Rain 35% | Next 8:15 PM", raceCard.metadataText)
    }

}
