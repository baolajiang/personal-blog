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


// 更新用户信息
export function updateUser(data: any) {
    return request({
        url: '/admin/user/update',
        method: 'post',
        data
    })
}


