package com.dti.gymtimer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "GymTimer-Service"

class GymTimerService(val context: Context) : Service() {
    val alarmController = AlarmController()
    val countdownService = CountdownService()
    val notificationService = NotificationService()

    private var _isRunning = false

    fun startTimer(initialSeconds: Int): Flow<CountdownEvent> = flow {
        _isRunning = true
        countdownService.startCountdown(initialSeconds).collect { event ->
            when (event) {
                CountdownEvent.CountdownCompleted -> {
                    onCompleted()
                    emit(CountdownEvent.CountdownCompleted)
                }

                is CountdownEvent.CountdownUpdated -> {
                    onUpdated(event.remainingSeconds)
                    emit(CountdownEvent.CountdownUpdated(event.remainingSeconds))
                }
            }
        }
    }

    private fun onCompleted() {
        Log.d(TAG, "Countdown completed")
        this._isRunning = false
        notificationService.showStopNotification(context)
        alarmController.start(context)
    }

    private fun onUpdated(remainingSeconds: Int) {
        Log.d(TAG, "Countdown updated: $remainingSeconds")
        notificationService.showCountdownNotification(context, remainingSeconds)
    }

    fun addTime(increase: Int) {
        countdownService.addSeconds(increase)
        Log.d(TAG, "Added $increase seconds to countdown")
    }

    fun pauseTimer() {
        countdownService.togglePause()
        this._isRunning = !this._isRunning
        if (this._isRunning) {
            Log.d(TAG, "Resumed timer")
        } else {
            Log.d(TAG, "Paused timer")
        }
    }

    fun resetTimer() {
        countdownService.resetCountdown()
        notificationService.cancelNotification(context)
        alarmController.stop(context)
    }

    fun isRunning(): Boolean {
        return this._isRunning
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
