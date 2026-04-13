package com.cooking.app.ui.mealplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cooking.app.data.model.DayMeals
import com.cooking.app.data.model.MealEntry
import com.cooking.app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealPlannerViewModel(private val repository: AppRepository) : ViewModel() {

    private val _plan = MutableStateFlow<Map<String, DayMeals>>(emptyMap())
    val plan: StateFlow<Map<String, DayMeals>> = _plan.asStateFlow()

    private var userId: String? = null

    fun init(userId: String) {
        if (this.userId == userId) return
        this.userId = userId
        viewModelScope.launch {
            repository.mealPlanFlow(userId).collect { _plan.value = it }
        }
    }

    fun loadOffline() {
        _plan.value = repository.loadMealPlanFromPrefs()
    }

    fun getMealsForDate(date: String): DayMeals =
        _plan.value[date] ?: DayMeals()

    fun setMeal(date: String, slot: String, entry: MealEntry?) {
        val current = _plan.value[date] ?: DayMeals()
        val updated = when (slot) {
            "breakfast" -> current.copy(breakfast = entry)
            "lunch" -> current.copy(lunch = entry)
            "dinner" -> current.copy(dinner = entry)
            else -> current
        }
        val newPlan = if (entry == null && updated.breakfast == null && updated.lunch == null && updated.dinner == null) {
            _plan.value - date
        } else {
            _plan.value + (date to updated)
        }
        _plan.value = newPlan
        save(newPlan)
    }

    private fun save(plan: Map<String, DayMeals>) {
        val uid = userId ?: return
        viewModelScope.launch {
            try {
                repository.saveMealPlan(uid, plan)
            } catch (e: Exception) {
                // Firestore sync failed
            }
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MealPlannerViewModel(repository) as T
    }
}
