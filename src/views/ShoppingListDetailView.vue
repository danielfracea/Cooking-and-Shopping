<template>
  <div>
    <div v-if="!list" class="empty-state">
      <p>List not found</p>
      <RouterLink to="/shopping-lists" class="btn btn-secondary">Back to Lists</RouterLink>
    </div>
    <div v-else>
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px">
          <RouterLink to="/shopping-lists" class="btn btn-secondary btn-sm">← Back</RouterLink>
          <h1 class="page-title">{{ list.name }}</h1>
        </div>
        <button class="btn btn-primary" @click="showAddItem = true">+ Add Item</button>
      </div>
      <div class="list-stats card" style="margin-bottom:20px">
        <span class="badge badge-green">{{ checkedCount }}/{{ list.items.length }} items</span>
        <div class="progress-bar" style="margin-top:8px" v-if="list.items.length > 0">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>
      <div v-if="list.items.length === 0" class="empty-state">
        <div class="icon">📝</div>
        <p>No items yet</p>
        <button class="btn btn-primary" @click="showAddItem = true">Add Your First Item</button>
      </div>
      <div v-else class="items-list">
        <div v-for="item in list.items" :key="item.id" :class="['item-row', { checked: item.checked }]">
          <button class="check-btn" @click="toggleItem(item.id)">{{ item.checked ? '✅' : '⬜' }}</button>
          <div class="item-info">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-qty" v-if="item.quantity || item.unit">{{ item.quantity }} {{ item.unit }}</span>
          </div>
          <button class="btn btn-danger btn-sm" @click="removeItem(item.id)">🗑</button>
        </div>
      </div>
    </div>
    <AddItemModal v-if="showAddItem" @close="showAddItem = false" @add-item="addItem" @add-recipe-ingredients="addRecipeIngredients" />
  </div>
</template>
<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useShoppingListsStore } from '../stores/shoppingLists'
import AddItemModal from '../components/AddItemModal.vue'
const route = useRoute()
const store = useShoppingListsStore()
const showAddItem = ref(false)
const list = computed(() => store.getList(route.params.id))
const checkedCount = computed(() => list.value?.items.filter(i => i.checked).length ?? 0)
const progressPercent = computed(() => list.value?.items.length > 0 ? (checkedCount.value / list.value.items.length) * 100 : 0)
function toggleItem(itemId) { store.toggleItem(route.params.id, itemId) }
function removeItem(itemId) { store.removeItemFromList(route.params.id, itemId) }
function addItem(item) { store.addItemToList(route.params.id, item) }
function addRecipeIngredients(ingredients) { store.addRecipeIngredientsToList(route.params.id, ingredients) }
</script>
<style scoped>
.items-list { background: var(--surface); border-radius: var(--radius); border: 1px solid var(--border); overflow: hidden; box-shadow: var(--shadow); }
.item-row { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border-bottom: 1px solid var(--border); transition: background 0.2s; }
.item-row:last-child { border-bottom: none; }
.item-row.checked .item-name { text-decoration: line-through; color: var(--text-secondary); }
.item-row.checked { background: #F9F9F9; }
.check-btn { background: none; border: none; cursor: pointer; font-size: 1.2rem; padding: 0; line-height: 1; }
.item-info { flex: 1; display: flex; flex-direction: column; }
.item-name { font-weight: 500; }
.item-qty { font-size: 0.8rem; color: var(--text-secondary); }
.progress-bar { height: 8px; background: var(--border); border-radius: 4px; overflow: hidden; }
.progress-fill { height: 100%; background: var(--primary); border-radius: 4px; transition: width 0.3s; }
</style>
