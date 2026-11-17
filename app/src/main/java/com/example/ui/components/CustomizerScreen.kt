package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CassetteShellStyle
import com.example.data.TapeFilterPreset
import com.example.ui.WlkmnUiState
import com.example.ui.theme.CassetteBlack
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
import com.example.ui.theme.WalkmanRedDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizerScreen(
    uiState: WlkmnUiState,
    onSelectPreset: (TapeFilterPreset) -> Unit,
    onEqBandChange: (Int, Int) -> Unit,
    onWarmthChange: (Float) -> Unit,
    onHissChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onShellStyleChange: (CassetteShellStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    val playback = uiState.playback
    val minMb = playback.minBandMb
    val maxMb = playback.maxBandMb

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(NavyDeep, NavyChassis, PitchBlack))
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Faceplate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurface)
                    .border(1.dp, SilverMetallic.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = WalkmanRedBright,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.customizer_title),
                            color = PureWhite,
                            fontFamily = HardwareDisplayFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = settings.preset.description,
                            color = SilverMetallic,
                            fontFamily = TechnicalMonoFont,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 1. Tape Filter Presets
            StudioSectionCard(title = stringResource(R.string.customizer_tape_filter_label)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TapeFilterPreset.entries.forEach { preset ->
                        val selected = settings.preset == preset
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) WalkmanRed else NavyDeep)
                                .border(
                                    width = 1.2.dp,
                                    color = if (selected) PureWhite else SilverMuted.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectPreset(preset) }
                                .minimumInteractiveComponentSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("preset_${preset.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (selected) PitchBlack else SilverMetallic)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = preset.badge,
                                    color = if (selected) PureWhite else PitchBlack,
                                    fontFamily = TechnicalMonoFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = preset.displayName,
                                color = PureWhite,
                                fontFamily = HardwareDisplayFont,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 2. Live EQ Frequency Response Curve + 5-Band Sliders
            StudioSectionCard(title = stringResource(R.string.customizer_eq_label)) {
                EqResponseCurveCanvas(
                    bandsMb = settings.eqBandsMb,
                    minMb = minMb,
                    maxMb = maxMb,
                    freqsHz = playback.bandCenterFreqsHz
                )

                Spacer(modifier = Modifier.height(10.dp))

                settings.eqBandsMb.forEachIndexed { index, levelMb ->
                    val freqHz = playback.bandCenterFreqsHz.getOrElse(index) { 1000 }
                    val freqLabel = if (freqHz >= 1000) {
                        val khz = freqHz / 1000f
                        if (khz % 1f == 0f) "${khz.toInt()}kHz" else "%.1fkHz".format(khz)
                    } else {
                        "${freqHz}Hz"
                    }
                    val db = levelMb / 100f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = freqLabel,
                            color = SilverBright,
                            fontFamily = TechnicalMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.width(54.dp)
                        )
                        Slider(
                            value = levelMb.toFloat(),
                            onValueChange = { onEqBandChange(index, it.toInt()) },
                            valueRange = minMb.toFloat()..maxMb.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = SilverBright,
                                activeTrackColor = if (levelMb >= 0) WalkmanRed else SilverMetallic,
                                inactiveTrackColor = PitchBlack
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("eq_band_slider_$index")
                        )
                        Text(
                            text = "%+.1f dB".format(db),
                            color = if (levelMb > 0) WalkmanRedBright else if (levelMb < 0) SilverMetallic else PureWhite,
                            fontFamily = TechnicalMonoFont,
                            fontSize = 11.sp,
                            modifier = Modifier.width(62.dp)
                        )
                    }
                }
            }

            // 3. Analog Tape Character (Warmth, Hiss, Speed)
            StudioSectionCard(title = stringResource(R.string.customizer_analog_label)) {
                AnalogControlSlider(
                    label = "Tape Warmth (Low-End Saturation)",
                    valueText = "${(settings.tapeWarmth * 100).toInt()}%",
                    value = settings.tapeWarmth,
                    valueRange = 0f..1f,
                    onValueChange = onWarmthChange,
                    testTag = "tape_warmth_slider"
                )

                Spacer(modifier = Modifier.height(6.dp))

                AnalogControlSlider(
                    label = "Analog Tape Hiss (Noise Floor)",
                    valueText = "${(settings.tapeHiss * 100).toInt()}%",
                    value = settings.tapeHiss,
                    valueRange = 0f..1f,
                    onValueChange = onHissChange,
                    testTag = "tape_hiss_slider"
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tape Motor Speed",
                        color = PureWhite,
                        fontFamily = HardwareDisplayFont,
                        fontSize = 13.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "%.2fx".format(settings.tapeSpeed),
                            color = WalkmanRedBright,
                            fontFamily = TechnicalMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NavyDeep)
                                .border(1.dp, SilverMuted.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable { onSpeedChange(1.0f) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("reset_speed_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Speed",
                                tint = SilverBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "1.00x",
                                color = SilverBright,
                                fontFamily = TechnicalMonoFont,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Slider(
                    value = settings.tapeSpeed,
                    onValueChange = onSpeedChange,
                    valueRange = 0.85f..1.15f,
                    colors = SliderDefaults.colors(
                        thumbColor = PureWhite,
                        activeTrackColor = WalkmanRed,
                        inactiveTrackColor = PitchBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tape_speed_slider")
                )
            }

            // 4. Cassette Shell Edition Selector (Navy, Silver, Black, Red)
            StudioSectionCard(title = stringResource(R.string.customizer_shell_label)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CassetteShellStyle.entries.forEach { style ->
                        val selected = settings.shellStyle == style
                        val swatchColor = when (style) {
                            CassetteShellStyle.CLASSIC_BLACK -> CassetteBlack
                            CassetteShellStyle.NAVY_WALKMAN -> NavyChassis
                            CassetteShellStyle.SILVER_CHROME -> SilverMetallic
                            CassetteShellStyle.RED_STUDIO -> WalkmanRedDark
                        }
                        val swatchText = if (style == CassetteShellStyle.SILVER_CHROME) PitchBlack else PureWhite

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(swatchColor)
                                .border(
                                    width = if (selected) 2.5.dp else 1.dp,
                                    color = if (selected) WalkmanRedBright else SilverMuted.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onShellStyleChange(style) }
                                .minimumInteractiveComponentSize()
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                                .testTag("shell_${style.name.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) WalkmanRed else Color.Transparent)
                                    .border(1.dp, swatchText.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = style.displayName,
                                color = swatchText,
                                fontFamily = HardwareDisplayFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = style.tapeSpec,
                                color = swatchText.copy(alpha = 0.72f),
                                fontFamily = TechnicalMonoFont,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StudioSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalPanel)
            .border(1.dp, SilverMetallic.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = SilverBright,
            fontFamily = TechnicalMonoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun AnalogControlSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = PureWhite,
                fontFamily = HardwareDisplayFont,
                fontSize = 13.sp
            )
            Text(
                text = valueText,
                color = WalkmanRedBright,
                fontFamily = TechnicalMonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = SilverBright,
                activeTrackColor = WalkmanRed,
                inactiveTrackColor = PitchBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}

@Composable
private fun EqResponseCurveCanvas(
    bandsMb: List<Int>,
    minMb: Int,
    maxMb: Int,
    freqsHz: List<Int>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PitchBlack)
            .border(1.dp, SilverMetallic.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val span = (maxMb - minMb).coerceAtLeast(1).toFloat()

            // Horizontal 0dB center line & + / - grid
            val zeroY = h * (1f - ((0f - minMb) / span))
            drawLine(
                color = WalkmanRed.copy(alpha = 0.45f),
                start = Offset(0f, zeroY),
                end = Offset(w, zeroY),
                strokeWidth = 1.dp.toPx()
            )
            listOf(0.2f, 0.8f).forEach { frac ->
                drawLine(
                    color = NavySurface.copy(alpha = 0.6f),
                    start = Offset(0f, h * frac),
                    end = Offset(w, h * frac),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Compute points
            val points = bandsMb.mapIndexed { idx, mb ->
                val x = if (bandsMb.size <= 1) w / 2f else (idx.toFloat() / (bandsMb.size - 1)) * w
                val norm = ((mb - minMb) / span).coerceIn(0f, 1f)
                val y = h * (1f - norm)
                Offset(x, y)
            }

            // Vertical band guide lines
            points.forEach { pt ->
                drawLine(
                    color = NavySurface.copy(alpha = 0.5f),
                    start = Offset(pt.x, 0f),
                    end = Offset(pt.x, h),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (points.size >= 2) {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(
                    path = path,
                    color = SilverBright,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            points.forEach { pt ->
                drawCircle(color = WalkmanRedBright, radius = 4.5.dp.toPx(), center = pt)
                drawCircle(color = PureWhite, radius = 2.dp.toPx(), center = pt)
            }
        }
    }
}
