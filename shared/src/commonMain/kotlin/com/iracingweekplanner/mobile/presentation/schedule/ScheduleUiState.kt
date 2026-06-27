package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.presentation.PlannerDataUiMessage
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardUi

data class ScheduleUiState(
    val selectedWeekNumber: Int,
    val availableWeekNumbers: List<Int>,
    val lastUpdatedDisplayText: String?,
    val raceCards: List<ScheduleRaceCardUi>,
    val panelMessage: PlannerDataUiMessage?,
    val isLoading: Boolean,
    val isEmpty: Boolean,
    val isCached: Boolean,
    val canSelectPreviousWeek: Boolean,
    val canSelectNextWeek: Boolean,
)
