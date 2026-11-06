package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.model.career.BookingState
import it.fra.myunical.domain.model.career.CareerOverview
import it.fra.myunical.domain.model.career.ExamBooking
import it.fra.myunical.domain.model.career.ExamSession
import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.general.ExamOutcome
import it.fra.myunical.domain.model.general.ExamResult
import it.fra.myunical.domain.model.general.UserInfoModel
import it.fra.myunical.domain.repository.UserPreferenceRepository
import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.CinecaRemoteDataSource
import it.fra.myunical.domain.sources.UserInfoDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class GetCareerDataUseCaseTest {

    private val today = LocalDate.of(2026, 11, 6)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    private fun course(id: Long, credits: Int, grade: Int? = null) = Course(
        id = id, code = "C$id", name = "Course $id", year = 1, credits = credits, attendanceAcademicYear = 2025,
        isPassed = grade != null, examType = "Scritto", courseType = "OB",
        outcome = grade?.let { ExamOutcome(date = 1L, academicYear = 2025, result = ExamResult.Graded(it, false)) }
    )

    private fun session(id: Long, row: Long, date: LocalDate, open: Boolean) = ExamSession(
        id = id, courseRowId = row, courseName = "Course $row", description = "", date = date, time = null,
        enrollStart = date.minusDays(20), enrollEnd = date.minusDays(2), isBookingOpen = open, president = null
    )

    private class FakeCinecaSource(
        val courses: List<Course>,
        val overview: CareerOverview?,
        val sessions: List<ExamSession>,
        val bookings: List<ExamBooking>
    ) : CinecaRemoteDataSource {
        override fun getUserPhotoEndpoint(persId: Int) = ""
        override suspend fun fetchStudyPlan(matId: Int) = Result.success(courses)
        override suspend fun fetchCareerOverview(matId: Int) = overview ?: error("stats unavailable")
        override suspend fun fetchExamSessions(matId: Int) = sessions
        override suspend fun fetchBookings(matId: Int) = bookings
    }

    private fun useCase(source: CinecaRemoteDataSource): GetCareerDataUseCase {
        val career = CareerTrackModel("Informatica", matId = 10, stuId = 1, matricola = "1", active = true, closedReason = null)
        val auth = object : AuthLocalDataSource {
            override suspend fun saveCredentials(credentials: UserCredentials) = Unit
            override suspend fun clearCredentials() = Unit
            override suspend fun getCredentials(): UserCredentials? = null
            override fun observeUsername(): Flow<String?> = flowOf("user")
        }
        val userInfo = object : UserInfoDataSource {
            override suspend fun saveUserInfo(userInfo: UserInfoModel) = Unit
            override fun getUserInfo(username: String) = flowOf(UserInfoModel(username, "A", "B", 1, listOf(career)))
        }
        val preferences = object : UserPreferenceRepository {
            override suspend fun setCareerId(value: Int) = Unit
            override fun getCareerId(): Flow<Int> = flowOf(10)
        }
        val getUserInfo = GetCurrentUserInfoUseCase(auth, userInfo)
        return GetCareerDataUseCase(GetCurrentCareerUseCase(preferences, getUserInfo), source, clock)
    }

    @Test
    fun `keeps only upcoming sessions of pending exams and attaches bookings`() = runBlocking {
        val source = FakeCinecaSource(
            courses = listOf(course(1, 9, grade = 28), course(2, 6)),
            overview = CareerOverview(120, 9, 1, 2, 28.0, 28.0, 102.67),
            sessions = listOf(
                session(1, row = 2, date = today.plusDays(10), open = true),
                session(2, row = 2, date = today.plusDays(3), open = false),
                session(3, row = 2, date = today.minusDays(5), open = false),
                // Exam already passed: not interesting anymore
                session(4, row = 1, date = today.plusDays(4), open = true)
            ),
            bookings = listOf(ExamBooking(courseRowId = 2, sessionId = 2, position = 3, room = "MT4", building = null))
        )
        val data = useCase(source)().first().getOrThrow()
        val sessions = data.upcomingSessions!!

        assertEquals(listOf(2L, 1L), sessions.map { it.id })
        assertEquals(BookingState.BOOKED, sessions.first().bookingState(today))
        assertEquals(3, sessions.first().booking?.position)
        assertEquals(listOf(1L), data.openSessions.map { it.id })
    }

    @Test
    fun `computes the overview from the libretto when Esse3 stats are unavailable`() = runBlocking {
        val source = FakeCinecaSource(
            courses = listOf(course(1, 9, grade = 30), course(2, 6, grade = 24), course(3, 12)),
            overview = null,
            sessions = emptyList(),
            bookings = emptyList()
        )
        val overview = useCase(source)().first().getOrThrow().overview

        assertEquals(27, overview.requiredCredits)
        assertEquals(15, overview.earnedCredits)
        assertEquals((9 * 30 + 6 * 24) / 15.0, overview.weightedAverage!!, 0.0001)
        assertEquals(27.0, overview.arithmeticAverage!!, 0.0001)
        assertTrue(overview.graduationBase!! > 0)
    }

    @Test
    fun `official averages are kept even without graded exams`() = runBlocking {
        val source = FakeCinecaSource(listOf(course(1, 9)), CareerOverview(120, 0, 0, 1, null, null, null), emptyList(), emptyList())
        val overview = useCase(source)().first().getOrThrow().overview

        assertEquals(120, overview.requiredCredits)
        assertNull(overview.weightedAverage)
    }
}
