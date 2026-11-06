package it.fra.myunical.documents.di

import it.fra.myunical.documents.presentation.DocumentsViewModel
import it.fra.myunical.documents.storage.DocumentStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val documentsModule = module {
    single { DocumentStore(androidContext()) }
    viewModelOf(::DocumentsViewModel)
}
