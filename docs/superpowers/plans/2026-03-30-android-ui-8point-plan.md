# Android UI 8-Point Sprint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Raise the Android app from the current ~6.5/10 prototype to a clearly usable 8/10 build by finishing the fixed-layout interaction model, simplifying selection flow, and tightening seamless playback switching.

**Architecture:** Keep the existing Android Compose + foreground playback service architecture, but rebalance responsibilities. The home screen becomes a fixed three-zone layout: hero playback display, lightweight practice picker strip, and compact transport bar. The full library becomes a lighter overlay. Playback state becomes more explicit so UI always reflects what is currently sounding and what is queued next.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, foreground `Service`, existing `PracticeSessionFactory`, Node string-contract tests, Gradle debug APK build pipeline.

---

## File Structure / Responsibility Map

- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeUiState.kt`
  - Shrink and rebalance state contracts for hero area, picker strip, overlay, and transport.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeAction.kt`
  - Add explicit actions for the new picker/overlay flow if needed.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeReducer.kt`
  - Keep transitions predictable for hot switching, overlay visibility, and compact controls.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeStateFactory.kt`
  - Derive the new compact UI slices and align ��current playback�� vs ��queued next�� labels.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeScreen.kt`
  - Host the true fixed three-zone layout.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\PlaybackDisplaySection.kt`
  - Hero card: current item, current note, step count, sequence, keyboard highlights.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\PlaybackControlsSection.kt`
  - Compact transport only: play/pause/resume, stop, loop, mode, BPM.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\PracticeLibrarySection.kt`
  - Lightweight overlay browser: filter, search, favorites, recents, grouped items.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\ui\XiyueApp.kt`
  - Keep playback refresh rules correct when switching while playing.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\PlaybackSnapshot.kt`
  - Add/keep snapshot fields needed for current-vs-queued playback UI.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\PracticePlaybackService.kt`
  - Make queued switch semantics and snapshot publication more consistent.
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\ui\theme\Color.kt`
- `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\ui\theme\Theme.kt`
  - Final dark grayscale cleanup.
- Tests:
  - `D:\Project\Xiyue\apps\android\test\home-components.test.js`
  - `D:\Project\Xiyue\apps\android\test\home-feature.test.js`
  - `D:\Project\Xiyue\apps\android\test\home-state-machine.test.js`
  - `D:\Project\Xiyue\apps\android\test\playback-service.test.js`

## Success Criteria for the 8/10 Gate

- Main screen has one obvious dominant playback display.
- High-frequency selection is reachable in one tap on the main screen.
- Full browsing is possible from a light overlay, not a settings-heavy subpage.
- Bottom controls feel like transport controls, not a second settings panel.
- Playing content can be switched without a stop/restart feel.
- UI clearly distinguishes ��currently sounding�� and ��queued next��.
- Full Node test suite passes and a fresh debug APK is archived with timestamp metadata.

---

### Task 1: Lock the new UI contract in tests first

**Files:**

- Modify: `D:\Project\Xiyue\apps\android\test\home-components.test.js`
- Modify: `D:\Project\Xiyue\apps\android\test\home-feature.test.js`
- Modify: `D:\Project\Xiyue\apps\android\test\home-state-machine.test.js`
- Test: `D:\Project\Xiyue\apps\android\test\home-components.test.js`

- [ ] **Step 1: Write the failing test assertions for the 8-point layout**
  - Assert the home screen uses fixed hero + picker strip + controls.
  - Assert `PlaybackDisplaySection` owns sequence + keyboard highlight presentation.
  - Assert `PlaybackControlsSection` no longer owns quick-select chips.
  - Assert `PracticeLibrarySection` no longer owns root/BPM/mode/loop settings.

- [ ] **Step 2: Run targeted tests to verify they fail**

Run: `npm run test:android -- home-components.test.js home-feature.test.js home-state-machine.test.js`
Expected: FAIL on old fields/components such as `quickSelectItems`, `tempoPresets`, `Settings`, or old screen structure.

- [ ] **Step 3: Keep the test contract small and structural**
  - Do not overfit to spacing values.
  - Only lock the architecture and user-critical controls.

- [ ] **Step 4: Re-run the same tests after edits**

Run: `npm run test:android -- home-components.test.js home-feature.test.js home-state-machine.test.js`
Expected: Still FAIL until production code is updated, but fail for the intended reasons.

- [ ] **Step 5: Commit**

