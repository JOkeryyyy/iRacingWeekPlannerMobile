package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerAppInfo

interface PlannerAppInfoRepository {
    fun getAppInfo(): PlannerAppInfo
}
