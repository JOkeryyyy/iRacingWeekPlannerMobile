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

## Required Tools

Use common/popular versions compatible with the current Kotlin Multiplatform and Compose Multiplatform scaffold:

- JDK compatible with the Gradle/Android plugin in this repo
- Android Studio
- Android SDK
- Xcode for iOS builds
- Gradle wrapper from this repo

## Common Commands

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
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/domain/GetAppInfoUseCaseTest.kt` verifies the initial domain use case.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/presentation/AppInfoStateHolderTest.kt` verifies the presentation state holder.
- `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/di/CommonAppModuleTest.kt` verifies Koin wiring and platform-owned dependencies.
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/SharedLogicAndroidHostTest.kt` verifies the shared smoke path from the Android host test target.
- `shared/src/iosTest/kotlin/com/iracingweekplanner/mobile/SharedLogicIOSTest.kt` verifies the shared smoke path from the iOS test source set.

Default local shared verification:

```bash
./gradlew :shared:testAndroidHostTest
```

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
- Mapper conversion from DTOs to domain models.
- Repository refresh/cache behavior with fake data sources.
- State-holder/ViewModel loading, loaded, empty, and error states.

Use Koin test modules or simple fakes to replace repositories, data sources, and state-holder dependencies in tests.

## iOS Toolchain Notes

If iOS Gradle tasks fail with `xcrun`, `xcodebuild`, or `MissingXcodeException`, verify that full Xcode is selected instead of only Command Line Tools:

```bash
xcode-select -p
/usr/bin/xcrun xcodebuild -version
```

To run an iOS Gradle task without changing the global developer directory, point the command at the installed full Xcode:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```

Only debug app source after the local Xcode selection is known to be valid.

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
