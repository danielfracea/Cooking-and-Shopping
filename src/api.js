const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:3001'

export function isApiConfigured() {
  return true
}

export async function saveCollectionAsJson(collectionName, data) {
  try {
    await fetch(`${API_BASE}/api/collections/${collectionName}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
  } catch (err) {
    console.error('API save error:', err)
  }
}

export async function loadCollectionAsJson(collectionName) {
  try {
    const res = await fetch(`${API_BASE}/api/collections/${collectionName}`)
    if (!res.ok) return null
    return await res.json()
  } catch {
    return null
  }
}

export function subscribeToCollection(collectionName, callback) {
  const eventSource = new EventSource(`${API_BASE}/api/collections/${collectionName}/subscribe`)
  eventSource.onmessage = (event) => {
    try {
      const parsed = JSON.parse(event.data)
      if (parsed !== null) callback(parsed)
    } catch { /* ignore parse errors */ }
  }
  return () => eventSource.close()
}
