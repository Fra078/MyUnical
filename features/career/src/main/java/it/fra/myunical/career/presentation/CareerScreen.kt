package it.fra.myunical.career.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.career.models.CareerIntent
import it.fra.myunical.career.models.CareerOption
import it.fra.myunical.career.models.CareerSection
import it.fra.myunical.career.models.CareerUiState
import it.fra.myunical.career.models.ExamFilter
import it.fra.myunical.career.models.ExamGroup
import it.fra.myunical.career.models.ExamRecord
import it.fra.myunical.career.models.ExamSortOption
import it.fra.myunical.career.models.OverviewUiModel
import it.fra.myunical.career.models.SessionUiModel
import it.fra.myunical.domain.model.career.BookingState
import it.fra.myunical.ui.Esse3Links
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.ui.components.ShapeBadge
import it.fra.myunical.ui.components.openInCustomTab
import it.fra.myunical.ui.components.rememberGroupedItemShape
import it.fra.myunical.ui.theme.Accent
import it.fra.myunical.ui.theme.extendedColors
import it.fra.myunical.ui.theme.role
import org.koin.androidx.compose.koinViewModel

/**
 * @param reselectSignal incremented by the navigation bar when the tab is selected again: scrolls back to the top.
 */
@Composable
fun CareerScreen(
    onOpenCourse: (Long) -> Unit,
    reselectSignal: Int = 0,
    viewModel: CareerViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    CareerScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onOpenPortal = { context.openInCustomTab(it) },
        onOpenCourse = onOpenCourse,
        reselectSignal = reselectSignal
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CareerScreen(
    state: CareerUiState,
    onIntent: (CareerIntent) -> Unit,
    onOpenPortal: (String) -> Unit,
    onOpenCourse: (Long) -> Unit,
    modifier: Modifier = Modifier,
    reselectSignal: Int = 0
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }
    var showCareerSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(reselectSignal) {
        if (reselectSignal > 0) listState.animateScrollToItem(0)
    }
    // Full-screen error only without data; otherwise keep the old data and notify
    LaunchedEffect(state.hasError, state.hasContent) {
        if (state.hasError && state.hasContent) snackbarHostState.showSnackbar("Impossibile aggiornare la carriera")
    }

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = state.sort,
            onSortSelected = {
                onIntent(CareerIntent.SetSort(it))
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }
    if (showCareerSheet) {
        CareerPickerSheet(
            careers = state.careers,
            onSelect = {
                onIntent(CareerIntent.SwitchCareer(it))
                showCareerSheet = false
            },
            onDismiss = { showCareerSheet = false }
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Carriera", fontWeight = FontWeight.Bold) },
                subtitle = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(enabled = state.canSwitchCareer) { showCareerSheet = true }
                    ) {
                        Text(
                            text = state.careerName.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (state.canSwitchCareer) {
                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Cambia carriera")
                        }
                    }
                },
                actions = {
                    if (state.section == CareerSection.LIBRETTO) {
                        IconButton(onClick = { showSortSheet = true }) {
                            Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Ordina esami")
                        }
                    } else {
                        IconButton(onClick = { onOpenPortal(Esse3Links.EXAM_BOOKINGS) }) {
                            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Le mie prenotazioni sul portale")
                        }
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
            onRefresh = { onIntent(CareerIntent.Refresh) },
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
                !state.hasContent -> LazyColumn(Modifier.fillMaxSize()) {
                    item {
                        EmptyState(
                            icon = Icons.Rounded.CloudOff,
                            title = "Carriera non disponibile",
                            description = "Non è stato possibile scaricare il libretto. Controlla la connessione e riprova.",
                            polygon = MaterialShapes.SoftBurst,
                            action = {
                                FilledTonalButton(onClick = { onIntent(CareerIntent.Refresh) }) { Text("Riprova") }
                            }
                        )
                    }
                }
                else -> CareerContent(state, listState, onIntent, onOpenPortal, onOpenCourse)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CareerContent(
    state: CareerUiState,
    listState: LazyListState,
    onIntent: (CareerIntent) -> Unit,
    onOpenPortal: (String) -> Unit,
    onOpenCourse: (Long) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        state.overview?.let { overview ->
            item(key = "overview", contentType = "overview") {
                ProgressCard(overview, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
        item(key = "sections", contentType = "sections") {
            SectionSelector(
                selected = state.section,
                openSessions = state.openSessionsCount,
                onSelect = { onIntent(CareerIntent.SelectSection(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        when (state.section) {
            CareerSection.LIBRETTO -> libretto(state, onIntent, onOpenCourse)
            CareerSection.SESSIONS -> sessions(state, onOpenPortal)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.libretto(
    state: CareerUiState,
    onIntent: (CareerIntent) -> Unit,
    onOpenCourse: (Long) -> Unit
) {
    item(key = "search", contentType = "search") {
        OutlinedTextField(
            value = state.query,
            onValueChange = { onIntent(CareerIntent.SetQuery(it)) },
            placeholder = { Text("Cerca un esame") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { onIntent(CareerIntent.SetQuery("")) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Cancella ricerca")
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
    item(key = "filters", contentType = "filters") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(ExamFilter.entries) { filter ->
                val selected = filter == state.filter
                FilterChip(
                    selected = selected,
                    onClick = { onIntent(CareerIntent.SetFilter(filter)) },
                    label = { Text(filter.label) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    shape = CircleShape
                )
            }
        }
    }

    if (state.examGroups.isEmpty()) {
        item(key = "no_exams") {
            EmptyState(
                icon = Icons.Rounded.SearchOff,
                title = "Nessun esame",
                description = if (state.query.isNotBlank()) "Nessun esame corrisponde a \"${state.query}\"."
                else "Non ci sono esami per il filtro selezionato.",
                polygon = MaterialShapes.Cookie6Sided
            )
        }
    }

    state.examGroups.forEach { group ->
        if (group.label != null) {
            stickyHeader(key = "group_${group.label}", contentType = "group_header") {
                GroupHeader(group)
            }
        } else {
            item(key = "group_spacer", contentType = "spacer") { Spacer(Modifier.height(8.dp)) }
        }
        itemsIndexed(
            items = group.exams,
            key = { _, exam -> exam.id },
            contentType = { _, _ -> "exam" }
        ) { index, exam ->
            ExamRow(
                exam = exam,
                index = index,
                count = group.exams.size,
                onClick = { onOpenCourse(exam.id) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 1.dp)
                    .animateItem()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.sessions(
    state: CareerUiState,
    onOpenPortal: (String) -> Unit
) {
    val sessions = state.sessions
    when {
        sessions == null -> item(key = "sessions_error") {
            EmptyState(
                icon = Icons.Rounded.CloudOff,
                title = "Appelli non disponibili",
                description = "Non è stato possibile scaricare gli appelli. Trascina verso il basso per riprovare.",
                polygon = MaterialShapes.SoftBurst
            )
        }
        sessions.isEmpty() -> item(key = "sessions_empty") {
            EmptyState(
                icon = Icons.Rounded.EventBusy,
                title = "Nessun appello in programma",
                description = "Quando verranno pubblicati nuovi appelli per i tuoi esami li troverai qui.",
                polygon = MaterialShapes.Cookie6Sided
            )
        }
        else -> items(sessions, key = { it.id }, contentType = { "session" }) { session ->
            SessionCard(
                session = session,
                onBook = { onOpenPortal(Esse3Links.EXAM_SESSIONS) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .animateItem()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProgressCard(overview: OverviewUiModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator(
                        progress = { overview.progress },
                        modifier = Modifier.size(112.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${overview.earnedCredits}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("di ${overview.requiredCredits} CFU", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Progresso", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${(overview.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(overview.passedLabel, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("Media ponderata", overview.weightedAverage, Modifier.weight(1f))
                MetricTile("Media aritm.", overview.arithmeticAverage, Modifier.weight(1f))
                MetricTile("Base /110", overview.graduationBase, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.7f)
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SectionSelector(
    selected: CareerSection,
    openSessions: Int,
    onSelect: (CareerSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        CareerSection.entries.forEachIndexed { index, section ->
            val checked = section == selected
            ToggleButton(
                checked = checked,
                onCheckedChange = { onSelect(section) },
                shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes()
                else ButtonGroupDefaults.connectedTrailingButtonShapes(),
                modifier = Modifier.weight(1f)
            ) {
                if (checked) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(ToggleButtonDefaults.IconSize))
                    Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
                }
                Text(
                    if (section == CareerSection.SESSIONS && openSessions > 0) "${section.label} · $openSessions"
                    else section.label
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(group: ExamGroup) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                group.label.orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${group.exams.count { it.isPassed }}/${group.exams.size} superati",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExamRow(exam: ExamRecord, index: Int, count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = rememberGroupedItemShape(index, count),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GradeBadge(exam)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    exam.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    listOfNotNull("${exam.credits} CFU", exam.examType).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (exam.passedOn != null) {
                    Text(
                        "Superato il ${exam.passedOn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            if (exam.openSessions > 0) {
                Text(
                    text = if (exam.openSessions == 1) "1 appello" else "${exam.openSessions} appelli",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun GradeBadge(exam: ExamRecord) {
    val colors = MaterialTheme.colorScheme
    val numeric = exam.grade?.firstOrNull()?.isDigit() == true
    when {
        exam.hasHonors -> ShapeBadge(
            polygon = MaterialShapes.Sunny, size = 52.dp,
            containerColor = colors.tertiary, contentColor = colors.onTertiary
        ) { Text(exam.grade.orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        exam.grade != null && numeric -> ShapeBadge(
            polygon = MaterialShapes.Cookie9Sided, size = 52.dp,
            containerColor = colors.secondaryContainer, contentColor = colors.onSecondaryContainer
        ) { Text(exam.grade, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        exam.grade != null -> ShapeBadge(
            polygon = MaterialShapes.Cookie9Sided, size = 52.dp,
            containerColor = MaterialTheme.extendedColors.success.container,
            contentColor = MaterialTheme.extendedColors.success.onContainer
        ) { Icon(Icons.Rounded.Check, contentDescription = exam.grade) }
        else -> ShapeBadge(
            polygon = MaterialShapes.Circle, size = 52.dp,
            containerColor = colors.surfaceContainerHighest, contentColor = colors.onSurfaceVariant
        ) { Icon(Icons.Rounded.HourglassEmpty, contentDescription = "Da sostenere", modifier = Modifier.size(22.dp)) }
    }
}

@Composable
internal fun SessionCard(session: SessionUiModel, onBook: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val accent = session.state.accent().role
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DateTile(session)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    session.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (session.description.isNotBlank()) {
                    Text(session.description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                session.time?.let { InfoLine(Icons.Rounded.Schedule, it) }
                session.president?.let { InfoLine(Icons.Rounded.Person, it) }
                Text(
                    text = session.stateLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent.onContainer,
                    modifier = Modifier
                        .background(accent.container, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
                session.bookingDetail?.let { InfoLine(Icons.Rounded.EventAvailable, it) }
                AnimatedVisibility(visible = session.state == BookingState.OPEN) {
                    Button(onClick = onBook, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Prenota sul portale")
                    }
                }
            }
        }
    }
}

@Composable
private fun DateTile(session: SessionUiModel) {
    // Booked sessions stand out with the solid color, the others use the container of their state
    val accent = session.state.accent().role
    val booked = session.state == BookingState.BOOKED
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (booked) accent.color else accent.container,
        contentColor = if (booked) accent.onColor else accent.onContainer,
        modifier = Modifier.width(60.dp)
    ) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(session.weekday, style = MaterialTheme.typography.labelSmall)
            Text(session.day, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(session.month, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Booked → blue, open → green, opening soon → orange, closed → neutral. */
private fun BookingState.accent(): Accent = when (this) {
    BookingState.BOOKED -> Accent.SECONDARY
    BookingState.OPEN -> Accent.SUCCESS
    BookingState.NOT_YET_OPEN -> Accent.WARNING
    BookingState.CLOSED -> Accent.NEUTRAL
}

@Composable
private fun InfoLine(icon: ImageVector, text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBottomSheet(
    currentSort: ExamSortOption,
    onSortSelected: (ExamSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "Ordina esami",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            ExamSortOption.entries.forEachIndexed { index, option ->
                val selected = option == currentSort
                SheetOption(
                    index = index,
                    count = ExamSortOption.entries.size,
                    selected = selected,
                    title = option.title,
                    subtitle = option.subtitle,
                    onClick = { onSortSelected(option) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CareerPickerSheet(
    careers: List<CareerOption>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "Cambia carriera",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
            Text(
                "La carriera scelta viene usata anche per Home e Tasse.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            careers.forEachIndexed { index, career ->
                SheetOption(
                    index = index,
                    count = careers.size,
                    selected = career.isSelected,
                    title = career.name,
                    subtitle = "${career.status} · Matr. ${career.matricola}",
                    onClick = { onSelect(career.matId) },
                    leading = {
                        Icon(Icons.Rounded.School, contentDescription = null)
                    }
                )
            }
        }
    }
}

@Composable
private fun SheetOption(
    index: Int,
    count: Int,
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null
) {
    val shape = rememberGroupedItemShape(index, count)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            leading?.invoke()
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CareerScreenPreview() {
    MaterialTheme {
        CareerScreen(
            state = CareerUiState(
                isLoading = false,
                careerName = "Ingegneria Informatica",
                careers = listOf(
                    CareerOption(1, "Telecommunication Engineering", "Attiva", "204512", false),
                    CareerOption(2, "Ingegneria Informatica", "Titolo conseguito", "123456", true)
                ),
                overview = OverviewUiModel(114, 180, 0.63f, "27,68", "27,76", "101,51", "15 di 23 esami superati"),
                examGroups = listOf(
                    ExamGroup(
                        "1° anno",
                        listOf(
                            ExamRecord(1, "Analisi Matematica", 9, "Scritto e orale", "30L", true, true, "15/02/2024", 0),
                            ExamRecord(2, "Fisica", 9, "Scritto", "27", false, true, "22/06/2024", 0),
                            ExamRecord(3, "Lingua Inglese", 3, null, "Idoneo", false, true, "10/01/2024", 0),
                            ExamRecord(4, "Chimica", 6, "Orale", null, false, false, null, 2)
                        )
                    )
                )
            ),
            onIntent = {},
            onOpenPortal = {},
            onOpenCourse = {}
        )
    }
}
