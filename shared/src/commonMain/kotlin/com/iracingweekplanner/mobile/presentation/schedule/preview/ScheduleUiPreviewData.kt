package com.iracingweekplanner.mobile.presentation.schedule.preview

import androidx.compose.runtime.Composable
import com.iracingweekplanner.mobile.presentation.PlannerDataUiMessage
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleTextResources
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleChipContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleRaceCardContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelContent

object ScheduleUiPreviewData {
    @Composable
    fun foundationResourceSample(): ScheduleUiFoundationSample =
        ScheduleUiFoundationSample(
            header = ScheduleTextResources.headerContent(
                weekNumber = 13,
                lastUpdatedTime = "10:42 AM",
            ),
            selector = ScheduleTextResources.dateWeekSelectorContent(
                weekNumber = 13,
                dateContext = "Jun 16 - Jun 23",
                previousEnabled = true,
                nextEnabled = true,
            ),
            chips = listOf(
                ScheduleChipContent(label = "Current", selected = true),
                ScheduleChipContent(label = ScheduleTextResources.weekLabel(13)),
                ScheduleChipContent(label = "Oval"),
            ),
            raceCard = ScheduleRaceCardContent(
                title = "GT Sprint Series",
                track = "Watkins Glen - Boot",
                carSummary = "GT3 Cars",
                metadataText = "45 min | Rain 35% | Next 8:15 PM | 12 races",
                chips = listOf(
                    ScheduleChipContent(label = "Road", selected = true),
                    ScheduleChipContent(label = "B License"),
                ),
            ),
            statePanels = emptyList(),
            bottomTabs = ScheduleTextResources.bottomTabs(),
        )

    @Composable
    fun loadingPanelResourceSample(): ScheduleStatePanelContent =
        ScheduleTextResources.loadingPanelContent()

    @Composable
    fun emptyPanelResourceSample(): ScheduleStatePanelContent =
        ScheduleTextResources.emptyPanelContent()

    @Composable
    fun errorPanelResourceSample(): ScheduleStatePanelContent =
        errorPanelResourceSample(PlannerDataUiMessage.PLANNER_DATA_UNAVAILABLE)

    @Composable
    fun errorPanelResourceSample(message: PlannerDataUiMessage): ScheduleStatePanelContent =
        ScheduleTextResources.statePanelContent(message)
}
