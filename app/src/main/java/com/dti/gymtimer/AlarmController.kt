package com.dti.gymtimer

import android.content.Context
import android.util.Log

private const val TAG = "GymTimer-Alarm"

class AlarmController {
    companion object {
        private var instance: AlarmController? = null
    }

    private var notificationService: NotificationService = NotificationService()

    private var soundService: SoundService = SoundService()

    init {
        instance = this
    }

    fun start(context: Context) {
        Log.d(TAG, "Starting alarm")
        vibrate(context)
        notificationService.showStopNotification(context)
        soundService.playSound(context)
    }

    fun stop(context: Context) {
        Log.d(TAG, "Stopping alarm")
        soundService.stopSound(context)
        notificationService.cancelNotification(context)
    }

}