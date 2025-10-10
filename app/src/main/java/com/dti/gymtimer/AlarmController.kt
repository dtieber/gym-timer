package com.dti.gymtimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "GymTimer-Alarm"

class AlarmController {
    companion object {
        private const val CHANNEL_ID = "gymtimer_alarm_channel"
        private var instance: AlarmController? = null

        fun getInstance(): AlarmController? = instance
    }

    private var player: MediaPlayer? = null
    private var autoStopJob: Job? = null
    private var prevSpeaker: Boolean? = null
    private var prevMode: Int? = null

    init {
        instance = this
    }

    fun start(context: Context, scope: CoroutineScope, onShow: () -> Unit, onHide: () -> Unit) {
        vibrate(context)
        onShow()
        showNotification(context)
        playSound(context)
        autoStopJob = scope.launch {
            delay(10000L)
            stop(context)
            Log.d(TAG, "Alarm auto stopped")
            onHide()
        }
    }

    private fun playSound(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val headphoneDevice = findHeadphoneDevice(am)
        saveAudioState(am)
        configureAudio(am, headphoneDevice != null)
        player = createPlayer(context, headphoneDevice)
        if (player != null) Log.d(TAG, "Sound started")
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
        cancelNotification(context)
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

    private fun showNotification(context: Context) {
        createNotificationChannel(context)

        val stopIntent = Intent(context, AlarmStopReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle("⏰ Gym Timer")
            .setContentText("Alarm ringing – tap to stop")
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setAutoCancel(true)
            .addAction(0, "Stop", stopPendingIntent)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, notification)
        Log.d(TAG, "Notification shown")
    }

    private fun cancelNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(1)
        Log.d(TAG, "Notification canceled")
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Gym Timer Alarm",
            NotificationManager.IMPORTANCE_HIGH
        )
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}