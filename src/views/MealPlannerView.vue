<template>
  <div>
    <!-- Header -->
    <div class="d-flex align-center justify-space-between mb-6 flex-wrap ga-3">
      <h1 class="text-h5 font-weight-bold">{{ t('mealPlanner.title') }}</h1>
      <v-btn color="primary" prepend-icon="mdi-cart-plus" @click="openAddToList">
        {{ t('mealPlanner.addToList') }}
      </v-btn>
    </div>

    <!-- Tabs -->
    <v-tabs v-model="activeTab" color="primary" class="mb-4">
      <v-tab value="day">{{ t('mealPlanner.tabs.day') }}</v-tab>
      <v-tab value="week">{{ t('mealPlanner.tabs.week') }}</v-tab>
      <v-tab value="month">{{ t('mealPlanner.tabs.month') }}</v-tab>
    </v-tabs>

    <!-- Period navigation -->
    <div class="d-flex align-center ga-2 mb-4">
      <v-btn icon="mdi-chevron-left" variant="text" density="compact" @click="prev" />
      <div class="text-subtitle-1 font-weight-medium text-center flex-grow-1">{{ periodLabel }}</div>
      <v-btn icon="mdi-chevron-right" variant="text" density="compact" @click="next" />
      <v-btn variant="outlined" size="small" @click="goToToday">{{ t('mealPlanner.today') }}</v-btn>
    </div>

    <v-tabs-window v-model="activeTab">
      <!-- DAY VIEW -->
      <v-tabs-window-item value="day">
        <v-row>
          <v-col v-for="slot in MEAL_SLOTS" :key="slot" cols="12" sm="4">
            <v-card>
              <v-card-title class="d-flex align-center justify-space-between pa-3 pb-1">
                <div class="d-flex align-center ga-2">
                  <v-icon :color="slotColor(slot)" size="20">{{ slotIcon(slot) }}</v-icon>
                  <span class="text-subtitle-1">{{ t(`mealPlanner.slots.${slot}`) }}</span>
                </div>
                <v-btn
                  v-if="getDayMeal(currentDateStr, slot)"
                  icon="mdi-close"
                  variant="text"
                  size="x-small"
                  @click="mealPlannerStore.clearMeal(currentDateStr, slot)"
                />
              </v-card-title>
              <v-card-text class="pt-2">
                <template v-if="getDayMeal(currentDateStr, slot)">
                  <div class="font-weight-medium text-body-2">{{ getRecipeName(getDayMeal(currentDateStr, slot).recipeId) }}</div>
                  <div class="text-caption text-medium-emphasis">{{ getDayMeal(currentDateStr, slot).servings }} {{ t('mealPlanner.servings') }}</div>
                  <v-btn size="x-small" variant="text" color="primary" prepend-icon="mdi-pencil" class="mt-1 px-0" @click="openAssignDialog(currentDateStr, slot)">
                    {{ t('mealPlanner.change') }}
                  </v-btn>
                </template>
                <v-btn v-else variant="outlined" size="small" block prepend-icon="mdi-plus" @click="openAssignDialog(currentDateStr, slot)">
                  {{ t('mealPlanner.assignRecipe') }}
                </v-btn>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-tabs-window-item>

      <!-- WEEK VIEW -->
      <v-tabs-window-item value="week">
        <div class="overflow-x-auto">
          <v-table density="compact">
            <thead>
              <tr>
                <th style="min-width:90px" />
                <th
                  v-for="d in weekDays"
                  :key="d.date"
                  class="text-center"
                  :class="{ 'today-col': d.isToday }"
                  style="min-width:120px"
                >
                  <div class="text-caption text-medium-emphasis">{{ d.dayName }}</div>
                  <div class="font-weight-bold" :class="{ 'text-primary': d.isToday }">{{ d.dayNum }}</div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="slot in MEAL_SLOTS" :key="slot">
                <td class="text-caption font-weight-medium">
                  <div class="d-flex align-center ga-1">
                    <v-icon :color="slotColor(slot)" size="16">{{ slotIcon(slot) }}</v-icon>
                    {{ t(`mealPlanner.slots.${slot}`) }}
                  </div>
                </td>
                <td
                  v-for="d in weekDays"
                  :key="d.date"
                  class="text-center py-1"
                  :class="{ 'today-col': d.isToday }"
                >
                  <template v-if="getDayMeal(d.date, slot)">
                    <v-chip
                      size="x-small"
                      :color="slotColor(slot)"
                      closable
                      style="max-width:110px; height:auto; white-space:normal; cursor:pointer"
                      @click:close="mealPlannerStore.clearMeal(d.date, slot)"
                      @click="openAssignDialog(d.date, slot)"
                    >
                      {{ getRecipeName(getDayMeal(d.date, slot).recipeId) }}
                    </v-chip>
                  </template>
                  <v-btn v-else icon="mdi-plus" size="x-small" variant="text" density="compact" @click="openAssignDialog(d.date, slot)" />
                </td>
              </tr>
            </tbody>
          </v-table>
        </div>
      </v-tabs-window-item>

      <!-- MONTH VIEW -->
      <v-tabs-window-item value="month">
        <div class="month-grid mb-1">
          <div v-for="h in weekDayHeaders" :key="h" class="text-center text-caption text-medium-emphasis py-1 font-weight-medium">
            {{ h }}
          </div>
        </div>
        <div class="month-grid">
          <div
            v-for="cell in monthCells"
            :key="cell.key"
            class="month-cell border rounded pa-1"
            :class="{
              'month-cell--today': cell.isToday,
              'month-cell--other': !cell.inMonth,
            }"
          >
            <div
              class="text-caption font-weight-bold mb-1"
              :class="cell.isToday ? 'text-primary' : (cell.inMonth ? '' : 'text-disabled')"
            >
              {{ cell.dayNum }}
            </div>
            <template v-if="cell.inMonth">
              <div v-for="slot in MEAL_SLOTS" :key="slot">
                <v-tooltip v-if="getDayMeal(cell.date, slot)" :text="getRecipeName(getDayMeal(cell.date, slot).recipeId)" location="top">
                  <template #activator="{ props }">
                    <div
                      v-bind="props"
                      class="meal-label text-truncate rounded px-1 mb-px"
                      :class="slotLabelClass(slot)"
                      @click="openAssignDialog(cell.date, slot)"
                    >
                      {{ getRecipeName(getDayMeal(cell.date, slot).recipeId) }}
                    </div>
                  </template>
                </v-tooltip>
              </div>
              <v-btn
                v-if="!getDayMeal(cell.date, 'breakfast') && !getDayMeal(cell.date, 'lunch') && !getDayMeal(cell.date, 'dinner')"
                icon="mdi-plus"
                size="x-small"
                variant="text"
                density="compact"
                class="month-add-btn"
                @click="openAssignDialog(cell.date, 'breakfast')"
              />
            </template>
          </div>
        </div>
      </v-tabs-window-item>
    </v-tabs-window>

    <!-- Assign Recipe Dialog -->
    <v-dialog v-model="showAssignDialog" max-width="480">
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
          <span>{{ t('mealPlanner.assignDialog.title') }}</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showAssignDialog = false" />
        </v-card-title>
        <v-card-text>
          <div class="text-caption text-medium-emphasis mb-3">
            {{ assignDialogDateLabel }} — {{ t(`mealPlanner.slots.${assignDialogSlot}`) }}
          </div>
          <v-text-field
            v-model="recipeSearch"
            prepend-inner-icon="mdi-magnify"
            :placeholder="t('mealPlanner.assignDialog.searchPlaceholder')"
            variant="outlined"
            density="compact"
            hide-details
            clearable
            class="mb-3"
          />
          <v-list density="compact" style="max-height:220px; overflow-y:auto" class="border rounded mb-3">
            <v-list-item
              v-for="recipe in filteredAssignRecipes"
              :key="recipe.id"
              :title="recipe.name"
              :subtitle="(recipe.tags || []).join(', ')"
              :active="assignRecipeId === recipe.id"
              active-color="primary"
              rounded
              @click="selectRecipeForAssign(recipe)"
            >
              <template #prepend>
                <v-icon size="20" color="primary">mdi-silverware-fork-knife</v-icon>
              </template>
            </v-list-item>
            <v-list-item v-if="filteredAssignRecipes.length === 0" :title="t('mealPlanner.assignDialog.noRecipes')" />
          </v-list>
          <v-text-field
            v-model.number="assignServings"
            :label="t('mealPlanner.assignDialog.servings')"
            type="number"
            variant="outlined"
            density="compact"
            min="1"
            hide-details
          />
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="showAssignDialog = false">{{ t('mealPlanner.assignDialog.cancel') }}</v-btn>
          <v-btn color="primary" :disabled="!assignRecipeId" @click="confirmAssign">{{ t('mealPlanner.assignDialog.assign') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Add to Shopping List Dialog -->
    <v-dialog v-model="showAddToListDialog" max-width="480">
      <v-card>
        <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
          <span>{{ t('mealPlanner.addToListDialog.title') }}</span>
          <v-btn icon="mdi-close" variant="text" size="small" @click="showAddToListDialog = false" />
        </v-card-title>
        <v-card-text>
          <p class="text-body-2 mb-4">{{ addToListSummary }}</p>
          <v-select
            v-model="targetListId"
            :items="shoppingLists"
            item-title="name"
            item-value="id"
            :label="t('mealPlanner.addToListDialog.selectList')"
            variant="outlined"
            density="compact"
            hide-details
            clearable
          />
        </v-card-text>
        <v-card-actions class="px-4 pb-4 flex-wrap ga-2">
          <v-btn variant="outlined" prepend-icon="mdi-plus" @click="createListAndAdd">{{ t('mealPlanner.addToListDialog.newList') }}</v-btn>
          <v-spacer />
          <v-btn variant="text" @click="showAddToListDialog = false">{{ t('mealPlanner.addToListDialog.cancel') }}</v-btn>
          <v-btn color="primary" :disabled="!targetListId" prepend-icon="mdi-cart-plus" @click="confirmAddToList">{{ t('mealPlanner.addToListDialog.add') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar" :timeout="3000">{{ snackbarText }}</v-snackbar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMealPlannerStore, MEAL_SLOTS } from '../stores/mealPlanner'
import { useRecipesStore } from '../stores/recipes'
import { useShoppingListsStore } from '../stores/shoppingLists'

const { t, locale } = useI18n()
const mealPlannerStore = useMealPlannerStore()
const recipesStore = useRecipesStore()
const listsStore = useShoppingListsStore()

// ── State ─────────────────────────────────────────────────────────────────
const activeTab = ref('day')
const currentDate = ref(new Date())
const showAssignDialog = ref(false)
const assignDialogDate = ref('')
const assignDialogSlot = ref('breakfast')
const assignRecipeId = ref('')
const assignServings = ref(1)
const recipeSearch = ref('')
const showAddToListDialog = ref(false)
const targetListId = ref('')
const snackbar = ref(false)
const snackbarText = ref('')

// ── Date helpers ──────────────────────────────────────────────────────────
function toDateStr(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function parseLocalDate(str) {
  const [y, m, d] = str.split('-').map(Number)
  return new Date(y, m - 1, d)
}

function intlLocale() {
  return locale.value === 'ro' ? 'ro-RO' : 'en-US'
}

// ── Computed ──────────────────────────────────────────────────────────────
const currentDateStr = computed(() => toDateStr(currentDate.value))

const periodLabel = computed(() => {
  const fmt = intlLocale()
  if (activeTab.value === 'day') {
    return currentDate.value.toLocaleDateString(fmt, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
  }
  if (activeTab.value === 'week') {
    const days = weekDays.value
    const start = parseLocalDate(days[0].date)
    const end = parseLocalDate(days[6].date)
    const startStr = start.toLocaleDateString(fmt, { month: 'short', day: 'numeric' })
    const endStr = end.toLocaleDateString(fmt, { month: 'short', day: 'numeric', year: 'numeric' })
    return `${startStr} – ${endStr}`
  }
  return currentDate.value.toLocaleDateString(fmt, { year: 'numeric', month: 'long' })
})

const weekDays = computed(() => {
  const day = new Date(currentDate.value)
  const dayOfWeek = day.getDay()
  const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek
  const monday = new Date(day)
  monday.setDate(day.getDate() + diff)
  const todayStr = toDateStr(new Date())
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    const dateStr = toDateStr(d)
    return {
      date: dateStr,
      dayName: d.toLocaleDateString(intlLocale(), { weekday: 'short' }),
      dayNum: d.getDate(),
      isToday: dateStr === todayStr,
    }
  })
})

const weekDayHeaders = computed(() => {
  const monday = new Date(2024, 0, 1) // Jan 1, 2024 is a Monday
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    return d.toLocaleDateString(intlLocale(), { weekday: 'short' })
  })
})

const monthCells = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstOfMonth = new Date(year, month, 1)
  const lastOfMonth = new Date(year, month + 1, 0)

  const startDayOfWeek = firstOfMonth.getDay()
  const startOffset = startDayOfWeek === 0 ? -6 : 1 - startDayOfWeek
  const startDate = new Date(firstOfMonth)
  startDate.setDate(firstOfMonth.getDate() + startOffset)

  const endDayOfWeek = lastOfMonth.getDay()
  const endOffset = endDayOfWeek === 0 ? 0 : 7 - endDayOfWeek
  const endDate = new Date(lastOfMonth)
  endDate.setDate(lastOfMonth.getDate() + endOffset)

  const todayStr = toDateStr(new Date())
  const cells = []
  const d = new Date(startDate)
  while (d <= endDate) {
    const dateStr = toDateStr(d)
    cells.push({
      date: dateStr,
      dayNum: d.getDate(),
      inMonth: d.getMonth() === month,
      isToday: dateStr === todayStr,
      key: dateStr,
    })
    d.setDate(d.getDate() + 1)
  }
  return cells
})

