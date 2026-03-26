import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const playbackFiles = [
  'apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshot.kt',
  'apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt',
  'apps/android/app/src/main/java/com/xiyue/app/playback/ToneSynth.kt',
];

test('android playback service files exist', () => {
  for (const filePath of playbackFiles) {
    assert.equal(existsSync(filePath), true, `${filePath} should exist`);
  }
});

test('android playback service supports foreground background playback with looped practice plans', () => {
  const manifest = readFileSync('apps/android/app/src/main/AndroidManifest.xml', 'utf8');
  const service = readFileSync('apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt', 'utf8');
  const snapshot = readFileSync('apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshot.kt', 'utf8');
  const synth = readFileSync('apps/android/app/src/main/java/com/xiyue/app/playback/ToneSynth.kt', 'utf8');

  assert.match(manifest, /android\.permission\.FOREGROUND_SERVICE/);
  assert.match(manifest, /android\.permission\.FOREGROUND_SERVICE_MEDIA_PLAYBACK/);
  assert.match(manifest, /PracticePlaybackService/);
  assert.match(manifest, /foregroundServiceType="mediaPlayback"/);
  assert.match(service, /class PracticePlaybackService/);
  assert.match(service, /MutableStateFlow/);
  assert.match(service, /startForeground/);
  assert.match(service, /ACTION_PLAY/);
  assert.match(service, /ACTION_STOP/);
  assert.match(service, /loopEnabled/);
  assert.match(service, /AudioTrack/);
  assert.match(snapshot, /data class PlaybackSnapshot/);
  assert.match(snapshot, /activePitchClasses/);
  assert.match(synth, /toneColor|harmonic|overtone|partials/i);
  assert.match(synth, /attack|decay|sustain|release/i);
  assert.match(synth, /CHANNEL_OUT_STEREO|stereo/i);
  assert.match(synth, /NOISE|noiseFloor|noise/i);
  assert.match(synth, /attackFrames|decayFrames|releaseFrames/);
  assert.match(synth, /RESONANCE|lowPass|filter/i);
  assert.match(synth, /damping|damp|balance/i);
  assert.match(synth, /noteCount|noteIndex|notes/i);
  assert.match(synth, /micro|stagger|onset/i);
  assert.match(synth, /releaseCurve|releaseTail|releaseShape/i);
  assert.match(synth, /bass|subharmonic|lowReinforce/i);
});
