package com.iracingweekplanner.mobile.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SchedulePackageStructureAndroidHostTest {

    @Test
    fun scheduleComponentsUseDedicatedPackageOneFilePerComponentAndLocalPreview() {
        val scheduleRoot = commonMainPackageRoot().resolve("presentation/schedule")
        val componentsRoot = scheduleRoot.resolve("components")
        val expectedComponentFiles = listOf(
            "DateWeekSelector.kt",
            "RaceCard.kt",
            "ScheduleBottomNavigation.kt",
            "ScheduleChip.kt",
            "ScheduleHeader.kt",
            "StatePanel.kt",
        )

        assertFalse(
            actual = Files.exists(scheduleRoot.resolve("ScheduleComponents.kt")),
            message = "Schedule components should not be grouped in a single ScheduleComponents.kt file.",
        )
        assertTrue(
            actual = Files.isDirectory(componentsRoot),
            message = "Expected Schedule components to live in presentation/schedule/components.",
        )

        val actualComponentFiles = Files.newDirectoryStream(componentsRoot, "*.kt").use { stream ->
            stream.map { it.fileName.toString() }.sorted()
        }
        assertEquals(expectedComponentFiles, actualComponentFiles)

        expectedComponentFiles.forEach { fileName ->
            val componentName = fileName.removeSuffix(".kt")
            val source = readText(componentsRoot.resolve(fileName))

            assertTrue(
                actual = source.startsWith("package com.iracingweekplanner.mobile.presentation.schedule.components"),
                message = "$fileName should use the dedicated Schedule components package.",
            )
            assertTrue(
                actual = source.contains("fun $componentName("),
                message = "$fileName should declare the $componentName composable.",
            )
            assertTrue(
                actual = source.contains("@Preview"),
                message = "$fileName should include a meaningful Compose preview.",
            )
            assertTrue(
                actual = source.contains("fun ${componentName}Preview()"),
                message = "$fileName should declare a ${componentName}Preview function.",
            )
        }
    }

    @Test
    fun scheduleFoundationPreviewOwnsFullScreenAppBackground() {
        val previewSource = readText(
            commonMainPackageRoot()
                .resolve("presentation/schedule/preview/ScheduleUiFoundationPreview.kt"),
        )

        assertTrue(
            actual = previewSource.contains("Surface("),
            message = "Schedule foundation preview should own an app-level background surface.",
        )
        assertTrue(
            actual = previewSource.contains("Modifier.fillMaxSize()"),
            message = "Schedule foundation preview should fill the preview canvas.",
        )
        assertTrue(
            actual = previewSource.contains("color = MaterialTheme.colorScheme.background"),
            message = "Schedule foundation preview should use the app background color.",
        )
    }

    @Test
    fun schedulePreviewsUseSharedAppTheme() {
        val scheduleRoot = commonMainPackageRoot().resolve("presentation/schedule")
        val previewFiles = Files.walk(scheduleRoot).use { paths ->
            paths
                .filter { path -> path.fileName.toString().endsWith(".kt") }
                .filter { path -> readText(path).contains("@Preview") }
                .toList()
        }

        assertTrue(
            actual = previewFiles.isNotEmpty(),
            message = "Expected Schedule preview files to exist.",
        )
        previewFiles.forEach { file ->
            val source = readText(file)
            assertTrue(
                actual = source.contains("IwpAppTheme {"),
                message = "${file.fileName} previews should use the shared app theme.",
            )
            assertFalse(
                actual = source.contains("MaterialTheme {"),
                message = "${file.fileName} previews should not create raw MaterialTheme wrappers.",
            )
        }
    }

    @Test
    fun scheduleFoundationPreviewUsesPopulatedSampleAndStatePanelPreviewsAreExplicit() {
        val scheduleRoot = commonMainPackageRoot().resolve("presentation/schedule")
        val foundationPreviewSource = readText(scheduleRoot.resolve("preview/ScheduleUiFoundationPreview.kt"))
        val statePanelSource = readText(scheduleRoot.resolve("components/StatePanel.kt"))

        assertFalse(
            actual = foundationPreviewSource.contains("StatePanel("),
            message = "Schedule foundation preview should show a populated schedule, not loading/empty/error panels.",
        )
        assertTrue(
            actual = statePanelSource.contains("fun StatePanelLoadingPreview()"),
            message = "StatePanel should have an explicit loading-state preview.",
        )
        assertTrue(
            actual = statePanelSource.contains("fun StatePanelEmptyPreview()"),
            message = "StatePanel should have an explicit empty-state preview.",
        )
        assertTrue(
            actual = statePanelSource.contains("fun StatePanelPreview()"),
            message = "StatePanel should keep the component preview required by the component file contract.",
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

    private fun readText(file: Path): String =
        Files.readAllLines(file).joinToString(separator = "\n")
}
