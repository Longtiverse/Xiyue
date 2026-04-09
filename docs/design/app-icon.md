# Xiyue App Icon (Android) - Design Notes

## Context

Xiyue is a minimalist music-practice app centered on selecting scales/chords quickly and playing them with note + keyboard highlighting. The icon should feel calm, precise, and readable at small sizes.

## Concepts (3)

### Concept A (Recommended): "12-tone ring + triad highlight"

A quiet dark tile with a ring of 12 dots (semitones). Three accent dots form a simple triad, and one dot is slightly larger to suggest the "currently active" note during playback.

Why this fits:

- Directly maps to the app's core idea: notes, pitch classes, highlighting.
- Stays abstract (not a literal piano) and remains elegant.
- Scales down well: the silhouette is basically "ring + 3 bright points".

### Concept B: "Minimal X as two crossing note stems"

A stylized "X" built from two thin stems (like simplified music notation), with a single accent circle at the crossing (active note).

Pros:

- Very brandable (Xiyue -> X).
- Extremely simple.

Cons:

- Risk of looking like a generic "X" or close icon at small sizes.

### Concept C: "Single key + sharp"

A single piano key shape with a small sharp sign (#) above, hinting at keyboard mapping.

Pros:

- Immediately music-related.

Cons:

- Piano-key icons are common; higher risk of looking generic.
- Sharp sign can get noisy at small sizes.

## Recommendation

Pick **Concept A**. It communicates "notes + highlight" with minimal parts and strong small-size legibility.

## Shape and Color Guidance (Concept A)

Shape:

- Use a rounded-square tile (adaptive icon background), with a centered circular composition.
- Ring dots: 12 evenly spaced points.
- Highlight: 3 points (triad) in the accent color; 1 of them larger (active note).

Color:

- Background: near-black navy/graphite, optionally with a subtle radial glow.
- Ring dots (inactive): muted cool gray-blue.
- Accent: soft mint/teal (not neon) to keep the overall feel calm.

Contrast:

- Ensure accent dots remain visible on both light and dark wallpapers.
- Avoid thin strokes as the primary identifier; rely on the dot-ring silhouette.

## Small-Size Readability Notes

- Design should still read at notification-size and home-screen smallest grids.
- Avoid any text ("Xiyue", "X") in the icon.
- Keep the ring and active dot inside the central safe area; do not rely on corner detail.
- If the ring feels too busy at 24dp, reduce inactive dot opacity and keep the 3 accent dots strong.

## Android Adaptive Icon Notes

- Provide separate **background** (tile) and **foreground** (ring + highlights).
- Foreground should be centered with generous padding so masks (circle, squircle, teardrop) do not clip meaningfully.
- Test masked previews (circle/squircle) and ensure the 3 highlighted dots remain visible.

## Draft Assets

Recommended draft SVG:

- [Concept A SVG](/D:/Project/Xiyue/docs/design/assets/xiyue-icon-concept-a.svg)
