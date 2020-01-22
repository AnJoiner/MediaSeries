package com.coder.media

/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-15
 */
class Mp3Encoder {

    companion object {
        init {
            System.loadLibrary("mp3encoder")
        }
    }

    external fun init(
        pcmPath: String,
        channel: Int,
        bitRate: Int,
        sampleRate: Int,
        mp3Path: String
    ): Int

    external fun encode()

    external fun destroy()
}