package com.iracingweekplanner.mobile.presentation.common.preview

import com.iracingweekplanner.mobile.presentation.common.model.DateWeekSelectorContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleChipContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleHeaderContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleStatePanelContent
import iracingweekplannermobile.shared.generated.resources.Res
import iracingweekplannermobile.shared.generated.resources.ic_favorites_tab
import iracingweekplannermobile.shared.generated.resources.ic_filters_tab
import iracingweekplannermobile.shared.generated.resources.ic_schedule_tab
import iracingweekplannermobile.shared.generated.resources.ic_settings_tab

object ScheduleUiPreviewData {
    fun foundationResourceSample(): ScheduleUiFoundationSample =
        ScheduleUiFoundationSample(
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
            chips = listOf(
                ScheduleChipContent(label = "Current", selected = true),
                ScheduleChipContent(label = "Week 13"),
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

    fun loadingPanelResourceSample(): ScheduleStatePanelContent =
        ScheduleStatePanelContent.loading(
            title = "Loading schedule",
            message = "Preparing race week data.",
        )

    fun emptyPanelResourceSample(): ScheduleStatePanelContent =
        ScheduleStatePanelContent.empty(
            title = "No races this week",
            message = "Try another week or clear active filters.",
        )

    fun errorPanelResourceSample(): ScheduleStatePanelContent =
        ScheduleStatePanelContent.error(
            title = "Schedule unavailable",
            message = "Retry when schedule data is available.",
            retryLabel = "Retry",
        )
}
