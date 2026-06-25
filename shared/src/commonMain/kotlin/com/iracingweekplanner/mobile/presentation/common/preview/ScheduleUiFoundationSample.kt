package com.iracingweekplanner.mobile.presentation.common.preview

import com.iracingweekplanner.mobile.presentation.common.model.DateWeekSelectorContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleChipContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleHeaderContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleStatePanelContent

data class ScheduleUiFoundationSample(
    val header: ScheduleHeaderContent,
    val selector: DateWeekSelectorContent,
    val chips: List<ScheduleChipContent>,
    val raceCard: ScheduleRaceCardContent,
    val statePanels: List<ScheduleStatePanelContent>,
    val bottomTabs: List<ScheduleBottomTab>,
)
