package com.iracingweekplanner.mobile.presentation.schedule.model

data class ScheduleBottomTab(
    val label: String,
    val selected: Boolean,
    val enabled: Boolean,
    val iconLabel: String = label.take(1),
) {
    companion object {
        fun defaultTabs(): List<ScheduleBottomTab> =
            listOf(
                ScheduleBottomTab(label = "Schedule", selected = true, enabled = true),
                ScheduleBottomTab(label = "Filters", selected = false, enabled = false),
                ScheduleBottomTab(label = "Favorites", selected = false, enabled = false),
                ScheduleBottomTab(label = "Settings", selected = false, enabled = false),
            )
    }
}
