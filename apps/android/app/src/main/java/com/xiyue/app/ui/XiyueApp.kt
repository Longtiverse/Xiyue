package com.xiyue.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.xiyue.app.features.combo.ComboScreen
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PracticeLibraryItem
import com.xiyue.app.features.eartraining.EarTrainingAction
import com.xiyue.app.features.eartraining.EarTrainingMode
import com.xiyue.app.features.eartraining.EarTrainingReducer
import com.xiyue.app.features.eartraining.EarTrainingScreen
import com.xiyue.app.features.eartraining.EarTrainingStage
import com.xiyue.app.features.eartraining.EarTrainingState
import com.xiyue.app.features.eartraining.OptionItem
import com.xiyue.app.features.favorites.FavoritesScreen
import com.xiyue.app.features.home.HomeAction
import com.xiyue.app.features.home.HomePreferencesRepository
import com.xiyue.app.features.home.HomePreferencesState
import com.xiyue.app.features.home.HomeReducer
import com.xiyue.app.features.home.HomeScreen
import com.xiyue.app.features.home.HomeStateFactory
import com.xiyue.app.features.home.HomeUiState
import com.xiyue.app.features.settings.SettingsScreen
import com.xiyue.app.features.settings.ThemeMode
import com.xiyue.app.navigation.BottomNavItem
import com.xiyue.app.navigation.XiyueBottomNavigation
import com.xiyue.app.persistence.SettingsRepository
import com.xiyue.app.data.AnalyticsRepository
import com.xiyue.app.playback.PlaybackRequest
import com.xiyue.app.playback.PracticePlaybackService
import com.xiyue.app.ui.theme.XiyueTheme

