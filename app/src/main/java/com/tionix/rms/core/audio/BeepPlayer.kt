package com.tionix.rms.core.audio

import android.media.AudioManager
import android.media.ToneGenerator
import com.tionix.rms.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BeepPlayer
 * ==========
 * Provides audible feedback for Honeywell scans:
 *  - positive(): successful or "In" scan (150ms)
 *  - error(): duplicate, mismatch, or "Out" scan (400ms)
 *  - warning(): capacity or soft alerts (250ms)
 *
 * Audio is routed to STREAM_MUSIC at maximum volume (100) as specified by the master requirements.
 * Playback automatically checks the user's scan beep preference in SettingsRepository.
 */
@Singleton
class BeepPlayer @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            // Initializing ToneGenerator with STREAM_MUSIC (3) and volume level 100
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    /**
     * Plays the specified tone for the given duration, respecting the user's settings.
     */
    private fun playTone(toneType: Int, durationMs: Int) {
        val tg = toneGenerator ?: return
        CoroutineScope(Dispatchers.Default).launch {
            val settings = settingsRepository.getSettings().getOrNull()
            // Only play audio if scannerBeep is enabled in preferences
            if (settings == null || settings.scannerBeep) {
                try {
                    tg.startTone(toneType, durationMs)
                } catch (e: Exception) {
                    // Safe catch-all if system fails to emit tone
                }
            }
        }
    }

    /** Plays positive beep: TONE_PROP_ACK, 150ms */
    fun positive() {
        playTone(ToneGenerator.TONE_PROP_ACK, 150)
    }

    /** Plays error beep: TONE_SUP_ERROR, 400ms */
    fun error() {
        playTone(ToneGenerator.TONE_SUP_ERROR, 400)
    }

    /** Plays warning beep: TONE_PROP_NACK, 250ms */
    fun warning() {
        playTone(ToneGenerator.TONE_PROP_NACK, 250)
    }

    /** Releases native resources */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
