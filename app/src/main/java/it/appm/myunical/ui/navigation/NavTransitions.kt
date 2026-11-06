package it.appm.myunical.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent
import it.fra.myunical.ui.animation.AppMotion

/** How far the screen underneath moves while the new one slides over it. */
private const val PARALLAX = 0.25f

private val slideSpec = AppMotion.spatial(IntOffset.VisibilityThreshold)
private val fadeSpec = AppMotion.spatial<Float>()

/**
 * Forward: the new screen slides in over the current one, which drifts a quarter to the left.
 * Both screens are always on screen, so there is never an empty frame.
 */
fun <T : Any> slideOverForward(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    slideInHorizontally(slideSpec) { it } togetherWith
        slideOutHorizontally(slideSpec) { -(it * PARALLAX).toInt() }
}

/** Backward: the current screen slides away to the right, uncovering the previous one. */
fun <T : Any> slideOverBackward(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    (slideInHorizontally(slideSpec) { -(it * PARALLAX).toInt() } togetherWith
        slideOutHorizontally(slideSpec) { it })
        // The leaving screen stays on top while it slides away
        .apply { targetContentZIndex = -1f }
}

// Keeps the leaving screen visible for most of the gesture, then fades it quickly
private val LateFade = CubicBezierEasing(0.7f, 0f, 1f, 1f)

/**
 * Predictive back: driven by the finger, so every spec is a linear tween. The leaving screen
 * shrinks and leans toward the swipe while the previous one comes back from the left.
 */
fun <T : Any> predictiveBack(): AnimatedContentTransitionScope<Scene<T>>.(Int) -> ContentTransform = { edge ->
    val direction = if (edge == NavigationEvent.EDGE_RIGHT) -1 else 1
    val linear = tween<Float>(easing = LinearEasing)
    (slideInHorizontally(tween(easing = LinearEasing)) { -(it * PARALLAX).toInt() } togetherWith
        (scaleOut(linear, targetScale = 0.9f) +
            slideOutHorizontally(tween(easing = LinearEasing)) { it / 8 * direction } +
            fadeOut(tween(easing = LateFade))))
        .apply { targetContentZIndex = -1f }
}

/** Material fade through between top-level tabs: the two contents cross without a gap. */
fun <S> AnimatedContentTransitionScope<S>.fadeThrough(): ContentTransform =
    (fadeIn(fadeSpec) + scaleIn(fadeSpec, initialScale = 0.94f)) togetherWith fadeOut(tween(120))
