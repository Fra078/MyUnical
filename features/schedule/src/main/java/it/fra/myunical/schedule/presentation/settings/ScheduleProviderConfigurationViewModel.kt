package it.fra.myunical.schedule.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.degreeexplorer.DegreeCategory
import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import it.fra.myunical.schedule.models.StudyPlanSetupIntent
import it.fra.myunical.schedule.models.StudyPlanSetupUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleProviderConfigurationViewModel(
   private val unicalStorageDataSource: UnicalStorageDataSource
) : ViewModel() {

    private val allDegrees = MutableStateFlow<List<DegreeProgram>>(emptyList())
    private val selectedCategory = MutableStateFlow(DegreeCategory.BACHELOR_AND_SINGLE_CYCLE)
    private val selectedDegreeRegId = MutableStateFlow<Int?>(null)

    private val filteredDegrees = allDegrees.combine(selectedCategory){ allDegrees, selectedCategory ->
        allDegrees.filter { it.category == selectedCategory }
    }

    private val selectedDegree = filteredDegrees.combine(selectedDegreeRegId){ degrees, regId ->
        degrees.find { it.regulationId == regId }
    }

    private val studyPaths = selectedDegree.map {
        it ?: return@map emptyList<StudyPath>()
        val regId = it.regulationId
        unicalStorageDataSource.getStudyPlansForCds(regId)
    }

    private val selectedPathId = MutableStateFlow<String?>(null)

    val state : StateFlow<StudyPlanSetupUiState> = combine(filteredDegrees, selectedCategory, selectedDegree, studyPaths, selectedPathId){ degrees, selectedCategory, selectedDegree, studyPaths, selectedPathId ->
        StudyPlanSetupUiState(
            allDegrees = degrees,
            selectedCategory = selectedCategory,
            selectedDegree = selectedDegree,
            studyPaths = studyPaths,
            selectedPathId = selectedPathId
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StudyPlanSetupUiState())

    init {

        viewModelScope.launch {
            allDegrees.value = unicalStorageDataSource.getAllDegreeCourses()
        }

    }

    fun onIntent(intent: StudyPlanSetupIntent){
        when(intent){
            StudyPlanSetupIntent.SaveStudyPlan -> {}
            is StudyPlanSetupIntent.SelectCategory -> selectedCategory.value = intent.category
            is StudyPlanSetupIntent.SelectDegree -> selectedDegreeRegId.value = intent.degree.regulationId
            is StudyPlanSetupIntent.SelectStudyPath -> selectedPathId.value = intent.pathId
            is StudyPlanSetupIntent.ToggleAcademicYear -> {}
            is StudyPlanSetupIntent.ToggleCourse -> {}
        }
    }

}