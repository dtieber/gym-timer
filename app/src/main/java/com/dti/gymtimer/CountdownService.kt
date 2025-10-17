package com.dti.gymtimer

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    fun startCountdown(context: Context, initialSeconds: Int) {
        CoroutineScope(Dispatchers.Default).launch {
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

    fun addSeconds(seconds: Int) {
        remainingSeconds += seconds
    }

    fun resetCountdown() {
        remainingSeconds = 0
        isPaused = false
    }
}
