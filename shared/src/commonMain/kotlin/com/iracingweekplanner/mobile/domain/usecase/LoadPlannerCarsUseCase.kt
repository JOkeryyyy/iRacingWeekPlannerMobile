package com.iracingweekplanner.mobile.domain.usecase

import com.iracingweekplanner.mobile.domain.model.PlannerCar
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.repository.PlannerCarRepository

class LoadPlannerCarsUseCase(
    private val repository: PlannerCarRepository,
) {
    suspend operator fun invoke(): PlannerDataResult<List<PlannerCar>> =
        repository.loadPlannerCars()
}
