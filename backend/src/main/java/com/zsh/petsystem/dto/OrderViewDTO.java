package com.zsh.petsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor // 添加无参构造函数，方便使用
public class OrderViewDTO {

  private Long id;
  private Long userId;
  private Long reservationId;
  private BigDecimal amount;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime payTime;
  private LocalDateTime completeTime;
  private String alipayTradeNo; // 支付宝交易号 (如果已添加)

  private String userName;
  private String serviceName; // 服务名称
  private String petName; // 宠物名称
  private String reservationServiceStartTime; // 预约的服务开始时间 (转为字符串)

  private Long petId;
  private Long serviceId;
  private Long reviewId;
}