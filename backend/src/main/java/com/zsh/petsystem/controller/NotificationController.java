package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.service.NotificationService;
import com.zsh.petsystem.annotation.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
// import java.util.List; // 如果返回List

@RestController
@RequestMapping("/notifications")
@CrossOrigin
public class NotificationController {

  @Autowired
  private NotificationService notificationService;

  @GetMapping("/my")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> getMyNotifications(
      @CurrentUser Long userId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) { // 移除了 read_status

    if (userId == null) {
      return ResponseEntity.status(401).body(Result.failed("用户未认证"));
    }
    try {
      Map<String, Object> paginatedResult = notificationService.getPaginatedUserNotifications(userId, page, size);
      return ResponseEntity.ok(Result.success(paginatedResult));
    } catch (Exception e) {
      // Log error
      return ResponseEntity.status(500).body(Result.failed("获取通知列表时发生服务器错误: " + e.getMessage()));
    }
  }
  // 移除了标记已读的API端点
}