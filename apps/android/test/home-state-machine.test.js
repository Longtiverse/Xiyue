import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const stateFiles = [
  'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeAction.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt',
];

test('android home action and reducer files exist', () => {
  for (const filePath of stateFiles) {
    assert.equal(existsSync(filePath), true, `${filePath} should exist`);
  }
});

test('android home reducer supports library selection and playback control actions', () => {
  const actions = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeAction.kt', 'utf8');
  const reducer = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt', 'utf8');
  const uiState = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt', 'utf8');

  assert.match(actions, /sealed interface HomeAction/);
  assert.match(actions, /data class SelectLibraryItem/);
  assert.match(actions, /data class ToggleFavoriteLibraryItem/);
  assert.match(actions, /data object ToggleLibraryOverlay/);
  assert.match(actions, /data object TogglePlaybackDisplayMode/);
  assert.match(actions, /data class UpdateBpm/);
  assert.match(actions, /data object TogglePlayback/);
  assert.match(actions, /data object StopPlayback/);
  assert.match(actions, /data class UpdateTonePreset/);
  assert.match(reducer, /class HomeReducer/);
  assert.match(reducer, /fun reduce\(/);
  assert.match(reducer, /selectedLibraryItemId/);
  assert.match(reducer, /favoriteLibraryItemIds/);
  assert.match(reducer, /isLibraryOverlayVisible/);
  assert.match(reducer, /displayMode/);
  assert.match(reducer, /StopPlayback/);
  assert.match(reducer, /SelectLibraryItem[\s\S]*isPaused = state\.isPaused/);
  assert.match(reducer, /SelectLibraryItem[\s\S]*isLibraryOverlayVisible = false/);
  assert.match(reducer, /SelectRoot[\s\S]*isPaused = state\.isPaused/);
  assert.match(reducer, /UpdatePlaybackMode[\s\S]*isPaused = state\.isPaused/);
  assert.match(reducer, /UpdateBpm[\s\S]*isPaused = state\.isPaused/);
  assert.match(reducer, /ToggleLoop[\s\S]*isPaused = state\.isPaused/);
  assert.match(reducer, /UpdateTonePreset[\s\S]*isPaused = state\.isPaused/);
  assert.match(uiState, /PracticePickerUiState/);
  assert.doesNotMatch(actions, /ToggleSelectorSheet/);
  assert.doesNotMatch(reducer, /isSelectorSheetVisible/);
  assert.doesNotMatch(reducer, /bpmLabel/);
});
