// import request from '@/utils/request';
// import type { BackendResult } from '@/types/api';

// // 1. 定义前端需要发送的预约数据结构
// export interface ReservationData {
//   petId: number;
//   serviceId: number;
//   reservationDate: string; // 格式: 'YYYY-MM-DD'
//   reservationTime: string; // 格式: 'YYYY-MM-DDTHH:mm:ss' (或其他后端接受的格式)
//   // userNotes?: string; // 可选的备注 (如果后端DTO和Service支持)
// }

// // 2. 定义前端展示用的预约信息结构 (根据后端 Reservation 模型调整)
// export interface Reservation {
//   id: number;
//   userId: number;
//   petId: number;
//   serviceId: number;
//   providerId: number; // 新增
//   orderId?: number;    // 新增
//   reservationDate: string; // 'YYYY-MM-DD'
//   serviceStartTime: string; // ISO 格式 'YYYY-MM-DDTHH:mm:ss'
//   serviceEndTime?: string;   // ISO 格式 'YYYY-MM-DDTHH:mm:ss'
//   status: string; // PENDING_CONFIRMATION, CONFIRMED, AWAITING_PAYMENT, ...
//   amount: number; // 使用 number 类型
//   userNotes?: string;
//   providerNotes?: string;
//   cancellationReason?: string;
//   rejectionReason?: string; // 新增
//   createdAt: string; // ISO 格式
//   updatedAt: string; // ISO 格式
//   // --- 为了方便显示，可能需要从后端获取或前端补充 ---
//   serviceName?: string; // (需要后端支持或前端额外获取)
//   petName?: string;     // (需要后端支持或前端额外获取)
// }


// // 3. 创建预约的 API 函数
// export function createReservation(data: ReservationData): Promise<BackendResult<Reservation>> {
//   return request<BackendResult<Reservation>>({
//     url: '/reservations/add', // 对应 ReservationController 的 @PostMapping("/add")
//     method: 'post',
//     data // 将表单数据作为请求体发送
//   });
// }

// // 4. 获取我的预约列表的 API 函数
// export function getMyReservations(): Promise<BackendResult<Reservation[]>> {
//   return request<BackendResult<Reservation[]>>({
//     url: '/reservations/my', // 对应 ReservationController 的 @GetMapping("/my")
//     method: 'get'
//   });
// }

// // 5. 取消预约的 API 函数
// export function cancelReservation(id: number): Promise<BackendResult<null>> {
//   return request<BackendResult<null>>({
//     url: `/reservations/${id}/cancel`, // 对应 ReservationController 的 @PostMapping("/{id}/cancel")
//     method: 'post' // 或者 'put'/'delete'，根据后端设定
//   });
// }

// // 【新增】获取当前服务商收到的预约列表
// export function getProviderReservations(params?: { status?: string }): Promise<BackendResult<Reservation[]>> {
//   return request<BackendResult<Reservation[]>>({
//     url: '/provider/reservations', // **确认后端有此接口**
//     method: 'get',
//     params
//   });
// }

// // 【新增】服务商确认预约
// export function confirmReservationByProvider(id: number): Promise<BackendResult<Reservation>> {
//   return request<BackendResult<Reservation>>({
//     url: `/provider/reservations/${id}/confirm`, // **确认后端有此接口**
//     method: 'put'
//   });
// }

// // 【新增】服务商拒绝预约
// export function rejectReservationByProvider(id: number, reason?: string): Promise<BackendResult<Reservation>> {
//   return request<BackendResult<Reservation>>({
//     url: `/provider/reservations/${id}/reject`, // **确认后端有此接口**
//     method: 'put',
//     data: { rejectionReason: reason }
//   });
// }

// // 【新增】服务商标记服务完成
// export function completeReservationByProvider(id: number): Promise<BackendResult<Reservation>> {
//   return request<BackendResult<Reservation>>({
//     url: `/provider/reservations/${id}/complete`, // **确认后端有此接口**
//     method: 'put'
//   });
// }

// // 【新增】服务商主动取消已确认的预约 (如果与拒绝使用不同接口)
// // 注意：cancelOrRejectReservation 是我之前给的一个通用名，你需要根据实际后端接口拆分或命名
// export function cancelReservationByProvider(id: number, reason?: string): Promise<BackendResult<Reservation>> {
//   return request<BackendResult<Reservation>>({
//     url: `/provider/reservations/${id}/cancel-by-provider`, // **假设一个不同的后端接口**
//     method: 'put',
//     data: { cancellationReason: reason }
//   });
// }

import request from '@/utils/request';
import type { BackendResult } from '@/types/api';

