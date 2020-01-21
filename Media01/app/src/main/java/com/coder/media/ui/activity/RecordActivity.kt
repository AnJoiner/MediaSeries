package com.coder.media.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.coder.media.R
import com.coder.media.Status
import kotlinx.android.synthetic.main.activity_record.*
import java.io.File
import java.io.FileOutputStream

/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-15
 */
private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecordActivity : AppCompatActivity() {

    private var sampleRate = 44100
    private var channel = AudioFormat.CHANNEL_IN_STEREO
    private var audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null

    private var bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat)

    private var status = Status.PREPARING

    private var filename = "record.pcm"

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private var thread:Thread ?= null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, RecordActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        ActivityCompat.requestPermissions(this, permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )

        btnRecord.setOnClickListener(View.OnClickListener {
            if (status == Status.PREPARING) {
                tvPath.setText("输出目录: ")
                createRecord()
                startRecord()
                btnRecord.setText("停止")
            } else if (status == Status.STARTING) {
                stopRecord()
                btnRecord.setText("录音")
                tvPath.setText("输出目录: " + (externalCacheDir?.absolutePath + File.separator + filename))
            }

        })

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }


    /**
     * 创建录音对象
     */
    private fun createRecord() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channel,
            audioFormat,
            bufferSizeInBytes
        )
    }

    private fun startRecord() {
        if (null!=audioRecord && audioRecord?.state!=AudioRecord.STATE_UNINITIALIZED){
            audioRecord?.startRecording()
            status = Status.STARTING
            thread = Thread(Runnable {
                writeData2File()
            })

            thread?.start()
        }

    }

    private fun stopRecord() {
        if (null!=audioRecord && audioRecord?.state!=AudioRecord.STATE_UNINITIALIZED){
            audioRecord?.stop()
            status = Status.STOP
            release()
        }
    }


    private fun release() {

        if (thread!=null){
            thread?.join()
            thread =null
        }

        if (audioRecord != null) {
            audioRecord?.release()
            audioRecord = null
        }

        status = Status.PREPARING
    }

    /**
     * 写入文件
     */
    private fun writeData2File() {

        var ret = 0
        val byteArray = ByteArray(bufferSizeInBytes)


        val file = File(externalCacheDir?.absolutePath + File.separator + filename)

        if (file.exists()) {
            file.delete()
        } else {
            file.createNewFile()
        }
        val fos = FileOutputStream(file)

        while (status == Status.STARTING) {
            ret = audioRecord?.read(byteArray, 0, bufferSizeInBytes)!!
            if (ret!=AudioRecord.ERROR_BAD_VALUE || ret!=AudioRecord.ERROR_INVALID_OPERATION|| ret!=AudioRecord.ERROR_DEAD_OBJECT){
                fos.write(byteArray)
            }
        }
        fos.close()
    }

}