import { BackendResult } from '@/types/api';
import request from '@/utils/request';

/**
 * 代表基础订单结构的接口，通常用于创建或作为其他DTO的基础。
 */
export interface Order {
  id: number;
  userId: number;
  // 注意：serviceId 和 petId 可能不是 Order 实体本身的直接字段，
  // 它们通常通过 reservationId 关联。请根据您的后端 Order 实体调整。
  // 如果 Order 实体中确实有 serviceId 和 petId，则保留。
  // 否则，如果它们仅用于创建订单前的上下文，则在 OrderCreateDTO 中体现可能更合适。
  serviceId?: number; // 假设可能通过预约间接关联，设为可选
  petId?: number;     // 假设可能通过预约间接关联，设为可选
  amount: number;
  status: 'pending' | 'paid' | 'completed' | 'cancelled'; // 建议与后端状态值统一
  createdAt: string; // ISO datetime string
  payTime?: string;   // ISO datetime string
  completeTime?: string; // ISO datetime string
  reservationId?: number;
  alipayTradeNo?: string;
  // reservationServiceStartTime?: string; // 这个字段通常属于 Reservation，但在 OrderViewDTO 中为了方便显示而加入
}

/**
 * 用于在前端展示订单时，包含更丰富信息的视图对象。
 * 对应后端 OrderServiceImpl 中转换得到的 OrderViewDTO.java
 */
export interface OrderViewDTO {
  id: number;
  userId: number;
  reservationId?: number;
  amount: number;
  status: string; // 与后端 Order 实体的 status 保持一致
  createdAt: string; // ISO datetime string
  payTime?: string;   // ISO datetime string
  completeTime?: string; // ISO datetime string
  alipayTradeNo?: string;

  // 从关联的预约、宠物、服务中获取的附加详细信息
  petId?: number;
  serviceId?: number;
  petName?: string;
  serviceName?: string;
  reservationServiceStartTime?: string; // 预约的服务开始时间
  reviewId?: number;
}
export interface OrderAdminViewDTO {
  id: number;
  userId: number;
  userName?: string;
  reservationId?: number;
  petId?: number;
  petName?: string;
  serviceId?: number;
  serviceName?: string;
  amount: number;
  status: string;
  reservationServiceStartTime?: string; // ISO datetime string
  createdAt: string; // ISO datetime string
  updatedAt?: string; // ISO datetime string // 后端 OrderAdminViewDTO.java 有 updatedAt
  // 根据后端 DTO 添加其他需要的字段
}

// 通用分页数据结构接口
export interface PaginatedData<T> {
  records: T[];         // 当前页的记录列表
  total: number;        // 总记录数
  size: number;         // 每页数量
  current: number;      // 当前页码
  pages?: number;       // 总页数 (可选, MyBatis-Plus IPage 会有)
  // 其他可能的分页字段...
}

/**
 * 获取当前登录用户的订单列表 (返回 OrderViewDTO 以包含更多信息)
 * 后端 /orders/my 接口应该返回 OrderViewDTO 类型的列表。
 */
export function getUserOrderList(): Promise<BackendResult<OrderViewDTO[]>> {
  return request<BackendResult<OrderViewDTO[]>>({ // 确保返回类型是 OrderViewDTO[]
    url: '/orders/my',
    method: 'get'
  });
}

/**
 * 获取当前登录服务商的订单列表 (返回 OrderViewDTO)
 */
export function getProviderOrderList(): Promise<BackendResult<OrderViewDTO[]>> {
  return request<BackendResult<OrderViewDTO[]>>({ // 返回类型应为 OrderViewDTO[]
    url: '/orders/provider/list', // 后端接口路径
    method: 'get'
  });
}

/**
 * 获取所有订单列表（管理员）
 * @param params 可选的分页和筛选参数
 */
export function getAllOrderList(params?: {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
  [key: string]: any;
}): Promise<BackendResult<PaginatedData<OrderAdminViewDTO>>> { // <--- 修改这里
  return request<BackendResult<PaginatedData<OrderAdminViewDTO>>>({ // <--- 修改这里
    url: '/admin/orders',
    method: 'get',
    params
  });
}

/**
 * 定义创建订单时需要的数据结构。
 * Omit 用于从 Order 接口排除某些字段。
 * 'createTime' 在您的 Order 接口中没有，通常是 createdAt。
 */
export type OrderCreationData = Omit<Order, 'id' | 'status' | 'createdAt' | 'payTime' | 'completeTime' | 'alipayTradeNo' | 'reservationServiceStartTime'> & {
  // 如果创建订单时某些字段是必须的，可以在这里明确指出
  reservationId: number; // 通常从预约创建订单时，预约ID是关键
};

/**
 * 创建订单 (通常是从一个已确认的预约创建)
 * @param data 创建订单所需的数据
 */
export function createOrder(data: OrderCreationData): Promise<BackendResult<Order>> { // 返回创建后的 Order 对象
  return request<BackendResult<Order>>({
    url: '/orders/create', // 确认此接口是否用于基于DTO的通用创建
    method: 'post',
    data
  });
}

/**
 * 支付订单 (通常是跳转到支付网关，或调用后端接口获取支付参数)
 * @param id 订单ID
 * 返回类型未知，取决于后端如何处理支付，可能是HTML表单、支付URL或其他
 */
export function payOrder(id: number): Promise<BackendResult<any>> { // 返回类型设为 any，具体根据后端调整
  return request({
    url: `/orders/pay/${id}`, // 示例：后端处理支付逻辑或生成支付参数
    method: 'post'
  });
}

/**
 * (服务商或系统)标记订单为已完成
 * @param id 订单ID
 */
export function completeOrder(id: number): Promise<BackendResult<Order | null>> { // 后端可能返回更新后的订单或仅成功消息
  return request<BackendResult<Order | null>>({
    url: `/orders/complete/${id}`, // 确保此接口存在且权限正确
    method: 'post' // 或 'put'
  });
}

/**
 * 用户取消订单
 * @param id 订单ID
 */
export function cancelOrder(id: number): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/orders/cancel/${id}`, // 对应 OrderController 中的 POST /orders/cancel/{orderId}
    method: 'post' // 与后端控制器一致
  });
}

/**
 * 从预约创建订单时所需的数据。
 */
export interface OrderFromReservationData {
  reservationId: number;
  // 其他可能需要的字段，例如选择的支付方式等，根据后端接口定义
}

/**
 * 调用后端从预约创建订单的接口。
 * @param data 包含 reservationId
 */
export function createOrderFromReservation(data: OrderFromReservationData): Promise<BackendResult<Order>> {
  return request<BackendResult<Order>>({
    url: '/orders/from-reservation', // 对应 OrderController 中的 POST /orders/from-reservation
    method: 'post',
    data
  });
}