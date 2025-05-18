package com.zsh.petsystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "chongleju1234567890chongleju1234567890"; // 至少256位bit (32字符)

    private static Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public static boolean verify(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET.getBytes())
                    .build()
                    .parseClaimsJws(token);
            return true; // token 合法
        } catch (JwtException e) {
            return false; // token 非法或过期
        }
    }

    public static String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1天
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSignKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token已过期");
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Token格式不支持");
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Token格式错误");
        } catch (SignatureException e) {
            throw new RuntimeException("Token签名验证失败");
        } catch (Exception e) {
            throw new RuntimeException("Token解析失败");
        }
    }

    public static String getUsernameFromToken(String token) {
        // return parseToken(token).getSubject();
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public static Long extractUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public static String extractUserRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
