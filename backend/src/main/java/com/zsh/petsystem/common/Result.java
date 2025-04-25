package com.zsh.petsystem.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应结果封装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;      // 响应状态码，如 200 成功，400 失败
    private String message;    // 提示信息
    private T data;            // 响应数据

    // 成功返回
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(200, message, data);
    }

    // 失败返回
    public static <T> Result<T> failed() {
        return new Result<>(400, "failed", null);
    }

    public static <T> Result<T> failed(String message) {
        return new Result<>(400, message, null);
    }

    public static <T> Result<T> failed(T data, String message) {
        return new Result<>(400, message, data);
    }
}