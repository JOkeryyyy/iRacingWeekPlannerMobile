package com.iracingweekplanner.mobile.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.presentation.PlannerDataAction
import com.iracingweekplanner.mobile.presentation.PlannerDataPresenter
import com.iracingweekplanner.mobile.presentation.PlannerDataUiMessage
import com.iracingweekplanner.mobile.presentation.PlannerDataUiState
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val plannerData: PlannerDataPresenter,
    private val currentWeekNumber: () -> Int? = { null },
) : ViewModel() {
    private val requestedWeekNumber = MutableStateFlow<Int?>(null)
    private val _state = MutableStateFlow(
        plannerData.uiState.value.toScheduleUiState(requestedWeek = null),
    )
    private var hasRequestedInitialLoad = false

    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            plannerData.uiState.collect { plannerState ->
                _state.value = plannerState.toScheduleUiState(requestedWeekNumber.value)
            }
        }
    }

    fun onAction(action: ScheduleAction) {
        when (action) {
            ScheduleAction.InitialLoad -> requestInitialLoad()
            ScheduleAction.Refresh,
            ScheduleAction.Retry,
            -> viewModelScope.launch {
                plannerData.onAction(PlannerDataAction.Retry)
            }
            ScheduleAction.PreviousWeek -> selectAdjacentWeek(offset = -1)
            ScheduleAction.NextWeek -> selectAdjacentWeek(offset = 1)
            ScheduleAction.Today -> selectCurrentWeek()
        }
    }

    private fun requestInitialLoad() {
        if (hasRequestedInitialLoad) return
        hasRequestedInitialLoad = true
        viewModelScope.launch {
            plannerData.onAction(PlannerDataAction.Load)
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
        _state.value = plannerData.uiState.value.toScheduleUiState(weekNumber)
    }

    private fun PlannerDataUiState.toScheduleUiState(requestedWeek: Int?): ScheduleUiState {
        val availableWeekNumbers = availableWeeks()
        val selectedWeekNumber = selectWeekNumber(
            availableWeekNumbers = availableWeekNumbers,
            requestedWeek = requestedWeek,
        )
        val raceCards = selectedWeekRaceCards(selectedWeekNumber)
        val isLoading = this is PlannerDataUiState.Loading || this is PlannerDataUiState.Idle
        val isError = this is PlannerDataUiState.Error

        return ScheduleUiState(
            selectedWeekNumber = selectedWeekNumber,
            availableWeekNumbers = availableWeekNumbers,
            lastUpdatedDisplayText = null,
            raceCards = raceCards,
            panelMessage = panelMessage(),
            isLoading = isLoading,
            isEmpty = !isLoading && !isError && raceCards.isEmpty(),
            isCached = isCached(),
            canSelectPreviousWeek = selectedWeekNumber != availableWeekNumbers.firstOrNull(),
            canSelectNextWeek = selectedWeekNumber != availableWeekNumbers.lastOrNull(),
        )
    }

    private fun PlannerDataUiState.availableWeeks(): List<Int> =
        when (this) {
            is PlannerDataUiState.Loaded -> raceWeeks
                .map { it.number.value }
                .distinct()
                .sorted()
            else -> emptyList()
        }

    private fun selectWeekNumber(
        availableWeekNumbers: List<Int>,
        requestedWeek: Int?,
    ): Int {
        if (availableWeekNumbers.isEmpty()) return requestedWeek ?: currentWeekNumber() ?: DefaultWeekNumber
        return when {
            requestedWeek in availableWeekNumbers -> requestedWeek
            currentWeekNumber() in availableWeekNumbers -> currentWeekNumber()
            else -> availableWeekNumbers.first()
        } ?: availableWeekNumbers.first()
    }

    private fun PlannerDataUiState.selectedWeekRaceCards(selectedWeekNumber: Int): List<ScheduleRaceCardUi> =
        when (this) {
            is PlannerDataUiState.Loaded -> plannerRaces
                .filter { it.weekNumber.value == selectedWeekNumber }
                .map { it.toRaceCardUi() }
            else -> emptyList()
        }

    private fun PlannerRace.toRaceCardUi(): ScheduleRaceCardUi {
        val trackName = listOfNotNull(track.name, track.configurationName)
            .joinToString(separator = " - ")
        return ScheduleRaceCardUi(
            raceId = id.value,
            title = seriesId.value,
            track = trackName,
            carSummary = carClasses.joinToString(separator = ", ").ifBlank { "Cars unavailable" },
            metadataText = metadataText(),
        )
    }

    private fun PlannerRace.metadataText(): String? =
        listOfNotNull(
            length?.lapCount?.let { "$it laps" },
            length?.timeLimitMinutes?.let { "$it min" },
            rainChance?.let { "${it.percentage.toInt()}% rain" },
        ).joinToString(separator = " - ").ifBlank { null }

    private fun PlannerDataUiState.isCached(): Boolean =
        when (this) {
            is PlannerDataUiState.Loaded -> isCached
            is PlannerDataUiState.Empty -> isCached
            else -> false
        }

    private fun PlannerDataUiState.panelMessage(): PlannerDataUiMessage? =
        when (this) {
            is PlannerDataUiState.Loaded -> message
            is PlannerDataUiState.Empty -> message
            is PlannerDataUiState.Error -> message
            else -> null
        }

    private companion object {
        const val DefaultWeekNumber = 13
    }
}
