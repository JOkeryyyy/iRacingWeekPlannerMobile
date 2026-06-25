package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.presentation.schedule.model.DateWeekSelectorContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleChipContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleHeaderContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleShellContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelContent
import iracingweekplannermobile.shared.generated.resources.Res
import iracingweekplannermobile.shared.generated.resources.ic_favorites_tab
import iracingweekplannermobile.shared.generated.resources.ic_filters_tab
import iracingweekplannermobile.shared.generated.resources.ic_schedule_tab
import iracingweekplannermobile.shared.generated.resources.ic_settings_tab
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
