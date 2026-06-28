package com.iracingweekplanner.mobile.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.iracingweekplanner.mobile.data.datasource.ComposeResourcePlannerLocalDataSource
import com.iracingweekplanner.mobile.data.repository.PlannerDataRefreshCoordinator
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSource
import com.iracingweekplanner.mobile.data.local.PlannerLocalDataStore
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerCarRepository
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataCoordinator
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataRepository
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerScheduleRepository
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerTrackRepository
import com.iracingweekplanner.mobile.data.local.SqlDelightPlannerLocalDataStore
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerCarsUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerRacesUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerTracksUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadRaceWeeksUseCase
import com.iracingweekplanner.mobile.domain.repository.PlannerCarRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerDataRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerScheduleRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerTrackRepository
import com.iracingweekplanner.mobile.platform.createAppDependenciesWith
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class CommonPlannerDataModuleAndroidHostTest {

    @Test
    fun resolvesSprint2PlannerGraphThroughInterfaces() {
        val koinApplication = koinApplication {
            modules(commonAppModule, testPlannerDatabaseModule())
        }

        try {
            val koin = koinApplication.koin

            assertIs<ComposeResourcePlannerLocalDataSource>(koin.get<PlannerDataSource>())
            assertIs<SqlDelightPlannerLocalDataStore>(koin.get<PlannerLocalDataStore>())
            assertIs<RefreshCachePlannerDataCoordinator>(koin.get<PlannerDataRefreshCoordinator>())
            assertIs<RefreshCachePlannerScheduleRepository>(koin.get<PlannerScheduleRepository>())
            assertIs<RefreshCachePlannerCarRepository>(koin.get<PlannerCarRepository>())
            assertIs<RefreshCachePlannerTrackRepository>(koin.get<PlannerTrackRepository>())
            assertIs<RefreshCachePlannerDataRepository>(koin.get<PlannerDataRepository>())
            assertIs<LoadRaceWeeksUseCase>(koin.get<LoadRaceWeeksUseCase>())
            assertIs<LoadPlannerRacesUseCase>(koin.get<LoadPlannerRacesUseCase>())
            assertIs<LoadPlannerCarsUseCase>(koin.get<LoadPlannerCarsUseCase>())
            assertIs<LoadPlannerTracksUseCase>(koin.get<LoadPlannerTracksUseCase>())
            assertIs<LoadPlannerDataUseCase>(koin.get<LoadPlannerDataUseCase>())
        } finally {
            koinApplication.close()
        }
    }

    @Test
    fun defaultPlannerDataSourceUsesLocalMockResources() {
        val koinApplication = koinApplication {
            modules(commonAppModule, testPlannerDatabaseModule())
        }

        try {
            assertIs<ComposeResourcePlannerLocalDataSource>(koinApplication.koin.get<PlannerDataSource>())
        } finally {
            koinApplication.close()
        }
    }

    @Test
    fun publicDependenciesOwnerExposesAppInfoAndPlannerDataUseCaseUntilClosed() {
        val dependencies = createAppDependenciesWith(testPlannerDatabaseModule())

        try {
            val appInfoStateHolder = dependencies.appInfoStateHolder
            val loadPlannerData = dependencies.loadPlannerData

            assertIs<AppInfoStateHolder>(appInfoStateHolder)
            assertIs<LoadPlannerDataUseCase>(loadPlannerData)
            assertSame(appInfoStateHolder, dependencies.appInfoStateHolder)
            assertSame(loadPlannerData, dependencies.loadPlannerData)
        } finally {
            dependencies.close()
        }
    }

    private fun testPlannerDatabaseModule(): Module = module {
        single { createInMemoryPlannerDatabase() }
    }

    private fun createInMemoryPlannerDatabase(): PlannerDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PlannerDatabase.Schema.create(driver)
        return PlannerDatabase(driver)
    }
}
