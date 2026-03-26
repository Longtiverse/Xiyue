import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

test('android includes practice domain placeholders for future shared-core integration', () => {
  assert.equal(existsSync('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeModels.kt'), true);
  assert.equal(existsSync('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeLibraryRepository.kt'), true);

  const models = readFileSync('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeModels.kt', 'utf8');
  const repository = readFileSync('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeLibraryRepository.kt', 'utf8');

  assert.match(models, /enum class PracticeKind/);
  assert.match(models, /data class PracticeLibraryItem/);
  assert.match(models, /enum class PlaybackMode/);
  assert.match(repository, /interface PracticeLibraryRepository/);
  assert.match(repository, /fun getLibraryItems/);
});
