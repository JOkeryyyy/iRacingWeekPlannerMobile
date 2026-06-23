package com.iracingweekplanner.mobile.data.datasource

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ComposeResourcePlannerLocalDataSourceTest {

    @Test
    fun returnsFailureWhenAResourceCannotBeLoaded() = runBlocking {
        val source = ComposeResourcePlannerLocalDataSource(
            readText = { path ->
                if (path == "manifest.json") {
                    error("resource missing")
                } else {
                    "{}"
                }
            },
        )

        val error = assertFailure(source.loadPlannerData())
        assertEquals("manifest.json", error.path)
        assertEquals(PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE, error.reason)
    }

    @Test
    fun returnsFailureWhenAFixtureCannotBeDecoded() = runBlocking {
        val source = ComposeResourcePlannerLocalDataSource(
            readText = { path ->
                if (path == "manifest.json") {
                    "{"
                } else {
                    "{}"
                }
            },
        )

        val error = assertFailure(source.loadPlannerData())
        assertEquals("manifest.json", error.path)
        assertEquals(PlannerDataSourceFailure.Reason.DECODE_FAILED, error.reason)
    }

    private fun assertFailure(result: PlannerDataSourceResult): PlannerDataSourceFailure =
        when (result) {
            is PlannerDataSourceResult.Failure -> result.failure
            is PlannerDataSourceResult.Loaded -> fail("Expected data-source failure, got $result")
        }
}
