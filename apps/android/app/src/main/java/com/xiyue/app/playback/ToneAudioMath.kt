package com.xiyue.app.playback

import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sin

internal object ToneAudioMath {
    fun softClip(value: Double): Double = (2.0 / PI) * atan(value * 1.35)

    fun sinWave(frequency: Double, timeSeconds: Double): Double =
        sin(2 * PI * frequency * timeSeconds)

    fun midiToFrequency(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

    fun toPcm16(value: Double): Short = (value * Short.MAX_VALUE)
        .toInt()
        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        .toShort()
}

internal fun AudioTrack.releaseQuietly() {
    runCatching {
        pause()
        flush()
        stop()
    }
    runCatching {
        release()
    }
}
