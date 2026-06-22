import http from '@/utils/request'

export const courseApi = {
  getCourses(teacherId?: number, semesterId?: number) {
    return http.get('/courses', { params: { teacherId, semesterId } })
  },
  getStudents(classId: number) {
    return http.get('/students', { params: { classId } })
  },
  getSemesters() {
    return http.get('/semesters')
  },
  getCurrentSemester() {
    return http.get('/semesters/current')
  }
}
