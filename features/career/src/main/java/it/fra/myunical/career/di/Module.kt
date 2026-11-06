package it.fra.myunical.career.di

import it.fra.myunical.career.presentation.CareerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val careerModule = module {
    viewModelOf(::CareerViewModel)
}