const currentPeriodDates = computed(() => {
  if (activeTab.value === 'day') return [currentDateStr.value]
  if (activeTab.value === 'week') return weekDays.value.map(d => d.date)
  return monthCells.value.filter(c => c.inMonth).map(c => c.date)
})

const filteredAssignRecipes = computed(() => {
  const q = recipeSearch.value.trim().toLowerCase()
  if (!q) return recipesStore.recipes
  return recipesStore.recipes.filter(r => r.name.toLowerCase().includes(q))
})

const shoppingLists = computed(() => listsStore.lists)

const assignDialogDateLabel = computed(() => {
  if (!assignDialogDate.value) return ''
  return parseLocalDate(assignDialogDate.value).toLocaleDateString(intlLocale(), { weekday: 'short', month: 'short', day: 'numeric' })
})

const addToListSummary = computed(() => {
  let count = 0
  for (const date of currentPeriodDates.value) {
    const dayPlan = mealPlannerStore.getMealForDate(date)
    for (const slot of MEAL_SLOTS) {
      if (dayPlan[slot]) count++
    }
  }
  return t('mealPlanner.addToListDialog.summary', { count })
})

// ── Navigation ────────────────────────────────────────────────────────────
function prev() {
  const d = new Date(currentDate.value)
  if (activeTab.value === 'day') d.setDate(d.getDate() - 1)
  else if (activeTab.value === 'week') d.setDate(d.getDate() - 7)
  else d.setMonth(d.getMonth() - 1)
  currentDate.value = d
}

