/* ─────────────────────────────────────────────
   Xiyue — Mobile Home Flow Showcase
   Storyboard state machine + micro-animations
   Zero dependencies · works offline via file:///
   ───────────────────────────────────────────── */

;(function () {
  'use strict';

  /* ── Stage definitions ──────────────── */
  var STAGES = [
    {
      id: 'ready',
      title: 'Home / Ready',
      callout: 'The first view stays calm and legible: playback card first, recent items nearby, and the fuller library held back.',
      behavior: 'Quick-start-first hierarchy',
      behaviorCopy: 'The home view preserves the Android structure: playback display first, compact selection nearby, controls reachable below.',
      motion: 'Soft fades and restrained scale feedback around a 150\u2013300\u2009ms feel.',
      source: 'HomeScreen + compact library selector + quick-start-first redesign guidance.',
      practiceTitle: 'G Dorian Flow',
      practiceSub: 'Rooted in G for a low-distraction warm start with space to listen.',
      heroLive: 'Ready',
      playLabel: 'Start practice',
      duration: 4500
    },
    {
      id: 'select-root',
      title: 'Select root note',
      callout: 'Horizontal chip row shifts emphasis locally \u2014 no full-page context switch. The selected note scales up while neighbours fade proportionally.',
      behavior: 'Inline selection, no modal',
      behaviorCopy: 'Root note changes happen in place with stepwise feedback. Inspired by SwipeableRootNoteSelector.',
      motion: 'Chip scale + border shift in 150\u2009ms; card text fades via 300\u2009ms cross-dissolve.',
      source: 'SwipeableRootNoteSelector + TransitionAnimations.',
      practiceTitle: 'A Dorian Flow',
      practiceSub: 'Shifted to A \u2014 practice adjusts root without interrupting the flow.',
      heroLive: 'Ready',
      playLabel: 'Start practice',
      duration: 4000
    },
    {
      id: 'select-practice',
      title: 'Select practice',
      callout: 'Tapping a recent card swaps the hero practice locally \u2014 the screen stays singular and never pushes a new page.',
      behavior: 'Card swap, not navigation',
      behaviorCopy: 'The hero card updates via a fade/slide micro-transition. Recent strip highlights the new selection.',
      motion: 'Hero title cross-fades in 200\u2009ms; recent card gets jade border emphasis.',
      source: 'HomeScreen recent/favorites strip + PlaybackDisplaySection card swap.',
      practiceTitle: 'A7 Shell Voicings',
      practiceSub: 'Compact chord study for right-hand timing and voicing clarity.',
      heroLive: 'Ready',
      playLabel: 'Start practice',
      duration: 4000
    },
    {
      id: 'playback-focus',
      title: 'Playback focus',
      callout: 'One strong play state: the button becomes a pause control, waveform starts, keyboard keys light up with the current note, and gold accents mark live activity.',
      behavior: 'Singular play emphasis',
      behaviorCopy: 'Play button morphs, waveform animates, keyboard shows active note. Inspired by AnimatedPlayButton + AnimatedKeyboardKey.',
      motion: 'Button color transition 300\u2009ms; waveform bars sync to displayed BPM; keyboard key scale 150\u2009ms.',
      source: 'AnimatedPlayButton + WaveformVisualizer + AnimatedKeyboardKey + MetronomeEdgeGlow.',
      practiceTitle: 'A7 Shell Voicings',
      practiceSub: 'Playing now \u2014 listen for the voicing sequence while watching active notes.',
      heroLive: 'Playing',
      playLabel: 'Pause',
      duration: 5500
    },
    {
      id: 'playback-detail',
      title: 'Playback detail',
      callout: 'Tap-to-expand reveals the full phrase sequence, progress position, loop settings, and BPM \u2014 without navigating away from the home screen.',
      behavior: 'Progressive detail reveal',
      behaviorCopy: 'Detail panel slides open below the note focus card. Matches PlaybackDisplaySection\'s two-mode concept.',
      motion: 'Panel max-height animates over 500\u2009ms; sequence chips get active/next state transitions.',
      source: 'PlaybackDisplaySection note-focus vs detailed mode + design token spacing.',
      practiceTitle: 'A7 Shell Voicings',
      practiceSub: 'Detail view shows the full phrase structure while playback continues.',
      heroLive: 'Playing',
      playLabel: 'Pause',
      duration: 5000
    },
    {
      id: 'browse-library',
      title: 'Browse library',
      callout: 'Full browsing arrives as a bottom sheet that slides up over the home view. The home screen stays underneath, maintaining context and orientation.',
      behavior: 'Sheet overlay, not page push',
      behaviorCopy: 'A bottom sheet contains search, filters, and the full list. Dismissing returns to home without re-navigation.',
      motion: 'Sheet slides up over 500\u2009ms with spring easing; overlay dims the background gently.',
      source: 'Library panel reimagined as Android bottom sheet pattern.',
      practiceTitle: 'A7 Shell Voicings',
      practiceSub: 'Browsing the library while maintaining the current practice context.',
      heroLive: 'Ready',
      playLabel: 'Start practice',
      duration: 5000
    }
  ];

  var currentIndex = 0;
  var playing = true;
  var timer = null;
  var waveRaf = null;
  var bpm = 92;

  /* ── DOM refs ───────────────────────── */
  var body = document.body;
  var stageItems = document.querySelectorAll('.stage-item');
  var calloutTitle = document.getElementById('callout-title');
  var calloutCopy = document.getElementById('callout-copy');
  var behaviorTitle = document.getElementById('behavior-title');
  var behaviorCopy = document.getElementById('behavior-copy');
  var summaryState = document.getElementById('summary-state');
  var summaryMotion = document.getElementById('summary-motion');
  var summarySource = document.getElementById('summary-source');
  var waveBars = document.querySelectorAll('.waveform span');
  var toggleBtn = document.querySelector('[data-action="toggle-play"]');
  var restartBtn = document.querySelector('[data-action="restart"]');

  /* ── Stage text updaters ────────────── */
  function setText(attr, value) {
    var els = document.querySelectorAll('[data-stage-text="' + attr + '"]');
    for (var i = 0; i < els.length; i++) {
      els[i].style.opacity = '0';
      (function (el, v) {
        setTimeout(function () {
          el.textContent = v;
          el.style.opacity = '1';
        }, 180);
      })(els[i], value);
    }
  }

  /* ── Apply a stage ──────────────────── */
  function applyStage(index) {
    var s = STAGES[index];
    body.setAttribute('data-stage', s.id);

    // Rail highlighting
    for (var i = 0; i < stageItems.length; i++) {
      stageItems[i].classList.toggle('is-active', i === index);
    }

    // Callouts
    if (calloutTitle) calloutTitle.textContent = s.title;
    if (calloutCopy) calloutCopy.textContent = s.callout;
    if (behaviorTitle) behaviorTitle.textContent = s.behavior;
    if (behaviorCopy) behaviorCopy.textContent = s.behaviorCopy;

    // Summary strip
    if (summaryState) summaryState.textContent = s.title;
    if (summaryMotion) summaryMotion.textContent = s.motion;
    if (summarySource) summarySource.textContent = s.source;

    // Stage-specific text inside the phone
    setText('practice-title', s.practiceTitle);
    setText('practice-subtitle', s.practiceSub);
    setText('hero-live', s.heroLive);
    setText('play-label', s.playLabel);

    // Root chip animation for select-root stage
    animateRootChips(s.id);

    // Scroll phone to relevant area
    scrollPhoneToSection(s.id);
  }

  /* ── Root chip interaction ──────────── */
  function animateRootChips(stageId) {
    var chips = document.querySelectorAll('.root-chip');
    if (!chips.length) return;

    // Default: A selected (index 2)
    var selectedIdx = 2;

    // In ready state, G is selected (index 1)
    if (stageId === 'ready') {
      selectedIdx = 1;
    }

    for (var i = 0; i < chips.length; i++) {
      chips[i].classList.remove('is-selected', 'is-near');
      var dist = Math.abs(i - selectedIdx);
      if (dist === 0) chips[i].classList.add('is-selected');
      else if (dist === 1) chips[i].classList.add('is-near');
    }
  }

  /* ── Scroll phone to relevant section ─ */
  function scrollPhoneToSection(stageId) {
    var screen = document.querySelector('.phone-screen');
    if (!screen) return;

    var target = 0;
    if (stageId === 'ready') target = 0;
    else if (stageId === 'select-root') target = 280;
    else if (stageId === 'select-practice') target = 180;
    else if (stageId === 'playback-focus') target = 80;
    else if (stageId === 'playback-detail') target = 120;
    else if (stageId === 'browse-library') target = 0;

    screen.scrollTo({ top: target, behavior: 'smooth' });
  }

  /* ── Waveform animation ─────────────── */
  var wavePhase = 0;

  function animateWave() {
    var stage = body.getAttribute('data-stage');
    var isPlaying = stage === 'playback-focus' || stage === 'playback-detail';
    var beatInterval = 60000 / bpm; // ms per beat
    var speed = 1000 / beatInterval; // beats per ms scaled

    wavePhase += 0.035 * (1 + speed);

    for (var i = 0; i < waveBars.length; i++) {
      var x = i / waveBars.length;
      if (isPlaying) {
        // Composite wave matching WaveformVisualizer.kt
        var wave1 = Math.sin((x * 4 * Math.PI) + wavePhase) * 0.30;
        var wave2 = Math.sin((x * 8 * Math.PI) + wavePhase * 1.3) * 0.20;
        var wave3 = Math.sin((x * 2 * Math.PI) + wavePhase * 0.7) * 0.15;
        var noise = (Math.random() - 0.5) * 0.10;
        var h = 0.5 + wave1 + wave2 + wave3 + noise;
        h = Math.max(0.08, Math.min(1, h));
        waveBars[i].style.height = (h * 100) + '%';
        waveBars[i].style.background = 'linear-gradient(180deg, var(--gold), rgba(201,167,106,0.35))';
      } else {
        // Idle: gentle breathing
        var idle = 0.12 + Math.sin(wavePhase * 0.4 + i * 0.5) * 0.06;
        waveBars[i].style.height = (idle * 100) + '%';
        waveBars[i].style.background = 'linear-gradient(180deg, var(--accent), rgba(115,211,181,0.3))';
      }
    }

    waveRaf = requestAnimationFrame(animateWave);
  }

  /* ── Keyboard note cycling ──────────── */
  var keyboardTimer = null;
  var noteSequence = [2, 3, 4, 5, 1, 0]; // indices into white keys: E, F, G, A, D, C
  var noteIndex = 0;

  function cycleKeyboardNote() {
    var stage = body.getAttribute('data-stage');
    var isPlaying = stage === 'playback-focus' || stage === 'playback-detail';
    if (!isPlaying) return;

    var whiteKeys = document.querySelectorAll('.keyboard-white-keys .white-key');
    var blackKeys = document.querySelectorAll('.keyboard-black-keys .black-key');

    // Clear all active states
    for (var i = 0; i < whiteKeys.length; i++) {
      whiteKeys[i].classList.remove('is-active', 'is-preview', 'is-ghost');
    }
    for (var i = 0; i < blackKeys.length; i++) {
      blackKeys[i].classList.remove('is-active');
    }

    // Set current note active
    var curr = noteSequence[noteIndex];
    if (whiteKeys[curr]) whiteKeys[curr].classList.add('is-active');

    // Set next note as preview
    var nextIdx = (noteIndex + 1) % noteSequence.length;
    var next = noteSequence[nextIdx];
    if (whiteKeys[next]) whiteKeys[next].classList.add('is-preview');

    // Set the one after as ghost
    var ghostIdx = (noteIndex + 2) % noteSequence.length;
    var ghost = noteSequence[ghostIdx];
    if (whiteKeys[ghost]) whiteKeys[ghost].classList.add('is-ghost');

    // Update the current note display
    var noteNames = ['C', 'D', 'E', 'F', 'G', 'A', 'B\u266D', 'C'];
    var currentNote = document.querySelector('.current-note');
    var ghostNote = document.querySelector('.note-ghost');
    if (currentNote) currentNote.textContent = noteNames[curr] || '';
    if (ghostNote) ghostNote.textContent = noteNames[next] || '';

    noteIndex = nextIdx;
  }

  function startKeyboardCycle() {
    if (keyboardTimer) return;
    var interval = (60000 / bpm); // one note per beat
    keyboardTimer = setInterval(cycleKeyboardNote, interval);
    cycleKeyboardNote(); // immediate first
  }

  function stopKeyboardCycle() {
    if (keyboardTimer) {
      clearInterval(keyboardTimer);
      keyboardTimer = null;
    }
  }

  /* ── Storyboard auto-play ───────────── */
  function scheduleNext() {
    if (!playing) return;
    var s = STAGES[currentIndex];
    timer = setTimeout(function () {
      currentIndex = (currentIndex + 1) % STAGES.length;
      applyStage(currentIndex);
      updatePlaybackAnimations();
      scheduleNext();
    }, s.duration);
  }

  function updatePlaybackAnimations() {
    var stage = body.getAttribute('data-stage');
    var isPlaying = stage === 'playback-focus' || stage === 'playback-detail';
    if (isPlaying) {
      startKeyboardCycle();
    } else {
      stopKeyboardCycle();
    }
  }

  /* ── Controls ───────────────────────── */
  if (toggleBtn) {
    toggleBtn.addEventListener('click', function () {
      playing = !playing;
      toggleBtn.textContent = playing ? 'Pause storyboard' : 'Play storyboard';
      if (playing) {
        scheduleNext();
      } else {
        clearTimeout(timer);
        timer = null;
      }
    });
  }

  if (restartBtn) {
    restartBtn.addEventListener('click', function () {
      clearTimeout(timer);
      timer = null;
      stopKeyboardCycle();
      currentIndex = 0;
      playing = true;
      if (toggleBtn) toggleBtn.textContent = 'Pause storyboard';
      applyStage(0);
      updatePlaybackAnimations();
      scheduleNext();
    });
  }

  /* ── Stage rail click-to-jump ────────── */
  for (var i = 0; i < stageItems.length; i++) {
    (function (idx) {
      stageItems[idx].addEventListener('click', function () {
        clearTimeout(timer);
        timer = null;
        stopKeyboardCycle();
        currentIndex = idx;
        applyStage(idx);
        updatePlaybackAnimations();
        if (playing) scheduleNext();
      });
    })(i);
  }

  /* ── Init ────────────────────────────── */
  applyStage(0);
  waveRaf = requestAnimationFrame(animateWave);
  scheduleNext();
})();
