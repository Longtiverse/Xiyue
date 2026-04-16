#include <aaudio/AAudio.h>
#include <android/log.h>
#include <jni.h>
#include <cstring>

#define LOG_TAG "AAudioBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_xiyue_app_playback_AaudioPlayer_nativeCreate(
        JNIEnv *env,
        jclass clazz,
        jint sampleRate,
        jint channelCount) {
    AAudioStreamBuilder *builder = nullptr;
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK || builder == nullptr) {
        LOGE("Failed to create AAudio stream builder: %s", AAudio_convertResultToText(result));
        return 0;
    }

    AAudioStreamBuilder_setDirection(builder, AAUDIO_DIRECTION_OUTPUT);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setSharingMode(builder, AAUDIO_SHARING_MODE_SHARED);
    AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_I16);
    AAudioStreamBuilder_setSampleRate(builder, sampleRate);
    AAudioStreamBuilder_setChannelCount(builder, channelCount);

    AAudioStream *stream = nullptr;
    result = AAudioStreamBuilder_openStream(builder, &stream);
    AAudioStreamBuilder_delete(builder);
    if (result != AAUDIO_OK || stream == nullptr) {
        LOGE("Failed to open AAudio stream: %s", AAudio_convertResultToText(result));
        return 0;
    }

    LOGI("AAudio stream created: sampleRate=%d channels=%d", sampleRate, channelCount);
    return reinterpret_cast<jlong>(stream);
}

JNIEXPORT void JNICALL
Java_com_xiyue_app_playback_AaudioPlayer_nativeDestroy(
        JNIEnv *env,
        jclass clazz,
        jlong handle) {
    if (handle == 0) return;
    auto *stream = reinterpret_cast<AAudioStream *>(handle);
    AAudioStream_requestStop(stream);
    AAudioStream_close(stream);
}

JNIEXPORT jint JNICALL
Java_com_xiyue_app_playback_AaudioPlayer_nativeWrite(
        JNIEnv *env,
        jclass clazz,
        jlong handle,
        jshortArray pcm,
        jint offset,
        jint length) {
    if (handle == 0) return 0;
    auto *stream = reinterpret_cast<AAudioStream *>(handle);
    jshort *data = env->GetShortArrayElements(pcm, nullptr);
    if (data == nullptr) return 0;
    auto written = AAudioStream_write(stream, data + offset, length, 0);
    env->ReleaseShortArrayElements(pcm, data, JNI_ABORT);
    return written;
}

JNIEXPORT void JNICALL
Java_com_xiyue_app_playback_AaudioPlayer_nativeStart(
        JNIEnv *env,
        jclass clazz,
        jlong handle) {
    if (handle == 0) return;
    AAudioStream_requestStart(reinterpret_cast<AAudioStream *>(handle));
}

JNIEXPORT void JNICALL
Java_com_xiyue_app_playback_AaudioPlayer_nativeStop(
        JNIEnv *env,
        jclass clazz,
        jlong handle) {
    if (handle == 0) return;
    AAudioStream_requestStop(reinterpret_cast<AAudioStream *>(handle));
}

} // extern "C"
