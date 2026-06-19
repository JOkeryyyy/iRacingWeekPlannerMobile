package com.iracingweekplanner.mobile.domain

import kotlin.jvm.JvmInline

@JvmInline
value class RaceId(val value: String)

data class PlannerRace(
    val id: RaceId,
    val seriesId: SeriesId,
    val weekNumber: RaceWeekNumber,
    val window: TimeWindow,
    val track: RaceTrackRef,
    val carIds: List<CarId>,
    val carClasses: List<String>,
    val sessions: List<RaceSessionSchedule>,
    val length: RaceLength? = null,
    val rainChance: RainChance? = null,
)

data class RaceTrackRef(
    val id: TrackId,
    val name: String,
    val configurationName: String? = null,
)

data class RaceLength(
    val lapCount: Int? = null,
    val timeLimitMinutes: Int? = null,
)

@JvmInline
value class RainChance(val percentage: Double)
