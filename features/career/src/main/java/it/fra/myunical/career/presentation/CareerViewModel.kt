package it.fra.myunical.career.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.career.mapper.toExamGroups
import it.fra.myunical.career.mapper.toOption
import it.fra.myunical.career.mapper.toUiModel
import it.fra.myunical.career.models.CareerIntent
import it.fra.myunical.career.models.CareerSection
import it.fra.myunical.career.models.CareerUiState
import it.fra.myunical.career.models.ExamFilter
import it.fra.myunical.career.models.ExamSortOption
import it.fra.myunical.domain.model.career.CareerData
import it.fra.myunical.domain.usecase.GetCareerDataUseCase
import it.fra.myunical.domain.usecase.GetCurrentUserInfoUseCase
import it.fra.myunical.domain.usecase.SetCurrentCareerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CareerViewModel(
    private val getCareerData: GetCareerDataUseCase,
    private val setCurrentCareer: SetCurrentCareerUseCase,
    getCurrentUserInfo: GetCurrentUserInfoUseCase
) : ViewModel() {

    private data class Controls(
        val section: CareerSection = CareerSection.LIBRETTO,
        val filter: ExamFilter = ExamFilter.ALL,
        val sort: ExamSortOption = ExamSortOption.COURSE_YEAR,
        val query: String = ""
    )

    /** Last successful data plus whether the latest load failed. */
    private data class LoadState(val data: CareerData? = null, val hasError: Boolean = false, val loaded: Boolean = false)

    private val controls = MutableStateFlow(Controls())
    private val isRefreshing = MutableStateFlow(false)

    private val loadState = getCareerData()
        .onEach { isRefreshing.value = false }
        .scan(LoadState()) { previous, result ->
            result.fold(
                onSuccess = { LoadState(data = it, loaded = true) },
                onFailure = { previous.copy(hasError = true, loaded = true) }
            )
        }

    private val careers = getCurrentUserInfo().map { it.careers }.onStart { emit(emptyList()) }

    val state: StateFlow<CareerUiState> = combine(loadState, careers, controls, isRefreshing) { load, careers, controls, refreshing ->
        val data = load.data
        CareerUiState(
            isLoading = !load.loaded,
            isRefreshing = refreshing,
            hasError = load.hasError,
            careerName = careers.firstOrNull { it.matId == data?.career?.matId }?.let { it.toOption(it.matId).name },
            careers = careers.map { it.toOption(data?.career?.matId) },
            overview = data?.overview?.toUiModel(),
            section = controls.section,
            filter = controls.filter,
            sort = controls.sort,
            query = controls.query,
            examGroups = data?.toExamGroups(controls.filter, controls.sort, controls.query).orEmpty(),
            sessions = data?.let { d -> d.upcomingSessions?.map { it.toUiModel(d.today) } } ?: emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CareerUiState())

    fun onIntent(intent: CareerIntent) {
        when (intent) {
            is CareerIntent.SelectSection -> controls.update { it.copy(section = intent.section) }
            is CareerIntent.SetFilter -> controls.update { it.copy(filter = intent.filter) }
            is CareerIntent.SetSort -> controls.update { it.copy(sort = intent.sort) }
            is CareerIntent.SetQuery -> controls.update { it.copy(query = intent.query) }
            is CareerIntent.SwitchCareer -> viewModelScope.launch { setCurrentCareer(intent.matId) }
            CareerIntent.Refresh -> {
                isRefreshing.value = true
                getCareerData.refresh()
            }
        }
    }
}
