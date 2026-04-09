<template>
  <v-dialog model-value max-width="500" @update:model-value="$emit('close')">
    <v-card>
      <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
        <span>Add Item</span>
        <v-btn icon="mdi-close" variant="text" size="small" @click="$emit('close')" />
      </v-card-title>

      <v-tabs v-model="activeTab" color="primary" grow>
        <v-tab value="manual">Manual</v-tab>
        <v-tab value="ingredient">From Ingredients</v-tab>
        <v-tab value="recipe">From Recipe</v-tab>
      </v-tabs>

      <v-card-text class="pt-4">
        <v-tabs-window v-model="activeTab">
          <!-- Manual Tab -->
          <v-tabs-window-item value="manual">
            <v-combobox
              v-model="manualSelection"
              :items="ingredientsStore.ingredients"
              item-title="name"
              return-object
              label="Item Name *"
              variant="outlined"
              density="compact"
              class="mb-3"
              clearable
              @update:model-value="onManualSelectionChange"
            />
            <v-row dense>
              <v-col cols="6">
                <v-text-field v-model="manualItem.quantity" label="Quantity" type="number" variant="outlined" density="compact" min="0.1" step="0.1" />
              </v-col>
              <v-col cols="6">
                <v-text-field v-model="manualItem.unit" label="Unit" variant="outlined" density="compact" placeholder="kg, L, pcs..." />
              </v-col>
            </v-row>
            <v-btn color="primary" block @click="addManual" :disabled="!manualItemName.trim()">Add Item</v-btn>
          </v-tabs-window-item>

          <!-- From Ingredients Tab -->
          <v-tabs-window-item value="ingredient">
            <v-text-field v-model="ingredientSearch" label="Search ingredients..." variant="outlined" density="compact" prepend-inner-icon="mdi-magnify" class="mb-2" />
            <v-list density="compact" max-height="200" class="border rounded mb-2" style="overflow-y:auto">
              <v-list-item
                v-for="ing in filteredIngredients"
                :key="ing.id"
                :title="ing.name"
                :subtitle="ing.category"
                :active="selectedIngredient?.id === ing.id"
                active-color="primary"
                @click="selectIngredient(ing)"
              />
            </v-list>
            <v-row v-if="selectedIngredient" dense class="mt-2">
              <v-col cols="6">
                <v-text-field v-model="ingQuantity" label="Quantity" type="number" variant="outlined" density="compact" min="0.1" step="0.1" />
              </v-col>
              <v-col cols="6">
                <v-text-field v-model="ingUnit" label="Unit" variant="outlined" density="compact" />
              </v-col>
            </v-row>
            <v-btn color="primary" block @click="addFromIngredient" :disabled="!selectedIngredient">
              Add {{ selectedIngredient?.name || 'Selected' }}
            </v-btn>
          </v-tabs-window-item>

          <!-- From Recipe Tab -->
          <v-tabs-window-item value="recipe">
            <v-list density="compact" max-height="200" class="border rounded mb-3" style="overflow-y:auto">
              <v-list-item
                v-for="recipe in recipes"
                :key="recipe.id"
                :title="recipe.name"
                :subtitle="recipe.ingredients.length + ' ingredients'"
                :active="selectedRecipe?.id === recipe.id"
                active-color="primary"
                @click="selectedRecipe = recipe"
              />
            </v-list>
            <v-card v-if="selectedRecipe" variant="tonal" color="primary" class="mb-3 pa-3">
              <p class="text-caption font-weight-bold mb-1">Ingredients:</p>
              <p v-for="ing in selectedRecipe.ingredients" :key="ing.ingredientId" class="text-caption">
                {{ ing.quantity }} {{ ing.unit }} {{ ing.name }}
              </p>
            </v-card>
            <v-btn color="primary" block @click="addFromRecipe" :disabled="!selectedRecipe">Add All Ingredients</v-btn>
          </v-tabs-window-item>
        </v-tabs-window>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useIngredientsStore } from '../stores/ingredients'
import { useRecipesStore } from '../stores/recipes'

const emit = defineEmits(['close', 'add-item', 'add-recipe-ingredients'])

const ingredientsStore = useIngredientsStore()
const recipesStore = useRecipesStore()

const activeTab = ref('manual')
const manualItem = ref({ quantity: 1, unit: '' })
// manualSelection can be a string (free text) or an ingredient object (picked from list)
const manualSelection = ref(null)
const ingredientSearch = ref('')
const selectedIngredient = ref(null)
const ingQuantity = ref(1)
const ingUnit = ref('')
const selectedRecipe = ref(null)

// Derive a plain name string from whatever the user typed or selected
const manualItemName = computed(() => {
  if (!manualSelection.value) return ''
  if (typeof manualSelection.value === 'string') return manualSelection.value
  return manualSelection.value.name
})

function onManualSelectionChange(val) {
  // When an existing ingredient is selected, pre-fill its unit;
  // when the user clears or types free text, reset the unit.
  if (val && typeof val === 'object') {
    manualItem.value.unit = val.unit || ''
  } else {
    manualItem.value.unit = ''
  }
}

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
  const name = manualItemName.value.trim()
  if (!name) return
  // Check if the typed/selected name matches an existing ingredient
  const matchedIngredient = ingredientsStore.ingredients.find(
    i => i.name.toLowerCase() === name.toLowerCase()
  )
  const item = {
    name: matchedIngredient ? matchedIngredient.name : name,
    quantity: manualItem.value.quantity,
    unit: manualItem.value.unit,
  }
  if (matchedIngredient) {
    item.ingredientId = matchedIngredient.id
  }
  emit('add-item', item)
  manualSelection.value = null
  manualItem.value = { quantity: 1, unit: '' }
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

