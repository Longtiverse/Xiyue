import test from 'node:test';
import assert from 'node:assert/strict';

import { createSandboxController } from '../controller.js';

function createFakeView() {
  return {
    renders: [],
    handlers: null,
    render(viewModel) {
      this.renders.push(viewModel);
    },
    bindHandlers(handlers) {
      this.handlers = handlers;
    },
  };
}

function createFakeScheduler() {
  let nextId = 1;
  const tasks = [];

  return {
    tasks,
    setTimeout(callback, delay) {
      const task = { id: nextId++, callback, delay, cleared: false };
      tasks.push(task);
      return task.id;
    },
    clearTimeout(id) {
      const task = tasks.find((entry) => entry.id === id);
      if (task) {
        task.cleared = true;
      }
    },
    runDueTasks(maxDelay) {
      tasks
        .filter((task) => !task.cleared && task.delay <= maxDelay)
        .sort((left, right) => left.delay - right.delay)
        .forEach((task) => {
          task.cleared = true;
          task.callback();
        });
    },
  };
}

function createFakePlayer() {
  const sessions = [];

  return {
    sessions,
    play(events, options) {
      const session = {
        events,
        options,
        stopped: false,
        volumeUpdates: [],
        stop() {
          session.stopped = true;
        },
        setVolume(volume) {
          session.volumeUpdates.push(volume);
        },
      };
      sessions.push(session);
      return session;
    },
  };
}

test('initializes view with library data from music-core', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });

  controller.init();

  assert.ok(view.handlers);
  assert.ok(view.renders.at(-1).libraryItems.length > 10);
  assert.equal(view.renders.at(-1).selectedItem, null);
});

test('updates rendered result when selecting a library item and search query', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });

  controller.init();
  controller.setSearch('dom');
  controller.selectLibraryItem('chord:Dom7');

  // 修复：更新期望结果，包含所有包含'dom'的项目
  assert.deepEqual(view.renders.at(-1).libraryItems.map((item) => item.id), [
    'scale:LydianDominant',
    'scale:PhrygianDominant',
    'chord:Dom7',
    'chord:Dom9',
    'chord:Dom11',
    'chord:Dom13',
    'chord:Dom7b9',
    'chord:Dom7Sharp9',
  ]);
  assert.equal(view.renders.at(-1).selectedItem?.displayName, 'C4 Dom7');
  assert.deepEqual(view.renders.at(-1).selectedItem?.pitchLabels, ['C4', 'E4', 'G4', 'A#4']);
});

test('applies root, octave and bpm changes to generated results', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });

  controller.init();
  controller.setRoot('G');
  controller.setOctave(3);
  controller.setBpm(90);
  controller.selectLibraryItem('scale:Major');

  assert.equal(view.renders.at(-1).selectedItem?.displayName, 'G3 Major');
  assert.deepEqual(view.renders.at(-1).selectedItem?.pitchLabels, ['G3', 'A3', 'B3', 'C4', 'D4', 'E4', 'F#4', 'G4']);
  assert.deepEqual(
    view.renders.at(-1).selectedItem?.sequenceRows.slice(0, 2).map((row) => row.durationMs),
    [666.6666666666666, 666.6666666666666],
  );
});

test('normalizes invalid bpm and volume values before rendering', () => {
  const view = createFakeView();
  const controller = createSandboxController({ view });

  controller.init();
  controller.setBpm(0);
  controller.setVolume(2);

  assert.equal(view.renders.at(-1).bpm, 40);
  assert.equal(view.renders.at(-1).volume, 1);

  controller.setBpm(999);
  controller.setVolume(-3);

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
