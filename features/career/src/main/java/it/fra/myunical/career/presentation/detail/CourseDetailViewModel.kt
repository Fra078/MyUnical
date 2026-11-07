package it.fra.myunical.career.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.career.mapper.detailSubtitle
import it.fra.myunical.career.mapper.toExamRecord
import it.fra.myunical.career.mapper.toOptions
import it.fra.myunical.career.mapper.toUiModel
import it.fra.myunical.career.models.CatalogState
import it.fra.myunical.career.models.CourseDetailUiState
import it.fra.myunical.domain.model.career.BookingState
import it.fra.myunical.domain.model.syllabus.CourseLookup
import it.fra.myunical.domain.repository.CourseCatalogRepository
import it.fra.myunical.domain.usecase.GetCourseDetailUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class CourseDetailViewModel(
    private val courseRowId: Long,
    private val getCourseDetail: GetCourseDetailUseCase,
    private val catalog: CourseCatalogRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CourseDetailUiState>(CourseDetailUiState.Loading)
    val state: StateFlow<CourseDetailUiState> = _state.asStateFlow()

    private var lookup: CourseLookup? = null
    private var syllabusJob: Job? = null

    init {
        load()
    }

    /** Loads what failed: the libretto row, or only the public page. */
    fun retry() {
        val loaded = _state.value as? CourseDetailUiState.Loaded
        when {
            loaded == null -> load()
            loaded.units.isEmpty() -> lookup?.let { viewModelScope.launch { loadUnits(it) } }
            else -> selectUnit(loaded.selectedUnit)
        }
    }

    fun selectUnit(index: Int) {
        val loaded = _state.value as? CourseDetailUiState.Loaded ?: return
        val unit = loaded.units.getOrNull(index) ?: return
        _state.value = loaded.copy(selectedUnit = index, catalog = CatalogState.Loading)
        syllabusJob?.cancel()
        syllabusJob = viewModelScope.launch {
            val catalogState = attempt { CatalogState.Loaded(catalog.getSyllabus(unit.activityId).toUiModel()) }
            updateLoaded { it.copy(catalog = catalogState) }
        }
    }

    private fun load() {
        _state.value = CourseDetailUiState.Loading
        viewModelScope.launch {
            val detail = try {
                getCourseDetail(courseRowId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = CourseDetailUiState.Error
                return@launch
            }
            val today = LocalDate.now()
            val sessions = detail.upcomingSessions.map { it.toUiModel(today) }
            _state.value = CourseDetailUiState.Loaded(
                exam = detail.course.toExamRecord(sessions.count { it.state == BookingState.OPEN }),
                subtitle = detail.course.detailSubtitle(),
                sessions = if (detail.course.isPassed) emptyList() else sessions
            )
            lookup = detail.lookup
            loadUnits(detail.lookup)
        }
    }

    private suspend fun loadUnits(lookup: CourseLookup) {
        updateLoaded { it.copy(catalog = CatalogState.Loading) }
        val units = try {
            catalog.findUnits(lookup)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            updateLoaded { it.copy(catalog = CatalogState.Error) }
            return
        }
        if (units.isEmpty()) {
            updateLoaded { it.copy(catalog = CatalogState.NotFound) }
            return
        }
        updateLoaded { it.copy(units = units.toOptions(), selectedUnit = 0) }
        selectUnit(0)
    }

    private inline fun updateLoaded(transform: (CourseDetailUiState.Loaded) -> CourseDetailUiState.Loaded) {
        _state.update { if (it is CourseDetailUiState.Loaded) transform(it) else it }
    }

    private inline fun attempt(block: () -> CatalogState): CatalogState = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: NoSuchElementException) {
        CatalogState.NotFound
    } catch (e: Exception) {
        CatalogState.Error
    }
}
