package com.iracingweekplanner.mobile.presentation.common.model

sealed interface ScheduleRaceSessionTimingUi {
    data class Recurring(
        val firstSessionOffsetMinutes: Int,
        val repeatEveryMinutes: Int,
    ) : ScheduleRaceSessionTimingUi

    data class SetTimes(
        val offsetMinutes: List<Int>,
    ) : ScheduleRaceSessionTimingUi
}
