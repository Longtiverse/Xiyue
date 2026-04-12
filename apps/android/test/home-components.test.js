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
  assert.match(screen, /CompactLibrarySelector\(/);
  assert.match(screen, /SwipeableRootNoteSelector/);
  assert.match(screen, /FilterChip/);
  assert.match(screen, /LazyRow/);
  assert.match(screen, /Column\(/);
  assert.doesNotMatch(screen, /OutlinedTextField/);
  assert.doesNotMatch(screen, /Search/);

  assert.match(displaySection, /AnimatedContent/);
  assert.match(displaySection, /TogglePlaybackDisplayMode/);
  assert.match(displaySection, /NOTE_FOCUS/);
  assert.match(displaySection, /Playing/);
  assert.match(displaySection, /sequenceNotes/);
  assert.match(displaySection, /keyboardState/);
  assert.match(displaySection, /TextAlign\.Center/);
  assert.match(displaySection, /WaveformVisualizer/);
  assert.match(displaySection, /gold|Gold|accent/i);

  assert.match(playbackSection, /combinedClickable|onLongClick|onDoubleClick/);
  assert.match(playbackSection, /SwipeableBpmSelector/);
  assert.match(playbackSection, /TogglePlayback/);
  assert.match(playbackSection, /StopPlayback/);
  assert.match(playbackSection, /ToggleLoop/);
  assert.match(playbackSection, /UpdateTonePreset/);
  assert.match(playbackSection, /UpdatePlaybackMode/);
  assert.match(playbackSection, /FilterChip|AssistChip|Surface/);
  assert.doesNotMatch(playbackSection, /Slider/);
  assert.doesNotMatch(playbackSection, /EnhancedBpmSlider/);
  assert.doesNotMatch(playbackSection, /AlertDialog/);
  assert.doesNotMatch(playbackSection, /OutlinedTextField/);
  assert.doesNotMatch(playbackSection, /OutlinedButton/);

  assert.match(keyboardSection, /Row/);
  assert.match(keyboardSection, /Live/);
  assert.match(keyboardSection, /current|active|scale/i);
  assert.match(keyboardSection, /animateColorAsState|Card|Surface/);
});
