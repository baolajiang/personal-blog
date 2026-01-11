<template>
  <el-menu
      :default-active="route.path"
      class="el-menu-vertical-demo"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      :collapse="false"
      router
  >
    <div class="logo-container">
      <h2 class="logo-title">MyBlog Admin</h2>
    </div>

    <el-menu-item index="/dashboard">
      <el-icon><Odometer /></el-icon>
      <span>仪表盘</span>
    </el-menu-item>

    <el-sub-menu index="blog">
      <template #title>
        <el-icon><Document /></el-icon>
        <span>博客管理</span>
      </template>
      <el-menu-item index="/article/write">
        <el-icon><Edit /></el-icon>
        <span>写文章</span>
      </el-menu-item>
      <el-menu-item index="/article/list">
        <el-icon><List /></el-icon>
        <span>文章列表</span>
      </el-menu-item>
      <el-menu-item index="/category/index">
        <el-icon><Folder /></el-icon>
        <span>分类管理</span>
      </el-menu-item>
      <el-menu-item index="/tag/index">
        <el-icon><Collection /></el-icon>
        <span>标签管理</span>
      </el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="operation">
      <template #title>
        <el-icon><Operation /></el-icon>
        <span>运营管理</span>
      </template>
      <el-menu-item index="/comment/index">
        <el-icon><ChatDotRound /></el-icon>
        <span>评论管理</span>
      </el-menu-item>
      <el-menu-item index="/link/index">
        <el-icon><Link /></el-icon>
        <span>友链管理</span>
      </el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="user">
      <template #title>
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </template>
      <el-menu-item index="/user/index">
        <el-icon><UserFilled /></el-icon>
        <span>用户列表</span>
      </el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="system">
      <template #title>
        <el-icon><Setting /></el-icon>
        <span>系统管理</span>
      </template>
      <el-menu-item index="/system/ip">
        <el-icon><Warning /></el-icon>
        <span>IP 黑名单</span>
      </el-menu-item>
      <el-menu-item index="/system/log">
        <el-icon><DataLine /></el-icon>
        <span>操作日志</span>
      </el-menu-item>
    </el-sub-menu>

  </el-menu>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import {
  Odometer, Document, Edit, List, Folder, Collection,
  Operation, ChatDotRound, Link,
  User, UserFilled,
  Setting, Warning, DataLine
} from '@element-plus/icons-vue'

const route = useRoute()
</script>

<style scoped>
.el-menu-vertical-demo { height: 100vh; border-right: none; }
.logo-container { height: 60px; display: flex; align-items: center; justify-content: center; background-color: #2b3649; }
.logo-title { color: #fff; font-weight: 600; font-size: 20px; }
</style>