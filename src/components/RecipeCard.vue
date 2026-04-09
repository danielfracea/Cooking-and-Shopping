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
        <div class="nutrition-stat rounded-lg px-2 py-1 bg-orange-lighten-4 d-flex align-center ga-1">
          <v-icon size="13" color="deep-orange-darken-1">mdi-fire</v-icon>
          <div>
            <div class="text-caption font-weight-bold" style="line-height:1.2">{{ nutrition.calories }}</div>
            <div class="text-caption text-uppercase text-medium-emphasis" style="line-height:1; font-size:9px!important">kcal</div>
          </div>
        </div>
        <div class="nutrition-stat rounded-lg px-2 py-1 bg-red-lighten-4 d-flex align-center ga-1">
          <v-icon size="13" color="red-darken-1">mdi-arm-flex</v-icon>
          <div>
            <div class="text-caption font-weight-bold" style="line-height:1.2">{{ nutrition.protein }}g</div>
            <div class="text-caption text-uppercase text-medium-emphasis" style="line-height:1; font-size:9px!important">{{ t('recipeCard.protein') }}</div>
          </div>
        </div>
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

