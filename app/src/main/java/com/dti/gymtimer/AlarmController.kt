package com.dti.gymtimer

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "GymTimer-Alarm"

class AlarmController {
    companion object {
        private var instance: AlarmController? = null

        fun getInstance(): AlarmController? = instance
    }
    private var autoStopJob: Job? = null

    private var notificationService: NotificationService = NotificationService()

    private var soundService: SoundService = SoundService()

    init {
        instance = this
    }

    fun start(context: Context, scope: CoroutineScope, onShow: () -> Unit, onHide: () -> Unit) {
        vibrate(context)
        onShow()
        notificationService.showStopNotification(context)
        soundService.playSound(context)
        autoStopJob = scope.launch {
            delay(10000L)
            stop(context)
            Log.d(TAG, "Alarm auto stopped")
            onHide()
        }
    }

    fun stop(context: Context) {
        autoStopJob?.cancel()
        soundService.stopSound(context)
        notificationService.cancelNotification(context)
    }

}