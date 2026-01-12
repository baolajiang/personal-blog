/**
 * 用户接口
 * 处理用户相关的业务逻辑，如查询、注册、登录、更新等
 */
import request from '../utils/request'

// 获取用户列表
export function getUserList(data: any) {
    return request({
        url: '/admin/user/list',
        method: 'post',
        data
    })
}

// 修改用户状态 (封禁/解封/警告)
export function updateUserStatus(data: any) {
    return request({
        url: '/admin/user/status',
        method: 'post',
        data
    })
}


