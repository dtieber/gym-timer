package com.dti.gymtimer

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log

private const val TAG = "GymTimer-Service"

class GymTimerService(val context: Context) : Service() {
    val alarmController = AlarmController()
    val countdownService = CountdownService()
    val notificationService = NotificationService()

    private var _isRunning = false

    fun startTimer(initialSeconds: Int) {
        _isRunning = true
        countdownService.startCountdown(context, initialSeconds)
    }

    fun registerCountdownReceiver() {
        val filter = IntentFilter().apply {
            addAction(CountdownService.ACTION_COUNTDOWN_UPDATED)
            addAction(CountdownService.ACTION_COUNTDOWN_COMPLETED)
        }
        context.registerReceiver(countdownReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        Log.d(TAG, "BroadcastReceiver manually registered")
    }

    private val countdownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received broadcast: ${intent?.action}")
            when (intent?.action) {
                CountdownService.ACTION_COUNTDOWN_UPDATED -> {
                    val remainingSeconds = intent.getIntExtra("remaining_seconds", 0)
                    onUpdated(remainingSeconds)
                }

                CountdownService.ACTION_COUNTDOWN_COMPLETED -> {
                    onCompleted()
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
