<template>
  <div class="d-flex align-center justify-center" style="min-height: 100vh; background: #F5F5F5">
    <v-card width="400" class="pa-6" elevation="3">
      <div class="text-center mb-6">
        <div class="text-h4 mb-2">🍳 Cook & Shop</div>
        <div class="text-body-1 text-medium-emphasis">{{ t('auth.signInPrompt') }}</div>
      </div>
      <v-btn
        block
        size="large"
        variant="outlined"
        prepend-icon="mdi-google"
        :loading="loading"
        @click="signIn"
      >
        {{ t('auth.signInWithGoogle') }}
      </v-btn>
      <v-divider class="my-4" />
      <v-btn
        block
        size="large"
        variant="text"
        prepend-icon="mdi-account-outline"
        @click="authStore.continueAsGuest()"
      >
        {{ t('auth.continueAsGuest') }}
      </v-btn>
      <div v-if="error" class="text-error text-caption mt-3 text-center">{{ t('auth.error') }}</div>
    </v-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'

const { t } = useI18n()
const authStore = useAuthStore()
const loading = ref(false)
const error = ref(false)

async function signIn() {
  loading.value = true
  error.value = false
  try {
    await authStore.signInWithGoogle()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}
</script>
