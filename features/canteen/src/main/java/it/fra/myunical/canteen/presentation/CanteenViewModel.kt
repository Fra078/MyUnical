package it.fra.myunical.canteen.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.canteen.mapper.dayMonth
import it.fra.myunical.canteen.mapper.toCourses
import it.fra.myunical.canteen.mapper.toDayOption
import it.fra.myunical.canteen.models.CanteenIntent
import it.fra.myunical.canteen.models.CanteenUiState
import it.fra.myunical.domain.model.canteen.CanteenMenu
import it.fra.myunical.domain.model.canteen.MealType
import it.fra.myunical.domain.usecase.GetCanteenMenuUseCase
import it.fra.myunical.domain.usecase.GetNextMealUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

class CanteenViewModel(
    private val getCanteenMenu: GetCanteenMenuUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CanteenUiState())
    val state: StateFlow<CanteenUiState> = _state.asStateFlow()

    private var menu: CanteenMenu? = null

    init {
        load()
    }

    fun onIntent(intent: CanteenIntent) {
        when (intent) {
            is CanteenIntent.SelectDay -> show { it.copy(selectedDate = intent.date) }
            is CanteenIntent.SelectMeal -> show { it.copy(meal = intent.meal) }
            is CanteenIntent.ToggleFilter -> show {
                it.copy(filters = if (intent.filter in it.filters) it.filters - intent.filter else it.filters + intent.filter)
            }
            CanteenIntent.Retry -> load()
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, hasError = false) }
        viewModelScope.launch {
            val loaded = try {
                getCanteenMenu()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, hasError = true) }
                return@launch
            }
            menu = loaded
            val now = LocalDateTime.now()
            val today = now.toLocalDate()
            // From the Monday of this week, so the week is complete; today is always there
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val dates = (loaded.days.map { it.date }.filter { !it.isBefore(weekStart) } + today).distinct().sorted()
            val afterDinner = GetNextMealUseCase.mealAt(now.toLocalTime()) == null
            val start = if (afterDinner && loaded.day(today.plusDays(1)) != null) today.plusDays(1) else today
            show {
                it.copy(
                    isLoading = false,
                    days = dates.map { date -> date.toDayOption(today) },
                    selectedDate = start,
                    meal = if (start == today) GetNextMealUseCase.mealAt(now.toLocalTime()) ?: MealType.LUNCH else MealType.LUNCH,
                    coveredUntil = loaded.days.lastOrNull()?.date?.dayMonth()
                )
            }
        }
    }

    /** Applies [change] and recomputes the dishes shown. */
    private fun show(change: (CanteenUiState) -> CanteenUiState) {
        _state.update { current ->
            val next = change(current)
            val meal = next.selectedDate?.let { menu?.day(it) }?.meal(next.meal)
            val all = meal?.courses?.sumOf { it.dishes.size } ?: 0
            val courses = meal?.toCourses(next.filters)
            next.copy(courses = courses, hiddenCount = all - (courses?.sumOf { it.dishes.size } ?: 0))
        }
    }
}
