package it.fra.myunical.ui.components

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

/** Expressive grouped-list shape: large outer corners, tight inner corners between siblings. */
@Composable
fun rememberGroupedItemShape(index: Int, totalCount: Int): RoundedCornerShape {
    return remember(index, totalCount) {
        val outerRadius = CornerSize(20.dp)
        val innerRadius = CornerSize(4.dp)
        when {
            totalCount == 1 -> RoundedCornerShape(outerRadius)
            index == 0 -> RoundedCornerShape(
                topStart = outerRadius,
                topEnd = outerRadius,
                bottomStart = innerRadius,
                bottomEnd = innerRadius
            )
            index == totalCount - 1 -> RoundedCornerShape(
                topStart = innerRadius,
                topEnd = innerRadius,
                bottomStart = outerRadius,
                bottomEnd = outerRadius
            )
            else -> RoundedCornerShape(innerRadius)
        }
    }
}
