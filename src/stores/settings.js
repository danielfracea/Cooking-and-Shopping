import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getPreferredUnit, convertUnit, formatQuantity } from '../utils/units.js'

const STORAGE_KEY = 'cooking_settings'

function loadSettings() {
  try {
    const data = localStorage.getItem(STORAGE_KEY)
    return data ? JSON.parse(data) : {}
  } catch {
    return {}
  }
}

export const useSettingsStore = defineStore('settings', () => {
  const saved = loadSettings()
  const unitSystem = ref(saved.unitSystem || 'metric')

  function setUnitSystem(system) {
    unitSystem.value = system
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...loadSettings(), unitSystem: system }))
  }

  // Return the preferred unit for a given stored unit
  function preferredUnit(unit) {
    return getPreferredUnit(unit, unitSystem.value)
  }

  // Convert and format a quantity to the preferred unit for display
  function displayQuantity(value, unit) {
    if (!unit || value === null || value === undefined) {
      return { quantity: formatQuantity(value), unit: unit || '' }
    }
    const preferred = preferredUnit(unit)
    const converted = convertUnit(parseFloat(value) || 0, unit, preferred)
    return { quantity: formatQuantity(converted), unit: preferred }
  }

  const isMetric = computed(() => unitSystem.value === 'metric')

  return { unitSystem, isMetric, setUnitSystem, preferredUnit, displayQuantity }
})
