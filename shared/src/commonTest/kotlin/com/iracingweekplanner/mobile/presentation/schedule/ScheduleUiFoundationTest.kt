package com.iracingweekplanner.mobile.presentation.schedule

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelVariant
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData
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
        val tabs = ScheduleBottomTab.defaultTabs()

        assertEquals(listOf("Schedule", "Filters", "Favorites", "Settings"), tabs.map { it.label })
        assertTrue(tabs.single { it.label == "Schedule" }.selected)
        assertTrue(tabs.single { it.label == "Schedule" }.enabled)
        assertTrue(tabs.filterNot { it.label == "Schedule" }.all { !it.enabled })
    }

    @Test
    fun statePanelModelsSupportLoadingEmptyAndRetryableErrorContent() {
        val loading = ScheduleStatePanelContent.loading()
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
    fun previewDataDrivesComponentsWithoutRepositoryGraph() {
        val sample = ScheduleUiPreviewData.foundationSample()

        assertEquals("Week 13 Schedule", sample.header.weekTitle)
        assertEquals("Last updated 10:42 AM", sample.header.lastUpdatedText)
        assertEquals(listOf("Current", "Week 13", "Oval"), sample.chips.map { it.label })
        assertTrue(sample.chips.first().selected)
        assertEquals("Schedule", sample.bottomTabs.single { it.selected }.label)
        assertEquals("GT Sprint Series", sample.raceCard.title)
        assertTrue(sample.statePanels.isEmpty())
    }

    @Test
    fun statePanelPreviewDataIsExplicitForLoadingEmptyAndErrorStates() {
        assertEquals(ScheduleStatePanelVariant.Loading, ScheduleUiPreviewData.loadingPanelSample().variant)
        assertEquals(ScheduleStatePanelVariant.Empty, ScheduleUiPreviewData.emptyPanelSample().variant)
        assertEquals(ScheduleStatePanelVariant.Error, ScheduleUiPreviewData.errorPanelSample().variant)
    }
}
