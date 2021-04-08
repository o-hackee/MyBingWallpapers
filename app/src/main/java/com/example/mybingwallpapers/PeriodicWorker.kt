package com.example.mybingwallpapers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class GetImageWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        const val workName = "getImage"

        fun start(context: Context) {
            val getterWork = OneTimeWorkRequestBuilder<GetImageWorker>()
                // leave backoff default
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                getterWork
            )
        }
    }

    override fun doWork(): Result {
        return if (BingWallpapersApi.getImageBlocking())
            Result.success()
        else
            Result.retry()
    }
}

class PeriodicWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        const val workName = "periodic"
    }

    override fun doWork(): Result {
        // start one-time job for getting - now
        GetImageWorker.start(applicationContext)

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}

class PeriodicWorkStarter(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        const val workName = "startPeriodic"
    }

    override fun doWork(): Result {
        // start one-time job for getting - now
        GetImageWorker.start(applicationContext)

        // start periodic job
        val getImageWorkRequest =
            PeriodicWorkRequestBuilder<PeriodicWorker>(
                1, TimeUnit.DAYS,
                1, TimeUnit.HOURS
            )
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

