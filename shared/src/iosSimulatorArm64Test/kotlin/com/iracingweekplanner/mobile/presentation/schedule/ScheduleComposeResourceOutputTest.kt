package com.iracingweekplanner.mobile.presentation.schedule

import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleComposeResourceOutputTest {

    @Test
    fun scheduleTextResourcesFormatEnglishTemplates() = runTest {
        assertEquals(
            "Schedule",
            getString(ScheduleTextResources.bottomTabResources().first().label),
        )
        assertEquals(
            "Week 13 Schedule",
            ScheduleTextResources.loadWeekTitle(weekNumber = 13),
        )
        assertEquals(
            "Last updated 10:42 AM",
            ScheduleTextResources.loadLastUpdated(time = "10:42 AM"),
        )
        assertEquals(
            "1 race",
            ScheduleTextResources.loadRaceCount(count = 1),
        )
        assertEquals(
            "12 races",
            ScheduleTextResources.loadRaceCount(count = 12),
        )
        assertEquals(
            "Cars unavailable",
            ScheduleTextResources.loadCarsUnavailable(),
        )
        assertEquals(
            "10 laps",
            ScheduleTextResources.loadLapCount(lapCount = 10),
        )
        assertEquals(
            "45 min",
            ScheduleTextResources.loadTimeLimitMinutes(minutes = 45),
        )
        assertEquals(
            "25% rain",
            ScheduleTextResources.loadRainChance(percent = 25),
        )
    }
}
