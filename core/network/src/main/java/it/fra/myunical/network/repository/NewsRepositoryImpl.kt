package it.fra.myunical.network.repository

import it.fra.myunical.domain.model.news.NewsPage
import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.domain.repository.NewsRepository
import it.fra.myunical.domain.sources.NewsRemoteDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Keeps loaded pages in memory, so the detail can open any news already seen. */
internal class NewsRepositoryImpl(
    private val newsRemoteDataSource: NewsRemoteDataSource
) : NewsRepository {

    private val pages = mutableMapOf<Int, NewsPage>()
    private val mutex = Mutex()

    override suspend fun getPage(page: Int, forceRefresh: Boolean): NewsPage {
        if (!forceRefresh) mutex.withLock { pages[page] }?.let { return it }
        val result = newsRemoteDataSource.getNews(page)
        mutex.withLock {
            // A refreshed first page makes the following ones stale
            if (forceRefresh && page == 1) pages.clear()
            pages[page] = result
        }
        return result
    }

    override suspend fun getNotice(id: Long): NoticeModel? = mutex.withLock {
        pages.values.firstNotNullOfOrNull { page -> page.items.firstOrNull { it.id == id } }
    }
}
