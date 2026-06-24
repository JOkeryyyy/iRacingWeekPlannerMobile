package com.iracingweekplanner.mobile.domain.model

data class PlannerData(
    val raceWeeks: List<RaceWeek>,
    val plannerRaces: List<PlannerRace>,
    val cars: List<PlannerCar>,
    val tracks: List<PlannerTrack>,
)
