<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px">
        <Sidebar />
      </el-aside>

      <el-container>
        <el-header style="padding: 0;">
          <Navbar />
        </el-header>

        <el-main style="padding: 0;">
          <AppMain />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'
</script>

<style scoped>
.common-layout {
  height: 100vh;
  width: 100vw;
}
</style>