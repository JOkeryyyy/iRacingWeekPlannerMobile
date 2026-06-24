package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerCar
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult

interface PlannerCarRepository {
    suspend fun loadPlannerCars(): PlannerDataResult<List<PlannerCar>>
}
