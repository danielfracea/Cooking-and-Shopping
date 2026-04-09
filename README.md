# Cooking & Shopping

A Vue 3 app for managing recipes, ingredients, and shopping lists, with real-time sync powered by Firebase Firestore through a dedicated Node.js + Express API.

## Architecture

```
┌──────────────────┐     HTTP / SSE     ┌─────────────────────────┐
│  Vue 3 frontend  │ ──────────────────▶ │  Express API (api/)     │
│  (Vite / Pinia)  │                     │  Firebase Firestore SDK  │
└──────────────────┘                     └─────────────────────────┘
```

- The **frontend** (`src/`) never talks to Firebase directly — it uses `src/api.js` to call the Express server.
- The **API** (`api/server.js`) holds all Firebase credentials and performs Firestore reads/writes.
- Real-time updates are streamed to the frontend via [Server-Sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events).

## Getting started

### 1. Install dependencies

```bash
npm install          # frontend deps (includes concurrently)
npm --prefix api install   # API deps
```

### 2. Configure environment

Copy `.env` and fill in your Firebase project values:

```
VITE_FIREBASE_API_KEY=...
VITE_FIREBASE_AUTH_DOMAIN=...
VITE_FIREBASE_PROJECT_ID=...
VITE_FIREBASE_STORAGE_BUCKET=...
VITE_FIREBASE_MESSAGING_SENDER_ID=...
VITE_FIREBASE_APP_ID=...
```

The API server reads the same `.env` file from the project root.

To point the frontend at a different API host (e.g. in production) set:

```
VITE_API_URL=https://your-api-host.example.com
```

### 3. Run locally

```bash
npm run dev
```

This starts both the Vite dev server (default: http://localhost:5173) and the Express API (default: http://localhost:3001) concurrently.

To run them separately:

```bash
npm run dev:frontend   # Vite only
npm run dev:api        # Express API only
```

### 4. Build the frontend

```bash
npm run build
```

Static files are output to `dist/`.

## Deployment

### Frontend

Deploy `dist/` to any static host. Firebase Hosting is already configured:

```bash
npm run build && firebase deploy --only hosting
```

### API

Deploy `api/` as any Node.js service (e.g. Railway, Render, Fly.io, a VPS). Set the same environment variables and ensure `npm install` runs before starting.

```bash
# example for a plain Node host
npm --prefix api install
npm --prefix api start    # starts on port 3001 (override with API_PORT)
```

Set `VITE_API_URL` in your frontend build environment to point at the deployed API.

