package com.example.mybingwallpapers.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mybingwallpapers.MainActivity
import com.example.mybingwallpapers.R

private const val NOTIFICATION_ID = 0

fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {
    // Create the content intent for the notification, which launches this activity
    val intent = Intent(applicationContext, MainActivity::class.java)
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    else
        PendingIntent.FLAG_UPDATE_CURRENT
    val pendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        intent,
        flags
    )

    val builder = NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.mbw_notification_channel_id))
        // Build the notification
        .setSmallIcon(R.drawable.ic_wallpaper)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    this.notify(NOTIFICATION_ID, builder.build())

}