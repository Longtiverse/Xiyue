# Xiyue UX Iteration Design

Date: 2026-04-14
Status: Draft for user review
Source: `docs/ux-iteration-plan.md`

## Goal

Deliver the full UX iteration roadmap in `docs/ux-iteration-plan.md` by progressing through `Batch 1` to `Batch 5` without pausing for manual approval between batches, unless a true blocker or product ambiguity appears. Each batch must end with focused verification before the next batch begins.

## Scope

This design covers:

- Playback-core changes needed for live BPM updates, direction hot-switching, pause/resume highlighting, beat-synced visuals, jump-to-step, and later playback extensions
- Home-state and Compose UI changes needed to keep preview state, controls, transitions, and visual emphasis in sync with playback
- Domain/data/resource changes for inversion, chord tone metadata, Chinese descriptions, teaching content, recommendation paths, Combo availability, and launcher icon updates
- Batch-by-batch verification gates so the implementation can proceed autonomously from `Batch 1` through `Batch 5`

This design does not attempt to schedule or implement the `P2` backlog.

## Execution Model

The iteration runs on one long-lived feature branch, but delivery is gated by batch:

1. Implement the current batch only
2. Add or update tests before production changes for the behaviors in that batch
3. Run targeted verification for the touched area
4. Run Android-focused regression checks
5. Continue into the next batch only after the current gate is green

This gives continuous forward progress without mixing unfinished behaviors across unrelated priorities.

## System Boundaries

The work is organized across four layers.

### 1. Playback Core

Primary files:

- `apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt`
- `apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackRunner.kt`
- `apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshot.kt`
- `apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshotManager.kt`

Responsibilities:

- Own the true playback session
- Accept runtime updates while audio is already playing
- Publish snapshots that are rich enough for UI state and animations
- Keep pause/resume/stop timing decisions out of Compose

### 2. Home State

Primary files:

- `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeAction.kt`
- `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt`
- `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt`
- `apps/android/app/src/main/java/com/xiyue/app/features/home/HomePlaybackStateComputer.kt`
- `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeSelectionResolver.kt`
- `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiStateBuilder.kt`

Responsibilities:

- Translate user intent into stable state transitions
- Build preview plans and UI-facing models from selection state
- Merge playback snapshots from the service into view state without duplicating playback logic

### 3. Compose UI

Primary files:

- `apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt`
- `apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackDisplaySection.kt`
- `apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt`
- `apps/android/app/src/main/java/com/xiyue/app/features/home/KeyboardPreviewSection.kt`
- `apps/android/app/src/main/java/com/xiyue/app/ui/components/MetronomeVisualEffect.kt`
- `apps/android/app/src/main/java/com/xiyue/app/ui/components/ZoomableKeyboard.kt`
- `apps/android/app/src/main/java/com/xiyue/app/ui/components/SwipeableBpmSelector.kt`

Responsibilities:

- Show playback and ready states with smooth transitions
- Keep selected controls available when playback permits runtime changes
- Present beat, highlight, and emphasis effects using state from snapshots rather than local timing hacks

### 4. Domain, Data, and Resources

Primary files:

- `apps/android/app/src/main/java/com/xiyue/app/domain/PracticeModels.kt`
- `apps/android/app/src/main/java/com/xiyue/app/domain/PracticeSessionFactory.kt`
- `apps/android/app/src/main/assets/library.json`
- Navigation files under `apps/android/app/src/main/java/com/xiyue/app/navigation/`
- Launcher icon resources under `apps/android/app/src/main/res/`

Responsibilities:

- Define the playback content model and derived metadata
- Generate plans for scales, chords, inversions, and duration variants
- Store extra library metadata for descriptions, teaching, and recommended next practice
- Control navigation availability for incomplete screens such as Combo

## Playback Core Design

### Runtime-Updatable Session

Playback must evolve from a fire-once loop into a runtime-updatable session:

- `PlaybackRequest` remains the single playback input
- `PracticeSessionFactory.createPlan()` remains the single source for plan generation
- `PlaybackRunner` gains the ability to swap request and plan during active playback

The supported runtime updates will include:

- BPM changes that take effect on the next beat
- Playback mode changes that rebuild the plan and resume from the closest logical position
- Future step jumps that reuse the same update path

This avoids encoding feature-specific control branches inside Compose.

### Snapshot Enrichment

`PlaybackSnapshot` will carry the minimum additional state needed for UX features:

- A lightweight resume-highlight indicator so UI can flash the current note before playback continues
- A beat or pulse token that changes when the active step advances, allowing beat visuals to respond to actual playback progress
- Any step-position metadata needed for sequence click-to-jump and smarter active-index rendering

