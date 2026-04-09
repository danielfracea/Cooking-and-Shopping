import { defineStore } from 'pinia'
import { ref } from 'vue'
import { saveCollectionAsJson, loadCollectionAsJson, isFirebaseConfigured } from '../firebase.js'

const STORAGE_KEY = 'cooking_ingredients'
const FIRESTORE_KEY = 'ingredients'

const DEFAULT_INGREDIENTS = [
  { id: '1', name: 'Chicken Breast', price: 28.99, unit: 'kg', calories: 165, protein: 31, carbs: 0, fat: 3.6, category: 'Meat' },
  { id: '2', name: 'Pasta', price: 6.99, unit: 'kg', calories: 371, protein: 13, carbs: 74, fat: 1.5, category: 'Grains' },
  { id: '3', name: 'Tomato', price: 9.99, unit: 'kg', calories: 18, protein: 0.9, carbs: 3.9, fat: 0.2, category: 'Vegetables' },
  { id: '4', name: 'Olive Oil', price: 39.99, unit: 'L', calories: 884, protein: 0, carbs: 0, fat: 100, category: 'Oils' },
  { id: '5', name: 'Garlic', price: 9.99, unit: 'kg', calories: 149, protein: 6.4, carbs: 33, fat: 0.5, category: 'Vegetables' },
  { id: '6', name: 'Onion', price: 3.99, unit: 'kg', calories: 40, protein: 1.1, carbs: 9.3, fat: 0.1, category: 'Vegetables' },
  { id: '7', name: 'Rice', price: 5.99, unit: 'kg', calories: 365, protein: 7.1, carbs: 79, fat: 0.7, category: 'Grains' },
  { id: '8', name: 'Eggs', price: 17.99, unit: 'dozen', calories: 155, protein: 13, carbs: 1.1, fat: 11, category: 'Dairy' },
]

function loadFromStorage() {
  try {
    const data = localStorage.getItem(STORAGE_KEY)
    return data ? JSON.parse(data) : DEFAULT_INGREDIENTS
  } catch {
    return DEFAULT_INGREDIENTS
  }
}

function saveToStorage(ingredients) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(ingredients))
  saveCollectionAsJson(FIRESTORE_KEY, ingredients).catch(console.error)
}

export const useIngredientsStore = defineStore('ingredients', () => {
  const ingredients = ref(loadFromStorage())

  if (isFirebaseConfigured()) {
    loadCollectionAsJson(FIRESTORE_KEY).then(data => {
      if (data) {
        ingredients.value = data
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
      }
    }).catch(console.error)
  }

  function addIngredient(ingredient) {
    const newIng = {
      id: Date.now().toString(),
      name: ingredient.name,
      price: parseFloat(ingredient.price) || 0,
      unit: ingredient.unit || '',
      calories: parseFloat(ingredient.calories) || 0,
      protein: parseFloat(ingredient.protein) || 0,
      carbs: parseFloat(ingredient.carbs) || 0,
      fat: parseFloat(ingredient.fat) || 0,
      category: ingredient.category || 'Other',
    }
    ingredients.value.push(newIng)
    saveToStorage(ingredients.value)
    return newIng
  }

  function updateIngredient(id, updates) {
    const idx = ingredients.value.findIndex(i => i.id === id)
    if (idx !== -1) {
      ingredients.value[idx] = { ...ingredients.value[idx], ...updates }
      saveToStorage(ingredients.value)
    }
  }

  function deleteIngredient(id) {
    ingredients.value = ingredients.value.filter(i => i.id !== id)
    saveToStorage(ingredients.value)
  }

  function getIngredient(id) {
    return ingredients.value.find(i => i.id === id)
  }

  return { ingredients, addIngredient, updateIngredient, deleteIngredient, getIngredient }
})
