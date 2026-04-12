import { createRouter, createWebHistory } from 'vue-router'
import ShoppingListsView from '../views/ShoppingListsView.vue'
import ShoppingListDetailView from '../views/ShoppingListDetailView.vue'
import IngredientsView from '../views/IngredientsView.vue'
import RecipesView from '../views/RecipesView.vue'
import SharedListView from '../views/SharedListView.vue'
import MealPlannerView from '../views/MealPlannerView.vue'
import SettingsView from '../views/SettingsView.vue'

const routes = [
  { path: '/', redirect: '/shopping-lists' },
  { path: '/shopping-lists', component: ShoppingListsView },
  { path: '/shopping-lists/:id', component: ShoppingListDetailView },
  { path: '/ingredients', component: IngredientsView },
  { path: '/recipes', component: RecipesView },
  { path: '/meal-planner', component: MealPlannerView },
  { path: '/shared', component: SharedListView },
  { path: '/settings', component: SettingsView },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
