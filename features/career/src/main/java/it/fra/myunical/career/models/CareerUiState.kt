package it.fra.myunical.career.models

import it.fra.myunical.domain.model.career.BookingState

data class CareerUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** Last load failed; previous data, if any, is still shown. */
    val hasError: Boolean = false,
    val careerName: String? = null,
    val careers: List<CareerOption> = emptyList(),
    val overview: OverviewUiModel? = null,
    val section: CareerSection = CareerSection.LIBRETTO,
    val filter: ExamFilter = ExamFilter.ALL,
    val sort: ExamSortOption = ExamSortOption.COURSE_YEAR,
    val query: String = "",
    val examGroups: List<ExamGroup> = emptyList(),
    /** null when Esse3 did not return the sessions. */
    val sessions: List<SessionUiModel>? = emptyList()
) {
    val hasContent: Boolean get() = overview != null
    val canSwitchCareer: Boolean get() = careers.size > 1
    val openSessionsCount: Int get() = sessions.orEmpty().count { it.state == BookingState.OPEN }
}

enum class CareerSection(val label: String) { LIBRETTO("Libretto"), SESSIONS("Appelli") }

data class CareerOption(
    val matId: Int,
    val name: String,
    val status: String,
    val matricola: String,
    val isSelected: Boolean
)

data class OverviewUiModel(
    val earnedCredits: Int,
    val requiredCredits: Int,
    val progress: Float,
    val weightedAverage: String,
    val arithmeticAverage: String,
    val graduationBase: String,
    val passedLabel: String
)

data class ExamGroup(
    /** "1° anno"; null for a flat list (sorting other than by course year). */
    val label: String?,
    val exams: List<ExamRecord>
)

data class SessionUiModel(
    val id: String,
    val courseName: String,
    val description: String,
    val day: String,
    val month: String,
    val weekday: String,
    val time: String?,
    val president: String?,
    val state: BookingState,
    /** "Prenotazioni aperte fino al 10/11", "Apre il 19/10"... */
    val stateLabel: String,
    /** Booking details when booked: "Posizione 3 · Aula MT4". */
    val bookingDetail: String?
)

sealed interface CareerIntent {
    data class SelectSection(val section: CareerSection) : CareerIntent
    data class SetFilter(val filter: ExamFilter) : CareerIntent
    data class SetSort(val sort: ExamSortOption) : CareerIntent
    data class SetQuery(val query: String) : CareerIntent
    data class SwitchCareer(val matId: Int) : CareerIntent
    data object Refresh : CareerIntent
}
