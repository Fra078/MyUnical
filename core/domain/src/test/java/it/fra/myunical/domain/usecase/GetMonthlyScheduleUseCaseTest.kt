package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.schedule.AcademicYear
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.model.schedule.TrackedCourse
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class GetMonthlyScheduleUseCaseTest {

    private data class Request(val cdsCode: String, val academicYear: Int, val cdsYear: Int, val year: Int, val month: Int)

    private class FakeDataSource(
        private val timetables: Map<Pair<String, Int>, List<ScheduleEvent>>
    ) : UnicalStorageDataSource {
        val requests = mutableListOf<Request>()

        override suspend fun getAllDegreeCourses(): List<DegreeProgram> = emptyList()
        override suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath> = emptyList()
        override suspend fun getTimetable(cdsCode: String, academicYear: Int, cdsYear: Int, year: Int, month: Int): List<ScheduleEvent> {
            requests += Request(cdsCode, academicYear, cdsYear, year, month)
            return timetables[cdsCode to cdsYear].orEmpty()
        }
    }

    private fun event(code: String, day: Int, hour: Int, cdsYear: Int = 1) = ScheduleEvent(
        providerId = "",
        courseCode = code,
        title = code,
        date = LocalDate.of(2026, 11, day),
        start = LocalTime.of(hour, 0),
        end = LocalTime.of(hour + 2, 0),
        rooms = emptyList(),
        teachers = emptyList(),
        cdsYear = cdsYear
    )

    private fun provider(id: String, cdsCode: String, vararg courses: TrackedCourse) = ScheduleProvider(
        id = id,
        displayName = id,
        cdsCode = cdsCode,
        regulationId = 1,
        degreeName = id,
        studyPathId = 1,
        studyPathName = "path",
        courses = courses.toList()
    )

    @Test
    fun `academic year starts in september`() {
        assertEquals(2026, AcademicYear.of(2026, 9))
        assertEquals(2026, AcademicYear.of(2026, 11))
        assertEquals(2026, AcademicYear.of(2027, 3))
        assertEquals(2025, AcademicYear.of(2026, 8))
    }

    @Test
    fun `keeps only tracked courses and module codes, merged across years and sorted`() = runBlocking {
        val dataSource = FakeDataSource(
            mapOf(
                ("0860" to 1) to listOf(event("MATH", 5, 11), event("ZOO", 2, 9), event("MOD_A", 2, 14)),
                ("0860" to 2) to listOf(event("GEN", 3, 9, cdsYear = 2), event("OTHER", 3, 11, cdsYear = 2))
            )
        )
        val useCase = GetMonthlyScheduleUseCase(dataSource)
        val provider = provider(
            "bio", "0860",
            TrackedCourse("MATH", "Matematica", 1),
            TrackedCourse("ZOO", "Zoologia", 1),
            TrackedCourse("ANAT", "Anatomia", 1, moduleCodes = listOf("MOD_A")),
            TrackedCourse("GEN", "Genetica", 2)
        )

        val result = useCase(listOf(provider), 2026, 11)

        assertEquals(listOf("ZOO", "MOD_A", "GEN", "MATH"), result.map { it.courseCode })
        assertEquals(setOf("bio"), result.map { it.providerId }.toSet())
        assertEquals(
            setOf(Request("0860", 2026, 1, 2026, 11), Request("0860", 2026, 2, 2026, 11)),
            dataSource.requests.toSet()
        )
    }

    @Test
    fun `cached months are not downloaded again unless forced`() = runBlocking {
        val dataSource = FakeDataSource(mapOf(("0860" to 1) to listOf(event("MATH", 5, 11))))
        val useCase = GetMonthlyScheduleUseCase(dataSource)
        val providers = listOf(provider("bio", "0860", TrackedCourse("MATH", "Matematica", 1)))

        useCase(providers, 2026, 11)
        useCase(providers, 2026, 11)
        assertEquals(1, dataSource.requests.size)

        useCase(providers, 2026, 11, forceRefresh = true)
        assertEquals(2, dataSource.requests.size)
    }

    @Test
    fun `providers sharing a degree keep their own courses`() = runBlocking {
        val dataSource = FakeDataSource(mapOf(("0860" to 1) to listOf(event("MATH", 5, 11), event("ZOO", 6, 9))))
        val useCase = GetMonthlyScheduleUseCase(dataSource)

        val result = useCase(
            listOf(
                provider("a", "0860", TrackedCourse("MATH", "Matematica", 1)),
                provider("b", "0860", TrackedCourse("ZOO", "Zoologia", 1))
            ),
            2026, 11
        )

        assertEquals(listOf("a" to "MATH", "b" to "ZOO"), result.map { it.providerId to it.courseCode })
    }
}
