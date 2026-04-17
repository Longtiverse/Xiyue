import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const read = (filePath) => readFileSync(filePath, 'utf8');

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
  const manifest = read('apps/android/app/src/main/AndroidManifest.xml');
  const service = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt'
  );
  const snapshot = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshot.kt'
  );
  const synth = read('apps/android/app/src/main/java/com/xiyue/app/playback/ToneSynth.kt');
  const app = read('apps/android/app/src/main/java/com/xiyue/app/ui/XiyueApp.kt');
  const strings = read('apps/android/app/src/main/res/values/strings.xml');
  const notifications = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackNotificationManager.kt'
  );
  const audioFocus = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackAudioFocusManager.kt'
  );

  assert.match(manifest, /android\.permission\.FOREGROUND_SERVICE/);
  assert.match(manifest, /android\.permission\.FOREGROUND_SERVICE_MEDIA_PLAYBACK/);
  assert.match(manifest, /PracticePlaybackService/);
  assert.match(manifest, /foregroundServiceType="mediaPlayback"/);
  assert.match(service, /class PracticePlaybackService/);
  assert.match(service, /MutableStateFlow/);
  assert.match(service, /startForeground/);
  assert.match(service, /ACTION_PLAY/);
  assert.match(service, /ACTION_PREPARE_PAUSED/);
  assert.match(service, /PlaybackNotificationManager\.ACTION_PAUSE/);
  assert.match(service, /PlaybackNotificationManager\.ACTION_RESUME/);
  assert.match(service, /PlaybackNotificationManager\.ACTION_STOP/);
  assert.match(service, /pausePlayback/);
  assert.match(service, /resumePlayback/);
  assert.match(service, /preparePausedPlayback/);
  assert.match(service, /resumableRequest/);
  assert.match(service, /pendingSwitchRequest/);
  assert.match(service, /queuePlaybackSwitch/);
  assert.match(service, /request == currentRequest|request == pendingSwitchRequest/);
  assert.match(service, /queuedItemId|queuedTitle/);
  assert.match(service, /tonePreset/);
  assert.match(service, /EXTRA_TONE_PRESET/);
  assert.match(service, /copy\([\s\S]*queuedItemId/);
  assert.match(service, /ACTION_PLAY[\s\S]*currentRequest[\s\S]*playbackJob/);
  assert.match(service, /switchRequestProvider\s*=\s*\{\s*pendingSwitchRequest\s*\}/);
  assert.match(service, /startStepIndex|resumeStepIndex/);
  assert.match(service, /currentSnapshot\.stepIndex|stepIndex = currentSnapshot\.stepIndex/);
  assert.match(service, /coerceAtLeast|coerceIn/);
  assert.doesNotMatch(
    service,
    /private fun queuePlaybackSwitch[\s\S]{0,240}resumableRequest = request/
  );
  assert.doesNotMatch(service, /pendingSwitchRequest \?: currentRequest \?: resumableRequest/);
  assert.match(notifications, /createPauseIntent|createResumeIntent|createTransportIntent/);
  assert.match(notifications, /Pause|Resume/);
  assert.match(notifications, /setSubText/);
  assert.match(notifications, /BigTextStyle|setStyle/);
  assert.match(notifications, /setSilent\(|setShowWhen\(false\)|PRIORITY_LOW|setPriority/);
  assert.match(
    notifications,
    /notificationDetailText|notificationStatusLabel|notificationToneLabel/i
  );
  assert.match(notifications, /CATEGORY_TRANSPORT/);
  assert.match(notifications, /activeNoteLabels/);
  assert.match(notifications, /stepCount|stepIndex/);
  assert.match(service, /loopEnabled/);
  assert.match(synth, /AudioTrack/);
  assert.match(audioFocus, /AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK/);
  assert.match(audioFocus, /AUDIOFOCUS_LOSS_TRANSIENT/);
  assert.doesNotMatch(
    audioFocus,
    /AUDIOFOCUS_LOSS_TRANSIENT,\s*AudioManager\.AUDIOFOCUS_LOSS,\s*-> stopPlayback/s
  );
  assert.match(snapshot, /data class PlaybackSnapshot/);
  assert.match(snapshot, /val isPaused: Boolean = false/);
  assert.match(snapshot, /val queuedItemId: String\? = null/);
  assert.match(snapshot, /val queuedTitle: String\? = null/);
  assert.match(snapshot, /activePitchClasses/);
  assert.match(app, /PracticePlaybackService\.play/);
  assert.match(app, /PracticePlaybackService\.preparePaused/);
  assert.match(app, /PracticePlaybackService\.(pause|resume)/);
  assert.match(app, /PracticePlaybackService\.stop/);
  assert.doesNotMatch(
    app,
    /HomeAction\.TogglePlayback[\s\S]{0,600}state = reducer\.reduce\(state, action\)/
  );
  assert.doesNotMatch(
    app,
    /HomeAction\.StopPlayback[\s\S]{0,400}state = reducer\.reduce\(state, action\)/
  );
  assert.match(app, /SelectLibraryItem|SelectRoot|UpdatePlaybackMode|UpdateBpm|ToggleLoop/);
  assert.match(app, /refreshPlayback|shouldRefreshPlayback/i);
  assert.match(app, /shouldPreparePausedPlayback|preparePausedPlayback/i);
  assert.match(app, /previousState\.isPaused/);
  assert.match(strings, /playback_action_pause/);
  assert.match(strings, /playback_action_resume/);
  assert.match(strings, /playback_action_stop/);
  assert.match(strings, /playback_status_playing/);
  assert.match(strings, /playback_status_paused/);
  assert.match(synth, /tonePreset/);
  assert.match(synth, /TonePreset/);
});

