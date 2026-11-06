package it.fra.myunical.domain.model.news

import java.time.LocalDateTime

data class NoticeModel(
    val id: Long,
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
