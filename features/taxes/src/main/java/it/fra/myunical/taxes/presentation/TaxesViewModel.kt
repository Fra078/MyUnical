package it.fra.myunical.taxes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.taxes.TaxSummary
import it.fra.myunical.domain.usecase.GetTaxSummaryUseCase
import it.fra.myunical.taxes.mapper.toOverview
import it.fra.myunical.taxes.mapper.toPaidGroups
import it.fra.myunical.taxes.mapper.toUnpaidUiModels
import it.fra.myunical.taxes.models.TaxesIntent
import it.fra.myunical.taxes.models.TaxesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TaxesViewModel(
    private val getTaxSummary: GetTaxSummaryUseCase
) : ViewModel() {

    /** Last successful summary plus whether the latest load failed. */
    private data class LoadState(val summary: TaxSummary? = null, val hasError: Boolean = false, val loaded: Boolean = false)

    private val expandedIds = MutableStateFlow(emptySet<String>())
    private val isRefreshing = MutableStateFlow(false)

    private val loadState = getTaxSummary()
        .onEach { isRefreshing.value = false }
        .scan(LoadState()) { previous, result ->
            result.fold(
                onSuccess = { LoadState(summary = it, loaded = true) },
                onFailure = { previous.copy(hasError = true, loaded = true) }
            )
        }

    val state: StateFlow<TaxesUiState> = combine(loadState, expandedIds, isRefreshing) { load, expanded, refreshing ->
        val summary = load.summary
        TaxesUiState(
            isLoading = !load.loaded,
            isRefreshing = refreshing,
            hasError = load.hasError,
            overview = summary?.toOverview(),
            unpaid = summary?.toUnpaidUiModels().orEmpty(),
            paidGroups = summary?.toPaidGroups().orEmpty(),
            expandedIds = expanded
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaxesUiState())

    fun onIntent(intent: TaxesIntent) {
        when (intent) {
            TaxesIntent.Refresh -> {
                isRefreshing.value = true
                getTaxSummary.refresh()
            }
            is TaxesIntent.ToggleExpanded -> expandedIds.update {
                if (intent.id in it) it - intent.id else it + intent.id
            }
        }
    }
}
