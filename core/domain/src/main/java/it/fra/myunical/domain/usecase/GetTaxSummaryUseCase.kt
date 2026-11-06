@file:OptIn(ExperimentalCoroutinesApi::class)

package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.taxes.StudentTaxes
import it.fra.myunical.domain.model.taxes.TaxSemaphore
import it.fra.myunical.domain.model.taxes.TaxSummary
import it.fra.myunical.domain.sources.TaxRemoteDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * Taxes of the logged user. Fees are attached to careers in Esse3, so every career is fetched
 * to show the full history, while the semaphore comes from the current career.
 * Results are cached and shared between collectors (home card and taxes screen) until [refresh].
 */
class GetTaxSummaryUseCase(
    private val getCurrentUserInfo: GetCurrentUserInfoUseCase,
    private val getCurrentCareer: GetCurrentCareerUseCase,
    private val taxRemoteDataSource: TaxRemoteDataSource,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    private data class CacheEntry(val generation: Int, val taxes: StudentTaxes)

    private val generation = MutableStateFlow(0)
    // Concurrent: careers are fetched in parallel
    private val cache = ConcurrentHashMap<Int, CacheEntry>()
    private val cacheMutex = Mutex()

    operator fun invoke(): Flow<Result<TaxSummary>> = combine(
        getCurrentUserInfo().map { user -> user.careers.map { it.stuId } }.distinctUntilChanged(),
        getCurrentCareer().map { it?.stuId }.distinctUntilChanged(),
        generation
    ) { stuIds, currentStuId, generation -> Triple(stuIds, currentStuId, generation) }
        .mapLatest { (stuIds, currentStuId, generation) ->
            try {
                Result.success(load(stuIds, currentStuId, generation))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Invalidates the cache: every active collector reloads. */
    fun refresh() = generation.update { it + 1 }

    private suspend fun load(stuIds: List<Int>, currentStuId: Int?, generation: Int): TaxSummary {
        val careers = (listOfNotNull(currentStuId) + stuIds).distinct()
        // Held for the whole load so concurrent collectors wait and then hit the cache
        val results = cacheMutex.withLock {
            coroutineScope {
                careers.map { stuId ->
                    async { stuId to runCatching { fetch(stuId, generation) } }
                }.awaitAll()
            }
        }.toMap()

        // The current career is mandatory, older ones only add history
        val current = currentStuId?.let { results.getValue(it).getOrThrow() }
        val all = results.values.mapNotNull { it.getOrNull() }
        if (all.isEmpty()) results.values.first().getOrThrow()

        return TaxSummary(
            semaphore = current?.semaphore ?: all.maxBy { it.semaphore.severity() }.semaphore,
            semaphoreAmountDue = current?.amountDue,
            invoices = all.flatMap { it.invoices }.distinctBy { it.id },
            today = LocalDate.now(clock)
        )
    }

    private suspend fun fetch(stuId: Int, generation: Int): StudentTaxes {
        cache[stuId]?.takeIf { it.generation >= generation }?.let { return it.taxes }
        val taxes = taxRemoteDataSource.getStudentTaxes(stuId)
        cache[stuId] = CacheEntry(generation, taxes)
        return taxes
    }

    private fun TaxSemaphore.severity() = when (this) {
        TaxSemaphore.UNKNOWN -> 0
        TaxSemaphore.GREEN -> 1
        TaxSemaphore.YELLOW -> 2
        TaxSemaphore.RED -> 3
    }
}
