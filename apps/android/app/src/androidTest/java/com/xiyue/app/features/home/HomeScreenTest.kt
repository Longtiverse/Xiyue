package com.xiyue.app.features.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestState(
        searchQuery: String = "",
        selectedRoot: PitchClass = PitchClass.C,
        isPlaying: Boolean = false,
        bpm: Int = 120
    ): HomeUiState {
        return HomeUiState(
            searchQuery = searchQuery,
            libraryFilter = LibraryFilter.ALL,
            selectedLibraryItemId = null,
            selectedRoot = selectedRoot,
            selectedPlaybackMode = PlaybackMode.SCALE_ASCENDING,
            chordBlockEnabled = true,
            chordArpeggioEnabled = false,
            selectedTonePreset = TonePreset.PURE,
            soundMode = PlaybackSoundMode.PITCH,
            bpm = bpm,
            isBpmInputVisible = false,
            loopEnabled = false,
            loopDurationMs = 0L,
            isPlaying = isPlaying,
            isPaused = false,
            isLibraryOverlayVisible = false,
            displayMode = PlaybackDisplayMode.NOTE_FOCUS,
            libraryItems = listOf(
                LibraryUiItem(
                    id = "major-scale",
                    label = "Major Scale",
                    kindLabel = "Scale",
                    supportingText = "Ionian mode",
                    favorite = false,
                    selected = false
                ),
                LibraryUiItem(
                    id = "minor-scale",
                    label = "Natural Minor",
                    kindLabel = "Scale",
                    supportingText = "Aeolian mode",
                    favorite = false,
                    selected = false
                ),
                LibraryUiItem(
                    id = "major-chord",
                    label = "Major Chord",
                    kindLabel = "Chord",
                    supportingText = "Triad",
                    favorite = false,
                    selected = false
                )
            ),
            groupedLibraryItems = emptyList(),
            favoriteLibraryItems = emptyList(),
            recentLibraryItems = emptyList(),
            practicePicker = PracticePickerUiState(
                summaryLabel = "C Major Scale",
                visibleShortcuts = emptyList(),
                rootNotes = PitchClass.entries.map { 
                    RootNoteUiItem(note = it, label = it.label, selected = it == selectedRoot)
                }
            ),
            playbackDisplay = PlaybackDisplayUiState(
                practiceLabel = "C Major Scale",
                toneLabel = "Pure",
                stepIndex = 0,
                stepCount = 8,
                statusLabel = if (isPlaying) "Playing" else "Ready",
                currentNoteLabel = "C",
                queuedLabel = null,
                hintLabel = "Tap play to start",
                displayMode = PlaybackDisplayMode.NOTE_FOCUS,
                sequenceNotes = emptyList()
            ),
            playbackControl = PlaybackControlUiState(
                bpm = bpm,
                loopEnabled = false,
                loopDurationMs = 0L,
                loopDurationLabel = "∞ No limit",
                chordBlockEnabled = true,
                chordArpeggioEnabled = false,
                isChord = false,
                soundMode = PlaybackSoundMode.PITCH,
                toneOptions = listOf(
                    TonePresetUiItem(TonePreset.PURE, "Pure", "P", selected = true),
                    TonePresetUiItem(TonePreset.WARM, "Warm", "W", selected = false)
                ),
                toneButtonLabel = "Pure",
                modeOptions = listOf(
                    PlaybackModeUiItem(PlaybackMode.SCALE_ASCENDING, "Ascending", selected = true),
                    PlaybackModeUiItem(PlaybackMode.SCALE_ASCENDING_DESCENDING, "Asc / Desc", selected = false)
                ),
                tempoPresets = emptyList(),
                playButtonLabel = if (isPlaying) "Pause" else "Play",
                showStopButton = isPlaying,
                stopButtonLabel = "Stop",
                isBpmInputVisible = false
            ),
            keyboardPreview = KeyboardPreviewUiState(
                title = "Keyboard",
                description = "",
                activeKeysLabel = "",
                keys = emptyList()
            )
        )
    }

    @Test
    fun homeScreen_displaysAllMainElements() {
        val testState = createTestState()
        
        composeTestRule.setContent {
            HomeScreen(
                state = testState,
                onAction = {}
            )
        }

        // Verify search field is displayed
        composeTestRule.onNodeWithText("Search scales or chords")
            .assertIsDisplayed()

        // Verify root note section is displayed
        composeTestRule.onNodeWithText("Root Note")
            .assertIsDisplayed()

        // Verify library items are displayed
        composeTestRule.onNodeWithText("Major Scale")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Natural Minor")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_searchQuery_updatesText() {
        var capturedQuery: String? = null
        val testState = createTestState(searchQuery = "")
        
        composeTestRule.setContent {
            HomeScreen(
                state = testState,
                onAction = { action ->
                    if (action is HomeAction.UpdateSearchQuery) {
                        capturedQuery = action.query
                    }
                }
            )
        }

        // Perform search input
        composeTestRule.onNodeWithText("Search scales or chords")
            .performTextInput("Major")

        // Verify action was triggered
        assert(capturedQuery == "Major")
    }

    @Test
    fun homeScreen_filterChips_areDisplayed() {
        val testState = createTestState()
        
        composeTestRule.setContent {
            HomeScreen(
                state = testState,
                onAction = {}
            )
        }

        // Verify filter chips are displayed
        composeTestRule.onNodeWithText("All")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Scales")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Chords")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_libraryItemSelection_triggersAction() {
        var selectedItemId: String? = null
        val testState = createTestState()
        
        composeTestRule.setContent {
            HomeScreen(
                state = testState,
                onAction = { action ->
                    if (action is HomeAction.SelectLibraryItem) {
                        selectedItemId = action.itemId
                    }
                }
            )
        }

        // Click on a library item
        composeTestRule.onNodeWithText("Major Scale")
            .performClick()

        // Verify correct item was selected
        assert(selectedItemId == "major-scale")
    }

    @Test
    fun homeScreen_clearSearchAction_isTriggered() {
        var clearActionTriggered = false
        val testState = createTestState(searchQuery = "Major")
        
        composeTestRule.setContent {
            HomeScreen(
                state = testState,
                onAction = { action ->
                    if (action == HomeAction.ClearSearchQuery) {
                        clearActionTriggered = true
                    }
                }
            )
        }

        // Click clear button (content description)
        composeTestRule.onNodeWithContentDescription("Clear search")
            .performClick()

        // Verify action was triggered
        assert(clearActionTriggered)
    }

    @Test
    fun homeScreen_rootNoteChange_triggersAction() {
        var selectedRoot: PitchClass? = null
        val testState = createTestState(selectedRoot = PitchClass.C)
        
        composeTestRule.setContent {
            HomeScreen(
                state = testState,
                onAction = { action ->
                    if (action is HomeAction.SelectRoot) {
                        selectedRoot = action.root
                    }
                }
            )
        }

        // Click on a different root note
        composeTestRule.onNodeWithText("D")
            .performClick()

        // Verify root note was changed
        assert(selectedRoot == PitchClass.D)
    }

    @Test
    fun homeScreen_filterSelection_triggersAction() {
        var selectedFilter: LibraryFilter? = null
        val testState = createTestState()
        
        composeTestRule.setContent {
            HomeScreen(
                state = testState,
                onAction = { action ->
                    if (action is HomeAction.UpdateLibraryFilter) {
                        selectedFilter = action.filter
                    }
                }
            )
        }

        // Click on Scales filter
        composeTestRule.onNodeWithText("Scales")
            .performClick()

        // Verify filter was selected
        assert(selectedFilter == LibraryFilter.SCALE)
    }

    @Test
    fun homeScreen_searchResultsCount_isDisplayed() {
        val testState = createTestState(searchQuery = "Major")
        
        composeTestRule.setContent {
            HomeScreen(
                state = testState,
                onAction = {}
            )
        }

        // Verify results count is displayed
        composeTestRule.onNodeWithText("3 results")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysInPlayingState() {
        val testState = createTestState(isPlaying = true, bpm = 140)
        
        composeTestRule.setContent {
            HomeScreen(
                state = testState,
                onAction = {}
            )
        }

        // Verify playing status is shown
        composeTestRule.onNodeWithText("Playing")
            .assertIsDisplayed()

        // Verify pause button is shown (not play)
        composeTestRule.onNodeWithContentDescription("Pause")
            .assertIsDisplayed()
    }
}
