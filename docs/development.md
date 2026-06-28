# Development

## Repository Layout

This repository is the mobile app repo and Gradle root.

Expected top-level modules:

- `androidApp`: Android app entry point
- `iosApp`: iOS app entry point
- `shared`: Kotlin Multiplatform shared code

The existing web repo is separate and remains the schedule generation/source-of-truth repo:

```text
/Users/gaojiahao/Documents/iracing/iRacing-week-planner
```

The current static JSON contract for frontend integration is documented in
[Hosted JSON API](hosted-json-api.md).

## Required Tools

Use common/popular versions compatible with the current Kotlin Multiplatform and Compose Multiplatform scaffold:

- JDK compatible with the Gradle/Android plugin in this repo
- Android Studio
- Android SDK
- Xcode for iOS builds
- Gradle wrapper from this repo

## Clean Checkout Setup

1. Open this repository root in Android Studio and let Gradle sync from `settings.gradle.kts`.
2. Confirm the expected modules are visible: `androidApp`, `shared`, and the `iosApp` Xcode project directory.
3. Run a lightweight Gradle project check:

```bash
./gradlew projects
```

4. Run the Android debug build and shared smoke tests:

```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
```

5. If full Xcode is installed, open and build the iOS app:

```bash
open iosApp/iosApp.xcodeproj
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```

## Common Commands

List Gradle projects:

```bash
./gradlew projects
```

Run shared Android host tests:

```bash
./gradlew :shared:testAndroidHostTest
```

Build Android debug app:

```bash
./gradlew :androidApp:assembleDebug
```

Run shared iOS simulator tests:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

## Shared Smoke Tests

The shared module currently has smoke coverage in common, Android host, and iOS source sets:

- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/SharedCommonTest.kt` verifies the shared app info smoke path.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/domain/usecase/GetAppInfoUseCaseTest.kt` verifies the initial domain use case.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/domain/usecase/PlannerDataUseCasesTest.kt` verifies the planner repository/use-case contracts with fake repositories.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/AppInfoStateHolderTest.kt` verifies the presentation state holder.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/schedule/ScheduleViewModelTest.kt` verifies schedule loading, loaded, cached, empty, error, retry, and week-selection state.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/schedule/ScheduleUiFoundationTest.kt` verifies the Sprint 3 reusable Schedule UI foundation contract.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/theme/IwpAppThemeTest.kt` verifies the shared app theme color contract.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/di/CommonAppModuleTest.kt` verifies app-info Koin wiring.
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/SharedLogicAndroidHostTest.kt` verifies the shared smoke path from the Android host test target.
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/AndroidEntryPointArchitectureAndroidHostTest.kt` verifies that Android app dependencies are owned by the `Application` class rather than `MainActivity`.
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SharedPackageStructureAndroidHostTest.kt` verifies that shared domain/data files stay grouped by role packages, each domain repository interface has its own matching file, and the app uses `presentation/theme/IwpAppTheme.kt`.
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SchedulePackageStructureAndroidHostTest.kt` verifies that Sprint 3 Schedule components live in a dedicated package with one file and app-themed preview per component.
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/di/CommonPlannerDataModuleAndroidHostTest.kt` verifies Sprint 2 planner Koin wiring with an in-memory SQLDelight driver.
- `shared/src/iosTest/kotlin/com/iracingweekplanner/mobile/SharedLogicIOSTest.kt` verifies the shared smoke path from the iOS test source set.

Default local shared verification:

```bash
./gradlew :shared:testAndroidHostTest
```

## Presentation MVI Conventions

- Screen state types use the `*UiState` suffix, for example `ScheduleUiState`.
- Screen actions use the `*Action` suffix, for example `ScheduleAction`.
- Public MVI contract types and reusable UI models each live in their own Kotlin file.
- `Screen` composables stay stateless and render from UI state plus callbacks.
- `ViewModel` classes own screen-level UI logic and expose `StateFlow<*UiState>` plus `onAction(*Action)`.
- Avoid duplicate `Content` models when a component can render an existing UI model directly.

Additional iOS verification when full Xcode is available:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

Open iOS app in Xcode:

```bash
open iosApp/iosApp.xcodeproj
```

Build the iOS simulator app from the command line:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```

The iOS target hosts shared Compose UI through `iosApp/iosApp/ContentView.swift`, which imports the generated `Shared` framework and presents `MainViewController()` from `shared/src/iosMain/kotlin/com/iracingweekplanner/mobile/MainViewController.kt`. The Xcode target has a `Compile Kotlin Framework` build phase that runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode`.

Signing metadata lives in `iosApp/Configuration/Config.xcconfig`. Simulator builds can usually leave `TEAM_ID` empty; device builds require an Apple development team ID.

Opening Xcode may require user approval or manual action depending on the environment. Command-line iOS builds require full Xcode, not only Command Line Tools.

## Sprint 3 Story-Focused Tests

Story 3.1 reusable Schedule UI foundation coverage:

- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/schedule/ScheduleUiFoundationTest.kt`
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/theme/IwpAppThemeTest.kt`
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SchedulePackageStructureAndroidHostTest.kt`
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SharedPackageStructureAndroidHostTest.kt`

Focused commands:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.presentation.schedule.ScheduleUiFoundationTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.presentation.theme.IwpAppThemeTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.architecture.SchedulePackageStructureAndroidHostTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.architecture.SharedPackageStructureAndroidHostTest
```

