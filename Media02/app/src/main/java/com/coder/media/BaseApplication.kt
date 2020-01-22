package com.coder.media

import android.app.Application

/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-21
 */
class BaseApplication : Application(){

    companion object{
        private lateinit var instance : BaseApplication

        fun getInstance(): BaseApplication{
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}