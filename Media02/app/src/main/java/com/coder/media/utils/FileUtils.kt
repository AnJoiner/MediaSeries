package com.coder.media.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-22
 */

class FileUtils {

    companion object{
        /**
         * 将asset文件写入缓存
         */
        fun copy2Memory(
            context: Context,
            fileName: String?
        ): Boolean {
            try {
                val cacheDir = context.externalCacheDir
                if (!cacheDir!!.exists()) {
                    cacheDir.mkdirs()
                }
                val outFile = File(cacheDir, fileName)
                Log.d("==>>>>", outFile.absolutePath)
                if (!outFile.exists()) {
                    val res = outFile.createNewFile()
                    if (!res) {
                        return false
                    }
                } else {
                    if (outFile.length() > 10) { //表示已经写入一次
                        return true
                    }
                }
                val ais = context.assets.open(fileName!!)
                val fos = FileOutputStream(outFile)
                val buffer = ByteArray(1024)
                var byteCount: Int
                while (ais.read(buffer).also { byteCount = it } != -1) {
                    fos.write(buffer, 0, byteCount)
                }
                fos.flush()
                ais.close()
                fos.close()
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return false
        }
    }

}