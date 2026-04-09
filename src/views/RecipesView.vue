<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">📖 Recipes</h1>
      <button class="btn btn-primary" @click="openAddRecipe">+ Add Recipe</button>
    </div>
    <div v-if="!selectedRecipe">
      <div v-if="recipes.length === 0" class="empty-state"><div class="icon">📒</div><p>No recipes yet</p><button class="btn btn-primary" @click="openAddRecipe">Add Your First Recipe</button></div>
      <div v-else class="grid"><RecipeCard v-for="recipe in recipes" :key="recipe.id" :recipe="recipe" @view="viewRecipe" /></div>
    </div>
    <div v-else>
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px">
          <button class="btn btn-secondary btn-sm" @click="selectedRecipe = null">← Back</button>
          <h2 style="font-size:1.5rem;font-weight:700">{{ selectedRecipe.name }}</h2>
          <span class="tag">{{ selectedRecipe.category }}</span>
        </div>
        <button class="btn btn-danger btn-sm" @click="deleteRecipe(selectedRecipe.id)">🗑 Delete</button>
      </div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px">
        <div class="card"><p class="meta-label">Description</p><p>{{ selectedRecipe.description || 'No description' }}</p></div>
        <div class="card"><div style="display:flex;gap:24px"><div><p class="meta-label">Prep Time</p><p style="font-size:1.4rem;font-weight:700">{{ selectedRecipe.prepTime }}<span style="font-size:0.9rem"> min</span></p></div><div><p class="meta-label">Servings</p><p style="font-size:1.4rem;font-weight:700">{{ selectedRecipe.servings }}<span style="font-size:0.9rem"> ppl</span></p></div></div></div>
      </div>
      <div class="card">
        <h3 style="margin-bottom:16px">Ingredients</h3>
        <div class="ingredients-list">
          <div v-for="ing in selectedRecipe.ingredients" :key="ing.ingredientId" class="ing-row">
            <span style="color:var(--primary);font-size:0.6rem">●</span>
            <span style="flex:1;font-weight:500">{{ ing.name }}</span>
            <span style="color:var(--text-secondary);font-size:0.9rem">{{ ing.quantity }} {{ ing.unit }}</span>
          </div>
        </div>
        <div style="margin-top:20px;padding-top:16px;border-top:1px solid var(--border)">
          <p style="margin-bottom:10px;font-weight:500">Add all ingredients to a shopping list:</p>
          <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center">
            <select class="form-control" style="max-width:240px" v-model="targetListId">
              <option value="">-- Select a list --</option>
              <option v-for="list in shoppingLists" :key="list.id" :value="list.id">{{ list.name }}</option>
            </select>
            <button class="btn btn-primary" @click="addToList" :disabled="!targetListId">🛒 Add to List</button>
            <button class="btn btn-secondary" @click="createAndAdd">+ New List &amp; Add</button>
          </div>
          <p v-if="addedMessage" style="margin-top:10px;color:var(--primary-dark);font-weight:500">{{ addedMessage }}</p>
        </div>
      </div>
    </div>
    <div v-if="showAddRecipe" class="modal-overlay" @click.self="showAddRecipe = false">
      <div class="modal">
        <div class="modal-header"><h2 class="modal-title">Add New Recipe</h2><button class="modal-close" @click="showAddRecipe = false">×</button></div>
        <div class="form-group"><label>Recipe Name *</label><input class="form-control" v-model="newRecipe.name" placeholder="e.g. Spaghetti Bolognese" /></div>
        <div class="form-group"><label>Description</label><input class="form-control" v-model="newRecipe.description" /></div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
          <div class="form-group"><label>Category</label><input class="form-control" v-model="newRecipe.category" /></div>
          <div class="form-group"><label>Prep Time (min)</label><input class="form-control" type="number" v-model="newRecipe.prepTime" min="0" /></div>
          <div class="form-group"><label>Servings</label><input class="form-control" type="number" v-model="newRecipe.servings" min="1" /></div>
        </div>
        <button class="btn btn-primary" style="width:100%;margin-top:8px" @click="saveRecipe" :disabled="!newRecipe.name.trim()">Create Recipe</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
import { useRecipesStore } from '../stores/recipes'
import { useShoppingListsStore } from '../stores/shoppingLists'
import RecipeCard from '../components/RecipeCard.vue'
const recipesStore = useRecipesStore(); const listsStore = useShoppingListsStore()
const recipes = computed(() => recipesStore.recipes); const shoppingLists = computed(() => listsStore.lists)
const selectedRecipe = ref(null); const targetListId = ref(''); const addedMessage = ref(''); const showAddRecipe = ref(false)
const newRecipe = ref({ name: '', description: '', category: '', prepTime: 30, servings: 4 })
function viewRecipe(recipe) { selectedRecipe.value = recipe; targetListId.value = ''; addedMessage.value = '' }
function addToList() { if (!targetListId.value) return; listsStore.addRecipeIngredientsToList(targetListId.value, selectedRecipe.value.ingredients); const list = listsStore.getList(targetListId.value); addedMessage.value = `✅ Added to "${list.name}"!`; setTimeout(() => addedMessage.value = '', 3000) }
function createAndAdd() { const list = listsStore.createList(`${selectedRecipe.value.name} Ingredients`); listsStore.addRecipeIngredientsToList(list.id, selectedRecipe.value.ingredients); addedMessage.value = `✅ Created "${list.name}"!`; setTimeout(() => addedMessage.value = '', 3000) }
function deleteRecipe(id) { if (confirm('Delete this recipe?')) { recipesStore.deleteRecipe(id); selectedRecipe.value = null } }
function openAddRecipe() { newRecipe.value = { name: '', description: '', category: '', prepTime: 30, servings: 4 }; showAddRecipe.value = true }
function saveRecipe() { if (!newRecipe.value.name.trim()) return; const r = recipesStore.addRecipe({ ...newRecipe.value, ingredients: [] }); showAddRecipe.value = false; viewRecipe(r) }
</script>
<style scoped>
.meta-label { font-size: 0.8rem; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px; font-weight: 600; }
.ingredients-list { display: flex; flex-direction: column; gap: 8px; }
.ing-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid var(--border); }
.ing-row:last-child { border-bottom: none; }
</style>
