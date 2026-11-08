package it.fra.myunical.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import it.fra.myunical.ui.components.openInCustomTab

/**
 * Opens a web link. The app provides one that shows Esse3 pages in its own signed-in browser and
 * everything else in a Custom Tab; without it (previews, tests) links open in a Custom Tab.
 */
val LocalLinkOpener = compositionLocalOf<((String) -> Unit)?> { null }

@Composable
fun rememberLinkOpener(): (String) -> Unit {
    val context = LocalContext.current
    val provided = LocalLinkOpener.current
    return remember(context, provided) { provided ?: { url: String -> context.openInCustomTab(url) } }
}
