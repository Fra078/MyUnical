package it.fra.myunical.documents.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.documents.models.DocumentUiModel
import it.fra.myunical.documents.models.DocumentsEvent
import it.fra.myunical.documents.models.DocumentsUiState
import it.fra.myunical.ui.Esse3Links
import it.fra.myunical.ui.components.GroupHeader
import it.fra.myunical.ui.components.GroupedListItem
import it.fra.myunical.ui.components.openInCustomTab
import it.fra.myunical.ui.theme.Accent
import it.fra.myunical.ui.theme.role
import org.koin.androidx.compose.koinViewModel

@Composable
fun DocumentsScreen(
    onBack: () -> Unit,
    viewModel: DocumentsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DocumentsEvent.Open -> context.openPdf(event.uri, event.title)
                is DocumentsEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    DocumentsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onDownload = viewModel::download,
        onOpenPortal = { context.openInCustomTab(it) }
    )
}

private data class PortalLink(val title: String, val description: String, val icon: ImageVector, val url: String)

private val PortalLinks = listOf(
    PortalLink("Certificati", "Iscrizione, esami sostenuti, laurea", Icons.Rounded.WorkspacePremium, Esse3Links.CERTIFICATES),
    PortalLink("Autocertificazioni", "Dichiarazioni sostitutive da stampare", Icons.Rounded.VerifiedUser, Esse3Links.SELF_CERTIFICATIONS)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DocumentsScreen(
    state: DocumentsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onDownload: (DocumentUiModel) -> Unit,
    onOpenPortal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Documenti", fontWeight = FontWeight.Bold) },
                subtitle = { Text("Certificati e ricevute da Esse3") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Indietro")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        val pullState = rememberPullToRefreshState()
        val isRefreshing = state.isLoading && (state.bookingReceipts != null || state.paymentReceipts != null)
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                item(key = "portal_header") { GroupHeader("Certificati dal portale") }
                itemsIndexed(PortalLinks, key = { _, link -> link.url }) { index, link ->
                    GroupedListItem(
                        title = link.title,
                        supporting = link.description,
                        icon = link.icon,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        index = index,
                        count = PortalLinks.size,
                        onClick = { onOpenPortal(link.url) },
                        trailing = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null) }
                    )
                }
                item(key = "portal_note") {
                    Text(
                        "Esse3 genera questi certificati solo dal portale web.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 6.dp)
                    )
                }

                documentSection(
                    key = "bookings",
                    title = "Promemoria prenotazioni",
                    icon = Icons.Rounded.EventAvailable,
                    accent = Accent.SECONDARY,
                    documents = state.bookingReceipts,
                    isLoading = state.isLoading,
                    emptyText = "Nessuna prenotazione attiva agli esami.",
                    downloading = state.downloading,
                    onDownload = onDownload
                )
                documentSection(
                    key = "payments",
                    title = "Ricevute di pagamento",
                    icon = Icons.Rounded.Receipt,
                    accent = Accent.SUCCESS,
                    documents = state.paymentReceipts,
                    isLoading = state.isLoading,
                    emptyText = "Nessuna tassa pagata con pagoPA.",
                    downloading = state.downloading,
                    onDownload = onDownload
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.documentSection(
    key: String,
    title: String,
    icon: ImageVector,
    accent: Accent,
    documents: List<DocumentUiModel>?,
    isLoading: Boolean,
    emptyText: String,
    downloading: Set<String>,
    onDownload: (DocumentUiModel) -> Unit
) {
    item(key = "${key}_header") { GroupHeader(title) }
    when {
        documents == null && isLoading -> item(key = "${key}_loading") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                LoadingIndicator(Modifier.size(40.dp))
            }
        }
        documents == null -> item(key = "${key}_error") {
            GroupedListItem(
                title = "Non disponibili",
                supporting = "Esse3 non ha risposto, trascina in basso per riprovare.",
                icon = Icons.Rounded.Info,
                index = 0,
                count = 1
            )
        }
        documents.isEmpty() -> item(key = "${key}_empty") {
            GroupedListItem(title = emptyText, icon = Icons.Rounded.Info, index = 0, count = 1)
        }
        else -> itemsIndexed(documents, key = { _, doc -> doc.id }) { index, doc ->
            val isDownloading = doc.id in downloading
            val role = accent.role
            GroupedListItem(
                title = doc.title,
                supporting = doc.subtitle.ifBlank { null },
                icon = icon,
                iconContainerColor = role.container,
                iconColor = role.onContainer,
                index = index,
                count = documents.size,
                onClick = { onDownload(doc) },
                modifier = Modifier.animateItem(),
                trailing = {
                    if (isDownloading) LoadingIndicator(Modifier.size(32.dp))
                    else Icon(Icons.Rounded.Download, contentDescription = "Scarica")
                }
            )
        }
    }
}

/** Opens the PDF in the user's viewer; without one, offers to share it. */
private fun Context.openPdf(uri: Uri, title: String) {
    val view = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "application/pdf")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try {
        startActivity(view)
    } catch (_: ActivityNotFoundException) {
        val send = Intent(Intent.ACTION_SEND)
            .setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(send, title))
    }
}
