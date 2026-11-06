package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.network.models.news.NewsEntry
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

private const val UnicalBaseUrl = "https://www.unical.it"

fun NewsEntry.mapToModel() : NoticeModel {
    return NoticeModel(
        id = id,
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
