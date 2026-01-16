<template>
  <div class="not-found">
    <h1>404</h1>
    <h2>页面未找到</h2>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
// 这里可以添加任何需要的逻辑
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
}

h1 {
  font-size: 10rem;
  margin: 0;
  color: #409EFF;
}

h2 {
  font-size: 2rem;
  margin-bottom: 2rem;
}
</style>