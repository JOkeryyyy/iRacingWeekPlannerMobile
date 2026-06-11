package com.iracingweekplanner.mobile.di

import com.iracingweekplanner.mobile.data.StaticPlannerAppInfoRepository
import com.iracingweekplanner.mobile.domain.GetAppInfoUseCase
import com.iracingweekplanner.mobile.domain.PlannerAppInfoRepository
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder
import org.koin.core.module.Module
import org.koin.dsl.module

val commonAppModule: Module = module {
    single<PlannerAppInfoRepository> { StaticPlannerAppInfoRepository() }
    factory { GetAppInfoUseCase(repository = get()) }
    factory { AppInfoStateHolder(getAppInfo = get()) }
}
