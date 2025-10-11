package com.dti.gymtimer

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log

private const val TAG = "GymTimer-Vibration"

fun vibrate(context: Context) {
    try {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vm.defaultVibrator
        val effect = VibrationEffect.createOneShot(2000, 255)
        vibrator.vibrate(effect)
        Log.d(TAG, "Vibrating")
    } catch (e: Exception) {
        Log.e(TAG, "vibrate", e)
    }
}