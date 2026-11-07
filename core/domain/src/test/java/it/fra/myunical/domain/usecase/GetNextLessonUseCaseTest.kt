package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.schedule.NextLesson
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.model.schedule.TrackedCourse
import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.OfferedActivity
import it.fra.myunical.domain.repository.ScheduleProviderRepository
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class GetNextLessonUseCaseTest {

    private val provider = ScheduleProvider(
        id = "p", displayName = "Informatica", cdsCode = "0842", regulationId = 1, degreeName = "INFORMATICA",
        studyPathId = 1, studyPathName = "path",
        courses = listOf(TrackedCourse("A", "A", 1), TrackedCourse("B", "B", 1))
    )

    private fun event(code: String, date: LocalDate, start: Int, end: Int) = ScheduleEvent(
        providerId = "", courseCode = code, title = code, date = date,
        start = LocalTime.of(start, 0), end = LocalTime.of(end, 0),
        rooms = emptyList(), teachers = emptyList(), cdsYear = 1
    )

    private fun useCase(byMonth: Map<Int, List<ScheduleEvent>>, fail: Boolean = false): GetNextLessonUseCase {
        val dataSource = object : UnicalStorageDataSource {
            override suspend fun getAllDegreeCourses(): List<DegreeProgram> = emptyList()
            override suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath> = emptyList()
            override suspend fun findDegreeProgram(cdsCode: String, academicYear: Int): DegreeProgram? = null
            override suspend fun searchActivities(query: String, academicYear: Int): List<OfferedActivity> = emptyList()
            override suspend fun getSyllabus(activityId: Int): CourseSyllabus = error("unused")
            override suspend fun getTimetable(cdsCode: String, academicYear: Int, cdsYear: Int, year: Int, month: Int): List<ScheduleEvent> {
                if (fail) error("offline")
                return byMonth[month].orEmpty()
            }
        }
        val repository = object : ScheduleProviderRepository {
            override fun getProviders(): Flow<List<ScheduleProvider>> = flowOf(listOf(provider))
            override suspend fun getProvider(id: String) = provider
            override suspend fun upsertProvider(item: ScheduleProvider) = Unit
            override suspend fun removeProvider(id: String) = Unit
        }
        return GetNextLessonUseCase(repository, GetMonthlyScheduleUseCase(dataSource))
    }

    @Test
    fun `returns the ongoing lesson and the following one of the same day`() = runBlocking {
        val day = LocalDate.of(2026, 11, 6)
        val result = useCase(mapOf(11 to listOf(event("A", day, 9, 11), event("B", day, 11, 13))))
            .find(listOf(provider), LocalDateTime.of(day, LocalTime.of(10, 0)))

        result as NextLesson.Upcoming
        assertEquals("A", result.lesson.courseCode)
        assertEquals("B", result.following?.courseCode)
        assertEquals("Informatica", result.providerName)
    }

    @Test
    fun `looks into the next month when the current one is over`() = runBlocking {
        val result = useCase(mapOf(11 to emptyList(), 12 to listOf(event("A", LocalDate.of(2026, 12, 1), 9, 11))))
            .find(listOf(provider), LocalDateTime.of(2026, 11, 30, 18, 0))

        result as NextLesson.Upcoming
        assertEquals(LocalDate.of(2026, 12, 1), result.lesson.date)
        assertNull(result.following)
    }

    @Test
    fun `reports missing configuration, empty window and errors`() = runBlocking {
        val now = LocalDateTime.of(2026, 11, 6, 10, 0)
        assertEquals(NextLesson.NotConfigured, useCase(emptyMap()).find(emptyList(), now))
        assertEquals(NextLesson.NoneScheduled, useCase(emptyMap()).find(listOf(provider), now))
        assertEquals(NextLesson.Unavailable, useCase(emptyMap(), fail = true).find(listOf(provider), now))
    }
}
