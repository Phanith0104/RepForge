package com.repforge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import android.util.Log

@HiltAndroidApp
class RepForgeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("RepForgeCrash", "Uncaught exception in thread ${thread.name}", throwable)
        }
    }
}
