package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.presentation.schedule.model.DateWeekSelectorContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleChipContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleHeaderContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleShellContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleShellContentTest {

    @Test
    fun shellContentKeepsScheduleRegionsTogetherForTheRootAppSurface() {
        val content = ScheduleShellContent(
            selectedWeekNumber = 13,
            header = ScheduleHeaderContent(
                weekTitle = "Week 13 Schedule",
                lastUpdatedText = "Last updated 10:42 AM",
                refreshLabel = "Refresh",
                refreshContentDescription = "Refresh schedule",
            ),
            selector = DateWeekSelectorContent(
                weekLabel = "Week 13",
                dateContext = "Jun 16 - Jun 23",
                previousEnabled = true,
                nextEnabled = true,
                previousLabel = "Prev",
                previousContentDescription = "Previous week",
                todayLabel = "Today",
                nextLabel = "Next",
                nextContentDescription = "Next week",
            ),
            summaryChips = listOf(
                ScheduleChipContent(label = "Week 13", selected = true),
                ScheduleChipContent(label = "12 races"),
            ),
            statePanel = ScheduleStatePanelContent.loading(
                title = "Loading schedule",
                message = "Preparing race week data.",
            ),
            bottomTabs = listOf(
                ScheduleBottomTab(label = "Schedule", selected = true, enabled = true),
                ScheduleBottomTab(label = "Filters", selected = false, enabled = false),
                ScheduleBottomTab(label = "Favorites", selected = false, enabled = false),
                ScheduleBottomTab(label = "Settings", selected = false, enabled = false),
            ),
        )

        assertEquals(13, content.selectedWeekNumber)
        assertEquals("Week 13 Schedule", content.header.weekTitle)
        assertEquals("Last updated 10:42 AM", content.header.lastUpdatedText)
        assertEquals("Week 13", content.selector.weekLabel)
        assertEquals(listOf("Week 13", "12 races"), content.summaryChips.map { it.label })
        assertTrue(content.summaryChips.single { it.label == "Week 13" }.selected)
        assertTrue(content.bottomTabs.single { it.label == "Schedule" }.selected)
        assertTrue(content.bottomTabs.single { it.label == "Schedule" }.enabled)
        assertFalse(content.bottomTabs.filterNot { it.label == "Schedule" }.any { it.enabled })
    }
}
