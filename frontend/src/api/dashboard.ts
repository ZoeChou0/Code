import request from '@/utils/request';
import type { BackendResult } from '@/types/api';

export interface ProviderDashboardStats {
  pendingReservationsCount: number;
  activeServicesCount: number;
  completedOrdersThisMonth: number;
  // 可以添加更多统计数据，如本月收入等
}

// 【重要】获取服务商工作台统计数据 (需要后端实现 GET /provider/dashboard/stats)
export function getProviderDashboardStats(): Promise<BackendResult<ProviderDashboardStats>> {
  return request<BackendResult<ProviderDashboardStats>>({
    url: '/provider/dashboard/stats', // **确保后端有此接口**
    method: 'get'
  });
}