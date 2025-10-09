package com.dti.gymtimer

import android.content.Context
import android.media.*
import android.os.*
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import java.util.Locale

private const val TAG = "GymTimer"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GymTimerApp(this) }
    }
}

@Composable
fun GymTimerApp(context: Context) {
    val scope = rememberCoroutineScope()
    var remainingTime by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    var alarmRinging by remember { mutableStateOf(false) }

    val alarmController = remember { AlarmController() }

    fun stopAlarm() {
        alarmController.stop(context)
        alarmRinging = false
        Log.d(TAG, "Alarm stopped")
    }

    fun resetTimer() {
        running = false
        timerJob?.cancel()
        remainingTime = 0
        Log.d(TAG, "Reset timer")
    }

    fun startTimer(seconds: Int) {
        timerJob?.cancel()
        remainingTime = seconds
        running = true
        timerJob = scope.launch {
            while (remainingTime > 0 && running) {
                delay(1000L)
                remainingTime--
            }
            if (remainingTime == 0 && running) {
                alarmController.start(context, scope, { alarmRinging = true }, { alarmRinging = false })
                running = false
            }
        }
        Log.d(TAG, "Timer started: $seconds")
    }

    fun addTenSeconds() {
        remainingTime += 10
        Log.d(TAG, "Added 10s to timer")
        if (!running) startTimer(remainingTime)
    }

    fun toggleTimer() {
        running = !running
        Log.d(TAG, "Toggle timer: $running")
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
                        .clickable { stopAlarm() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏰ Alarm ringing – tap to stop", color = Color.Black, fontWeight = FontWeight.Bold)
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

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(onClick = { startTimer(60) }) { Text("1:00", fontSize = 20.sp) }
                    Button(onClick = { startTimer(90) }) { Text("1:30", fontSize = 20.sp) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(onClick = { startTimer(120) }) { Text("2:00", fontSize = 20.sp) }
                    Button(onClick = { addTenSeconds() }) { Text("+10s", fontSize = 20.sp) }
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

private class AlarmController {
    private var player: MediaPlayer? = null
    private var autoStopJob: Job? = null
    private var prevSpeaker: Boolean? = null
    private var prevMode: Int? = null

    fun start(context: Context, scope: CoroutineScope, onShow: () -> Unit, onHide: () -> Unit) {
        vibrate(context)
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val headphoneDevice = findHeadphoneDevice(am)
        saveAudioState(am)
        configureAudio(am, headphoneDevice != null)
        onShow()
        player = createPlayer(context, headphoneDevice)
        if (player != null) Log.d(TAG, "Alarm started")
        autoStopJob = scope.launch {
            delay(10000L)
            stop(context)
            Log.d(TAG, "Alarm auto stopped")
            onHide()
        }
    }

    fun stop(context: Context) {
        player?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
                Log.d(TAG, "Alarm stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Stop error", e)
            }
        }
        player = null
        autoStopJob?.cancel()
        restoreAudio(context)
    }

    private fun findHeadphoneDevice(am: AudioManager): AudioDeviceInfo? {
        val outputs = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val device = outputs.firstOrNull {
            when (it.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLE_SPEAKER -> true
                else -> false
            }
        }
        Log.d(TAG, "Headphone device=${device?.productName ?: "none"}")
        return device
    }

    private fun saveAudioState(am: AudioManager) {
        prevSpeaker = try { am.isSpeakerphoneOn } catch (_: Exception) { null }
        prevMode = try { am.mode } catch (_: Exception) { null }
    }

    private fun configureAudio(am: AudioManager, hasHeadphones: Boolean) {
        try {
            am.mode = AudioManager.MODE_NORMAL
            am.isSpeakerphoneOn = !hasHeadphones
            Log.d(TAG, "Speaker active=${!hasHeadphones}")
        } catch (e: Exception) {
            Log.e(TAG, "configureAudio", e)
        }
    }

    private fun createPlayer(context: Context, headphoneDevice: AudioDeviceInfo?): MediaPlayer? {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return try {
            MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                prepare()
                headphoneDevice?.let {
                    setPreferredDevice(it)
                    Log.d(TAG, "Routing alarm to ${it.productName}")
                }
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "createPlayer", e)
            null
        }
    }

    private fun vibrate(context: Context) {
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


    private fun restoreAudio(context: Context) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            prevSpeaker?.let { am.isSpeakerphoneOn = it }
            prevMode?.let { am.mode = it }
            Log.d(TAG, "Audio restored")
        } catch (e: Exception) {
            Log.e(TAG, "restoreAudio", e)
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.ENGLISH, "%02d:%02d", m, s)
}