```bash
git add apps/android/test/home-components.test.js apps/android/test/home-feature.test.js apps/android/test/home-state-machine.test.js
git commit -m "test: lock 8-point home ui contract"
```

### Task 2: Simplify the home state model

**Files:**

- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeUiState.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeAction.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeReducer.kt`
- Test: `D:\Project\Xiyue\apps\android\test\home-state-machine.test.js`

- [ ] **Step 1: Write/adjust failing reducer contract tests for renamed or removed state**
  - Rename `isSelectorSheetVisible` to a neutral overlay concept.
  - Remove dead fields such as `sections` if they are no longer used.
  - Add a compact picker strip UI state instead of keeping picker data inside transport.

- [ ] **Step 2: Run reducer/state tests to verify failure**

Run: `npm run test:android -- home-state-machine.test.js home-feature.test.js`
Expected: FAIL because state/action names no longer match current implementation.

- [ ] **Step 3: Implement the minimal state contract changes**
  - Add a dedicated picker strip model containing current filter, visible shortcuts, and a ��more�� affordance.
  - Reduce `PlaybackControlUiState` to transport-only data.
  - Keep `PlaybackDisplayUiState` focused on hero presentation data.

- [ ] **Step 4: Re-run reducer/state tests**

Run: `npm run test:android -- home-state-machine.test.js home-feature.test.js`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt apps/android/app/src/main/java/com/xiyue/app/features/home/HomeAction.kt apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt apps/android/test/home-state-machine.test.js apps/android/test/home-feature.test.js
git commit -m "refactor: simplify home ui state for fixed layout"
```

### Task 3: Rebuild derived state in `HomeStateFactory`

**Files:**

- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeStateFactory.kt`
- Test: `D:\Project\Xiyue\apps\android\test\home-feature.test.js`

- [ ] **Step 1: Add failing factory assertions**
  - Hero display should derive current item, current note, step index/count, queued-next hint.
  - Picker strip should derive only 4-6 visible shortcuts.
  - Full library overlay should continue to derive grouped results independently.

- [ ] **Step 2: Run targeted test to verify failure**

Run: `npm run test:android -- home-feature.test.js`
Expected: FAIL because factory still feeds old fields like `sections`, `quickSelectItems`, or transport-heavy data.

- [ ] **Step 3: Implement minimal derivation changes**
  - Use playback snapshot as the truth source for what is currently sounding.
  - Use selected item/root/mode/BPM for intent state only when nothing is playing.
  - Separate shortcut strip derivation from overlay list derivation.

- [ ] **Step 4: Re-run factory tests**

Run: `npm run test:android -- home-feature.test.js`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt apps/android/test/home-feature.test.js
git commit -m "refactor: derive hero and picker ui state"
```

### Task 4: Finish the fixed home layout

**Files:**

- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeScreen.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\PlaybackDisplaySection.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\PlaybackControlsSection.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\PracticeLibrarySection.kt`
- Test: `D:\Project\Xiyue\apps\android\test\home-components.test.js`

- [ ] **Step 1: Keep failing structure tests in place**
  - No implementation edits before the red state is confirmed.

- [ ] **Step 2: Rebuild `HomeScreen.kt` around three fixed zones**
  - Hero display gets the largest weight.
  - Picker strip shows filter tabs + current shortcuts + root shortcuts.
  - Controls section stays compact at the bottom.

- [ ] **Step 3: Rebuild `PlaybackDisplaySection.kt` as a single dominant hero**
  - Preserve big current note.
  - Show step progress as text, not a loud secondary UI if it fights the minimalist feel.
  - Keep sequence and keyboard highlight inside the same card.
  - Remove leftover explanatory clutter that does not help real use.

- [ ] **Step 4: Reduce `PlaybackControlsSection.kt` to transport semantics**
  - Play/pause/resume button.
  - Stop button when relevant.
  - Loop toggle.
  - Direct mode toggle.
  - Direct BPM adjustment.
  - No quick-select chips here.

- [ ] **Step 5: Trim `PracticeLibrarySection.kt` into a browsing overlay**
  - Keep search, filter, favorites, recents, grouped list.
  - Remove settings accordion and embedded playback controls.

- [ ] **Step 6: Run UI contract tests**

Run: `npm run test:android -- home-components.test.js home-feature.test.js`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackDisplaySection.kt apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt apps/android/app/src/main/java/com/xiyue/app/features/home/PracticeLibrarySection.kt apps/android/test/home-components.test.js apps/android/test/home-feature.test.js
git commit -m "feat: finish fixed minimalist home layout"
```

