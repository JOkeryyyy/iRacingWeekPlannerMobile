package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.domain.PlannerDataError
import com.iracingweekplanner.mobile.domain.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.PlannerDataResult

interface PlannerDataRefreshCoordinator {
    suspend fun loadPlannerData(): PlannerDataResult<PlannerStoredPlannerData>
}

class RefreshCachePlannerDataCoordinator(
    private val source: PlannerDataSource,
    private val localDataStore: PlannerLocalDataStore,
) : PlannerDataRefreshCoordinator {

    override suspend fun loadPlannerData(): PlannerDataResult<PlannerStoredPlannerData> =
        when (val sourceResult = source.loadPlannerData()) {
            is PlannerDataSourceResult.Loaded -> loadFresh(sourceResult.bundle)
            is PlannerDataSourceResult.Failure -> loadCachedOrFailure(sourceResult.failure.toDomainError())
        }

    private suspend fun loadFresh(bundle: PlannerDataBundle): PlannerDataResult<PlannerStoredPlannerData> =
        when (val mapped = bundle.toStoredPlannerData()) {
            is PlannerDataResult.Loaded -> saveFresh(mapped.data)
            is PlannerDataResult.Failure -> loadCachedOrFailure(mapped.error)
        }

    private suspend fun saveFresh(dataset: PlannerStoredPlannerData): PlannerDataResult<PlannerStoredPlannerData> =
        when (localDataStore.replace(dataset)) {
            is PlannerLocalDataWriteResult.Saved -> PlannerDataResult.Loaded(
                data = dataset,
                freshness = PlannerDataFreshness.FRESH,
            )
            is PlannerLocalDataWriteResult.Failure -> loadCachedOrFailure(
                PlannerDataError.LocalStoreFailure(
                    operation = PlannerDataError.LocalStoreOperation.WRITE,
                ),
            )
        }

    private suspend fun loadCachedOrFailure(
        failure: PlannerDataError,
    ): PlannerDataResult<PlannerStoredPlannerData> =
        when (val cached = localDataStore.read()) {
            is PlannerLocalDataReadResult.Hit -> PlannerDataResult.Loaded(
                data = cached.data,
                freshness = PlannerDataFreshness.CACHED,
            )
            is PlannerLocalDataReadResult.Miss -> PlannerDataResult.Failure(failure)
            is PlannerLocalDataReadResult.Failure -> PlannerDataResult.Failure(
                PlannerDataError.LocalStoreFailure(
                    operation = PlannerDataError.LocalStoreOperation.READ,
                ),
            )
        }

    private fun PlannerDataSourceFailure.toDomainError(): PlannerDataError =
        when (reason) {
            PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE -> PlannerDataError.SourceUnavailable(
                path = path,
                detail = "Source resource is unavailable",
            )
            PlannerDataSourceFailure.Reason.DECODE_FAILED -> PlannerDataError.SourceDecodeFailed(
                path = path,
                detail = "Source data could not be decoded",
            )
            PlannerDataSourceFailure.Reason.INVALID_REFERENCE -> PlannerDataError.InvalidSourceData(
                path = path,
                detail = "Invalid source reference",
            )
        }
}
