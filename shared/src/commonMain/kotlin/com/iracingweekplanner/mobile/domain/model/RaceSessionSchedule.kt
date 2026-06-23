package com.iracingweekplanner.mobile.domain.model

import kotlin.time.Duration

sealed interface RaceSessionSchedule {
    data class Recurring(
        val firstSessionOffset: Duration,
        val repeatEvery: Duration,
    ) : RaceSessionSchedule

    data class SetTimes(
        val offsetsFromRaceStart: List<Duration>,
    ) : RaceSessionSchedule
}
