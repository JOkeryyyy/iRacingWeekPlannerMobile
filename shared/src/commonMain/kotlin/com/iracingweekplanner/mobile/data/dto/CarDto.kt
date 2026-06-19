package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CarDto(
    val sku: String,
    val displayName: String,
    val sourceCarId: Int? = null,
    val sourceSkuName: String? = null,
    val categories: List<String>? = null,
    val carClasses: List<String>? = null,
    val freeWithSubscription: Boolean? = null,
    val imageUrl: String? = null,
)
