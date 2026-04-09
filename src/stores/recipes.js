import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'cooking_recipes'

const DEFAULT_RECIPES = [
  {
    id: '1',
    name: 'Pasta Arrabbiata',
    description: 'Spicy Italian pasta with tomato sauce',
    servings: 4,
    prepTime: 30,
    category: 'Italian',
    ingredients: [
      { ingredientId: '2', name: 'Pasta', quantity: 0.5, unit: 'kg' },
      { ingredientId: '3', name: 'Tomato', quantity: 0.4, unit: 'kg' },
      { ingredientId: '4', name: 'Olive Oil', quantity: 0.05, unit: 'L' },
      { ingredientId: '5', name: 'Garlic', quantity: 0.02, unit: 'kg' },
    ],
  },
  {
    id: '2',
    name: 'Chicken Rice Bowl',
    description: 'Healthy chicken and rice with vegetables',
    servings: 2,
    prepTime: 40,
    category: 'Healthy',
    ingredients: [
      { ingredientId: '1', name: 'Chicken Breast', quantity: 0.4, unit: 'kg' },
      { ingredientId: '7', name: 'Rice', quantity: 0.2, unit: 'kg' },
      { ingredientId: '6', name: 'Onion', quantity: 0.1, unit: 'kg' },
      { ingredientId: '4', name: 'Olive Oil', quantity: 0.03, unit: 'L' },
    ],
  },
  {
    id: '3',
    name: 'Garlic Pasta',
    description: 'Simple and delicious aglio e olio',
    servings: 2,
    prepTime: 20,
    category: 'Italian',
    ingredients: [
      { ingredientId: '2', name: 'Pasta', quantity: 0.3, unit: 'kg' },
      { ingredientId: '5', name: 'Garlic', quantity: 0.03, unit: 'kg' },
      { ingredientId: '4', name: 'Olive Oil', quantity: 0.08, unit: 'L' },
    ],
  },
]

function loadFromStorage() {
  try {
    const data = localStorage.getItem(STORAGE_KEY)
    return data ? JSON.parse(data) : DEFAULT_RECIPES
  } catch {
    return DEFAULT_RECIPES
  }
}

function saveToStorage(recipes) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(recipes))
}

export const useRecipesStore = defineStore('recipes', () => {
  const recipes = ref(loadFromStorage())
  const selectedRecipe = ref(null)

  function selectRecipe(recipe) {
    selectedRecipe.value = recipe
  }

  function addRecipe(recipe) {
    const newRecipe = {
      id: Date.now().toString(),
      name: recipe.name,
      description: recipe.description || '',
      servings: parseInt(recipe.servings) || 1,
      prepTime: parseInt(recipe.prepTime) || 0,
      category: recipe.category || 'Other',
      ingredients: recipe.ingredients || [],
    }
    recipes.value.push(newRecipe)
    saveToStorage(recipes.value)
    return newRecipe
  }

  function deleteRecipe(id) {
    recipes.value = recipes.value.filter(r => r.id !== id)
    saveToStorage(recipes.value)
  }

  return { recipes, selectedRecipe, selectRecipe, addRecipe, deleteRecipe }
})
