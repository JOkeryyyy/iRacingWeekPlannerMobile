package com.iracingweekplanner.mobile.data.mapper

import com.iracingweekplanner.mobile.domain.model.PlannerDataResult

internal inline fun <T, R> PlannerDataResult<T>.mapLoaded(
    transform: (T) -> R,
): PlannerDataResult<R> =
    when (this) {
        is PlannerDataResult.Loaded -> PlannerDataResult.Loaded(
            data = transform(data),
            freshness = freshness,
        )
        is PlannerDataResult.Failure -> this
    }
