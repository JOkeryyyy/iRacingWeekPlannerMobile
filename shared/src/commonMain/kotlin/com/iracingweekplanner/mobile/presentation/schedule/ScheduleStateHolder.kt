package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.presentation.PlannerDataAction
import com.iracingweekplanner.mobile.presentation.PlannerDataPresenter
import com.iracingweekplanner.mobile.presentation.PlannerDataUiMessage
import com.iracingweekplanner.mobile.presentation.PlannerDataUiState
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleStatePanelContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScheduleAction {
    data object InitialLoad : ScheduleAction
    data object Refresh : ScheduleAction
    data object Retry : ScheduleAction
    data object PreviousWeek : ScheduleAction
    data object NextWeek : ScheduleAction
    data object Today : ScheduleAction
}

data class ScheduleState(
    val selectedWeekNumber: Int,
    val availableWeekNumbers: List<Int>,
    val scheduleTitle: String,
    val lastUpdatedDisplayText: String?,
    val raceCountText: String,
    val raceCards: List<ScheduleRaceCardUi>,
    val statePanel: ScheduleStatePanelContent?,
    val panelMessage: PlannerDataUiMessage?,
    val isLoading: Boolean,
    val isEmpty: Boolean,
    val isCached: Boolean,
    val canSelectPreviousWeek: Boolean,
    val canSelectNextWeek: Boolean,
)

data class ScheduleRaceCardUi(
    val raceId: String,
    val title: String,
    val track: String,
    val carSummary: String,
    val metadataText: String?,
)

class ScheduleStateHolder(
    private val plannerData: PlannerDataPresenter,
    scope: CoroutineScope,
    private val currentWeekNumber: () -> Int? = { null },
    private val textFormatter: ScheduleStateTextFormatter = ScheduleStateTextFormatter.Default,
) {
    private val requestedWeekNumber = MutableStateFlow<Int?>(null)
    private val _state = MutableStateFlow(
        plannerData.uiState.value.toScheduleState(requestedWeek = null),
    )
    private var hasRequestedInitialLoad = false

    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    init {
        scope.launch {
            plannerData.uiState.collect { plannerState ->
                _state.value = plannerState.toScheduleState(requestedWeekNumber.value)
            }
        }
    }

    suspend fun onAction(action: ScheduleAction) {
        when (action) {
            ScheduleAction.InitialLoad -> requestInitialLoad()
            ScheduleAction.Refresh,
            ScheduleAction.Retry,
            -> plannerData.onAction(PlannerDataAction.Retry)
            ScheduleAction.PreviousWeek -> selectAdjacentWeek(offset = -1)
            ScheduleAction.NextWeek -> selectAdjacentWeek(offset = 1)
            ScheduleAction.Today -> selectCurrentWeek()
        }
    }

    private suspend fun requestInitialLoad() {
        if (hasRequestedInitialLoad) return
        hasRequestedInitialLoad = true
        plannerData.onAction(PlannerDataAction.Load)
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
        _state.value = plannerData.uiState.value.toScheduleState(weekNumber)
    }

    private fun PlannerDataUiState.toScheduleState(requestedWeek: Int?): ScheduleState {
        val availableWeekNumbers = availableWeeks()
        val selectedWeekNumber = selectWeekNumber(
            availableWeekNumbers = availableWeekNumbers,
            requestedWeek = requestedWeek,
        )
        val raceCards = selectedWeekRaceCards(selectedWeekNumber)
        val statePanel = statePanelFor(raceCards)
        val isLoading = this is PlannerDataUiState.Loading || this is PlannerDataUiState.Idle
        val isError = this is PlannerDataUiState.Error

        return ScheduleState(
            selectedWeekNumber = selectedWeekNumber,
            availableWeekNumbers = availableWeekNumbers,
            scheduleTitle = textFormatter.weekTitle(selectedWeekNumber),
            lastUpdatedDisplayText = null,
            raceCountText = textFormatter.raceCount(raceCards.size),
            raceCards = raceCards,
            statePanel = statePanel,
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

    private fun PlannerDataUiState.statePanelFor(
        raceCards: List<ScheduleRaceCardUi>,
    ): ScheduleStatePanelContent? =
        when (this) {
            PlannerDataUiState.Idle,
            PlannerDataUiState.Loading,
            -> textFormatter.loadingPanel()
            is PlannerDataUiState.Empty -> message
                ?.let(textFormatter::statePanel)
                ?: textFormatter.emptyPanel()
            is PlannerDataUiState.Error -> textFormatter.statePanel(message)
            is PlannerDataUiState.Loaded -> when {
                message != null -> textFormatter.statePanel(message)
                raceCards.isEmpty() -> textFormatter.emptyPanel()
                else -> null
            }
        }

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

interface ScheduleStateTextFormatter {
    fun weekTitle(weekNumber: Int): String
    fun raceCount(count: Int): String
    fun loadingPanel(): ScheduleStatePanelContent
    fun emptyPanel(): ScheduleStatePanelContent
    fun statePanel(message: PlannerDataUiMessage): ScheduleStatePanelContent

    object Default : ScheduleStateTextFormatter {
        override fun weekTitle(weekNumber: Int): String = "Week $weekNumber Schedule"

        override fun raceCount(count: Int): String =
            if (count == 1) {
                "1 race"
            } else {
                "$count races"
            }

        override fun loadingPanel(): ScheduleStatePanelContent =
            ScheduleStatePanelContent.loading(
                title = "Loading schedule",
                message = "Preparing race week data.",
            )

        override fun emptyPanel(): ScheduleStatePanelContent =
            ScheduleStatePanelContent.empty(
                title = "No races this week",
                message = "Try another week or clear active filters.",
            )

        override fun statePanel(message: PlannerDataUiMessage): ScheduleStatePanelContent =
            when (message) {
                PlannerDataUiMessage.SHOWING_CACHED_PLANNER_DATA -> ScheduleStatePanelContent.empty(
                    title = "Showing cached schedule data",
                    message = "You can keep browsing while refreshed data is unavailable.",
                )
                PlannerDataUiMessage.PLANNER_DATA_UNAVAILABLE -> ScheduleStatePanelContent.error(
                    title = "Schedule unavailable",
                    message = "Retry when schedule data is available.",
                    retryLabel = "Retry",
                )
                PlannerDataUiMessage.INVALID_PLANNER_DATA -> ScheduleStatePanelContent.error(
                    title = "Schedule data needs an update",
                    message = "Retry to load a valid schedule.",
                    retryLabel = "Retry",
                )
                PlannerDataUiMessage.LOCAL_PLANNER_DATA_UNAVAILABLE -> ScheduleStatePanelContent.error(
                    title = "Saved schedule unavailable",
                    message = "Retry to reload schedule data.",
                    retryLabel = "Retry",
                )
            }
    }
}
