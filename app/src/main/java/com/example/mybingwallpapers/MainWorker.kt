package com.example.mybingwallpapers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit


class MainWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        const val workName = "getImage"
    }

    override fun doWork(): Result {

        // do now TODO

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}

class OneTimeWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        const val workName = "startGettingImages"
    }

    override fun doWork(): Result {
        // do now TODO

        // start periodic job
        val getImageWorkRequest =
            PeriodicWorkRequestBuilder<MainWorker>(
                1, TimeUnit.DAYS,
                1, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.REPLACE,
            getImageWorkRequest
        )

        // Indicate whether the work finished successfully with the Result
        return Result.success()

    }
}

