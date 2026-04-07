package com.xiyue.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.xiyue.app.domain.PitchClass
import org.junit.Rule
import org.junit.Test

class ComponentTests {
    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== AnimatedPlayButton Tests ====================

    @Test
    fun animatedPlayButton_showsPlayIcon_whenNotPlaying() {
        composeTestRule.setContent {
            AnimatedPlayButton(
                isPlaying = false,
                onClick = {}
            )
        }

        // Verify Play icon is displayed
        composeTestRule.onNodeWithContentDescription("Play")
            .assertIsDisplayed()
    }

    @Test
    fun animatedPlayButton_showsPauseIcon_whenPlaying() {
        composeTestRule.setContent {
            AnimatedPlayButton(
                isPlaying = true,
                onClick = {}
            )
        }

        // Verify Pause icon is displayed
        composeTestRule.onNodeWithContentDescription("Pause")
            .assertIsDisplayed()
    }

    @Test
    fun animatedPlayButton_click_triggersCallback() {
        var clicked = false
        
        composeTestRule.setContent {
            AnimatedPlayButton(
                isPlaying = false,
                onClick = { clicked = true }
            )
        }

        // Click the button
        composeTestRule.onNodeWithContentDescription("Play")
            .performClick()

        // Verify callback was triggered
        assert(clicked)
    }

    // ==================== CompactPlayButton Tests ====================

    @Test
    fun compactPlayButton_showsPlayIcon_whenNotPlaying() {
        composeTestRule.setContent {
            CompactPlayButton(
                isPlaying = false,
                onClick = {}
            )
        }

        // Verify Play icon is displayed
        composeTestRule.onNodeWithContentDescription("Play")
            .assertIsDisplayed()
    }

    @Test
    fun compactPlayButton_showsPauseIcon_whenPlaying() {
        composeTestRule.setContent {
            CompactPlayButton(
                isPlaying = true,
                onClick = {}
            )
        }

        // Verify Pause icon is displayed
        composeTestRule.onNodeWithContentDescription("Pause")
            .assertIsDisplayed()
    }

    @Test
    fun compactPlayButton_click_triggersCallback() {
        var clicked = false
        
        composeTestRule.setContent {
            CompactPlayButton(
                isPlaying = false,
                onClick = { clicked = true }
            )
        }

        // Click the button
        composeTestRule.onNodeWithContentDescription("Play")
            .performClick()

        // Verify callback was triggered
        assert(clicked)
    }

    // ==================== EnhancedBpmSlider Tests ====================

    @Test
    fun enhancedBpmSlider_displaysBpmLabel() {
        composeTestRule.setContent {
            EnhancedBpmSlider(
                value = 120,
                onValueChange = {}
            )
        }

        // Verify BPM label is displayed
        composeTestRule.onNodeWithContentDescription("BPM")
            .assertIsDisplayed()
    }

    @Test
    fun enhancedBpmSlider_displaysCurrentValue() {
        composeTestRule.setContent {
            EnhancedBpmSlider(
                value = 140,
                onValueChange = {}
            )
        }

        // Verify current BPM value is displayed
        composeTestRule.onNodeWithText("140")
            .assertIsDisplayed()
    }

    @Test
    fun enhancedBpmSlider_displaysAllPresets() {
        composeTestRule.setContent {
            EnhancedBpmSlider(
                value = 120,
                onValueChange = {}
            )
        }

        // Verify all preset values are displayed
        composeTestRule.onNodeWithText("60")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("80")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("100")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("120")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("140")
            .assertIsDisplayed()
    }

    @Test
    fun enhancedBpmSlider_presetClick_triggersCallback() {
        var selectedBpm: Int? = null
        
        composeTestRule.setContent {
            EnhancedBpmSlider(
                value = 120,
                onValueChange = { selectedBpm = it }
            )
        }

        // Click on a preset
        composeTestRule.onNodeWithText("80")
            .performClick()

        // Verify callback was triggered with correct value
        assert(selectedBpm == 80)
    }

    @Test
    fun enhancedBpmSlider_showsBpmDescription() {
        composeTestRule.setContent {
            EnhancedBpmSlider(
                value = 100,
                onValueChange = {}
            )
        }

        // Verify BPM description is shown
        composeTestRule.onNodeWithText("Moderate (Andante)", substring = true)
            .assertIsDisplayed()
    }

    // ==================== CompactBpmSlider Tests ====================

    @Test
    fun compactBpmSlider_displaysBpmLabel() {
        composeTestRule.setContent {
            CompactBpmSlider(
                value = 120,
                onValueChange = {}
            )
        }

        // Verify BPM label is displayed
        composeTestRule.onNodeWithText("BPM")
            .assertIsDisplayed()
    }

    @Test
    fun compactBpmSlider_displaysCurrentValue() {
        composeTestRule.setContent {
            CompactBpmSlider(
                value = 140,
                onValueChange = {}
            )
        }

        // Verify current BPM value is displayed
        composeTestRule.onNodeWithText("140")
            .assertIsDisplayed()
    }

