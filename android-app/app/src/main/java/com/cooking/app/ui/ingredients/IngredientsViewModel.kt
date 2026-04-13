package com.cooking.app.ui.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cooking.app.data.model.Ingredient
import com.cooking.app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IngredientsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients.asStateFlow()

    private var userId: String? = null

    fun init(userId: String) {
        if (this.userId == userId) return
        this.userId = userId
        viewModelScope.launch {
            repository.ingredientsFlow(userId).collect { _ingredients.value = it }
        }
    }

    fun loadOffline() {
        _ingredients.value = repository.loadIngredientsFromPrefs()
    }

    fun addIngredient(ingredient: Ingredient) {
        val newIng = ingredient.copy(id = repository.generateId())
        val updated = _ingredients.value + newIng
        _ingredients.value = updated
        save(updated)
    }

    fun updateIngredient(id: String, updated: Ingredient) {
        val list = _ingredients.value.map { if (it.id == id) updated.copy(id = id) else it }
        _ingredients.value = list
        save(list)
    }

    fun deleteIngredient(id: String) {
        val updated = _ingredients.value.filter { it.id != id }
        _ingredients.value = updated
        save(updated)
    }

    fun getIngredient(id: String): Ingredient? = _ingredients.value.find { it.id == id }

    private fun save(ingredients: List<Ingredient>) {
        val uid = userId ?: return
        viewModelScope.launch {
            try {
                repository.saveIngredients(uid, ingredients)
            } catch (e: Exception) {
                // Firestore sync failed
            }
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = IngredientsViewModel(repository) as T
    }
}
