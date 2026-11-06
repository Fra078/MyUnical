package it.fra.myunical.network.models.news

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsPaginationResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val page: Int,
    @SerialName("per_page")
    val perPage: Int,
    @SerialName("total_pages")
    val totalPages: Int,
    val results: List<NewsEntry>
)

@Serializable
data class NewsEntry(
    val id: Long,
    val path: String,
    val created: String,
    @SerialName("date_start")
    val dateStart: String? = null,
    val publication: NewsPublication
)

@Serializable
data class NewsPublication(
    val id: Long,
    val title: String,
    val subheading: String? = null,
    val content: String,
    val tags: List<String> = emptyList(),
    @SerialName("preview_image")
    val previewImage: MediaImage? = null,
    @SerialName("category_data")
    val categories: List<NewsCategory> = emptyList()
)

@Serializable
data class MediaImage(
    val id: Long,
    val file: String,
    val title: String? = null,
    val description: String? = null
)

@Serializable
data class NewsCategory(
    val id: Long,
    val name: String,
    val description: String? = null
)