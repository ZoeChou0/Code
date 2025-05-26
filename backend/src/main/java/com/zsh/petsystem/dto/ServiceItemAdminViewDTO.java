package com.zsh.petsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceItemAdminViewDTO {
  private Long id;
  private Long providerId;
  private String providerName; // 可以考虑从 Users 表关联查询得到
  private String name;
  private String description;
  private BigDecimal price;
  private Integer duration;
  private Integer dailyCapacity;
  private String reviewStatus;
  private String rejectionReason;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}