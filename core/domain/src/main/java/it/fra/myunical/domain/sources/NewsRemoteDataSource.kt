package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.news.NoticeModel

interface NewsRemoteDataSource {
    suspend fun getLatestNews(): List<NoticeModel>
}