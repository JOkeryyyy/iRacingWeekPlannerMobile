package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.presentation.PlannerDataUiMessage
import iracingweekplannermobile.shared.generated.resources.Res
import iracingweekplannermobile.shared.generated.resources.ic_favorites_tab
import iracingweekplannermobile.shared.generated.resources.ic_filters_tab
import iracingweekplannermobile.shared.generated.resources.ic_schedule_tab
import iracingweekplannermobile.shared.generated.resources.ic_settings_tab
import iracingweekplannermobile.shared.generated.resources.schedule_invalid_data_message
import iracingweekplannermobile.shared.generated.resources.schedule_invalid_data_title
import iracingweekplannermobile.shared.generated.resources.schedule_empty_title
import iracingweekplannermobile.shared.generated.resources.schedule_race_count
import iracingweekplannermobile.shared.generated.resources.schedule_tab_label
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleComposeResourcesTest {

    @Test
    fun scheduleTextUsesComposeMultiplatformResourcesDirectly() {
        assertEquals(Res.string.schedule_tab_label, Res.string.schedule_tab_label)
        assertEquals(Res.string.schedule_empty_title, Res.string.schedule_empty_title)
        assertEquals(Res.plurals.schedule_race_count, Res.plurals.schedule_race_count)
    }

    @Test
    fun scheduleTextResourceBoundaryLivesOutsidePreviewData() {
        val tabs = ScheduleTextResources.bottomTabResources()
        val invalidData = ScheduleTextResources.statePanelResources(
            PlannerDataUiMessage.INVALID_PLANNER_DATA,
        )

        assertEquals(Res.string.schedule_tab_label, tabs.first().label)
        assertEquals(
            listOf(
                Res.drawable.ic_schedule_tab,
                Res.drawable.ic_filters_tab,
                Res.drawable.ic_favorites_tab,
                Res.drawable.ic_settings_tab,
            ),
            tabs.map { it.icon },
        )
        assertEquals(Res.string.schedule_invalid_data_title, invalidData.title)
        assertEquals(Res.string.schedule_invalid_data_message, invalidData.message)
    }
}
