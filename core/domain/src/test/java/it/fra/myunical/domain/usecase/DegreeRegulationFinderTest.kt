package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.Department
import it.fra.myunical.domain.model.degreeexplorer.Regulation
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.OfferedActivity
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DegreeRegulationFinderTest {

    private fun program(url: String?) = DegreeProgram(
        cdsCode = "0819", regulationId = 1, name = "X", category = DegreeCategory.BACHELOR_AND_SINGLE_CYCLE, regulationUrl = url
    )

    /** [published] maps an academic year to the regulation URL of its catalogue record. */
    private fun find(published: Map<Int, String>, cohort: Int, current: Int): Pair<Regulation?, Regulation?> = runBlocking {
        val dataSource = object : UnicalStorageDataSource {
            override suspend fun getAllDegreeCourses(): List<DegreeProgram> = emptyList()
            override suspend fun getDepartments(): List<Department> = emptyList()
            override suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath> = emptyList()
            override suspend fun findDegreeProgram(cdsCode: String, academicYear: Int): DegreeProgram? =
                if (academicYear > 2100) error("offline") else published[academicYear]?.let { program(it) }
            override suspend fun searchActivities(query: String, academicYear: Int): List<OfferedActivity> = emptyList()
            override suspend fun getSyllabus(activityId: Int): CourseSyllabus = error("unused")
            override suspend fun getTimetable(cdsCode: String, academicYear: Int, cdsYear: Int, year: Int, month: Int): List<ScheduleEvent> = emptyList()
        }
        val useCase = DegreeRegulationFinder(dataSource, currentYear = { current })
        useCase.find("0819", cohort).let { it.cohort to it.latest }
    }

    @Test
    fun `cohort and latest are different documents when the course changed regulation`() {
        val (cohort, latest) = find(mapOf(2023 to "a.pdf", 2024 to "b.pdf"), cohort = 2023, current = 2026)
        assertEquals(Regulation(2023, "a.pdf"), cohort)
        assertEquals(Regulation(2024, "b.pdf"), latest)
    }

    @Test
    fun `latest skips years that are not published yet`() {
        val (_, latest) = find(mapOf(2023 to "a.pdf"), cohort = 2023, current = 2026)
        assertEquals(Regulation(2023, "a.pdf"), latest)
    }

    @Test
    fun `cohort falls back to earlier years`() {
        val (cohort, latest) = find(mapOf(2022 to "old.pdf"), cohort = 2023, current = 2026)
        assertEquals(Regulation(2022, "old.pdf"), cohort)
        assertNull(latest)
    }

    @Test
    fun `nothing published gives no regulations`() {
        val (cohort, latest) = find(emptyMap(), cohort = 2023, current = 2026)
        assertNull(cohort)
        assertNull(latest)
    }

    @Test
    fun `network errors count as not published`() {
        val (cohort, latest) = find(mapOf(2023 to "a.pdf"), cohort = 2023, current = 2200)
        assertEquals(Regulation(2023, "a.pdf"), cohort)
        assertEquals(Regulation(2023, "a.pdf"), latest)
    }
}
