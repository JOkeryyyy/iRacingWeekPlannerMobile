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

        assertTrue(
            actual = Files.exists(applicationFile),
            message = "Expected Android app dependencies to be owned by an Application class.",
        )

        val applicationSource = readText(applicationFile)
        val activitySource = readText(activityFile)
        val manifestSource = readText(manifestFile)

        assertTrue(
            actual = manifestSource.contains("""android:name=".IRacingWeekPlannerApplication""""),
            message = "Expected AndroidManifest.xml to register IRacingWeekPlannerApplication.",
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
