package it.fra.myunical.canteen.mapper

import it.fra.myunical.canteen.models.CourseUiModel
import it.fra.myunical.canteen.models.DayOption
import it.fra.myunical.canteen.models.DishUiModel
import it.fra.myunical.domain.model.canteen.CourseType
import it.fra.myunical.domain.model.canteen.DietFilter
import it.fra.myunical.domain.model.canteen.DietTag
import it.fra.myunical.domain.model.canteen.DishLine
import it.fra.myunical.domain.model.canteen.Meal
import it.fra.myunical.domain.model.canteen.MealType
import it.fra.myunical.domain.model.canteen.matches
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val ItalianLocale: Locale = Locale.ITALIAN
private val DayMonthFormatter = DateTimeFormatter.ofPattern("d MMMM", ItalianLocale)

fun LocalDate.toDayOption(today: LocalDate) = DayOption(
    date = this,
    weekday = dayOfWeek.getDisplayName(TextStyle.SHORT, ItalianLocale).take(3),
    day = dayOfMonth.toString(),
    isToday = this == today
)

fun LocalDate.dayMonth(): String = format(DayMonthFormatter)

/** Courses with the dishes passing [filters]; empty courses are dropped. */
fun Meal.toCourses(filters: Set<DietFilter>): List<CourseUiModel> = courses.mapNotNull { course ->
    val dishes = course.dishes.filter { it.matches(filters) }
    if (dishes.isEmpty()) return@mapNotNull null
    CourseUiModel(
        type = course.type,
        title = course.type.title,
        dishes = dishes.map { DishUiModel(it.name, it.line, DietTag.entries.filter(it.tags::contains), it.frozen) }
    )
}

val CourseType.title: String
    get() = when (this) {
        CourseType.FIRST -> "Primi piatti"
        CourseType.SECOND -> "Secondi piatti"
        CourseType.SIDE -> "Contorni"
    }

val MealType.label: String
    get() = when (this) {
        MealType.LUNCH -> "Pranzo"
        MealType.DINNER -> "Cena"
    }

val DishLine.label: String
    get() = when (this) {
        DishLine.CLASSIC -> "Classica"
        DishLine.VEGETARIAN -> "Vegetariana"
        DishLine.SOUP -> "Minestra"
        DishLine.COLD -> "Piatto freddo"
        DishLine.MEDITERRANEAN -> "Mediterranea"
    }

val DietTag.label: String
    get() = when (this) {
        DietTag.LIGHT -> "Light"
        DietTag.GLUTEN_FREE -> "Senza glutine"
        DietTag.PORK -> "Maiale"
        DietTag.VEGETARIAN -> "Vegetariano"
        DietTag.LACTOSE_FREE -> "Senza lattosio"
        DietTag.VEGAN -> "Vegano"
    }

val DietFilter.label: String
    get() = when (this) {
        DietFilter.VEGETARIAN -> "Vegetariano"
        DietFilter.VEGAN -> "Vegano"
        DietFilter.GLUTEN_FREE -> "Senza glutine"
        DietFilter.LACTOSE_FREE -> "Senza lattosio"
        DietFilter.NO_PORK -> "Senza maiale"
    }
