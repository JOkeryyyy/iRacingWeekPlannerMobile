package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.domain.PlannerCar
import com.iracingweekplanner.mobile.domain.PlannerRace
import com.iracingweekplanner.mobile.domain.PlannerSeason
import com.iracingweekplanner.mobile.domain.PlannerTrack

interface PlannerLocalDataStore {
    suspend fun read(): PlannerStoredPlannerData?
    suspend fun replaceIfValid(bundle: PlannerDataBundle): Boolean
}

data class PlannerStoredPlannerData(
    val metadata: PlannerStoredDatasetMetadata,
    val season: PlannerSeason,
    val cars: List<PlannerCar>,
    val tracks: List<PlannerTrack>,
)

data class PlannerStoredDatasetMetadata(
    val schemaVersion: Int,
    val generatedAt: String,
    val seasonId: String,
    val seasonFile: String,
    val carsFile: String,
    val tracksFile: String,
    val revision: String?,
    val checksums: Map<String, String>,
)

internal data class ValidatedPlannerDataset(
    val metadata: PlannerStoredDatasetMetadata,
    val season: PlannerSeason,
    val cars: List<PlannerCar>,
    val tracks: List<PlannerTrack>,
    val races: List<PlannerRace>,
)
