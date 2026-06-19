package com.iracingweekplanner.mobile.domain

class LoadPlannerCarsUseCase(
    private val repository: PlannerCarRepository,
) {
    suspend operator fun invoke(): List<PlannerCar> =
        repository.loadPlannerCars()
}
