package com.iracingweekplanner.mobile.platform

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

fun createAppDependencies(context: Context): AppDependencies =
    createAppDependenciesWith(
        PlannerDataSourceConfig(
            hostedManifestUrl = context.hostedPlannerManifestUrl(),
        ),
        androidPlannerDatabaseModule(context.applicationContext),
        androidPlannerNetworkModule(),
    )

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

private fun androidPlannerNetworkModule(): Module = module {
    single { HttpClient(OkHttp) }
}

private fun Context.hostedPlannerManifestUrl(): String? {
    val applicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getApplicationInfo(
            packageName,
            PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    }

    return applicationInfo.metaData
        ?.getString(HOSTED_MANIFEST_URL_METADATA)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

private const val PLANNER_DATABASE_NAME = "planner.db"
private const val HOSTED_MANIFEST_URL_METADATA =
    "com.iracingweekplanner.mobile.HOSTED_MANIFEST_URL"
