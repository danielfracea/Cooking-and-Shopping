<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <h2 class="modal-title">Add Item</h2>
        <button class="modal-close" @click="$emit('close')">×</button>
      </div>

      <div class="tab-bar">
        <button :class="['tab-btn', { active: activeTab === 'manual' }]" @click="activeTab = 'manual'">Manual</button>
        <button :class="['tab-btn', { active: activeTab === 'ingredient' }]" @click="activeTab = 'ingredient'">From Ingredients</button>
        <button :class="['tab-btn', { active: activeTab === 'recipe' }]" @click="activeTab = 'recipe'">From Recipe</button>
      </div>

      <!-- Manual Tab -->
      <div v-if="activeTab === 'manual'" class="tab-content">
        <div class="form-group">
          <label>Item Name *</label>
          <input class="form-control" v-model="manualItem.name" placeholder="e.g. Milk" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>Quantity</label>
            <input class="form-control" type="number" v-model="manualItem.quantity" min="0.1" step="0.1" />
          </div>
          <div class="form-group">
            <label>Unit</label>
            <input class="form-control" v-model="manualItem.unit" placeholder="kg, L, pcs..." />
          </div>
        </div>
        <button class="btn btn-primary" style="width:100%" @click="addManual" :disabled="!manualItem.name.trim()">Add Item</button>
      </div>

      <!-- From Ingredients Tab -->
      <div v-if="activeTab === 'ingredient'" class="tab-content">
        <input class="form-control" v-model="ingredientSearch" placeholder="Search ingredients..." style="margin-bottom:12px" />
        <div class="ingredient-list">
          <div
            v-for="ing in filteredIngredients"
            :key="ing.id"
            :class="['ingredient-option', { selected: selectedIngredient?.id === ing.id }]"
            @click="selectIngredient(ing)"
          >
            <span class="ing-name">{{ ing.name }}</span>
            <span class="ing-meta">{{ ing.category }}</span>
          </div>
        </div>
        <div v-if="selectedIngredient" class="form-row" style="margin-top:12px">
          <div class="form-group">
            <label>Quantity</label>
            <input class="form-control" type="number" v-model="ingQuantity" min="0.1" step="0.1" />
          </div>
          <div class="form-group">
            <label>Unit</label>
            <input class="form-control" v-model="ingUnit" />
          </div>
        </div>
        <button class="btn btn-primary" style="width:100%;margin-top:8px" @click="addFromIngredient" :disabled="!selectedIngredient">
          Add {{ selectedIngredient?.name || 'Selected' }}
        </button>
      </div>

      <!-- From Recipe Tab -->
      <div v-if="activeTab === 'recipe'" class="tab-content">
        <div class="recipe-list">
          <div
            v-for="recipe in recipes"
            :key="recipe.id"
            :class="['recipe-option', { selected: selectedRecipe?.id === recipe.id }]"
            @click="selectedRecipe = recipe"
          >
            <span class="recipe-name">{{ recipe.name }}</span>
            <span class="recipe-meta">{{ recipe.ingredients.length }} ingredients</span>
          </div>
        </div>
        <div v-if="selectedRecipe" class="recipe-preview">
          <h4>Ingredients:</h4>
          <ul>
            <li v-for="ing in selectedRecipe.ingredients" :key="ing.ingredientId">
              {{ ing.quantity }} {{ ing.unit }} {{ ing.name }}
            </li>
          </ul>
        </div>
        <button class="btn btn-primary" style="width:100%;margin-top:12px" @click="addFromRecipe" :disabled="!selectedRecipe">
          Add All Ingredients
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useIngredientsStore } from '../stores/ingredients'
import { useRecipesStore } from '../stores/recipes'

const emit = defineEmits(['close', 'add-item', 'add-recipe-ingredients'])

const ingredientsStore = useIngredientsStore()
const recipesStore = useRecipesStore()

const activeTab = ref('manual')
const manualItem = ref({ name: '', quantity: 1, unit: '' })
const ingredientSearch = ref('')
const selectedIngredient = ref(null)
const ingQuantity = ref(1)
const ingUnit = ref('')
const selectedRecipe = ref(null)

const filteredIngredients = computed(() =>
  ingredientsStore.ingredients.filter(i =>
    i.name.toLowerCase().includes(ingredientSearch.value.toLowerCase())
  )
)

const recipes = computed(() => recipesStore.recipes)

function selectIngredient(ing) {
  selectedIngredient.value = ing
  ingUnit.value = ing.unit
  ingQuantity.value = 1
}

function addManual() {
  if (!manualItem.value.name.trim()) return
  emit('add-item', { ...manualItem.value })
  manualItem.value = { name: '', quantity: 1, unit: '' }
  emit('close')
}

function addFromIngredient() {
  if (!selectedIngredient.value) return
  emit('add-item', {
    name: selectedIngredient.value.name,
    quantity: ingQuantity.value,
    unit: ingUnit.value,
    ingredientId: selectedIngredient.value.id,
  })
  emit('close')
}

function addFromRecipe() {
  if (!selectedRecipe.value) return
  emit('add-recipe-ingredients', selectedRecipe.value.ingredients)
  emit('close')
}
</script>

<style scoped>
.tab-bar {
  display: flex;
  border-bottom: 1px solid var(--border);
  margin-bottom: 16px;
  gap: 4px;
}

.tab-btn {
  padding: 8px 14px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.85rem;
  color: var(--text-secondary);
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  margin-bottom: -1px;
}

.tab-btn.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  font-weight: 600;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.ingredient-list, .recipe-list {
  max-height: 180px;
  overflow-y: auto;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
}

.ingredient-option, .recipe-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--border);
  transition: background 0.15s;
}

.ingredient-option:last-child, .recipe-option:last-child {
  border-bottom: none;
}

.ingredient-option:hover, .recipe-option:hover {
  background: var(--background);
}

.ingredient-option.selected, .recipe-option.selected {
  background: var(--primary-light);
}

.ing-name, .recipe-name {
  font-weight: 500;
}

.ing-meta, .recipe-meta {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.recipe-preview {
  background: var(--background);
  border-radius: var(--radius-sm);
  padding: 12px;
  margin-top: 10px;
}

.recipe-preview h4 {
  font-size: 0.85rem;
  margin-bottom: 8px;
  color: var(--text-secondary);
}

.recipe-preview ul {
  list-style: none;
  font-size: 0.9rem;
}

.recipe-preview li {
  padding: 2px 0;
}
</style>