Story 3.2 Schedule localization-readiness coverage:

- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/schedule/ScheduleComposeResourcesTest.kt`
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/schedule/ScheduleUiFoundationTest.kt` verifies that race-card metadata is already prepared display text before reaching `RaceCard`.
- `shared/src/iosSimulatorArm64Test/kotlin/com/iracingweekplanner/mobile/presentation/schedule/ScheduleComposeResourceOutputTest.kt`

Focused commands:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.presentation.schedule.ScheduleComposeResourcesTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.presentation.schedule.ScheduleUiFoundationTest
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```

Story 3.3 Schedule shell coverage:

- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/schedule/ScheduleShellContentTest.kt`
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/AppScheduleShellAndroidHostTest.kt`

Focused command:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.presentation.schedule.ScheduleShellContentTest --tests com.iracingweekplanner.mobile.architecture.AppScheduleShellAndroidHostTest
```

## Sprint 2 Story-Focused Tests

Story 2.5 repository refresh/cache fallback coverage:

- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/repository/RefreshCachePlannerDataCoordinatorTest.kt`
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/domain/usecase/PlannerDataUseCasesTest.kt`

Focused commands:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataCoordinatorTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.domain.usecase.PlannerDataUseCasesTest
```

Story 3.4 schedule MVI coverage:

- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/schedule/ScheduleViewModelTest.kt`

Focused command:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.presentation.schedule.ScheduleViewModelTest
```

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

Story 2.7 DI wiring coverage:

- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/di/CommonAppModuleTest.kt`
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/di/CommonPlannerDataModuleAndroidHostTest.kt`
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/AndroidEntryPointArchitectureAndroidHostTest.kt`
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SharedPackageStructureAndroidHostTest.kt`

Focused commands:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.di.CommonAppModuleTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.di.CommonPlannerDataModuleAndroidHostTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.architecture.AndroidEntryPointArchitectureAndroidHostTest
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.architecture.SharedPackageStructureAndroidHostTest
```

Common Sprint 2 verification commands:

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```

Sprint 2 Definition of Done:

- Local mock JSON can be loaded through a shared data-source API.
- Hosted JSON loading is represented by a tested source shape without requiring a production endpoint.
- Last successful planner data can be persisted to SQLDelight and read back from the local source of truth.
- Repository implementations return fresh data when available and cached data when refresh fails.
- Invalid required source data is represented as an explicit error state, not silently coerced or dropped.
- Presentation-friendly planner data states exist for Sprint 3 UI work.
- Koin wiring resolves the Sprint 2 data/repository/use-case/state-holder graph.
- Story-focused tests pass.
- Baseline verification passes or has a documented local tooling blocker.

Sprint 2 tooling caveats:

- Android-host DI tests use an in-memory SQLDelight JDBC driver and assert that the default `PlannerDataSource` is local-resource backed. Do not make these tests perform default Compose resource reads; resource loading is covered by the dedicated local-source fixture tests.
- Android app dependency creation now needs an Android `Context` so the platform source set can create the SQLDelight Android driver. The Android `Application` owns these dependencies for the app lifetime; Activities should consume the application-owned dependencies rather than creating or closing their own graph.
- iOS simulator tests require full Xcode. If the global developer directory points at Command Line Tools, run iOS Gradle verification with `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`.

## TDD Workflow

Use TDD for implementation stories when behavior can be meaningfully tested:

1. Write or adjust a focused test.
2. Run the targeted test and confirm failure when practical.
3. Implement the smallest change that satisfies the test.
4. Re-run targeted tests.
5. Run broader verification before completing the story.

Good default test targets:

- Domain use cases with fake repositories.
- DTO parsing against local mock JSON.
- Mapper conversion from DTOs to domain models, including imperfect source-data cases that should map without broad domain `require` checks.
- Repository refresh/cache behavior with fake data sources.
- State-holder/ViewModel loading, loaded, empty, and error states.

Use Koin test modules or simple fakes to replace repositories, data sources, and state-holder dependencies in tests.

## iOS Toolchain Notes

If iOS Gradle tasks fail with `xcrun`, `xcodebuild`, or `MissingXcodeException`, verify that full Xcode is selected instead of only Command Line Tools:

```bash
xcode-select -p
/usr/bin/xcrun xcodebuild -version
/usr/bin/xcrun simctl list devices
```

To run an iOS Gradle task without changing the global developer directory, point the command at the installed full Xcode:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```

Only debug app source after the local Xcode selection is known to be valid.

## Troubleshooting

