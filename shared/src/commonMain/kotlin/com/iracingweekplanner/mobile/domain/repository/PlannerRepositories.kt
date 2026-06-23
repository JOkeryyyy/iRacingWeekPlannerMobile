package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerCar
import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.domain.model.PlannerTrack
import com.iracingweekplanner.mobile.domain.model.RaceWeek

interface PlannerScheduleRepository {
    suspend fun loadRaceWeeks(): PlannerDataResult<List<RaceWeek>>
    suspend fun loadPlannerRaces(): PlannerDataResult<List<PlannerRace>>
}

interface PlannerDataRepository {
    suspend fun loadPlannerData(): PlannerDataResult<PlannerData>
}

interface PlannerCarRepository {
    suspend fun loadPlannerCars(): PlannerDataResult<List<PlannerCar>>
}

interface PlannerTrackRepository {
    suspend fun loadPlannerTracks(): PlannerDataResult<List<PlannerTrack>>
}
