package it.fra.myunical.schedule.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.abs

/** Stable accent color for a course, so the same lesson is recognizable across days and months. */
@Composable
@ReadOnlyComposable
fun courseAccentColor(courseCode: String): Color {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val hue = (abs(courseCode.hashCode()) % 360).toFloat()
    return Color.hsl(
        hue = hue,
        saturation = if (isDark) 0.55f else 0.60f,
        lightness = if (isDark) 0.72f else 0.42f
    )
}
