package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.degreeexplorer.AcademicYearCourseGroup
import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.Department
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.degreeexplorer.SubjectCourse
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.OfferedActivity
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CareerScheduleBuilderTest {

    private fun course(code: String, passed: Boolean) = Course(
        id = code.hashCode().toLong(), code = code, name = code, year = 1, credits = 6,
        attendanceAcademicYear = 2026, isPassed = passed, examType = "", courseType = "", outcome = null
    )

    private fun subject(code: String) = SubjectCourse(id = code, code = code, name = "Corso $code", credits = 6)

    private val pathA = StudyPath(1, "Percorso A", listOf(AcademicYearCourseGroup(1, listOf(subject("A1"), subject("A2")))))
    private val pathB = StudyPath(2, "Percorso B", listOf(AcademicYearCourseGroup(1, listOf(subject("A1"), subject("B1"), subject("B2")))))

    private fun builder(paths: List<StudyPath>, programYear: Int? = 2026) = CareerScheduleBuilder(
        object : UnicalStorageDataSource {
            override suspend fun getAllDegreeCourses(): List<DegreeProgram> = emptyList()
            override suspend fun getDepartments(): List<Department> = emptyList()
            override suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath> = paths
            override suspend fun findDegreeProgram(cdsCode: String, academicYear: Int): DegreeProgram? =
                if (academicYear == programYear) DegreeProgram("0819", 7, "INGEGNERIA INFORMATICA", DegreeCategory.BACHELOR_AND_SINGLE_CYCLE) else null
            override suspend fun searchActivities(query: String, academicYear: Int): List<OfferedActivity> = emptyList()
            override suspend fun getSyllabus(activityId: Int): CourseSyllabus = error("unused")
            override suspend fun getTimetable(cdsCode: String, academicYear: Int, cdsYear: Int, year: Int, month: Int): List<ScheduleEvent> = emptyList()
        },
        currentYear = { 2026 }
    )

    private fun build(builder: CareerScheduleBuilder, libretto: List<Course>) =
        runBlocking { builder.build("career-1", 1, "0819", 2026, libretto) }

    @Test
    fun `picks the curriculum that holds the most exams still to pass`() {
        val provider = build(builder(listOf(pathA, pathB)), listOf(course("B1", false), course("B2", false), course("A1", false)))!!
        assertEquals(2, provider.studyPathId)
        assertEquals(listOf("A1", "B1", "B2"), provider.courses.map { it.code })
        assertEquals(1, provider.careerMatId)
        assertEquals("Ingegneria Informatica", provider.displayName)
    }

    @Test
    fun `skips passed exams`() {
        val provider = build(builder(listOf(pathA)), listOf(course("A1", true), course("A2", false)))!!
        assertEquals(listOf("A2"), provider.courses.map { it.code })
    }

    @Test
    fun `no exam left or unknown course gives no schedule`() {
        assertNull(build(builder(listOf(pathA)), listOf(course("A1", true))))
        assertNull(build(builder(listOf(pathA), programYear = null), listOf(course("A1", false))))
    }
}
