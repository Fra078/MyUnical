package it.fra.myunical.career.models

sealed interface CourseDetailUiState {
    data object Loading : CourseDetailUiState
    data object Error : CourseDetailUiState

    /** Libretto data is shown right away; the public page ([catalog]) loads afterwards. */
    data class Loaded(
        val exam: ExamRecord,
        /** "1° anno · A.A. 2024/25" */
        val subtitle: String,
        val sessions: List<SessionUiModel>,
        val units: List<UnitOption> = emptyList(),
        val selectedUnit: Int = 0,
        val catalog: CatalogState = CatalogState.Loading
    ) : CourseDetailUiState
}

/** A module or group of the course, picked with a chip ([label]) when there is more than one. */
data class UnitOption(
    val activityId: Int,
    /** "Modulo 1", "Gruppo A", "Modulo 1 · Gruppo A" */
    val label: String,
    /** Full name of a module, shown under the chips; null for groups. */
    val title: String? = null
)

sealed interface CatalogState {
    data object Loading : CatalogState
    /** The catalogue does not list the course (or it has no public page). */
    data object NotFound : CatalogState
    data object Error : CatalogState
    data class Loaded(val syllabus: SyllabusUiModel) : CatalogState
}

data class SyllabusUiModel(
    val activityId: Int,
    /** Settore, semestre, lingua... */
    val facts: List<SyllabusFact>,
    val workload: WorkloadUiModel,
    val teachers: List<TeacherUiModel>,
    val sections: List<SyllabusSectionUiModel>,
    val teamsLink: String?,
    val teamsCode: String?
)

data class SyllabusFact(val label: String, val value: String)

data class WorkloadUiModel(
    val credits: String?,
    val totalHours: Int,
    /** "Lezione" to 57 */
    val hours: List<Pair<String, Int>>,
    /** Study time estimated by the teacher, free text. */
    val estimate: String?
) {
    val isEmpty: Boolean get() = credits == null && hours.isEmpty() && estimate == null
}

data class TeacherUiModel(
    val id: String?,
    val name: String,
    /** "Lezione 57 h · Esercitazione 27 h" */
    val hours: String?
)

data class SyllabusSectionUiModel(val title: String, val text: String)
