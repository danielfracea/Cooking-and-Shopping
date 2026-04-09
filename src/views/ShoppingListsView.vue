<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">🛒 Shopping Lists</h1>
      <button class="btn btn-primary" @click="showCreate = true">+ New List</button>
    </div>
    <div v-if="lists.length === 0" class="empty-state">
      <div class="icon">🛍️</div>
      <p>No shopping lists yet</p>
      <button class="btn btn-primary" @click="showCreate = true">Create Your First List</button>
    </div>
    <div v-else class="grid">
      <ShoppingListCard v-for="list in lists" :key="list.id" :list="list" @open="openList" @delete="deleteList" />
    </div>
    <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
      <div class="modal">
        <div class="modal-header">
          <h2 class="modal-title">Create New List</h2>
          <button class="modal-close" @click="showCreate = false">×</button>
        </div>
        <div class="form-group">
          <label>List Name *</label>
          <input class="form-control" v-model="newListName" placeholder="e.g. Weekly Groceries" @keyup.enter="createList" />
        </div>
        <button class="btn btn-primary" style="width:100%" @click="createList" :disabled="!newListName.trim()">Create List</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useShoppingListsStore } from '../stores/shoppingLists'
import ShoppingListCard from '../components/ShoppingListCard.vue'
const router = useRouter()
const store = useShoppingListsStore()
const lists = computed(() => store.lists)
const showCreate = ref(false)
const newListName = ref('')
function createList() {
  if (!newListName.value.trim()) return
  const list = store.createList(newListName.value.trim())
  newListName.value = ''; showCreate.value = false
  router.push(`/shopping-lists/${list.id}`)
}
function openList(list) { router.push(`/shopping-lists/${list.id}`) }
function deleteList(id) { if (confirm('Delete this shopping list?')) store.deleteList(id) }
</script>
