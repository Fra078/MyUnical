package it.fra.myunical.schedule.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.repository.ScheduleProviderRepository
import it.fra.myunical.domain.usecase.GetMonthlyScheduleUseCase
import it.fra.myunical.schedule.mapper.toUiModel
import it.fra.myunical.schedule.models.ProviderFilter
import it.fra.myunical.schedule.models.ScheduleDay
import it.fra.myunical.schedule.models.ScheduleIntent
import it.fra.myunical.schedule.models.ScheduleUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class ScheduleViewModel(
    providerRepository: ScheduleProviderRepository,
    private val getMonthlySchedule: GetMonthlyScheduleUseCase
) : ViewModel() {

    private data class LoadResult(
        val providers: List<ScheduleProvider>? = null,
        val events: List<ScheduleEvent> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val hasError: Boolean = false
    )

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val hiddenProviderIds = MutableStateFlow(emptySet<String>())
    private val refreshRequests = MutableStateFlow(0)
    private val loadResult = MutableStateFlow(LoadResult())

    val state: StateFlow<ScheduleUiState> =
        combine(selectedMonth, loadResult, hiddenProviderIds) { month, result, hidden ->
            buildUiState(month, result, hidden)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleUiState())

    init {
        viewModelScope.launch {
            var handledRefresh = refreshRequests.value
            combine(providerRepository.getProviders(), selectedMonth, refreshRequests, ::Triple)
                .collectLatest { (providers, month, refresh) ->
                    val forceRefresh = refresh != handledRefresh
                    handledRefresh = refresh
                    load(providers, month, forceRefresh)
                }
        }
    }

    fun onIntent(intent: ScheduleIntent) {
        when (intent) {
            ScheduleIntent.PreviousMonth -> selectedMonth.update { it.minusMonths(1) }
            ScheduleIntent.NextMonth -> selectedMonth.update { it.plusMonths(1) }
            ScheduleIntent.GoToToday -> selectedMonth.value = YearMonth.now()
            ScheduleIntent.Refresh -> refreshRequests.update { it + 1 }
            is ScheduleIntent.ToggleProvider -> hiddenProviderIds.update {
                if (intent.providerId in it) it - intent.providerId else it + intent.providerId
            }
        }
    }

    private suspend fun load(providers: List<ScheduleProvider>, month: YearMonth, forceRefresh: Boolean) {
        if (providers.isEmpty()) {
            loadResult.value = LoadResult(providers = providers)
            return
        }
        loadResult.update {
            it.copy(
                providers = providers,
                isLoading = !forceRefresh,
                isRefreshing = forceRefresh,
                hasError = false
            )
        }
        try {
            val events = getMonthlySchedule(providers, month.year, month.monthValue, forceRefresh)
            loadResult.value = LoadResult(providers = providers, events = events)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Keep what was already shown (e.g. on a failed pull-to-refresh) and surface the error
            loadResult.update { it.copy(isLoading = false, isRefreshing = false, hasError = true) }
        }
    }

    private fun buildUiState(month: YearMonth, result: LoadResult, hidden: Set<String>): ScheduleUiState {
        val providers = result.providers
        val namesById = providers.orEmpty().associate { it.id to it.displayName }
        val showProviderName = namesById.size > 1
        val days = result.events
            .filter { it.providerId !in hidden && YearMonth.from(it.date) == month }
            .groupBy(ScheduleEvent::date)
            .map { (date, events) ->
                ScheduleDay(
                    date = date,
                    lessons = events.map { it.toUiModel(namesById[it.providerId]?.takeIf { showProviderName }) }
                )
            }
        return ScheduleUiState(
            month = month,
            today = LocalDate.now(),
            providers = providers?.map { ProviderFilter(it.id, it.displayName, it.id !in hidden) },
            days = days,
            isLoading = result.isLoading,
            isRefreshing = result.isRefreshing,
            hasError = result.hasError
        )
    }
}
