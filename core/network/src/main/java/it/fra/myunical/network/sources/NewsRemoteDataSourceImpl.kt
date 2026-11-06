package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.domain.sources.NewsRemoteDataSource
import it.fra.myunical.network.mapper.mapToModel
import it.fra.myunical.network.models.news.NewsEntry
import it.fra.myunical.network.models.news.NewsPaginationResponse

class NewsRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : NewsRemoteDataSource {

    override suspend fun getLatestNews() : List<NoticeModel> {
        val result = httpClient.get("news/by-context/1")
        val body = result.body<NewsPaginationResponse>()
        return body.results.map(NewsEntry::mapToModel)
    }

}