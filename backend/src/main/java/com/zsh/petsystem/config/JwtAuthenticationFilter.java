package com.zsh.petsystem.config;

import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.UserService;
import com.zsh.petsystem.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils; // 导入 StringUtils

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final UserService userService;

  public JwtAuthenticationFilter(@Lazy UserService userService) {
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String uri = request.getRequestURI();
    logger.info("JwtFilter processing request: {}", uri);

    // --- START: 修改白名单逻辑 ---
    if (uri.startsWith("/users/login")
        || uri.startsWith("/users/register")
        || uri.startsWith("/users/send-email-code")
        || uri.startsWith("/users/send-code")
        || uri.startsWith("/services/search")
        || uri.startsWith("/uploads")
        || uri.startsWith("/v3/api-docs")
        || uri.startsWith("/swagger-ui")
        || uri.startsWith("/services/active")
        || uri.equals("/alipay/notify")) {
      logger.info("Path {} is whitelisted in JwtAuthenticationFilter, proceeding with filter chain.", uri);
      filterChain.doFilter(request, response); // 调用过滤器链的下一个
      return; // 然后直接返回，不再执行后续的 Token 校验逻辑
    }

    if ("/users/info".equals(uri)) {
      logger.info("--- Debugging /users/info ---");
      String authHeader = request.getHeader("Authorization");
      logger.info("Authorization Header: {}", authHeader);
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String jwt = authHeader.substring(7);
        logger.info("Extracted Token: {}", jwt); // 注意：生产环境不应记录完整 token
        try {
          boolean isValid = JwtUtil.validateToken(jwt);
          logger.info("Token validation result: {}", isValid);
          if (isValid) {
            String userEmail = JwtUtil.getUsernameFromToken(jwt);
            logger.info("Email from Token: {}", userEmail);
            Users user = userService.findByEmail(userEmail);
            logger.info("User found from DB: {}",
                (user != null ? user.getEmail() + " | Role: " + user.getRole() : "null"));
            if (user != null) {
              // 检查 Security Context 是否已存在认证信息
              Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
              logger.info("Existing authentication before setting: {}",
                  (existingAuth != null ? existingAuth.getName() : "null"));

              List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                  new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
              logger.info("Authorities created: {}", authorities);

              UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                  userEmail, null, authorities);
              authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              logger.info("Attempting to set authentication in SecurityContextHolder...");
              SecurityContextHolder.getContext().setAuthentication(authToken);
              logger.info("Authentication set successfully in SecurityContextHolder.");

              // 再次检查是否设置成功
              Authentication afterSetAuth = SecurityContextHolder.getContext().getAuthentication();
              logger.info("Authentication after setting: {}",
                  (afterSetAuth != null
                      ? afterSetAuth.getName() + " | Authenticated: " + afterSetAuth.isAuthenticated()
                          + " | Authorities: " + afterSetAuth.getAuthorities()
                      : "null"));

            }
          }
        } catch (Exception e) {
          logger.error("Error during /users/info debug logging in filter: {}", e.getMessage());
        }
      } else {
        logger.warn("Authorization header missing or not Bearer for /users/info");
      }
      logger.info("--- End Debugging /users/info ---");
    }

    // ✅ 检查 Authorization Header 是否存在 Bearer Token
    final String authHeader = request.getHeader("Authorization");
    String jwt = null;
    String userEmail = null;

    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      jwt = authHeader.substring(7);
      try {
        userEmail = JwtUtil.getUsernameFromToken(jwt); // 提取用户名
      } catch (ExpiredJwtException e) {
        logger.warn("JWT Token expired");
        setUnauthorizedResponse(response, "Token expired");
        return;
      } catch (Exception e) {
        logger.warn("Invalid JWT Token");
        setUnauthorizedResponse(response, "Invalid token");
        return;
      }
    } else {
      logger.warn("Missing or invalid Authorization header");
      return;
    }

    // ✅ 设置用户身份到 Spring Security Context
    if (StringUtils.hasText(userEmail) &&
        SecurityContextHolder.getContext().getAuthentication() == null) {
      Users user = userService.findByEmail(userEmail);
      if (user != null && JwtUtil.validateToken(jwt)) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userEmail,
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" +
                user.getRole().toUpperCase())));
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        logger.info("Authenticated user: {}", userEmail);
      } else {
        logger.warn("User not found or token invalid");
        setUnauthorizedResponse(response, "Token invalid or user not found");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private void setUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"error\": \"" + message + "\"}");
  }
}