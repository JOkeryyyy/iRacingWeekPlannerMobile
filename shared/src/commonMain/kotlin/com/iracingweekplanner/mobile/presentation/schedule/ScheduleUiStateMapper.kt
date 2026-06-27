package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataError
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardUi

internal fun initialScheduleUiState(currentWeekNumber: () -> Int?): ScheduleUiState {
    val selectedWeekNumber = currentWeekNumber() ?: DefaultWeekNumber
    return ScheduleUiState(
        selectedWeekNumber = selectedWeekNumber,
        availableWeekNumbers = emptyList(),
        lastUpdatedDisplayText = null,
        raceCards = emptyList(),
        panelMessage = null,
        isLoading = true,
        isEmpty = false,
        isCached = false,
        canSelectPreviousWeek = false,
        canSelectNextWeek = false,
    )
}

internal fun ScheduleUiState.toScheduleLoadingState(requestedWeek: Int?): ScheduleUiState {
    val selectedWeekNumber = requestedWeek ?: this.selectedWeekNumber
    return copy(
        selectedWeekNumber = selectedWeekNumber,
        availableWeekNumbers = emptyList(),
        raceCards = emptyList(),
        isLoading = true,
        isEmpty = false,
        panelMessage = null,
        isCached = false,
        canSelectPreviousWeek = false,
        canSelectNextWeek = false,
    )
}

internal fun PlannerData.toScheduleUiState(
    freshness: PlannerDataFreshness,
    requestedWeek: Int?,
    currentWeekNumber: () -> Int?,
): ScheduleUiState {
    val availableWeekNumbers = availableWeeks()
    val selectedWeekNumber = selectWeekNumber(
        availableWeekNumbers = availableWeekNumbers,
        requestedWeek = requestedWeek,
        currentWeekNumber = currentWeekNumber,
    )
    val raceCards = selectedWeekRaceCards(selectedWeekNumber)
    val hasEmptySourceData = raceWeeks.isEmpty() || plannerRaces.isEmpty()

    return ScheduleUiState(
        selectedWeekNumber = selectedWeekNumber,
        availableWeekNumbers = availableWeekNumbers,
        lastUpdatedDisplayText = null,
        raceCards = raceCards,
        panelMessage = freshness.cachedMessage(),
        isLoading = false,
        isEmpty = hasEmptySourceData || raceCards.isEmpty(),
        isCached = freshness == PlannerDataFreshness.CACHED,
        canSelectPreviousWeek = selectedWeekNumber != availableWeekNumbers.firstOrNull(),
        canSelectNextWeek = selectedWeekNumber != availableWeekNumbers.lastOrNull(),
    )
}

internal fun PlannerDataError.toScheduleErrorState(currentWeekNumber: () -> Int?): ScheduleUiState =
    initialScheduleUiState(currentWeekNumber).copy(
        isLoading = false,
        panelMessage = toUiMessage(),
    )

private fun PlannerData.availableWeeks(): List<Int> =
    raceWeeks
        .map { it.number.value }
        .distinct()
        .sorted()

private fun selectWeekNumber(
    availableWeekNumbers: List<Int>,
    requestedWeek: Int?,
    currentWeekNumber: () -> Int?,
): Int {
    if (availableWeekNumbers.isEmpty()) return requestedWeek ?: currentWeekNumber() ?: DefaultWeekNumber
    return when {
        requestedWeek in availableWeekNumbers -> requestedWeek
        currentWeekNumber() in availableWeekNumbers -> currentWeekNumber()
        else -> availableWeekNumbers.first()
    } ?: availableWeekNumbers.first()
}

private fun PlannerData.selectedWeekRaceCards(selectedWeekNumber: Int): List<ScheduleRaceCardUi> =
    plannerRaces
        .filter { it.weekNumber.value == selectedWeekNumber }
        .map { it.toRaceCardUi() }

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

private fun PlannerDataFreshness.cachedMessage(): ScheduleUiMessage? =
    if (this == PlannerDataFreshness.CACHED) {
        ScheduleUiMessage.ShowingCachedPlannerData
    } else {
        null
    }

private fun PlannerDataError.toUiMessage(): ScheduleUiMessage =
    when (this) {
        is PlannerDataError.InvalidSourceData -> ScheduleUiMessage.InvalidPlannerData
        is PlannerDataError.LocalStoreFailure -> ScheduleUiMessage.LocalPlannerDataUnavailable
        is PlannerDataError.SourceDecodeFailed,
        is PlannerDataError.SourceUnavailable,
        -> ScheduleUiMessage.PlannerDataUnavailable
    }

private const val DefaultWeekNumber = 13
