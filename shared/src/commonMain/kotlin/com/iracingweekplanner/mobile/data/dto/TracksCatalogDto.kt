package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TracksCatalogDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val tracks: List<TrackDto>,
)
