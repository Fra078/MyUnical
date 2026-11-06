package it.fra.myunical.directory.di

import it.fra.myunical.directory.presentation.detail.ContactDetailViewModel
import it.fra.myunical.directory.presentation.search.DirectoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val directoryModule = module {
    viewModelOf(::DirectoryViewModel)
    viewModel { params -> ContactDetailViewModel(contactId = params.get(), directory = get()) }
}
