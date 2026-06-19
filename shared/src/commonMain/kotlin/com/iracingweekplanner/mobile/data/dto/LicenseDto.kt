package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LicenseDto(
    val className: String,
    val level: Int? = null,
)
