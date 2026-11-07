package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.auth.AuthResponseModel
import it.fra.myunical.domain.model.career.ExamSession
import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.syllabus.CourseLookup
import it.fra.myunical.domain.model.syllabus.cohortYear
import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.AuthRemoteDataSource
import it.fra.myunical.domain.sources.UserInfoDataSource
import kotlinx.coroutines.CancellationException
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
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userInfoDataSource: UserInfoDataSource
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
                cdsCode = cdsCodeOf(data.career),
                cohort = data.courses.cohortYear(),
                attendanceYear = course.attendanceAcademicYear
            )
        )
    }

    /**
     * Careers saved by older versions lack the degree course code, without which modules and groups
     * cannot be found: the profile is downloaded again with the saved credentials and stored.
     */
    private suspend fun cdsCodeOf(career: CareerTrackModel): String? {
        career.cdsCode?.let { return it }
        val credentials = authLocalDataSource.getCredentials() ?: return null
        val response = try {
            authRemoteDataSource.validateCredentials(credentials)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
        if (response !is AuthResponseModel.Success) return null
        userInfoDataSource.saveUserInfo(response.content)
        return response.content.careers.firstOrNull { it.matId == career.matId }?.cdsCode
    }
}
