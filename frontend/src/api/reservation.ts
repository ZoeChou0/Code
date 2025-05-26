import request from '@/utils/request';
import type { BackendResult } from '@/types/api';

// 1. 定义前端需要发送的预约数据结构
export interface ReservationData {
  petId: number;
  serviceId: number;
  reservationDate: string; // 格式: 'YYYY-MM-DD'
  reservationTime: string; // 格式: 'YYYY-MM-DDTHH:mm:ss' (或其他后端接受的格式)
  // userNotes?: string; // 可选的备注 (如果后端DTO和Service支持)
}

// 2. 定义前端展示用的预约信息结构 (根据后端 Reservation 模型调整)
export interface Reservation {
  id: number;
  userId: number;
  petId: number;
  serviceId: number;
  providerId: number; // 新增
  orderId?: number;    // 新增
  reservationDate: string; // 'YYYY-MM-DD'
  serviceStartTime: string; // ISO 格式 'YYYY-MM-DDTHH:mm:ss'
  serviceEndTime?: string;   // ISO 格式 'YYYY-MM-DDTHH:mm:ss'
  status: string; // PENDING_CONFIRMATION, CONFIRMED, AWAITING_PAYMENT, ...
  amount: number; // 使用 number 类型
  userNotes?: string;
  providerNotes?: string;
  cancellationReason?: string;
  rejectionReason?: string; // 新增
  createdAt: string; // ISO 格式
  updatedAt: string; // ISO 格式
  // --- 为了方便显示，可能需要从后端获取或前端补充 ---
  serviceName?: string; // (需要后端支持或前端额外获取)
  petName?: string;     // (需要后端支持或前端额外获取)
}


// 3. 创建预约的 API 函数
export function createReservation(data: ReservationData): Promise<BackendResult<Reservation>> {
  return request<BackendResult<Reservation>>({
    url: '/reservations/add', // 对应 ReservationController 的 @PostMapping("/add")
    method: 'post',
    data // 将表单数据作为请求体发送
  });
}

// 4. 获取我的预约列表的 API 函数
export function getMyReservations(): Promise<BackendResult<Reservation[]>> {
  return request<BackendResult<Reservation[]>>({
    url: '/reservations/my', // 对应 ReservationController 的 @GetMapping("/my")
    method: 'get'
  });
}

// 5. 取消预约的 API 函数
export function cancelReservation(id: number): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/reservations/${id}/cancel`, // 对应 ReservationController 的 @PostMapping("/{id}/cancel")
    method: 'post' // 或者 'put'/'delete'，根据后端设定
  });
}

// 【新增】获取当前服务商收到的预约列表
export function getProviderReservations(params?: { status?: string }): Promise<BackendResult<Reservation[]>> {
  return request<BackendResult<Reservation[]>>({
    url: '/provider/reservations', // **确认后端有此接口**
    method: 'get',
    params
  });
}

// 【新增】服务商确认预约
export function confirmReservationByProvider(id: number): Promise<BackendResult<Reservation>> {
  return request<BackendResult<Reservation>>({
    url: `/provider/reservations/${id}/confirm`, // **确认后端有此接口**
    method: 'put'
  });
}

// 【新增】服务商拒绝预约
export function rejectReservationByProvider(id: number, reason?: string): Promise<BackendResult<Reservation>> {
  return request<BackendResult<Reservation>>({
    url: `/provider/reservations/${id}/reject`, // **确认后端有此接口**
    method: 'put',
    data: { rejectionReason: reason }
  });
}

// 【新增】服务商标记服务完成
export function completeReservationByProvider(id: number): Promise<BackendResult<Reservation>> {
  return request<BackendResult<Reservation>>({
    url: `/provider/reservations/${id}/complete`, // **确认后端有此接口**
    method: 'put'
  });
}

// 【新增】服务商主动取消已确认的预约 (如果与拒绝使用不同接口)
// 注意：cancelOrRejectReservation 是我之前给的一个通用名，你需要根据实际后端接口拆分或命名
export function cancelReservationByProvider(id: number, reason?: string): Promise<BackendResult<Reservation>> {
  return request<BackendResult<Reservation>>({
    url: `/provider/reservations/${id}/cancel-by-provider`, // **假设一个不同的后端接口**
    method: 'put',
    data: { cancellationReason: reason }
  });
}
