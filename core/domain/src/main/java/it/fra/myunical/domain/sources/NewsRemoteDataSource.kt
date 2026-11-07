package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.news.NewsPage
import it.fra.myunical.domain.model.news.NewsSource

interface NewsRemoteDataSource {
    /** News of [source], newest first; [page] starts from 1. Throws on errors. */
    suspend fun getNews(source: NewsSource, page: Int): NewsPage
}
