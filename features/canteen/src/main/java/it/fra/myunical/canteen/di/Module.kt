package it.fra.myunical.canteen.di

import it.fra.myunical.canteen.presentation.CanteenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val canteenModule = module {
    viewModelOf(::CanteenViewModel)
}
