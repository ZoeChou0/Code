package com.zsh.petsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor // 添加无参构造函数，方便使用
public class OrderViewDTO {

  // --- 基础订单信息 (与 Order 模型对应) ---
  private Long id;
  private Long userId;
  private Long reservationId;
  private BigDecimal amount;
  private String status; // 建议后续统一为英文或枚举
  private LocalDateTime createdAt;
  private LocalDateTime payTime;
  private LocalDateTime completeTime;
  private String alipayTradeNo; // 支付宝交易号 (如果已添加)

  // --- 额外需要展示的信息 ---
  private String serviceName; // 服务名称
  private String petName; // 宠物名称
  private String reservationServiceStartTime; // 预约的服务开始时间 (转为字符串)
  // 可以根据需要添加更多字段，例如服务图片、宠物头像等
  // private String serviceImageUrl;
  // private String petPhotoUrl;
}