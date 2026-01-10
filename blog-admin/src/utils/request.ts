import axios from 'axios'

// 创建 axios 实例
const service = axios.create({
    // 这里必须填 /api，配合 vite.config.ts 代理
    baseURL: '/api',
    timeout: 5000 // 请求超时时间
})

// 请求拦截器 (可以在这里统一把 Token 带给后端)
service.interceptors.request.use(
    (config) => {
        // 比如：const token = localStorage.getItem('token')
        // if (token) { config.headers.Authorization = token }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器 (可以在这里统一处理 401 没权限、500 报错)
service.interceptors.response.use(
    (response) => {
        // 假设后端返回的数据格式是 { code: 200, data: ..., msg: ... }
        const res = response.data
        // 这里可以根据后端的 code 做判断
        return res
    },
    (error) => {
        console.error('请求出错：', error)
        return Promise.reject(error)
    }
)

export default service