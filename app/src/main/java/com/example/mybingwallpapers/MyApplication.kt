package com.example.mybingwallpapers

import android.app.Application
import timber.log.Timber
import androidx.work.Configuration

class MyApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.plant(FileLoggingTree(applicationContext))
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

}