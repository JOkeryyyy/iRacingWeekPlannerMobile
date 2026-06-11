package com.iracingweekplanner.mobile.domain

class GetAppInfoUseCase(
    private val repository: PlannerAppInfoRepository,
) {
    operator fun invoke(): PlannerAppInfo = repository.getAppInfo()
}
