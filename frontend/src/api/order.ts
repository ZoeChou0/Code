import { BackendResult } from '@/types/api'
import request from '@/utils/request'

export interface Order {
  id: number
  userId: number
  serviceId: number
  petId: number
  amount: number
  status: 'pending' | 'paid' | 'completed' | 'cancelled'
  createdAt: string
  payTime?: string
  completeTime?: string
  reservationId?: number;
  alipayTradeNo?: string;
  reservationServiceStartTime?: string;
}

// 获取用户订单列表
export function getUserOrderList() {
  return request({
    url: '/orders/my',
    method: 'get'
  })
}

// 获取服务商订单列表
export function getProviderOrderList() {
  return request({
    url: '/orders/provider/list',
    method: 'get'
  })
}

// 获取所有订单列表（管理员）
export function getAllOrderList(params?: { page?: number; size?: number; keyword?: string;[key: string]: any }): Promise<BackendResult<Order[]>> {
  return request<BackendResult<Order[]>>({
    url: '/admin/orders',
    method: 'get',
    params
  });
}
// 创建订单
export function createOrder(data: Omit<Order, 'id' | 'status' | 'createTime' | 'payTime' | 'completeTime'>) {
  return request({
    url: '/orders/create',
    method: 'post',
    data
  })
}

// 支付订单
export function payOrder(id: number) {
  return request({
    url: `/orders/pay/${id}`,
    method: 'post'
  })
}

// 完成订单
export function completeOrder(id: number) {
  return request({
    url: `/orders/complete/${id}`,
    method: 'post'
  })
}

// 取消订单
export function cancelOrder(id: number): Promise<BackendResult<null>> {
  return request({
    url: `/orders/cancel/${id}`,
    method: 'post'
  })
}

// 假设后端需要 reservationId 来创建订单
export interface OrderFromReservationData {
  reservationId: number;
  // 其他可能需要的字段...
}

// 调用后端从预约创建订单的接口
export function createOrderFromReservation(data: OrderFromReservationData): Promise<BackendResult<Order>> {
  return request<BackendResult<Order>>({
    // !! URL 需要与后端新接口匹配 !!
    url: '/orders/from-reservation', // 假设的后端接口路径
    method: 'post',
    data
  });
}