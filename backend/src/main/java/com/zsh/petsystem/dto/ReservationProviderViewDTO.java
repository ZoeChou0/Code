package com.zsh.petsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal; // 如果你的 Reservation 或 ServiceItem 中有价格，并且你想显示
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat; // 用于日期时间格式化

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationProviderViewDTO {

  // 从 Reservation 实体直接复制的字段
  private Long id;
  private Long userId; // 预约用户ID
  private Long providerId; // 服务商ID (通常是当前登录的服务商)
  private Long serviceId; // 服务项ID
  private Long petId; // 宠物ID

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate reservationDate; // 预约日期

  @JsonFormat(pattern = "HH:mm:ss")
  private LocalTime reservationTime; // 预约时间 (或者你可以用 String 如果前端传来的是字符串)

  private String status; // 预约状态 (e.g., PENDING, CONFIRMED, COMPLETED, CANCELLED, REJECTED)
  private String remark; // 用户备注
  private String rejectionReason; // 服务商拒绝理由 (如果适用)

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt; // 预约创建时间

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt; // 预约更新时间

  // --- 新增的、通过关联查询填充的字段 ---
  private String userName; // 预约用户的名称
  private String userPhone; // (可选) 预约用户的联系电话 (注意隐私)
  private String petName; // 预约宠物的名称
  private String serviceName; // 预约服务的名称
  private BigDecimal servicePrice; // (可选) 服务的价格 (如果需要显示)

  // 你可以根据需要添加更多字段，例如：
  // private String petSpecies;
  // private String userAvatarUrl;
}
