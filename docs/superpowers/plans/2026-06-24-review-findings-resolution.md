# Review Findings Resolution Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the current Sprint 2 Story 2.7 branch merge-ready by fixing confirmed blockers from the review threads and documenting non-blocking architecture follow-ups.

**Architecture:** Keep Story 2.7 scoped to DI wiring, platform dependency wiring, repository-interface packaging, and development documentation. Do not add new repository behavior, presentation states, module splits, or SQLDelight schema migrations in this fix.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Koin, SQLDelight, Gradle, Xcode project configuration.

---

## Issue Summary

Blocking issues to fix in this branch:

- The iOS app target does not permanently link SQLite even though `iosMain` now uses SQLDelight's native SQLite driver. The Xcode target needs a committed SQLite link setting, not a one-off `OTHER_LDFLAGS=-lsqlite3` command-line workaround.
- The live tree deletes `PlannerRepositories.kt` and creates split repository-interface files, but the new files are untracked. A clean checkout of the branch can miss files that the local build used.

Review hygiene to resolve in this branch:

- The one-interface-per-file repository rule is extra architecture scope. This plan keeps it because the current live tree already includes the test and documentation, but the branch must make that choice explicit and committed.
- `docs/development.md` currently records an iOS app build as passing while the project file still has no permanent SQLite link. Update the docs after the committed Xcode fix is verified.

Non-blocking architecture follow-ups to document, not implement here:

- Clean Architecture boundaries are currently package/test enforced inside `:shared`, not Gradle-module enforced.
- Android app dependency ownership is Activity-scoped.
- SQLDelight relationship tables do not yet use database-level foreign-key constraints.

## File Structure

- Modify: `iosApp/iosApp.xcodeproj/project.pbxproj`
  - Add permanent SQLite linker settings for the iOS app Debug and Release target build configurations.
- Delete: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerRepositories.kt`
  - The grouped repository-interface file is replaced by one interface per file.
- Create/track: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt`
- Create/track: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerDataRepository.kt`
- Create/track: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerScheduleRepository.kt`
- Create/track: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerTrackRepository.kt`
- Modify: `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SharedPackageStructureAndroidHostTest.kt`
  - Keep the architecture guard requiring one repository interface per matching file.
- Modify: `docs/architecture.md`
  - Keep the repository package rule and record deferred architecture hardening.
- Modify: `docs/development.md`
  - Document Story 2.7 verification commands and the iOS SQLite link requirement.

---

### Task 1: Make Repository Interface Split Branch-Complete

**Files:**
- Delete: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerRepositories.kt`
- Create: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerDataRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerScheduleRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerTrackRepository.kt`
- Test: `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SharedPackageStructureAndroidHostTest.kt`

- [ ] **Step 1: Verify the failure mode before staging**

Run:

```bash
git status --short --branch
git ls-files --others --exclude-standard
git ls-files --error-unmatch shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt
```

Expected before the fix:

```text
?? shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt
error: pathspec 'shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt' did not match any file(s) known to git
```

- [ ] **Step 2: Keep the split repository files with exact contents**

`shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt`:

```kotlin
package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerCar
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult

interface PlannerCarRepository {
    suspend fun loadPlannerCars(): PlannerDataResult<List<PlannerCar>>
}
```

`shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerDataRepository.kt`:

```kotlin
package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult

interface PlannerDataRepository {
    suspend fun loadPlannerData(): PlannerDataResult<PlannerData>
}
```

`shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerScheduleRepository.kt`:

```kotlin
package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.domain.model.RaceWeek

interface PlannerScheduleRepository {
    suspend fun loadRaceWeeks(): PlannerDataResult<List<RaceWeek>>
    suspend fun loadPlannerRaces(): PlannerDataResult<List<PlannerRace>>
}
```

`shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerTrackRepository.kt`:

```kotlin
package com.iracingweekplanner.mobile.domain.repository

import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerTrack

interface PlannerTrackRepository {
    suspend fun loadPlannerTracks(): PlannerDataResult<List<PlannerTrack>>
}
```

- [ ] **Step 3: Stage the deletion and new files together**

Run:

```bash
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerRepositories.kt
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerDataRepository.kt
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerScheduleRepository.kt
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerTrackRepository.kt
```

Expected:

```text
No command output.
```

- [ ] **Step 4: Verify Git now tracks the replacement files**

Run:

```bash
git ls-files --error-unmatch shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt
git ls-files --error-unmatch shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerDataRepository.kt
git ls-files --error-unmatch shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerScheduleRepository.kt
git ls-files --error-unmatch shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerTrackRepository.kt
```

Expected:

```text
shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt
shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerDataRepository.kt
shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerScheduleRepository.kt
shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerTrackRepository.kt
```

---

### Task 2: Permanently Link SQLite in the iOS App Target

**Files:**
- Modify: `iosApp/iosApp.xcodeproj/project.pbxproj`
- Test: iOS app `xcodebuild`

- [ ] **Step 1: Reproduce the linker failure without command-line linker overrides**

Run:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/xcode-derived-data build
```

