package it.fra.myunical.ui.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp

/** Scope shared by every navigation entry; provided by the app around its NavDisplay. */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** Visibility scope of the navigation entry being composed. */
val LocalNavVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Motion shared by screen transitions and shared elements, so they move as one. */
object AppMotion {
    /** Critically damped: smooth, no bounce, settles in about 400 ms. */
    const val STIFFNESS = 380f

    fun <T> spatial(visibilityThreshold: T? = null) =
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = STIFFNESS, visibilityThreshold = visibilityThreshold)
}

/**
 * Remembers the corner radius each side of a shared element last used, so the side that
 * appears can morph its corners from the other one (e.g. a 16 dp thumbnail into a 28 dp cover).
 */
class SharedCornerRegistry {
    private val radii = mutableMapOf<Any, Dp>()
    operator fun get(key: Any): Dp? = radii[key]
    operator fun set(key: Any, radius: Dp) {
        radii[key] = radius
    }
}

val LocalSharedCornerRegistry = staticCompositionLocalOf { SharedCornerRegistry() }

@OptIn(ExperimentalSharedTransitionApi::class)
private val SharedBoundsTransform = BoundsTransform { _, _ -> AppMotion.spatial(Rect.VisibilityThreshold) }

/**
 * Clips this image to rounded corners and animates it between screens that use the same [key].
 * The clip belongs to the image itself, so corners stay rounded while it flies in the overlay,
 * and they morph from the radius of the other screen. Outside a shared transition (previews,
 * tests) it is just a rounded clip.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedImage(key: Any, cornerRadius: Dp): Modifier {
    val transitionScope = LocalSharedTransitionScope.current
    val visibilityScope = LocalNavVisibilityScope.current
    if (transitionScope == null || visibilityScope == null) return clip(RoundedCornerShape(cornerRadius))

    val registry = LocalSharedCornerRegistry.current
    // Read before registering: it is the radius of the screen this element comes from
    val fromRadius = remember(key) { registry[key] ?: cornerRadius }
    SideEffect { registry[key] = cornerRadius }

    val radius by visibilityScope.transition.animateDp(
        transitionSpec = { AppMotion.spatial(Dp.VisibilityThreshold) },
        label = "sharedCorners"
    ) { state -> if (state == EnterExitState.Visible) cornerRadius else fromRadius }
    val shape = RoundedCornerShape(radius)

    return with(transitionScope) {
        this@sharedImage
            .sharedElement(
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = visibilityScope,
                boundsTransform = SharedBoundsTransform,
                clipInOverlayDuringTransition = OverlayClip(shape)
            )
            .clip(shape)
    }
}

private const val OVERLAY_AFTER_FRACTION = 0.4

/**
 * Draws this content above elements flying between screens, e.g. a caption over the image
 * that is landing below it. No-op outside a shared transition.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.aboveSharedElements(): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@aboveSharedElements.renderInSharedTransitionScopeOverlay(
            // Only on the screen coming back, and once the screen leaving has mostly slid away:
            // earlier the content would be drawn on top of it
            renderInOverlay = {
                val transition = visibilityScope.transition
                isTransitionActive && transition.targetState == EnterExitState.Visible &&
                    transition.playTimeNanos > transition.totalDurationNanos * OVERLAY_AFTER_FRACTION
            },
            zIndexInOverlay = 1f
        )
    }
}

/** Shared key of a news cover image, used by the Home carousel, the news list and the detail. */
fun newsImageKey(newsId: Long): String = "news-image-$newsId"
