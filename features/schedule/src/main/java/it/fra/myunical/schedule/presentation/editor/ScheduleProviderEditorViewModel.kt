package it.fra.myunical.schedule.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.model.schedule.TrackedCourse
import it.fra.myunical.domain.repository.ScheduleProviderRepository
import it.fra.myunical.domain.usecase.GetDegreeProgramsUseCase
import it.fra.myunical.domain.usecase.GetStudyPathsUseCase
import it.fra.myunical.schedule.mapper.toDisplayName
import it.fra.myunical.schedule.models.DegreeGroup
import it.fra.myunical.schedule.models.EditorStep
import it.fra.myunical.schedule.models.Loadable
import it.fra.myunical.schedule.models.ProviderEditorIntent
import it.fra.myunical.schedule.models.ProviderEditorUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.UUID

class ScheduleProviderEditorViewModel(
    private val providerId: String?,
    private val providerRepository: ScheduleProviderRepository,
    private val getDegreePrograms: GetDegreeProgramsUseCase,
    private val getStudyPaths: GetStudyPathsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProviderEditorUiState(isEditing = providerId != null))
    val state: StateFlow<ProviderEditorUiState> = _state.asStateFlow()

    private val closeEvents = Channel<Unit>(Channel.CONFLATED)
    /** Emits when the editor has finished (saved or dismissed from the first step). */
    val close: Flow<Unit> = closeEvents.receiveAsFlow()

    private var editedProvider: ScheduleProvider? = null
    private var displayNameEdited = false
    private var studyPathsJob: Job? = null

    init {
        viewModelScope.launch {
            editedProvider = providerId?.let { providerRepository.getProvider(it) }
            loadCatalog()
        }
    }

    fun onIntent(intent: ProviderEditorIntent) {
        when (intent) {
            is ProviderEditorIntent.SelectCategory ->
                _state.update { it.copy(category = intent.category).withFilteredDegrees() }
            is ProviderEditorIntent.UpdateDegreeQuery ->
                _state.update { it.copy(degreeQuery = intent.query).withFilteredDegrees() }
            is ProviderEditorIntent.SelectDegree -> selectDegree(intent.degree)
            is ProviderEditorIntent.SelectStudyPath -> selectStudyPath(intent.pathId)
            is ProviderEditorIntent.ToggleCourse -> _state.update {
                val codes = it.selectedCourseCodes
                it.copy(selectedCourseCodes = if (intent.courseCode in codes) codes - intent.courseCode else codes + intent.courseCode)
            }
            is ProviderEditorIntent.SetYearSelected -> _state.update { state ->
                val yearCodes = state.selectedPath?.academicYears
                    ?.find { it.yearNumber == intent.yearNumber }
                    ?.courses?.map { it.code }.orEmpty()
                state.copy(
                    selectedCourseCodes = if (intent.selected) state.selectedCourseCodes + yearCodes
                    else state.selectedCourseCodes - yearCodes.toSet()
                )
            }
            is ProviderEditorIntent.UpdateCourseQuery -> _state.update { it.copy(courseQuery = intent.query) }
            is ProviderEditorIntent.UpdateDisplayName -> {
                displayNameEdited = true
                _state.update { it.copy(displayName = intent.name) }
            }
            ProviderEditorIntent.Retry -> retry()
            ProviderEditorIntent.Back -> back()
            ProviderEditorIntent.Save -> save()
        }
    }

    private suspend fun loadCatalog() {
        _state.update { it.copy(catalog = Loadable.Loading) }
        val catalog = try {
            getDegreePrograms()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(catalog = Loadable.Error) }
            return
        }
        _state.update { it.copy(catalog = Loadable.Ready(catalog)).withFilteredDegrees() }

        // When editing, jump straight to the course selection of the stored provider
        val provider = editedProvider ?: return
        if (_state.value.selectedDegree != null) return
        val degree = catalog.find { it.cdsCode == provider.cdsCode }
            ?: catalog.find { it.regulationId == provider.regulationId }
            ?: DegreeProgram(
                cdsCode = provider.cdsCode,
                regulationId = provider.regulationId,
                name = provider.degreeName,
                category = _state.value.category
            )
        displayNameEdited = true
        _state.update {
            it.copy(
                category = degree.category,
                displayName = provider.displayName,
                selectedCourseCodes = provider.courses.mapTo(mutableSetOf(), TrackedCourse::code)
            ).withFilteredDegrees()
        }
        selectDegree(degree, preferredPathId = provider.studyPathId)
    }

    private fun selectDegree(degree: DegreeProgram, preferredPathId: Int? = null) {
        val isSameDegree = _state.value.selectedDegree?.regulationId == degree.regulationId
        _state.update {
            it.copy(
                selectedDegree = degree,
                step = EditorStep.PATH,
                studyPaths = if (isSameDegree) it.studyPaths else Loadable.Loading,
                selectedPath = if (isSameDegree) it.selectedPath else null,
                selectedCourseCodes = if (isSameDegree || preferredPathId != null) it.selectedCourseCodes else emptySet(),
                displayName = if (displayNameEdited) it.displayName else degree.name.toDisplayName()
            )
        }
        if (isSameDegree && _state.value.studyPaths is Loadable.Ready) {
            skipSinglePath()
            return
        }
        studyPathsJob?.cancel()
        studyPathsJob = viewModelScope.launch {
            val paths = try {
                getStudyPaths(degree.regulationId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(studyPaths = Loadable.Error) }
                return@launch
            }
            _state.update { it.copy(studyPaths = Loadable.Ready(paths)) }
            val preferred = paths.find { it.id == preferredPathId }
            if (preferred != null) selectStudyPath(preferred.id, keepSelection = true)
            else skipSinglePath()
        }
    }

    private fun skipSinglePath() {
        val paths = (_state.value.studyPaths as? Loadable.Ready)?.value ?: return
        if (paths.size == 1) {
            selectStudyPath(paths.single().id, keepSelection = _state.value.selectedPath?.id == paths.single().id)
        }
    }

    private fun selectStudyPath(pathId: Int, keepSelection: Boolean = false) {
        val paths = (_state.value.studyPaths as? Loadable.Ready)?.value ?: return
        val path = paths.find { it.id == pathId } ?: return
        _state.update {
            val keep = keepSelection || it.selectedPath?.id == path.id
            it.copy(
                selectedPath = path,
                step = EditorStep.COURSES,
                courseQuery = "",
                selectedCourseCodes = if (keep) it.selectedCourseCodes.intersect(path.allCodes()) else emptySet()
            )
        }
    }

    private fun retry() {
        val state = _state.value
        when {
            state.catalog is Loadable.Error -> viewModelScope.launch { loadCatalog() }
            state.studyPaths is Loadable.Error && state.selectedDegree != null ->
                selectDegree(state.selectedDegree, preferredPathId = editedProvider?.studyPathId)
        }
    }

    private fun back() {
        val state = _state.value
        when (state.step) {
            EditorStep.DEGREE -> closeEvents.trySend(Unit)
            EditorStep.PATH -> {
                studyPathsJob?.cancel()
                _state.update { it.copy(step = EditorStep.DEGREE) }
            }
            EditorStep.COURSES -> _state.update {
                it.copy(step = if (it.hasPathStep) EditorStep.PATH else EditorStep.DEGREE)
            }
        }
    }

    private fun save() {
        val state = _state.value
        val degree = state.selectedDegree ?: return
        val path = state.selectedPath ?: return
        if (!state.canSave) return

        val courses = path.academicYears.flatMap { year ->
            year.courses
                .filter { it.code in state.selectedCourseCodes }
                .map { TrackedCourse(code = it.code, name = it.name, yearNumber = year.yearNumber, moduleCodes = it.moduleCodes) }
        }.distinctBy(TrackedCourse::code)

        val provider = ScheduleProvider(
            id = editedProvider?.id ?: UUID.randomUUID().toString(),
            displayName = state.displayName.trim(),
            cdsCode = degree.cdsCode,
            regulationId = degree.regulationId,
            degreeName = degree.name,
            studyPathId = path.id,
            studyPathName = path.name,
            courses = courses,
            // A saved edit is the user's own choice: the career no longer overwrites it
            careerMatId = null
        )
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            providerRepository.upsertProvider(provider)
            closeEvents.send(Unit)
        }
    }

    private fun ProviderEditorUiState.withFilteredDegrees(): ProviderEditorUiState {
        val all = (catalog as? Loadable.Ready)?.value.orEmpty()
        val tokens = degreeQuery.normalized().split(' ').filter(String::isNotBlank)
        val filtered = all.filter { degree ->
            degree.category == category && tokens.all { token ->
                degree.name.normalized().contains(token) ||
                    degree.area.normalized().contains(token) ||
                    degree.departmentName.normalized().contains(token) ||
                    degree.cdsCode.contains(token)
            }
        }
        return copy(
            degreeGroups = filtered
                .groupBy { it.area.ifBlank { "Altro" } }
                .toSortedMap()
                .map { (area, degrees) -> DegreeGroup(area, degrees) }
        )
    }

    private fun StudyPath.allCodes(): Set<String> =
        academicYears.flatMap { it.courses }.mapTo(mutableSetOf()) { it.code }

    private fun String.normalized(): String =
        Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
}
