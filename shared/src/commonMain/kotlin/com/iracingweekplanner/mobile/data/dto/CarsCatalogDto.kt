package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CarsCatalogDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val cars: List<CarDto>,
)
