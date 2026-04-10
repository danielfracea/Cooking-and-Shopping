import { defineStore } from 'pinia'
import { ref } from 'vue'
import { auth } from '../firebase.js'
import { signInWithPopup, GoogleAuthProvider, signOut, onAuthStateChanged } from 'firebase/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(true)

  onAuthStateChanged(auth, (u) => {
    user.value = u
    loading.value = false
  })

  async function signInWithGoogle() {
    const provider = new GoogleAuthProvider()
    await signInWithPopup(auth, provider)
  }

  async function signOutUser() {
    await signOut(auth)
  }

  return { user, loading, signInWithGoogle, signOutUser }
})
