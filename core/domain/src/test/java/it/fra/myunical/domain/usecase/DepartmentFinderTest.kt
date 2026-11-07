package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.Department
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.OfferedActivity
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DepartmentFinderTest {

    private val desf = Department("002025", "DESF", "Dipartimento di Economia", "desf.unical.it")
    private val dimes = Department("002017", "DIMES", "Dipartimento di Ingegneria", "dimes.unical.it")

    /** [codeByYear] maps an academic year to the department code of the course record. */
    private class Storage(
        private val codeByYear: Map<Int, String>,
        private val departments: List<Department>
    ) : UnicalStorageDataSource {
        var departmentCalls = 0
        override suspend fun getAllDegreeCourses(): List<DegreeProgram> = emptyList()
        override suspend fun getDepartments(): List<Department> = departments.also { departmentCalls++ }
        override suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath> = emptyList()
        override suspend fun findDegreeProgram(cdsCode: String, academicYear: Int): DegreeProgram? =
            codeByYear[academicYear]?.let {
                DegreeProgram(cdsCode, 1, "X", DegreeCategory.MASTER, departmentCode = it)
            }
        override suspend fun searchActivities(query: String, academicYear: Int): List<OfferedActivity> = emptyList()
        override suspend fun getSyllabus(activityId: Int): CourseSyllabus = error("unused")
        override suspend fun getTimetable(cdsCode: String, academicYear: Int, cdsYear: Int, year: Int, month: Int): List<ScheduleEvent> = emptyList()
    }

    private fun finder(storage: Storage) = DepartmentFinder(storage, currentYear = { 2026 })

    @Test
    fun `uses the department of the cohort record`() = runBlocking {
        val storage = Storage(mapOf(2023 to "002025", 2026 to "002017"), listOf(desf, dimes))
        assertEquals(desf, finder(storage).find("0819", cohort = 2023))
    }

    @Test
    fun `falls back to the current year when the cohort has no record`() = runBlocking {
        val storage = Storage(mapOf(2026 to "002017"), listOf(desf, dimes))
        assertEquals(dimes, finder(storage).find("0819", cohort = 2023))
        assertEquals(dimes, finder(storage).find("0819", cohort = null))
    }

    @Test
    fun `unknown course or department gives null`() = runBlocking {
        assertNull(finder(Storage(emptyMap(), listOf(desf))).find("0819", cohort = 2023))
        assertNull(finder(Storage(mapOf(2023 to "999999"), listOf(desf))).find("0819", cohort = 2023))
    }

    @Test
    fun `the list of departments is loaded once`() = runBlocking {
        val storage = Storage(mapOf(2023 to "002025"), listOf(desf))
        val finder = finder(storage)
        finder.find("0819", 2023)
        finder.find("0819", 2023)
        assertEquals(1, storage.departmentCalls)
    }
}
