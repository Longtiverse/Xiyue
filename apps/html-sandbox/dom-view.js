const NOTE_OPTIONS = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B'];
const OCTAVE_OPTIONS = [2, 3, 4, 5];
const PLAYBACK_MODE_LABELS = {
  scaleAscending: '音阶上行',
  chordBlock: '和弦齐奏',
  chordArpeggioUp: '和弦琶音上行',
};

function createOptionMarkup(options, selectedValue) {
  return options
    .map((value) => `<option value="${value}"${String(value) === String(selectedValue) ? ' selected' : ''}>${value}</option>`)
    .join('');
}

function createLibraryItemMarkup(item, isSelected) {
  return `
    <li>
      <button
        type="button"
        class="library-item${isSelected ? ' is-selected' : ''}"
        data-library-item-id="${item.id}"
      >
        <span class="library-item-kind">${item.kind === 'scale' ? '音阶' : '和弦'}</span>
        <strong>${item.label}</strong>
      </button>
    </li>
  `;
}

function createPitchChipMarkup(pitchLabel) {
  return `<li class="pitch-chip">${pitchLabel}</li>`;
}

function createSequenceRowMarkup(row) {
  return `
    <li class="sequence-row${row.isActive ? ' is-active' : ''}">
      <span class="sequence-pitch">${row.pitchLabel}</span>
      <span class="sequence-time">${row.startMs.toFixed(0)}ms</span>
      <span class="sequence-time">${row.durationMs.toFixed(0)}ms</span>
    </li>
  `;
}

function createPlaybackModeMarkup(mode, selectedMode) {
  const label = PLAYBACK_MODE_LABELS[mode] ?? mode;
  return `<option value="${mode}"${mode === selectedMode ? ' selected' : ''}>${label}</option>`;
}

function renderKeyboard(container, keyboardKeys) {
  const whiteKeyWidth = 44;
  const blackKeyWidth = 28;
  let whiteKeyIndex = 0;
  const whiteKeyCount = keyboardKeys.filter((key) => !key.isSharp).length;

  container.style.width = `${whiteKeyCount * whiteKeyWidth}px`;
  container.innerHTML = keyboardKeys
    .map((key) => {
      const left = key.isSharp ? whiteKeyIndex * whiteKeyWidth - blackKeyWidth / 2 : whiteKeyIndex * whiteKeyWidth;
      const markup = `
        <button
          type="button"
          class="keyboard-key${key.isSharp ? ' is-sharp' : ' is-white'}${key.isPreview ? ' is-preview' : ''}${
            key.isActive ? ' is-active' : ''
          }"
          data-midi-number="${key.midiNumber}"
          style="left:${left}px"
          tabindex="-1"
        >
          <span>${key.label}</span>
        </button>
      `;

      if (!key.isSharp) {
        whiteKeyIndex += 1;
      }

      return markup;
    })
    .join('');
}

