package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RaceLengthDto(
    val laps: Int? = null,
    val minutes: Int? = null,
)
