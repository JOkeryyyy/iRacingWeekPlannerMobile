package com.iracingweekplanner.mobile.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class GetAppInfoUseCaseTest {

    @Test
    fun returnsPlannerAppInfoFromRepository() {
        val expectedInfo = PlannerAppInfo(
            name = "iRacing Week Planner Mobile",
            sourceSet = "shared",
        )
        val useCase = GetAppInfoUseCase(
            repository = object : PlannerAppInfoRepository {
                override fun getAppInfo(): PlannerAppInfo = expectedInfo
            },
        )

        assertEquals(expectedInfo, useCase())
    }
}
