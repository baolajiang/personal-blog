<template>
  <div class="dashboard-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>👋 欢迎回来</span>
        </div>
      </template>
      <div class="text item">
        <h3>Admin 用户，下午好！</h3>
        <p>今天是你的博客运营第 1 天。</p>
      </div>
    </el-card>

    <div class="data-row">
      <el-card shadow="hover" class="data-card">
        <h3>文章总数</h3>
        <p class="num">0</p>
      </el-card>
      <el-card shadow="hover" class="data-card">
        <h3>总访问量</h3>
        <p class="num">0</p>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 20px;
}
.box-card {
  margin-bottom: 20px;
}
.data-row {
  display: flex;
  gap: 20px;
}
.data-card {
  flex: 1;
  text-align: center;
}
.num {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}
</style>