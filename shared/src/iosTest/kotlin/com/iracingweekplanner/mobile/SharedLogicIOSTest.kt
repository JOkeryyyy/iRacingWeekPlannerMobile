package com.iracingweekplanner.mobile

import com.iracingweekplanner.mobile.data.repository.StaticPlannerAppInfoRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedLogicIOSTest {

    @Test
    fun sharedSmokeInfoIsAvailableOnIos() {
        assertEquals(
            "iRacing Week Planner Mobile shared module is ready",
            StaticPlannerAppInfoRepository().getAppInfo().statusMessage,
        )
    }
}
