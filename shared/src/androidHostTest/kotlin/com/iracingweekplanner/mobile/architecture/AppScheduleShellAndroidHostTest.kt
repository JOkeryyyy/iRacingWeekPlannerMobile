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
            actual = appSource.contains("ScheduleShell("),
            message = "The shared App root should open on the Sprint 3 Schedule shell.",
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
    fun scheduleShellUsesSprint3ComponentsAndResourceBackedTextBoundary() {
        val shellSource = readText(commonMainPackageRoot().resolve("presentation/schedule/ScheduleShell.kt"))

        listOf(
            "ScheduleHeader(",
            "DateWeekSelector(",
            "ScheduleChip(",
            "StatePanel(",
            "ScheduleBottomNavigation(",
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

    private fun readText(file: Path): String = Files.readAllLines(file).joinToString(separator = "\n")
}
