package com.iracingweekplanner.mobile.presentation.common.model

data class ScheduleShellContent(
    val selectedWeekNumber: Int,
    val header: ScheduleHeaderContent,
    val selector: DateWeekSelectorContent,
    val summaryChips: List<ScheduleChipContent>,
    val statePanel: ScheduleStatePanelContent?,
    val bottomTabs: List<ScheduleBottomTab>,
    val raceCards: List<ScheduleRaceCardContent> = emptyList(),
)
