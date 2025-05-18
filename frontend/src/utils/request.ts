
import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

// 1. 新建一个 axios 实例
const instance = axios.create({
  baseURL: 'http://localhost:8080', // 或者你的后端地址
  timeout: 5_000,
})



// 2. 请求拦截器：比如自动带上 token
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  console.log('[DEBUG] 当前 request 请求配置为：', config)
  return config


})

// 3. 响应拦截器：把 data 拿出来
instance.interceptors.response.use(
  response => response.data,
  error => {
    const message = error?.response?.data || error.message || '请求失败'

    if (error.response?.status === 401) {
      ElMessage.error(message) // 显示“邮箱或密码错误”之类
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

// 4. 改写默认导出，让它的签名是 Promise<T>
export default function request<T = any>(
  config: AxiosRequestConfig
): Promise<T> {
  // instance(config) 的内部签名是 Promise<AxiosResponse<any>>
  // 但因为我们上面拦截器把它变成了 Promise<any>.then(data)
  // 所以这里直接返回 instance(config) 就是 Promise<T>
  return instance.request<any, T>(config)
}