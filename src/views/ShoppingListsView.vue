<template>
  <div>
    <div class="d-flex align-center justify-space-between mb-6 flex-wrap ga-3">
      <h1 class="text-h5 font-weight-bold">🛒 Shopping Lists</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="showCreate = true">New List</v-btn>
    </div>

    <v-empty-state
      v-if="lists.length === 0"
      icon="mdi-cart-off"
      title="No shopping lists yet"
      text="Create your first shopping list to get started."
    >
      <template #actions>
        <v-btn color="primary" @click="showCreate = true">Create Your First List</v-btn>
      </template>
    </v-empty-state>

    <v-row v-else>
      <v-col v-for="list in lists" :key="list.id" cols="12" sm="6" md="4">
        <ShoppingListCard :list="list" @open="openList" @delete="deleteList" />
      </v-col>
    </v-row>

    <v-dialog v-model="showCreate" max-width="480">
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
          <span>Create New List</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showCreate = false" />
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="newListName"
            label="List Name *"
            variant="outlined"
            placeholder="e.g. Weekly Groceries"
            autofocus
            @keyup.enter="createList"
          />
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showCreate = false">Cancel</v-btn>
          <v-btn color="primary" :disabled="!newListName.trim()" @click="createList">Create List</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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

