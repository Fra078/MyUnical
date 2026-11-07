package it.fra.myunical.datastore.repository

import it.fra.myunical.domain.model.canteen.CourseType
import it.fra.myunical.domain.model.canteen.DietTag
import it.fra.myunical.domain.model.canteen.DishLine
import it.fra.myunical.domain.model.canteen.MealType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CanteenMenuRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun repository(text: String) = CanteenMenuRepositoryImpl(json) { text.byteInputStream() }

    @Test
    fun `unknown values are skipped`() = runBlocking {
        val menu = repository(
            """
            {"version": 2, "extra": true, "days": [
              {"date": "2026-11-09", "meals": [
                {"type": "BREAKFAST", "courses": []},
                {"type": "LUNCH", "courses": [
                  {"type": "DESSERT", "dishes": [{"name": "Torta", "line": "CLASSIC"}]},
                  {"type": "FIRST", "dishes": [
                    {"name": "Pasta e ceci", "line": "STREET_FOOD", "tags": ["VEGAN", "SPICY"], "frozen": true}
                  ]}
                ]}
              ]},
              {"date": "not a date", "meals": []}
            ]}
            """.trimIndent()
        ).getMenu()

        val day = menu.days.single()
        val meal = day.meals.single()
        assertEquals(MealType.LUNCH, meal.type)
        val course = meal.courses.single()
        assertEquals(CourseType.FIRST, course.type)
        val dish = course.dishes.single()
        assertEquals(DishLine.CLASSIC, dish.line)
        assertEquals(setOf(DietTag.VEGAN), dish.tags)
        assertTrue(dish.frozen)
    }

    /** Checks the file shipped with the app, regenerated at each menu update. */
    @Test
    fun `the bundled menu is complete`() = runBlocking {
        val asset = File("src/main/assets/${CanteenMenuRepositoryImpl.ASSET}")
        val menu = repository(asset.readText()).getMenu()

        assertTrue(menu.days.isNotEmpty())
        assertEquals(menu.days.map { it.date }.distinct(), menu.days.map { it.date })
        menu.days.forEach { day ->
            assertEquals("meals of ${day.date}", listOf(MealType.LUNCH, MealType.DINNER), day.meals.map { it.type })
            day.meals.forEach { meal ->
                meal.courses.forEach { course ->
                    assertTrue("dishes of ${day.date} ${meal.type} ${course.type}", course.dishes.size in setOf(0, 5))
                    course.dishes.forEach { assertTrue("empty name on ${day.date}", it.name.isNotBlank()) }
                }
            }
        }
    }
}
