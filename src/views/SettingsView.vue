<template>
  <v-container max-width="600">
    <h1 class="text-h5 font-weight-bold mb-6">⚙️ {{ t('settings.title') }}</h1>

    <v-card rounded="lg" elevation="1">
      <v-card-title class="text-subtitle-1 font-weight-medium pa-4 pb-2">
        {{ t('settings.unitSystem') }}
      </v-card-title>
      <v-card-text>
        <p class="text-body-2 text-medium-emphasis mb-4">{{ t('settings.unitSystemDescription') }}</p>
        <v-btn-toggle
          v-model="settingsStore.unitSystem"
          mandatory
          density="comfortable"
          rounded="lg"
          @update:model-value="settingsStore.setUnitSystem"
        >
          <v-btn value="metric" prepend-icon="mdi-scale-balance">{{ t('settings.metric') }}</v-btn>
          <v-btn value="imperial" prepend-icon="mdi-ruler">{{ t('settings.imperial') }}</v-btn>
        </v-btn-toggle>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { useSettingsStore } from '../stores/settings'

const { t } = useI18n()
const settingsStore = useSettingsStore()
</script>
