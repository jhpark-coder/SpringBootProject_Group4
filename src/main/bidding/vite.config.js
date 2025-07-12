// bidding/vite.config.js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  // 이 앱의 모든 경로는 /bidding/ 을 기준으로 합니다.
  base: '/bidding/',
  plugins: [react()],
  build: {
    // 빌드 결과물은 Spring Boot 프로젝트의 static/bidding 폴더로 갑니다.
    outDir: '../../main/resources/static/bidding', // 상대 경로 수정
    emptyOutDir: true,
    rollupOptions: {
      // 진입점은 index.html 입니다. (Vite의 기본 방식)
      input: {
        main: path.resolve(__dirname, 'index.html')
      },
      output: {
        // 파일 이름에 해시값이 붙지 않도록 하여 Thymeleaf에서 부르기 쉽게 만듭니다.
        entryFileNames: `assets/[name].js`,
        chunkFileNames: `assets/[name].js`,
        assetFileNames: `assets/[name].[ext]`
      }
    }
  },
  server: {
    // 프록시 설정은 그대로 유지
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})