package it.fra.myunical.news.di

import it.fra.myunical.news.presentation.detail.NewsDetailViewModel
import it.fra.myunical.news.presentation.list.NewsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val newsModule = module {
    viewModelOf(::NewsListViewModel)
    viewModel { params -> NewsDetailViewModel(newsId = params.get(), newsRepository = get()) }
}
