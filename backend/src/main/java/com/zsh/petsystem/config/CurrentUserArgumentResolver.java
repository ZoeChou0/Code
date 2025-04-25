package com.zsh.petsystem.config; // 或其他合适的包，如 'resolver'

import com.zsh.petsystem.annotation.CurrentUser;
import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.service.UserService;
import com.zsh.petsystem.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component // 声明为 Spring Bean
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserService userService; // 注入 UserService 以便在需要时获取完整的 User 对象

    // 检查此解析器是否支持给定的参数
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 检查参数是否带有 @CurrentUser 注解
        // 并且参数类型是 Long (用于 userId) 或 Users (用于完整对象)
        return parameter.getParameterAnnotation(CurrentUser.class) != null &&
               (parameter.getParameterType().isAssignableFrom(Long.class) ||
                parameter.getParameterType().isAssignableFrom(Users.class));
    }

    // 解析参数值
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            // 或者进行适当处理，可能返回 null 或抛出异常
            return null;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
             // 或者进行适当处理
            return null;
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            // 假设 JwtUtil 中有 extractUserId 方法
            Long userId = JwtUtil.extractUserId(token);

            // 如果 Controller 方法需要 Long 类型的 userId
            if (parameter.getParameterType().isAssignableFrom(Long.class)) {
                return userId;
            }

            // 如果 Controller 方法需要完整的 Users 对象
            if (parameter.getParameterType().isAssignableFrom(Users.class)) {
                // 你可能需要处理用户未找到的情况
                // 假设 UserService 有 getById 方法
                return userService.getById(userId);
            }

        } catch (Exception e) {
            // 处理 JWT 解析异常 (例如过期、签名无效等)
            // 记录错误日志，可能返回 null 或抛出异常
            System.err.println("从 Token 解析当前用户时出错: " + e.getMessage());
            return null; // 或者抛出适当的异常
        }

        return null; // 默认返回 null
    }
}