package it.fra.myunical.domain.model.syllabus

/**
 * Part of a libretto course with its own public page on corsi.unical.it: the course itself,
 * one of its modules or one of its groups ("Gruppo A"), which have separate teachers.
 */
data class CourseUnit(
    val activityId: Int,
    val code: String,
    val name: String,
    /** "Gruppo A"; null when the course is not split. */
    val partition: String? = null,
    val credits: Double? = null,
    val semester: String? = null
)

/** Public page ("scheda insegnamento") of a teaching activity. */
data class CourseSyllabus(
    val activityId: Int,
    val code: String,
    val name: String,
    val partition: String?,
    val degreeCourse: String?,
    val credits: Double?,
    val ssdCode: String?,
    val ssd: String?,
    val courseYear: Int?,
    val semester: String?,
    val languages: List<String>,
    /** "Base", "Caratterizzante"... */
    val teachingType: String?,
    val isCompulsory: Boolean?,
    val teachers: List<SyllabusTeacher>,
    val sections: List<SyllabusSection>,
    val teamsLink: String?,
    val teamsCode: String?,
    /** Set when the page is borrowed ("mutuata") from another activity, which holds the contents. */
    val borrowedFromId: Int? = null
) {
    val totalHours: Int get() = teachers.sumOf { teacher -> teacher.hours.sumOf(TeachingHours::hours) }

    /** Hours per activity type over all teachers, in order of first appearance. */
    val hoursByType: List<TeachingHours>
        get() = teachers.flatMap(SyllabusTeacher::hours)
            .groupBy(TeachingHours::type)
            .map { (type, hours) -> TeachingHours(type, hours.sumOf(TeachingHours::hours)) }

    val hasContents: Boolean get() = sections.isNotEmpty() || teachers.isNotEmpty()

    fun section(kind: SyllabusSectionKind): String? = sections.firstOrNull { it.kind == kind }?.text
}

data class SyllabusTeacher(
    /** Address book id; null when the teacher is not assigned yet. */
    val id: String?,
    val name: String,
    val hours: List<TeachingHours>
)

/** [type] is the Esse3 activity type code: LEZ, ESE, LAB... */
data class TeachingHours(val type: String, val hours: Int)

data class SyllabusSection(val kind: SyllabusSectionKind, val text: String)

/** Text sections of the page, in the order they are shown; [code] is the API content code. */
enum class SyllabusSectionKind(val code: String) {
    OBJECTIVES("OBIETT_FORM"),
    SUMMARY("CONTENUTI"),
    PROGRAM("PROGR_EST"),
    PREREQUISITES("PREREQ"),
    PROPAEDEUTICS("PROPEDE"),
    TEACHING_METHODS("METODI_DID"),
    ASSESSMENT("MOD_VER_APPR"),
    TEXTBOOKS("TESTI_RIF"),
    WORKLOAD("STIMA_CAR_LAV");

    companion object {
        fun fromCode(code: String): SyllabusSectionKind? = entries.firstOrNull { it.code == code }
    }
}
