import { initializeApp } from 'firebase/app'
import { getFirestore, collection, getDocs, addDoc, updateDoc, deleteDoc, doc, onSnapshot, setDoc, getDoc } from 'firebase/firestore'

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

console.log(firebaseConfig);

let app = null
let db = null

export function isFirebaseConfigured() {
  return !!(
    import.meta.env.VITE_FIREBASE_API_KEY &&
    import.meta.env.VITE_FIREBASE_PROJECT_ID &&
    import.meta.env.VITE_FIREBASE_API_KEY !== 'your-api-key'
  )
}

if (isFirebaseConfigured()) {
  app = initializeApp(firebaseConfig)
  db = getFirestore(app)
}

export { db }
export { collection, getDocs, addDoc, updateDoc, deleteDoc, doc, onSnapshot }

const APP_DATA_COLLECTION = 'app_data'

export async function saveCollectionAsJson(collectionName, data) {
  if (!db) return
  const docRef = doc(db, APP_DATA_COLLECTION, collectionName)
  await setDoc(docRef, { data: JSON.stringify(data), updatedAt: new Date().toISOString() })
}

export async function loadCollectionAsJson(collectionName) {
  if (!db) return null
  const docRef = doc(db, APP_DATA_COLLECTION, collectionName)
  const snap = await getDoc(docRef)
  if (snap.exists()) {
    try {
      return JSON.parse(snap.data().data)
    } catch {
      return null
    }
  }
  return null
}

export function subscribeToCollection(collectionName, callback) {
  if (!db) return () => {}
  const docRef = doc(db, APP_DATA_COLLECTION, collectionName)
  return onSnapshot(docRef, (snap) => {
    if (snap.exists()) {
      try {
        const parsed = JSON.parse(snap.data().data)
        callback(parsed)
      } catch { /* ignore parse errors */ }
    }
  })
}
