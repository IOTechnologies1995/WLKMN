package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CassetteShellStyle
import com.example.data.TapeFilterPreset
import com.example.ui.theme.CassetteBlack
import com.example.ui.theme.HandwrittenBodyFont
import com.example.ui.theme.HandwrittenScriptFont
import com.example.ui.theme.HardwareDisplayFont
import com.example.ui.theme.NavyChassis
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyInk
import com.example.ui.theme.PaperCream
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverBright
import com.example.ui.theme.SilverMetallic
import com.example.ui.theme.SilverMuted
import com.example.ui.theme.TechnicalMonoFont
import com.example.ui.theme.WalkmanRed
import com.example.ui.theme.WalkmanRedDark
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CassetteTapeVisual(
    songTitle: String?,
    artistName: String?,
    albumName: String?,
    isPlaying: Boolean,
    progress: Float,
    speedMultiplier: Float,
    shellStyle: CassetteShellStyle,
    tapePreset: TapeFilterPreset,
    modifier: Modifier = Modifier,
    applyDefaultSizing: Boolean = true
) {
    var rotationDeg by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying, speedMultiplier) {
        if (!isPlaying) return@LaunchedEffect
        var lastNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastNanos != 0L) {
                    val dtSec = (frameNanos - lastNanos) / 1_000_000_000f
                    rotationDeg = (rotationDeg + dtSec * 85f * speedMultiplier) % 360f
                }
                lastNanos = frameNanos
            }
        }
    }

    val shellColor = when (shellStyle) {
        CassetteShellStyle.CLASSIC_BLACK -> CassetteBlack
        CassetteShellStyle.NAVY_WALKMAN -> NavyChassis
        CassetteShellStyle.SILVER_CHROME -> SilverMetallic
        CassetteShellStyle.RED_STUDIO -> WalkmanRedDark
    }
    val shellBorder = when (shellStyle) {
        CassetteShellStyle.SILVER_CHROME -> PitchBlack
        else -> SilverMuted.copy(alpha = 0.65f)
    }

    val clampedProgress = progress.coerceIn(0f, 1f)
    val displayTitle = songTitle?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.cassette_no_track_title)
    val displayArtist = artistName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.cassette_no_track_artist)

    val containerModifier = if (applyDefaultSizing) {
        modifier
            .fillMaxWidth()
            .aspectRatio(1.58f)
    } else {
        modifier
    }

    BoxWithConstraints(
        modifier = containerModifier
            .shadow(14.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(shellColor)
            .border(2.dp, shellBorder, RoundedCornerShape(14.dp))
    ) {
        val cassetteW = maxWidth
        val titleSize = (cassetteW.value * 0.060f).coerceIn(18f, 36f).sp
        val titleLineHeight = (cassetteW.value * 0.066f).coerceIn(20f, 40f).sp
        val artistSize = (cassetteW.value * 0.041f).coerceIn(13f, 23f).sp
        val albumSize = (cassetteW.value * 0.034f).coerceIn(11f, 19f).sp
        val badgeSize = (cassetteW.value * 0.026f).coerceIn(9f, 14f).sp
        val labelTopPad = (cassetteW * 0.032f).coerceIn(10.dp, 22.dp)
        val labelHorizPad = (cassetteW * 0.034f).coerceIn(10.dp, 22.dp)
        val labelVertPad = (cassetteW * 0.018f).coerceIn(5.dp, 12.dp)
        val stripeHeight = (cassetteW * 0.0085f).coerceIn(2.5.dp, 5.dp)

        // Cassette mechanical casing, tape reels, and bottom head bridge
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle inner bevel frame
            drawRoundRect(
                color = SilverBright.copy(alpha = 0.14f),
                topLeft = Offset(w * 0.025f, h * 0.035f),
                size = Size(w * 0.95f, h * 0.93f),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 5 Cassette Shell Screws
            val screwPositions = listOf(
                Offset(w * 0.055f, h * 0.08f),
                Offset(w * 0.945f, h * 0.08f),
                Offset(w * 0.055f, h * 0.91f),
                Offset(w * 0.945f, h * 0.91f),
                Offset(w * 0.50f, h * 0.87f)
            )
            screwPositions.forEach { center ->
                drawScrew(center, radius = w * 0.017f)
            }

            // Center dark window cutout (around spools)
            val winLeft = w * 0.19f
            val winTop = h * 0.43f
            val winW = w * 0.62f
            val winH = h * 0.25f

            drawRoundRect(
                color = PitchBlack,
                topLeft = Offset(winLeft, winTop),
                size = Size(winW, winH),
                cornerRadius = CornerRadius(18.dp.toPx())
            )
            drawRoundRect(
                color = SilverMetallic.copy(alpha = 0.45f),
                topLeft = Offset(winLeft, winTop),
                size = Size(winW, winH),
                cornerRadius = CornerRadius(18.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Tape window transparent glass sheen & measurement ticks
            val glassLeft = w * 0.36f
            val glassTop = h * 0.455f
            val glassW = w * 0.28f
            val glassH = h * 0.20f
            drawRoundRect(
                color = NavyDeep.copy(alpha = 0.65f),
                topLeft = Offset(glassLeft, glassTop),
                size = Size(glassW, glassH),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            drawRoundRect(
                color = SilverBright.copy(alpha = 0.35f),
                topLeft = Offset(glassLeft, glassTop),
                size = Size(glassW, glassH),
                cornerRadius = CornerRadius(6.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
            // White gauge ticks inside tape window
            for (i in 0..6) {
                val tx = glassLeft + glassW * (0.14f + i * 0.12f)
                val tickH = if (i == 3) glassH * 0.32f else glassH * 0.18f
                drawLine(
                    color = PureWhite.copy(alpha = 0.65f),
                    start = Offset(tx, glassTop + glassH * 0.72f - tickH),
                    end = Offset(tx, glassTop + glassH * 0.72f),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // Left (supply) and Right (take-up) magnetic tape spools
            val leftCenter = Offset(w * 0.295f, h * 0.555f)
            val rightCenter = Offset(w * 0.705f, h * 0.555f)
            val hubRadius = w * 0.058f
            val maxTapeRadius = w * 0.112f
            val minTapeRadius = w * 0.068f

            val leftTapeRadius = maxTapeRadius - (maxTapeRadius - minTapeRadius) * clampedProgress
            val rightTapeRadius = minTapeRadius + (maxTapeRadius - minTapeRadius) * clampedProgress

            // Dark brown/black magnetic tape ribbon on spools
            drawCircle(color = Color(0xFF231C1A), radius = leftTapeRadius, center = leftCenter)
            drawCircle(
                color = SilverMuted.copy(alpha = 0.22f),
                radius = leftTapeRadius * 0.85f,
                center = leftCenter,
                style = Stroke(width = 1.dp.toPx())
            )

            drawCircle(color = Color(0xFF231C1A), radius = rightTapeRadius, center = rightCenter)
            drawCircle(
                color = SilverMuted.copy(alpha = 0.22f),
                radius = rightTapeRadius * 0.85f,
                center = rightCenter,
                style = Stroke(width = 1.dp.toPx())
            )

            // Rotating white/silver cassette hubs
            drawReelHub(center = leftCenter, radius = hubRadius, rotationDeg = -rotationDeg)
            drawReelHub(center = rightCenter, radius = hubRadius, rotationDeg = -rotationDeg * 1.18f)

            // Bottom trapezoid head bridge
            val bridgePath = Path().apply {
                moveTo(w * 0.20f, h * 0.99f)
                lineTo(w * 0.24f, h * 0.75f)
                lineTo(w * 0.76f, h * 0.75f)
                lineTo(w * 0.80f, h * 0.99f)
                close()
            }
            drawPath(bridgePath, color = PitchBlack.copy(alpha = 0.55f))
            drawPath(
                bridgePath,
                color = SilverMetallic.copy(alpha = 0.4f),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Magnetic tape ribbon passing behind bridge cutouts
            drawLine(
                color = Color(0xFF3A2D28),
                start = Offset(w * 0.27f, h * 0.87f),
                end = Offset(w * 0.73f, h * 0.87f),
                strokeWidth = 3.5.dp.toPx()
            )

            // Capstan holes and guide pins along bottom bridge
            val holeRadius = w * 0.022f
            drawCircle(color = NavyDeep, radius = holeRadius, center = Offset(w * 0.30f, h * 0.88f))
            drawCircle(color = SilverMetallic, radius = holeRadius, center = Offset(w * 0.30f, h * 0.88f), style = Stroke(1.2.dp.toPx()))

            drawCircle(color = NavyDeep, radius = holeRadius, center = Offset(w * 0.70f, h * 0.88f))
            drawCircle(color = SilverMetallic, radius = holeRadius, center = Offset(w * 0.70f, h * 0.88f), style = Stroke(1.2.dp.toPx()))

            // Pressure pad center felt
            drawRoundRect(
                color = WalkmanRed.copy(alpha = 0.85f),
                topLeft = Offset(w * 0.47f, h * 0.89f),
                size = Size(w * 0.06f, h * 0.045f),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }

        // Top Paper Cassette Label displaying Song & Artist Name
        Box(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .align(Alignment.TopCenter)
                .padding(top = labelTopPad)
                .clip(RoundedCornerShape(6.dp))
                .background(PaperCream)
                .border(1.dp, NavyInk.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = labelHorizPad, vertical = labelVertPad)
            ) {
                // Label top badge row (Side A, Tape Spec, Active Filter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(NavyInk)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "A",
                                color = PureWhite,
                                fontFamily = HardwareDisplayFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = badgeSize
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WLKMN ${shellStyle.tapeSpec}",
                            color = NavyInk,
                            fontFamily = TechnicalMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = badgeSize,
                            letterSpacing = 0.6.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (tapePreset == TapeFilterPreset.OFF) SilverBright else WalkmanRed)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = tapePreset.badge,
                            color = if (tapePreset == TapeFilterPreset.OFF) PitchBlack else PureWhite,
                            fontFamily = TechnicalMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = badgeSize
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Handwritten Song Title on Ruled Label Line
                Text(
                    text = displayTitle,
                    color = PitchBlack,
                    fontFamily = HandwrittenScriptFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = titleSize,
                    lineHeight = titleLineHeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(NavyInk.copy(alpha = 0.25f))
                )

                // Handwritten Artist + Album Subtitle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayArtist,
                        color = NavyInk,
                        fontFamily = HandwrittenBodyFont,
                        fontSize = artistSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!albumName.isNullOrBlank()) {
                        Text(
                            text = albumName,
                            color = SilverMuted,
                            fontFamily = HandwrittenBodyFont,
                            fontSize = albumSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Classic Walkman Navy + Red horizontal tape racing stripes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(stripeHeight)
                        .background(NavyInk)
                )
                Spacer(modifier = Modifier.height(1.5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(stripeHeight)
                        .background(WalkmanRed)
                )
            }
        }
    }
}

private fun DrawScope.drawScrew(center: Offset, radius: Float) {
    drawCircle(color = SilverMetallic, radius = radius, center = center)
    drawCircle(color = PitchBlack.copy(alpha = 0.4f), radius = radius, center = center, style = Stroke(1.dp.toPx()))
    drawLine(
        color = PitchBlack.copy(alpha = 0.7f),
        start = Offset(center.x - radius * 0.55f, center.y - radius * 0.35f),
        end = Offset(center.x + radius * 0.55f, center.y + radius * 0.35f),
        strokeWidth = 1.2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawReelHub(center: Offset, radius: Float, rotationDeg: Float) {
    rotate(degrees = rotationDeg, pivot = center) {
        // Outer white gear ring
        drawCircle(color = PureWhite, radius = radius, center = center)
        drawCircle(color = PitchBlack, radius = radius * 0.74f, center = center)

        // 6 inner drive teeth
        for (i in 0 until 6) {
            val angleRad = Math.toRadians((i * 60).toDouble())
            val innerR = radius * 0.44f
            val outerR = radius * 0.76f
            val start = Offset(
                center.x + (innerR * cos(angleRad)).toFloat(),
                center.y + (innerR * sin(angleRad)).toFloat()
            )
            val end = Offset(
                center.x + (outerR * cos(angleRad)).toFloat(),
                center.y + (outerR * sin(angleRad)).toFloat()
            )
            drawLine(
                color = SilverBright,
                start = start,
                end = end,
                strokeWidth = radius * 0.24f,
                cap = StrokeCap.Butt
            )
        }

        // Center spindle hole
        drawCircle(color = NavyDeep, radius = radius * 0.34f, center = center)
        drawCircle(
            color = SilverMetallic,
            radius = radius * 0.34f,
            center = center,
            style = Stroke(1.dp.toPx())
        )
    }
}
