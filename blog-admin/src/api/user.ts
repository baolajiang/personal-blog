// src/api/user.ts
import request from '../utils/request'

// 获取用户列表
export function getUserList(data: any) {
    return request({
        url: '/admin/user/list',
        method: 'post',
        data
    })
}

// 修改用户状态 (封禁/解封/观察)
// 注意：这里把 URL 改成了我们刚才在 AdminController 里写的 /admin/user/status
export function updateUserStatus(data: any) {
    return request({
        url: '/admin/user/status',
        method: 'post',
        data
    })
}