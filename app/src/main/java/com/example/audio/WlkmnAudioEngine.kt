package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import com.example.data.RepeatMode
import com.example.data.TapeSettings
import com.example.data.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlaybackState(
    val currentTrack: TrackEntity? = null,
    val isPlaying: Boolean = false,
    val isPrepared: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bandCenterFreqsHz: List<Int> = listOf(60, 230, 910, 3600, 14000),
    val minBandMb: Int = -1500,
    val maxBandMb: Int = 1500,
    val isHardwareEqActive: Boolean = false,
    val errorMessage: String? = null
)

class WlkmnAudioEngine(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private val hissGenerator = TapeHissGenerator()

    private var currentQueue: List<TrackEntity> = emptyList()
    private var currentSettings: TapeSettings = TapeSettings()
    private var tickerJob: Job? = null

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    fun updateQueue(queue: List<TrackEntity>) {
        currentQueue = queue
        val active = _state.value.currentTrack
        if (active != null && queue.none { it.id == active.id }) {
            stop()
            _state.update { it.copy(currentTrack = null, durationMs = 0L, positionMs = 0L) }
        }
    }

    fun applySettings(settings: TapeSettings) {
        currentSettings = settings
        applyEqAndEffects()
        applyPlaybackSpeed()
        hissGenerator.update(_state.value.isPlaying, settings.tapeHiss)
    }

    fun playTrack(track: TrackEntity, queue: List<TrackEntity>) {
        currentQueue = queue
        releasePlayer()

        _state.update {
            it.copy(
                currentTrack = track,
                isPlaying = false,
                isPrepared = false,
                positionMs = 0L,
                durationMs = track.durationMs,
                errorMessage = null
            )
        }

        val player = MediaPlayer()
        mediaPlayer = player

        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            val rawName = track.rawResName
            if (rawName != null) {
                val resId = context.resources.getIdentifier(rawName, "raw", context.packageName)
                if (resId == 0) {
                    handleError("Audio resource not found: $rawName")
                    return
                }
                val afd = context.resources.openRawResourceFd(resId)
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else {
                val uri = Uri.parse(track.uriString)
                player.setDataSource(context, uri)
            }

            player.setOnPreparedListener { mp ->
                attachAudioEffects(mp.audioSessionId)
                applyEqAndEffects()
                applyPlaybackSpeed()

                val duration = mp.duration.toLong().coerceAtLeast(track.durationMs)
                mp.start()
                _state.update {
                    it.copy(
                        isPlaying = true,
                        isPrepared = true,
                        durationMs = duration,
                        positionMs = 0L,
                        errorMessage = null
                    )
                }
                hissGenerator.update(true, currentSettings.tapeHiss)
                startProgressTicker()
            }

            player.setOnCompletionListener {
                handleTrackCompletion()
            }

            player.setOnErrorListener { _, what, extra ->
                handleError("Playback error ($what/$extra) for ${track.title}")
                true
            }

            player.prepareAsync()
        } catch (e: Exception) {
            handleError("Could not open ${track.title}: ${e.localizedMessage ?: "Unsupported file"}")
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer
        val activeTrack = _state.value.currentTrack
        if (mp == null || !_state.value.isPrepared) {
            val target = activeTrack ?: currentQueue.firstOrNull()
            if (target != null) {
                playTrack(target, currentQueue)
            }
            return
        }

        runCatching {
            if (mp.isPlaying) {
                mp.pause()
                _state.update { it.copy(isPlaying = false) }
                hissGenerator.update(false, currentSettings.tapeHiss)
                stopProgressTicker()
            } else {
                applyPlaybackSpeed()
                mp.start()
                _state.update { it.copy(isPlaying = true) }
                hissGenerator.update(true, currentSettings.tapeHiss)
                startProgressTicker()
            }
        }
    }

    fun stop() {
        stopProgressTicker()
        hissGenerator.stop()
        val mp = mediaPlayer
        if (mp != null && _state.value.isPrepared) {
            runCatching {
                if (mp.isPlaying) mp.pause()
                mp.seekTo(0)
            }
        }
        _state.update {
            it.copy(
                isPlaying = false,
                positionMs = 0L
            )
        }
    }

    fun seekTo(positionMs: Long) {
        val mp = mediaPlayer ?: return
        if (!_state.value.isPrepared) return
        val clamped = positionMs.coerceIn(0L, _state.value.durationMs.coerceAtLeast(1L))
        runCatching {
            mp.seekTo(clamped.toInt())
            _state.update { it.copy(positionMs = clamped) }
        }
    }

    fun skipNext() {
        if (currentQueue.isEmpty()) return
        val current = _state.value.currentTrack
        if (currentSettings.isShuffle && currentQueue.size > 1) {
            val candidates = currentQueue.filter { it.id != current?.id }
            playTrack(candidates.random(), currentQueue)
            return
        }
        val idx = currentQueue.indexOfFirst { it.id == current?.id }
        val nextIdx = if (idx < 0) 0 else (idx + 1) % currentQueue.size
        playTrack(currentQueue[nextIdx], currentQueue)
    }

    fun skipPrevious() {
        if (currentQueue.isEmpty()) return
        if (_state.value.positionMs > 3000L) {
            seekTo(0L)
            return
        }
        val current = _state.value.currentTrack
        val idx = currentQueue.indexOfFirst { it.id == current?.id }
        val prevIdx = if (idx <= 0) currentQueue.lastIndex else idx - 1
        playTrack(currentQueue[prevIdx], currentQueue)
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun handleTrackCompletion() {
        if (currentQueue.isEmpty()) {
            stop()
            return
        }
        when (currentSettings.repeatMode) {
            RepeatMode.ONE -> {
                _state.value.currentTrack?.let { playTrack(it, currentQueue) }
            }
            RepeatMode.ALL -> {
                skipNext()
            }
            RepeatMode.OFF -> {
                val idx = currentQueue.indexOfFirst { it.id == _state.value.currentTrack?.id }
                if (currentSettings.isShuffle || (idx >= 0 && idx < currentQueue.lastIndex)) {
                    skipNext()
                } else {
                    stop()
                }
            }
        }
    }

    private fun attachAudioEffects(sessionId: Int) {
        releaseEffects()
        runCatching {
            val eq = Equalizer(0, sessionId).apply { enabled = true }
            equalizer = eq

            val numBands = eq.numberOfBands.toInt().coerceAtLeast(1)
            val freqs = (0 until numBands).map { band ->
                (eq.getCenterFreq(band.toShort()) / 1000).coerceAtLeast(20)
            }
            val range = eq.bandLevelRange
            val minMb = range.getOrNull(0)?.toInt() ?: -1500
            val maxMb = range.getOrNull(1)?.toInt() ?: 1500

            _state.update {
                it.copy(
                    bandCenterFreqsHz = if (freqs.size >= 5) freqs.take(5) else it.bandCenterFreqsHz,
                    minBandMb = minMb,
                    maxBandMb = maxMb,
                    isHardwareEqActive = true
                )
            }
        }.onFailure {
            _state.update { it.copy(isHardwareEqActive = false) }
        }

        runCatching {
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
        }
    }

    private fun applyEqAndEffects() {
        val eq = equalizer
        if (eq != null) {
            runCatching {
                eq.enabled = true
                val numBands = eq.numberOfBands.toInt()
                val minMb = _state.value.minBandMb
                val maxMb = _state.value.maxBandMb
                for (i in 0 until minOf(numBands, currentSettings.eqBandsMb.size)) {
                    val target = currentSettings.eqBandsMb[i].coerceIn(minMb, maxMb).toShort()
                    eq.setBandLevel(i.toShort(), target)
                }
            }
        }

        val bb = bassBoost
        if (bb != null) {
            runCatching {
                if (bb.strengthSupported) {
                    val strength = (currentSettings.tapeWarmth * 850f).toInt().coerceIn(0, 1000).toShort()
                    bb.enabled = strength > 0
                    bb.setStrength(strength)
                }
            }
        }
    }

    private fun applyPlaybackSpeed() {
        val mp = mediaPlayer ?: return
        if (!_state.value.isPrepared && !_state.value.isPlaying) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            runCatching {
                val params = mp.playbackParams
                params.speed = currentSettings.tapeSpeed.coerceIn(0.85f, 1.15f)
                mp.playbackParams = params
            }
        }
    }

    private fun startProgressTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val mp = mediaPlayer
                if (mp != null && _state.value.isPrepared) {
                    runCatching {
                        val pos = mp.currentPosition.toLong()
                        val dur = mp.duration.toLong().coerceAtLeast(_state.value.durationMs)
                        _state.update {
                            it.copy(
                                positionMs = pos.coerceAtLeast(0L),
                                durationMs = dur,
                                isPlaying = mp.isPlaying
                            )
                        }
                    }
                }
                delay(120L)
            }
        }
    }

    private fun stopProgressTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun handleError(message: String) {
        stopProgressTicker()
        hissGenerator.stop()
        _state.update {
            it.copy(
                isPlaying = false,
                isPrepared = false,
                errorMessage = message
            )
        }
    }

    private fun releaseEffects() {
        runCatching { equalizer?.release() }
        equalizer = null
        runCatching { bassBoost?.release() }
        bassBoost = null
    }

    private fun releasePlayer() {
        stopProgressTicker()
        hissGenerator.stop()
        releaseEffects()
        runCatching {
            mediaPlayer?.stop()
        }
        runCatching {
            mediaPlayer?.release()
        }
        mediaPlayer = null
    }

    fun release() {
        releasePlayer()
    }
}
