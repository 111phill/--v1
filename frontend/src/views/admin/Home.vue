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
        <h3>管理员控制台</h3>
        <el-row :gutter="20">
          <el-col :xs="24" :md="12" style="margin-bottom:20px">
            <el-card>
              <template #header>学期管理</template>
              <el-select v-model="semesterId" placeholder="选择学期" @change="publishGrades">
                <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
              <el-button type="success" @click="publishGrades" style="margin-left:10px">发布成绩</el-button>
            </el-card>
          </el-col>
          <el-col :xs="24" :md="12" style="margin-bottom:20px">
            <el-card>
              <template #header>待审核修改申请</template>
              <el-statistic :value="pendingCount" />
              <el-button type="primary" @click="$router.push('/admin/review')" style="margin-top:10px">去审核</el-button>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { courseApi } from '@/api/course'
import { gradeApi } from '@/api/grade'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)
const semesters = ref<any[]>([])
const semesterId = ref<number|null>(null)
const pendingCount = ref(0)

onMounted(async () => {
  const [sRes, pRes] = await Promise.all([
    courseApi.getSemesters(),
    gradeApi.getPendingModifications()
  ])
  semesters.value = sRes.data || []
  pendingCount.value = (pRes.data || []).length
})
async function publishGrades() {
  if (!semesterId.value) { ElMessage.warning('请选择学期'); return }
  try {
    const res = await gradeApi.publishGrades(semesterId.value)
    ElMessage.success(res.msg)
  } catch {}
}
function logout() { userStore.logout(); router.push('/login') }
</script>
