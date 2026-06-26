package com.iracingweekplanner.mobile.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppScheduleShellAndroidHostTest {

    @Test
    fun sharedAppRootUsesScheduleShellInsteadOfStarterComposeSurface() {
        val appSource = readText(commonMainPackageRoot().resolve("presentation/App.kt"))

        assertTrue(
            actual = appSource.contains("ScheduleRoot("),
            message = "The shared App root should open on the Sprint 3 Schedule MVI root.",
        )
        assertFalse(
            actual = appSource.contains("Click me!"),
            message = "The starter click-counter UI should be removed from the main app surface.",
        )
        assertFalse(
            actual = appSource.contains("compose_multiplatform"),
            message = "The starter Compose logo resource should not be used by the main app surface.",
        )
        assertFalse(
            actual = appSource.contains("AppInfoStateHolder"),
            message = "The root Schedule shell should not depend on the old app-info starter state holder.",
        )
    }

    @Test
    fun platformEntryPointsPassPlannerDataStateHolderIntoSharedAppRoot() {
        val androidSource = readText(projectRoot().resolve("androidApp/src/main/kotlin/com/iracingweekplanner/mobile/MainActivity.kt"))
        val iosSource = readText(sharedProjectRoot().resolve("src/iosMain/kotlin/com/iracingweekplanner/mobile/MainViewController.kt"))

        assertTrue(
            actual = androidSource.contains("appDependencies.plannerDataStateHolder"),
            message = "Android should pass the existing planner data state holder into the shared Schedule root.",
        )
        assertTrue(
            actual = iosSource.contains("appDependencies.plannerDataStateHolder"),
            message = "iOS should pass the existing planner data state holder into the shared Schedule root.",
        )
        assertFalse(
            actual = androidSource.contains("PlannerDataSource") || iosSource.contains("PlannerDataSource"),
            message = "Platform UI entry points should not construct planner data sources directly.",
        )
    }

    @Test
    fun scheduleShellUsesSprint3ComponentsAndResourceBackedTextBoundary() {
        val shellSource = readText(commonMainPackageRoot().resolve("presentation/schedule/ScheduleShell.kt"))

        listOf(
            "ScheduleHeader(",
            "DateWeekSelector(",
            "ScheduleChip(",
            "StatePanel(",
        ).forEach { call ->
            assertTrue(
                actual = shellSource.contains(call),
                message = "ScheduleShell should render $call as part of the shared app shell.",
            )
        }

        assertTrue(
            actual = shellSource.contains("ScheduleTextResources.headerContent("),
            message = "ScheduleShell should use the Schedule text resource boundary for the week title.",
        )
        assertTrue(
            actual = shellSource.contains("weekNumber = selectedWeekNumber"),
            message = "The shell header should be derived from the selected race week.",
        )
        assertTrue(
            actual = shellSource.contains("ScheduleTextResources.bottomTabs()"),
            message = "Bottom navigation labels should come from the Schedule text resource boundary.",
        )
    }

    @Test
    fun appScaffoldOwnsRootSurfaceAndSafeAreaInsteadOfScheduleShell() {
        val scaffoldFile = commonMainPackageRoot().resolve("presentation/common/scaffold/IwpAppScaffold.kt")
        assertTrue(
            actual = Files.exists(scaffoldFile),
            message = "Expected a shared app scaffold so screens do not recreate the root Surface.",
        )

        val scaffoldSource = readText(scaffoldFile)
        val shellSource = readText(commonMainPackageRoot().resolve("presentation/schedule/ScheduleShell.kt"))

        assertTrue(
            actual = scaffoldSource.contains("Surface("),
            message = "IwpAppScaffold should own the app-level background Surface.",
        )
        assertTrue(
            actual = scaffoldSource.contains("WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)"),
            message = "IwpAppScaffold should own app-level vertical safe-area padding.",
        )
        assertFalse(
            actual = scaffoldSource.contains("presentation.schedule"),
            message = "The generic app scaffold should not depend on the Schedule feature package.",
        )
        assertFalse(
            actual = shellSource.contains("Surface("),
            message = "ScheduleShell should not recreate the app-level Surface.",
        )
        assertFalse(
            actual = shellSource.contains("safeContentPadding()"),
            message = "ScheduleShell should not own app-level safe-area padding.",
        )
    }

    @Test
    fun scheduleBottomNavigationIsPlacedInTheScaffoldBottomBarOutsideScreenPadding() {
        val shellSource = readText(commonMainPackageRoot().resolve("presentation/schedule/ScheduleShell.kt"))

        assertTrue(
            actual = shellSource.contains("IwpAppScaffold("),
            message = "ScheduleShell should use the shared app scaffold for bottom-bar placement.",
        )
        assertTrue(
            actual = shellSource.contains("bottomBar = {"),
            message = "Schedule bottom navigation should be passed through the scaffold bottomBar slot.",
        )
        assertTrue(
            actual = shellSource.contains("ScheduleBottomNavigation("),
            message = "Schedule bottom navigation should remain visible in the Schedule shell.",
        )
        assertTrue(
            actual = shellSource.contains("modifier = Modifier.padding(contentPadding)"),
            message = "Schedule content should receive screen padding separately from the bottom bar.",
        )
    }

    private fun commonMainPackageRoot(): Path =
        sharedProjectRoot()
            .resolve("src/commonMain/kotlin/com/iracingweekplanner/mobile")

    private fun sharedProjectRoot(): Path {
        val cwd = Path("").toAbsolutePath()
        return when {
            Files.isDirectory(cwd.resolve("src/commonMain/kotlin")) -> cwd
            Files.isDirectory(cwd.resolve("shared/src/commonMain/kotlin")) -> cwd.resolve("shared")
            else -> error("Could not locate shared project root from $cwd")
        }
    }

    private fun projectRoot(): Path {
        val cwd = Path("").toAbsolutePath()
        return when {
            Files.isDirectory(cwd.resolve("androidApp")) -> cwd
            Files.isDirectory(cwd.resolve("../androidApp")) -> cwd.resolve("..").normalize()
            else -> error("Could not locate project root from $cwd")
        }
    }

    private fun readText(file: Path): String = Files.readAllLines(file).joinToString(separator = "\n")
}
