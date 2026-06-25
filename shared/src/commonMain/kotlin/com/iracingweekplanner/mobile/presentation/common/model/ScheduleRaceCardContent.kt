package com.iracingweekplanner.mobile.presentation.common.model

data class ScheduleRaceCardContent(
    val title: String,
    val track: String,
    val carSummary: String,
    val metadataText: String? = null,
    val chips: List<ScheduleChipContent> = emptyList(),
)
