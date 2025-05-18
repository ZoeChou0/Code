package com.zsh.petsystem.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateProfileDTO {
  // 只包含允许用户修改的字段
  private String name;
  private String phone;

  private String addressLine1;

  private String city;
  private String state;
  private String zipCode;

  private LocalDate birthday;
  private String profilePhotoUrl;
}