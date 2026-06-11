package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.domain.PlannerAppInfo
import com.iracingweekplanner.mobile.domain.PlannerAppInfoRepository

class StaticPlannerAppInfoRepository : PlannerAppInfoRepository {
    override fun getAppInfo(): PlannerAppInfo =
        PlannerAppInfo(
            name = "iRacing Week Planner Mobile",
            sourceSet = "shared",
        )
}
