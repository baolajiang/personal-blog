import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '../layout/index.vue'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                component: () => import('../views/dashboard/index.vue'),
                meta: { title: '仪表盘' }
            },
            // === 博客管理 ===
            {
                path: 'article/write',
                component: () => import('../views/article/write.vue'),
                meta: { title: '写文章' }
            },
            {
                path: 'article/list',
                component: () => import('../views/article/list.vue'),
                meta: { title: '文章列表' }
            },
            {
                path: 'category/index',
                component: () => import('../views/category/index.vue'),
                meta: { title: '分类管理' }
            },
            {
                path: 'tag/index',
                component: () => import('../views/tag/index.vue'),
                meta: { title: '标签管理' }
            },
            // === 运营管理 ===
            {
                path: 'comment/index',
                component: () => import('../views/comment/index.vue'),
                meta: { title: '评论管理' }
            },
            {
                path: 'link/index',
                component: () => import('../views/link/index.vue'),
                meta: { title: '友链管理' }
            },
            // === 用户管理 ===
            {
                path: 'user/index',
                component: () => import('../views/user/index.vue'),
                meta: { title: '用户列表', requireAuth: true }
            },
            // === 系统管理 ===
            {
                path: 'system/ip',
                component: () => import('../views/system/ip.vue'),
                meta: { title: 'IP黑名单' }
            },
            {
                path: 'system/log',
                component: () => import('../views/system/log.vue'),
                meta: { title: '操作日志' }
            },



        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})





export default router