package it.fra.myunical.taxes.di

import it.fra.myunical.taxes.presentation.TaxesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val taxesModule = module {
    viewModelOf(::TaxesViewModel)
}
