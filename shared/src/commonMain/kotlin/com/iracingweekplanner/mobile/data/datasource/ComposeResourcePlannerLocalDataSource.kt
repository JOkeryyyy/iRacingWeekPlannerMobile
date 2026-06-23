package com.iracingweekplanner.mobile.data.datasource

import com.iracingweekplanner.mobile.data.dto.CarsCatalogDto
import com.iracingweekplanner.mobile.data.dto.MobileDataManifestDto
import com.iracingweekplanner.mobile.data.dto.SeasonDto
import com.iracingweekplanner.mobile.data.dto.TracksCatalogDto
import iracingweekplannermobile.shared.generated.resources.Res
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class ComposeResourcePlannerLocalDataSource(
    private val json: Json = Json {
        ignoreUnknownKeys = false
    },
    private val readText: suspend (String) -> String = ::readMockResourceText,
) : PlannerDataSource {

    override suspend fun loadPlannerData(): PlannerDataSourceResult {
        val manifest = loadDto<MobileDataManifestDto>("manifest.json")
        if (manifest is DecodeResult.Failure) return manifest.toSourceFailure()
        manifest as DecodeResult.Success

        val season = loadDto<SeasonDto>(manifest.data.seasonFile)
        if (season is DecodeResult.Failure) return season.toSourceFailure()
        season as DecodeResult.Success

        val cars = loadDto<CarsCatalogDto>(manifest.data.carsFile)
        if (cars is DecodeResult.Failure) return cars.toSourceFailure()
        cars as DecodeResult.Success

        val tracks = loadDto<TracksCatalogDto>(manifest.data.tracksFile)
        if (tracks is DecodeResult.Failure) return tracks.toSourceFailure()
        tracks as DecodeResult.Success

        return PlannerDataSourceResult.Loaded(
            PlannerDataBundle(
                manifest = manifest.data,
                season = season.data,
                cars = cars.data,
                tracks = tracks.data,
            ),
        )
    }

    private suspend inline fun <reified T> loadDto(path: String): DecodeResult<T> {
        val text = try {
            readText(path)
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            return DecodeResult.Failure(
                PlannerDataSourceFailure(
                    path = path,
                    reason = PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE,
                    detail = error.message.orEmpty(),
                ),
            )
        }

        return try {
            DecodeResult.Success(data = json.decodeFromString(text))
        } catch (error: SerializationException) {
            DecodeResult.Failure(
                PlannerDataSourceFailure(
                    path = path,
                    reason = PlannerDataSourceFailure.Reason.DECODE_FAILED,
                    detail = error.message.orEmpty(),
                ),
            )
        } catch (error: IllegalArgumentException) {
            DecodeResult.Failure(
                PlannerDataSourceFailure(
                    path = path,
                    reason = PlannerDataSourceFailure.Reason.DECODE_FAILED,
                    detail = error.message.orEmpty(),
                ),
            )
        }
    }

    private fun DecodeResult.Failure.toSourceFailure(): PlannerDataSourceResult.Failure =
        PlannerDataSourceResult.Failure(failure)

    private sealed interface DecodeResult<out T> {
        data class Success<out T>(
            val data: T,
        ) : DecodeResult<T>

        data class Failure(
            val failure: PlannerDataSourceFailure,
        ) : DecodeResult<Nothing>
    }
}

private suspend fun readMockResourceText(path: String): String =
    Res.readBytes("files/mock-data/$path").decodeToString()
