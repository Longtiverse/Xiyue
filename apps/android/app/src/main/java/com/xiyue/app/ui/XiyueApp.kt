package com.xiyue.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.xiyue.app.features.home.HomeAction
import com.xiyue.app.features.home.HomeUiState
import com.xiyue.app.features.home.HomePreferencesRepository
import com.xiyue.app.features.home.HomePreferencesState
import com.xiyue.app.features.home.HomeReducer
import com.xiyue.app.features.home.HomeScreen
import com.xiyue.app.features.home.HomeStateFactory
import com.xiyue.app.playback.PlaybackRequest
import com.xiyue.app.playback.PracticePlaybackService
import com.xiyue.app.ui.theme.XiyueTheme

@Composable
fun XiyueApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val preferencesRepository = remember(appContext) { HomePreferencesRepository(appContext) }
    val initialPreferences = remember(preferencesRepository) { preferencesRepository.load() }
    val stateFactory = remember(appContext) { HomeStateFactory() }
    val reducer = remember(stateFactory) { HomeReducer(stateFactory) }
    var state by remember {
        mutableStateOf(
            stateFactory.create(
                selectedLibraryItemId = initialPreferences.selectedLibraryItemId,
                favoriteLibraryItemIds = initialPreferences.favoriteLibraryItemIds,
                recentLibraryItemIds = initialPreferences.recentLibraryItemIds,
                selectedRoot = initialPreferences.selectedRoot,
                selectedPlaybackMode = initialPreferences.selectedPlaybackMode,
                selectedTonePreset = initialPreferences.selectedTonePreset,
                soundMode = initialPreferences.soundMode,
                chordBlockEnabled = initialPreferences.chordBlockEnabled,
                chordArpeggioEnabled = initialPreferences.chordArpeggioEnabled,
                loopEnabled = initialPreferences.loopEnabled,
                loopDurationMs = initialPreferences.loopDurationMs,
                bpm = initialPreferences.bpm,
                isBpmInputVisible = false,
                displayMode = initialPreferences.displayMode,
            ),
        )
    }
    val playbackSnapshot by PracticePlaybackService.state.collectAsState()

    LaunchedEffect(playbackSnapshot) {
        state = reducer.reduce(state, HomeAction.SyncPlaybackSnapshot(playbackSnapshot))
    }

    LaunchedEffect(
        state.selectedLibraryItemId,
        state.favoriteLibraryItems,
        state.selectedRoot,
        state.selectedPlaybackMode,
        state.selectedTonePreset,
        state.soundMode,
        state.chordBlockEnabled,
        state.chordArpeggioEnabled,
        state.bpm,
        state.loopEnabled,
        state.loopDurationMs,
        state.displayMode,
        state.recentLibraryItems,
    ) {
        preferencesRepository.save(
            HomePreferencesState(
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = state.favoriteLibraryItems.map { it.id },
                recentLibraryItemIds = state.recentLibraryItems.map { it.id },
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                selectedTonePreset = state.selectedTonePreset,
                soundMode = state.soundMode,
                chordBlockEnabled = state.chordBlockEnabled,
                chordArpeggioEnabled = state.chordArpeggioEnabled,
                bpm = state.bpm,
                loopEnabled = state.loopEnabled,
                loopDurationMs = state.loopDurationMs,
                displayMode = state.displayMode,
            ),
        )
    }

    HomeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                HomeAction.TogglePlayback -> {
                    createPlaybackRequest(state)?.let { request ->
                        when {
                            state.isPlaying -> PracticePlaybackService.pause(context)
                            state.isPaused -> PracticePlaybackService.resume(context)
                            else -> PracticePlaybackService.play(
                                context = context,
                                request = request,
                            )
                        }
                    }
                }

                HomeAction.StopPlayback -> {
                    PracticePlaybackService.stop(context)
                }

                else -> {
                    val previousState = state
                    val nextState = reducer.reduce(state, action)
                    state = nextState

                    if (shouldPreparePausedPlayback(previousState, nextState, action)) {
                        preparePausedPlayback(context, nextState)
                    } else if (shouldRefreshPlayback(previousState, nextState, action)) {
                        refreshPlayback(context, nextState)
                    }
                }
            }
        },
    )
}

