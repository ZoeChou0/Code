// import { BackendResult } from '@/types/api';
// import request from '@/utils/request';

// export interface ProviderDTO {
//   id: number;
//   name: string;
//   email: string;
//   phone: string;
//   role: string;
//   qualificationStatus: string;
//   qualificationRejectionReason?: string;
//   addressLine1?: string;
//   city?: string;
//   createdAt: string;
//   updatedAt: string;
//   approving?: boolean;
//   rejecting?: boolean;
// }

// // 获取所有服务商
// export function getAllProviders(): Promise<BackendResult<ProviderDTO[]>> {
//   return request<BackendResult<ProviderDTO[]>>({
//     url: '/admin/users/providers',
//     method: 'get'
//   });
// }

// // 获取待审核服务商
// export function getPendingProviders(): Promise<BackendResult<ProviderDTO[]>> {
//   return request<BackendResult<ProviderDTO[]>>({
//     url: '/admin/users/providers/pending',
//     method: 'get'
//   });
// }

// // 获取已批准服务商
// export function getApprovedProviders(): Promise<BackendResult<ProviderDTO[]>> {
//   return request<BackendResult<ProviderDTO[]>>({
//     url: '/admin/users/providers/approved',
//     method: 'get'
//   });
// }

// // 获取已拒绝服务商
// export function getRejectedProviders(): Promise<BackendResult<ProviderDTO[]>> {
//   return request<BackendResult<ProviderDTO[]>>({
//     url: '/admin/users/providers/rejected',
//     method: 'get'
//   });
// }

// // 批准服务商资质
// export function approveProvider(id: number): Promise<BackendResult<null>> {
//   return request<BackendResult<null>>({
//     url: `/admin/users/providers/${id}/approve`,
//     method: 'put'
//   });
// }

// // 拒绝服务商资质
// export function rejectProvider(id: number, reason: string): Promise<BackendResult<null>> {
//   return request<BackendResult<null>>({
//     url: `/admin/users/providers/${id}/reject`,
//     method: 'put',
//     data: { reason }
//   });
// } 


// frontend/src/api/admin.ts - 这部分可以保持现有结构，因为它与后端特定路径匹配
import { BackendResult } from '@/types/api';
import request from '@/utils/request';

export interface ProviderDTO {
  id: number;
  name: string;
  email: string;
  phone: string;
  role: string;
  qualificationStatus: string; // PENDING_REVIEW, APPROVED, REJECTED
  qualificationRejectionReason?: string;
  addressLine1?: string;
  city?: string;
  state?: string; // 后端 Users 实体有 state，ProviderDTO 也应该有
  zipCode?: string; // 后端 Users 实体有 zipCode
  createdAt: string; // 通常是 LocalDateTime，前端接收为 string
  updatedAt: string;
  // 根据需要添加更多前端展示所需的字段
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