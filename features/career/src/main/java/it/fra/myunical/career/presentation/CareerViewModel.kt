package it.fra.myunical.career.presentation

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.career.mapper.mapToUiModel
import it.fra.myunical.career.models.CareerMetrics
import it.fra.myunical.career.models.ExamFilter
import it.fra.myunical.career.models.ExamRecord
import it.fra.myunical.career.models.ExamSortOption
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.general.ExamResult
import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.CinecaRemoteDataSource
import it.fra.myunical.domain.usecase.GetCurrentStudyPlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CareerViewModel(
    val getCurrentStudyPlan: GetCurrentStudyPlanUseCase
) : ViewModel() {

    private val studyPlan = getCurrentStudyPlan()

    val stats = getCurrentStudyPlan().map(::calculateCareerMetrics)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val examFilter : StateFlow<ExamFilter>
        field = MutableStateFlow(ExamFilter.ALL)

    val sortOption : StateFlow<ExamSortOption>
        field = MutableStateFlow(ExamSortOption.ACADEMIC_YEAR_DESC)

    val examList = studyPlan.combine(examFilter){ courses, filter ->
        when(filter){
            ExamFilter.ALL -> courses
            ExamFilter.PASSED -> courses.filter(Course::isPassed)
            ExamFilter.PENDING -> courses.filterNot(Course::isPassed)
        }
    }.combine(sortOption){ courses, sortOption ->
        when(sortOption){
            ExamSortOption.CHRONOLOGICAL_DESC -> courses.sortedBy {
                val outcome = it.outcome
                outcome?.date ?: -1
            }
            ExamSortOption.GRADE_DESC -> courses.sortedByDescending {
                when (val result = it.outcome?.result) {
                    null -> -10
                    is ExamResult.Judgment -> -1
                    ExamResult.NotEvaluated -> -2
                    is ExamResult.Graded -> result.grade + if (result.hasHonors) 1 else 0
                }
            }
            ExamSortOption.ACADEMIC_YEAR_DESC -> courses.sortedBy { it.attendanceAcademicYear }
            ExamSortOption.ALPHABETICAL_ASC -> courses.sortedBy { it.name }
        }
    }.map { it.map(Course::mapToUiModel) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setFilter(filter: ExamFilter){
        examFilter.value = filter
    }

    fun setSortOption(option: ExamSortOption){
        sortOption.value = option
    }
    fun calculateCareerMetrics(courses: List<Course>): CareerMetrics {
        val passedExams = courses.filter(Course::isPassed)
        val earnedCredits = passedExams.sumOf(Course::credits)

        // Pre-extract grade pairs to avoid repetitive unsafe casts
        val gradedExams = passedExams.mapNotNull { course ->
            val gradedResult = course.outcome?.result as? ExamResult.Graded
            gradedResult?.let { course to it.grade }
        }

        val gradedCredits = gradedExams.sumOf { (course, _) -> course.credits }
        val weightedSum = gradedExams.sumOf { (course, grade) -> course.credits * grade }
        val gradeSum = gradedExams.sumOf { (_, grade) -> grade }

        val weightedAverage = if (gradedCredits > 0) {
            weightedSum.toDouble() / gradedCredits
        } else {
            0.0
        }

        val arithmeticAverage = if (gradedExams.isNotEmpty()) {
            gradeSum.toDouble() / gradedExams.size
        } else {
            0.0
        }

        return CareerMetrics(
            earnedCredits = earnedCredits,
            totalCredits = 180,
            weightedAverage = weightedAverage,
            arithmeticAverage = arithmeticAverage,
            graduationBaseGrade = weightedAverage * 110.0 / 30.0
        )
    }


}