### Task 5: Make seamless switching state-consistent

**Files:**

- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\PlaybackSnapshot.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\PracticePlaybackService.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\ui\XiyueApp.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeStateFactory.kt`
- Test: `D:\Project\Xiyue\apps\android\test\playback-service.test.js`

- [ ] **Step 1: Write or update failing service contract assertions**
  - Snapshot must distinguish current item and queued next item.
  - Queueing during playback must update notification/UI state immediately.
  - When queued switch applies, queued fields must clear.
  - Pause/resume semantics must not resurrect the wrong item.

- [ ] **Step 2: Run playback tests to verify failure**

Run: `npm run test:android -- playback-service.test.js`
Expected: FAIL on missing clear/reset semantics or missing queued-field usage.

- [ ] **Step 3: Implement the smallest consistent semantics**
  - Treat snapshot as authoritative for current playback.
  - Publish queued info immediately.
  - Clear queued info exactly when the new plan becomes active or playback stops.
  - Ensure `resumePlayback()` uses the intended request after queued changes.

- [ ] **Step 4: Re-run playback tests**

Run: `npm run test:android -- playback-service.test.js`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshot.kt apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt apps/android/test/playback-service.test.js
git commit -m "fix: align hot switching playback state"
```

### Task 6: Final dark-theme cleanup

**Files:**

- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\ui\theme\Color.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\ui\theme\Theme.kt`
- Test: `D:\Project\Xiyue\apps\android\test\theme-and-icon.test.js`

- [ ] **Step 1: Add/keep failing palette assertions only if the grayscale cleanup changes contract**
- [ ] **Step 2: Run theme test if needed**

Run: `npm run test:android -- theme-and-icon.test.js`
Expected: PASS or intentional RED before palette update.

- [ ] **Step 3: Remove any remaining bright accent usage that fights the black/white brief**
- [ ] **Step 4: Re-run theme test**

Run: `npm run test:android -- theme-and-icon.test.js`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/android/app/src/main/java/com/xiyue/app/ui/theme/Color.kt apps/android/app/src/main/java/com/xiyue/app/ui/theme/Theme.kt apps/android/test/theme-and-icon.test.js
git commit -m "style: finalize dark monochrome theme"
```

### Task 7: Full verification and APK archive

**Files:**

- Verify only; no planned source changes unless failures appear.

- [ ] **Step 1: Run Android tests**

Run: `npm run test:android`
Expected: PASS.

- [ ] **Step 2: Run full repository tests**

Run: `npm test`
Expected: PASS.

- [ ] **Step 3: Build fresh Android APK**

Run: `npm run build:android`
Expected: `BUILD SUCCESSFUL` and a new timestamped archive in `D:\Project\Xiyue\builds\android\archive` plus updated `latest\build-info.json`.

- [ ] **Step 4: Smoke-check archive metadata**

Run: `Get-Content D:\Project\Xiyue\builds\android\latest\build-info.json`
Expected: updated `version`, absolute timestamp like `20260330-xxxx`, and latest/archive filenames.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "chore: ship 8-point android ui sprint"
```

---

## Parallel Subagent Execution Plan

Use parallel agents only on disjoint write scopes:

1. **Worker A �C UI contracts + state model**
   - Owns: `HomeUiState.kt`, `HomeAction.kt`, `HomeReducer.kt`, `home-state-machine.test.js`, part of `home-feature.test.js`

2. **Worker B �C Hero/picker/overlay Compose UI**
   - Owns: `HomeScreen.kt`, `PlaybackDisplaySection.kt`, `PlaybackControlsSection.kt`, `PracticeLibrarySection.kt`, `home-components.test.js`

3. **Worker C �C Playback switch consistency**
   - Owns: `PlaybackSnapshot.kt`, `PracticePlaybackService.kt`, `XiyueApp.kt`, `playback-service.test.js`

Critical path rule: land Task 1 first, then Tasks 2-5 can overlap with careful file ownership, then Task 7 runs after integration.

## 8/10 Self-Review Checklist

Before calling the sprint done, manually judge:

- Can I switch to a common scale/chord from the main screen in one tap?
- Does switching while playing avoid a ��stop then restart�� feeling?
- Is the largest thing on the screen always the playback content?
- Does the bottom zone feel like transport, not settings overload?
- Does the overlay feel like ��more choices��, not ��another page��?
- Is the screen mostly dark grayscale and visually calm?
- Would I personally score this at least 8/10 against the user��s brief?
