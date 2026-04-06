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
  assert.match(uiState, /data class LibraryGroupUiState/);
  assert.match(uiState, /data class PracticePickerUiState/);
  assert.match(uiState, /data class PracticeShortcutUiItem/);
  assert.match(uiState, /data class RootNoteUiItem/);
  assert.match(uiState, /data class PlaybackControlUiState/);
  assert.match(uiState, /data class PlaybackModeUiItem/);
  assert.match(uiState, /data class TempoPresetUiItem/);
  assert.match(uiState, /data class TonePresetUiItem/);
  assert.match(uiState, /TonePreset/);
  assert.match(uiState, /data class KeyboardKeyUiState/);
  assert.match(uiState, /val isPaused: Boolean/);
  assert.match(uiState, /val isLibraryOverlayVisible: Boolean/);
  assert.match(uiState, /val practicePicker: PracticePickerUiState/);
  assert.match(factory, /class HomeStateFactory/);
  assert.match(factory, /fun create\(/);
  assert.match(factory, /selectedRoot/);
  assert.match(factory, /loopEnabled/);
  assert.match(factory, /background playback/i);
  assert.match(factory, /effectivePaused|isPaused/);
  assert.match(factory, /groupedLibraryItems|buildLibraryGroups/);
  assert.match(factory, /buildPracticePicker|visibleShortcuts/);
  assert.match(factory, /favoriteLibraryItems/);
  assert.match(factory, /recentLibraryItems/);
  assert.match(factory, /tempoPresets/);
  assert.match(factory, /toneOptions/);
  assert.match(factory, /TonePreset/);
  assert.match(factory, /TonePreset\.WARM_PRACTICE/);
  assert.match(factory, /selectedTonePreset/);
  assert.match(factory, /TonePreset\.entries|preset\.label|preset\.shortLabel/);
  assert.match(factory, /toneLabel/);
  assert.doesNotMatch(factory, /路/);
  assert.match(factory, /Pause Practice|Resume Practice|Start Practice/);
  assert.match(factory, /showStopButton|stopButtonLabel/);
  assert.match(factory, /stepIndex|stepCount|currentNoteLabel/);
  assert.match(screen, /@Composable/);
  assert.match(screen, /HomeScreen/);
  assert.match(screen, /Column/);
  assert.doesNotMatch(screen, /LazyColumn/);
  assert.match(screen, /PlaybackControlsSection\(/);
  assert.match(screen, /PracticePickerStrip/);
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
  const action = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeAction.kt', 'utf8');
  const reducer = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt', 'utf8');

  assert.match(action, /TonePreset/);
  assert.match(action, /UpdateTonePreset/);
  assert.match(reducer, /TonePreset/);
  assert.match(reducer, /UpdateTonePreset/);
});
