package it.appm.myunical.ui.screen.esse3

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.usecase.GetSavedCredentialsUseCase
import it.fra.myunical.ui.Esse3Links
import it.fra.myunical.ui.components.openInCustomTab
import org.koin.compose.koinInject

/**
 * Esse3 website inside the app, already signed in with the saved credentials: the portal accepts
 * HTTP Basic authentication, which a Custom Tab cannot send. The credentials only go to the Esse3 host;
 * links to other sites open in a Custom Tab, and the web session is dropped when the screen closes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Esse3WebScreen(url: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val getCredentials = koinInject<GetSavedCredentialsUseCase>()
    // null = still reading, then Result with the credentials (null when logged out)
    val credentials by produceState<Result<UserCredentials?>?>(null) { value = runCatching { getCredentials() } }

    var title by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = webView?.canGoBack() == true) { webView?.goBack() }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title.ifBlank { "Esse3" }, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.Close, contentDescription = "Chiudi")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            val ready = credentials
            if (ready != null) {
                Esse3WebView(
                    url = url,
                    credentials = ready.getOrNull(),
                    onTitle = { title = it },
                    onProgress = { progress = it },
                    onCreated = { webView = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (ready == null || progress in 0.01f..0.99f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun Esse3WebView(
    url: String,
    credentials: UserCredentials?,
    onTitle: (String) -> Unit,
    onProgress: (Float) -> Unit,
    onCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val basic = remember(credentials) {
        credentials?.sanitized()?.let {
            "Basic " + Base64.encodeToString("${it.username}:${it.password}".toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                // A leftover session from another account must never be reused
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)

                webViewClient = object : WebViewClient() {
                    private var authAttempts = 0

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val target = request.url.toString()
                        if (Esse3Links.isEsse3(target)) return false
                        context.openInCustomTab(target)
                        return true
                    }

                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        onTitle("")
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        onTitle(view.title.orEmpty().removePrefix("Esse3 - "))
                    }

                    override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String?) {
                        val saved = credentials?.sanitized()
                        // Only the Esse3 host, and a wrong password is tried once, not in a loop
                        if (saved == null || host != Esse3Links.HOST || authAttempts++ >= 1) {
                            handler.cancel()
                        } else {
                            handler.proceed(saved.username, saved.password)
                        }
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView, newProgress: Int) = onProgress(newProgress / 100f)
                }
                setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, _ ->
                    enqueueDownload(ctx, downloadUrl, userAgent, contentDisposition, mimeType, basic)
                }
                onCreated(this)
                if (basic != null && Esse3Links.isEsse3(url)) loadUrl(url, mapOf("Authorization" to basic)) else loadUrl(url)
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            CookieManager.getInstance().removeAllCookies(null)
            webView.clearCache(true)
            webView.destroy()
        }
    )
}

private fun enqueueDownload(
    context: Context,
    url: String,
    userAgent: String?,
    contentDisposition: String?,
    mimeType: String?,
    authorization: String?
) {
    val uri = url.toUri()
    if (uri.host != Esse3Links.HOST) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return
    }
    val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
    val request = DownloadManager.Request(uri)
        .setTitle(fileName)
        .setMimeType(mimeType)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
    userAgent?.let { request.addRequestHeader("User-Agent", it) }
    CookieManager.getInstance().getCookie(url)?.let { request.addRequestHeader("Cookie", it) }
    authorization?.let { request.addRequestHeader("Authorization", it) }
    (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
}
