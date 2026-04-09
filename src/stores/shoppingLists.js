import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'cooking_shopping_lists'

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
}

export const useShoppingListsStore = defineStore('shoppingLists', () => {
  const lists = ref(loadFromStorage())

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
      list.items.push({
        id: Date.now().toString() + Math.random(),
        name: item.name,
        quantity: item.quantity || 1,
        unit: item.unit || '',
        checked: false,
        ingredientId: item.ingredientId || null,
      })
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

  return { lists, createList, deleteList, getList, addItemToList, removeItemFromList, toggleItem, addRecipeIngredientsToList }
})
