import { defineStore } from 'pinia'
import { ref } from 'vue'
import { onScopeDispose } from 'vue'
import { saveCollectionAsJson, subscribeToCollection } from '../api.js'

const STORAGE_KEY = 'cooking_meal_planner'
const FIRESTORE_KEY = 'mealPlanner'

export const MEAL_SLOTS = ['breakfast', 'lunch', 'dinner']

function loadFromStorage() {
  try {
    const data = localStorage.getItem(STORAGE_KEY)
    return data ? JSON.parse(data) : {}
  } catch {
    return {}
  }
}

function saveToStorage(plan) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(plan))
  saveCollectionAsJson(FIRESTORE_KEY, plan).catch(console.error)
}

export const useMealPlannerStore = defineStore('mealPlanner', () => {
  const plan = ref(loadFromStorage())

  const unsubscribe = subscribeToCollection(FIRESTORE_KEY, (data) => {
    if (data === null) {
      plan.value = {}
      localStorage.removeItem(STORAGE_KEY)
      return
    }
    plan.value = data
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  })
  onScopeDispose(unsubscribe)

  function getMealForDate(date) {
    return plan.value[date] || { breakfast: null, lunch: null, dinner: null }
  }

  function setMeal(date, slot, entry) {
    if (!plan.value[date]) {
      plan.value[date] = { breakfast: null, lunch: null, dinner: null }
    }
    plan.value[date][slot] = entry
    saveToStorage(plan.value)
  }

  function clearMeal(date, slot) {
    if (!plan.value[date]) return
    plan.value[date][slot] = null
    if (!plan.value[date].breakfast && !plan.value[date].lunch && !plan.value[date].dinner) {
      const updated = { ...plan.value }
      delete updated[date]
      plan.value = updated
    }
    saveToStorage(plan.value)
  }

  return { plan, getMealForDate, setMeal, clearMeal }
})
