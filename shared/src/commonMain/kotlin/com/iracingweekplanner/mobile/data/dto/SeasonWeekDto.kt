package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SeasonWeekDto(
    val weekNumber: Int,
    val startsAt: String,
    val endsAt: String,
)
