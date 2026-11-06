package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.schedule.AcademicYear
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GetMonthlyScheduleUseCase(
    private val unicalStorageDataSource: UnicalStorageDataSource
) {

    private data class CacheKey(
        val cdsCode: String,
        val academicYear: Int,
        val cdsYear: Int,
        val year: Int,
        val month: Int
    )

    // Raw (unfiltered) timetables are shared between providers of the same degree
    private val cache = mutableMapOf<CacheKey, List<ScheduleEvent>>()
    private val cacheMutex = Mutex()

    suspend operator fun invoke(
        providers: List<ScheduleProvider>,
        year: Int,
        month: Int,
        forceRefresh: Boolean = false
    ): List<ScheduleEvent> = coroutineScope {
        val academicYear = AcademicYear.of(year, month)
        providers.flatMap { provider ->
            provider.cdsYears.map { cdsYear ->
                async {
                    val key = CacheKey(provider.cdsCode, academicYear, cdsYear, year, month)
                    val events = fetch(key, forceRefresh)
                    val codes = provider.trackedCodes
                    events.filter { it.courseCode in codes }
                        .map { it.copy(providerId = provider.id) }
                }
            }
        }.awaitAll()
            .flatten()
            .distinctBy { listOf(it.providerId, it.courseCode, it.date, it.start, it.end) }
            .sortedWith(compareBy(ScheduleEvent::date, ScheduleEvent::start, ScheduleEvent::end))
    }

    private suspend fun fetch(key: CacheKey, forceRefresh: Boolean): List<ScheduleEvent> {
        if (!forceRefresh) cacheMutex.withLock { cache[key] }?.let { return it }
        val events = unicalStorageDataSource.getTimetable(
            cdsCode = key.cdsCode,
            academicYear = key.academicYear,
            cdsYear = key.cdsYear,
            year = key.year,
            month = key.month
        )
        cacheMutex.withLock { cache[key] = events }
        return events
    }
}
