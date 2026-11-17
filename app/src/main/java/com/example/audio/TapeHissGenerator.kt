package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.random.Random

/**
 * Generates soft, warm low-pass filtered analog cassette hiss using AudioTrack
 * while playback is active and hissLevel > 0.
 */
class TapeHissGenerator {

    private var audioTrack: AudioTrack? = null
    private var currentLevel: Float = 0f
    private var isRunning: Boolean = false

    fun update(shouldPlay: Boolean, hissLevel: Float) {
        val clamped = hissLevel.coerceIn(0f, 1f)
        currentLevel = clamped
        if (shouldPlay && clamped > 0.01f) {
            startIfNeeded()
            runCatching {
                // Keep analog hiss subtle and realistic (max 0.055 linear gain)
                audioTrack?.setVolume(clamped * 0.055f)
            }
        } else {
            stop()
        }
    }

    private fun startIfNeeded() {
        if (isRunning && audioTrack != null) return
        runCatching {
            val sampleRate = 22050
            val numSamples = sampleRate * 2 // 2-second seamless buffer
            val pcm = ShortArray(numSamples)

            // Generate warm tape-shaped noise (one-pole low-pass + subtle 60Hz hum warmth)
            var lp = 0f
            val alpha = 0.28f
            for (i in 0 until numSamples) {
                val white = (Random.nextFloat() * 2f) - 1f
                lp += alpha * (white - lp)
                val sample = (lp * 12000f).toInt().coerceIn(-32767, 32767)
                pcm[i] = sample.toShort()
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(numSamples * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf.coerceAtLeast(numSamples * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.setLoopPoints(0, pcm.size, -1)
            track.setVolume(currentLevel * 0.055f)
            track.play()
            audioTrack = track
            isRunning = true
        }
    }

    fun stop() {
        if (!isRunning && audioTrack == null) return
        isRunning = false
        runCatching {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        }
        audioTrack = null
    }
}
