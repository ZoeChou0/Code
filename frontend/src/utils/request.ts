
import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { BackendResult } from '@/types/api';

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
  response => response.data, // 如果 code 是 200, 返回 Result 对象本身
  error => {
    // 后端返回的业务错误 (例如 Result.code 是 400 但 HTTP 状态码可能是 200 或 4xx)
    // 或者网络错误、服务器500错误
    const backendResult = error?.response?.data as BackendResult<any>; // 尝试获取 Result 对象
    let message = '请求失败';

    if (backendResult && backendResult.message) { // 优先使用 Result.message
      message = backendResult.message;
    } else if (error.message) { // 其次使用 Axios 的 error.message (通常是网络错误)
      message = error.message;
    }
    // 您原有的逻辑
    // const message = error?.response?.data || error.message || '请求失败'


    if (error.response?.status === 401) {
      ElMessage.error(message); // 例如：Token 失效，密码错误等
      // TODO: 可能需要引导用户重新登录
    } else {
      ElMessage.error(message); // 显示具体的业务错误或通用网络错误
    }

    return Promise.reject(error); // 继续传递错误，让调用方catch
  }
);

// 4. 改写默认导出，让它的签名是 Promise<T>
export default function request<T = any>(
  config: AxiosRequestConfig
): Promise<T> {
  // instance(config) 的内部签名是 Promise<AxiosResponse<any>>
  // 但因为我们上面拦截器把它变成了 Promise<any>.then(data)
  // 所以这里直接返回 instance(config) 就是 Promise<T>
  return instance.request<any, T>(config)
}