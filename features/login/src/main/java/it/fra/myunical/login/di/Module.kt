package it.fra.myunical.login.di

import it.fra.myunical.login.presentation.credentials.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    viewModelOf(::LoginViewModel)
}