export function createDomView(doc = globalThis.document) {
  const elements = {
    searchInput: doc.querySelector('#search-input'),
    kindFilter: doc.querySelector('#kind-filter'),
    rootSelect: doc.querySelector('#root-select'),
    octaveSelect: doc.querySelector('#octave-select'),
    bpmInput: doc.querySelector('#bpm-input'),
    volumeInput: doc.querySelector('#volume-input'),
    volumeValue: doc.querySelector('#volume-value'),
    resetButton: doc.querySelector('#reset-button'),
    libraryList: doc.querySelector('#library-list'),
    emptyState: doc.querySelector('#empty-state'),
    selectedTitle: doc.querySelector('#selected-title'),
    selectedMeta: doc.querySelector('#selected-meta'),
    playbackStatus: doc.querySelector('#playback-status'),
    pitchList: doc.querySelector('#pitch-list'),
    playbackModeSelect: doc.querySelector('#playback-mode-select'),
    playButton: doc.querySelector('#play-button'),
    stopButton: doc.querySelector('#stop-button'),
    sequenceList: doc.querySelector('#sequence-list'),
    keyboard: doc.querySelector('#keyboard'),
  };

  function bindHandlers(handlers) {
    elements.searchInput?.addEventListener('input', (event) => handlers.onSearchChange?.(event.target.value));
    elements.kindFilter?.addEventListener('change', (event) => handlers.onKindFilterChange?.(event.target.value));
    elements.rootSelect?.addEventListener('change', (event) => handlers.onRootChange?.(event.target.value));
    elements.octaveSelect?.addEventListener('change', (event) => handlers.onOctaveChange?.(Number(event.target.value)));
    elements.bpmInput?.addEventListener('change', (event) => handlers.onBpmChange?.(Number(event.target.value)));
    elements.volumeInput?.addEventListener('input', (event) => handlers.onVolumeChange?.(Number(event.target.value)));
    elements.resetButton?.addEventListener('click', () => handlers.onResetControls?.());
    elements.playbackModeSelect?.addEventListener('change', (event) =>
      handlers.onPlaybackModeChange?.(event.target.value),
    );
    elements.playButton?.addEventListener('click', () => handlers.onPlaySelection?.());
    elements.stopButton?.addEventListener('click', () => handlers.onStopSelection?.());
    elements.libraryList?.addEventListener('click', (event) => {
      const button = event.target.closest('[data-library-item-id]');

      if (button) {
        handlers.onLibraryItemSelect?.(button.dataset.libraryItemId);
      }
    });
  }

  function render(viewModel) {
    if (elements.rootSelect && elements.rootSelect.options.length === 0) {
      elements.rootSelect.innerHTML = createOptionMarkup(NOTE_OPTIONS, viewModel.root);
    }

    if (elements.octaveSelect && elements.octaveSelect.options.length === 0) {
      elements.octaveSelect.innerHTML = createOptionMarkup(OCTAVE_OPTIONS, viewModel.octave);
    }

    if (elements.rootSelect) {
      elements.rootSelect.value = viewModel.root;
    }

    if (elements.octaveSelect) {
      elements.octaveSelect.value = String(viewModel.octave);
    }

    if (elements.bpmInput) {
      elements.bpmInput.value = String(viewModel.bpm);
    }

    if (elements.volumeInput) {
      elements.volumeInput.value = String(viewModel.volume);
    }

    if (elements.volumeValue) {
      elements.volumeValue.textContent = `${Math.round(viewModel.volume * 100)}%`;
    }

    elements.libraryList.innerHTML = viewModel.libraryItems
      .map((item) => createLibraryItemMarkup(item, item.id === viewModel.selectedLibraryItemId))
      .join('');
    elements.emptyState.hidden = viewModel.libraryItems.length > 0;
    elements.playbackStatus.textContent = viewModel.isPlaying ? '播放中：高亮与音频已同步' : '就绪：可预览当前选择';

    if (!viewModel.selectedItem) {
      elements.selectedTitle.textContent = '从左侧选择一个音阶或和弦';
      elements.selectedMeta.textContent = '支持搜索、筛选、生成序列、真实播放与键盘高亮。';
      elements.pitchList.innerHTML = '';
      elements.playbackModeSelect.innerHTML = '<option value="">请先选择条目</option>';
      elements.playbackModeSelect.disabled = true;
      elements.playButton.disabled = true;
      elements.stopButton.disabled = true;
      elements.sequenceList.innerHTML = '';
      renderKeyboard(elements.keyboard, viewModel.keyboardKeys);
      return;
    }

    elements.selectedTitle.textContent = viewModel.selectedItem.displayName;
    elements.selectedMeta.textContent = `${viewModel.selectedItem.kind === 'scale' ? '音阶' : '和弦'} · BPM ${
      viewModel.bpm
    } · 音量 ${Math.round(viewModel.volume * 100)}%`;
    elements.pitchList.innerHTML = viewModel.selectedItem.pitchLabels.map(createPitchChipMarkup).join('');
    elements.playbackModeSelect.innerHTML = viewModel.selectedItem.supportedModes
      .map((mode) => createPlaybackModeMarkup(mode, viewModel.selectedItem.playbackMode))
      .join('');
    elements.playbackModeSelect.disabled = false;
    elements.playButton.disabled = viewModel.isPlaying;
    elements.stopButton.disabled = !viewModel.isPlaying;
    elements.sequenceList.innerHTML = viewModel.selectedItem.sequenceRows.map(createSequenceRowMarkup).join('');
    renderKeyboard(elements.keyboard, viewModel.keyboardKeys);
  }

  return {
    bindHandlers,
    render,
  };
}
