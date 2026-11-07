package it.fra.myunical.datastore.models

import kotlinx.serialization.Serializable

/** `assets/canteen/menu.json`, written by tools/canteen/parse_menu.py. Enums are strings, so new values do not break old apps. */
@Serializable
data class CanteenMenuDataModel(
    val version: Int,
    val days: List<CanteenDayDataModel> = emptyList()
)

@Serializable
data class CanteenDayDataModel(val date: String, val meals: List<MealDataModel> = emptyList())

@Serializable
data class MealDataModel(val type: String, val courses: List<CourseDataModel> = emptyList())

@Serializable
data class CourseDataModel(val type: String, val dishes: List<DishDataModel> = emptyList())

@Serializable
data class DishDataModel(
    val name: String,
    val line: String,
    val tags: List<String> = emptyList(),
    val frozen: Boolean = false
)
