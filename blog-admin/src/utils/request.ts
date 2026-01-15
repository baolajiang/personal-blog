import axios from 'axios'
import { ElMessage } from 'element-plus'

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
// === 响应拦截器 (接收响应后执行) ===
service.interceptors.response.use(
    (response) => {
        // 检查业务逻辑是否成功
        const { data } = response

        // 如果业务逻辑失败（success: false）
        if (data && data.success === false) {
            // Token相关错误（10003: token不合法）
            if (data.code === 10003 || data.code === 401 || data.code === 403) {
                // 清除本地token
                localStorage.removeItem('token')

                // 显示提示信息
                ElMessage.error(data.msg || '登录已过期，请重新登录')

                // 跳转到app端登录页面
                setTimeout(() => {
                    window.location.href = 'http://localhost:48082/#/login'
                }, 1500)

                // 返回一个reject的Promise，阻止后续处理
                return Promise.reject(new Error(data.msg ))
            } else {
                // 其他业务错误，显示错误信息
                ElMessage.error(data.msg || '操作失败')

                return Promise.reject(new Error(data.msg))
            }
        }

        // 业务逻辑成功，正常返回
        return response
    },
    (error) => {
        // 处理HTTP错误
        if (error.response) {
            const { status, data } = error.response

            // Token失效或未授权 (401, 403)
            if (status === 401 || status === 403) {
                // 清除本地token
                localStorage.removeItem('token')

                // 显示提示信息
                ElMessage.error('登录已过期，请重新登录')

                // 跳转到app端登录页面
                setTimeout(() => {
                    window.location.href = 'http://localhost:48082/#/login'
                }, 1500)
            } else if (status >= 500) {
                ElMessage.error('服务器错误，请稍后重试')
            } else {
                // 其他错误显示后端返回的消息
                const message = data?.message || '请求失败'
                ElMessage.error(message)
            }
        } else if (error.request) {
            // 网络错误或请求超时
            ElMessage.error('网络错误，请检查网络连接')
        } else {
            // 其他错误
            ElMessage.error('请求错误：' + error.message)
        }

        return Promise.reject(error)
    }
)



export default service