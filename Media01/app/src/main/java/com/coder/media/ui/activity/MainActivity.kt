package com.coder.media.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.coder.media.Mp3Encoder
import com.coder.media.R
import com.coder.media.codec.AACEncoder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        initListener()
    }

    private fun initListener() {
        btnRecord.setOnClickListener {
            RecordActivity.start(this)
        }
        btnEncodeMp3.setOnClickListener {
            encodeAudioMP3()
        }

        btn2Play.setOnClickListener(View.OnClickListener {
            PlayActivity.start(this)
        })

        btnEncodeAAC.setOnClickListener {
            encodeAudioAAC()
        }
    }

    /**
     * 将pcm转mp3
     */
    private fun encodeAudioMP3() {
        val pcmPath = File(externalCacheDir, "record.pcm").absolutePath
        val target = File(externalCacheDir, "target.mp3").absolutePath
        val encoder = Mp3Encoder()
        if (!File(pcmPath).exists()) {
            Toast.makeText(this, "请先进行录制PCM音频", Toast.LENGTH_SHORT).show()
            return
        }
        val ret = encoder.init(pcmPath, 2, 128, 44100, target)
        if (ret == 0) {
            encoder.encode()
            encoder.destroy()
            Toast.makeText(this, "PCM->MP3编码完成", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Lame初始化失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * pcm转aac
     */
    private fun encodeAudioAAC(){
        val pcmPath = File(externalCacheDir, "record.pcm").absolutePath
        val target = File(externalCacheDir, "target.aac").absolutePath
        if (!File(pcmPath).exists()) {
            Toast.makeText(this, "请先进行录制PCM音频", Toast.LENGTH_SHORT).show()
            return
        }
        var encoder = AACEncoder()
        encoder.init()
        encoder.startASync(pcmPath,target)
    }
}
