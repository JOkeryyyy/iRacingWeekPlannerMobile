package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardUi

enum class ScheduleUiMessage {
    ShowingCachedPlannerData,
    PlannerDataUnavailable,
    InvalidPlannerData,
    LocalPlannerDataUnavailable,
}

data class ScheduleUiState(
    val selectedWeekNumber: Int,
    val availableWeekNumbers: List<Int>,
    val dateContext: String?,
    val lastUpdatedDisplayText: String?,
    val raceCards: List<ScheduleRaceCardUi>,
    val panelMessage: ScheduleUiMessage?,
    val isLoading: Boolean,
    val isEmpty: Boolean,
    val isCached: Boolean,
    val canSelectPreviousWeek: Boolean,
    val canSelectCurrentWeek: Boolean,
    val canSelectNextWeek: Boolean,
)
