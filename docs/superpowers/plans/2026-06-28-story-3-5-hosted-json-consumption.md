# Story 3.5 Hosted JSON Consumption Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire the app so a configured Supabase Storage manifest URL uses hosted JSON data while the zero-config developer default continues to use bundled mock JSON.

**Architecture:** Keep source selection below presentation by adding a small data-source config object and Koin module factory around the existing `PlannerDataSource` contract. Reuse `KtorPlannerHostedDataSource`, `RefreshCachePlannerDataCoordinator`, and SQLDelight cache fallback instead of adding new repository behavior. Platform source sets provide production Ktor clients and optional manifest URL configuration; `ScheduleViewModel` and `ScheduleScreen` continue to use `LoadPlannerDataUseCase` only.

**Tech Stack:** Kotlin Multiplatform, Koin 4.1.1, Ktor Client 3.5.0, SQLDelight 2.3.2, Compose Multiplatform resources, kotlin.test, Ktor MockEngine, Android manifest placeholders.

---

## File Structure

- Create `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/datasource/PlannerDataSourceConfig.kt`
  - Holds nullable hosted manifest URL configuration and normalizes blank strings to the local mock default.
- Modify `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/di/CommonAppModule.kt`
  - Keep `commonAppModule` as the local mock default for existing tests.
  - Add `plannerCommonAppModule(config)` for configured source selection.
  - Bind `PlannerDataSource` to `ComposeResourcePlannerLocalDataSource` when no hosted URL is configured.
  - Bind `PlannerDataSource` to `KtorPlannerHostedDataSource` when a hosted URL is configured and an `HttpClient` exists in Koin.
- Modify `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.kt`
  - Preserve the existing `createAppDependenciesWith(vararg platformModules)` test helper.
  - Add an overload that accepts `PlannerDataSourceConfig`.
- Create `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/di/PlannerDataSourceSelectionTest.kt`
  - Verifies local default, blank config fallback, and hosted config using the Koin-provided `HttpClient`.
- Modify `gradle/libs.versions.toml`
  - Add production Ktor engine aliases for Android and iOS.
- Modify `shared/build.gradle.kts`
  - Add `ktor-client-okhttp` to `androidMain`.
  - Add `ktor-client-darwin` to `iosMain`.
- Modify `androidApp/build.gradle.kts`
  - Add default blank `plannerHostedManifestUrl` manifest placeholder.
- Modify `androidApp/src/main/AndroidManifest.xml`
  - Add `android.permission.INTERNET`.
  - Add app metadata for `com.iracingweekplanner.mobile.HOSTED_MANIFEST_URL`.
- Modify `shared/src/androidMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.android.kt`
  - Read the Android manifest metadata.
  - Provide an OkHttp-backed `HttpClient`.
  - Pass `PlannerDataSourceConfig` into shared dependency creation.
- Modify `shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.ios.kt`
  - Add a nullable hosted URL parameter.
  - Provide a Darwin-backed `HttpClient`.
- Modify `shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/MainViewController.kt`
  - Preserve the no-argument entry point while allowing a nullable hosted URL to be passed from iOS wiring.
- Modify `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/AndroidEntryPointArchitectureAndroidHostTest.kt`
  - Guard the Android network permission and manifest metadata placeholder.
- Create `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/data/HostedPlannerDataRefreshAndroidHostTest.kt`
  - Verifies hosted success persists through SQLDelight, hosted failure falls back to cache, and hosted failure with no cache returns a source error.
- Modify `docs/development.md`
  - Add Story 3.5 configuration and focused verification commands.

## Task 1: Add Configurable Planner Data Source Selection

**Files:**
- Create: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/datasource/PlannerDataSourceConfig.kt`
- Modify: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/di/CommonAppModule.kt`
- Modify: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.kt`
- Test: `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/di/PlannerDataSourceSelectionTest.kt`

- [ ] **Step 1: Write the failing source-selection tests**

Create `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/di/PlannerDataSourceSelectionTest.kt` with this content:

```kotlin
package com.iracingweekplanner.mobile.di