private fun createPlaybackRequest(state: HomeUiState): PlaybackRequest? =
    state.selectedLibraryItemId?.let { selectedItemId ->
        PlaybackRequest(
            itemId = selectedItemId,
            root = state.selectedRoot,
            bpm = state.bpm,
            loopEnabled = state.loopEnabled,
            loopDurationMs = state.loopDurationMs,
            playbackMode = state.selectedPlaybackMode,
            tonePreset = state.selectedTonePreset,
            chordBlockEnabled = state.chordBlockEnabled,
            chordArpeggioEnabled = state.chordArpeggioEnabled,
            soundMode = state.soundMode,
        )
    }

private fun shouldRefreshPlayback(
    previousState: HomeUiState,
    nextState: HomeUiState,
    action: HomeAction,
): Boolean {
    if (!previousState.isPlaying) return false

    return when (action) {
        is HomeAction.SelectLibraryItem,
        is HomeAction.SelectRoot,
        is HomeAction.UpdatePlaybackMode,
        is HomeAction.UpdateTonePreset,
        is HomeAction.UpdateSoundMode,
        is HomeAction.UpdateBpm,
        is HomeAction.UpdateLoopDuration,
        HomeAction.ToggleLoop,
        HomeAction.ToggleChordBlock,
        HomeAction.ToggleChordArpeggio,
        -> playbackConfigChanged(previousState, nextState)

        else -> false
    }
}

private fun shouldPreparePausedPlayback(
    previousState: HomeUiState,
    nextState: HomeUiState,
    action: HomeAction,
): Boolean {
    if (!previousState.isPaused) return false

    return when (action) {
        is HomeAction.SelectLibraryItem,
        is HomeAction.SelectRoot,
        is HomeAction.UpdatePlaybackMode,
        is HomeAction.UpdateTonePreset,
        is HomeAction.UpdateSoundMode,
        is HomeAction.UpdateBpm,
        is HomeAction.UpdateLoopDuration,
        HomeAction.ToggleLoop,
        HomeAction.ToggleChordBlock,
        HomeAction.ToggleChordArpeggio,
        -> playbackConfigChanged(previousState, nextState)

        else -> false
    }
}

private fun playbackConfigChanged(
    previousState: HomeUiState,
    nextState: HomeUiState,
): Boolean = previousState.selectedLibraryItemId != nextState.selectedLibraryItemId ||
    previousState.selectedRoot != nextState.selectedRoot ||
    previousState.selectedPlaybackMode != nextState.selectedPlaybackMode ||
    previousState.selectedTonePreset != nextState.selectedTonePreset ||
    previousState.soundMode != nextState.soundMode ||
    previousState.bpm != nextState.bpm ||
    previousState.loopEnabled != nextState.loopEnabled ||
    previousState.loopDurationMs != nextState.loopDurationMs ||
    previousState.chordBlockEnabled != nextState.chordBlockEnabled ||
    previousState.chordArpeggioEnabled != nextState.chordArpeggioEnabled

private fun refreshPlayback(
    context: android.content.Context,
    state: HomeUiState,
) {
    val request = createPlaybackRequest(state) ?: return

    PracticePlaybackService.play(
        context = context,
        request = request,
    )
}

private fun preparePausedPlayback(
    context: android.content.Context,
    state: HomeUiState,
) {
    val request = createPlaybackRequest(state) ?: return

    PracticePlaybackService.preparePaused(context, request)
}

@Preview(showBackground = true)
@Composable
private fun XiyueAppPreview() {
    XiyueTheme {
        XiyueApp()
    }
}
