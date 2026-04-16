import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const read = (filePath) => readFileSync(filePath, 'utf8');

const componentFiles = [
  'apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/KeyboardPreviewSection.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackDisplaySection.kt',
];

test('android home section components exist', () => {
  for (const filePath of componentFiles) {
    assert.equal(existsSync(filePath), true, `${filePath} should exist`);
  }
});

test('android home screen delegates to spec-shaped section composables', () => {
  const screen = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt');
  const playbackSection = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt'
  );
  const keyboardSection = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/KeyboardPreviewSection.kt'
  );
  const displaySection = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackDisplaySection.kt'
  );

  assert.match(screen, /PlaybackControlsSection\(/);
  assert.match(screen, /PlaybackDisplaySection\(/);
  assert.match(screen, /KeyboardPreviewSection\(/);
  assert.match(screen, /CompactLibrarySelector\(/);
  assert.match(screen, /SwipeableRootNoteSelector/);
  assert.match(screen, /LazyRow/);
  assert.match(screen, /Column\(/);
  assert.match(screen, /MockupSectionSurface/);
  assert.doesNotMatch(screen, /modifier = Modifier\.weight\(1f\)/);
  assert.doesNotMatch(screen, /OutlinedTextField/);
  // Search icon is now legitimately used for empty-state UI

  assert.match(displaySection, /AnimatedContent/);
  assert.match(displaySection, /TogglePlaybackDisplayMode/);
  assert.match(displaySection, /NOTE_FOCUS/);
  assert.match(displaySection, /Playing/);
  assert.match(displaySection, /sequenceNotes/);
  assert.match(displaySection, /keyboardState/);
  assert.match(displaySection, /TextAlign\.Center/);
  assert.match(displaySection, /WaveformVisualizer/);
  assert.match(displaySection, /gold|Gold|accent/i);
  assert.match(displaySection, /playbackGlowBrush/);
  assert.match(displaySection, /LiveStatusPill/);
  assert.match(displaySection, /SequenceChip/);
  assert.doesNotMatch(displaySection, /PianoKeyboardDisplay/);

  assert.match(playbackSection, /combinedClickable|onLongClick|onDoubleClick/);
  assert.match(playbackSection, /SwipeableBpmSelector/);
  assert.match(playbackSection, /Brush\.linearGradient/);
  assert.match(playbackSection, /widthIn\(max = 240\.dp\)/);
  assert.match(playbackSection, /OptionPill/);
  assert.match(playbackSection, /StatusPill/);
  assert.match(playbackSection, /TogglePlayback/);
  assert.match(playbackSection, /StopPlayback/);
  assert.match(playbackSection, /ToggleLoop/);
  assert.match(playbackSection, /UpdateTonePreset/);
  assert.match(playbackSection, /UpdatePlaybackMode/);
  assert.match(playbackSection, /Surface/);
  assert.doesNotMatch(playbackSection, /FilterChip/);
  assert.doesNotMatch(playbackSection, /AssistChip/);
  assert.doesNotMatch(playbackSection, /Slider/);
  assert.doesNotMatch(playbackSection, /EnhancedBpmSlider/);
  assert.doesNotMatch(playbackSection, /AlertDialog/);
  assert.doesNotMatch(playbackSection, /OutlinedTextField/);
  assert.doesNotMatch(playbackSection, /OutlinedButton/);

  assert.match(keyboardSection, /Row/);
  assert.match(keyboardSection, /Live/);
  assert.match(keyboardSection, /current|active|scale/i);
  assert.match(keyboardSection, /BlackKey/);
  assert.match(keyboardSection, /LocalDensity/);
  assert.match(keyboardSection, /whiteKeyWidthPx/);
  assert.match(keyboardSection, /blackKeyWidthPx/);
  assert.match(keyboardSection, /offset\(x = /);
  assert.match(keyboardSection, /KeyboardLegend/);
  assert.match(keyboardSection, /Brush\.verticalGradient/);
});

test('android root and bpm selectors use mockup scaled custom chips', () => {
  const rootSelector = read(
    'apps/android/app/src/main/java/com/xiyue/app/ui/components/SwipeableRootNoteSelector.kt'
  );
  const bpmSelector = read(
    'apps/android/app/src/main/java/com/xiyue/app/ui/components/SwipeableBpmSelector.kt'
  );

  assert.match(rootSelector, /RootSelectorChip/);
  assert.match(rootSelector, /targetValue = 0\.92f/);
  assert.match(rootSelector, /targetValue = 0\.85f/);
  assert.match(rootSelector, /alpha/);
  assert.doesNotMatch(rootSelector, /FilterChip/);

  assert.match(bpmSelector, /BpmSelectorChip/);
  assert.match(bpmSelector, /tempoLabel/);
  assert.match(bpmSelector, /Andante|Moderato|Allegro/);
  assert.match(bpmSelector, /TextAlign\.Center/);
  assert.match(bpmSelector, /targetValue = 0\.92f/);
  assert.match(bpmSelector, /targetValue = 0\.85f/);
  assert.doesNotMatch(bpmSelector, /FilterChip/);
});
