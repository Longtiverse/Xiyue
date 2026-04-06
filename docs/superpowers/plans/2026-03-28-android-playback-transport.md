# Android Playback Transport Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add pause/resume-aware playback transport to the Android app and foreground notification.

**Architecture:** Extend playback snapshots with paused transport metadata, update the foreground service to manage pause/resume/stop intents and notification actions, and map that state into home UI controls. Keep the implementation minimal by resuming from the start of the current request.

**Tech Stack:** Kotlin, Jetpack Compose, Android foreground service, Node test runner

---

### Task 1: Lock the desired behavior in tests

**Files:**
- Modify: `apps/android/test/playback-service.test.js`
- Modify: `apps/android/test/home-feature.test.js`
- Modify: `apps/android/test/home-components.test.js`

- [ ] Add failing expectations for paused playback snapshots, pause/resume notification actions, and pause/resume/stop-aware home controls.
- [ ] Run targeted Android tests and confirm the new expectations fail for the intended reason.

### Task 2: Implement pause/resume transport state

**Files:**
- Modify: `apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshot.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt`

- [ ] Add paused-state fields and resumable request bookkeeping.
- [ ] Implement pause/resume intents and transport-aware notification building.
- [ ] Keep stop behavior resetting the service to the idle snapshot.

### Task 3: Surface the transport state in the home UI

**Files:**
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeAction.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/features/home/PracticeLibrarySection.kt`
- Modify: `apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt`

- [ ] Map playback snapshots into Start / Pause / Resume labels and an explicit Stop action.
- [ ] Wire the Compose actions to the service so app controls and notification controls stay aligned.

### Task 4: Verify the full Android iteration

**Files:**
- Verify only

- [ ] Run `npm run test:android`.
- [ ] Run `npm test`.
- [ ] Run `npm run build:android`.
- [ ] Capture the latest APK output path from `builds/android/latest`.
