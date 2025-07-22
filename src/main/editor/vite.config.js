import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  base: '/editor/',
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    outDir: '../resources/static/editor',
    emptyOutDir: true,
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom'],
          'tiptap-vendor': [
            '@tiptap/react',
            '@tiptap/starter-kit',
            '@tiptap/extension-underline',
            '@tiptap/extension-link',
            '@tiptap/extension-text-align',
            '@tiptap/extension-text-style'
          ],
          'icons-vendor': ['lucide-react'],
          'utils-vendor': ['axios', 'lowlight', 'highlight.js']
        }
      }
    }
  },
  server: {
    proxy: {
      '/editor/api': {
        target: process.env.VITE_API_SERVER_URL || 'http://43.202.160.225:8080',
        changeOrigin: true,
        cookieDomainRewrite: ""
      },
      '/api': {
        target: process.env.VITE_API_SERVER_URL || 'http://43.202.160.225:8080',
        changeOrigin: true,
        cookieDomainRewrite: ""
      },
      '/uploads': {
        target: process.env.VITE_API_SERVER_URL || 'http://43.202.160.225:8080',
        changeOrigin: true,
        cookieDomainRewrite: ""
      }
    }
  }
})
