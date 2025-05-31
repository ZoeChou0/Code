package com.zsh.petsystem.dto;

import lombok.Data;
import java.time.LocalDate; // 新增导入 LocalDate
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
  private String state; // 对应 Users 实体的 state
  private String zipCode; // 对应 Users 实体的 zipCode
  private LocalDate birthday; // <<--- 新增：对应 Users 实体的 birthday (LocalDate 会被序列化为 "YYYY-MM-DD" 字符串)
  private String profilePhotoUrl; // 对应 Users 实体的 profilePhotoUrl
  private String status; // <<--- 新增：对应 Users 实体的 status (账号状态，如 active, banned)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}