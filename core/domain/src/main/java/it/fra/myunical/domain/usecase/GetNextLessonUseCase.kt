package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.schedule.NextLesson
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.repository.ScheduleProviderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Emits the ongoing or next lesson across all configured schedules, re-evaluated every minute.
 * Timetables come from [GetMonthlyScheduleUseCase], so periodic re-evaluation hits its in-memory cache.
 */
class GetNextLessonUseCase(
    private val providerRepository: ScheduleProviderRepository,
    private val getMonthlySchedule: GetMonthlyScheduleUseCase,
    private val clock: () -> LocalDateTime = LocalDateTime::now
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<NextLesson> =
        providerRepository.getProviders().flatMapLatest { providers ->
            flow {
                while (true) {
                    emit(find(providers, clock()))
                    delay(REFRESH_INTERVAL_MILLIS)
                }
            }
        }.distinctUntilChanged()

    suspend fun find(providers: List<ScheduleProvider>, now: LocalDateTime): NextLesson {
        if (providers.isEmpty()) return NextLesson.NotConfigured
        val namesById = providers.associate { it.id to it.displayName }
        var month = YearMonth.from(now)
        repeat(LOOK_AHEAD_MONTHS) {
            val events = try {
                getMonthlySchedule(providers, month.year, month.monthValue)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return NextLesson.Unavailable
            }
            val index = events.indexOfFirst { LocalDateTime.of(it.date, it.end).isAfter(now) }
            if (index != -1) {
                val lesson = events[index]
                return NextLesson.Upcoming(
                    lesson = lesson,
                    providerName = namesById[lesson.providerId].orEmpty(),
                    following = events.getOrNull(index + 1)?.takeIf { it.date == lesson.date }
                )
            }
            month = month.plusMonths(1)
        }
        return NextLesson.NoneScheduled
    }

    private companion object {
        const val REFRESH_INTERVAL_MILLIS = 60_000L
        const val LOOK_AHEAD_MONTHS = 2
    }
}
