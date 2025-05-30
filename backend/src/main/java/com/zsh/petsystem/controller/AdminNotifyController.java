package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.AdminNotificationRequestDTO;
import com.zsh.petsystem.dto.WebSocketMessageDTO;
import com.zsh.petsystem.entity.Notifications; // 导入 Notifications 实体
import com.zsh.petsystem.service.NotificationService; // 导入 NotificationService
import com.zsh.petsystem.service.WebNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; // 导入 LocalDateTime
import java.util.Map;

@RestController
@RequestMapping("/admin/notify")
@CrossOrigin
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotifyController {

    @Autowired
    private WebNotificationService webNotificationService;

    @Autowired // 注入 NotificationService
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendAdminNotification(@RequestBody AdminNotificationRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getContent() == null || requestDTO.getContent().isEmpty()) {
            return ResponseEntity.badRequest().body(Result.failed("通知内容不能为空。"));
        }

        WebSocketMessageDTO<Map<String, Object>> wsMessage = new WebSocketMessageDTO<>(
                requestDTO.getType() != null ? requestDTO.getType() : "generic_admin_message",
                requestDTO.getTitle(),
                requestDTO.getContent(),
                System.currentTimeMillis(),
                requestDTO.getLevel() != null ? requestDTO.getLevel() : "info",
                requestDTO.getData());

        boolean wsSent = webNotificationService.sendMessageToAllWebClients(wsMessage);

        // 将通知保存到数据库
        Notifications dbNotification = new Notifications();
        dbNotification.setType(wsMessage.getType());
        dbNotification.setTitle(wsMessage.getTitle());
        dbNotification.setContent(wsMessage.getContent());
        dbNotification.setLevel(wsMessage.getLevel());
        dbNotification.setData(wsMessage.getData());
        dbNotification.setIsBroadcast(true); // 管理员通知是广播
        dbNotification.setUserId(null); // 广播没有特定的用户
        dbNotification.setCreatedAt(LocalDateTime.now());
        dbNotification.setSentAt(LocalDateTime.now()); // 或者在 WebSocket 确认发送时设置

        try {
            notificationService.saveNotification(dbNotification); // 使用服务保存
            log.info("管理员通知 (类型: {}) 已保存到数据库，ID为: {}", dbNotification.getType(), dbNotification.getId());
        } catch (Exception e) {
            log.error("保存管理员通知 (类型: {}) 到数据库失败: {}", dbNotification.getType(), e.getMessage(), e);
            // 决定这是否应该导致整个操作失败
            // 目前，我们假设 WebSocket 发送失败对于立即错误响应更为关键
        }

        if (wsSent) {
            log.info("管理员通知 (类型: {}) 已通过 WebSocket 成功发送。", wsMessage.getType());
            return ResponseEntity.ok(Result.success(null, "通知已发送并保存。"));
        } else {
            log.warn("管理员通知 (类型: {}) WebSocket 发送失败。", wsMessage.getType());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failed("通知 WebSocket 发送失败。"));
        }
    }
}