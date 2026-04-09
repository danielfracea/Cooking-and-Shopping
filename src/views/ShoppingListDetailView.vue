<template>
  <div>
    <v-empty-state v-if="!list" icon="mdi-alert-circle-outline" title="List not found">
      <template #actions>
        <v-btn :to="'/shopping-lists'" variant="outlined">Back to Lists</v-btn>
      </template>
    </v-empty-state>

    <div v-else>
      <div class="d-flex align-center justify-space-between mb-4 flex-wrap ga-3">
        <div class="d-flex align-center ga-3">
          <v-btn :to="'/shopping-lists'" variant="outlined" size="small" prepend-icon="mdi-arrow-left">Back</v-btn>
          <h1 class="text-h5 font-weight-bold">{{ list.name }}</h1>
        </div>
        <v-btn color="primary" prepend-icon="mdi-plus" @click="showAddItem = true">Add Item</v-btn>
      </div>

      <v-card class="mb-4">
        <v-card-text>
          <div class="d-flex align-center justify-space-between mb-2">
            <v-chip color="primary" variant="tonal" size="small">{{ checkedCount }}/{{ list.items.length }} items</v-chip>
          </div>
          <v-progress-linear
            v-if="list.items.length > 0"
            :model-value="progressPercent"
            color="primary"
            rounded
            height="8"
          />
        </v-card-text>
      </v-card>

      <v-empty-state
        v-if="list.items.length === 0"
        icon="mdi-clipboard-list-outline"
        title="No items yet"
        text="Add your first item to this list."
      >
        <template #actions>
          <v-btn color="primary" @click="showAddItem = true">Add Your First Item</v-btn>
        </template>
      </v-empty-state>

      <v-card v-else>
        <v-list>
          <v-list-item
            v-for="item in list.items"
            :key="item.id"
            :class="{ 'text-decoration-line-through text-medium-emphasis': item.checked }"
          >
            <template #prepend>
              <v-checkbox-btn
                :model-value="item.checked"
                color="primary"
                @update:model-value="toggleItem(item.id)"
              />
            </template>
            <v-list-item-title>{{ item.name }}</v-list-item-title>
            <v-list-item-subtitle v-if="item.quantity || item.unit">
              {{ item.quantity }} {{ item.unit }}
            </v-list-item-subtitle>
            <template #append>
              <v-btn icon="mdi-delete" variant="text" color="error" size="small" @click="removeItem(item.id)" />
            </template>
          </v-list-item>
        </v-list>
      </v-card>
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

