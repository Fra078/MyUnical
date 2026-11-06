package it.fra.myunical.directory.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.directory.models.DirectoryUiState
import it.fra.myunical.directory.models.initials
import it.fra.myunical.directory.models.label
import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.ui.components.rememberGroupedItemShape
import org.koin.androidx.compose.koinViewModel

@Composable
fun DirectoryScreen(
    onBack: () -> Unit,
    onOpenContact: (String) -> Unit,
    viewModel: DirectoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DirectoryScreen(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onLoadMore = viewModel::loadMore,
        onOpenContact = onOpenContact
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DirectoryScreen(
    state: DirectoryUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenContact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Infinite scroll: ask for the next page when the last rows become visible
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - 4
        }
    }
    LaunchedEffect(shouldLoadMore, state.contacts.size) {
        if (shouldLoadMore && state.contacts.isNotEmpty() && !state.hasError) onLoadMore()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Rubrica di ateneo", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                )
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Cerca docenti e personale") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Cancella ricerca")
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        onSearch()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isQueryTooShort -> EmptyState(
                    icon = Icons.Rounded.Contacts,
                    title = "Cerca nella rubrica",
                    description = "Scrivi almeno ${DirectoryUiState.MIN_QUERY_LENGTH} lettere del nome o del cognome di un docente o di un membro del personale.",
                    polygon = MaterialShapes.Cookie9Sided
                )
                state.isSearching && state.contacts.isEmpty() ->
                    LoadingIndicator(Modifier.size(64.dp).align(Alignment.Center))
                state.contacts.isEmpty() && state.hasError -> EmptyState(
                    icon = Icons.Rounded.CloudOff,
                    title = "Rubrica non raggiungibile",
                    description = "Controlla la connessione e riprova.",
                    polygon = MaterialShapes.SoftBurst,
                    action = { FilledTonalButton(onClick = onSearch) { Text("Riprova") } }
                )
                state.contacts.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.PersonSearch,
                    title = "Nessun risultato",
                    description = "Nessuna persona corrisponde a \"${state.query.trim()}\".",
                    polygon = MaterialShapes.Clover4Leaf
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
                ) {
                    item(key = "count") {
                        Text(
                            if (state.totalCount == 1) "1 risultato" else "${state.totalCount} risultati",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                    }
                    itemsIndexed(state.contacts, key = { _, contact -> contact.id }) { index, contact ->
                        ContactRow(
                            contact = contact,
                            index = index,
                            count = state.contacts.size,
                            onClick = {
                                focusManager.clearFocus()
                                onOpenContact(contact.id)
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                    item(key = "footer") {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                state.hasError -> TextButton(onClick = onLoadMore) { Text("Errore di caricamento · Riprova") }
                                state.isLoadingMore -> LoadingIndicator(Modifier.size(48.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ContactRow(
    contact: Contact,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = rememberGroupedItemShape(index, count),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
    ) {
        ListItem(
            headlineContent = { Text(contact.name, fontWeight = FontWeight.Medium) },
            supportingContent = contact.roles.firstOrNull()?.let { role ->
                { Text(role.label, maxLines = 2, overflow = TextOverflow.Ellipsis) }
            },
            leadingContent = { InitialsAvatar(contact.initials) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

/** Initials inside an expressive shape, used as avatar since the address book has no photos. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InitialsAvatar(initials: String, modifier: Modifier = Modifier, size: Int = 44) {
    Surface(
        shape = MaterialShapes.Cookie6Sided.toShape(),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.size(size.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                initials,
                style = if (size > 60) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Preview
@Composable
private fun DirectoryScreenPreview() {
    MaterialTheme {
        DirectoryScreen(
            state = DirectoryUiState(query = "ros", isSearching = false),
            onBack = {}, onQueryChange = {}, onSearch = {}, onLoadMore = {}, onOpenContact = {}
        )
    }
}
