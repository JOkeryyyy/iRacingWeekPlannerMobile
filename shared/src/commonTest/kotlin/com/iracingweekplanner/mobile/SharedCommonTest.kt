package com.iracingweekplanner.mobile

import com.iracingweekplanner.mobile.data.repository.StaticPlannerAppInfoRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun sharedSmokeInfoIdentifiesTheMobilePlanner() {
        val info = StaticPlannerAppInfoRepository().getAppInfo()

        assertEquals("iRacing Week Planner Mobile", info.name)
        assertEquals("shared", info.sourceSet)
        assertEquals("iRacing Week Planner Mobile shared module is ready", info.statusMessage)
    }
}
