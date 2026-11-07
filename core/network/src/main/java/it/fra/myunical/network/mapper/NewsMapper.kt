package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.network.models.news.NewsEntry
import it.fra.myunical.network.models.news.SearchHit
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

private const val UnicalBaseUrl = "https://www.unical.it"

fun NewsEntry.mapToModel() : NoticeModel {
    return NoticeModel(
        id = "n$id",
        title = publication.title.trim(),
        category = publication.categories.firstOrNull()?.name.orEmpty(),
        publishedAt = (dateStart ?: created).toLocalDateTime(),
        link = UnicalBaseUrl + path,
        summary = publication.subheading?.trim().orEmpty(),
        imageUrl = publication.previewImage?.file?.let { UnicalBaseUrl + it },
        contentHtml = publication.content,
        tags = publication.tags
    )
}

private fun String.toLocalDateTime(): LocalDateTime? = runCatching {
    OffsetDateTime.parse(this).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
}.getOrNull()

private const val NOTICES_CATEGORY = "Avvisi"

/** A result of the search index: a news of a department or a notice of the university or a department. */
fun SearchHit.toNotice(): NoticeModel {
    val host = sites.firstOrNull() ?: "www.unical.it"
    return NoticeModel(
        id = "s$contentId",
        title = title.trim(),
        // "Avvisi" says little in a list of notices: prefer the topic when there is one
        category = categories.firstOrNull { it != NOTICES_CATEGORY } ?: categories.firstOrNull().orEmpty(),
        publishedAt = published?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() },
        link = urls.firstOrNull().toAbsoluteUrl() ?: "https://$host/",
        summary = heading?.trim().orEmpty(),
        imageUrl = image?.takeIf { it.startsWith("/") }?.let { "https://$host$it" },
        contentHtml = content.orEmpty(),
        tags = tags
    )
}
