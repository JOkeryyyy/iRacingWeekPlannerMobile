package com.iracingweekplanner.mobile.domain

class LoadPlannerTracksUseCase(
    private val repository: PlannerTrackRepository,
) {
    suspend operator fun invoke(): List<PlannerTrack> =
        repository.loadPlannerTracks()
}
