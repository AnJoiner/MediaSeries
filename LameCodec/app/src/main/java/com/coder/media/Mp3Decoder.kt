package com.coder.media

/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-20
 */

class Mp3Decoder {
    companion object {
        init {
            System.loadLibrary("mp3decoder")
        }
    }

    external fun init(
        pcmPath: String,
        mp3Path: String
    ): Int

    external fun decode()

    external fun destroy()
}