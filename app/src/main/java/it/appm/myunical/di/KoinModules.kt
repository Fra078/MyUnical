package it.appm.myunical.di

import it.fra.myunical.career.presentation.CareerViewModel
import it.fra.myunical.domain.usecase.GetAuthStateUseCase
import it.fra.myunical.domain.usecase.GetCurrentCareerUseCase
import it.fra.myunical.domain.usecase.GetCurrentStudyPlanUseCase
import it.fra.myunical.domain.usecase.GetCurrentUserInfoUseCase
import it.fra.myunical.domain.usecase.LoginUseCase
import it.fra.myunical.domain.usecase.SaveCredentialsUseCase
import it.fra.myunical.schedule.presentation.settings.ScheduleProviderConfigurationViewModel
import it.fra.myunical.today.presentation.TodayScreenViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val domainModule = module {
    singleOf(::LoginUseCase)
    singleOf(::GetAuthStateUseCase)
    singleOf(::SaveCredentialsUseCase)
    singleOf(::GetCurrentCareerUseCase)
    singleOf(::GetCurrentUserInfoUseCase)
    singleOf(::GetCurrentStudyPlanUseCase)
}

val appModule = module {
    viewModelOf(::TodayScreenViewModel)
    viewModelOf(::CareerViewModel)
    viewModelOf(::ScheduleProviderConfigurationViewModel)
}