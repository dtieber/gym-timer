package com.dti.gymtimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "GymTimer-Notifications"

class NotificationService {
    companion object {
        private const val CHANNEL_ID = "gymtimer_alarm_channel"
    }

    fun showCountdownNotification(context: Context, remainingSeconds: Int) {
        createNotificationChannel(context)

        val formattedTime = formatTime(remainingSeconds)

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle("⏳ Countdown Running")
            .setContentText("Time left: $formattedTime")
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setOngoing(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, notification)
        Log.d(TAG, "Countdown notification updated: $formattedTime")
    }

    fun showStopNotification(context: Context) {
        createNotificationChannel(context)

        val stopIntent = Intent(context, AlarmStopReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle("⏰ Gym Timer")
            .setContentText("Alarm ringing – tap to stop")
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setAutoCancel(true)
            .addAction(0, "Stop", stopPendingIntent)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, notification)
        Log.d(TAG, "Notification shown")
    }

    fun cancelNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(1)
        Log.d(TAG, "Notification canceled")
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Gym Timer Alarm",
            NotificationManager.IMPORTANCE_HIGH
        )
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}