- If Gradle sync cannot resolve Android artifacts, verify Android Studio has the Android SDK installed and that this repository root, not a nested directory, was opened.
- If Android builds fail with SDK version errors, install the compile SDK listed in `gradle/libs.versions.toml`.
- If iOS commands fail with `xcrun: error: unable to find utility "xcodebuild"` or `simctl`, install/select full Xcode with `xcode-select`.
- If iOS device builds fail signing, set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`.
- If shared tests fail after a dependency change, run `./gradlew :shared:tasks --all` and confirm the expected test task still exists.

## Documentation Workflow

- Update `docs/roadmap.md` when sprint scope changes.
- Update the active sprint story file when acceptance criteria change.
- Update `docs/architecture.md` when layer boundaries or package layout change.
- Update `docs/data-contract.md` when JSON fields change.
- Update this file when setup, run, or verification commands change.
- Update root `AGENTS.md` when project rules for agents/developers change.

## Sprint 0 Exit Checklist

- Android app builds or has a documented blocker.
- iOS app builds/runs or has a documented blocker.
- Shared tests pass or have a documented blocker.
- Clean Architecture package boundaries exist.
- Baseline dependencies are added or explicitly deferred.
- Local setup and verification commands are documented.

## Sprint 0 Verification Record

Recorded on 2026-06-16 from branch `main`.

| Area | Command or check | Result |
| --- | --- | --- |
| Android debug build | `./gradlew :androidApp:assembleDebug` | PASS: `BUILD SUCCESSFUL`; 57 actionable tasks, 1 executed. |
| Android emulator availability | `adb devices` | PASS: `emulator-5556 device`. |
| Android install | `./gradlew :androidApp:installDebug` | PASS: installed `androidApp-debug.apk` on `Pixel_10_Pro_Fold(AVD) - 17`. |
| Android launch target | `adb -s emulator-5556 shell cmd package resolve-activity --brief com.iracingweekplanner.mobile` | PASS: `com.iracingweekplanner.mobile/.MainActivity`. |
| Android app launch | `adb -s emulator-5556 shell am start -n com.iracingweekplanner.mobile/.MainActivity` | PASS: activity started. |
| Android placeholder UI | `adb -s emulator-5556 exec-out uiautomator dump /dev/tty` before and after one button tap | PASS: initial UI contained `iRacing Week Planner Mobile` and `Click me!`; expanded UI contained `Source: shared` and `iRacing Week Planner Mobile shared module is ready`. |
| Android screenshots | `adb -s emulator-5556 shell screencap -p ...` and `adb -s emulator-5556 pull ...` | PASS: evidence saved in `docs/qa/sprint-0-evidence/android-initial.png` and `docs/qa/sprint-0-evidence/android-expanded.png`. |
| Shared Android host tests | `./gradlew :shared:testAndroidHostTest` | PASS: `BUILD SUCCESSFUL`; 32 actionable tasks, 1 executed. |
| Shared iOS simulator tests | `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test` | PASS: `BUILD SUCCESSFUL`; 24 actionable tasks, 3 executed. |
| Xcode build tool | `/Applications/Xcode.app/Contents/Developer/usr/bin/xcodebuild -version` | PASS: Xcode 26.5, build 17F42. |
| iOS simulator availability | `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer /usr/bin/xcrun simctl list devices available` | PASS: `iPhone 17 Pro` was booted on iOS 26.5. |
| iOS app build command | `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,id=569EBA36-164A-4D82-B362-5B2D87A351A7' -derivedDataPath build/xcode-derived-data build` | PASS: `BUILD SUCCEEDED`. |
| iOS install and launch | `simctl install ... iRacingWeekPlannerMobile.app` and `simctl launch ... com.iracingweekplanner.mobile` | PASS: launch returned process id `56565`. |
| iOS placeholder UI | Computer Use on Simulator before and after one button click | PASS: initial UI contained `iRacing Week Planner Mobile` and `Click me!`; expanded UI contained `Source: shared` and `iRacing Week Planner Mobile shared module is ready`. |
| iOS screenshots | `simctl io ... screenshot ...` | PASS: evidence saved in `docs/qa/sprint-0-evidence/ios-initial.png` and `docs/qa/sprint-0-evidence/ios-expanded.png`. |
| Clean Architecture package boundaries | `find shared/src/commonMain/kotlin/com/iracingweekplanner/mobile -maxdepth 3 -type d -print` | PASS: `domain/model`, `domain/repository`, `domain/usecase`, `data/datasource`, `data/dto`, `data/local`, `data/mapper`, `data/repository`, `presentation`, and `platform` package directories exist. |
| Domain dependency boundary | `rg -n "import .*compose|import .*ktor|import .*koin|import .*platform|import .*android|import .*UIKit|import .*Settings|import .*serialization" shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain` | PASS: no disallowed domain imports found. |
| Local setup docs | `README.md` and this file | PASS: required tools, Gradle commands, Android build/test commands, iOS open/build commands, troubleshooting, and web-repo separation are documented. |
| QA evidence index | `docs/qa/sprint-0-evidence/README.md` | PASS: product-review screenshots and command evidence are indexed for Sprint 0 closeout. |

Known setup risks before Sprint 1:

- The global `xcode-select -p` value was `/Library/Developer/CommandLineTools` during QA. iOS verification passes when commands set `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`, or when full Xcode is selected globally.
- Set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` only when device signing is needed.
