package it.fra.myunical.network.models.news

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Answer of the uniCMS search, which indexes the news and notices of every Unical site. */
@Serializable
data class SearchResponse(
    val count: Int = 0,
    val next: String? = null,
    val results: List<SearchHit> = emptyList()
)

@Serializable
data class SearchHit(
    val title: String,
    /** Short summary; empty for many notices. */
    val heading: String? = null,
    @SerialName("content_id") val contentId: String,
    /** Path under the site's host ("/media/..."), null for notices. */
    val image: String? = null,
    val content: String? = null,
    val sites: List<String> = emptyList(),
    /** Scheme-relative article URLs, the first is the canonical one. */
    val urls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    /** Local date time without offset. */
    val published: String? = null
)
