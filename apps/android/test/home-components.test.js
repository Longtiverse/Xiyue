import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const read = (filePath) => readFileSync(filePath, 'utf8');

const componentFiles = [
  'apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/KeyboardPreviewSection.kt',
  'apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackDisplaySection.kt',
];

test('android home section components exist', () => {
  for (const filePath of componentFiles) {
    assert.equal(existsSync(filePath), true, `${filePath} should exist`);
  }
});

test('android home ui state includes library filter and playback metadata', () => {
  const uiState = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt');

  assert.match(uiState, /enum class LibraryFilter/);
  assert.match(uiState, /enum class PlaybackDisplayMode/);
  assert.match(uiState, /data class PracticePickerUiState/);
  assert.match(uiState, /data class PracticeShortcutUiItem/);
  assert.match(uiState, /data class LibraryGroupUiState/);
  assert.match(uiState, /data class TempoPresetUiItem/);
  assert.match(uiState, /data class TonePresetUiItem/);
  assert.match(uiState, /val libraryFilter: LibraryFilter/);
  assert.match(uiState, /val isPaused: Boolean/);
  assert.match(uiState, /val isLibraryOverlayVisible: Boolean/);
  assert.match(uiState, /val displayMode: PlaybackDisplayMode/);
  assert.match(uiState, /val groupedLibraryItems: List<LibraryGroupUiState>/);
  assert.match(uiState, /val recentLibraryItems: List<LibraryUiItem>/);
  assert.match(uiState, /val favoriteLibraryItems: List<LibraryUiItem>/);
  assert.match(uiState, /val favorite: Boolean/);
  assert.match(uiState, /val selected: Boolean/);
  assert.match(uiState, /val supportingText: String/);
  assert.match(uiState, /val practicePicker: PracticePickerUiState/);
  assert.match(uiState, /val practiceLabel: String/);
  assert.match(uiState, /val toneLabel: String/);
  assert.match(uiState, /val stepIndex: Int/);
  assert.match(uiState, /val stepCount: Int/);
  assert.match(uiState, /val currentNoteLabel: String/);
  assert.match(uiState, /val tempoPresets: List<TempoPresetUiItem>/);
  assert.match(uiState, /val selectedTonePreset: TonePreset/);
  assert.match(uiState, /val toneOptions: List<TonePresetUiItem>/);
  assert.match(uiState, /val toneButtonLabel: String/);
  assert.match(uiState, /val playButtonLabel: String/);
  assert.match(uiState, /val showStopButton: Boolean/);
  assert.match(uiState, /val stopButtonLabel: String/);
  assert.doesNotMatch(uiState, /data class PlaybackQuickSelectUiItem/);
  assert.doesNotMatch(uiState, /val sections: List<HomeSectionUiState>/);
  assert.doesNotMatch(uiState, /val selectorSummaryLabel: String/);
  assert.doesNotMatch(uiState, /val quickSelectItems:/);
  assert.doesNotMatch(uiState, /val currentItemLabel: String/);
  assert.doesNotMatch(uiState, /val detailLabel: String/);
});

