package com.iracingweekplanner.mobile.domain

class LoadPlannerCarsUseCase(
    private val repository: PlannerCarRepository,
) {
    suspend operator fun invoke(): PlannerDataResult<List<PlannerCar>> =
        repository.loadPlannerCars()
}
