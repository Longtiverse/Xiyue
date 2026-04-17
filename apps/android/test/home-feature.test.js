import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const read = (filePath) => readFileSync(filePath, 'utf8');

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

test('android repository still provides scale and chord library data from assets', () => {
  const repository = read(
    'apps/android/app/src/main/java/com/xiyue/app/domain/InMemoryPracticeLibraryRepository.kt'
  );
  const models = read('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeModels.kt');

  assert.match(repository, /class InMemoryPracticeLibraryRepository/);
  assert.match(
    repository,
    /private val items = context\?\.let\(::loadFromAssets\) \?: defaultItems\(\)/
  );
  assert.match(repository, /loadFromAssets/);
  assert.match(repository, /PracticeKind\.SCALE/);
  assert.match(repository, /PracticeKind\.CHORD/);
  assert.match(repository, /"Major"/);
  assert.match(repository, /"Major Triad"/);
  assert.match(repository, /override fun searchLibraryItems/);
  assert.match(models, /enum class PitchClass/);
  assert.match(models, /enum class PlaybackMode/);
  assert.match(models, /val intervals: List<Int>/);
  assert.match(models, /val aliases: List<String>/);
});

test('android home state is aligned with the design spec selectors and playback metadata', () => {
  const uiState = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt');
  const factory = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt'
  );
  const screen = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt');
  const sessionFactory = read(
    'apps/android/app/src/main/java/com/xiyue/app/domain/PracticeSessionFactory.kt'
  );

  assert.match(uiState, /data class HomeUiState/);
  assert.match(uiState, /data class LibraryUiItem/);
  assert.match(uiState, /data class PlaybackDisplayUiState/);
  assert.match(uiState, /data class PlaybackControlUiState/);
  assert.match(uiState, /data class KeyboardPreviewUiState/);
  assert.match(uiState, /val libraryFilter: LibraryFilter/);
  assert.match(uiState, /val showHints: Boolean/);
  assert.match(uiState, /val isPaused: Boolean/);
  assert.match(uiState, /val playbackDisplay: PlaybackDisplayUiState/);
  assert.match(uiState, /val playbackControl: PlaybackControlUiState/);
  assert.match(uiState, /val keyboardPreview: KeyboardPreviewUiState/);
  assert.match(uiState, /val currentNoteLabel: String/);
  assert.match(uiState, /val stepIndex: Int/);
  assert.match(uiState, /val stepCount: Int/);
  assert.match(uiState, /val toneButtonLabel: String/);
  assert.match(uiState, /val modeOptions: List<PlaybackModeUiItem>/);
  assert.match(uiState, /val hintLabel: String/);
  assert.doesNotMatch(uiState, /val searchQuery: String/);
  assert.doesNotMatch(uiState, /val isBpmInputVisible: Boolean/);
  assert.doesNotMatch(uiState, /val showStopButton: Boolean/);
  assert.doesNotMatch(uiState, /val stopButtonLabel: String/);

  assert.match(factory, /class HomeStateFactory/);
  assert.match(factory, /buildPlaybackDisplay/);
  assert.match(factory, /buildPlaybackControl/);
  assert.match(factory, /buildKeyboardPreview/);
  assert.match(factory, /showHints/);
  assert.match(factory, /selectedTonePreset/);
  assert.match(factory, /durationMultiplier/);
  assert.match(
    factory,
    /keyboardPreview = uiStateBuilder\.buildKeyboardPreview[\s\S]*selectedRhythmPattern = selectedRhythmPattern,\s*durationMultiplier = durationMultiplier,/
  );
  assert.doesNotMatch(factory, /searchQuery/);
  assert.doesNotMatch(factory, /isBpmInputVisible/);

  assert.match(screen, /CompactLibrarySelector/);
  assert.match(screen, /SwipeableRootNoteSelector/);
  assert.match(screen, /SelectRoot/);
  assert.match(screen, /UpdateLibraryFilter/);
  assert.doesNotMatch(screen, /OutlinedTextField/);
  // Icons.Default.Search is now legitimately used for empty-state UI
  assert.doesNotMatch(screen, /UpdateSearchQuery/);
  assert.doesNotMatch(screen, /ClearSearchQuery/);

  assert.match(sessionFactory, /class PracticeSessionFactory/);
  assert.match(sessionFactory, /fun createPlan\(/);
  assert.match(sessionFactory, /PlaybackStep/);
});

test('android home actions expose playback, hints, and filter updates without text search or bpm dialog actions', () => {
  const action = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeAction.kt');
  const reducer = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt');

  assert.match(action, /UpdateLibraryFilter/);
  assert.match(action, /SelectRoot/);
  assert.match(action, /UpdateTonePreset/);
  assert.match(action, /ToggleLoop/);
  assert.match(action, /StopPlayback/);
  assert.doesNotMatch(action, /UpdateSearchQuery/);
  assert.doesNotMatch(action, /ClearSearchQuery/);
  assert.doesNotMatch(action, /OpenBpmInput/);
  assert.doesNotMatch(action, /CloseBpmInput/);
  assert.doesNotMatch(action, /SubmitBpmInput/);

  assert.match(reducer, /UpdateLibraryFilter/);
  assert.match(reducer, /ToggleLoop/);
  assert.match(reducer, /SyncPlaybackSnapshot/);
  assert.match(reducer, /UpdateDurationMultiplier/);
  assert.match(
    reducer,
    /stateFactory\.create\([\s\S]*selectedRhythmPattern = selectedRhythmPattern,\s*durationMultiplier = durationMultiplier,\s*playbackSnapshot = playbackSnapshot/
  );
  assert.doesNotMatch(reducer, /searchQuery/);
  assert.doesNotMatch(reducer, /isBpmInputVisible/);
});
