package com.iracingweekplanner.mobile.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class StaticPlannerAppInfoRepositoryTest {

    @Test
    fun mapsStaticSharedAppInfoToDomainModel() {
        val info = StaticPlannerAppInfoRepository().getAppInfo()

        assertEquals("iRacing Week Planner Mobile", info.name)
        assertEquals("shared", info.sourceSet)
        assertEquals("iRacing Week Planner Mobile shared module is ready", info.statusMessage)
    }
}
