package com.iracingweekplanner.mobile.di

import com.iracingweekplanner.mobile.domain.repository.PlannerAppInfoRepository
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder
import com.iracingweekplanner.mobile.presentation.AppInfoUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.dsl.koinApplication

class CommonAppModuleTest {
    @Test
    fun resolvesAppInfoDependenciesThroughInterfaces() {
        val koinApplication = koinApplication {
            modules(commonAppModule)
        }

        val koin = koinApplication.koin
        val repository = koin.get<PlannerAppInfoRepository>()
        val stateHolder = koin.get<AppInfoStateHolder>()

        assertEquals("iRacing Week Planner Mobile", repository.getAppInfo().name)
        assertEquals(
            AppInfoUiState(
                appName = "iRacing Week Planner Mobile",
                sourceSet = "shared",
                statusMessage = "iRacing Week Planner Mobile shared module is ready",
            ),
            stateHolder.uiState,
        )
    }
}
