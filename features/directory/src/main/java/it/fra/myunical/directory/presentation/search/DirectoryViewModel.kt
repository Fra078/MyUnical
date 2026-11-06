@file:OptIn(FlowPreview::class)

package it.fra.myunical.directory.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.directory.models.DirectoryUiState
import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.domain.repository.DirectoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DirectoryViewModel(
    private val directory: DirectoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DirectoryUiState())
    val state: StateFlow<DirectoryUiState> = _state.asStateFlow()

    private val loaded = mutableListOf<Contact>()
    private var nextPage = 1
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            _state.map { it.query.trim() }
                .distinctUntilChanged()
                .drop(1)
                .debounce(DEBOUNCE_MS)
                .collect { startSearch() }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    /** Runs the search right away (keyboard "search" action or retry). */
    fun search() = startSearch()

    fun loadMore() {
        val current = _state.value
        if (searchJob?.isActive == true || current.endReached || current.isQueryTooShort) return
        _state.update { it.copy(isLoadingMore = true, hasError = false) }
        searchJob = viewModelScope.launch { load(current.query, nextPage) }
    }

    private fun startSearch() {
        searchJob?.cancel()
        loaded.clear()
        nextPage = 1
        val current = _state.value
        if (current.isQueryTooShort) {
            _state.update { it.copy(contacts = emptyList(), totalCount = 0, isSearching = false, endReached = true, hasError = false) }
            return
        }
        _state.update { it.copy(isSearching = true, hasError = false) }
        searchJob = viewModelScope.launch { load(current.query, page = 1) }
    }

    private suspend fun load(query: String, page: Int) {
        try {
            val result = directory.search(query, page)
            val known = loaded.mapTo(HashSet()) { it.id }
            loaded += result.items.filter { it.id !in known }
            nextPage = page + 1
            _state.update {
                it.copy(
                    contacts = loaded.toList(),
                    totalCount = result.totalCount,
                    isSearching = false,
                    isLoadingMore = false,
                    endReached = !result.hasNext
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(isSearching = false, isLoadingMore = false, hasError = true) }
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 300L
    }
}
