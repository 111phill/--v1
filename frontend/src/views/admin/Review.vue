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
        <h3>成绩修改审核</h3>
        <el-table :data="modifications" border v-loading="loading">
          <el-table-column prop="teacherName" label="申请人" width="100" />
          <el-table-column prop="courseName" label="课程" width="140" />
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="oldScore" label="原成绩" width="80" />
          <el-table-column prop="newScore" label="新成绩" width="80" />
          <el-table-column prop="reason" label="修改原因" min-width="160" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.status==='PENDING'">待审核</el-tag>
              <el-tag v-else-if="row.status==='APPROVED'" type="success">已通过</el-tag>
              <el-tag v-else type="danger">已驳回</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" v-if="modifications.some(m=>m.status==='PENDING')">
            <template #default="{ row }">
              <template v-if="row.status==='PENDING'">
                <el-button type="success" size="small" @click="review(row.id, 'APPROVED')">通过</el-button>
                <el-button type="danger" size="small" @click="review(row.id, 'REJECTED')">驳回</el-button>
              </template>
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
import { gradeApi } from '@/api/grade'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)
const loading = ref(false)
const modifications = ref<any[]>([])

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await gradeApi.getPendingModifications()
    modifications.value = res.data || []
  } finally { loading.value = false }
}

async function review(id: number, action: string) {
  try {
    await gradeApi.reviewModification(id, action,
      action === 'APPROVED' ? '审核通过' : '审核驳回')
    ElMessage.success(action === 'APPROVED' ? '已通过' : '已驳回')
    loadData()
  } catch {}
}

function logout() { userStore.logout(); router.push('/login') }
</script>
