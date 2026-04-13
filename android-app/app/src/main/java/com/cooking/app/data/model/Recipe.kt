package com.cooking.app.data.model

data class RecipeStep(
    val id: String = "",
    val title: String = "",
    val description: String = ""
)

data class RecipeIngredient(
    val ingredientId: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = ""
)

data class Recipe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val servings: Int = 1,
    val prepTime: Int = 0,
    val tags: List<String> = emptyList(),
    val tools: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList()
)

val RECIPE_TAGS = listOf(
    "Breakfast", "Salad", "Soup", "Main Course",
    "Italian", "Healthy", "Dessert", "Snack", "Pasta", "Other"
)

val DEFAULT_RECIPES = listOf(
    Recipe(
        id = "1",
        name = "Pasta Arrabbiata",
        description = "Spicy Italian pasta with tomato sauce",
        servings = 4,
        prepTime = 30,
        tags = listOf("Italian", "Pasta", "Main Course"),
        tools = listOf("Large cooking pot", "Frying pan", "Colander", "Wooden spoon", "Chef's knife"),
        ingredients = listOf(
            RecipeIngredient("2", "Pasta", 0.5, "kg"),
            RecipeIngredient("3", "Tomato", 0.4, "kg"),
            RecipeIngredient("4", "Olive Oil", 0.05, "L"),
            RecipeIngredient("5", "Garlic", 0.02, "kg")
        ),
        steps = listOf(
            RecipeStep("s1-1", "Boil Water", "Bring a large pot of salted water to a boil."),
            RecipeStep("s1-2", "Cook the Pasta", "Add pasta and cook according to package instructions until al dente. Reserve ½ cup of pasta water."),
            RecipeStep("s1-3", "Prepare the Sauce", "Heat olive oil in a pan over medium heat. Add minced garlic and cook for 1 minute until fragrant."),
            RecipeStep("s1-4", "Add Tomatoes", "Add crushed tomatoes, season with salt and chilli flakes, and simmer for 10 minutes."),
            RecipeStep("s1-5", "Combine & Serve", "Drain pasta and toss with the sauce. Add a splash of pasta water if needed. Serve immediately.")
        )
    ),
    Recipe(
        id = "2",
        name = "Chicken Rice Bowl",
        description = "Healthy chicken and rice with vegetables",
        servings = 2,
        prepTime = 40,
        tags = listOf("Main Course", "Healthy"),
        tools = listOf("Rice cooker", "Frying pan", "Cutting board", "Chef's knife", "Mixing bowl"),
        ingredients = listOf(
            RecipeIngredient("1", "Chicken Breast", 0.4, "kg"),
            RecipeIngredient("7", "Rice", 0.2, "kg"),
            RecipeIngredient("6", "Onion", 0.1, "kg"),
            RecipeIngredient("4", "Olive Oil", 0.03, "L")
        ),
        steps = listOf(
            RecipeStep("s2-1", "Cook the Rice", "Rinse the rice and cook in 400ml of water with a pinch of salt. Bring to a boil then simmer for 15 minutes."),
            RecipeStep("s2-2", "Season the Chicken", "Cut chicken breast into bite-sized pieces and season with salt, pepper, and your favourite spices."),
            RecipeStep("s2-3", "Sauté the Onion", "Heat olive oil in a pan. Add sliced onion and cook for 5 minutes until softened and golden."),
            RecipeStep("s2-4", "Cook the Chicken", "Add chicken to the pan and cook for 7–8 minutes, stirring occasionally, until cooked through."),
            RecipeStep("s2-5", "Assemble & Serve", "Spoon rice into bowls and top with the chicken and onion mixture. Garnish as desired.")
        )
    ),
    Recipe(
        id = "3",
        name = "Garlic Pasta",
        description = "Simple and delicious aglio e olio",
        servings = 2,
        prepTime = 20,
        tags = listOf("Italian", "Pasta"),
        tools = listOf("Large cooking pot", "Large frying pan", "Colander", "Wooden spoon"),
        ingredients = listOf(
            RecipeIngredient("2", "Pasta", 0.3, "kg"),
            RecipeIngredient("5", "Garlic", 0.03, "kg"),
            RecipeIngredient("4", "Olive Oil", 0.08, "L")
        ),
        steps = listOf(
            RecipeStep("s3-1", "Boil the Pasta", "Cook pasta in well-salted boiling water until al dente. Reserve 1 cup of pasta water before draining."),
            RecipeStep("s3-2", "Infuse the Oil", "Gently heat olive oil in a large pan over low heat. Add thinly sliced garlic and cook until lightly golden, about 3 minutes."),
            RecipeStep("s3-3", "Combine", "Add drained pasta to the pan with a splash of pasta water. Toss well to coat every strand in the garlic oil."),
            RecipeStep("s3-4", "Serve", "Plate immediately and finish with a drizzle of extra olive oil and fresh parsley if available.")
        )
    ),
    Recipe(
        id = "4",
        name = "Greek Salad",
        description = "Classic Mediterranean salad with feta and olives",
        servings = 2,
        prepTime = 15,
        tags = listOf("Salad", "Healthy"),
        tools = listOf("Large salad bowl", "Chef's knife", "Cutting board"),
        ingredients = listOf(
            RecipeIngredient("3", "Tomato", 0.3, "kg"),
            RecipeIngredient("4", "Olive Oil", 0.03, "L")
        ),
        steps = listOf(
            RecipeStep("s4-1", "Chop the Vegetables", "Cut tomatoes and cucumber into chunks. Slice the red onion thinly."),
            RecipeStep("s4-2", "Combine", "Toss tomatoes, cucumber, onion, and olives together in a large bowl."),
            RecipeStep("s4-3", "Dress & Serve", "Drizzle with olive oil, season with salt and oregano, then top with cubed feta cheese.")
        )
    ),
    Recipe(
        id = "5",
        name = "Chocolate Mousse",
        description = "Light and fluffy chocolate dessert",
        servings = 4,
        prepTime = 20,
        tags = listOf("Dessert"),
        tools = listOf("Double boiler (bain-marie)", "Hand mixer", "Mixing bowls", "Serving glasses", "Spatula"),
        steps = listOf(
            RecipeStep("s5-1", "Melt the Chocolate", "Break dark chocolate into pieces and melt over a bain-marie. Let it cool slightly."),
            RecipeStep("s5-2", "Whip the Cream", "Whip the heavy cream to soft peaks in a chilled bowl."),
            RecipeStep("s5-3", "Fold Together", "Gently fold the melted chocolate into the whipped cream until just combined. Do not overmix."),
            RecipeStep("s5-4", "Chill & Serve", "Divide into serving glasses and refrigerate for at least 1 hour before serving.")
        )
    )
)
