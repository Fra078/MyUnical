package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.canteen.CanteenDay
import it.fra.myunical.domain.model.canteen.CanteenMenu
import it.fra.myunical.domain.model.canteen.Meal
import it.fra.myunical.domain.model.canteen.MealType
import it.fra.myunical.domain.repository.CanteenMenuRepository
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class GetCanteenMenuUseCase(private val repository: CanteenMenuRepository) {
    suspend operator fun invoke(): CanteenMenu = repository.getMenu()
}

/** The next meal served at the canteen, with its day. */
data class NextMeal(val date: LocalDate, val meal: Meal)

/** Lunch until 15:00, dinner until 21:30, then the following days; null past the last menu. */
class GetNextMealUseCase(
    private val repository: CanteenMenuRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    suspend operator fun invoke(): NextMeal? = repository.getMenu().nextMeal(LocalDateTime.now(clock))

    companion object {
        val LUNCH_END: LocalTime = LocalTime.of(15, 0)
        val DINNER_END: LocalTime = LocalTime.of(21, 30)

        fun mealAt(time: LocalTime): MealType? = when {
            time < LUNCH_END -> MealType.LUNCH
            time < DINNER_END -> MealType.DINNER
            else -> null
        }

        internal fun CanteenMenu.nextMeal(now: LocalDateTime): NextMeal? {
            val today = now.toLocalDate()
            val first = mealAt(now.toLocalTime())
            return days.asSequence()
                .filter { !it.date.isBefore(today) }
                .flatMap { day -> day.mealsFrom(if (day.date == today) first else MealType.LUNCH) }
                .firstOrNull()
        }

        private fun CanteenDay.mealsFrom(first: MealType?): Sequence<NextMeal> =
            meals.asSequence()
                .filter { first != null && it.type >= first && it.courses.any { course -> course.dishes.isNotEmpty() } }
                .sortedBy { it.type }
                .map { NextMeal(date, it) }
    }
}
