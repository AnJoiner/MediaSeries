package com.coder.media

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_play.*
import java.io.File
import java.io.FileInputStream


/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-16
 */
class PlayActivity : AppCompatActivity() {

    private var sampleRate = 44100
    private var channel = AudioFormat.CHANNEL_IN_STEREO
    private var audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat)

    private var audioTrack: AudioTrack? = null

    private var status = Status.PREPARING
    private var filename = "record.pcm"

    private var thread:Thread ?= null

    private var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0){
                btnPlay.setText("开始")
            }
        }
    }


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PlayActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        initListener()
    }

    private fun initListener() {
        btnPlay.setOnClickListener(View.OnClickListener {
            if (status == Status.PREPARING) {
                createTrack()
                startPlay()
                btnPlay.setText("停止")
            } else if (status == Status.STARTING) {
                stopPlay()
                btnPlay.setText("开始")
            }
        })
    }

    /**
     * 创建录音对象
     */
    private fun createTrack() {
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            channel,
            audioFormat,
            bufferSizeInBytes,
            AudioTrack.MODE_STREAM
        )
    }

    private fun startPlay() {
        if (null!=audioTrack && audioTrack?.state != AudioTrack.STATE_UNINITIALIZED){
            audioTrack?.play()

            thread= Thread(Runnable {
                readDataFromFile()
            })
            thread?.start()
        }

    }

    private fun stopPlay() {
        if (audioTrack != null && audioTrack?.state != AudioTrack.STATE_UNINITIALIZED) {
            audioTrack?.stop()
            status = Status.STOP

        }
        release()
    }


    private fun release() {
        if (thread!=null){
            thread?.join()
            thread =null
        }
        if (audioTrack != null) {
            audioTrack?.release()
            audioTrack = null
        }
        status = Status.PREPARING
    }


    private fun readDataFromFile() {
        val byteArray = ByteArray(bufferSizeInBytes)


        val file = File(externalCacheDir?.absolutePath + File.separator + filename)
        if (!file.exists()) {
            Toast.makeText(this, "请先进行录制PCM音频", Toast.LENGTH_SHORT).show()
            return
        }
        val fis = FileInputStream(file)
        var read: Int
        status = Status.STARTING

        while ({ read = fis.read(byteArray);read }() > 0) {
            var ret = audioTrack?.write(byteArray, 0, bufferSizeInBytes)!!
            if (ret == AudioTrack.ERROR_BAD_VALUE || ret == AudioTrack.ERROR_INVALID_OPERATION || ret == AudioManager.ERROR_DEAD_OBJECT) {
                break
            }
        }
        fis.close()
        stopPlay()
        handler.sendEmptyMessage(0)
    }
}