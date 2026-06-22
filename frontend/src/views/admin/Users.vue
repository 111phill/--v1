<template>
  <el-container>
    <el-header style="background:#E6A23C;color:#fff;display:flex;align-items:center;justify-content:space-between">
      <span>大学生成绩管理系统 — 管理员端</span>
      <div>
        <span style="margin-right:20px">{{ user?.realName }}</span>
        <el-button type="danger" size="small" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" style="background:#f5f5f5;min-height:calc(100vh - 60px)">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/admin"><el-icon><HomeFilled /></el-icon> 控制台</el-menu-item>
          <el-menu-item index="/admin/users"><el-icon><User /></el-icon> 用户管理</el-menu-item>
          <el-menu-item index="/admin/review"><el-icon><Check /></el-icon> 成绩审核</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-upload :before-upload="handleImport" :show-file-list="false" accept=".xlsx">
              <el-button type="primary">导入学生 (Excel)</el-button>
            </el-upload>
          </el-col>
          <el-col :span="12">
            <el-input v-model="keyword" placeholder="搜索用户名/姓名" clearable @clear="loadUsers" @keyup.enter="loadUsers">
              <template #append><el-button @click="loadUsers">搜索</el-button></template>
            </el-input>
          </el-col>
        </el-row>

        <el-table :data="users" border style="margin-top:20px" v-loading="loading">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="100" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.role==='ADMIN'" type="danger">管理员</el-tag>
              <el-tag v-else-if="row.role==='TEACHER'">教师</el-tag>
              <el-tag v-else type="success">学生</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-switch :model-value="row.status==='ACTIVE'"
                @change="(v:boolean) => toggleStatus(row.id, v ? 'ACTIVE' : 'DISABLED')" />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="warning" size="small" @click="resetPwd(row.id)">重置密码</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { adminApi } from '@/api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)
const loading = ref(false)
const users = ref<any[]>([])
const keyword = ref('')

onMounted(() => loadUsers())

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminApi.listUsers({ keyword: keyword.value || undefined })
    users.value = res.data || []
  } finally { loading.value = false }
}

async function toggleStatus(userId: number, status: string) {
  await adminApi.toggleStatus(userId, status)
  ElMessage.success('状态已更新')
  loadUsers()
}

async function resetPwd(userId: number) {
  await adminApi.resetPassword(userId)
  ElMessage.success('密码已重置为 Reset@123')
}

async function handleImport(file: File) {
  try {
    await adminApi.importUsers(file)
    ElMessage.success('导入成功')
    loadUsers()
  } catch {}
  return false
}

function logout() { userStore.logout(); router.push('/login') }
</script>
