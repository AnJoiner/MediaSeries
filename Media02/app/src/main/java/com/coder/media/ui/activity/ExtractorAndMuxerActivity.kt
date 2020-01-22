package com.coder.media.ui.activity

import android.content.Context
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.coder.media.R
import com.coder.media.utils.FileUtils
import kotlinx.android.synthetic.main.activity_extractor_muxer.*
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.abs


/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-22
 */
class ExtractorAndMuxerActivity : AppCompatActivity() {

    private var mVideoPath: String? = null
    private var mVideoResult:String?=null
    private var mAudioResult:String?=null

    private var mMediaExtractor: MediaExtractor? = null
    private var mMediaMuxer: MediaMuxer?=null

    private var videoTrackIndex = -1
    private var audioTrackIndex = -1
    private var videoTrackFormat:MediaFormat?=null
    private var audioTrackFormat:MediaFormat?=null

    companion object{
        fun start(context: Context) {
            val intent = Intent(context, ExtractorAndMuxerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extractor_muxer)
        initData()
        initListener()
    }

    private fun initData() {
        FileUtils.copy2Memory(this, "test.mp4")
        mVideoPath = File(externalCacheDir, "test.mp4").absolutePath
        mVideoResult = File(externalCacheDir,"result.mp4").absolutePath
        mAudioResult = File(externalCacheDir,"result.aac").absolutePath
    }

    private fun initListener() {
        btnSeparation.setOnClickListener {
            createMediaExtractor()
        }
        btnMerge.setOnClickListener {

        }
    }

    private fun createMediaExtractor() {
        mMediaExtractor = MediaExtractor()
        //设置视频地址
        mMediaExtractor?.setDataSource(mVideoPath.toString())
        val trackCount = mMediaExtractor?.trackCount

        for (i in 0 until trackCount!!) {
            val trackFormat = mMediaExtractor?.getTrackFormat(i)
            val mime = trackFormat?.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/")!!) {
                audioTrackIndex = i
                audioTrackFormat = trackFormat
                Log.d("AUDIO","channel:"+trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)+",bitRate :"+trackFormat.getInteger(MediaFormat.KEY_BIT_RATE)+", sampleRate:"+trackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)+",mine:"+mime)
            }
            if (mime.startsWith("video/")) {
                videoTrackIndex = i
                videoTrackFormat = trackFormat
                Log.d("VIDEO","frameRate:"+trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE)+",mine:"+mime)
            }
        }
//        saveVideo()
        saveAudio()
    }

    private fun saveVideo(){
        Thread(Runnable {
            mMediaExtractor?.selectTrack(videoTrackIndex)
            mMediaMuxer = MediaMuxer(mVideoResult!!,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            audioTrackIndex = mMediaMuxer!!.addTrack(videoTrackFormat!!)
            mMediaMuxer!!.start()
            val frameRate = videoTrackFormat?.getInteger(MediaFormat.KEY_FRAME_RATE)
            val info = MediaCodec.BufferInfo()
            info.presentationTimeUs = 0
            val buffer = ByteBuffer.allocate(100 * 1024)

            var sampleSize = 0
            while (mMediaExtractor!!.readSampleData(buffer, 0).also { sampleSize = it } > 0) {
                info.offset = 0
                info.size = sampleSize
                info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
                info.presentationTimeUs += 1000 * 1000 / frameRate!!
                mMediaMuxer!!.writeSampleData(videoTrackIndex, buffer, info)
                mMediaExtractor!!.advance()
            }

            mMediaExtractor!!.release()

            mMediaMuxer!!.stop()
            mMediaMuxer!!.release()

            mMediaExtractor = null
            mMediaMuxer = null

            Log.d("VIDEO","分离视频完成")
        }).start()
    }

    private fun saveAudio() {
        Thread(Runnable {
            mMediaExtractor?.selectTrack(audioTrackIndex)
            val buffer = ByteBuffer.allocate(100*1024)

            mMediaExtractor!!.readSampleData(buffer, 0)
            val firstSampleTime: Long = mMediaExtractor!!.sampleTime
            mMediaExtractor!!.advance()
            val secondSampleTime: Long = mMediaExtractor!!.sampleTime

            val frameRate = abs(secondSampleTime - firstSampleTime) //时间戳
            mMediaExtractor!!.unselectTrack(audioTrackIndex)

            //
            mMediaExtractor?.selectTrack(audioTrackIndex)
            mMediaMuxer = MediaMuxer(mAudioResult!!,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            audioTrackIndex = mMediaMuxer!!.addTrack(audioTrackFormat!!)
            mMediaMuxer!!.start()


            val info = MediaCodec.BufferInfo()
            info.presentationTimeUs = 0

            var sampleSize = 0
            while (mMediaExtractor!!.readSampleData(buffer, 0).also { sampleSize = it } > 0) {
                info.offset = 0
                info.size = sampleSize
                info.flags = mMediaExtractor!!.sampleFlags
                info.presentationTimeUs += frameRate
                mMediaMuxer!!.writeSampleData(audioTrackIndex, buffer, info)
                mMediaExtractor!!.advance()
            }

            mMediaExtractor!!.release()

            mMediaMuxer!!.stop()
            mMediaMuxer!!.release()

            mMediaExtractor = null
            mMediaMuxer = null

            Log.d("VIDEO","分离音频完成")
        }).start()
    }



}