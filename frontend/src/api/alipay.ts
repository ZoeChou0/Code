import request from '@/utils/request';
import type { BackendResult } from '@/types/api'; // 假设你有这个类型

export interface AliPayReq {
  outTradeNo: string; // 对应后端的 Order ID
}

// 发起支付宝PC网页支付 - **修改为 POST**
export function initiatePcPayment(data: AliPayReq): Promise<BackendResult<string>> { // 假设成功时 data 是 HTML 字符串或其他需要前端处理的内容
  return request<BackendResult<string>>({

    url: '/alipay/pc',       // **新 URL (POST)**
    method: 'post',          // **改为 POST**
    data                     // **使用 data 传递请求体**
  });
}

// 查询支付状态接口 (如果需要前端轮询)
export function queryPayStatus(params: { outTradeNo?: string; tradeNo?: string }): Promise<BackendResult<any>> {
  return request<BackendResult<any>>({
    url: '/alipay/queryPaymentStatus',
    method: 'get',
    params
  });

}