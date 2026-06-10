package com.iracingweekplanner.mobile

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedLogicAndroidHostTest {

    @Test
    fun sharedSmokeInfoIsAvailableOnAndroidHost() {
        assertEquals(
            "iRacing Week Planner Mobile shared module is ready",
            SharedSmokeInfo().statusMessage(),
        )
    }
}
