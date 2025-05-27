package com.zsh.petsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class OrderAdminViewDTO {
  private Long id;
  private Long userId;
  private String userName; // 关联查询：用户名
  private Long reservationId;
  private Long petId; // 从 Reservation 获取
  private String petName; // 关联查询：宠物名
  private Long serviceId; // 从 Reservation 获取
  private String serviceName;// 关联查询：服务名
  private BigDecimal amount;
  private String status;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime reservationServiceStartTime; // 从 Reservation 获取

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  // 可以添加更多管理员想看到的字段，例如支付时间、支付方式等
  // private String paymentMethod;
  // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  // private LocalDateTime paymentTime;
}