import com.iracingweekplanner.mobile.data.datasource.ComposeResourcePlannerLocalDataSource
import com.iracingweekplanner.mobile.data.datasource.KtorPlannerHostedDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceFailure
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class PlannerDataSourceSelectionTest {

    @Test
    fun defaultPlannerSourceUsesBundledLocalMockResources() {
        val koinApplication = koinApplication {
            modules(plannerCommonAppModule())
        }

        try {
            assertIs<ComposeResourcePlannerLocalDataSource>(
                koinApplication.koin.get<PlannerDataSource>(),
            )
        } finally {
            koinApplication.close()
        }
    }

    @Test
    fun blankHostedManifestUrlUsesBundledLocalMockResources() {
        val koinApplication = koinApplication {
            modules(
                plannerCommonAppModule(
                    PlannerDataSourceConfig(hostedManifestUrl = "   "),
                ),
            )
        }

        try {
            assertIs<ComposeResourcePlannerLocalDataSource>(
                koinApplication.koin.get<PlannerDataSource>(),
            )
        } finally {
            koinApplication.close()
        }
    }

    @Test
    fun configuredHostedManifestUrlUsesHostedSourceWithKoinHttpClient() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    requestedUrls += request.url.toString()
                    respondError(HttpStatusCode.ServiceUnavailable)
                }
            }
        }
        val koinApplication = koinApplication {
            modules(
                plannerCommonAppModule(
                    PlannerDataSourceConfig(
                        hostedManifestUrl = "  $BaseManifestUrl  ",
                    ),
                ),
                module {
                    single { httpClient }
                },
            )
        }

        try {
            val source = koinApplication.koin.get<PlannerDataSource>()

            assertIs<KtorPlannerHostedDataSource>(source)
            val failure = assertFailure(source.loadPlannerData())
            assertEquals(BaseManifestUrl, requestedUrls.single())
            assertEquals(BaseManifestUrl, failure.path)
            assertEquals(
                PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE,
                failure.reason,
            )
        } finally {
            koinApplication.close()
            httpClient.close()
        }
    }

    private fun assertFailure(result: PlannerDataSourceResult): PlannerDataSourceFailure =
        when (result) {
            is PlannerDataSourceResult.Failure -> result.failure
            is PlannerDataSourceResult.Loaded -> fail("Expected hosted source failure, got $result")
        }

    private companion object {
        const val BaseManifestUrl =
            "https://example.supabase.co/storage/v1/object/public/planner-data/data/mobile/v1/manifest.json"
    }
}
```

- [ ] **Step 2: Run the failing source-selection tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.di.PlannerDataSourceSelectionTest
```

Expected: FAIL because `PlannerDataSourceConfig` and `plannerCommonAppModule` do not exist yet.

- [ ] **Step 3: Add the source configuration type**

Create `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/datasource/PlannerDataSourceConfig.kt` with this content:

```kotlin
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
```

- [ ] **Step 4: Replace the common app module with a default val plus configurable factory**

Replace `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/di/CommonAppModule.kt` with this content:

```kotlin
package com.iracingweekplanner.mobile.di

import com.iracingweekplanner.mobile.data.datasource.ComposeResourcePlannerLocalDataSource
import com.iracingweekplanner.mobile.data.datasource.KtorPlannerHostedDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.data.local.PlannerLocalDataStore
import com.iracingweekplanner.mobile.data.local.SqlDelightPlannerLocalDataStore
import com.iracingweekplanner.mobile.data.repository.PlannerDataRefreshCoordinator
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerCarRepository
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataCoordinator
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataRepository
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerScheduleRepository
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerTrackRepository
import com.iracingweekplanner.mobile.data.repository.StaticPlannerAppInfoRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerAppInfoRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerCarRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerDataRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerScheduleRepository
import com.iracingweekplanner.mobile.domain.repository.PlannerTrackRepository
import com.iracingweekplanner.mobile.domain.usecase.GetAppInfoUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerCarsUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerRacesUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerTracksUseCase
import com.iracingweekplanner.mobile.domain.usecase.LoadRaceWeeksUseCase
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

val commonAppModule: Module = plannerCommonAppModule()

fun plannerCommonAppModule(
    plannerDataSourceConfig: PlannerDataSourceConfig = PlannerDataSourceConfig.LocalMock,
): Module = module {
    single<PlannerAppInfoRepository> { StaticPlannerAppInfoRepository() }
    factory { GetAppInfoUseCase(repository = get()) }
    factory { AppInfoStateHolder(getAppInfo = get()) }

    single {
        Json {
            ignoreUnknownKeys = false
        }
    }
    single<PlannerDataSource> {
        createPlannerDataSource(
            config = plannerDataSourceConfig,
            json = get(),
            httpClient = { get() },
        )
    }
    single<PlannerLocalDataStore> { SqlDelightPlannerLocalDataStore(database = get()) }
    single<PlannerDataRefreshCoordinator> {
        RefreshCachePlannerDataCoordinator(
            source = get(),
            localDataStore = get(),
        )
    }
    single<PlannerScheduleRepository> { RefreshCachePlannerScheduleRepository(coordinator = get()) }
    single<PlannerCarRepository> { RefreshCachePlannerCarRepository(coordinator = get()) }
    single<PlannerTrackRepository> { RefreshCachePlannerTrackRepository(coordinator = get()) }
    single<PlannerDataRepository> { RefreshCachePlannerDataRepository(coordinator = get()) }
    factory { LoadRaceWeeksUseCase(repository = get()) }
    factory { LoadPlannerRacesUseCase(repository = get()) }
    factory { LoadPlannerCarsUseCase(repository = get()) }
    factory { LoadPlannerTracksUseCase(repository = get()) }
    factory { LoadPlannerDataUseCase(repository = get()) }
}

private fun createPlannerDataSource(
    config: PlannerDataSourceConfig,
    json: Json,
    httpClient: () -> HttpClient,
): PlannerDataSource {
    val hostedManifestUrl = config.normalizedHostedManifestUrl
        ?: return ComposeResourcePlannerLocalDataSource(json = json)

    return KtorPlannerHostedDataSource(
        manifestUrl = hostedManifestUrl,
        httpClient = httpClient(),
        json = json,
    )
}
```

