package com.iracingweekplanner.mobile.di

import com.iracingweekplanner.mobile.data.datasource.ComposeResourcePlannerLocalDataSource
import com.iracingweekplanner.mobile.data.datasource.KtorPlannerHostedDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.data.local.PlannerLocalDataStore
import com.iracingweekplanner.mobile.data.local.SqlDelightPlannerLocalDataStore
import com.iracingweekplanner.mobile.data.repository.PlannerDataRefreshCoordinator
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerCarRepository
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataCoordinator
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataRepository
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerScheduleRepository
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerTrackRepository
import com.iracingweekplanner.mobile.data.repository.StaticPlannerAppInfoRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerAppInfoRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerCarRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerDataRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerScheduleRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerTrackRepository
import com.iracingweekplanner.mobile.domain.usecase.GetAppInfoUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerCarsUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerRacesUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerTracksUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadRaceWeeksUseCase
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

val commonAppModule: Module = plannerCommonAppModule()

fun plannerCommonAppModule(
    plannerDataSourceConfig: PlannerDataSourceConfig = PlannerDataSourceConfig.LocalMock,
): Module = module {
    single<PlannerAppInfoRepository> { StaticPlannerAppInfoRepository() }
    factory { GetAppInfoUseCase(repository = get()) }
    factory { AppInfoStateHolder(getAppInfo = get()) }

    single {
        Json {
            ignoreUnknownKeys = false
        }
    }
    single<PlannerDataSource> {
        createPlannerDataSource(
            config = plannerDataSourceConfig,
            json = get(),
            httpClient = { get() },
        )
    }
    single<PlannerLocalDataStore> { SqlDelightPlannerLocalDataStore(database = get()) }
    single<PlannerDataRefreshCoordinator> {
        RefreshCachePlannerDataCoordinator(
            source = get(),
            localDataStore = get(),
        )
    }
    single<PlannerScheduleRepository> { RefreshCachePlannerScheduleRepository(coordinator = get()) }
    single<PlannerCarRepository> { RefreshCachePlannerCarRepository(coordinator = get()) }
    single<PlannerTrackRepository> { RefreshCachePlannerTrackRepository(coordinator = get()) }
    single<PlannerDataRepository> { RefreshCachePlannerDataRepository(coordinator = get()) }
    factory { LoadRaceWeeksUseCase(repository = get()) }
    factory { LoadPlannerRacesUseCase(repository = get()) }
    factory { LoadPlannerCarsUseCase(repository = get()) }
    factory { LoadPlannerTracksUseCase(repository = get()) }
    factory { LoadPlannerDataUseCase(repository = get()) }
}

private fun createPlannerDataSource(
    config: PlannerDataSourceConfig,
    json: Json,
    httpClient: () -> HttpClient,
): PlannerDataSource {
    val hostedManifestUrl = config.normalizedHostedManifestUrl
        ?: return ComposeResourcePlannerLocalDataSource(json = json)

    return KtorPlannerHostedDataSource(
        manifestUrl = hostedManifestUrl,
        httpClient = httpClient(),
        json = json,
    )
}
