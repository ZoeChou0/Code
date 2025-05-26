package com.zsh.petsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceItemDTO {
  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private Integer duration;
  private Integer dailyCapacity;
  private String category;
  private String reviewStatus;
  private String rejectionReason;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // 不包含providerId等敏感信息
}