package it.fra.myunical.news.mapper

import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.news.models.NewsDetailUiModel
import it.fra.myunical.news.models.NewsItemUiModel
import it.fra.myunical.ui.format.toRelativeLabel

fun NoticeModel.toItemUiModel() = NewsItemUiModel(
    id = id,
    title = title,
    summary = summary,
    category = category,
    dateLabel = publishedAt?.toLocalDate()?.toRelativeLabel(),
    imageUrl = imageUrl
)

fun NoticeModel.toDetailUiModel() = NewsDetailUiModel(
    id = id,
    title = title,
    summary = summary,
    category = category,
    dateLabel = publishedAt?.toLocalDate()?.toRelativeLabel(),
    imageUrl = imageUrl,
    contentHtml = contentHtml,
    tags = tags,
    link = link
)
