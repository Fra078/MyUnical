package it.fra.myunical.canteen.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material.icons.rounded.NoMeals
import androidx.compose.material3.ButtonGroupDefaults
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.canteen.mapper.label
import it.fra.myunical.canteen.mapper.toDayOption
import it.fra.myunical.canteen.models.CanteenIntent
import it.fra.myunical.canteen.models.CanteenUiState
import it.fra.myunical.canteen.models.CourseUiModel
import it.fra.myunical.canteen.models.DayOption
import it.fra.myunical.canteen.models.DishUiModel
import it.fra.myunical.domain.model.canteen.CourseType
import it.fra.myunical.domain.model.canteen.DietFilter
import it.fra.myunical.domain.model.canteen.DietTag
import it.fra.myunical.domain.model.canteen.DishLine
import it.fra.myunical.domain.model.canteen.MealType
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.ui.components.GroupHeader
import it.fra.myunical.ui.components.openInCustomTab
import it.fra.myunical.ui.components.rememberGroupedItemShape
import it.fra.myunical.ui.theme.Accent
import it.fra.myunical.ui.theme.role
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

/** Page of the university with the canteen services and the published PDFs. */
const val CANTEEN_PAGE_URL = "https://www.unical.it/campus/vivere-il-campus/servizio-mensa/"

