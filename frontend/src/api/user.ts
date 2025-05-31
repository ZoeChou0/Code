import request from '@/utils/request'
import type { BackendResult, LoginSuccessData } from '@/types/api'

export interface LoginData {
  identifier: string
  password: string
}

export interface RegisterData {
  name: string
  email: string
  password: string
  phone: string
  role: 'user' | 'provider'
  verificationCode: string
}

export interface UserInfo {
  id: number
  name: string
  email: string
  phone: string
  role: string
  qualificationStatus?: string

  addressLine1?: string
  city?: string
  state?: string
  zipCode?: string
  profilePhotoUrl?: string
  birthday?: string

  status?: string;
}

export interface UserUpdateProfileData {
  name?: string;
  phone?: string;
  addressLine1?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  birthday?: string | null;
  profilePhotoUrl?: string;

}

// 登录，拿到 { code, message, data: { token } }
export function login(data: LoginData): Promise<BackendResult<LoginSuccessData>> {
  return request<BackendResult<LoginSuccessData>>({
    url: '/users/login',
    method: 'post',
    data
  })
}

// 注册
export function register(data: RegisterData): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: '/users/register',
    method: 'post',
    data
  })
}

// 获取当前登录用户信息
export function getUserInfo(): Promise<BackendResult<UserInfo>> {
  return request<BackendResult<UserInfo>>({
    url: '/users/info',
    method: 'get'
  })
}

// 退出登录
export function logout(): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: '/users/logout',
    method: 'post'
  })
}
// 发送验证码
export function sendVerificationCode(email: string): Promise<BackendResult<LoginSuccessData>> {
  return request({
    url: '/user/send-code',
    method: 'post',
    data: { email }
  })
}

// 发送邮箱验证码
export function sendEmailCode(email: string): Promise<BackendResult<null>> {
  return request({
    url: '/users/send-email-code',
    method: 'post',
    data: { email }
  });
}

//更新用户信息
export function updateUserProfile(data: UserUpdateProfileData): Promise<BackendResult<UserInfo>> {
  // 返回更新后的 UserInfo
  return request<BackendResult<UserInfo>>({
    url: '/users/profile', // 后端接口路径
    method: 'put',         // 使用 PUT 方法
    data                   // 传递要更新的数据
  })
}

export function getAllUsers(): Promise<BackendResult<UserInfo[]>> {
  return request<BackendResult<UserInfo[]>>({
    url: '/admin/users',
    method: 'get'
  })
}

/**
 * 禁用用户 (管理员)
 * @param userId 要禁用的用户 ID
 */
export function banUser(userId: number): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/admin/users/${userId}/ban`, // 对应后端的 PUT /admin/users/{id}/ban
    method: 'put' // 使用 PUT 请求
  })
}

/**
 * 解禁用户 (管理员)
 * @param userId 要解禁的用户 ID
 */
export function unbanUser(userId: number): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/admin/users/${userId}/unban`, // 对应后端的 PUT /admin/users/{id}/unban
    method: 'put' // 使用 PUT 请求
  })
}


export function getPendingProviders(): Promise<BackendResult<UserInfo[]>> {
  return request<BackendResult<UserInfo[]>>({
    url: '/admin/users/providers/pending-review', // 对应后端 GET
    method: 'get'
  })
}

export function approveProvider(providerId: number): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/admin/users/providers/${providerId}/approve`, // 对应后端 PUT
    method: 'put'
  })
}

export function rejectProvider(providerId: number): Promise<BackendResult<null>> {
  // 如果后端需要拒绝原因，这里需要添加 data: { reason: '...' }
  return request<BackendResult<null>>({
    url: `/admin/users/providers/${providerId}/reject`, // 对应后端 PUT
    method: 'put'
  })
}
