package com.iracingweekplanner.mobile.presentation

import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataError
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlannerDataStateHolder(
    private val loadPlannerData: LoadPlannerDataUseCase,
) {
    private val _uiState = MutableStateFlow<PlannerDataUiState>(PlannerDataUiState.Idle)
    val uiState: StateFlow<PlannerDataUiState> = _uiState.asStateFlow()

    suspend fun onAction(action: PlannerDataAction) {
        when (action) {
            PlannerDataAction.Load,
            PlannerDataAction.Retry,
            -> load()
        }
    }

    private suspend fun load() {
        _uiState.value = PlannerDataUiState.Loading
        _uiState.value = loadPlannerData().toUiState()
    }

    private fun PlannerDataResult<PlannerData>.toUiState(): PlannerDataUiState =
        when (this) {
            is PlannerDataResult.Loaded -> data.toUiState(freshness)
            is PlannerDataResult.Failure -> PlannerDataUiState.Error(error.toUiMessage())
        }

    private fun PlannerData.toUiState(freshness: PlannerDataFreshness): PlannerDataUiState {
        val message = freshness.cachedMessage()
        return if (raceWeeks.isEmpty() || plannerRaces.isEmpty()) {
            PlannerDataUiState.Empty(
                freshness = freshness,
                message = message,
            )
        } else {
            PlannerDataUiState.Loaded(
                raceWeeks = raceWeeks,
                plannerRaces = plannerRaces,
                cars = cars,
                tracks = tracks,
                freshness = freshness,
                message = message,
            )
        }
    }

    private fun PlannerDataFreshness.cachedMessage(): PlannerDataUiMessage? =
        if (this == PlannerDataFreshness.CACHED) {
            PlannerDataUiMessage.SHOWING_CACHED_PLANNER_DATA
        } else {
            null
        }

    private fun PlannerDataError.toUiMessage(): PlannerDataUiMessage =
        when (this) {
            is PlannerDataError.InvalidSourceData -> PlannerDataUiMessage.INVALID_PLANNER_DATA
            is PlannerDataError.LocalStoreFailure -> PlannerDataUiMessage.LOCAL_PLANNER_DATA_UNAVAILABLE
            is PlannerDataError.SourceDecodeFailed,
            is PlannerDataError.SourceUnavailable,
            -> PlannerDataUiMessage.PLANNER_DATA_UNAVAILABLE
        }
}