@Composable
fun CanteenScreen(
    onBack: () -> Unit,
    viewModel: CanteenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    CanteenScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onOpenPage = { context.openInCustomTab(CANTEEN_PAGE_URL) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CanteenScreen(
    state: CanteenUiState,
    onIntent: (CanteenIntent) -> Unit,
    onBack: () -> Unit,
    onOpenPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Mensa", fontWeight = FontWeight.Bold) },
                subtitle = {
                    Text(state.coveredUntil?.let { "Menù ordinario fino al $it" } ?: "Menù ordinario")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenPage) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Servizio mensa sul sito Unical")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> LoadingIndicator(Modifier.size(64.dp).align(Alignment.Center))
                state.hasError -> EmptyState(
                    icon = Icons.Rounded.CloudOff,
                    title = "Menù non disponibili",
                    description = "Non è stato possibile leggere i menù della mensa.",
                    polygon = MaterialShapes.SoftBurst,
                    action = { FilledTonalButton(onClick = { onIntent(CanteenIntent.Retry) }) { Text("Riprova") } }
                )
                else -> CanteenContent(state, listState, onIntent, onOpenPage)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CanteenContent(
    state: CanteenUiState,
    listState: LazyListState,
    onIntent: (CanteenIntent) -> Unit,
    onOpenPage: () -> Unit
) {
    // A new day or meal starts from the top of its dishes
    LaunchedEffect(state.selectedDate, state.meal) {
        if (listState.firstVisibleItemIndex > 3) listState.animateScrollToItem(3)
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item(key = "days") {
            DayStrip(state.days, state.selectedDate) { onIntent(CanteenIntent.SelectDay(it)) }
        }
        item(key = "meal") {
            MealSelector(
                selected = state.meal,
                onSelect = { onIntent(CanteenIntent.SelectMeal(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item(key = "filters") {
            FilterRow(state.filters) { onIntent(CanteenIntent.ToggleFilter(it)) }
        }

        val courses = state.courses
        when {
            courses == null -> item(key = "no_menu") {
                EmptyState(
                    icon = Icons.Rounded.NoMeals,
                    title = "Menù non ancora disponibile",
                    description = "I menù di questo giorno non sono ancora stati pubblicati. Controlla il sito per eventuali aggiornamenti.",
                    polygon = MaterialShapes.Cookie6Sided,
                    action = { TextButton(onClick = onOpenPage) { Text("Apri il sito della mensa") } }
                )
            }
            courses.isEmpty() -> item(key = "filtered_out") {
                EmptyState(
                    icon = Icons.Rounded.FilterAltOff,
                    title = "Nessun piatto",
                    description = "Nessun piatto di questo pasto rispetta tutti i filtri scelti.",
                    polygon = MaterialShapes.Cookie6Sided
                )
            }
            else -> {
                courses.forEach { course ->
                    item(key = "header_${course.type}") {
                        GroupHeader(course.title, Modifier.padding(horizontal = 16.dp))
                    }
                    itemsIndexed(course.dishes, key = { index, _ -> "${course.type}_$index" }) { index, dish ->
                        DishRow(
                            dish = dish,
                            index = index,
                            count = course.dishes.size,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 1.dp)
                                .animateItem()
                        )
                    }
                }
                item(key = "footer") {
                    Text(
                        listOfNotNull(
                            "Pane e frutta sono sempre inclusi.",
                            if (state.hiddenCount > 0) "${state.hiddenCount} piatti nascosti dai filtri." else null
                        ).joinToString(" "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayStrip(days: List<DayOption>, selected: LocalDate?, onSelect: (LocalDate) -> Unit) {
    val index = days.indexOfFirst { it.date == selected }.coerceAtLeast(0)
    val stripState = rememberLazyListState(initialFirstVisibleItemIndex = (index - 2).coerceAtLeast(0))
    LazyRow(
        state = stripState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days, key = { it.date.toEpochDay() }) { day ->
            DayChip(day, isSelected = day.date == selected, onClick = { onSelect(day.date) })
        }
    }
}

@Composable
private fun DayChip(day: DayOption, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Expressive shape morph: the selected day squares up, the others stay pill-shaped
    val corner by animateDpAsState(if (isSelected) 16.dp else 28.dp, label = "DayChipCorner")
    val container by animateColorAsState(
        when {
            isSelected -> colors.primary
            day.isToday -> colors.secondaryContainer
            else -> colors.surfaceContainerHigh
        },
        label = "DayChipContainer"
    )
    val content = when {
        isSelected -> colors.onPrimary
        day.isToday -> colors.onSecondaryContainer
        else -> colors.onSurface
    }
    val shape = RoundedCornerShape(corner)
    Column(
        modifier = Modifier
            .width(52.dp)
            .clip(shape)
            .background(container, shape)
            .then(if (day.isToday && !isSelected) Modifier.border(2.dp, colors.secondary, shape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(day.weekday, style = MaterialTheme.typography.labelMedium, color = content)
        Text(day.day, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = content)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MealSelector(selected: MealType, onSelect: (MealType) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        MealType.entries.forEachIndexed { index, meal ->
            val checked = meal == selected
            ToggleButton(
                checked = checked,
                onCheckedChange = { onSelect(meal) },
                shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes()
                else ButtonGroupDefaults.connectedTrailingButtonShapes(),
                modifier = Modifier.weight(1f)
            ) {
                if (checked) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(ToggleButtonDefaults.IconSize))
                    Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
                }
                Text(meal.label)
            }
        }
    }
}

@Composable
private fun FilterRow(filters: Set<DietFilter>, onToggle: (DietFilter) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items(DietFilter.entries) { filter ->
            val selected = filter in filters
            FilterChip(
                selected = selected,
                onClick = { onToggle(filter) },
                label = { Text(filter.label) },
                leadingIcon = if (selected) {
                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                shape = CircleShape
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DishRow(dish: DishUiModel, index: Int, count: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = rememberGroupedItemShape(index, count),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LinePill(dish.line)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    dish.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (dish.frozen) {
                    Icon(
                        Icons.Rounded.AcUnit,
                        contentDescription = "Prodotto surgelato o che può contenerne",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (dish.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    dish.tags.forEach { TagLabel(it) }
                }
            }
        }
    }
}

@Composable
private fun LinePill(line: DishLine) {
    val role = line.accent.role
    Text(
        line.label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = role.onContainer,
        modifier = Modifier
            .background(role.container, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun TagLabel(tag: DietTag) {
    val role = tag.accent.role
    Text(
        tag.label,
        style = MaterialTheme.typography.labelMedium,
        color = role.color,
        modifier = Modifier
            .border(1.dp, role.color.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

private val DishLine.accent: Accent
    get() = when (this) {
        DishLine.CLASSIC -> Accent.PRIMARY
        DishLine.VEGETARIAN -> Accent.SUCCESS
        DishLine.SOUP -> Accent.WARNING
        DishLine.COLD -> Accent.INFO
        DishLine.MEDITERRANEAN -> Accent.SECONDARY
    }

private val DietTag.accent: Accent
    get() = when (this) {
        DietTag.VEGAN, DietTag.VEGETARIAN -> Accent.SUCCESS
        DietTag.GLUTEN_FREE -> Accent.TERTIARY
        DietTag.LACTOSE_FREE -> Accent.INFO
        DietTag.LIGHT -> Accent.SECONDARY
        DietTag.PORK -> Accent.NEUTRAL
    }

@Preview(showBackground = true)
@Composable
private fun CanteenScreenPreview() {
    val today = LocalDate.of(2026, 11, 9)
    MaterialTheme {
        CanteenScreen(
            state = CanteenUiState(
                isLoading = false,
                days = (0L..6L).map { today.plusDays(it).toDayOption(today) },
                selectedDate = today,
                coveredUntil = "15 novembre",
                courses = listOf(
                    CourseUiModel(
                        CourseType.FIRST, "Primi piatti", listOf(
                            DishUiModel("Pasta al prosciutto", DishLine.CLASSIC, listOf(DietTag.PORK), false),
                            DishUiModel("Penne rigate con salmone", DishLine.CLASSIC, emptyList(), true),
                            DishUiModel("Pennette con broccoletti", DishLine.VEGETARIAN, listOf(DietTag.VEGETARIAN), false)
                        )
                    )
                )
            ),
            onIntent = {},
            onBack = {},
            onOpenPage = {}
        )
    }
}
