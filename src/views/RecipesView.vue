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

      <v-row v-else>
        <v-col v-for="recipe in recipes" :key="recipe.id" cols="12" sm="6" md="4">
          <RecipeCard :recipe="recipe" @view="viewRecipe" />
        </v-col>
      </v-row>
    </div>

    <!-- Recipe Detail -->
    <div v-else>
      <div class="d-flex align-center justify-space-between mb-4 flex-wrap ga-3">
        <div class="d-flex align-center ga-3 flex-wrap">
          <v-btn variant="outlined" size="small" prepend-icon="mdi-arrow-left" @click="selectedRecipe = null">Back</v-btn>
          <h2 class="text-h5 font-weight-bold">{{ selectedRecipe.name }}</h2>
          <v-chip v-if="selectedRecipe.category" size="small" color="blue-lighten-4" text-color="blue-darken-3">{{ selectedRecipe.category }}</v-chip>
        </div>
        <v-btn color="error" size="small" prepend-icon="mdi-delete" @click="deleteRecipe(selectedRecipe.id)">Delete</v-btn>
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

      <v-card>
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
            <v-col cols="12" sm="4"><v-text-field v-model="newRecipe.category" label="Category" variant="outlined" density="compact" /></v-col>
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRecipesStore } from '../stores/recipes'
import { useShoppingListsStore } from '../stores/shoppingLists'
import RecipeCard from '../components/RecipeCard.vue'
const recipesStore = useRecipesStore(); const listsStore = useShoppingListsStore()
const recipes = computed(() => recipesStore.recipes); const shoppingLists = computed(() => listsStore.lists)
const selectedRecipe = ref(null); const targetListId = ref(''); const addedMessage = ref(''); const showAddRecipe = ref(false)
const newRecipe = ref({ name: '', description: '', category: '', prepTime: 30, servings: 4 })
function viewRecipe(recipe) { selectedRecipe.value = recipe; targetListId.value = ''; addedMessage.value = '' }
function addToList() { if (!targetListId.value) return; listsStore.addRecipeIngredientsToList(targetListId.value, selectedRecipe.value.ingredients); const list = listsStore.getList(targetListId.value); addedMessage.value = `Added to "${list.name}"!`; setTimeout(() => addedMessage.value = '', 3000) }
function createAndAdd() { const list = listsStore.createList(`${selectedRecipe.value.name} Ingredients`); listsStore.addRecipeIngredientsToList(list.id, selectedRecipe.value.ingredients); addedMessage.value = `Created "${list.name}"!`; setTimeout(() => addedMessage.value = '', 3000) }
function deleteRecipe(id) { if (confirm('Delete this recipe?')) { recipesStore.deleteRecipe(id); selectedRecipe.value = null } }
function openAddRecipe() { newRecipe.value = { name: '', description: '', category: '', prepTime: 30, servings: 4 }; showAddRecipe.value = true }
function saveRecipe() { if (!newRecipe.value.name.trim()) return; const r = recipesStore.addRecipe({ ...newRecipe.value, ingredients: [] }); showAddRecipe.value = false; viewRecipe(r) }
</script>

