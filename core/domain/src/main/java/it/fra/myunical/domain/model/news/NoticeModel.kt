package it.fra.myunical.domain.model.news

data class NoticeModel(
    val id: Long,
    val title: String,
    val category: String,
    val date: String,
    val link: String,
    val summary: String,
    val imageUrl: String? = null
)