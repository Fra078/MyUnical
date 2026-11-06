package it.fra.myunical.career.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.shimmerEffect(shape: Shape = RoundedCornerShape(8.dp)): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_animation"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f)
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation, y = translateAnimation)
        ),
        shape = shape
    ).clip(shape)
}