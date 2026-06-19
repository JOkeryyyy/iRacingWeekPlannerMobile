package com.iracingweekplanner.mobile.domain

import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.time.Instant

@JvmInline
value class SeasonId(val value: String)

@JvmInline
value class RaceId(val value: String)

@JvmInline
value class SeriesId(val value: String)

@JvmInline
value class CarId(val value: String)

@JvmInline
value class TrackId(val value: String)

@JvmInline
value class RaceWeekNumber(val value: Int)

@JvmInline
value class SeriesCategory(val displayName: String)

data class TimeWindow(
    val startsAt: Instant,
    val endsAt: Instant,
)

data class PlannerSeason(
    val id: SeasonId,
    val name: String,
    val window: TimeWindow,
    val weekCalculationStartsAt: Instant,
    val weeks: List<RaceWeek>,
    val series: List<PlannerSeries>,
    val races: List<PlannerRace>,
)

data class RaceWeek(
    val number: RaceWeekNumber,
    val window: TimeWindow,
)

data class PlannerSeries(
    val id: SeriesId,
    val name: String,
    val category: SeriesCategory,
    val license: LicenseRequirement,
    val setup: RaceSetup,
    val isOfficial: Boolean,
)

data class LicenseRequirement(
    val className: String,
    val safetyRatingLevel: Int? = null,
)

enum class RaceSetup {
    FIXED,
    OPEN,
}

data class PlannerRace(
    val id: RaceId,
    val seriesId: SeriesId,
    val weekNumber: RaceWeekNumber,
    val window: TimeWindow,
    val track: RaceTrackRef,
    val carIds: List<CarId>,
    val carClasses: List<String>,
    val sessions: List<RaceSessionSchedule>,
    val length: RaceLength? = null,
    val rainChance: RainChance? = null,
)

data class RaceTrackRef(
    val id: TrackId,
    val name: String,
    val configurationName: String? = null,
)

data class RaceLength(
    val lapCount: Int? = null,
    val timeLimitMinutes: Int? = null,
)

@JvmInline
value class RainChance(val percentage: Double)

sealed interface RaceSessionSchedule {
    data class Recurring(
        val firstSessionOffset: Duration,
        val repeatEvery: Duration,
    ) : RaceSessionSchedule

    data class SetTimes(
        val offsetsFromRaceStart: List<Duration>,
    ) : RaceSessionSchedule
}

data class PlannerCar(
    val id: CarId,
    val displayName: String,
    val sourceCarId: Int? = null,
    val sourceSkuName: String? = null,
    val categories: Set<String> = emptySet(),
    val carClasses: Set<String> = emptySet(),
    val isFreeWithSubscription: Boolean? = null,
    val imageUrl: String? = null,
)

data class PlannerTrack(
    val id: TrackId,
    val displayName: String,
    val sourceTrackIds: Set<Int>,
    val primaryType: TrackType? = null,
    val supportedTypes: Set<TrackType> = emptySet(),
    val isDefaultContent: Boolean? = null,
    val mapUrl: String? = null,
    val imageUrl: String? = null,
)

enum class TrackType {
    ROAD,
    OVAL,
    DIRT_OVAL,
    DIRT_ROAD,
}

data class PlannerFilters(
    val carOwnership: ContentOwnershipFilter = ContentOwnershipFilter.ANY,
    val trackOwnership: ContentOwnershipFilter = ContentOwnershipFilter.ANY,
    val favorites: FavoriteFilter = FavoriteFilter.ANY,
    val ownedCarIds: Set<CarId> = emptySet(),
    val favoriteCarIds: Set<CarId> = emptySet(),
    val ownedTrackIds: Set<TrackId> = emptySet(),
    val favoriteTrackIds: Set<TrackId> = emptySet(),
    val selectedSeriesIds: Set<SeriesId> = emptySet(),
    val selectedCategories: Set<SeriesCategory> = emptySet(),
)

enum class ContentOwnershipFilter {
    ANY,
    OWNED,
    UNOWNED,
}

enum class FavoriteFilter {
    ANY,
    FAVORITES_ONLY,
}