@Composable
fun XiyueApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext

    val settingsRepository = remember(appContext) { SettingsRepository(appContext) }
    var themeMode by remember { mutableStateOf(settingsRepository.getThemeMode()) }
    var showHints by remember { mutableStateOf(settingsRepository.getShowHints()) }
    val analyticsRepository = remember(appContext) { AnalyticsRepository(appContext) }
    LaunchedEffect(Unit) {
        analyticsRepository.recordAppOpen()
    }

    val preferencesRepository = remember(appContext) { HomePreferencesRepository(appContext) }
    val initialPreferences = remember(preferencesRepository) { preferencesRepository.load() }
    val stateFactory = remember(appContext) { HomeStateFactory() }
    val reducer = remember(stateFactory) { HomeReducer(stateFactory) }

    val allLibraryItems = remember(appContext) {
        com.xiyue.app.domain.InMemoryPracticeLibraryRepository(appContext).getLibraryItems()
    }
    val earTrainingReducer = remember(allLibraryItems) { EarTrainingReducer(allLibraryItems) }
    var earTrainingState by rememberSaveable(stateSaver = earTrainingStateSaver(allLibraryItems)) {
        mutableStateOf(EarTrainingState())
    }

    val hasValidProgress = initialPreferences.practiceProgressTimestamp > 0L
    val initialItemId = if (hasValidProgress) initialPreferences.practiceProgressItemId else initialPreferences.selectedLibraryItemId
    val initialRoot = if (hasValidProgress) initialPreferences.practiceProgressRoot ?: initialPreferences.selectedRoot else initialPreferences.selectedRoot
    var pendingResumeStepIndex by rememberSaveable { mutableStateOf(if (hasValidProgress) initialPreferences.practiceProgressStepIndex else null) }
    var hasAttemptedResume by rememberSaveable { mutableStateOf(false) }

    var state by remember {
        mutableStateOf(
            stateFactory.create(
                selectedLibraryItemId = initialItemId,
                favoriteLibraryItemIds = initialPreferences.favoriteLibraryItemIds,
                recentLibraryItemIds = initialPreferences.recentLibraryItemIds,
                selectedRoot = initialRoot,
                selectedPlaybackMode = initialPreferences.selectedPlaybackMode,
                selectedTonePreset = initialPreferences.selectedTonePreset,
                soundMode = initialPreferences.soundMode,
                chordBlockEnabled = initialPreferences.chordBlockEnabled,
                chordArpeggioEnabled = initialPreferences.chordArpeggioEnabled,
                loopEnabled = initialPreferences.loopEnabled,
                loopDurationMs = initialPreferences.loopDurationMs,
                bpm = initialPreferences.bpm,
                displayMode = initialPreferences.displayMode,
                selectedInversion = initialPreferences.selectedInversion,
                selectedOctave = initialPreferences.selectedOctave,
                selectedRhythmPattern = initialPreferences.selectedRhythmPattern,
                durationMultiplier = initialPreferences.durationMultiplier,
                showHints = showHints,
            ),
        )
    }

    val playbackSnapshot by PracticePlaybackService.state.collectAsState()

    LaunchedEffect(playbackSnapshot) {
        state = reducer.reduce(state, HomeAction.SyncPlaybackSnapshot(playbackSnapshot))

        pendingResumeStepIndex?.let { stepIndex ->
            if (playbackSnapshot.stepCount > 0 && playbackSnapshot.currentItemId != null) {
                val target = stepIndex.coerceIn(0, playbackSnapshot.stepCount - 1)
                if (target == playbackSnapshot.stepIndex) {
                    pendingResumeStepIndex = null
                    hasAttemptedResume = true
                } else if (!hasAttemptedResume) {
                    PracticePlaybackService.seekToStep(context, target)
                    hasAttemptedResume = true
                }
            }
        }
    }

    var isFirstTone by remember { mutableStateOf(true) }
    LaunchedEffect(state.selectedTonePreset) {
        if (!isFirstTone) {
            analyticsRepository.recordToneChange()
        }
        isFirstTone = false
    }

    val savePreferences = {
        preferencesRepository.save(
            HomePreferencesState(
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = state.favoriteLibraryItems.map { it.id },
                recentLibraryItemIds = state.recentLibraryItems.map { it.id },
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                selectedTonePreset = state.selectedTonePreset,
                soundMode = state.soundMode,
                bpm = state.bpm,
                loopEnabled = state.loopEnabled,
                loopDurationMs = state.loopDurationMs,
                chordBlockEnabled = state.chordBlockEnabled,
                chordArpeggioEnabled = state.chordArpeggioEnabled,
                displayMode = state.displayMode,
                selectedInversion = state.selectedInversion,
                selectedOctave = state.selectedOctave,
                selectedDifficultyLabel = state.selectedDifficultyLabel,
                selectedRhythmPattern = state.selectedRhythmPattern,
                durationMultiplier = state.durationMultiplier,
                practiceProgressItemId = playbackSnapshot.currentItemId ?: state.selectedLibraryItemId,
                practiceProgressRoot = state.selectedRoot,
                practiceProgressStepIndex = playbackSnapshot.stepIndex,
                practiceProgressTimestamp = System.currentTimeMillis(),
            ),
        )
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
        state.selectedInversion,
        state.selectedOctave,
        state.selectedDifficultyLabel,
        state.selectedRhythmPattern,
        state.durationMultiplier,
        state.recentLibraryItems,
    ) {
        savePreferences()
    }

    LaunchedEffect(Unit) {
        flow {
            while (true) {
                emit(playbackSnapshot.stepIndex to playbackSnapshot.currentItemId)
                delay(2000)
            }
        }
            .distinctUntilChanged()
            .debounce(3000)
            .collect { savePreferences() }
    }

    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    XiyueTheme(darkTheme = isDarkTheme, dynamicColor = false) {
        MainScreen(
            state = state,
            onHomeAction = { action ->
                when (action) {
                    HomeAction.TogglePlayback -> {
                        val request = createPlaybackRequest(state)
                        if (request != null) {
                            if (state.isPlaying) {
                                PracticePlaybackService.pause(context)
                            } else if (state.isPaused) {
                                PracticePlaybackService.resume(context)
                            } else {
                                PracticePlaybackService.play(
                                    context = context,
                                    request = request,
                                )
                            }
                        }
                    }

                    HomeAction.StopPlayback -> {
                        PracticePlaybackService.stop(context)
                    }

                    is HomeAction.SeekToStep -> {
                        PracticePlaybackService.seekToStep(context, action.stepIndex)
                    }

                    else -> {
                        val previousState = state
                        val nextState = reducer.reduce(state, action)
                        state = nextState

                        if (action is HomeAction.SelectLibraryItem && !previousState.isPlaying && !previousState.isPaused) {
                            createPlaybackRequest(nextState)?.copy(loopDurationMs = 3000L)?.let { previewRequest ->
                                PracticePlaybackService.play(context, previewRequest)
                            }
                        } else if (shouldPreparePausedPlayback(previousState, nextState, action)) {
                            preparePausedPlayback(context, nextState)
                        } else if (shouldRefreshPlayback(previousState, nextState, action)) {
                            refreshPlayback(context, nextState)
                        }
                    }
                }
            },
            onToggleFavorite = { itemId ->
                state = reducer.reduce(state, HomeAction.ToggleFavoriteLibraryItem(itemId))
            },
            onSelectItemFromFavorites = { itemId ->
                state = reducer.reduce(state, HomeAction.SelectLibraryItem(itemId))
            },
            themeMode = themeMode,
            onThemeModeChange = { newMode ->
                themeMode = newMode
                settingsRepository.setThemeMode(newMode)
            },
            showHints = showHints,
            onShowHintsChange = { nextShowHints ->
                showHints = nextShowHints
                settingsRepository.setShowHints(nextShowHints)
                state = reducer.reduce(state, HomeAction.UpdateHintsVisibility(nextShowHints))
            },
            earTrainingState = earTrainingState,
            onEarTrainingAction = { action ->
                if (action is com.xiyue.app.features.eartraining.EarTrainingAction.SelectAnswer) {
                    val correct = action.item.id == earTrainingState.currentItem?.id
                    analyticsRepository.recordEarTrainingAnswer(correct)
                }
                earTrainingState = earTrainingReducer.reduce(earTrainingState, action)
            },
            analyticsSummary = analyticsRepository.getSummary(),
        )
    }
}

