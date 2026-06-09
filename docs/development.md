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

Open iOS app:

```bash
open iosApp
```

Opening Xcode may require user approval or manual action depending on the environment.

## iOS Toolchain Notes

If iOS Gradle tasks fail with `xcrun`, `xcodebuild`, or `MissingXcodeException`, verify that full Xcode is selected instead of only Command Line Tools:

```bash
xcode-select -p
/usr/bin/xcrun xcodebuild -version
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
