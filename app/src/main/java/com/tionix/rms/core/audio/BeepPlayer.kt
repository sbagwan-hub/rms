package com.tionix.rms.core.audio

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.tionix.rms.core.settings.AppSettingsStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BeepPlayer @Inject constructor(
    private val appSettingsStore: AppSettingsStore
) {
    private var toneGenerator: ToneGenerator? = null
    var isMuted: Boolean = false

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {
            toneGenerator = null
        }
        CoroutineScope(Dispatchers.IO).launch {
            appSettingsStore.soundMutedFlow.collect { muted ->
                isMuted = muted
            }
        }
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        if (isMuted) return
        val tg = toneGenerator ?: return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                tg.startTone(toneType, durationMs)
            } catch (_: Exception) {
                // Ignore tone playback failures on unsupported devices.
            }
        }
    }

    fun positive() {
        playTone(ToneGenerator.TONE_PROP_ACK, 150)
    }

    fun error() {
        playTone(ToneGenerator.TONE_SUP_ERROR, 400)
    }

    fun warning() {
        playTone(ToneGenerator.TONE_PROP_NACK, 250)
    }

    fun testPositive() = positive()
    fun testError() = error()
    fun testWarning() = warning()

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
