package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.domain.model.RaceWeek

interface PlannerScheduleRepository {
    suspend fun loadRaceWeeks(): PlannerDataResult<List<RaceWeek>>
    suspend fun loadPlannerRaces(): PlannerDataResult<List<PlannerRace>>
}
