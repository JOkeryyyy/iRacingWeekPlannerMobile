package com.iracingweekplanner.mobile.presentation.common.model

data class ScheduleRaceCardUi(
    val raceId: String,
    val title: String,
    val track: String,
    val carSummary: String?,
    val lapCount: Int? = null,
    val timeLimitMinutes: Int? = null,
    val rainChancePercent: Int? = null,
    val metadataText: String? = null,
    val chips: List<ScheduleChipContent> = emptyList(),
)
