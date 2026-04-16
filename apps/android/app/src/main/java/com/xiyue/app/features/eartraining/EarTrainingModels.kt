package com.xiyue.app.features.eartraining

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PracticeLibraryItem

enum class EarTrainingMode { SCALE, CHORD }

enum class EarTrainingStage { IDLE, PLAYING, ANSWERING, RESULT, FINISHED }

data class EarTrainingState(
    val mode: EarTrainingMode = EarTrainingMode.SCALE,
    val stage: EarTrainingStage = EarTrainingStage.IDLE,
    val currentItem: PracticeLibraryItem? = null,
    val currentRoot: PitchClass = PitchClass.C,
    val options: List<OptionItem> = emptyList(),
    val correctCount: Int = 0,
    val totalAnswered: Int = 0,
    val streak: Int = 0,
    val message: String = "",
    val roundCount: Int = 0,
    val maxRounds: Int = 10,
) {
    val accuracyText: String
        get() = if (totalAnswered > 0) "${(correctCount * 100 / totalAnswered)}%" else "--"
}

data class OptionItem(
    val item: PracticeLibraryItem,
    val selected: Boolean = false,
)

sealed interface EarTrainingAction {
    data object StartSession : EarTrainingAction
    data class SelectMode(val mode: EarTrainingMode) : EarTrainingAction
    data object NextQuestion : EarTrainingAction
    data class SelectAnswer(val item: PracticeLibraryItem) : EarTrainingAction
    data object PlaybackStarted : EarTrainingAction
    data object PlaybackFinished : EarTrainingAction
    data object ReplayQuestion : EarTrainingAction
    data object FinishSession : EarTrainingAction
    data object Reset : EarTrainingAction
}
