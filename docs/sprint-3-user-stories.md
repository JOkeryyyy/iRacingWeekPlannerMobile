# Sprint 3 User Stories: Schedule UI MVP

## Sprint Goal

Build the first usable shared Compose schedule screen for Android and iOS.

Sprint 3 should turn the Sprint 2 planner data state into a mobile Schedule tab that can load planner data, show the selected week, render ordered race cards, and handle loading, cached, empty, and error states. The implementation should follow a lightweight shared MVI shape so later Sprint 4 filtering and Sprint 5 race-detail work can extend the screen without rewriting the presentation boundary.

Sprint 3 should still be implemented story-by-story. Do not combine all UI foundation, localization readiness, state wiring, date controls, race list, and platform verification work into one implementation diff unless explicitly approved.

## Scope Defaults

- The primary bottom-tab label is `Schedule`, not `Planner`.
- The Schedule header uses a week-specific title such as `Week 13 Schedule` and a last-updated line such as `Last updated 10:42 AM`.
- Race display order is deterministic by selected week, then stable source order or a simple stable display key. Sprint 3 does not expose user-controlled sorting.
- Sprint 3 uses existing Sprint 2 local mock-first planner data and presentation state.
- Sprint 3 ships English-only, but user-facing Schedule strings should use Compose Multiplatform string/plural resources or resource-backed parameters so later localization work can add languages without rewriting screen components.
- Sprint 3 may show inactive future affordances only when they do not imply completed behavior. Filters, preferences, sorting controls, owned/favorite settings, and race details stay out of scope.
- No Firebase, account login, cloud sync, mobile scraping, production hosted URL, or iRacing credential handling is part of this sprint.

## Linked Knowledge and Design References

- Roadmap scope: [docs/roadmap.md](roadmap.md)
- Architecture boundaries: [docs/architecture.md](architecture.md)
- Current development commands and Sprint 2 verification: [docs/development.md](development.md)
- Mobile data contract for displayed fields: [docs/data-contract.md](data-contract.md)
- Sprint 2 presentation-state starting point: [docs/sprint-2-user-stories.md](sprint-2-user-stories.md)
- Annotated Sprint 3/4/5 wireframe board: [docs/diagrams/sprint-3-wireframes.html](diagrams/sprint-3-wireframes.html)

The wireframe HTML is a review artifact. The implementation-facing design constraints below should be treated as the stable Sprint 3 reference.

## Sprint 3 Design Constraints

- Reference mobile frame: `390 x 844`.
- Screen padding: `16dp` horizontal, `12dp` top, and `16dp` bottom above navigation.
- Spacing scale: `8dp` compact gap, `12dp` default gap, `16dp` section gap, `24dp` major separation.
- Radius scale: `8dp` controls, `12dp` cards, `16dp` bottom navigation, `24dp` future bottom-sheet top corners.
- Schedule title: `24sp`, semibold or bold.
- Section title and race title: `16sp`, semibold.
- Track, metadata, captions, update time, chip labels, and nav labels: `12sp` to `13sp`.
- Icon buttons: minimum `44 x 44dp` touch target, `12dp` visual radius.
- Chips: minimum `32dp` height, `12dp` horizontal padding, full radius.
- Race cards: `12dp` radius, `1dp` border, `14dp` internal padding, `10dp` internal gap, `12dp` card-to-card gap.
- Bottom navigation: `64dp` to `72dp` height, four equal items, each item at least `48dp` touch height.

## Story 3.1: Add Reusable Schedule UI Foundation

**As a developer, I want reusable shared Compose UI primitives so the Schedule screen can be built consistently and extended in later sprints.**

### Current Starting Point

- The shared `App` UI is still the starter screen.
- Sprint 3 has a wireframe-defined component set: `ScheduleHeader`, `DateWeekSelector`, `ScheduleChip`, `RaceCard`, `StatePanel`, and `BottomNavigation`.
- Sprint 4 and Sprint 5 should reuse these primitives rather than creating unrelated visual patterns.

### Acceptance Criteria

