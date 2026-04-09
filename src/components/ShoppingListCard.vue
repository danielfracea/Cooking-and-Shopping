<template>
  <v-card hover @click="$emit('open', list)" :ripple="true">
    <v-card-title class="d-flex align-center justify-space-between pb-1">
      <span class="text-subtitle-1 font-weight-semibold">{{ list.name }}</span>
      <v-btn icon="mdi-delete" variant="text" color="error" size="small" @click.stop="$emit('delete', list.id)" />
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
  </v-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ list: Object })
defineEmits(['open', 'delete'])

const checkedCount = computed(() => props.list.items.filter(i => i.checked).length)
const progressPercent = computed(() =>
  props.list.items.length > 0 ? (checkedCount.value / props.list.items.length) * 100 : 0
)

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>

