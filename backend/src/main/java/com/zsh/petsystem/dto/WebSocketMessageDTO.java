package com.zsh.petsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO<T> { // 使用泛型 T 来表示 data 的具体类型
  private String type;
  private String title;
  private String content;
  private long timestamp;
  private String level; // "info", "success", "warning", "error"
  private T data; // 具体的业务数据
}