function next() {
  const d = new Date(currentDate.value)
  if (activeTab.value === 'day') d.setDate(d.getDate() + 1)
  else if (activeTab.value === 'week') d.setDate(d.getDate() + 7)
  else d.setMonth(d.getMonth() + 1)
  currentDate.value = d
}

function goToToday() {
  currentDate.value = new Date()
}

// ── Helpers ───────────────────────────────────────────────────────────────
function getDayMeal(dateStr, slot) {
  const dayPlan = mealPlannerStore.getMealForDate(dateStr)
  return dayPlan[slot] || null
}

function getRecipeName(recipeId) {
  const recipe = recipesStore.recipes.find(r => r.id === recipeId)
  return recipe ? recipe.name : t('mealPlanner.unknownRecipe')
}

function slotColor(slot) {
  if (slot === 'breakfast') return 'orange'
  if (slot === 'lunch') return 'green'
  return 'blue'
}

function slotIcon(slot) {
  if (slot === 'breakfast') return 'mdi-weather-sunny'
  if (slot === 'lunch') return 'mdi-food'
  return 'mdi-moon-waning-crescent'
}

function slotLabelClass(slot) {
  if (slot === 'breakfast') return 'bg-orange-lighten-4 text-orange-darken-2'
  if (slot === 'lunch') return 'bg-green-lighten-4 text-green-darken-2'
  return 'bg-blue-lighten-4 text-blue-darken-2'
}

