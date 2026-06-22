package com.iracingweekplanner.mobile.data

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ComposeResourcePlannerLocalDataSourceIosResourceTest {

    @Test
    fun defaultSourceLoadsMockFixtureDtosFromComposeResources() = runBlocking {
        val bundle = assertLoaded(ComposeResourcePlannerLocalDataSource().loadPlannerData())

        assertEquals("2026-s2", bundle.manifest.seasonId)
        assertEquals("2026 Season 2", bundle.season.seasonName)
        assertEquals(4, bundle.cars.cars.size)
        assertEquals(4, bundle.tracks.tracks.size)
    }

    private fun assertLoaded(result: PlannerDataSourceResult): PlannerDataBundle =
        when (result) {
            is PlannerDataSourceResult.Loaded -> result.bundle
            is PlannerDataSourceResult.Failure -> fail("Expected loaded data, got $result")
        }
}
