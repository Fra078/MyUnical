package it.fra.myunical.news.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.domain.repository.NewsRepository
import it.fra.myunical.news.mapper.toItemUiModel
import it.fra.myunical.news.models.NewsListUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewsListViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewsListUiState())
    val state: StateFlow<NewsListUiState> = _state.asStateFlow()

    private val loaded = mutableListOf<NoticeModel>()
    private var nextPage = 1
    private var loadJob: Job? = null

    init {
        loadMore()
    }

    /** Loads the next page, unless one is already loading or the end was reached. */
    fun loadMore() {
        if (loadJob?.isActive == true || _state.value.endReached) return
        _state.update { it.copy(isLoadingMore = loaded.isNotEmpty(), hasError = false) }
        loadJob = viewModelScope.launch { load(nextPage, forceRefresh = false) }
    }

    fun refresh() {
        loadJob?.cancel()
        _state.update { it.copy(isRefreshing = true, hasError = false) }
        loadJob = viewModelScope.launch { load(page = 1, forceRefresh = true) }
    }

    private suspend fun load(page: Int, forceRefresh: Boolean) {
        try {
            val result = newsRepository.getPage(page, forceRefresh)
            if (page == 1) loaded.clear()
            // Pages can overlap when news are published while scrolling
            val known = loaded.mapTo(HashSet()) { it.id }
            loaded += result.items.filter { it.id !in known }
            nextPage = page + 1
            _state.value = NewsListUiState(
                items = loaded.map(NoticeModel::toItemUiModel),
                isLoading = false,
                endReached = !result.hasNext
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, isLoadingMore = false, isRefreshing = false, hasError = true) }
        }
    }
}
