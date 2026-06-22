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
          <el-menu-item index="/student"><el-icon><HomeFilled /></el-icon> 首页</el-menu-item>
          <el-menu-item index="/student/grades"><el-icon><Document /></el-icon> 我的成绩</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-select v-model="semesterId" placeholder="选择学期" @change="loadGrades" style="width:100%">
              <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-col>
        </el-row>

        <el-table :data="grades" border style="margin-top:20px" v-loading="loading">
          <el-table-column prop="courseName" label="课程名称" min-width="150" />
          <el-table-column prop="credit" label="学分" width="80" />
          <el-table-column prop="usualScore" label="平时成绩" width="100" />
          <el-table-column prop="examScore" label="期末成绩" width="100" />
          <el-table-column prop="totalScore" label="总评成绩" width="100">
            <template #default="{ row }">
              <el-tag :type="row.totalScore >= 60 ? 'success' : 'danger'">
                {{ row.totalScore }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="gpa" label="绩点" width="80" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status==='PUBLISHED'" type="success">已发布</el-tag>
              <el-tag v-else-if="row.status==='SUBMITTED'" type="warning">已提交</el-tag>
              <el-tag v-else type="info">草稿</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- GPA趋势图 -->
        <div ref="chartRef" style="width:100%;height:300px;margin-top:30px"></div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { gradeApi } from '@/api/grade'
import { courseApi } from '@/api/course'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)

const loading = ref(false)
const grades = ref<any[]>([])
const semesters = ref<any[]>([])
const semesterId = ref<number|null>(null)
const chartRef = ref<HTMLElement>()

onMounted(async () => {
  const res = await courseApi.getSemesters()
  semesters.value = res.data || []
  if (semesters.value.length > 0) {
    semesterId.value = semesters.value[semesters.value.length - 1].id
    await loadGrades()
  }
  await nextTick()
  await loadGpaChart()
})

async function loadGrades() {
  if (!semesterId.value) return
  loading.value = true
  try {
    const res = await gradeApi.getMyGrades(semesterId.value)
    grades.value = res.data || []
  } finally { loading.value = false }
}

async function loadGpaChart() {
  try {
    const res = await gradeApi.getGpaTrend()
    const data = res.data || []
    if (chartRef.value && data.length > 0) {
      const chart = echarts.init(chartRef.value)
      chart.setOption({
        title: { text: 'GPA 走势', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: { data: data.map((d:any) => d.semester_name || d.semesterName) },
        yAxis: { min: 0, max: 4.5 },
        series: [{
          name: '学期GPA', type: 'line', smooth: true,
          data: data.map((d:any) => d.semester_gpa || d.semesterGpa),
          itemStyle: { color: '#409EFF' }
        }, {
          name: '累计GPA', type: 'line', smooth: true,
          data: data.map((d:any) => d.cumulative_gpa || d.cumulativeGpa),
          itemStyle: { color: '#67C23A' }
        }]
      })
    }
  } catch {}
}

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>