The snapshot remains descriptive, not imperative. UI reads it; the service remains in control.

### Pause and Resume Behavior

Pause and resume stay inside `PracticePlaybackService`:

- `pausePlayback()` preserves the current step and active note labels
- `resumePlayback()` re-enters playback from the saved step
- Before normal playback resumes, the service publishes a highlight-first snapshot for roughly one second
- Only after that highlight phase does the runner continue sounding notes

This keeps the resume cue synchronized across display, keyboard preview, and notifications.

### Error Handling

Playback mutations must fail safely:

- If a rebuilt plan cannot be created, the service keeps the current session instead of dropping into an invalid state
- Jump requests clamp to valid step indices
- BPM and timing inputs remain clamped to supported bounds
- Stale queued updates are cleared after application

## Home State Design

### Reducer Role

`HomeReducer` remains a pure state transition layer, but it must stop blocking runtime-safe changes while playing:

- `UpdateBpm` should update state even during playback
- `UpdatePlaybackMode` should update state even during playback
- `SelectRoot` should immediately recompute preview state and, when appropriate, trigger paused-preview refresh behavior

The reducer itself should not try to perform playback side effects. `XiyueApp` remains the place that compares previous and next state and decides whether the service must refresh or prepare a paused session.

### Preview and Keyboard State

`HomeStateFactory` and friends should ensure preview state always reflects the latest selection:

- Root changes must recompute preview pitch classes immediately
- Non-playing keyboard preview should show the selected scale or chord tones instead of appearing empty
- Chord previews must be able to carry degree metadata for layered coloring later in `Batch 2`

This means the plan-derived preview model must be robust even when no live playback snapshot exists.

## Compose UI Design

### Screen-Level Transitions

`HomeScreen` becomes the top-level owner of ready/playing layout transitions:

- Ready and playing regions transition with `AnimatedVisibility`, `AnimatedContent`, or `Crossfade`
- Controls that are safe during playback stay interactive instead of being globally disabled
- The playing state should not hide important orientation controls that the new UX depends on

### Playback Display

`PlaybackDisplaySection` will become the main playback-feedback surface:

- Current note and active chip receive stronger emphasis
- Resume highlight state produces a short flash treatment before sound continues
- Sequence chips can become clickable for jumping in later batches
- Overflow behavior supports larger sequences cleanly

### Beat-Synced Visuals

Beat effects should respond to real playback progress, not just an infinite timer:

- `MetronomeEdgeGlow` should be driven by snapshot pulse changes when possible
- The play button and current note emphasis can use the same pulse source
- This allows visuals to stay aligned after BPM changes, pauses, and resume flows

### Keyboard Preview

`KeyboardPreviewSection` and `ZoomableKeyboard` should converge on a richer preview model:

- Root changes should visibly update without needing unrelated recomposition
- Non-playing selections should highlight the target notes
- Chord tone degree colors should map root, third, fifth, and seventh to progressively lighter values

## Data and Resource Design

### Domain Models

The domain layer should gain only the fields that unlock planned UX:

- `PracticeSelection.inversion` for chord inversion support
- Additional step metadata, likely on `PlaybackStep`, for chord tone degree and any future UI emphasis hints

The design intentionally avoids a broad model rewrite. New metadata should be additive and local.

### Session Factory

`PracticeSessionFactory` remains the only place that defines what gets played:

- Inversion changes reorder chord intervals before step creation
- Block-mode duration variants come from plan-generation rules, not UI timers
- Preview plans and live plans share the same underlying logic to reduce drift

### Library Data

`library.json` should evolve incrementally to support later batches:

- Chinese-forward display metadata
- Teaching or “learn more” content
- Recommended next-practice links

The app should degrade gracefully when optional fields are absent.

### Navigation and Combo

Combo usability is treated as a product-debt decision:

- Short-term acceptable outcome: hide the tab until playback is actually usable
- Long-term outcome: implement functional Combo playback

The implementation can choose the short-term path first if it is the safer route during the full iteration.

### Launcher Icon

The launcher icon update is isolated to `Batch 2` resource work:

- Replace the current foreground vector with the “sound ripple” concept from `docs/icon-proposals.html`
- Keep adaptive-icon safe-zone constraints for round and rounded-square crops
- Prefer resource-level verification rather than mixing icon iteration into playback work

## Batch Strategy

### Batch 1

Focus: playback pain points that unblock the rest of the roadmap.

Must include:

- Real-time BPM updates during playback
- Playback-mode hot switching during playback
- Immediate keyboard refresh after root changes
- Beat-synced visual feedback
- Resume highlight before sound continues
- Smooth ready/playing transitions

