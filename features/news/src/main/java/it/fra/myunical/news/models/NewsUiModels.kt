package it.fra.myunical.news.models

data class NewsItemUiModel(
    val id: Long,
    val title: String,
    val summary: String,
    val category: String,
    val dateLabel: String?,
    val imageUrl: String?
)

data class NewsListUiState(
    val items: List<NewsItemUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    /** Set when the last page request failed. */
    val hasError: Boolean = false,
    val endReached: Boolean = false
)

data class NewsDetailUiModel(
    val id: Long,
    val title: String,
    val summary: String,
    val category: String,
    val dateLabel: String?,
    val imageUrl: String?,
    val contentHtml: String,
    val tags: List<String>,
    val link: String
)

sealed interface NewsDetailUiState {
    data object Loading : NewsDetailUiState
    /** The news is not in memory anymore (e.g. after process death). */
    data object NotFound : NewsDetailUiState
    data class Loaded(val news: NewsDetailUiModel) : NewsDetailUiState
}
