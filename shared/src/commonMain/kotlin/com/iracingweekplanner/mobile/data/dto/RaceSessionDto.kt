package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RaceSessionDto(
    val type: String,
    val firstSessionOffsetMinutes: Int? = null,
    val repeatEveryMinutes: Int? = null,
    val offsetMinutes: List<Int>? = null,
)
