import request from '@/utils/request';
import type { BackendResult } from '@/types/api'; //

// 与后端 WebSocketMessageDTO 或数据库存储结构对应的通知接口
export interface NotificationItem {
  id: number;
  type: string;
  title?: string;
  content: string;
  level?: 'info' | 'success' | 'warning' | 'error';
  data?: Record<string, any>;
  parsedData?: any; // 前端解析后的data
  created_at: string; // ISO 格式日期字符串
}

// 定义后端返回的分页数据结构
export interface PaginatedNotifications {
  items: NotificationItem[];
  total: number;
  currentPage: number;
  pageSize: number;
  totalPages: number;
}

// 获取当前用户的通知列表 (分页)
export function getMyNotifications(params: { page?: number, size?: number }): Promise<BackendResult<PaginatedNotifications>> { // <--- 修改这里的泛型
  return request({
    url: '/notifications/my',
    method: 'get',
    params,
  });
}