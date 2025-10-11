package com.dti.gymtimer

import android.content.Context

private const val TAG = "GymTimer-Alarm"

class AlarmController {
    companion object {
        private var instance: AlarmController? = null

        fun getInstance(): AlarmController? = instance
    }

    private var notificationService: NotificationService = NotificationService()

    private var soundService: SoundService = SoundService()

    init {
        instance = this
    }

    fun start(context: Context) {
        vibrate(context)
        notificationService.showStopNotification(context)
        soundService.playSound(context)
    }

    fun stop(context: Context) {
        soundService.stopSound(context)
        notificationService.cancelNotification(context)
    }

}