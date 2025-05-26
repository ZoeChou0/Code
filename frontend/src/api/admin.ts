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
  createdAt: string;
  updatedAt: string;
  approving?: boolean;
  rejecting?: boolean;
}

// 获取所有服务商
export function getAllProviders(): Promise<BackendResult<ProviderDTO[]>> {
  return request<BackendResult<ProviderDTO[]>>({
    url: '/admin/users/providers',
    method: 'get'
  });
}

// 获取待审核服务商
export function getPendingProviders(): Promise<BackendResult<ProviderDTO[]>> {
  return request<BackendResult<ProviderDTO[]>>({
    url: '/admin/users/providers/pending',
    method: 'get'
  });
}

// 获取已批准服务商
export function getApprovedProviders(): Promise<BackendResult<ProviderDTO[]>> {
  return request<BackendResult<ProviderDTO[]>>({
    url: '/admin/users/providers/approved',
    method: 'get'
  });
}

// 获取已拒绝服务商
export function getRejectedProviders(): Promise<BackendResult<ProviderDTO[]>> {
  return request<BackendResult<ProviderDTO[]>>({
    url: '/admin/users/providers/rejected',
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
    data: { reason }
  });
} 