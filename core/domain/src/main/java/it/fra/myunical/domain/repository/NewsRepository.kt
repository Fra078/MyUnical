package it.fra.myunical.domain.repository

import it.fra.myunical.domain.model.news.NewsPage
import it.fra.myunical.domain.model.news.NoticeModel

/** Paged university news shared by the home carousel, the news list and the detail screen. */
interface NewsRepository {
    /** Returns a cached page unless [forceRefresh]; refreshing page 1 drops the following ones. */
    suspend fun getPage(page: Int, forceRefresh: Boolean = false): NewsPage

    /** A news already loaded in some page; the API has no single-item endpoint. */
    suspend fun getNotice(id: Long): NoticeModel?
}
