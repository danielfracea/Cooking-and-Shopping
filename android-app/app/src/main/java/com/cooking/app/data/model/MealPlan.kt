package com.cooking.app.data.model

data class MealEntry(
    val recipeId: String = "",
    val recipeName: String = ""
)

data class DayMeals(
    val breakfast: MealEntry? = null,
    val lunch: MealEntry? = null,
    val dinner: MealEntry? = null
)

val MEAL_SLOTS = listOf("breakfast", "lunch", "dinner")
