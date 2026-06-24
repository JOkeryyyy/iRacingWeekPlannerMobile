package com.iracingweekplanner.mobile.data.repository

import com.iracingweekplanner.mobile.data.mapper.mapLoaded
import com.iracingweekplanner.mobile.domain.model.PlannerCar
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.repository.PlannerCarRepository

class RefreshCachePlannerCarRepository(
    private val coordinator: PlannerDataRefreshCoordinator,
) : PlannerCarRepository {
    override suspend fun loadPlannerCars(): PlannerDataResult<List<PlannerCar>> =
        coordinator.loadPlannerData().mapLoaded { it.cars }
}
