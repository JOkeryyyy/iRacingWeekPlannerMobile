package com.iracingweekplanner.mobile.domain

class LoadPlannerDataUseCase(
    private val repository: PlannerDataRepository,
) {
    suspend operator fun invoke(): PlannerDataResult<PlannerData> =
        repository.loadPlannerData()
}
