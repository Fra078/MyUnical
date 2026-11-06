package it.fra.myunical.schedule.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.domain.model.degreeexplorer.AcademicYearCourseGroup
import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.degreeexplorer.SubjectCourse
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.ui.components.ShapeBadge
import it.fra.myunical.ui.components.rememberGroupedItemShape
import it.fra.myunical.schedule.mapper.toDisplayName
import it.fra.myunical.schedule.models.DegreeGroup
import it.fra.myunical.schedule.models.EditorStep
import it.fra.myunical.schedule.models.Loadable
import it.fra.myunical.schedule.models.ProviderEditorIntent
import it.fra.myunical.schedule.models.ProviderEditorUiState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ScheduleProviderEditorScreen(
    providerId: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleProviderEditorViewModel = koinViewModel { parametersOf(providerId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.close.collect { onClose() }
    }

    ScheduleProviderEditorScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onClose = onClose,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleProviderEditorScreen(
    state: ProviderEditorUiState,
    onIntent: (ProviderEditorIntent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // System back walks through the steps before leaving the editor
    BackHandler(enabled = state.step != EditorStep.DEGREE) {
        onIntent(ProviderEditorIntent.Back)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (state.isEditing) "Modifica orario" else "Nuovo orario",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    subtitle = {
                        val degree = state.selectedDegree
                        if (degree != null && state.step != EditorStep.DEGREE) {
                            Text(degree.name.toDisplayName(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        if (state.step == EditorStep.DEGREE) {
                            IconButton(onClick = onClose) {
                                Icon(Icons.Rounded.Close, contentDescription = "Chiudi")
                            }
                        } else {
                            IconButton(onClick = { onIntent(ProviderEditorIntent.Back) }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Passo precedente")
                            }
                        }
                    }
                )
                StepProgress(state)
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.step == EditorStep.COURSES,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SaveBar(state = state, onSave = { onIntent(ProviderEditorIntent.Save) })
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally { width -> if (forward) width / 3 else -width / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { width -> if (forward) -width / 3 else width / 3 } + fadeOut())
            },
            label = "EditorStep",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) { step ->
            when (step) {
                EditorStep.DEGREE -> DegreeStep(state, onIntent)
                EditorStep.PATH -> PathStep(state, onIntent)
                EditorStep.COURSES -> CoursesStep(state, onIntent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StepProgress(state: ProviderEditorUiState) {
    val progress by animateFloatAsState(
        targetValue = state.stepNumber / state.totalSteps.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "StepProgress"
    )
    val stepTitle = when (state.step) {
        EditorStep.DEGREE -> "Corso di laurea"
        EditorStep.PATH -> "Percorso"
        EditorStep.COURSES -> "Insegnamenti"
    }
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Passo ${state.stepNumber} di ${state.totalSteps} · $stepTitle",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// region Step 1: degree

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DegreeStep(state: ProviderEditorUiState, onIntent: (ProviderEditorIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
    ) {
        item(key = "header") {
            StepHeader(
                title = "Qual è il tuo corso di laurea?",
                description = "Cerca per nome, area o dipartimento."
            )
        }
        item(key = "search") {
            DegreeSearchField(
                query = state.degreeQuery,
                onQueryChange = { onIntent(ProviderEditorIntent.UpdateDegreeQuery(it)) }
            )
        }
        item(key = "category") {
            CategoryToggleGroup(
                selected = state.category,
                onSelect = { onIntent(ProviderEditorIntent.SelectCategory(it)) },
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        when (val catalog = state.catalog) {
            Loadable.Loading -> item(key = "loading") { LoadingBlock("Carico i corsi di laurea…") }
            Loadable.Error -> item(key = "error") { ErrorBlock(onRetry = { onIntent(ProviderEditorIntent.Retry) }) }
            is Loadable.Ready -> if (state.degreeGroups.isEmpty()) {
                item(key = "no_results") {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = "Nessun corso trovato",
                        description = if (state.degreeQuery.isBlank()) "Non ci sono corsi in questa categoria."
                        else "Nessun risultato per “${state.degreeQuery}”. Prova con un'altra parola o cambia categoria.",
                        polygon = MaterialShapes.Ghostish
                    )
                }
            } else {
                degreeGroups(
                    groups = state.degreeGroups,
                    selected = state.selectedDegree,
                    onSelect = { onIntent(ProviderEditorIntent.SelectDegree(it)) }
                )
            }
        }
    }
}

private fun LazyListScope.degreeGroups(
    groups: List<DegreeGroup>,
    selected: DegreeProgram?,
    onSelect: (DegreeProgram) -> Unit
) {
    groups.forEach { group ->
        item(key = "area_${group.area}", contentType = "area_header") {
            Text(
                text = group.area,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                    .animateItem()
            )
        }
        itemsIndexed(
            items = group.degrees,
            key = { _, degree -> "degree_${degree.regulationId}" },
            contentType = { _, _ -> "degree" }
        ) { index, degree ->
            DegreeItem(
                degree = degree,
                isSelected = degree.regulationId == selected?.regulationId,
                shape = rememberGroupedItemShape(index, group.degrees.size),
                onClick = { onSelect(degree) },
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .animateItem()
            )
        }
    }
}

@Composable
private fun DegreeSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Es. Ingegneria informatica"
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cancella ricerca")
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoryToggleGroup(
    selected: DegreeCategory,
    onSelect: (DegreeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = DegreeCategory.entries
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        categories.forEachIndexed { index, category ->
            val checked = category == selected
            ToggleButton(
                checked = checked,
                onCheckedChange = { onSelect(category) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    categories.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics { role = Role.RadioButton }
            ) {
                if (checked) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(ToggleButtonDefaults.IconSize))
                    Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
                }
                Text(
                    text = when (category) {
                        DegreeCategory.BACHELOR_AND_SINGLE_CYCLE -> "Triennali e C.U."
                        DegreeCategory.MASTER -> "Magistrali"
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DegreeItem(
    degree: DegreeProgram,
    isSelected: Boolean,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (isSelected) colors.secondaryContainer else colors.surfaceContainer,
        contentColor = if (isSelected) colors.onSecondaryContainer else colors.onSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShapeBadge(
                polygon = if (degree.category == DegreeCategory.MASTER) MaterialShapes.Cookie7Sided else MaterialShapes.Cookie4Sided,
                size = 44.dp,
                containerColor = if (isSelected) colors.primary else colors.primaryContainer,
                contentColor = if (isSelected) colors.onPrimary else colors.onPrimaryContainer
            ) {
                Text(degree.courseType, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = degree.name.toDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val details = listOfNotNull(
                    degree.durationYears.takeIf { it > 0 }?.let { "$it anni" },
                    degree.departmentName.takeIf(String::isNotBlank)
                ).joinToString(" · ")
                if (details.isNotEmpty()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

// endregion

// region Step 2: study path

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PathStep(state: ProviderEditorUiState, onIntent: (ProviderEditorIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "header") {
            StepHeader(
                title = "Scegli il percorso",
                description = "Il corso prevede più percorsi formativi: seleziona quello del tuo piano di studi."
            )
        }
        when (val paths = state.studyPaths) {
            Loadable.Loading -> item(key = "loading") { LoadingBlock("Carico i piani di studio…") }
            Loadable.Error -> item(key = "error") { ErrorBlock(onRetry = { onIntent(ProviderEditorIntent.Retry) }) }
            is Loadable.Ready -> if (paths.value.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Rounded.Route,
                        title = "Piano di studi non disponibile",
                        description = "Per questo corso non è ancora pubblicato un piano di studi. Prova con un altro corso.",
                        polygon = MaterialShapes.Pentagon
                    )
                }
            } else {
                items(paths.value, key = StudyPath::id) { path ->
                    PathItem(
                        path = path,
                        isSelected = path.id == state.selectedPath?.id,
                        onClick = { onIntent(ProviderEditorIntent.SelectStudyPath(path.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PathItem(path: StudyPath, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val courseCount = path.academicYears.sumOf { it.courses.size }
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (isSelected) colors.secondaryContainer else colors.surfaceContainer,
        contentColor = if (isSelected) colors.onSecondaryContainer else colors.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(selected = isSelected, onClick = null, modifier = Modifier.padding(horizontal = 4.dp))
            Column(Modifier.weight(1f)) {
                Text(path.name.toDisplayName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "$courseCount insegnamenti · ${path.academicYears.size} anni",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

// endregion

// region Step 3: courses

@Composable
private fun CoursesStep(state: ProviderEditorUiState, onIntent: (ProviderEditorIntent) -> Unit) {
    val path = state.selectedPath ?: return
    var collapsedYears by remember(path.id) { mutableStateOf(emptySet<Int>()) }
    val totalCourses = path.academicYears.sumOf { it.courses.size }
    val query = state.courseQuery.trim()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
    ) {
        item(key = "header") {
            StepHeader(
                title = "Quali insegnamenti segui?",
                description = "Vedrai solo le lezioni degli insegnamenti selezionati. Puoi selezionare un intero anno in un tocco."
            )
        }
        item(key = "name") {
            OutlinedTextField(
                value = state.displayName,
                onValueChange = { onIntent(ProviderEditorIntent.UpdateDisplayName(it)) },
                label = { Text("Nome dell'orario") },
                supportingText = { Text("Ti aiuta a riconoscerlo se ne aggiungi più di uno") },
                isError = state.displayName.isBlank(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (totalCourses > 8) {
            item(key = "course_search") {
                DegreeSearchField(
                    query = state.courseQuery,
                    onQueryChange = { onIntent(ProviderEditorIntent.UpdateCourseQuery(it)) },
                    placeholder = "Filtra insegnamenti"
                )
            }
        }

        path.academicYears.forEach { yearGroup ->
            val visibleCourses = if (query.isEmpty()) yearGroup.courses
            else yearGroup.courses.filter { it.name.contains(query, ignoreCase = true) || it.code.contains(query) }
            if (visibleCourses.isEmpty()) return@forEach
            val isExpanded = query.isNotEmpty() || yearGroup.yearNumber !in collapsedYears

            item(key = "year_${yearGroup.yearNumber}", contentType = "year_header") {
                YearHeader(
                    yearGroup = yearGroup,
                    selectedCount = state.selectedCount(yearGroup),
                    isExpanded = isExpanded,
                    onToggleExpanded = {
                        collapsedYears = if (isExpanded) collapsedYears + yearGroup.yearNumber
                        else collapsedYears - yearGroup.yearNumber
                    },
                    onSelectAll = { selected ->
                        onIntent(ProviderEditorIntent.SetYearSelected(yearGroup.yearNumber, selected))
                    },
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 8.dp)
                        .animateItem()
                )
            }
            if (isExpanded) {
                itemsIndexed(
                    items = visibleCourses,
                    key = { _, course -> "course_${yearGroup.yearNumber}_${course.code}" },
                    contentType = { _, _ -> "course" }
                ) { index, course ->
                    CourseItem(
                        course = course,
                        isSelected = course.code in state.selectedCourseCodes,
                        shape = rememberGroupedItemShape(index, visibleCourses.size),
                        onToggle = { onIntent(ProviderEditorIntent.ToggleCourse(course.code)) },
                        modifier = Modifier
                            .padding(vertical = 1.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun YearHeader(
    yearGroup: AcademicYearCourseGroup,
    selectedCount: Int,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectAll: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val toggleState = when (selectedCount) {
        0 -> ToggleableState.Off
        yearGroup.courses.size -> ToggleableState.On
        else -> ToggleableState.Indeterminate
    }
    val arrowRotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "YearArrow")

    Surface(
        onClick = onToggleExpanded,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TriStateCheckbox(
                state = toggleState,
                onClick = { onSelectAll(toggleState != ToggleableState.On) }
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${yearGroup.yearNumber}° anno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$selectedCount di ${yearGroup.courses.size} selezionati",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = if (isExpanded) "Comprimi" else "Espandi",
                modifier = Modifier.rotate(arrowRotation)
            )
        }
    }
}

@Composable
private fun CourseItem(
    course: SubjectCourse,
    isSelected: Boolean,
    shape: RoundedCornerShape,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onToggle,
        shape = shape,
        color = if (isSelected) colors.surfaceContainerHighest else colors.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.padding(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = course.name.toDisplayName(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
                val details = listOfNotNull(
                    course.code,
                    course.credits.takeIf { it > 0 }?.let { "$it CFU" },
                    course.moduleCodes.size.takeIf { it > 0 }?.let { if (it == 1) "1 modulo" else "$it moduli" }
                ).joinToString(" · ")
                Text(text = details, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SaveBar(state: ProviderEditorUiState, onSave: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val count = state.selectedCourseCodes.size
            Column(Modifier.weight(1f)) {
                Text(
                    text = when (count) {
                        0 -> "Nessun insegnamento"
                        1 -> "1 insegnamento"
                        else -> "$count insegnamenti"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (count == 0) "Selezionane almeno uno"
                    else state.selectedYears.joinToString(prefix = "Anni: ") { "$it°" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier.heightIn(min = 56.dp)
            ) {
                if (state.isSaving) {
                    LoadingIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (state.isEditing) "Aggiorna" else "Salva", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// endregion

@Composable
private fun StepHeader(title: String, description: String) {
    Column(
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingBlock(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LoadingIndicator(Modifier.size(64.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ErrorBlock(onRetry: () -> Unit) {
    EmptyState(
        icon = Icons.Rounded.CloudOff,
        title = "Qualcosa è andato storto",
        description = "Non è stato possibile contattare il portale di Ateneo. Controlla la connessione e riprova.",
        polygon = MaterialShapes.SoftBurst,
        action = { FilledTonalButton(onClick = onRetry) { Text("Riprova") } }
    )
}

@Preview(showBackground = true)
@Composable
private fun DegreeStepPreview() {
    val degrees = listOf(
        DegreeProgram("0860", 1, "BIOLOGIA", DegreeCategory.BACHELOR_AND_SINGLE_CYCLE, "L", "Scienze", "Dipartimento di Biologia", 3),
        DegreeProgram("0839", 2, "CHIMICA", DegreeCategory.BACHELOR_AND_SINGLE_CYCLE, "L", "Scienze", "Dipartimento di Chimica", 3)
    )
    MaterialTheme {
        ScheduleProviderEditorScreen(
            state = ProviderEditorUiState(
                catalog = Loadable.Ready(degrees),
                degreeGroups = listOf(DegreeGroup("Scienze", degrees))
            ),
            onIntent = {},
            onClose = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CoursesStepPreview() {
    val path = StudyPath(
        id = 1,
        name = "PERCORSO COMUNE",
        academicYears = listOf(
            AcademicYearCourseGroup(1, listOf(SubjectCourse("1", "27002141", "MATEMATICA", 6), SubjectCourse("2", "27002143", "ZOOLOGIA", 9))),
            AcademicYearCourseGroup(2, listOf(SubjectCourse("3", "27002200", "GENETICA", 9)))
        )
    )
    MaterialTheme {
        ScheduleProviderEditorScreen(
            state = ProviderEditorUiState(
                step = EditorStep.COURSES,
                selectedDegree = DegreeProgram("0860", 1, "BIOLOGIA", DegreeCategory.BACHELOR_AND_SINGLE_CYCLE),
                studyPaths = Loadable.Ready(listOf(path)),
                selectedPath = path,
                selectedCourseCodes = setOf("27002141"),
                displayName = "Biologia"
            ),
            onIntent = {},
            onClose = {}
        )
    }
}
