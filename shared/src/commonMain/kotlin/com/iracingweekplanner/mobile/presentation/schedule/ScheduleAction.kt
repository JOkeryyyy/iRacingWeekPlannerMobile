package com.iracingweekplanner.mobile.presentation.schedule

sealed interface ScheduleAction {
    data object InitialLoad : ScheduleAction
    data object Refresh : ScheduleAction
    data object Retry : ScheduleAction
    data object PreviousWeek : ScheduleAction
    data object NextWeek : ScheduleAction
    data object Today : ScheduleAction
}
