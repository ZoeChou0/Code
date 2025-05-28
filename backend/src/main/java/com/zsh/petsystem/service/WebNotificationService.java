package com.zsh.petsystem.service;

import com.zsh.petsystem.dto.WebSocketMessageDTO; // 导入
import java.util.Map;

public interface WebNotificationService {
    boolean sendWebBroadcast(String alertContent, String title, Map<String, String> extras);

    <T> boolean sendMessageToAllWebClients(WebSocketMessageDTO<T> message); // 新增
}