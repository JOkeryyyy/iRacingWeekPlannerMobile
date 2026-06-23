package com.iracingweekplanner.mobile.data.repository

import com.iracingweekplanner.mobile.data.mapper.mapLoaded
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerTrack
import com.iracingweekplanner.mobile.domain.repository.PlannerTrackRepository

class RefreshCachePlannerTrackRepository(
    private val coordinator: PlannerDataRefreshCoordinator,
) : PlannerTrackRepository {
    override suspend fun loadPlannerTracks(): PlannerDataResult<List<PlannerTrack>> =
        coordinator.loadPlannerData().mapLoaded { it.tracks }
}
