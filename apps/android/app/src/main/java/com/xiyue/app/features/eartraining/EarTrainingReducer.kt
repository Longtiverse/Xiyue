package com.xiyue.app.features.eartraining

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PracticeLibraryItem

class EarTrainingReducer(
    private val allItems: List<PracticeLibraryItem>,
) {
    fun reduce(state: EarTrainingState, action: EarTrainingAction): EarTrainingState = when (action) {
        EarTrainingAction.StartSession -> {
            val base = state.copy(
                stage = EarTrainingStage.PLAYING,
                roundCount = 0,
                correctCount = 0,
                totalAnswered = 0,
                streak = 0,
                message = "",
            )
            generateQuestion(base)
        }

        is EarTrainingAction.SelectMode -> state.copy(mode = action.mode)

        EarTrainingAction.NextQuestion -> {
            if (state.roundCount >= state.maxRounds) {
                state.copy(stage = EarTrainingStage.FINISHED)
            } else {
                generateQuestion(state.copy(stage = EarTrainingStage.PLAYING, message = ""))
            }
        }

        is EarTrainingAction.SelectAnswer -> {
            val correct = action.item.id == state.currentItem?.id
            state.copy(
                stage = EarTrainingStage.RESULT,
                options = state.options.map { it.copy(selected = it.item.id == action.item.id) },
                correctCount = if (correct) state.correctCount + 1 else state.correctCount,
                totalAnswered = state.totalAnswered + 1,
                streak = if (correct) state.streak + 1 else 0,
                message = if (correct) "回答正确！" else "正确答案是 ${state.currentItem?.label}",
            )
        }

        EarTrainingAction.PlaybackStarted -> state.copy(stage = EarTrainingStage.PLAYING)

        EarTrainingAction.ReplayQuestion -> state.copy(stage = EarTrainingStage.PLAYING)

        EarTrainingAction.PlaybackFinished -> {
            if (state.stage == EarTrainingStage.PLAYING) {
                state.copy(stage = EarTrainingStage.ANSWERING)
            } else {
                state
            }
        }

        EarTrainingAction.FinishSession -> state.copy(stage = EarTrainingStage.FINISHED)

        EarTrainingAction.Reset -> EarTrainingState(mode = state.mode)
    }

    private fun generateQuestion(state: EarTrainingState): EarTrainingState {
        val pool = allItems.filter { item ->
            when (state.mode) {
                EarTrainingMode.SCALE -> item.id.startsWith("scale:")
                EarTrainingMode.CHORD -> item.id.startsWith("chord:")
            }
        }
        if (pool.size < 4) {
            return state.copy(stage = EarTrainingStage.FINISHED, message = "题库不足，无法开始")
        }
        val correct = pool.random()
        val distractors = pool.filter { it.id != correct.id }.shuffled().take(3)
        val options = (distractors + correct).shuffled().map { OptionItem(it) }
        val root = PitchClass.entries.random()
        return state.copy(
            currentItem = correct,
            currentRoot = root,
            options = options,
            roundCount = state.roundCount + 1,
            stage = EarTrainingStage.PLAYING,
        )
    }
}
