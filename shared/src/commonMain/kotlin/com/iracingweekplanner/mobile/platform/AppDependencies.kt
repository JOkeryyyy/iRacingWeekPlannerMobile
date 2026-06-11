package com.iracingweekplanner.mobile.platform

import com.iracingweekplanner.mobile.data.StaticPlannerAppInfoRepository
import com.iracingweekplanner.mobile.domain.GetAppInfoUseCase
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder

fun createAppInfoStateHolder(): AppInfoStateHolder =
    AppInfoStateHolder(
        getAppInfo = GetAppInfoUseCase(
            repository = StaticPlannerAppInfoRepository(),
        ),
    )
