package com.iracingweekplanner.mobile.data.repository

import com.iracingweekplanner.mobile.data.mapper.mapLoaded
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.domain.model.RaceWeek
import com.iracingweekplanner.mobile.domain.repository.PlannerScheduleRepository

class RefreshCachePlannerScheduleRepository(
    private val coordinator: PlannerDataRefreshCoordinator,
) : PlannerScheduleRepository {
    override suspend fun loadRaceWeeks(): PlannerDataResult<List<RaceWeek>> =
        coordinator.loadPlannerData().mapLoaded { it.season.weeks }

    override suspend fun loadPlannerRaces(): PlannerDataResult<List<PlannerRace>> =
        coordinator.loadPlannerData().mapLoaded { it.season.races }
}
