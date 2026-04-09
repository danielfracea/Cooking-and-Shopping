<template>
  <div>
    <v-empty-state
      v-if="error"
      icon="mdi-alert-circle-outline"
      title="Invalid share link"
      text="This link is invalid or has expired."
    >
      <template #actions>
        <v-btn :to="'/shopping-lists'" variant="outlined">Go to My Lists</v-btn>
      </template>
    </v-empty-state>

    <div v-else-if="sharedList">
      <div class="d-flex align-center justify-space-between mb-4 flex-wrap ga-3">
        <div class="d-flex align-center ga-3">
          <v-btn :to="'/shopping-lists'" variant="outlined" size="small" prepend-icon="mdi-arrow-left">My Lists</v-btn>
          <div>
            <h1 class="text-h5 font-weight-bold">{{ sharedList.name }}</h1>
            <span class="text-caption text-medium-emphasis">Shared list (read-only)</span>
          </div>
        </div>
        <v-btn color="primary" prepend-icon="mdi-import" :loading="importing" @click="importList">
          Import to My Lists
        </v-btn>
      </div>

      <v-card class="mb-4">
        <v-card-text>
          <v-chip color="primary" variant="tonal" size="small">{{ sharedList.items.length }} items</v-chip>
        </v-card-text>
      </v-card>

      <v-empty-state
        v-if="sharedList.items.length === 0"
        icon="mdi-clipboard-list-outline"
        title="No items in this list"
      />

      <v-card v-else>
        <v-list>
          <v-list-item v-for="item in sharedList.items" :key="item.id">
            <template #prepend>
              <v-icon icon="mdi-circle-small" />
            </template>
            <v-list-item-title>{{ item.name }}</v-list-item-title>
            <v-list-item-subtitle v-if="item.quantity || item.unit">
              {{ item.quantity }} {{ item.unit }}
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card>

      <v-snackbar v-model="snackbar" color="success" :timeout="3000">
        List imported! You can now find it in your lists.
        <template #actions>
          <v-btn variant="text" @click="router.push('/shopping-lists')">View</v-btn>
        </template>
      </v-snackbar>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useShoppingListsStore } from '../stores/shoppingLists'

const route = useRoute()
const router = useRouter()
const store = useShoppingListsStore()

const sharedList = ref(null)
const error = ref(false)
const importing = ref(false)
const snackbar = ref(false)

onMounted(() => {
  try {
    const data = route.query.data
    if (!data) throw new Error('No data')
    const decoded = new TextDecoder().decode(Uint8Array.from(atob(data), c => c.charCodeAt(0)))
    sharedList.value = JSON.parse(decoded)
    if (!sharedList.value?.name || !Array.isArray(sharedList.value?.items)) throw new Error('Invalid')
  } catch {
    error.value = true
  }
})

function importList() {
  importing.value = true
  store.importList(sharedList.value)
  importing.value = false
  snackbar.value = true
}
</script>
