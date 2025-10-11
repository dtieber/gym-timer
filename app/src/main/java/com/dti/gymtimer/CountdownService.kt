package com.dti.gymtimer

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CountdownService {

    fun startCountdown(initialSeconds: Int): Flow<CountdownEvent> = flow {
        var remaining = initialSeconds
        while (remaining >= 0) {
            emit(CountdownEvent.CountdownUpdated(remaining))
            delay(1000L)
            remaining--
        }
        emit(CountdownEvent.CountdownCompleted)
    }
}
