<template>
  <v-app>
    <div v-if="authStore.loading" class="d-flex align-center justify-center" style="min-height: 100vh">
      <v-progress-circular indeterminate color="primary" size="64" />
    </div>
    <LoginView v-else-if="!authStore.user" />
    <template v-else>
      <NavBar />
      <v-main>
        <v-container class="py-6">
          <RouterView />
        </v-container>
      </v-main>
    </template>
  </v-app>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
import LoginView from './views/LoginView.vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
</script>

