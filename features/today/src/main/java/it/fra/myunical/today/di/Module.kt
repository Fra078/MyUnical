package it.fra.myunical.today.di

import it.fra.myunical.today.presentation.TodayScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val todayModule = module {
    viewModelOf(::TodayScreenViewModel)
}
