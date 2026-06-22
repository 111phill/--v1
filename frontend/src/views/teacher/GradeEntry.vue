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
        <!-- 选择器行 -->
        <el-row :gutter="16" style="margin-bottom:16px">
          <el-col :xs="24" :sm="8">
            <span style="line-height:32px;margin-right:8px">学期</span>
            <el-select v-model="semesterId" placeholder="选择学期" @change="onSemesterChange" style="width:180px">
              <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="10">
            <span style="line-height:32px;margin-right:8px">课程-班级</span>
            <el-select v-model="selectedBinding" placeholder="请选择课程和班级"
                       @change="onBindingChange" style="width:320px" value-key="key">
              <el-option v-for="b in bindings" :key="b.key"
                         :label="b.courseName + ' — ' + b.className"
                         :value="b" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="6" v-if="currentCourse">
            <el-tag type="info">学分: {{ currentCourse.credit }}</el-tag>
            <el-tag type="info" style="margin-left:8px">
              平时{{ (currentCourse.usualRatio * 100).toFixed(0) }}% / 期末{{ (currentCourse.examRatio * 100).toFixed(0) }}%
            </el-tag>
          </el-col>
        </el-row>

        <!-- 操作按钮 -->
        <div style="margin-bottom:16px">
          <el-upload :before-upload="handleExcel" :show-file-list="false"
                     accept=".xlsx" :disabled="!canEdit || !selectedBinding">
            <el-button type="success" :disabled="!canEdit || !selectedBinding">导入 Excel</el-button>
          </el-upload>
          <el-button type="primary" :disabled="!canEdit || !selectedBinding"
                     @click="saveDraft" :loading="saving" style="margin-left:10px">暂存</el-button>
          <el-button type="warning" :disabled="!canEdit || !selectedBinding"
                     @click="submitGrades" :loading="saving" style="margin-left:10px">正式提交</el-button>
          <el-tag v-if="isSubmitted" type="warning" style="margin-left:10px">成绩已提交，已锁定</el-tag>
        </div>

        <!-- 成绩表格 -->
        <el-table :data="gradeTable" border v-loading="loading" :row-class-name="rowClass">
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="100" />
          <el-table-column label="平时成绩 (0-100)" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.usualScore" :min="0" :max="100" :precision="1"
                               :disabled="!canEdit" size="small" controls-position="right"
                               @change="(v: number | undefined) => onScoreChange(row)" />
            </template>
          </el-table-column>
          <el-table-column label="期末成绩 (0-100)" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.examScore" :min="0" :max="100" :precision="1"
                               :disabled="!canEdit" size="small" controls-position="right"
                               @change="(v: number | undefined) => onScoreChange(row)" />
            </template>
          </el-table-column>
          <el-table-column label="总评（预览）" width="110">
            <template #default="{ row }">
              <span v-if="row.previewScore != null" :style="{color: row.previewScore < 60 ? '#F56C6C' : '#67C23A', fontWeight:'bold'}">
                {{ row.previewScore }}
              </span>
              <span v-else style="color:#ccc">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalScore" label="已存总评" width="100">
            <template #default="{ row }">
              <span v-if="row.totalScore != null">{{ row.totalScore }}</span>
              <span v-else style="color:#ccc">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.status==='SUBMITTED'" type="warning" size="small">已提交</el-tag>
              <el-tag v-else-if="row.totalScore != null" type="success" size="small">草稿</el-tag>
              <el-tag v-else type="info" size="small">未录入</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="gradeTable.length > 0" style="margin-top:12px;color:#909399">
          共 {{ gradeTable.length }} 名学生，已录入 {{ enteredCount }} 人，未录入 {{ gradeTable.length - enteredCount }} 人
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { courseApi } from '@/api/course'
import { gradeApi } from '@/api/grade'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)

// --- 状态 ---
const loading = ref(false)
const saving = ref(false)
const semesters = ref<any[]>([])
const semesterId = ref<number | null>(null)
const bindings = ref<any[]>([])           // 当前学期下该教师的课程-班级绑定
const selectedBinding = ref<any>(null)     // 当前选中的 {courseId, classId, courseName, className, usualRatio, examRatio, credit}
const gradeTable = ref<any[]>([])          // 成绩表格数据
const currentCourse = ref<any>(null)       // 当前课程信息（含占比）

// --- 计算属性 ---
const canEdit = computed(() => !!selectedBinding.value && !gradeTable.value.some(g => g.status === 'SUBMITTED'))
const isSubmitted = computed(() => !!selectedBinding.value && gradeTable.value.some(g => g.status === 'SUBMITTED'))
const enteredCount = computed(() => gradeTable.value.filter(g => g.totalScore != null || g.usualScore != null).length)

// --- 初始化 ---
onMounted(async () => {
  // 获取学期列表
  const sRes = await courseApi.getSemesters()
  semesters.value = sRes.data || []

  // 默认选中最新学期
  if (semesters.value.length > 0) {
    const targetId = semesters.value[semesters.value.length - 1].id
    semesterId.value = targetId
    await loadBindings(targetId)
  }

  // 如果从"我的课程"页面跳转过来，自动选中
  const queryCourseId = Number(route.query.courseId)
  const queryClassId = Number(route.query.classId)
  if (queryCourseId && queryClassId) {
    const match = bindings.value.find(
      (b: any) => b.courseId === queryCourseId && b.classId === queryClassId
    )
    if (match) {
      selectedBinding.value = match
      currentCourse.value = match
      await loadGradeData()
    }
  }
})

