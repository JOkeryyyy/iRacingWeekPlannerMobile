package com.iracingweekplanner.mobile.domain

enum class PlannerDataFreshness {
    FRESH,
    CACHED,
}

sealed interface PlannerDataResult<out T> {
    data class Loaded<out T>(
        val data: T,
        val freshness: PlannerDataFreshness = PlannerDataFreshness.FRESH,
    ) : PlannerDataResult<T>

    data class Failure(
        val error: PlannerDataError,
    ) : PlannerDataResult<Nothing>
}

sealed interface PlannerDataError {
    data class InvalidSourceData(
        val path: String,
        val detail: String,
    ) : PlannerDataError
}
