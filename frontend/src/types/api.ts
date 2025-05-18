

export interface BackendResult<T = any> { // 使用泛型 T 来表示 data 的具体类型
  code: number;
  message: string;
  data: T;
}

// 定义登录成功时 data 字段的具体类型
export interface LoginSuccessData {
  token: string;
}