package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataError
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.domain.model.RaceSessionSchedule
import com.iracingweekplanner.mobile.domain.model.SeriesId
import com.iracingweekplanner.mobile.domain.model.TimeWindow
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardUi
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceSessionTimingUi

internal fun initialScheduleUiState(currentWeekNumber: () -> Int?): ScheduleUiState {
    val selectedWeekNumber = currentWeekNumber() ?: DefaultWeekNumber
    return ScheduleUiState(
        selectedWeekNumber = selectedWeekNumber,
        availableWeekNumbers = emptyList(),
        dateContext = null,
        lastUpdatedDisplayText = null,
        raceCards = emptyList(),
        panelMessage = null,
        isLoading = true,
        isEmpty = false,
        isCached = false,
        canSelectPreviousWeek = false,
        canSelectCurrentWeek = false,
        canSelectNextWeek = false,
    )
}

internal fun ScheduleUiState.toScheduleLoadingState(requestedWeek: Int?): ScheduleUiState {
    val selectedWeekNumber = requestedWeek ?: this.selectedWeekNumber
    return copy(
        selectedWeekNumber = selectedWeekNumber,
        availableWeekNumbers = emptyList(),
        dateContext = null,
        raceCards = emptyList(),
        isLoading = true,
        isEmpty = false,
        panelMessage = null,
        isCached = false,
        canSelectPreviousWeek = false,
        canSelectCurrentWeek = false,
        canSelectNextWeek = false,
    )
}

internal fun PlannerData.toScheduleUiState(
    freshness: PlannerDataFreshness,
    requestedWeek: Int?,
    currentWeekNumber: () -> Int?,
): ScheduleUiState {
    val availableWeekNumbers = availableWeeks()
    val currentWeek = currentWeekNumber()
    val selectedWeekNumber = selectWeekNumber(
        availableWeekNumbers = availableWeekNumbers,
        requestedWeek = requestedWeek,
        currentWeek = currentWeek,
    )
    val selectedWeek = raceWeeks.firstOrNull { it.number.value == selectedWeekNumber }
    val seriesNamesById = series.associate { it.id to it.name }
    val raceCards = selectedWeekRaceCards(
        selectedWeekNumber = selectedWeekNumber,
        seriesNamesById = seriesNamesById,
    )
    val hasEmptySourceData = raceWeeks.isEmpty() || plannerRaces.isEmpty()

    return ScheduleUiState(
        selectedWeekNumber = selectedWeekNumber,
        availableWeekNumbers = availableWeekNumbers,
        dateContext = selectedWeek?.window?.toDateContext(),
        lastUpdatedDisplayText = null,
        raceCards = raceCards,
        panelMessage = freshness.cachedMessage(),
        isLoading = false,
        isEmpty = hasEmptySourceData || raceCards.isEmpty(),
        isCached = freshness == PlannerDataFreshness.CACHED,
        canSelectPreviousWeek = selectedWeekNumber != availableWeekNumbers.firstOrNull(),
        canSelectCurrentWeek = currentWeek != null && availableWeekNumbers.isNotEmpty(),
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
    currentWeek: Int?,
): Int {
    if (availableWeekNumbers.isEmpty()) return requestedWeek ?: currentWeek ?: DefaultWeekNumber
    return when {
        requestedWeek in availableWeekNumbers -> requestedWeek
        currentWeek in availableWeekNumbers -> currentWeek
        else -> availableWeekNumbers.first()
    } ?: availableWeekNumbers.first()
}

private fun PlannerData.selectedWeekRaceCards(
    selectedWeekNumber: Int,
    seriesNamesById: Map<SeriesId, String>,
): List<ScheduleRaceCardUi> =
    plannerRaces
        .filter { it.weekNumber.value == selectedWeekNumber }
        .map { it.toRaceCardUi(seriesNamesById) }

private fun PlannerRace.toRaceCardUi(seriesNamesById: Map<SeriesId, String>): ScheduleRaceCardUi {
    val trackName = listOfNotNull(track.name, track.configurationName)
        .joinToString(separator = " - ")
    val carSummary = carClasses
        .joinToString(separator = ", ")
        .ifBlank { null }

    return ScheduleRaceCardUi(
        raceId = id.value,
        title = seriesNamesById[seriesId] ?: seriesId.value,
        track = trackName,
        carSummary = carSummary,
        lapCount = length?.lapCount,
        timeLimitMinutes = length?.timeLimitMinutes,
        rainChancePercent = rainChance?.percentage?.toInt(),
        sessionTiming = sessions.firstOrNull()?.toSessionTimingUi(),
    )
}

private fun RaceSessionSchedule.toSessionTimingUi(): ScheduleRaceSessionTimingUi =
    when (this) {
        is RaceSessionSchedule.Recurring -> ScheduleRaceSessionTimingUi.Recurring(
            firstSessionOffsetMinutes = firstSessionOffset.inWholeMinutes.toInt(),
            repeatEveryMinutes = repeatEvery.inWholeMinutes.toInt(),
        )
        is RaceSessionSchedule.SetTimes -> ScheduleRaceSessionTimingUi.SetTimes(
            offsetMinutes = offsetsFromRaceStart.map { it.inWholeMinutes.toInt() },
        )
    }

private fun TimeWindow.toDateContext(): String =
    "${startsAt.toMonthDayString()} - ${endsAt.toMonthDayString()}"

private fun kotlin.time.Instant.toMonthDayString(): String {
    val date = toString().substringBefore('T')
    val month = date.substring(startIndex = 5, endIndex = 7).toIntOrNull()
    val day = date.substring(startIndex = 8, endIndex = 10).toIntOrNull()
    return if (month != null && month in 1..12 && day != null) {
        "${MonthLabels[month - 1]} $day"
    } else {
        date
    }
}

private val MonthLabels = listOf(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
)

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
