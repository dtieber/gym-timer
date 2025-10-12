package com.dti.gymtimer

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CountdownService {

    private var remainingSeconds = 0

    fun startCountdown(initialSeconds: Int): Flow<CountdownEvent> = flow {
        remainingSeconds = initialSeconds

        while (remainingSeconds >= 0) {
            emit(CountdownEvent.CountdownUpdated(remainingSeconds))
            delay(1000L)
        }
        emit(CountdownEvent.CountdownCompleted)
    }
}
