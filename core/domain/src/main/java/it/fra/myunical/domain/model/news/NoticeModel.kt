package it.fra.myunical.domain.model.news

import java.time.LocalDateTime

data class NoticeModel(
    /** Unique across sources: the university feed and the search index number their items differently. */
    val id: String,
    val title: String,
    val category: String,
    val publishedAt: LocalDateTime?,
    val link: String,
    val summary: String,
    val imageUrl: String? = null,
    val contentHtml: String = "",
    val tags: List<String> = emptyList()
)

data class NewsPage(
    val items: List<NoticeModel>,
    val page: Int,
    val hasNext: Boolean
)
