package com.zsh.petsystem.service.impl;

import com.zsh.petsystem.dto.WebSocketMessageDTO; // 导入新的 DTO
import com.zsh.petsystem.service.WebNotificationService;
import com.zsh.petsystem.controller.NotificationWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class WebNotificationServiceImpl implements WebNotificationService {

    @Override
    public boolean sendWebBroadcast(String alertContent, String title, Map<String, String> extras) {
        WebSocketMessageDTO<Map<String, String>> message = new WebSocketMessageDTO<>(
                "system_broadcast", // type
                title, // title
                alertContent, // content
                System.currentTimeMillis(), // timestamp
                "info", // level
                extras // data
        );
        return sendMessageToAllWebClients(message);
    }

    /**
     * 新增：发送一个结构化的 WebSocketMessageDTO 给所有在线 Web 客户端
     * 
     * @param message 要发送的消息对象
     * @return 是否成功尝试发送
     */
    public <T> boolean sendMessageToAllWebClients(WebSocketMessageDTO<T> message) {
        if (message == null) {
            log.warn("尝试发送空的 WebSocket 消息。");
            return false;
        }
        try {
            NotificationWebSocketServer.sendObjectInfo(message); // 这里的 sendObjectInfo 会将对象序列化为JSON
            log.info("WebSocket 消息已发送给所有客户端: 类型='{}', 内容='{}'", message.getType(), message.getContent());
            return true;
        } catch (Exception e) {
            log.error("发送 WebSocket 消息时出错: {}", e.getMessage(), e);
            return false;
        }
    }
}