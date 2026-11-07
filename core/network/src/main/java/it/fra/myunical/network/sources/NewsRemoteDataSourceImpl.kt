package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import it.fra.myunical.domain.model.news.NewsPage
import it.fra.myunical.domain.model.news.NewsSource
import it.fra.myunical.domain.sources.NewsRemoteDataSource
import it.fra.myunical.network.mapper.mapToModel
import it.fra.myunical.network.mapper.toNotice
import it.fra.myunical.network.models.news.NewsEntry
import it.fra.myunical.network.models.news.NewsPaginationResponse
import it.fra.myunical.network.models.news.SearchResponse

class NewsRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : NewsRemoteDataSource {

    override suspend fun getNews(source: NewsSource, page: Int): NewsPage = when (source) {
        NewsSource.University -> getUniversityNews(page)
        NewsSource.Notices -> search(page, host = UNIVERSITY_HOST, noticesOnly = true)
        is NewsSource.Department -> search(page, host = source.host, noticesOnly = source.noticesOnly)
    }

    private suspend fun getUniversityNews(page: Int): NewsPage {
        val response = httpClient.get("news/by-context/1") {
            parameter("page", page)
        }
        if (response.status != HttpStatusCode.OK)
            throw IllegalStateException("Errore nella richiesta: ${response.status}")
        val body = response.body<NewsPaginationResponse>()
        return NewsPage(
            items = body.results.map(NewsEntry::mapToModel),
            page = page,
            hasNext = body.next != null
        )
    }

    /** The search index covers every Unical site; `sites` and `categories` filter it (the page size is fixed). */
    private suspend fun search(page: Int, host: String, noticesOnly: Boolean): NewsPage {
        val response = httpClient.get("search/") {
            parameter("sites", host)
            if (noticesOnly) parameter("categories", NOTICES_CATEGORY)
            parameter("page", page)
        }
        if (response.status != HttpStatusCode.OK)
            throw IllegalStateException("Errore nella richiesta: ${response.status}")
        val body = response.body<SearchResponse>()
        return NewsPage(
            items = body.results.map { it.toNotice() },
            page = page,
            hasNext = body.next != null
        )
    }

    private companion object {
        const val UNIVERSITY_HOST = "www.unical.it"
        const val NOTICES_CATEGORY = "Avvisi"
    }
}
