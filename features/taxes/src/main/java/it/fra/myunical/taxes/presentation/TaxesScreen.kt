package it.fra.myunical.taxes.presentation

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.domain.model.taxes.TaxStatus
import it.fra.myunical.taxes.models.InvoiceItemUiModel
import it.fra.myunical.taxes.models.InvoiceUiModel
import it.fra.myunical.taxes.models.OverviewTone
import it.fra.myunical.taxes.models.PaidYearGroup
import it.fra.myunical.taxes.models.TaxOverview
import it.fra.myunical.taxes.models.TaxesIntent
import it.fra.myunical.taxes.models.TaxesUiState
import it.fra.myunical.ui.Esse3Links
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.ui.components.ShapeBadge
import it.fra.myunical.ui.components.openInCustomTab
import it.fra.myunical.ui.components.rememberGroupedItemShape
import it.fra.myunical.ui.theme.extendedColors
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun TaxesScreen(
    onBack: () -> Unit,
    viewModel: TaxesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    TaxesScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onOpenPortal = { context.openInCustomTab(Esse3Links.TAXES) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaxesScreen(
    state: TaxesUiState,
    onIntent: (TaxesIntent) -> Unit,
    onBack: () -> Unit,
    onOpenPortal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    // Full-screen error is shown only without data; otherwise keep the old data and notify
    LaunchedEffect(state.hasError, state.hasContent) {
        if (state.hasError && state.hasContent) {
            snackbarHostState.showSnackbar("Impossibile aggiornare i dati delle tasse")
        }
    }

    val onCopyNotice: (String) -> Unit = { code ->
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Codice avviso pagoPA", code)))
            snackbarHostState.showSnackbar("Codice avviso copiato")
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Tasse", fontWeight = FontWeight.Bold) },
                subtitle = { Text("Addebiti e pagamenti") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenPortal) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Apri il portale")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val pullState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(TaxesIntent.Refresh) },
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator(Modifier.size(64.dp))
                }
                !state.hasContent -> ScrollableMessage {
                    EmptyState(
                        icon = Icons.Rounded.CloudOff,
                        title = "Tasse non disponibili",
                        description = "Non è stato possibile scaricare i tuoi addebiti. Controlla la connessione e riprova.",
                        polygon = MaterialShapes.SoftBurst,
                        action = {
                            FilledTonalButton(onClick = { onIntent(TaxesIntent.Refresh) }) { Text("Riprova") }
                        }
                    )
                }
                else -> TaxesContent(
                    state = state,
                    onIntent = onIntent,
                    onOpenPortal = onOpenPortal,
                    onCopyNotice = onCopyNotice
                )
            }
        }
    }
}

