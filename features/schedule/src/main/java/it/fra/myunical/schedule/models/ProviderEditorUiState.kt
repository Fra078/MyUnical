package it.fra.myunical.schedule.models

import androidx.compose.runtime.Immutable
import it.fra.myunical.domain.model.degreeexplorer.AcademicYearCourseGroup
import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath

sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>
    data object Error : Loadable<Nothing>
    data class Ready<T>(val value: T) : Loadable<T>
}

enum class EditorStep { DEGREE, PATH, COURSES }

@Immutable
data class DegreeGroup(
    val area: String,
    val degrees: List<DegreeProgram>
)

@Immutable
data class ProviderEditorUiState(
    val isEditing: Boolean = false,
    val step: EditorStep = EditorStep.DEGREE,
    val category: DegreeCategory = DegreeCategory.BACHELOR_AND_SINGLE_CYCLE,
    val degreeQuery: String = "",
    val catalog: Loadable<List<DegreeProgram>> = Loadable.Loading,
    val degreeGroups: List<DegreeGroup> = emptyList(),
    val selectedDegree: DegreeProgram? = null,
    val studyPaths: Loadable<List<StudyPath>> = Loadable.Loading,
    val selectedPath: StudyPath? = null,
    val selectedCourseCodes: Set<String> = emptySet(),
    val courseQuery: String = "",
    val displayName: String = "",
    val isSaving: Boolean = false
) {
    /** The path step is skipped when the degree has a single study path. */
    val hasPathStep: Boolean
        get() = (studyPaths as? Loadable.Ready)?.value?.size != 1

    val totalSteps: Int get() = if (hasPathStep) 3 else 2

    val stepNumber: Int
        get() = when (step) {
            EditorStep.DEGREE -> 1
            EditorStep.PATH -> 2
            EditorStep.COURSES -> if (hasPathStep) 3 else 2
        }

    val selectedYears: List<Int>
        get() = selectedPath?.academicYears.orEmpty()
            .filter { year -> year.courses.any { it.code in selectedCourseCodes } }
            .map(AcademicYearCourseGroup::yearNumber)

    val canSave: Boolean
        get() = selectedPath != null && selectedCourseCodes.isNotEmpty() && displayName.isNotBlank() && !isSaving

    fun selectedCount(yearGroup: AcademicYearCourseGroup): Int =
        yearGroup.courses.count { it.code in selectedCourseCodes }
}

sealed interface ProviderEditorIntent {
    data class SelectCategory(val category: DegreeCategory) : ProviderEditorIntent
    data class UpdateDegreeQuery(val query: String) : ProviderEditorIntent
    data class SelectDegree(val degree: DegreeProgram) : ProviderEditorIntent
    data class SelectStudyPath(val pathId: Int) : ProviderEditorIntent
    data class ToggleCourse(val courseCode: String) : ProviderEditorIntent
    data class SetYearSelected(val yearNumber: Int, val selected: Boolean) : ProviderEditorIntent
    data class UpdateCourseQuery(val query: String) : ProviderEditorIntent
    data class UpdateDisplayName(val name: String) : ProviderEditorIntent
    data object Retry : ProviderEditorIntent
    data object Back : ProviderEditorIntent
    data object Save : ProviderEditorIntent
}
