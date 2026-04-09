import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';

const read = (filePath) => readFileSync(filePath, 'utf8');

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
];

test('android project skeleton files exist', () => {
  for (const filePath of requiredFiles) {
    assert.equal(existsSync(filePath), true, `${filePath} should exist`);
  }
});

test('android gradle settings and app config declare the xiyue compose app', () => {
  const settings = read('apps/android/settings.gradle.kts');
  const appGradle = read('apps/android/app/build.gradle.kts');
  const manifest = read('apps/android/app/src/main/AndroidManifest.xml');
  const mainActivity = read('apps/android/app/src/main/java/com/xiyue/app/MainActivity.kt');

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
  const appScreen = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt'
  );
  const stateFactory = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt'
  );
  const strings = read('apps/android/app/src/main/res/values/strings.xml');

  assert.match(appScreen, /HomeScreen/);
  assert.match(appScreen, /CompactLibrarySelector/);
  assert.match(stateFactory, /buildPracticePicker/);
  assert.match(appScreen, /PlaybackDisplaySection/);
  assert.match(strings, /习乐/);
});
