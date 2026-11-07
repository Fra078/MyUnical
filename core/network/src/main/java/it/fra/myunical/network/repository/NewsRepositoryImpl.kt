package it.fra.myunical.network.repository

import it.fra.myunical.domain.model.news.NewsPage
import it.fra.myunical.domain.model.news.NewsSource
import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.domain.repository.NewsRepository
import it.fra.myunical.domain.sources.NewsRemoteDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Keeps loaded pages of every source in memory, so the detail can open any news already seen. */
internal class NewsRepositoryImpl(
    private val newsRemoteDataSource: NewsRemoteDataSource
) : NewsRepository {

    private val pages = mutableMapOf<NewsSource, MutableMap<Int, NewsPage>>()
    private val mutex = Mutex()

    override suspend fun getPage(source: NewsSource, page: Int, forceRefresh: Boolean): NewsPage {
        if (!forceRefresh) mutex.withLock { pages[source]?.get(page) }?.let { return it }
        val result = newsRemoteDataSource.getNews(source, page)
        mutex.withLock {
            val ofSource = pages.getOrPut(source) { mutableMapOf() }
            // A refreshed first page makes the following ones stale
            if (forceRefresh && page == 1) ofSource.clear()
            ofSource[page] = result
        }
        return result
    }

    override suspend fun getNotice(id: String): NoticeModel? = mutex.withLock {
        pages.values.asSequence()
            .flatMap { it.values.asSequence() }
            .firstNotNullOfOrNull { page -> page.items.firstOrNull { it.id == id } }
    }
}
