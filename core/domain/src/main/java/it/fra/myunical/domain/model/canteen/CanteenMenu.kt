package it.fra.myunical.domain.model.canteen

import java.time.LocalDate

/** Canteen menus of the days covered by the published PDFs, sorted by date. */
data class CanteenMenu(val days: List<CanteenDay>) {
    fun day(date: LocalDate): CanteenDay? = days.firstOrNull { it.date == date }
}

data class CanteenDay(val date: LocalDate, val meals: List<Meal>) {
    fun meal(type: MealType): Meal? = meals.firstOrNull { it.type == type }
}

enum class MealType { LUNCH, DINNER }

data class Meal(val type: MealType, val courses: List<Course>)

enum class CourseType { FIRST, SECOND, SIDE }

data class Course(val type: CourseType, val dishes: List<Dish>)

data class Dish(
    val name: String,
    val line: DishLine,
    val tags: Set<DietTag>,
    /** Frozen product, or it may contain some (the asterisk of the PDF). */
    val frozen: Boolean
)

/** The serving line: two classic dishes, a vegetarian one, a soup or cold dish and a Mediterranean one. */
enum class DishLine { CLASSIC, VEGETARIAN, SOUP, COLD, MEDITERRANEAN }

/** The icons of the menu. */
enum class DietTag { LIGHT, GLUTEN_FREE, PORK, VEGETARIAN, LACTOSE_FREE, VEGAN }

/** Filters offered to the user; a dish passes when it matches all the selected ones. */
enum class DietFilter {
    VEGETARIAN, VEGAN, GLUTEN_FREE, LACTOSE_FREE, NO_PORK;

    fun matches(dish: Dish): Boolean = when (this) {
        // Vegan dishes are not marked vegetarian too in the PDF, and the vegetarian line is not always marked
        VEGETARIAN -> DietTag.VEGETARIAN in dish.tags || DietTag.VEGAN in dish.tags || dish.line == DishLine.VEGETARIAN
        VEGAN -> DietTag.VEGAN in dish.tags
        GLUTEN_FREE -> DietTag.GLUTEN_FREE in dish.tags
        LACTOSE_FREE -> DietTag.LACTOSE_FREE in dish.tags
        NO_PORK -> DietTag.PORK !in dish.tags
    }
}

fun Dish.matches(filters: Set<DietFilter>): Boolean = filters.all { it.matches(this) }
