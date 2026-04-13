package com.cooking.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.cooking.app.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class AppRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cooking_app_prefs", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ─── Auth ────────────────────────────────────────────────────────────────────

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    // ─── Recipes ─────────────────────────────────────────────────────────────────

    fun recipesFlow(userId: String): Flow<List<Recipe>> = callbackFlow {
        val docRef = firestore.collection("users").document(userId)
            .collection("data").document("recipes")
        val registration = docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.get("items")
                if (data is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    trySend(parseRecipesFromFirestore(data as List<Map<String, Any>>))
                    return@addSnapshotListener
                }
            }
            // Firestore has no data yet – fall back to local storage
            trySend(loadRecipesFromPrefs())
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveRecipes(userId: String, recipes: List<Recipe>) {
        saveRecipesToPrefs(recipes)
        val items = recipes.map { recipeToMap(it) }
        firestore.collection("users").document(userId)
            .collection("data").document("recipes")
            .set(mapOf("items" to items))
            .await()
    }

    fun loadRecipesFromPrefs(): List<Recipe> {
        val json = prefs.getString("recipes", null) ?: return DEFAULT_RECIPES
        return try {
            parseRecipesFromJson(json)
        } catch (e: Exception) {
            DEFAULT_RECIPES
        }
    }

    private fun saveRecipesToPrefs(recipes: List<Recipe>) {
        prefs.edit().putString("recipes", recipesToJson(recipes)).apply()
    }

    // ─── Ingredients ─────────────────────────────────────────────────────────────

    fun ingredientsFlow(userId: String): Flow<List<Ingredient>> = callbackFlow {
        val docRef = firestore.collection("users").document(userId)
            .collection("data").document("ingredients")
        val registration = docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.get("items")
                if (data is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    trySend(parseIngredientsFromFirestore(data as List<Map<String, Any>>))
                    return@addSnapshotListener
                }
            }
            trySend(loadIngredientsFromPrefs())
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveIngredients(userId: String, ingredients: List<Ingredient>) {
        saveIngredientsToPrefs(ingredients)
        val items = ingredients.map { ingredientToMap(it) }
        firestore.collection("users").document(userId)
            .collection("data").document("ingredients")
            .set(mapOf("items" to items))
            .await()
    }

    fun loadIngredientsFromPrefs(): List<Ingredient> {
        val json = prefs.getString("ingredients", null) ?: return DEFAULT_INGREDIENTS
        return try {
            parseIngredientsFromJson(json)
        } catch (e: Exception) {
            DEFAULT_INGREDIENTS
        }
    }

    private fun saveIngredientsToPrefs(ingredients: List<Ingredient>) {
        prefs.edit().putString("ingredients", ingredientsToJson(ingredients)).apply()
    }

    // ─── Shopping Lists ───────────────────────────────────────────────────────────

    fun shoppingListsFlow(userId: String): Flow<List<ShoppingList>> = callbackFlow {
        val docRef = firestore.collection("users").document(userId)
            .collection("data").document("shoppingLists")
        val registration = docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.get("items")
                if (data is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    trySend(parseShoppingListsFromFirestore(data as List<Map<String, Any>>))
                    return@addSnapshotListener
                }
            }
            trySend(loadShoppingListsFromPrefs())
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveShoppingLists(userId: String, lists: List<ShoppingList>) {
        saveShoppingListsToPrefs(lists)
        val items = lists.map { shoppingListToMap(it) }
        firestore.collection("users").document(userId)
            .collection("data").document("shoppingLists")
            .set(mapOf("items" to items))
            .await()
    }

    fun loadShoppingListsFromPrefs(): List<ShoppingList> {
        val json = prefs.getString("shopping_lists", null) ?: return emptyList()
        return try {
            parseShoppingListsFromJson(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveShoppingListsToPrefs(lists: List<ShoppingList>) {
        prefs.edit().putString("shopping_lists", shoppingListsToJson(lists)).apply()
    }

    // ─── Meal Planner ─────────────────────────────────────────────────────────────

    fun mealPlanFlow(userId: String): Flow<Map<String, DayMeals>> = callbackFlow {
        val docRef = firestore.collection("users").document(userId)
            .collection("data").document("mealPlan")
        val registration = docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                @Suppress("UNCHECKED_CAST")
                val map = snapshot.data as? Map<String, Any> ?: emptyMap()
                trySend(parseMealPlanFromFirestore(map))
                return@addSnapshotListener
            }
            trySend(loadMealPlanFromPrefs())
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveMealPlan(userId: String, plan: Map<String, DayMeals>) {
        saveMealPlanToPrefs(plan)
        val data = plan.mapValues { (_, v) -> dayMealsToMap(v) }
        firestore.collection("users").document(userId)
            .collection("data").document("mealPlan")
            .set(data)
            .await()
    }

    fun loadMealPlanFromPrefs(): Map<String, DayMeals> {
        val json = prefs.getString("meal_plan", null) ?: return emptyMap()
        return try {
            parseMealPlanFromJson(json)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveMealPlanToPrefs(plan: Map<String, DayMeals>) {
        prefs.edit().putString("meal_plan", mealPlanToJson(plan)).apply()
    }

    // ─── Settings ─────────────────────────────────────────────────────────────────

    fun getUnitSystem(): String = prefs.getString("unit_system", "metric") ?: "metric"

    fun setUnitSystem(system: String) {
        prefs.edit().putString("unit_system", system).apply()
    }

    // ─── Serialization helpers ────────────────────────────────────────────────────

    private fun recipeToMap(r: Recipe): Map<String, Any> = mapOf(
        "id" to r.id,
        "name" to r.name,
        "description" to r.description,
        "servings" to r.servings,
        "prepTime" to r.prepTime,
        "tags" to r.tags,
        "tools" to r.tools,
        "ingredients" to r.ingredients.map { mapOf("ingredientId" to it.ingredientId, "name" to it.name, "quantity" to it.quantity, "unit" to it.unit) },
        "steps" to r.steps.map { mapOf("id" to it.id, "title" to it.title, "description" to it.description) }
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseRecipesFromFirestore(items: List<Map<String, Any>>): List<Recipe> =
        items.map { m ->
            Recipe(
                id = m["id"] as? String ?: "",
                name = m["name"] as? String ?: "",
                description = m["description"] as? String ?: "",
                servings = (m["servings"] as? Long)?.toInt() ?: 1,
                prepTime = (m["prepTime"] as? Long)?.toInt() ?: 0,
                tags = (m["tags"] as? List<String>) ?: emptyList(),
                tools = (m["tools"] as? List<String>) ?: emptyList(),
                ingredients = ((m["ingredients"] as? List<Map<String, Any>>) ?: emptyList()).map { i ->
                    RecipeIngredient(
                        ingredientId = i["ingredientId"] as? String ?: "",
                        name = i["name"] as? String ?: "",
                        quantity = (i["quantity"] as? Double) ?: (i["quantity"] as? Long)?.toDouble() ?: 0.0,
                        unit = i["unit"] as? String ?: ""
                    )
                },
                steps = ((m["steps"] as? List<Map<String, Any>>) ?: emptyList()).map { s ->
                    RecipeStep(
                        id = s["id"] as? String ?: "",
                        title = s["title"] as? String ?: "",
                        description = s["description"] as? String ?: ""
                    )
                }
            )
        }

    private fun recipesToJson(recipes: List<Recipe>): String {
        val arr = JSONArray()
        recipes.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("name", r.name)
            obj.put("description", r.description)
            obj.put("servings", r.servings)
            obj.put("prepTime", r.prepTime)
            obj.put("tags", JSONArray(r.tags))
            obj.put("tools", JSONArray(r.tools))
            val ings = JSONArray()
            r.ingredients.forEach { i ->
                ings.put(JSONObject().apply { put("ingredientId", i.ingredientId); put("name", i.name); put("quantity", i.quantity); put("unit", i.unit) })
            }
            obj.put("ingredients", ings)
            val steps = JSONArray()
            r.steps.forEach { s ->
                steps.put(JSONObject().apply { put("id", s.id); put("title", s.title); put("description", s.description) })
            }
            obj.put("steps", steps)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseRecipesFromJson(json: String): List<Recipe> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val tags = obj.optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
            val tools = obj.optJSONArray("tools")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
            val ings = obj.optJSONArray("ingredients")?.let { a ->
                (0 until a.length()).map { j ->
                    val io = a.getJSONObject(j)
                    RecipeIngredient(io.optString("ingredientId"), io.optString("name"), io.optDouble("quantity", 0.0), io.optString("unit"))
                }
            } ?: emptyList()
            val steps = obj.optJSONArray("steps")?.let { a ->
                (0 until a.length()).map { j ->
                    val so = a.getJSONObject(j)
                    RecipeStep(so.optString("id"), so.optString("title"), so.optString("description"))
                }
            } ?: emptyList()
            Recipe(obj.optString("id"), obj.optString("name"), obj.optString("description"), obj.optInt("servings", 1), obj.optInt("prepTime", 0), tags, tools, ings, steps)
        }
    }

    private fun ingredientToMap(i: Ingredient): Map<String, Any> = mapOf(
        "id" to i.id, "name" to i.name, "price" to i.price, "unit" to i.unit,
        "calories" to i.calories, "protein" to i.protein, "carbs" to i.carbs, "fat" to i.fat, "category" to i.category
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseIngredientsFromFirestore(items: List<Map<String, Any>>): List<Ingredient> =
        items.map { m ->
            Ingredient(
                id = m["id"] as? String ?: "",
                name = m["name"] as? String ?: "",
                price = (m["price"] as? Double) ?: (m["price"] as? Long)?.toDouble() ?: 0.0,
                unit = m["unit"] as? String ?: "",
                calories = (m["calories"] as? Double) ?: (m["calories"] as? Long)?.toDouble() ?: 0.0,
                protein = (m["protein"] as? Double) ?: (m["protein"] as? Long)?.toDouble() ?: 0.0,
                carbs = (m["carbs"] as? Double) ?: (m["carbs"] as? Long)?.toDouble() ?: 0.0,
                fat = (m["fat"] as? Double) ?: (m["fat"] as? Long)?.toDouble() ?: 0.0,
                category = m["category"] as? String ?: "Other"
            )
        }

    private fun ingredientsToJson(ingredients: List<Ingredient>): String {
        val arr = JSONArray()
        ingredients.forEach { i ->
            arr.put(JSONObject().apply {
                put("id", i.id); put("name", i.name); put("price", i.price)
                put("unit", i.unit); put("calories", i.calories); put("protein", i.protein)
                put("carbs", i.carbs); put("fat", i.fat); put("category", i.category)
            })
        }
        return arr.toString()
    }

    private fun parseIngredientsFromJson(json: String): List<Ingredient> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Ingredient(o.optString("id"), o.optString("name"), o.optDouble("price"), o.optString("unit"),
                o.optDouble("calories"), o.optDouble("protein"), o.optDouble("carbs"), o.optDouble("fat"), o.optString("category", "Other"))
        }
    }

    private fun shoppingListToMap(l: ShoppingList): Map<String, Any> = mapOf(
        "id" to l.id, "name" to l.name, "createdAt" to l.createdAt,
        "items" to l.items.map { mapOf("id" to it.id, "name" to it.name, "quantity" to it.quantity, "unit" to it.unit, "checked" to it.checked, "ingredientId" to (it.ingredientId ?: "")) }
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseShoppingListsFromFirestore(items: List<Map<String, Any>>): List<ShoppingList> =
        items.map { m ->
            ShoppingList(
                id = m["id"] as? String ?: "",
                name = m["name"] as? String ?: "",
                createdAt = m["createdAt"] as? String ?: "",
                items = ((m["items"] as? List<Map<String, Any>>) ?: emptyList()).map { i ->
                    ShoppingItem(
                        id = i["id"] as? String ?: "",
                        name = i["name"] as? String ?: "",
                        quantity = (i["quantity"] as? Double) ?: (i["quantity"] as? Long)?.toDouble() ?: 1.0,
                        unit = i["unit"] as? String ?: "",
                        checked = i["checked"] as? Boolean ?: false,
                        ingredientId = (i["ingredientId"] as? String)?.ifEmpty { null }
                    )
                }
            )
        }

    private fun shoppingListsToJson(lists: List<ShoppingList>): String {
        val arr = JSONArray()
        lists.forEach { l ->
            val items = JSONArray()
            l.items.forEach { i ->
                items.put(JSONObject().apply {
                    put("id", i.id); put("name", i.name); put("quantity", i.quantity)
                    put("unit", i.unit); put("checked", i.checked); put("ingredientId", i.ingredientId ?: "")
                })
            }
            arr.put(JSONObject().apply { put("id", l.id); put("name", l.name); put("createdAt", l.createdAt); put("items", items) })
        }
        return arr.toString()
    }

    private fun parseShoppingListsFromJson(json: String): List<ShoppingList> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val items = o.optJSONArray("items")?.let { a ->
                (0 until a.length()).map { j ->
                    val io = a.getJSONObject(j)
                    ShoppingItem(io.optString("id"), io.optString("name"), io.optDouble("quantity", 1.0), io.optString("unit"), io.optBoolean("checked"), io.optString("ingredientId").ifEmpty { null })
                }
            } ?: emptyList()
            ShoppingList(o.optString("id"), o.optString("name"), items, o.optString("createdAt"))
        }
    }

    private fun dayMealsToMap(d: DayMeals): Map<String, Any?> = mapOf(
        "breakfast" to d.breakfast?.let { mapOf("recipeId" to it.recipeId, "recipeName" to it.recipeName) },
        "lunch" to d.lunch?.let { mapOf("recipeId" to it.recipeId, "recipeName" to it.recipeName) },
        "dinner" to d.dinner?.let { mapOf("recipeId" to it.recipeId, "recipeName" to it.recipeName) }
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseMealPlanFromFirestore(map: Map<String, Any>): Map<String, DayMeals> =
        map.mapValues { (_, v) ->
            val d = v as? Map<String, Any> ?: return@mapValues DayMeals()
            fun entry(key: String): MealEntry? {
                val e = d[key] as? Map<String, Any> ?: return null
                return MealEntry(e["recipeId"] as? String ?: "", e["recipeName"] as? String ?: "")
            }
            DayMeals(entry("breakfast"), entry("lunch"), entry("dinner"))
        }

    private fun mealPlanToJson(plan: Map<String, DayMeals>): String {
        val obj = JSONObject()
        plan.forEach { (date, meals) ->
            val d = JSONObject()
            fun putEntry(key: String, entry: MealEntry?) {
                if (entry != null) d.put(key, JSONObject().apply { put("recipeId", entry.recipeId); put("recipeName", entry.recipeName) })
                else d.put(key, JSONObject.NULL)
            }
            putEntry("breakfast", meals.breakfast)
            putEntry("lunch", meals.lunch)
            putEntry("dinner", meals.dinner)
            obj.put(date, d)
        }
        return obj.toString()
    }

    private fun parseMealPlanFromJson(json: String): Map<String, DayMeals> {
        val obj = JSONObject(json)
        val result = mutableMapOf<String, DayMeals>()
        obj.keys().forEach { date ->
            val d = obj.optJSONObject(date) ?: return@forEach
            fun parseEntry(key: String): MealEntry? {
                val e = d.optJSONObject(key) ?: return null
                return MealEntry(e.optString("recipeId"), e.optString("recipeName"))
            }
            result[date] = DayMeals(parseEntry("breakfast"), parseEntry("lunch"), parseEntry("dinner"))
        }
        return result
    }

    fun generateId(): String = UUID.randomUUID().toString()

    fun todayString(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
}
