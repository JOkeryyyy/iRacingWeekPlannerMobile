package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.domain.PlannerCar
import com.iracingweekplanner.mobile.domain.PlannerSeason
import com.iracingweekplanner.mobile.domain.PlannerTrack

interface PlannerLocalDataStore {
    suspend fun read(): PlannerLocalDataReadResult
    suspend fun replace(dataset: PlannerStoredPlannerData): PlannerLocalDataWriteResult
}

sealed interface PlannerLocalDataReadResult {
    data class Hit(
        val data: PlannerStoredPlannerData,
    ) : PlannerLocalDataReadResult

    data object Miss : PlannerLocalDataReadResult
    data object Failure : PlannerLocalDataReadResult
}

sealed interface PlannerLocalDataWriteResult {
    data object Saved : PlannerLocalDataWriteResult
    data object Failure : PlannerLocalDataWriteResult
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
