import request from '@/request'
// 登录接口
export function login(account, password) {
  const data = {
    account,
    password
  }
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

//登出
export function logout(token) {
  return request({
    headers: {'Authorization': token},
    url: '/logout',
    method: 'get'
  })
}
//获取当前登录用户的信息
export function getUserInfo(token) {
  return request({
    headers: {'Authorization': token},
    url: '/users/currentUser',
    method: 'get'
  })
}
// 注册接口
export function register(data) {
  return request({
    url: '/register',
    method: 'post',
    data // 简写为 data 原行 data: data
  })
}

// 发送验证码接口
export function sendCode(email) {
  return request({
    url: '/register/sendCode',
    method: 'post',
    data: { email }
  })
}
