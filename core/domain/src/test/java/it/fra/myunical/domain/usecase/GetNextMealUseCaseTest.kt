package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.canteen.CanteenDay
import it.fra.myunical.domain.model.canteen.CanteenMenu
import it.fra.myunical.domain.model.canteen.Course
import it.fra.myunical.domain.model.canteen.CourseType
import it.fra.myunical.domain.model.canteen.DietFilter
import it.fra.myunical.domain.model.canteen.DietTag
import it.fra.myunical.domain.model.canteen.Dish
import it.fra.myunical.domain.model.canteen.DishLine
import it.fra.myunical.domain.model.canteen.Meal
import it.fra.myunical.domain.model.canteen.MealType
import it.fra.myunical.domain.model.canteen.matches
import it.fra.myunical.domain.usecase.GetNextMealUseCase.Companion.nextMeal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class GetNextMealUseCaseTest {

    private val monday = LocalDate.of(2026, 11, 9)

    private fun dish(name: String, vararg tags: DietTag) = Dish(name, DishLine.CLASSIC, tags.toSet(), frozen = false)

    private fun meal(type: MealType) = Meal(type, listOf(Course(CourseType.FIRST, listOf(dish("$type")))))

    private val menu = CanteenMenu(
        listOf(
            CanteenDay(monday, listOf(meal(MealType.LUNCH), meal(MealType.DINNER))),
            CanteenDay(monday.plusDays(1), listOf(meal(MealType.LUNCH), meal(MealType.DINNER)))
        )
    )

    private fun at(day: LocalDate, hour: Int, minute: Int = 0) = LocalDateTime.of(day, java.time.LocalTime.of(hour, minute))

    @Test
    fun `lunch until three, then dinner`() {
        assertEquals(MealType.LUNCH, menu.nextMeal(at(monday, 8))?.meal?.type)
        assertEquals(MealType.LUNCH, menu.nextMeal(at(monday, 14, 59))?.meal?.type)
        assertEquals(MealType.DINNER, menu.nextMeal(at(monday, 15))?.meal?.type)
    }

    @Test
    fun `after dinner the next day's lunch`() {
        val next = menu.nextMeal(at(monday, 22))
        assertEquals(monday.plusDays(1), next?.date)
        assertEquals(MealType.LUNCH, next?.meal?.type)
    }

    @Test
    fun `nothing after the last menu`() {
        assertNull(menu.nextMeal(at(monday.plusDays(1), 22)))
        assertNull(menu.nextMeal(at(monday.plusDays(5), 10)))
    }

    @Test
    fun `days before the menus start at the first lunch`() {
        val next = menu.nextMeal(at(monday.minusDays(2), 20))
        assertEquals(monday, next?.date)
        assertEquals(MealType.LUNCH, next?.meal?.type)
    }

    @Test
    fun `diet filters`() {
        val vegan = dish("Tofu", DietTag.VEGAN, DietTag.GLUTEN_FREE)
        val carbonara = dish("Carbonara", DietTag.PORK)
        val cheese = dish("Frittata", DietTag.VEGETARIAN)

        assertTrue(vegan.matches(setOf(DietFilter.VEGETARIAN, DietFilter.GLUTEN_FREE)))
        assertTrue(cheese.matches(setOf(DietFilter.VEGETARIAN, DietFilter.NO_PORK)))
        assertFalse(cheese.matches(setOf(DietFilter.VEGAN)))
        assertFalse(carbonara.matches(setOf(DietFilter.NO_PORK)))
        assertTrue(carbonara.matches(emptySet()))
        assertTrue(Dish("Mezze maniche ai formaggi", DishLine.VEGETARIAN, emptySet(), false).matches(setOf(DietFilter.VEGETARIAN)))
    }
}
