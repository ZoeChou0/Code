package com.zsh.petsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProviderDTO {
  private Long id;
  private String name;
  private String email;
  private String phone;
  private String role;
  private String qualificationStatus;
  private String qualificationRejectionReason;
  private String addressLine1;
  private String city;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // 不包含敏感信息如密码等
}