package com.zsh.petsystem.config;

import com.zsh.petsystem.common.Result; // 导入你的 Result 类
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity; // 导入 ResponseEntity
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.zsh.petsystem.controller")
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    Class<?> parameterType = returnType.getParameterType();
    // 如果 Controller 方法直接返回 Result, ResponseEntity, 或者 String，则本 Advice 不进行处理
    return !(parameterType.isAssignableFrom(Result.class) ||
        parameterType.isAssignableFrom(ResponseEntity.class) ||
        parameterType.isAssignableFrom(String.class)); // <-- 添加对 String 的排除
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request, ServerHttpResponse response) {
    // 因为 supports 方法已经排除了 String, Result, ResponseEntity，
    // 所以这里的 body 理论上是其他需要包装的类型
    if (body == null) {
      return Result.success(null);
    }
    // 对于所有其他类型，都包装成 Result
    return Result.success(body);
  }
}