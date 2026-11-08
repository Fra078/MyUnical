package it.fra.myunical.career.presentation.detail

import it.fra.myunical.ui.rememberLinkOpener
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.career.models.CatalogState
import it.fra.myunical.career.models.CourseDetailUiState
import it.fra.myunical.career.models.ExamRecord
import it.fra.myunical.career.models.SyllabusFact
import it.fra.myunical.career.models.SyllabusSectionUiModel
import it.fra.myunical.career.models.SyllabusUiModel
import it.fra.myunical.career.models.TeacherUiModel
import it.fra.myunical.career.models.UnitOption
import it.fra.myunical.career.models.WorkloadUiModel
import it.fra.myunical.career.presentation.GradeBadge
import it.fra.myunical.career.presentation.SessionCard
import it.fra.myunical.ui.Esse3Links
import it.fra.myunical.ui.components.EmptyState
import it.fra.myunical.ui.components.GroupHeader
import it.fra.myunical.ui.components.GroupedListItem
import it.fra.myunical.ui.theme.extendedColors
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CourseDetailScreen(
    courseRowId: Long,
    onBack: () -> Unit,
    onOpenContact: (String) -> Unit,
    viewModel: CourseDetailViewModel = koinViewModel(key = "course_$courseRowId") { parametersOf(courseRowId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val openLink = rememberLinkOpener()
    CourseDetailScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onSelectUnit = viewModel::selectUnit,
        onOpenContact = onOpenContact,
        onOpenUrl = { openLink(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CourseDetailScreen(
    state: CourseDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelectUnit: (Int) -> Unit,
    onOpenContact: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    // The name is in the header; the bar shows it once the header scrolls away
    val showTitle by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    if (state is CourseDetailUiState.Loaded && showTitle) {
                        Text(state.exam.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Indietro")
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
            when (state) {
                CourseDetailUiState.Loading -> LoadingIndicator(Modifier.size(64.dp).align(Alignment.Center))
                CourseDetailUiState.Error -> EmptyState(
                    icon = Icons.Rounded.CloudOff,
                    title = "Insegnamento non disponibile",
                    description = "Non è stato possibile caricare il libretto. Controlla la connessione e riprova.",
                    polygon = MaterialShapes.SoftBurst,
                    action = { FilledTonalButton(onClick = onRetry) { Text("Riprova") } }
                )
                is CourseDetailUiState.Loaded -> CourseDetail(state, listState, onRetry, onSelectUnit, onOpenContact, onOpenUrl)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CourseDetail(
    state: CourseDetailUiState.Loaded,
    listState: LazyListState,
    onRetry: () -> Unit,
    onSelectUnit: (Int) -> Unit,
    onOpenContact: (String) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
    ) {
        item(key = "header") { Header(state.exam, state.subtitle) }

        if (state.sessions.isNotEmpty()) {
            item(key = "sessions_header") { GroupHeader("Prossimi appelli") }
            items(state.sessions, key = { "session_${it.id}" }) { session ->
                SessionCard(
                    session = session,
                    onBook = { onOpenUrl(Esse3Links.EXAM_SESSIONS) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        item(key = "catalog_header") { GroupHeader("Scheda dell'insegnamento") }
        if (state.units.size > 1) {
            item(key = "units") { UnitSelector(state.units, state.selectedUnit, onSelectUnit) }
            state.units.getOrNull(state.selectedUnit)?.title?.let { title ->
                item(key = "unit_title") {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                }
            }
        }
        when (val catalog = state.catalog) {
            CatalogState.Loading -> item(key = "catalog_loading") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) { LoadingIndicator() }
            }
            CatalogState.NotFound -> item(key = "catalog_missing") {
                EmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = "Scheda non disponibile",
                    description = "Questo insegnamento non ha una scheda pubblica sul portale dei corsi.",
                    polygon = MaterialShapes.Cookie6Sided
                )
            }
            CatalogState.Error -> item(key = "catalog_error") {
                EmptyState(
                    icon = Icons.Rounded.CloudOff,
                    title = "Scheda non disponibile",
                    description = "Non è stato possibile scaricare la scheda dal portale dei corsi.",
                    polygon = MaterialShapes.SoftBurst,
                    action = { FilledTonalButton(onClick = onRetry) { Text("Riprova") } }
                )
            }
            is CatalogState.Loaded -> syllabus(catalog.syllabus, onOpenContact, onOpenUrl)
        }
    }
}

@Composable
private fun Header(exam: ExamRecord, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GradeBadge(exam)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(exam.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                listOfNotNull("${exam.credits} CFU", exam.examType).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = exam.passedOn?.let { "Superato il $it" } ?: "Da sostenere",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (exam.isPassed) MaterialTheme.extendedColors.success.color else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun UnitSelector(units: List<UnitOption>, selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        itemsIndexed(units, key = { _, unit -> unit.activityId }) { index, unit ->
            val isSelected = index == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(index) },
                label = { Text(unit.label) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                shape = CircleShape
            )
        }
    }
}

private fun LazyListScope.syllabus(
    syllabus: SyllabusUiModel,
    onOpenContact: (String) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    if (syllabus.facts.isNotEmpty()) {
        item(key = "facts_${syllabus.activityId}") { FactsCard(syllabus.facts) }
    }
    if (!syllabus.workload.isEmpty) {
        item(key = "workload_header_${syllabus.activityId}") { GroupHeader("Carico didattico") }
        item(key = "workload_${syllabus.activityId}") { WorkloadCard(syllabus.workload) }
    }
    if (syllabus.teachers.isNotEmpty()) {
        item(key = "teachers_header_${syllabus.activityId}") { GroupHeader("Docenti") }
        itemsIndexed(syllabus.teachers, key = { index, _ -> "teacher_${syllabus.activityId}_$index" }) { index, teacher ->
            TeacherRow(teacher, index, syllabus.teachers.size, onOpenContact)
        }
    }
    syllabus.teamsLink?.let { link ->
        item(key = "teams_${syllabus.activityId}") {
            Column {
                GroupHeader("Classe virtuale")
                GroupedListItem(
                    title = "Apri il team su Microsoft Teams",
                    supporting = syllabus.teamsCode?.let { "Codice $it" },
                    icon = Icons.Rounded.Groups,
                    iconContainerColor = MaterialTheme.extendedColors.info.container,
                    iconColor = MaterialTheme.extendedColors.info.onContainer,
                    index = 0,
                    count = 1,
                    onClick = { onOpenUrl(link) },
                    trailing = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null) }
                )
            }
        }
    }
    items(syllabus.sections, key = { "section_${syllabus.activityId}_${it.title}" }) { section ->
        Column {
            GroupHeader(section.title)
            TextCard(section.text, collapsedLines = 6)
        }
    }
}

@Composable
private fun FactsCard(facts: List<SyllabusFact>) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            facts.forEachIndexed { index, fact ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        fact.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(0.4f)
                    )
                    Text(
                        fact.value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkloadCard(workload: WorkloadUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                workload.credits?.let { Metric(it, "crediti") }
                if (workload.totalHours > 0) Metric("${workload.totalHours} h", "di didattica")
            }
            if (workload.hours.size > 1 || (workload.hours.isNotEmpty() && workload.totalHours == 0)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    workload.hours.forEach { (type, hours) ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(type, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("$hours h", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
    workload.estimate?.let {
        Spacer(Modifier.height(8.dp))
        TextCard(it, collapsedLines = 4, title = "Stima del carico di studio")
    }
}

@Composable
private fun Metric(value: String, label: String) {
    Column {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TeacherRow(teacher: TeacherUiModel, index: Int, count: Int, onOpenContact: (String) -> Unit) {
    GroupedListItem(
        title = teacher.name,
        supporting = teacher.hours,
        icon = Icons.Rounded.Person,
        index = index,
        count = count,
        onClick = teacher.id?.let { id -> { onOpenContact(id) } },
        trailing = teacher.id?.let { { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null) } }
    )
}

private val HtmlTag = Regex("</?(p|br|strong|b|i|em|ul|ol|li|a|span|div|h[1-6])\\b[^>]*>", RegexOption.IGNORE_CASE)

/**
 * Text from the course page, plain or HTML depending on when it was written; long texts collapse
 * to [collapsedLines] with a toggle.
 */
@Composable
private fun TextCard(text: String, collapsedLines: Int, title: String? = null) {
    val linkColor = MaterialTheme.colorScheme.primary
    val content = remember(text, linkColor) {
        if (!HtmlTag.containsMatchIn(text)) AnnotatedString(text)
        else {
            val parsed = AnnotatedString.fromHtml(
                text,
                linkStyles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
            )
            // Paragraphs leave trailing new lines that would show as empty space
            parsed.subSequence(0, parsed.text.trimEnd().length)
        }
    }
    var expanded by rememberSaveable(text) { mutableStateOf(false) }
    var overflows by remember(text) { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .animateContentSize(spring(stiffness = 380f))
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = if (overflows || expanded) 4.dp else 14.dp)
        ) {
            title?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else collapsedLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow }
            )
            if (overflows || expanded) {
                TextButton(onClick = { expanded = !expanded }, modifier = Modifier.align(Alignment.End)) {
                    Text(if (expanded) "Mostra meno" else "Mostra tutto")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreview() {
    MaterialTheme {
        CourseDetailScreen(
            state = CourseDetailUiState.Loaded(
                exam = ExamRecord(1, "Fisica", 12, "Scritto e orale", null, false, false, null, 1),
                subtitle = "1° anno · A.A. 2025/26",
                sessions = emptyList(),
                units = listOf(UnitOption(1, "Modulo 1", "Meccanica"), UnitOption(2, "Modulo 2", "Elettricità e magnetismo")),
                catalog = CatalogState.Loaded(
                    SyllabusUiModel(
                        activityId = 1,
                        facts = listOf(SyllabusFact("Settore", "FIS/01 · Fisica sperimentale"), SyllabusFact("Periodo", "Primo Semestre")),
                        workload = WorkloadUiModel("6 CFU", 56, listOf("Lezione" to 38, "Esercitazione" to 18), "Lezioni: 38 ore\nStudio individuale: 94 ore"),
                        teachers = listOf(TeacherUiModel("9003", "Lombardi Chiara", "Lezione 38 h")),
                        sections = listOf(SyllabusSectionUiModel("Programma", "Calcolo vettoriale. Cinematica del punto. Dinamica.")),
                        teamsLink = null,
                        teamsCode = null
                    )
                )
            ),
            onBack = {},
            onRetry = {},
            onSelectUnit = {},
            onOpenContact = {},
            onOpenUrl = {}
        )
    }
}
