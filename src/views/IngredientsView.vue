<template>
  <div>
    <div class="d-flex align-center justify-space-between mb-4 flex-wrap ga-3">
      <h1 class="text-h5 font-weight-bold">{{ t('ingredients.title') }}</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="openAddModal">{{ t('ingredients.add') }}</v-btn>
    </div>

    <v-row dense class="mb-4">
      <v-col cols="12" sm="6" md="4">
        <v-text-field
          v-model="search"
          :label="t('ingredients.search')"
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
          :items="categoryItems"
          item-title="title"
          item-value="value"
          :label="t('ingredients.category')"
          variant="outlined"
          density="compact"
          hide-details
        />
      </v-col>
    </v-row>

    <v-empty-state
      v-if="filteredIngredients.length === 0"
      icon="mdi-food-off"
      :title="t('ingredients.empty.title')"
    />

    <!-- Desktop table -->
    <v-card v-else class="d-none d-md-block">
      <v-table>
        <thead>
          <tr>
            <th>{{ t('ingredients.table.name') }}</th>
            <th>{{ t('ingredients.table.category') }}</th>
            <th>{{ t('ingredients.table.price') }}</th>
            <th>{{ t('ingredients.table.calories') }}</th>
            <th>{{ t('ingredients.table.protein') }}</th>
            <th>{{ t('ingredients.table.carbs') }}</th>
            <th>{{ t('ingredients.table.fat') }}</th>
            <th>{{ t('ingredients.table.actions') }}</th>
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
            <v-btn prepend-icon="mdi-pencil" size="small" variant="text" @click="openEditModal(ing)">{{ t('ingredients.actions.edit') }}</v-btn>
            <v-btn prepend-icon="mdi-delete" size="small" variant="text" color="error" @click="deleteIngredient(ing.id)">{{ t('ingredients.actions.delete') }}</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Add/Edit dialog -->
    <v-dialog v-model="showModal" max-width="520">
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
          <span>{{ editingId ? t('ingredients.dialog.editTitle') : t('ingredients.dialog.addTitle') }}</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showModal = false" />
        </v-card-title>
        <v-card-text>
          <v-text-field v-model="form.name" :label="t('ingredients.dialog.name')" variant="outlined" density="compact" class="mb-2" />
          <v-row dense>
            <v-col cols="6"><v-text-field v-model="form.category" :label="t('ingredients.dialog.category')" variant="outlined" density="compact" /></v-col>
            <v-col cols="6"><v-text-field v-model="form.unit" :label="t('ingredients.dialog.unit')" variant="outlined" density="compact" /></v-col>
            <v-col cols="6"><v-text-field v-model="form.price" :label="t('ingredients.dialog.price')" type="number" variant="outlined" density="compact" min="0" step="0.01" /></v-col>
            <v-col cols="6"><v-text-field v-model="form.calories" :label="t('ingredients.dialog.calories')" type="number" variant="outlined" density="compact" min="0" /></v-col>
            <v-col cols="4"><v-text-field v-model="form.protein" :label="t('ingredients.dialog.protein')" type="number" variant="outlined" density="compact" min="0" step="0.1" /></v-col>
            <v-col cols="4"><v-text-field v-model="form.carbs" :label="t('ingredients.dialog.carbs')" type="number" variant="outlined" density="compact" min="0" step="0.1" /></v-col>
            <v-col cols="4"><v-text-field v-model="form.fat" :label="t('ingredients.dialog.fat')" type="number" variant="outlined" density="compact" min="0" step="0.1" /></v-col>
          </v-row>
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showModal = false">{{ t('ingredients.dialog.cancel') }}</v-btn>
          <v-btn color="primary" :disabled="!form.name.trim()" @click="saveIngredient">{{ editingId ? t('ingredients.dialog.update') : t('ingredients.dialog.add') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useIngredientsStore } from '../stores/ingredients'
const { t } = useI18n()
const store = useIngredientsStore()
const search = ref('')
const selectedCategory = ref(null)
const showModal = ref(false)
const editingId = ref(null)
const form = ref({ name: '', category: '', unit: '', price: 0, calories: 0, protein: 0, carbs: 0, fat: 0 })
const categories = computed(() => [...new Set(store.ingredients.map(i => i.category))].sort())
const categoryItems = computed(() => [
  { title: t('ingredients.allCategories'), value: null },
  ...categories.value.map(c => ({ title: c, value: c })),
])
const filteredIngredients = computed(() => store.ingredients.filter(i =>
  i.name.toLowerCase().includes(search.value.toLowerCase()) &&
  (selectedCategory.value === null || i.category === selectedCategory.value)
))
function openAddModal() { editingId.value = null; form.value = { name: '', category: '', unit: 'kg', price: 0, calories: 0, protein: 0, carbs: 0, fat: 0 }; showModal.value = true }
function openEditModal(ing) { editingId.value = ing.id; form.value = { ...ing }; showModal.value = true }
function saveIngredient() { if (!form.value.name.trim()) return; editingId.value ? store.updateIngredient(editingId.value, { ...form.value }) : store.addIngredient({ ...form.value }); showModal.value = false }
function deleteIngredient(id) { if (confirm(t('ingredients.confirm.delete'))) store.deleteIngredient(id) }
</script>

