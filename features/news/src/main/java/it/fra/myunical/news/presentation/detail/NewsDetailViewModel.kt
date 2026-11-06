package it.fra.myunical.news.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.repository.NewsRepository
import it.fra.myunical.news.mapper.toDetailUiModel
import it.fra.myunical.news.models.NewsDetailUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class NewsDetailViewModel(
    newsId: Long,
    newsRepository: NewsRepository
) : ViewModel() {

    val state: StateFlow<NewsDetailUiState> = flow {
        val news = newsRepository.getNotice(newsId)
        emit(news?.let { NewsDetailUiState.Loaded(it.toDetailUiModel()) } ?: NewsDetailUiState.NotFound)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewsDetailUiState.Loading)
}
