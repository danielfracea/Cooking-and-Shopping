<template>
  <v-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" max-width="560" persistent>
    <v-card>
      <!-- Header -->
      <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
        <div class="d-flex align-center ga-2">
          <v-icon color="primary">mdi-chef-hat</v-icon>
          <span class="text-h6">{{ recipeName }}</span>
        </div>
        <v-btn icon="mdi-close" variant="text" size="small" @click="close" />
      </v-card-title>

      <!-- Progress bar -->
      <v-progress-linear
        :model-value="progress"
        color="primary"
        height="4"
        rounded
      />

      <v-card-text class="pa-4">
        <!-- No steps fallback -->
        <div v-if="steps.length === 0" class="text-center py-8 text-medium-emphasis">
          <v-icon size="48" class="mb-3">mdi-text-box-outline</v-icon>
          <p>{{ t('recipeWizard.noSteps') }}</p>
        </div>

        <template v-else>
          <!-- Step counter -->
          <div class="d-flex align-center justify-space-between mb-4">
            <v-chip size="small" color="primary" variant="tonal">
              {{ t('recipeWizard.stepOf', { current: currentIndex + 1, total: steps.length }) }}
            </v-chip>
            <div class="d-flex ga-1">
              <v-icon
                v-for="(_, i) in steps"
                :key="i"
                :color="i === currentIndex ? 'primary' : i < currentIndex ? 'success' : 'grey-lighten-2'"
                size="10"
              >
                {{ i <= currentIndex ? 'mdi-circle' : 'mdi-circle-outline' }}
              </v-icon>
            </div>
          </div>

          <!-- Step card -->
          <v-card variant="tonal" color="primary" rounded="lg" class="mb-4">
            <v-card-text class="pa-5">
              <p class="text-overline text-primary mb-1">{{ t('recipeWizard.stepLabel', { n: currentIndex + 1 }) }}</p>
              <p class="text-h6 font-weight-bold mb-3">{{ currentStep.title }}</p>
              <p class="text-body-1">{{ currentStep.description }}</p>
            </v-card-text>
          </v-card>

          <!-- Completed banner -->
          <v-alert
            v-if="completed"
            type="success"
            variant="tonal"
            rounded="lg"
            icon="mdi-check-circle"
            class="mb-2"
          >
            {{ t('recipeWizard.completed') }}
          </v-alert>
        </template>
      </v-card-text>

      <!-- Navigation -->
      <v-card-actions class="px-4 pb-4 ga-2">
        <v-btn
          variant="outlined"
          prepend-icon="mdi-chevron-left"
          :disabled="currentIndex === 0"
          @click="prev"
        >
          {{ t('recipeWizard.previous') }}
        </v-btn>
        <v-spacer />
        <v-btn
          v-if="!isLast"
          color="primary"
          append-icon="mdi-chevron-right"
          @click="next"
        >
          {{ t('recipeWizard.next') }}
        </v-btn>
        <v-btn
          v-else
          color="success"
          prepend-icon="mdi-check"
          @click="close"
        >
          {{ t('recipeWizard.done') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps({
  modelValue: Boolean,
  recipeName: String,
  steps: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue'])

const currentIndex = ref(0)
const completed = ref(false)

const currentStep = computed(() => props.steps[currentIndex.value] ?? {})
const isLast = computed(() => currentIndex.value === props.steps.length - 1)
const progress = computed(() =>
  props.steps.length === 0 ? 0 : ((currentIndex.value + 1) / props.steps.length) * 100
)

function next() {
  if (!isLast.value) {
    currentIndex.value++
  }
  if (isLast.value) {
    completed.value = true
  }
}

function prev() {
  if (currentIndex.value > 0) {
    currentIndex.value--
    completed.value = false
  }
}

function close() {
  emit('update:modelValue', false)
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      currentIndex.value = 0
      completed.value = false
    }
  }
)
</script>
