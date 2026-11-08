package it.fra.myunical.domain.model.schedule

data class ScheduleProvider(
    val id: String,
    val displayName: String,
    val cdsCode: String,
    val regulationId: Int,
    val degreeName: String,
    val studyPathId: Int,
    val studyPathName: String,
    val courses: List<TrackedCourse>,
    /** Career whose libretto keeps this schedule up to date; null for schedules built by hand. */
    val careerMatId: Int? = null
) {
    val cdsYears: List<Int>
        get() = courses.map(TrackedCourse::yearNumber).distinct().sorted()

    val trackedCodes: Set<String>
        get() = courses.flatMapTo(mutableSetOf()) { it.moduleCodes + it.code }
}

data class TrackedCourse(
    val code: String,
    val name: String,
    val yearNumber: Int,
    val moduleCodes: List<String> = emptyList()
)
