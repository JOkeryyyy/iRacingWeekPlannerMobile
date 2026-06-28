package com.iracingweekplanner.mobile.platform

import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.di.plannerCommonAppModule
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication

class AppDependencies internal constructor(
    private val koinApplication: KoinApplication,
) {
    val appInfoStateHolder: AppInfoStateHolder = koinApplication.koin.get()
    val loadPlannerData: LoadPlannerDataUseCase = koinApplication.koin.get()

    fun close() {
        koinApplication.close()
    }
}

internal fun createAppDependenciesWith(vararg platformModules: Module): AppDependencies =
    createAppDependenciesWith(
        PlannerDataSourceConfig.LocalMock,
        *platformModules,
    )

internal fun createAppDependenciesWith(
    plannerDataSourceConfig: PlannerDataSourceConfig,
    vararg platformModules: Module,
): AppDependencies =
    AppDependencies(
        koinApplication = koinApplication {
            modules(listOf(plannerCommonAppModule(plannerDataSourceConfig)) + platformModules)
        },
    )
