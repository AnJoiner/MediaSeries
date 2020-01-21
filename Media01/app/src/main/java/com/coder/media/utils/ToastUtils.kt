package com.coder.media.utils

import android.widget.Toast
import com.coder.media.BaseApplication

/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-21
 */

class ToastUtils {
    companion object{
        fun show(msg:String){
            Toast.makeText(BaseApplication.getInstance().applicationContext,msg,Toast.LENGTH_SHORT).show()
        }
    }
}