# Android Playback Transport Iteration Design

## Goal

Add a more practical playback transport flow for the Android app by letting practice sessions pause and resume from the foreground notification and by reflecting that paused state in the home controls.

## Why this slice

This improves two current priorities at once: stronger background playback and more practical notification/playback interaction. It is shippable without expanding the audio engine surface too far because resume can restart the current selection from the beginning rather than trying to resume sample-accurately mid-note.

## Approach

1. Extend playback state with a paused flag so the UI and service can distinguish idle vs paused.
2. Teach `PracticePlaybackService` new pause/resume notification actions while retaining the existing play/stop path and APK archive pipeline.
3. Update home UI state and controls so the main transport button becomes Start / Pause / Resume, with an explicit Stop affordance when a session is active or paused.

## Constraints

- Keep test-first discipline with repository text-contract tests.
- Preserve the existing foreground service and build/archive flow.
- Avoid large refactors or toolchain changes.
