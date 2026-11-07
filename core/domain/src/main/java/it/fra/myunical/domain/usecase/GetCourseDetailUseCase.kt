package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.career.ExamSession
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.syllabus.CourseLookup
import it.fra.myunical.domain.model.syllabus.cohortYear
import kotlinx.coroutines.flow.first

/** A libretto course with its upcoming sessions and what is needed to find its public pages. */
data class CourseDetail(
    val course: Course,
    val upcomingSessions: List<ExamSession>,
    val lookup: CourseLookup
)

/** Libretto row [courseRowId] of the current career, from the cached career data. */
class GetCourseDetailUseCase(
    private val getCareerData: GetCareerDataUseCase,
    private val resolveCdsCode: ResolveCdsCodeUseCase
) {
    suspend operator fun invoke(courseRowId: Long): CourseDetail {
        val data = getCareerData().first().getOrThrow()
        val course = data.courses.firstOrNull { it.id == courseRowId }
            ?: throw NoSuchElementException("Course $courseRowId not in the libretto")
        return CourseDetail(
            course = course,
            upcomingSessions = data.upcomingSessions.orEmpty().filter { it.courseRowId == courseRowId },
            lookup = CourseLookup(
                code = course.code,
                name = course.name,
                cdsCode = resolveCdsCode(data.career),
                cohort = data.courses.cohortYear(),
                attendanceYear = course.attendanceAcademicYear
            )
        )
    }
}
