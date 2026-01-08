import request from '@/request'
// 获取所有分类接口
export function getAllCategorys() {
  return request({
    url: '/categorys',
    method: 'get',
  })
}
// 获取所有分类详情接口
export function getAllCategorysDetail() {
  return request({
    url: '/categorys/detail',
    method: 'get',
  })
}
// 获取分类接口
export function getCategory(id) {
  return request({
    url: `/categorys/${id}`,
    method: 'get',
  })
}
// 获取分类详情接口
export function getCategoryDetail(id) {
  return request({
    url: `/categorys/detail/${id}`,
    method: 'get',
  })
}
