package com.iracingweekplanner.mobile.domain.usecase

import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.domain.repository.PlannerScheduleRepository

class LoadPlannerRacesUseCase(
    private val repository: PlannerScheduleRepository,
) {
    suspend operator fun invoke(): PlannerDataResult<List<PlannerRace>> =
        repository.loadPlannerRaces()
}
