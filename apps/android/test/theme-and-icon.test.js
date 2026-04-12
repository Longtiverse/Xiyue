import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

test('android theme palette aligns with the design spec accent and gold tokens', () => {
  const colors = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/ui/theme/Color.kt',
    'utf8'
  );
  const theme = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/ui/theme/Theme.kt',
    'utf8'
  );
  const app = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt',
    'utf8'
  );

  assert.match(colors, /XiyueAccent/);
  assert.match(colors, /XiyueAccentStrong/);
  assert.match(colors, /XiyueGold/);
  assert.match(colors, /XiyueGoldStrong/);
  assert.match(colors, /XiyueBackground/);
  assert.match(theme, /darkColorScheme/);
  assert.match(theme, /primary = XiyueAccent/);
  assert.match(theme, /tertiary = XiyueGold|secondary = XiyueGold/);
  assert.match(theme, /dynamicColor: Boolean = false/);
  assert.match(theme, /XiyueDarkColorScheme/);
  assert.doesNotMatch(theme, /dynamicLightColorScheme/);
  assert.match(app, /XiyueTheme\(darkTheme = isDarkTheme, dynamicColor = false\)/);
});

test('android adaptive icon resources define foreground and background layers', () => {
  const foreground = readFileSync(
    'apps/android/app/src/main/res/drawable/ic_launcher_foreground.xml',
    'utf8'
  );
  const background = readFileSync(
    'apps/android/app/src/main/res/color/ic_launcher_background.xml',
    'utf8'
  );
  const icon = readFileSync(
    'apps/android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml',
    'utf8'
  );

  assert.match(foreground, /vector|shape|group/);
  assert.match(foreground, /path|circle/);
  assert.match(background, /color/);
  assert.match(icon, /adaptive-icon/);
  assert.match(icon, /foreground/);
  assert.match(icon, /background/);
});
