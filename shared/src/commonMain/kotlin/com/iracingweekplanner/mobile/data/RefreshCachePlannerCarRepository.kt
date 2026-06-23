package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.domain.PlannerCar
import com.iracingweekplanner.mobile.domain.PlannerCarRepository
import com.iracingweekplanner.mobile.domain.PlannerDataResult

class RefreshCachePlannerCarRepository(
    private val coordinator: PlannerDataRefreshCoordinator,
) : PlannerCarRepository {
    override suspend fun loadPlannerCars(): PlannerDataResult<List<PlannerCar>> =
        coordinator.loadPlannerData().mapLoaded { it.cars }
}
