package com.iracingweekplanner.mobile.presentation.schedule.model

data class ScheduleBottomTab(
    val label: String,
    val selected: Boolean,
    val enabled: Boolean,
    val iconLabel: String = label.take(1),
)
