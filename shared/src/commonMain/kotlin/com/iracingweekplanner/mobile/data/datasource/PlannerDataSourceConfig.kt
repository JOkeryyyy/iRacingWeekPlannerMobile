package com.iracingweekplanner.mobile.data.datasource

data class PlannerDataSourceConfig(
    val hostedManifestUrl: String? = null,
) {
    val normalizedHostedManifestUrl: String?
        get() = hostedManifestUrl
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    companion object {
        val LocalMock = PlannerDataSourceConfig()
    }
}
