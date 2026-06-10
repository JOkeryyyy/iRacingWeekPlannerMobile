package com.iracingweekplanner.mobile

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun sharedSmokeInfoIdentifiesTheMobilePlanner() {
        val info = SharedSmokeInfo()

        assertEquals("iRacing Week Planner Mobile", info.appName)
        assertEquals("shared", info.sourceSet)
        assertEquals("iRacing Week Planner Mobile shared module is ready", info.statusMessage())
    }
}
