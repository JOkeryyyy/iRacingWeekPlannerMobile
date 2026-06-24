package com.iracingweekplanner.mobile.data.repository

import com.iracingweekplanner.mobile.domain.model.PlannerAppInfo
import com.iracingweekplanner.mobile.domain.repository.PlannerAppInfoRepository

class StaticPlannerAppInfoRepository : PlannerAppInfoRepository {
    override fun getAppInfo(): PlannerAppInfo =
        PlannerAppInfo(
            name = "iRacing Week Planner Mobile",
            sourceSet = "shared",
        )
}