// ── Assign dialog ─────────────────────────────────────────────────────────
function openAssignDialog(date, slot) {
  assignDialogDate.value = date
  assignDialogSlot.value = slot
  const existing = getDayMeal(date, slot)
  if (existing) {
    assignRecipeId.value = existing.recipeId
    assignServings.value = existing.servings
  } else {
    assignRecipeId.value = ''
    assignServings.value = 1
  }
  recipeSearch.value = ''
  showAssignDialog.value = true
}

function selectRecipeForAssign(recipe) {
  assignRecipeId.value = recipe.id
  assignServings.value = recipe.servings || 1
}

function confirmAssign() {
  if (!assignRecipeId.value) return
  mealPlannerStore.setMeal(assignDialogDate.value, assignDialogSlot.value, {
    recipeId: assignRecipeId.value,
    servings: assignServings.value || 1,
  })
  showAssignDialog.value = false
}

// ── Add to shopping list ──────────────────────────────────────────────────
function collectIngredients(dates) {
  const ingredientMap = new Map()
  for (const date of dates) {
    const dayPlan = mealPlannerStore.getMealForDate(date)
    for (const slot of MEAL_SLOTS) {
      const entry = dayPlan[slot]
      if (!entry) continue
      const recipe = recipesStore.recipes.find(r => r.id === entry.recipeId)
      if (!recipe?.ingredients) continue
      const planServings = entry.servings || recipe.servings || 1
      const recipeServings = recipe.servings || 1
      const scale = planServings / recipeServings
      for (const ing of recipe.ingredients) {
        const key = `${ing.ingredientId || ('_' + ing.name)}__${ing.unit || ''}`
        if (ingredientMap.has(key)) {
          ingredientMap.get(key).quantity += ing.quantity * scale
        } else {
          ingredientMap.set(key, {
            name: ing.name,
            quantity: ing.quantity * scale,
            unit: ing.unit || '',
            ingredientId: ing.ingredientId || null,
          })
        }
      }
    }
  }
  return [...ingredientMap.values()]
}