test('android tone preset enum defines three choices with warm default', () => {
  const tonePreset = read('apps/android/app/src/main/java/com/xiyue/app/playback/TonePreset.kt');

  assert.match(tonePreset, /enum class TonePreset/);
  assert.equal(
    (tonePreset.match(/shortLabel = "/g) || []).length,
    3,
    'TonePreset enum must declare exactly three presets'
  );
  assert.match(tonePreset, /WARM_PRACTICE/);
  assert.match(tonePreset, /SOFT_PIANO/);
  assert.match(tonePreset, /CLEAR_WOOD/);
  assert.match(tonePreset, /Warm Practice/);
  assert.match(tonePreset, /Soft Piano/);
  assert.match(tonePreset, /Clear Wood/);
});

test('android home factory and reducer support search and filter driven library items', () => {
  const factory = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt'
  );
  const reducer = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt');

  assert.match(factory, /searchQuery/);
  assert.match(factory, /selectionResolver\.resolve\(/);
  assert.match(factory, /favoriteLibraryItems/);
  assert.match(factory, /recentLibraryItems/);
  assert.match(factory, /displayMode/);
  assert.match(factory, /practicePicker/);
  assert.match(factory, /groupedLibraryItems|buildLibraryGroups/);
  assert.match(factory, /buildPracticePicker/);
  assert.match(factory, /selectedTonePreset|effectiveTonePreset/);
  assert.doesNotMatch(factory, /路/);
  assert.match(factory, /selectedTonePreset/);
  assert.match(factory, /buildPlaybackControl/);
  assert.match(factory, /computeCurrentPracticeLabel|practiceLabel/);
  assert.match(factory, /LibraryFilter/);
  assert.match(reducer, /UpdateSearchQuery/);
  assert.match(reducer, /ClearSearchQuery/);
  assert.match(reducer, /UpdateLibraryFilter/);
  assert.match(reducer, /ToggleLibraryOverlay/);
  assert.match(reducer, /TogglePlaybackDisplayMode/);
  assert.match(reducer, /create\(/);
  assert.doesNotMatch(factory, /selectorSummaryLabel/);
  assert.doesNotMatch(factory, /quickSelectItems\s*=/);
  assert.doesNotMatch(factory, /sections\s*=/);
});

test('android home screen delegates to split section composables', () => {
  const screen = read('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt');
  const playbackSection = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt'
  );
  const keyboardSection = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/KeyboardPreviewSection.kt'
  );
  const displaySection = read(
    'apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackDisplaySection.kt'
  );

  assert.match(screen, /PlaybackControlsSection\(/);
  assert.match(screen, /PlaybackDisplaySection\(/);
  assert.match(screen, /CompactLibrarySelector\(/);
  assert.match(screen, /SelectRoot/);
  assert.match(screen, /selectedRoot/);
  assert.match(screen, /UpdateLibraryFilter/);
  assert.match(screen, /Column\(/);
  assert.match(screen, /weight\(/);
  assert.doesNotMatch(screen, /Dialog\(/);
  assert.match(screen, /Surface|CardDefaults\.cardColors|containerColor/);
  assert.match(screen, /FilterChip/);
  assert.match(screen, /LazyRow/);
  assert.match(displaySection, /AnimatedContent/);
  assert.match(displaySection, /transitionSpec|fadeIn|fadeOut|tween/);
  assert.match(displaySection, /TogglePlaybackDisplayMode/);
  assert.match(displaySection, /keyboardState|keys/);
  assert.match(displaySection, /TextAlign\.Center|textAlign = TextAlign\.Center/);
  assert.match(displaySection, /stepIndex|stepCount|currentNoteLabel|toneLabel/);
  assert.match(displaySection, /clickable[\s\S]*onAction\(HomeAction\.TogglePlaybackDisplayMode\)/);
  assert.match(displaySection, /fillMaxWidth\(\)/);
  assert.match(displaySection, /FontWeight\.Bold|fontWeight/);
  assert.match(playbackSection, /FilledTonalButton|Button/);
  assert.match(playbackSection, /toneOptions|selectedTonePreset|toneButtonLabel/);
  assert.match(playbackSection, /TonePreset/);
  assert.match(
    playbackSection,
    /DropdownMenu|DropdownMenuItem|toneMenuExpanded|modeMenuExpanded|tempoMenuExpanded/
  );
  assert.match(playbackSection, /UpdateTonePreset/);
  assert.match(playbackSection, /toneButtonLabel|option\.label|shortLabel/);
  assert.match(playbackSection, /Slider|EnhancedBpmSlider/);
  assert.match(playbackSection, /UpdatePlaybackMode/);
  assert.match(playbackSection, /ToggleLoop/);
  assert.match(playbackSection, /StopPlayback/);
  assert.match(playbackSection, /showStopButton|stopButtonLabel/);
  assert.match(playbackSection, /RoundedCornerShape|shape =/);
  assert.match(playbackSection, /ButtonDefaults|CardDefaults|containerColor/);
  assert.match(keyboardSection, /Row/);
  assert.match(keyboardSection, /animateColorAsState|Surface|Card/);
});
