/**
 * 应用配置管理
 * 统一管理所有硬编码的URL和配置项
 */

// 配置项类型定义
interface AppConfig {
  frontendLoginUrl: string
  backendBaseUrl: string
  requestTimeout: number
  tokenErrorCodes:readonly number[]
  redirectDelay: number
}

// 环境变量配置（可根据不同环境动态设置）
export const ENV_CONFIG = {
  // 开发环境
  development: {
    frontendLoginUrl: 'http://localhost:48082/#/login',
    backendBaseUrl: '/api',
    requestTimeout: 5000,
    tokenErrorCodes: [10003, 401, 403],
    redirectDelay: 1500
  },
  // 生产环境（可根据实际部署环境修改）
  production: {
    frontendLoginUrl: 'https://myo.pub:48082/login',
    backendBaseUrl: '/api',
    requestTimeout: 10000,
    tokenErrorCodes: [10003, 401, 403],
    redirectDelay: 1500
  }
} as const

// 获取当前环境配置（带类型）
export const getConfig = (): AppConfig => {
  console.log('当前环境:', import.meta.env.MODE)
  const env = import.meta.env.MODE || 'development'
  return ENV_CONFIG[env as keyof typeof ENV_CONFIG] || ENV_CONFIG.development
}

// 向后兼容的导出（简化版本）
export const APP_CONFIG = getConfig()