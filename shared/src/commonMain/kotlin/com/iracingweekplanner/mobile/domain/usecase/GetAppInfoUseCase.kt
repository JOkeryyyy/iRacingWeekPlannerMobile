package com.iracingweekplanner.mobile.domain.usecase

import com.iracingweekplanner.mobile.domain.model.PlannerAppInfo
import com.iracingweekplanner.mobile.domain.repository.PlannerAppInfoRepository

class GetAppInfoUseCase(
    private val repository: PlannerAppInfoRepository,
) {
    operator fun invoke(): PlannerAppInfo = repository.getAppInfo()
}
