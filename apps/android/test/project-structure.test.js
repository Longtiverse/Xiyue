import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';

const requiredFiles = [
  'apps/android/settings.gradle.kts',
  'apps/android/build.gradle.kts',
  'apps/android/gradle.properties',
  'apps/android/gradle/libs.versions.toml',
  'apps/android/app/build.gradle.kts',
  'apps/android/app/proguard-rules.pro',
  'apps/android/app/src/main/AndroidManifest.xml',
  'apps/android/app/src/main/java/com/xiyue/app/MainActivity.kt',
  'apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt',
  'apps/android/app/src/main/java/com/xiyue/app/ui/theme/Theme.kt',
  'apps/android/app/src/main/java/com/xiyue/app/playback/TonePreset.kt',
  'apps/android/app/src/main/res/values/strings.xml',
  'apps/android/app/src/main/res/values/colors.xml',
  'apps/android/app/src/main/res/values/themes.xml',
  'apps/android/app/src/main/res/color/ic_launcher_background.xml',
  'apps/android/app/src/main/res/drawable/ic_launcher_foreground.xml',
  'apps/android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml',
  'apps/android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml',
  'scripts/codex-next-iteration.ps1',
];

test('android project skeleton files exist', () => {
  for (const filePath of requiredFiles) {
    assert.equal(existsSync(filePath), true, `${filePath} should exist`);
  }
});

test('android gradle settings and app config declare the xiyue compose app', () => {
  const settings = readFileSync('apps/android/settings.gradle.kts', 'utf8');
  const appGradle = readFileSync('apps/android/app/build.gradle.kts', 'utf8');
  const manifest = readFileSync('apps/android/app/src/main/AndroidManifest.xml', 'utf8');
  const mainActivity = readFileSync('apps/android/app/src/main/java/com/xiyue/app/MainActivity.kt', 'utf8');

  assert.match(settings, /rootProject\.name\s*=\s*"XiyueAndroid"/);
  assert.match(settings, /include\(":app"\)/);
  assert.match(appGradle, /namespace\s*=\s*"com\.xiyue\.app"/);
  assert.match(appGradle, /buildFeatures\s*\{[\s\S]*compose\s*=\s*true/);
  assert.match(manifest, /package="com\.xiyue\.app"|<manifest/);
  assert.match(manifest, /android:icon="@mipmap\/ic_launcher"/);
  assert.match(manifest, /android:roundIcon="@mipmap\/ic_launcher_round"/);
  assert.match(manifest, /MainActivity/);
  assert.match(mainActivity, /setContent/);
  assert.match(mainActivity, /XiyueApp\(/);
});

test('android app screen includes core practice placeholders', () => {
  const appScreen = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt', 'utf8');
  const stateFactory = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt', 'utf8');
  const strings = readFileSync('apps/android/app/src/main/res/values/strings.xml', 'utf8');

  assert.match(appScreen, /HomeScreen/);
  assert.match(appScreen, /PracticePickerStrip/);
  assert.match(stateFactory, /buildPracticePicker/);
  assert.match(stateFactory, /Keyboard Preview/);
  assert.match(stateFactory, /Pause Practice|Resume Practice|Start Practice/);
  assert.match(strings, /习乐/);
});

test('repo includes codex control launcher for next autonomous iteration', () => {
  const packageJson = readFileSync('package.json', 'utf8');
  const script = readFileSync('scripts/codex-next-iteration.ps1', 'utf8');

  assert.match(packageJson, /codex:next/);
  assert.match(script, /codex\s+exec/);
  assert.match(script, /--cd/);
  assert.match(script, /D:\\Project\\Xiyue/);
  assert.match(script, /Iterations/);
});
