package com.dti.gymtimer

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CountdownService {

    private var isPaused = false
    private var remainingSeconds = 0

    fun startCountdown(initialSeconds: Int): Flow<CountdownEvent> = flow {
        remainingSeconds = initialSeconds
        isPaused = false

        while (remainingSeconds >= 0) {
            if (!isPaused) {
                emit(CountdownEvent.CountdownUpdated(remainingSeconds))
                remainingSeconds--
            }
            delay(1000L)
        }
        emit(CountdownEvent.CountdownCompleted)
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
