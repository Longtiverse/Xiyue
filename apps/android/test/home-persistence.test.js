import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const persistenceFiles = [
  'apps/android/app/src/main/java/com/xiyue/app/features/home/HomePreferencesRepository.kt',
  'apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt',
];

test('android home persistence files exist', () => {
  for (const filePath of persistenceFiles) {
    assert.equal(existsSync(filePath), true, `${filePath} should exist`);
  }
});

test('android home persists last selection, display settings, and recent items', () => {
  const repository = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/HomePreferencesRepository.kt',
    'utf8'
  );
  const app = readFileSync('apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt', 'utf8');
  const factory = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt',
    'utf8'
  );
  const reducer = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt',
    'utf8'
  );

  assert.match(repository, /class HomePreferencesRepository/);
  assert.match(repository, /data class HomePreferencesState/);
  assert.match(repository, /fun load\(\): HomePreferencesState/);
  assert.match(repository, /fun save\(state: HomePreferencesState\)/);
  assert.match(repository, /selectedLibraryItemId/);
  assert.match(repository, /favoriteLibraryItemIds/);
  assert.match(repository, /selectedRoot/);
  assert.match(repository, /selectedPlaybackMode/);
  assert.match(repository, /selectedTonePreset/);
  assert.match(repository, /TonePreset\.WARM_PRACTICE/);
  assert.match(repository, /displayMode/);
  assert.match(repository, /recentLibraryItemIds/);
  assert.match(repository, /KEY_SELECTED_TONE_PRESET/);
  assert.match(app, /HomePreferencesRepository/);
  assert.match(app, /preferencesRepository\.load\(\)/);
  assert.match(app, /preferencesRepository\.save\(/);
  assert.match(app, /selectedTonePreset = state\.selectedTonePreset/);
  assert.match(app, /favoriteLibraryItemIds = state\.favoriteLibraryItems\.map \{ it\.id \}/);
  assert.match(factory, /favoriteLibraryItemIds/);
  assert.match(factory, /selectedTonePreset/);
  assert.match(factory, /TonePreset/);
  assert.match(factory, /favoriteLibraryItems/);
  assert.match(factory, /recentLibraryItemIds/);
  assert.match(reducer, /UpdateTonePreset/);
  assert.match(reducer, /TonePreset/);
  assert.match(reducer, /recentLibraryItems\.map \{ it\.id \}/);
  assert.match(reducer, /favoriteLibraryItems\.map \{ it\.id \}/);
});
