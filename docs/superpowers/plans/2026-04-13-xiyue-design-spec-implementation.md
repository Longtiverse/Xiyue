# Xiyue Design Spec Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align the Android Compose app with `docs/design-spec.md`, focusing on the spec-defined practice home, custom combo tab, favorites, settings, navigation, and visual tokens.

**Architecture:** Keep the existing Compose structure and state reducer flow, but reshape the UI/state contracts around the spec: no search input on the home screen, swipe-style selectors for root/BPM, single-button playback, read-only playing pills, a fourth navigation tab for custom combos, and settings-driven hint visibility. Add a small custom-combo feature module instead of overloading the home screen.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, reducer-style UI state, Node-based source contract tests, Gradle Android app module.

---

### Task 1: Lock the target contracts in tests

**Files:**
- Modify: `apps/android/test/home-feature.test.js`
- Modify: `apps/android/test/home-components.test.js`
- Create: `apps/android/test/navigation-and-secondary-screens.test.js`

- [ ] **Step 1: Write failing tests for the spec-driven UI contracts**
- [ ] **Step 2: Run `npm run test:android -- home-feature home-components navigation-and-secondary-screens` or equivalent targeted commands and verify the new assertions fail for the right reasons**
- [ ] **Step 3: Update tests only until they describe the intended design-spec behavior precisely**
- [ ] **Step 4: Re-run the targeted tests and confirm they still fail because implementation is missing**

### Task 2: Reshape home-state contracts for spec terminology

**Files:**
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeAction.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiStateBuilder.kt`

- [ ] **Step 1: Add failing assertions for single-button playback, BPM chip selector state, hint visibility, and chord playback mode**
- [ ] **Step 2: Run the targeted tests and verify red**
- [ ] **Step 3: Implement the minimal state/action changes to support the new contracts**
- [ ] **Step 4: Re-run the targeted tests and confirm green before moving to UI work**

### Task 3: Rebuild the practice home UI around the approved spec

**Files:**
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackDisplaySection.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/KeyboardPreviewSection.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/ui/components/SwipeableRootNoteSelector.kt`
- Create or modify: `apps/android/app/src/main/java/com/xiyue/app/ui/components/SwipeableBpmSelector.kt`

- [ ] **Step 1: Write/extend tests for removing the search box and stop button, adding spec tabs, selector chips, hint text, and playing-state pills**
- [ ] **Step 2: Run the focused tests and verify they fail**
- [ ] **Step 3: Implement the home-screen UI changes with the smallest passing edit set**
- [ ] **Step 4: Re-run focused tests and refactor only after green**

### Task 4: Align navigation, favorites, settings, and custom combo screen

**Files:**
- Modify: `apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/navigation/BottomNavigation.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/favorites/FavoritesScreen.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/settings/SettingsScreen.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/persistence/SettingsRepository.kt`
- Create: `apps/android/app/src/main/java/com/xiyue/app/features/combo/ComboScreen.kt`

- [ ] **Step 1: Add failing tests covering the fourth tab, favorites count/empty state, settings hint toggle, and combo screen existence**
- [ ] **Step 2: Run the focused tests and verify red**
- [ ] **Step 3: Implement the new tab and the secondary-screen updates**
- [ ] **Step 4: Re-run focused tests and keep changes green**

### Task 5: Refresh theme tokens and verify end-to-end

**Files:**
- Modify: `apps/android/app/src/main/java/com/xiyue/app/ui/theme/Color.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/ui/theme/Theme.kt`
- Modify: any related Compose files that need updated colors

- [ ] **Step 1: Add failing assertions for accent/gold token coverage**
- [ ] **Step 2: Run the targeted theme tests and verify red**
- [ ] **Step 3: Implement the minimal palette/theme updates needed by the spec**
- [ ] **Step 4: Run `npm run test:android` and then `npm run lint -- apps/android/test` or the closest repo-supported verification commands**

