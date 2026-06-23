package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.domain.PlannerData
import com.iracingweekplanner.mobile.domain.PlannerDataRepository
import com.iracingweekplanner.mobile.domain.PlannerDataResult

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
