package it.fra.myunical.today.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import it.fra.myunical.ui.animation.AppMotion
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Drag distance (px) or fling speed (px/s) that closes the sheet. */
private const val DISMISS_DISTANCE = 220f
private const val DISMISS_VELOCITY = 1800f

/**
 * A modal bottom sheet drawn inside the app window instead of a dialog. Being part of the
 * screen, it moves with it during navigation: the next screen slides over Home and the sheet
 * together, in a single movement. It closes with the back gesture, a tap on the scrim or a
 * downward drag (also continuing the scroll of its content past the top).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaySheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    BackHandler(enabled = visible, onBack = onDismiss)

    BoxWithConstraints(modifier.fillMaxSize()) {
        val maxSheetHeight = maxHeight * 0.92f

        AnimatedVisibility(visible, enter = fadeIn(AppMotion.spatial()), exit = fadeOut(AppMotion.spatial())) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(BottomSheetDefaults.ScrimColor)
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } }
            )
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(AppMotion.spatial(IntOffset.VisibilityThreshold)) { it },
            exit = slideOutVertically(AppMotion.spatial(IntOffset.VisibilityThreshold)) { it }
        ) {
            val drag = remember { Animatable(0f) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { drag.snapTo(0f) }

            fun release(velocity: Float) {
                if (drag.value > DISMISS_DISTANCE || velocity > DISMISS_VELOCITY) onDismiss()
                else scope.launch { drag.animateTo(0f, AppMotion.spatial()) }
            }

            // Scrolling down past the top of the content drags the sheet; scrolling up first
            // brings the sheet back before scrolling the content
            val connection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        if (available.y < 0 && drag.value > 0f) {
                            val consumed = maxOf(available.y, -drag.value)
                            scope.launch { drag.snapTo(drag.value + consumed) }
                            return Offset(0f, consumed)
                        }
                        return Offset.Zero
                    }

                    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                        if (available.y > 0 && source == NestedScrollSource.UserInput) {
                            scope.launch { drag.snapTo(drag.value + available.y) }
                            return Offset(0f, available.y)
                        }
                        return Offset.Zero
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (drag.value > 0f) {
                            release(available.y)
                            return available
                        }
                        return Velocity.Zero
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .offset { IntOffset(0, drag.value.roundToInt().coerceAtLeast(0)) }
                    .nestedScroll(connection)
                    .draggable(
                        state = rememberDraggableState { delta ->
                            scope.launch { drag.snapTo((drag.value + delta).coerceAtLeast(0f)) }
                        },
                        orientation = Orientation.Vertical,
                        onDragStopped = { velocity -> release(velocity) }
                    )
            ) {
                Column {
                    BottomSheetDefaults.DragHandle(Modifier.align(Alignment.CenterHorizontally))
                    content()
                }
            }
        }
    }
}
