<template>
  <div>
    <div v-if="!selectedRecipe">
      <div class="d-flex align-center justify-space-between mb-6 flex-wrap ga-3">
        <h1 class="text-h5 font-weight-bold">📖 Recipes</h1>
        <v-btn color="primary" prepend-icon="mdi-plus" @click="openAddRecipe">Add Recipe</v-btn>
      </div>

      <v-empty-state
        v-if="recipes.length === 0"
        icon="mdi-book-open-outline"
        title="No recipes yet"
        text="Add your first recipe to get started."
      >
        <template #actions>
          <v-btn color="primary" @click="openAddRecipe">Add Your First Recipe</v-btn>
        </template>
      </v-empty-state>

      <template v-else>
        <!-- Category filter -->
        <div class="d-flex flex-wrap ga-2 mb-4">
          <v-chip
            v-for="cat in availableCategories"
            :key="cat"
            :color="selectedCategory === cat ? 'primary' : undefined"
            :variant="selectedCategory === cat ? 'flat' : 'outlined'"
            size="small"
            clickable
            @click="selectedCategory = cat"
          >{{ cat }}</v-chip>
        </div>

        <v-empty-state
          v-if="filteredRecipes.length === 0"
          icon="mdi-filter-outline"
          title="No recipes in this category"
          text="Try a different category or add a new recipe."
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
          <v-btn variant="outlined" size="small" prepend-icon="mdi-arrow-left" @click="selectedRecipe = null">Back</v-btn>
          <h2 class="text-h5 font-weight-bold">{{ selectedRecipe.name }}</h2>
          <v-chip v-if="selectedRecipe.category" size="small" color="blue-lighten-4" text-color="blue-darken-3">{{ selectedRecipe.category }}</v-chip>
        </div>
        <div class="d-flex ga-2">
          <v-btn
            color="primary"
            prepend-icon="mdi-chef-hat"
            :disabled="(selectedRecipe.steps || []).length === 0"
            @click="showWizard = true"
          >
            Start Cooking
          </v-btn>
          <v-btn color="error" size="small" variant="outlined" prepend-icon="mdi-delete" @click="deleteRecipe(selectedRecipe.id)">Delete</v-btn>
        </div>
      </div>

      <v-row class="mb-4">
        <v-col cols="12" md="6">
          <v-card height="100%">
            <v-card-title class="text-caption text-uppercase text-medium-emphasis pb-1">Description</v-card-title>
            <v-card-text>{{ selectedRecipe.description || 'No description' }}</v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card height="100%">
            <v-card-text>
              <v-row>
                <v-col cols="6" class="text-center">
                  <p class="text-caption text-uppercase text-medium-emphasis">Prep Time</p>
                  <p class="text-h5 font-weight-bold">{{ selectedRecipe.prepTime }}<span class="text-body-2"> min</span></p>
                </v-col>
                <v-col cols="6" class="text-center">
                  <p class="text-caption text-uppercase text-medium-emphasis">Servings</p>
                  <p class="text-h5 font-weight-bold">{{ selectedRecipe.servings }}<span class="text-body-2"> ppl</span></p>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Ingredients -->
      <v-card class="mb-4">
        <v-card-title class="pb-1">Ingredients</v-card-title>
        <v-list density="compact">
          <v-list-item
            v-for="ing in selectedRecipe.ingredients"
            :key="ing.ingredientId"
            :title="ing.name"
            :subtitle="`${ing.quantity} ${ing.unit}`"
          >
            <template #prepend>
              <v-icon color="primary" size="8">mdi-circle</v-icon>
            </template>
          </v-list-item>
        </v-list>
        <v-divider />
        <v-card-text>
          <p class="font-weight-medium mb-3">Add all ingredients to a shopping list:</p>
          <div class="d-flex align-center ga-3 flex-wrap">
            <v-select
              v-model="targetListId"
              :items="shoppingLists"
              item-title="name"
              item-value="id"
              label="Select a list"
              variant="outlined"
              density="compact"
              hide-details
              style="max-width:280px"
            />
            <v-btn color="primary" prepend-icon="mdi-cart" :disabled="!targetListId" @click="addToList">Add to List</v-btn>
            <v-btn variant="outlined" prepend-icon="mdi-plus" @click="createAndAdd">New List &amp; Add</v-btn>
          </div>
          <v-alert v-if="addedMessage" type="success" variant="tonal" density="compact" class="mt-3">{{ addedMessage }}</v-alert>
        </v-card-text>
      </v-card>

      <!-- Nutritional Values -->
      <v-card class="mb-4">
        <v-card-title class="pb-1">Nutritional Values</v-card-title>
        <v-card-text>
          <v-row dense>
            <v-col cols="12" sm="6">
              <p class="text-caption text-uppercase text-medium-emphasis mb-2">Total (whole recipe)</p>
              <div class="d-flex flex-wrap ga-2">
                <v-chip color="orange-lighten-4" size="small" prepend-icon="mdi-fire">{{ recipeNutrition.total.calories }} kcal</v-chip>
                <v-chip color="red-lighten-4" size="small" prepend-icon="mdi-arm-flex">{{ recipeNutrition.total.protein }}g protein</v-chip>
                <v-chip color="yellow-lighten-3" size="small" prepend-icon="mdi-grain">{{ recipeNutrition.total.carbs }}g carbs</v-chip>
                <v-chip color="blue-lighten-4" size="small" prepend-icon="mdi-water">{{ recipeNutrition.total.fat }}g fat</v-chip>
              </div>
            </v-col>
            <v-col cols="12" sm="6">
              <p class="text-caption text-uppercase text-medium-emphasis mb-2">Per serving ({{ selectedRecipe.servings }} servings)</p>
              <div class="d-flex flex-wrap ga-2">
                <v-chip color="orange-lighten-4" size="small" prepend-icon="mdi-fire">{{ recipeNutrition.perServing.calories }} kcal</v-chip>
                <v-chip color="red-lighten-4" size="small" prepend-icon="mdi-arm-flex">{{ recipeNutrition.perServing.protein }}g protein</v-chip>
                <v-chip color="yellow-lighten-3" size="small" prepend-icon="mdi-grain">{{ recipeNutrition.perServing.carbs }}g carbs</v-chip>
                <v-chip color="blue-lighten-4" size="small" prepend-icon="mdi-water">{{ recipeNutrition.perServing.fat }}g fat</v-chip>
              </div>
            </v-col>
          </v-row>
          <p v-if="recipeNutrition.total.calories === 0" class="text-body-2 text-medium-emphasis mt-2">
            Nutritional data unavailable — add ingredients with nutrition info to see values.
          </p>
        </v-card-text>
      </v-card>

      <!-- Steps -->
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pb-1">
          <span>Preparation Steps</span>
          <v-btn size="small" variant="text" color="primary" prepend-icon="mdi-plus" @click="openAddStep">Add Step</v-btn>
        </v-card-title>

        <div v-if="(selectedRecipe.steps || []).length === 0" class="pa-4 text-medium-emphasis text-body-2 text-center">
          No steps yet. Add the first preparation step.
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
          <span>Add New Recipe</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showAddRecipe = false" />
        </v-card-title>
        <v-card-text>
          <v-text-field v-model="newRecipe.name" label="Recipe Name *" variant="outlined" density="compact" placeholder="e.g. Spaghetti Bolognese" class="mb-2" />
          <v-text-field v-model="newRecipe.description" label="Description" variant="outlined" density="compact" class="mb-2" />
          <v-row dense>
            <v-col cols="12" sm="4">
              <v-combobox
                v-model="newRecipe.category"
                :items="RECIPE_CATEGORIES"
                label="Category"
                variant="outlined"
                density="compact"
                clearable
              />
            </v-col>
            <v-col cols="6" sm="4"><v-text-field v-model="newRecipe.prepTime" label="Prep Time (min)" type="number" variant="outlined" density="compact" min="0" /></v-col>
            <v-col cols="6" sm="4"><v-text-field v-model="newRecipe.servings" label="Servings" type="number" variant="outlined" density="compact" min="1" /></v-col>
          </v-row>
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showAddRecipe = false">Cancel</v-btn>
          <v-btn color="primary" :disabled="!newRecipe.name.trim()" @click="saveRecipe">Create Recipe</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Add Step Dialog -->
    <v-dialog v-model="showAddStep" max-width="480">
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
          <span>Add Preparation Step</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showAddStep = false" />
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="newStep.title"
            label="Step Title *"
            variant="outlined"
            density="compact"
            placeholder="e.g. Boil the water"
            class="mb-2"
          />
          <v-textarea
            v-model="newStep.description"
            label="Instructions *"
            variant="outlined"
            density="compact"
            rows="3"
            placeholder="Describe what to do in this step..."
          />
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showAddStep = false">Cancel</v-btn>
          <v-btn color="primary" :disabled="!newStep.title.trim() || !newStep.description.trim()" @click="saveStep">Add Step</v-btn>
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
import { useRecipesStore, RECIPE_CATEGORIES } from '../stores/recipes'
import { useShoppingListsStore } from '../stores/shoppingLists'
import { useIngredientsStore } from '../stores/ingredients'
import RecipeCard from '../components/RecipeCard.vue'
import RecipeWizard from '../components/RecipeWizard.vue'

