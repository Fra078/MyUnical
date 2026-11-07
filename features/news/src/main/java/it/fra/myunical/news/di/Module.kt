package it.fra.myunical.news.di

import it.fra.myunical.news.presentation.detail.NewsDetailViewModel
import it.fra.myunical.news.presentation.list.NewsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val newsModule = module {
    viewModel { params -> NewsListViewModel(newsRepository = get(), getStudentDepartment = get(), openDepartment = params.get()) }
    viewModel { params -> NewsDetailViewModel(newsId = params.get(), newsRepository = get()) }
}
