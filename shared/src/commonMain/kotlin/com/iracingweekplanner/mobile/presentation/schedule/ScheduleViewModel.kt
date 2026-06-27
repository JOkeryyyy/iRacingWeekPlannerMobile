package com.iracingweekplanner.mobile.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val loadPlannerData: LoadPlannerDataUseCase,
    private val currentWeekNumber: () -> Int? = { null },
) : ViewModel() {
    private val requestedWeekNumber = MutableStateFlow<Int?>(null)
    private val _state = MutableStateFlow(initialScheduleUiState(currentWeekNumber))
    private var latestPlannerData: PlannerData? = null
    private var latestFreshness: PlannerDataFreshness? = null
    private var hasRequestedInitialLoad = false

    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    fun onAction(action: ScheduleAction) {
        when (action) {
            ScheduleAction.InitialLoad -> requestInitialLoad()
            ScheduleAction.Refresh,
            ScheduleAction.Retry,
            -> load()
            ScheduleAction.PreviousWeek -> selectAdjacentWeek(offset = -1)
            ScheduleAction.NextWeek -> selectAdjacentWeek(offset = 1)
            ScheduleAction.Today -> selectCurrentWeek()
        }
    }

    private fun requestInitialLoad() {
        if (hasRequestedInitialLoad) return
        hasRequestedInitialLoad = true
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.toScheduleLoadingState(requestedWeek = requestedWeekNumber.value)
            }
            when (val result = loadPlannerData()) {
                is PlannerDataResult.Loaded -> {
                    latestPlannerData = result.data
                    latestFreshness = result.freshness
                    _state.value = result.data.toScheduleUiState(
                        freshness = result.freshness,
                        requestedWeek = requestedWeekNumber.value,
                        currentWeekNumber = currentWeekNumber,
                    )
                }
                is PlannerDataResult.Failure -> {
                    latestPlannerData = null
                    latestFreshness = null
                    _state.value = result.error.toScheduleErrorState(currentWeekNumber)
                }
            }
        }
    }

    private fun selectAdjacentWeek(offset: Int) {
        val currentState = state.value
        val currentIndex = currentState.availableWeekNumbers.indexOf(currentState.selectedWeekNumber)
        if (currentIndex == -1) return

        val targetIndex = (currentIndex + offset)
            .coerceIn(0, currentState.availableWeekNumbers.lastIndex)
        updateSelectedWeek(currentState.availableWeekNumbers[targetIndex])
    }

    private fun selectCurrentWeek() {
        val currentWeek = currentWeekNumber() ?: return
        val currentState = state.value
        val targetWeek = currentState.availableWeekNumbers.minByOrNull { weekNumber ->
            kotlin.math.abs(weekNumber - currentWeek)
        } ?: return

        updateSelectedWeek(targetWeek)
    }

    private fun updateSelectedWeek(weekNumber: Int) {
        requestedWeekNumber.value = weekNumber
        val plannerData = latestPlannerData ?: return
        val freshness = latestFreshness ?: return
        _state.value = plannerData.toScheduleUiState(
            freshness = freshness,
            requestedWeek = weekNumber,
            currentWeekNumber = currentWeekNumber,
        )
    }
}
