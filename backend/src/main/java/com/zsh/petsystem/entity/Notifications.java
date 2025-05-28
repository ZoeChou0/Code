package com.zsh.petsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler; // 用于处理JSON字段
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "notifications", autoResultMap = true) // autoResultMap = true 用于配合 TypeHandler
public class Notifications {

  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("user_id")
  private Integer userId; // 与您的 notifications 表定义中的 user_id INT NULL 对应

  private String type;
  private String title;
  private String content;
  private String level;

  // 如果 data_json 在数据库中是 JSON 类型或 TEXT 类型存储JSON字符串
  // 使用 JacksonTypeHandler (MyBatis-Plus 提供) 可以自动转换 Map<String, Object> 和 JSON 字符串
  @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
  private Map<String, Object> data;

  @TableField("is_broadcast")
  private Boolean isBroadcast;

  @TableField("created_at") // 假设数据库自动填充或您在插入时设置
  private LocalDateTime createdAt;

  @TableField("sent_at")
  private LocalDateTime sentAt;
}