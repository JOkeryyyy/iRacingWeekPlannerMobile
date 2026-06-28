package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RaceDto(
    val raceId: String,
    val seriesId: String,
    val seriesName: String? = null,
    val category: String? = null,
    val weekNumber: Int,
    val startsAt: String,
    val endsAt: String,
    val trackPackageId: String,
    val trackName: String,
    val carSkus: List<String>,
    val carClasses: List<String>,
    val sessions: List<RaceSessionDto>,
    val trackConfigName: String? = null,
    val setupType: String? = null,
    val setupSource: String? = null,
    val startType: String? = null,
    val startTypeSource: String? = null,
    val raceLength: RaceLengthDto? = null,
    val precipChance: Double? = null,
)
