package com.dti.gymtimer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner

fun isAppInBackground(): Boolean {
    return !ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
}
