import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { BackendResult, LoginSuccessData } from '@/types/api'
import type { LoginData, RegisterData, UserInfo } from '@/api/user'
import { login, register, getUserInfo, logout as apiLogout } from '@/api/user'
import { ElMessage } from 'element-plus'

export type UserRole = 'user' | 'provider' | 'admin'

export const useUserStore = defineStore('user', () => {
  // token 用来给后续请求带上
  const token = ref<string>(localStorage.getItem('token') || '')

  const userInfo = ref<UserInfo | null>(null)

  async function loginAction(loginData: LoginData): Promise<boolean> {
    try {
      const res: BackendResult<LoginSuccessData> = await login(loginData)
      if (res.code === 200 && res.data?.token) {
        // 1. 保存 token
        token.value = res.data.token
        localStorage.setItem('token', res.data.token)

        // 2. 立即拉取用户信息
        const infoRes: BackendResult<UserInfo> = await getUserInfo()
        if (infoRes.code === 200 && infoRes.data) {
          userInfo.value = infoRes.data
        } else {
          console.warn('获取用户信息失败：', infoRes.message)
        }

        return true
      } else {
        console.warn('登录失败：', res.message)
        return false
      }
    } catch (e: any) {
      console.error('loginAction 出错：', e)
      return false
    }
  }

  /** 注册 */
  async function registerAction(registerData: RegisterData): Promise<boolean> {
    try {
      const res = await register(registerData)
      if (res.code === 200) {
        ElMessage.success('注册成功，请登录')
        return true
      } else {
        ElMessage.error(res.message || '注册失败')
        return false
      }
    } catch (e: any) {
      console.error('registerAction 出错：', e)
      ElMessage.error(e.message || '注册请求异常')
      return false
    }
  }

  /** 只是拉取用户信息（比如页面刷新后） */
  async function getUserInfoAction(): Promise<boolean> {
    try {
      const res: BackendResult<UserInfo> = await getUserInfo()
      if (res.code === 200 && res.data) {
        userInfo.value = res.data
        return true
      } else {
        ElMessage.error(res.message || '获取用户信息失败')
        return false
      }
    } catch (e: any) {
      console.error('getUserInfoAction 出错：', e)
      ElMessage.error(e.message || '获取用户信息异常')
      return false
    }
  }

  /** 退出登录 */
  async function logoutAction(): Promise<boolean> {
    try {

      const res = await apiLogout(); // 调用重命名后的 API 函数

      if (res.code !== 200) {

        console.warn('Backend logout failed:', res.message);
        // 可以选择是否提示用户，或者静默处理
        // ElMessage.error(res.message || '退出登录时后端发生错误');
        // return false; // 可以选择在这里中断，但通常不建议，前端清理更重要
      } else {
        console.log('Backend logout successful.');
      }

      token.value = '';
      userInfo.value = null;
      localStorage.removeItem('token');

      ElMessage.success('您已成功退出登录'); // 给用户反馈
      return true;

    } catch (e: any) {
      console.error('logoutAction 出错：', e);
      // 网络错误等也可能导致调用失败，同样应该清理前端
      token.value = '';
      userInfo.value = null;
      localStorage.removeItem('token');
      // (可选) 清理 Axios Header

      ElMessage.error(e.message || '退出登录请求异常，但已在本地清除登录状态');
      return false; // 返回 false 表示过程中有异常
    }
  }

  /** 下面是一些角色判断的辅助方法 */
  function hasRole(role: UserRole): boolean {
    return userInfo.value?.role === role
  }
  function isUser() { return hasRole('user') }
  function isProvider() { return hasRole('provider') }
  function isAdmin() { return hasRole('admin') }

  return {
    token,
    userInfo,
    loginAction,
    registerAction,
    getUserInfoAction,
    logoutAction,
    hasRole,
    isUser,
    isProvider,
    isAdmin,
  }
})