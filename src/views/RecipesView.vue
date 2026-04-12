<template>
  <div>
    <div v-if="!selectedRecipe">
      <div class="d-flex align-center justify-space-between mb-6 flex-wrap ga-3">
        <h1 class="text-h5 font-weight-bold">{{ t('recipes.title') }}</h1>
        <v-btn color="primary" prepend-icon="mdi-plus" @click="openAddRecipe">{{ t('recipes.add') }}</v-btn>
      </div>

      <v-empty-state
        v-if="recipes.length === 0"
        icon="mdi-book-open-outline"
        :title="t('recipes.empty.title')"
        :text="t('recipes.empty.text')"
      >
        <template #actions>
          <v-btn color="primary" @click="openAddRecipe">{{ t('recipes.empty.action') }}</v-btn>
        </template>
      </v-empty-state>

      <template v-else>
        <!-- Tag filter -->
        <div class="d-flex flex-wrap ga-2 mb-4">
          <v-chip
            v-for="tag in availableTags"
            :key="tag"
            :color="selectedTag === tag ? 'primary' : undefined"
            :variant="selectedTag === tag ? 'flat' : 'outlined'"
            size="small"
            clickable
            @click="selectedTag = tag"
          >{{ tag }}</v-chip>
        </div>

        <v-empty-state
          v-if="filteredRecipes.length === 0"
          icon="mdi-filter-outline"
          title="No recipes with this tag"
          text="Try a different tag or add a new recipe."
        />

        <v-row v-else>
          <v-col v-for="recipe in filteredRecipes" :key="recipe.id" cols="12" sm="6" md="4">
            <RecipeCard :recipe="recipe" @view="viewRecipe" />
          </v-col>
        </v-row>
      </template>
    </div>

    <!-- Recipe Detail -->
    <div v-else>
      <div class="d-flex align-center justify-space-between mb-4 flex-wrap ga-3">
        <div class="d-flex align-center ga-3 flex-wrap">
          <v-btn variant="outlined" size="small" prepend-icon="mdi-arrow-left" @click="selectedRecipe = null">{{ t('recipes.detail.back') }}</v-btn>
          <h2 class="text-h5 font-weight-bold">{{ selectedRecipe.name }}</h2>
          <v-chip v-for="tag in (selectedRecipe.tags || [])" :key="tag" size="small" color="blue-lighten-4" text-color="blue-darken-3">{{ tag }}</v-chip>
        </div>
        <div class="d-flex ga-2">
          <v-btn
            color="primary"
            prepend-icon="mdi-chef-hat"
            :disabled="(selectedRecipe.steps || []).length === 0"
            @click="showWizard = true"
          >
            {{ t('recipes.detail.startCooking') }}
          </v-btn>
          <v-btn color="error" size="small" variant="outlined" prepend-icon="mdi-delete" @click="deleteRecipe(selectedRecipe.id)">{{ t('recipes.detail.delete') }}</v-btn>
        </div>
      </div>

      <v-row class="mb-4">
        <v-col cols="12" md="6">
          <v-card height="100%">
            <v-card-title class="text-caption text-uppercase text-medium-emphasis pb-1">{{ t('recipes.detail.description') }}</v-card-title>
            <v-card-text>{{ selectedRecipe.description || t('recipes.detail.noDescription') }}</v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card height="100%">
            <v-card-text>
              <v-row>
                <v-col cols="6" class="text-center">
                  <p class="text-caption text-uppercase text-medium-emphasis">{{ t('recipes.detail.prepTime') }}</p>
                  <p class="text-h5 font-weight-bold">{{ selectedRecipe.prepTime }}<span class="text-body-2"> {{ t('recipes.detail.min') }}</span></p>
                </v-col>
                <v-col cols="6" class="text-center">
                  <p class="text-caption text-uppercase text-medium-emphasis">{{ t('recipes.detail.servings') }}</p>
                  <p class="text-h5 font-weight-bold">{{ selectedRecipe.servings }}<span class="text-body-2"> {{ t('recipes.detail.ppl') }}</span></p>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Ingredients -->
      <v-card class="mb-4">
        <v-card-title class="pb-1">{{ t('recipes.detail.ingredients') }}</v-card-title>
        <v-list density="compact">
          <v-list-item
            v-for="ing in selectedRecipe.ingredients"
            :key="ing.ingredientId"
            :title="ing.name"
            :subtitle="formatIngredientQuantity(ing)"
          >
            <template #prepend>
              <v-icon color="primary" size="8">mdi-circle</v-icon>
            </template>
          </v-list-item>
        </v-list>
        <v-divider />
        <v-card-text>
          <p class="font-weight-medium mb-3">{{ t('recipes.detail.addToListLabel') }}</p>
          <div class="d-flex align-center ga-3 flex-wrap">
            <v-select
              v-model="targetListId"
              :items="shoppingLists"
              item-title="name"
              item-value="id"
              :label="t('recipes.detail.selectList')"
              variant="outlined"
              density="compact"
              hide-details
              style="max-width:280px"
            />
            <v-btn color="primary" prepend-icon="mdi-cart" :disabled="!targetListId" @click="addToList">{{ t('recipes.detail.addToList') }}</v-btn>
            <v-btn variant="outlined" prepend-icon="mdi-plus" @click="createAndAdd">{{ t('recipes.detail.newListAndAdd') }}</v-btn>
          </div>
          <v-alert v-if="addedMessage" type="success" variant="tonal" density="compact" class="mt-3">{{ addedMessage }}</v-alert>
        </v-card-text>
      </v-card>

      <!-- Nutritional Values -->
      <v-card class="mb-4">
        <v-card-title class="pb-1">{{ t('recipes.detail.nutritionalValues') }}</v-card-title>
        <v-card-text>
          <v-row dense>
            <v-col cols="12" sm="6">
              <p class="text-caption text-uppercase text-medium-emphasis mb-2">{{ t('recipes.detail.total') }}</p>
              <div class="d-flex flex-wrap ga-2">
                <div class="nutrition-stat rounded-lg px-3 py-2 bg-orange-lighten-4 d-flex align-center ga-2">
                  <v-icon size="18" color="deep-orange-darken-1">mdi-fire</v-icon>
                  <div>
                    <div class="text-body-2 font-weight-bold">{{ recipeNutrition.total.calories }}</div>
                    <div class="nutrition-stat__label">{{ t('recipes.detail.kcal') }}</div>
                  </div>
                </div>
                <div class="nutrition-stat rounded-lg px-3 py-2 bg-red-lighten-4 d-flex align-center ga-2">
                  <v-icon size="18" color="red-darken-1">mdi-arm-flex</v-icon>
                  <div>
                    <div class="text-body-2 font-weight-bold">{{ recipeNutrition.total.protein }}g</div>
                    <div class="nutrition-stat__label">{{ t('recipes.detail.protein') }}</div>
                  </div>
                </div>
                <div class="nutrition-stat rounded-lg px-3 py-2 bg-yellow-lighten-3 d-flex align-center ga-2">
                  <v-icon size="18" color="amber-darken-2">mdi-grain</v-icon>
                  <div>
                    <div class="text-body-2 font-weight-bold">{{ recipeNutrition.total.carbs }}g</div>
                    <div class="nutrition-stat__label">{{ t('recipes.detail.carbs') }}</div>
                  </div>
                </div>
                <div class="nutrition-stat rounded-lg px-3 py-2 bg-blue-lighten-4 d-flex align-center ga-2">
                  <v-icon size="18" color="blue-darken-1">mdi-water</v-icon>
                  <div>
                    <div class="text-body-2 font-weight-bold">{{ recipeNutrition.total.fat }}g</div>
                    <div class="nutrition-stat__label">{{ t('recipes.detail.fat') }}</div>
                  </div>
                </div>
              </div>
            </v-col>
            <v-col cols="12" sm="6">
              <p class="text-caption text-uppercase text-medium-emphasis mb-2">{{ t('recipes.detail.perServing', { n: selectedRecipe.servings }) }}</p>
              <div class="d-flex flex-wrap ga-2">
                <div class="nutrition-stat rounded-lg px-3 py-2 bg-orange-lighten-4 d-flex align-center ga-2">
                  <v-icon size="18" color="deep-orange-darken-1">mdi-fire</v-icon>
                  <div>
                    <div class="text-body-2 font-weight-bold">{{ recipeNutrition.perServing.calories }}</div>
                    <div class="nutrition-stat__label">{{ t('recipes.detail.kcal') }}</div>
                  </div>
                </div>
                <div class="nutrition-stat rounded-lg px-3 py-2 bg-red-lighten-4 d-flex align-center ga-2">
                  <v-icon size="18" color="red-darken-1">mdi-arm-flex</v-icon>
                  <div>
                    <div class="text-body-2 font-weight-bold">{{ recipeNutrition.perServing.protein }}g</div>
                    <div class="nutrition-stat__label">{{ t('recipes.detail.protein') }}</div>
                  </div>
                </div>
                <div class="nutrition-stat rounded-lg px-3 py-2 bg-yellow-lighten-3 d-flex align-center ga-2">
                  <v-icon size="18" color="amber-darken-2">mdi-grain</v-icon>
                  <div>
                    <div class="text-body-2 font-weight-bold">{{ recipeNutrition.perServing.carbs }}g</div>
                    <div class="nutrition-stat__label">{{ t('recipes.detail.carbs') }}</div>
                  </div>
                </div>
                <div class="nutrition-stat rounded-lg px-3 py-2 bg-blue-lighten-4 d-flex align-center ga-2">
                  <v-icon size="18" color="blue-darken-1">mdi-water</v-icon>
                  <div>
                    <div class="text-body-2 font-weight-bold">{{ recipeNutrition.perServing.fat }}g</div>
                    <div class="nutrition-stat__label">{{ t('recipes.detail.fat') }}</div>
                  </div>
                </div>
              </div>
            </v-col>
          </v-row>
          <p v-if="recipeNutrition.total.calories === 0" class="text-body-2 text-medium-emphasis mt-2">
            {{ t('recipes.detail.noNutrition') }}
          </p>
        </v-card-text>
      </v-card>

      <!-- Tools -->
      <v-card class="mb-4">
        <v-card-title class="d-flex align-center justify-space-between pb-1">
          <span>{{ t('recipes.detail.tools') }}</span>
          <v-btn size="small" variant="text" color="primary" prepend-icon="mdi-plus" @click="openAddTool">{{ t('recipes.detail.addTool') }}</v-btn>
        </v-card-title>

        <div v-if="(selectedRecipe.tools || []).length === 0" class="pa-4 text-medium-emphasis text-body-2 text-center">
          {{ t('recipes.detail.noTools') }}
        </div>

        <v-list v-else density="compact">
          <v-list-item
            v-for="(tool, index) in selectedRecipe.tools"
            :key="index"
            :title="tool"
          >
            <template #prepend>
              <v-icon color="primary" size="20">mdi-tools</v-icon>
            </template>
            <template #append>
              <v-btn icon="mdi-delete" size="x-small" variant="text" color="error" @click.stop="removeTool(index)" />
            </template>
          </v-list-item>
        </v-list>
      </v-card>

      <!-- Steps -->
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pb-1">
          <span>{{ t('recipes.detail.preparationSteps') }}</span>
          <v-btn size="small" variant="text" color="primary" prepend-icon="mdi-plus" @click="openAddStep">{{ t('recipes.detail.addStep') }}</v-btn>
        </v-card-title>

        <div v-if="(selectedRecipe.steps || []).length === 0" class="pa-4 text-medium-emphasis text-body-2 text-center">
          {{ t('recipes.detail.noSteps') }}
        </div>

        <v-list v-else density="compact">
          <v-list-item
            v-for="(step, index) in selectedRecipe.steps"
            :key="step.id"
            :title="`${index + 1}. ${step.title}`"
            :subtitle="step.description"
          >
            <template #prepend>
              <v-avatar color="primary" size="28" class="text-caption font-weight-bold">{{ index + 1 }}</v-avatar>
            </template>
            <template #append>
              <v-btn icon="mdi-delete" size="x-small" variant="text" color="error" @click.stop="removeStep(index)" />
            </template>
          </v-list-item>
        </v-list>
      </v-card>
    </div>

    <!-- Add Recipe Dialog -->
    <v-dialog v-model="showAddRecipe" max-width="520">
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
          <span>{{ t('recipes.addDialog.title') }}</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showAddRecipe = false" />
        </v-card-title>
        <v-card-text>
          <v-text-field v-model="newRecipe.name" :label="t('recipes.addDialog.name')" variant="outlined" density="compact" :placeholder="t('recipes.addDialog.placeholder.name')" class="mb-2" />
          <v-text-field v-model="newRecipe.description" :label="t('recipes.addDialog.description')" variant="outlined" density="compact" class="mb-2" />
          <v-row dense>
            <v-col cols="12">
              <v-combobox
                v-model="newRecipe.tags"
                :items="RECIPE_TAGS"
                :label="t('recipes.addDialog.tags')"
                variant="outlined"
                density="compact"
                multiple
                chips
                closable-chips
                class="mb-2"
              />
            </v-col>
            <v-col cols="12">
              <v-combobox
                v-model="newRecipe.tools"
                :label="t('recipes.addDialog.tools')"
                variant="outlined"
                density="compact"
                multiple
                chips
                closable-chips
                :placeholder="t('recipes.addDialog.toolsPlaceholder')"
                class="mb-2"
              />
            </v-col>
            <v-col cols="6" sm="6"><v-text-field v-model="newRecipe.prepTime" :label="t('recipes.addDialog.prepTime')" type="number" variant="outlined" density="compact" min="0" /></v-col>
            <v-col cols="6" sm="6"><v-text-field v-model="newRecipe.servings" :label="t('recipes.addDialog.servings')" type="number" variant="outlined" density="compact" min="1" /></v-col>
          </v-row>
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showAddRecipe = false">{{ t('recipes.addDialog.cancel') }}</v-btn>
          <v-btn color="primary" :disabled="!newRecipe.name.trim()" @click="saveRecipe">{{ t('recipes.addDialog.create') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Add Step Dialog -->
    <v-dialog v-model="showAddStep" max-width="480">
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
          <span>{{ t('recipes.stepDialog.title') }}</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showAddStep = false" />
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="newStep.title"
            :label="t('recipes.stepDialog.stepTitle')"
            variant="outlined"
            density="compact"
            :placeholder="t('recipes.stepDialog.placeholder.stepTitle')"
            class="mb-2"
          />
          <v-textarea
            v-model="newStep.description"
            :label="t('recipes.stepDialog.instructions')"
            variant="outlined"
            density="compact"
            rows="3"
            :placeholder="t('recipes.stepDialog.placeholder.instructions')"
          />
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showAddStep = false">{{ t('recipes.stepDialog.cancel') }}</v-btn>
          <v-btn color="primary" :disabled="!newStep.title.trim() || !newStep.description.trim()" @click="saveStep">{{ t('recipes.stepDialog.add') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Add Tool Dialog -->
    <v-dialog v-model="showAddTool" max-width="480">
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
          <span>{{ t('recipes.detail.toolDialog.title') }}</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showAddTool = false" />
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="newTool"
            :label="t('recipes.detail.toolDialog.toolName')"
            variant="outlined"
            density="compact"
            :placeholder="t('recipes.detail.toolDialog.placeholder')"
          />
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showAddTool = false">{{ t('recipes.detail.toolDialog.cancel') }}</v-btn>
          <v-btn color="primary" :disabled="!newTool.trim()" @click="saveTool">{{ t('recipes.detail.toolDialog.add') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Cooking Wizard -->
    <RecipeWizard
      v-model="showWizard"
      :recipe-name="selectedRecipe ? selectedRecipe.name : ''"
      :steps="selectedRecipe ? (selectedRecipe.steps || []) : []"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRecipesStore, RECIPE_TAGS } from '../stores/recipes'
import { useShoppingListsStore } from '../stores/shoppingLists'
import { useIngredientsStore } from '../stores/ingredients'
import { useSettingsStore } from '../stores/settings'
import RecipeCard from '../components/RecipeCard.vue'
import RecipeWizard from '../components/RecipeWizard.vue'

const { t } = useI18n()
const recipesStore = useRecipesStore()
const listsStore = useShoppingListsStore()
const ingredientsStore = useIngredientsStore()
const settingsStore = useSettingsStore()

function formatIngredientQuantity(ing) {
  const { quantity, unit } = settingsStore.displayQuantity(ing.quantity, ing.unit)
  return unit ? `${quantity} ${unit}` : quantity
}

const recipes = computed(() => recipesStore.recipes)
const shoppingLists = computed(() => listsStore.lists)

const selectedTag = ref('All')

const filteredRecipes = computed(() => {
  if (selectedTag.value === 'All') return recipes.value
  return recipes.value.filter(r => (r.tags || []).includes(selectedTag.value))
})

const availableTags = computed(() => {
  const usedTags = new Set(recipes.value.flatMap(r => r.tags || []))
  const predefinedUsed = RECIPE_TAGS.filter(t => usedTags.has(t))
  const customTags = [...usedTags].filter(t => !RECIPE_TAGS.includes(t))
  return ['All', ...predefinedUsed, ...customTags]
})

const recipeNutrition = computed(() => {
  const recipe = selectedRecipe.value
  if (!recipe) return { total: { calories: 0, protein: 0, carbs: 0, fat: 0 }, perServing: { calories: 0, protein: 0, carbs: 0, fat: 0 } }
  let calories = 0, protein = 0, carbs = 0, fat = 0
  for (const ing of (recipe.ingredients || [])) {
    const data = ingredientsStore.getIngredient(ing.ingredientId)
    if (data) {
      calories += (data.calories || 0) * ing.quantity
      protein += (data.protein || 0) * ing.quantity
      carbs += (data.carbs || 0) * ing.quantity
      fat += (data.fat || 0) * ing.quantity
    }
  }
  const servings = recipe.servings > 0 ? recipe.servings : 1
  return {
    total: { calories: Math.round(calories), protein: Math.round(protein), carbs: Math.round(carbs), fat: Math.round(fat) },
    perServing: {
      calories: Math.round(calories / servings),
      protein: Math.round(protein / servings),
      carbs: Math.round(carbs / servings),
      fat: Math.round(fat / servings),
    },
  }
})

const selectedRecipe = ref(null)
const targetListId = ref('')
const addedMessage = ref('')
const showAddRecipe = ref(false)
const showWizard = ref(false)
const showAddStep = ref(false)
const showAddTool = ref(false)

const newRecipe = ref({ name: '', description: '', tags: [], tools: [], prepTime: 30, servings: 4 })
const newStep = ref({ title: '', description: '' })
const newTool = ref('')

function viewRecipe(recipe) {
  selectedRecipe.value = recipe
  targetListId.value = ''
  addedMessage.value = ''
}

function addToList() {
  if (!targetListId.value) return
  listsStore.addRecipeIngredientsToList(targetListId.value, selectedRecipe.value.ingredients)
  const list = listsStore.getList(targetListId.value)
  addedMessage.value = t('recipes.detail.addedTo', { name: list.name })
  setTimeout(() => addedMessage.value = '', 3000)
}

function createAndAdd() {
  const list = listsStore.createList(`${selectedRecipe.value.name} Ingredients`)
  listsStore.addRecipeIngredientsToList(list.id, selectedRecipe.value.ingredients)
  addedMessage.value = t('recipes.detail.created', { name: list.name })
  setTimeout(() => addedMessage.value = '', 3000)
}

function deleteRecipe(id) {
  if (confirm(t('recipes.confirm.delete'))) {
    recipesStore.deleteRecipe(id)
    selectedRecipe.value = null
  }
}

function openAddRecipe() {
  newRecipe.value = { name: '', description: '', tags: [], tools: [], prepTime: 30, servings: 4 }
  showAddRecipe.value = true
}

function saveRecipe() {
  if (!newRecipe.value.name.trim()) return
  const r = recipesStore.addRecipe({ ...newRecipe.value, ingredients: [] })
  showAddRecipe.value = false
  viewRecipe(r)
}

function openAddStep() {
  newStep.value = { title: '', description: '' }
  showAddStep.value = true
}

function saveStep() {
  if (!newStep.value.title.trim() || !newStep.value.description.trim()) return
  const steps = [...(selectedRecipe.value.steps || []), {
    id: Date.now().toString(),
    title: newStep.value.title.trim(),
    description: newStep.value.description.trim(),
  }]
  recipesStore.updateRecipeSteps(selectedRecipe.value.id, steps)
  selectedRecipe.value = recipesStore.recipes.find(r => r.id === selectedRecipe.value.id)
  showAddStep.value = false
}

function removeStep(index) {
  const steps = [...(selectedRecipe.value.steps || [])]
  steps.splice(index, 1)
  recipesStore.updateRecipeSteps(selectedRecipe.value.id, steps)
  selectedRecipe.value = recipesStore.recipes.find(r => r.id === selectedRecipe.value.id)
}

function openAddTool() {
  newTool.value = ''
  showAddTool.value = true
}

function saveTool() {
  if (!newTool.value.trim()) return
  const tools = [...(selectedRecipe.value.tools || []), newTool.value.trim()]
  recipesStore.updateRecipeTools(selectedRecipe.value.id, tools)
  selectedRecipe.value = recipesStore.recipes.find(r => r.id === selectedRecipe.value.id)
  showAddTool.value = false
}

function removeTool(index) {
  const tools = [...(selectedRecipe.value.tools || [])]
  tools.splice(index, 1)
  recipesStore.updateRecipeTools(selectedRecipe.value.id, tools)
  selectedRecipe.value = recipesStore.recipes.find(r => r.id === selectedRecipe.value.id)
}
</script>

<style scoped>
.nutrition-stat__label {
  font-size: 0.65rem;
  line-height: 1;
  text-transform: uppercase;
  opacity: 0.7;
}
</style>
