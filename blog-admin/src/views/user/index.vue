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

        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.email || '未绑定' }}</template>
        </el-table-column>

        <el-table-column prop="mobilePhoneNumber" label="手机号" width="130">
          <template #default="scope">{{ scope.row.mobilePhoneNumber || '未绑定' }}</template>
        </el-table-column>

        <el-table-column label="最近登录IP" width="130" show-overflow-tooltip>
          <template #default="scope">
            <div>{{ scope.row.ipaddr || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="最后活跃" width="160">
          <template #default="scope">
            <div v-if="scope.row.lastLogin">
              <div class="time-text">{{ formatTime(scope.row.lastLogin) }}</div>
              <div class="time-ago">{{ formatRelativeTime(scope.row.lastLogin) }}</div>
            </div>
            <span v-else class="no-data">从未登录</span>
          </template>
        </el-table-column>

        <el-table-column label="账号状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '99'" type="danger">已封禁</el-tag>
            <el-tag v-else-if="scope.row.status === '1'" type="warning">警告</el-tag>
            <el-tag v-else-if="scope.row.status === '0'" type="success">正常</el-tag>
            <el-tag v-else type="info">未知状态: {{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="在线状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.online" type="success" effect="plain" round size="small">在线</el-tag>
            <el-tag v-else type="info" effect="plain" round size="small">离线</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createDate) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>


            <el-button
                v-if="scope.row.status === '99'"
                link type="success" size="small"
                @click="handleStatusChange(scope.row, '0')"
            >解封</el-button>
            <el-button
                v-else
                link type="danger" size="small"
                @click="handleStatusChange(scope.row, '99')"
            >封禁</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserList, updateUserStatus } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  pageSize: 5
})

const formatTime = (timestamp: number) => {
  if (!timestamp) return ''
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm')
}

const formatRelativeTime = (timestamp: number) => {
  if (!timestamp) return ''
  return dayjs(timestamp).fromNow()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getUserList(queryParams)

    if (res.success && res.data) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else if(res.records) {
      tableData.value = res.records
      total.value = res.total
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  fetchData()
}
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchData()
}

const handleStatusChange = (row: any, status: string) => {
  const actionText = status === '99' ? '封禁' : '解封'

  ElMessageBox.confirm(
      `确定要${actionText}用户 "${row.nickname}" 吗？`,
      '提示',
      { type: 'warning' }
  ).then(async () => {
    try {
      const res: any = await updateUserStatus({
        id: row.id,
        status: status
      })
      if (res.success) {
        if(status === '99') {
          ElMessage.error(`${actionText}成功，用户已被封禁`)
        } else if(status === '0'){
          ElMessage.success(`${actionText}成功`)
        }else if(status === '1'){
          ElMessage.warning(`${actionText}成功，用户警告中`)
        }
        fetchData()
      } else {
        ElMessage.error(res.msg || `${actionText}失败`)
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('操作异常')
    }
  })
}

const handleEdit = (row: any) => {
  console.log('编辑用户', row)
  ElMessage.info('编辑功能开发中...')
}



onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.index-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.subtitle { margin-left: 10px; font-size: 13px; color: #999; }
.left { display: flex; align-items: center; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.time-text { font-size: 13px; line-height: 1.2; }
.time-ago { font-size: 12px; color: #999; margin-top: 2px; }
.no-data { color: #ccc; font-size: 12px; }
</style>