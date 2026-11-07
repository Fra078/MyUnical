package it.fra.myunical.today.mapper

import it.fra.myunical.domain.model.canteen.CourseType
import it.fra.myunical.domain.model.canteen.DishLine
import it.fra.myunical.domain.model.canteen.MealType
import it.fra.myunical.domain.usecase.NextMeal
import it.fra.myunical.today.models.CanteenCardState
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val HIGHLIGHTS = 3

/** The classic, vegetarian and Mediterranean first courses tell what the meal is like. */
fun NextMeal?.toCanteenState(today: LocalDate): CanteenCardState {
    if (this == null) return CanteenCardState.Unavailable
    val day = when (date) {
        today -> "Oggi"
        today.plusDays(1) -> "Domani"
        else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ITALIAN).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }
    val firsts = meal.courses.firstOrNull { it.type == CourseType.FIRST }?.dishes.orEmpty()
    val highlights = listOf(DishLine.CLASSIC, DishLine.VEGETARIAN, DishLine.MEDITERRANEAN)
        .mapNotNull { line -> firsts.firstOrNull { it.line == line }?.name }
        .ifEmpty { firsts.map { it.name } }
        .take(HIGHLIGHTS)
    return CanteenCardState.Available(
        title = "$day a ${if (meal.type == MealType.LUNCH) "pranzo" else "cena"}",
        highlights = highlights,
        dishCount = meal.courses.sumOf { it.dishes.size }
    )
}
