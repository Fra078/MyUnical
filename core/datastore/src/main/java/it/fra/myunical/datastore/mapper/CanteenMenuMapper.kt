package it.fra.myunical.datastore.mapper

import it.fra.myunical.datastore.models.CanteenMenuDataModel
import it.fra.myunical.domain.model.canteen.CanteenDay
import it.fra.myunical.domain.model.canteen.CanteenMenu
import it.fra.myunical.domain.model.canteen.Course
import it.fra.myunical.domain.model.canteen.CourseType
import it.fra.myunical.domain.model.canteen.DietTag
import it.fra.myunical.domain.model.canteen.Dish
import it.fra.myunical.domain.model.canteen.DishLine
import it.fra.myunical.domain.model.canteen.Meal
import it.fra.myunical.domain.model.canteen.MealType
import java.time.LocalDate

/** Values this version does not know are skipped: a newer file still shows what it can. */
fun CanteenMenuDataModel.toDomainModel() = CanteenMenu(
    days = days.mapNotNull { day ->
        val date = runCatching { LocalDate.parse(day.date) }.getOrNull() ?: return@mapNotNull null
        CanteenDay(
            date = date,
            meals = day.meals.mapNotNull { meal ->
                Meal(
                    type = enumOrNull<MealType>(meal.type) ?: return@mapNotNull null,
                    courses = meal.courses.mapNotNull { course ->
                        Course(
                            type = enumOrNull<CourseType>(course.type) ?: return@mapNotNull null,
                            dishes = course.dishes.map { dish ->
                                Dish(
                                    name = dish.name,
                                    line = enumOrNull<DishLine>(dish.line) ?: DishLine.CLASSIC,
                                    tags = dish.tags.mapNotNull { enumOrNull<DietTag>(it) }.toSet(),
                                    frozen = dish.frozen
                                )
                            }
                        )
                    }.sortedBy { it.type }
                )
            }.sortedBy { it.type }
        )
    }.sortedBy { it.date }
)

private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? = enumValues<T>().firstOrNull { it.name == name }
