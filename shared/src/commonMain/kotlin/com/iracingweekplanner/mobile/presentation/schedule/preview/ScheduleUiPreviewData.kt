package com.iracingweekplanner.mobile.presentation.schedule.preview

import com.iracingweekplanner.mobile.presentation.schedule.model.DateWeekSelectorContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleChipContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleHeaderContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleRaceCardContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelContent

object ScheduleUiPreviewData {
    fun foundationSample(): ScheduleUiFoundationSample =
        ScheduleUiFoundationSample(
            header = ScheduleHeaderContent(
                weekTitle = "Week 13 Schedule",
                lastUpdatedText = "Last updated 10:42 AM",
            ),
            selector = DateWeekSelectorContent(
                weekLabel = "Week 13",
                dateContext = "Jun 16 - Jun 23",
                previousEnabled = true,
                nextEnabled = true,
            ),
            chips = listOf(
                ScheduleChipContent(label = "Current", selected = true),
                ScheduleChipContent(label = "Week 13"),
                ScheduleChipContent(label = "Oval"),
            ),
            raceCard = ScheduleRaceCardContent(
                title = "GT Sprint Series",
                track = "Watkins Glen - Boot",
                carSummary = "GT3 Cars",
                metadata = listOf("45 min", "Rain 35%", "Next 8:15 PM"),
                chips = listOf(
                    ScheduleChipContent(label = "Road", selected = true),
                    ScheduleChipContent(label = "B License"),
                ),
            ),
            statePanels = emptyList(),
            bottomTabs = ScheduleBottomTab.defaultTabs(),
        )

    fun loadingPanelSample(): ScheduleStatePanelContent =
        ScheduleStatePanelContent.loading(
            title = "Loading Week 13 schedule",
            message = "Preparing sessions, cars, and track data.",
        )

    fun emptyPanelSample(): ScheduleStatePanelContent =
        ScheduleStatePanelContent.empty(
            title = "No races this week",
            message = "Try another week or clear active filters.",
        )

    fun errorPanelSample(): ScheduleStatePanelContent =
        ScheduleStatePanelContent.error(
            title = "Schedule unavailable",
            message = "Retry when the data source is available.",
            retryLabel = "Retry",
        )
}
