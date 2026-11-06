package it.fra.myunical.career.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.ui.res.painterResource
import it.fra.myunical.career.R

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.career.components.shimmerEffect
import it.fra.myunical.career.models.CareerMetrics
import it.fra.myunical.career.models.ExamFilter
import it.fra.myunical.career.models.ExamRecord
import it.fra.myunical.career.models.ExamSortOption
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun CareerScreen(
    viewModel: CareerViewModel = koinViewModel()
){

    val list = viewModel.examList.collectAsStateWithLifecycle().value
    val metrics = viewModel.stats.collectAsStateWithLifecycle().value
    val sortOption = viewModel.sortOption.collectAsStateWithLifecycle().value
    val filter = viewModel.examFilter.collectAsStateWithLifecycle().value


    CareerScreen(
        metrics = metrics,
        exams = list,
        selectedSortOption = sortOption,
        selectedFilter = filter,
        selectFilter = viewModel::setFilter,
        selectSortOption = viewModel::setSortOption
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CareerScreen(
    metrics: CareerMetrics?,
    exams: List<ExamRecord>?,
    selectedSortOption: ExamSortOption,
    selectedFilter: ExamFilter,
    selectFilter: (ExamFilter) -> Unit,
    selectSortOption: (ExamSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var showSortSheet by remember { mutableStateOf(false) }

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = selectedSortOption,
            onSortSelected = {
                selectSortOption(it)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = "Carriera",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(
                            painter = painterResource(R.drawable.sort),
                            contentDescription = "Sort exams"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CareerAnalyticsCard(metrics = metrics)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChipRow(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectFilter(it) }
                    )

                    AssistChip(
                        onClick = { showSortSheet = true },
                        label = {
                            Text(
                                text = selectedSortOption.title.split(" ").first(),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.sort),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = CircleShape,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                }
            }

            if (exams != null)
                items(
                    items = exams,
                    key = { it.id }
                ) { exam ->
                    ExamCard(exam = exam)
                }
            else
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        LoadingIndicator(Modifier.size(64.dp))
                    }
                }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSort: ExamSortOption,
    onSortSelected: (ExamSortOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ordina esami",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExamSortOption.entries.forEach { option ->
                    val isSelected = option == currentSort

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = { onSortSelected(option) },
                                role = Role.RadioButton
                            ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = option.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CareerAnalyticsCard(
    metrics: CareerMetrics?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Crossfade(
            targetState = metrics,
            label = "metrics_crossfade"
        ) { currentMetrics ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentMetrics == null) {
                    CareerAnalyticsSkeleton()
                } else {
                    val progress = (currentMetrics.earnedCredits.toFloat() / currentMetrics.totalCredits).coerceIn(0f, 1f)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progresso Carriera",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${currentMetrics.earnedCredits} / ${currentMetrics.totalCredits} CFU",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricItem(
                            label = "Media Pond.",
                            value = String.format("%.2f", currentMetrics.weightedAverage),
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "Media Aritm.",
                            value = String.format("%.2f", currentMetrics.arithmeticAverage),
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "Base Laurea",
                            value = String.format("%.1f", currentMetrics.graduationBaseGrade),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerAnalyticsSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 24.dp)
                    .shimmerEffect(CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 20.dp)
                    .shimmerEffect(CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .shimmerEffect(CircleShape)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp) // Adjusted based on typical MetricItem height
                    .shimmerEffect(RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChipRow(
    selectedFilter: ExamFilter,
    onFilterSelected: (ExamFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExamFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun ExamCard(
    exam: ExamRecord,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GradeBadge(grade = exam.grade, isPassed = exam.isPassed)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${exam.scientificSector} • ${exam.credits} CFU",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val statusText = if (exam.isPassed) {
                    "Superato il ${exam.date.orEmpty()} (${exam.academicYear})"
                } else {
                    "Anno accademico: ${exam.academicYear}"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun GradeBadge(
    grade: String?,
    isPassed: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isPassed) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val contentColor = if (isPassed) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (grade != null) {
            Text(
                text = grade,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.hourglass_empty),
                contentDescription = "Pending exam",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun CareerScreenPreview() {
    MaterialTheme {
        val sampleMetrics = CareerMetrics(
            earnedCredits = 114,
            totalCredits = 180,
            weightedAverage = 28.35,
            arithmeticAverage = 27.80,
            graduationBaseGrade = 103.95
        )

        val sampleExams = listOf(
            ExamRecord(
                id = "1",
                title = "Sistemi Operativi e Reti",
                scientificSector = "ING-INF/05",
                credits = 9,
                grade = "30L",
                isPassed = true,
                date = "15/02/2026",
                academicYear = "2025/2026"
            ),
            ExamRecord(
                id = "2",
                title = "Ingegneria del Software",
                scientificSector = "ING-INF/05",
                credits = 9,
                grade = "28",
                isPassed = true,
                date = "22/01/2026",
                academicYear = "2025/2026"
            ),
            ExamRecord(
                id = "3",
                title = "Algoritmi e Strutture Dati",
                scientificSector = "ING-INF/05",
                credits = 9,
                grade = "25",
                isPassed = true,
                date = "18/06/2025",
                academicYear = "2024/2025"
            ),
            ExamRecord(
                id = "4",
                title = "Chimica e Scienza dei Materiali",
                scientificSector = "CHIM/07",
                credits = 6,
                grade = null,
                isPassed = false,
                date = null,
                academicYear = "2025/2026"
            )
        )

        CareerScreen(
            metrics = sampleMetrics,
            exams = sampleExams,
            selectedSortOption = ExamSortOption.ACADEMIC_YEAR_DESC,
            selectedFilter = ExamFilter.ALL,
            selectFilter = {},
            selectSortOption = {  }
        )
    }
}