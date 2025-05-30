import { BackendResult } from '@/types/api';
import request from '@/utils/request';

export interface Service {
  id: number
  name: string
  description: string
  price: number
  duration: number
  reviewStatus: 'PENDING' | 'APPROVED' | 'REJECTED'
  rejectionReason?: string
  providerId: number
  dailyCapacity: number
  requiredVaccinations: string
  requiresNeutered: boolean
  minAge?: number
  maxAge?: number
  temperamentRequirements?: string
  prohibitedBreeds?: string


  providerName?: string;         // 服务商名称
  providerProfilePhotoUrl?: string; // 服务商头像
  providerAddressLine1?: string; // 服务商地址行1
  providerCity?: string;         // 服务商城市
  providerState?: string;        // 服务商省份/州
  // 可以合并为一个完整的 providerLocation 字符串，如果后端这样处理的话
  // providerLocation?: string;
  providerAverageRating?: number; // 服务商的平均评分 (基于所有对其服务的评价)

}

export interface ServiceReservation {
  id: number
  serviceId: number
  petId: number
  userId: number
  reservationDate: string
  reservationTime: string
  status: 'pending' | 'confirmed' | 'completed' | 'cancelled'
  remark: string
}

// 获取所有可用服务
export function getActiveServices(params?: Record<string, any>): Promise<BackendResult<Service[]>> {
  return request<BackendResult<Service[]>>({ // 明确期望返回 BackendResult<Service[]>
    url: '/services/active',
    method: 'get',
    params
  });
}

// 获取所有服务（管理员）
export function getAllServices(): Promise<BackendResult<Service[]>> {
  return request<BackendResult<Service[]>>({
    url: '/admin/services/all',
    method: 'get'
  });
}

// 获取服务商的服务列表
export function getProviderServices() {
  return request({
    url: '/provider/services/my',
    method: 'get'
  })
}

// 添加服务
export function addService(data: Omit<Service, 'id' | 'reviewStatus' | 'rejectionReason'>) {
  return request({
    url: '/provider/services/add',
    method: 'post',
    data
  })
}

// 更新服务
export function updateService(data: Service) {
  return request({
    url: `/provider/services/${data.id}`,
    method: 'put',
    data
  })
}

// 删除服务
export function deleteService(id: number) {
  return request({
    url: `/provider/services/${id}`,
    method: 'delete'
  })
}

// 预约服务
export function reserveService(data: Omit<ServiceReservation, 'id' | 'status'>) {
  return request({
    url: '/service/reserve',
    method: 'post',
    data
  })
}

// 获取预约列表
export function getReservationList() {
  return request({
    url: '/service/reservation/list',
    method: 'get'
  })
}

// 更新预约状态
export function updateReservationStatus(id: number, status: ServiceReservation['status']) {
  return request({
    url: '/service/reservation/status',
    method: 'put',
    data: { id, status }
  })
}

/**
 * (管理员) 获取所有服务列表
 */
export function getAllServicesForAdmin(): Promise<BackendResult<Service[]>> {
  return request<BackendResult<Service[]>>({
    url: '/admin/services/all', // 确认后端路径
    method: 'get'
  })
}


/**
 * (管理员) 获取待审核服务列表
 */
export function getPendingServicesForAdmin(): Promise<BackendResult<Service[]>> {
  return request<BackendResult<Service[]>>({
    url: '/admin/services/pending', // 确认后端路径
    method: 'get'
  })
}

/**
 * (管理员) 批准服务
 * @param serviceId 服务 ID
 */
export function approveService(serviceId: number): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/admin/services/${serviceId}/approve`, // 确认后端路径
    method: 'put'
  })
}

// 定义拒绝原因的数据类型 (对应后端的 ServiceItemRejectionDTO)
interface RejectReasonPayload {
  reason: string;
}

/**
 * (管理员) 拒绝服务
 * @param serviceId 服务 ID
 * @param reason 拒绝原因
 */
export function rejectService(serviceId: number, reason: string): Promise<BackendResult<null>> {
  const payload: RejectReasonPayload = { reason };
  return request<BackendResult<null>>({
    url: `/admin/services/${serviceId}/reject`, // 确认后端路径
    method: 'put',
    data: payload // 将原因放在请求体中
  })
}

// 【新增】服务商删除自己的服务
export function deleteProviderService(id: number): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/provider/services/delete/${id}`,
    method: 'delete'
  });
}

// 【新增】服务商设置服务可用性 (上架/下架)
export function setServiceAvailability(id: number, available: boolean): Promise<BackendResult<Service>> {
  return request<BackendResult<Service>>({
    url: `/provider/services/${id}/availability`, // 后端接口路径
    method: 'put',
    data: { available } // 后端期望接收的数据格式
  });
}

export function getServiceDetailById(id: string): Promise<BackendResult<Service>> {
  return request<BackendResult<Service>>({
    url: `/services/${id}`, // 对应新后端接口
    method: 'get'
  });
}