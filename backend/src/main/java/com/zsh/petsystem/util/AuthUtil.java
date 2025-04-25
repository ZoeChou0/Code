package com.zsh.petsystem.util;

import jakarta.servlet.http.HttpServletRequest;

public class AuthUtil {

    //判断管理员
    public static boolean isAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        token = token.replace("Bearer ", "");
        String role = JwtUtil.extractUserRole(token);
        return "admin".equalsIgnoreCase(role);
    }

    //判断服务商
    public static boolean isProvider(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        token = token.replace("Bearer ", "");
        String role = JwtUtil.extractUserRole(token);
        return "provider".equalsIgnoreCase(role);

    }


}
