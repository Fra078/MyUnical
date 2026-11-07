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
import it.fra.myunical.domain.model.degreeexplorer.SubjectModule
import it.fra.myunical.domain.model.schedule.AcademicYear
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.OfferedActivity
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import it.fra.myunical.network.mapper.toDomainModel
import it.fra.myunical.network.models.schedule.StudyCourseInfo
import it.fra.myunical.network.models.schedule.StudyCoursePaginationResponse
import it.fra.myunical.network.models.schedule.TimetableEntryDto
import it.fra.myunical.network.models.studyplans.StudyPlanResponse
import it.fra.myunical.network.models.syllabus.StudyActivityDetailResponse
import it.fra.myunical.network.models.syllabus.StudyActivityListResponse

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
        return result.body<StudyCoursePaginationResponse>().results.map { it.toDegreeProgram() }
    }

    override suspend fun findDegreeProgram(cdsCode: String, academicYear: Int): DegreeProgram? =
        httpClient.get("ricerca/cds/") {
            parameter("cdscod", cdsCode)
            parameter("academicyear", academicYear)
            parameter("lang", "it")
        }.body<StudyCoursePaginationResponse>().results.firstOrNull { it.courseCode == cdsCode }?.toDegreeProgram()

    override suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath> {
        val result = httpClient.get("ricerca/cds/$regDidId/studyplans-activities/")
        val body = result.body<StudyPlanResponse>()
        // A regulation can list several plans: the tabs are merged, skipping repeated curricula
        return body.results.flatMap { it.planTabs }.distinctBy { it.studyPlanId }.map {
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
                                activityId = activity.studyActivityId,
                                modules = activity.afSubModules.map { module ->
                                    SubjectModule(
                                        activityId = module.studyActivityId,
                                        code = module.studyActivityCod,
                                        name = module.studyActivityName,
                                        partition = module.studyActivityPartitionDes,
                                        credits = module.studyActivityCreditValue,
                                        semester = module.studyActivitySemester
                                    )
                                }
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

    override suspend fun searchActivities(query: String, academicYear: Int): List<OfferedActivity> =
        httpClient.get("ricerca/activities/") {
            // `search` is ignored by this endpoint: `name` filters by activity name
            parameter("name", query)
            parameter("academic_year", academicYear)
            parameter("page_size", 100)
            parameter("lang", "it")
        }.body<StudyActivityListResponse>().results.map { it.toDomainModel() }

    override suspend fun getSyllabus(activityId: Int): CourseSyllabus =
        httpClient.get("ricerca/activities/$activityId/") {
            parameter("lang", "it")
        }.body<StudyActivityDetailResponse>().results.toDomainModel()

    private fun StudyCourseInfo.toDegreeProgram() = DegreeProgram(
        cdsCode = courseCode,
        regulationId = regulationId.toInt(),
        name = courseName,
        category = if (courseType == "L" || courseType == "LM5") DegreeCategory.BACHELOR_AND_SINGLE_CYCLE else DegreeCategory.MASTER,
        courseType = courseType,
        area = academicArea,
        departmentName = departmentName,
        durationYears = durationYears
    )
}
