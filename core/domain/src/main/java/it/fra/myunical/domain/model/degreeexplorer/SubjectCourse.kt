package it.fra.myunical.domain.model.degreeexplorer
data class SubjectCourse(
    val id: String,
    val code: String,
    val name: String,
    val credits: Int,
    /** Page id on the course catalogue; null for courses split into modules or groups. */
    val activityId: Int? = null,
    val modules: List<SubjectModule> = emptyList()
) {
    /** Codes of the modules; groups share the course code and are not counted. */
    val moduleCodes: List<String> get() = modules.map(SubjectModule::code).distinct() - code
}

/** A module or a group ("Gruppo A") of a course; groups share the course code. */
data class SubjectModule(
    val activityId: Int?,
    val code: String,
    val name: String,
    val partition: String? = null,
    val credits: Double? = null,
    val semester: String? = null
)
