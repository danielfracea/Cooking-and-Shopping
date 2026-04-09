<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">🥦 Ingredients</h1>
      <button class="btn btn-primary" @click="openAddModal">+ Add Ingredient</button>
    </div>
    <div class="filters" style="margin-bottom:16px;display:flex;gap:12px;flex-wrap:wrap">
      <input class="form-control" style="max-width:260px" v-model="search" placeholder="Search ingredients..." />
      <select class="form-control" style="max-width:180px" v-model="selectedCategory">
        <option value="">All Categories</option>
        <option v-for="cat in categories" :key="cat">{{ cat }}</option>
      </select>
    </div>
    <div v-if="filteredIngredients.length === 0" class="empty-state"><div class="icon">🥗</div><p>No ingredients found</p></div>
    <div v-else class="ingredients-table card" style="padding:0">
      <table style="width:100%;border-collapse:collapse">
        <thead><tr style="background:var(--background)"><th>Name</th><th>Category</th><th>Price</th><th>Calories</th><th>Protein</th><th>Carbs</th><th>Fat</th><th>Actions</th></tr></thead>
        <tbody>
          <tr v-for="ing in filteredIngredients" :key="ing.id">
            <td><strong>{{ ing.name }}</strong></td>
            <td><span class="tag">{{ ing.category }}</span></td>
            <td>${{ ing.price }}/{{ ing.unit }}</td>
            <td>{{ ing.calories }} kcal</td><td>{{ ing.protein }}g</td><td>{{ ing.carbs }}g</td><td>{{ ing.fat }}g</td>
            <td><div style="display:flex;gap:6px"><button class="btn btn-secondary btn-sm" @click="openEditModal(ing)">✏️</button><button class="btn btn-danger btn-sm" @click="deleteIngredient(ing.id)">🗑</button></div></td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header"><h2 class="modal-title">{{ editingId ? 'Edit' : 'Add' }} Ingredient</h2><button class="modal-close" @click="showModal = false">×</button></div>
        <div class="form-group"><label>Name *</label><input class="form-control" v-model="form.name" placeholder="e.g. Tomato" /></div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
          <div class="form-group"><label>Category</label><input class="form-control" v-model="form.category" /></div>
          <div class="form-group"><label>Unit</label><input class="form-control" v-model="form.unit" /></div>
          <div class="form-group"><label>Price ($)</label><input class="form-control" type="number" v-model="form.price" min="0" step="0.01" /></div>
          <div class="form-group"><label>Calories</label><input class="form-control" type="number" v-model="form.calories" min="0" /></div>
          <div class="form-group"><label>Protein (g)</label><input class="form-control" type="number" v-model="form.protein" min="0" step="0.1" /></div>
          <div class="form-group"><label>Carbs (g)</label><input class="form-control" type="number" v-model="form.carbs" min="0" step="0.1" /></div>
          <div class="form-group"><label>Fat (g)</label><input class="form-control" type="number" v-model="form.fat" min="0" step="0.1" /></div>
        </div>
        <button class="btn btn-primary" style="width:100%;margin-top:8px" @click="saveIngredient" :disabled="!form.name.trim()">{{ editingId ? 'Update' : 'Add' }} Ingredient</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
import { useIngredientsStore } from '../stores/ingredients'
const store = useIngredientsStore()
const search = ref(''); const selectedCategory = ref(''); const showModal = ref(false); const editingId = ref(null)
const form = ref({ name: '', category: '', unit: '', price: 0, calories: 0, protein: 0, carbs: 0, fat: 0 })
const categories = computed(() => [...new Set(store.ingredients.map(i => i.category))].sort())
const filteredIngredients = computed(() => store.ingredients.filter(i => i.name.toLowerCase().includes(search.value.toLowerCase()) && (!selectedCategory.value || i.category === selectedCategory.value)))
function openAddModal() { editingId.value = null; form.value = { name: '', category: '', unit: 'kg', price: 0, calories: 0, protein: 0, carbs: 0, fat: 0 }; showModal.value = true }
function openEditModal(ing) { editingId.value = ing.id; form.value = { ...ing }; showModal.value = true }
function saveIngredient() { if (!form.value.name.trim()) return; editingId.value ? store.updateIngredient(editingId.value, { ...form.value }) : store.addIngredient({ ...form.value }); showModal.value = false }
function deleteIngredient(id) { if (confirm('Delete this ingredient?')) store.deleteIngredient(id) }
</script>
<style scoped>
table th, table td { padding: 12px 16px; text-align: left; border-bottom: 1px solid var(--border); font-size: 0.9rem; }
table th { font-weight: 600; font-size: 0.8rem; color: var(--text-secondary); text-transform: uppercase; }
table tr:last-child td { border-bottom: none; }
table tr:hover td { background: var(--background); }
</style>