@Composable
private fun MainScreen(
    state: HomeUiState,
    onHomeAction: (HomeAction) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSelectItemFromFavorites: (String) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    showHints: Boolean,
    onShowHintsChange: (Boolean) -> Unit,
    earTrainingState: com.xiyue.app.features.eartraining.EarTrainingState,
    onEarTrainingAction: (com.xiyue.app.features.eartraining.EarTrainingAction) -> Unit,
    analyticsSummary: com.xiyue.app.data.AnalyticsSummary,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavItem.PRACTICE) }
    var previousTab by rememberSaveable { mutableStateOf<BottomNavItem?>(null) }

    LaunchedEffect(selectedTab) {
        val prev = previousTab
        previousTab = selectedTab
        if (prev == null) return@LaunchedEffect

        when {
            prev == BottomNavItem.PRACTICE && selectedTab == BottomNavItem.EAR_TRAINING -> {
                PracticePlaybackService.stop(context)
            }
            prev == BottomNavItem.PRACTICE && (selectedTab == BottomNavItem.FAVORITES || selectedTab == BottomNavItem.SETTINGS) -> {
                PracticePlaybackService.pause(context)
            }
            prev == BottomNavItem.PRACTICE && selectedTab == BottomNavItem.COMBO -> {
                PracticePlaybackService.stop(context)
            }
            prev == BottomNavItem.EAR_TRAINING && selectedTab != BottomNavItem.EAR_TRAINING -> {
                PracticePlaybackService.stop(context)
            }
            else -> { }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            XiyueBottomNavigation(
                selectedItem = selectedTab,
                onItemSelected = { selectedTab = it },
            )
        },
    ) { padding ->
        when (selectedTab) {
            BottomNavItem.PRACTICE -> {
                HomeScreen(
                    state = state,
                    onAction = onHomeAction,
                    modifier = modifier.padding(padding),
                )
            }

            BottomNavItem.EAR_TRAINING -> {
                com.xiyue.app.features.eartraining.EarTrainingScreen(
                    state = earTrainingState,
                    onAction = onEarTrainingAction,
                    onBack = { selectedTab = BottomNavItem.PRACTICE },
                    modifier = modifier.padding(padding),
                )
            }

            BottomNavItem.COMBO -> {
                ComboScreen(
                    modifier = modifier.padding(padding),
                )
            }

            BottomNavItem.FAVORITES -> {
                FavoritesScreen(
                    favorites = state.favoriteLibraryItems,
                    onToggleFavorite = onToggleFavorite,
                    onSelectItem = { itemId ->
                        onSelectItemFromFavorites(itemId)
                        selectedTab = BottomNavItem.PRACTICE
                    },
                    onBrowseLibrary = { selectedTab = BottomNavItem.PRACTICE },
                    modifier = modifier.padding(padding),
                )
            }

            BottomNavItem.SETTINGS -> {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    showHints = showHints,
                    onShowHintsChange = onShowHintsChange,
                    analyticsSummary = analyticsSummary,
                    modifier = modifier.padding(padding),
                )
            }
        }
    }
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
            inversion = state.selectedInversion,
            octave = state.selectedOctave,
            rhythmPattern = state.selectedRhythmPattern,
            durationMultiplier = state.durationMultiplier,
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
        is HomeAction.UpdateChordPlaybackMode,
        is HomeAction.UpdateTonePreset,
        is HomeAction.UpdateSoundMode,
        is HomeAction.UpdateBpm,
        is HomeAction.UpdateLoopDuration,
        is HomeAction.UpdateInversion,
        is HomeAction.UpdateOctave,
        is HomeAction.SelectDifficulty,
        is HomeAction.UpdateRhythmPattern,
        is HomeAction.UpdateDurationMultiplier,
        HomeAction.ToggleLoop,
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
        is HomeAction.UpdateChordPlaybackMode,
        is HomeAction.UpdateTonePreset,
        is HomeAction.UpdateSoundMode,
        is HomeAction.UpdateBpm,
        is HomeAction.UpdateLoopDuration,
        is HomeAction.UpdateInversion,
        is HomeAction.UpdateOctave,
        is HomeAction.SelectDifficulty,
        is HomeAction.UpdateRhythmPattern,
        is HomeAction.UpdateDurationMultiplier,
        HomeAction.ToggleLoop,
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
    previousState.chordArpeggioEnabled != nextState.chordArpeggioEnabled ||
    previousState.selectedInversion != nextState.selectedInversion ||
    previousState.selectedOctave != nextState.selectedOctave ||
    previousState.selectedDifficultyLabel != nextState.selectedDifficultyLabel ||
    previousState.selectedRhythmPattern != nextState.selectedRhythmPattern ||
    previousState.durationMultiplier != nextState.durationMultiplier

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

