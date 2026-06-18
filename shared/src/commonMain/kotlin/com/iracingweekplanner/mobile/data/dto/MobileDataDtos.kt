package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MobileDataManifestDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val seasonId: String,
    val seasonFile: String,
    val carsFile: String,
    val tracksFile: String,
    val revision: String? = null,
    val checksums: Map<String, String>? = null,
)

@Serializable
data class SeasonDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val seasonId: String,
    val seasonName: String,
    val seasonStart: String,
    val seasonEnd: String,
    val weekSeasonStart: String,
    val weeks: List<SeasonWeekDto>,
    val series: List<SeriesDto>,
    val races: List<RaceDto>,
)

@Serializable
data class SeasonWeekDto(
    val weekNumber: Int,
    val startsAt: String,
    val endsAt: String,
)

@Serializable
data class SeriesDto(
    val seriesId: String,
    val name: String,
    val category: String,
    val license: LicenseDto,
    val isOfficial: Boolean,
    val isFixedSetup: Boolean,
)

@Serializable
data class LicenseDto(
    val className: String,
    val level: Int? = null,
)

@Serializable
data class RaceDto(
    val raceId: String,
    val seriesId: String,
    val weekNumber: Int,
    val startsAt: String,
    val endsAt: String,
    val trackPackageId: String,
    val trackName: String,
    val carSkus: List<String>,
    val carClasses: List<String>,
    val sessions: List<RaceSessionDto>,
    val trackConfigName: String? = null,
    val raceLength: RaceLengthDto? = null,
    val precipChance: Double? = null,
)

@Serializable
data class RaceLengthDto(
    val laps: Int? = null,
    val minutes: Int? = null,
)

@Serializable
data class RaceSessionDto(
    val type: String,
    val firstSessionOffsetMinutes: Int? = null,
    val repeatEveryMinutes: Int? = null,
    val offsetMinutes: List<Int>? = null,
)

@Serializable
data class CarsCatalogDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val cars: List<CarDto>,
)

@Serializable
data class CarDto(
    val sku: String,
    val displayName: String,
    val sourceCarId: Int? = null,
    val sourceSkuName: String? = null,
    val categories: List<String>? = null,
    val carClasses: List<String>? = null,
    val freeWithSubscription: Boolean? = null,
    val imageUrl: String? = null,
)

@Serializable
data class TracksCatalogDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val tracks: List<TrackDto>,
)

@Serializable
data class TrackDto(
    val packageId: String,
    val displayName: String,
    val sourceTrackIds: List<Int>,
    val type: String? = null,
    val supportedTypes: List<String>? = null,
    val isDefaultContent: Boolean? = null,
    val mapUrl: String? = null,
    val imageUrl: String? = null,
)
