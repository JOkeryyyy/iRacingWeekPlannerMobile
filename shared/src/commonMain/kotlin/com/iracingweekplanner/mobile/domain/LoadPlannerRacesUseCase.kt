package com.iracingweekplanner.mobile.domain

class LoadPlannerRacesUseCase(
    private val repository: PlannerScheduleRepository,
) {
    suspend operator fun invoke(): PlannerDataResult<List<PlannerRace>> =
        repository.loadPlannerRaces()
}
