package com.xiyue.app.playback

enum class TonePreset(
    val label: String,
    val shortLabel: String,
) {
    WARM_PRACTICE(
        label = "Warm Practice",
        shortLabel = "Warm",
    ),
    SOFT_PIANO(
        label = "Soft Piano",
        shortLabel = "Piano",
    ),
    CLEAR_WOOD(
        label = "Clear Wood",
        shortLabel = "Wood",
    ),
}

enum class PlaybackSoundMode(
    val label: String,
) {
    PITCH("Pitch"),
    SOLFEGE("Solfege"),
}
