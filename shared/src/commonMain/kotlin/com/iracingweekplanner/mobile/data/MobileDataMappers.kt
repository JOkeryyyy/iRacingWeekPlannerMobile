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

fun SeasonDto.toDomain(): PlannerSeason =
    PlannerSeason(
        id = SeasonId(seasonId),
        name = seasonName,
        window = TimeWindow(
            startsAt = seasonStart.toInstantOrEpoch(),
            endsAt = seasonEnd.toInstantOrEpoch(),
        ),
        weekCalculationStartsAt = weekSeasonStart.toInstantOrEpoch(),
        weeks = weeks.map { it.toDomain() },
        series = series.map { it.toDomain() },
        races = races.map { it.toDomain() },
    )

fun CarsCatalogDto.toDomain(): List<PlannerCar> =
    cars.map { it.toDomain() }

fun TracksCatalogDto.toDomain(): List<PlannerTrack> =
    tracks.map { it.toDomain() }

private fun SeasonWeekDto.toDomain(): RaceWeek =
    RaceWeek(
        number = RaceWeekNumber(weekNumber),
        window = TimeWindow(
            startsAt = startsAt.toInstantOrEpoch(),
            endsAt = endsAt.toInstantOrEpoch(),
        ),
    )

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

private fun RaceDto.toDomain(): PlannerRace =
    PlannerRace(
        id = RaceId(raceId),
        seriesId = SeriesId(seriesId),
        weekNumber = RaceWeekNumber(weekNumber),
        window = TimeWindow(
            startsAt = startsAt.toInstantOrEpoch(),
            endsAt = endsAt.toInstantOrEpoch(),
        ),
        track = RaceTrackRef(
            id = TrackId(trackPackageId),
            name = trackName,
            configurationName = trackConfigName,
        ),
        carIds = carSkus.map { CarId(it) },
        carClasses = carClasses,
        sessions = sessions.mapNotNull { it.toDomain() },
        length = raceLength?.toDomain(),
        rainChance = precipChance?.let { RainChance(it) },
    )

private fun RaceLengthDto.toDomain(): RaceLength =
    RaceLength(
        lapCount = laps,
        timeLimitMinutes = minutes,
    )

private fun RaceSessionDto.toDomain(): RaceSessionSchedule? =
    when (type) {
        "recurring" -> RaceSessionSchedule.Recurring(
            firstSessionOffset = (firstSessionOffsetMinutes ?: 0).minutes,
            repeatEvery = (repeatEveryMinutes ?: 0).minutes,
        )
        "setTimes" -> RaceSessionSchedule.SetTimes(
            offsetsFromRaceStart = offsetMinutes.orEmpty().map { it.minutes },
        )
        else -> null
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

private fun String.toInstantOrEpoch(): Instant =
    try {
        Instant.parse(this)
    } catch (_: IllegalArgumentException) {
        Instant.fromEpochMilliseconds(0)
    }

private fun String.toTrackTypeOrNull(): TrackType? =
    when (this) {
        "road" -> TrackType.ROAD
        "oval" -> TrackType.OVAL
        "dirtOval" -> TrackType.DIRT_OVAL
        "dirtRoad" -> TrackType.DIRT_ROAD
        else -> null
    }