- [ ] **Step 5: Add a configured dependency-creation overload without breaking existing test helpers**

Replace `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.kt` with this content:

```kotlin
package com.iracingweekplanner.mobile.platform

import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.di.plannerCommonAppModule
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import com.iracingweekplanner.mobile.presentation.AppInfoStateHolder
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication

class AppDependencies internal constructor(
    private val koinApplication: KoinApplication,
) {
    val appInfoStateHolder: AppInfoStateHolder = koinApplication.koin.get()
    val loadPlannerData: LoadPlannerDataUseCase = koinApplication.koin.get()

    fun close() {
        koinApplication.close()
    }
}

internal fun createAppDependenciesWith(vararg platformModules: Module): AppDependencies =
    createAppDependenciesWith(
        PlannerDataSourceConfig.LocalMock,
        *platformModules,
    )

internal fun createAppDependenciesWith(
    plannerDataSourceConfig: PlannerDataSourceConfig,
    vararg platformModules: Module,
): AppDependencies =
    AppDependencies(
        koinApplication = koinApplication {
            modules(listOf(plannerCommonAppModule(plannerDataSourceConfig)) + platformModules)
        },
    )
```

- [ ] **Step 6: Run the focused source-selection tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.di.PlannerDataSourceSelectionTest
```

Expected: PASS.

- [ ] **Step 7: Run existing DI regression tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.di.CommonAppModuleTest --tests com.iracingweekplanner.mobile.di.CommonPlannerDataModuleAndroidHostTest
```

Expected: PASS. This confirms the existing `commonAppModule` default still resolves bundled local mock JSON and existing app dependency helpers still work.

- [ ] **Step 8: Commit**

```bash
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/datasource/PlannerDataSourceConfig.kt shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/di/CommonAppModule.kt shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.kt shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/di/PlannerDataSourceSelectionTest.kt
git commit -m "feat: add planner data source selection config"
```

