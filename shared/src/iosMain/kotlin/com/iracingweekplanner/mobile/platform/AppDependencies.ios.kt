package com.iracingweekplanner.mobile.platform

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

fun createAppDependencies(hostedManifestUrl: String? = null): AppDependencies =
    createAppDependenciesWith(
        PlannerDataSourceConfig(hostedManifestUrl = hostedManifestUrl),
        iosPlannerDatabaseModule(),
        iosPlannerNetworkModule(),
    )

private fun iosPlannerDatabaseModule(): Module = module {
    single {
        PlannerDatabase(
            NativeSqliteDriver(
                schema = PlannerDatabase.Schema,
                name = PLANNER_DATABASE_NAME,
            ),
        )
    }
}

private fun iosPlannerNetworkModule(): Module = module {
    single { HttpClient(Darwin) }
}

private const val PLANNER_DATABASE_NAME = "planner.db"
