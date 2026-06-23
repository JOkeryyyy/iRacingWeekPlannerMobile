package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.domain.PlannerDataResult
import com.iracingweekplanner.mobile.domain.PlannerRace
import com.iracingweekplanner.mobile.domain.PlannerScheduleRepository
import com.iracingweekplanner.mobile.domain.RaceWeek

class RefreshCachePlannerScheduleRepository(
    private val coordinator: PlannerDataRefreshCoordinator,
) : PlannerScheduleRepository {
    override suspend fun loadRaceWeeks(): PlannerDataResult<List<RaceWeek>> =
        coordinator.loadPlannerData().mapLoaded { it.season.weeks }

    override suspend fun loadPlannerRaces(): PlannerDataResult<List<PlannerRace>> =
        coordinator.loadPlannerData().mapLoaded { it.season.races }
}
