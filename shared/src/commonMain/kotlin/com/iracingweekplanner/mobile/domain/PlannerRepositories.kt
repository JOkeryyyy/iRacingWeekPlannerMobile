package com.iracingweekplanner.mobile.domain

interface PlannerScheduleRepository {
    suspend fun loadRaceWeeks(): PlannerDataResult<List<RaceWeek>>
    suspend fun loadPlannerRaces(): PlannerDataResult<List<PlannerRace>>
}

interface PlannerCarRepository {
    suspend fun loadPlannerCars(): PlannerDataResult<List<PlannerCar>>
}

interface PlannerTrackRepository {
    suspend fun loadPlannerTracks(): PlannerDataResult<List<PlannerTrack>>
}
