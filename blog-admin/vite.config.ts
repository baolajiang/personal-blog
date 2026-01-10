import { defineConfig } from 'vite'

// @ts-ignore
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    server: {
        host: '0.0.0.0', // 允许局域网访问
        port: 48182,      // 前端端口
        open: true,      // 启动自动打开浏览器
        proxy: {
            // 代理配置：让前端能访问 Spring Boot (48882)
            '/api': {
                target: 'http://localhost:48882',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, '')
            }
        }
    }
})