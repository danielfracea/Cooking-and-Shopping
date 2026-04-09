<template>
  <div class="recipe-card card" @click="$emit('view', recipe)">
    <div class="recipe-header">
      <h3 class="recipe-name">{{ recipe.name }}</h3>
      <span class="tag">{{ recipe.category }}</span>
    </div>
    <p class="recipe-desc">{{ recipe.description }}</p>
    <div class="recipe-meta">
      <span>⏱ {{ recipe.prepTime }} min</span>
      <span>👤 {{ recipe.servings }} servings</span>
      <span>🥗 {{ recipe.ingredients.length }} ingredients</span>
    </div>
  </div>
</template>
<script setup>
defineProps({ recipe: Object })
defineEmits(['view'])
</script>
<style scoped>
.recipe-card { cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; }
.recipe-card:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,0.12); }
.recipe-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 8px; gap: 8px; }
.recipe-name { font-size: 1.1rem; font-weight: 600; }
.recipe-desc { color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 12px; }
.recipe-meta { display: flex; gap: 16px; font-size: 0.85rem; color: var(--text-secondary); }
</style>
