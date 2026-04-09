<template>
  <v-app-bar color="surface" elevation="2">
    <v-app-bar-nav-icon class="d-sm-none" @click="drawer = !drawer" />
    <v-app-bar-title>
      <span class="text-h6 font-weight-bold text-primary d-flex align-center ga-2">
        <span>🍳</span> Cook &amp; Shop
      </span>
    </v-app-bar-title>
    <template #append>
      <div class="d-none d-sm-flex ga-1 mr-2">
        <v-btn :to="'/shopping-lists'" variant="text" prepend-icon="mdi-cart" rounded="lg">Shopping Lists</v-btn>
        <v-btn :to="'/ingredients'" variant="text" prepend-icon="mdi-food-apple" rounded="lg">Ingredients</v-btn>
        <v-btn :to="'/recipes'" variant="text" prepend-icon="mdi-book-open-variant" rounded="lg">Recipes</v-btn>
      </div>
    </template>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" temporary>
    <v-list-item title="Cook &amp; Shop" prepend-icon="mdi-pot-steam" nav />
    <v-divider />
    <v-list density="compact" nav>
      <v-list-item :to="'/shopping-lists'" prepend-icon="mdi-cart" title="Shopping Lists" @click="drawer = false" />
      <v-list-item :to="'/ingredients'" prepend-icon="mdi-food-apple" title="Ingredients" @click="drawer = false" />
      <v-list-item :to="'/recipes'" prepend-icon="mdi-book-open-variant" title="Recipes" @click="drawer = false" />
    </v-list>
  </v-navigation-drawer>
</template>

<script setup>
import { ref } from 'vue'
const drawer = ref(false)
</script>

