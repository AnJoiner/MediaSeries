package com.coder.media

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        // Example of a call to a native method
        sample_text.text = stringFromJNI()
    }

    private fun init() {
        initListener()
    }

    private fun initListener() {
        btnRecord.setOnClickListener {
            RecordActivity.start(this)
        }
        btnEncode.setOnClickListener {
            encodeAudio()
        }
        btnDecode.setOnClickListener {
//            decodeAudio()
        }

//        btnEncode2.setOnClickListener(View.OnClickListener {
//            mp3ToAAC()
//        })
    }

    /**
     * 将pcm转mp3
     */
    private fun encodeAudio() {
        var pcmPath = File(externalCacheDir, "record.pcm").absolutePath
        var target = File(externalCacheDir, "target.mp3").absolutePath
        var encoder = Mp3Encoder()
        if (!File(pcmPath).exists()) {
            Toast.makeText(this, "请先进行录制PCM音频", Toast.LENGTH_SHORT).show()
            return
        }
        var ret = encoder.init(pcmPath, 2, 128, 44100, target)
        if (ret == 0) {
            encoder.encode()
            encoder.destroy()
            Toast.makeText(this, "PCM->MP3编码完成", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Lame初始化失败", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * 将mp3转pcm
     */
    private fun decodeAudio() {
        var mp3Path = File(externalCacheDir, "target.mp3").absolutePath
        var target = File(externalCacheDir, "target.pcm").absolutePath
        var decoder = Mp3Decoder()
        if (!File(mp3Path).exists()) {
            Toast.makeText(this, "请先进行准备一个Mp3音频", Toast.LENGTH_SHORT).show()
            return
        }
        var ret = decoder.init(mp3Path, target)
        if (ret == 0) {
            decoder.decode()
            decoder.destroy()
            Toast.makeText(this, "MP3->PCM解码完成", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Lame初始化失败", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

}
