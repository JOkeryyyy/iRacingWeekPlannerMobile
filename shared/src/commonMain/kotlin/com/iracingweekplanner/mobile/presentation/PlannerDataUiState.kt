package com.iracingweekplanner.mobile.presentation

import com.iracingweekplanner.mobile.domain.model.PlannerCar
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.domain.model.PlannerTrack
import com.iracingweekplanner.mobile.domain.model.RaceWeek

sealed interface PlannerDataAction {
    data object Load : PlannerDataAction
    data object Retry : PlannerDataAction
}

enum class PlannerDataUiMessage {
    SHOWING_CACHED_PLANNER_DATA,
    PLANNER_DATA_UNAVAILABLE,
    INVALID_PLANNER_DATA,
    LOCAL_PLANNER_DATA_UNAVAILABLE,
}

sealed interface PlannerDataUiState {
    data object Idle : PlannerDataUiState
    data object Loading : PlannerDataUiState

    data class Loaded(
        val raceWeeks: List<RaceWeek>,
        val plannerRaces: List<PlannerRace>,
        val cars: List<PlannerCar>,
        val tracks: List<PlannerTrack>,
        val freshness: PlannerDataFreshness,
        val message: PlannerDataUiMessage? = null,
    ) : PlannerDataUiState {
        val isCached: Boolean = freshness == PlannerDataFreshness.CACHED
    }

    data class Empty(
        val freshness: PlannerDataFreshness,
        val message: PlannerDataUiMessage? = null,
    ) : PlannerDataUiState {
        val isCached: Boolean = freshness == PlannerDataFreshness.CACHED
    }

    data class Error(
        val message: PlannerDataUiMessage,
    ) : PlannerDataUiState
}
