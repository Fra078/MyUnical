package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import it.fra.myunical.domain.model.news.NewsPage
import it.fra.myunical.domain.sources.NewsRemoteDataSource
import it.fra.myunical.network.mapper.mapToModel
import it.fra.myunical.network.models.news.NewsEntry
import it.fra.myunical.network.models.news.NewsPaginationResponse

class NewsRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : NewsRemoteDataSource {

    override suspend fun getNews(page: Int): NewsPage {
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
}
