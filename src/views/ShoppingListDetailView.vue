<template>
  <div>
    <v-empty-state v-if="!list" icon="mdi-alert-circle-outline" :title="t('shoppingListDetail.notFound.title')">
      <template #actions>
        <v-btn :to="'/shopping-lists'" variant="outlined">{{ t('shoppingListDetail.notFound.back') }}</v-btn>
      </template>
    </v-empty-state>

    <div v-else>
      <div class="d-flex align-center justify-space-between mb-4 flex-wrap ga-3">
        <div class="d-flex align-center ga-3">
          <v-btn :to="'/shopping-lists'" variant="outlined" size="small" prepend-icon="mdi-arrow-left">{{ t('shoppingListDetail.back') }}</v-btn>
          <h1 class="text-h5 font-weight-bold">{{ list.name }}</h1>
        </div>
        <div class="d-flex ga-2">
          <v-btn variant="outlined" prepend-icon="mdi-share-variant" @click="shareList">{{ t('shoppingListDetail.share') }}</v-btn>
          <v-btn color="primary" prepend-icon="mdi-plus" @click="showAddItem = true">{{ t('shoppingListDetail.addItem') }}</v-btn>
        </div>
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

      <v-card class="mb-4 pa-3">
        <v-row dense align="center">
          <v-col>
            <v-combobox
              v-model="quickAddSelection"
              :items="ingredientsStore.ingredients"
              item-title="name"
              return-object
              :label="t('shoppingListDetail.quickAdd')"
              variant="outlined"
              density="compact"
              hide-details
              clearable
              prepend-inner-icon="mdi-magnify"
              @update:model-value="onQuickAddSelectionChange"
              @keyup.enter="submitQuickAdd"
            />
          </v-col>
          <v-col cols="auto">
            <v-btn
              :icon="isListening ? 'mdi-microphone-off' : 'mdi-microphone'"
              :color="isListening ? 'error' : 'default'"
              variant="tonal"
              size="small"
              :title="t('shoppingListDetail.speechToText')"
              @click="toggleSpeech"
            />
          </v-col>
          <v-col cols="auto">
            <v-btn
              color="primary"
              icon="mdi-plus"
              :disabled="!quickAddName.trim()"
              @click="submitQuickAdd"
            />
          </v-col>
        </v-row>
        <v-alert v-if="speechUnsupported" type="warning" density="compact" class="mt-2" variant="tonal">
          {{ t('shoppingListDetail.speechNotSupported') }}
        </v-alert>
        <v-alert v-if="speechError" type="error" density="compact" class="mt-2" variant="tonal">
          {{ t('shoppingListDetail.speechError') }}
        </v-alert>
        <v-alert v-if="isListening" type="info" density="compact" class="mt-2" variant="tonal" icon="mdi-microphone">
          {{ t('shoppingListDetail.speechListening') }}
        </v-alert>
      </v-card>

      <v-empty-state
        v-if="list.items.length === 0"
        icon="mdi-clipboard-list-outline"
        :title="t('shoppingListDetail.empty.title')"
        :text="t('shoppingListDetail.empty.text')"
      />

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
              {{ formatItemQuantity(item) }}
            </v-list-item-subtitle>
            <template #append>
              <v-btn icon="mdi-delete" variant="text" color="error" size="small" @click="removeItem(item.id)" />
            </template>
          </v-list-item>
        </v-list>
      </v-card>
    </div>

    <AddItemModal v-if="showAddItem" @close="showAddItem = false" @add-item="addItem" @add-recipe-ingredients="addRecipeIngredients" />

    <v-snackbar v-model="shareCopied" color="success" :timeout="3000">
      {{ t('shoppingListDetail.shareCopied') }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { computed, ref, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useShoppingListsStore } from '../stores/shoppingLists'
import { useIngredientsStore } from '../stores/ingredients'
import { useSettingsStore } from '../stores/settings'
import AddItemModal from '../components/AddItemModal.vue'
import { LOCALE_TO_SPEECH_LANG } from '../utils/speech.js'
const { t, locale } = useI18n()
const route = useRoute()
const store = useShoppingListsStore()
const ingredientsStore = useIngredientsStore()
const settingsStore = useSettingsStore()
const showAddItem = ref(false)
const shareCopied = ref(false)
const list = computed(() => store.getList(route.params.id))
const checkedCount = computed(() => list.value?.items.filter(i => i.checked).length ?? 0)
const progressPercent = computed(() => list.value?.items.length > 0 ? (checkedCount.value / list.value.items.length) * 100 : 0)
function formatItemQuantity(item) {
  const { quantity, unit } = settingsStore.displayQuantity(item.quantity, item.unit)
  return unit ? `${quantity} ${unit}` : quantity
}
function toggleItem(itemId) { store.toggleItem(route.params.id, itemId) }
function removeItem(itemId) { store.removeItemFromList(route.params.id, itemId) }
function addItem(item) { store.addItemToList(route.params.id, item) }
function addRecipeIngredients(ingredients) { store.addRecipeIngredientsToList(route.params.id, ingredients) }

// Quick-add inline input
const quickAddSelection = ref(null)
const quickAddUnit = ref('')
const quickAddName = computed(() => {
  if (!quickAddSelection.value) return ''
  if (typeof quickAddSelection.value === 'string') return quickAddSelection.value
  return quickAddSelection.value.name
})
function onQuickAddSelectionChange(val) {
  if (val && typeof val === 'object') {
    quickAddUnit.value = val.unit || ''
  } else {
    const matched = typeof val === 'string'
      ? ingredientsStore.ingredients.find(i => i.name.toLowerCase() === val.toLowerCase())
      : null
    quickAddUnit.value = matched?.unit ?? ''
  }
}
function resetQuickAdd() {
  quickAddSelection.value = null
  quickAddUnit.value = ''
}
function submitQuickAdd() {
  const name = quickAddName.value.trim()
  if (!name) return
  const matched = ingredientsStore.ingredients.find(i => i.name.toLowerCase() === name.toLowerCase())
  const item = {
    name: matched ? matched.name : name,
    quantity: 1,
    unit: quickAddUnit.value || (matched?.unit ?? ''),
  }
  if (matched) item.ingredientId = matched.id
  store.addItemToList(route.params.id, item)
  resetQuickAdd()
}
async function shareList() {
  const url = store.generateShareUrl(route.params.id)
  if (!url) return
  try {
    await navigator.clipboard.writeText(url)
    shareCopied.value = true
  } catch {
    prompt('Copy this link to share:', url)
  }
}

// ── Speech-to-text ────────────────────────────────────────────────────────────
const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
const isListening = ref(false)
const speechUnsupported = ref(false)
const speechError = ref(false)
let recognition = null

if (SpeechRecognition) {
  recognition = new SpeechRecognition()
  recognition.interimResults = false
  recognition.maxAlternatives = 1

  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript
    quickAddSelection.value = transcript
    onQuickAddSelectionChange(transcript)
    isListening.value = false
  }
  recognition.onerror = () => {
    speechError.value = true
    isListening.value = false
  }
  recognition.onend = () => { isListening.value = false }
}

function toggleSpeech() {
  if (!SpeechRecognition) {
    speechUnsupported.value = true
    return
  }
  speechError.value = false
  if (isListening.value) {
    recognition.stop()
    isListening.value = false
    return
  }
  recognition.lang = LOCALE_TO_SPEECH_LANG[locale.value] || navigator.language || 'en-US'
  recognition.start()
  isListening.value = true
}

onUnmounted(() => { recognition?.stop() })
</script>

