import { db, auth } from './firebase.js'
import { doc, getDoc, setDoc, onSnapshot } from 'firebase/firestore'
import { onAuthStateChanged } from 'firebase/auth'

const USERS_COLLECTION = 'users'
const DATA_SUBCOLLECTION = 'data'

export async function saveCollectionAsJson(collectionName, data) {
  const user = auth.currentUser
  if (!user) return
  const docRef = doc(db, USERS_COLLECTION, user.uid, DATA_SUBCOLLECTION, collectionName)
  await setDoc(docRef, {
    data: JSON.stringify(data),
    updatedAt: new Date().toISOString(),
  })
}

export async function loadCollectionAsJson(collectionName) {
  const user = auth.currentUser
  if (!user) return null
  const docRef = doc(db, USERS_COLLECTION, user.uid, DATA_SUBCOLLECTION, collectionName)
  const snap = await getDoc(docRef)
  if (snap.exists()) return JSON.parse(snap.data().data)
  return null
}

/**
 * Subscribes to a user's Firestore collection document.
 * Automatically re-subscribes when auth state changes.
 * Calls callback(null) when the user signs out so stores can reset.
 */
export function subscribeToCollection(collectionName, callback) {
  let unsubscribeSnapshot = null

  const unsubscribeAuth = onAuthStateChanged(auth, (user) => {
    if (unsubscribeSnapshot) {
      unsubscribeSnapshot()
      unsubscribeSnapshot = null
    }
    if (user) {
      const docRef = doc(db, USERS_COLLECTION, user.uid, DATA_SUBCOLLECTION, collectionName)
      unsubscribeSnapshot = onSnapshot(docRef, (snap) => {
        if (snap.exists()) {
          try {
            const parsed = JSON.parse(snap.data().data)
            if (parsed !== null) callback(parsed)
          } catch { /* ignore parse errors */ }
        }
      }, (err) => {
        console.error(`Firestore snapshot error for ${collectionName}:`, err)
      })
    } else {
      callback(null)
    }
  })

  return () => {
    if (unsubscribeSnapshot) unsubscribeSnapshot()
    unsubscribeAuth()
  }
}
