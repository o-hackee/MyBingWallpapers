package com.example.mybingwallpapers.work

import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.work.*
import com.example.mybingwallpapers.http.BingWallpapersApi
import com.example.mybingwallpapers.R
import com.example.mybingwallpapers.utils.sendNotification
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.TimeUnit

class GetImageWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        const val workName = "getImage"
        const val withNotificationDataField = "withNotification"

        fun start(context: Context, withNotification: Boolean = false) {
            Timber.i("b1 GetImageWorker queued")
            val builder = OneTimeWorkRequestBuilder<GetImageWorker>()
                    // leave backoff default
                    .setConstraints(
                            Constraints.Builder()
                                    .setRequiresBatteryNotLow(true)
                                    .build()
                    )
            if (withNotification) {
                builder.setInputData(workDataOf(withNotificationDataField to true))
            }
            val getterWork = builder.build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                getterWork
            )
        }
    }

    override fun doWork(): Result {
        val withNotification = inputData.getBoolean(withNotificationDataField, false)
        var notificationManager: NotificationManager? = null
        if (withNotification) {
            notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)
            notificationManager?.cancelAll()
        }

        val market = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getString(applicationContext.getString(R.string.key_selected_market), "")
        if (market == null){
            Timber.i("b1 couldn't get a market")
            if (withNotification) {
                notificationManager?.sendNotification(applicationContext.getString(R.string.get_image_failed), applicationContext)
            }
            return Result.failure()
        }
        val imageId = BingWallpapersApi.getImageInfoBlocking(market)
        Timber.i("b1 GetImageWorker imageId = $imageId")
        if (imageId == null) {
            Timber.i("b1 GetImageWorker work failed info")

            if (runAttemptCount > 5) {
                notificationManager?.sendNotification(applicationContext.getString(R.string.get_image_keeps_failing), applicationContext)
            }

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

        if (withNotification) {
            notificationManager?.sendNotification(applicationContext.getString(R.string.got_new_image), applicationContext)
        }
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
        GetImageWorker.start(applicationContext, withNotification = true)

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
        GetImageWorker.start(applicationContext, withNotification = true)

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

