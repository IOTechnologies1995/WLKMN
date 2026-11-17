package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WlkmnColorScheme = darkColorScheme(
    primary = WalkmanRed,
    onPrimary = PureWhite,
    primaryContainer = NavySurface,
    onPrimaryContainer = PureWhite,
    secondary = SilverMetallic,
    onSecondary = PitchBlack,
    secondaryContainer = NavyChassis,
    onSecondaryContainer = SilverBright,
    tertiary = PaperCream,
    onTertiary = NavyInk,
    background = NavyDeep,
    onBackground = PureWhite,
    surface = NavyChassis,
    onSurface = PureWhite,
    surfaceVariant = CharcoalPanel,
    onSurfaceVariant = SilverMetallic,
    outline = SilverMuted,
    outlineVariant = NavySurface,
    error = WalkmanRedBright,
    onError = PureWhite
)

private val WlkmnShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

@Composable
fun WlkmnTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WlkmnColorScheme,
        typography = WlkmnTypography,
        shapes = WlkmnShapes,
        content = content
    )
}
