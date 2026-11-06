package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.network.models.news.NewsEntry

fun NewsEntry.mapToModel() : NoticeModel {
    return NoticeModel(
        id = id,
        title = publication.title,
        category = publication.categories.firstOrNull()?.name ?: "",
        date = created,
        link = "https://unical.it$path",
        summary = publication.subheading?: "",
        imageUrl = "https://unical.it"+publication.previewImage?.file
    )
}