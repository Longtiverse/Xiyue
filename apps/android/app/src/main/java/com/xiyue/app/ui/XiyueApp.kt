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
    val stateFactory = remember { HomeStateFactory() }
    val reducer = remember { HomeReducer(stateFactory) }
    var state by remember {
        mutableStateOf(
            stateFactory.create(
                selectedLibraryItemId = initialPreferences.selectedLibraryItemId,
                favoriteLibraryItemIds = initialPreferences.favoriteLibraryItemIds,
                recentLibraryItemIds = initialPreferences.recentLibraryItemIds,
                selectedRoot = initialPreferences.selectedRoot,
                selectedPlaybackMode = initialPreferences.selectedPlaybackMode,
                loopEnabled = initialPreferences.loopEnabled,
                bpm = initialPreferences.bpm,
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
        state.bpm,
        state.loopEnabled,
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
                bpm = state.bpm,
                loopEnabled = state.loopEnabled,
                displayMode = state.displayMode,
            ),
        )
    }

    HomeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                HomeAction.TogglePlayback -> {
                    state.selectedLibraryItemId?.let { selectedItemId ->
                        if (state.isPlaying) {
                            PracticePlaybackService.stop(context)
                        } else {
                            PracticePlaybackService.play(
                                context = context,
                                request = PlaybackRequest(
                                    itemId = selectedItemId,
                                    root = state.selectedRoot,
                                    bpm = state.bpm,
                                    loopEnabled = state.loopEnabled,
                                    playbackMode = state.selectedPlaybackMode,
                                ),
                            )
                        }
                        state = reducer.reduce(state, action)
                    }
                }

                else -> {
                    state = reducer.reduce(state, action)
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun XiyueAppPreview() {
    XiyueTheme {
        XiyueApp()
    }
}
