package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.data.dto.CarDto
import com.iracingweekplanner.mobile.data.dto.CarsCatalogDto
import com.iracingweekplanner.mobile.data.dto.LicenseDto
import com.iracingweekplanner.mobile.data.dto.RaceDto
import com.iracingweekplanner.mobile.data.dto.RaceLengthDto
import com.iracingweekplanner.mobile.data.dto.RaceSessionDto
import com.iracingweekplanner.mobile.data.dto.SeasonDto
import com.iracingweekplanner.mobile.data.dto.SeasonWeekDto
import com.iracingweekplanner.mobile.data.dto.SeriesDto
import com.iracingweekplanner.mobile.data.dto.TrackDto
import com.iracingweekplanner.mobile.data.dto.TracksCatalogDto
import com.iracingweekplanner.mobile.domain.CarId
import com.iracingweekplanner.mobile.domain.LicenseRequirement
import com.iracingweekplanner.mobile.domain.PlannerCar
import com.iracingweekplanner.mobile.domain.PlannerDataError
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

fun SeasonDto.toDomain(): PlannerDataResult<PlannerSeason> {
    val seasonStartsAt = seasonStart.toInstantResult(path = "seasonStart").dataOrReturn { return it }
    val seasonEndsAt = seasonEnd.toInstantResult(path = "seasonEnd").dataOrReturn { return it }
    val weekCalculationStartsAt = weekSeasonStart
        .toInstantResult(path = "weekSeasonStart")
        .dataOrReturn { return it }
    val mappedWeeks = weeks.mapIndexedResult { index, week ->
        week.toDomain(path = "weeks[$index]")
    }.dataOrReturn { return it }
    val mappedRaces = races.mapIndexedResult { _, race ->
        race.toDomain(path = "races[${race.raceId}]")
    }.dataOrReturn { return it }

    return PlannerDataResult.Loaded(
        PlannerSeason(
            id = SeasonId(seasonId),
            name = seasonName,
            window = TimeWindow(
                startsAt = seasonStartsAt,
                endsAt = seasonEndsAt,
            ),
            weekCalculationStartsAt = weekCalculationStartsAt,
            weeks = mappedWeeks,
            series = series.map { it.toDomain() },
            races = mappedRaces,
        ),
    )
}

fun CarsCatalogDto.toDomain(): PlannerDataResult<List<PlannerCar>> =
    PlannerDataResult.Loaded(cars.map { it.toDomain() })

fun TracksCatalogDto.toDomain(): PlannerDataResult<List<PlannerTrack>> =
    PlannerDataResult.Loaded(tracks.map { it.toDomain() })

private fun SeasonWeekDto.toDomain(path: String): PlannerDataResult<RaceWeek> {
    val startsAt = startsAt.toInstantResult(path = "$path.startsAt").dataOrReturn { return it }
    val endsAt = endsAt.toInstantResult(path = "$path.endsAt").dataOrReturn { return it }

    return PlannerDataResult.Loaded(
        RaceWeek(
            number = RaceWeekNumber(weekNumber),
            window = TimeWindow(
                startsAt = startsAt,
                endsAt = endsAt,
            ),
        ),
    )
}

private fun SeriesDto.toDomain(): PlannerSeries =
    PlannerSeries(
        id = SeriesId(seriesId),
        name = name,
        category = SeriesCategory(category),
        license = license.toDomain(),
        setup = if (isFixedSetup) RaceSetup.FIXED else RaceSetup.OPEN,
        isOfficial = isOfficial,
    )

private fun LicenseDto.toDomain(): LicenseRequirement =
    LicenseRequirement(
        className = className,
        safetyRatingLevel = level,
    )

private fun RaceDto.toDomain(path: String): PlannerDataResult<PlannerRace> {
    val startsAt = startsAt.toInstantResult(path = "$path.startsAt").dataOrReturn { return it }
    val endsAt = endsAt.toInstantResult(path = "$path.endsAt").dataOrReturn { return it }
    val mappedSessions = sessions.mapIndexedResult { index, session ->
        session.toDomain(path = "$path.sessions[$index]")
    }.dataOrReturn { return it }

    return PlannerDataResult.Loaded(
        PlannerRace(
            id = RaceId(raceId),
            seriesId = SeriesId(seriesId),
            weekNumber = RaceWeekNumber(weekNumber),
            window = TimeWindow(
                startsAt = startsAt,
                endsAt = endsAt,
            ),
            track = RaceTrackRef(
                id = TrackId(trackPackageId),
                name = trackName,
                configurationName = trackConfigName,
            ),
            carIds = carSkus.map { CarId(it) },
            carClasses = carClasses,
            sessions = mappedSessions,
            length = raceLength?.toDomain(),
            rainChance = precipChance?.let { RainChance(it) },
        ),
    )
}

