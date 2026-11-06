package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.news.NewsPage

interface NewsRemoteDataSource {
    /** University news, newest first; [page] starts from 1. Throws on errors. */
    suspend fun getNews(page: Int): NewsPage
}
