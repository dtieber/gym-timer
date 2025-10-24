package com.dti.gymtimer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log

private const val TAG = "GymTimer-SoundService"

class SoundService {
    private var player: MediaPlayer? = null
    private var prevSpeaker: Boolean? = null
    private var prevMode: Int? = null

    fun playSound(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val headphoneDevice = findHeadphoneDevice(am)
        saveAudioState(am)
        configureAudio(am, headphoneDevice != null)
        player = createPlayer(context, headphoneDevice)
        if (player != null) Log.d(TAG, "Sound started")
    }

    fun stopSound(context: Context) {
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
        prevSpeaker = try {
            val outputDevices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            outputDevices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
        } catch (_: Exception) {
            null
        }
        prevMode = try {
            am.mode
        } catch (_: Exception) {
            null
        }
    }

    private fun configureAudio(am: AudioManager, hasHeadphones: Boolean) {
        try {
            am.mode = AudioManager.MODE_NORMAL
            val target = am.availableCommunicationDevices.find {
                if (hasHeadphones) it.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                else it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }
            target?.let {
                am.setCommunicationDevice(it)
                Log.d(
                    TAG,
                    "Selected device: ${it.productName} (type=${it.type}), Speaker active=${it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "configureAudio", e)
        }
    }

    private fun createPlayer(context: Context, headphoneDevice: AudioDeviceInfo?): MediaPlayer? {
        return try {
            val afd = context.resources.openRawResourceFd(R.raw.alarm)
            MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                prepare()
                headphoneDevice?.let {
                    val ok = setPreferredDevice(it)
                    Log.d(TAG, "Routing alarm to ${it.productName}, success=$ok")
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
            prevSpeaker?.let {
                val target = am.availableCommunicationDevices.find { device ->
                    (it && device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) ||
                            (!it && device.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
                }
                target?.let { am.setCommunicationDevice(it) }
            }
            prevMode?.let { am.mode = it }
            Log.d(TAG, "Audio restored")
        } catch (e: Exception) {
            Log.e(TAG, "restoreAudio", e)
        }
    }

}