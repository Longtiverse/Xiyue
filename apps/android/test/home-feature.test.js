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

test('android repository provides sample scale and chord library data', () => {
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
  assert.match(repository, /defaultItems\(\): List<PracticeLibraryItem> = listOf\(/);
  assert.match(repository, /PracticeKind\.SCALE/);
  assert.match(repository, /PracticeKind\.CHORD/);
  assert.match(repository, /"Major"/);
  assert.match(repository, /"Natural Minor"/);
  assert.match(repository, /override fun searchLibraryItems/);
  assert.match(repository, /"Major Triad"/);
  assert.match(repository, /"Minor Triad"/);
  assert.match(repository, /aliases/);
  assert.match(
    repository,
    /parseItems\(root\.getJSONArray\("scales"\), PracticeKind\.SCALE, "scale"\)/
  );
  assert.match(
    repository,
    /parseItems\(root\.getJSONArray\("chords"\), PracticeKind\.CHORD, "chord"\)/
  );
  assert.match(models, /enum class PitchClass/);
  assert.match(models, /enum class PlaybackMode/);
  assert.match(models, /val intervals: List<Int>/);
  assert.match(models, /val aliases: List<String>/);
});

test('android home state maps practice library into quick selection, playback controls, and keyboard preview', () => {
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
  assert.match(uiState, /data class LibraryGroupUiState/);
  assert.match(uiState, /data class PracticePickerUiState/);
  assert.match(uiState, /data class PracticeShortcutUiItem/);
  assert.match(uiState, /data class RootNoteUiItem/);
  assert.match(uiState, /data class PlaybackControlUiState/);
  assert.match(uiState, /data class PlaybackModeUiItem/);
  assert.match(uiState, /data class PlaybackDisplayUiState/);
  assert.match(uiState, /data class TempoPresetUiItem/);
  assert.match(uiState, /data class TonePresetUiItem/);
  assert.match(uiState, /TonePreset/);
  assert.match(uiState, /data class KeyboardPreviewUiState/);
  assert.match(uiState, /data class KeyboardKeyUiState/);
  assert.match(uiState, /val isPaused: Boolean/);
  assert.match(uiState, /val isLibraryOverlayVisible: Boolean/);
  assert.match(uiState, /val practicePicker: PracticePickerUiState/);
  assert.match(uiState, /val playbackDisplay: PlaybackDisplayUiState/);
  assert.match(uiState, /val playbackControl: PlaybackControlUiState/);
  assert.match(uiState, /val keyboardPreview: KeyboardPreviewUiState/);
  assert.match(factory, /class HomeStateFactory/);
  assert.match(factory, /fun create\(/);
  assert.match(factory, /selectedRoot/);
  assert.match(factory, /loopEnabled/);
  assert.match(factory, /background playback/i);
  assert.match(factory, /effectivePaused|isPaused/);
  assert.match(factory, /groupedLibraryItems|buildLibraryGroups/);
  assert.match(factory, /buildPracticePicker/);
  assert.match(factory, /buildPlaybackDisplay/);
  assert.match(factory, /buildPlaybackControl/);
  assert.match(factory, /buildKeyboardPreview/);
  assert.match(factory, /favoriteLibraryItems/);
  assert.match(factory, /recentLibraryItems/);
  assert.match(factory, /TonePreset/);
  assert.match(factory, /TonePreset\.WARM_PRACTICE/);
  assert.match(factory, /selectedTonePreset/);
  assert.match(factory, /selectedTonePreset|effectiveTonePreset/);
  assert.match(factory, /buildPlaybackDisplay|buildPlaybackControl/);
  assert.doesNotMatch(factory, /路/);
  assert.match(factory, /stepCount|currentActiveNote|sequenceNotes/);
  assert.match(screen, /@Composable/);
  assert.match(screen, /HomeScreen/);
  assert.match(screen, /Column/);
  assert.doesNotMatch(screen, /LazyColumn/);
  assert.match(screen, /PlaybackControlsSection\(/);
  assert.match(screen, /PlaybackDisplaySection\(/);
  assert.match(screen, /CompactLibrarySelector/);
  assert.doesNotMatch(screen, /Dialog\(/);
  assert.match(screen, /SelectRoot/);
  assert.match(screen, /previousRoot|nextRoot|selectedRoot/);
  assert.doesNotMatch(screen, /rootNotes\.forEach/);
  assert.doesNotMatch(screen, /practicePicker\.summaryLabel/);
  assert.match(screen, /UpdateLibraryFilter/);
  assert.match(screen, /weight\(/);
  assert.match(sessionFactory, /class PracticeSessionFactory/);
  assert.match(sessionFactory, /fun createPlan\(/);
  assert.match(sessionFactory, /PlaybackStep/);
  assert.doesNotMatch(uiState, /PlaybackQuickSelectUiItem/);
  assert.doesNotMatch(uiState, /val sections:/);
  assert.doesNotMatch(factory, /sections\s*=/);
  assert.doesNotMatch(factory, /quickSelectItems\s*=/);
});

test('android home actions expose tone preset updates', () => {
  const action = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeAction.kt');
  const reducer = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt');

  assert.match(action, /TonePreset/);
  assert.match(action, /UpdateTonePreset/);
  assert.match(reducer, /TonePreset/);
  assert.match(reducer, /UpdateTonePreset/);
});
