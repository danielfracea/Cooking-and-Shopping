package com.cooking.app.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cooking.app.data.model.*
import com.cooking.app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipesViewModel(private val repository: AppRepository) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    private var userId: String? = null

    fun init(userId: String) {
        if (this.userId == userId) return
        this.userId = userId
        viewModelScope.launch {
            repository.recipesFlow(userId).collect { _recipes.value = it }
        }
    }

    fun loadOffline() {
        _recipes.value = repository.loadRecipesFromPrefs()
    }

    fun selectRecipe(recipe: Recipe?) {
        _selectedRecipe.value = recipe
    }

    fun addRecipe(
        name: String,
        description: String,
        servings: Int,
        prepTime: Int,
        tags: List<String>,
        tools: List<String>,
        ingredients: List<RecipeIngredient>,
        steps: List<RecipeStep>
    ) {
        val newRecipe = Recipe(
            id = repository.generateId(),
            name = name,
            description = description,
            servings = servings,
            prepTime = prepTime,
            tags = tags,
            tools = tools,
            ingredients = ingredients,
            steps = steps
        )
        val updated = _recipes.value + newRecipe
        _recipes.value = updated
        save(updated)
    }

    fun deleteRecipe(id: String) {
        val updated = _recipes.value.filter { it.id != id }
        _recipes.value = updated
        if (_selectedRecipe.value?.id == id) _selectedRecipe.value = null
        save(updated)
    }

    private fun save(recipes: List<Recipe>) {
        val uid = userId ?: return
        viewModelScope.launch {
            try {
                repository.saveRecipes(uid, recipes)
            } catch (e: Exception) {
                // Firestore sync failed - data is already saved to SharedPreferences
            }
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RecipesViewModel(repository) as T
    }
}
