# Adaptive and Navigation 3 Readiness Design

## Summary

Adaptive and responsive design is a cross-sprint architecture concern, not a Sprint 3 feature. Sprint 3 should stay focused on the phone-first Schedule MVP while preserving the seams needed for future compact and expanded layouts. Navigation 3 should be introduced when the app gets its first real navigation feature: Schedule list to Race Detail.

This design intentionally does not choose tablet, iPad, foldable, landscape, or exact breakpoint behavior. Those are Sprint 5 product/design decisions.

## Current State

- The shared app root opens the Schedule shell directly.
- Bottom tabs are visual Sprint 3 affordances, not real route/back-stack navigation.
- Race-card tap navigation is out of scope until Race Detail.
- Sprint 3 already expects stateless screen components, callback-driven UI, and presentation models instead of DTOs or storage rows.

## Strategy

- Keep Sprint 3 phone-first, but require Schedule components to avoid phone-only assumptions that would block expanded layouts later.
- Keep app-level chrome separate from Schedule business state. Schedule screen state should not own route keys, back stacks, Navigation 3 APIs, or top-level navigation policy.
- When data-backed race cards are introduced, their display models should preserve a stable race identifier. Sprint 5 can then route to Race Detail without re-deriving identity from display text.
- Keep Sprint 4 filters, sorting, and preference controls reusable as content that can live in compact sheets or future expanded panes.
- Introduce Navigation 3 only with real navigation behavior, currently Sprint 5 Race Detail.

## Sprint 5 Direction

Sprint 5 should start with a Navigation 3 compatibility gate before depending on Navigation 3 APIs:

- Add Navigation 3 runtime, Navigation 3 UI, and Material adaptive Navigation 3 dependencies in the smallest possible change.
- Verify Android and iOS shared builds/tests before routing or adaptive layout work depends on those APIs.
- If iOS parity fails, stop and raise the blocker instead of quietly shipping Android-only adaptive behavior.

After the gate passes, Sprint 5 can add shared route keys such as `ScheduleRoute` and `RaceDetailRoute(raceId)`. The app root should own route/back-stack state. Schedule and Race Detail screens should remain callback-driven and render only the state they receive.

Compact behavior should open Race Detail as a normal destination from a selected race card. Expanded behavior can render Schedule and Race Detail together through Navigation 3 scenes or Material adaptive Navigation 3 once the Sprint 5 layout design chooses exact size-class behavior.

## References

- Navigation 3 setup: https://developer.android.com/guide/navigation/navigation-3/get-started
- Navigation 3 scenes: https://developer.android.com/guide/navigation/navigation-3/scenes
- Roadmap: [../../roadmap.md](../../roadmap.md)
- Sprint 3 stories: [../../sprint-3-user-stories.md](../../sprint-3-user-stories.md)

## Verification

Docs-only updates should be verified with:

```bash
git diff --check
```

The future Navigation 3 compatibility gate should run:

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```

Future UI tests and previews should cover compact single-pane behavior and expanded list-detail behavior after the Sprint 5 layout decision exists.
