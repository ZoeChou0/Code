
import { BackendResult } from '@/types/api';
import request from '@/utils/request';

export interface ProviderDTO {
  id: number;
  name: string;
  email: string;
  phone: string;
  role: string;
  qualificationStatus: string;
  qualificationRejectionReason?: string;
  addressLine1?: string;
  city?: string;
  state?: string;          // 确保存在
  zipCode?: string;        // 确保存在
  birthday?: string;       // <<--- 新增 (LocalDate 会被序列化为 "YYYY-MM-DD" 字符串)
  profilePhotoUrl?: string; // 确保存在
  status?: string;         // <<--- 新增 (例如 'active', 'banned')
  createdAt: string;       // LocalDateTime 会被序列化为 ISO 字符串
  updatedAt: string;       // LocalDateTime 会被序列化为 ISO 字符串
}


// 获取所有服务商 (通常指所有角色为 'provider' 的用户)
export function getAllProviders(): Promise<BackendResult<ProviderDTO[]>> {
  return request<BackendResult<ProviderDTO[]>>({
    url: '/admin/users/providers', // 后端 AdminUserController.@GetMapping("/providers")
    method: 'get'
  });
}

// 获取待审核服务商
export function getPendingProviders(): Promise<BackendResult<ProviderDTO[]>> {
  return request<BackendResult<ProviderDTO[]>>({
    url: '/admin/users/providers/pending', // 对应后端 AdminUserController.@GetMapping("/providers/pending")
    method: 'get'
  });
}

// 获取已批准服务商
export function getApprovedProviders(): Promise<BackendResult<ProviderDTO[]>> {
  return request<BackendResult<ProviderDTO[]>>({
    url: '/admin/users/providers/approved', // 对应后端 AdminUserController.@GetMapping("/providers/approved")
    method: 'get'
  });
}

// 获取已拒绝服务商
export function getRejectedProviders(): Promise<BackendResult<ProviderDTO[]>> {
  return request<BackendResult<ProviderDTO[]>>({
    url: '/admin/users/providers/rejected', // 对应后端 AdminUserController.@GetMapping("/providers/rejected")
    method: 'get'
  });
}

// 批准服务商资质
export function approveProvider(id: number): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/admin/users/providers/${id}/approve`,
    method: 'put'
  });
}

// 拒绝服务商资质
export function rejectProvider(id: number, reason: string): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/admin/users/providers/${id}/reject`,
    method: 'put',
    data: { reason } // 后端 AdminUserController 已修改为接收 @RequestBody Map<String, String> payload
  });
}


export function getProviderById(id: number): Promise<BackendResult<ProviderDTO>> {
  return request<BackendResult<ProviderDTO>>({
    url: `/admin/users/providers/${id}`, // 确保路径与后端控制器匹配
    method: 'get'
  });
}