/** Keeps pull-to-refresh working on full-screen messages. */
@Composable
private fun ScrollableMessage(content: @Composable () -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { content() }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TaxesContent(
    state: TaxesUiState,
    onIntent: (TaxesIntent) -> Unit,
    onOpenPortal: () -> Unit,
    onCopyNotice: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        state.overview?.let { overview ->
            item(key = "overview", contentType = "overview") {
                OverviewCard(
                    overview = overview,
                    onOpenPortal = onOpenPortal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        if (state.isEmpty) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    title = "Nessun addebito",
                    description = "Non ci sono tasse associate alla tua carriera.",
                    polygon = MaterialShapes.Cookie6Sided
                )
            }
        }

        if (state.unpaid.isNotEmpty()) {
            item(key = "unpaid_header", contentType = "section_header") {
                SectionHeader(title = "Da pagare", count = state.unpaid.size)
            }
            items(state.unpaid, key = { it.id }, contentType = { "unpaid" }) { invoice ->
                UnpaidInvoiceCard(
                    invoice = invoice,
                    expanded = invoice.id in state.expandedIds,
                    onToggle = { onIntent(TaxesIntent.ToggleExpanded(invoice.id)) },
                    onOpenPortal = onOpenPortal,
                    onCopyNotice = onCopyNotice,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .animateItem()
                )
            }
        }

        if (state.paidGroups.isNotEmpty()) {
            item(key = "paid_header", contentType = "section_header") {
                SectionHeader(title = "Pagate", count = state.paidGroups.sumOf { it.invoices.size })
            }
            state.paidGroups.forEach { group ->
                stickyHeader(key = "year_${group.label}", contentType = "year_header") {
                    YearHeader(group)
                }
                itemsIndexed(
                    items = group.invoices,
                    key = { _, invoice -> invoice.id },
                    contentType = { _, _ -> "paid" }
                ) { index, invoice ->
                    PaidInvoiceRow(
                        invoice = invoice,
                        index = index,
                        count = group.invoices.size,
                        expanded = invoice.id in state.expandedIds,
                        onToggle = { onIntent(TaxesIntent.ToggleExpanded(invoice.id)) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 1.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

private data class ToneStyle(
    val container: Color,
    val content: Color,
    val accent: Color,
    val onAccent: Color,
    val icon: ImageVector,
    val polygon: RoundedPolygon
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OverviewTone.style(): ToneStyle {
    val colors = MaterialTheme.colorScheme
    val success = MaterialTheme.extendedColors.success
    val warning = MaterialTheme.extendedColors.warning
    return when (this) {
        OverviewTone.OK -> ToneStyle(
            success.container, success.onContainer,
            success.color, success.onColor,
            Icons.Rounded.Verified, MaterialShapes.Cookie9Sided
        )
        OverviewTone.WARNING -> ToneStyle(
            warning.container, warning.onContainer,
            warning.color, warning.onColor,
            Icons.Rounded.Schedule, MaterialShapes.Sunny
        )
        OverviewTone.ALERT -> ToneStyle(
            colors.errorContainer, colors.onErrorContainer,
            colors.error, colors.onError,
            Icons.Rounded.PriorityHigh, MaterialShapes.SoftBurst
        )
    }
}

@Composable
private fun OverviewCard(
    overview: TaxOverview,
    onOpenPortal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = overview.tone.style()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = style.container, contentColor = style.content)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShapeBadge(
                    polygon = style.polygon,
                    size = 64.dp,
                    containerColor = style.accent,
                    contentColor = style.onAccent
                ) {
                    Icon(style.icon, contentDescription = null, modifier = Modifier.size(32.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(overview.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (overview.amount != null) {
                        Text(
                            text = overview.amount,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(overview.description, style = MaterialTheme.typography.bodyMedium)
            if (overview.tone != OverviewTone.OK) {
                Button(onClick = onOpenPortal, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Paga sul portale")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Text(count.toString(), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun YearHeader(group: PaidYearGroup) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = group.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun StatusPill(status: TaxStatus) {
    val (label, container, content) = when (status) {
        TaxStatus.OVERDUE -> Triple("Scaduta", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
        TaxStatus.DUE -> Triple("Da pagare", MaterialTheme.extendedColors.warning.color, MaterialTheme.extendedColors.warning.onColor)
        TaxStatus.PAID -> Triple("Pagata", MaterialTheme.extendedColors.success.color, MaterialTheme.extendedColors.success.onColor)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = content,
        modifier = Modifier
            .background(container, CircleShape)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

@Composable
private fun ExpandArrow(expanded: Boolean) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "ExpandArrow")
    Icon(
        Icons.Rounded.KeyboardArrowDown,
        contentDescription = if (expanded) "Comprimi" else "Espandi",
        modifier = Modifier.rotate(rotation)
    )
}

@Composable
private fun UnpaidInvoiceCard(
    invoice: InvoiceUiModel,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenPortal: () -> Unit,
    onCopyNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val overdue = invoice.status == TaxStatus.OVERDUE
    Card(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPill(invoice.status)
                    Text(
                        invoice.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (invoice.subtitle.isNotEmpty()) {
                        Text(
                            invoice.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(invoice.amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    ExpandArrow(expanded)
                }
            }

            invoice.dateLabel?.let { label ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(Icons.Rounded.Event, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    Text(label, style = MaterialTheme.typography.labelLarge, color = color)
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider()
                    InvoiceItems(invoice.items)
                    invoice.noticeCode?.let { code ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Codice avviso pagoPA",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(code, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        invoice.noticeCode?.let { code ->
                            OutlinedButton(onClick = { onCopyNotice(code) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Copia codice")
                            }
                        }
                        Button(onClick = onOpenPortal, modifier = Modifier.weight(1f)) {
                            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Paga")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PaidInvoiceRow(
    invoice: InvoiceUiModel,
    index: Int,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        shape = rememberGroupedItemShape(index, count),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ShapeBadge(
                    polygon = MaterialShapes.Cookie6Sided,
                    size = 40.dp,
                    containerColor = MaterialTheme.extendedColors.success.container,
                    contentColor = MaterialTheme.extendedColors.success.onContainer
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        invoice.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = if (expanded) 3 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        listOfNotNull(invoice.dateLabel, invoice.installment).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(invoice.amount, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.padding(start = 54.dp, top = 12.dp)) {
                    InvoiceItems(invoice.items)
                }
            }
        }
    }
}

@Composable
private fun InvoiceItems(items: List<InvoiceItemUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Dettaglio voci",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                Text(item.amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaxesScreenPreview() {
    val items = listOf(
        InvoiceItemUiModel("Assicurazione", "2,75 €"),
        InvoiceItemUiModel("Bollo virtuale", "16,00 €")
    )
    MaterialTheme {
        TaxesScreen(
            state = TaxesUiState(
                isLoading = false,
                overview = TaxOverview(OverviewTone.WARNING, "Da pagare", "148,50 €", "1 addebito in attesa · prossima scadenza 30/11/2026"),
                unpaid = listOf(
                    InvoiceUiModel("F1", "Tassa onnicomprensiva di iscrizione", "Rata 2 di 4", "A.A. 2026/2027", "148,50 €",
                        TaxStatus.DUE, "Scade tra 24 giorni", "300000012041341789", items)
                ),
                paidGroups = listOf(
                    PaidYearGroup(
                        "A.A. 2026/2027",
                        listOf(
                            InvoiceUiModel("F2", "Tassa onnicomprensiva di iscrizione", "Rata 1 di 4", "A.A. 2026/2027", "18,75 €",
                                TaxStatus.PAID, "Pagata il 16/07/2026", null, items)
                        )
                    )
                ),
                expandedIds = setOf("F1")
            ),
            onIntent = {},
            onBack = {},
            onOpenPortal = {}
        )
    }
}
