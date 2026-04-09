import { defineStore } from 'pinia'
import { ref } from 'vue'
import { saveCollectionAsJson, loadCollectionAsJson, isApiConfigured } from '../api.js'

const STORAGE_KEY = 'cooking_shopping_lists'
const FIRESTORE_KEY = 'shoppingLists'

function loadFromStorage() {
  try {
    const data = localStorage.getItem(STORAGE_KEY)
    return data ? JSON.parse(data) : []
  } catch {
    return []
  }
}

function saveToStorage(lists) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(lists))
  saveCollectionAsJson(FIRESTORE_KEY, lists).catch(console.error)
}

export const useShoppingListsStore = defineStore('shoppingLists', () => {
  const lists = ref(loadFromStorage())

  if (isApiConfigured()) {
    loadCollectionAsJson(FIRESTORE_KEY).then(data => {
      if (data) {
        lists.value = data
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
      }
    }).catch(console.error)
  }

  function createList(name) {
    const newList = {
      id: Date.now().toString(),
      name,
      items: [],
      createdAt: new Date().toISOString(),
    }
    lists.value.push(newList)
    saveToStorage(lists.value)
    return newList
  }

  function deleteList(id) {
    lists.value = lists.value.filter(l => l.id !== id)
    saveToStorage(lists.value)
  }

  function getList(id) {
    return lists.value.find(l => l.id === id)
  }

  function addItemToList(listId, item) {
    const list = getList(listId)
    if (list) {
      const existing = list.items.find(i =>
        ((item.ingredientId && i.ingredientId && i.ingredientId === item.ingredientId) ||
        (!item.ingredientId && !i.ingredientId && i.name.toLowerCase() === item.name.toLowerCase())) &&
        (i.unit || '') === (item.unit || '')
      )
      if (existing) {
        existing.quantity = (parseFloat(existing.quantity) || 0) + (parseFloat(item.quantity) || 1)
      } else {
        list.items.push({
          id: Date.now().toString() + Math.random(),
          name: item.name,
          quantity: item.quantity || 1,
          unit: item.unit || '',
          checked: false,
          ingredientId: item.ingredientId || null,
        })
      }
      saveToStorage(lists.value)
    }
  }

  function removeItemFromList(listId, itemId) {
    const list = getList(listId)
    if (list) {
      list.items = list.items.filter(i => i.id !== itemId)
      saveToStorage(lists.value)
    }
  }

  function toggleItem(listId, itemId) {
    const list = getList(listId)
    if (list) {
      const item = list.items.find(i => i.id === itemId)
      if (item) {
        item.checked = !item.checked
        saveToStorage(lists.value)
      }
    }
  }

  function addRecipeIngredientsToList(listId, ingredients) {
    ingredients.forEach(ing => addItemToList(listId, ing))
  }

  function generateShareUrl(id) {
    const list = getList(id)
    if (!list) return null
    const encoded = btoa(String.fromCharCode(...new TextEncoder().encode(JSON.stringify(list))))
    const base = window.location.origin + window.location.pathname.replace(/\/$/, '')
    return `${base}/shared?data=${encoded}`
  }

  function importList(sharedList) {
    const newList = {
      id: crypto.randomUUID(),
      name: sharedList.name,
      items: sharedList.items.map(item => ({ ...item, id: crypto.randomUUID(), checked: false })),
      createdAt: new Date().toISOString(),
    }
    lists.value.push(newList)
    saveToStorage(lists.value)
    return newList
  }

  return { lists, createList, deleteList, getList, addItemToList, removeItemFromList, toggleItem, addRecipeIngredientsToList, generateShareUrl, importList }
})