function openAddToList() {
  targetListId.value = ''
  showAddToListDialog.value = true
}

function confirmAddToList() {
  if (!targetListId.value) return
  const ingredients = collectIngredients(currentPeriodDates.value)
  listsStore.addRecipeIngredientsToList(targetListId.value, ingredients)
  const list = listsStore.getList(targetListId.value)
  showAddToListDialog.value = false
  snackbarText.value = t('mealPlanner.addToListDialog.added', { name: list.name })
  snackbar.value = true
}

function createListAndAdd() {
  const ingredients = collectIngredients(currentPeriodDates.value)
  const list = listsStore.createList(`${t('mealPlanner.title')} – ${periodLabel.value}`)
  listsStore.addRecipeIngredientsToList(list.id, ingredients)
  showAddToListDialog.value = false
  snackbarText.value = t('mealPlanner.addToListDialog.created', { name: list.name })
  snackbar.value = true
}
</script>

<style scoped>
.today-col {
  background-color: rgba(var(--v-theme-primary), 0.06);
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.month-cell {
  min-height: 72px;
  position: relative;
}

.month-cell--today {
  background-color: rgba(var(--v-theme-primary), 0.07);
  border-color: rgb(var(--v-theme-primary)) !important;
}

.month-cell--other {
  opacity: 0.4;
}

.meal-label {
  font-size: 0.6rem;
  line-height: 1.4;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
}

.month-add-btn {
  opacity: 0;
  transition: opacity 0.15s;
}

.month-cell:hover .month-add-btn {
  opacity: 1;
}
</style>
