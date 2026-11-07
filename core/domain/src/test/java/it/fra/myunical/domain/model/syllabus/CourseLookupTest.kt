package it.fra.myunical.domain.model.syllabus

import it.fra.myunical.domain.model.degreeexplorer.AcademicYearCourseGroup
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.degreeexplorer.SubjectCourse
import it.fra.myunical.domain.model.degreeexplorer.SubjectModule
import it.fra.myunical.domain.model.general.Course
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseLookupTest {

    private fun course(year: Int, attendance: Int) = Course(
        id = year * 100L + attendance, code = "C", name = "Corso", year = year, credits = 6,
        attendanceAcademicYear = attendance, isPassed = false, examType = "", courseType = "", outcome = null
    )

    @Test
    fun `cohort is the most common enrolment year`() {
        val courses = listOf(
            course(1, 2023), course(1, 2023), course(2, 2024), course(3, 2025),
            // Repeated in a later year
            course(1, 2024)
        )
        assertEquals(2023, courses.cohortYear())
    }

    @Test
    fun `no cohort without attendance years`() {
        assertNull(listOf(course(1, 0)).cohortYear())
    }

    private val plan = listOf(
        StudyPath(
            id = 1, name = "Processi",
            academicYears = listOf(
                AcademicYearCourseGroup(
                    1, listOf(
                        SubjectCourse("203549", "27000001", "ANALISI MATEMATICA I", 9, activityId = 203549),
                        SubjectCourse(
                            "27000005", "27000005", "FISICA", 12,
                            modules = listOf(
                                SubjectModule(203557, "27008451", "Modulo 1: MECCANICA", credits = 6.0),
                                SubjectModule(203558, "27008452", "Modulo 2: ELETTRICITA'", credits = 6.0)
                            )
                        ),
                        SubjectCourse(
                            "27000002", "27000002", "FONDAMENTI DI INFORMATICA", 9,
                            modules = listOf(
                                SubjectModule(203476, "27000002", "FONDAMENTI DI INFORMATICA", partition = "Gruppo A"),
                                SubjectModule(203477, "27000002", "FONDAMENTI DI INFORMATICA", partition = "Gruppo B")
                            )
                        ),
                        SubjectCourse(
                            "27000016", "27000016", "INGLESE", 3,
                            modules = listOf(SubjectModule(null, "27008861", "Modulo 1"))
                        )
                    )
                )
            )
        )
    )

    @Test
    fun `a course with a page is a single unit`() {
        assertEquals(listOf(203549), plan.unitsFor("27000001").map { it.activityId })
    }

    @Test
    fun `modules and groups become units`() {
        assertEquals(listOf(203557, 203558), plan.unitsFor("27000005").map { it.activityId })
        val groups = plan.unitsFor("27000002")
        assertEquals(listOf("Gruppo A", "Gruppo B"), groups.map { it.partition })
        assertEquals(emptyList<String>(), plan.first().academicYears.first().courses[2].moduleCodes)
    }

    @Test
    fun `units without a page are dropped`() {
        assertTrue(plan.unitsFor("27000016").isEmpty())
        assertTrue(plan.unitsFor("missing").isEmpty())
    }

    @Test
    fun `search results prefer the career degree course`() {
        val offered = listOf(
            OfferedActivity(1, "27000001", "ANALISI MATEMATICA I", "0876", null, null),
            OfferedActivity(2, "27000001", "ANALISI MATEMATICA I", "0829", null, null),
            OfferedActivity(3, "27000002", "FONDAMENTI DI INFORMATICA", "0829", null, null)
        )
        assertEquals(listOf(2), offered.unitsFor("27000001", "0829").map { it.activityId })
        assertEquals(listOf(1), offered.unitsFor("27000001", null).map { it.activityId })
        assertTrue(offered.unitsFor("27000099", "0829").isEmpty())
    }
}
