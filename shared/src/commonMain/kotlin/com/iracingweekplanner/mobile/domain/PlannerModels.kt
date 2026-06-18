package com.iracingweekplanner.mobile.domain

import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.time.Instant

@JvmInline
value class SeasonId(val value: String) {
    init {
        require(value.isNotBlank()) { "SeasonId must not be blank." }
    }
}

@JvmInline
value class RaceId(val value: String) {
    init {
        require(value.isNotBlank()) { "RaceId must not be blank." }
    }
}

@JvmInline
value class SeriesId(val value: String) {
    init {
        require(value.isNotBlank()) { "SeriesId must not be blank." }
    }
}

@JvmInline
value class CarId(val value: String) {
    init {
        require(value.isNotBlank()) { "CarId must not be blank." }
    }
}

@JvmInline
value class TrackId(val value: String) {
    init {
        require(value.isNotBlank()) { "TrackId must not be blank." }
    }
}

@JvmInline
value class RaceWeekNumber(val value: Int) {
    init {
        require(value > 0) { "RaceWeekNumber must be positive." }
    }
}

@JvmInline
value class SeriesCategory(val displayName: String) {
    init {
        require(displayName.isNotBlank()) { "SeriesCategory displayName must not be blank." }
    }
}

data class TimeWindow(
    val startsAt: Instant,
    val endsAt: Instant,
) {
    init {
        require(endsAt > startsAt) { "TimeWindow endsAt must be after startsAt." }
    }
}

data class PlannerSeason(
    val id: SeasonId,
    val name: String,
    val window: TimeWindow,
    val weekCalculationStartsAt: Instant,
    val weeks: List<RaceWeek>,
    val series: List<PlannerSeries>,
    val races: List<PlannerRace>,
) {
    init {
        require(name.isNotBlank()) { "PlannerSeason name must not be blank." }
    }
}

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
) {
    init {
        require(name.isNotBlank()) { "PlannerSeries name must not be blank." }
    }
}

data class LicenseRequirement(
    val className: String,
    val safetyRatingLevel: Int? = null,
) {
    init {
        require(className.isNotBlank()) { "LicenseRequirement className must not be blank." }
        require(safetyRatingLevel == null || safetyRatingLevel >= 0) {
            "LicenseRequirement safetyRatingLevel must be non-negative."
        }
    }
}

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
) {
    init {
        require(carIds.isNotEmpty()) { "PlannerRace carIds must not be empty." }
        require(sessions.isNotEmpty()) { "PlannerRace sessions must not be empty." }
    }
}

data class RaceTrackRef(
    val id: TrackId,
    val name: String,
    val configurationName: String? = null,
) {
    init {
        require(name.isNotBlank()) { "RaceTrackRef name must not be blank." }
        require(configurationName == null || configurationName.isNotBlank()) {
            "RaceTrackRef configurationName must not be blank when present."
        }
    }
}

data class RaceLength(
    val lapCount: Int? = null,
    val timeLimitMinutes: Int? = null,
) {
    init {
        require(lapCount != null || timeLimitMinutes != null) {
            "RaceLength must include lapCount or timeLimitMinutes."
        }
        require(lapCount == null || lapCount > 0) { "RaceLength lapCount must be positive." }
        require(timeLimitMinutes == null || timeLimitMinutes > 0) {
            "RaceLength timeLimitMinutes must be positive."
        }
    }
}

@JvmInline
value class RainChance(val percentage: Double) {
    init {
        require(percentage >= 0.0 && percentage <= 100.0) {
            "RainChance percentage must be between 0.0 and 100.0."
        }
    }
}

sealed interface RaceSessionSchedule {
    data class Recurring(
        val firstSessionOffset: Duration,
        val repeatEvery: Duration,
    ) : RaceSessionSchedule {
        init {
            require(firstSessionOffset >= Duration.ZERO) {
                "Recurring firstSessionOffset must be non-negative."
            }
            require(repeatEvery > Duration.ZERO) { "Recurring repeatEvery must be positive." }
        }
    }

    data class SetTimes(
        val offsetsFromRaceStart: List<Duration>,
    ) : RaceSessionSchedule {
        init {
            require(offsetsFromRaceStart.isNotEmpty()) {
                "SetTimes offsetsFromRaceStart must not be empty."
            }
            require(offsetsFromRaceStart.all { it >= Duration.ZERO }) {
                "SetTimes offsetsFromRaceStart must be non-negative."
            }
        }
    }
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
) {
    init {
        require(displayName.isNotBlank()) { "PlannerCar displayName must not be blank." }
        require(sourceCarId == null || sourceCarId > 0) { "PlannerCar sourceCarId must be positive." }
    }
}

data class PlannerTrack(
    val id: TrackId,
    val displayName: String,
    val sourceTrackIds: Set<Int>,
    val primaryType: TrackType? = null,
    val supportedTypes: Set<TrackType> = emptySet(),
    val isDefaultContent: Boolean? = null,
    val mapUrl: String? = null,
    val imageUrl: String? = null,
) {
    init {
        require(displayName.isNotBlank()) { "PlannerTrack displayName must not be blank." }
        require(sourceTrackIds.all { it > 0 }) { "PlannerTrack sourceTrackIds must be positive." }
    }
}

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
