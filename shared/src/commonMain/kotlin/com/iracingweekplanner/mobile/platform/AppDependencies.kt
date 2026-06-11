package com.iracingweekplanner.mobile.platform

import com.iracingweekplanner.mobile.di.commonAppModule
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

class AppDependencies internal constructor(
    private val koinApplication: KoinApplication,
) {
    val appInfoStateHolder: AppInfoStateHolder = koinApplication.koin.get()

    fun close() {
        koinApplication.close()
    }
}

fun createAppDependencies(): AppDependencies =
    AppDependencies(
        koinApplication = koinApplication {
            modules(commonAppModule)
        },
    )
