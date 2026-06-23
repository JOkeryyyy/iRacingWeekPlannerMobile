package com.iracingweekplanner.mobile.domain.usecase

import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerTrack
import com.iracingweekplanner.mobile.domain.repository.PlannerTrackRepository

class LoadPlannerTracksUseCase(
    private val repository: PlannerTrackRepository,
) {
    suspend operator fun invoke(): PlannerDataResult<List<PlannerTrack>> =
        repository.loadPlannerTracks()
}
