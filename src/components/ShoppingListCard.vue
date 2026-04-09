<template>
  <div class="list-card card" @click="$emit('open', list)">
    <div class="card-header">
      <h3 class="list-name">{{ list.name }}</h3>
      <button class="btn btn-danger btn-sm" @click.stop="$emit('delete', list.id)">🗑</button>
    </div>
    <div class="card-meta">
      <span class="badge badge-green">{{ checkedCount }}/{{ list.items.length }} items</span>
      <span class="card-date">{{ formatDate(list.createdAt) }}</span>
    </div>
    <div class="progress-bar" v-if="list.items.length > 0">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
    </div>
  </div>
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

<style scoped>
.list-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.list-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.list-name {
  font-size: 1.1rem;
  font-weight: 600;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-date {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.progress-bar {
  height: 6px;
  background: var(--border);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 3px;
  transition: width 0.3s;
}
</style>
