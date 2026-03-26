import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const featureFiles = [
  'apps/android/app/src/main/java/com/xiyue/app/domain/InMemoryPracticeLibraryRepository.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt',
  'apps/android/app/src/main/java/com/xiyue/app/domain/PracticeSessionFactory.kt',
  'apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshot.kt',
  'apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt',
];

test('android home feature files exist', () => {
  for (const filePath of featureFiles) {
    assert.equal(existsSync(filePath), true, `${filePath} should exist`);
  }
});

test('android repository provides sample scale and chord library data', () => {
  const repository = readFileSync('apps/android/app/src/main/java/com/xiyue/app/domain/InMemoryPracticeLibraryRepository.kt', 'utf8');
  const models = readFileSync('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeModels.kt', 'utf8');

  assert.match(repository, /class InMemoryPracticeLibraryRepository/);
  assert.match(repository, /val items = listOf\(/);
  assert.match(repository, /PracticeKind\.SCALE/);
  assert.match(repository, /PracticeKind\.CHORD/);
  assert.match(repository, /"Major"/);
  assert.match(repository, /"Natural Minor"/);
  assert.match(repository, /"Harmonic Minor"/);
  assert.match(repository, /"Melodic Minor"/);
  assert.match(repository, /"Dorian"/);
  assert.match(repository, /"Mixolydian"/);
  assert.match(repository, /"Lydian"/);
  assert.match(repository, /"Phrygian"/);
  assert.match(repository, /"Locrian"/);
  assert.match(repository, /"Whole Tone"/);
  assert.match(repository, /"Major Blues"/);
  assert.match(repository, /"Major Triad"/);
  assert.match(repository, /"Minor Triad"/);
  assert.match(repository, /"Diminished"/);
  assert.match(repository, /"Augmented"/);
  assert.match(repository, /"Maj7"/);
  assert.match(repository, /"Dom7"/);
  assert.match(repository, /"Minor 7"/);
  assert.match(repository, /"Dim7"/);
  assert.match(repository, /"Add9"/);
  assert.match(repository, /"Sus2"/);
  assert.match(repository, /"Sus4"/);
  assert.match(repository, /aliases/);
  assert.match(repository, /override fun searchLibraryItems/);
  assert.match(models, /enum class PitchClass/);
  assert.match(models, /enum class PlaybackMode/);
  assert.match(models, /val intervals: List<Int>/);
  assert.match(models, /val aliases: List<String>/);
});

test('android home state maps practice library into quick selection, playback controls, and keyboard preview', () => {
  const uiState = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt', 'utf8');
  const factory = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt', 'utf8');
  const screen = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt', 'utf8');
  const sessionFactory = readFileSync('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeSessionFactory.kt', 'utf8');

  assert.match(uiState, /data class HomeUiState/);
  assert.match(uiState, /data class LibraryUiItem/);
  assert.match(uiState, /data class RootNoteUiItem/);
  assert.match(uiState, /data class PlaybackControlUiState/);
  assert.match(uiState, /data class PlaybackModeUiItem/);
  assert.match(uiState, /data class KeyboardKeyUiState/);
  assert.match(factory, /class HomeStateFactory/);
  assert.match(factory, /fun create\(/);
  assert.match(factory, /selectedRoot/);
  assert.match(factory, /loopEnabled/);
  assert.match(factory, /background playback/i);
  assert.match(factory, /Practice Library/);
  assert.match(factory, /Playback Controls/);
  assert.match(factory, /Keyboard Preview/);
  assert.match(screen, /@Composable/);
  assert.match(screen, /HomeScreen/);
  assert.match(screen, /LazyColumn/);
  assert.match(screen, /PracticeLibrarySection\(/);
  assert.match(screen, /PlaybackControlsSection\(/);
  assert.match(screen, /KeyboardPreviewSection\(/);
  assert.match(sessionFactory, /class PracticeSessionFactory/);
  assert.match(sessionFactory, /fun createPlan\(/);
  assert.match(sessionFactory, /PlaybackStep/);
});
