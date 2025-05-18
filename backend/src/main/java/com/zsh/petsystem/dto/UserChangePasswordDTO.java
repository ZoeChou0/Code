package com.zsh.petsystem.dto;

import lombok.Data;
// 可以考虑添加 JSR 303 Validation 注解
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;

@Data
public class UserChangePasswordDTO {

  // @NotBlank(message = "当前密码不能为空")
  private String currentPassword; // 当前密码，用于验证

  // @NotBlank(message = "新密码不能为空")
  // @Size(min = 6, max = 20, message = "新密码长度必须在6到20位之间") // 示例：添加长度限制
  private String newPassword; // 新密码
}