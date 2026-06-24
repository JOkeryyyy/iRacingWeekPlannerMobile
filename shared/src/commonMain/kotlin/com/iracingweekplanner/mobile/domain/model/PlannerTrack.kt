package com.iracingweekplanner.mobile.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class TrackId(val value: String)

data class PlannerTrack(
    val id: TrackId,
    val displayName: String,
    val sourceTrackIds: Set<Int>,
    val primaryType: TrackType? = null,
    val supportedTypes: Set<TrackType> = emptySet(),
    val isDefaultContent: Boolean? = null,
    val mapUrl: String? = null,
    val imageUrl: String? = null,
)

enum class TrackType {
    ROAD,
    OVAL,
    DIRT_OVAL,
    DIRT_ROAD,
}
