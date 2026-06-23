package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.data.dto.CarsCatalogDto
import com.iracingweekplanner.mobile.data.dto.MobileDataManifestDto
import com.iracingweekplanner.mobile.data.dto.SeasonDto
import com.iracingweekplanner.mobile.data.dto.TracksCatalogDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class KtorPlannerHostedDataSource(
    private val manifestUrl: String,
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = false },
) : PlannerDataSource {

    override suspend fun loadPlannerData(): PlannerDataSourceResult {
        val manifest = loadDto<MobileDataManifestDto>(manifestUrl)
        if (manifest is FetchResult.Failure) return manifest.toSourceFailure()
        manifest as FetchResult.Success

        val seasonUrl = resolveManifestReference(manifest.data.seasonFile)
            ?: return invalidReference("seasonFile")
        val carsUrl = resolveManifestReference(manifest.data.carsFile)
            ?: return invalidReference("carsFile")
        val tracksUrl = resolveManifestReference(manifest.data.tracksFile)
            ?: return invalidReference("tracksFile")

        val season = loadDto<SeasonDto>(seasonUrl)
        if (season is FetchResult.Failure) return season.toSourceFailure()
        season as FetchResult.Success

        val cars = loadDto<CarsCatalogDto>(carsUrl)
        if (cars is FetchResult.Failure) return cars.toSourceFailure()
        cars as FetchResult.Success

        val tracks = loadDto<TracksCatalogDto>(tracksUrl)
        if (tracks is FetchResult.Failure) return tracks.toSourceFailure()
        tracks as FetchResult.Success

        return PlannerDataSourceResult.Loaded(
            PlannerDataBundle(
                manifest = manifest.data,
                season = season.data,
                cars = cars.data,
                tracks = tracks.data,
            ),
        )
    }

    private suspend inline fun <reified T> loadDto(url: String): FetchResult<T> {
        val text = when (val response = fetchText(url)) {
            is FetchResult.Failure -> return response
            is FetchResult.Success -> response.data
        }

        return try {
            FetchResult.Success(json.decodeFromString(text))
        } catch (error: SerializationException) {
            FetchResult.Failure(
                PlannerDataSourceFailure(
                    path = url,
                    reason = PlannerDataSourceFailure.Reason.DECODE_FAILED,
                    detail = error.message.orEmpty(),
                ),
            )
        } catch (error: IllegalArgumentException) {
            FetchResult.Failure(
                PlannerDataSourceFailure(
                    path = url,
                    reason = PlannerDataSourceFailure.Reason.DECODE_FAILED,
                    detail = error.message.orEmpty(),
                ),
            )
        }
    }

    private suspend fun fetchText(url: String): FetchResult<String> =
        try {
            val response = httpClient.get(url)
            if (response.status.value !in 200..299) {
                FetchResult.Failure(
                    PlannerDataSourceFailure(
                        path = url,
                        reason = PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE,
                        detail = "HTTP ${response.status.value} ${response.status.description}",
                    ),
                )
            } else {
                FetchResult.Success(response.bodyAsText())
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            FetchResult.Failure(
                PlannerDataSourceFailure(
                    path = url,
                    reason = PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE,
                    detail = error.message.orEmpty(),
                ),
            )
        }

    private fun resolveManifestReference(path: String): String? {
        if (!isSafeRelativeReference(path)) return null

        val manifestPath = manifestUrl
            .substringBefore('#')
            .substringBefore('?')
        val directoryEnd = manifestPath.lastIndexOf('/').takeIf { it >= 0 } ?: return path
        return manifestPath.substring(0, directoryEnd + 1) + path
    }

    private fun isSafeRelativeReference(path: String): Boolean {
        if (path.isBlank()) return false
        if (path.startsWith("/")) return false
        if ("//" in path) return false
        if (ProtocolReference.containsMatchIn(path)) return false

        return path.split('/').none { segment ->
            segment == "." || segment == ".."
        }
    }

    private fun invalidReference(fieldName: String): PlannerDataSourceResult.Failure =
        PlannerDataSourceResult.Failure(
            PlannerDataSourceFailure(
                path = fieldName,
                reason = PlannerDataSourceFailure.Reason.INVALID_REFERENCE,
                detail = "Unsafe manifest reference",
            ),
        )

    private fun FetchResult.Failure.toSourceFailure(): PlannerDataSourceResult.Failure =
        PlannerDataSourceResult.Failure(failure)

    private sealed interface FetchResult<out T> {
        data class Success<out T>(
            val data: T,
        ) : FetchResult<T>

        data class Failure(
            val failure: PlannerDataSourceFailure,
        ) : FetchResult<Nothing>
    }

    private companion object {
        val ProtocolReference = Regex("^[A-Za-z][A-Za-z0-9+.-]*:")
    }
}
