package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.data.dto.CarDto
import com.iracingweekplanner.mobile.data.dto.CarsCatalogDto
import com.iracingweekplanner.mobile.data.dto.LicenseDto
import com.iracingweekplanner.mobile.data.dto.MobileDataManifestDto
import com.iracingweekplanner.mobile.data.dto.RaceDto
import com.iracingweekplanner.mobile.data.dto.RaceLengthDto
import com.iracingweekplanner.mobile.data.dto.RaceSessionDto
import com.iracingweekplanner.mobile.data.dto.SeasonDto
import com.iracingweekplanner.mobile.data.dto.SeasonWeekDto
import com.iracingweekplanner.mobile.data.dto.SeriesDto
import com.iracingweekplanner.mobile.data.dto.TrackDto
import com.iracingweekplanner.mobile.data.dto.TracksCatalogDto
import com.iracingweekplanner.mobile.domain.PlannerDataError
import com.iracingweekplanner.mobile.domain.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.PlannerDataResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RefreshCachePlannerDataCoordinatorTest {

    @Test
    fun successfulSourceLoadMapsSavesAndReturnsFreshDataset() = runBlocking {
        val store = FakePlannerLocalDataStore()
        val coordinator = RefreshCachePlannerDataCoordinator(
            source = FakePlannerDataSource(PlannerDataSourceResult.Loaded(sampleBundle(revision = "fresh"))),
            localDataStore = store,
        )

        val loaded = assertLoaded(coordinator.loadPlannerData())

        assertEquals(PlannerDataFreshness.FRESH, loaded.freshness)
        assertEquals("fresh", loaded.data.metadata.revision)
        assertEquals("fresh", store.savedDatasets.single().metadata.revision)
        assertEquals(0, store.readCount)
    }

    @Test
    fun sourceResourceFailureReturnsCachedDatasetWhenLocalStoreHasData() = runBlocking {
        val cached = sampleStoredPlannerData(revision = "cached")
        val store = FakePlannerLocalDataStore(
            readResult = PlannerLocalDataReadResult.Hit(cached),
        )
        val coordinator = RefreshCachePlannerDataCoordinator(
            source = FakePlannerDataSource(sourceFailure(PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE)),
            localDataStore = store,
        )

        val loaded = assertLoaded(coordinator.loadPlannerData())

        assertEquals(PlannerDataFreshness.CACHED, loaded.freshness)
        assertEquals("cached", loaded.data.metadata.revision)
        assertEquals(1, store.readCount)
        assertEquals(0, store.savedDatasets.size)
    }

    @Test
    fun sourceResourceFailureReturnsSourceUnavailableWhenThereIsNoCachedDataset() = runBlocking {
        val coordinator = RefreshCachePlannerDataCoordinator(
            source = FakePlannerDataSource(sourceFailure(PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE)),
            localDataStore = FakePlannerLocalDataStore(readResult = PlannerLocalDataReadResult.Miss),
        )

        val failure = assertFailure(coordinator.loadPlannerData())

        assertEquals(
            PlannerDataError.SourceUnavailable(
                path = "manifest.json",
                detail = "Source resource is unavailable",
            ),
            failure.error,
        )
    }

    @Test
    fun sourceDecodeFailureReturnsDecodeErrorWhenThereIsNoCachedDataset() = runBlocking {
        val coordinator = RefreshCachePlannerDataCoordinator(
            source = FakePlannerDataSource(sourceFailure(PlannerDataSourceFailure.Reason.DECODE_FAILED)),
            localDataStore = FakePlannerLocalDataStore(readResult = PlannerLocalDataReadResult.Miss),
        )

        val failure = assertFailure(coordinator.loadPlannerData())

        assertEquals(
            PlannerDataError.SourceDecodeFailed(
                path = "manifest.json",
                detail = "Source data could not be decoded",
            ),
            failure.error,
        )
    }

    @Test
    fun invalidSourceDataFallsBackToCachedDatasetAndDoesNotOverwriteCache() = runBlocking {
        val store = FakePlannerLocalDataStore(
            readResult = PlannerLocalDataReadResult.Hit(sampleStoredPlannerData(revision = "cached")),
        )
        val coordinator = RefreshCachePlannerDataCoordinator(
            source = FakePlannerDataSource(
                PlannerDataSourceResult.Loaded(
                    sampleBundle(
                        revision = "invalid",
                        seasonStart = "not-a-timestamp",
                    ),
                ),
            ),
            localDataStore = store,
        )

        val loaded = assertLoaded(coordinator.loadPlannerData())

        assertEquals(PlannerDataFreshness.CACHED, loaded.freshness)
        assertEquals("cached", loaded.data.metadata.revision)
        assertEquals(0, store.savedDatasets.size)
    }

    @Test
    fun invalidSourceDataWithoutCacheReturnsInvalidSourceDataFailure() = runBlocking {
        val coordinator = RefreshCachePlannerDataCoordinator(
            source = FakePlannerDataSource(
                PlannerDataSourceResult.Loaded(
                    sampleBundle(
                        revision = "invalid",
                        seasonStart = "not-a-timestamp",
                    ),
                ),
            ),
            localDataStore = FakePlannerLocalDataStore(readResult = PlannerLocalDataReadResult.Miss),
        )

        val failure = assertFailure(coordinator.loadPlannerData())

        assertEquals(
            PlannerDataError.InvalidSourceData(
                path = "seasonStart",
                detail = "Invalid timestamp",
            ),
            failure.error,
        )
    }

    @Test
    fun localStoreReadFailureReturnsDomainSafeLocalStoreFailure() = runBlocking {
        val coordinator = RefreshCachePlannerDataCoordinator(
            source = FakePlannerDataSource(sourceFailure(PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE)),
            localDataStore = FakePlannerLocalDataStore(readResult = PlannerLocalDataReadResult.Failure),
        )

        val failure = assertFailure(coordinator.loadPlannerData())

        assertEquals(
            PlannerDataError.LocalStoreFailure(
                operation = PlannerDataError.LocalStoreOperation.READ,
            ),
            failure.error,
        )
    }

    @Test
    fun localStoreWriteFailureReturnsDomainSafeLocalStoreFailureWhenNoCacheCanBeRead() = runBlocking {
        val coordinator = RefreshCachePlannerDataCoordinator(
            source = FakePlannerDataSource(PlannerDataSourceResult.Loaded(sampleBundle(revision = "fresh"))),
            localDataStore = FakePlannerLocalDataStore(
                readResult = PlannerLocalDataReadResult.Miss,
                writeResult = PlannerLocalDataWriteResult.Failure,
            ),
        )

        val failure = assertFailure(coordinator.loadPlannerData())

        assertEquals(
            PlannerDataError.LocalStoreFailure(
                operation = PlannerDataError.LocalStoreOperation.WRITE,
            ),
            failure.error,
        )
    }

    @Test
    fun repositoryImplementationsProjectScheduleCarsAndTracksFromLoadedPlannerDataset() = runBlocking {
        val dataset = sampleStoredPlannerData(revision = "cached")
        val coordinator = FakePlannerDataRefreshCoordinator(
            PlannerDataResult.Loaded(dataset, PlannerDataFreshness.CACHED),
        )

        val weeks = assertLoaded(RefreshCachePlannerScheduleRepository(coordinator).loadRaceWeeks())
        val races = assertLoaded(RefreshCachePlannerScheduleRepository(coordinator).loadPlannerRaces())
        val cars = assertLoaded(RefreshCachePlannerCarRepository(coordinator).loadPlannerCars())
        val tracks = assertLoaded(RefreshCachePlannerTrackRepository(coordinator).loadPlannerTracks())

        assertEquals(PlannerDataFreshness.CACHED, weeks.freshness)
        assertEquals(dataset.season.weeks, weeks.data)
        assertEquals(dataset.season.races, races.data)
        assertEquals(dataset.cars, cars.data)
        assertEquals(dataset.tracks, tracks.data)
    }

    private class FakePlannerDataSource(
        private val result: PlannerDataSourceResult,
    ) : PlannerDataSource {
        override suspend fun loadPlannerData(): PlannerDataSourceResult = result
    }

    private class FakePlannerLocalDataStore(
        private val readResult: PlannerLocalDataReadResult = PlannerLocalDataReadResult.Miss,
        private val writeResult: PlannerLocalDataWriteResult = PlannerLocalDataWriteResult.Saved,
    ) : PlannerLocalDataStore {
        val savedDatasets = mutableListOf<PlannerStoredPlannerData>()
        var readCount = 0
            private set

        override suspend fun read(): PlannerLocalDataReadResult {
            readCount += 1
            return readResult
        }

        override suspend fun replace(dataset: PlannerStoredPlannerData): PlannerLocalDataWriteResult {
            if (writeResult is PlannerLocalDataWriteResult.Saved) {
                savedDatasets += dataset
            }
            return writeResult
        }
    }

    private class FakePlannerDataRefreshCoordinator(
        private val result: PlannerDataResult<PlannerStoredPlannerData>,
    ) : PlannerDataRefreshCoordinator {
        override suspend fun loadPlannerData(): PlannerDataResult<PlannerStoredPlannerData> = result
    }

    private fun sourceFailure(
        reason: PlannerDataSourceFailure.Reason,
    ): PlannerDataSourceResult.Failure =
        PlannerDataSourceResult.Failure(
            PlannerDataSourceFailure(
                path = "manifest.json",
                reason = reason,
                detail = "raw detail should not leak",
            ),
        )

    private fun sampleStoredPlannerData(revision: String): PlannerStoredPlannerData =
        assertLoaded(sampleBundle(revision = revision).toStoredPlannerData()).data

    private fun <T> assertLoaded(result: PlannerDataResult<T>): PlannerDataResult.Loaded<T> =
        when (result) {
            is PlannerDataResult.Loaded -> result
            is PlannerDataResult.Failure -> fail("Expected loaded data, got $result")
        }

    private fun assertFailure(result: PlannerDataResult<*>): PlannerDataResult.Failure =
        when (result) {
            is PlannerDataResult.Failure -> result
            is PlannerDataResult.Loaded -> fail("Expected failure, got $result")
        }

    private fun sampleBundle(
        revision: String,
        seasonStart: String = "2026-03-17T00:00:00Z",
    ): PlannerDataBundle =
        PlannerDataBundle(
            manifest = MobileDataManifestDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                seasonId = "2026-s2",
                seasonFile = "season.json",
                carsFile = "cars.json",
                tracksFile = "tracks.json",
                revision = revision,
            ),
            season = SeasonDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                seasonId = "2026-s2",
                seasonName = "2026 Season 2",
                seasonStart = seasonStart,
                seasonEnd = "2026-06-09T00:00:00Z",
                weekSeasonStart = "2026-03-17T00:00:00Z",
                weeks = listOf(
                    SeasonWeekDto(
                        weekNumber = 1,
                        startsAt = "2026-03-17T00:00:00Z",
                        endsAt = "2026-03-24T00:00:00Z",
                    ),
                ),
                series = listOf(
                    SeriesDto(
                        seriesId = "series-global-mazda-mx5",
                        name = "Global Mazda MX-5 Cup",
                        category = "Sports Car",
                        license = LicenseDto(className = "Rookie", level = 1),
                        isOfficial = true,
                        isFixedSetup = true,
                    ),
                ),
                races = listOf(
                    RaceDto(
                        raceId = "race-global-mazda-week-1",
                        seriesId = "series-global-mazda-mx5",
                        weekNumber = 1,
                        startsAt = "2026-03-17T00:00:00Z",
                        endsAt = "2026-03-24T00:00:00Z",
                        trackPackageId = "okayama-international-circuit",
                        trackName = "Okayama International Circuit",
                        trackConfigName = "Full Course",
                        carSkus = listOf("mazda-mx5-cup"),
                        carClasses = listOf("Global Mazda MX-5 Cup"),
                        sessions = listOf(
                            RaceSessionDto(
                                type = "recurring",
                                firstSessionOffsetMinutes = 60,
                                repeatEveryMinutes = 120,
                            ),
                        ),
                        raceLength = RaceLengthDto(laps = 10),
                        precipChance = 0.0,
                    ),
                ),
            ),
            cars = CarsCatalogDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                cars = listOf(
                    CarDto(
                        sku = "mazda-mx5-cup",
                        displayName = "Global Mazda MX-5 Cup",
                        sourceCarId = 1,
                        sourceSkuName = "Mazda MX-5 Cup",
                        categories = listOf("Sports Car", "Road"),
                        carClasses = listOf("Global Mazda MX-5 Cup"),
                        freeWithSubscription = true,
                        imageUrl = "https://example.test/mx5.png",
                    ),
                ),
            ),
            tracks = TracksCatalogDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                tracks = listOf(
                    TrackDto(
                        packageId = "okayama-international-circuit",
                        displayName = "Okayama International Circuit",
                        sourceTrackIds = listOf(1, 2),
                        type = "road",
                        supportedTypes = listOf("road", "oval"),
                        isDefaultContent = true,
                        mapUrl = "https://example.test/okayama-map.png",
                        imageUrl = "https://example.test/okayama.png",
                    ),
                ),
            ),
        )
}
