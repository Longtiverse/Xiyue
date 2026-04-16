package com.xiyue.app.playback

enum class TonePreset(
    val label: String,
    val shortLabel: String,
) {
    PIANO(label = "Piano", shortLabel = "Piano"),
    PAD(label = "Pad", shortLabel = "Pad"),
    PLUCK(label = "Pluck", shortLabel = "Pluck"),
    VOCAL(label = "Vocal", shortLabel = "Vocal"),
}

enum class PlaybackSoundMode(
    val label: String,
) {
    PITCH("Pitch"),
    SOLFEGE("Solfege"),
}
