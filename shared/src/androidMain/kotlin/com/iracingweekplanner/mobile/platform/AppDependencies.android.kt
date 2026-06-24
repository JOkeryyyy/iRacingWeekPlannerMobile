package com.iracingweekplanner.mobile.platform

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

fun createAppDependencies(context: Context): AppDependencies =
    createAppDependenciesWith(androidPlannerDatabaseModule(context.applicationContext))

private fun androidPlannerDatabaseModule(context: Context): Module = module {
    single {
        PlannerDatabase(
            AndroidSqliteDriver(
                schema = PlannerDatabase.Schema,
                context = context,
                name = PLANNER_DATABASE_NAME,
            ),
        )
    }
}

private const val PLANNER_DATABASE_NAME = "planner.db"
