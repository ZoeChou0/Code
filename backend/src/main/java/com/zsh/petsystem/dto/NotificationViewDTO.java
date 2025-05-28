package com.zsh.petsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationViewDTO {
  private Long id;
  private Long userId; // 依然可以保留，用于标识是否是针对性通知
  private String type;
  private String title;
  private String content;
  private String level;
  private Map<String, Object> data;
  private Boolean isBroadcast;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime sentAt;
  // 移除了 isRead 和 readAt
}