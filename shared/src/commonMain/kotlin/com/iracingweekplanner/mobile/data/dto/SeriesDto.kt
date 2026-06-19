package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SeriesDto(
    val seriesId: String,
    val name: String,
    val category: String,
    val license: LicenseDto,
    val isOfficial: Boolean,
    val isFixedSetup: Boolean,
)
