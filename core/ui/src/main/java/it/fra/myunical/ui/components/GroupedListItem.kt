package it.fra.myunical.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A row of an expressive grouped list: rows of the same group touch each other and share
 * rounded outer corners (see [rememberGroupedItemShape]).
 */
@Composable
fun GroupedListItem(
    title: String,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    icon: ImageVector? = null,
    iconContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val shape = rememberGroupedItemShape(index, count)
    val content: @Composable () -> Unit = {
        ListItem(
            headlineContent = { Text(title, fontWeight = FontWeight.Medium, color = titleColor) },
            supportingContent = supporting?.let { { Text(it) } },
            leadingContent = icon?.let {
                {
                    Surface(shape = CircleShape, color = iconContainerColor, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(it, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            },
            trailingContent = trailing,
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
    val surfaceModifier = modifier
        .fillMaxWidth()
        .padding(bottom = 2.dp)
    if (onClick != null) {
        Surface(onClick = onClick, shape = shape, color = MaterialTheme.colorScheme.surfaceContainer, modifier = surfaceModifier) {
            content()
        }
    } else {
        Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainer, modifier = surfaceModifier) {
            content()
        }
    }
}

/** Title above a group of [GroupedListItem]s. */
@Composable
fun GroupHeader(text: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp), verticalArrangement = Arrangement.Center) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
