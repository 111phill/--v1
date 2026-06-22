<template>
  <el-container>
    <el-header style="background:#409EFF;color:#fff;display:flex;align-items:center;justify-content:space-between">
      <span>大学生成绩管理系统 — 学生端</span>
      <div>
        <span style="margin-right:20px">{{ user?.realName }}</span>
        <el-button type="danger" size="small" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" style="background:#f5f5f5;min-height:calc(100vh - 60px)">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/student">
            <el-icon><HomeFilled /></el-icon> 首页
          </el-menu-item>
          <el-menu-item index="/student/grades">
            <el-icon><Document /></el-icon> 我的成绩
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <h3>欢迎, {{ user?.realName }}</h3>
        <el-descriptions :column="2" border style="margin-top:20px">
          <el-descriptions-item label="学号">{{ user?.username }}</el-descriptions-item>
          <el-descriptions-item label="角色">学生</el-descriptions-item>
        </el-descriptions>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>
