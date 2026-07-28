# UI

React + Vite front-end for the Distributed Tic Tac Toe simulation.

See the [repository root README](../README.md) for full build/run instructions, architecture notes, and API documentation. Quick start:

```bash
npm install
npm run dev
```

This starts the dev server on http://localhost:5173 and proxies `/sessions/*` calls to the Game Session Service on http://localhost:8082 (see `vite.config.js`). Start both backend services first.
