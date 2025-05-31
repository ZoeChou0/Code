package com.zsh.petsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItemDetailDTO {
  // --- ServiceItem Fields ---
  private Long id;
  private String name;
  private String description;
  private Double price;
  private Integer duration;
  private String category;
  private Long providerId;
  private Integer dailyCapacity;
  private String requiredVaccinations;
  private Boolean requiresNeutered;
  private Integer minAge;
  private Integer maxAge;
  private String temperamentRequirements;
  private String prohibitedBreeds;

  // --- Added Provider Fields ---
  private String providerName;
  private String providerProfilePhotoUrl;
  private String providerAddressLine1; // 地址行1
  private String providerCity; // 城市
  private String providerState; // 省份/州
  // 您可以选择合并地址为一个字段，如果后端或前端需要
  // private String providerFullAddress;
  private Double providerAverageRating; // 服务商/此服务的平均评分 (请根据实际计算逻辑确认)
}