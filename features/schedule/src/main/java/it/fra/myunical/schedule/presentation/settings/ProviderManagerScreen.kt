package it.fra.myunical.schedule.presentation.settings

import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.runtime.Composable
import it.fra.myunical.domain.model.degreeexplorer.AcademicYearCourseGroup
import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.degreeexplorer.SubjectCourse
import it.fra.myunical.schedule.models.StudyPlanSetupIntent
import it.fra.myunical.schedule.models.StudyPlanSetupUiState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.schedule.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScheduleProviderConfigurationScreen(
    viewModel: ScheduleProviderConfigurationViewModel = koinViewModel()
){
    val state = viewModel.state.collectAsStateWithLifecycle().value

    StudyPlanSetupScreen(
        uiState = state,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanSetupScreen(
    uiState: StudyPlanSetupUiState,
    onIntent: (StudyPlanSetupIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configura Piano di Studi",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.selectedDegree != null && !uiState.isLoadingCatalog,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { onIntent(StudyPlanSetupIntent.SaveStudyPlan) },
                    icon = { Icon(painterResource(R.drawable.check), contentDescription = null) },
                    text = { Text("Salva (${uiState.enabledCourseIds.size})") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp
            )
        ) {
            item(key = "header_controls") {
                DegreeSelectionHeader(
                    selectedCategory = uiState.selectedCategory,
                    availableDegrees = uiState.allDegrees,
                    selectedDegree = uiState.selectedDegree,
                    studyPaths = uiState.studyPaths,
                    selectedPathId = uiState.selectedPathId,
                    onCategorySelected = { onIntent(StudyPlanSetupIntent.SelectCategory(it)) },
                    onDegreeSelected = { onIntent(StudyPlanSetupIntent.SelectDegree(it)) },
                    onPathSelected = { onIntent(StudyPlanSetupIntent.SelectStudyPath(it)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isLoadingCatalog) {
                item(key = "loading_indicator") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                val academicYears = uiState.activeStudyPath?.academicYears.orEmpty()

                academicYears.forEach { yearGroup ->
                    item(
                        key = "year_header_${yearGroup.yearNumber}",
                        contentType = "year_header"
                    ) {
                        AcademicYearHeader(
                            yearGroup = yearGroup,
                            isYearEnabled = uiState.isYearFullyEnabled(yearGroup),
                            enabledCount = yearGroup.courses.count { it.id in uiState.enabledCourseIds },
                            onToggleYear = { enabled ->
                                onIntent(
                                    StudyPlanSetupIntent.ToggleAcademicYear(
                                        yearNumber = yearGroup.yearNumber,
                                        enable = enabled
                                    )
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    itemsIndexed(
                        items = yearGroup.courses,
                        key = { _, course -> course.id },
                        contentType = { _, _ -> "course_row" }
                    ) { index, course ->
                        val itemShape = rememberGroupedItemShape(
                            index = index,
                            totalCount = yearGroup.courses.size
                        )

                        CourseSwitchItem(
                            course = course,
                            isEnabled = course.id in uiState.enabledCourseIds,
                            shape = itemShape,
                            onToggle = { onIntent(StudyPlanSetupIntent.ToggleCourse(course.id)) },
                            modifier = Modifier.animateItem()
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    item(key = "year_spacer_${yearGroup.yearNumber}") {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DegreeSelectionHeader(
    selectedCategory: DegreeCategory,
    availableDegrees: List<DegreeProgram>,
    selectedDegree: DegreeProgram?,
    studyPaths: List<StudyPath>,
    selectedPathId: String?,
    onCategorySelected: (DegreeCategory) -> Unit,
    onDegreeSelected: (DegreeProgram) -> Unit,
    onPathSelected: (String) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            DegreeCategory.entries.forEachIndexed { index, category ->
                val label = when (category) {
                    DegreeCategory.BACHELOR_AND_SINGLE_CYCLE -> "Triennali / Ciclo Unico"
                    DegreeCategory.MASTER -> "Magistrali"
                }
                SegmentedButton(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = DegreeCategory.entries.size
                    ),
                    label = { Text(label, maxLines = 1) }
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedDegree?.name ?: "Seleziona corso di studio",
                onValueChange = {},
                readOnly = true,
                label = { Text("Facoltà / Corso di Laurea") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                availableDegrees.forEach { degree ->
                    DropdownMenuItem(
                        text = { Text(degree.name) },
                        onClick = {
                            onDegreeSelected(degree)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        if (studyPaths.size > 1) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Percorso Formativo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(studyPaths, key = { it.id }) { path ->
                        FilterChip(
                            selected = path.id.toString() == selectedPathId,
                            onClick = { onPathSelected(path.id.toString()) },
                            label = { Text(path.name) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AcademicYearHeader(
    yearGroup: AcademicYearCourseGroup,
    isYearEnabled: Boolean,
    enabledCount: Int,
    onToggleYear: (Boolean) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${yearGroup.yearNumber}° Anno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$enabledCount di ${yearGroup.courses.size} discipline attive",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Switch(
                checked = isYearEnabled,
                onCheckedChange = onToggleYear
            )
        }
    }
}

@Composable
private fun CourseSwitchItem(
    course: SubjectCourse,
    isEnabled: Boolean,
    shape: RoundedCornerShape,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = shape,
        color = if (isEnabled) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isEnabled) FontWeight.Medium else FontWeight.Normal,
                    color = if (isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = "${course.code} • ${course.credits} CFU",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun rememberGroupedItemShape(index: Int, totalCount: Int): RoundedCornerShape {
    return remember(index, totalCount) {
        val outerRadius = CornerSize(16.dp)
        val innerRadius = CornerSize(4.dp)
        when {
            totalCount == 1 -> RoundedCornerShape(outerRadius)
            index == 0 -> RoundedCornerShape(
                topStart = outerRadius,
                topEnd = outerRadius,
                bottomStart = innerRadius,
                bottomEnd = innerRadius
            )
            index == totalCount - 1 -> RoundedCornerShape(
                topStart = innerRadius,
                topEnd = innerRadius,
                bottomStart = outerRadius,
                bottomEnd = outerRadius
            )
            else -> RoundedCornerShape(innerRadius)
        }
    }
}

private class StudyPlanPreviewProvider : PreviewParameterProvider<StudyPlanSetupUiState> {
    override val values: Sequence<StudyPlanSetupUiState> = sequenceOf(
        StudyPlanSetupUiState(
            selectedCategory = DegreeCategory.MASTER,
            selectedDegree = DegreeProgram("1", 0,"Telecommunication Engineering", DegreeCategory.MASTER),
            studyPaths = listOf(
                StudyPath(
                    id = 1,
                    name = "Smart Sensing & Computing",
                    academicYears = listOf(
                        AcademicYearCourseGroup(
                            yearNumber = 1,
                            courses = listOf(
                                SubjectCourse("c1", "27001", "Internet of Things", 9),
                                SubjectCourse("c2", "27002", "Machine Learning", 6)
                            )
                        ),
                        AcademicYearCourseGroup(
                            yearNumber = 2,
                            courses = listOf(
                                SubjectCourse("c3", "27003", "Cloud & Edge Computing", 6)
                            )
                        )
                    )
                )
            ),
            selectedPathId = "path_smart",
            enabledCourseIds = setOf("c1", "c3")
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun StudyPlanSetupScreenPreview(
    @PreviewParameter(StudyPlanPreviewProvider::class) state: StudyPlanSetupUiState
) {
    MaterialTheme {
        StudyPlanSetupScreen(uiState = state, onIntent = {})
    }
}