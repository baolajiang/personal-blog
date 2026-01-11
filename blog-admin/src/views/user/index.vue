<template>
  <div class="index-container">
    <el-card class="index-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <h3>用户管理</h3>
            <span class="subtitle">共 {{ total }} 位用户</span>
          </div>
          <el-button type="primary" :loading="loading" @click="fetchData">刷新列表</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" border stripe>

        <el-table-column label="头像" width="70" align="center">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" shape="square" size="small" />
          </template>
        </el-table-column>

        <el-table-column prop="account" label="账号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.online" type="success" effect="dark" round size="small">
              在线
            </el-tag>
            <el-tag v-else type="info" effect="plain" round size="small">
              离线
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="最近登录IP" width="140" show-overflow-tooltip>
          <template #default="scope">
            <div>{{ scope.row.ipaddr || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="最后活跃" width="180">
          <template #default="scope">
            <div v-if="scope.row.lastLogin">
              <div class="time-text">{{ formatTime(scope.row.lastLogin) }}</div>
              <div class="time-ago">{{ formatRelativeTime(scope.row.lastLogin) }}</div>
            </div>
            <span v-else class="no-data">从未登录</span>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createDate) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="">
            <el-button link type="primary" size="small">编辑</el-button>
            <el-button link type="danger" size="small">封禁</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, prev, pager, next, jumper"
            :total="total"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 初始化 dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  pageSize: 10
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request.post('/admin/user/list', queryParams)
    if (res.success) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchData()
}

// 时间格式化工具
const formatTime = (time: number) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
const formatRelativeTime = (time: number) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.index-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.subtitle { margin-left: 10px; font-size: 13px; color: #999; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.time-text { font-size: 13px; }
.time-ago { font-size: 12px; color: #999; }
.no-data { color: #ccc; font-size: 12px; }
</style>