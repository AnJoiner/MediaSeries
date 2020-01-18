//
// Created by anjoiner on 20-1-15.
//

#include <jni.h>
#include <string>
#include "android/log.h"
#include "libmp3lame/lame.h"
#include "mp3-encoder.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "mp3-encoder", __VA_ARGS__)

extern "C" JNIEXPORT jint JNICALL
Java_com_coder_media_Mp3Encoder_init(JNIEnv *env, jobject obj, jstring pcmPathParam, jint channels,
                                     jint bitRate, jint sampleRate, jstring mp3PathParam) {
    LOGD("encoder init");
    int ret = -1;
    const char* pcmPath = env->GetStringUTFChars(pcmPathParam, NULL);
    const char* mp3Path = env->GetStringUTFChars(mp3PathParam, NULL);
    pcmFile = fopen(pcmPath,"rb");
    if (pcmFile){
        mp3File = fopen(mp3Path,"wb");
        if (mp3File){
            lameClient = lame_init();
            lame_set_in_samplerate(lameClient, sampleRate);
            lame_set_out_samplerate(lameClient,sampleRate);
            lame_set_num_channels(lameClient,channels);
            lame_set_brate(lameClient,bitRate);
            lame_init_params(lameClient);
            ret = 0;
        }
    }
    env->ReleaseStringUTFChars(mp3PathParam, mp3Path);
    env->ReleaseStringUTFChars(pcmPathParam, pcmPath);
    return ret;
}


extern "C" JNIEXPORT void JNICALL
Java_com_coder_media_Mp3Encoder_encode(JNIEnv *env, jobject obj) {
    LOGD("encoder encode");
    int bufferSize = 1024 * 256;
    short* buffer = new short[bufferSize / 2];
    short* leftBuffer = new short[bufferSize / 4];
    short* rightBuffer = new short[bufferSize / 4];

    unsigned char* mp3_buffer = new unsigned char[bufferSize];
    size_t readBufferSize = 0;

    while ((readBufferSize = fread(buffer, 2, bufferSize / 2, pcmFile)) > 0) {
        for (int i = 0; i < readBufferSize; i++) {
            if (i % 2 == 0) {
                leftBuffer[i / 2] = buffer[i];
            } else {
                rightBuffer[i / 2] = buffer[i];
            }
        }
        size_t wroteSize = lame_encode_buffer(lameClient, (short int *) leftBuffer, (short int *) rightBuffer,
                                              (int)(readBufferSize / 2), mp3_buffer, bufferSize);
        fwrite(mp3_buffer, 1, wroteSize, mp3File);
    }
    delete[] buffer;
    delete[] leftBuffer;
    delete[] rightBuffer;
    delete[] mp3_buffer;
}

extern "C" JNIEXPORT void JNICALL
Java_com_coder_media_Mp3Encoder_destroy(JNIEnv *env, jobject obj) {
    LOGD("encoder destroy");
    if(pcmFile) {
        fclose(pcmFile);
    }
    if(mp3File) {
        fclose(mp3File);
        lame_close(lameClient);
    }
}