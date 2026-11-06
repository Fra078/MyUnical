package it.fra.myunical.schedule.presentation.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.repository.ScheduleProviderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleProvidersViewModel(
    private val providerRepository: ScheduleProviderRepository
) : ViewModel() {

    /** `null` until the stored providers have been read. */
    val providers: StateFlow<List<ScheduleProvider>?> = providerRepository.getProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun remove(provider: ScheduleProvider) {
        viewModelScope.launch { providerRepository.removeProvider(provider.id) }
    }

    fun restore(provider: ScheduleProvider) {
        viewModelScope.launch { providerRepository.upsertProvider(provider) }
    }
}
