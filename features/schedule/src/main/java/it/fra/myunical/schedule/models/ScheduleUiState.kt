package it.fra.myunical.schedule.models

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Immutable
data class ScheduleUiState(
    val month: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    /** `null` while the configured providers are still being read. */
    val providers: List<ProviderFilter>? = null,
    val days: List<ScheduleDay> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false
) {
    val daysWithLessons: Set<LocalDate> = days.mapTo(mutableSetOf(), ScheduleDay::date)
    val isCurrentMonth: Boolean get() = month == YearMonth.from(today)
}

@Immutable
data class ProviderFilter(
    val id: String,
    val name: String,
    val visible: Boolean
)

@Immutable
data class ScheduleDay(
    val date: LocalDate,
    val lessons: List<LessonUiModel>
)

@Immutable
data class LessonUiModel(
    val key: String,
    val providerName: String?,
    val courseCode: String,
    val title: String,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val location: String?,
    val teachers: String?
)

sealed interface ScheduleIntent {
    data object PreviousMonth : ScheduleIntent
    data object NextMonth : ScheduleIntent
    data object GoToToday : ScheduleIntent
    data object Refresh : ScheduleIntent
    data class ToggleProvider(val providerId: String) : ScheduleIntent
}
