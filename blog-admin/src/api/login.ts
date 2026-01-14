import request from '../utils/request'

// 票据兑换 Token 接口
export function exchangeToken(ticket: string) {
    return request({
        url: '/login/ticket/exchange',
        method: 'post',
        data: ticket,
        // 显式指定发送 JSON 格式，Axios 会自动将字符串转为 "TICKET_XXX" (带双引号)
        // 后端的 replace("\"", "") 正是用来处理这个双引号的
        headers: {
            'Content-Type': 'application/json'
        }
    })
}