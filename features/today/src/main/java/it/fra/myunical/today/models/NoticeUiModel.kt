package it.fra.myunical.today.models

data class NoticeUiModel(
    val id: Long,
    val title: String,
    val category: String,
    /** "Oggi", "Ieri", "5 nov"... */
    val date: String,
    val summary: String,
    val imageUrl: String? = null
)
