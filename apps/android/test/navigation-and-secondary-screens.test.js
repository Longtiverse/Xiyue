import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const read = (filePath) => readFileSync(filePath, 'utf8');

test('android navigation exposes practice, combo, favorites, and settings tabs', () => {
  const navigation = read(
    'apps/android/app/src/main/java/com/xiyue/app/navigation/BottomNavigation.kt'
  );
  const app = read('apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt');

  assert.match(navigation, /enum class BottomNavItem/);
  assert.match(navigation, /PRACTICE/);
  assert.match(navigation, /COMBO/);
  assert.match(navigation, /FAVORITES/);
  assert.match(navigation, /SETTINGS/);
  assert.match(navigation, /NavigationBar/);
  assert.match(app, /BottomNavItem\.COMBO/);
});

test('android settings screen follows spec with theme selector, hints toggle, and version only', () => {
  const settings = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/settings/SettingsScreen.kt'
  );
  const repository = read(
    'apps/android/app/src/main/java/com/xiyue/app/persistence/SettingsRepository.kt'
  );

  assert.match(settings, /ThemeModeSelector/);
  assert.match(settings, /showHints/);
  assert.match(settings, /Switch/);
  assert.match(settings, /版本 0\.1\.0|Version 0\.1\.0/);
  assert.doesNotMatch(settings, /Slider/);
  assert.doesNotMatch(settings, /defaultBpm/);

  assert.match(repository, /getShowHints/);
  assert.match(repository, /setShowHints/);
});

test('android favorites screen shows count and a guided empty state', () => {
  const favorites = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/favorites/FavoritesScreen.kt'
  );

  assert.match(favorites, /favorites\.size/);
  assert.match(favorites, /暂无收藏|No favorites yet/);
  assert.match(favorites, /项收藏|favorites/);
});

test('android custom combo screen exists with mode tabs, note grid, preview, and chord builder', () => {
  const comboPath = 'apps/android/app/src/main/java/com/xiyue/app/features/combo/ComboScreen.kt';
  assert.equal(existsSync(comboPath), true, `${comboPath} should exist`);

  const combo = read(comboPath);
  assert.match(combo, /ComboScreen/);
  assert.match(combo, /音符选择|Note Selection/);
  assert.match(combo, /和弦进行|Chord Progression/);
  assert.match(combo, /LazyVerticalGrid|FlowRow/);
  assert.match(combo, /Selected Preview|已选内容预览/);
  assert.match(combo, /Chord Constructor|和弦构造器/);
  assert.match(combo, /ii.?V.?I|I.?vi.?IV.?V/);
});

test('android theme palette includes accent and gold tokens from the spec', () => {
  const colors = read('apps/android/app/src/main/java/com/xiyue/app/ui/theme/Color.kt');
  const theme = read('apps/android/app/src/main/java/com/xiyue/app/ui/theme/Theme.kt');

  assert.match(colors, /XiyueAccent/);
  assert.match(colors, /XiyueAccentStrong/);
  assert.match(colors, /XiyueGold/);
  assert.match(colors, /XiyueGoldStrong/);
  assert.match(colors, /XiyueBackground/);
  assert.match(theme, /darkColorScheme/);
  assert.match(theme, /primary = XiyueAccent/);
  assert.match(theme, /tertiary = XiyueGold|secondary = XiyueGold/);
});
