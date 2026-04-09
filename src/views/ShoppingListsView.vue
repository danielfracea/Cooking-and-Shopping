<template>
  <div>
    <div class="d-flex align-center justify-space-between mb-6 flex-wrap ga-3">
      <h1 class="text-h5 font-weight-bold">{{ t('shoppingLists.title') }}</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="showCreate = true">{{ t('shoppingLists.newList') }}</v-btn>
    </div>

    <v-empty-state
      v-if="lists.length === 0"
      icon="mdi-cart-off"
      :title="t('shoppingLists.empty.title')"
      :text="t('shoppingLists.empty.text')"
    >
      <template #actions>
        <v-btn color="primary" @click="showCreate = true">{{ t('shoppingLists.empty.action') }}</v-btn>
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
          <span>{{ t('shoppingLists.dialog.title') }}</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showCreate = false" />
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="newListName"
            :label="t('shoppingLists.dialog.label')"
            variant="outlined"
            :placeholder="t('shoppingLists.dialog.placeholder')"
            autofocus
            @keyup.enter="createList"
          />
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showCreate = false">{{ t('shoppingLists.dialog.cancel') }}</v-btn>
          <v-btn color="primary" :disabled="!newListName.trim()" @click="createList">{{ t('shoppingLists.dialog.create') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useShoppingListsStore } from '../stores/shoppingLists'
import ShoppingListCard from '../components/ShoppingListCard.vue'
const { t } = useI18n()
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
function deleteList(id) { if (confirm(t('shoppingLists.confirm.delete'))) store.deleteList(id) }
</script>

