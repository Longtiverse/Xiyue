# Xiyue Tone Presets Design

**Date:** 2026-03-30  
**Scope:** Android playback tone quality, preset selection, and seamless preset switching

---

## Goal

Improve Xiyue playback timbre so it feels comfortable for long practice sessions, avoiding a sound that is overly electronic, sharp, hollow, hard, or fake.

## Product Decisions

### Tone Presets

Xiyue will provide exactly three tone presets:

1. **Warm Practice** — default preset, tuned for long-form practice comfort
2. **Soft Piano** — more natural and instrument-like
3. **Clear Wood** — clearer attack for ear training and note recognition

No additional presets or manual synthesis controls are included in this iteration.

### UI Placement

Tone selection belongs to the playback control area, not the content library.

- The current preset is shown as a compact label/chip in the bottom playback module
- Tapping it opens a lightweight in-context selector
- Choosing a preset applies immediately without navigating away

### Playback Behavior

- Switching tone while playing must **not stop playback**
- The new preset takes effect from the **next played note/step**
- Paused playback should keep the selected tone configuration instead of resetting to ready state

## Technical Design

### State Flow

Tone preset becomes part of:

- home UI state
- preferences persistence
- playback request
- playback service state transitions

### Synth Strategy

The existing synth remains sample-free and AudioTrack-based, but gains preset-specific shaping:

- harmonic balance
- attack/release contour
- high-frequency damping
- resonance/body shaping
- noise/wood emphasis
- stereo/bass bloom balance

The three presets share the same synthesis engine and differ only by carefully tuned parameter profiles.

### Constraints

- Keep implementation lightweight; no external sample packs
- Preserve existing background playback and notification controls
- Maintain seamless queued switching semantics already implemented for practice content

## Testing Expectations

Add test coverage for:

- tone preset state and UI exposure
- persistence of selected preset
- playback request carrying tone preset
- playback service preserving preset during play/pause/switch flows
- synth source containing preset-specific shaping logic

## Success Criteria

- User can switch between Warm / Piano / Wood directly from playback controls
- Default preset is Warm Practice
- Switching tone during playback is immediate and smooth
- Selected preset survives app restart
- Android tests and APK build continue passing
