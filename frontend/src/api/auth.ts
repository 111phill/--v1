import http from '@/utils/request'

export const authApi = {
  login(username: string, password: string, role: string) {
    return http.post('/auth/login', { username, password, role })
  },
  refresh() {
    return http.post('/auth/refresh')
  }
}