- A shared UI foundation exists under the presentation/shared Compose layer for Sprint 3 visual primitives.
- UI tokens or constants cover the Sprint 3 design constraints for spacing, radius, type scale, and minimum touch targets.
- `ScheduleHeader` can display a week title, last-updated text, and a refresh action with a `44 x 44dp` minimum touch target.
- `ScheduleChip` supports at least default and selected visual states with `32dp` minimum height.
- `StatePanel` supports loading, empty, and error-style content with optional retry action.
- `BottomNavigation` can render a selected `Schedule` tab and inactive future tabs without enabling out-of-scope flows.
- Components remain shared Compose UI and do not depend on Android-only or iOS-only APIs.
- Components receive display state and callbacks through parameters; they do not load repositories, parse data, or own business logic.

### QA Test Cases

- Build a preview or focused Compose rendering path that shows the reusable components with representative Sprint 3 data.
- Verify the selected bottom tab label reads `Schedule`.
- Verify `ScheduleHeader` displays `Week 13 Schedule`, `Last updated 10:42 AM`, and one refresh action.
- Verify chips do not visually shrink below the target height when labels are short or long.
- Verify the state panel can render loading, empty, and retryable error variants without layout overlap.
- Verify component parameters can be driven by fake state without constructing the real repository graph.

## Story 3.2: Add Schedule Localization Readiness

**As a developer, I want Schedule screen text to be centralized and parameterized so the English-only MVP can support future localization without reworking reusable UI components.**

### Current Starting Point

- The starter UI and early Sprint 3 wireframes use direct English labels such as `Schedule`, `Week 13 Schedule`, and `Last updated 10:42 AM`.
- Sprint 3 reusable components should not hardcode display copy internally because Sprint 4 filters and Sprint 5 details will add more user-facing text.
- The app does not need non-English resources in Sprint 3, but Schedule text should be stored behind Compose Multiplatform resources so language-specific resource files can be added later.

### Acceptance Criteria

- A Schedule UI string boundary exists in shared presentation code for Sprint 3 user-facing labels, messages, and formatted text.
- Sprint 3 remains English-only; no non-English translations are required for this story, but English Schedule copy lives in Compose Multiplatform resources.
- Reusable components receive already prepared display text or string-provider output through state/parameters instead of constructing user-facing copy internally.
- Dynamic text uses whole-message string/plural resource templates, not fragile concatenation of localized fragments. Examples include `Week {number} Schedule`, `Last updated {time}`, and `{count} races`.
- Loading, empty, cached, source-error, invalid-data, local-store-error, refresh, retry, week navigation, and bottom-navigation labels are covered by the string boundary.
- Date and time display uses an injectable or testable formatter boundary where practical so tests can verify stable output without depending on the device clock or locale.
- Error copy remains user-presentable and does not expose raw exception messages, source URLs, SQLDelight details, Ktor errors, DTO field names, or local file paths.
- The localization boundary stays in presentation/shared code and uses Compose Multiplatform resources, not Android-only or iOS-only resource dependencies.

### QA Test Cases

- Verify the selected bottom tab label still reads `Schedule`.
- Verify a fake week 13 state produces `Week 13 Schedule`.
- Verify a fake last-updated timestamp produces deterministic display text such as `Last updated 10:42 AM`.
- Verify race counts are formatted through the Compose Multiplatform plural resource boundary for both singular and plural cases.
- Verify loading, empty, cached, source-error, invalid-data, and local-store-error states all render user-facing copy from the shared boundary.
- Verify reusable components can be rendered with alternate fake strings without changing component internals.
- Verify no raw internal exception, source URL, SQLDelight detail, Ktor detail, DTO field name, or local file path appears in user-facing error text.

## Story 3.3: Replace the Starter App With the Schedule Shell

**As a user, I want the app to open on a Schedule screen so I immediately understand that the app is for browsing iRacing race weeks.**

### Current Starting Point

- Android and iOS currently host the shared starter `App` UI.
- Sprint 2 exposes shared app dependencies and planner data presentation state, but the starter UI does not consume the planner state holder.

### Acceptance Criteria

- The starter Compose logo/click-counter UI is removed from the main shared app surface.
- The first screen is a Schedule shell with:
  - `ScheduleHeader`,
  - date/week selector area,
  - action/count area,
  - race-list content area,
  - bottom navigation with `Schedule` selected.
- The top header title is derived from the selected race week and follows the pattern `Week {number} Schedule`.
- The header shows a last-updated line when freshness metadata is available.
- Inactive future tabs are visually present only if they do not navigate to unimplemented screens.
- Layout follows Sprint 3 screen padding, spacing, and bottom-navigation constraints.
- The app shell is shared between Android and iOS through Compose Multiplatform.

