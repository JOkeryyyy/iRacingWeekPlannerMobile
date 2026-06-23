package com.iracingweekplanner.mobile.domain.model

data class PlannerAppInfo(
    val name: String,
    val sourceSet: String,
) {
    val statusMessage: String = "$name shared module is ready"
}