const recipesStore = useRecipesStore()
const listsStore = useShoppingListsStore()
const ingredientsStore = useIngredientsStore()

const recipes = computed(() => recipesStore.recipes)
const shoppingLists = computed(() => listsStore.lists)

const selectedCategory = ref('All')

const filteredRecipes = computed(() => {
  if (selectedCategory.value === 'All') return recipes.value
  return recipes.value.filter(r => r.category === selectedCategory.value)
})

const availableCategories = computed(() => {
  const usedCategories = new Set(recipes.value.map(r => r.category).filter(Boolean))
  const predefinedUsed = RECIPE_CATEGORIES.filter(c => usedCategories.has(c))
  const customCategories = [...usedCategories].filter(c => !RECIPE_CATEGORIES.includes(c))
  return ['All', ...predefinedUsed, ...customCategories]
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

const newRecipe = ref({ name: '', description: '', category: '', prepTime: 30, servings: 4 })
const newStep = ref({ title: '', description: '' })

function viewRecipe(recipe) {
  selectedRecipe.value = recipe
  targetListId.value = ''
  addedMessage.value = ''
}

function addToList() {
  if (!targetListId.value) return
  listsStore.addRecipeIngredientsToList(targetListId.value, selectedRecipe.value.ingredients)
  const list = listsStore.getList(targetListId.value)
  addedMessage.value = `Added to "${list.name}"!`
  setTimeout(() => addedMessage.value = '', 3000)
}

function createAndAdd() {
  const list = listsStore.createList(`${selectedRecipe.value.name} Ingredients`)
  listsStore.addRecipeIngredientsToList(list.id, selectedRecipe.value.ingredients)
  addedMessage.value = `Created "${list.name}"!`
  setTimeout(() => addedMessage.value = '', 3000)
}

function deleteRecipe(id) {
  if (confirm('Delete this recipe?')) {
    recipesStore.deleteRecipe(id)
    selectedRecipe.value = null
  }
}

function openAddRecipe() {
  newRecipe.value = { name: '', description: '', category: '', prepTime: 30, servings: 4 }
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
</script>

