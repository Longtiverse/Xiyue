package com.xiyue.app.features.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset
import org.junit.Rule
import org.junit.Test

class PlaybackControlsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createPlaybackControlState(
        isPlaying: Boolean = false,
        bpm: Int = 120,
        loopEnabled: Boolean = false,
        isChord: Boolean = false,
        chordBlockEnabled: Boolean = true,
        chordArpeggioEnabled: Boolean = false,
        soundMode: PlaybackSoundMode = PlaybackSoundMode.PITCH
    ): PlaybackControlUiState {
        return PlaybackControlUiState(
            bpm = bpm,
            loopEnabled = loopEnabled,
            loopDurationMs = 0L,
            loopDurationLabel = "∞ No limit",
            chordBlockEnabled = chordBlockEnabled,
            chordArpeggioEnabled = chordArpeggioEnabled,
            isChord = isChord,
            soundMode = soundMode,
            toneOptions = listOf(
                TonePresetUiItem(TonePreset.PURE, "Pure", "P", selected = true),
                TonePresetUiItem(TonePreset.WARM, "Warm", "W", selected = false),
                TonePresetUiItem(TonePreset.BRIGHT, "Bright", "B", selected = false)
            ),
            toneButtonLabel = "Pure",
            modeOptions = listOf(
                PlaybackModeUiItem(PlaybackMode.SCALE_ASCENDING, "Ascending", selected = true),
                PlaybackModeUiItem(PlaybackMode.SCALE_ASCENDING_DESCENDING, "Asc / Desc", selected = false),
                PlaybackModeUiItem(PlaybackMode.CHORD_BLOCK, "Block", selected = false)
            ),
            tempoPresets = emptyList(),
            playButtonLabel = if (isPlaying) "Pause" else "Play",
            showStopButton = isPlaying,
            stopButtonLabel = "Stop",
            isBpmInputVisible = false
        )
    }

    @Test
    fun playbackControls_displaysAllMainElements() {
        val state = createPlaybackControlState()
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify Play button is displayed
        composeTestRule.onNodeWithContentDescription("Play")
            .assertIsDisplayed()

        // Verify BPM label is displayed
        composeTestRule.onNodeWithText("BPM")
            .assertIsDisplayed()

        // Verify current BPM is displayed
        composeTestRule.onNodeWithText("120")
            .assertIsDisplayed()

        // Verify BPM presets are displayed
        composeTestRule.onNodeWithText("60")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("120")
            .assertIsDisplayed()
    }

    @Test
    fun playbackControls_playButton_triggersTogglePlayback() {
        var togglePlaybackCalled = false
        val state = createPlaybackControlState(isPlaying = false)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = { action ->
                    if (action == HomeAction.TogglePlayback) {
                        togglePlaybackCalled = true
                    }
                }
            )
        }

        // Click play button
        composeTestRule.onNodeWithContentDescription("Play")
            .performClick()

        // Verify action was triggered
        assert(togglePlaybackCalled)
    }

    @Test
    fun playbackControls_pauseButton_showsWhenPlaying() {
        val state = createPlaybackControlState(isPlaying = true)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify Pause button is displayed
        composeTestRule.onNodeWithContentDescription("Pause")
            .assertIsDisplayed()

        // Verify Play button is NOT displayed
        composeTestRule.onNodeWithContentDescription("Play")
            .assertDoesNotExist()
    }

    @Test
    fun playbackControls_stopButton_disabledWhenNotPlaying() {
        val state = createPlaybackControlState(isPlaying = false)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify Stop button is disabled
        composeTestRule.onNodeWithText("Stop")
            .assertIsNotEnabled()
    }

    @Test
    fun playbackControls_stopButton_enabledWhenPlaying() {
        val state = createPlaybackControlState(isPlaying = true)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify Stop button is enabled
        composeTestRule.onNodeWithText("Stop")
            .assertIsEnabled()
    }

    @Test
    fun playbackControls_stopButton_triggersStopAction() {
        var stopCalled = false
        val state = createPlaybackControlState(isPlaying = true)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = { action ->
                    if (action == HomeAction.StopPlayback) {
                        stopCalled = true
                    }
                }
            )
        }

        // Click stop button
        composeTestRule.onNodeWithText("Stop")
            .performClick()

        // Verify action was triggered
        assert(stopCalled)
    }

    @Test
    fun playbackControls_bpmSlider_updatesValue() {
        var updatedBpm: Int? = null
        val state = createPlaybackControlState(bpm = 120)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = { action ->
                    if (action is HomeAction.UpdateBpm) {
                        updatedBpm = action.bpm
                    }
                }
            )
        }

        // Click on BPM preset
        composeTestRule.onNodeWithText("100")
            .performClick()

        // Verify BPM was updated
        assert(updatedBpm == 100)
    }

    @Test
    fun playbackControls_loopToggle_showsCorrectState() {
        val state = createPlaybackControlState(loopEnabled = false)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify Loop Off is shown
        composeTestRule.onNodeWithText("Loop Off")
            .assertIsDisplayed()
    }

    @Test
    fun playbackControls_loopToggle_triggersAction() {
        var toggleLoopCalled = false
        val state = createPlaybackControlState(loopEnabled = false)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = { action ->
                    if (action == HomeAction.ToggleLoop) {
                        toggleLoopCalled = true
                    }
                }
            )
        }

        // Click loop toggle
        composeTestRule.onNodeWithText("Loop Off")
            .performClick()

        // Verify action was triggered
        assert(toggleLoopCalled)
    }

    @Test
    fun playbackControls_toneSelector_showsCurrentTone() {
        val state = createPlaybackControlState()
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify tone button shows current selection
        composeTestRule.onNodeWithText("Pure")
            .assertIsDisplayed()
    }

    @Test
    fun playbackControls_modeSelector_showsCurrentMode() {
        val state = createPlaybackControlState()
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify mode button shows current selection
        composeTestRule.onNodeWithText("Ascending")
            .assertIsDisplayed()
    }

    @Test
    fun playbackControls_chordOptions_shownWhenIsChord() {
        val state = createPlaybackControlState(isChord = true)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify chord-specific options are shown
        composeTestRule.onNodeWithText("Block")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Arpeggio")
            .assertIsDisplayed()
    }

    @Test
    fun playbackControls_chordOptions_hiddenWhenNotChord() {
        val state = createPlaybackControlState(isChord = false)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify chord-specific options are NOT shown
        composeTestRule.onNodeWithText("Block")
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("Arpeggio")
            .assertDoesNotExist()
    }

    @Test
    fun playbackControls_chordBlockToggle_triggersAction() {
        var toggleBlockCalled = false
        val state = createPlaybackControlState(isChord = true, chordBlockEnabled = false)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = { action ->
                    if (action == HomeAction.ToggleChordBlock) {
                        toggleBlockCalled = true
                    }
                }
            )
        }

        // Click block toggle
        composeTestRule.onNodeWithText("Block")
            .performClick()

        // Verify action was triggered
        assert(toggleBlockCalled)
    }

    @Test
    fun playbackControls_soundModeToggle_showsCorrectMode() {
        val state = createPlaybackControlState(soundMode = PlaybackSoundMode.PITCH)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify sound mode is displayed
        composeTestRule.onNodeWithText("Pitch")
            .assertIsDisplayed()
    }

    @Test
    fun playbackControls_bpmDescription_isDisplayed() {
        val state = createPlaybackControlState(bpm = 120)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify BPM description is shown
        composeTestRule.onNodeWithText("Walking Pace (Moderato)")
            .assertIsDisplayed()
    }

    @Test
    fun playbackControls_slowBpm_showsSlowDescription() {
        val state = createPlaybackControlState(bpm = 50)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify slow BPM description is shown
        composeTestRule.onNodeWithText("Very Slow (Largo)")
            .assertIsDisplayed()
    }

    @Test
    fun playbackControls_fastBpm_showsFastDescription() {
        val state = createPlaybackControlState(bpm = 180)
        
        composeTestRule.setContent {
            PlaybackControlsSection(
                state = state,
                onAction = {}
            )
        }

        // Verify fast BPM description is shown
        composeTestRule.onNodeWithText("Fast (Allegro)")
            .assertIsDisplayed()
    }
}
