<template>
  <v-card hover @click="$emit('view', recipe)" :ripple="true">
    <v-card-title class="d-flex align-start justify-space-between ga-2 flex-wrap pb-1">
      <span class="text-subtitle-1 font-weight-semibold">{{ recipe.name }}</span>
      <v-chip v-if="recipe.category" size="x-small" color="blue-lighten-4" text-color="blue-darken-3">{{ recipe.category }}</v-chip>
    </v-card-title>
    <v-card-text class="pt-1">
      <p v-if="recipe.description" class="text-body-2 text-medium-emphasis mb-3">{{ recipe.description }}</p>
      <div class="d-flex flex-wrap ga-3 text-caption text-medium-emphasis mb-2">
        <span><v-icon size="14" class="mr-1">mdi-clock-outline</v-icon>{{ recipe.prepTime }} {{ t('recipeCard.min') }}</span>
        <span><v-icon size="14" class="mr-1">mdi-account-outline</v-icon>{{ recipe.servings }} {{ t('recipeCard.servings') }}</span>
        <span><v-icon size="14" class="mr-1">mdi-food-variant</v-icon>{{ recipe.ingredients.length }} {{ t('recipeCard.ingredients') }}</span>
      </div>
      <div v-if="nutrition.calories > 0" class="d-flex flex-wrap ga-2 text-caption">
        <v-chip size="x-small" color="orange-lighten-4" prepend-icon="mdi-fire">{{ nutrition.calories }} kcal</v-chip>
        <v-chip size="x-small" color="red-lighten-4" prepend-icon="mdi-arm-flex">{{ nutrition.protein }}g {{ t('recipeCard.protein') }}</v-chip>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useIngredientsStore } from '../stores/ingredients'

const { t } = useI18n()
const props = defineProps({ recipe: Object })
defineEmits(['view'])

const ingredientsStore = useIngredientsStore()

const nutrition = computed(() => {
  let calories = 0, protein = 0
  for (const ing of props.recipe.ingredients) {
    const data = ingredientsStore.getIngredient(ing.ingredientId)
    if (data) {
      calories += (data.calories || 0) * ing.quantity
      protein += (data.protein || 0) * ing.quantity
    }
  }
  return { calories: Math.round(calories), protein: Math.round(protein) }
})
</script>

