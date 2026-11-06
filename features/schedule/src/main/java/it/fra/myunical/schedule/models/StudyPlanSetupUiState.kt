package it.fra.myunical.schedule.models

import androidx.compose.runtime.Immutable
import it.fra.myunical.domain.model.degreeexplorer.AcademicYearCourseGroup
import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath

@Immutable
data class StudyPlanSetupUiState(
    val selectedCategory: DegreeCategory = DegreeCategory.BACHELOR_AND_SINGLE_CYCLE,
    val allDegrees: List<DegreeProgram> = emptyList(),
    val selectedDegree: DegreeProgram? = null,
    val studyPaths: List<StudyPath> = emptyList(),
    val selectedPathId: String? = null,
    val enabledCourseIds: Set<String> = emptySet(),
    val isLoadingCatalog: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val activeStudyPath: StudyPath?
        get() = if (studyPaths.size > 1) studyPaths.find { it.id.toString() == selectedPathId } else if(studyPaths.size == 1) studyPaths.first() else null

    fun isYearFullyEnabled(yearGroup: AcademicYearCourseGroup): Boolean =
        yearGroup.courses.isNotEmpty() && yearGroup.courses.all { it.id in enabledCourseIds }
}

sealed interface StudyPlanSetupIntent {
    data class SelectCategory(val category: DegreeCategory) : StudyPlanSetupIntent
    data class SelectDegree(val degree: DegreeProgram) : StudyPlanSetupIntent
    data class SelectStudyPath(val pathId: String) : StudyPlanSetupIntent
    data class ToggleCourse(val courseId: String) : StudyPlanSetupIntent
    data class ToggleAcademicYear(val yearNumber: Int, val enable: Boolean) : StudyPlanSetupIntent
    data object SaveStudyPlan : StudyPlanSetupIntent
}