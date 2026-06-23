package com.iracingweekplanner.mobile.data.repository

import com.iracingweekplanner.mobile.data.local.PlannerStoredPlannerData
import com.iracingweekplanner.mobile.data.mapper.mapLoaded
import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.repository.PlannerDataRepository

class RefreshCachePlannerDataRepository(
    private val coordinator: PlannerDataRefreshCoordinator,
) : PlannerDataRepository {
    override suspend fun loadPlannerData(): PlannerDataResult<PlannerData> =
        coordinator.loadPlannerData().mapLoaded { it.toPlannerData() }

    private fun PlannerStoredPlannerData.toPlannerData(): PlannerData =
        PlannerData(
            raceWeeks = season.weeks,
            plannerRaces = season.races,
            cars = cars,
            tracks = tracks,
        )
}
