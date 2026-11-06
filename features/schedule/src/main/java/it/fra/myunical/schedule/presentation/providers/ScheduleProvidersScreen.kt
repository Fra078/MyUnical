package it.fra.myunical.schedule.presentation.providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.model.schedule.TrackedCourse
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.ui.components.ShapeBadge
import it.fra.myunical.schedule.mapper.toDisplayName
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScheduleProvidersScreen(
    onBack: () -> Unit,
    onAddProvider: () -> Unit,
    onEditProvider: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleProvidersViewModel = koinViewModel()
) {
    val providers by viewModel.providers.collectAsStateWithLifecycle()

    ScheduleProvidersScreen(
        providers = providers,
        onBack = onBack,
        onAddProvider = onAddProvider,
        onEditProvider = onEditProvider,
        onRemoveProvider = viewModel::remove,
        onRestoreProvider = viewModel::restore,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleProvidersScreen(
    providers: List<ScheduleProvider>?,
    onBack: () -> Unit,
    onAddProvider: () -> Unit,
    onEditProvider: (String) -> Unit,
    onRemoveProvider: (ScheduleProvider) -> Unit,
    onRestoreProvider: (ScheduleProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    fun removeWithUndo(provider: ScheduleProvider) {
        onRemoveProvider(provider)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "\"${provider.displayName}\" eliminato",
                actionLabel = "Annulla",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) onRestoreProvider(provider)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("I tuoi orari", fontWeight = FontWeight.Bold) },
                subtitle = {
                    val count = providers?.size ?: 0
                    Text(
                        when (count) {
                            0 -> "Nessun orario configurato"
                            1 -> "1 orario configurato"
                            else -> "$count orari configurati"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Indietro")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (!providers.isNullOrEmpty()) {
                MediumExtendedFloatingActionButton(
                    text = { Text("Aggiungi orario") },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    onClick = onAddProvider,
                    expanded = fabExpanded
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            providers == null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(Modifier.size(64.dp))
            }

            providers.isEmpty() -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "Aggiungi il tuo primo orario",
                        description = "Puoi combinare più corsi di laurea e scegliere solo gli insegnamenti che segui.",
                        polygon = MaterialShapes.Clover4Leaf,
                        action = {
                            Button(onClick = onAddProvider) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Text("Aggiungi orario", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    )
                }
            }

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(providers, key = ScheduleProvider::id) { provider ->
                    ProviderCard(
                        provider = provider,
                        onEdit = { onEditProvider(provider.id) },
                        onRemove = { removeWithUndo(provider) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val BadgeShapes = listOf(
    MaterialShapes.Cookie9Sided,
    MaterialShapes.Clover4Leaf,
    MaterialShapes.Sunny,
    MaterialShapes.Cookie6Sided,
    MaterialShapes.Flower
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProviderCard(
    provider: ScheduleProvider,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onEdit,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShapeBadge(
                    polygon = BadgeShapes[Math.floorMod(provider.cdsCode.hashCode(), BadgeShapes.size)],
                    size = 56.dp,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Rounded.School, contentDescription = null)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = provider.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (provider.displayName != provider.degreeName.toDisplayName()) {
                        Text(
                            text = provider.degreeName.toDisplayName(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = provider.studyPathName.toDisplayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                provider.courses.groupBy(TrackedCourse::yearNumber).toSortedMap().forEach { (year, courses) ->
                    SuggestionChip(
                        onClick = onEdit,
                        label = {
                            Text("$year° anno · ${courses.size} ${if (courses.size == 1) "insegnamento" else "insegnamenti"}")
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Elimina ${provider.displayName}")
                }
                FilledTonalButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Modifica", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScheduleProvidersScreenPreview() {
    MaterialTheme {
        ScheduleProvidersScreen(
            providers = listOf(
                ScheduleProvider(
                    id = "1",
                    displayName = "Biologia",
                    cdsCode = "0860",
                    regulationId = 83249,
                    degreeName = "BIOLOGIA",
                    studyPathId = 1,
                    studyPathName = "PERCORSO COMUNE",
                    courses = listOf(
                        TrackedCourse("27002141", "MATEMATICA", 1),
                        TrackedCourse("27002143", "ZOOLOGIA", 1),
                        TrackedCourse("27002200", "GENETICA", 2)
                    )
                )
            ),
            onBack = {},
            onAddProvider = {},
            onEditProvider = {},
            onRemoveProvider = {},
            onRestoreProvider = {}
        )
    }
}
