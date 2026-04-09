# Playback Quick Select Strip Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a bottom playback quick-select strip so Android users can switch between current, favorite, and recent scale/chord items without reopening the selector sheet.

**Architecture:** Extend the home UI state with compact quick-select chips derived from the selected item plus favorites/recent items. Render the chips inside the playback controls card so selection changes stay near tempo/playback actions and reuse the existing reducer + playback refresh flow.

**Tech Stack:** Kotlin, Jetpack Compose, Node test runner regex-based Android contract tests

---

### Task 1: Lock the slice with failing Android contract tests

**Files:**

- Modify: `apps/android/test/home-components.test.js`
- Modify: `apps/android/test/home-feature.test.js`

- [ ] Add assertions for quick-select UI state and playback controls rendering.
- [ ] Run `npm run test:android` and confirm the new assertions fail for missing quick-select support.

### Task 2: Add quick-select state plumbing

**Files:**

- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt`

- [ ] Add a focused UI model for playback quick-select chips.
- [ ] Derive deduplicated quick-select items from current, favorite, and recent library items.
- [ ] Keep current selection highlighted so playback changes remain obvious.

### Task 3: Render quick-select chips in playback controls

**Files:**

- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt`

- [ ] Render the quick-select chips above the play button.
- [ ] Wire each chip to `HomeAction.SelectLibraryItem` so existing reducer/playback refresh logic handles switching.

### Task 4: Verify the Android iteration

**Files:**

- No code changes expected.

- [ ] Run `npm run test:android`.
- [ ] Run `npm test`.
- [ ] Run `npm run build:android`.
- [ ] Record the latest APK path from `builds/android/latest`.
