<template>
  <div>
    <div class="d-flex align-center justify-space-between mb-4 flex-wrap ga-3">
      <h1 class="text-h5 font-weight-bold">🥦 Ingredients</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="openAddModal">Add Ingredient</v-btn>
    </div>

    <v-row dense class="mb-4">
      <v-col cols="12" sm="6" md="4">
        <v-text-field
          v-model="search"
          label="Search ingredients..."
          variant="outlined"
          density="compact"
          prepend-inner-icon="mdi-magnify"
          clearable
          hide-details
        />
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <v-select
          v-model="selectedCategory"
          :items="['All Categories', ...categories]"
          label="Category"
          variant="outlined"
          density="compact"
          hide-details
        />
      </v-col>
    </v-row>

    <v-empty-state
      v-if="filteredIngredients.length === 0"
      icon="mdi-food-off"
      title="No ingredients found"
    />

    <!-- Desktop table -->
    <v-card v-else class="d-none d-md-block">
      <v-table>
        <thead>
          <tr>
            <th>Name</th><th>Category</th><th>Price</th>
            <th>Calories</th><th>Protein</th><th>Carbs</th><th>Fat</th><th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ing in filteredIngredients" :key="ing.id">
            <td><strong>{{ ing.name }}</strong></td>
            <td><v-chip size="x-small" color="blue-lighten-4" text-color="blue-darken-3">{{ ing.category }}</v-chip></td>
            <td>{{ ing.price }} RON/{{ ing.unit }}</td>
            <td>{{ ing.calories }} kcal</td>
            <td>{{ ing.protein }}g</td>
            <td>{{ ing.carbs }}g</td>
            <td>{{ ing.fat }}g</td>
            <td>
              <v-btn icon="mdi-pencil" variant="text" size="small" @click="openEditModal(ing)" />
              <v-btn icon="mdi-delete" variant="text" color="error" size="small" @click="deleteIngredient(ing.id)" />
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>

    <!-- Mobile cards -->
    <v-row class="d-md-none">
      <v-col v-for="ing in filteredIngredients" :key="ing.id" cols="12" sm="6">
        <v-card>
          <v-card-title class="d-flex align-center justify-space-between pb-1">
            <span class="text-subtitle-2 font-weight-semibold">{{ ing.name }}</span>
            <v-chip size="x-small" color="blue-lighten-4" text-color="blue-darken-3">{{ ing.category }}</v-chip>
          </v-card-title>
          <v-card-text class="pt-1">
            <v-row dense class="text-caption text-medium-emphasis">
              <v-col cols="6">💰 {{ ing.price }} RON/{{ ing.unit }}</v-col>
              <v-col cols="6">🔥 {{ ing.calories }} kcal</v-col>
              <v-col cols="4">P: {{ ing.protein }}g</v-col>
              <v-col cols="4">C: {{ ing.carbs }}g</v-col>
              <v-col cols="4">F: {{ ing.fat }}g</v-col>
            </v-row>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <v-btn prepend-icon="mdi-pencil" size="small" variant="text" @click="openEditModal(ing)">Edit</v-btn>
            <v-btn prepend-icon="mdi-delete" size="small" variant="text" color="error" @click="deleteIngredient(ing.id)">Delete</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Add/Edit dialog -->
    <v-dialog v-model="showModal" max-width="520">
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
          <span>{{ editingId ? 'Edit' : 'Add' }} Ingredient</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showModal = false" />
        </v-card-title>
        <v-card-text>
          <v-text-field v-model="form.name" label="Name *" variant="outlined" density="compact" class="mb-2" />
          <v-row dense>
            <v-col cols="6"><v-text-field v-model="form.category" label="Category" variant="outlined" density="compact" /></v-col>
            <v-col cols="6"><v-text-field v-model="form.unit" label="Unit" variant="outlined" density="compact" /></v-col>
            <v-col cols="6"><v-text-field v-model="form.price" label="Price (RON)" type="number" variant="outlined" density="compact" min="0" step="0.01" /></v-col>
            <v-col cols="6"><v-text-field v-model="form.calories" label="Calories" type="number" variant="outlined" density="compact" min="0" /></v-col>
            <v-col cols="4"><v-text-field v-model="form.protein" label="Protein (g)" type="number" variant="outlined" density="compact" min="0" step="0.1" /></v-col>
            <v-col cols="4"><v-text-field v-model="form.carbs" label="Carbs (g)" type="number" variant="outlined" density="compact" min="0" step="0.1" /></v-col>
            <v-col cols="4"><v-text-field v-model="form.fat" label="Fat (g)" type="number" variant="outlined" density="compact" min="0" step="0.1" /></v-col>
          </v-row>
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showModal = false">Cancel</v-btn>
          <v-btn color="primary" :disabled="!form.name.trim()" @click="saveIngredient">{{ editingId ? 'Update' : 'Add' }} Ingredient</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useIngredientsStore } from '../stores/ingredients'
const store = useIngredientsStore()
const search = ref(''); const selectedCategory = ref('All Categories'); const showModal = ref(false); const editingId = ref(null)
const form = ref({ name: '', category: '', unit: '', price: 0, calories: 0, protein: 0, carbs: 0, fat: 0 })
const categories = computed(() => [...new Set(store.ingredients.map(i => i.category))].sort())
const filteredIngredients = computed(() => store.ingredients.filter(i =>
  i.name.toLowerCase().includes(search.value.toLowerCase()) &&
  (selectedCategory.value === 'All Categories' || i.category === selectedCategory.value)
))
function openAddModal() { editingId.value = null; form.value = { name: '', category: '', unit: 'kg', price: 0, calories: 0, protein: 0, carbs: 0, fat: 0 }; showModal.value = true }
function openEditModal(ing) { editingId.value = ing.id; form.value = { ...ing }; showModal.value = true }
function saveIngredient() { if (!form.value.name.trim()) return; editingId.value ? store.updateIngredient(editingId.value, { ...form.value }) : store.addIngredient({ ...form.value }); showModal.value = false }
function deleteIngredient(id) { if (confirm('Delete this ingredient?')) store.deleteIngredient(id) }
</script>

