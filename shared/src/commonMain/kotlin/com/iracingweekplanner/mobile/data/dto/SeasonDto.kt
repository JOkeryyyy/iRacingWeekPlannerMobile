package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SeasonDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val seasonId: String,
    val seasonName: String,
    val seasonStart: String,
    val seasonEnd: String,
    val weekSeasonStart: String,
    val weeks: List<SeasonWeekDto>,
    val series: List<SeriesDto>,
    val races: List<RaceDto>,
)
