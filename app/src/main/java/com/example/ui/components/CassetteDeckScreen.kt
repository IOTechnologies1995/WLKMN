package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.RepeatMode
import com.example.data.TapeFilterPreset
import com.example.data.formatDurationMs
import com.example.ui.WlkmnUiState
import com.example.ui.theme.CharcoalPanel
import com.example.ui.theme.HardwareDisplayFont
import com.example.ui.theme.NavyChassis
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverBright
import com.example.ui.theme.SilverMetallic
import com.example.ui.theme.SilverMuted
import com.example.ui.theme.TechnicalMonoFont
import com.example.ui.theme.WalkmanRed
import com.example.ui.theme.WalkmanRedBright

@Composable
fun CassetteDeckScreen(
    uiState: WlkmnUiState,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenCustomizer: () -> Unit,
    onOpenSongList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playback = uiState.playback
    val settings = uiState.settings
    val durationMs = playback.durationMs.coerceAtLeast(1L)
    val currentProgress = (playback.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }
    val displayFraction = if (isScrubbing) scrubFraction else currentProgress

    // Simulated mechanical tape counter (0000 - 0999)
    val tapeCounter = ((playback.positionMs / 250L) % 1000L).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDeep, NavyChassis, PitchBlack)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Walkman Silver & Navy Top Bezel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(NavySurface, NavyChassis)
                        )
                    )
                    .border(1.dp, SilverMetallic.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WLKMN",
                        color = PureWhite,
                        fontFamily = HardwareDisplayFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "STEREO CASSETTE PLAYER",
                        color = SilverMetallic,
                        fontFamily = TechnicalMonoFont,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Live Red LED + Tape Filter Quick Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PitchBlack)
                            .border(1.dp, SilverMuted.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (playback.isPlaying) WalkmanRedBright else SilverMuted.copy(alpha = 0.4f))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (playback.isPlaying) "PLAY" else "STBY",
                            color = if (playback.isPlaying) PureWhite else SilverMuted,
                            fontFamily = TechnicalMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (settings.preset != TapeFilterPreset.OFF) WalkmanRed else CharcoalPanel)
                            .clickable(onClick = onOpenCustomizer)
                            .minimumInteractiveComponentSize()
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("deck_tape_filter_badge")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Open Tape Filter Customizer",
                            tint = PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = settings.preset.badge,
                            color = PureWhite,
                            fontFamily = TechnicalMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recessed Cassette Bay Window holding the animated Cassette Tape
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PitchBlack)
                    .border(2.dp, SilverMetallic.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    .padding(10.dp)
                    .clickable {
                        if (playback.currentTrack == null) onOpenSongList()
                    }
            ) {
                CassetteTapeVisual(
                    songTitle = playback.currentTrack?.title,
                    artistName = playback.currentTrack?.artist,
                    albumName = playback.currentTrack?.album,
                    isPlaying = playback.isPlaying,
                    progress = displayFraction,
                    speedMultiplier = settings.tapeSpeed,
                    shellStyle = settings.shellStyle,
                    tapePreset = settings.preset
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Black LCD Tape Counter + Time Scrubber Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PitchBlack)
                    .border(1.dp, SilverMetallic.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CNT %04d".format(tapeCounter),
                        color = WalkmanRedBright,
                        fontFamily = TechnicalMonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "SPD %.2fx • HISS %d%%".format(settings.tapeSpeed, (settings.tapeHiss * 100).toInt()),
                        color = SilverMetallic,
                        fontFamily = TechnicalMonoFont,
                        fontSize = 11.sp
                    )
                    val shownPosMs = (displayFraction * durationMs).toLong()
                    Text(
                        text = "${formatDurationMs(shownPosMs)} / ${formatDurationMs(playback.durationMs)}",
                        color = PureWhite,
                        fontFamily = TechnicalMonoFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                Slider(
                    value = displayFraction,
                    onValueChange = { fraction ->
                        isScrubbing = true
                        scrubFraction = fraction
                    },
                    onValueChangeFinished = {
                        isScrubbing = false
                        onSeekTo((scrubFraction * durationMs).toLong())
                    },
                    enabled = playback.currentTrack != null,
                    colors = SliderDefaults.colors(
                        thumbColor = SilverBright,
                        activeTrackColor = WalkmanRed,
                        inactiveTrackColor = NavySurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cassette_seek_slider")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Brushed Silver & Navy Mechanical Transport Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(SilverBright.copy(alpha = 0.14f), CharcoalPanel)
                        )
                    )
                    .border(1.dp, SilverMetallic.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("transport_shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = stringResource(R.string.action_shuffle),
                        tint = if (settings.isShuffle) WalkmanRedBright else SilverMuted
                    )
                }

                TransportKeyButton(
                    onClick = onSkipPrevious,
                    isAccent = false,
                    testTag = "transport_prev_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = stringResource(R.string.action_previous),
                        tint = PitchBlack,
                        modifier = Modifier.size(26.dp)
                    )
                }

                TransportKeyButton(
                    onClick = onTogglePlayPause,
                    isAccent = true,
                    sizeDp = 64,
                    testTag = "transport_play_pause_button"
                ) {
                    Icon(
                        imageVector = if (playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(
                            if (playback.isPlaying) R.string.action_pause else R.string.action_play
                        ),
                        tint = PureWhite,
                        modifier = Modifier.size(34.dp)
                    )
                }

                TransportKeyButton(
                    onClick = onStop,
                    isAccent = false,
                    testTag = "transport_stop_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = stringResource(R.string.action_stop),
                        tint = PitchBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }

                TransportKeyButton(
                    onClick = onSkipNext,
                    isAccent = false,
                    testTag = "transport_next_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.action_next),
                        tint = PitchBlack,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("transport_repeat_button")
                ) {
                    Icon(
                        imageVector = if (settings.repeatMode == RepeatMode.ONE) {
                            Icons.Default.RepeatOne
                        } else {
                            Icons.Default.Repeat
                        },
                        contentDescription = stringResource(R.string.action_repeat),
                        tint = if (settings.repeatMode == RepeatMode.OFF) SilverMuted else WalkmanRedBright
                    )
                }
            }
        }
    }
}

