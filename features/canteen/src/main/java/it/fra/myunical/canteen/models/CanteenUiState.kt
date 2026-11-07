package it.fra.myunical.canteen.models

import it.fra.myunical.domain.model.canteen.CourseType
import it.fra.myunical.domain.model.canteen.DietFilter
import it.fra.myunical.domain.model.canteen.DietTag
import it.fra.myunical.domain.model.canteen.DishLine
import it.fra.myunical.domain.model.canteen.MealType
import java.time.LocalDate

data class CanteenUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val days: List<DayOption> = emptyList(),
    val selectedDate: LocalDate? = null,
    val meal: MealType = MealType.LUNCH,
    val filters: Set<DietFilter> = emptySet(),
    /** null when there is no menu for the selected day. */
    val courses: List<CourseUiModel>? = null,
    /** Dishes of the meal hidden by the filters. */
    val hiddenCount: Int = 0,
    /** Last day with a menu, "15 novembre"; null without menus. */
    val coveredUntil: String? = null
)

data class DayOption(
    val date: LocalDate,
    val weekday: String,
    val day: String,
    val isToday: Boolean
)

data class CourseUiModel(val type: CourseType, val title: String, val dishes: List<DishUiModel>)

data class DishUiModel(
    val name: String,
    val line: DishLine,
    val tags: List<DietTag>,
    val frozen: Boolean
)

sealed interface CanteenIntent {
    data class SelectDay(val date: LocalDate) : CanteenIntent
    data class SelectMeal(val meal: MealType) : CanteenIntent
    data class ToggleFilter(val filter: DietFilter) : CanteenIntent
    data object Retry : CanteenIntent
}
