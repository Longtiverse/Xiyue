import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

test('android includes practice domain placeholders for future shared-core integration', () => {
  assert.equal(
    existsSync('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeModels.kt'),
    true
  );
  assert.equal(
    existsSync('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeLibraryRepository.kt'),
    true
  );
  assert.equal(
    existsSync('apps/android/app/src/main/java/com/xiyue/app/domain/PracticeSessionFactory.kt'),
    true
  );

  const models = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/domain/PracticeModels.kt',
    'utf8'
  );
  const repository = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/domain/PracticeLibraryRepository.kt',
    'utf8'
  );
  const sessionFactory = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/domain/PracticeSessionFactory.kt',
    'utf8'
  );

  assert.match(models, /enum class PracticeKind/);
  assert.match(models, /data class PracticeLibraryItem/);
  assert.match(models, /enum class PlaybackMode/);
  assert.match(models, /SCALE_ASCENDING\("Ascending"\)/);
  assert.match(models, /SCALE_ASCENDING_DESCENDING\("Asc \/ Desc"\)/);
  assert.match(models, /CHORD_BLOCK\("Block"\)/);
  assert.match(models, /CHORD_ARPEGGIO_UP\("Up"\)/);
  assert.match(models, /CHORD_ARPEGGIO_DOWN\("Down"\)/);
  assert.match(models, /CHORD_ARPEGGIO_UP_DOWN\("Up & Down"\)/);
  assert.doesNotMatch(models, /涓|榻|鐞|路/);
  assert.match(repository, /interface PracticeLibraryRepository/);
  assert.match(repository, /fun getLibraryItems/);
  assert.match(sessionFactory, /append\(" BPM"\)[\s\S]*append\(" · "\)/);
  assert.match(sessionFactory, /Loop/);
  assert.doesNotMatch(sessionFactory, /路/);
});

test('android practice session factory preserves duration multiplier across rhythm shaping and combo plans', () => {
  const sessionFactory = readFileSync(
    'apps/android/app/src/main/java/com/xiyue/app/domain/PracticeSessionFactory.kt',
    'utf8'
  );

  assert.match(
    sessionFactory,
    /createComboNotePlan\([\s\S]*durationMultiplier: Float[\s\S]*PlaybackStep\([\s\S]*durationMs = \(beatDurationMs\(bpm\) \* durationMultiplier\)\.toLong\(\)/
  );
  assert.match(
    sessionFactory,
    /createComboChordPlan\([\s\S]*durationMultiplier: Float[\s\S]*selection\.durationMultiplier/
  );
  assert.match(
    sessionFactory,
    /createComboNotePlan\([\s\S]*durationMultiplier = selection\.durationMultiplier/
  );
  assert.match(
    sessionFactory,
    /createComboChordPlan\([\s\S]*durationMultiplier = selection\.durationMultiplier/
  );
  assert.match(sessionFactory, /val baseDuration = step\.durationMs\.toFloat\(\)/);
  assert.match(sessionFactory, /val newDuration = \(baseDuration \* multiplier\)\.toLong\(\)\.coerceAtLeast\(50\)/);
  assert.doesNotMatch(sessionFactory, /val baseBeat = beatDurationMs\(bpm\)\.toFloat\(\)/);
});
