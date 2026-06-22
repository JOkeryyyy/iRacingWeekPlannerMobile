package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.data.dto.CarsCatalogDto
import com.iracingweekplanner.mobile.data.dto.MobileDataManifestDto
import com.iracingweekplanner.mobile.data.dto.SeasonDto
import com.iracingweekplanner.mobile.data.dto.TracksCatalogDto

data class PlannerDataBundle(
    val manifest: MobileDataManifestDto,
    val season: SeasonDto,
    val cars: CarsCatalogDto,
    val tracks: TracksCatalogDto,
)

sealed interface PlannerDataSourceResult {
    data class Loaded(
        val bundle: PlannerDataBundle,
    ) : PlannerDataSourceResult

    data class Failure(
        val failure: PlannerDataSourceFailure,
    ) : PlannerDataSourceResult
}

data class PlannerDataSourceFailure(
    val path: String,
    val reason: Reason,
    val detail: String,
) {
    enum class Reason {
        RESOURCE_UNAVAILABLE,
        DECODE_FAILED,
    }
}

interface PlannerLocalDataSource {
    suspend fun loadPlannerData(): PlannerDataSourceResult
}
