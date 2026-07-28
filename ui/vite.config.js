import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Proxies calls to the Game Session Service during `npm run dev` so the
    // UI can call same-origin relative paths and avoid CORS entirely.
    // Change the target if the session service runs on a different port.
    proxy: {
      '/sessions': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
    },
  },
})
