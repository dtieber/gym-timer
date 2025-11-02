package com.dti.gymtimer.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dti.gymtimer.MainActivity
import com.dti.gymtimer.R
import com.dti.gymtimer.formatTime

private const val TAG = "GymTimer-Notifications"

class NotificationService {
    companion object {
        private const val CHANNEL_ID_ACTIVE = "gymtimer_active_channel"
        private const val CHANNEL_ID_PASSIVE = "gymtimer_passive_channel"
    }

    fun createForegroundNotification(context: Context): Notification {
        createNotificationChannels(context)

        val builder = Notification.Builder(context, CHANNEL_ID_PASSIVE)
            .setContentTitle("Gym Timer")
            .setContentText("Timer is running")
            .setSmallIcon(R.drawable.ic_notification_timer)

        return builder.build()
    }

    fun showCountdownNotification(context: Context, remainingSeconds: Int) {
        createNotificationChannels(context)

        val formattedTime = formatTime(remainingSeconds)

        val notification = Notification.Builder(context, CHANNEL_ID_PASSIVE)
            .setContentTitle("⏳ Countdown Running")
            .setContentText("Time left: $formattedTime")
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setContentIntent(getNavigateBackToAppIntent(context))
            .setOngoing(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, notification)
        Log.d(TAG, "Countdown notification updated: $formattedTime")
    }

    fun showStopNotification(context: Context) {
        createNotificationChannels(context)

        val notification = Notification.Builder(context, CHANNEL_ID_ACTIVE)
            .setContentTitle("⏰ Gym Timer")
            .setContentText("Alarm ringing – tap to stop")
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setAutoCancel(true)
            .setContentIntent(getNavigateBackToAppIntent(context))
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, notification)
        Log.d(TAG, "Stop notification shown")
    }

    fun cancelNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(1)
        Log.d(TAG, "Notification canceled")
    }

    private fun createNotificationChannels(context: Context) {
        val alertChannel = NotificationChannel(
            CHANNEL_ID_ACTIVE,
            "Gym Timer Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
        }

        val passiveChannel = NotificationChannel(
            CHANNEL_ID_PASSIVE,
            "Gym Timer Passive",
            NotificationManager.IMPORTANCE_LOW
        )

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(alertChannel)
        nm.createNotificationChannel(passiveChannel)
    }

    private fun getNavigateBackToAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
