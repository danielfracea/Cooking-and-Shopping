import express from 'express'
import cors from 'cors'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'
import dotenv from 'dotenv'
import { initializeApp } from 'firebase/app'
import { getFirestore, doc, setDoc, getDoc, onSnapshot } from 'firebase/firestore'

const __dirname = dirname(fileURLToPath(import.meta.url))
dotenv.config({ path: join(__dirname, '..', '.env') })

const firebaseConfig = {
  apiKey: process.env.VITE_FIREBASE_API_KEY,
  authDomain: process.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: process.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.VITE_FIREBASE_APP_ID,
}

const APP_DATA_COLLECTION = 'app_data'

let db = null

function isFirebaseConfigured() {
  return !!(
    firebaseConfig.apiKey &&
    firebaseConfig.projectId &&
    firebaseConfig.apiKey !== 'your-api-key'
  )
}

if (isFirebaseConfigured()) {
  const fbApp = initializeApp(firebaseConfig)
  db = getFirestore(fbApp)
}

const app = express()
app.use(cors())
app.use(express.json())

app.get('/api/collections/:name', async (req, res) => {
  if (!db) return res.json(null)
  try {
    const docRef = doc(db, APP_DATA_COLLECTION, req.params.name)
    const snap = await getDoc(docRef)
    if (snap.exists()) {
      res.json(JSON.parse(snap.data().data))
    } else {
      res.json(null)
    }
  } catch (err) {
    res.status(500).json({ error: err.message })
  }
})

app.post('/api/collections/:name', async (req, res) => {
  if (!db) return res.json({ ok: true })
  try {
    const docRef = doc(db, APP_DATA_COLLECTION, req.params.name)
    await setDoc(docRef, {
      data: JSON.stringify(req.body),
      updatedAt: new Date().toISOString(),
    })
    res.json({ ok: true })
  } catch (err) {
    res.status(500).json({ error: err.message })
  }
})

app.get('/api/collections/:name/subscribe', (req, res) => {
  res.setHeader('Content-Type', 'text/event-stream')
  res.setHeader('Cache-Control', 'no-cache')
  res.setHeader('Connection', 'keep-alive')
  res.flushHeaders()

  if (!db) {
    return
  }

  const docRef = doc(db, APP_DATA_COLLECTION, req.params.name)
  const unsubscribe = onSnapshot(docRef, (snap) => {
    if (snap.exists()) {
      res.write(`data: ${snap.data().data}\n\n`)
    }
  })

  req.on('close', () => unsubscribe())
})

const PORT = process.env.API_PORT || 3001
app.listen(PORT, () => {
  console.log(`API server running on http://localhost:${PORT}`)
})