@Composable
private fun TransportKeyButton(
    onClick: () -> Unit,
    isAccent: Boolean,
    testTag: String,
    sizeDp: Int = 50,
    content: @Composable () -> Unit
) {
    val bgBrush = if (isAccent) {
        Brush.verticalGradient(listOf(WalkmanRedBright, WalkmanRed))
    } else {
        Brush.verticalGradient(listOf(PureWhite, SilverMetallic))
    }
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush)
            .border(
                width = 1.5.dp,
                color = if (isAccent) PureWhite.copy(alpha = 0.5f) else PitchBlack.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun LandscapeFullCassetteScreen(
    uiState: WlkmnUiState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playback = uiState.playback
    val settings = uiState.settings
    val durationMs = playback.durationMs.coerceAtLeast(1L)
    val progress = (playback.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

    // Show compact transport HUD when paused or when the user taps the cassette
    var showOverlayHud by remember(playback.isPlaying) {
        mutableStateOf(!playback.isPlaying)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(NavyChassis, PitchBlack)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showOverlayHud = !showOverlayHud
            }
            .testTag("landscape_cassette_root"),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val safeW = maxWidth
            val safeH = maxHeight
            val aspect = safeW / safeH.coerceAtLeast(1.dp)
            val targetW = if (aspect > 1.58f) safeH * 1.58f else safeW
            val targetH = if (aspect > 1.58f) safeH else safeW / 1.58f

            CassetteTapeVisual(
                songTitle = playback.currentTrack?.title,
                artistName = playback.currentTrack?.artist,
                albumName = playback.currentTrack?.album,
                isPlaying = playback.isPlaying,
                progress = progress,
                speedMultiplier = settings.tapeSpeed,
                shellStyle = settings.shellStyle,
                tapePreset = settings.preset,
                applyDefaultSizing = false,
                modifier = Modifier
                    .size(width = targetW, height = targetH)
                    .testTag("landscape_full_cassette")
            )
        }

        AnimatedVisibility(
            visible = showOverlayHud,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(PitchBlack.copy(alpha = 0.84f))
                    .border(1.dp, SilverMetallic.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("landscape_hud_pill"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("landscape_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = stringResource(R.string.action_previous),
                        tint = PureWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WalkmanRed)
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("landscape_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(
                            if (playback.isPlaying) R.string.action_pause else R.string.action_play
                        ),
                        tint = PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("landscape_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.action_next),
                        tint = PureWhite
                    )
                }

                Text(
                    text = "${formatDurationMs(playback.positionMs)} / ${formatDurationMs(playback.durationMs)}",
                    color = SilverBright,
                    fontFamily = TechnicalMonoFont,
                    fontSize = 11.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (settings.preset == TapeFilterPreset.OFF) NavySurface else WalkmanRed)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = settings.preset.badge,
                        color = PureWhite,
                        fontFamily = TechnicalMonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

