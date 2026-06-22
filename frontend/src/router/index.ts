import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/student',
      name: 'StudentHome',
      component: () => import('@/views/student/Home.vue'),
      meta: { requiresAuth: true, roles: ['STUDENT'] }
    },
    {
      path: '/student/grades',
      name: 'StudentGrades',
      component: () => import('@/views/student/Grades.vue'),
      meta: { requiresAuth: true, roles: ['STUDENT'] }
    },
    {
      path: '/teacher',
      name: 'TeacherHome',
      component: () => import('@/views/teacher/Home.vue'),
      meta: { requiresAuth: true, roles: ['TEACHER'] }
    },
    {
      path: '/teacher/entry',
      name: 'GradeEntry',
      component: () => import('@/views/teacher/GradeEntry.vue'),
      meta: { requiresAuth: true, roles: ['TEACHER'] }
    },
    {
      path: '/admin',
      name: 'AdminHome',
      component: () => import('@/views/admin/Home.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    {
      path: '/admin/users',
      name: 'AdminUsers',
      component: () => import('@/views/admin/Users.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    {
      path: '/admin/review',
      name: 'AdminReview',
      component: () => import('@/views/admin/Review.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    { path: '/', redirect: '/login' },
    { path: '/:pathMatch(.*)*', redirect: '/login' }
  ]
})

router.beforeEach((to, _from, next) => {
  if (!to.meta.requiresAuth) {
    next()
    return
  }
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (!token || !user) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.roles && !(to.meta.roles as string[]).includes(user.role)) {
    const defaultRoutes: Record<string, string> = {
      STUDENT: '/student', TEACHER: '/teacher', ADMIN: '/admin'
    }
    next(defaultRoutes[user.role] || '/login')
    return
  }
  next()
})

export default router
