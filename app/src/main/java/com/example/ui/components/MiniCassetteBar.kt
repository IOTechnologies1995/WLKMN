package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.TapeFilterPreset
import com.example.data.TrackEntity
import com.example.ui.theme.HandwrittenBodyFont
import com.example.ui.theme.NavyChassis
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverBright
import com.example.ui.theme.SilverMetallic
import com.example.ui.theme.TechnicalMonoFont
import com.example.ui.theme.WalkmanRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MiniCassetteBar(
    track: TrackEntity,
    isPlaying: Boolean,
    tapePreset: TapeFilterPreset,
    onOpenDeck: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var angle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = (now - lastNanos) / 1_000_000_000f
                    angle = (angle + dt * 110f) % 360f
                }
                lastNanos = now
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NavyChassis)
            .border(1.5.dp, SilverMetallic.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .clickable(onClick = onOpenDeck)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("mini_cassette_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spinning mini cassette reel icon
            Canvas(modifier = Modifier.size(34.dp)) {
                val r = size.minDimension / 2f
                val center = Offset(r, r)
                drawCircle(color = PitchBlack, radius = r, center = center)
                drawCircle(color = SilverBright, radius = r * 0.85f, center = center, style = Stroke(1.5.dp.toPx()))
                rotate(angle, pivot = center) {
                    for (i in 0 until 4) {
                        val rad = Math.toRadians((i * 90).toDouble())
                        drawLine(
                            color = PureWhite,
                            start = Offset(
                                center.x + (r * 0.28f * cos(rad)).toFloat(),
                                center.y + (r * 0.28f * sin(rad)).toFloat()
                            ),
                            end = Offset(
                                center.x + (r * 0.65f * cos(rad)).toFloat(),
                                center.y + (r * 0.65f * sin(rad)).toFloat()
                            ),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
                drawCircle(color = WalkmanRed, radius = r * 0.22f, center = center)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = PureWhite,
                    fontFamily = HandwrittenBodyFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        color = SilverMetallic,
                        fontFamily = HandwrittenBodyFont,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (tapePreset == TapeFilterPreset.OFF) PitchBlack else WalkmanRed)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = tapePreset.badge,
                            color = PureWhite,
                            fontFamily = TechnicalMonoFont,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WalkmanRed)
                    .clickable(onClick = onTogglePlayPause)
                    .minimumInteractiveComponentSize()
                    .testTag("mini_play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.action_pause else R.string.action_play
                    ),
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onSkipNext,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("mini_next_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = stringResource(R.string.action_next),
                    tint = SilverBright
                )
            }
        }
    }
}