// 1. 定义前端需要发送的预约数据结构 (这个可以保留，用于创建预约)
export interface ReservationData {
  petId: number;
  serviceId: number;
  reservationStartDate: string; // 'YYYY-MM-DD' (与后端实体对齐)
  reservationEndDate: string;   // 'YYYY-MM-DD' (与后端实体对齐)
  reservationTime: string;      // 'HH:mm:ss' (如果仅时间部分) 或完整日期时间字符串，取决于后端如何处理
  // 如果 reservationTime 实际上是 serviceStartTime，则应为完整日期时间
}

// 2. 定义前端展示用的通用预约信息结构 (主要用于用户视角或基础显示)
export interface Reservation {
  id: number;
  userId: number;
  petId: number;
  serviceId: number;
  providerId: number;
  orderId?: number;
  reservationStartDate: string; // 'YYYY-MM-DD'
  reservationEndDate?: string;   // 'YYYY-MM-DD'
  serviceStartTime: string;     // ISO 格式 'YYYY-MM-DDTHH:mm:ss'
  serviceEndTime?: string;       // ISO 格式 'YYYY-MM-DDTHH:mm:ss'
  status: string;
  amount: number; // 使用 number 类型
  userNotes?: string; // 用户备注
  providerNotes?: string; // 服务商备注
  cancellationReason?: string;
  rejectionReason?: string;
  createdAt: string; // ISO 格式
  updatedAt: string; // ISO 格式
  serviceName?: string;
  petName?: string;
}

// 3. 【新增/修改】服务商视角下的预约列表项接口 (对应后端的 ReservationProviderViewDTO)
export interface ReservationProviderView {
  id: number;
  userId: number;
  providerId: number;
  serviceId: number;
  petId: number;

  // 这些字段应与 ProviderReservationController 中 convertToProviderViewDto 方法实际填充的 DTO 字段一致
  // 并且与 ReservationList.vue 模板中使用的一致
  reservationStartDate: string; // 对应 reservation.reservationStartDate (LocalDate -> 'YYYY-MM-DD')
  reservationEndDate?: string;  // 对应 reservation.reservationEndDate
  serviceStartTime: string;     // 对应 reservation.serviceStartTime (LocalDateTime -> ISO string)
  serviceEndTime?: string;       // 对应 reservation.serviceEndTime

  status: string;
  remark?: string; // 通常这是用户的备注 (userNotes)
  rejectionReason?: string;
  cancellationReason?: string; // 虽然 DTO 中没有，但可以加上以防万一
  createdAt: string;
  updatedAt: string;

  // 从关联查询中填充的字段 (来自 ReservationProviderViewDTO)
  userName?: string;
  userPhone?: string;
  petName?: string;
  serviceName?: string;
  // servicePrice?: number; // 如果您的 DTO 有这个字段
}


// 4. 创建预约的 API 函数 (保持不变, 返回通用 Reservation)
export function createReservation(data: ReservationData): Promise<BackendResult<Reservation>> {
  return request<BackendResult<Reservation>>({
    url: '/reservations/add',
    method: 'post',
    data
  });
}

// 5. 获取我的预约列表的 API 函数 (保持不变, 返回通用 Reservation)
export function getMyReservations(): Promise<BackendResult<Reservation[]>> {
  return request<BackendResult<Reservation[]>>({
    url: '/reservations/my',
    method: 'get'
  });
}

// 6. 取消预约的 API 函数 (保持不变, 返回通用 null)
export function cancelReservation(id: number): Promise<BackendResult<null>> {
  return request<BackendResult<null>>({
    url: `/reservations/${id}/cancel`,
    method: 'post'
  });
}

// --- 服务商相关API函数，确保返回类型为 ReservationProviderView ---

export function getProviderReservations(params?: { status?: string }): Promise<BackendResult<ReservationProviderView[]>> {
  return request<BackendResult<ReservationProviderView[]>>({
    url: '/provider/reservations',
    method: 'get',
    params
  });
}

export function confirmReservationByProvider(id: number): Promise<BackendResult<ReservationProviderView>> {
  return request<BackendResult<ReservationProviderView>>({
    url: `/provider/reservations/${id}/confirm`,
    method: 'put'
  });
}

export function rejectReservationByProvider(id: number, reason?: string): Promise<BackendResult<ReservationProviderView>> {
  // 后端 ProviderReservationController 期望的是 Map<String, String> payload, { "rejectionReason": "..." }
  const payload: { rejectionReason?: string } = {};
  if (reason !== undefined && reason !== null) { // 确保 reason 存在才添加到 payload
    payload.rejectionReason = reason;
  }
  return request<BackendResult<ReservationProviderView>>({
    url: `/provider/reservations/${id}/reject`,
    method: 'put',
    data: payload // 使用构建的 payload
  });
}

export function completeReservationByProvider(id: number): Promise<BackendResult<ReservationProviderView>> {
  return request<BackendResult<ReservationProviderView>>({
    url: `/provider/reservations/${id}/complete`,
    method: 'put'
  });
}