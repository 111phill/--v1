<template>
  <el-container>
    <el-header style="background:#67C23A;color:#fff;display:flex;align-items:center;justify-content:space-between">
      <span>大学生成绩管理系统 — 教师端</span>
      <div>
        <span style="margin-right:20px">{{ user?.realName }}</span>
        <el-button type="danger" size="small" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" style="background:#f5f5f5;min-height:calc(100vh - 60px)">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/teacher"><el-icon><HomeFilled /></el-icon> 我的课程</el-menu-item>
          <el-menu-item index="/teacher/entry"><el-icon><Edit /></el-icon> 成绩录入</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <h3>我的授课课程</h3>
        <el-table :data="courses" border v-loading="loading">
          <el-table-column prop="name" label="课程名称" />
          <el-table-column prop="code" label="课程编号" />
          <el-table-column prop="credit" label="学分" width="80" />
          <el-table-column prop="className" label="授课班级" />
          <el-table-column prop="studentCount" label="选课人数" width="100" />
          <el-table-column prop="enteredCount" label="已录入" width="80" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="goEntry(row)">录入成绩</el-button>
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
import { courseApi } from '@/api/course'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)
const loading = ref(false)
const courses = ref<any[]>([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await courseApi.getCourses(user.value?.id)
    courses.value = res.data || []
  } finally { loading.value = false }
})

function goEntry(course: any) {
  router.push({
    path: '/teacher/entry',
    query: { courseId: course.id, classId: course.classId }
  })
}

function logout() { userStore.logout(); router.push('/login') }
</script>
