package com.iracingweekplanner.mobile.presentation.schedule.model

data class ScheduleRaceCardContent(
    val title: String,
    val track: String,
    val carSummary: String,
    val metadata: List<String> = emptyList(),
    val chips: List<ScheduleChipContent> = emptyList(),
)
