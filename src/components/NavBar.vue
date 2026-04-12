<template>
  <v-app-bar color="surface" elevation="2">
    <v-app-bar-nav-icon class="d-sm-none" @click="drawer = !drawer" />
    <v-app-bar-title>
      <span class="text-h6 font-weight-bold text-primary d-flex align-center ga-2">
        <span>🍳</span> {{ t('nav.title') }}
      </span>
    </v-app-bar-title>
    <template #append>
      <div class="d-none d-sm-flex ga-1 mr-2">
        <v-btn :to="'/shopping-lists'" variant="text" prepend-icon="mdi-cart" rounded="lg">{{ t('nav.shoppingLists') }}</v-btn>
        <v-btn :to="'/ingredients'" variant="text" prepend-icon="mdi-food-apple" rounded="lg">{{ t('nav.ingredients') }}</v-btn>
        <v-btn :to="'/recipes'" variant="text" prepend-icon="mdi-book-open-variant" rounded="lg">{{ t('nav.recipes') }}</v-btn>
        <v-btn :to="'/meal-planner'" variant="text" prepend-icon="mdi-calendar-month" rounded="lg">{{ t('nav.mealPlanner') }}</v-btn>
      </div>
      <v-btn-toggle v-model="currentLocale" mandatory density="compact" rounded="lg" class="mr-2" @update:model-value="setLocale">
        <v-btn value="en" size="small">EN</v-btn>
        <v-btn value="ro" size="small">RO</v-btn>
      </v-btn-toggle>
      <v-menu v-if="authStore.user">
        <template #activator="{ props }">
          <v-btn icon v-bind="props" class="mr-1">
            <v-avatar size="32" color="primary">
              <v-img v-if="authStore.user.photoURL" :src="authStore.user.photoURL" :alt="authStore.user.displayName" />
              <span v-else class="text-caption font-weight-bold text-white">{{ userInitial }}</span>
            </v-avatar>
          </v-btn>
        </template>
        <v-list density="compact" min-width="180">
          <v-list-item
            :title="authStore.user.displayName || authStore.user.email"
            :subtitle="authStore.user.isGuest ? t('nav.guest') : authStore.user.email"
          />
          <v-divider />
          <v-list-item prepend-icon="mdi-cog" :title="t('nav.settings')" :to="'/settings'" />
          <v-list-item prepend-icon="mdi-logout" :title="t('nav.signOut')" @click="authStore.signOutUser()" />
        </v-list>
      </v-menu>
    </template>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" temporary>
    <v-list-item :title="t('nav.title')" prepend-icon="mdi-pot-steam" nav />
    <v-divider />
    <v-list density="compact" nav>
      <v-list-item :to="'/shopping-lists'" prepend-icon="mdi-cart" :title="t('nav.shoppingLists')" @click="drawer = false" />
      <v-list-item :to="'/ingredients'" prepend-icon="mdi-food-apple" :title="t('nav.ingredients')" @click="drawer = false" />
      <v-list-item :to="'/recipes'" prepend-icon="mdi-book-open-variant" :title="t('nav.recipes')" @click="drawer = false" />
      <v-list-item :to="'/meal-planner'" prepend-icon="mdi-calendar-month" :title="t('nav.mealPlanner')" @click="drawer = false" />
      <v-divider class="my-1" />
      <v-list-item prepend-icon="mdi-logout" :title="t('nav.signOut')" @click="authStore.signOutUser()" />
    </v-list>
  </v-navigation-drawer>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'

const { t, locale } = useI18n()
const authStore = useAuthStore()
const drawer = ref(false)
const currentLocale = ref(locale.value)

const userInitial = computed(() => {
  const name = authStore.user?.displayName || authStore.user?.email || '?'
  return name.charAt(0).toUpperCase()
})

function setLocale(val) {
  locale.value = val
  localStorage.setItem('locale', val)
}
</script>

