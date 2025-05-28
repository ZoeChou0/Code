package com.zsh.petsystem.dto;

import lombok.Data;
import java.util.Map;

@Data
public class JPushBroadcastRequestDTO {
  private String title; // 通知标题 (可选)
  private String content; // 通知内容 (必填)
  private Map<String, String> extras; // 额外参数 (可选)
}