test('android playback stack threads tone presets through request snapshot service and synth without forced stop', () => {
  const service = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt'
  );
  const snapshot = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/PlaybackSnapshot.kt'
  );
  const synth = read('apps/android/app/src/main/java/com/xiyue/app/playback/ToneSynth.kt');
  const tonePreset = read('apps/android/app/src/main/java/com/xiyue/app/playback/TonePreset.kt');
  const karplusEngine = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/KarplusStrongEngine.kt'
  );
  const vocalEngine = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/VocalSynthesisEngine.kt'
  );

  assert.match(tonePreset, /enum class TonePreset/);
  assert.match(tonePreset, /PIANO/);
  assert.match(tonePreset, /PAD/);
  assert.match(tonePreset, /PLUCK/);
  assert.match(tonePreset, /VOCAL/);
  assert.match(tonePreset, /enum class PlaybackSoundMode/);
  assert.match(tonePreset, /PITCH/);
  assert.match(tonePreset, /SOLFEGE/);
  assert.doesNotMatch(tonePreset, /WARM_PRACTICE|SOFT_PIANO|CLEAR_WOOD/);
  assert.match(snapshot, /data class PlaybackRequest[\s\S]*val tonePreset: TonePreset/);
  assert.match(snapshot, /data class PlaybackSnapshot[\s\S]*val tonePreset: TonePreset/);
  assert.match(service, /EXTRA_TONE_PRESET = "tone_preset"/);
  assert.match(service, /putExtra\(EXTRA_TONE_PRESET, request\.tonePreset\.name\)/);
  assert.match(service, /val tonePreset = intent\.getStringExtra\(EXTRA_TONE_PRESET\)/);
  assert.match(service, /TonePreset\.valueOf\(it\)/);
  assert.match(service, /createSnapshot = \{ request, plan, isPlaying, isPaused/);
  assert.match(
    service,
    /if \(currentRequest != null && playbackJob != null\) \{[\s\S]*queuePlaybackSwitch\(request\)/
  );
  assert.match(service, /switchRequestProvider\s*=\s*\{\s*pendingSwitchRequest\s*\}/);
  assert.doesNotMatch(service, /private fun queuePlaybackSwitch[\s\S]{0,240}stopPlayback\(/);
  assert.match(synth, /playStep\([^)]*tonePreset: TonePreset/);
  assert.match(synth, /private val karplusEngine = KarplusStrongEngine/);
  assert.match(synth, /private val vocalEngine = VocalSynthesisEngine/);
  assert.match(synth, /val isVocal = soundMode == PlaybackSoundMode\.SOLFEGE/);
  assert.match(synth, /karplusEngine\.synthesizeChord/);
  assert.match(synth, /vocalEngine\.synthesizeVocalChord/);
  assert.match(synth, /noteNameToSolfege/);
  assert.doesNotMatch(synth, /delay\(step\.durationMs\)\s*stop\(\)/);
  assert.match(karplusEngine, /class KarplusStrongEngine/);
  assert.match(karplusEngine, /TonePreset\.PIANO/);
  assert.match(karplusEngine, /TonePreset\.PAD/);
  assert.match(karplusEngine, /TonePreset\.PLUCK/);
  assert.match(karplusEngine, /TonePreset\.VOCAL/);
  assert.match(karplusEngine, /adsrEnvelope/);
  assert.match(vocalEngine, /class VocalSynthesisEngine/);
  assert.match(vocalEngine, /solfegeToVowel/);
  assert.match(vocalEngine, /VowelPresets/);
  assert.match(vocalEngine, /KlattResonator/);
});

test('android playback service preserves duration multiplier through play and paused intents', () => {
  const service = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/PracticePlaybackService.kt'
  );

  assert.match(service, /private const val EXTRA_DURATION_MULTIPLIER = "duration_multiplier"/);
  assert.match(service, /putExtra\(EXTRA_DURATION_MULTIPLIER, request\.durationMultiplier\)/);
  assert.match(service, /val durationMultiplier = intent\.getFloatExtra\(EXTRA_DURATION_MULTIPLIER, 1\.0f\)/);
  assert.match(
    service,
    /PlaybackRequest\([\s\S]*durationMultiplier = durationMultiplier[\s\S]*\)/
  );
});

