package com.example.mybingwallpapers

import android.app.WallpaperManager
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.TimeUnit

class GetImageWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        const val workName = "getImage"

        fun start(context: Context) {
            Timber.i("b1 GetImageWorker queued")
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
        val market = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getString(applicationContext.getString(R.string.key_selected_market), "")
        if (market == null){
            Timber.i("b1 couldn't get a market")
            return Result.failure()
        }
        val imageId = BingWallpapersApi.getImageInfoBlocking(market)
        Timber.i("b1 GetImageWorker imageId = $imageId")
        if (imageId == null) {
            Timber.i("b1 GetImageWorker work failed info")
            return Result.retry()
        }
        val stream = BingWallpapersApi.downloadImage(imageId)
        if (stream == null) {
            Timber.i("b1 GetImageWorker work failed download")
            return Result.retry()
        }
        try {
            WallpaperManager.getInstance(applicationContext).setStream(stream)
        } catch (e: Exception) {
            Timber.e("b1 GetImageWorker set wallpaper failed with ${e.message}")
            return Result.retry()
        }
        Timber.i("b1 GetImageWorker success")
        return Result.success()
    }

    override fun onStopped() {
        Timber.i("b1 GetImageWorker stopped")
        super.onStopped()
    }
}

class PeriodicWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        const val workName = "periodic"
    }

    override fun doWork(): Result {
        Timber.i("b1 PeriodicWorker work")

        // start one-time job for getting - now
        GetImageWorker.start(applicationContext)

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    override fun onStopped() {
        Timber.i("b1 PeriodicWorker stopped")
        super.onStopped()
    }
}

class PeriodicWorkStarter(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        const val workName = "startPeriodic"
    }

    override fun doWork(): Result {
        Timber.i("b1 PeriodicWorkStarter work")

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
            PeriodicWorker.workName,
            ExistingPeriodicWorkPolicy.REPLACE,
            getImageWorkRequest
        )

        // Indicate whether the work finished successfully with the Result
        return Result.success()

    }

    override fun onStopped() {
        Timber.i("b1 PeriodicWorkStarter stopped")
        super.onStopped()
    }
}

