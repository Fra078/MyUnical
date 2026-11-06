package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import it.fra.myunical.domain.model.degreeexplorer.AcademicYearCourseGroup
import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.degreeexplorer.SubjectCourse
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import it.fra.myunical.network.models.schedule.StudyCoursePaginationResponse
import it.fra.myunical.network.models.studyplans.StudyPlanResponse

class UnicalStorageDataSourceImpl(
    private val httpClient: HttpClient
) : UnicalStorageDataSource {

    override suspend fun getAllDegreeCourses() : List<DegreeProgram> {
        val result = httpClient.get("ricerca/cds/?page_size=300&check_exclusion=1&lang=it&academicyear=2026&coursetype=L,LM,LM5,LM6")
        val body = result.body<StudyCoursePaginationResponse>()
        return body.results.map {
            DegreeProgram(
                id = it.courseCode,
                regulationId = it.regulationId.toInt(),
                name = it.courseName,
                category = if (it.courseType == "L" || it.courseType == "LM5") DegreeCategory.BACHELOR_AND_SINGLE_CYCLE else DegreeCategory.MASTER
            )
        }
    }

    override suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath> {
        val result = httpClient.get("ricerca/cds/$regDidId/studyplans-activities")
        val body = result.body<StudyPlanResponse>()
        return body.results.first().planTabs.map {
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
                                credits = activity.studyActivityEcts?.toInt() ?: -1
                            )
                        }
                    )
                })
        }
    }

}