    // ==================== SwipeableRootNoteSelector Tests ====================

    @Test
    fun swipeableRootNoteSelector_displaysAllNotes() {
        composeTestRule.setContent {
            SwipeableRootNoteSelector(
                selectedRoot = PitchClass.C,
                onRootChange = {}
            )
        }

        // Verify all root notes are displayed
        composeTestRule.onNodeWithText("C")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("D")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("E")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("F")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("G")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("A")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("B")
            .assertIsDisplayed()
    }

    @Test
    fun swipeableRootNoteSelector_displaysSharpNotes() {
        composeTestRule.setContent {
            SwipeableRootNoteSelector(
                selectedRoot = PitchClass.C,
                onRootChange = {}
            )
        }

        // Verify sharp notes are displayed
        composeTestRule.onNodeWithText("C#")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("D#")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("F#")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("G#")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("A#")
            .assertIsDisplayed()
    }

    @Test
    fun swipeableRootNoteSelector_noteClick_triggersCallback() {
        var selectedNote: PitchClass? = null
        
        composeTestRule.setContent {
            SwipeableRootNoteSelector(
                selectedRoot = PitchClass.C,
                onRootChange = { selectedNote = it }
            )
        }

        // Click on a note
        composeTestRule.onNodeWithText("G")
            .performClick()

        // Verify callback was triggered with correct note
        assert(selectedNote == PitchClass.G)
    }

    @Test
    fun swipeableRootNoteSelector_sharpNoteClick_triggersCallback() {
        var selectedNote: PitchClass? = null
        
        composeTestRule.setContent {
            SwipeableRootNoteSelector(
                selectedRoot = PitchClass.C,
                onRootChange = { selectedNote = it }
            )
        }

        // Click on a sharp note
        composeTestRule.onNodeWithText("F#")
            .performClick()

        // Verify callback was triggered with correct note
        assert(selectedNote == PitchClass.F_SHARP)
    }

    // ==================== CompactRootNoteSelector Tests ====================

    @Test
    fun compactRootNoteSelector_displaysNotes() {
        composeTestRule.setContent {
            CompactRootNoteSelector(
                selectedRoot = PitchClass.C,
                onRootChange = {}
            )
        }

        // Verify notes are displayed
        composeTestRule.onNodeWithText("C")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("G")
            .assertIsDisplayed()
    }

    @Test
    fun compactRootNoteSelector_noteClick_triggersCallback() {
        var selectedNote: PitchClass? = null
        
        composeTestRule.setContent {
            CompactRootNoteSelector(
                selectedRoot = PitchClass.C,
                onRootChange = { selectedNote = it }
            )
        }

        // Click on a note
        composeTestRule.onNodeWithText("A")
            .performClick()

        // Verify callback was triggered
        assert(selectedNote == PitchClass.A)
    }

    // ==================== AnimatedPlayPauseIcon Tests ====================

    @Test
    fun animatedPlayPauseIcon_showsPlay_whenNotPlaying() {
        composeTestRule.setContent {
            AnimatedPlayPauseIcon(
                isPlaying = false
            )
        }

        // Verify Play content description
        composeTestRule.onNodeWithContentDescription("Play")
            .assertIsDisplayed()
    }

    @Test
    fun animatedPlayPauseIcon_showsPause_whenPlaying() {
        composeTestRule.setContent {
            AnimatedPlayPauseIcon(
                isPlaying = true
            )
        }

        // Verify Pause content description
        composeTestRule.onNodeWithContentDescription("Pause")
            .assertIsDisplayed()
    }

    // ==================== AnimatedIcon Tests ====================

    @Test
    fun animatedIcon_displaysCorrectly() {
        composeTestRule.setContent {
            AnimatedIcon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                isActive = false
            )
        }

        // Verify icon is displayed
        composeTestRule.onNodeWithContentDescription("Favorite")
            .assertIsDisplayed()
    }

    @Test
    fun animatedIcon_showsActiveState() {
        composeTestRule.setContent {
            AnimatedIcon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                isActive = true
            )
        }

        // Verify icon is displayed
        composeTestRule.onNodeWithContentDescription("Favorite")
            .assertIsDisplayed()
    }

    // ==================== PulsingIcon Tests ====================

    @Test
    fun pulsingIcon_displaysCorrectly() {
        composeTestRule.setContent {
            PulsingIcon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Pulse Favorite",
                isPulsing = false
            )
        }

        // Verify icon is displayed
        composeTestRule.onNodeWithContentDescription("Pulse Favorite")
            .assertIsDisplayed()
    }

    @Test
    fun pulsingIcon_showsPulsingState() {
        composeTestRule.setContent {
            PulsingIcon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Pulsing",
                isPulsing = true
            )
        }

        // Verify icon is displayed
        composeTestRule.onNodeWithContentDescription("Pulsing")
            .assertIsDisplayed()
    }
}
