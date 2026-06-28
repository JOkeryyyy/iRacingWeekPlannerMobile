package com.iracingweekplanner.mobile.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IwpAppScaffoldAndroidHostTest {

    @Test
    fun scaffoldAppliesOnlyVerticalSafeInsetsSoScreensOwnHorizontalPadding() {
        val source = readText(
            commonMainPackageRoot()
                .resolve("presentation/common/scaffold/IwpAppScaffold.kt"),
        )

        assertFalse(
            actual = source.contains("safeContentPadding()"),
            message = "IwpAppScaffold should not add top or horizontal safe padding on top of screen padding.",
        )
        assertTrue(
            actual = source.contains("WindowInsetsSides.Vertical"),
            message = "IwpAppScaffold should apply top and bottom safe insets without horizontal safe padding.",
        )
    }

    @Test
    fun bottomNavigationUsesAppBackgroundWithoutTonalOverlay() {
        val source = readText(
            commonMainPackageRoot()
                .resolve("presentation/common/components/ScheduleBottomNavigation.kt"),
        )

        assertTrue(
            actual = source.contains(".background(MaterialTheme.colorScheme.background)"),
            message = "Bottom navigation should use the same background color as the app surface.",
        )
        assertFalse(
            actual = source.contains("tonalElevation"),
            message = "Bottom navigation should not apply a tonal overlay that shifts its color.",
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
