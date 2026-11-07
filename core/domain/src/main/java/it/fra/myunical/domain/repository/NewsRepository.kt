package it.fra.myunical.domain.repository

import it.fra.myunical.domain.model.news.NewsPage
import it.fra.myunical.domain.model.news.NewsSource
import it.fra.myunical.domain.model.news.NoticeModel

/** Paged news shared by the home, the news list and the detail screen. */
interface NewsRepository {
    /** Returns a cached page unless [forceRefresh]; refreshing page 1 drops the following ones of [source]. */
    suspend fun getPage(source: NewsSource, page: Int, forceRefresh: Boolean = false): NewsPage

    /** A news already loaded from any source; the APIs have no single-item endpoint. */
    suspend fun getNotice(id: String): NoticeModel?
}
