package it.fra.myunical.today.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.sources.CinecaRemoteDataSource
import it.fra.myunical.domain.sources.NewsRemoteDataSource
import it.fra.myunical.domain.usecase.GetCurrentCareerUseCase
import it.fra.myunical.domain.usecase.GetCurrentUserInfoUseCase
import it.fra.myunical.today.models.NoticeUiModel
import it.fra.myunical.today.models.StudentHeaderInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.text.category

class TodayScreenViewModel(
    private val newsSource: NewsRemoteDataSource,
    private val getCurrentUserInfo: GetCurrentUserInfoUseCase,
    private val getCurrentCareer: GetCurrentCareerUseCase,
    private val cinecaSource: CinecaRemoteDataSource
) : ViewModel() {

    val newsList = flow {
        emit(newsSource.getLatestNews())
    }.map { list ->
        list.map {
            NoticeUiModel(
                id = it.id,
                title = it.title,
                category = it.category,
                date = it.date,
                summary = it.summary,
                imageUrl = it.imageUrl
            )
        }
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val studentInfo : StateFlow<StudentHeaderInfo?> = combine(getCurrentUserInfo(), getCurrentCareer().filterNotNull()){ user, career ->
        val photoUrl = cinecaSource.getUserPhotoEndpoint(user.persId)
        StudentHeaderInfo(
            firstName = user.firstName,
            matricola = career.matricola,
            avatarUrl = photoUrl
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)


}