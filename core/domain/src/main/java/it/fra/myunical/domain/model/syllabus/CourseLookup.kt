package it.fra.myunical.domain.model.syllabus

import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.degreeexplorer.SubjectCourse
import it.fra.myunical.domain.model.general.Course

/** What is needed to find the public pages of a libretto course. */
data class CourseLookup(
    val code: String,
    val name: String,
    /** Degree course of the career; null for careers saved before it was stored. */
    val cdsCode: String?,
    /** Enrolment cohort, whose study plan lists the activity ids. */
    val cohort: Int?,
    val attendanceYear: Int
)

/** A teaching activity offered in an academic year, as listed by the course catalogue. */
data class OfferedActivity(
    val activityId: Int,
    val code: String,
    val name: String,
    val cdsCode: String?,
    val partition: String?,
    val semester: String?
)

/**
 * Cohort of a career, from its libretto: each course is attended in cohort + year - 1,
 * so the most common value wins over repeated or anticipated courses.
 */
fun List<Course>.cohortYear(): Int? =
    filter { it.attendanceAcademicYear > 0 && it.year > 0 }
        .groupingBy { it.attendanceAcademicYear - it.year + 1 }
        .eachCount()
        .maxWithOrNull(compareBy<Map.Entry<Int, Int>> { it.value }.thenBy { it.key })
        ?.key

/**
 * Units of the course [code] in a study plan: the course itself when it has a page, otherwise
 * its modules and groups (the parent of a split course has none).
 */
fun List<StudyPath>.unitsFor(code: String): List<CourseUnit> {
    val course = asSequence()
        .flatMap { it.academicYears }
        .flatMap { it.courses }
        .filter { it.code == code }
        .sortedByDescending { it.activityId != null || it.modules.any { module -> module.activityId != null } }
        .firstOrNull() ?: return emptyList()
    return course.toUnits()
}

fun SubjectCourse.toUnits(): List<CourseUnit> {
    activityId?.let {
        return listOf(CourseUnit(it, code, name, credits = credits.takeIf { c -> c > 0 }?.toDouble()))
    }
    return modules.mapNotNull { module ->
        module.activityId?.let {
            CourseUnit(it, module.code, module.name, module.partition, module.credits, module.semester)
        }
    }
}

/** Matches catalogue results to [code], preferring the career's degree course; one unit per group. */
fun List<OfferedActivity>.unitsFor(code: String, cdsCode: String?): List<CourseUnit> {
    val matching = filter { it.code == code }
    val sameCds = matching.filter { cdsCode != null && it.cdsCode == cdsCode }
    val chosen = sameCds.ifEmpty {
        // Courses borrowed by several degree courses: keep the first one offering each group
        val firstCds = matching.firstOrNull()?.cdsCode
        matching.filter { it.cdsCode == firstCds }
    }
    return chosen
        .distinctBy { it.partition }
        .map { CourseUnit(it.activityId, it.code, it.name, it.partition, semester = it.semester) }
}