// --- 学期切换 ---
async function onSemesterChange(sid: number) {
  selectedBinding.value = null
  currentCourse.value = null
  gradeTable.value = []
  await loadBindings(sid)
}

// --- 加载教师的课程-班级绑定 ---
async function loadBindings(sid: number) {
  bindings.value = []
  if (!user.value?.id) return
  const res = await courseApi.getCourses(user.value.id, sid)
  const courses = res.data || []
  bindings.value = courses.map((c: any) => ({
    key: c.id + '-' + c.classId,
    courseId: c.id,
    classId: c.classId,
    courseName: c.name,
    className: c.className,
    usualRatio: c.usualRatio,
    examRatio: c.examRatio,
    credit: c.credit,
    studentCount: c.studentCount,
    enteredCount: c.enteredCount
  }))
}

// --- 选择课程-班级绑定 ---
async function onBindingChange(binding: any) {
  selectedBinding.value = binding
  currentCourse.value = binding
  await loadGradeData()
}

// --- 加载学生名单与已有成绩 ---
async function loadGradeData() {
  const b = selectedBinding.value
  if (!b) return
  loading.value = true
  try {
    const [sRes, gRes] = await Promise.all([
      courseApi.getStudents(b.classId),
      gradeApi.getClassGrades(b.courseId, b.classId, semesterId.value!)
    ])
    const students = sRes.data || []
    const existing = gRes.data || []
    const gradeMap = new Map<number, any>(existing.map((g: any) => [g.studentId, g]))

    gradeTable.value = students.map((s: any) => {
      const g = gradeMap.get(s.userId)
      return {
        studentId: s.userId,
        studentNo: s.studentNo || s.username,
        studentName: s.realName,
        usualScore: g?.usualScore ?? null,
        examScore: g?.examScore ?? null,
        totalScore: g?.totalScore ?? null,
        status: g?.status ?? 'DRAFT',
        previewScore: g?.totalScore ?? null
      }
    })
  } finally {
    loading.value = false
  }
}

// --- 计算预览分数 ---
function onScoreChange(row: any) {
  const usual = row.usualScore
  const exam = row.examScore
  if (usual != null && exam != null && currentCourse.value) {
    const ur = Number(currentCourse.value.usualRatio || 0.3)
    const er = Number(currentCourse.value.examRatio || 0.7)
    row.previewScore = (usual * ur + exam * er).toFixed(1)
  } else {
    row.previewScore = row.totalScore ?? null
  }
}

// --- 行样式：未录入标黄 ---
function rowClass({ row }: any) {
  if (row.usualScore == null && row.examScore == null && row.totalScore == null) return 'row-empty'
  return ''
}

// --- Excel 导入 ---
function handleExcel(file: File) {
  const reader = new FileReader()
  reader.onload = (e) => {
    const wb = XLSX.read(e.target?.result, { type: 'binary' })
    const sheet = wb.Sheets[wb.SheetNames[0]]
    const rows = XLSX.utils.sheet_to_json(sheet) as any[]
    let count = 0
    rows.forEach(row => {
      const g = gradeTable.value.find(
        s => s.studentNo === String(row['学号']))
      if (!g || g.status === 'SUBMITTED') return
      const us = Number(row['平时成绩'])
      const es = Number(row['期末成绩'])
      if (us >= 0 && us <= 100) { g.usualScore = us; count++ }
      if (es >= 0 && es <= 100) { g.examScore = es }
      onScoreChange(g)
    })
    ElMessage.success(`已导入 ${count} 条记录`)
  }
  reader.readAsBinaryString(file)
  return false
}

// --- 暂存 ---
async function saveDraft() {
  await doSave('DRAFT')
}

// --- 正式提交 ---
async function submitGrades() {
  const emptyRows = gradeTable.value.filter(
    g => g.usualScore == null || g.examScore == null)
  if (emptyRows.length > 0) {
    const names = emptyRows.map(g => g.studentNo).join('、')
    ElMessage.warning(`以下学生成绩未录入：${names}`)
    return
  }
  await ElMessageBox.confirm(
    '提交后成绩将锁定，无法再修改。确定提交吗？',
    '确认提交',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  )
  await doSave('SUBMITTED')
}

// --- 核心保存逻辑 ---
async function doSave(action: string) {
  const b = selectedBinding.value
  if (!b) return
  saving.value = true
  try {
    await gradeApi.batchSave({
      courseId: b.courseId,
      classId: b.classId,
      semesterId: semesterId.value!,
      action,
      grades: gradeTable.value.map(g => ({
        studentId: g.studentId,
        usualScore: g.usualScore ?? 0,
        examScore: g.examScore ?? 0
      }))
    })
    ElMessage.success(action === 'SUBMITTED' ? '成绩提交成功，已锁定' : '成绩暂存成功')
    await loadGradeData()
  } catch (e: any) {
    // 错误已在拦截器处理，这里做静默处理
  } finally {
    saving.value = false
  }
}

function logout() { userStore.logout(); router.push('/login') }
</script>

<style scoped>
.row-empty {
  background-color: #fdf6ec;
}
</style>
