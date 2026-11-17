package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackState
import com.example.audio.WlkmnAudioEngine
import com.example.data.AlbumGroup
import com.example.data.AudioImporter
import com.example.data.CassetteShellStyle
import com.example.data.LibraryViewMode
import com.example.data.TapeFilterPreset
import com.example.data.TapeSettings
import com.example.data.TapeSettingsRepository
import com.example.data.TrackEntity
import com.example.data.WlkmnDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WlkmnUiState(
    val tracks: List<TrackEntity> = emptyList(),
    val filteredTracks: List<TrackEntity> = emptyList(),
    val albumGroups: List<AlbumGroup> = emptyList(),
    val viewMode: LibraryViewMode = LibraryViewMode.ALBUMS,
    val searchQuery: String = "",
    val playback: AudioPlaybackState = AudioPlaybackState(),
    val settings: TapeSettings = TapeSettings(),
    val isImporting: Boolean = false,
    val statusBanner: String? = null
)

class WlkmnViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WlkmnDatabase.getInstance(application)
    private val trackDao = database.trackDao()
    private val settingsRepo = TapeSettingsRepository(application)
    private val audioEngine = WlkmnAudioEngine(application)

    private val viewModeFlow = MutableStateFlow(LibraryViewMode.ALBUMS)
    private val searchQueryFlow = MutableStateFlow("")
    private val isImportingFlow = MutableStateFlow(false)
    private val statusBannerFlow = MutableStateFlow<String?>(null)

    private val libraryControlsFlow = combine(
        viewModeFlow,
        searchQueryFlow,
        isImportingFlow,
        statusBannerFlow
    ) { mode, query, importing, banner ->
        LibraryControls(mode, query, importing, banner)
    }

    val uiState: StateFlow<WlkmnUiState> = combine(
        trackDao.observeAllTracks(),
        audioEngine.state,
        settingsRepo.settingsFlow,
        libraryControlsFlow
    ) { tracks, playback, settings, controls ->
        val trimmed = controls.query.trim().lowercase()
        val filtered = if (trimmed.isEmpty()) {
            tracks
        } else {
            tracks.filter {
                it.title.lowercase().contains(trimmed) ||
                    it.artist.lowercase().contains(trimmed) ||
                    it.album.lowercase().contains(trimmed)
            }
        }

        val albums = filtered
            .groupBy { it.album.ifBlank { "Mixtape Singles" } }
            .entries
            .map { (albumName, albumTracks) ->
                val artists = albumTracks.map { it.artist }.distinct()
                val artistSummary = when {
                    artists.isEmpty() -> "Various Artists"
                    artists.size == 1 -> artists.first()
                    else -> "${artists.first()} + ${artists.size - 1} more"
                }
                AlbumGroup(
                    albumName = albumName,
                    artistSummary = artistSummary,
                    tracks = albumTracks
                )
            }

        WlkmnUiState(
            tracks = tracks,
            filteredTracks = filtered,
            albumGroups = albums,
            viewMode = controls.viewMode,
            searchQuery = controls.query,
            playback = playback,
            settings = settings,
            isImporting = controls.isImporting,
            statusBanner = controls.statusBanner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = WlkmnUiState()
    )

    init {
        seedStarterCassetteIfNeeded()

        viewModelScope.launch {
            trackDao.observeAllTracks().collect { tracks ->
                audioEngine.updateQueue(tracks)
            }
        }

        viewModelScope.launch {
            settingsRepo.settingsFlow.collect { settings ->
                audioEngine.applySettings(settings)
            }
        }
    }

    private fun seedStarterCassetteIfNeeded() {
        viewModelScope.launch {
            if (!settingsRepo.hasSeededStarterTape() && trackDao.countTracks() == 0) {
                val starterTracks = listOf(
                    TrackEntity(
                        title = "Midnight Ferric",
                        artist = "Analog Tape Lab",
                        album = "WLKMN Starter Mix (Demo)",
                        durationMs = 16000L,
                        uriString = "",
                        rawResName = "midnight_ferric",
                        trackNumber = 1,
                        isDemo = true
                    ),
                    TrackEntity(
                        title = "Neon Boulevard",
                        artist = "Tokyo Cassette 84",
                        album = "WLKMN Starter Mix (Demo)",
                        durationMs = 16000L,
                        uriString = "",
                        rawResName = "neon_boulevard",
                        trackNumber = 2,
                        isDemo = true
                    ),
                    TrackEntity(
                        title = "Silver Cassette Rain",
                        artist = "CrO2 Echoes",
                        album = "WLKMN Starter Mix (Demo)",
                        durationMs = 16000L,
                        uriString = "",
                        rawResName = "silver_cassette_rain",
                        trackNumber = 3,
                        isDemo = true
                    )
                )
                trackDao.insertTracks(starterTracks)
                settingsRepo.markStarterTapeSeeded()
            }
        }
    }

    fun setViewMode(mode: LibraryViewMode) {
        viewModeFlow.value = mode
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun importAudioFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            isImportingFlow.value = true
            val imported = AudioImporter.importAudioUris(getApplication(), uris)
            if (imported.isNotEmpty()) {
                trackDao.insertTracks(imported)
                statusBannerFlow.value = "Added ${imported.size} track(s) to handwritten list"
            } else {
                statusBannerFlow.value = "No playable audio files found"
            }
            isImportingFlow.value = false
        }
    }

    fun importAudioFolder(treeUri: Uri?) {
        if (treeUri == null) return
        viewModelScope.launch {
            isImportingFlow.value = true
            val imported = AudioImporter.importAudioTree(getApplication(), treeUri)
            if (imported.isNotEmpty()) {
                trackDao.insertTracks(imported)
                statusBannerFlow.value = "Added ${imported.size} track(s) from folder"
            } else {
                statusBannerFlow.value = "No audio files found in selected folder"
            }
            isImportingFlow.value = false
        }
    }

    fun removeTrack(trackId: Long) {
        viewModelScope.launch {
            trackDao.deleteTrackById(trackId)
        }
    }

    fun clearDemoTracks() {
        viewModelScope.launch {
            trackDao.deleteDemoTracks()
            statusBannerFlow.value = "Removed demo cassette tracks"
        }
    }

    fun playTrack(track: TrackEntity) {
        val queue = uiState.value.filteredTracks.ifEmpty { uiState.value.tracks }
        audioEngine.playTrack(track, queue)
    }

    fun playAlbum(album: AlbumGroup) {
        val first = album.tracks.firstOrNull() ?: return
        audioEngine.playTrack(first, album.tracks)
    }

    fun togglePlayPause() {
        audioEngine.togglePlayPause()
    }

    fun stopPlayback() {
        audioEngine.stop()
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun skipNext() {
        audioEngine.skipNext()
    }

    fun skipPrevious() {
        audioEngine.skipPrevious()
    }

    fun selectTapeFilterPreset(preset: TapeFilterPreset) {
        viewModelScope.launch {
            settingsRepo.selectPreset(preset)
        }
    }

    fun updateEqBand(bandIndex: Int, levelMb: Int) {
        viewModelScope.launch {
            settingsRepo.updateEqBand(bandIndex, levelMb)
        }
    }

    fun updateTapeWarmth(warmth: Float) {
        viewModelScope.launch {
            settingsRepo.updateWarmth(warmth)
        }
    }

    fun updateTapeHiss(hiss: Float) {
        viewModelScope.launch {
            settingsRepo.updateHiss(hiss)
        }
    }

    fun updateTapeSpeed(speed: Float) {
        viewModelScope.launch {
            settingsRepo.updateSpeed(speed)
        }
    }

    fun updateShellStyle(style: CassetteShellStyle) {
        viewModelScope.launch {
            settingsRepo.updateShellStyle(style)
        }
    }

    fun toggleShuffle() {
        viewModelScope.launch {
            settingsRepo.toggleShuffle()
        }
    }

    fun cycleRepeatMode() {
        viewModelScope.launch {
            settingsRepo.cycleRepeatMode()
        }
    }

    fun dismissBanner() {
        statusBannerFlow.value = null
        audioEngine.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }

    private data class LibraryControls(
        val viewMode: LibraryViewMode,
        val query: String,
        val isImporting: Boolean,
        val statusBanner: String?
    )
}
