import axios from 'axios'


// 创建 axios 实例
const service = axios.create({
    // 这里必须填 /api，配合 vite.config.ts 代理
    baseURL: '/api',
    timeout: 5000 // 请求超时时间
})

// === 请求拦截器 (发送请求前执行) ===
service.interceptors.request.use(
    (config) => {
        // 从 localStorage 获取 token
        // 注意：这里要在登录成功后把 token 存进去：localStorage.setItem('token', token)
        const token = localStorage.getItem('token')
        if (token) {
            // 把 Token 放到请求头 Header 中
            config.headers.Authorization = token
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)



export default service