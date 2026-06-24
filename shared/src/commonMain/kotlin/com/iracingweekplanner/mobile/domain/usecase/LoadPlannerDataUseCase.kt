package com.iracingweekplanner.mobile.domain.usecase

import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.repository.PlannerDataRepository

class LoadPlannerDataUseCase(
    private val repository: PlannerDataRepository,
) {
    suspend operator fun invoke(): PlannerDataResult<PlannerData> =
        repository.loadPlannerData()
}
