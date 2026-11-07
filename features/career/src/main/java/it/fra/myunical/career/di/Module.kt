package it.fra.myunical.career.di

import it.fra.myunical.career.presentation.CareerViewModel
import it.fra.myunical.career.presentation.detail.CourseDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val careerModule = module {
    viewModelOf(::CareerViewModel)
    viewModel { params -> CourseDetailViewModel(courseRowId = params.get(), getCourseDetail = get(), catalog = get()) }
}
