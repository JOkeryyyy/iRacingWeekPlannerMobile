package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackDto(
    val packageId: String,
    val displayName: String,
    val sourceTrackIds: List<Int>,
    val type: String? = null,
    val supportedTypes: List<String>? = null,
    val isDefaultContent: Boolean? = null,
    val mapUrl: String? = null,
    val imageUrl: String? = null,
)
