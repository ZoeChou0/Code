package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.AdminNotificationRequestDTO; // 导入新的统一 DTO
import com.zsh.petsystem.dto.WebSocketMessageDTO;
import com.zsh.petsystem.service.WebNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/notify")
@CrossOrigin
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotifyController {

    @Autowired
    private WebNotificationService webNotificationService;

    // 如果您还需要邮件功能，可以在这里保留 EmailApi 的注入和相关方法。

    /**
     * 管理员发送通知的统一接口 (包括活动信息、系统广播等)
     * 请求体 DTO: AdminNotificationRequestDTO
     */
    @PostMapping("/send") // 使用一个统一的发送路径，例如 /send
    public ResponseEntity<?> sendAdminNotification(@RequestBody AdminNotificationRequestDTO requestDTO) {
        // 基本的请求体验证
        if (requestDTO == null) {
            return ResponseEntity.badRequest().body(Result.failed("请求体不能为空"));
        }
        if (requestDTO.getContent() == null || requestDTO.getContent().isEmpty()) {
            return ResponseEntity.badRequest().body(Result.failed("通知内容不能为空"));
        }
        if (requestDTO.getType() == null || requestDTO.getType().isEmpty()) {
            // 如果类型是必需的，可以取消注释下一行
            // return ResponseEntity.badRequest().body(Result.failed("通知类型不能为空"));
            // 或者如果类型可以为空，则在构建 WebSocketMessageDTO 时提供一个默认类型
            log.warn("通知类型未在请求中指定，将使用默认类型。");
        }

        // 构建将通过 WebSocket 发送的 WebSocketMessageDTO
        WebSocketMessageDTO<Map<String, Object>> wsMessage = new WebSocketMessageDTO<>(
                requestDTO.getType() != null ? requestDTO.getType() : "generic_admin_message", // 如果前端没传type，给个默认
                requestDTO.getTitle(), // 通知标题
                requestDTO.getContent(), // 通知主要内容
                System.currentTimeMillis(), // 时间戳
                requestDTO.getLevel() != null ? requestDTO.getLevel() : "info", // 消息级别, 默认 "info"
                requestDTO.getData() // 附加数据
        );

        log.info("管理员尝试发送通知: Type='{}', Title='{}', Content='{}', Level='{}', Data={}",
                wsMessage.getType(),
                wsMessage.getTitle(),
                wsMessage.getContent(),
                wsMessage.getLevel(),
                wsMessage.getData());

        boolean success = webNotificationService.sendMessageToAllWebClients(wsMessage);

        if (success) {
            log.info("管理员通知 (Type: {}) 已成功发送。", wsMessage.getType());
            return ResponseEntity.ok(Result.success(null, "通知已成功发送"));
        } else {
            log.warn("管理员通知 (Type: {}) 发送失败。", wsMessage.getType());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failed("通知发送过程中发生错误"));
        }
    }
}