test('android piano preset exposes soft-practice shaping hooks in the karplus engine', () => {
  const karplusEngine = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/KarplusStrongEngine.kt'
  );

  assert.match(karplusEngine, /getHfDampCoeff/);
  assert.match(karplusEngine, /getExciterNoiseMix/);
  assert.match(karplusEngine, /getExciterSawMix/);
  assert.match(karplusEngine, /getBodyResonanceGain/);
  assert.match(karplusEngine, /getAttackMs/);
  assert.match(karplusEngine, /TonePreset\.PIANO/);

  const pianoAttack = karplusEngine.match(
    /private fun getAttackMs\([^)]*\): Double = when \(preset\) \{[\s\S]*?TonePreset\.PIANO\s*->\s*([0-9.]+)/
  );
  assert.notEqual(pianoAttack, null, 'Piano attack must be explicit');
  assert.ok(
    Number.parseFloat(pianoAttack[1]) > 5.0,
    'Piano attack should be longer than the old 5ms baseline for a rounder start'
  );

  const pianoCutoff = karplusEngine.match(
    /getHfDampCoeff[\s\S]*?val cutoff = when \(preset\) \{[\s\S]*?TonePreset\.PIANO\s*->\s*([0-9.]+)/
  );
  assert.notEqual(pianoCutoff, null, 'Piano HF cutoff must be explicit');
  assert.ok(
    Number.parseFloat(pianoCutoff[1]) <= 5000,
    'Piano HF cutoff should be lower for a rounder long-session tone'
  );

  const pianoExciterGain = karplusEngine.match(
    /private fun getExciterGain\([^)]*\): Double = when \(preset\) \{[\s\S]*?TonePreset\.PIANO\s*->\s*([0-9.]+)/
  );
  assert.notEqual(pianoExciterGain, null, 'Piano exciter gain must be explicit');
  assert.ok(
    Number.parseFloat(pianoExciterGain[1]) <= 0.30,
    'Piano exciter gain should be reduced to avoid a sharp attack'
  );

  const pianoSawMix = karplusEngine.match(
    /private fun getExciterSawMix[\s\S]*?when \(preset\) \{[\s\S]*?TonePreset\.PIANO\s*->\s*([0-9.]+)/
  );
  assert.notEqual(pianoSawMix, null, 'Piano saw mix must be explicit');
  assert.ok(
    Number.parseFloat(pianoSawMix[1]) <= 0.12,
    'Piano saw mix should stay low so the tone feels rounded rather than plucky'
  );
});

test('android piano chord path applies chord-aware detune, gain staging, and bus limiting', () => {
  const karplusEngine = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/KarplusStrongEngine.kt'
  );

  assert.match(karplusEngine, /buildDetunedVoices/);
  assert.match(karplusEngine, /getChordVoiceGain/);
  assert.match(karplusEngine, /getStereoSpread/);
  assert.match(karplusEngine, /getLimiterDrive/);
  assert.match(karplusEngine, /getBusSmoothingCoeff/);
  assert.match(karplusEngine, /applySoftLimiter/);
  assert.match(karplusEngine, /frequencies\.size/);
  assert.match(karplusEngine, /val noteCount = frequencies\.size/);
  assert.match(
    karplusEngine,
    /TonePreset\.PIANO\s*->\s*when\s*\{[\s\S]*noteCount >= 4[\s\S]*listOf\(freq\)[\s\S]*noteCount >= 3/
  );
  assert.match(
    karplusEngine,
    /val chordVoiceGain = getChordVoiceGain\(tonePreset, noteCount\)/
  );
  assert.match(
    karplusEngine,
    /val stereoSpread = getStereoSpread\(tonePreset, noteCount\)/
  );
  assert.match(
    karplusEngine,
    /val limiterDrive = getLimiterDrive\(tonePreset, noteCount\)/
  );
  assert.match(
    karplusEngine,
    /val limitedLeft = applySoftLimiter\(left \* scale, limiterDrive\)/
  );
  assert.match(
    karplusEngine,
    /val limitedRight = applySoftLimiter\(right \* scale, limiterDrive\)/
  );
  assert.match(
    karplusEngine,
    /val smoothedLeft = previousLeft \* busSmoothingCoeff \+ limitedLeft \* \(1\.0 - busSmoothingCoeff\)/
  );
  assert.match(
    karplusEngine,
    /val smoothedRight = previousRight \* busSmoothingCoeff \+ limitedRight \* \(1\.0 - busSmoothingCoeff\)/
  );
  assert.match(
    karplusEngine,
    /output\[i \* 2\] = \(applySoftLimiter\(smoothedLeft, 1\.0\) \* 32767\)/
  );
  assert.match(
    karplusEngine,
    /output\[i \* 2 \+ 1\] = \(applySoftLimiter\(smoothedRight, 1\.0\) \* 32767\)/
  );
});

test('android piano dense chords trim transients and use slight onset staggering', () => {
  const karplusEngine = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/KarplusStrongEngine.kt'
  );

  assert.match(karplusEngine, /getChordExciterTrim/);
  assert.match(karplusEngine, /getChordBodyTrim/);
  assert.match(karplusEngine, /getChordAttackScale/);
  assert.match(karplusEngine, /getChordOnsetSpreadMs/);
  assert.match(
    karplusEngine,
    /val chordExciterTrim = getChordExciterTrim\(tonePreset, noteCount\)/
  );
  assert.match(
    karplusEngine,
    /val chordBodyTrim = getChordBodyTrim\(tonePreset, noteCount\)/
  );
  assert.match(
    karplusEngine,
    /val chordAttackScale = getChordAttackScale\(tonePreset, noteCount\)/
  );
  assert.match(
    karplusEngine,
    /val onsetSpreadMs = getChordOnsetSpreadMs\(tonePreset, noteCount\)/
  );
  assert.match(
    karplusEngine,
    /startDelayFrames = msToFrames\(staggerOffsetMs\)/
  );
  assert.match(
    karplusEngine,
    /if \(delayedFrame < 0\) return@forEach/
  );
  assert.match(
    karplusEngine,
    /getExciterGain\(tonePreset\) \* exciterGainScale/
  );
  assert.match(
    karplusEngine,
    /getBodyResonanceGain\(tonePreset\) \* bodyResonanceScale/
  );
  assert.match(
    karplusEngine,
    /sampleRate \* getAttackMs\(tonePreset\) \* attackScale/
  );
  assert.match(
    karplusEngine,
    /TonePreset\.PIANO -> when \{[\s\S]*noteCount >= 4[\s\S]*noteCount >= 3/
  );
});

test('android solfege vocal path exposes clarity controls for a gentle humming voice', () => {
  const vocalEngine = read(
    'apps/android/app/src/main/java/com/xiyue/app/playback/VocalSynthesisEngine.kt'
  );
  const synth = read('apps/android/app/src/main/java/com/xiyue/app/playback/ToneSynth.kt');

  assert.match(vocalEngine, /getVibratoDepth/);
  assert.match(vocalEngine, /getBreathinessGain/);
  assert.match(vocalEngine, /getEnvelopeAttackMs/);
  assert.match(vocalEngine, /getEnvelopeReleaseMs/);
  assert.match(vocalEngine, /getFormantPresenceGain/);
  assert.match(vocalEngine, /getVowelClarityGain/);
  assert.match(vocalEngine, /solfegeToVowel/);
  assert.match(vocalEngine, /VowelPresets/);
  assert.match(vocalEngine, /DO[\s\S]*VowelPresets\.O/);
  assert.match(vocalEngine, /RE[\s\S]*VowelPresets\.E/);
  assert.match(vocalEngine, /MI[\s\S]*VowelPresets\.I/);
  assert.match(vocalEngine, /FA[\s\S]*VowelPresets\.A/);
  assert.match(synth, /noteNameToSolfege/);
});