private fun RaceLengthDto.toDomain(): RaceLength =
    RaceLength(
        lapCount = laps,
        timeLimitMinutes = minutes,
    )

private fun RaceSessionDto.toDomain(path: String): PlannerDataResult<RaceSessionSchedule> =
    when (type) {
        "recurring" -> {
            val firstSessionOffsetMinutes = firstSessionOffsetMinutes
                ?: return invalidSourceData(
                    path = "$path.firstSessionOffsetMinutes",
                    detail = "Missing required recurring session field",
                )
            val repeatEveryMinutes = repeatEveryMinutes
                ?: return invalidSourceData(
                    path = "$path.repeatEveryMinutes",
                    detail = "Missing required recurring session field",
                )
            PlannerDataResult.Loaded(
                RaceSessionSchedule.Recurring(
                    firstSessionOffset = firstSessionOffsetMinutes.minutes,
                    repeatEvery = repeatEveryMinutes.minutes,
                ),
            )
        }
        "setTimes" -> {
            val offsets = offsetMinutes
                ?: return invalidSourceData(
                    path = "$path.offsetMinutes",
                    detail = "Missing required set-times session field",
                )
            if (offsets.isEmpty()) {
                return invalidSourceData(
                    path = "$path.offsetMinutes",
                    detail = "Set-times session must include at least one offset",
                )
            }
            PlannerDataResult.Loaded(
                RaceSessionSchedule.SetTimes(
                    offsetsFromRaceStart = offsets.map { it.minutes },
                ),
            )
        }
        else -> invalidSourceData(
            path = "$path.type",
            detail = "Unknown session type: $type",
        )
    }

private fun CarDto.toDomain(): PlannerCar =
    PlannerCar(
        id = CarId(sku),
        displayName = displayName,
        sourceCarId = sourceCarId,
        sourceSkuName = sourceSkuName,
        categories = categories.orEmpty().toSet(),
        carClasses = carClasses.orEmpty().toSet(),
        isFreeWithSubscription = freeWithSubscription,
        imageUrl = imageUrl,
    )

private fun TrackDto.toDomain(): PlannerTrack =
    PlannerTrack(
        id = TrackId(packageId),
        displayName = displayName,
        sourceTrackIds = sourceTrackIds.toSet(),
        primaryType = type?.toTrackTypeOrNull(),
        supportedTypes = supportedTypes.orEmpty().mapNotNull { it.toTrackTypeOrNull() }.toSet(),
        isDefaultContent = isDefaultContent,
        mapUrl = mapUrl,
        imageUrl = imageUrl,
    )

private fun String.toInstantResult(path: String): PlannerDataResult<Instant> =
    try {
        PlannerDataResult.Loaded(Instant.parse(this))
    } catch (_: IllegalArgumentException) {
        invalidSourceData(
            path = path,
            detail = "Invalid timestamp",
        )
    }

private fun String.toTrackTypeOrNull(): TrackType? =
    when (this) {
        "road" -> TrackType.ROAD
        "oval" -> TrackType.OVAL
        "dirtOval" -> TrackType.DIRT_OVAL
        "dirtRoad" -> TrackType.DIRT_ROAD
        else -> null
    }

private fun <T, R> List<T>.mapIndexedResult(
    transform: (index: Int, value: T) -> PlannerDataResult<R>,
): PlannerDataResult<List<R>> {
    val mapped = mutableListOf<R>()
    forEachIndexed { index, value ->
        when (val result = transform(index, value)) {
            is PlannerDataResult.Loaded -> mapped += result.data
            is PlannerDataResult.Failure -> return result
        }
    }
    return PlannerDataResult.Loaded(mapped)
}

private inline fun <T> PlannerDataResult<T>.dataOrReturn(
    onFailure: (PlannerDataResult.Failure) -> Nothing,
): T =
    when (this) {
        is PlannerDataResult.Loaded -> data
        is PlannerDataResult.Failure -> onFailure(this)
    }

private fun <T> invalidSourceData(path: String, detail: String): PlannerDataResult<T> =
    PlannerDataResult.Failure(
        PlannerDataError.InvalidSourceData(
            path = path,
            detail = detail,
        ),
    )
