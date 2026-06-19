package com.iracingweekplanner.mobile.domain

import kotlin.jvm.JvmInline

@JvmInline
value class SeriesId(val value: String)

@JvmInline
value class SeriesCategory(val displayName: String)

data class PlannerSeries(
    val id: SeriesId,
    val name: String,
    val category: SeriesCategory,
    val license: LicenseRequirement,
    val setup: RaceSetup,
    val isOfficial: Boolean,
)

data class LicenseRequirement(
    val className: String,
    val safetyRatingLevel: Int? = null,
)

enum class RaceSetup {
    FIXED,
    OPEN,
}
