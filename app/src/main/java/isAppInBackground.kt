import androidx.lifecycle.ProcessLifecycleOwner

fun isAppInBackground(): Boolean {
    return !ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
}
