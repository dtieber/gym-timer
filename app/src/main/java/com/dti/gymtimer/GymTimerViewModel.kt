package com.dti.gymtimer

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.dti.gymtimer.service.countdown.CountdownService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "GymTimerViewModel"

class GymTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _remainingTime = MutableStateFlow(0)
    val remainingTime: StateFlow<Int> = _remainingTime

    private val _alarmRinging = MutableStateFlow(false)
    val alarmRinging: StateFlow<Boolean> = _alarmRinging

    private val _currentSet = MutableStateFlow<Int?>(null)
    val currentSet: StateFlow<Int?> = _currentSet

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received broadcast: ${intent?.action}")
            when (intent?.action) {
                CountdownService.ACTION_COUNTDOWN_UPDATED -> {
                    val remaining = intent.getIntExtra("remaining_seconds", 0)
                    _remainingTime.value = remaining
                    Log.d(TAG, "Received update: $remaining")
                }

                CountdownService.ACTION_COUNTDOWN_COMPLETED -> {
                    _alarmRinging.value = true
                    advanceSet()
                    Log.d(TAG, "Received completion event")
                }

                GymTimerService.RESET_COMPLETE -> {
                    _remainingTime.value = 0
                    _alarmRinging.value = false
                    Log.d(TAG, "Received reset completed event")
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(CountdownService.ACTION_COUNTDOWN_UPDATED)
            addAction(CountdownService.ACTION_COUNTDOWN_COMPLETED)
            addAction(GymTimerService.RESET_COMPLETE)
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        Log.d(TAG, "BroadcastReceiver registered")
    }

    override fun onCleared() {
        context.unregisterReceiver(receiver)
        super.onCleared()
    }

    fun startTimer(seconds: Int) {
        val intent = Intent(context, GymTimerService::class.java).apply {
            action = GymTimerService.TimerCommands.START
            putExtra("time", seconds)
        }
        ContextCompat.startForegroundService(context, intent)
        Log.d(TAG, "Timer started: $seconds")
    }

    fun addSeconds(seconds: Int) {
        val intent = Intent(context, GymTimerService::class.java).apply {
            action = GymTimerService.TimerCommands.ADD_TIME
            putExtra("time", seconds)
        }
        context.startService(intent)
        Log.d(TAG, "Added $seconds seconds to timer")
    }

    fun toggleTimer() {
        val intent = Intent(context, GymTimerService::class.java).apply {
            action = GymTimerService.TimerCommands.PAUSE
        }
        context.startService(intent)
        Log.d(TAG, "Pause timer")
    }

    fun resetTimer() {
        val intent = Intent(context, GymTimerService::class.java).apply {
            action = GymTimerService.TimerCommands.RESET
        }
        context.startService(intent)
        Log.d(TAG, "Reset timer")
    }

    fun selectSet(set: Int) {
        val computedSet = if (_currentSet.value == set) null else set
        _currentSet.value = computedSet
        Log.d(TAG, "Set set to ${computedSet}")
    }

    private fun advanceSet() {
        _currentSet.value?.let {
            val next = if (it == 5) 1 else it + 1
            Log.d(TAG, "Advancing set from $it to $next")
            _currentSet.value = next
        }
    }
}
