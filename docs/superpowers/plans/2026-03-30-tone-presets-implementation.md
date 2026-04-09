# Tone Presets Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add three minimal tone presets with an in-player selector, persistent state, and seamless playback switching.

**Architecture:** Introduce a shared tone preset enum that flows from home state to playback requests and into the synth. Reuse the current AudioTrack synth engine, but parameterize its shaping so Warm Practice, Soft Piano, and Clear Wood sound distinct without adding heavy dependencies. Preserve the current queued-switch playback semantics and extend them to tone preset changes.

**Tech Stack:** Kotlin, Jetpack Compose, AudioTrack synthesis, SharedPreferences, Node string-contract tests, Gradle Android build

---

### Task 1: Model the tone preset state

**Files:**

- Create: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\TonePreset.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\PlaybackSnapshot.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeUiState.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeAction.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeStateFactory.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeReducer.kt`
- Test: `D:\Project\Xiyue\apps\android\test\home-components.test.js`
- Test: `D:\Project\Xiyue\apps\android\test\home-feature.test.js`

- [ ] **Step 1: Write failing tests for tone preset state exposure**
- [ ] **Step 2: Run targeted tests and verify RED**
- [ ] **Step 3: Add shared tone preset enum and UI state fields**
- [ ] **Step 4: Re-run targeted tests and verify GREEN**

### Task 2: Persist and transport the selected tone preset

**Files:**

- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomePreferencesRepository.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\ui\XiyueApp.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\PlaybackSnapshot.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\PracticePlaybackService.kt`
- Test: `D:\Project\Xiyue\apps\android\test\home-persistence.test.js`
- Test: `D:\Project\Xiyue\apps\android\test\playback-service.test.js`

- [ ] **Step 1: Write failing tests for persistence and playback request transport**
- [ ] **Step 2: Run targeted tests and verify RED**
- [ ] **Step 3: Thread tone preset through preferences, requests, intents, and snapshots**
- [ ] **Step 4: Re-run targeted tests and verify GREEN**

### Task 3: Add the playback-controls tone selector

**Files:**

- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\PlaybackControlsSection.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\HomeScreen.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\features\home\PracticeLibrarySection.kt`
- Test: `D:\Project\Xiyue\apps\android\test\home-components.test.js`

- [ ] **Step 1: Write failing UI contract tests for compact tone selector**
- [ ] **Step 2: Run targeted tests and verify RED**
- [ ] **Step 3: Implement compact tone selector in playback controls**
- [ ] **Step 4: Re-run targeted tests and verify GREEN**

### Task 4: Implement preset-specific synth shaping and seamless switching

**Files:**

- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\ToneSynth.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\playback\PracticePlaybackService.kt`
- Modify: `D:\Project\Xiyue\apps\android\app\src\main\java\com\xiyue\app\ui\XiyueApp.kt`
- Test: `D:\Project\Xiyue\apps\android\test\playback-service.test.js`

- [ ] **Step 1: Write failing tests for preset-aware synth/service behavior**
- [ ] **Step 2: Run targeted tests and verify RED**
- [ ] **Step 3: Parameterize synth profiles and apply preset switching on next note**
- [ ] **Step 4: Re-run targeted tests and verify GREEN**

### Task 5: Full verification and APK build

**Files:**

- Modify: `D:\Project\Xiyue\scripts\build-android.ps1`
- Test: `D:\Project\Xiyue\package.json`
- Test: `D:\Project\Xiyue\apps\android\test\build-archive.test.js`

- [ ] **Step 1: Run `npm run test:android`**
- [ ] **Step 2: Run `npm test`**
- [ ] **Step 3: Run `npm run build:android`**
- [ ] **Step 4: Verify latest/archive APK outputs and build-info metadata**
