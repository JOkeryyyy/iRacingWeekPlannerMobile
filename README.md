# iRacing Week Planner Mobile

This is the dedicated Kotlin Multiplatform + Compose Multiplatform mobile app for iRacing Week Planner, targeting Android and iOS.

The existing web app and scraper remain separate and are still the source of truth for schedule generation. This mobile repo starts from local/shared scaffolding and does not scrape iRacing or store iRacing credentials.

## Project Layout

- [/androidApp](./androidApp) contains the Android app entry point.
- [/iosApp](./iosApp) contains the Xcode iOS app entry point.
- [/shared](./shared/src) contains shared Kotlin code for domain, data, presentation, platform, and DI wiring.
- [docs/development.md](./docs/development.md) contains the detailed local setup, build, test, and troubleshooting workflow.

## Quick Start

Required tools:

- JDK compatible with the Gradle/Android plugin in this repo
- Android Studio and Android SDK
- Xcode for iOS builds
- The checked-in Gradle wrapper

Common commands:

```bash
./gradlew projects
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
open iosApp/iosApp.xcodeproj
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```

Use Android Studio to sync the Gradle root at this repository root. Use Xcode to open `iosApp/iosApp.xcodeproj` for the iOS target.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
