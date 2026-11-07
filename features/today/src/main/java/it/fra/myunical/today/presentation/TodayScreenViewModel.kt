package it.fra.myunical.today.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.degreeexplorer.DegreeRegulations
import it.fra.myunical.domain.model.general.statusLabel
import it.fra.myunical.domain.model.news.NewsSource
import it.fra.myunical.domain.model.news.NoticeModel
import it.fra.myunical.domain.repository.NewsRepository
import it.fra.myunical.domain.usecase.GetCareerDataUseCase
import it.fra.myunical.domain.usecase.GetCurrentCareerUseCase
import it.fra.myunical.domain.usecase.GetCurrentUserInfoUseCase
import it.fra.myunical.domain.usecase.GetDegreeRegulationsUseCase
import it.fra.myunical.domain.usecase.GetNextLessonUseCase
import it.fra.myunical.domain.usecase.GetNextMealUseCase
import it.fra.myunical.domain.usecase.GetProfilePhotoUrlUseCase
import it.fra.myunical.domain.usecase.GetStudentDepartmentUseCase
import it.fra.myunical.domain.usecase.GetTaxSummaryUseCase
import it.fra.myunical.domain.usecase.LogoutUseCase
import it.fra.myunical.domain.usecase.SetCurrentCareerUseCase
import it.fra.myunical.today.mapper.toCanteenState
import it.fra.myunical.today.mapper.toSnapshotState
import it.fra.myunical.today.mapper.toTuitionState
import it.fra.myunical.today.mapper.toUiState
import it.fra.myunical.today.models.CanteenCardState
import it.fra.myunical.today.models.CareerSnapshotState
import it.fra.myunical.today.models.DepartmentNotices
import it.fra.myunical.today.models.NoticeUiModel
import it.fra.myunical.today.models.ProfileCareer
import it.fra.myunical.today.models.ProfileUiState
import it.fra.myunical.today.models.StudentHeaderInfo
import it.fra.myunical.today.models.TuitionState
import it.fra.myunical.today.models.UpcomingCommitmentState
import it.fra.myunical.ui.format.toRelativeLabel
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayScreenViewModel(
    newsRepository: NewsRepository,
    getCurrentUserInfo: GetCurrentUserInfoUseCase,
    getCurrentCareer: GetCurrentCareerUseCase,
    private val getProfilePhotoUrl: GetProfilePhotoUrlUseCase,
    getNextLesson: GetNextLessonUseCase,
    getTaxSummary: GetTaxSummaryUseCase,
    getCareerData: GetCareerDataUseCase,
    getNextMeal: GetNextMealUseCase,
    private val getDegreeRegulations: GetDegreeRegulationsUseCase,
    private val getStudentDepartment: GetStudentDepartmentUseCase,
    private val setCurrentCareer: SetCurrentCareerUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    val upcomingCommitment: StateFlow<UpcomingCommitmentState> = getNextLesson()
        .map { it.toUiState(LocalDateTime.now()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UpcomingCommitmentState.Loading)

    val tuitionState: StateFlow<TuitionState> = getTaxSummary()
        .map { it.toTuitionState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TuitionState.Loading)

    val careerSnapshot: StateFlow<CareerSnapshotState> = getCareerData()
        .map { it.toSnapshotState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CareerSnapshotState.Loading)

    val canteen: StateFlow<CanteenCardState> = flow {
        val next = try {
            getNextMeal()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        emit(next.toCanteenState(LocalDate.now()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CanteenCardState.Loading)

    /** Latest news for the carousel; null while loading, empty when unavailable. */
    val newsList: StateFlow<List<NoticeUiModel>?> = flow {
        val page = runCatching { newsRepository.getPage(NewsSource.University, 1) }.getOrNull()
        emit(page?.items.orEmpty().take(HOME_NEWS_COUNT).map { it.toNoticeUiModel() })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Latest notices of the student's department; null while loading or when there is nothing to show. */
    val departmentNotices: StateFlow<DepartmentNotices?> = getCurrentCareer().filterNotNull()
        .map { it.matId }
        .distinctUntilChanged()
        .map {
            try {
                val department = getStudentDepartment() ?: return@map null
                val page = newsRepository.getPage(NewsSource.Department(department.host, noticesOnly = true), 1)
                page.items.take(HOME_DEPARTMENT_NOTICES).map { it.toNoticeUiModel() }
                    .takeIf { it.isNotEmpty() }
                    ?.let { DepartmentNotices(department.shortName, it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val studentInfo : StateFlow<StudentHeaderInfo?> = combine(getCurrentUserInfo(), getCurrentCareer().filterNotNull()){ user, career ->
        val photoUrl = getProfilePhotoUrl(user.persId)
        StudentHeaderInfo(
            firstName = user.firstName.capitalizeWords(),
            matricola = career.matricola,
            avatarUrl = photoUrl
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Content of the profile menu opened from the avatar. */
    val profile: StateFlow<ProfileUiState?> = combine(getCurrentUserInfo(), getCurrentCareer().filterNotNull()) { user, career ->
        ProfileUiState(
            fullName = "${user.firstName} ${user.lastName}".capitalizeWords(),
            matricola = career.matricola,
            courseName = career.name.capitalizeWords(),
            avatarUrl = getProfilePhotoUrl(user.persId),
            careers = user.careers.map {
                ProfileCareer(
                    matId = it.matId,
                    name = it.name.capitalizeWords(),
                    status = it.statusLabel,
                    matricola = it.matricola,
                    isSelected = it.matId == career.matId
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Regulations of the current career's degree course; null while loading or when unavailable. */
    val regulations: StateFlow<DegreeRegulations?> = getCurrentCareer().filterNotNull()
        .map { it.matId }
        .distinctUntilChanged()
        .map {
            try {
                getDegreeRegulations()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectCareer(matId: Int) {
        viewModelScope.launch { setCurrentCareer(matId) }
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }

    // Esse3 names are upper case: "FRANCESCO" -> "Francesco"
    private fun String.capitalizeWords(): String =
        lowercase(Locale.ITALIAN).split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.titlecase(Locale.ITALIAN) } }

    private fun NoticeModel.toNoticeUiModel() = NoticeUiModel(
        id = id,
        title = title,
        category = category,
        date = publishedAt?.toLocalDate()?.toRelativeLabel().orEmpty(),
        summary = summary,
        imageUrl = imageUrl
    )

    private companion object {
        const val HOME_NEWS_COUNT = 5
        const val HOME_DEPARTMENT_NOTICES = 3
    }
}
