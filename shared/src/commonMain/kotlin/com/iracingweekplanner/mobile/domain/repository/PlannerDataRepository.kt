package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult

interface PlannerDataRepository {
    suspend fun loadPlannerData(): PlannerDataResult<PlannerData>
}