### Batch 2

Focus: visual hierarchy, tone feedback, inversion support, and icon polish.

Must include:

- Current-note emphasis
- Chord-degree keyboard colors
- Tone-preset demo playback
- Solfege explanation copy
- Chord inversion support
- Combo usability fix or tab hiding
- Launcher icon replacement

### Batch 3

Focus: onboarding clarity and richer library content.

Must include:

- Idle or first-open play-button breathing animation that gently suggests the primary action without adding a full onboarding flow
- Chinese-forward library cards where the Chinese description or localized name is prominent and the English label remains secondary
- Difficulty stars on library items, with a simple beginner/intermediate/advanced visual distinction
- Grouped library browsing so scales and chords are easier to scan than the current flat row
- A "learn more" affordance for each library item that surfaces description, interval structure, and typical use cases from `library.json`

Primary acceptance criteria:

- A new user can identify what to tap first without a blocking tutorial
- Library rows communicate name, type, difficulty, and learning context without opening playback
- Existing favorites and recent selections still work with grouped display
- Missing optional teaching metadata does not crash or hide the item

Out of scope:

- No first-run wizard, account system, progress tracking, or full lesson engine

### Batch 4

Focus: playback control depth and richer interaction.

Must include:

- Automatic short audio preview after selecting a scale or chord
- BPM fine controls with +/- stepping and a path toward direct numeric input
- Octave selector using the existing `PracticeSelection.octave` field instead of hard-coded octave 4
- Tap an already-rendered sequence step to jump back to that playback position
- More visible stop affordance for pause state or first-use long-press guidance

Primary acceptance criteria:

- Selection preview is brief, interruptible, and does not conflict with active practice playback
- BPM fine changes are reflected in state, persisted preferences, and playback requests
- Octave changes affect generated MIDI notes and visible note labels
- Step jump clamps safely and updates both audio position and UI snapshot
- Stop/reset is discoverable without removing the existing long-press/double-tap shortcut

Out of scope:

- No MIDI clock sync, external controller support, or full transport timeline

### Batch 5

Focus: polish, readability, guidance, and final UX consistency.

Must include:

- Non-playing keyboard preview highlights the full selected scale or chord instead of waiting for playback
- Dark-theme white keys gain clearer contrast through border/shadow treatment
- Long playback sequences wrap or scroll cleanly instead of crowding chips
- Recommended next-practice path appears after completing or practicing an item, backed by optional `nextRecommended` data
- Chord block duration can be controlled independently from BPM for sustained chord practice

Primary acceptance criteria:

- Ready state still teaches the selected material visually before sound starts
- Keyboard contrast is improved without breaking active/current key color hierarchy
- Sequences with 8 or more notes remain legible on mobile width
- Recommendation UI is optional and data-driven; absent data means no recommendation, not an error
- Chord block duration only affects block playback and does not unexpectedly slow arpeggios

Out of scope:

- No statistics dashboard, mastery tracking, achievements, reminders, or custom scale authoring

## Testing Strategy

The project already relies heavily on Node-based source-contract tests under `apps/android/test`. This iteration will preserve that pattern while tightening behavior coverage.

For each batch:

1. Add or update the failing test first for the behavior being introduced
2. Verify the test fails for the expected reason
3. Implement the smallest change set that makes it pass
4. Re-run the focused test file
5. Re-run `npm run test:android`

Milestone checks:

- Run `npm test` after larger cross-batch changes or before any merge-quality completion claim
- Run targeted lint or build checks if the touched files require them

## Risks and Mitigations

### Risk: Playback logic drifts from preview logic

Mitigation:

- Keep `PracticeSessionFactory` as the common plan-generation source
- Use the same selection inputs for preview and live playback whenever possible

### Risk: UI animations desynchronize from actual beats

Mitigation:

- Drive beat visuals from snapshot step progression instead of local infinite loops alone

### Risk: Too many unrelated edits accumulate in one pass

Mitigation:

- Keep the implementation batch-gated even on a single branch
- Verify at every batch boundary before moving forward

### Risk: Dirty working tree causes accidental unrelated commits

Mitigation:

- Stage and commit only files created or changed for the current task
- Avoid touching existing unrelated docs and local files

## Verification Plan

Before claiming this iteration design is ready to implement:

- Confirm the batch order and autonomous progression rule with the user
- Confirm the playback core, home state, UI, and data boundaries
- Write this design doc
- Perform a local self-review for gaps and inconsistencies
- Ask the user to review this spec file before implementation planning begins

## Implementation Handoff

Once the user approves this spec, the next step is to write a detailed implementation plan that breaks the work into batch-sized execution steps, test additions, and verification commands.
