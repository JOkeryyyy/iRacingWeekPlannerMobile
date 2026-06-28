package com.iracingweekplanner.mobile.di

import com.iracingweekplanner.mobile.data.datasource.ComposeResourcePlannerLocalDataSource
import com.iracingweekplanner.mobile.data.datasource.KtorPlannerHostedDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceFailure
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class PlannerDataSourceSelectionTest {

    @Test
    fun defaultPlannerSourceUsesBundledLocalMockResources() {
        val koinApplication = koinApplication {
            modules(plannerCommonAppModule())
        }

        try {
            assertIs<ComposeResourcePlannerLocalDataSource>(
                koinApplication.koin.get<PlannerDataSource>(),
            )
        } finally {
            koinApplication.close()
        }
    }

    @Test
    fun blankHostedManifestUrlUsesBundledLocalMockResources() {
        val koinApplication = koinApplication {
            modules(
                plannerCommonAppModule(
                    PlannerDataSourceConfig(hostedManifestUrl = "   "),
                ),
            )
        }

        try {
            assertIs<ComposeResourcePlannerLocalDataSource>(
                koinApplication.koin.get<PlannerDataSource>(),
            )
        } finally {
            koinApplication.close()
        }
    }

    @Test
    fun configuredHostedManifestUrlUsesHostedSourceWithKoinHttpClient() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    requestedUrls += request.url.toString()
                    respondError(HttpStatusCode.ServiceUnavailable)
                }
            }
        }
        val koinApplication = koinApplication {
            modules(
                plannerCommonAppModule(
                    PlannerDataSourceConfig(
                        hostedManifestUrl = "  $BaseManifestUrl  ",
                    ),
                ),
                module {
                    single { httpClient }
                },
            )
        }

        try {
            val source = koinApplication.koin.get<PlannerDataSource>()

            assertIs<KtorPlannerHostedDataSource>(source)
            val failure = assertFailure(source.loadPlannerData())
            assertEquals(BaseManifestUrl, requestedUrls.single())
            assertEquals(BaseManifestUrl, failure.path)
            assertEquals(
                PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE,
                failure.reason,
            )
        } finally {
            koinApplication.close()
            httpClient.close()
        }
    }

    private fun assertFailure(result: PlannerDataSourceResult): PlannerDataSourceFailure =
        when (result) {
            is PlannerDataSourceResult.Failure -> result.failure
            is PlannerDataSourceResult.Loaded -> fail("Expected hosted source failure, got $result")
        }

    private companion object {
        const val BaseManifestUrl =
            "https://example.supabase.co/storage/v1/object/public/planner-data/data/mobile/v1/manifest.json"
    }
}
