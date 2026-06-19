package com.iracingweekplanner.mobile.domain

interface PlannerScheduleRepository {
    suspend fun loadRaceWeeks(): List<RaceWeek>
    suspend fun loadPlannerRaces(): List<PlannerRace>
}

interface PlannerCarRepository {
    suspend fun loadPlannerCars(): List<PlannerCar>
}

interface PlannerTrackRepository {
    suspend fun loadPlannerTracks(): List<PlannerTrack>
}
