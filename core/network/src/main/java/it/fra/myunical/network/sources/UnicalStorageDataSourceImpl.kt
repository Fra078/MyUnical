package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import it.fra.myunical.domain.model.degreeexplorer.AcademicYearCourseGroup
import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.degreeexplorer.SubjectCourse
import it.fra.myunical.domain.model.schedule.AcademicYear
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import it.fra.myunical.network.mapper.toDomainModel
import it.fra.myunical.network.models.schedule.StudyCoursePaginationResponse
import it.fra.myunical.network.models.schedule.TimetableEntryDto
import it.fra.myunical.network.models.studyplans.StudyPlanResponse

class UnicalStorageDataSourceImpl(
    private val httpClient: HttpClient
) : UnicalStorageDataSource {

    override suspend fun getAllDegreeCourses() : List<DegreeProgram> {
        val result = httpClient.get("ricerca/cds/") {
            parameter("page_size", 300)
            parameter("check_exclusion", 1)
            parameter("lang", "it")
            parameter("academicyear", AcademicYear.current())
            parameter("coursetype", "L,LM,LM5,LM6")
        }
        val body = result.body<StudyCoursePaginationResponse>()
        return body.results.map {
            DegreeProgram(
                cdsCode = it.courseCode,
                regulationId = it.regulationId.toInt(),
                name = it.courseName,
                category = if (it.courseType == "L" || it.courseType == "LM5") DegreeCategory.BACHELOR_AND_SINGLE_CYCLE else DegreeCategory.MASTER,
                courseType = it.courseType,
                area = it.academicArea,
                departmentName = it.departmentName,
                durationYears = it.durationYears
            )
        }
    }

    override suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath> {
        val result = httpClient.get("ricerca/cds/$regDidId/studyplans-activities/")
        val body = result.body<StudyPlanResponse>()
        val plan = body.results.firstOrNull() ?: return emptyList()
        return plan.planTabs.map {
            StudyPath(
                id = it.studyPlanId,
                name = it.studyPlanName,
                academicYears = it.studyActivities.map { (id, activities) ->
                    AcademicYearCourseGroup(
                        yearNumber = id.toInt(),
                        courses = activities.map { activity ->
                            SubjectCourse(
                                id = activity.studyActivityId?.toString() ?: activity.studyActivityCod,
                                code = activity.studyActivityCod,
                                name = activity.studyActivityName,
                                credits = activity.studyActivityEcts?.toInt() ?: -1,
                                moduleCodes = activity.afSubModules.map { module -> module.studyActivityCod }
                            )
                        }
                    )
                }.sortedBy(AcademicYearCourseGroup::yearNumber)
            )
        }
    }

    override suspend fun getTimetable(
        cdsCode: String,
        academicYear: Int,
        cdsYear: Int,
        year: Int,
        month: Int
    ): List<ScheduleEvent> {
        val result = httpClient.get("ricerca/cds-websites/$cdsCode/timetable/") {
            accept(ContentType.Application.Json)
            parameter("academic_year", academicYear)
            parameter("year", cdsYear)
            parameter("date_year", year)
            parameter("date_month", month)
            parameter("lang", "it")
        }
        return result.body<List<TimetableEntryDto>>().mapNotNull { it.toDomainModel(cdsYear) }
    }

}
