package it.appm.myunical.di

import it.fra.myunical.domain.usecase.GetAuthStateUseCase
import it.fra.myunical.domain.usecase.GetCanteenMenuUseCase
import it.fra.myunical.domain.usecase.GetCareerDataUseCase
import it.fra.myunical.domain.usecase.GetCourseDetailUseCase
import it.fra.myunical.domain.usecase.DegreeRegulationFinder
import it.fra.myunical.domain.usecase.GetDegreeRegulationsUseCase
import it.fra.myunical.domain.usecase.ResolveCdsCodeUseCase
import it.fra.myunical.domain.usecase.GetCurrentCareerUseCase
import it.fra.myunical.domain.usecase.GetCurrentUserInfoUseCase
import it.fra.myunical.domain.usecase.GetDegreeProgramsUseCase
import it.fra.myunical.domain.usecase.GetMonthlyScheduleUseCase
import it.fra.myunical.domain.usecase.GetNextLessonUseCase
import it.fra.myunical.domain.usecase.GetNextMealUseCase
import it.fra.myunical.domain.usecase.GetProfilePhotoUrlUseCase
import it.fra.myunical.domain.usecase.GetStudyPathsUseCase
import it.fra.myunical.domain.usecase.GetTaxSummaryUseCase
import it.fra.myunical.domain.usecase.LoginUseCase
import it.fra.myunical.domain.usecase.LogoutUseCase
import it.fra.myunical.domain.usecase.SaveCredentialsUseCase
import it.fra.myunical.domain.usecase.SetCurrentCareerUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Use cases. Stateless ones are factories; the ones keeping an in-memory cache shared by
 * several screens (career, taxes, timetable) are singletons.
 */
val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::GetAuthStateUseCase)
    factoryOf(::SaveCredentialsUseCase)
    factoryOf(::GetCurrentCareerUseCase)
    factoryOf(::GetCurrentUserInfoUseCase)
    factoryOf(::SetCurrentCareerUseCase)
    factoryOf(::GetProfilePhotoUrlUseCase)
    factoryOf(::GetDegreeProgramsUseCase)
    factoryOf(::GetStudyPathsUseCase)
    factoryOf(::ResolveCdsCodeUseCase)
    factoryOf(::GetCourseDetailUseCase)
    factory { DegreeRegulationFinder(storageDataSource = get()) }
    factoryOf(::GetDegreeRegulationsUseCase)
    factoryOf(::GetCanteenMenuUseCase)
    factory { GetNextMealUseCase(repository = get()) }
    factory { GetNextLessonUseCase(providerRepository = get(), getMonthlySchedule = get()) }

    single { GetCareerDataUseCase(getCurrentCareer = get(), cinecaDataSource = get()) }
    single {
        GetTaxSummaryUseCase(getCurrentUserInfo = get(), getCurrentCareer = get(), taxRemoteDataSource = get())
    }
    singleOf(::GetMonthlyScheduleUseCase)
}
