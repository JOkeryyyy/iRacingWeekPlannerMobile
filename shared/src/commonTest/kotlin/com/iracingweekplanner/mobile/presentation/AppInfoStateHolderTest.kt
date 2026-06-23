package com.iracingweekplanner.mobile.presentation

import com.iracingweekplanner.mobile.domain.usecase.GetAppInfoUseCase
import com.iracingweekplanner.mobile.domain.model.PlannerAppInfo
import com.iracingweekplanner.mobile.domain.repository.PlannerAppInfoRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class AppInfoStateHolderTest {

    @Test
    fun exposesUiStateFromDomainUseCase() {
        val stateHolder = AppInfoStateHolder(
            getAppInfo = GetAppInfoUseCase(
                repository = object : PlannerAppInfoRepository {
                    override fun getAppInfo(): PlannerAppInfo =
                        PlannerAppInfo(name = "Test Planner", sourceSet = "test")
                },
            ),
        )

        assertEquals(
            AppInfoUiState(
                appName = "Test Planner",
                sourceSet = "test",
                statusMessage = "Test Planner shared module is ready",
            ),
            stateHolder.uiState,
        )
    }
}
