package it.fra.myunical.schedule.di

import it.fra.myunical.schedule.presentation.editor.ScheduleProviderEditorViewModel
import it.fra.myunical.schedule.presentation.providers.ScheduleProvidersViewModel
import it.fra.myunical.schedule.presentation.schedule.ScheduleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val scheduleModule = module {
    viewModelOf(::ScheduleViewModel)
    viewModelOf(::ScheduleProvidersViewModel)
    viewModel { params ->
        ScheduleProviderEditorViewModel(
            providerId = params.getOrNull<String>(),
            providerRepository = get(),
            getDegreePrograms = get(),
            getStudyPaths = get()
        )
    }
}
