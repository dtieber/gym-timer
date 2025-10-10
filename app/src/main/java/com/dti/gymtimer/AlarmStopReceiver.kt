package com.dti.gymtimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GymTimer-Receiver", "Stop alarm action received")

        val alarmController = AlarmController.getInstance()
        alarmController?.stop(context)
    }
}