Expected before the fix:

```text
Undefined symbols for architecture arm64:
  "_sqlite3_bind_blob"
  "_sqlite3_close"
  "_sqlite3_column_blob"
```

The exact list of `_sqlite3_*` symbols can be longer. The important signal is that the failure is unresolved SQLite symbols from the app link step.

- [ ] **Step 2: Add target-level SQLite linker flags**

In `iosApp/iosApp.xcodeproj/project.pbxproj`, add `OTHER_LDFLAGS` to both target build configurations `DC99974CB9616E646AEA30DB /* Debug */` and `A6CFD33750649873D73009F1 /* Release */`.

Insert this setting immediately after each target configuration's `LD_RUNPATH_SEARCH_PATHS` block:

```text
					OTHER_LDFLAGS = (
						"$(inherited)",
						"-lsqlite3",
					);
```

The Debug target block should include:

```text
					LD_RUNPATH_SEARCH_PATHS = (
						"$(inherited)",
						"@executable_path/Frameworks",
					);
					OTHER_LDFLAGS = (
						"$(inherited)",
						"-lsqlite3",
					);
					SWIFT_EMIT_LOC_STRINGS = YES;
```

The Release target block should include the same `OTHER_LDFLAGS` setting between `LD_RUNPATH_SEARCH_PATHS` and `SWIFT_EMIT_LOC_STRINGS`.

- [ ] **Step 3: Verify the linker flag is committed in the project file**

Run:

```bash
rg -n "OTHER_LDFLAGS|sqlite3" iosApp/iosApp.xcodeproj/project.pbxproj
```

Expected:

```text
iosApp/iosApp.xcodeproj/project.pbxproj:<line>:					OTHER_LDFLAGS = (
iosApp/iosApp.xcodeproj/project.pbxproj:<line>:						"-lsqlite3",
iosApp/iosApp.xcodeproj/project.pbxproj:<line>:					OTHER_LDFLAGS = (
iosApp/iosApp.xcodeproj/project.pbxproj:<line>:						"-lsqlite3",
```

- [ ] **Step 4: Verify the iOS app target builds without command-line linker overrides**

Run:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/xcode-derived-data build
```

Expected:

```text
** BUILD SUCCEEDED **
```

---

### Task 3: Keep Architecture Guard Explicit and Document Deferred Architecture Work

**Files:**
- Modify: `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SharedPackageStructureAndroidHostTest.kt`
- Modify: `docs/architecture.md`

- [ ] **Step 1: Keep the repository package guard**

Ensure `SharedPackageStructureAndroidHostTest.kt` contains this test:

```kotlin
@Test
fun domainRepositoryInterfacesUseOneFilePerInterface() {
    val repositoryRoot = commonMainPackageRoot().resolve("domain/repository")
    val violations = Files.newDirectoryStream(repositoryRoot, "*.kt").use { stream ->
        stream
            .mapNotNull { file ->
                val interfaceNames = repositoryInterfaceNames(file)
                when {
                    interfaceNames.size != 1 ->
                        "${file.fileName} should declare exactly one repository interface. Found: $interfaceNames"

                    file.fileName.toString() != "${interfaceNames.single()}.kt" ->
                        "${file.fileName} should be named ${interfaceNames.single()}.kt"

                    else -> null
                }
            }
            .sorted()
    }

    assertTrue(
        actual = violations.isEmpty(),
        message = "Expected one domain repository interface per file. Violations: $violations",
    )
}
```

Ensure the helper is present:

```kotlin
private fun repositoryInterfaceNames(file: Path): List<String> =
    repositoryInterfacePattern.findAll(Files.readAllLines(file).joinToString(separator = "\n"))
        .map { match -> match.groupValues[1] }
        .toList()

private companion object {
    val repositoryInterfacePattern = Regex("""(?m)^\s*interface\s+(\w+Repository)\b""")
}
```

- [ ] **Step 2: Keep the current repository package wording**

In `docs/architecture.md`, keep this package organization line:

```text
    repository/   domain repository interfaces, one interface per file
```

Keep this test strategy sentence:

```text
Tests should mirror the layer and role package being tested. The Android-host architecture test guards against adding new direct Kotlin files under `domain/` or `data/` root folders, and against grouping multiple domain repository interfaces in one file.
```

- [ ] **Step 3: Add explicit non-blocking architecture follow-ups**

Add this section near the end of `docs/architecture.md`:

```markdown
## Deferred Architecture Hardening

