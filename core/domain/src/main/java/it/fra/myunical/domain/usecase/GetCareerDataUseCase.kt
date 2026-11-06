@file:OptIn(ExperimentalCoroutinesApi::class)

package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.career.CareerData
import it.fra.myunical.domain.model.career.CareerOverview
import it.fra.myunical.domain.model.career.ExamBooking
import it.fra.myunical.domain.model.career.ExamSession
import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.general.ExamResult
import it.fra.myunical.domain.sources.CinecaRemoteDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate

/**
 * Libretto, progress and upcoming exam sessions of the current career.
 * Results are cached per career and shared between collectors (home card and career tab) until [refresh].
 */
class GetCareerDataUseCase(
    private val getCurrentCareer: GetCurrentCareerUseCase,
    private val cinecaDataSource: CinecaRemoteDataSource,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    private data class CacheEntry(val generation: Int, val data: CareerData)

    private val generation = MutableStateFlow(0)
    private val cache = mutableMapOf<Int, CacheEntry>()
    private val cacheMutex = Mutex()

    operator fun invoke(): Flow<Result<CareerData>> = combine(
        getCurrentCareer().filterNotNull().distinctUntilChanged(),
        generation
    ) { career, generation -> career to generation }
        .mapLatest { (career, generation) ->
            try {
                Result.success(load(career, generation))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Invalidates the cache: every active collector reloads. */
    fun refresh() = generation.update { it + 1 }

    // Held for the whole load so concurrent collectors wait and then hit the cache
    private suspend fun load(career: CareerTrackModel, generation: Int): CareerData = cacheMutex.withLock {
        cache[career.matId]?.takeIf { it.generation >= generation }?.let { return it.data }
        val data = fetch(career)
        cache[career.matId] = CacheEntry(generation, data)
        data
    }

    private suspend fun fetch(career: CareerTrackModel): CareerData = coroutineScope {
        val overview = async { runCatching { cinecaDataSource.fetchCareerOverview(career.matId) } }
        val sessions = async { runCatching { cinecaDataSource.fetchExamSessions(career.matId) } }
        val bookings = async { runCatching { cinecaDataSource.fetchBookings(career.matId) } }
        val courses = cinecaDataSource.fetchStudyPlan(career.matId).getOrThrow()
        val today = LocalDate.now(clock)

        CareerData(
            career = career,
            courses = courses,
            overview = overview.await().getOrNull().withFallback(courses),
            upcomingSessions = sessions.await().getOrNull()?.let { all ->
                upcomingSessions(all, bookings.await().getOrNull().orEmpty(), courses, today)
            },
            bookings = bookings.await().getOrNull(),
            today = today
        )
    }

    private fun upcomingSessions(
        sessions: List<ExamSession>,
        bookings: List<ExamBooking>,
        courses: List<Course>,
        today: LocalDate
    ): List<ExamSession> {
        val passedRows = courses.filter(Course::isPassed).map(Course::id).toSet()
        return sessions
            .filter { !it.date.isBefore(today) && it.courseRowId !in passedRows }
            .map { session ->
                session.copy(booking = bookings.firstOrNull {
                    it.courseRowId == session.courseRowId && it.sessionId == session.id
                })
            }
            .sortedWith(compareBy(ExamSession::date, ExamSession::time))
    }

    companion object {

        /** Fills what Esse3 did not provide (stats unavailable or averages not computed yet). */
        internal fun CareerOverview?.withFallback(courses: List<Course>): CareerOverview {
            val passed = courses.filter(Course::isPassed)
            val graded = passed.mapNotNull { course ->
                (course.outcome?.result as? ExamResult.Graded)?.let { course to it.grade }
            }
            val gradedCredits = graded.sumOf { (course, _) -> course.credits }
            val weighted = if (gradedCredits > 0) {
                graded.sumOf { (course, grade) -> course.credits * grade }.toDouble() / gradedCredits
            } else null
            val arithmetic = if (graded.isNotEmpty()) graded.sumOf { it.second }.toDouble() / graded.size else null

            val base = this ?: CareerOverview(
                requiredCredits = courses.sumOf(Course::credits),
                earnedCredits = passed.sumOf(Course::credits),
                passedCount = passed.size,
                totalCount = courses.size,
                weightedAverage = null,
                arithmeticAverage = null,
                graduationBase = null
            )
            return base.copy(
                weightedAverage = base.weightedAverage ?: weighted,
                arithmeticAverage = base.arithmeticAverage ?: arithmetic,
                graduationBase = base.graduationBase ?: weighted?.let { it * 110.0 / 30.0 }
            )
        }
    }
}
