package com.cooking.app.data.model

data class Ingredient(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val unit: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val category: String = "Other"
)

val INGREDIENT_UNITS = listOf("kg", "g", "L", "ml", "dozen", "piece", "cup", "tbsp", "tsp", "oz", "lb")

val DEFAULT_INGREDIENTS = listOf(
    Ingredient("1", "Chicken Breast", 28.99, "kg", 165.0, 31.0, 0.0, 3.6, "Meat"),
    Ingredient("2", "Pasta", 6.99, "kg", 371.0, 13.0, 74.0, 1.5, "Grains"),
    Ingredient("3", "Tomato", 9.99, "kg", 18.0, 0.9, 3.9, 0.2, "Vegetables"),
    Ingredient("4", "Olive Oil", 39.99, "L", 884.0, 0.0, 0.0, 100.0, "Oils"),
    Ingredient("5", "Garlic", 9.99, "kg", 149.0, 6.4, 33.0, 0.5, "Vegetables"),
    Ingredient("6", "Onion", 3.99, "kg", 40.0, 1.1, 9.3, 0.1, "Vegetables"),
    Ingredient("7", "Rice", 5.99, "kg", 365.0, 7.1, 79.0, 0.7, "Grains"),
    Ingredient("8", "Eggs", 17.99, "dozen", 155.0, 13.0, 1.1, 11.0, "Dairy")
)
