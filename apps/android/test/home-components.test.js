import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';

const componentFiles = [
  'apps/android/app/src/main/java/com/xiyue/app/features/home/PracticeLibrarySection.kt',
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
  const uiState = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeUiState.kt', 'utf8');

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
  const tonePreset = readFileSync('apps/android/app/src/main/java/com/xiyue/app/playback/TonePreset.kt', 'utf8');

  assert.match(tonePreset, /enum class TonePreset/);
  assert.equal((tonePreset.match(/shortLabel = "/g) || []).length, 3, 'TonePreset enum must declare exactly three presets');
  assert.match(tonePreset, /WARM_PRACTICE/);
  assert.match(tonePreset, /SOFT_PIANO/);
  assert.match(tonePreset, /CLEAR_WOOD/);
  assert.match(tonePreset, /Warm Practice/);
  assert.match(tonePreset, /Soft Piano/);
  assert.match(tonePreset, /Clear Wood/);
});

test('android home factory and reducer support search and filter driven library items', () => {
  const factory = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt', 'utf8');
  const reducer = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt', 'utf8');

  assert.match(factory, /searchLibraryItems/);
  assert.match(factory, /recentLibraryItems/);
  assert.match(factory, /favoriteLibraryItems/);
  assert.match(factory, /displayMode/);
  assert.match(factory, /practicePicker/);
  assert.match(factory, /stepIndex|stepCount/);
  assert.match(factory, /groupedLibraryItems|buildLibraryGroups/);
  assert.match(factory, /buildPracticePicker|visibleShortcuts/);
  assert.match(factory, /tempoPresets|TempoPresetUiItem/);
  assert.match(factory, /TonePresetUiItem|toneOptions/);
  assert.match(factory, /TonePreset\.entries|preset\.label|preset\.shortLabel/);
  assert.doesNotMatch(factory, /路/);
  assert.match(factory, /Pause Practice|Resume Practice|Start Practice/);
  assert.match(factory, /showStopButton|stopButtonLabel/);
  assert.match(factory, /currentNoteLabel|practiceLabel/);
  assert.match(factory, /LibraryFilter/);
  assert.match(reducer, /UpdateLibraryFilter/);
  assert.match(reducer, /ToggleLibraryOverlay/);
  assert.match(reducer, /TogglePlaybackDisplayMode/);
  assert.match(reducer, /create\(/);
  assert.doesNotMatch(factory, /selectorSummaryLabel/);
  assert.doesNotMatch(factory, /quickSelectItems\s*=/);
  assert.doesNotMatch(factory, /sections\s*=/);
});

test('android home screen delegates to split section composables', () => {
  const screen = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt', 'utf8');
  const librarySection = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/PracticeLibrarySection.kt', 'utf8');
  const playbackSection = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt', 'utf8');
  const keyboardSection = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/KeyboardPreviewSection.kt', 'utf8');
  const displaySection = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackDisplaySection.kt', 'utf8');

  assert.match(screen, /PlaybackControlsSection\(/);
  assert.match(screen, /PlaybackDisplaySection\(/);
  assert.match(screen, /PracticePickerStrip/);
  assert.match(screen, /SelectRoot/);
  assert.match(screen, /previousRoot|nextRoot|selectedRoot/);
  assert.doesNotMatch(screen, /rootNotes\.forEach/);
  assert.doesNotMatch(screen, /practicePicker\.summaryLabel/);
  assert.match(screen, /UpdateLibraryFilter/);
  assert.match(librarySection, /UpdateSearchQuery|searchQuery/);
  assert.doesNotMatch(screen, /Dialog\(/);
  assert.match(screen, /Column\(/);
  assert.match(screen, /weight\(/);
  assert.match(screen, /weight\(1\.25f\)|weight\(1\.3f\)|weight\(1\.4f\)|weight\(1\.5f\)/);
  assert.doesNotMatch(screen, /LazyColumn/);
  assert.match(screen, /Surface|CardDefaults\.cardColors|containerColor/);
  assert.match(screen, /FilterChip|AssistChip/);
  assert.match(librarySection, /recentLibraryItems/);
  assert.match(librarySection, /Pinned|Recent/);
  assert.match(librarySection, /favoriteLibraryItems/);
  assert.match(librarySection, /ToggleFavoriteLibraryItem/);
  assert.match(librarySection, /val selectAndClose:[\s\S]*SelectLibraryItem\([\s\S]*ToggleLibraryOverlay/);
  assert.match(librarySection, /LibraryItemRow\([\s\S]*onSelect = \{ selectAndClose\(item\.id\) \}/);
  assert.doesNotMatch(librarySection, /practicePicker\.summaryLabel/);
  assert.match(librarySection, /ToggleLibraryOverlay|onDismissRequest/);
  assert.match(librarySection, /HorizontalDivider|Divider/);
  assert.match(librarySection, /groupedLibraryItems|LibraryGroupUiState/);
  assert.match(librarySection, /Surface\(|TextAlign\.Center/);
  assert.match(librarySection, /Results|result/i);
  assert.doesNotMatch(librarySection, /Settings/);
  assert.doesNotMatch(librarySection, /Slider/);
  assert.doesNotMatch(librarySection, /Switch/);
  assert.doesNotMatch(librarySection, /TogglePlayback/);
  assert.doesNotMatch(librarySection, /Selected/);
  assert.match(displaySection, /AnimatedContent|Crossfade/);
  assert.match(displaySection, /transitionSpec|fadeIn|fadeOut|tween|spring/);
  assert.doesNotMatch(displaySection, /LinearProgressIndicator/);
  assert.match(displaySection, /TogglePlaybackDisplayMode/);
  assert.match(displaySection, /keyboardState|keys/);
  assert.match(displaySection, /TextAlign\.Center|textAlign = TextAlign\.Center/);
  assert.match(displaySection, /stepIndex|stepCount|currentNoteLabel|toneLabel/);
  assert.match(displaySection, /clickable[\s\S]*onAction\(HomeAction\.TogglePlaybackDisplayMode\)/);
  assert.match(displaySection, /headlineLarge|fillMaxWidth\(\)/);
  assert.match(displaySection, /FontWeight\.Bold|fontWeight/);
  assert.doesNotMatch(displaySection, /路/);
    assert.match(playbackSection, /FilledTonalButton|Button/);
  assert.match(playbackSection, /tempoPresets|UpdateBpm/);
  assert.match(playbackSection, /toneOptions|selectedTonePreset|toneButtonLabel/);
  assert.match(playbackSection, /TonePreset/);
  assert.match(playbackSection, /DropdownMenu|DropdownMenuItem|toneMenuExpanded|modeMenuExpanded|tempoMenuExpanded/);
  assert.doesNotMatch(playbackSection, /LaunchedEffect/);
  assert.match(playbackSection, /UpdateTonePreset/);
  assert.match(playbackSection, /toneButtonLabel|option\.label|shortLabel/);
  assert.match(playbackSection, /Slider/);
  assert.match(playbackSection, /onValueChangeFinished/);
  assert.match(playbackSection, /UpdatePlaybackMode/);
  assert.match(playbackSection, /ToggleLoop/);
  assert.match(playbackSection, /StopPlayback/);
  assert.match(playbackSection, /showStopButton|stopButtonLabel/);
  assert.doesNotMatch(playbackSection, /quickSelectItems/);
  assert.doesNotMatch(playbackSection, /SelectLibraryItem/);
  assert.doesNotMatch(playbackSection, /ToggleSelectorSheet/);
  assert.match(playbackSection, /RoundedCornerShape|shape =/);
  assert.match(playbackSection, /ButtonDefaults|CardDefaults|containerColor/);
  assert.match(keyboardSection, /LazyVerticalGrid|Row/);
  assert.match(keyboardSection, /animateColorAsState|AnimatedContent|Surface/);
});
