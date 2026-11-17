package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.CassetteDeckScreen
import com.example.ui.components.CustomizerScreen
import com.example.ui.components.HandwrittenSongListScreen
import com.example.ui.components.LandscapeFullCassetteScreen
import com.example.ui.components.MiniCassetteBar
import com.example.ui.theme.HardwareDisplayFont
import com.example.ui.theme.NavyChassis
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverMetallic
import com.example.ui.theme.WalkmanRed

enum class WlkmnDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
) {
    SONG_LIST(
        route = "song_list",
        labelRes = R.string.nav_song_list,
        icon = Icons.AutoMirrored.Filled.QueueMusic,
        testTag = "nav_song_list"
    ),
    CASSETTE_DECK(
        route = "cassette_deck",
        labelRes = R.string.nav_cassette_deck,
        icon = Icons.Default.Album,
        testTag = "nav_cassette_deck"
    ),
    CUSTOMIZER(
        route = "customizer",
        labelRes = R.string.nav_customizer,
        icon = Icons.Default.Tune,
        testTag = "nav_customizer"
    )
}

@Composable
fun WlkmnApp(
    viewModel: WlkmnViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentDestination by rememberSaveable { mutableStateOf(WlkmnDestination.SONG_LIST) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Back navigation returns from Deck or Customizer to Song List
    BackHandler(enabled = currentDestination != WlkmnDestination.SONG_LIST) {
        currentDestination = WlkmnDestination.SONG_LIST
    }

    val openAudioFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importAudioFiles(uris)
        }
    }

    val openAudioFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            viewModel.importAudioFolder(treeUri)
        }
    }

    val activeMessage = uiState.playback.errorMessage ?: uiState.statusBanner
    LaunchedEffect(activeMessage) {
        if (activeMessage != null) {
            snackbarHostState.showSnackbar(activeMessage)
            viewModel.dismissBanner()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        val isLandscape = maxWidth > maxHeight
        val isExpanded = maxWidth >= 840.dp && !isLandscape

        if (isLandscape) {
            LandscapeFullCassetteScreen(
                uiState = uiState,
                onTogglePlayPause = viewModel::togglePlayPause,
                onSkipNext = viewModel::skipNext,
                onSkipPrevious = viewModel::skipPrevious
            )
        } else if (isExpanded) {
            // Adaptive Expanded Layout (Tablets / Foldables): NavigationRail + Split Deck & List/Customizer
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                NavigationRail(
                    containerColor = PitchBlack,
                    contentColor = PureWhite
                ) {
                    val railItems = listOf(WlkmnDestination.SONG_LIST, WlkmnDestination.CUSTOMIZER)
                    railItems.forEach { dest ->
                        val selected = currentDestination == dest ||
                            (currentDestination == WlkmnDestination.CASSETTE_DECK && dest == WlkmnDestination.SONG_LIST)
                        NavigationRailItem(
                            selected = selected,
                            onClick = { currentDestination = dest },
                            icon = {
                                Icon(
                                    imageVector = dest.icon,
                                    contentDescription = stringResource(dest.labelRes)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(dest.labelRes),
                                    fontFamily = HardwareDisplayFont,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = PureWhite,
                                selectedTextColor = PureWhite,
                                indicatorColor = WalkmanRed,
                                unselectedIconColor = SilverMetallic,
                                unselectedTextColor = SilverMetallic
                            ),
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }

                // Left Pane: Live Cassette Deck always visible on Expanded screens
                Box(
                    modifier = Modifier
                        .weight(0.48f)
                        .fillMaxHeight()
                ) {
                    CassetteDeckScreen(
                        uiState = uiState,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onStop = viewModel::stopPlayback,
                        onSeekTo = viewModel::seekTo,
                        onSkipNext = viewModel::skipNext,
                        onSkipPrevious = viewModel::skipPrevious,
                        onToggleShuffle = viewModel::toggleShuffle,
                        onCycleRepeat = viewModel::cycleRepeatMode,
                        onOpenCustomizer = { currentDestination = WlkmnDestination.CUSTOMIZER },
                        onOpenSongList = { currentDestination = WlkmnDestination.SONG_LIST }
                    )
                }

                // Right Pane: Handwritten Song List or Tape Filter Customizer
                Box(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                ) {
                    if (currentDestination == WlkmnDestination.CUSTOMIZER) {
                        CustomizerScreen(
                            uiState = uiState,
                            onSelectPreset = viewModel::selectTapeFilterPreset,
                            onEqBandChange = viewModel::updateEqBand,
                            onWarmthChange = viewModel::updateTapeWarmth,
                            onHissChange = viewModel::updateTapeHiss,
                            onSpeedChange = viewModel::updateTapeSpeed,
                            onShellStyleChange = viewModel::updateShellStyle
                        )
                    } else {
                        HandwrittenSongListScreen(
                            uiState = uiState,
                            onAddFilesClick = { openAudioFilesLauncher.launch(arrayOf("audio/*")) },
                            onAddFolderClick = { openAudioFolderLauncher.launch(null) },
                            onSelectTrack = { track -> viewModel.playTrack(track) },
                            onPlayAlbum = { album -> viewModel.playAlbum(album) },
                            onRemoveTrack = viewModel::removeTrack,
                            onClearDemoTracks = viewModel::clearDemoTracks,
                            onViewModeChange = viewModel::setViewMode,
                            onSearchQueryChange = viewModel::setSearchQuery
                        )
                    }
                }
            }
        } else {
            // Compact / Medium Layout: Scaffold with Bottom NavigationBar + MiniCassetteBar
            Scaffold(
                containerColor = NavyDeep,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    Column(modifier = Modifier.background(PitchBlack)) {
                        val currentTrack = uiState.playback.currentTrack
                        if (currentTrack != null && currentDestination != WlkmnDestination.CASSETTE_DECK) {
                            MiniCassetteBar(
                                track = currentTrack,
                                isPlaying = uiState.playback.isPlaying,
                                tapePreset = uiState.settings.preset,
                                onOpenDeck = { currentDestination = WlkmnDestination.CASSETTE_DECK },
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onSkipNext = viewModel::skipNext
                            )
                        }

                        NavigationBar(
                            containerColor = PitchBlack,
                            contentColor = PureWhite
                        ) {
                            WlkmnDestination.entries.forEach { dest ->
                                val selected = currentDestination == dest
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(dest.labelRes),
                                            fontFamily = HardwareDisplayFont,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PureWhite,
                                        selectedTextColor = PureWhite,
                                        indicatorColor = WalkmanRed,
                                        unselectedIconColor = SilverMetallic,
                                        unselectedTextColor = SilverMetallic
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(innerPadding)
                ) {
                    when (currentDestination) {
                        WlkmnDestination.SONG_LIST -> {
                            HandwrittenSongListScreen(
                                uiState = uiState,
                                onAddFilesClick = { openAudioFilesLauncher.launch(arrayOf("audio/*")) },
                                onAddFolderClick = { openAudioFolderLauncher.launch(null) },
                                onSelectTrack = { track ->
                                    viewModel.playTrack(track)
                                    currentDestination = WlkmnDestination.CASSETTE_DECK
                                },
                                onPlayAlbum = { album ->
                                    viewModel.playAlbum(album)
                                    currentDestination = WlkmnDestination.CASSETTE_DECK
                                },
                                onRemoveTrack = viewModel::removeTrack,
                                onClearDemoTracks = viewModel::clearDemoTracks,
                                onViewModeChange = viewModel::setViewMode,
                                onSearchQueryChange = viewModel::setSearchQuery
                            )
                        }

                        WlkmnDestination.CASSETTE_DECK -> {
                            CassetteDeckScreen(
                                uiState = uiState,
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onStop = viewModel::stopPlayback,
                                onSeekTo = viewModel::seekTo,
                                onSkipNext = viewModel::skipNext,
                                onSkipPrevious = viewModel::skipPrevious,
                                onToggleShuffle = viewModel::toggleShuffle,
                                onCycleRepeat = viewModel::cycleRepeatMode,
                                onOpenCustomizer = { currentDestination = WlkmnDestination.CUSTOMIZER },
                                onOpenSongList = { currentDestination = WlkmnDestination.SONG_LIST }
                            )
                        }

                        WlkmnDestination.CUSTOMIZER -> {
                            CustomizerScreen(
                                uiState = uiState,
                                onSelectPreset = viewModel::selectTapeFilterPreset,
                                onEqBandChange = viewModel::updateEqBand,
                                onWarmthChange = viewModel::updateTapeWarmth,
                                onHissChange = viewModel::updateTapeHiss,
                                onSpeedChange = viewModel::updateTapeSpeed,
                                onShellStyleChange = viewModel::updateShellStyle
                            )
                        }
                    }
                }
            }
        }
    }
}
