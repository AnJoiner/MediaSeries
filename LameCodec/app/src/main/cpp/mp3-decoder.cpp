//
// Created by anjoiner on 20-1-20.
//

#include <jni.h>
#include <string>
#include "android/log.h"
#include "libmp3lame/lame.h"
#include "libmp3lame/mpglib_interface.c"
#include "mp3-decoder.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "mp3-decoder", __VA_ARGS__)

#define BUFFER_SIZE 1024 * 256

extern "C" JNIEXPORT jint JNICALL
Java_com_coder_media_Mp3Decoder_init(JNIEnv *env, jobject obj, jstring mp3PathParam, jstring pcmPathParam) {
    int ret = -1;
    const char *mp3Path = env->GetStringUTFChars(mp3PathParam, NULL);
    const char *pcmPath = env->GetStringUTFChars(pcmPathParam, NULL);

    mp3File = fopen(mp3Path, "rb");
    if (mp3File) {
        pcmFile = fopen(pcmPath, "wb");
        if (pcmFile) {
            hipClient = hip_decode_init();
            ret = 0;
        }
    }
    env->ReleaseStringUTFChars(pcmPathParam, pcmPath);
    env->ReleaseStringUTFChars(mp3PathParam, mp3Path);
    return ret;
}

extern "C" JNIEXPORT void JNICALL
Java_com_coder_media_Mp3Decoder_decode(JNIEnv *env, jobject obj) {
    short *buffer = new short[BUFFER_SIZE / 2];
    short *leftBuffer = new short[BUFFER_SIZE / 4];
    short *rightBuffer = new short[BUFFER_SIZE / 4];

    unsigned char *mp3_buffer = new unsigned char[BUFFER_SIZE];
    size_t readBufferSize = 0;
    while ((readBufferSize = fread(buffer, 2, BUFFER_SIZE / 2, mp3File)) > 0) {
        for (int i = 0; i < readBufferSize; i++) {
            if (i % 2 == 0) {
                leftBuffer[i / 2] = buffer[i];
            } else {
                rightBuffer[i / 2] = buffer[i];
            }
        }
        size_t wroteSize = hip_decode(hipClient, mp3_buffer, readBufferSize / 2, leftBuffer,
                                      rightBuffer);

        fwrite(mp3_buffer,1,wroteSize,pcmFile);
    }

    delete[] buffer;
    delete[] leftBuffer;
    delete[] rightBuffer;
    delete[] mp3_buffer;
}


extern "C" JNIEXPORT void JNICALL
Java_com_coder_media_Mp3Decoder_destroy(JNIEnv *env, jobject obj) {
    if (mp3File) {
        fclose(mp3File);
    }
    if (pcmFile) {
        fclose(pcmFile);
        hip_decode_exit(hipClient);
    }

}