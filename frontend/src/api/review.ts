import request from '@/utils/request';
import type { BackendResult } from '@/types/api';

// 1. 定义提交评价时发送的数据结构 (对应后端的 ReviewDTO)
export interface ReviewSubmitData {
  reservationId: number; // 必须有关联的预约ID
  rating: number;        // 评分 (例如 1-5)
  comment: string;       // 评价内容
}

// 2. 定义评价信息的数据结构 (对应后端的 Review 模型)
export interface Review {
  id: number;
  userId: number;
  serviceItemId: number; // 注意：后端模型是 serviceItemId
  rating: number;
  comment: string;
  createdAt: string; // ISO 格式日期字符串
  // 可能需要用户信息用于显示，如果后端返回的话
  // userName?: string;
  // userAvatar?: string;
}

// 3. 提交评价的 API 函数
export function submitReview(data: ReviewSubmitData): Promise<BackendResult<null>> { // 假设成功时不返回特定数据
  return request<BackendResult<null>>({
    url: '/reviews/submit', // 对应 ReviewController 的 @PostMapping("/submit")
    method: 'post',
    data
  });
}

// 4. 根据服务项ID获取评价列表的 API 函数
export function getReviewsByService(serviceItemId: number): Promise<BackendResult<Review[]>> {
  return request<BackendResult<Review[]>>({
    url: `/reviews/service/${serviceItemId}`, // 对应 ReviewController 的 @GetMapping("/service/{serviceItemId}")
    method: 'get'
  });
}

// 5. 根据服务项ID获取平均评分的 API 函数
export function getAverageRatingByService(serviceItemId: number): Promise<BackendResult<number>> { // 假设返回平均分数字
  return request<BackendResult<number>>({
    url: `/reviews/service/${serviceItemId}/average`, // 对应 ReviewController 的 @GetMapping("/service/{serviceItemId}/average")
    method: 'get'
  });
}

// 可选：如果需要根据预约ID获取评价 (后端目前没有此接口)
// export function getReviewByReservation(reservationId: number): Promise<BackendResult<Review | null>> { ... }