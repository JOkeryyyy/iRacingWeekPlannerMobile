package com.iracingweekplanner.mobile.platform

import com.iracingweekplanner.mobile.di.commonAppModule
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder
import com.iracingweekplanner.mobile.presentation.PlannerDataStateHolder
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication

class AppDependencies internal constructor(
    private val koinApplication: KoinApplication,
) {
    val appInfoStateHolder: AppInfoStateHolder = koinApplication.koin.get()
    val plannerDataStateHolder: PlannerDataStateHolder = koinApplication.koin.get()

    fun close() {
        koinApplication.close()
    }
}

internal fun createAppDependenciesWith(vararg platformModules: Module): AppDependencies =
    AppDependencies(
        koinApplication = koinApplication {
            modules(listOf(commonAppModule) + platformModules)
        },
    )
