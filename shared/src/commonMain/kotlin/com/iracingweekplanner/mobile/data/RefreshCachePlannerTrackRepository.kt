package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.domain.PlannerDataResult
import com.iracingweekplanner.mobile.domain.PlannerTrack
import com.iracingweekplanner.mobile.domain.PlannerTrackRepository

class RefreshCachePlannerTrackRepository(
    private val coordinator: PlannerDataRefreshCoordinator,
) : PlannerTrackRepository {
    override suspend fun loadPlannerTracks(): PlannerDataResult<List<PlannerTrack>> =
        coordinator.loadPlannerData().mapLoaded { it.tracks }
}
