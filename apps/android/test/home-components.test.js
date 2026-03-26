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
  assert.match(uiState, /val libraryFilter: LibraryFilter/);
  assert.match(uiState, /val isSelectorSheetVisible: Boolean/);
  assert.match(uiState, /val displayMode: PlaybackDisplayMode/);
  assert.match(uiState, /val recentLibraryItems: List<LibraryUiItem>/);
  assert.match(uiState, /val favoriteLibraryItems: List<LibraryUiItem>/);
  assert.match(uiState, /val selectorSummaryLabel: String/);
  assert.match(uiState, /val favorite: Boolean/);
  assert.match(uiState, /val selected: Boolean/);
  assert.match(uiState, /val supportingText: String/);
  assert.match(uiState, /val currentItemLabel: String/);
  assert.match(uiState, /val progressFraction: Float/);
  assert.match(uiState, /val playButtonLabel: String/);
});

test('android home factory and reducer support search and filter driven library items', () => {
  const factory = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeStateFactory.kt', 'utf8');
  const reducer = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeReducer.kt', 'utf8');

  assert.match(factory, /searchLibraryItems/);
  assert.match(factory, /recentLibraryItems/);
  assert.match(factory, /favoriteLibraryItems/);
  assert.match(factory, /displayMode/);
  assert.match(factory, /selectorSummaryLabel/);
  assert.match(factory, /progressFraction/);
  assert.match(factory, /LibraryFilter/);
  assert.match(reducer, /UpdateLibraryFilter/);
  assert.match(reducer, /ToggleSelectorSheet/);
  assert.match(reducer, /TogglePlaybackDisplayMode/);
  assert.match(reducer, /create\(/);
});

test('android home screen delegates to split section composables', () => {
  const screen = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/HomeScreen.kt', 'utf8');
  const librarySection = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/PracticeLibrarySection.kt', 'utf8');
  const playbackSection = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackControlsSection.kt', 'utf8');
  const keyboardSection = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/KeyboardPreviewSection.kt', 'utf8');
  const displaySection = readFileSync('apps/android/app/src/main/java/com/xiyue/app/features/home/PlaybackDisplaySection.kt', 'utf8');

  assert.match(screen, /PracticeLibrarySection\(/);
  assert.match(screen, /PlaybackControlsSection\(/);
  assert.match(screen, /KeyboardPreviewSection\(/);
  assert.match(screen, /PlaybackDisplaySection\(/);
  assert.match(screen, /Root Note|Root/);
  assert.match(librarySection, /UpdateSearchQuery|searchQuery/);
  assert.match(screen, /Scaffold/);
  assert.match(screen, /ModalBottomSheet|BottomSheet/);
  assert.match(screen, /CardDefaults\.cardColors|containerColor/);
  assert.match(screen, /AssistChip|Surface/);
  assert.match(librarySection, /recentLibraryItems/);
  assert.match(librarySection, /favoriteLibraryItems/);
  assert.match(librarySection, /ToggleFavoriteLibraryItem/);
  assert.match(librarySection, /TogglePlayback/);
  assert.match(librarySection, /ToggleSelectorSheet/);
  assert.match(librarySection, /HorizontalDivider|Divider/);
  assert.match(librarySection, /selectorSummaryLabel/);
  assert.match(librarySection, /Settings/);
  assert.match(librarySection, /Selected/);
  assert.match(librarySection, /Play/);
  assert.match(librarySection, /AnimatedVisibility|remember/);
  assert.match(displaySection, /AnimatedContent|Crossfade/);
  assert.match(displaySection, /LinearProgressIndicator/);
  assert.match(displaySection, /TogglePlaybackDisplayMode/);
  assert.match(playbackSection, /FilledTonalButton|Button/);
  assert.match(playbackSection, /currentItemLabel/);
  assert.match(playbackSection, /RoundedCornerShape|shape =/);
  assert.match(playbackSection, /ButtonDefaults|CardDefaults|containerColor/);
  assert.match(keyboardSection, /LazyVerticalGrid|Row/);
  assert.match(keyboardSection, /animateColorAsState|AnimatedContent|Surface/);
});