The current `:shared` module enforces Clean Architecture by package structure, review, and architecture tests rather than by separate Gradle modules. Before the planner UI and settings flows grow significantly, evaluate whether `domain`, `data`, and `presentation` should become separate shared modules.

Android dependency ownership is currently created from the Activity entry point. Before adding background refresh, process-wide app state, or long-lived shared services, move shared dependency ownership to an Android application-level composition root.

SQLDelight relationship tables currently rely on mapper and transaction integrity rather than database-level foreign keys. Before treating the cache as long-lived user data, add foreign-key constraints and migration coverage in a dedicated storage story.
```

- [ ] **Step 4: Run the architecture test**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests com.iracingweekplanner.mobile.architecture.SharedPackageStructureAndroidHostTest
```

Expected:

```text
BUILD SUCCESSFUL
```

---

### Task 4: Correct Development Verification Documentation

**Files:**
- Modify: `docs/development.md`

- [ ] **Step 1: Keep Story 2.7 test-file documentation**

Ensure the shared test inventory includes:

```markdown
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SharedPackageStructureAndroidHostTest.kt` verifies that shared domain/data files stay grouped by role packages and that each domain repository interface has its own matching file.
- `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/di/CommonPlannerDataModuleAndroidHostTest.kt` verifies Sprint 2 planner Koin wiring with an in-memory SQLDelight driver.
```

- [ ] **Step 2: Add the permanent iOS SQLite link caveat**

In the Sprint 2 tooling caveats section, add:

```markdown
- The iOS app target must link SQLite because the shared iOS framework uses SQLDelight's native SQLite driver. Keep `-lsqlite3` in the iOS app target build settings; do not rely on command-line `OTHER_LDFLAGS` for normal verification.
```

- [ ] **Step 3: Record the iOS app verification command**

In the Sprint 2 verification commands section, include:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/xcode-derived-data build
```

- [ ] **Step 4: Correct stale pass notes after verification**

If `docs/development.md` has a verification table entry for an iOS app build, make the row match the committed command and the committed project-file fix:

```markdown
| iOS app build command | `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/xcode-derived-data build` | PASS: `BUILD SUCCEEDED` with SQLite linked by the Xcode target build settings. |
```

---

### Task 5: Run Merge-Readiness Verification

**Files:**
- Verify all files changed by Tasks 1-4.

- [ ] **Step 1: Check Git completeness**

Run:

```bash
git status --short --branch
git ls-files --others --exclude-standard
git diff --cached --name-status
git diff --check
git diff --cached --check
```

Expected:

```text
git ls-files --others --exclude-standard
```

prints no files relevant to Story 2.7. `git diff --check` and `git diff --cached --check` print no whitespace errors.

- [ ] **Step 2: Run shared Android-host tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: Run Android debug assembly**

Run:

```bash
./gradlew :androidApp:assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 4: Run shared iOS simulator tests**

Run:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 5: Run iOS app build**

Run:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/xcode-derived-data build
```

Expected:

```text
** BUILD SUCCEEDED **
```

- [ ] **Step 6: Commit the resolution**

Run:

```bash
git add iosApp/iosApp.xcodeproj/project.pbxproj
git add docs/architecture.md docs/development.md
git add shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SharedPackageStructureAndroidHostTest.kt
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerRepositories.kt
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerDataRepository.kt
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerScheduleRepository.kt
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerTrackRepository.kt
git status --short
git commit -m "fix: resolve story 2.7 review blockers"
```

Expected staged files before commit:

```text
M  docs/architecture.md
M  docs/development.md
M  iosApp/iosApp.xcodeproj/project.pbxproj
M  shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/architecture/SharedPackageStructureAndroidHostTest.kt
D  shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerRepositories.kt
A  shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerCarRepository.kt
A  shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerDataRepository.kt
A  shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerScheduleRepository.kt
A  shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/domain/repository/PlannerTrackRepository.kt
```

Expected commit result:

```text
[feature/sprint2story6 <commit>] fix: resolve story 2.7 review blockers
```

---

## Self-Review

- Spec coverage: The plan covers all confirmed review blockers from the referenced chats: missing iOS SQLite linkage, untracked repository-interface files, explicit handling of the one-interface-per-file rule, stale iOS verification documentation, and non-blocking architecture follow-ups.
- Scope control: The plan does not implement Gradle module separation, Android application-level DI, or SQLDelight foreign keys in this branch because those are architecture hardening items outside Story 2.7's wiring/docs scope.
- Verification: The plan requires Android host tests, Android assembly, shared iOS simulator tests, and an iOS app `xcodebuild` without command-line SQLite linker overrides before committing.
