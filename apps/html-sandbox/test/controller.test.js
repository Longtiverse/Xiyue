import test from 'node:test';
import assert from 'node:assert/strict';
import { JSDOM } from 'jsdom';
import { createSandboxController } from '../controller.js';
import { createFakeView, createFakeScheduler, createFakePlayer } from './helpers.js';

test('initializes view with library data from music-core', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });

  controller.init();

  assert.ok(view.renders.length > 0);
  assert.ok(view.renders[0].libraryItems.length > 10);
});

test('shows onboarding for first-time users', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });
  
  // Clear any stored settings to simulate first visit
  localStorage.clear();
  
  controller.init();
  
  // Should show onboarding
  assert.ok(view.renders[0].showOnboarding);
  assert.equal(view.renders[0].onboardingStep, 0);
});

test('displays Chinese labels for library items', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });
  
  controller.init();
  
  // Check that library items have Chinese labels
  const majorScale = view.renders[0].libraryItems.find(item => item.id === 'scale:Major');
  assert.ok(majorScale);
  assert.equal(majorScale.labelZh, '大调音阶');
  assert.equal(majorScale.difficulty, 'beginner');
});

test('shows difficulty badges for library items', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });
  
  controller.init();
  
  // Check that items have difficulty labels
  const items = view.renders[0].libraryItems;
  assert.ok(items.some(item => item.difficultyLabel === '入门'));
  assert.ok(items.some(item => item.difficultyLabel === '进阶'));
});

test('keyboard displays finger numbers when fingering mode is enabled', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });
  
  controller.init();
  controller.selectLibraryItem('scale:Major');
  controller.setFingeringMode(true);
  
  const lastRender = view.renders[view.renders.length - 1];
  const keyboardKeys = lastRender.keyboardKeys;
  
  // Check that active keys have finger numbers
  const activeKeys = keyboardKeys.filter(key => key.isActive || key.isPreview);
  assert.ok(activeKeys.length > 0);
  assert.ok(activeKeys.some(key => key.fingerNumber !== null));
});

test('updates rendered result when selecting a library item and search query', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });

  controller.init();
  controller.setSearch('major');
  controller.selectLibraryItem('scale:Major');

  assert.ok(view.renders.length > 1);
  assert.ok(view.renders.at(-1).selectedItem);
  assert.ok(view.renders.at(-1).libraryItems.every((item) => item.searchText.includes('major')));
});

test('applies root, octave and bpm changes to generated results', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });

  controller.init();
  controller.selectLibraryItem('scale:Major');
  controller.setRoot('G');
  controller.setOctave(5);
  controller.setBpm(140);

  const render = view.renders.at(-1);
  assert.equal(render.root, 'G');
  assert.equal(render.octave, 5);
  assert.equal(render.bpm, 140);
  assert.ok(render.selectedItem.pitchLabels.some((label) => label.startsWith('G')));
});

test('normalizes invalid bpm and volume values before rendering', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });

  controller.init();
  controller.setBpm(500);
  controller.setVolume(-0.5);

  assert.equal(view.renders.at(-1).bpm, 240);
  assert.equal(view.renders.at(-1).volume, 0);
});

test('starts audio playback, exposes playing state, and syncs active highlights over time', () => {
  const view = createFakeView();
  const scheduler = createFakeScheduler();
  const player = createFakePlayer();
  const controller = createSandboxController({ view, scheduler, player });

  controller.init();
  controller.selectLibraryItem('chord:Maj7');
  controller.setPlaybackMode('chordArpeggioUp');
  controller.setVolume(0.4);
  controller.playSelection();

  assert.equal(player.sessions.length, 1);
  assert.equal(player.sessions[0].options.volume, 0.4);
  assert.equal(view.renders.at(-1).isPlaying, true);
  assert.deepEqual(
    scheduler.tasks.filter((task) => !task.cleared).map((task) => task.delay),
    [0, 500, 1000, 1500, 2000, 2500],
  );

  scheduler.runDueTasks(1000);

  assert.deepEqual(
    view.renders.at(-1).keyboardKeys.filter((key) => key.isActive).map((key) => key.label),
    ['G4'],
  );

  scheduler.runDueTasks(3000);

  assert.equal(view.renders.at(-1).isPlaying, false);
  assert.deepEqual(
    view.renders.at(-1).keyboardKeys.filter((key) => key.isActive).map((key) => key.label),
    [],
  );
});

test('stops current playback and clears pending highlights', () => {
  const view = createFakeView();
  const scheduler = createFakeScheduler();
  const player = createFakePlayer();
  const controller = createSandboxController({ view, scheduler, player });

  controller.init();
  controller.selectLibraryItem('scale:Major');
  controller.playSelection();
  controller.stopSelection();

  assert.equal(player.sessions[0].stopped, true);
  assert.equal(view.renders.at(-1).isPlaying, false);
  assert.ok(scheduler.tasks.every((task) => task.cleared));
  assert.deepEqual(
    view.renders.at(-1).keyboardKeys.filter((key) => key.isActive).map((key) => key.label),
    [],
  );
});

test('updates active session volume immediately and stops playback before bpm regeneration', () => {
  const view = createFakeView();
  const scheduler = createFakeScheduler();
  const player = createFakePlayer();
  const controller = createSandboxController({ view, scheduler, player });

  controller.init();
  controller.selectLibraryItem('scale:Major');
  controller.playSelection();
  controller.setVolume(0.2);

  assert.deepEqual(player.sessions[0].volumeUpdates, [0.2]);

  controller.setBpm(90);

  assert.equal(player.sessions[0].stopped, true);
  assert.equal(view.renders.at(-1).isPlaying, false);
  assert.deepEqual(
    view.renders.at(-1).selectedItem?.sequenceRows.slice(0, 2).map((row) => row.durationMs),
    [666.6666666666666, 666.6666666666666],
  );
});

test('resets filters and playback controls back to defaults', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });

  controller.init();
  controller.setSearch('minor');
  controller.setKindFilter('scale');
  controller.setRoot('G');
  controller.setOctave(3);
  controller.setBpm(90);
  controller.setVolume(0.2);
  controller.selectLibraryItem('scale:NaturalMinor');
  controller.resetControls();

  assert.equal(view.renders.at(-1).selectedItem, null);
  assert.equal(view.renders.at(-1).root, 'C');
  assert.equal(view.renders.at(-1).octave, 4);
  assert.equal(view.renders.at(-1).bpm, 120);
  assert.equal(view.renders.at(-1).volume, 0.35);
  assert.equal(view.renders.at(-1).selectedLibraryItemId, null);
  assert.ok(view.renders.at(-1).libraryItems.length > 10);
});