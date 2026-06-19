package com.iracingweekplanner.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MobileDataManifestDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val seasonId: String,
    val seasonFile: String,
    val carsFile: String,
    val tracksFile: String,
    val revision: String? = null,
    val checksums: Map<String, String>? = null,
)
