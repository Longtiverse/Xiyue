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
  const app = readFileSync('apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt', 'utf8');
  const strings = readFileSync('apps/android/app/src/main/res/values/strings.xml', 'utf8');

  assert.match(manifest, /android\.permission\.FOREGROUND_SERVICE/);
  assert.match(manifest, /android\.permission\.FOREGROUND_SERVICE_MEDIA_PLAYBACK/);
  assert.match(manifest, /PracticePlaybackService/);
  assert.match(manifest, /foregroundServiceType="mediaPlayback"/);
  assert.match(service, /class PracticePlaybackService/);
  assert.match(service, /MutableStateFlow/);
  assert.match(service, /startForeground/);
  assert.match(service, /ACTION_PLAY/);
  assert.match(service, /ACTION_PAUSE/);
  assert.match(service, /ACTION_RESUME/);
  assert.match(service, /ACTION_STOP/);
  assert.match(service, /ACTION_PREPARE_PAUSED/);
  assert.match(service, /pausePlayback/);
  assert.match(service, /resumePlayback/);
  assert.match(service, /preparePausedPlayback|replacePausedPlayback|updatePausedPlayback/);
  assert.match(service, /pausedRequest|resumeRequest|resumableRequest/);
  assert.match(service, /pendingSwitchRequest/);
  assert.match(service, /queuePlaybackSwitch|queueSwitchRequest|applyPendingSwitch/);
  assert.match(service, /request == currentRequest|request == pendingSwitchRequest|duplicate request/i);
  assert.match(service, /queuedItemId|queuedTitle/);
  assert.match(service, /tonePreset/);
  assert.match(service, /EXTRA_TONE_PRESET/);
  assert.match(service, /copy\([\s\S]*queuedItemId/);
  assert.match(service, /ACTION_PLAY[\s\S]*currentRequest[\s\S]*playbackJob/);
  assert.match(service, /continue@|break@|switchApplied|pendingSwitchRequest\?\.let/);
  assert.match(service, /startStepIndex|resumeStepIndex|resumeFromStep/);
  assert.match(service, /currentSnapshot\.stepIndex|stepIndex = currentSnapshot\.stepIndex/);
  assert.match(service, /drop\(|coerceAtLeast|coerceIn/);
  assert.match(service, /isPaused = true/);
  assert.match(service, /isPaused = false/);
  assert.doesNotMatch(service, /private fun queuePlaybackSwitch[\s\S]{0,240}resumableRequest = request/);
  assert.doesNotMatch(service, /pendingSwitchRequest \?: currentRequest \?: resumableRequest/);
  assert.doesNotMatch(service, /do \{[\s\S]*if \(switchApplied\) continue[\s\S]*while \(activeRequest\.loopEnabled\)/);
  assert.match(service, /createPauseIntent|createResumeIntent|createTransportIntent/);
  assert.match(service, /Pause|Resume/);
  assert.match(service, /setSubText/);
  assert.match(service, /BigTextStyle|setStyle/);
  assert.match(service, /setSilent\(|setShowWhen\(false\)|PRIORITY_LOW|setPriority/);
  assert.match(service, /notificationDetailText|notificationStatusLabel|notificationToneLabel/i);
  assert.match(service, /CATEGORY_TRANSPORT/);
  assert.match(service, /activeNoteLabels/);
  assert.match(service, /stepCount|stepIndex/);
  assert.match(service, /loopEnabled/);
  assert.match(service, /AudioTrack/);
  assert.match(service, /AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK/);
  assert.match(service, /AUDIOFOCUS_LOSS_TRANSIENT/);
  assert.doesNotMatch(service, /AUDIOFOCUS_LOSS_TRANSIENT,\s*AudioManager\.AUDIOFOCUS_LOSS,\s*-> stopPlayback/s);
  assert.match(snapshot, /data class PlaybackSnapshot/);
  assert.match(snapshot, /val isPaused: Boolean = false/);
  assert.match(snapshot, /val queuedItemId: String\? = null/);
  assert.match(snapshot, /val queuedTitle: String\? = null/);
  assert.match(snapshot, /activePitchClasses/);
  assert.match(app, /PracticePlaybackService\.play/);
  assert.match(app, /PracticePlaybackService\.(preparePaused|replacePausedPlayback)/);
  assert.match(app, /PracticePlaybackService\.(pause|resume)/);
  assert.match(app, /PracticePlaybackService\.stop/);
  assert.doesNotMatch(app, /HomeAction\.TogglePlayback[\s\S]{0,600}state = reducer\.reduce\(state, action\)/);
  assert.doesNotMatch(app, /HomeAction\.StopPlayback[\s\S]{0,400}state = reducer\.reduce\(state, action\)/);
  assert.match(app, /SelectLibraryItem|SelectRoot|UpdatePlaybackMode|UpdateBpm|ToggleLoop/);
  assert.match(app, /refreshPlayback|restartPlayback|shouldRefreshPlayback/i);
  assert.match(app, /shouldPreparePausedPlayback|preparePausedPlayback/i);
  assert.match(app, /previousState\.isPaused/);
  assert.match(strings, /playback_action_pause/);
  assert.match(strings, /playback_action_resume/);
  assert.match(strings, /playback_action_stop/);
  assert.match(strings, /playback_status_playing/);
  assert.match(strings, /playback_status_paused/);
  assert.match(synth, /toneColor|harmonic|overtone|partials/i);
  assert.match(synth, /TonePreset/);
  assert.match(synth, /Warm Practice|Soft Piano|Clear Wood|warmPractice|softPiano|clearWood/);
  assert.match(synth, /releaseTail|tailFrames|tailDuration|linger|overlap/i);
  assert.match(synth, /hammer|felt|transient|woodPulse|bodyResonance/i);
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
  assert.match(synth, /chorus|warmth|bloom|glow/i);
  assert.match(synth, /presenceSoften|airTrim|softAttackBlend|mellowRollOff|warmBlur/i);
});

test('android playback stack threads tone presets through request snapshot service and synth without forced stop', () => {
  const service = readFileSync('apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt', 'utf8');
  const snapshot = readFileSync('apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshot.kt', 'utf8');
  const synth = readFileSync('apps/android/app/src/main/java/com/xiyue/app/playback/ToneSynth.kt', 'utf8');
  const tonePreset = readFileSync('apps/android/app/src/main/java/com/xiyue/app/playback/TonePreset.kt', 'utf8');

  assert.match(tonePreset, /enum class TonePreset/);
  assert.match(tonePreset, /WARM_PRACTICE/);
  assert.match(tonePreset, /SOFT_PIANO/);
  assert.match(tonePreset, /CLEAR_WOOD/);
  assert.match(snapshot, /data class PlaybackRequest[\s\S]*val tonePreset: TonePreset/);
  assert.match(snapshot, /data class PlaybackSnapshot[\s\S]*val tonePreset: TonePreset/);
  assert.match(service, /EXTRA_TONE_PRESET = "tone_preset"/);
  assert.match(service, /putExtra\(EXTRA_TONE_PRESET, request\.tonePreset\.name\)/);
  assert.match(service, /val tonePreset = intent\.getStringExtra\(EXTRA_TONE_PRESET\)/);
  assert.match(service, /TonePreset\.valueOf\(it\)/);
  assert.match(service, /createSnapshot\([\s\S]*tonePreset = request\.tonePreset/);
  assert.match(service, /if \(currentRequest != null && playbackJob != null\) \{[\s\S]*queuePlaybackSwitch\(request\)/);
  assert.match(service, /pendingSwitchRequest\?\.let[\s\S]*activeRequest = queuedRequest/);
  assert.doesNotMatch(service, /private fun queuePlaybackSwitch[\s\S]{0,240}stopPlayback\(/);
  assert.match(synth, /playStep\([^)]*tonePreset: TonePreset/);
  assert.match(synth, /createSamples\([\s\S]*tonePreset: TonePreset/);
  assert.doesNotMatch(synth, /delay\(step\.durationMs\)\s*stop\(\)/);
  assert.match(synth, /when\s*\(\s*tonePreset\s*\)|profileFor|presetProfile/i);
  assert.match(synth, /WARM_PRACTICE[\s\S]*(warmth|bloom|chorus|bass)/i);
  assert.match(synth, /WARM_PRACTICE[\s\S]*(hammer|felt|transient|body)/i);
  assert.match(synth, /SOFT_PIANO[\s\S]*(soft|felt|attack|noise|body)/i);
  assert.match(synth, /CLEAR_WOOD[\s\S]*(wood|pluck|clarity|transient|overtone|pulse)/i);
});
