import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

test('android build script keeps latest apk versioned and records it in metadata', () => {
  const script = readFileSync('scripts/build-android.ps1', 'utf8');

  assert.match(script, /xiyue-android-v\$versionName-latest-debug\.apk/);
  assert.match(script, /latestApk/);
  assert.match(script, /archiveApk/);
});
