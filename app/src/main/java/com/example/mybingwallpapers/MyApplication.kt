package com.example.mybingwallpapers

import android.app.Application
import timber.log.Timber
import androidx.work.Configuration
import com.example.mybingwallpapers.utils.FileLoggingTree

class MyApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(FileLoggingTree(applicationContext))
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

}