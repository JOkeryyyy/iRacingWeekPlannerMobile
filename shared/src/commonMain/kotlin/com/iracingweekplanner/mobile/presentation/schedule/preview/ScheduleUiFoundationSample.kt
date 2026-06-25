package com.iracingweekplanner.mobile.presentation.schedule.preview

import com.iracingweekplanner.mobile.presentation.schedule.model.DateWeekSelectorContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleChipContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleHeaderContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleRaceCardContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelContent

data class ScheduleUiFoundationSample(
    val header: ScheduleHeaderContent,
    val selector: DateWeekSelectorContent,
    val chips: List<ScheduleChipContent>,
    val raceCard: ScheduleRaceCardContent,
    val statePanels: List<ScheduleStatePanelContent>,
    val bottomTabs: List<ScheduleBottomTab>,
)
