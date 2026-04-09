<template>
  <v-card hover @click="$emit('view', recipe)" :ripple="true">
    <v-card-title class="d-flex align-start justify-space-between ga-2 flex-wrap pb-1">
      <span class="text-subtitle-1 font-weight-semibold">{{ recipe.name }}</span>
      <v-chip v-if="recipe.category" size="x-small" color="blue-lighten-4" text-color="blue-darken-3">{{ recipe.category }}</v-chip>
    </v-card-title>
    <v-card-text class="pt-1">
      <p v-if="recipe.description" class="text-body-2 text-medium-emphasis mb-3">{{ recipe.description }}</p>
      <div class="d-flex flex-wrap ga-3 text-caption text-medium-emphasis">
        <span><v-icon size="14" class="mr-1">mdi-clock-outline</v-icon>{{ recipe.prepTime }} min</span>
        <span><v-icon size="14" class="mr-1">mdi-account-outline</v-icon>{{ recipe.servings }} servings</span>
        <span><v-icon size="14" class="mr-1">mdi-food-variant</v-icon>{{ recipe.ingredients.length }} ingredients</span>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup>
defineProps({ recipe: Object })
defineEmits(['view'])
</script>

