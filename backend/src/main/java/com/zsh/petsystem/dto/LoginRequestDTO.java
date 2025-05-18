package com.zsh.petsystem.dto;

import lombok.Data;
// Optional: Add validation annotations if needed
// import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequestDTO {

  // @NotBlank(message = "登录凭证不能为空")
  private String identifier; // This field will hold username, email, or phone

  // @NotBlank(message = "密码不能为空")
  private String password;
}