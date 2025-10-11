package com.dti.gymtimer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "GymTimer-Alarm"

class AlarmController {
    companion object {
        private var instance: AlarmController? = null

        fun getInstance(): AlarmController? = instance
    }

    private var player: MediaPlayer? = null
    private var autoStopJob: Job? = null
    private var prevSpeaker: Boolean? = null
    private var prevMode: Int? = null

    private var notificationService: NotificationService= NotificationService()

    init {
        instance = this
    }

    fun start(context: Context, scope: CoroutineScope, onShow: () -> Unit, onHide: () -> Unit) {
        vibrate(context)
        onShow()
        notificationService.showStopNotification(context)
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
        notificationService.cancelNotification(context)
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