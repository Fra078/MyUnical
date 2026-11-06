package it.fra.myunical.today.models

data class NoticeUiModel(
    val id: Long,
    val title: String,
    val category: String,
    val date: String,
    val summary: String,
    val imageUrl: String? = null
)
