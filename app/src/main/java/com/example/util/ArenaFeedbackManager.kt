package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Self-contained procedural audio & haptic engine for AAA chess arena tactile feedback.
 * Synthesizes crystal-clear acoustic waveforms (piece clack, dash whoosh, explosive shatter, check alarm, fanfare)
 * and multi-stage haptic vibrations without external audio files.
 */
class ArenaFeedbackManager(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    fun onPieceSelected() {
        if (hapticsEnabled) vibrateClick(14, 70)
        if (soundEnabled) {
            audioScope.launch {
                playSynthesizedTone(freqStart = 520.0, freqEnd = 740.0, durationMs = 45, noiseMix = 0.05)
            }
        }
    }

    fun onNormalMove() {
        if (hapticsEnabled) vibrateClick(25, 130)
        if (soundEnabled) {
            audioScope.launch {
                // Warm marble stone placement thud
                playSynthesizedTone(freqStart = 210.0, freqEnd = 115.0, durationMs = 85, noiseMix = 0.22)
            }
        }
    }

    fun onKillDashStart() {
        if (hapticsEnabled) vibrateClick(35, 160)
        if (soundEnabled) {
            audioScope.launch {
                // High-velocity sword/comet whoosh
                playSynthesizedTone(freqStart = 180.0, freqEnd = 690.0, durationMs = 240, noiseMix = 0.38)
            }
        }
    }

    fun onKillImpactExplosion() {
        if (hapticsEnabled) {
            vibrateWaveform(
                timings = longArrayOf(0, 65, 30, 95, 40, 55),
                amplitudes = intArrayOf(0, 255, 80, 220, 50, 140)
            )
        }
        if (soundEnabled) {
            audioScope.launch {
                // Heavy bass impact + shattering stone/glass high-frequency burst
                playExplosionShatterSound()
            }
        }
    }

    fun onKingInCheck() {
        if (hapticsEnabled) {
            vibrateWaveform(
                timings = longArrayOf(0, 80, 60, 110),
                amplitudes = intArrayOf(0, 230, 0, 255)
            )
        }
        if (soundEnabled) {
            audioScope.launch {
                playSynthesizedTone(freqStart = 440.0, freqEnd = 330.0, durationMs = 260, noiseMix = 0.08)
            }
        }
    }

    fun onVictoryOrGameOver() {
        if (hapticsEnabled) {
            vibrateWaveform(
                timings = longArrayOf(0, 70, 50, 70, 50, 180),
                amplitudes = intArrayOf(0, 180, 0, 210, 0, 255)
            )
        }
        if (soundEnabled) {
            audioScope.launch {
                playSynthesizedTone(freqStart = 523.25, freqEnd = 659.25, durationMs = 180, noiseMix = 0.03)
                playSynthesizedTone(freqStart = 659.25, freqEnd = 783.99, durationMs = 260, noiseMix = 0.03)
            }
        }
    }

    private fun vibrateClick(durationMs: Long, amplitude: Int) {
        try {
            val vibrator = getVibrator() ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Throwable) {
            // Ignore on devices without vibrator support
        }
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        try {
            val vibrator = getVibrator() ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(180L)
            }
        } catch (_: Throwable) {
        }
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun playSynthesizedTone(
        freqStart: Double,
        freqEnd: Double,
        durationMs: Int,
        noiseMix: Double
    ) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val env = exp(-4.2 * t)
                val freq = freqStart + (freqEnd - freqStart) * t
                phase += 2.0 * PI * freq / sampleRate
                val tone = sin(phase)
                val noise = (Random.nextDouble() * 2.0 - 1.0)
                val combined = ((1.0 - noiseMix) * tone + noiseMix * noise) * env
                samples[i] = (combined * 18000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            writeAudioSamples(samples, sampleRate)
        } catch (_: Throwable) {
        }
    }

    private fun playExplosionShatterSound() {
        try {
            val sampleRate = 22050
            val durationMs = 480
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)
            var subPhase = 0.0
            var chimePhase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                // Deep sub-bass boom
                val subFreq = 135.0 * exp(-5.5 * t) + 38.0
                subPhase += 2.0 * PI * subFreq / sampleRate
                val subBoom = sin(subPhase) * exp(-4.0 * t)

                // Crystalline marble/obsidian shatter crackle
                val crackleEnv = exp(-6.5 * t) * (if (i % 180 < 45) 1.2 else 0.5)
                val noise = (Random.nextDouble() * 2.0 - 1.0) * crackleEnv

                // Metallic resonance ring
                chimePhase += 2.0 * PI * 920.0 / sampleRate
                val ring = sin(chimePhase) * exp(-9.0 * t) * 0.3

                val mix = (subBoom * 0.58 + noise * 0.34 + ring * 0.18).coerceIn(-1.0, 1.0)
                samples[i] = (mix * 24000).toInt().toShort()
            }
            writeAudioSamples(samples, sampleRate)
        } catch (_: Throwable) {
        }
    }

    private fun writeAudioSamples(samples: ShortArray, sampleRate: Int) {
        val bufferSize = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize.coerceAtLeast(1024))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.play()
        Thread.sleep((samples.size * 1000L / sampleRate) + 40L)
        track.stop()
        track.release()
    }
}
