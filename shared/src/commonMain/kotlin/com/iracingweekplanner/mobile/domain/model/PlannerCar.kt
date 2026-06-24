package com.iracingweekplanner.mobile.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class CarId(val value: String)

data class PlannerCar(
    val id: CarId,
    val displayName: String,
    val sourceCarId: Int? = null,
    val sourceSkuName: String? = null,
    val categories: Set<String> = emptySet(),
    val carClasses: Set<String> = emptySet(),
    val isFreeWithSubscription: Boolean? = null,
    val imageUrl: String? = null,
)
