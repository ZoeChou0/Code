package com.zsh.petsystem.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AdminNotificationRequestDTO {
  private String type; // 例如 "activity_announcement", "system_broadcast", "urgent_alert"
  private String title;
  private String content;
  private String level; // "info", "success", "warning", "error" (可选, 后端可设默认值)
  private Map<String, Object> data; // 用于携带任何类型的附加数据
}