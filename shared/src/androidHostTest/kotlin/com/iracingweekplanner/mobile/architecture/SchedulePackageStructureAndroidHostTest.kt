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
            "ScheduleButton.kt",
            "ScheduleCard.kt",
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
                actual = source.contains("@IWPPreview"),
                message = "$fileName should include the shared Schedule component preview variants.",
            )
            assertTrue(
                actual = source.contains("fun ${componentName}Preview()"),
                message = "$fileName should declare a ${componentName}Preview function.",
            )
        }
    }

    @Test
    fun scheduleButtonsUseSharedScheduleButtonComponent() {
        val componentsRoot = commonMainPackageRoot().resolve("presentation/schedule/components")
        val scheduleButtonSource = readText(componentsRoot.resolve("ScheduleButton.kt"))

        assertTrue(
            actual = scheduleButtonSource.contains("fun ScheduleButton("),
            message = "ScheduleButton should be the shared button component for Schedule actions.",
        )
        assertTrue(
            actual = scheduleButtonSource.contains("icon: ImageVector"),
            message = "ScheduleButton should be icon-first for Schedule actions.",
        )
        assertTrue(
            actual = scheduleButtonSource.contains("Icon("),
            message = "ScheduleButton should render an icon instead of visible action text.",
        )
        assertTrue(
            actual = scheduleButtonSource.contains("IconButton("),
            message = "ScheduleButton should delegate to Material icon buttons.",
        )
        assertTrue(
            actual = scheduleButtonSource.contains("FilledIconButton("),
            message = "ScheduleButton should keep a filled icon variant for primary/retry actions.",
        )
        assertFalse(
            actual = scheduleButtonSource.contains("label: String"),
            message = "ScheduleButton should not expose text-button labels until a text-button use case exists.",
        )
        assertFalse(
            actual = scheduleButtonSource.contains("enum class ScheduleButtonStyle"),
            message = "ScheduleButton should not keep the old text/filled text-button style API.",
        )
        assertFalse(
            actual = scheduleButtonSource.contains("TextButton("),
            message = "ScheduleButton should not instantiate text buttons.",
        )
        assertFalse(
            actual = scheduleButtonSource.contains("Text("),
            message = "ScheduleButton should not render visible button text.",
        )

        listOf("ScheduleHeader.kt", "DateWeekSelector.kt", "StatePanel.kt").forEach { fileName ->
            val source = readText(componentsRoot.resolve(fileName))

            assertTrue(
                actual = source.contains("ScheduleButton("),
                message = "$fileName should use ScheduleButton for action controls.",
            )
            assertFalse(
                actual = source.contains("import androidx.compose.material3.Button"),
                message = "$fileName should not import raw Material Button.",
            )
            assertFalse(
                actual = source.contains("import androidx.compose.material3.TextButton"),
                message = "$fileName should not import raw Material TextButton.",
            )
            assertFalse(
                actual = source.contains("TextButton("),
                message = "$fileName should not instantiate raw Material TextButton.",
            )
        }
    }

    @Test
    fun cardLikeScheduleComponentsUseSharedScheduleCardContainer() {
        val componentsRoot = commonMainPackageRoot().resolve("presentation/schedule/components")
        val scheduleCardSource = readText(componentsRoot.resolve("ScheduleCard.kt"))

        assertTrue(
            actual = scheduleCardSource.contains("fun ScheduleCard("),
            message = "ScheduleCard should be the shared card-like container for Schedule components.",
        )
        assertTrue(
            actual = scheduleCardSource.contains("Card("),
            message = "ScheduleCard should delegate to Material Card styling instead of duplicating Surface roots.",
        )

        listOf("DateWeekSelector.kt", "RaceCard.kt", "StatePanel.kt").forEach { fileName ->
            val source = readText(componentsRoot.resolve(fileName))

            assertTrue(
                actual = source.contains("ScheduleCard("),
                message = "$fileName should use ScheduleCard for card-like container styling.",
            )
            assertFalse(
                actual = source.contains("Surface("),
                message = "$fileName should not duplicate a root Surface container.",
            )
        }

        val chipSource = readText(componentsRoot.resolve("ScheduleChip.kt"))
        assertFalse(
            actual = chipSource.contains("ScheduleCard("),
            message = "ScheduleChip is a chip control, not a schedule card container.",
        )
        assertTrue(
            actual = chipSource.contains("RoundedCornerShape(ScheduleUiTokens.ControlRadius)"),
            message = "ScheduleChip should use compact label corners instead of button-like pill corners.",
        )
        assertFalse(
            actual = chipSource.contains("RoundedCornerShape(percent = 50)"),
            message = "ScheduleChip should not use pill styling that reads like a button.",
        )
        assertFalse(
            actual = chipSource.contains("BorderStroke("),
            message = "ScheduleChip should avoid bordered button-like styling.",
        )
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
                actual = source.contains("IwpAppTheme {") ||
                    source.contains("ScheduleComponentPreviewTheme"),
                message = "${file.fileName} previews should use the shared app theme directly or through ScheduleComponentPreviewTheme.",
            )
            assertFalse(
                actual = source.contains("MaterialTheme {"),
                message = "${file.fileName} previews should not create raw MaterialTheme wrappers.",
            )
        }
    }

    @Test
    fun scheduleComponentPreviewsUseSharedPreviewVariants() {
        val componentsRoot = commonMainPackageRoot().resolve("presentation/schedule/components")
        val previewSupportSource = readText(
            commonMainPackageRoot()
                .resolve("presentation/schedule/preview/IWPPreview.kt"),
        )

        assertTrue(
            actual = previewSupportSource.contains("annotation class IWPPreview"),
            message = "Schedule components should share one preview annotation for component preview variants.",
        )
        assertTrue(
            actual = previewSupportSource.contains("name = \"Dark\""),
            message = "Schedule component previews should include a dark mode variant.",
        )
        assertTrue(
            actual = previewSupportSource.contains("uiMode = UI_MODE_NIGHT_YES"),
            message = "Schedule component previews should set the dark mode preview uiMode.",
        )
        assertTrue(
            actual = previewSupportSource.contains("fontScale = 1.5f"),
            message = "Schedule component previews should include a large font size variant.",
        )
        assertTrue(
            actual = previewSupportSource.contains("fun ScheduleComponentPreviewTheme("),
            message = "Schedule component previews should share theme setup.",
        )

        listOf(
            "DateWeekSelector.kt",
            "RaceCard.kt",
            "ScheduleBottomNavigation.kt",
            "ScheduleButton.kt",
            "ScheduleCard.kt",
            "ScheduleChip.kt",
            "ScheduleHeader.kt",
            "StatePanel.kt",
        ).forEach { fileName ->
            val source = readText(componentsRoot.resolve(fileName))

            assertTrue(
                actual = source.contains("@IWPPreview"),
                message = "$fileName should use the shared Schedule component preview variants.",
            )
            assertTrue(
                actual = source.contains("ScheduleComponentPreviewTheme"),
                message = "$fileName preview should use the shared Schedule component preview theme wrapper.",
            )
            assertFalse(
                actual = source.contains("@Preview"),
                message = "$fileName should not define one-off raw Preview annotations.",
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
