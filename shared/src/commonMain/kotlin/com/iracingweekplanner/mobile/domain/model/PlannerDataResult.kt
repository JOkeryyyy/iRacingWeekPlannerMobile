package com.iracingweekplanner.mobile.domain.model

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
    enum class LocalStoreOperation {
        READ,
        WRITE,
    }

    data class InvalidSourceData(
        val path: String,
        val detail: String,
    ) : PlannerDataError

    data class SourceUnavailable(
        val path: String,
        val detail: String,
    ) : PlannerDataError

    data class SourceDecodeFailed(
        val path: String,
        val detail: String,
    ) : PlannerDataError

    data class LocalStoreFailure(
        val operation: LocalStoreOperation,
    ) : PlannerDataError
}
