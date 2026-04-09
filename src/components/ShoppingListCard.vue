<template>
  <v-card hover @click="$emit('open', list)" :ripple="true">
    <v-card-title class="d-flex align-center justify-space-between pb-1">
      <span class="text-subtitle-1 font-weight-semibold">{{ list.name }}</span>
      <div>
        <v-btn icon="mdi-share-variant" variant="text" size="small" @click.stop="shareList" />
        <v-btn icon="mdi-delete" variant="text" color="error" size="small" @click.stop="$emit('delete', list.id)" />
      </div>
    </v-card-title>
    <v-card-text class="pt-1">
      <div class="d-flex align-center justify-space-between mb-3">
        <v-chip color="primary" size="small" variant="tonal">{{ checkedCount }}/{{ list.items.length }} items</v-chip>
        <span class="text-caption text-medium-emphasis">{{ formatDate(list.createdAt) }}</span>
      </div>
      <v-progress-linear
        v-if="list.items.length > 0"
        :model-value="progressPercent"
        color="primary"
        rounded
        height="6"
      />
    </v-card-text>

    <v-snackbar v-model="shareCopied" color="success" :timeout="3000">
      Share link copied to clipboard!
    </v-snackbar>
  </v-card>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useShoppingListsStore } from '../stores/shoppingLists'

const props = defineProps({ list: Object })
defineEmits(['open', 'delete'])

const store = useShoppingListsStore()
const shareCopied = ref(false)

const checkedCount = computed(() => props.list.items.filter(i => i.checked).length)
const progressPercent = computed(() =>
  props.list.items.length > 0 ? (checkedCount.value / props.list.items.length) * 100 : 0
)

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

async function shareList() {
  const url = store.generateShareUrl(props.list.id)
  if (!url) return
  try {
    await navigator.clipboard.writeText(url)
    shareCopied.value = true
  } catch {
    prompt('Copy this link to share:', url)
  }
}
</script>

