package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerTrack

interface PlannerTrackRepository {
    suspend fun loadPlannerTracks(): PlannerDataResult<List<PlannerTrack>>
}
