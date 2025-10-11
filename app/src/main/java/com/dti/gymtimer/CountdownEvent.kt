package com.dti.gymtimer

sealed class CountdownEvent {
    data class CountdownUpdated(val remainingSeconds: Int) : CountdownEvent()
    object CountdownCompleted : CountdownEvent()
}
