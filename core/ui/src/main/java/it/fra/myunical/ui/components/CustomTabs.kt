package it.fra.myunical.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

/** Opens [url] in a Custom Tab, falling back to any app able to open the link. */
fun Context.openInCustomTab(url: String) {
    val uri = url.toUri()
    try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(this, uri)
    } catch (_: ActivityNotFoundException) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
