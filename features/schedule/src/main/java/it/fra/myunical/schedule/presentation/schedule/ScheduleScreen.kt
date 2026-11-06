package it.fra.myunical.schedule.presentation.schedule

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.schedule.components.courseAccentColor
import it.fra.myunical.ui.components.rememberGroupedItemShape
import it.fra.myunical.schedule.models.LessonUiModel
import it.fra.myunical.schedule.models.ProviderFilter
import it.fra.myunical.schedule.models.ScheduleDay
import it.fra.myunical.schedule.models.ScheduleIntent
import it.fra.myunical.schedule.models.ScheduleUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val ItalianLocale: Locale = Locale.ITALIAN
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DayHeaderFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", ItalianLocale)

@Composable
fun ScheduleScreen(
    onManageProviders: () -> Unit,
    onAddProvider: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ScheduleScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onManageProviders = onManageProviders,
        onAddProvider = onAddProvider,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleScreen(
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
    onManageProviders: () -> Unit,
    onAddProvider: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val hasProviders = !state.providers.isNullOrEmpty()
    val showProviderFilters = (state.providers?.size ?: 0) > 1

    // Position of every day header inside the agenda, used by the day strip to jump around
    val headerIndexes = remember(state.days, showProviderFilters) {
        var index = if (showProviderFilters) 1 else 0
        state.days.associate { day ->
            (day.date to index).also { index += 1 + day.lessons.size }
        }
    }
    val visibleDay by remember(headerIndexes) {
        derivedStateOf {
            val first = listState.firstVisibleItemIndex
            headerIndexes.entries.lastOrNull { it.value <= first }?.key
                ?: headerIndexes.keys.firstOrNull()
        }
    }

    // Jump to today (or the next day with lessons) whenever a month is shown for the first time
    LaunchedEffect(state.month, state.days.isNotEmpty()) {
        if (state.days.isEmpty()) return@LaunchedEffect
        val target = if (state.isCurrentMonth) {
            state.days.firstOrNull { !it.date.isBefore(state.today) }?.date
        } else null
        listState.scrollToItem(target?.let { headerIndexes[it] } ?: 0)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                MediumFlexibleTopAppBar(
                    title = { Text("Orario", fontWeight = FontWeight.Bold) },
                    subtitle = { Text(state.month.displayName()) },
                    actions = {
                        if (hasProviders) {
                            if (!state.isCurrentMonth) {
                                IconButton(onClick = { onIntent(ScheduleIntent.GoToToday) }) {
                                    Icon(Icons.Rounded.Today, contentDescription = "Vai a oggi")
                                }
                            }
                            IconButton(onClick = { onIntent(ScheduleIntent.PreviousMonth) }) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mese precedente")
                            }
                            IconButton(onClick = { onIntent(ScheduleIntent.NextMonth) }) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mese successivo")
                            }
                        }
                        IconButton(onClick = onManageProviders) {
                            Icon(Icons.Rounded.EditCalendar, contentDescription = "Gestisci orari")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
                if (hasProviders) {
                    DayStrip(
                        month = state.month,
                        today = state.today,
                        selectedDay = visibleDay,
                        daysWithLessons = state.daysWithLessons,
                        onDayClick = { date ->
                            val target = state.days.firstOrNull { !it.date.isBefore(date) } ?: state.days.lastOrNull()
                            target?.let { headerIndexes[it.date] }?.let { index ->
                                scope.launch { listState.animateScrollToItem(index) }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        val pullState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(ScheduleIntent.Refresh) },
            state = pullState,
            enabled = hasProviders,
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
                state.providers == null -> CenteredLoading()
                state.providers.isEmpty() -> NoProvidersState(onAddProvider)
                else -> Agenda(
                    state = state,
                    listState = listState,
                    showProviderFilters = showProviderFilters,
                    onIntent = onIntent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Agenda(
    state: ScheduleUiState,
    listState: LazyListState,
    showProviderFilters: Boolean,
    onIntent: (ScheduleIntent) -> Unit
) {
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            delay(30_000)
            value = LocalDateTime.now()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (showProviderFilters) {
            item(key = "provider_filters", contentType = "filters") {
                ProviderFilters(
                    providers = state.providers.orEmpty(),
                    onToggle = { onIntent(ScheduleIntent.ToggleProvider(it)) }
                )
            }
        }

        when {
            state.days.isEmpty() && state.isLoading -> item(key = "loading") { CenteredLoading(Modifier.padding(top = 96.dp)) }
            state.days.isEmpty() && state.hasError -> item(key = "error") {
                EmptyState(
                    icon = Icons.Rounded.CloudOff,
                    title = "Orario non disponibile",
                    description = "Non è stato possibile scaricare le lezioni. Controlla la connessione e riprova.",
                    polygon = MaterialShapes.SoftBurst,
                    action = {
                        FilledTonalButton(onClick = { onIntent(ScheduleIntent.Refresh) }) { Text("Riprova") }
                    }
                )
            }
            state.days.isEmpty() -> item(key = "empty") {
                EmptyState(
                    icon = Icons.Rounded.EventBusy,
                    title = "Nessuna lezione",
                    description = "Non ci sono lezioni a ${state.month.displayName().lowercase(ItalianLocale)} " +
                        "per gli insegnamenti selezionati.",
                    polygon = MaterialShapes.Cookie6Sided
                )
            }
            else -> state.days.forEach { day ->
                stickyHeader(key = "header_${day.date}", contentType = "day_header") {
                    DayHeader(day = day, today = state.today)
                }
                itemsIndexed(
                    items = day.lessons,
                    key = { _, lesson -> lesson.key },
                    contentType = { _, _ -> "lesson" }
                ) { index, lesson ->
                    LessonCard(
                        lesson = lesson,
                        isOngoing = lesson.date == now.toLocalDate() &&
                            !now.toLocalTime().isBefore(lesson.start) && now.toLocalTime().isBefore(lesson.end),
                        isPast = LocalDateTime.of(lesson.date, lesson.end).isBefore(now),
                        shape = rememberGroupedItemShape(index, day.lessons.size),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 1.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun DayStrip(
    month: YearMonth,
    today: LocalDate,
    selectedDay: LocalDate?,
    daysWithLessons: Set<LocalDate>,
    onDayClick: (LocalDate) -> Unit
) {
    val stripState = rememberLazyListState()
    val days = remember(month) { (1..month.lengthOfMonth()).map(month::atDay) }

    LaunchedEffect(month, selectedDay) {
        val index = selectedDay?.dayOfMonth?.minus(1)
            ?: if (YearMonth.from(today) == month) today.dayOfMonth - 1 else 0
        stripState.animateScrollToItem((index - 2).coerceAtLeast(0))
    }

    LazyRow(
        state = stripState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(days, key = { it.toEpochDay() }) { date ->
            DayChip(
                date = date,
                isToday = date == today,
                isSelected = date == selectedDay,
                hasLessons = date in daysWithLessons,
                onClick = { onDayClick(date) }
            )
        }
    }
}

@Composable
private fun DayChip(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    hasLessons: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    // Expressive shape morph: the selected day squares up, the others stay pill-shaped
    val corner by animateDpAsState(if (isSelected) 16.dp else 28.dp, label = "DayChipCorner")
    val container by animateColorAsState(
        when {
            isSelected -> colors.primary
            isToday -> colors.primaryContainer
            else -> colors.surfaceContainerHigh
        },
        label = "DayChipContainer"
    )
    val content = when {
        isSelected -> colors.onPrimary
        isToday -> colors.onPrimaryContainer
        hasLessons -> colors.onSurface
        else -> colors.onSurfaceVariant.copy(alpha = 0.6f)
    }
    val shape = RoundedCornerShape(corner)

    Column(
        modifier = Modifier
            .width(52.dp)
            .clip(shape)
            .background(container, shape)
            .then(if (isToday && !isSelected) Modifier.border(2.dp, colors.primary, shape) else Modifier)
            .clickable(enabled = hasLessons, role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, ItalianLocale).take(3),
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = content
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(if (hasLessons) content else container, CircleShape)
        )
    }
}

@Composable
private fun ProviderFilters(
    providers: List<ProviderFilter>,
    onToggle: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        items(providers, key = ProviderFilter::id) { provider ->
            FilterChip(
                selected = provider.visible,
                onClick = { onToggle(provider.id) },
                label = { Text(provider.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = if (provider.visible) {
                    { Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else null
            )
        }
    }
}

@Composable
private fun DayHeader(day: ScheduleDay, today: LocalDate) {
    val relative = when (day.date) {
        today -> "Oggi"
        today.plusDays(1) -> "Domani"
        else -> null
    }
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (relative != null) {
                Text(
                    text = relative,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
            Text(
                text = day.date.format(DayHeaderFormatter).replaceFirstChar { it.titlecase(ItalianLocale) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (day.lessons.size == 1) "1 lezione" else "${day.lessons.size} lezioni",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LessonUiModel,
    isOngoing: Boolean,
    isPast: Boolean,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val accent = courseAccentColor(lesson.courseCode)
    val contentAlpha = if (isPast) 0.6f else 1f

    Surface(
        shape = shape,
        color = if (isOngoing) colors.primaryContainer else colors.surfaceContainer,
        contentColor = if (isOngoing) colors.onPrimaryContainer else colors.onSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.width(48.dp)) {
                Text(
                    text = lesson.start.format(TimeFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColorWithAlpha(contentAlpha)
                )
                Text(
                    text = lesson.end.format(TimeFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant.copy(alpha = contentAlpha)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = Duration.between(lesson.start, lesson.end).toDisplayString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent.copy(alpha = contentAlpha), CircleShape)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isOngoing) {
                    Text(
                        text = "IN CORSO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColorWithAlpha(contentAlpha)
                )
                lesson.location?.let { LessonDetail(Icons.Rounded.Place, it, contentAlpha) }
                lesson.teachers?.let { LessonDetail(Icons.Rounded.Person, it, contentAlpha) }
                lesson.providerName?.let {
                    LessonDetail(Icons.Rounded.CalendarMonth, it, contentAlpha)
                }
            }
        }
    }
}

@Composable
private fun LessonDetail(icon: ImageVector, text: String, alpha: Float) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@ReadOnlyComposable
private fun contentColorWithAlpha(alpha: Float) =
    LocalContentColor.current.copy(alpha = alpha)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NoProvidersState(onAddProvider: () -> Unit) {
    // Scrollable so that pull-to-refresh nested scrolling keeps working
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        item {
            EmptyState(
                icon = Icons.Rounded.CalendarMonth,
                title = "Nessun orario configurato",
                description = "Scegli il tuo corso di laurea e gli insegnamenti che segui per vedere qui le tue lezioni.",
                polygon = MaterialShapes.Cookie9Sided,
                action = {
                    Button(onClick = onAddProvider, modifier = Modifier.heightIn(min = 56.dp)) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Aggiungi orario", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CenteredLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        LoadingIndicator(Modifier.size(64.dp))
    }
}

private fun YearMonth.displayName(): String =
    "${month.getDisplayName(TextStyle.FULL_STANDALONE, ItalianLocale).replaceFirstChar { it.titlecase(ItalianLocale) }} $year"

private fun Duration.toDisplayString(): String {
    val hours = toHours()
    val minutes = toMinutes() % 60
    return when {
        hours == 0L -> "${minutes}m"
        minutes == 0L -> "${hours}h"
        else -> "${hours}h${minutes}"
    }
}

@Preview(showBackground = true)
@Composable
private fun ScheduleScreenPreview() {
    val today = LocalDate.of(2026, 11, 6)
    val lessons = listOf(
        LessonUiModel("1", null, "27002141", "Matematica", today, LocalTime.of(9, 0), LocalTime.of(11, 0), "MT5, Cubo 31A", "Rossi Mario"),
        LessonUiModel("2", null, "27002143", "Zoologia", today, LocalTime.of(11, 30), LocalTime.of(13, 30), "5C1, Cubo 5C", "Bianchi Laura")
    )
    MaterialTheme {
        ScheduleScreen(
            state = ScheduleUiState(
                month = YearMonth.from(today),
                today = today,
                providers = listOf(ProviderFilter("p", "Biologia", true)),
                days = listOf(ScheduleDay(today, lessons), ScheduleDay(today.plusDays(3), lessons))
            ),
            onIntent = {},
            onManageProviders = {},
            onAddProvider = {}
        )
    }
}
