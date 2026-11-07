package it.fra.myunical.news.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.degreeexplorer.Department
import it.fra.myunical.domain.model.news.NewsSource
import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.domain.repository.NewsRepository
import it.fra.myunical.domain.usecase.GetStudentDepartmentUseCase
import it.fra.myunical.news.mapper.toItemUiModel
import it.fra.myunical.news.models.NewsListUiState
import it.fra.myunical.news.models.NewsScreenState
import it.fra.myunical.news.models.NewsTab
import it.fra.myunical.news.models.NewsTabUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * One paged feed per tab: university news, university notices and, once the student's department is
 * known, the news and notices of its site. A feed starts loading when its tab is first shown.
 */
class NewsListViewModel(
    private val newsRepository: NewsRepository,
    private val getStudentDepartment: GetStudentDepartmentUseCase,
    openDepartment: Boolean
) : ViewModel() {

    private class Feed {
        val loaded = mutableListOf<NoticeModel>()
        var nextPage = 1
        var job: Job? = null
    }

    private val _state = MutableStateFlow(
        NewsScreenState(tabs = baseTabs, selected = if (openDepartment) NewsTab.DEPARTMENT else NewsTab.NEWS)
    )
    val state: StateFlow<NewsScreenState> = _state.asStateFlow()

    private val feeds = NewsTab.entries.associateWith { Feed() }
    private var department: Department? = null

    init {
        viewModelScope.launch {
            department = try {
                getStudentDepartment()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            val found = department
            _state.update { state ->
                val tabs = if (found == null) baseTabs else baseTabs + NewsTabUi(NewsTab.DEPARTMENT, found.shortName)
                // A department tab asked for but not available falls back to the news
                state.copy(tabs = tabs, selected = if (found == null && state.selected == NewsTab.DEPARTMENT) NewsTab.NEWS else state.selected)
            }
            loadMore(_state.value.selected)
        }
    }

    fun select(tab: NewsTab) {
        _state.update { it.copy(selected = tab) }
        if (_state.value.lists[tab] == null) loadMore(tab)
    }

    /** Loads the next page of [tab], unless one is already loading or the end was reached. */
    fun loadMore(tab: NewsTab = _state.value.selected) {
        val source = sourceOf(tab) ?: return
        val feed = feeds.getValue(tab)
        val current = _state.value.lists[tab]
        if (feed.job?.isActive == true || current?.endReached == true) return
        updateList(tab) { it.copy(isLoadingMore = feed.loaded.isNotEmpty(), hasError = false) }
        feed.job = viewModelScope.launch { load(tab, source, feed.nextPage, forceRefresh = false) }
    }

    fun refresh(tab: NewsTab = _state.value.selected) {
        val source = sourceOf(tab) ?: return
        val feed = feeds.getValue(tab)
        feed.job?.cancel()
        updateList(tab) { it.copy(isRefreshing = true, hasError = false) }
        feed.job = viewModelScope.launch { load(tab, source, page = 1, forceRefresh = true) }
    }

    private fun sourceOf(tab: NewsTab): NewsSource? = when (tab) {
        NewsTab.NEWS -> NewsSource.University
        NewsTab.NOTICES -> NewsSource.Notices
        NewsTab.DEPARTMENT -> department?.let { NewsSource.Department(it.host) }
    }

    private fun updateList(tab: NewsTab, transform: (NewsListUiState) -> NewsListUiState) {
        _state.update { it.copy(lists = it.lists + (tab to transform(it.lists[tab] ?: NewsListUiState()))) }
    }

    private suspend fun load(tab: NewsTab, source: NewsSource, page: Int, forceRefresh: Boolean) {
        val feed = feeds.getValue(tab)
        try {
            val result = newsRepository.getPage(source, page, forceRefresh)
            if (page == 1) feed.loaded.clear()
            // Pages can overlap when news are published while scrolling
            val known = feed.loaded.mapTo(HashSet()) { it.id }
            feed.loaded += result.items.filter { it.id !in known }
            feed.nextPage = page + 1
            updateList(tab) {
                NewsListUiState(
                    items = feed.loaded.map(NoticeModel::toItemUiModel),
                    isLoading = false,
                    endReached = !result.hasNext
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            updateList(tab) { it.copy(isLoading = false, isLoadingMore = false, isRefreshing = false, hasError = true) }
        }
    }

    private companion object {
        val baseTabs = listOf(NewsTabUi(NewsTab.NEWS, "Notizie"), NewsTabUi(NewsTab.NOTICES, "Avvisi"))
    }
}
