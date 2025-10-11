package com.dti.gymtimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AlarmStateHolder {
    private val _alarmRinging = MutableStateFlow(false)
    val alarmRinging: StateFlow<Boolean> get() = _alarmRinging

    fun setAlarmRinging(ringing: Boolean) {
        _alarmRinging.value = ringing
    }
}
