import request from '@/request'

// 获取文章评论接口
export function getCommentsByArticle(id) {
  return request({
    url: `/comments/article/${id}`,
    method: 'get'
  })
}
// 发布评论接口
export function publishComment(comment,token) {
  return request({
    headers: {'Authorization': token},
    url: '/comments/create/change',
    method: 'post',
    data: comment
  })
}

export function getCount(id) {
  return request({
    url: `/comments/queryCommentCount/${id}`,
    method: 'get',
  })
}