## Task 2: Add Platform Ktor Clients and Hosted URL Configuration

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`
- Modify: `androidApp/build.gradle.kts`
- Modify: `androidApp/src/main/AndroidManifest.xml`
- Modify: `shared/src/androidMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.android.kt`
- Modify: `shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.ios.kt`
- Modify: `shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/MainViewController.kt`
- Test: `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/AndroidEntryPointArchitectureAndroidHostTest.kt`

- [ ] **Step 1: Write the failing Android manifest guard**

Update `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/AndroidEntryPointArchitectureAndroidHostTest.kt` so the test method reads `androidApp/build.gradle.kts` and checks the network permission plus hosted metadata. Use this full file content:

```kotlin
package com.iracingweekplanner.mobile.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidEntryPointArchitectureAndroidHostTest {

    @Test
    fun androidAppDependenciesAreOwnedByApplicationNotActivity() {
        val androidPackageRoot = androidAppPackageRoot()
        val applicationFile = androidPackageRoot.resolve("IRacingWeekPlannerApplication.kt")
        val activityFile = androidPackageRoot.resolve("MainActivity.kt")
        val manifestFile = repoRoot().resolve("androidApp/src/main/AndroidManifest.xml")
        val androidBuildFile = repoRoot().resolve("androidApp/build.gradle.kts")

        assertTrue(
            actual = Files.exists(applicationFile),
            message = "Expected Android app dependencies to be owned by an Application class.",
        )

        val applicationSource = readText(applicationFile)
        val activitySource = readText(activityFile)
        val manifestSource = readText(manifestFile)
        val androidBuildSource = readText(androidBuildFile)

        assertTrue(
            actual = manifestSource.contains("""android:name=".IRacingWeekPlannerApplication""""),
            message = "Expected AndroidManifest.xml to register IRacingWeekPlannerApplication.",
        )
        assertTrue(
            actual = manifestSource.contains(
                """<uses-permission android:name="android.permission.INTERNET" />""",
            ),
            message = "Expected AndroidManifest.xml to declare Internet permission for hosted JSON reads.",
        )
        assertTrue(
            actual = manifestSource.contains(
                "android:name=\"com.iracingweekplanner.mobile.HOSTED_MANIFEST_URL\"",
            ),
            message = "Expected AndroidManifest.xml to expose hosted manifest URL metadata.",
        )
        assertTrue(
            actual = manifestSource.contains("android:value=\"${'$'}{plannerHostedManifestUrl}\""),
            message = "Expected hosted manifest metadata to read from the Gradle manifest placeholder.",
        )
        assertTrue(
            actual = androidBuildSource.contains(
                "providers.gradleProperty(\"plannerHostedManifestUrl\").orElse(\"\").get()",
            ),
            message = "Expected Android defaultConfig to read the hosted manifest URL from a Gradle property with a blank default.",
        )
        assertTrue(
            actual = applicationSource.contains("class IRacingWeekPlannerApplication : Application()"),
            message = "Expected IRacingWeekPlannerApplication to extend android.app.Application.",
        )
        assertTrue(
            actual = applicationSource.contains("createAppDependencies(this)"),
            message = "Expected IRacingWeekPlannerApplication to create shared app dependencies.",
        )
        assertFalse(
            actual = activitySource.contains("createAppDependencies("),
            message = "MainActivity should consume Application-owned dependencies, not create them.",
        )
        assertFalse(
            actual = activitySource.contains("appDependencies.close()"),
            message = "MainActivity should not close app-lifetime dependencies.",
        )
    }

    private fun androidAppPackageRoot(): Path =
        repoRoot()
            .resolve("androidApp/src/main/kotlin/com/iracingweekplanner/mobile")

    private fun readText(file: Path): String =
        Files.readAllLines(file).joinToString(separator = "\n")

    private fun repoRoot(): Path {
        val cwd = Path("").toAbsolutePath()
        return when {
            Files.isDirectory(cwd.resolve("androidApp/src/main/kotlin")) -> cwd
            Files.isDirectory(cwd.resolve("../androidApp/src/main/kotlin")) -> cwd.resolve("..").normalize()
            else -> error("Could not locate repository root from $cwd")
        }
    }
}
```

- [ ] **Step 2: Run the failing Android manifest guard**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.architecture.AndroidEntryPointArchitectureAndroidHostTest
```

Expected: FAIL because the Android manifest has no Internet permission, no hosted metadata, and the Android app Gradle file has no placeholder yet.

- [ ] **Step 3: Add Ktor production engine aliases**

In `gradle/libs.versions.toml`, add these library entries beside the existing Ktor aliases:

```toml
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
```

- [ ] **Step 4: Add platform Ktor engine dependencies**

In `shared/build.gradle.kts`, update the platform source-set dependencies to this shape:

```kotlin
androidMain.dependencies {
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.sqldelight.android.driver)
}
iosMain.dependencies {
    implementation(libs.ktor.client.darwin)
    implementation(libs.sqldelight.native.driver)
}
```

- [ ] **Step 5: Add the Android manifest placeholder default**

In `androidApp/build.gradle.kts`, update `defaultConfig` to include a blank hosted URL by default:

```kotlin
defaultConfig {
    applicationId = "com.iracingweekplanner.mobile"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
    manifestPlaceholders["plannerHostedManifestUrl"] =
        providers.gradleProperty("plannerHostedManifestUrl").orElse("").get()
}
```

- [ ] **Step 6: Add Android Internet permission and hosted manifest metadata**

Replace `androidApp/src/main/AndroidManifest.xml` with this content:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:name=".IRacingWeekPlannerApplication"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <meta-data
            android:name="com.iracingweekplanner.mobile.HOSTED_MANIFEST_URL"
            android:value="${plannerHostedManifestUrl}" />

        <activity
            android:exported="true"
            android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 7: Wire Android hosted config and OkHttp client**

Replace `shared/src/androidMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.android.kt` with this content:

```kotlin
package com.iracingweekplanner.mobile.platform

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

fun createAppDependencies(context: Context): AppDependencies =
    createAppDependenciesWith(
        PlannerDataSourceConfig(
            hostedManifestUrl = context.hostedPlannerManifestUrl(),
        ),
        androidPlannerDatabaseModule(context.applicationContext),
        androidPlannerNetworkModule(),
    )

private fun androidPlannerDatabaseModule(context: Context): Module = module {
    single {
        PlannerDatabase(
            AndroidSqliteDriver(
                schema = PlannerDatabase.Schema,
                context = context,
                name = PLANNER_DATABASE_NAME,
            ),
        )
    }
}

private fun androidPlannerNetworkModule(): Module = module {
    single { HttpClient(OkHttp) }
}

private fun Context.hostedPlannerManifestUrl(): String? {
    val applicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getApplicationInfo(
            packageName,
            PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    }

    return applicationInfo.metaData
        ?.getString(HOSTED_MANIFEST_URL_METADATA)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

private const val PLANNER_DATABASE_NAME = "planner.db"
private const val HOSTED_MANIFEST_URL_METADATA =
    "com.iracingweekplanner.mobile.HOSTED_MANIFEST_URL"
```

- [ ] **Step 8: Wire iOS hosted config and Darwin client**

Replace `shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.ios.kt` with this content:

```kotlin
package com.iracingweekplanner.mobile.platform

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSourceConfig
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

fun createAppDependencies(hostedManifestUrl: String? = null): AppDependencies =
    createAppDependenciesWith(
        PlannerDataSourceConfig(hostedManifestUrl = hostedManifestUrl),
        iosPlannerDatabaseModule(),
        iosPlannerNetworkModule(),
    )

private fun iosPlannerDatabaseModule(): Module = module {
    single {
        PlannerDatabase(
            NativeSqliteDriver(
                schema = PlannerDatabase.Schema,
                name = PLANNER_DATABASE_NAME,
            ),
        )
    }
}

private fun iosPlannerNetworkModule(): Module = module {
    single { HttpClient(Darwin) }
}

private const val PLANNER_DATABASE_NAME = "planner.db"
```

- [ ] **Step 9: Preserve the iOS no-argument UI entry while allowing config injection**

Replace `shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/MainViewController.kt` with this content:

```kotlin
package com.iracingweekplanner.mobile

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.iracingweekplanner.mobile.platform.createAppDependencies
import com.iracingweekplanner.mobile.presentation.App

fun MainViewController(
    hostedManifestUrl: String? = null,
) = ComposeUIViewController {
    val appDependencies = remember(hostedManifestUrl) {
        createAppDependencies(hostedManifestUrl = hostedManifestUrl)
    }
    DisposableEffect(appDependencies) {
        onDispose {
            appDependencies.close()
        }
    }

    App(
        loadPlannerData = appDependencies.loadPlannerData,
    )
}
```

- [ ] **Step 10: Run the Android manifest guard**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.architecture.AndroidEntryPointArchitectureAndroidHostTest
```

Expected: PASS.

- [ ] **Step 11: Run platform compile/build checks**

Run:

```bash
./gradlew :shared:compileKotlinAndroid :androidApp:assembleDebug
```

Expected: PASS. This verifies the Android OkHttp engine dependency, manifest placeholder, and app manifest compile.

- [ ] **Step 12: Commit**

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts androidApp/build.gradle.kts androidApp/src/main/AndroidManifest.xml shared/src/androidMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.android.kt shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/platform/AppDependencies.ios.kt shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/MainViewController.kt shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/AndroidEntryPointArchitectureAndroidHostTest.kt
git commit -m "feat: wire hosted planner source platform config"
```

## Task 3: Prove Hosted Loads Use the Existing SQLDelight Refresh Cache Path

**Files:**
- Create: `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/data/HostedPlannerDataRefreshAndroidHostTest.kt`

- [ ] **Step 1: Write the failing hosted refresh/cache integration tests**

Create `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/data/HostedPlannerDataRefreshAndroidHostTest.kt` with this content:

```kotlin
package com.iracingweekplanner.mobile.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.iracingweekplanner.mobile.data.datasource.KtorPlannerHostedDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSource
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import com.iracingweekplanner.mobile.data.local.SqlDelightPlannerLocalDataStore
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataCoordinator
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataRepository
import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataError
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HostedPlannerDataRefreshAndroidHostTest {

    @Test
    fun hostedSuccessPersistsThroughSqlDelightRefreshCachePath() = runBlocking {
        val database = createInMemoryPlannerDatabase()
        val repository = hostedRepository(
            source = hostedSource(
                httpClient = hostedSuccessClient(revision = "fresh"),
            ),
            database = database,
        )

        val loaded = assertLoaded(repository.loadPlannerData())

        assertEquals(PlannerDataFreshness.FRESH, loaded.freshness)
        assertEquals("Hosted Series", loaded.data.series.single().name)
        assertEquals("Hosted Track", loaded.data.tracks.single().displayName)

        val cached = assertLoaded(
            hostedRepository(
                source = hostedSource(httpClient = hostedFailureClient()),
                database = database,
            ).loadPlannerData(),
        )
        assertEquals(PlannerDataFreshness.CACHED, cached.freshness)
        assertEquals("Hosted Series", cached.data.series.single().name)
        assertEquals("Hosted Track", cached.data.tracks.single().displayName)
    }

    @Test
    fun hostedRefreshFailureUsesLastSuccessfulSqlDelightDataset() = runBlocking {
        val database = createInMemoryPlannerDatabase()
        val firstLoad = hostedRepository(
            source = hostedSource(
                httpClient = hostedSuccessClient(revision = "cached-revision"),
            ),
            database = database,
        )
        assertLoaded(firstLoad.loadPlannerData())

        val secondLoad = hostedRepository(
            source = hostedSource(httpClient = hostedFailureClient()),
            database = database,
        )

        val loaded = assertLoaded(secondLoad.loadPlannerData())

        assertEquals(PlannerDataFreshness.CACHED, loaded.freshness)
        assertEquals("Hosted Series", loaded.data.series.single().name)
        assertEquals("car-hosted", loaded.data.cars.single().id.value)
    }

    @Test
    fun hostedRefreshFailureWithoutCacheReturnsSourceUnavailable() = runBlocking {
        val repository = hostedRepository(
            source = hostedSource(httpClient = hostedFailureClient()),
            database = createInMemoryPlannerDatabase(),
        )

        val failure = assertFailure(repository.loadPlannerData())

        assertEquals(
            PlannerDataError.SourceUnavailable(
                path = "$BaseUrl/manifest.json",
                detail = "Source resource is unavailable",
            ),
            failure.error,
        )
    }

    private fun hostedRepository(
        source: PlannerDataSource,
        database: PlannerDatabase,
    ): RefreshCachePlannerDataRepository =
        RefreshCachePlannerDataRepository(
            coordinator = RefreshCachePlannerDataCoordinator(
                source = source,
                localDataStore = SqlDelightPlannerLocalDataStore(database),
            ),
        )

    private fun hostedSource(httpClient: HttpClient): KtorPlannerHostedDataSource =
        KtorPlannerHostedDataSource(
            manifestUrl = "$BaseUrl/manifest.json",
            httpClient = httpClient,
        )

    private fun hostedSuccessClient(revision: String): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.toString()) {
                        "$BaseUrl/manifest.json" -> respondJson(manifestJson(revision))
                        "$BaseUrl/releases/2026-s3/$revision/season.json" -> respondJson(SeasonJson)
                        "$BaseUrl/releases/2026-s3/$revision/cars.json" -> respondJson(CarsJson)
                        "$BaseUrl/releases/2026-s3/$revision/tracks.json" -> respondJson(TracksJson)
                        else -> respondError(HttpStatusCode.NotFound)
                    }
                }
            }
        }

    private fun hostedFailureClient(): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler {
                    respondError(HttpStatusCode.ServiceUnavailable)
                }
            }
        }

    private fun createInMemoryPlannerDatabase(): PlannerDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PlannerDatabase.Schema.create(driver)
        return PlannerDatabase(driver)
    }

    private fun io.ktor.client.engine.mock.MockRequestHandleScope.respondJson(
        content: String,
    ) = respond(
        content = content,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private fun assertLoaded(
        result: PlannerDataResult<PlannerData>,
    ): PlannerDataResult.Loaded<PlannerData> =
        when (result) {
            is PlannerDataResult.Loaded -> result
            is PlannerDataResult.Failure -> fail("Expected loaded planner data, got $result")
        }

    private fun assertFailure(
        result: PlannerDataResult<PlannerData>,
    ): PlannerDataResult.Failure =
        when (result) {
            is PlannerDataResult.Failure -> result
            is PlannerDataResult.Loaded -> fail("Expected planner data failure, got $result")
        }

    private companion object {
        const val BaseUrl =
            "https://project-ref.supabase.co/storage/v1/object/public/planner-data/data/mobile/v1"

        fun manifestJson(revision: String): String =
            """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-26T15:05:47Z",
              "seasonId": "2026-s3",
              "revision": "$revision",
              "seasonFile": "releases/2026-s3/$revision/season.json",
              "carsFile": "releases/2026-s3/$revision/cars.json",
              "tracksFile": "releases/2026-s3/$revision/tracks.json"
            }
            """.trimIndent()

        val SeasonJson =
            """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-26T15:05:47Z",
              "seasonId": "2026-s3",
              "seasonName": "2026 Season 3",
              "seasonStart": "2026-06-16T00:00:00Z",
              "seasonEnd": "2026-09-08T00:00:00Z",
              "weekSeasonStart": "2026-06-16T00:00:00Z",
              "weeks": [
                {
                  "weekNumber": 1,
                  "startsAt": "2026-06-16T00:00:00Z",
                  "endsAt": "2026-06-23T00:00:00Z"
                }
              ],
              "series": [
                {
                  "seriesId": "series-hosted",
                  "name": "Hosted Series",
                  "category": "Sports Car",
                  "license": {
                    "className": "Class D",
                    "safetyRating": 4.0,
                    "raw": "Class D 4.0"
                  },
                  "isOfficial": true,
                  "setupType": "fixed",
                  "setupSource": "parser",
                  "startType": "standing",
                  "startTypeSource": "parser",
                  "raceIds": [
                    "race-hosted-week-1"
                  ]
                }
              ],
              "races": [
                {
                  "raceId": "race-hosted-week-1",
                  "seriesId": "series-hosted",
                  "seriesName": "Hosted Series",
                  "category": "Sports Car",
                  "weekNumber": 1,
                  "startsAt": "2026-06-16T00:00:00Z",
                  "endsAt": "2026-06-23T00:00:00Z",
                  "trackPackageId": "track-hosted",
                  "trackName": "Hosted Track",
                  "trackConfigName": "GP",
                  "carSkus": [
                    "car-hosted"
                  ],
                  "carClasses": [
                    "Hosted Class"
                  ],
                  "setupType": "fixed",
                  "setupSource": "parser",
                  "startType": "standing",
                  "startTypeSource": "parser",
                  "raceLength": {
                    "laps": 10
                  },
                  "precipChance": 0,
                  "sessions": [
                    {
                      "type": "recurring",
                      "firstSessionOffsetMinutes": 15,
                      "repeatEveryMinutes": 60
                    }
                  ]
                }
              ]
            }
            """.trimIndent()

        val CarsJson =
            """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-26T15:05:47Z",
              "cars": [
                {
                  "sku": "car-hosted",
                  "displayName": "Hosted Car",
                  "sourceCarId": 101,
                  "sourceSkuName": "Hosted Car",
                  "categories": [
                    "Sports Car"
                  ],
                  "carClasses": [
                    "Hosted Class"
                  ],
                  "freeWithSubscription": true,
                  "imageUrl": "https://example.test/hosted-car.png"
                }
              ]
            }
            """.trimIndent()

        val TracksJson =
            """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-26T15:05:47Z",
              "tracks": [
                {
                  "packageId": "track-hosted",
                  "displayName": "Hosted Track",
                  "sourceTrackIds": [
                    201
                  ],
                  "type": "road",
                  "supportedTypes": [
                    "road"
                  ],
                  "isDefaultContent": true,
                  "mapUrl": "https://example.test/hosted-track-map.png",
                  "imageUrl": "https://example.test/hosted-track.png"
                }
              ]
            }
            """.trimIndent()
    }
}
```

- [ ] **Step 2: Run the failing hosted integration tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.data.HostedPlannerDataRefreshAndroidHostTest
```

Expected before Task 1 and Task 2 are complete: FAIL because hosted source config/platform wiring is not complete. Expected after Task 1 and Task 2 are complete: PASS.

- [ ] **Step 3: Run the existing hosted data-source safety tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.data.datasource.KtorPlannerHostedDataSourceTest
```

Expected: PASS. This confirms `manifest.json` is requested first, relative `seasonFile`/`carsFile`/`tracksFile` references resolve under the manifest directory, and unsafe references are rejected.

- [ ] **Step 4: Run the existing refresh/cache coordinator tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataCoordinatorTest
```

Expected: PASS. This confirms repository cache fallback remains intact for source failures, decode failures, and invalid source data.

- [ ] **Step 5: Run the schedule presentation safety tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.presentation.schedule.ScheduleViewModelTest
```

Expected: PASS. This confirms no raw source URL, DTO field detail, Ktor detail, SQLDelight detail, or local file path is exposed through Schedule presentation copy.

- [ ] **Step 6: Commit**

```bash
git add shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/data/HostedPlannerDataRefreshAndroidHostTest.kt
git commit -m "test: cover hosted planner refresh cache path"
```

## Task 4: Document Story 3.5 Configuration and Verification

**Files:**
- Modify: `docs/development.md`

- [ ] **Step 1: Add Story 3.5 development documentation**

In `docs/development.md`, add this section after the Story 3.4 focused command block:

````markdown
Story 3.5 hosted JSON consumption coverage:

- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/di/PlannerDataSourceSelectionTest.kt` verifies local mock fallback and hosted source selection.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/datasource/KtorPlannerHostedDataSourceTest.kt` verifies hosted manifest-first loading, relative reference resolution, HTTP/decode failures, and unsafe reference rejection.
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/data/HostedPlannerDataRefreshAndroidHostTest.kt` verifies hosted success persists through SQLDelight, hosted refresh failure returns cached data, and no-cache hosted failure returns a source error.
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/AndroidEntryPointArchitectureAndroidHostTest.kt` verifies Android keeps app dependencies in `Application`, declares Internet permission, and leaves the hosted manifest URL blank by default.

Focused commands:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.di.PlannerDataSourceSelectionTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.data.datasource.KtorPlannerHostedDataSourceTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.data.HostedPlannerDataRefreshAndroidHostTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.architecture.AndroidEntryPointArchitectureAndroidHostTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.presentation.schedule.ScheduleViewModelTest
```

Android hosted JSON configuration:

```bash
./gradlew :androidApp:assembleDebug -PplannerHostedManifestUrl=https://<project-ref>.supabase.co/storage/v1/object/public/planner-data/data/mobile/v1/manifest.json
```

The default Android manifest placeholder is blank, so local developer builds use bundled mock JSON unless the hosted manifest URL is supplied. The actual Supabase project ref is environment configuration and must not be hardcoded in common shared code.

iOS hosted JSON configuration:

`MainViewController(hostedManifestUrl: String? = null)` preserves the default bundled mock JSON path. A Swift or Info.plist wiring change can pass the Supabase Storage manifest URL into `MainViewController(hostedManifestUrl:)` when a production iOS configuration is ready.
````

- [ ] **Step 2: Run docs grep checks**

Run:

```bash
rg -n "Story 3\\.5|plannerHostedManifestUrl|HOSTED_MANIFEST_URL|MainViewController\\(hostedManifestUrl" docs/development.md androidApp/src/main/AndroidManifest.xml shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/MainViewController.kt
```

Expected: output includes the new Story 3.5 docs, the Android metadata name, the Gradle placeholder name, and the iOS nullable entry point.

- [ ] **Step 3: Commit**

```bash
git add docs/development.md
git commit -m "docs: document hosted planner JSON configuration"
```

## Task 5: Final Verification

**Files:**
- Verify: all Story 3.5 files

- [ ] **Step 1: Run the full shared Android host test suite**

Run:

```bash
./gradlew :shared:testAndroidHostTest
```

Expected: PASS.

- [ ] **Step 2: Build the Android debug app**

Run:

```bash
./gradlew :androidApp:assembleDebug
```

Expected: PASS.

- [ ] **Step 3: Run iOS simulator tests when full Xcode is available**

Run:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```

Expected when full Xcode is installed and selected: PASS. If the command fails because the local machine has only Command Line Tools or an invalid Xcode selection, record the exact failure in the final handoff and do not claim iOS verification passed.

- [ ] **Step 4: Check for hardcoded production Supabase URLs**

Run:

```bash
rg -n "supabase\\.co/storage/v1/object/public/planner-data|project-ref|plannerHostedManifestUrl|HOSTED_MANIFEST_URL" shared androidApp docs
```

Expected:

- `project-ref` appears only in tests or documentation examples.
- `plannerHostedManifestUrl` appears in Android Gradle/manifest/docs.
- `HOSTED_MANIFEST_URL` appears in Android platform wiring and tests.
- No real production Supabase project ref appears in `shared/src/commonMain`.

- [ ] **Step 5: Check diff cleanliness**

Run:

```bash
git diff --check
```

Expected: no output.

- [ ] **Step 6: Commit any verification-only doc adjustment**

If the iOS command is blocked and a doc or handoff note is added, commit that exact adjustment:

```bash
git add docs/development.md
git commit -m "docs: record hosted planner verification notes"
```

Skip this commit if no file changed in this step.

## Acceptance Criteria Coverage

- Configured Supabase Storage manifest URL: Task 1 source config, Task 2 platform wiring, Task 3 hosted integration.
- No hardcoded production URL in common shared code: Task 1 config object, Task 2 manifest placeholder/nullable iOS parameter, Task 5 grep check.
- No configured URL falls back to bundled mock JSON: Task 1 tests and default `commonAppModule`.
- Source selection owned by DI/platform/data wiring: Task 1 `plannerCommonAppModule`, Task 2 platform `createAppDependencies`.
- Android and iOS production Ktor clients: Task 2 OkHttp and Darwin modules.
- Android network permission: Task 2 manifest and architecture guard.
- iOS HTTPS with no broad ATS exception: Task 2 uses HTTPS URL parameter and no Info.plist ATS change.
- Manifest-first hosted loading and safe relative references: existing `KtorPlannerHostedDataSourceTest` re-run in Task 3.
- Successful hosted loads persist through SQLDelight: Task 3 hosted integration.
- Hosted failure after prior success falls back to cached SQLDelight data: Task 3 hosted integration.
- Hosted failure with no cache returns source error: Task 3 hosted integration.
- Presentation-safe Schedule error copy: Task 3 `ScheduleViewModelTest`.
- Hosted JSON remains public static schedule/catalog data only: no preference/account/scraping/Firebase files are touched.
- No publishing/upload tooling, Edge Functions, login, scraping, filtering, sorting, or race detail navigation: file structure excludes those areas and verification grep focuses only source selection/cache paths.
