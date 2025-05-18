package com.zsh.petsystem.config;

import com.zsh.petsystem.service.UserService;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

  // 1. 密码加密器
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 2. 把 JwtAuthenticationFilter 注册成 Bean，参数由 Spring 注入 UserService
  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(UserService userService) {
    return new JwtAuthenticationFilter(userService);
  }

  // 3. 主过滤链，只注入 HttpSecurity 和上面那个 jwtAuthenticationFilter
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http
        // 禁用 CSRF
        .csrf(AbstractHttpConfigurer::disable)
        // 启用 CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // 不使用 session，走无状态的 JWT
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // 授权配置
        .authorizeHttpRequests(auth -> auth
            .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
            .requestMatchers(
                "/users/login",
                "/users/register",
                "/users/send-email-code",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/uploads/**",
                "/swagger-ui/**",
                "/services/active",
                "/alipay/notify")
            .permitAll()
            .anyRequest().authenticated())
        // 关闭 Spring Security 自带的登录页和 BasicAuth
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable);

    // 把自定义的 JWT 过滤器加到 Spring 原生的 UsernamePasswordAuthenticationFilter 之前
    http.addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // 4. 全局 CORS 策略
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/**", config);
    return src;
  }
}