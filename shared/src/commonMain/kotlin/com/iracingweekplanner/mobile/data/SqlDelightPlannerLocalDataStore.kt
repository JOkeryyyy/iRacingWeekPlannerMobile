package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import com.iracingweekplanner.mobile.domain.CarId
import com.iracingweekplanner.mobile.domain.LicenseRequirement
import com.iracingweekplanner.mobile.domain.PlannerCar
import com.iracingweekplanner.mobile.domain.PlannerDataResult
import com.iracingweekplanner.mobile.domain.PlannerRace
import com.iracingweekplanner.mobile.domain.PlannerSeason
import com.iracingweekplanner.mobile.domain.PlannerSeries
import com.iracingweekplanner.mobile.domain.PlannerTrack
import com.iracingweekplanner.mobile.domain.RaceId
import com.iracingweekplanner.mobile.domain.RaceLength
import com.iracingweekplanner.mobile.domain.RaceSessionSchedule
import com.iracingweekplanner.mobile.domain.RaceSetup
import com.iracingweekplanner.mobile.domain.RaceTrackRef
import com.iracingweekplanner.mobile.domain.RaceWeek
import com.iracingweekplanner.mobile.domain.RaceWeekNumber
import com.iracingweekplanner.mobile.domain.RainChance
import com.iracingweekplanner.mobile.domain.SeasonId
import com.iracingweekplanner.mobile.domain.SeriesCategory
import com.iracingweekplanner.mobile.domain.SeriesId
import com.iracingweekplanner.mobile.domain.TimeWindow
import com.iracingweekplanner.mobile.domain.TrackId
import com.iracingweekplanner.mobile.domain.TrackType
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class SqlDelightPlannerLocalDataStore(
    private val database: PlannerDatabase,
) : PlannerLocalDataStore {

    private val queries = database.plannerLocalDataQueries

    override suspend fun read(): PlannerStoredPlannerData? =
        try {
            val metadataRow = queries.selectMetadata().executeAsOneOrNull()
                ?: return null
            val seasonRow = queries.selectSeason().executeAsOneOrNull()
                ?: return null

            PlannerStoredPlannerData(
                metadata = PlannerStoredDatasetMetadata(
                    schemaVersion = metadataRow.schema_version.toInt(),
                    generatedAt = metadataRow.generated_at,
                    seasonId = metadataRow.season_id,
                    seasonFile = metadataRow.season_file_path,
                    carsFile = metadataRow.cars_file_path,
                    tracksFile = metadataRow.tracks_file_path,
                    revision = metadataRow.revision,
                    checksums = queries.selectChecksums()
                        .executeAsList()
                        .associate { it.file_path to it.checksum },
                ),
                season = PlannerSeason(
                    id = SeasonId(seasonRow.season_id),
                    name = seasonRow.name,
                    window = TimeWindow(
                        startsAt = Instant.parse(seasonRow.starts_at),
                        endsAt = Instant.parse(seasonRow.ends_at),
                    ),
                    weekCalculationStartsAt = Instant.parse(seasonRow.week_calculation_starts_at),
                    weeks = readWeeks(),
                    series = readSeries(),
                    races = readRaces(),
                ),
                cars = readCars(),
                tracks = readTracks(),
            )
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            null
        }

    override suspend fun replaceIfValid(bundle: PlannerDataBundle): Boolean {
        val dataset = bundle.toValidatedDataset() ?: return false

        return try {
            database.transaction {
                clearPlannerDataset()
                insertDataset(dataset)
            }
            true
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            false
        }
    }

    private fun readWeeks(): List<RaceWeek> =
        queries.selectWeeks().executeAsList().map { row ->
            RaceWeek(
                number = RaceWeekNumber(row.week_number.toInt()),
                window = TimeWindow(
                    startsAt = Instant.parse(row.starts_at),
                    endsAt = Instant.parse(row.ends_at),
                ),
            )
        }

    private fun readSeries(): List<PlannerSeries> =
        queries.selectSeries().executeAsList().map { row ->
            PlannerSeries(
                id = SeriesId(row.series_id),
                name = row.name,
                category = SeriesCategory(row.category),
                license = LicenseRequirement(
                    className = row.license_class_name,
                    safetyRatingLevel = row.license_safety_rating_level?.toInt(),
                ),
                setup = RaceSetup.valueOf(row.setup),
                isOfficial = row.is_official.toBooleanValue(),
            )
        }

    private fun readRaces(): List<PlannerRace> =
        queries.selectRaces().executeAsList().map { row ->
            PlannerRace(
                id = RaceId(row.race_id),
                seriesId = SeriesId(row.series_id),
                weekNumber = RaceWeekNumber(row.week_number.toInt()),
                window = TimeWindow(
                    startsAt = Instant.parse(row.starts_at),
                    endsAt = Instant.parse(row.ends_at),
                ),
                track = RaceTrackRef(
                    id = TrackId(row.track_package_id),
                    name = row.track_name,
                    configurationName = row.track_config_name,
                ),
                carIds = queries.selectRaceCarSkus(row.race_id).executeAsList().map(::CarId),
                carClasses = queries.selectRaceCarClasses(row.race_id).executeAsList(),
                sessions = readRaceSessions(row.race_id),
                length = if (row.lap_count == null && row.time_limit_minutes == null) {
                    null
                } else {
                    RaceLength(
                        lapCount = row.lap_count?.toInt(),
                        timeLimitMinutes = row.time_limit_minutes?.toInt(),
                    )
                },
                rainChance = row.rain_chance_percentage?.let(::RainChance),
            )
        }

    private fun readRaceSessions(raceId: String): List<RaceSessionSchedule> =
        queries.selectRaceSessions(raceId)
            .executeAsList()
            .groupBy { it.position }
            .values
            .map { rows ->
                val first = rows.first()
                when (first.type) {
                    SESSION_TYPE_RECURRING -> RaceSessionSchedule.Recurring(
                        firstSessionOffset = first.first_session_offset_minutes.requireLong(
                            path = "race_sessions[$raceId].first_session_offset_minutes",
                        ).minutes,
                        repeatEvery = first.repeat_every_minutes.requireLong(
                            path = "race_sessions[$raceId].repeat_every_minutes",
                        ).minutes,
                    )
                    SESSION_TYPE_SET_TIMES -> RaceSessionSchedule.SetTimes(
                        offsetsFromRaceStart = rows.map {
                            it.offset_minutes.requireLong(
                                path = "race_sessions[$raceId].offset_minutes",
                            ).minutes
                        },
                    )
                    else -> throw IllegalStateException("Unknown persisted session type: ${first.type}")
                }
            }

    private fun readCars(): List<PlannerCar> =
        queries.selectCars().executeAsList().map { row ->
            PlannerCar(
                id = CarId(row.sku),
                displayName = row.display_name,
                sourceCarId = row.source_car_id?.toInt(),
                sourceSkuName = row.source_sku_name,
                categories = queries.selectCarCategories(row.sku).executeAsList().toSet(),
                carClasses = queries.selectCarClasses(row.sku).executeAsList().toSet(),
                isFreeWithSubscription = row.is_free_with_subscription?.toBooleanValue(),
                imageUrl = row.image_url,
            )
        }

    private fun readTracks(): List<PlannerTrack> =
        queries.selectTracks().executeAsList().map { row ->
            PlannerTrack(
                id = TrackId(row.package_id),
                displayName = row.display_name,
                sourceTrackIds = queries.selectTrackSourceIds(row.package_id)
                    .executeAsList()
                    .map { it.toInt() }
                    .toSet(),
                primaryType = row.primary_type?.let(TrackType::valueOf),
                supportedTypes = queries.selectTrackSupportedTypes(row.package_id)
                    .executeAsList()
                    .map(TrackType::valueOf)
                    .toSet(),
                isDefaultContent = row.is_default_content?.toBooleanValue(),
                mapUrl = row.map_url,
                imageUrl = row.image_url,
            )
        }

    private fun clearPlannerDataset() {
        queries.deleteManifestChecksums()
        queries.deleteTrackSupportedTypes()
        queries.deleteTrackSourceIds()
        queries.deleteTracks()
        queries.deleteCarClasses()
        queries.deleteCarCategories()
        queries.deleteCars()
        queries.deleteRaceSessions()
        queries.deleteRaceCarClasses()
        queries.deleteRaceCarSkus()
        queries.deleteRaces()
        queries.deleteSeries()
        queries.deleteRaceWeeks()
        queries.deleteSeasons()
        queries.deleteMetadata()
    }

    private fun insertDataset(dataset: ValidatedPlannerDataset) {
        queries.insertMetadata(
            schema_version = dataset.metadata.schemaVersion.toLong(),
            generated_at = dataset.metadata.generatedAt,
            season_id = dataset.metadata.seasonId,
            season_file_path = dataset.metadata.seasonFile,
            cars_file_path = dataset.metadata.carsFile,
            tracks_file_path = dataset.metadata.tracksFile,
            revision = dataset.metadata.revision,
        )
        dataset.metadata.checksums.forEach { (path, checksum) ->
            queries.insertChecksum(file_path = path, checksum = checksum)
        }
        queries.insertSeason(
            season_id = dataset.season.id.value,
            name = dataset.season.name,
            starts_at = dataset.season.window.startsAt.toString(),
            ends_at = dataset.season.window.endsAt.toString(),
            week_calculation_starts_at = dataset.season.weekCalculationStartsAt.toString(),
        )
        dataset.season.weeks.forEach { week ->
            queries.insertRaceWeek(
                week_number = week.number.value.toLong(),
                starts_at = week.window.startsAt.toString(),
                ends_at = week.window.endsAt.toString(),
            )
        }
        dataset.season.series.forEach { series ->
            queries.insertSeries(
                series_id = series.id.value,
                name = series.name,
                category = series.category.displayName,
                license_class_name = series.license.className,
                license_safety_rating_level = series.license.safetyRatingLevel?.toLong(),
                setup = series.setup.name,
                is_official = series.isOfficial.toLongValue(),
            )
        }
        dataset.races.forEach { race ->
            queries.insertRace(
                race_id = race.id.value,
                series_id = race.seriesId.value,
                week_number = race.weekNumber.value.toLong(),
                starts_at = race.window.startsAt.toString(),
                ends_at = race.window.endsAt.toString(),
                track_package_id = race.track.id.value,
                track_name = race.track.name,
                track_config_name = race.track.configurationName,
                lap_count = race.length?.lapCount?.toLong(),
                time_limit_minutes = race.length?.timeLimitMinutes?.toLong(),
                rain_chance_percentage = race.rainChance?.percentage,
            )
            race.carIds.forEachIndexed { index, carId ->
                queries.insertRaceCarSku(
                    race_id = race.id.value,
                    position = index.toLong(),
                    car_sku = carId.value,
                )
            }
            race.carClasses.forEachIndexed { index, carClass ->
                queries.insertRaceCarClass(
                    race_id = race.id.value,
                    position = index.toLong(),
                    car_class = carClass,
                )
            }
            race.sessions.forEachIndexed { index, session ->
                when (session) {
                    is RaceSessionSchedule.Recurring -> queries.insertRaceSession(
                        race_id = race.id.value,
                        position = index.toLong(),
                        type = SESSION_TYPE_RECURRING,
                        first_session_offset_minutes = session.firstSessionOffset.inWholeMinutes,
                        repeat_every_minutes = session.repeatEvery.inWholeMinutes,
                        offset_minutes = null,
                    )
                    is RaceSessionSchedule.SetTimes -> session.offsetsFromRaceStart.forEach { offset ->
                        queries.insertRaceSession(
                            race_id = race.id.value,
                            position = index.toLong(),
                            type = SESSION_TYPE_SET_TIMES,
                            first_session_offset_minutes = null,
                            repeat_every_minutes = null,
                            offset_minutes = offset.inWholeMinutes,
                        )
                    }
                }
            }
        }
        dataset.cars.forEach { car ->
            queries.insertCar(
                sku = car.id.value,
                display_name = car.displayName,
                source_car_id = car.sourceCarId?.toLong(),
                source_sku_name = car.sourceSkuName,
                is_free_with_subscription = car.isFreeWithSubscription?.toLongValue(),
                image_url = car.imageUrl,
            )
            car.categories.forEachIndexed { index, category ->
                queries.insertCarCategory(
                    sku = car.id.value,
                    position = index.toLong(),
                    category = category,
                )
            }
            car.carClasses.forEachIndexed { index, carClass ->
                queries.insertCarClass(
                    sku = car.id.value,
                    position = index.toLong(),
                    car_class = carClass,
                )
            }
        }
        dataset.tracks.forEach { track ->
            queries.insertTrack(
                package_id = track.id.value,
                display_name = track.displayName,
                primary_type = track.primaryType?.name,
                is_default_content = track.isDefaultContent?.toLongValue(),
                map_url = track.mapUrl,
                image_url = track.imageUrl,
            )
            track.sourceTrackIds.forEachIndexed { index, sourceTrackId ->
                queries.insertTrackSourceId(
                    package_id = track.id.value,
                    position = index.toLong(),
                    source_track_id = sourceTrackId.toLong(),
                )
            }
            track.supportedTypes.forEachIndexed { index, supportedType ->
                queries.insertTrackSupportedType(
                    package_id = track.id.value,
                    position = index.toLong(),
                    supported_type = supportedType.name,
                )
            }
        }
    }

    private fun PlannerDataBundle.toValidatedDataset(): ValidatedPlannerDataset? {
        val season = season.toDomain().dataOrNull() ?: return null
        val cars = cars.toDomain().dataOrNull() ?: return null
        val tracks = tracks.toDomain().dataOrNull() ?: return null
        return ValidatedPlannerDataset(
            metadata = PlannerStoredDatasetMetadata(
                schemaVersion = manifest.schemaVersion,
                generatedAt = manifest.generatedAt,
                seasonId = manifest.seasonId,
                seasonFile = manifest.seasonFile,
                carsFile = manifest.carsFile,
                tracksFile = manifest.tracksFile,
                revision = manifest.revision,
                checksums = manifest.checksums.orEmpty(),
            ),
            season = season,
            cars = cars,
            tracks = tracks,
            races = season.races,
        )
    }

    private fun <T> PlannerDataResult<T>.dataOrNull(): T? =
        when (this) {
            is PlannerDataResult.Loaded -> data
            is PlannerDataResult.Failure -> null
        }

    private companion object {
        const val SESSION_TYPE_RECURRING = "recurring"
        const val SESSION_TYPE_SET_TIMES = "setTimes"
    }
}

private fun Long.toBooleanValue(): Boolean =
    this != 0L

private fun Boolean.toLongValue(): Long =
    if (this) 1L else 0L

private fun Long?.requireLong(path: String): Long =
    this ?: error("Missing required persisted value at $path")
