package com.cooking.app.ui.shoppinglists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cooking.app.data.model.*
import com.cooking.app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter

class ShoppingListsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _lists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val lists: StateFlow<List<ShoppingList>> = _lists.asStateFlow()

    private var userId: String? = null

    fun init(userId: String) {
        if (this.userId == userId) return
        this.userId = userId
        viewModelScope.launch {
            repository.shoppingListsFlow(userId).collect { _lists.value = it }
        }
    }

    fun loadOffline() {
        _lists.value = repository.loadShoppingListsFromPrefs()
    }

    fun createList(name: String): ShoppingList {
        val newList = ShoppingList(
            id = repository.generateId(),
            name = name,
            items = emptyList(),
            createdAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )
        val updated = _lists.value + newList
        _lists.value = updated
        save(updated)
        return newList
    }

    fun deleteList(id: String) {
        val updated = _lists.value.filter { it.id != id }
        _lists.value = updated
        save(updated)
    }

    fun getList(id: String): ShoppingList? = _lists.value.find { it.id == id }

    fun addItemToList(listId: String, item: ShoppingItem) {
        val updated = _lists.value.map { list ->
            if (list.id != listId) return@map list
            val existing = list.items.find { existing ->
                (item.ingredientId != null && existing.ingredientId == item.ingredientId) ||
                    (item.ingredientId == null && existing.name.equals(item.name, ignoreCase = true))
            }
            val newItems = if (existing != null) {
                list.items.map { i ->
                    if (i.id == existing.id) i.copy(quantity = i.quantity + item.quantity)
                    else i
                }
            } else {
                list.items + item.copy(id = repository.generateId())
            }
            list.copy(items = newItems)
        }
        _lists.value = updated
        save(updated)
    }

    fun removeItemFromList(listId: String, itemId: String) {
        val updated = _lists.value.map { list ->
            if (list.id != listId) list
            else list.copy(items = list.items.filter { it.id != itemId })
        }
        _lists.value = updated
        save(updated)
    }

    fun toggleItem(listId: String, itemId: String) {
        val updated = _lists.value.map { list ->
            if (list.id != listId) return@map list
            list.copy(items = list.items.map { item ->
                if (item.id == itemId) item.copy(checked = !item.checked) else item
            })
        }
        _lists.value = updated
        save(updated)
    }

    fun addRecipeIngredientsToList(listId: String, ingredients: List<RecipeIngredient>) {
        ingredients.forEach { ing ->
            addItemToList(
                listId,
                ShoppingItem(
                    name = ing.name,
                    quantity = ing.quantity,
                    unit = ing.unit,
                    ingredientId = ing.ingredientId.ifEmpty { null }
                )
            )
        }
    }

    private fun save(lists: List<ShoppingList>) {
        val uid = userId ?: return
        viewModelScope.launch {
            try {
                repository.saveShoppingLists(uid, lists)
            } catch (e: Exception) {
                // Firestore sync failed - data already in SharedPreferences
            }
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShoppingListsViewModel(repository) as T
    }
}
