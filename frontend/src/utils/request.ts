import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

// 请求拦截器
http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
http.interceptors.response.use(
  response => {
    const newToken = response.headers['x-refreshed-token']
    if (newToken) {
      localStorage.setItem('token', newToken)
    }
    return response.data
  },
  error => {
    const status = error.response?.status
    switch (status) {
      case 401:
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
        break
      case 403:
        ElMessage.error('无操作权限')
        break
      case 500:
        ElMessage.error('服务器内部错误')
        break
      default:
        if (error.code === 'ECONNABORTED') {
          ElMessage.error('网络连接超时')
        } else {
          ElMessage.error(error.response?.data?.msg || '请求失败')
        }
    }
    return Promise.reject(error)
  }
)

export default http
