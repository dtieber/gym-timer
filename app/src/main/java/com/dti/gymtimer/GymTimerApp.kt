package com.dti.gymtimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private const val TAG = "GymTimer"

@Composable
fun GymTimerApp(context: Context) {
    var remainingTime by remember { mutableIntStateOf(0) }
    var alarmRinging by remember { mutableStateOf(false) }

    val gymTimerService = remember { GymTimerService(context) }

    fun resetTimer() {
        remainingTime = 0
        alarmRinging = false
        gymTimerService.resetTimer()
        Log.d(TAG, "Reset timer")
    }

    fun onUpdate(remaining: Int) {
        remainingTime = remaining
    }

    fun onCompleted() {
        Log.d(TAG, "Show alarm ringing info in app")
        alarmRinging = true
    }

    fun startTimer(seconds: Int) {
        gymTimerService.startTimer(seconds)
        Log.d(TAG, "Timer started: $seconds")
    }

    fun addSeconds(seconds: Int) {
        if (!gymTimerService.isRunning()) {
            return startTimer(seconds)
        }
        gymTimerService.addTime(seconds)
        Log.d(TAG, "Added $seconds seconds to timer")
    }

    fun toggleTimer() {
        gymTimerService.pauseTimer()
        Log.d(TAG, "Pause timer")
    }

    LaunchedEffect(Unit) {
        gymTimerService.registerCountdownReceiver()
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "Received broadcast: ${intent?.action}")
                when (intent?.action) {
                    CountdownService.ACTION_COUNTDOWN_UPDATED -> {
                        val remaining = intent.getIntExtra("remaining_seconds", 0)
                        onUpdate(remaining)
                        Log.d(TAG, "Received update: $remaining")
                    }

                    CountdownService.ACTION_COUNTDOWN_COMPLETED -> {
                        onCompleted()
                        Log.d(TAG, "Received completion event")
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(CountdownService.ACTION_COUNTDOWN_UPDATED)
            addAction(CountdownService.ACTION_COUNTDOWN_COMPLETED)
        }

        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        Log.d(TAG, "BroadcastReceiver registered")

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        color = Color(0xFF101010)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (alarmRinging) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFFFC107))
                        .clickable { resetTimer() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "⏰ Alarm ringing – tap to stop",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = formatTime(remainingTime),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable { toggleTimer() }
                    .padding(bottom = 40.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(onClick = { startTimer(60) }) { Text("1:00", fontSize = 20.sp) }
                    Button(onClick = { startTimer(90) }) { Text("1:30", fontSize = 20.sp) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(onClick = { startTimer(120) }) { Text("2:00", fontSize = 20.sp) }
                    Button(onClick = { addSeconds(10) }) { Text("+10s", fontSize = 20.sp) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(
                        onClick = { resetTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Reset", fontSize = 20.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.ENGLISH, "%02d:%02d", m, s)
}
