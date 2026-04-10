import { defineStore } from 'pinia'
import { ref } from 'vue'
import { auth } from '../firebase.js'
import { signInWithPopup, GoogleAuthProvider, signOut, onAuthStateChanged } from 'firebase/auth'

const GUEST_PHOTO_URL = 'https://ui-avatars.com/api/?name=Guest&background=9E9E9E&color=fff&size=128'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(true)

  onAuthStateChanged(auth, (u) => {
    user.value = u || (user.value?.isGuest ? user.value : null)
    loading.value = false
  })

  async function signInWithGoogle() {
    const provider = new GoogleAuthProvider()
    await signInWithPopup(auth, provider)
  }

  function continueAsGuest() {
    const randomId = 'guest-' + crypto.randomUUID()
    user.value = {
      uid: randomId,
      displayName: 'Guest',
      email: null,
      photoURL: GUEST_PHOTO_URL,
      isGuest: true,
    }
  }

  async function signOutUser() {
    if (user.value?.isGuest) {
      user.value = null
      return
    }
    await signOut(auth)
  }

  return { user, loading, signInWithGoogle, continueAsGuest, signOutUser }
})
