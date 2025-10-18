package com.dti.gymtimer

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log

private const val TAG = "GymTimer-Service"

class GymTimerService() : Service() {
    companion object TimerCommands {
        const val START = "START"
        const val PAUSE = "PAUSE"
        const val RESET = "RESET"
        const val ADD_TIME = "ADD_TIME"
    }

    val alarmController = AlarmController()
    val countdownService = CountdownService()
    val notificationService = NotificationService()

    private var _isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationService.createForegroundNotification(this)
        startForeground(1, notification)

        Log.d(TAG, "Received intent: ${intent?.action}")
        when (intent?.action) {
            START -> {
                val seconds = intent.getIntExtra("time", 0)
                startTimer(seconds)
            }

            PAUSE -> pauseTimer()
            RESET -> resetTimer()
            ADD_TIME -> {
                val seconds = intent.getIntExtra("time", 0)
                addTime(seconds)
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        registerCountdownReceiver()
    }

    private fun startTimer(initialSeconds: Int) {
        _isRunning = true
        countdownService.startCountdown(this, initialSeconds)
    }

    private fun registerCountdownReceiver() {
        val filter = IntentFilter().apply {
            addAction(CountdownService.ACTION_COUNTDOWN_UPDATED)
            addAction(CountdownService.ACTION_COUNTDOWN_COMPLETED)
        }
        this.registerReceiver(countdownReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        Log.d(TAG, "BroadcastReceiver registered")
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
        notificationService.showStopNotification(this)
        alarmController.start(this)
    }

    private fun onUpdated(remainingSeconds: Int) {
        Log.d(TAG, "Countdown updated: $remainingSeconds")
        notificationService.showCountdownNotification(this, remainingSeconds)
    }

    private fun addTime(increase: Int) {
        countdownService.addSeconds(increase)
        Log.d(TAG, "Added $increase seconds to countdown")
    }

    private fun pauseTimer() {
        countdownService.togglePause()
        this._isRunning = !this._isRunning
        if (this._isRunning) {
            Log.d(TAG, "Resumed timer")
        } else {
            Log.d(TAG, "Paused timer")
        }
    }

    private fun resetTimer() {
        countdownService.resetCountdown()
        notificationService.cancelNotification(this)
        alarmController.stop(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
