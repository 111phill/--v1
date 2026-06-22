import http from '@/utils/request'

export const adminApi = {
  importUsers(file: File) {
    const fd = new FormData()
    fd.append('file', file)
    return http.post('/admin/users/import', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  listUsers(params: { role?: string; keyword?: string; page?: number; size?: number }) {
    return http.get('/admin/users', { params })
  },
  toggleStatus(userId: number, status: string) {
    return http.put(`/admin/users/${userId}/status`, null, { params: { status } })
  },
  resetPassword(userId: number) {
    return http.put(`/admin/users/${userId}/reset-password`)
  },
  queryLogs(params: any) {
    return http.get('/admin/logs', { params })
  },
  dashboard() {
    return http.get('/admin/dashboard')
  },
  bindCourse(courseId: number, classId: number, teacherId: number, semesterId: number) {
    return http.post(`/admin/courses/${courseId}/bind`, null, {
      params: { classId, teacherId, semesterId }
    })
  }
}
