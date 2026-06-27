package com.iracingweekplanner.mobile.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppScheduleScreenAndroidHostTest {

    @Test
    fun sharedAppRootUsesScheduleScreenInsteadOfStarterComposeSurface() {
        val appSource = readText(commonMainPackageRoot().resolve("presentation/App.kt"))

        assertTrue(
            actual = appSource.contains("ScheduleScreen("),
            message = "The shared App root should open on the Sprint 3 Schedule screen.",
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
            message = "The Schedule screen should not depend on the old app-info starter state holder.",
        )
        listOf("ScheduleRoot", "ScheduleShell", "ScheduleShellContent").forEach { oldName ->
            assertFalse(
                actual = appSource.contains(oldName),
                message = "App should not reference obsolete $oldName naming.",
            )
        }
    }

    @Test
    fun schedulePresentationUsesScreenAndViewModelNaming() {
        val schedulePackage = commonMainPackageRoot().resolve("presentation/schedule")
        val commonModelPackage = commonMainPackageRoot().resolve("presentation/common/model")
        val screenFile = schedulePackage.resolve("ScheduleScreen.kt")
        val viewModelFile = schedulePackage.resolve("ScheduleViewModel.kt")

        assertTrue(
            actual = Files.exists(screenFile),
            message = "Schedule UI should live in ScheduleScreen.kt.",
        )
        assertTrue(
            actual = Files.exists(viewModelFile),
            message = "Schedule UI logic should live in ScheduleViewModel.kt.",
        )
        assertTrue(
            actual = Files.exists(schedulePackage.resolve("ScheduleAction.kt")),
            message = "ScheduleAction should live in its own MVI contract file.",
        )
        assertTrue(
            actual = Files.exists(schedulePackage.resolve("ScheduleUiState.kt")),
            message = "ScheduleUiState should live in its own MVI contract file.",
        )
        assertTrue(
            actual = Files.exists(commonModelPackage.resolve("ScheduleRaceCardUi.kt")),
            message = "ScheduleRaceCardUi should be the reusable RaceCard UI model.",
        )
        assertFalse(
            actual = Files.exists(schedulePackage.resolve("ScheduleShell.kt")),
            message = "ScheduleShell.kt should be removed in favor of ScheduleScreen.kt.",
        )
        assertFalse(
            actual = Files.exists(schedulePackage.resolve("ScheduleStateHolder.kt")),
            message = "ScheduleStateHolder.kt should be removed in favor of ScheduleViewModel.kt.",
        )
        assertFalse(
            actual = Files.exists(commonMainPackageRoot().resolve("presentation/common/model/ScheduleShellContent.kt")),
            message = "ScheduleShellContent should be removed; ScheduleScreen should render from ScheduleUiState.",
        )
        assertFalse(
            actual = Files.exists(commonModelPackage.resolve("ScheduleRaceCardContent.kt")),
            message = "ScheduleRaceCardContent should be removed; RaceCard should render ScheduleRaceCardUi directly.",
        )
    }

    @Test
    fun platformEntryPointsPassPlannerDataUseCaseIntoSharedAppRoot() {
        val androidSource = readText(projectRoot().resolve("androidApp/src/main/kotlin/com/iracingweekplanner/mobile/MainActivity.kt"))
        val iosSource = readText(sharedProjectRoot().resolve("src/iosMain/kotlin/com/iracingweekplanner/mobile/MainViewController.kt"))

        assertTrue(
            actual = androidSource.contains("appDependencies.loadPlannerData"),
            message = "Android should pass the planner data use case into the shared Schedule root.",
        )
        assertTrue(
            actual = iosSource.contains("appDependencies.loadPlannerData"),
            message = "iOS should pass the planner data use case into the shared Schedule screen.",
        )
        assertFalse(
            actual = androidSource.contains("PlannerDataSource") || iosSource.contains("PlannerDataSource"),
            message = "Platform UI entry points should not construct planner data sources directly.",
        )
    }

    @Test
    fun scheduleScreenUsesSprint3ComponentsAndResourceBackedTextBoundary() {
        val screenSource = readText(commonMainPackageRoot().resolve("presentation/schedule/ScheduleScreen.kt"))

        listOf(
            "ScheduleHeader(",
            "DateWeekSelector(",
            "ScheduleChip(",
            "StatePanel(",
        ).forEach { call ->
            assertTrue(
                actual = screenSource.contains(call),
                message = "ScheduleScreen should render $call as part of the shared Schedule screen.",
            )
        }

        assertTrue(
            actual = screenSource.contains("ScheduleTextResources.headerContent("),
            message = "ScheduleScreen should use the Schedule text resource boundary for the week title.",
        )
        assertTrue(
            actual = screenSource.contains("weekNumber = state.selectedWeekNumber"),
            message = "The screen header should be derived from the selected race week.",
        )
        assertTrue(
            actual = screenSource.contains("ScheduleTextResources.bottomTabs()"),
            message = "Bottom navigation labels should come from the Schedule text resource boundary.",
        )
    }

    @Test
    fun appScaffoldOwnsRootSurfaceAndSafeAreaInsteadOfScheduleScreen() {
        val scaffoldFile = commonMainPackageRoot().resolve("presentation/common/scaffold/IwpAppScaffold.kt")
        assertTrue(
            actual = Files.exists(scaffoldFile),
            message = "Expected a shared app scaffold so screens do not recreate the root Surface.",
        )

        val scaffoldSource = readText(scaffoldFile)
        val screenSource = readText(commonMainPackageRoot().resolve("presentation/schedule/ScheduleScreen.kt"))

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
            actual = screenSource.contains("Surface("),
            message = "ScheduleScreen should not recreate the app-level Surface.",
        )
        assertFalse(
            actual = screenSource.contains("safeContentPadding()"),
            message = "ScheduleScreen should not own app-level safe-area padding.",
        )
    }

    @Test
    fun scheduleBottomNavigationIsPlacedInTheScaffoldBottomBarOutsideScreenPadding() {
        val screenSource = readText(commonMainPackageRoot().resolve("presentation/schedule/ScheduleScreen.kt"))

        assertTrue(
            actual = screenSource.contains("IwpAppScaffold("),
            message = "ScheduleScreen should use the shared app scaffold for bottom-bar placement.",
        )
        assertTrue(
            actual = screenSource.contains("bottomBar = {"),
            message = "Schedule bottom navigation should be passed through the scaffold bottomBar slot.",
        )
        assertTrue(
            actual = screenSource.contains("ScheduleBottomNavigation("),
            message = "Schedule bottom navigation should remain visible in the Schedule screen.",
        )
        assertTrue(
            actual = screenSource.contains("modifier = Modifier.padding(contentPadding)"),
            message = "Schedule content should receive screen padding separately from the bottom bar.",
        )
    }

    @Test
    fun scheduleViewModelDoesNotCallDataLayerDirectly() {
        val viewModelSource = readText(
            commonMainPackageRoot().resolve("presentation/schedule/ScheduleViewModel.kt"),
        )
        val mapperSource = readText(
            commonMainPackageRoot().resolve("presentation/schedule/ScheduleUiStateMapper.kt"),
        )
        val uiStateSource = readText(
            commonMainPackageRoot().resolve("presentation/schedule/ScheduleUiState.kt"),
        )

        assertTrue(
            actual = viewModelSource.contains("StateFlow<ScheduleUiState>"),
            message = "ScheduleViewModel should expose the screen state as StateFlow<ScheduleUiState>.",
        )
        assertTrue(
            actual = viewModelSource.contains("LoadPlannerDataUseCase"),
            message = "ScheduleViewModel should consume the domain-safe planner data use case.",
        )
        assertFalse(
            actual = viewModelSource.contains("PlannerDataPresenter"),
            message = "ScheduleViewModel should not depend on the old PlannerDataPresenter seam.",
        )
        assertFalse(
            actual = uiStateSource.contains("data class ScheduleState"),
            message = "ScheduleState should be fully replaced by ScheduleUiState.",
        )
        assertTrue(
            actual = Files.exists(commonMainPackageRoot().resolve("presentation/schedule/ScheduleUiStateMapper.kt")),
            message = "Schedule domain-to-UI mapping helpers should live in a dedicated file.",
        )
        assertFalse(
            actual = viewModelSource.contains("ScheduleUiStateMapper(") ||
                viewModelSource.contains("stateMapper"),
            message = "ScheduleViewModel should not store a mapper object for stateless mapping helpers.",
        )
        assertTrue(
            actual = mapperSource.contains("fun initialScheduleUiState(") &&
                mapperSource.contains("fun PlannerData.toScheduleUiState(") &&
                mapperSource.contains("fun PlannerDataError.toScheduleErrorState("),
            message = "Schedule domain-to-UI mapping should be exposed as stateless helper functions.",
        )
        assertFalse(
            actual = mapperSource.contains("class ScheduleUiStateMapper"),
            message = "Schedule domain-to-UI mapping does not need a dedicated mapper class.",
        )
        listOf("toRaceCardUi", "metadataText", "toUiMessage").forEach { mapperFunction ->
            assertFalse(
                actual = viewModelSource.contains(mapperFunction),
                message = "ScheduleViewModel should not own mapper function $mapperFunction.",
            )
            assertTrue(
                actual = mapperSource.contains(mapperFunction),
                message = "ScheduleUiStateMapper should own mapper function $mapperFunction.",
            )
        }

        listOf("com.iracingweekplanner.mobile.data", "repository.", "PlannerDataSource", "SqlDelight", "Ktor").forEach { forbidden ->
            assertFalse(
                actual = viewModelSource.contains(forbidden),
                message = "ScheduleViewModel should consume a domain-safe use case, not data-layer detail: $forbidden",
            )
        }
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
