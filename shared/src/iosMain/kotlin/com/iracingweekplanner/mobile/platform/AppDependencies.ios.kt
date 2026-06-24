package com.iracingweekplanner.mobile.platform

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

fun createAppDependencies(): AppDependencies =
    createAppDependenciesWith(iosPlannerDatabaseModule())

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

private const val PLANNER_DATABASE_NAME = "planner.db"
