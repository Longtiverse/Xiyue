package com.xiyue.app.playback

import android.os.Build

class AaudioPlayer(
    sampleRate: Int,
    channelCount: Int = 2,
) {
    private var nativeHandle: Long = 0L

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nativeHandle = try {
                nativeCreate(sampleRate, channelCount)
            } catch (_: UnsatisfiedLinkError) {
                0L
            }
        }
    }

    val isSupported: Boolean
        get() = nativeHandle != 0L

    fun write(pcm: ShortArray, offset: Int = 0, length: Int = pcm.size): Int {
        if (nativeHandle == 0L) return 0
        return nativeWrite(nativeHandle, pcm, offset, length)
    }

    fun start() {
        if (nativeHandle != 0L) nativeStart(nativeHandle)
    }

    fun stop() {
        if (nativeHandle != 0L) nativeStop(nativeHandle)
    }

    fun release() {
        if (nativeHandle != 0L) {
            nativeDestroy(nativeHandle)
            nativeHandle = 0L
        }
    }

    private external fun nativeCreate(sampleRate: Int, channelCount: Int): Long
    private external fun nativeDestroy(handle: Long)
    private external fun nativeWrite(handle: Long, pcm: ShortArray, offset: Int, length: Int): Int
    private external fun nativeStart(handle: Long)
    private external fun nativeStop(handle: Long)

    companion object {
        init {
            try {
                System.loadLibrary("aaudio-bridge")
            } catch (_: UnsatisfiedLinkError) {
                // ignore: isSupported will be false
            }
        }
    }
}
