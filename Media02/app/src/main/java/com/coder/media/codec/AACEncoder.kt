package com.coder.media.codec

import android.annotation.SuppressLint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Handler
import android.os.Message
import android.util.Log
import com.coder.media.utils.ToastUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-17
 */

private val TAG = "AACEncoder"
class AACEncoder {
    private val MINE_TYPE = "audio/mp4a-latm"
    private var mediaCodec:MediaCodec?=null
    private var mediaFormat:MediaFormat?=null

    private val sampleRate = 44100
    private val channel = 2
    private val bitRate = 96000


    private var inputBuffers:Array<ByteBuffer>? =null
    private var outputBuffers:Array<ByteBuffer>? =null

    private var fos:FileOutputStream? =null

    private var thread:Thread?=null


    private var handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0){
                ToastUtils.show("PCM->AAC编码完成")
            }
        }
    }

    fun init(){
        mediaFormat = MediaFormat.createAudioFormat(MINE_TYPE,sampleRate,channel)
        mediaFormat?.setInteger(MediaFormat.KEY_BIT_RATE,bitRate)
        mediaFormat?.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        mediaFormat?.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 10 * 1024)

        mediaCodec = MediaCodec.createEncoderByType(MINE_TYPE)
        mediaCodec!!.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec?.start()

        inputBuffers = mediaCodec?.inputBuffers
        outputBuffers = mediaCodec?.outputBuffers
    }


    private fun encode(byteArray: ByteArray){
        mediaCodec?.run {
            //返回要用有效数据填充的输入缓冲区的索引， -1 无限期地等待输入缓冲区的可用性
            val inputIndex = dequeueInputBuffer(-1)
            if (inputIndex > 0){
                // 根据索引获取可用输入缓存区
                val inputBuffer = this@AACEncoder.inputBuffers!![inputIndex]
                // 清空缓冲区
                inputBuffer.clear()
                // 将pcm数据放入缓冲区
                inputBuffer.put(byteArray)
                // 提交放入数据缓冲区索引以及大小
                queueInputBuffer(inputIndex,0,byteArray.size,System.nanoTime(),0)
            }
            // 指定编码器缓冲区中有效数据范围
            val bufferInfo = MediaCodec.BufferInfo()
            // 获取输出缓冲区索引
            var outputIndex = dequeueOutputBuffer(bufferInfo,0)

            while (outputIndex>0){
                // 根据索引获取可用输出缓存区
                val outputBuffer =this@AACEncoder.outputBuffers!![outputIndex]
                // 测量输出缓冲区大小
                val bufferSize = bufferInfo.size
                // 输出缓冲区实际大小，ADTS头部长度为7
                val bufferOutSize = bufferSize+7

                // 指定输出缓存区偏移位置以及限制大小
                outputBuffer.position(bufferInfo.offset)
                outputBuffer.limit(bufferInfo.offset+bufferSize)
                // 创建输出缓存空间
                val data = ByteArray(bufferOutSize)
                // 增加ADTS头部
                addADTStoPacket(data, bufferOutSize)
                // 将编码输出数据写入缓存空间
                outputBuffer.get(data,7,bufferInfo.size)
                // 重新指定输出缓存区偏移
                outputBuffer.position(bufferInfo.offset)
                // 将获取的数据写入文件
                fos?.write(data)
                // 释放输出缓冲区
                releaseOutputBuffer(outputIndex,false)
                // 重新获取输出缓冲区索引
                outputIndex=dequeueOutputBuffer(bufferInfo,0)
            }
        }
    }


    private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        val profile = 2 // AAC LC
        val freqIdx = 4 // 44.1KHz
        val chanCfg = 2// CPE
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F) .toByte()
        packet[6] = 0xFC.toByte()
    }

    fun startASync(pcmPath :String, aacPath:String){
        val byteArray = ByteArray(1024)
        val pcmFile = File(pcmPath)
        if (!pcmFile.exists()){
            Log.e(TAG,"pcm file not exist!")
            return
        }
        val aacFile = File(aacPath)
        if (aacFile.exists()){
            aacFile.delete()
        }
        thread = Thread(Runnable {
            val fis = FileInputStream(pcmFile)
            fos = FileOutputStream(aacFile)
            var read: Int
            while ({ read = fis.read(byteArray);read }() > 0) {
                encode(byteArray)
            }
            fos?.close()
            fis.close()
            handler.sendEmptyMessage(0)
            Log.d("AAC Encode", "PCM->AAC编码完成")
            stop()
            release()
        })
        thread?.start()
    }

    private fun stop(){
        if (mediaCodec!=null){
            mediaCodec?.stop()
        }
    }

    private fun release(){

        if (thread!=null){
            thread?.join()
            thread =null
        }

        if (mediaCodec!=null){
            mediaCodec?.release()
            mediaCodec = null

            mediaFormat = null
            inputBuffers = null
            outputBuffers = null
        }
    }


}