private fun earTrainingStateSaver(
    allItems: List<PracticeLibraryItem>,
): Saver<EarTrainingState, Any> = mapSaver(
    save = { state ->
        mapOf(
            "mode" to state.mode.name,
            "stage" to state.stage.name,
            "currentItemId" to (state.currentItem?.id ?: ""),
            "currentRoot" to state.currentRoot.name,
            "options" to state.options.map { listOf(it.item.id, it.selected) },
            "correctCount" to state.correctCount,
            "totalAnswered" to state.totalAnswered,
            "streak" to state.streak,
            "message" to state.message,
            "roundCount" to state.roundCount,
            "maxRounds" to state.maxRounds,
        )
    },
    restore = { map ->
        val idsToItems = allItems.associateBy { it.id }
        val currentItemId = map["currentItemId"] as? String
        val currentItem = if (!currentItemId.isNullOrBlank()) idsToItems[currentItemId] else null
        val optionsRaw = map["options"] as? List<*>
        val options = optionsRaw?.map { raw ->
            val list = raw as List<*>
            OptionItem(
                item = idsToItems[list[0] as String] ?: allItems.firstOrNull { it.id == list[0] } ?: allItems.first(),
                selected = list[1] as Boolean,
            )
        } ?: emptyList()
        EarTrainingState(
            mode = runCatching { EarTrainingMode.valueOf(map["mode"] as String) }.getOrNull() ?: EarTrainingMode.SCALE,
            stage = runCatching { EarTrainingStage.valueOf(map["stage"] as String) }.getOrNull() ?: EarTrainingStage.IDLE,
            currentItem = currentItem,
            currentRoot = runCatching { PitchClass.valueOf(map["currentRoot"] as String) }.getOrNull() ?: PitchClass.C,
            options = options,
            correctCount = map["correctCount"] as? Int ?: 0,
            totalAnswered = map["totalAnswered"] as? Int ?: 0,
            streak = map["streak"] as? Int ?: 0,
            message = map["message"] as? String ?: "",
            roundCount = map["roundCount"] as? Int ?: 0,
            maxRounds = map["maxRounds"] as? Int ?: 10,
        )
    },
)

@Preview(showBackground = true)
@Composable
private fun XiyueAppPreview() {
    XiyueTheme {
        XiyueApp()
    }
}
