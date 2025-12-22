import request from '@/request'

export function sendChatMsg(message) {
  return request({
    url: `/chat/send/`,   // 对应后端 Controller 的地址
    method: 'post',
    data: {
      message: message   // 传给后端的参数
    }
  })
}
