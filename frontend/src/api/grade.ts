import http from '@/utils/request'

export const gradeApi = {
  getMyGrades(semesterId: number) {
    return http.get('/grades', { params: { semesterId } })
  },
  getClassGrades(courseId: number, classId: number, semesterId: number) {
    return http.get('/grades/class', { params: { courseId, classId, semesterId } })
  },
  batchSave(data: {
    courseId: number; classId: number; semesterId: number;
    action: string; grades: { studentId: number; usualScore: number; examScore: number }[]
  }) {
    return http.post('/grades/batch', data)
  },
  getStatistics(courseId: number, classId: number, semesterId: number) {
    return http.get('/grades/statistics', { params: { courseId, classId, semesterId } })
  },
  requestModification(gradeId: number, newScore: number, reason: string) {
    return http.post('/grades/modification', { gradeId, newScore, reason })
  },
  getPendingModifications() {
    return http.get('/grades/modifications/pending')
  },
  reviewModification(modId: number, action: string, reviewComment: string) {
    return http.put(`/grades/modification/${modId}`, { action, reviewComment })
  },
  publishGrades(semesterId: number) {
    return http.put('/grades/publish', null, { params: { semesterId } })
  },
  getGpaTrend() {
    return http.get('/grades/gpa-trend')
  }
}
