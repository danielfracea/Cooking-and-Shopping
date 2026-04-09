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
      </div>
      <v-btn-toggle v-model="currentLocale" mandatory density="compact" rounded="lg" class="mr-2" @update:model-value="setLocale">
        <v-btn value="en" size="small">EN</v-btn>
        <v-btn value="ro" size="small">RO</v-btn>
      </v-btn-toggle>
    </template>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" temporary>
    <v-list-item :title="t('nav.title')" prepend-icon="mdi-pot-steam" nav />
    <v-divider />
    <v-list density="compact" nav>
      <v-list-item :to="'/shopping-lists'" prepend-icon="mdi-cart" :title="t('nav.shoppingLists')" @click="drawer = false" />
      <v-list-item :to="'/ingredients'" prepend-icon="mdi-food-apple" :title="t('nav.ingredients')" @click="drawer = false" />
      <v-list-item :to="'/recipes'" prepend-icon="mdi-book-open-variant" :title="t('nav.recipes')" @click="drawer = false" />
    </v-list>
  </v-navigation-drawer>
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const drawer = ref(false)
const currentLocale = ref(locale.value)

function setLocale(val) {
  locale.value = val
  localStorage.setItem('locale', val)
}
</script>

