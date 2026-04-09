<template>
  <div>
    <v-empty-state
      v-if="error"
      icon="mdi-alert-circle-outline"
      :title="t('sharedList.error.title')"
      :text="t('sharedList.error.text')"
    >
      <template #actions>
        <v-btn :to="'/shopping-lists'" variant="outlined">{{ t('sharedList.error.action') }}</v-btn>
      </template>
    </v-empty-state>

    <div v-else-if="sharedList">
      <div class="d-flex align-center justify-space-between mb-4 flex-wrap ga-3">
        <div class="d-flex align-center ga-3">
          <v-btn :to="'/shopping-lists'" variant="outlined" size="small" prepend-icon="mdi-arrow-left">{{ t('sharedList.myLists') }}</v-btn>
          <div>
            <h1 class="text-h5 font-weight-bold">{{ sharedList.name }}</h1>
            <span class="text-caption text-medium-emphasis">{{ t('sharedList.readOnly') }}</span>
          </div>
        </div>
        <v-btn color="primary" prepend-icon="mdi-import" :loading="importing" @click="importList">
          {{ t('sharedList.import') }}
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
        :title="t('sharedList.empty.title')"
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
        {{ t('sharedList.snackbar.imported') }}
        <template #actions>
          <v-btn variant="text" @click="router.push('/shopping-lists')">{{ t('sharedList.snackbar.view') }}</v-btn>
        </template>
      </v-snackbar>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useShoppingListsStore } from '../stores/shoppingLists'

const { t } = useI18n()
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
