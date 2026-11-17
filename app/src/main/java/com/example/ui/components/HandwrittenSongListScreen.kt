package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AlbumGroup
import com.example.data.LibraryViewMode
import com.example.data.TrackEntity
import com.example.data.formatDurationMs
import com.example.ui.WlkmnUiState
import com.example.ui.theme.HandwrittenBodyFont
import com.example.ui.theme.HandwrittenScriptFont
import com.example.ui.theme.HardwareDisplayFont
import com.example.ui.theme.NavyChassis
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyInk
import com.example.ui.theme.PaperCream
import com.example.ui.theme.PaperLineNavy
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverBright
import com.example.ui.theme.SilverMetallic
import com.example.ui.theme.SilverMuted
import com.example.ui.theme.TechnicalMonoFont
import com.example.ui.theme.WalkmanRed

@Composable
fun HandwrittenSongListScreen(
    uiState: WlkmnUiState,
    onAddFilesClick: () -> Unit,
    onAddFolderClick: () -> Unit,
    onSelectTrack: (TrackEntity) -> Unit,
    onPlayAlbum: (AlbumGroup) -> Unit,
    onRemoveTrack: (Long) -> Unit,
    onClearDemoTracks: () -> Unit,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTrackId = uiState.playback.currentTrack?.id
    val hasDemoTracks = uiState.tracks.any { it.isDemo }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDeep)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Handwritten Notebook / Mixtape J-Card Sheet resting on Navy Walkman Desk
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp)
                .shadow(12.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(PaperCream)
                .border(1.dp, SilverMetallic, RoundedCornerShape(10.dp))
        ) {
            // Ruled Paper Lines, Red Margin Rule, and Binder Punch Holes
            Canvas(modifier = Modifier.fillMaxSize()) {
                val marginX = 46.dp.toPx()
                val lineSpacing = 34.dp.toPx()

                // Horizontal Navy Ink Notebook Rules
                var y = 62.dp.toPx()
                while (y < size.height) {
                    drawLine(
                        color = PaperLineNavy.copy(alpha = 0.16f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += lineSpacing
                }

                // Vertical Red Margin Line
                drawLine(
                    color = WalkmanRed.copy(alpha = 0.45f),
                    start = Offset(marginX, 0f),
                    end = Offset(marginX, size.height),
                    strokeWidth = 1.6.dp.toPx()
                )

                // Three Classic Punched Binder Holes on the left edge
                val holePositions = listOf(0.18f, 0.50f, 0.82f)
                holePositions.forEach { frac ->
                    val cy = size.height * frac
                    drawCircle(
                        color = NavyDeep,
                        radius = 7.dp.toPx(),
                        center = Offset(18.dp.toPx(), cy)
                    )
                    drawCircle(
                        color = SilverMuted.copy(alpha = 0.5f),
                        radius = 7.5.dp.toPx(),
                        center = Offset(18.dp.toPx(), cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Sheet Header (above red/navy margin)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 56.dp, end = 14.dp, top = 12.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.paper_header_title),
                                color = NavyInk,
                                fontFamily = HandwrittenScriptFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                lineHeight = 28.sp
                            )
                            // Hand-drawn red ink underline
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(4.dp)
                            ) {
                                drawLine(
                                    color = WalkmanRed,
                                    start = Offset(0f, 2.dp.toPx()),
                                    end = Offset(size.width, 1.dp.toPx()),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        Text(
                            text = "${uiState.filteredTracks.size} tracks",
                            color = NavyInk.copy(alpha = 0.7f),
                            fontFamily = HandwrittenBodyFont,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Row: + Add Audio, + Folder, and Albums/Songs Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PaperActionChip(
                            label = stringResource(R.string.action_add_files),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            containerColor = NavyChassis,
                            contentColor = PureWhite,
                            onClick = onAddFilesClick,
                            testTag = "add_audio_files_button"
                        )

                        PaperActionChip(
                            label = stringResource(R.string.action_add_folder),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.CreateNewFolder,
                                    contentDescription = null,
                                    tint = PitchBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            containerColor = SilverBright,
                            contentColor = PitchBlack,
                            onClick = onAddFolderClick,
                            testTag = "add_audio_folder_button"
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Handwritten Albums / Songs Switcher
                        PaperActionChip(
                            label = if (uiState.viewMode == LibraryViewMode.ALBUMS) "Albums" else "Songs",
                            icon = {
                                Icon(
                                    imageVector = if (uiState.viewMode == LibraryViewMode.ALBUMS) {
                                        Icons.Default.Album
                                    } else {
                                        Icons.Default.LibraryMusic
                                    },
                                    contentDescription = "Switch Song or Album View",
                                    tint = PureWhite,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            containerColor = WalkmanRed,
                            contentColor = PureWhite,
                            onClick = {
                                val next = if (uiState.viewMode == LibraryViewMode.ALBUMS) {
                                    LibraryViewMode.SONGS
                                } else {
                                    LibraryViewMode.ALBUMS
                                }
                                onViewModeChange(next)
                            },
                            testTag = "toggle_library_view_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Handwritten Search Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(PureWhite.copy(alpha = 0.75f))
                            .border(1.dp, NavyInk.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = NavyInk.copy(alpha = 0.65f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.action_search),
                                    color = NavyInk.copy(alpha = 0.45f),
                                    fontFamily = HandwrittenBodyFont,
                                    fontSize = 16.sp
                                )
                            }
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = PitchBlack,
                                    fontFamily = HandwrittenBodyFont,
                                    fontSize = 16.sp
                                ),
                                cursorBrush = SolidColor(WalkmanRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("song_search_input")
                            )
                        }
                        if (uiState.searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = WalkmanRed,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onSearchQueryChange("") }
                            )
                        }
                    }

                    if (uiState.isImporting) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            color = WalkmanRed,
                            trackColor = SilverBright,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Ruled Paper Song / Album List Content
                if (uiState.filteredTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 56.dp, end = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = NavyInk.copy(alpha = 0.5f),
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.paper_empty_title),
                                color = NavyInk,
                                fontFamily = HandwrittenScriptFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.paper_empty_subtitle),
                                color = PitchBlack.copy(alpha = 0.72f),
                                fontFamily = HandwrittenBodyFont,
                                fontSize = 17.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        if (uiState.viewMode == LibraryViewMode.ALBUMS) {
                            uiState.albumGroups.forEach { albumGroup ->
                                item(key = "album_${albumGroup.albumName}") {
                                    HandwrittenAlbumHeader(
                                        albumGroup = albumGroup,
                                        onPlayAlbum = { onPlayAlbum(albumGroup) },
                                        onClearDemo = if (albumGroup.tracks.all { it.isDemo } && hasDemoTracks) {
                                            onClearDemoTracks
                                        } else null
                                    )
                                }
                                itemsIndexed(
                                    items = albumGroup.tracks,
                                    key = { _, track -> "track_${track.id}" }
                                ) { idx, track ->
                                    HandwrittenTrackRow(
                                        index = idx + 1,
                                        track = track,
                                        isActive = track.id == activeTrackId,
                                        isPlaying = track.id == activeTrackId && uiState.playback.isPlaying,
                                        indented = true,
                                        onSelect = { onSelectTrack(track) },
                                        onRemove = { onRemoveTrack(track.id) }
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(
                                items = uiState.filteredTracks,
                                key = { _, track -> "flat_${track.id}" }
                            ) { idx, track ->
                                HandwrittenTrackRow(
                                    index = idx + 1,
                                    track = track,
                                    isActive = track.id == activeTrackId,
                                    isPlaying = track.id == activeTrackId && uiState.playback.isPlaying,
                                    indented = false,
                                    onSelect = { onSelectTrack(track) },
                                    onRemove = { onRemoveTrack(track.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaperActionChip(
    label: String,
    icon: @Composable () -> Unit,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(containerColor)
            .border(1.dp, PitchBlack.copy(alpha = 0.25f), RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = contentColor,
            fontFamily = HardwareDisplayFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun HandwrittenAlbumHeader(
    albumGroup: AlbumGroup,
    onPlayAlbum: () -> Unit,
    onClearDemo: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayAlbum)
            .padding(start = 56.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Album: ${albumGroup.albumName}",
                    color = NavyInk,
                    fontFamily = HandwrittenScriptFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${albumGroup.artistSummary} • ${albumGroup.tracks.size} track(s) (tap to load)",
                color = NavyInk.copy(alpha = 0.65f),
                fontFamily = HandwrittenBodyFont,
                fontSize = 13.sp
            )
            // Handwritten dashed ink rule under album title
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(3.dp)
                    .padding(top = 2.dp)
            ) {
                drawLine(
                    color = WalkmanRed.copy(alpha = 0.7f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                )
            }
        }

        if (onClearDemo != null) {
            Text(
                text = "Clear Demo",
                color = WalkmanRed,
                fontFamily = HandwrittenBodyFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onClearDemo)
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("clear_demo_tracks_button")
            )
        }
    }
}

@Composable
private fun HandwrittenTrackRow(
    index: Int,
    track: TrackEntity,
    isActive: Boolean,
    isPlaying: Boolean,
    indented: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    val inkColor by animateColorAsState(
        targetValue = if (isActive) WalkmanRed else PitchBlack,
        label = "inkColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .minimumInteractiveComponentSize()
            .padding(
                start = 10.dp,
                end = 6.dp,
                top = 4.dp,
                bottom = 4.dp
            )
            .testTag("track_row_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track number in left margin (to the left of the red margin line)
        Box(
            modifier = Modifier.width(34.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .border(1.5.dp, WalkmanRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d".format(index),
                        color = WalkmanRed,
                        fontFamily = HandwrittenBodyFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Text(
                    text = "%02d.".format(index),
                    color = NavyInk.copy(alpha = 0.72f),
                    fontFamily = HandwrittenBodyFont,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(if (indented) 18.dp else 12.dp))

        // Handwritten Song Title & Artist Name
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Now Playing",
                        tint = WalkmanRed,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                }
                Text(
                    text = track.title,
                    color = inkColor,
                    fontFamily = HandwrittenBodyFont,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 19.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${track.artist} — ${track.album}",
                color = if (isActive) WalkmanRed.copy(alpha = 0.85f) else NavyInk.copy(alpha = 0.68f),
                fontFamily = HandwrittenBodyFont,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(1.5.dp)
                        .background(WalkmanRed.copy(alpha = 0.65f))
                )
            }
        }

        // Handwritten Duration
        Text(
            text = formatDurationMs(track.durationMs),
            color = inkColor.copy(alpha = 0.8f),
            fontFamily = HandwrittenBodyFont,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("remove_track_${track.id}")
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = stringResource(R.string.action_remove_track),
                tint = NavyInk.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