### QA Test Cases

- Launch Android debug app and confirm the starter screen is gone.
- Launch iOS Compose host or build the iOS target and confirm the shared Schedule shell is the root UI.
- Verify the selected bottom item says `Schedule`.
- Verify the header title uses a week title rather than the generic app name.
- Verify the screen remains readable at the reference `390 x 844` frame and does not overlap the bottom navigation.
- Verify future tabs do not crash or navigate to blank screens if tapped; disabled/no-op behavior is acceptable for Sprint 3.

## Story 3.4: Add Schedule MVI State Wiring

**As a developer, I want a small shared MVI boundary for the Schedule screen so UI behavior is testable and future filtering/detail work has a stable extension point.**

### Current Starting Point

- `PlannerDataStateHolder` already exposes planner data UI state and actions from Sprint 2.
- Sprint 3 needs screen-specific selected-week/date state and formatted display models without pushing UI logic into raw composables.

### Acceptance Criteria

- A Schedule presentation state model exists for the screen-level UI.
- A Schedule action model exists for at least initial load, refresh/retry, previous week, next week, and today/current-week selection.
- Screen-level state derives:
  - selected week,
  - schedule title,
  - last-updated display text,
  - race count text,
  - selected-week race card display models,
  - loading, empty, cached, and error rendering flags.
- The root composable collects state and sends actions; stateless screen composables render state and callbacks.
- MVI wiring uses the existing Sprint 2 planner data state holder or domain-safe use cases. UI code does not call data sources, local storage, DTOs, SQLDelight, Ktor, or repository implementations directly.
- Initial screen load happens once per root lifecycle entry and does not trigger independent duplicate loads for weeks, races, cars, and tracks.
- Refresh/retry routes through the existing planner data load action.
- Tests cover state derivation and action handling with fake planner data state.

### QA Test Cases

- With fake loaded data, verify the state model produces `Week 13 Schedule`, a last-updated string, a race count string, and non-empty race cards.
- With fake cached data, verify the state model preserves displayable data and exposes a cached warning/status.
- With fake empty data, verify state requests the empty panel rather than an empty race list with no explanation.
- With fake source error and no cache, verify state requests an error panel with retry.
- Trigger refresh/retry and verify one planner load action is sent.
- Trigger previous/next/today actions and verify selected-week state changes without reloading source data unnecessarily.

## Story 3.5: Implement Date and Week Selector Behavior

**As a user, I want to move between race weeks so I can browse the schedule for the week I care about.**

### Current Starting Point

- Domain race-week metadata is available from Sprint 2 loaded planner data.
- The wireframe defines a centered date/week selector with previous, next, today/date controls.

### Acceptance Criteria

- The date/week selector renders the selected week label and date context from loaded race-week data.
- Previous and next controls move to adjacent available race weeks.
- Previous is disabled or no-op at the first available week.
- Next is disabled or no-op at the last available week.
- Today/current-week selection chooses the race week that contains the current date when available.
- If the current date is outside the loaded season range, today/current-week selection chooses the nearest available week and does not crash.
- The selector uses Sprint 3 component constraints: `12dp` container padding, arrow controls with `44dp` minimum touch target, and readable `16sp`/`13sp` labels.
- Week selection is local presentation state. It does not mutate domain models or local cache.

### QA Test Cases

- Given three fake weeks, tapping next from the first week selects the second week.
- Given three fake weeks, tapping previous from the second week selects the first week.
- At the first week, previous does not move selection below available data.
- At the last week, next does not move selection beyond available data.
- Given a fake current date inside week 13, tapping today selects week 13.
- Given a fake current date outside the season, tapping today selects the nearest available week and keeps the UI stable.
- Verify the selector labels remain readable on a narrow mobile width.

## Story 3.6: Render the Selected-Week Race Card List

**As a user, I want to scan races for the selected week so I can quickly see series, track, car class, session timing, duration, and rain context.**

### Current Starting Point

- Sprint 2 loaded state includes race weeks, planner races, cars, tracks, freshness, and message data needed for the first schedule screen.
- Sprint 3 uses default display order only. User-controlled sorting belongs to Sprint 4.

### Acceptance Criteria

