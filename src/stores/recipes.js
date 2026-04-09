import { defineStore } from 'pinia'
import { ref } from 'vue'
import { saveCollectionAsJson, loadCollectionAsJson, isFirebaseConfigured } from '../firebase.js'

const STORAGE_KEY = 'cooking_recipes'
const FIRESTORE_KEY = 'recipes'

export const RECIPE_TAGS = [
  'Breakfast',
  'Salad',
  'Soup',
  'Main Course',
  'Italian',
  'Healthy',
  'Dessert',
  'Snack',
  'Pasta',
  'Other',
]

const DEFAULT_RECIPES = [
  {
    id: '1',
    name: 'Pasta Arrabbiata',
    description: 'Spicy Italian pasta with tomato sauce',
    servings: 4,
    prepTime: 30,
    tags: ['Italian', 'Pasta', 'Main Course'],
    ingredients: [
      { ingredientId: '2', name: 'Pasta', quantity: 0.5, unit: 'kg' },
      { ingredientId: '3', name: 'Tomato', quantity: 0.4, unit: 'kg' },
      { ingredientId: '4', name: 'Olive Oil', quantity: 0.05, unit: 'L' },
      { ingredientId: '5', name: 'Garlic', quantity: 0.02, unit: 'kg' },
    ],
    steps: [
      { id: 's1-1', title: 'Boil Water', description: 'Bring a large pot of salted water to a boil.' },
      { id: 's1-2', title: 'Cook the Pasta', description: 'Add pasta and cook according to package instructions until al dente. Reserve ½ cup of pasta water.' },
      { id: 's1-3', title: 'Prepare the Sauce', description: 'Heat olive oil in a pan over medium heat. Add minced garlic and cook for 1 minute until fragrant.' },
      { id: 's1-4', title: 'Add Tomatoes', description: 'Add crushed tomatoes, season with salt and chilli flakes, and simmer for 10 minutes.' },
      { id: 's1-5', title: 'Combine & Serve', description: 'Drain pasta and toss with the sauce. Add a splash of pasta water if needed. Serve immediately.' },
    ],
  },
  {
    id: '2',
    name: 'Chicken Rice Bowl',
    description: 'Healthy chicken and rice with vegetables',
    servings: 2,
    prepTime: 40,
    tags: ['Main Course', 'Healthy'],
    ingredients: [
      { ingredientId: '1', name: 'Chicken Breast', quantity: 0.4, unit: 'kg' },
      { ingredientId: '7', name: 'Rice', quantity: 0.2, unit: 'kg' },
      { ingredientId: '6', name: 'Onion', quantity: 0.1, unit: 'kg' },
      { ingredientId: '4', name: 'Olive Oil', quantity: 0.03, unit: 'L' },
    ],
    steps: [
      { id: 's2-1', title: 'Cook the Rice', description: 'Rinse the rice and cook in 400ml of water with a pinch of salt. Bring to a boil then simmer for 15 minutes.' },
      { id: 's2-2', title: 'Season the Chicken', description: 'Cut chicken breast into bite-sized pieces and season with salt, pepper, and your favourite spices.' },
      { id: 's2-3', title: 'Sauté the Onion', description: 'Heat olive oil in a pan. Add sliced onion and cook for 5 minutes until softened and golden.' },
      { id: 's2-4', title: 'Cook the Chicken', description: 'Add chicken to the pan and cook for 7–8 minutes, stirring occasionally, until cooked through.' },
      { id: 's2-5', title: 'Assemble & Serve', description: 'Spoon rice into bowls and top with the chicken and onion mixture. Garnish as desired.' },
    ],
  },
  {
    id: '3',
    name: 'Garlic Pasta',
    description: 'Simple and delicious aglio e olio',
    servings: 2,
    prepTime: 20,
    tags: ['Italian', 'Pasta'],
    ingredients: [
      { ingredientId: '2', name: 'Pasta', quantity: 0.3, unit: 'kg' },
      { ingredientId: '5', name: 'Garlic', quantity: 0.03, unit: 'kg' },
      { ingredientId: '4', name: 'Olive Oil', quantity: 0.08, unit: 'L' },
    ],
    steps: [
      { id: 's3-1', title: 'Boil the Pasta', description: 'Cook pasta in well-salted boiling water until al dente. Reserve 1 cup of pasta water before draining.' },
      { id: 's3-2', title: 'Infuse the Oil', description: 'Gently heat olive oil in a large pan over low heat. Add thinly sliced garlic and cook until lightly golden, about 3 minutes.' },
      { id: 's3-3', title: 'Combine', description: 'Add drained pasta to the pan with a splash of pasta water. Toss well to coat every strand in the garlic oil.' },
      { id: 's3-4', title: 'Serve', description: 'Plate immediately and finish with a drizzle of extra olive oil and fresh parsley if available.' },
    ],
  },
  {
    id: '4',
    name: 'Greek Salad',
    description: 'Classic Mediterranean salad with feta and olives',
    servings: 2,
    prepTime: 15,
    tags: ['Salad', 'Healthy'],
    ingredients: [
      { ingredientId: '3', name: 'Tomato', quantity: 0.3, unit: 'kg' },
      { ingredientId: '4', name: 'Olive Oil', quantity: 0.03, unit: 'L' },
    ],
    steps: [
      { id: 's4-1', title: 'Chop the Vegetables', description: 'Cut tomatoes and cucumber into chunks. Slice the red onion thinly.' },
      { id: 's4-2', title: 'Combine', description: 'Toss tomatoes, cucumber, onion, and olives together in a large bowl.' },
      { id: 's4-3', title: 'Dress & Serve', description: 'Drizzle with olive oil, season with salt and oregano, then top with cubed feta cheese.' },
    ],
  },
  {
    id: '5',
    name: 'Chocolate Mousse',
    description: 'Light and fluffy chocolate dessert',
    servings: 4,
    prepTime: 20,
    tags: ['Dessert'],
    ingredients: [],
    steps: [
      { id: 's5-1', title: 'Melt the Chocolate', description: 'Break dark chocolate into pieces and melt over a bain-marie. Let it cool slightly.' },
      { id: 's5-2', title: 'Whip the Cream', description: 'Whip the heavy cream to soft peaks in a chilled bowl.' },
      { id: 's5-3', title: 'Fold Together', description: 'Gently fold the melted chocolate into the whipped cream until just combined. Do not overmix.' },
      { id: 's5-4', title: 'Chill & Serve', description: 'Divide into serving glasses and refrigerate for at least 1 hour before serving.' },
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
  saveCollectionAsJson(FIRESTORE_KEY, recipes).catch(console.error)
}

export const useRecipesStore = defineStore('recipes', () => {
  const recipes = ref(loadFromStorage())
  const selectedRecipe = ref(null)

  if (isFirebaseConfigured()) {
    loadCollectionAsJson(FIRESTORE_KEY).then(data => {
      if (data) {
        recipes.value = data
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
      }
    }).catch(console.error)
  }

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
      tags: Array.isArray(recipe.tags) ? recipe.tags : [],
      ingredients: recipe.ingredients || [],
      steps: recipe.steps || [],
    }
    recipes.value.push(newRecipe)
    saveToStorage(recipes.value)
    return newRecipe
  }

  function updateRecipeSteps(id, steps) {
    const recipe = recipes.value.find(r => r.id === id)
    if (recipe) {
      recipe.steps = steps
      saveToStorage(recipes.value)
    }
  }

  function deleteRecipe(id) {
    recipes.value = recipes.value.filter(r => r.id !== id)
    saveToStorage(recipes.value)
  }

  return { recipes, selectedRecipe, selectRecipe, addRecipe, updateRecipeSteps, deleteRecipe }
})
