package com.dti.gymtimer

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "GymTimer-Countdown"

class CountdownService {

    companion object {
        const val ACTION_COUNTDOWN_UPDATED = "com.dti.gymtimer.COUNTDOWN_UPDATED"
        const val ACTION_COUNTDOWN_COMPLETED = "com.dti.gymtimer.COUNTDOWN_COMPLETED"
    }

    private var isPaused = false
    private var remainingSeconds = 0
    private var countdownJob: Job? = null

    fun startCountdown(context: Context, initialSeconds: Int) {
        countdownJob = CoroutineScope(Dispatchers.Default).launch {
            remainingSeconds = initialSeconds
            isPaused = false

            while (remainingSeconds >= 0) {
                if (!isPaused) {
                    sendBroadcast(context, ACTION_COUNTDOWN_UPDATED, remainingSeconds)
                    remainingSeconds--
                }
                delay(1000L)
            }
            sendBroadcast(context, ACTION_COUNTDOWN_COMPLETED)
        }
    }

    private fun sendBroadcast(context: Context, action: String, remainingSeconds: Int? = null) {
        val intent = Intent(action).apply {
            setPackage(context.packageName)
            remainingSeconds?.let {
                putExtra("remaining_seconds", it)
            }
        }
        Log.d(TAG, "Sending broadcast: $action")
        context.sendBroadcast(intent)
    }

    fun togglePause() {
        isPaused = !isPaused
    }

    fun addSeconds(context: Context, seconds: Int) {
        if (countdownJob == null) {
            startCountdown(context, seconds)
            return
        }
        remainingSeconds += seconds
    }

    fun resetCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        remainingSeconds = 0
        isPaused = false
    }
}
