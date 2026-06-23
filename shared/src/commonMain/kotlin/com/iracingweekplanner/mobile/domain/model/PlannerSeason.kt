package com.iracingweekplanner.mobile.domain.model

import kotlin.jvm.JvmInline
import kotlin.time.Instant

@JvmInline
value class SeasonId(val value: String)

@JvmInline
value class RaceWeekNumber(val value: Int)

data class TimeWindow(
    val startsAt: Instant,
    val endsAt: Instant,
)

data class PlannerSeason(
    val id: SeasonId,
    val name: String,
    val window: TimeWindow,
    val weekCalculationStartsAt: Instant,
    val weeks: List<RaceWeek>,
    val series: List<PlannerSeries>,
    val races: List<PlannerRace>,
)

data class RaceWeek(
    val number: RaceWeekNumber,
    val window: TimeWindow,
)
