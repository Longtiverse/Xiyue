import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

test('android build script keeps latest apk versioned and records it in metadata', () => {
  const script = readFileSync('scripts/build-android.ps1', 'utf8');

  assert.match(script, /xiyue-android-v\$versionName-\$timestamp-release\.apk/);
  assert.match(script, /latestApk/);
  assert.match(script, /archiveApk/);
  assert.match(script, /GRADLE_USER_HOME/);
  assert.match(script, /ANDROID_USER_HOME|ANDROID_SDK_HOME/);
  assert.match(script, /ANDROID_HOME|Resolve-AndroidSdkPath/);
  assert.match(script, /local\.properties|sdk\.dir/);
  assert.match(script, /native|tmp|TEMP/);
  assert.match(script, /\.tmp\\gradle-dist|workspaceGradleDist|workspaceGradlePath/);
  assert.match(script, /Seed-WorkspaceGradleHome|seed workspace gradle home/i);
  assert.match(script, /\.lock/);
  assert.match(script, /caches/);
  assert.match(script, /wrapper/);
  assert.match(script, /output-metadata\.json/);
  assert.match(script, /Get-ChildItem[\s\S]*\.apk/);
});