- The Schedule screen filters planner races to the selected week.
- The race count reflects only races displayable for the selected week.
- Race cards display the best available Sprint 3 fields:
  - series or race name,
  - track name and configuration when available,
  - car class or car summary,
  - license/category chips when available,
  - race length when available,
  - rain chance when available,
  - next session or session summary when available.
- Display data is mapped into presentation UI models before rendering cards.
- Race cards use Sprint 3 design constraints: `12dp` radius, `1dp` border, `14dp` padding, `10dp` internal gap, `12dp` list gap.
- Race order is deterministic for the selected week by stable source order or a stable display key. No user-controlled sort UI is exposed in Sprint 3.
- Missing optional display fields do not crash the UI and do not create broken labels.
- Race-card tap navigation is not implemented in Sprint 3. Cards may be non-clickable or expose disabled semantics until Sprint 5.

### QA Test Cases

- Given loaded data with races in multiple weeks, verify only the selected week’s races are rendered.
- Given selected week 13 with twelve races, verify the count reads `12 races`.
- Given a race with rain chance and race length, verify both are visible on the card.
- Given a race without optional rain chance, verify the card omits the rain label cleanly.
- Given repeated renders of the same data, verify race order stays stable.
- Verify no sort controls are present.
- Verify tapping a race card does not navigate to an unimplemented detail screen or crash.

## Story 3.7: Complete Schedule UI States and Platform Verification

**As a user, I want loading, cached, empty, and error states to be understandable so I know whether schedule data is ready, stale, unavailable, or invalid.**

### Current Starting Point

- Sprint 2 presentation state distinguishes loading, fresh loaded, cached loaded, empty, invalid data, source unavailable, and local-store failures.
- Sprint 3 needs to render those states in the Schedule UI and prove the shared screen runs on Android and iOS.

### Acceptance Criteria

- Loading state shows a clear state panel or progress region without displaying stale skeleton cards as real data.
- Empty state explains that no races are available for the selected week or loaded data set.
- Cached loaded state renders the schedule and shows a concise cached/stale warning or status line.
- Source unavailable with no cache renders an error state with retry.
- Invalid required source data with no cache renders a distinct user-presentable error state with retry.
- Local-store failure renders a user-presentable error state with retry when retry is meaningful.
- Error copy is presentation-safe and does not expose raw exception messages, source URLs, SQLDelight details, Ktor errors, or DTO field names.
- Basic accessibility semantics exist for the refresh action, week navigation controls, bottom navigation selected state, and retry action.
- Android debug build and shared Android host tests pass or blockers are documented.
- iOS simulator shared tests or iOS build are run when local tooling permits; blockers are documented with exact commands.
- `docs/development.md` is updated with any new Sprint 3 focused test files and verification commands.

### QA Test Cases

- Force loading state with fake state and verify no race cards are shown as real loaded data.
- Force empty loaded state and verify an empty panel is shown.
- Force cached loaded state and verify race cards remain visible with cached/stale status text.
- Force source unavailable error and verify retry is visible and sends a load action.
- Force invalid required source data error and verify the copy is distinct from empty state.
- Force local-store failure and verify raw internal exception details are not shown.
- Run Android host tests focused on Schedule state/UI logic.
- Run `./gradlew :androidApp:assembleDebug`.
- Run `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test` when Xcode is available.

## Sprint 3 Definition of Done

- The app opens to a shared Compose `Schedule` tab instead of the starter screen.
- The Schedule header displays a selected-week title and last-updated status.
- Week/date controls can move between available weeks and select the current week.
- The selected week’s races render as deterministic race cards.
- Loading, cached, empty, invalid-data, source-error, and local-store-error states are visible and testable.
- UI components follow the Sprint 3 design constraints and are reusable for Sprint 4/5.
- User-facing Schedule strings use Compose Multiplatform string/plural resources or resource-backed parameters for localization readiness; Sprint 3 remains English-only.
- Presentation follows a lightweight MVI boundary with stateless screen components.
- UI does not consume DTOs, SQLDelight rows, Ktor errors, source URLs, or local-storage details.
- Story-focused tests pass.
- Android debug build passes.
- iOS shared verification passes or has a documented local tooling blocker.

## Verification Plan

Run story-focused tests first, especially Schedule string-boundary tests, Schedule state/action tests, and component rendering tests.

Common Sprint 3 verification commands:

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```

Documentation-only grooming can be checked with:

```bash
git diff --check
```
